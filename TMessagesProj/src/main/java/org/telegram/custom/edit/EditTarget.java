package org.telegram.custom.edit;

import android.graphics.RectF;
import android.view.View;
import android.view.ViewParent;

import org.telegram.custom.Customize;
import org.telegram.custom.core.ResolvedElement;

import java.lang.ref.WeakReference;

/** Выбранный в режиме редактирования элемент. View держим слабо: ячейки переиспользуются. */
public final class EditTarget {

    private final WeakReference<View> viewRef;
    public final EditorSpec spec;
    public final String elementId;
    public final long dialogId;
    public final int account;
    public final String title;

    EditTarget(View view, EditorSpec spec) {
        this.viewRef = new WeakReference<>(view);
        this.spec = spec;
        this.elementId = spec.elementId(view);
        this.dialogId = spec.dialogId(view);
        this.account = spec.account(view);
        this.title = spec.title(view);
    }

    public View view() {
        View v = viewRef.get();
        // Ячейка могла уехать в пул и показывать уже другое сообщение.
        if (v == null || !v.isAttachedToWindow() || !spec.handles(v) || !elementId.equals(spec.elementId(v)) || spec.dialogId(v) != dialogId) {
            return null;
        }
        return v;
    }

    public boolean isAuto() {
        return spec instanceof AutoSpec;
    }

    public ResolvedElement style() {
        return Customize.resolve(account, dialogId, elementId);
    }

    /** Родительский элемент — для авторежима, когда палец попал во вложенный view. */
    public EditTarget parent() {
        View v = view();
        if (v == null) {
            return null;
        }
        ViewParent p = v.getParent();
        if (!(p instanceof View)) {
            return null;
        }
        return SpecRegistry.targetFor((View) p);
    }

    /** Границы элемента на экране. */
    public boolean screenBounds(RectF out) {
        View v = view();
        if (v == null) {
            return false;
        }
        int[] loc = new int[2];
        v.getLocationOnScreen(loc);
        out.set(loc[0], loc[1], loc[0] + v.getWidth(), loc[1] + v.getHeight());
        return true;
    }
}
