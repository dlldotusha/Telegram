package org.telegram.custom;

import org.telegram.messenger.LocaleController;

import java.util.HashMap;

/**
 * Строки кастомизации. Держим их отдельно от strings.xml Telegram,
 * чтобы не ловить конфликты при каждом мердже апстрима.
 */
public final class CustomStrings {

    private static final HashMap<String, String[]> S = new HashMap<>();

    static {
        // ключ → { ru, en }
        put("customize", "Режим кастомизации", "Customization mode");
        put("customize_info", "Зажмите элемент, чтобы изменить его", "Long-press any element to edit it");
        put("done", "Готово", "Done");
        put("save_to", "Сохранять в", "Save to");
        put("scope_global", "Везде", "Everywhere");
        put("scope_chat", "Этот чат", "This chat");
        put("scope_folder", "Папка «%s»", "Folder “%s”");
        put("cat_contacts", "Все контакты", "All contacts");
        put("cat_non_contacts", "Все не контакты", "All non-contacts");
        put("cat_groups", "Все группы", "All groups");
        put("cat_channels", "Все каналы", "All channels");
        put("cat_bots", "Все боты", "All bots");
        put("from", "из: %s", "from: %s");
        put("not_set", "по умолчанию", "default");
        put("theme_colors", "Цвета темы", "Theme colors");
        put("theme_colors_info", "Меняются везде, где используется этот цвет, а не только здесь. Сохраняются для текущей темы.", "Changes everywhere this color is used, not only here. Saved for the current theme.");
        put("no_spec", "Для этого элемента ещё нет заготовки — доступны только цвета темы.", "No prepared editor for this element yet — only theme colors are available.");
        put("parent", "Выделить родительский элемент", "Select parent element");
        put("hide_by_tap", "Скрыть части касанием", "Hide parts by tapping");
        put("hide_by_tap_hint", "Нажмите на часть, чтобы скрыть или показать", "Tap a part to hide or show it");
        put("reset_element", "Сбросить элемент на этом уровне", "Reset element at this level");
        put("long_press_reset", "Зажмите строку, чтобы сбросить значение на выбранном уровне", "Long-press a row to reset it at the selected level");
        put("color", "Цвет", "Color");
        put("hidden", "Скрыть", "Hide");
        put("seconds", "Показывать секунды", "Show seconds");
        put("msg_in", "Входящее сообщение", "Incoming message");
        put("msg_out", "Исходящее сообщение", "Outgoing message");
        put("part_time", "Время", "Time");
        put("cancel", "Отмена", "Cancel");
        put("reset", "Сбросить", "Reset");
        put("hex", "Цвет в HEX", "HEX color");
    }

    private static void put(String key, String ru, String en) {
        S.put(key, new String[]{ru, en});
    }

    private CustomStrings() {
    }

    public static String get(String key) {
        String[] v = S.get(key);
        if (v == null) {
            return key;
        }
        return isRussian() ? v[0] : v[1];
    }

    public static String format(String key, Object... args) {
        return String.format(get(key), args);
    }

    private static boolean isRussian() {
        try {
            LocaleController.LocaleInfo info = LocaleController.getInstance().getCurrentLocaleInfo();
            return info != null && info.shortName != null && info.shortName.startsWith("ru");
        } catch (Throwable e) {
            return false;
        }
    }
}
