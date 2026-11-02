package org.nextgram.engine.edit

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.nextgram.engine.EngineStrings
import org.nextgram.engine.NextEngine
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.spoilers.SpoilerEffect
import kotlin.math.abs

/**
 * Слой поверх окна в режиме редактирования: плавающая панель в стиле Telegram,
 * рамка вокруг выбранного элемента и спойлеры над скрытыми частями.
 *
 * Панель берёт цвета из темы (фон пунктов, текст, акцентная кнопка), поэтому
 * перекрашивается вместе с остальным интерфейсом. Её можно перетащить за любое
 * место и свернуть в круглую кнопку — положение запоминается.
 */
@SuppressLint("ViewConstructor")
internal class EditOverlay(context: Context) : FrameLayout(context) {

    private val panel = LinearLayout(context)
    private val icon = ImageView(context)
    private val hintView = TextView(context)
    private val collapseView = ImageView(context)
    private val doneView = TextView(context)
    private var collapsed = prefs().getBoolean("collapsed", false)

    private val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = AndroidUtilities.dp(2f).toFloat()
    }
    private val tmp = RectF()
    private val selectedRect = RectF()
    private var hasSelectedRect = false

    /** Скрытые части на экране, в координатах слоя. */
    private val hiddenRects = ArrayList<RectF>()
    private val spoilers = ArrayList<SpoilerEffect>()
    private val loc = IntArray(2)
    private val ownLoc = IntArray(2)
    private var observer: ViewTreeObserver? = null
    private var bottomInset = 0

    private val preDraw = ViewTreeObserver.OnPreDrawListener {
        val hadSomething = hasSelectedRect || hiddenRects.isNotEmpty()
        collect()
        if (hadSomething || hasSelectedRect || hiddenRects.isNotEmpty()) invalidate()
        true
    }

    init {
        setWillNotDraw(false)
        isClickable = false
        isFocusable = false

        panel.orientation = LinearLayout.HORIZONTAL
        panel.gravity = Gravity.CENTER_VERTICAL
        panel.elevation = AndroidUtilities.dp(6f).toFloat()
        panel.isClickable = true

        icon.setImageResource(R.drawable.msg_customize)
        icon.scaleType = ImageView.ScaleType.CENTER
        icon.setOnClickListener { if (collapsed) setCollapsed(false) }
        panel.addView(icon, LayoutHelper.createLinear(44, 44, Gravity.CENTER_VERTICAL))

        hintView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        hintView.maxLines = 2
        hintView.ellipsize = TextUtils.TruncateAt.END
        panel.addView(hintView, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, Gravity.CENTER_VERTICAL, 0, 0, 4, 0))

        collapseView.setImageResource(R.drawable.msg_close)
        collapseView.scaleType = ImageView.ScaleType.CENTER
        collapseView.contentDescription = EngineStrings.get("collapse")
        collapseView.setOnClickListener { setCollapsed(true) }
        panel.addView(collapseView, LayoutHelper.createLinear(36, 36, Gravity.CENTER_VERTICAL))

        doneView.text = EngineStrings.get("done")
        doneView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        doneView.typeface = AndroidUtilities.bold()
        doneView.gravity = Gravity.CENTER
        doneView.setPadding(AndroidUtilities.dp(14f), 0, AndroidUtilities.dp(14f), 0)
        doneView.setOnClickListener { EditMode.onDone() }
        panel.addView(doneView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 32, Gravity.CENTER_VERTICAL, 2, 0, 6, 0))

        addView(panel, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 44, Gravity.TOP or Gravity.LEFT))
        setupDrag()
        setOnApplyWindowInsetsListener { _, insets ->
            @Suppress("DEPRECATION")
            bottomInset = insets.systemWindowInsetBottom
            insets
        }
        updateColors()
        applyCollapsed()
        updateHint()
    }

    // ------------------------------------------------------------------ внешний вид

    /** Цвета берутся из темы на каждом показе — панель следует за изменениями цветов. */
    private fun updateColors() {
        val bg = Theme.getColor(Theme.key_windowBackgroundWhite)
        val textColor = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText)
        val iconColor = Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon)
        val accent = Theme.getColor(Theme.key_featuredStickers_addButton)
        panel.background = GradientDrawable().apply {
            cornerRadius = AndroidUtilities.dp(22f).toFloat()
            setColor(bg)
        }
        hintView.setTextColor(textColor)
        icon.colorFilter = PorterDuffColorFilter(if (collapsed) accent else iconColor, PorterDuff.Mode.SRC_IN)
        icon.background = Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), Theme.RIPPLE_MASK_CIRCLE_20DP)
        collapseView.colorFilter = PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN)
        collapseView.background = Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), Theme.RIPPLE_MASK_CIRCLE_20DP)
        doneView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText))
        doneView.background = Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(16f), accent, Theme.getColor(Theme.key_featuredStickers_addButtonPressed))
        framePaint.color = accent
    }

    private fun setCollapsed(value: Boolean) {
        collapsed = value
        prefs().edit().putBoolean("collapsed", value).apply()
        applyCollapsed()
        updateColors()
    }

    private fun applyCollapsed() {
        val v = if (collapsed) GONE else VISIBLE
        hintView.visibility = v
        collapseView.visibility = v
        doneView.visibility = v
        panel.post { clampPanel() }
    }

    // ------------------------------------------------------------------ перетаскивание

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDrag() {
        val slop = ViewConfiguration.get(context).scaledTouchSlop
        var startX = 0f
        var startY = 0f
        var baseX = 0f
        var baseY = 0f
        var dragging = false
        val listener = OnTouchListener { v, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = e.rawX
                    startY = e.rawY
                    baseX = panel.translationX
                    baseY = panel.translationY
                    dragging = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!dragging && (abs(e.rawX - startX) > slop || abs(e.rawY - startY) > slop)) {
                        dragging = true
                        v.isPressed = false
                        v.cancelLongPress()
                        val cancel = MotionEvent.obtain(e).apply { action = MotionEvent.ACTION_CANCEL }
                        v.onTouchEvent(cancel)
                        cancel.recycle()
                    }
                    if (dragging) {
                        panel.translationX = baseX + e.rawX - startX
                        panel.translationY = baseY + e.rawY - startY
                        clampPanel()
                        return@OnTouchListener true
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> if (dragging) {
                    dragging = false
                    savePosition()
                    return@OnTouchListener true
                }
            }
            false
        }
        // Тащить можно за любое место панели, кнопки при этом не нажимаются.
        panel.setOnTouchListener(listener)
        icon.setOnTouchListener(listener)
        hintView.setOnTouchListener(listener)
        collapseView.setOnTouchListener(listener)
        doneView.setOnTouchListener(listener)
    }

    private fun clampPanel() {
        if (width == 0 || panel.width == 0) return
        val m = AndroidUtilities.dp(8f).toFloat()
        panel.translationX = panel.translationX.coerceIn(m, (width - panel.width - m).coerceAtLeast(m))
        panel.translationY = panel.translationY.coerceIn(AndroidUtilities.statusBarHeight + m, (height - panel.height - bottomInset - m).coerceAtLeast(m))
    }

    private fun savePosition() {
        if (width == 0 || height == 0) return
        prefs().edit()
            .putFloat("x", (panel.translationX + panel.width / 2f) / width)
            .putFloat("y", (panel.translationY + panel.height / 2f) / height)
            .apply()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) restorePosition()
    }

    private fun restorePosition() {
        val p = prefs()
        // По умолчанию — внизу по центру, над полем ввода.
        val cx = p.getFloat("x", 0.5f) * width
        val cy = p.getFloat("y", 0.8f) * height
        panel.translationX = cx - panel.width / 2f
        panel.translationY = cy - panel.height / 2f
        clampPanel()
    }

    // ------------------------------------------------------------------ жизненный цикл

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        observer = rootView.viewTreeObserver.also { it.addOnPreDrawListener(preDraw) }
        requestApplyInsets()
    }

    override fun onDetachedFromWindow() {
        observer?.takeIf { it.isAlive }?.removeOnPreDrawListener(preDraw)
        observer = null
        super.onDetachedFromWindow()
    }

    fun detach() {
        (parent as? ViewGroup)?.removeView(this)
    }

    fun isOnToolbar(rawX: Float, rawY: Float): Boolean {
        panel.getLocationOnScreen(loc)
        return rawX >= loc[0] && rawY >= loc[1] && rawX < loc[0] + panel.width && rawY < loc[1] + panel.height
    }

    fun onSelectionChanged() {
        updateColors()
        updateHint()
        collect()
        invalidate()
    }

    private fun updateHint() {
        val t = EditMode.selected
        hintView.text = when {
            EditMode.isHideByTap && t != null ->
                EngineStrings.get("hide_by_tap_hint") + " · " + EditMode.currentScope(ScopeOption.forTarget(t)).label
            t != null -> t.partInfo()?.let { t.title + " · " + it.title } ?: t.title
            else -> EngineStrings.get("customize_info")
        }
        panel.post { clampPanel() }
    }

    // ------------------------------------------------------------------ рамка и спойлеры

    /** Пересчитать рамку выбранного элемента и прямоугольники скрытых частей. */
    private fun collect() {
        getLocationOnScreen(ownLoc)
        val t = EditMode.selected
        hasSelectedRect = t != null && t.screenBounds(selectedRect)
        if (hasSelectedRect) selectedRect.offset(-ownLoc[0].toFloat(), -ownLoc[1].toFloat())

        hiddenRects.clear()
        HitTest.forEachVisible(rootView, this) { v ->
            val spec = SpecRegistry.find(v) ?: return@forEachVisible
            if (spec.parts(v).none { it.hideable }) return@forEachVisible
            val style = NextEngine.resolve(spec.account(v), spec.dialogId(v), spec.elementId(v))
            if (style.isEmpty) return@forEachVisible
            for (part in spec.parts(v)) {
                if (!part.hideable || !style.getBool(part.id, "hidden", false) || !spec.partBounds(v, part.id, tmp)) continue
                v.getLocationOnScreen(loc)
                hiddenRects += RectF(tmp).apply { offset((loc[0] - ownLoc[0]).toFloat(), (loc[1] - ownLoc[1]).toFloat()) }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        while (spoilers.size < hiddenRects.size) {
            spoilers += SpoilerEffect().apply {
                setColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText))
                setParentView(this@EditOverlay)
            }
        }
        hiddenRects.forEachIndexed { i, r ->
            spoilers[i].setBounds(r.left.toInt(), r.top.toInt(), r.right.toInt(), r.bottom.toInt())
            spoilers[i].draw(canvas)
        }
        if (hasSelectedRect) {
            val d = AndroidUtilities.dp(1f).toFloat()
            tmp.set(selectedRect.left + d, selectedRect.top + d, selectedRect.right - d, selectedRect.bottom - d)
            val r = AndroidUtilities.dp(6f).toFloat()
            canvas.drawRoundRect(tmp, r, r, framePaint)
        }
    }

    private companion object {
        fun prefs() = ApplicationLoader.applicationContext.getSharedPreferences("nextgram_edit_panel", Context.MODE_PRIVATE)
    }
}
