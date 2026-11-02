package org.nextgram.engine.edit

import android.view.View
import android.view.ViewGroup

/** Поиск самого глубокого видимого view под точкой экрана и обход видимых view. */
internal object HitTest {

    fun deepestAt(root: View, screenX: Int, screenY: Int, exclude: View?): View? =
        find(root, screenX, screenY, exclude, IntArray(2))

    private fun find(v: View?, x: Int, y: Int, exclude: View?, loc: IntArray): View? {
        if (v == null || v === exclude || v.visibility != View.VISIBLE || v.alpha < 0.02f) return null
        v.getLocationOnScreen(loc)
        if (x < loc[0] || y < loc[1] || x >= loc[0] + v.width || y >= loc[1] + v.height) return null
        if (v is ViewGroup) {
            // Последний ребёнок рисуется поверх — проверяем с конца.
            for (i in v.childCount - 1 downTo 0) {
                find(v.getChildAt(i), x, y, exclude, loc)?.let { return it }
            }
        }
        return v
    }

    fun forEachVisible(v: View?, exclude: View?, visitor: (View) -> Unit) {
        if (v == null || v === exclude || v.visibility != View.VISIBLE || !v.isAttachedToWindow) return
        visitor(v)
        if (v is ViewGroup) {
            for (i in 0 until v.childCount) forEachVisible(v.getChildAt(i), exclude, visitor)
        }
    }
}
