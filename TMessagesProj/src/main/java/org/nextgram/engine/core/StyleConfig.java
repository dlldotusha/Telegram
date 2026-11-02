package org.nextgram.engine.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Весь конфиг кастомизации.
 *
 * layers: уровень → id элемента → переопределения.
 * themeColors: тема → имя цветового ключа Telegram → цвет. Это «базовый»
 * режим: цвет ключа темы, который меняется везде, где ключ используется.
 *
 * Класс не потокобезопасен: меняйте его только с UI-потока (через NextEngine.edit).
 */
public final class StyleConfig {

    public static final int VERSION = 1;

    final LinkedHashMap<String, LinkedHashMap<String, ElementStyle>> layers = new LinkedHashMap<>();
    final LinkedHashMap<String, LinkedHashMap<String, String>> themeColors = new LinkedHashMap<>();

    public Object get(String scope, String element, String part, String prop) {
        Map<String, ElementStyle> layer = layers.get(scope);
        if (layer == null) {
            return null;
        }
        ElementStyle style = layer.get(element);
        return style == null ? null : style.get(part, prop);
    }

    /** value == null — сбросить свойство на этом уровне. Пустые узлы удаляются. */
    public void set(String scope, String element, String part, String prop, Object value) {
        LinkedHashMap<String, ElementStyle> layer = layers.get(scope);
        if (layer == null) {
            if (value == null) {
                return;
            }
            layer = new LinkedHashMap<>();
            layers.put(scope, layer);
        }
        ElementStyle style = layer.get(element);
        if (style == null) {
            if (value == null) {
                return;
            }
            style = new ElementStyle();
            layer.put(element, style);
        }
        if (value instanceof Integer && prop.startsWith("color")) {
            value = Colors.format((Integer) value);
        }
        style.set(part, prop, value);
        if (style.isEmpty()) {
            layer.remove(element);
        }
        if (layer.isEmpty()) {
            layers.remove(scope);
        }
    }

    /** Сбросить все правки элемента на уровне. */
    public void clearElement(String scope, String element) {
        LinkedHashMap<String, ElementStyle> layer = layers.get(scope);
        if (layer != null) {
            layer.remove(element);
            if (layer.isEmpty()) {
                layers.remove(scope);
            }
        }
    }

    /** Удалить уровень целиком (например, чат или папка больше не существуют). */
    public void removeScope(String scope) {
        layers.remove(scope);
    }

    public Set<String> scopes() {
        return Collections.unmodifiableSet(layers.keySet());
    }

    /**
     * Каскад: проходит уровни от общего к частному, каждое свойство берётся
     * с последнего уровня, где оно задано. Слияние идёт по свойствам, а не по
     * элементам: если в чате задан только цвет времени, остальное наследуется.
     */
    public ResolvedElement resolve(List<String> chain, String element) {
        ResolvedElement out = null;
        for (int i = 0, n = chain.size(); i < n; i++) {
            String scope = chain.get(i);
            Map<String, ElementStyle> layer = layers.get(scope);
            if (layer == null) {
                continue;
            }
            ElementStyle style = layer.get(element);
            if (style == null) {
                continue;
            }
            if (out == null) {
                out = new ResolvedElement();
            }
            out.putAll(style, scope);
        }
        return out == null ? ResolvedElement.EMPTY : out;
    }

    public Map<String, String> themeColors(String themeKey) {
        Map<String, String> m = themeColors.get(themeKey);
        return m == null ? Collections.<String, String>emptyMap() : Collections.unmodifiableMap(m);
    }

    /** argb == null — сбросить цвет ключа. */
    public void setThemeColor(String themeKey, String colorKey, Integer argb) {
        LinkedHashMap<String, String> m = themeColors.get(themeKey);
        if (argb == null) {
            if (m != null) {
                m.remove(colorKey);
                if (m.isEmpty()) {
                    themeColors.remove(themeKey);
                }
            }
            return;
        }
        if (m == null) {
            m = new LinkedHashMap<>();
            themeColors.put(themeKey, m);
        }
        m.put(colorKey, Colors.format(argb));
    }

    public StyleConfig copy() {
        StyleConfig c = new StyleConfig();
        for (Map.Entry<String, LinkedHashMap<String, ElementStyle>> l : layers.entrySet()) {
            LinkedHashMap<String, ElementStyle> layer = new LinkedHashMap<>();
            for (Map.Entry<String, ElementStyle> e : l.getValue().entrySet()) {
                layer.put(e.getKey(), e.getValue().copy());
            }
            c.layers.put(l.getKey(), layer);
        }
        for (Map.Entry<String, LinkedHashMap<String, String>> t : themeColors.entrySet()) {
            c.themeColors.put(t.getKey(), new LinkedHashMap<>(t.getValue()));
        }
        return c;
    }
}
