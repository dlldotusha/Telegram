package org.telegram.custom.edit;

import android.app.Activity;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.telegram.custom.Customize;
import org.telegram.custom.core.Scopes;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Components.RecyclerListView;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Режим кастомизации.
 *
 * Обычные касания проходят в интерфейс как есть — по экранам можно ходить и
 * листать. Долгое нажатие (короче системного, чтобы не открылось меню
 * сообщения) отменяет жест у интерфейса и выбирает элемент под пальцем.
 *
 * Вход в режим — из «Оформления», выход — кнопкой «Готово» на панели.
 */
public final class EditMode {

    private static final long LONG_PRESS_MS = 320;

    private static boolean active;
    private static WeakReference<Activity> activityRef;
    private static EditOverlay overlay;
    private static EditTarget selected;
    private static boolean hideByTap;
    private static EditorSheet sheet;

    // Уровень, на который сохраняем правки. Запоминаем вид уровня, а не сам ключ:
    // выбрал «Этот чат» — в следующем чате тоже сохранится в «Этот чат».
    private static Scopes.Kind preferredKind = Scopes.Kind.CHAT;
    private static String preferredScope;

    private static final Runnable configListener = EditMode::onConfigChanged;

    // Состояние жеста
    private static final int GESTURE_NONE = 0, GESTURE_PENDING = 1, GESTURE_CONSUMED = 2, GESTURE_PART_TAP = 3;
    private static int gesture;
    private static float downX, downY;
    private static long downTime;
    private static final Runnable longPress = EditMode::onLongPress;

    private EditMode() {
    }

    public static boolean isActive() {
        return active;
    }

    public static void enter(Activity activity) {
        if (active || activity == null) {
            return;
        }
        active = true;
        activityRef = new WeakReference<>(activity);
        ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
        overlay = new EditOverlay(activity);
        decor.addView(overlay, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        Customize.addListener(configListener);
        Customize.invalidateCache();
        refreshAll();
    }

    public static void exit() {
        if (!active) {
            return;
        }
        active = false;
        if (sheet != null) {
            sheet.dismiss();
            sheet = null;
        }
        selected = null;
        hideByTap = false;
        AndroidUtilities.cancelRunOnUIThread(longPress);
        gesture = GESTURE_NONE;
        Customize.removeListener(configListener);
        if (overlay != null) {
            overlay.detach();
            overlay = null;
        }
        refreshAll();
        activityRef = null;
    }

    static Activity activity() {
        return activityRef == null ? null : activityRef.get();
    }

    static EditTarget selected() {
        return selected;
    }

    static boolean isHideByTap() {
        return hideByTap;
    }

    // ------------------------------------------------------------------ касания

    /** Хук из LaunchActivity.dispatchTouchEvent. true — событие съедено режимом. */
    public static boolean dispatchTouchEvent(Activity activity, MotionEvent ev) {
        if (!active || overlay == null || activity != activity()) {
            return false;
        }
        int action = ev.getActionMasked();
        float x = ev.getRawX(), y = ev.getRawY();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                AndroidUtilities.cancelRunOnUIThread(longPress);
                if (overlay.isOnToolbar(x, y)) {
                    gesture = GESTURE_NONE;
                    return false;
                }
                downX = x;
                downY = y;
                downTime = ev.getDownTime();
                if (hideByTap && selected != null && isInside(selected, x, y)) {
                    gesture = GESTURE_PART_TAP;
                    return true;
                }
                gesture = GESTURE_PENDING;
                AndroidUtilities.runOnUIThread(longPress, LONG_PRESS_MS);
                return false;
            }
            case MotionEvent.ACTION_POINTER_DOWN:
                if (gesture == GESTURE_PENDING) {
                    AndroidUtilities.cancelRunOnUIThread(longPress);
                    gesture = GESTURE_NONE;
                }
                return gesture == GESTURE_CONSUMED || gesture == GESTURE_PART_TAP;
            case MotionEvent.ACTION_MOVE: {
                if (gesture == GESTURE_PENDING || gesture == GESTURE_PART_TAP) {
                    int slop = ViewConfiguration.get(activity).getScaledTouchSlop();
                    if (Math.abs(x - downX) > slop || Math.abs(y - downY) > slop) {
                        AndroidUtilities.cancelRunOnUIThread(longPress);
                        if (gesture == GESTURE_PENDING) {
                            gesture = GESTURE_NONE;
                        } else {
                            gesture = GESTURE_CONSUMED; // палец уехал — это не тап по части
                        }
                    }
                }
                return gesture == GESTURE_CONSUMED || gesture == GESTURE_PART_TAP;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                AndroidUtilities.cancelRunOnUIThread(longPress);
                int g = gesture;
                gesture = GESTURE_NONE;
                if (g == GESTURE_PART_TAP && action == MotionEvent.ACTION_UP) {
                    onPartTap(x, y);
                }
                return g == GESTURE_CONSUMED || g == GESTURE_PART_TAP;
            }
        }
        return gesture == GESTURE_CONSUMED || gesture == GESTURE_PART_TAP;
    }

    private static void onLongPress() {
        Activity a = activity();
        if (!active || a == null || gesture != GESTURE_PENDING) {
            return;
        }
        gesture = GESTURE_CONSUMED;
        // Отменяем жест у интерфейса, чтобы не сработал клик или меню сообщения.
        long now = SystemClock.uptimeMillis();
        MotionEvent cancel = MotionEvent.obtain(downTime, now, MotionEvent.ACTION_CANCEL, downX, downY, 0);
        a.getWindow().superDispatchTouchEvent(cancel);
        cancel.recycle();

        View root = a.getWindow().getDecorView();
        View hit = HitTest.deepestAt(root, (int) downX, (int) downY, overlay);
        if (hit == null) {
            return;
        }
        root.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        select(SpecRegistry.targetFor(hit));
    }

    private static boolean isInside(EditTarget t, float x, float y) {
        RectF r = new RectF();
        return t.screenBounds(r) && r.contains(x, y);
    }

    private static void onPartTap(float x, float y) {
        EditTarget t = selected;
        View v = t == null ? null : t.view();
        if (v == null) {
            return;
        }
        int[] loc = new int[2];
        v.getLocationOnScreen(loc);
        float lx = x - loc[0], ly = y - loc[1];
        RectF r = new RectF();
        for (EditorSpec.Part part : t.spec.parts(v)) {
            if (!part.hideable || !t.spec.partBounds(v, part.id, r) || !r.contains(lx, ly)) {
                continue;
            }
            final String scope = currentScope(ScopeOption.forTarget(t)).scope;
            final boolean hidden = t.style().getBool(part.id, "hidden", false);
            final String partId = part.id;
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            Customize.edit(c -> c.set(scope, t.elementId, partId, "hidden", hidden ? null : Boolean.TRUE));
            // Если скрытие пришло с более общего уровня, сброс на текущем его не вернёт — пишем явное false.
            if (hidden && t.style().getBool(partId, "hidden", false)) {
                Customize.edit(c -> c.set(scope, t.elementId, partId, "hidden", Boolean.FALSE));
            }
            return;
        }
    }

    // ------------------------------------------------------------------ выбор

    static void select(EditTarget t) {
        selected = t;
        hideByTap = false;
        if (overlay != null) {
            overlay.onSelectionChanged();
        }
        openSheet();
    }

    static void openSheet() {
        Activity a = activity();
        if (a == null || selected == null) {
            return;
        }
        if (sheet != null) {
            // Сначала отвязываем: dismiss может вызвать onSheetDismissed синхронно и сбросить выбор.
            EditorSheet old = sheet;
            sheet = null;
            old.dismiss();
        }
        EditTarget t = selected;
        if (t == null) {
            return;
        }
        sheet = new EditorSheet(a, t);
        sheet.show();
    }

    static void onSheetDismissed(EditorSheet s) {
        if (sheet == s) {
            sheet = null;
            if (!hideByTap) {
                selected = null;
                if (overlay != null) {
                    overlay.onSelectionChanged();
                }
            }
        }
    }

    static void startHideByTap() {
        hideByTap = true;
        if (sheet != null) {
            EditorSheet s = sheet;
            sheet = null;
            s.dismiss();
        }
        if (overlay != null) {
            overlay.onSelectionChanged();
        }
    }

    /** «Готово» на панели: из режима скрытия возвращаемся к свойствам, иначе выходим. */
    static void onDone() {
        if (hideByTap) {
            hideByTap = false;
            if (selected != null && selected.view() != null) {
                openSheet();
            } else {
                selected = null;
            }
            if (overlay != null) {
                overlay.onSelectionChanged();
            }
        } else {
            exit();
        }
    }

    // ------------------------------------------------------------------ уровни

    static ScopeOption currentScope(List<ScopeOption> options) {
        for (ScopeOption o : options) {
            if (o.kind == preferredKind && (preferredKind != Scopes.Kind.FOLDER || o.scope.equals(preferredScope))) {
                return o;
            }
        }
        // Нужного вида нет (например, элемент вне чатов) — берём самый частный из доступных,
        // но папку подставляем только явно выбранную.
        for (int i = options.size() - 1; i >= 0; i--) {
            ScopeOption o = options.get(i);
            if (o.kind != Scopes.Kind.FOLDER) {
                return o;
            }
        }
        return options.get(0);
    }

    static void setPreferredScope(ScopeOption o) {
        preferredKind = o.kind;
        preferredScope = o.scope;
        if (overlay != null) {
            overlay.onSelectionChanged();
        }
    }

    // ------------------------------------------------------------------ применение

    private static void onConfigChanged() {
        refreshAll();
        if (sheet != null) {
            sheet.rebuild();
        }
        if (overlay != null) {
            overlay.invalidate();
        }
    }

    /** Применить конфиг ко всем элементам с заготовками на экране (и в кэше списков). */
    public static void refreshAll() {
        Activity a = activity();
        if (a == null) {
            a = org.telegram.ui.LaunchActivity.instance;
        }
        if (a == null) {
            return;
        }
        apply(a.getWindow().getDecorView());
    }

    private static void apply(View v) {
        if (v == overlay) {
            return;
        }
        EditorSpec s = SpecRegistry.find(v);
        if (s != null) {
            s.apply(v);
        }
        if (v instanceof RecyclerListView) {
            RecyclerListView list = (RecyclerListView) v;
            // Ячейки в кэше и пуле не перерисуются сами — обновляем и их.
            for (int i = 0, n = list.getCachedChildCount(); i < n; i++) {
                applyLeaf(list.getCachedChildAt(i));
            }
            for (int i = 0, n = list.getHiddenChildCount(); i < n; i++) {
                applyLeaf(list.getHiddenChildAt(i));
            }
            list.getRecycledViewPool().clear();
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0, n = g.getChildCount(); i < n; i++) {
                apply(g.getChildAt(i));
            }
        }
    }

    private static void applyLeaf(View v) {
        if (v == null) {
            return;
        }
        EditorSpec s = SpecRegistry.find(v);
        if (s != null) {
            s.apply(v);
        }
    }
}
