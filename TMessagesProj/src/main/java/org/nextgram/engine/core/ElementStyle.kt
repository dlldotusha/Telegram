package org.nextgram.engine.core

/**
 * Переопределения одного элемента интерфейса на одном уровне (scope).
 * Свойства самого элемента — в [props], свойства его частей (время, просмотры...) — в [parts].
 *
 * Значения: Boolean, Number или String (цвета хранятся строкой "#AARRGGBB").
 */
class ElementStyle {

    val props = LinkedHashMap<String, Any>()
    val parts = LinkedHashMap<String, LinkedHashMap<String, Any>>()

    /** part == null — свойство самого элемента. */
    fun get(part: String?, prop: String): Any? =
        (if (part == null) props else parts[part])?.get(prop)

    /** value == null — удалить свойство. */
    fun set(part: String?, prop: String, value: Any?) {
        if (part == null) {
            if (value == null) props.remove(prop) else props[prop] = value
            return
        }
        if (value == null) {
            val m = parts[part] ?: return
            m.remove(prop)
            if (m.isEmpty()) parts.remove(part)
            return
        }
        parts.getOrPut(part) { LinkedHashMap() }[prop] = value
    }

    val isEmpty: Boolean
        get() = props.isEmpty() && parts.isEmpty()

    fun copy(): ElementStyle = ElementStyle().also { s ->
        s.props.putAll(props)
        parts.forEach { (k, v) -> s.parts[k] = LinkedHashMap(v) }
    }
}
