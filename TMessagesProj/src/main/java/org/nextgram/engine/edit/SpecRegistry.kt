package org.nextgram.engine.edit

import android.view.View

/** Реестр заготовок. Новая заготовка = класс EditorSpec + строка в [specs]. */
object SpecRegistry {

    private val specs: List<EditorSpec> = listOf<EditorSpec>(MessageCellSpec()) + ThemeCatalog.specs

    /** Заготовка для конкретного view или null. */
    @JvmStatic
    fun find(view: View): EditorSpec? = specs.firstOrNull { it.handles(view) }

    /** Цель редактирования для точки: ближайший предок с заготовкой, иначе сам view в авторежиме. */
    @JvmStatic
    fun targetFor(deepest: View): EditTarget {
        var v: View? = deepest
        while (v != null) {
            find(v)?.let { return EditTarget(v!!, it) }
            v = v.parent as? View
        }
        return EditTarget(deepest, AutoSpec)
    }
}
