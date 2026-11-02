package org.telegram.custom.edit;

import android.view.View;
import android.view.ViewParent;

import java.util.ArrayList;

/**
 * Реестр заготовок. Новая заготовка = класс EditorSpec + строка в {@link #SPECS}.
 */
public final class SpecRegistry {

    private static final ArrayList<EditorSpec> SPECS = new ArrayList<>();

    static {
        SPECS.add(new MessageCellSpec());
    }

    private SpecRegistry() {
    }

    /** Заготовка для конкретного view или null. */
    public static EditorSpec find(View view) {
        for (int i = 0, n = SPECS.size(); i < n; i++) {
            EditorSpec s = SPECS.get(i);
            if (s.handles(view)) {
                return s;
            }
        }
        return null;
    }

    /** Цель редактирования для точки: ближайший предок с заготовкой, иначе сам view в авторежиме. */
    public static EditTarget targetFor(View deepest) {
        for (View v = deepest; v != null; ) {
            EditorSpec s = find(v);
            if (s != null) {
                return new EditTarget(v, s);
            }
            ViewParent p = v.getParent();
            v = p instanceof View ? (View) p : null;
        }
        return new EditTarget(deepest, AutoSpec.INSTANCE);
    }
}
