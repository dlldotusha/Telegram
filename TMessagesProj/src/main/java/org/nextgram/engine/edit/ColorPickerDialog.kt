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
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import org.nextgram.engine.EngineStrings
import org.nextgram.engine.core.Colors
import org.telegram.messenger.AndroidUtilities
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.LayoutHelper
import kotlin.math.roundToInt

/**
 * Выбор цвета в стиле диалогов Telegram: квадрат цвета, ползунки «Оттенок»
 * и «Прозрачность» с подписями, поле HEX и сравнение «было/стало».
 */
object ColorPickerDialog {

    fun interface Callback {
        /** null — сбросить значение. */
        fun onPicked(argb: Int?)
    }

    /** Общее состояние трёх ползунков. */
    private class State(initial: Int) {
        val hsv = FloatArray(3)
        var alpha = 0
        val listeners = ArrayList<() -> Unit>()

        init {
            set(initial)
        }

        val color: Int get() = Color.HSVToColor(alpha, hsv)

        fun set(argb: Int) {
            Color.colorToHSV(argb, hsv)
            alpha = Color.alpha(argb)
        }

        fun changed() = listeners.forEach { it() }
    }

    @JvmStatic
    fun show(context: Context, title: String, initial: Int, callback: Callback) {
        val state = State(initial)
        val text = Theme.getColor(Theme.key_dialogTextBlack)
        val gray = Theme.getColor(Theme.key_dialogTextGray3)

        fun label(s: String) = TextView(context).apply {
            this.text = s
            setTextColor(gray)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(AndroidUtilities.dp(24f), AndroidUtilities.dp(4f), AndroidUtilities.dp(24f), 0)
        }

        // было / стало
        val preview = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        val oldSwatch = View(context).apply { background = swatch(initial, left = true) }
        val newSwatch = View(context)
        preview.addView(oldSwatch, LayoutHelper.createLinear(0, 28, 1f))
        preview.addView(newSwatch, LayoutHelper.createLinear(0, 28, 1f))
        layout.addView(preview, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 28, 0f, 0f, 0f, 12f))

        layout.addView(SvView(context, state), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 180))
        layout.addView(label(EngineStrings.get("hue")), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0f, 14f, 0f, 6f))
        layout.addView(BarView(context, state, hue = true), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 24))
        val opacityLabel = label("")
        layout.addView(opacityLabel, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0f, 14f, 0f, 6f))
        layout.addView(BarView(context, state, hue = false), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 24))

        val hex = EditText(context).apply {
            hint = EngineStrings.get("hex")
            isSingleLine = true
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            setTextColor(text)
            setHintTextColor(Theme.getColor(Theme.key_dialogTextHint))
            gravity = Gravity.CENTER
            setText(Colors.format(initial))
        }
        layout.addView(hex, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0f, 8f, 0f, 0f))

        var fromText = false
        val update = {
            newSwatch.background = swatch(state.color, left = false)
            opacityLabel.text = EngineStrings.get("opacity") + " · " + (100 - state.alpha * 100 / 255) + "%"
            if (!fromText) hex.setText(Colors.format(state.color))
        }
        state.listeners += update
        update()
        hex.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val c = Colors.parse(s?.toString()) ?: return
                if (c != state.color) {
                    fromText = true
                    state.set(c)
                    state.changed()
                    fromText = false
                }
            }
        })

        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(layout)
            .setPositiveButton(EngineStrings.get("done")) { _, _ -> callback.onPicked(state.color) }
            .setNeutralButton(EngineStrings.get("reset")) { _, _ -> callback.onPicked(null) }
            .setNegativeButton(EngineStrings.get("cancel"), null)
            .show()
    }

    private fun swatch(color: Int, left: Boolean): GradientDrawable {
        val r = AndroidUtilities.dp(8f).toFloat()
        return GradientDrawable().apply {
            setColor(color)
            cornerRadii = if (left) floatArrayOf(r, r, 0f, 0f, 0f, 0f, r, r) else floatArrayOf(0f, 0f, r, r, r, r, 0f, 0f)
        }
    }

    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = AndroidUtilities.dp(2.5f).toFloat()
        color = Color.WHITE
        setShadowLayer(AndroidUtilities.dp(1.5f).toFloat(), 0f, 0f, 0x80000000.toInt())
    }

    /** Квадрат «насыщенность × яркость». */
    @SuppressLint("ViewConstructor")
    private class SvView(context: Context, private val state: State) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rect = RectF()
        private var shaderHue = -1f

        init {
            state.listeners += { invalidate() }
            setLayerType(LAYER_TYPE_SOFTWARE, null) // тень бегунка
        }

        override fun onDraw(canvas: Canvas) {
            rect.set(0f, 0f, width.toFloat(), height.toFloat())
            if (shaderHue != state.hsv[0] || paint.shader == null) {
                shaderHue = state.hsv[0]
                val pure = Color.HSVToColor(floatArrayOf(state.hsv[0], 1f, 1f))
                paint.shader = ComposeShader(
                    LinearGradient(0f, 0f, rect.right, 0f, Color.WHITE, pure, Shader.TileMode.CLAMP),
                    LinearGradient(0f, 0f, 0f, rect.bottom, Color.WHITE, Color.BLACK, Shader.TileMode.CLAMP),
                    PorterDuff.Mode.MULTIPLY,
                )
            }
            val r = AndroidUtilities.dp(10f).toFloat()
            canvas.drawRoundRect(rect, r, r, paint)
            canvas.drawCircle(state.hsv[1] * width, (1f - state.hsv[2]) * height, AndroidUtilities.dp(10f).toFloat(), thumbPaint)
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            paint.shader = null
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(e: MotionEvent): Boolean {
            if (e.actionMasked == MotionEvent.ACTION_DOWN) parent.requestDisallowInterceptTouchEvent(true)
            state.hsv[1] = (e.x / width).coerceIn(0f, 1f)
            state.hsv[2] = 1f - (e.y / height).coerceIn(0f, 1f)
            state.changed()
            return true
        }
    }

    /** Ползунок оттенка или прозрачности. */
    @SuppressLint("ViewConstructor")
    private class BarView(context: Context, private val state: State, private val hue: Boolean) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val checker = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFCCCCCC.toInt() }
        private val rect = RectF()

        init {
            state.listeners += { invalidate() }
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }

        override fun onDraw(canvas: Canvas) {
            val pad = AndroidUtilities.dp(10f).toFloat()
            rect.set(0f, height * 0.2f, width.toFloat(), height * 0.8f)
            val r = rect.height() / 2
            if (hue) {
                paint.shader = LinearGradient(
                    0f, 0f, rect.right, 0f,
                    intArrayOf(0xFFFF0000.toInt(), 0xFFFFFF00.toInt(), 0xFF00FF00.toInt(), 0xFF00FFFF.toInt(), 0xFF0000FF.toInt(), 0xFFFF00FF.toInt(), 0xFFFF0000.toInt()),
                    null, Shader.TileMode.CLAMP,
                )
            } else {
                // шахматка — чтобы прозрачность была видна
                canvas.save()
                canvas.clipRect(rect)
                val cell = rect.height() / 2
                var x = 0f
                var i = 0
                while (x < rect.right) {
                    canvas.drawRect(x, if (i % 2 == 0) rect.top else rect.top + cell, x + cell, if (i % 2 == 0) rect.top + cell else rect.bottom, checker)
                    x += cell
                    i++
                }
                canvas.restore()
                val opaque = Color.HSVToColor(255, state.hsv)
                paint.shader = LinearGradient(0f, 0f, rect.right, 0f, opaque and 0x00FFFFFF, opaque, Shader.TileMode.CLAMP)
            }
            canvas.drawRoundRect(rect, r, r, paint)
            val pos = if (hue) state.hsv[0] / 360f else state.alpha / 255f
            val cx = pad + pos * (width - 2 * pad)
            canvas.drawCircle(cx, height / 2f, AndroidUtilities.dp(10f).toFloat(), thumbPaint)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouchEvent(e: MotionEvent): Boolean {
            if (e.actionMasked == MotionEvent.ACTION_DOWN) parent.requestDisallowInterceptTouchEvent(true)
            val pad = AndroidUtilities.dp(10f).toFloat()
            val f = ((e.x - pad) / (width - 2 * pad)).coerceIn(0f, 1f)
            if (hue) state.hsv[0] = f * 359.9f else state.alpha = (f * 255).roundToInt()
            state.changed()
            return true
        }
    }
}
