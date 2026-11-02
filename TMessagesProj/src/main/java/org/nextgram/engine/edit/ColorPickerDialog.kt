package org.nextgram.engine.edit

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Shader
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import org.nextgram.engine.EngineStrings
import org.nextgram.engine.core.Colors
import org.telegram.messenger.AndroidUtilities
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.LayoutHelper
import kotlin.math.roundToInt

/** Простой HSV-пикер с прозрачностью и полем HEX. */
object ColorPickerDialog {

    fun interface Callback {
        /** null — сбросить значение. */
        fun onPicked(argb: Int?)
    }

    @JvmStatic
    fun show(context: Context, title: String, initial: Int, callback: Callback) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(AndroidUtilities.dp(20f), AndroidUtilities.dp(8f), AndroidUtilities.dp(20f), 0)
        }
        val picker = HsvView(context, initial)
        layout.addView(picker, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 260))

        val hex = EditText(context).apply {
            hint = EngineStrings.get("hex")
            isSingleLine = true
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            setTextColor(Theme.getColor(Theme.key_dialogTextBlack))
            setHintTextColor(Theme.getColor(Theme.key_dialogTextHint))
            gravity = Gravity.CENTER
            setText(Colors.format(initial))
        }
        layout.addView(hex, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0f, 8f, 0f, 0f))

        var fromText = false
        picker.listener = { argb -> if (!fromText) hex.setText(Colors.format(argb)) }
        hex.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val c = Colors.parse(s?.toString()) ?: return
                if (c != picker.color) {
                    fromText = true
                    picker.color = c
                    fromText = false
                }
            }
        })

        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(layout)
            .setPositiveButton(EngineStrings.get("done")) { _, _ -> callback.onPicked(picker.color) }
            .setNeutralButton(EngineStrings.get("reset")) { _, _ -> callback.onPicked(null) }
            .setNegativeButton(EngineStrings.get("cancel"), null)
            .show()
    }

    /** Квадрат насыщенность/яркость, под ним полосы оттенка и прозрачности. */
    @SuppressLint("ViewConstructor")
    private class HsvView(context: Context, initial: Int) : View(context) {

        var listener: ((Int) -> Unit)? = null
        private val hsv = FloatArray(3)
        private var alpha = 0

        private val svPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val huePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val alphaPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val checkerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFCCCCCC.toInt() }
        private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = AndroidUtilities.dp(2f).toFloat()
            color = Color.WHITE
            setShadowLayer(AndroidUtilities.dp(1.5f).toFloat(), 0f, 0f, 0x80000000.toInt())
        }
        private val sv = RectF()
        private val hue = RectF()
        private val alphaBar = RectF()
        private var dragging = 0 // 0 — нет, 1 — квадрат, 2 — оттенок, 3 — прозрачность
        private var lastHue = -1f

        var color: Int
            get() = Color.HSVToColor(alpha, hsv)
            set(argb) {
                setColorInternal(argb)
                invalidate()
                listener?.invoke(color)
            }

        init {
            setColorInternal(initial)
        }

        private fun setColorInternal(argb: Int) {
            Color.colorToHSV(argb, hsv)
            alpha = Color.alpha(argb)
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            val bar = AndroidUtilities.dp(24f).toFloat()
            val gap = AndroidUtilities.dp(12f).toFloat()
            alphaBar.set(0f, h - bar, w.toFloat(), h.toFloat())
            hue.set(0f, alphaBar.top - gap - bar, w.toFloat(), alphaBar.top - gap)
            sv.set(0f, 0f, w.toFloat(), hue.top - gap)
            huePaint.shader = LinearGradient(
                0f, 0f, w.toFloat(), 0f,
                intArrayOf(0xFFFF0000.toInt(), 0xFFFFFF00.toInt(), 0xFF00FF00.toInt(), 0xFF00FFFF.toInt(), 0xFF0000FF.toInt(), 0xFFFF00FF.toInt(), 0xFFFF0000.toInt()),
                null, Shader.TileMode.CLAMP,
            )
            lastHue = -1f
        }

        override fun onDraw(canvas: Canvas) {
            val r = AndroidUtilities.dp(8f).toFloat()
            if (lastHue != hsv[0]) {
                lastHue = hsv[0]
                val pure = Color.HSVToColor(floatArrayOf(hsv[0], 1f, 1f))
                val sat = LinearGradient(sv.left, 0f, sv.right, 0f, Color.WHITE, pure, Shader.TileMode.CLAMP)
                val value = LinearGradient(0f, sv.top, 0f, sv.bottom, Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP)
                svPaint.shader = ComposeShader(sat, value, PorterDuff.Mode.MULTIPLY)
            }
            canvas.drawRoundRect(sv, r, r, svPaint)
            canvas.drawRoundRect(hue, r, r, huePaint)

            // шахматка под полосой прозрачности
            val cell = AndroidUtilities.dp(6f).toFloat()
            canvas.save()
            canvas.clipRect(alphaBar)
            var x = alphaBar.left
            while (x < alphaBar.right) {
                var y = alphaBar.top
                while (y < alphaBar.bottom) {
                    if ((((x - alphaBar.left) / cell).toInt() + ((y - alphaBar.top) / cell).toInt()) and 1 == 0) {
                        canvas.drawRect(x, y, x + cell, y + cell, checkerPaint)
                    }
                    y += cell
                }
                x += cell
            }
            canvas.restore()
            val opaque = Color.HSVToColor(255, hsv)
            alphaPaint.shader = LinearGradient(alphaBar.left, 0f, alphaBar.right, 0f, opaque and 0x00FFFFFF, opaque, Shader.TileMode.CLAMP)
            canvas.drawRoundRect(alphaBar, r, r, alphaPaint)

            val tr = AndroidUtilities.dp(9f).toFloat()
            canvas.drawCircle(sv.left + hsv[1] * sv.width(), sv.top + (1f - hsv[2]) * sv.height(), tr, thumbPaint)
            canvas.drawCircle(hue.left + hsv[0] / 360f * hue.width(), hue.centerY(), tr, thumbPaint)
            canvas.drawCircle(alphaBar.left + alpha / 255f * alphaBar.width(), alphaBar.centerY(), tr, thumbPaint)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(e: MotionEvent): Boolean {
            val x = e.x
            val y = e.y
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    if (e.actionMasked == MotionEvent.ACTION_DOWN) {
                        dragging = when {
                            sv.contains(x, y) -> 1
                            hue.contains(x, y) -> 2
                            alphaBar.contains(x, y) -> 3
                            else -> 0
                        }
                        if (dragging != 0) parent.requestDisallowInterceptTouchEvent(true)
                    }
                    when (dragging) {
                        1 -> {
                            hsv[1] = clamp((x - sv.left) / sv.width())
                            hsv[2] = 1f - clamp((y - sv.top) / sv.height())
                        }
                        2 -> hsv[0] = clamp((x - hue.left) / hue.width()) * 359.9f
                        3 -> alpha = (clamp((x - alphaBar.left) / alphaBar.width()) * 255).roundToInt()
                        else -> return false
                    }
                    invalidate()
                    listener?.invoke(color)
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    dragging = 0
                    return true
                }
            }
            return super.onTouchEvent(e)
        }

        private fun clamp(v: Float) = v.coerceIn(0f, 1f)
    }
}
