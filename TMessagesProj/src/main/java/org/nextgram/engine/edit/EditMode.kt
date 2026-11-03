package org.nextgram.engine.edit

import android.app.Activity
import android.graphics.RectF
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import org.nextgram.engine.NextEngine
import org.nextgram.engine.core.Scopes
import org.telegram.messenger.AndroidUtilities
import org.telegram.ui.Components.RecyclerListView
import org.telegram.ui.LaunchActivity
import java.lang.ref.WeakReference
import kotlin.math.abs

/**
 * Режим редактирования NextEngine.
 *
 * Обычные касания проходят в интерфейс как есть — по экранам можно ходить и
 * листать. Долгое нажатие (короче системного, чтобы не открылось меню
 * сообщения) отменяет жест у интерфейса и выбирает элемент под пальцем.
 *
 * Вход — из «Настройки → NextGram» или «Оформление», выход — «Готово» на панели.
 */
object EditMode {

    private const val LONG_PRESS_MS = 320L

    private var active = false
    private var activityRef: WeakReference<Activity>? = null
    private var overlay: EditOverlay? = null
    internal var selected: EditTarget? = null
        private set
    internal var isHideByTap = false
        private set
    private var sheet: EditorSheet? = null

    // Уровень, на который сохраняем правки. Запоминаем вид уровня, а не сам ключ:
    // выбрали «Этот чат» — в следующем чате тоже сохранится в «Этот чат».
    private var preferredKind = Scopes.Kind.CHAT
    private var preferredScope: String? = null

    private val configListener = Runnable { onConfigChanged() }

    // Состояние жеста
    private enum class Gesture { NONE, PENDING, CONSUMED, PART_TAP }

    private var gesture = Gesture.NONE
    private var downX = 0f
    private var downY = 0f
    private var downTime = 0L
    private val longPress = Runnable { onLongPress() }

    @JvmStatic
    fun isActive() = active

    @JvmStatic
    fun enter(activity: Activity?) {
        if (active || activity == null) return
        active = true
        activityRef = WeakReference(activity)
        val decor = activity.window.decorView as ViewGroup
        // Старые слои (если остались от прошлого входа) убираем, чтобы не перехватывали выбор.
        for (i in decor.childCount - 1 downTo 0) {
            if (decor.getChildAt(i) is EditOverlay) decor.removeViewAt(i)
        }
        overlay = EditOverlay(activity).also {
            decor.addView(it, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        }
        NextEngine.addListener(configListener)
        NextEngine.invalidateCache()
        refreshAll()
    }

    @JvmStatic
    fun exit() {
        if (!active) return
        active = false
        sheet?.let { s -> sheet = null; s.dismiss() }
        selected = null
        isHideByTap = false
        AndroidUtilities.cancelRunOnUIThread(longPress)
        gesture = Gesture.NONE
        NextEngine.removeListener(configListener)
        overlay?.detach()
        overlay = null
        refreshAll()
        ThemeApply.onEditModeExit()
        activityRef = null
    }

    internal fun activity(): Activity? = activityRef?.get()

    // ------------------------------------------------------------------ касания

    /** Хук из LaunchActivity.dispatchTouchEvent. true — событие съедено режимом. */
    @JvmStatic
    fun dispatchTouchEvent(activity: Activity, ev: MotionEvent): Boolean {
        val ov = overlay
        if (!active || ov == null || activity !== activity() || dispatchingCancel) return false
        if (ev.actionMasked == MotionEvent.ACTION_DOWN && ov.isOnToolbar(ev.rawX, ev.rawY)) {
            AndroidUtilities.cancelRunOnUIThread(longPress)
            gesture = Gesture.NONE
            return false
        }
        return handleTouch(activity.window.decorView, ev, activity)
    }

    /**
     * Хук из окон поверх экрана: BottomSheet, AlertDialog (root — окно диалога)
     * и всплывающие меню (root — корень попапа). Пока открыта панель свойств,
     * окна не трогаем: это наши собственные окна (свойства, выбор цвета).
     */
    @JvmStatic
    fun dispatchWindowTouch(root: View?, ev: MotionEvent): Boolean {
        if (!active || root == null || overlay == null || sheet != null || dispatchingCancel) return false
        val a = activity() ?: return false
        // Меню, показанное внутри окна приложения, уже обработано хуком активности.
        if (root === a.window.decorView) return false
        return handleTouch(root, ev, a)
    }

    @JvmStatic
    fun dispatchDialogTouch(dialog: android.app.Dialog, ev: MotionEvent): Boolean =
        if (!active) false else dispatchWindowTouch(dialog.window?.decorView, ev)

    private var gestureRoot: WeakReference<View>? = null
    private var dispatchingCancel = false

    private fun handleTouch(root: View, ev: MotionEvent, activity: Activity): Boolean {
        val x = ev.rawX
        val y = ev.rawY
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                AndroidUtilities.cancelRunOnUIThread(longPress)
                gestureRoot = WeakReference(root)
                downX = x
                downY = y
                downTime = ev.downTime
                val sel = selected
                if (isHideByTap && sel != null && isInside(sel, x, y)) {
                    gesture = Gesture.PART_TAP
                    return true
                }
                gesture = Gesture.PENDING
                AndroidUtilities.runOnUIThread(longPress, LONG_PRESS_MS)
                return false
            }
            MotionEvent.ACTION_POINTER_DOWN -> if (gesture == Gesture.PENDING) {
                AndroidUtilities.cancelRunOnUIThread(longPress)
                gesture = Gesture.NONE
            }
            MotionEvent.ACTION_MOVE -> if (gesture == Gesture.PENDING || gesture == Gesture.PART_TAP) {
                val slop = ViewConfiguration.get(activity).scaledTouchSlop
                if (abs(x - downX) > slop || abs(y - downY) > slop) {
                    AndroidUtilities.cancelRunOnUIThread(longPress)
                    // палец уехал: обычная прокрутка, либо это уже не тап по части
                    gesture = if (gesture == Gesture.PENDING) Gesture.NONE else Gesture.CONSUMED
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                AndroidUtilities.cancelRunOnUIThread(longPress)
                val g = gesture
                gesture = Gesture.NONE
                if (g == Gesture.PART_TAP && ev.actionMasked == MotionEvent.ACTION_UP) onPartTap(x, y)
                return g == Gesture.CONSUMED || g == Gesture.PART_TAP
            }
        }
        return gesture == Gesture.CONSUMED || gesture == Gesture.PART_TAP
    }

    private fun onLongPress() {
        val a = activity()
        val root = gestureRoot?.get()
        if (!active || a == null || root == null || gesture != Gesture.PENDING) return
        gesture = Gesture.CONSUMED
        // Отменяем жест у интерфейса, чтобы не сработал клик или меню сообщения.
        val cancel = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), MotionEvent.ACTION_CANCEL, downX, downY, 0)
        dispatchingCancel = true
        try {
            if (root === a.window.decorView) a.window.superDispatchTouchEvent(cancel) else root.dispatchTouchEvent(cancel)
        } catch (e: Throwable) {
            org.telegram.messenger.FileLog.e(e)
        } finally {
            dispatchingCancel = false
            cancel.recycle()
        }

        val target = SpecRegistry.targetAt(root, downX, downY, overlay) ?: return
        root.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        select(target)
    }

    private fun isInside(t: EditTarget, x: Float, y: Float): Boolean {
        val r = RectF()
        return t.screenBounds(r) && r.contains(x, y)
    }

    private fun onPartTap(x: Float, y: Float) {
        val t = selected ?: return
        val v = t.view() ?: return
        val loc = IntArray(2)
        v.getLocationOnScreen(loc)
        val lx = x - loc[0]
        val ly = y - loc[1]
        val r = RectF()
        val part = t.spec.parts(v).firstOrNull { it.hideable && t.spec.partBounds(v, it.id, r) && r.contains(lx, ly) } ?: return
        val scope = currentScope(ScopeOption.forTarget(t)).scope
        val hidden = t.style().getBool(part.id, "hidden", false)
        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        NextEngine.edit { it.set(scope, t.elementId, part.id, "hidden", if (hidden) null else true) }
        // Если скрытие пришло с более общего уровня, сброс на текущем его не вернёт — пишем явное false.
        if (hidden && t.style().getBool(part.id, "hidden", false)) {
            NextEngine.edit { it.set(scope, t.elementId, part.id, "hidden", false) }
        }
    }

    // ------------------------------------------------------------------ выбор

    /** Переключить выбранную часть того же элемента (из панели свойств). */
    internal fun selectPart(part: String?) {
        selected?.part = part
        overlay?.onSelectionChanged()
        sheet?.rebuild()
    }

    internal fun select(t: EditTarget) {
        selected = t
        isHideByTap = false
        overlay?.onSelectionChanged()
        openSheet()
    }

    private fun openSheet() {
        val a = activity() ?: return
        // Сначала отвязываем: dismiss может вызвать onSheetDismissed синхронно и сбросить выбор.
        sheet?.let { old -> sheet = null; old.dismiss() }
        val t = selected ?: return
        sheet = EditorSheet(a, t).also { it.show() }
    }

    internal fun onSheetDismissed(s: EditorSheet) {
        if (sheet !== s) return
        sheet = null
        if (!isHideByTap) {
            selected = null
            overlay?.onSelectionChanged()
        }
    }

    internal fun startHideByTap() {
        isHideByTap = true
        sheet?.let { s -> sheet = null; s.dismiss() }
        overlay?.onSelectionChanged()
    }

    /** «Готово» на панели: из режима скрытия возвращаемся к свойствам, иначе выходим. */
    internal fun onDone() {
        if (isHideByTap) {
            isHideByTap = false
            if (selected?.view() != null) openSheet() else selected = null
            overlay?.onSelectionChanged()
        } else {
            exit()
        }
    }

    // ------------------------------------------------------------------ уровни

    internal fun currentScope(options: List<ScopeOption>): ScopeOption {
        options.firstOrNull { it.kind == preferredKind && (preferredKind != Scopes.Kind.FOLDER || it.scope == preferredScope) }
            ?.let { return it }
        // Нужного вида нет (например, элемент вне чатов) — берём самый частный из доступных,
        // но папку подставляем только явно выбранную.
        return options.lastOrNull { it.kind != Scopes.Kind.FOLDER } ?: options[0]
    }

    internal fun setPreferredScope(o: ScopeOption) {
        preferredKind = o.kind
        preferredScope = o.scope
        overlay?.onSelectionChanged()
    }

    // ------------------------------------------------------------------ применение

    private fun onConfigChanged() {
        refreshAll()
        sheet?.rebuild()
        overlay?.invalidate()
    }

    /** Применить конфиг ко всем элементам с заготовками на экране (и в кэше списков). */
    @JvmStatic
    fun refreshAll() {
        val a: Activity = activity() ?: LaunchActivity.instance ?: return
        apply(a.window.decorView)
    }

    private fun apply(v: View) {
        if (v === overlay) return
        SpecRegistry.find(v)?.apply(v)
        if (v is RecyclerListView) {
            // Ячейки в кэше и пуле не перерисуются сами — обновляем и их.
            for (i in 0 until v.cachedChildCount) applyLeaf(v.getCachedChildAt(i))
            for (i in 0 until v.hiddenChildCount) applyLeaf(v.getHiddenChildAt(i))
            v.recycledViewPool.clear()
        }
        if (v is ViewGroup) {
            for (i in 0 until v.childCount) apply(v.getChildAt(i))
        }
    }

    private fun applyLeaf(v: View?) {
        if (v != null) SpecRegistry.find(v)?.apply(v)
    }
}
