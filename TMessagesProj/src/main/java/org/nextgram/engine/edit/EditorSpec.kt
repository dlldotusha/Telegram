package org.nextgram.engine.edit

import android.graphics.RectF
import android.view.View

/**
 * Заготовка редактора для класса View. Заготовка знает, из каких частей
 * состоит элемент (это нужно, чтобы понять, во что ткнули, и чтобы скрывать
 * части касанием) и какие у него есть свойства.
 *
 * Если для View заготовки нет, используется [AutoSpec]: только цвета темы.
 */
interface EditorSpec {

    /** Часть элемента: время, просмотры, текст... */
    data class Part(val id: String, val title: String, val hideable: Boolean)

    /** Редактируемое свойство. part == null — свойство самого элемента. */
    data class Prop(
        val part: String?,
        val key: String,
        val type: Type,
        val title: String,
        /** Значение, если свойство нигде не задано (для BOOL). */
        val defaultBool: Boolean = false,
    ) {
        enum class Type { BOOL, COLOR }
    }

    fun handles(view: View): Boolean

    /** Стабильный id элемента в конфиге. */
    fun elementId(view: View): String

    fun title(view: View): String

    /** Чат, к которому относится элемент, или 0. От этого зависят доступные уровни. */
    fun dialogId(view: View): Long

    fun account(view: View): Int

    fun parts(view: View): List<Part>

    /** Границы части в координатах view или false, если часть сейчас не видна. */
    fun partBounds(view: View, partId: String, out: RectF): Boolean

    fun props(view: View): List<Prop>

    /**
     * Цвета темы, за которые отвечает элемент, с понятными названиями.
     * Цвета темы глобальные: меняются везде, где используются.
     */
    fun colorGroups(view: View): List<ThemeCatalog.ColorGroup> = emptyList()

    /** Применить изменившийся конфиг к view (перемерить, перерисовать). */
    fun apply(view: View)
}
