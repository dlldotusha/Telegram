package org.nextgram.engine.edit

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import org.nextgram.engine.EngineStrings
import org.nextgram.engine.NextEngine
import org.telegram.messenger.AndroidUtilities
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.spoilers.SpoilerEffect

/**
 * Слой поверх окна в режиме редактирования: панель с подсказкой и «Готово»,
 * рамка вокруг выбранного элемента и спойлеры над скрытыми частями.
 * Сам слой касания не ловит — они проходят в интерфейс (кроме панели).
 */
@SuppressLint("ViewConstructor")
internal class EditOverlay(context: Context) : FrameLayout(context) {

    private val toolbar = LinearLayout(context)
    private val hintView = TextView(context)

    private val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = AndroidUtilities.dp(2f).toFloat()
        color = Theme.getColor(Theme.key_featuredStickers_addButton)
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

        toolbar.orientation = LinearLayout.HORIZONTAL
        toolbar.gravity = Gravity.CENTER_VERTICAL
        toolbar.background = GradientDrawable().apply {
            cornerRadius = AndroidUtilities.dp(22f).toFloat()
            setColor(0xE6202124.toInt())
        }
        toolbar.setPadding(AndroidUtilities.dp(16f), 0, AndroidUtilities.dp(6f), 0)
        toolbar.isClickable = true

        hintView.setTextColor(0xFFFFFFFF.toInt())
        hintView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
        hintView.maxLines = 2
        hintView.ellipsize = TextUtils.TruncateAt.END
        toolbar.addView(hintView, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, Gravity.CENTER_VERTICAL, 0, 8, 8, 8))

        val doneView = TextView(context).apply {
            text = EngineStrings.get("done")
            setTextColor(0xFF000000.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            typeface = AndroidUtilities.bold()
            gravity = Gravity.CENTER
            setPadding(AndroidUtilities.dp(14f), 0, AndroidUtilities.dp(14f), 0)
            background = GradientDrawable().apply {
                cornerRadius = AndroidUtilities.dp(16f).toFloat()
                setColor(0xFFFFFFFF.toInt())
            }
            setOnClickListener { EditMode.onDone() }
        }
        toolbar.addView(doneView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 32, Gravity.CENTER_VERTICAL))

        addView(toolbar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44f, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 16f, 0f, 16f, 16f))
        // Панель над навигационной панелью системы.
        setOnApplyWindowInsetsListener { _, insets ->
            val p = toolbar.layoutParams as LayoutParams
            @Suppress("DEPRECATION")
            p.bottomMargin = AndroidUtilities.dp(16f) + insets.systemWindowInsetBottom
            toolbar.layoutParams = p
            insets
        }
        updateHint()
    }

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
        toolbar.getLocationOnScreen(loc)
        return rawX >= loc[0] && rawY >= loc[1] && rawX < loc[0] + toolbar.width && rawY < loc[1] + toolbar.height
    }

    fun onSelectionChanged() {
        updateHint()
        collect()
        invalidate()
    }

    private fun updateHint() {
        val t = EditMode.selected
        hintView.text = when {
            EditMode.isHideByTap && t != null ->
                EngineStrings.get("hide_by_tap_hint") + " · " + EditMode.currentScope(ScopeOption.forTarget(t)).label
            t != null -> t.title
            else -> EngineStrings.get("customize_info")
        }
    }

    /** Пересчитать рамку выбранного элемента и прямоугольники скрытых частей. */
    private fun collect() {
        getLocationOnScreen(ownLoc)
        val t = EditMode.selected
        hasSelectedRect = t != null && t.screenBounds(selectedRect)
        if (hasSelectedRect) selectedRect.offset(-ownLoc[0].toFloat(), -ownLoc[1].toFloat())

        hiddenRects.clear()
        HitTest.forEachVisible(rootView, this) { v ->
            val spec = SpecRegistry.find(v) ?: return@forEachVisible
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
}
