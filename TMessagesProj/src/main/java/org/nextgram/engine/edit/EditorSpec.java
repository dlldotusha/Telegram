package org.telegram.custom.edit;

import android.graphics.RectF;
import android.view.View;

import java.util.List;

/**
 * Заготовка редактора для класса View. Заготовка знает, из каких частей
 * состоит элемент (это нужно, чтобы понять, во что ткнули, и чтобы скрывать
 * части касанием) и какие у него есть свойства.
 *
 * Если для View заготовки нет, используется {@link AutoSpec}: только цвета темы.
 */
public interface EditorSpec {

    /** Часть элемента: время, просмотры, текст... */
    final class Part {
        public final String id;
        public final String title;
        public final boolean hideable;

        public Part(String id, String title, boolean hideable) {
            this.id = id;
            this.title = title;
            this.hideable = hideable;
        }
    }

    /** Редактируемое свойство. part == null — свойство самого элемента. */
    final class Prop {
        public enum Type { BOOL, COLOR }

        public final String part;
        public final String key;
        public final Type type;
        public final String title;
        /** Значение, если свойство нигде не задано (для BOOL). */
        public final boolean defaultBool;

        public Prop(String part, String key, Type type, String title, boolean defaultBool) {
            this.part = part;
            this.key = key;
            this.type = type;
            this.title = title;
            this.defaultBool = defaultBool;
        }
    }

    boolean handles(View view);

    /** Стабильный id элемента в конфиге. */
    String elementId(View view);

    String title(View view);

    /** Чат, к которому относится элемент, или 0. От этого зависят доступные уровни. */
    long dialogId(View view);

    int account(View view);

    List<Part> parts(View view);

    /** Границы части в координатах view или false, если часть сейчас не видна. */
    boolean partBounds(View view, String partId, RectF out);

    List<Prop> props(View view);

    /** Применить изменившийся конфиг к view (перемерить, перерисовать). */
    void apply(View view);
}
