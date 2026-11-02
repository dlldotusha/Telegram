package org.nextgram.engine.core

/**
 * Весь конфиг NextEngine.
 *
 * layers: уровень → id элемента → переопределения.
 * themeColors: тема → имя цветового ключа Telegram → цвет. Это «базовый»
 * режим: цвет ключа темы меняется везде, где ключ используется.
 *
 * Класс не потокобезопасен: меняйте его только с UI-потока (через NextEngine.edit).
 */
class StyleConfig {

    internal val layers = LinkedHashMap<String, LinkedHashMap<String, ElementStyle>>()
    internal val themeColors = LinkedHashMap<String, LinkedHashMap<String, String>>()

    fun get(scope: String, element: String, part: String?, prop: String): Any? =
        layers[scope]?.get(element)?.get(part, prop)

    /** value == null — сбросить свойство на этом уровне. Пустые узлы удаляются. */
    fun set(scope: String, element: String, part: String?, prop: String, value: Any?) {
        val layer = layers[scope] ?: if (value == null) return else LinkedHashMap<String, ElementStyle>().also { layers[scope] = it }
        val style = layer[element] ?: if (value == null) return else ElementStyle().also { layer[element] = it }
        val stored = if (value is Int && prop.startsWith("color")) Colors.format(value) else value
        style.set(part, prop, stored)
        if (style.isEmpty) layer.remove(element)
        if (layer.isEmpty()) layers.remove(scope)
    }

    /** Сбросить все правки элемента на уровне. */
    fun clearElement(scope: String, element: String) {
        val layer = layers[scope] ?: return
        layer.remove(element)
        if (layer.isEmpty()) layers.remove(scope)
    }

    /** Удалить уровень целиком (например, чат или папка больше не существуют). */
    fun removeScope(scope: String) {
        layers.remove(scope)
    }

    val scopes: Set<String> get() = layers.keys

    /**
     * Каскад: проходит уровни от общего к частному, каждое свойство берётся
     * с последнего уровня, где оно задано. Слияние идёт по свойствам, а не по
     * элементам: если в чате задан только цвет времени, остальное наследуется.
     */
    fun resolve(chain: List<String>, element: String): ResolvedElement {
        var out: ResolvedElement? = null
        for (scope in chain) {
            val style = layers[scope]?.get(element) ?: continue
            if (out == null) out = ResolvedElement()
            out.putAll(style, scope)
        }
        return out ?: ResolvedElement.EMPTY
    }

    fun themeColors(themeKey: String): Map<String, String> = themeColors[themeKey] ?: emptyMap()

    /** argb == null — сбросить цвет ключа. */
    fun setThemeColor(themeKey: String, colorKey: String, argb: Int?) {
        if (argb == null) {
            val m = themeColors[themeKey] ?: return
            m.remove(colorKey)
            if (m.isEmpty()) themeColors.remove(themeKey)
            return
        }
        themeColors.getOrPut(themeKey) { LinkedHashMap() }[colorKey] = Colors.format(argb)
    }

    fun copy(): StyleConfig = StyleConfig().also { c ->
        layers.forEach { (scope, layer) ->
            c.layers[scope] = LinkedHashMap<String, ElementStyle>().apply { layer.forEach { (e, s) -> this[e] = s.copy() } }
        }
        themeColors.forEach { (t, m) -> c.themeColors[t] = LinkedHashMap(m) }
    }

    companion object {
        const val VERSION = 1
    }
}
