package org.nextgram.profiles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.SystemClock;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LiteMode;

import java.util.Random;

/**
 * Частицы в шапке профиля NextGram. Рисуются только в пределах высоты шапки,
 * которую сообщает ProfileActivity (она меняется при прокрутке).
 */
@SuppressLint("ViewConstructor")
public final class ParticlesView extends View {

    public interface HeightProvider {
        float headerHeight();
    }

    private static final int BASE_COUNT = 36;

    private final HeightProvider heightProvider;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path heart = new Path();
    private final Random random = new Random();

    private NgProfile profile;
    private float[] x, y, size, speed, phase;
    private long lastFrame;

    public ParticlesView(Context context, HeightProvider heightProvider) {
        super(context);
        this.heightProvider = heightProvider;
        setWillNotDraw(false);
    }

    public void setProfile(NgProfile p) {
        profile = p == null || p.isEmpty() ? null : p;
        x = null;
        setVisibility(profile == null ? GONE : VISIBLE);
        invalidate();
    }

    private void init(int w, int h) {
        int n = Math.max(4, Math.round(BASE_COUNT * profile.density));
        x = new float[n];
        y = new float[n];
        size = new float[n];
        speed = new float[n];
        phase = new float[n];
        for (int i = 0; i < n; i++) {
            x[i] = random.nextFloat() * w;
            y[i] = random.nextFloat() * h;
            size[i] = AndroidUtilities.dp(1.2f + random.nextFloat() * 2.4f);
            speed[i] = AndroidUtilities.dp(8 + random.nextFloat() * 22); // px в секунду
            phase[i] = random.nextFloat() * 6.283f;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (profile == null || getWidth() == 0) {
            return;
        }
        int w = getWidth();
        float h = Math.min(getHeight(), heightProvider.headerHeight());
        if (h <= 0) {
            return;
        }
        if (x == null) {
            init(w, (int) h);
            lastFrame = SystemClock.elapsedRealtime();
        }
        long now = SystemClock.elapsedRealtime();
        float dt = Math.min(0.05f, (now - lastFrame) / 1000f);
        lastFrame = now;
        boolean animate = LiteMode.isEnabled(LiteMode.FLAG_PARTICLES);
        if (!animate) {
            dt = 0;
        }

        canvas.save();
        canvas.clipRect(0, 0, w, h);
        int color = profile.particlesColor;
        int baseAlpha = color >>> 24;
        float t = now / 1000f;
        for (int i = 0; i < x.length; i++) {
            float s = size[i];
            switch (profile.particles) {
                case "snow":
                    y[i] += speed[i] * dt;
                    x[i] += (float) Math.sin(t + phase[i]) * AndroidUtilities.dp(6) * dt;
                    if (y[i] > h + s) {
                        y[i] = -s;
                        x[i] = random.nextFloat() * w;
                    }
                    break;
                case "bubbles":
                case "hearts":
                    y[i] -= speed[i] * dt;
                    x[i] += (float) Math.sin(t * 1.3f + phase[i]) * AndroidUtilities.dp(5) * dt;
                    if (y[i] < -s * 4) {
                        y[i] = h + s * 4;
                        x[i] = random.nextFloat() * w;
                    }
                    break;
                default: // stars — мерцают на месте и медленно дрейфуют
                    x[i] += speed[i] * 0.15f * dt;
                    if (x[i] > w + s) {
                        x[i] = -s;
                    }
                    break;
            }
            float twinkle = "stars".equals(profile.particles) ? 0.35f + 0.65f * (0.5f + 0.5f * (float) Math.sin(t * 2.2f + phase[i] * 3)) : 0.85f;
            paint.setColor(color);
            paint.setAlpha(Math.round(baseAlpha * twinkle));
            drawParticle(canvas, x[i], y[i], s);
        }
        canvas.restore();
        if (animate) {
            postInvalidateOnAnimation();
        }
    }

    private void drawParticle(Canvas canvas, float cx, float cy, float s) {
        switch (profile.particles) {
            case "bubbles":
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(AndroidUtilities.dp(1));
                canvas.drawCircle(cx, cy, s * 1.8f, paint);
                paint.setStyle(Paint.Style.FILL);
                break;
            case "hearts":
                float k = s * 1.6f;
                heart.rewind();
                heart.moveTo(cx, cy + k);
                heart.cubicTo(cx - k * 2, cy - k * 0.2f, cx - k * 0.8f, cy - k * 1.6f, cx, cy - k * 0.5f);
                heart.cubicTo(cx + k * 0.8f, cy - k * 1.6f, cx + k * 2, cy - k * 0.2f, cx, cy + k);
                heart.close();
                canvas.drawPath(heart, paint);
                break;
            case "stars":
                // четырёхлучевая искра
                paint.setStrokeWidth(Math.max(1f, s * 0.45f));
                paint.setStrokeCap(Paint.Cap.ROUND);
                canvas.drawLine(cx - s * 1.6f, cy, cx + s * 1.6f, cy, paint);
                canvas.drawLine(cx, cy - s * 1.6f, cx, cy + s * 1.6f, paint);
                break;
            default:
                canvas.drawCircle(cx, cy, s, paint);
                break;
        }
    }
}
