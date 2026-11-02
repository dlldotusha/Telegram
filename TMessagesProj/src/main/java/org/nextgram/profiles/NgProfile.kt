package org.nextgram.profiles

import org.json.JSONObject
import org.nextgram.engine.core.Colors

/** Расширенный профиль NextGram. Формат совпадает с тем, что принимает сервер (backend/worker). */
data class NgProfile(
    /** null — частиц нет. */
    var particles: String? = null,
    var particlesColor: Int = 0xFFFFFFFF.toInt(),
    var density: Float = 1f,
) {
    val isEmpty: Boolean get() = particles == null

    fun toJson(): JSONObject = JSONObject().apply {
        put("v", 1)
        particles?.let { type ->
            put("particles", JSONObject().apply {
                put("type", type)
                put("color", Colors.format(particlesColor))
                put("density", density.toDouble())
            })
        }
    }

    companion object {
        val PARTICLE_TYPES = listOf("stars", "snow", "bubbles", "hearts")

        fun fromJson(o: JSONObject?): NgProfile? {
            if (o == null) return null
            val p = NgProfile()
            o.optJSONObject("particles")?.let { pt ->
                p.particles = pt.optString("type").takeIf { it in PARTICLE_TYPES }
                Colors.parse(pt.optString("color"))?.let { p.particlesColor = it }
                p.density = pt.optDouble("density", 1.0).coerceIn(0.3, 2.0).toFloat()
            }
            return p
        }
    }
}
