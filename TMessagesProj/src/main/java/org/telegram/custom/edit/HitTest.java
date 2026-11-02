package org.telegram.custom.edit;

import android.view.View;
import android.view.ViewGroup;

/** Поиск самого глубокого видимого view под точкой экрана. */
final class HitTest {

    private HitTest() {
    }

    static View deepestAt(View root, int screenX, int screenY, View exclude) {
        int[] loc = new int[2];
        return find(root, screenX, screenY, exclude, loc);
    }

    private static View find(View v, int x, int y, View exclude, int[] loc) {
        if (v == null || v == exclude || v.getVisibility() != View.VISIBLE || v.getAlpha() < 0.02f) {
            return null;
        }
        v.getLocationOnScreen(loc);
        if (x < loc[0] || y < loc[1] || x >= loc[0] + v.getWidth() || y >= loc[1] + v.getHeight()) {
            return null;
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            // Последний ребёнок рисуется поверх — проверяем с конца.
            for (int i = g.getChildCount() - 1; i >= 0; i--) {
                View hit = find(g.getChildAt(i), x, y, exclude, loc);
                if (hit != null) {
                    return hit;
                }
            }
        }
        return v;
    }

    /** Обход всех видимых view (для оверлея). */
    interface Visitor {
        void visit(View v);
    }

    static void forEachVisible(View v, View exclude, Visitor visitor) {
        if (v == null || v == exclude || v.getVisibility() != View.VISIBLE || !v.isAttachedToWindow()) {
            return;
        }
        visitor.visit(v);
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0, n = g.getChildCount(); i < n; i++) {
                forEachVisible(g.getChildAt(i), exclude, visitor);
            }
        }
    }
}
