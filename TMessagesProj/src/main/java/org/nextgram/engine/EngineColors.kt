package org.nextgram.engine

import org.nextgram.engine.core.Colors
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.ActionBar.ThemeColors

/**
 * Базовый уровень кастомизации: переопределение цветовых ключей темы.
 * Вызывается из Theme.getColor, поэтому на горячем пути здесь только
 * сравнение пары полей и чтение из массива.
 *
 * Цвета хранятся отдельно для каждой темы (и её акцента): цвет, подобранный
 * под тёмную тему, не должен ломать светлую.
 */
object EngineColors {

    private class Snapshot(
        val theme: Any?,
        val accent: Int,
        val generation: Int,
        val present: BooleanArray,
        val colors: IntArray,
    )

    @Volatile
    private var snapshot: Snapshot? = null

    /** Ключ темы в конфиге. */
    @JvmStatic
    fun themeKey(): String {
        val t = Theme.getActiveTheme() ?: return "default"
        val accent = t.currentAccentId
        return if (accent != 0) "${t.key}#$accent" else t.key
    }

    /** Хук в Theme.getColor: переопределён ли ключ. */
    @JvmStatic
    fun has(key: Int): Boolean {
        val s = current() ?: return false
        return key >= 0 && key < s.present.size && s.present[key]
    }

    @JvmStatic
    fun get(key: Int): Int = snapshot?.colors?.get(key) ?: 0

    /** Переопределённый цвет ключа или null. */
    @JvmStatic
    fun override(key: Int): Int? = if (has(key)) get(key) else null

    private fun current(): Snapshot? {
        if (!NextEngine.isReady) return null
        val t = Theme.getActiveTheme()
        val accent = t?.currentAccentId ?: 0
        val gen = NextEngine.generation
        val s = snapshot
        if (s != null && s.theme === t && s.accent == accent && s.generation == gen) return s
        return build(t, accent, gen).also { snapshot = it }
    }

    private fun build(t: Theme.ThemeInfo?, accent: Int, gen: Int): Snapshot {
        val n = maxOf(Theme.colorsCount, 1)
        val present = BooleanArray(n)
        val colors = IntArray(n)
        for ((name, value) in NextEngine.config().themeColors(themeKey())) {
            val key = ThemeColors.stringKeyToInt(name)
            val argb = Colors.parse(value)
            if (key in 0 until n && argb != null) {
                present[key] = true
                colors[key] = argb
            }
        }
        return Snapshot(t, accent, gen, present, colors)
    }
}
