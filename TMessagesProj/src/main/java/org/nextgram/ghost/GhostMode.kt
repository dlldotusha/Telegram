package org.nextgram.ghost

import android.content.Context
import android.content.SharedPreferences
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.tgnet.tl.TL_account
import org.telegram.tgnet.tl.TL_stories

/**
 * Режим призрака: не отправлять на сервер то, по чему другие видят твою активность.
 *
 * Работает на уровне запросов: хук в ConnectionsManager.sendRequestInternal
 * спрашивает [intercept]. Запрос либо не отправляется вовсе (колбэк не
 * вызывается — так интерфейс не получает «ответ» с чужими pts), либо
 * переписывается (статус «в сети» → «не в сети»).
 *
 * Правила Telegram API (п. 1.4) прямо запрещают подмену статусов прочтения.
 * Со своим api_id это риск отзыва ключа. По умолчанию выключено.
 *
 * Чего режим не может: сервер сам помечает чат прочитанным, когда ты
 * отправляешь в него сообщение.
 */
object GhostMode {

    private const val PREFS = "nextgram_ghost"

    const val KEY_ENABLED = "enabled"
    const val KEY_READ = "hide_read"
    const val KEY_ONLINE = "hide_online"
    const val KEY_TYPING = "hide_typing"
    const val KEY_STORIES = "hide_stories"

    @Volatile private var loaded = false
    @Volatile private var enabled = false
    @Volatile private var hideRead = true
    @Volatile private var hideOnline = true
    @Volatile private var hideTyping = true
    @Volatile private var hideStories = true

    private fun prefs(): SharedPreferences =
        ApplicationLoader.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun ensureLoaded() {
        if (loaded || ApplicationLoader.applicationContext == null) return
        val p = prefs()
        enabled = p.getBoolean(KEY_ENABLED, false)
        hideRead = p.getBoolean(KEY_READ, true)
        hideOnline = p.getBoolean(KEY_ONLINE, true)
        hideTyping = p.getBoolean(KEY_TYPING, true)
        hideStories = p.getBoolean(KEY_STORIES, true)
        loaded = true
    }

    @JvmStatic
    fun get(key: String): Boolean {
        ensureLoaded()
        return when (key) {
            KEY_ENABLED -> enabled
            KEY_READ -> hideRead
            KEY_ONLINE -> hideOnline
            KEY_TYPING -> hideTyping
            KEY_STORIES -> hideStories
            else -> false
        }
    }

    @JvmStatic
    fun set(key: String, value: Boolean) {
        ensureLoaded()
        prefs().edit().putBoolean(key, value).apply()
        when (key) {
            KEY_ENABLED -> enabled = value
            KEY_READ -> hideRead = value
            KEY_ONLINE -> hideOnline = value
            KEY_TYPING -> hideTyping = value
            KEY_STORIES -> hideStories = value
        }
        if (value && enabled && hideOnline && (key == KEY_ENABLED || key == KEY_ONLINE)) goOfflineNow()
    }

    /** Сразу показать «не в сети» во всех аккаунтах, не дожидаясь очередного пинга статуса. */
    private fun goOfflineNow() {
        for (a in 0 until UserConfig.MAX_ACCOUNT_COUNT) {
            if (!UserConfig.getInstance(a).isClientActivated) continue
            ConnectionsManager.getInstance(a).sendRequest(TL_account.updateStatus().apply { offline = true }, null)
        }
    }

    /**
     * Хук из ConnectionsManager. true — запрос не отправлять.
     * Вызывается на stageQueue для каждого запроса, поэтому только проверки полей.
     */
    @JvmStatic
    fun intercept(request: TLObject?): Boolean {
        if (!loaded) ensureLoaded()
        if (!enabled || request == null) return false
        if (hideOnline && request is TL_account.updateStatus) {
            request.offline = true
            return false
        }
        return hideRead && isReadReceipt(request) ||
            hideTyping && (request is TLRPC.TL_messages_setTyping || request is TLRPC.TL_messages_setEncryptedTyping) ||
            hideStories && (request is TL_stories.TL_stories_readStories || request is TL_stories.TL_stories_incrementStoryViews)
    }

    private fun isReadReceipt(r: TLObject) =
        r is TLRPC.TL_messages_readHistory ||
            r is TLRPC.TL_channels_readHistory ||
            r is TLRPC.TL_messages_readMessageContents ||
            r is TLRPC.TL_channels_readMessageContents ||
            r is TLRPC.TL_messages_readEncryptedHistory ||
            r is TLRPC.TL_messages_readDiscussion
}
