package org.nextgram.engine.core

import org.json.JSONException
import org.json.JSONObject

/**
 * JSON-формат конфига:
 *
 * ```
 * {
 *   "version": 1,
 *   "layers": {
 *     "global":        { "chat.message.in": { "parts": { "time": { "seconds": true } } } },
 *     "type:channels": { "chat.message.in": { "parts": { "time": { "hidden": true } } } },
 *     "chat:-1001234": { "chat.message.in": { "parts": { "time": { "color": "#FF88AAFF" } } } }
 *   },
 *   "themeColors": { "Blue": { "chat_inTimeText": "#FF6A7B8C" } }
 * }
 * ```
 *
 * Свойства самого элемента лежат в "props", свойства частей — в "parts".
 */
object ConfigCodec {

    fun toJson(config: StyleConfig): String {
        val root = JSONObject()
        root.put("version", StyleConfig.VERSION)
        val layers = JSONObject()
        config.layers.forEach { (scope, layer) ->
            val layerJson = JSONObject()
            layer.forEach { (element, s) ->
                val el = JSONObject()
                if (s.props.isNotEmpty()) el.put("props", mapToJson(s.props))
                if (s.parts.isNotEmpty()) {
                    val parts = JSONObject()
                    s.parts.forEach { (p, m) -> parts.put(p, mapToJson(m)) }
                    el.put("parts", parts)
                }
                layerJson.put(element, el)
            }
            layers.put(scope, layerJson)
        }
        root.put("layers", layers)
        val theme = JSONObject()
        config.themeColors.forEach { (t, m) ->
            val colors = JSONObject()
            m.forEach { (k, v) -> colors.put(k, v) }
            theme.put(t, colors)
        }
        root.put("themeColors", theme)
        return root.toString(2)
    }

    /** Бросает JSONException на битом файле — вызывающий решает, что с ним делать. */
    @Throws(JSONException::class)
    fun fromJson(json: String): StyleConfig {
        val config = StyleConfig()
        val root = JSONObject(json)
        // version пока один; более новые версии читаем как есть, неизвестные поля игнорируем.
        root.optJSONObject("layers")?.let { layers ->
            for (scope in layers.keys()) {
                val layerJson = layers.optJSONObject(scope) ?: continue
                val layer = LinkedHashMap<String, ElementStyle>()
                for (element in layerJson.keys()) {
                    val el = layerJson.optJSONObject(element) ?: continue
                    val s = ElementStyle()
                    readMap(el.optJSONObject("props"), s.props)
                    el.optJSONObject("parts")?.let { parts ->
                        for (part in parts.keys()) {
                            val m = LinkedHashMap<String, Any>()
                            readMap(parts.optJSONObject(part), m)
                            if (m.isNotEmpty()) s.parts[part] = m
                        }
                    }
                    if (!s.isEmpty) layer[element] = s
                }
                if (layer.isNotEmpty()) config.layers[scope] = layer
            }
        }
        root.optJSONObject("themeColors")?.let { theme ->
            for (themeKey in theme.keys()) {
                val colors = theme.optJSONObject(themeKey) ?: continue
                val m = LinkedHashMap<String, String>()
                for (key in colors.keys()) {
                    Colors.parse(colors.opt(key))?.let { m[key] = Colors.format(it) }
                }
                if (m.isNotEmpty()) config.themeColors[themeKey] = m
            }
        }
        return config
    }

    private fun mapToJson(map: Map<String, Any>): JSONObject =
        JSONObject().apply { map.forEach { (k, v) -> put(k, v) } }

    private fun readMap(o: JSONObject?, out: MutableMap<String, Any>) {
        if (o == null) return
        for (key in o.keys()) {
            val v = o.opt(key)
            if (v is Boolean || v is Number || v is String) out[key] = v
        }
    }
}
