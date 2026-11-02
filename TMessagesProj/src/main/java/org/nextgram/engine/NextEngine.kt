package org.nextgram.engine

import org.nextgram.engine.core.ConfigCodec
import org.nextgram.engine.core.ResolvedElement
import org.nextgram.engine.core.StyleConfig
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.FileLog
import org.telegram.messenger.Utilities
import java.io.File

/**
 * NextEngine: хранит конфиг кастомизации, отдаёт итоговые стили элементов
 * и сохраняет правки на диск.
 *
 * Все чтения стиля идут через [resolve] — он кэширует результат каскада
 * на чат, так что хуки в отрисовке почти ничего не стоят.
 */
object NextEngine {

    fun interface Editor {
        fun edit(config: StyleConfig)
    }

    private const val FILE_NAME = "nextengine.json"

    /** Членство чата в папках и категория могут меняться — кэш живёт недолго. */
    private const val CACHE_TTL_MS = 30_000L

    @Volatile
    private var config: StyleConfig? = null

    /** Увеличивается при каждой правке; по нему инвалидируются кэши. */
    @Volatile
    @JvmStatic
    var generation = 0
        private set

    private val listeners = ArrayList<Runnable>()

    private class CacheEntry(val value: ResolvedElement, val generation: Int, val time: Long)

    private val cache = HashMap<String, CacheEntry>()
    private val saveRunnable = Runnable { saveNow() }

    /** Конфиг только для чтения. Менять — через [edit]. */
    @JvmStatic
    fun config(): StyleConfig {
        config?.let { return it }
        synchronized(this) {
            return config ?: load().also { config = it }
        }
    }

    /** Можно ли уже читать конфиг (контекст приложения создан). */
    internal val isReady: Boolean
        get() = config != null || ApplicationLoader.applicationContext != null

    /** Правка конфига. Только с UI-потока. */
    @JvmStatic
    fun edit(editor: Editor) {
        val next = config().copy()
        editor.edit(next)
        config = next
        generation++
        synchronized(cache) { cache.clear() }
        Utilities.globalQueue.cancelRunnable(saveRunnable)
        Utilities.globalQueue.postRunnable(saveRunnable, 300)
        ArrayList(listeners).forEach { it.run() }
    }

    @JvmStatic
    fun addListener(r: Runnable) {
        listeners.add(r)
    }

    @JvmStatic
    fun removeListener(r: Runnable) {
        listeners.remove(r)
    }

    /** Стиль элемента в чате. dialogId == 0 — элемент вне чатов, только global. */
    @JvmStatic
    fun resolve(account: Int, dialogId: Long, element: String): ResolvedElement {
        if (!isReady) return ResolvedElement.EMPTY
        val c = config()
        if (c.scopes.isEmpty()) return ResolvedElement.EMPTY
        val key = "$account:$dialogId:$element"
        val now = System.currentTimeMillis()
        val gen = generation
        synchronized(cache) {
            val e = cache[key]
            if (e != null && e.generation == gen && now - e.time < CACHE_TTL_MS) return e.value
        }
        val value = c.resolve(ScopeResolver.chain(account, dialogId), element)
        synchronized(cache) {
            if (cache.size > 2000) cache.clear()
            cache[key] = CacheEntry(value, gen, now)
        }
        return value
    }

    /** Сбросить кэш каскада (например, после изменения папок). */
    @JvmStatic
    fun invalidateCache() {
        synchronized(cache) { cache.clear() }
    }

    private fun file() = File(ApplicationLoader.getFilesDirFixed(), FILE_NAME)

    private fun load(): StyleConfig {
        val f = file()
        if (!f.exists()) return StyleConfig()
        return try {
            ConfigCodec.fromJson(f.readText(Charsets.UTF_8))
        } catch (e: Throwable) {
            // Не теряем битый файл молча: откладываем рядом, чтобы можно было разобраться.
            FileLog.e(e)
            f.renameTo(File(f.parentFile, "$FILE_NAME.broken"))
            StyleConfig()
        }
    }

    private fun saveNow() {
        val c = config ?: return
        val f = file()
        val tmp = File(f.parentFile, "$FILE_NAME.tmp")
        try {
            tmp.writeText(ConfigCodec.toJson(c), Charsets.UTF_8)
        } catch (e: Throwable) {
            FileLog.e(e)
            return
        }
        if (!tmp.renameTo(f)) FileLog.e("nextengine: can't replace $f")
    }

    /** Экспорт конфига (для обмена темами со своими). */
    @JvmStatic
    fun exportJson(): String = ConfigCodec.toJson(config())

    /** Импорт конфига целиком. Бросает исключение на битом JSON, текущий конфиг не трогается. */
    @JvmStatic
    fun importJson(json: String) {
        val imported = ConfigCodec.fromJson(json)
        AndroidUtilities.runOnUIThread {
            config = imported
            edit { }
        }
    }
}
