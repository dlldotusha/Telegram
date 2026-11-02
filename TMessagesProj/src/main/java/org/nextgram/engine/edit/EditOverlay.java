package org.nextgram.engine.edit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.nextgram.engine.EngineStrings;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.spoilers.SpoilerEffect;

import java.util.ArrayList;

/**
 * Слой поверх окна в режиме кастомизации: панель с подсказкой и «Готово»,
 * рамка вокруг выбранного элемента и спойлеры над скрытыми частями.
 * Сам слой касания не ловит — они проходят в интерфейс (кроме панели).
 */
@SuppressLint("ViewConstructor")
final class EditOverlay extends FrameLayout {

    private final LinearLayout toolbar;
    private final TextView hintView;
    private final TextView doneView;

    private final Paint framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF tmp = new RectF();
    private final RectF selectedRect = new RectF();
    private boolean hasSelectedRect;

    /** Скрытые части на экране, в координатах слоя. */
    private final ArrayList<RectF> hiddenRects = new ArrayList<>();
    private final ArrayList<SpoilerEffect> spoilers = new ArrayList<>();
    private final int[] loc = new int[2];
    private final int[] ownLoc = new int[2];

    private final ViewTreeObserver.OnPreDrawListener preDraw = () -> {
        boolean hadSomething = hasSelectedRect || !hiddenRects.isEmpty();
        collect();
        if (hadSomething || hasSelectedRect || !hiddenRects.isEmpty()) {
            invalidate();
        }
        return true;
    };

    EditOverlay(Context context) {
        super(context);
        setWillNotDraw(false);
        setClickable(false);
        setFocusable(false);

        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(AndroidUtilities.dp(2));
        framePaint.setColor(Theme.getColor(Theme.key_featuredStickers_addButton));

        toolbar = new LinearLayout(context);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(AndroidUtilities.dp(22));
        bg.setColor(0xE6202124);
        toolbar.setBackground(bg);
        toolbar.setPadding(AndroidUtilities.dp(16), 0, AndroidUtilities.dp(6), 0);
        toolbar.setClickable(true);

        hintView = new TextView(context);
        hintView.setTextColor(0xFFFFFFFF);
        hintView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        hintView.setMaxLines(2);
        hintView.setEllipsize(TextUtils.TruncateAt.END);
        toolbar.addView(hintView, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, Gravity.CENTER_VERTICAL, 0, 8, 8, 8));

        doneView = new TextView(context);
        doneView.setText(EngineStrings.get("done"));
        doneView.setTextColor(0xFF000000);
        doneView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        doneView.setTypeface(AndroidUtilities.bold());
        doneView.setGravity(Gravity.CENTER);
        doneView.setPadding(AndroidUtilities.dp(14), 0, AndroidUtilities.dp(14), 0);
        GradientDrawable doneBg = new GradientDrawable();
        doneBg.setCornerRadius(AndroidUtilities.dp(16));
        doneBg.setColor(0xFFFFFFFF);
        doneView.setBackground(doneBg);
        doneView.setOnClickListener(v -> EditMode.onDone());
        toolbar.addView(doneView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 32, Gravity.CENTER_VERTICAL));

        FrameLayout.LayoutParams lp = LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 16, 0, 16, 16);
        addView(toolbar, lp);
        // Панель над навигационной панелью системы.
        setOnApplyWindowInsetsListener((v, insets) -> {
            FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) toolbar.getLayoutParams();
            //noinspection deprecation
            p.bottomMargin = AndroidUtilities.dp(16) + insets.getSystemWindowInsetBottom();
            toolbar.setLayoutParams(p);
            return insets;
        });
        updateHint();
    }

    private ViewTreeObserver observer;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        observer = getRootView().getViewTreeObserver();
        observer.addOnPreDrawListener(preDraw);
        requestApplyInsets();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (observer != null && observer.isAlive()) {
            observer.removeOnPreDrawListener(preDraw);
        }
        observer = null;
        super.onDetachedFromWindow();
    }

    void detach() {
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(this);
        }
    }

    boolean isOnToolbar(float rawX, float rawY) {
        toolbar.getLocationOnScreen(loc);
        return rawX >= loc[0] && rawY >= loc[1] && rawX < loc[0] + toolbar.getWidth() && rawY < loc[1] + toolbar.getHeight();
    }

    void onSelectionChanged() {
        updateHint();
        collect();
        invalidate();
    }

    private void updateHint() {
        EditTarget t = EditMode.selected();
        if (EditMode.isHideByTap() && t != null) {
            String scope = EditMode.currentScope(ScopeOption.forTarget(t)).label;
            hintView.setText(EngineStrings.get("hide_by_tap_hint") + " · " + scope);
        } else if (t != null) {
            hintView.setText(t.title);
        } else {
            hintView.setText(EngineStrings.get("customize_info"));
        }
    }

    /** Пересчитать рамку выбранного элемента и прямоугольники скрытых частей. */
    private void collect() {
        getLocationOnScreen(ownLoc);
        EditTarget t = EditMode.selected();
        hasSelectedRect = t != null && t.screenBounds(selectedRect);
        if (hasSelectedRect) {
            selectedRect.offset(-ownLoc[0], -ownLoc[1]);
        }

        hiddenRects.clear();
        HitTest.forEachVisible(getRootView(), this, v -> {
            EditorSpec spec = SpecRegistry.find(v);
            if (spec == null) {
                return;
            }
            org.nextgram.engine.core.ResolvedElement style = org.nextgram.engine.NextEngine.resolve(spec.account(v), spec.dialogId(v), spec.elementId(v));
            if (style.isEmpty()) {
                return;
            }
            for (EditorSpec.Part part : spec.parts(v)) {
                if (!part.hideable || !style.getBool(part.id, "hidden", false) || !spec.partBounds(v, part.id, tmp)) {
                    continue;
                }
                v.getLocationOnScreen(loc);
                RectF r = new RectF(tmp);
                r.offset(loc[0] - ownLoc[0], loc[1] - ownLoc[1]);
                hiddenRects.add(r);
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        while (spoilers.size() < hiddenRects.size()) {
            SpoilerEffect s = new SpoilerEffect();
            s.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
            s.setParentView(this);
            spoilers.add(s);
        }
        for (int i = 0; i < hiddenRects.size(); i++) {
            RectF r = hiddenRects.get(i);
            SpoilerEffect s = spoilers.get(i);
            s.setBounds((int) r.left, (int) r.top, (int) r.right, (int) r.bottom);
            s.draw(canvas);
        }
        if (hasSelectedRect) {
            float d = AndroidUtilities.dp(1);
            tmp.set(selectedRect.left + d, selectedRect.top + d, selectedRect.right - d, selectedRect.bottom - d);
            canvas.drawRoundRect(tmp, AndroidUtilities.dp(6), AndroidUtilities.dp(6), framePaint);
        }
    }
}
