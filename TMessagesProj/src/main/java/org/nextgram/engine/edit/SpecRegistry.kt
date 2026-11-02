package org.nextgram.engine.edit

import android.view.View

/** Реестр заготовок. Новая заготовка = класс EditorSpec + строка в [specs]. */
object SpecRegistry {

    private val specs: List<EditorSpec> = listOf<EditorSpec>(MessageCellSpec()) + ThemeCatalog.specs

    /** Заготовка для конкретного view или null. */
    @JvmStatic
    fun find(view: View): EditorSpec? = specs.firstOrNull { it.handles(view) }

    /**
     * Цель редактирования для точки экрана: ближайший предок с заготовкой и его
     * видимая часть под пальцем (части проверяются по порядку — мелкие раньше крупных).
     * Если заготовки нет — сам view в авторежиме.
     */
    @JvmStatic
    fun targetFor(deepest: View, rawX: Float = Float.NaN, rawY: Float = Float.NaN): EditTarget {
        var v: View? = deepest
        while (v != null) {
            val spec = find(v)
            if (spec != null) return EditTarget(v, spec, if (rawX.isNaN()) null else partAt(v, spec, rawX, rawY))
            v = v.parent as? View
        }
        return EditTarget(deepest, AutoSpec)
    }

    private fun partAt(view: View, spec: EditorSpec, rawX: Float, rawY: Float): String? {
        val loc = IntArray(2)
        view.getLocationOnScreen(loc)
        val x = rawX - loc[0]
        val y = rawY - loc[1]
        val r = android.graphics.RectF()
        return spec.parts(view).firstOrNull { spec.partBounds(view, it.id, r) && r.contains(x, y) }?.id
    }
}
