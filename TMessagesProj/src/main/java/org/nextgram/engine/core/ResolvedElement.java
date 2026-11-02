package org.telegram.custom.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Итоговые свойства элемента после каскада. Для каждого свойства помнит,
 * с какого уровня пришло значение — редактор показывает это пользователю.
 */
public final class ResolvedElement {

    public static final ResolvedElement EMPTY = new ResolvedElement();

    /** Ключ части для свойств самого элемента. */
    static final String SELF = "";

    private static final class Value {
        final Object value;
        final String source;

        Value(Object value, String source) {
            this.value = value;
            this.source = source;
        }
    }

    private final HashMap<String, HashMap<String, Value>> values = new HashMap<>();

    void put(String part, String prop, Object value, String source) {
        String key = part == null ? SELF : part;
        HashMap<String, Value> m = values.get(key);
        if (m == null) {
            m = new HashMap<>();
            values.put(key, m);
        }
        m.put(prop, new Value(value, source));
    }

    void putAll(ElementStyle style, String source) {
        for (Map.Entry<String, Object> e : style.props.entrySet()) {
            put(null, e.getKey(), e.getValue(), source);
        }
        for (Map.Entry<String, java.util.LinkedHashMap<String, Object>> p : style.parts.entrySet()) {
            for (Map.Entry<String, Object> e : p.getValue().entrySet()) {
                put(p.getKey(), e.getKey(), e.getValue(), source);
            }
        }
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public Object get(String part, String prop) {
        HashMap<String, Value> m = values.get(part == null ? SELF : part);
        if (m == null) {
            return null;
        }
        Value v = m.get(prop);
        return v == null ? null : v.value;
    }

    /** Уровень, с которого пришло значение, или null, если свойство не задано. */
    public String sourceOf(String part, String prop) {
        HashMap<String, Value> m = values.get(part == null ? SELF : part);
        if (m == null) {
            return null;
        }
        Value v = m.get(prop);
        return v == null ? null : v.source;
    }

    public boolean getBool(String part, String prop, boolean def) {
        Object v = get(part, prop);
        return v instanceof Boolean ? (Boolean) v : def;
    }

    public float getFloat(String part, String prop, float def) {
        Object v = get(part, prop);
        return v instanceof Number ? ((Number) v).floatValue() : def;
    }

    /** Цвет ARGB или null, если не задан или записан с ошибкой. */
    public Integer getColor(String part, String prop) {
        return Colors.parse(get(part, prop));
    }
}
