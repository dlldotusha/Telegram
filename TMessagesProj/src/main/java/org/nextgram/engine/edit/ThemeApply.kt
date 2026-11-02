package org.nextgram.engine.edit

import android.view.View
import android.view.ViewGroup
import org.nextgram.engine.EngineColors
import org.nextgram.engine.NextEngine
import org.telegram.messenger.FileLog
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.ActionBar.ThemeColors
import org.telegram.ui.LaunchActivity

/**
 * Изменение цвета темы так, чтобы его подхватило всё приложение:
 * текущий экран — сразу через его ThemeDescription, статические кисти Telegram —
 * через Theme.refreshThemeColors, экраны в стеке — пересборкой при выходе из режима.
 */
internal object ThemeApply {

    private var changedThisSession = false

    fun color(key: Int): Int = Theme.getColor(key)

    /** argb == null — вернуть цвет темы. */
    fun setColor(key: Int, argb: Int?) {
        val name = ThemeColors.getStringName(key) ?: return
        val themeKey = EngineColors.themeKey()
        NextEngine.edit { it.setThemeColor(themeKey, name, argb) }
        changedThisSession = true
        try {
            LaunchActivity.getLastFragment()?.themeDescriptions?.forEach { d ->
                if (d != null && d.currentKey == key) d.setColor(d.getSetColor(), false, false)
            }
        } catch (e: Throwable) {
            FileLog.e(e)
        }
        try {
            Theme.refreshThemeColors(false, true)
        } catch (e: Throwable) {
            FileLog.e(e)
        }
        EditMode.activity()?.window?.decorView?.let { invalidateAll(it) }
    }

    /** При выходе из режима: пересобрать экраны в стеке, чтобы и они взяли новые цвета. */
    fun onEditModeExit() {
        if (!changedThisSession) return
        changedThisSession = false
        try {
            LaunchActivity.instance?.actionBarLayout?.rebuildAllFragmentViews(false, false)
        } catch (e: Throwable) {
            FileLog.e(e)
        }
    }

    private fun invalidateAll(v: View) {
        v.invalidate()
        if (v is ViewGroup) for (i in 0 until v.childCount) invalidateAll(v.getChildAt(i))
    }
}
