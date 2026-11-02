package org.telegram.custom.edit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.telegram.custom.CustomStrings;
import org.telegram.custom.core.Colors;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

/** Простой HSV-пикер с прозрачностью и полем HEX. */
final class ColorPickerDialog {

    interface Callback {
        void onPicked(Integer argb);
    }

    private ColorPickerDialog() {
    }

    static void show(Context context, String title, int initial, Callback callback) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(8), AndroidUtilities.dp(20), 0);

        HsvView picker = new HsvView(context, initial);
        layout.addView(picker, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 260));

        EditText hex = new EditText(context);
        hex.setHint(CustomStrings.get("hex"));
        hex.setSingleLine(true);
        hex.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        hex.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        hex.setHintTextColor(Theme.getColor(Theme.key_dialogTextHint));
        hex.setGravity(Gravity.CENTER);
        hex.setText(Colors.format(initial));
        layout.addView(hex, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 8, 0, 0));

        final boolean[] fromText = {false};
        picker.listener = argb -> {
            if (!fromText[0]) {
                hex.setText(Colors.format(argb));
            }
        };
        hex.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Integer c = Colors.parse(s.toString());
                if (c != null && c != picker.color()) {
                    fromText[0] = true;
                    picker.setColor(c);
                    fromText[0] = false;
                }
            }
        });

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(layout)
                .setPositiveButton(CustomStrings.get("done"), (d, w) -> callback.onPicked(picker.color()))
                .setNeutralButton(CustomStrings.get("reset"), (d, w) -> callback.onPicked(null))
                .setNegativeButton(CustomStrings.get("cancel"), null)
                .show();
    }

    /** Квадрат насыщенность/яркость, под ним полосы оттенка и прозрачности. */
    @SuppressLint("ViewConstructor")
    static final class HsvView extends View {

        interface Listener {
            void onColor(int argb);
        }

        Listener listener;
        private final float[] hsv = new float[3];
        private int alpha;

        private final Paint svPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint huePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint alphaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint checkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF sv = new RectF(), hue = new RectF(), alphaBar = new RectF();
        private int dragging; // 0 — нет, 1 — квадрат, 2 — оттенок, 3 — прозрачность
        private float lastHue = -1;

        HsvView(Context context, int initial) {
            super(context);
            setColorInternal(initial);
            thumbPaint.setStyle(Paint.Style.STROKE);
            thumbPaint.setStrokeWidth(AndroidUtilities.dp(2));
            thumbPaint.setColor(Color.WHITE);
            thumbPaint.setShadowLayer(AndroidUtilities.dp(1.5f), 0, 0, 0x80000000);
            checkerPaint.setColor(0xFFCCCCCC);
        }

        int color() {
            return Color.HSVToColor(alpha, hsv);
        }

        void setColor(int argb) {
            setColorInternal(argb);
            invalidate();
            if (listener != null) {
                listener.onColor(color());
            }
        }

        private void setColorInternal(int argb) {
            Color.colorToHSV(argb, hsv);
            alpha = Color.alpha(argb);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            float bar = AndroidUtilities.dp(24), gap = AndroidUtilities.dp(12);
            alphaBar.set(0, h - bar, w, h);
            hue.set(0, alphaBar.top - gap - bar, w, alphaBar.top - gap);
            sv.set(0, 0, w, hue.top - gap);
            huePaint.setShader(new LinearGradient(0, 0, w, 0,
                    new int[]{0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000}, null, Shader.TileMode.CLAMP));
            lastHue = -1;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float r = AndroidUtilities.dp(8);
            if (lastHue != hsv[0]) {
                lastHue = hsv[0];
                int pure = Color.HSVToColor(new float[]{hsv[0], 1f, 1f});
                Shader sat = new LinearGradient(sv.left, 0, sv.right, 0, Color.WHITE, pure, Shader.TileMode.CLAMP);
                Shader val = new LinearGradient(0, sv.top, 0, sv.bottom, Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP);
                svPaint.setShader(new ComposeShader(sat, val, PorterDuff.Mode.MULTIPLY));
            }
            canvas.drawRoundRect(sv, r, r, svPaint);
            canvas.drawRoundRect(hue, r, r, huePaint);

            // шахматка под полосой прозрачности
            float cell = AndroidUtilities.dp(6);
            canvas.save();
            canvas.clipRect(alphaBar);
            for (float x = alphaBar.left; x < alphaBar.right; x += cell) {
                for (float y = alphaBar.top; y < alphaBar.bottom; y += cell) {
                    if ((((int) ((x - alphaBar.left) / cell) + (int) ((y - alphaBar.top) / cell)) & 1) == 0) {
                        canvas.drawRect(x, y, x + cell, y + cell, checkerPaint);
                    }
                }
            }
            canvas.restore();
            int opaque = Color.HSVToColor(255, hsv);
            alphaPaint.setShader(new LinearGradient(alphaBar.left, 0, alphaBar.right, 0, opaque & 0x00FFFFFF, opaque, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(alphaBar, r, r, alphaPaint);

            float tr = AndroidUtilities.dp(9);
            canvas.drawCircle(sv.left + hsv[1] * sv.width(), sv.top + (1f - hsv[2]) * sv.height(), tr, thumbPaint);
            canvas.drawCircle(hue.left + hsv[0] / 360f * hue.width(), hue.centerY(), tr, thumbPaint);
            canvas.drawCircle(alphaBar.left + alpha / 255f * alphaBar.width(), alphaBar.centerY(), tr, thumbPaint);
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent e) {
            float x = e.getX(), y = e.getY();
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    dragging = sv.contains(x, y) ? 1 : hue.contains(x, y) ? 2 : alphaBar.contains(x, y) ? 3 : 0;
                    if (dragging != 0) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    // fallthrough
                case MotionEvent.ACTION_MOVE:
                    if (dragging == 1) {
                        hsv[1] = clamp((x - sv.left) / sv.width());
                        hsv[2] = 1f - clamp((y - sv.top) / sv.height());
                    } else if (dragging == 2) {
                        hsv[0] = clamp((x - hue.left) / hue.width()) * 359.9f;
                    } else if (dragging == 3) {
                        alpha = Math.round(clamp((x - alphaBar.left) / alphaBar.width()) * 255);
                    } else {
                        return false;
                    }
                    invalidate();
                    if (listener != null) {
                        listener.onColor(color());
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    dragging = 0;
                    return true;
            }
            return super.onTouchEvent(e);
        }

        private static float clamp(float v) {
            return v < 0 ? 0 : v > 1 ? 1 : v;
        }
    }
}
