package org.telegram.custom;

import org.telegram.custom.core.Colors;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeColors;

import java.util.Map;

/**
 * Базовый уровень кастомизации: переопределение цветовых ключей темы.
 * Вызывается из Theme.getColor, поэтому всё, что тут делается на горячем
 * пути, — это сравнение пары полей и чтение из массива.
 *
 * Цвета хранятся отдельно для каждой темы (и её акцента): цвет, подобранный
 * под тёмную тему, не должен ломать светлую.
 */
public final class CustomColors {

    private static final class Snapshot {
        final Object theme;
        final int accent;
        final int generation;
        final boolean[] present;
        final int[] colors;

        Snapshot(Object theme, int accent, int generation, boolean[] present, int[] colors) {
            this.theme = theme;
            this.accent = accent;
            this.generation = generation;
            this.present = present;
            this.colors = colors;
        }
    }

    private static volatile Snapshot snapshot;

    private CustomColors() {
    }

    /** Ключ темы в конфиге. */
    public static String themeKey() {
        Theme.ThemeInfo t = Theme.getActiveTheme();
        if (t == null) {
            return "default";
        }
        int accent = t.currentAccentId;
        return accent != 0 ? t.getKey() + "#" + accent : t.getKey();
    }

    /** Хук в Theme.getColor: переопределён ли ключ. */
    public static boolean has(int key) {
        Snapshot s = current();
        return s != null && key >= 0 && key < s.present.length && s.present[key];
    }

    public static int get(int key) {
        Snapshot s = snapshot;
        return s == null ? 0 : s.colors[key];
    }

    /** Переопределённый цвет ключа или null. */
    public static Integer override(int key) {
        return has(key) ? get(key) : null;
    }

    private static Snapshot current() {
        if (!Customize.isReady()) {
            return null;
        }
        Theme.ThemeInfo t = Theme.getActiveTheme();
        int accent = t == null ? 0 : t.currentAccentId;
        int gen = Customize.generation();
        Snapshot s = snapshot;
        if (s != null && s.theme == t && s.accent == accent && s.generation == gen) {
            return s;
        }
        s = build(t, accent, gen);
        snapshot = s;
        return s;
    }

    private static Snapshot build(Theme.ThemeInfo t, int accent, int gen) {
        int n = Math.max(Theme.colorsCount, 1);
        boolean[] present = new boolean[n];
        int[] colors = new int[n];
        Map<String, String> m = Customize.config().themeColors(themeKey());
        for (Map.Entry<String, String> e : m.entrySet()) {
            int key = ThemeColors.stringKeyToInt(e.getKey());
            Integer argb = Colors.parse(e.getValue());
            if (key >= 0 && key < n && argb != null) {
                present[key] = true;
                colors[key] = argb;
            }
        }
        return new Snapshot(t, accent, gen, present, colors);
    }
}
