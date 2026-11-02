package org.nextgram.engine.edit

import android.view.View

/** Реестр заготовок. Новая заготовка = класс EditorSpec + строка в [specs]. */
object SpecRegistry {

    private val specs: List<EditorSpec> = listOf<EditorSpec>(MessageCellSpec()) + ThemeCatalog.specs

    /** Заготовка для конкретного view или null. */
    @JvmStatic
    fun find(view: View): EditorSpec? = specs.firstOrNull { it.handles(view) }

    /**
     * Цель редактирования для точки экрана.
     *
     * Перебираем всё, что под пальцем, сверху вниз: первый view, у которого
     * (или у чьего предка) есть заготовка, побеждает — даже если над ним лежит
     * прозрачный слой на весь экран. Если заготовок нет — первый «непрозрачный»
     * view в авторежиме.
     */
    @JvmStatic
    fun targetAt(root: View, rawX: Float, rawY: Float, exclude: View?): EditTarget? {
        val hits = HitTest.allAt(root, rawX.toInt(), rawY.toInt(), exclude)
        if (hits.isEmpty()) return null
        for (hit in hits) {
            // Встретили непрозрачный элемент без заготовки — дальше вглубь не идём,
            // но сначала проверяем его предков (ячейка могла состоять из детей-view).
            var v: View? = hit
            while (v != null && v !== root) {
                val spec = find(v)
                if (spec != null) return EditTarget(v, spec, partAt(v, spec, rawX, rawY))
                v = v.parent as? View
            }
            if (HitTest.isSolid(hit, root)) return EditTarget(hit, AutoSpec)
        }
        return EditTarget(hits.minByOrNull { it.width.toLong() * it.height } ?: hits.first(), AutoSpec)
    }

    /** Цель для view (без точки): ближайший предок с заготовкой, иначе сам view в авторежиме. */
    @JvmStatic
    fun targetFor(view: View): EditTarget {
        var v: View? = view
        while (v != null) {
            val spec = find(v)
            if (spec != null) return EditTarget(v, spec)
            v = v.parent as? View
        }
        return EditTarget(view, AutoSpec)
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
