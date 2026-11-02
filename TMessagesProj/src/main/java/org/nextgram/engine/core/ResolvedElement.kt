package org.nextgram.engine.core

/**
 * Итоговые свойства элемента после каскада. Для каждого свойства помнит,
 * с какого уровня пришло значение — редактор показывает это пользователю.
 */
class ResolvedElement {

    private class Value(val value: Any, val source: String)

    private val values = HashMap<String, HashMap<String, Value>>()

    internal fun put(part: String?, prop: String, value: Any, source: String) {
        values.getOrPut(part ?: SELF) { HashMap() }[prop] = Value(value, source)
    }

    internal fun putAll(style: ElementStyle, source: String) {
        style.props.forEach { (k, v) -> put(null, k, v, source) }
        style.parts.forEach { (part, m) -> m.forEach { (k, v) -> put(part, k, v, source) } }
    }

    val isEmpty: Boolean get() = values.isEmpty()

    fun get(part: String?, prop: String): Any? = values[part ?: SELF]?.get(prop)?.value

    /** Уровень, с которого пришло значение, или null, если свойство не задано. */
    fun sourceOf(part: String?, prop: String): String? = values[part ?: SELF]?.get(prop)?.source

    fun getBool(part: String?, prop: String, def: Boolean): Boolean = get(part, prop) as? Boolean ?: def

    fun getFloat(part: String?, prop: String, def: Float): Float = (get(part, prop) as? Number)?.toFloat() ?: def

    /** Цвет ARGB или null, если не задан или записан с ошибкой. */
    fun getColor(part: String?, prop: String): Int? = Colors.parse(get(part, prop))

    companion object {
        /** Ключ части для свойств самого элемента. */
        private const val SELF = ""

        @JvmField
        val EMPTY = ResolvedElement()
    }
}
