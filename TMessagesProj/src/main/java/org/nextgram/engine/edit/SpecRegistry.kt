package org.nextgram.engine.edit

import android.view.View

/** Реестр заготовок. Новая заготовка = класс EditorSpec + строка в [specs]. */
object SpecRegistry {

    private val specs: List<EditorSpec> = listOf<EditorSpec>(MessageCellSpec()) + ThemeCatalog.specs + MoreCatalog.specs

    /** Заготовка для конкретного view или null. */
    @JvmStatic
    fun find(view: View): EditorSpec? = specs.firstOrNull { it.handles(view) }

    /**
     * Цель редактирования для точки экрана.
     *
     * Перебираем всё, что под пальцем, сверху вниз и от глубоких к внешним.
     * Побеждает первый элемент с заготовкой (у себя или у предка) — даже если над
     * ним лежит прозрачный слой на весь экран. Мелкий виджет (галочка, переключатель)
     * уступает элементу вокруг, если у того есть часть под пальцем. Экран целиком
     * выбирается, только если под пальцем нет ничего конкретнее.
     */
    @JvmStatic
    fun targetAt(root: View, rawX: Float, rawY: Float, exclude: View?): EditTarget? {
        val hits = HitTest.allAt(root, rawX.toInt(), rawY.toInt(), exclude)
        if (hits.isEmpty()) return null
        var container: EditTarget? = null
        for (hit in hits) {
            var v: View? = hit
            var widget: EditTarget? = null
            var hitContainer: EditTarget? = null
            while (v != null && v !== root) {
                val spec = find(v)
                if (spec != null) when (spec.kind) {
                    EditorSpec.Kind.WIDGET -> if (widget == null) widget = EditTarget(v, spec, partAt(v, spec, rawX, rawY))
                    EditorSpec.Kind.ELEMENT -> {
                        val part = partAt(v, spec, rawX, rawY)
                        // Виджет внутри элемента: берём элемент, если палец попал в его часть.
                        return if (widget != null && part == null) widget else EditTarget(v, spec, part)
                    }
                    EditorSpec.Kind.CONTAINER -> if (hitContainer == null && widget == null) {
                        hitContainer = EditTarget(v, spec, partAt(v, spec, rawX, rawY))
                    }
                }
                v = v.parent as? View
            }
            if (widget != null) return widget
            // Непрозрачный view без заготовки сверху: если окно/экран вокруг знает эту
            // часть — берём окно с частью, иначе сам view в авторежиме.
            if (HitTest.isSolid(hit, root)) {
                val hc = hitContainer
                if (hc != null && hc.part != null && hc.part != "all") return hc
                // Без заготовки выбираем, только если у view есть что менять (цвета темы).
                if (AutoSpec.hasColors(hit)) return EditTarget(hit, AutoSpec)
            }
            if (container == null) container = hitContainer
        }
        // Под пальцем нет ничего, что имеет смысл править, — берём экран целиком или ничего.
        return container
    }

    /** Цель для view (без точки): ближайший предок с заготовкой, иначе сам view в авторежиме. */
    @JvmStatic
    fun targetFor(view: View): EditTarget? {
        var v: View? = view
        while (v != null) {
            val spec = find(v)
            if (spec != null) return EditTarget(v, spec)
            v = v.parent as? View
        }
        return if (AutoSpec.hasColors(view)) EditTarget(view, AutoSpec) else null
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
