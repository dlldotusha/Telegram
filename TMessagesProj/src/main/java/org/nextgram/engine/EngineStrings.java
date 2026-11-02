package org.nextgram.engine;

import org.telegram.messenger.LocaleController;

import java.util.HashMap;

/**
 * Строки кастомизации. Держим их отдельно от strings.xml Telegram,
 * чтобы не ловить конфликты при каждом мердже апстрима.
 */
public final class EngineStrings {

    private static final HashMap<String, String[]> S = new HashMap<>();

    static {
        // ключ → { ru, en }
        put("customize", "Кастомизация · NextEngine", "Customization · NextEngine");
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
        // экран NextGram
        put("ng_settings_info", "Кастомизация, режим призрака, профиль", "Customization, ghost mode, profile");
        put("ng_engine", "Интерфейс · NextEngine", "Interface · NextEngine");
        put("ng_engine_open", "Редактировать интерфейс", "Edit interface");
        put("ng_engine_info", "Включает режим редактирования: зажмите любой элемент интерфейса, чтобы изменить его. Правки сохраняются глобально, для категории чатов, папки или одного чата.", "Turns on edit mode: long-press any element to change it. Edits can apply everywhere, to a chat category, a folder or one chat.");
        put("ng_ghost", "Режим призрака", "Ghost mode");
        put("ng_ghost_enabled", "Включить режим призрака", "Enable ghost mode");
        put("ng_ghost_read", "Не отправлять «прочитано»", "Don't send read receipts");
        put("ng_ghost_online", "Не показывать «в сети»", "Don't show online");
        put("ng_ghost_typing", "Не показывать «печатает»", "Don't show typing");
        put("ng_ghost_stories", "Не отмечать просмотр историй", "Don't mark stories as viewed");
        put("ng_ghost_info", "Правила Telegram API запрещают подмену статусов прочтения — со своим api_id это риск отзыва ключа. Сервер всё равно помечает чат прочитанным, когда вы отправляете в него сообщение.", "Telegram API terms forbid tampering with read statuses — with your own api_id this risks key revocation. The server still marks a chat as read when you send a message to it.");
        put("ng_profile", "Профиль NextGram", "NextGram profile");
        put("ng_profile_off", "Сервер профилей не настроен в этой сборке (NG_BACKEND_URL).", "Profile server is not configured in this build (NG_BACKEND_URL).");
        put("ng_profile_connect", "Подключить профиль", "Connect profile");
        put("ng_profile_connecting", "Подключаем…", "Connecting…");
        put("ng_profile_connect_info", "Приложение запустит служебного бота @%s от вашего имени — так сервер убедится, что аккаунт ваш. Расширенный профиль видят только пользователи NextGram.", "The app will start the service bot @%s on your behalf so the server can verify the account. The extended profile is visible only to NextGram users.");
        put("ng_profile_particles", "Частицы в профиле", "Profile particles");
        put("ng_profile_color", "Цвет частиц", "Particles color");
        put("ng_profile_density", "Плотность", "Density");
        put("ng_profile_disconnect", "Отключить профиль", "Disconnect profile");
        put("ng_profile_saved", "Сохранено", "Saved");
        put("ng_profile_error", "Не получилось: %s", "Failed: %s");
        put("ng_off", "Выкл.", "Off");
        put("ng_stars", "Звёзды", "Stars");
        put("ng_snow", "Снег", "Snow");
        put("ng_bubbles", "Пузыри", "Bubbles");
        put("ng_hearts", "Сердца", "Hearts");
        put("ng_density_low", "Реже", "Sparse");
        put("ng_density_mid", "Обычно", "Normal");
        put("ng_density_high", "Гуще", "Dense");
    }

    private static void put(String key, String ru, String en) {
        S.put(key, new String[]{ru, en});
    }

    private EngineStrings() {
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
