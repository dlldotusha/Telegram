package org.telegram.custom.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Переопределения одного элемента интерфейса на одном уровне (scope).
 * Свойства самого элемента лежат в {@link #props}, свойства его частей
 * (время, просмотры, текст...) — в {@link #parts}.
 *
 * Значения: Boolean, Number или String (цвета хранятся строкой "#AARRGGBB").
 */
public final class ElementStyle {

    public final LinkedHashMap<String, Object> props = new LinkedHashMap<>();
    public final LinkedHashMap<String, LinkedHashMap<String, Object>> parts = new LinkedHashMap<>();

    /** part == null — свойство самого элемента. */
    public Object get(String part, String prop) {
        Map<String, Object> m = part == null ? props : parts.get(part);
        return m == null ? null : m.get(prop);
    }

    /** value == null — удалить свойство. */
    public void set(String part, String prop, Object value) {
        if (part == null) {
            if (value == null) {
                props.remove(prop);
            } else {
                props.put(prop, value);
            }
            return;
        }
        LinkedHashMap<String, Object> m = parts.get(part);
        if (value == null) {
            if (m != null) {
                m.remove(prop);
                if (m.isEmpty()) {
                    parts.remove(part);
                }
            }
            return;
        }
        if (m == null) {
            m = new LinkedHashMap<>();
            parts.put(part, m);
        }
        m.put(prop, value);
    }

    public boolean isEmpty() {
        return props.isEmpty() && parts.isEmpty();
    }

    public ElementStyle copy() {
        ElementStyle s = new ElementStyle();
        s.props.putAll(props);
        for (Map.Entry<String, LinkedHashMap<String, Object>> e : parts.entrySet()) {
            s.parts.put(e.getKey(), new LinkedHashMap<>(e.getValue()));
        }
        return s;
    }
}
