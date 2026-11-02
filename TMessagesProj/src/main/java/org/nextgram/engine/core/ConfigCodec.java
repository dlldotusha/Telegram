package org.nextgram.engine.core;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON-формат конфига:
 *
 * <pre>
 * {
 *   "version": 1,
 *   "layers": {
 *     "global":        { "chat.message.in": { "parts": { "time": { "seconds": true } } } },
 *     "type:channels": { "chat.message.in": { "parts": { "time": { "hidden": true } } } },
 *     "chat:-1001234": { "chat.message.in": { "parts": { "time": { "color": "#FF88AAFF" } } } }
 *   },
 *   "themeColors": { "Blue": { "chat_inTimeText": "#FF6A7B8C" } }
 * }
 * </pre>
 *
 * Свойства самого элемента лежат в "props", свойства частей — в "parts".
 */
public final class ConfigCodec {

    private ConfigCodec() {
    }

    public static String toJson(StyleConfig config) {
        try {
            JSONObject root = new JSONObject();
            root.put("version", StyleConfig.VERSION);

            JSONObject layers = new JSONObject();
            for (Map.Entry<String, LinkedHashMap<String, ElementStyle>> l : config.layers.entrySet()) {
                JSONObject layer = new JSONObject();
                for (Map.Entry<String, ElementStyle> e : l.getValue().entrySet()) {
                    ElementStyle s = e.getValue();
                    JSONObject el = new JSONObject();
                    if (!s.props.isEmpty()) {
                        el.put("props", mapToJson(s.props));
                    }
                    if (!s.parts.isEmpty()) {
                        JSONObject parts = new JSONObject();
                        for (Map.Entry<String, LinkedHashMap<String, Object>> p : s.parts.entrySet()) {
                            parts.put(p.getKey(), mapToJson(p.getValue()));
                        }
                        el.put("parts", parts);
                    }
                    layer.put(e.getKey(), el);
                }
                layers.put(l.getKey(), layer);
            }
            root.put("layers", layers);

            JSONObject theme = new JSONObject();
            for (Map.Entry<String, LinkedHashMap<String, String>> t : config.themeColors.entrySet()) {
                JSONObject colors = new JSONObject();
                for (Map.Entry<String, String> c : t.getValue().entrySet()) {
                    colors.put(c.getKey(), c.getValue());
                }
                theme.put(t.getKey(), colors);
            }
            root.put("themeColors", theme);
            return root.toString(2);
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Бросает JSONException на битом файле — вызывающий решает, что с ним делать. */
    public static StyleConfig fromJson(String json) throws JSONException {
        StyleConfig config = new StyleConfig();
        JSONObject root = new JSONObject(json);
        // version пока один; более новые версии читаем как есть, неизвестные поля игнорируем.
        JSONObject layers = root.optJSONObject("layers");
        if (layers != null) {
            for (Iterator<String> it = layers.keys(); it.hasNext(); ) {
                String scope = it.next();
                JSONObject layerJson = layers.optJSONObject(scope);
                if (layerJson == null) {
                    continue;
                }
                LinkedHashMap<String, ElementStyle> layer = new LinkedHashMap<>();
                for (Iterator<String> eit = layerJson.keys(); eit.hasNext(); ) {
                    String element = eit.next();
                    JSONObject el = layerJson.optJSONObject(element);
                    if (el == null) {
                        continue;
                    }
                    ElementStyle s = new ElementStyle();
                    readMap(el.optJSONObject("props"), s.props);
                    JSONObject parts = el.optJSONObject("parts");
                    if (parts != null) {
                        for (Iterator<String> pit = parts.keys(); pit.hasNext(); ) {
                            String part = pit.next();
                            LinkedHashMap<String, Object> m = new LinkedHashMap<>();
                            readMap(parts.optJSONObject(part), m);
                            if (!m.isEmpty()) {
                                s.parts.put(part, m);
                            }
                        }
                    }
                    if (!s.isEmpty()) {
                        layer.put(element, s);
                    }
                }
                if (!layer.isEmpty()) {
                    config.layers.put(scope, layer);
                }
            }
        }
        JSONObject theme = root.optJSONObject("themeColors");
        if (theme != null) {
            for (Iterator<String> it = theme.keys(); it.hasNext(); ) {
                String themeKey = it.next();
                JSONObject colors = theme.optJSONObject(themeKey);
                if (colors == null) {
                    continue;
                }
                LinkedHashMap<String, String> m = new LinkedHashMap<>();
                for (Iterator<String> cit = colors.keys(); cit.hasNext(); ) {
                    String key = cit.next();
                    Integer argb = Colors.parse(colors.opt(key));
                    if (argb != null) {
                        m.put(key, Colors.format(argb));
                    }
                }
                if (!m.isEmpty()) {
                    config.themeColors.put(themeKey, m);
                }
            }
        }
        return config;
    }

    private static JSONObject mapToJson(Map<String, Object> map) throws JSONException {
        JSONObject o = new JSONObject();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            o.put(e.getKey(), e.getValue());
        }
        return o;
    }

    private static void readMap(JSONObject o, Map<String, Object> out) {
        if (o == null) {
            return;
        }
        for (Iterator<String> it = o.keys(); it.hasNext(); ) {
            String key = it.next();
            Object v = o.opt(key);
            if (v instanceof Boolean || v instanceof Number || v instanceof String) {
                out.put(key, v);
            }
        }
    }
}
