package org.nextgram.profiles

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.SystemClock
import android.view.View
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LiteMode
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * Частицы в шапке профиля NextGram. Рисуются только в пределах высоты шапки,
 * которую сообщает ProfileActivity (она меняется при прокрутке).
 */
@SuppressLint("ViewConstructor")
class ParticlesView(context: Context, private val heightProvider: HeightProvider) : View(context) {

    fun interface HeightProvider {
        fun headerHeight(): Float
    }

    private class Particle(var x: Float, var y: Float, val size: Float, val speed: Float, val phase: Float)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val heart = Path()
    private var profile: NgProfile? = null
    private var particles: List<Particle> = emptyList()
    private var lastFrame = 0L

    init {
        setWillNotDraw(false)
    }

    fun setProfile(p: NgProfile?) {
        profile = p?.takeIf { !it.isEmpty }
        particles = emptyList()
        visibility = if (profile == null) GONE else VISIBLE
        invalidate()
    }

    private fun init(p: NgProfile, w: Int, h: Int) {
        val n = (BASE_COUNT * p.density).roundToInt().coerceAtLeast(4)
        particles = List(n) {
            Particle(
                x = Random.nextFloat() * w,
                y = Random.nextFloat() * h,
                size = AndroidUtilities.dp(1.2f + Random.nextFloat() * 2.4f).toFloat(),
                speed = AndroidUtilities.dp(8f + Random.nextFloat() * 22f).toFloat(), // px в секунду
                phase = Random.nextFloat() * 6.283f,
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        val p = profile ?: return
        val type = p.particles ?: return
        val w = width
        val h = minOf(height.toFloat(), heightProvider.headerHeight())
        if (w == 0 || h <= 0f) return
        if (particles.isEmpty()) {
            init(p, w, h.toInt())
            lastFrame = SystemClock.elapsedRealtime()
        }
        val now = SystemClock.elapsedRealtime()
        val animate = LiteMode.isEnabled(LiteMode.FLAG_PARTICLES)
        val dt = if (animate) minOf(0.05f, (now - lastFrame) / 1000f) else 0f
        lastFrame = now
        val t = now / 1000f
        val baseAlpha = p.particlesColor ushr 24

        canvas.save()
        canvas.clipRect(0f, 0f, w.toFloat(), h)
        for (q in particles) {
            val s = q.size
            when (type) {
                "snow" -> {
                    q.y += q.speed * dt
                    q.x += sin(t + q.phase) * AndroidUtilities.dp(6f) * dt
                    if (q.y > h + s) {
                        q.y = -s
                        q.x = Random.nextFloat() * w
                    }
                }
                "bubbles", "hearts" -> {
                    q.y -= q.speed * dt
                    q.x += sin(t * 1.3f + q.phase) * AndroidUtilities.dp(5f) * dt
                    if (q.y < -s * 4) {
                        q.y = h + s * 4
                        q.x = Random.nextFloat() * w
                    }
                }
                else -> { // stars — мерцают на месте и медленно дрейфуют
                    q.x += q.speed * 0.15f * dt
                    if (q.x > w + s) q.x = -s
                }
            }
            val twinkle = if (type == "stars") 0.35f + 0.65f * (0.5f + 0.5f * sin(t * 2.2f + q.phase * 3)) else 0.85f
            paint.color = p.particlesColor
            paint.alpha = (baseAlpha * twinkle).roundToInt()
            drawParticle(canvas, type, q.x, q.y, s)
        }
        canvas.restore()
        if (animate) postInvalidateOnAnimation()
    }

    private fun drawParticle(canvas: Canvas, type: String, cx: Float, cy: Float, s: Float) {
        when (type) {
            "bubbles" -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = AndroidUtilities.dp(1f).toFloat()
                canvas.drawCircle(cx, cy, s * 1.8f, paint)
                paint.style = Paint.Style.FILL
            }
            "hearts" -> {
                val k = s * 1.6f
                heart.rewind()
                heart.moveTo(cx, cy + k)
                heart.cubicTo(cx - k * 2, cy - k * 0.2f, cx - k * 0.8f, cy - k * 1.6f, cx, cy - k * 0.5f)
                heart.cubicTo(cx + k * 0.8f, cy - k * 1.6f, cx + k * 2, cy - k * 0.2f, cx, cy + k)
                heart.close()
                canvas.drawPath(heart, paint)
            }
            "stars" -> { // четырёхлучевая искра
                paint.strokeWidth = maxOf(1f, s * 0.45f)
                paint.strokeCap = Paint.Cap.ROUND
                canvas.drawLine(cx - s * 1.6f, cy, cx + s * 1.6f, cy, paint)
                canvas.drawLine(cx, cy - s * 1.6f, cx, cy + s * 1.6f, paint)
            }
            else -> canvas.drawCircle(cx, cy, s, paint)
        }
    }

    private companion object {
        const val BASE_COUNT = 36
    }
}
