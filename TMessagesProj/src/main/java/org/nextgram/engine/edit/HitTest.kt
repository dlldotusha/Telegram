package org.nextgram.engine.edit

import android.view.View
import android.view.ViewGroup

/** Поиск view под точкой экрана и обход видимых view. */
internal object HitTest {

    /**
     * Все видимые view под точкой, в порядке «что видит палец»: сначала верхние
     * по z-порядку, внутри поддерева — сначала самые глубокие.
     */
    fun allAt(root: View, screenX: Int, screenY: Int, exclude: View?): List<View> {
        val out = ArrayList<View>()
        collect(root, screenX, screenY, exclude, IntArray(2), out)
        return out
    }

    private fun collect(v: View?, x: Int, y: Int, exclude: View?, loc: IntArray, out: MutableList<View>) {
        // Слой режима правки исключаем по типу: даже если в окне остался старый экземпляр.
        if (v == null || v === exclude || v is EditOverlay || v.visibility != View.VISIBLE || v.alpha < 0.02f) return
        v.getLocationOnScreen(loc)
        if (x < loc[0] || y < loc[1] || x >= loc[0] + v.width || y >= loc[1] + v.height) return
        if (v is ViewGroup) {
            // Последний ребёнок рисуется поверх — проверяем с конца.
            for (i in v.childCount - 1 downTo 0) collect(v.getChildAt(i), x, y, exclude, loc, out)
        }
        out += v
    }

    /**
     * «Непрозрачный» для пальца view: что-то рисует и не накрывает пол-экрана.
     * Прозрачные слои Telegram поверх списков (размытие, анимации, оверлеи)
     * сюда не попадают, поэтому палец «проходит» сквозь них к сообщению.
     */
    fun isSolid(v: View, root: View): Boolean {
        val area = v.width.toLong() * v.height
        val rootArea = maxOf(1L, root.width.toLong() * root.height)
        if (area * 2 > rootArea) return false
        return v !is ViewGroup || v.background != null
    }

    fun forEachVisible(v: View?, exclude: View?, visitor: (View) -> Unit) {
        if (v == null || v === exclude || v is EditOverlay || v.visibility != View.VISIBLE || !v.isAttachedToWindow) return
        visitor(v)
        if (v is ViewGroup) {
            for (i in 0 until v.childCount) forEachVisible(v.getChildAt(i), exclude, visitor)
        }
    }
}
