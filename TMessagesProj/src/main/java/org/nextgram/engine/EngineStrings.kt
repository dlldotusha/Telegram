package org.nextgram.engine

import org.telegram.messenger.LocaleController

/**
 * Строки NextGram. Держим их отдельно от strings.xml Telegram,
 * чтобы не ловить конфликты при каждом мердже апстрима.
 */
object EngineStrings {

    // ключ → [ru, en]
    private val strings = hashMapOf(
        "screen_element" to arrayOf("Элемент экрана", "Screen element"),
        "other_parts" to arrayOf("Другие части элемента", "Other parts of the element"),
        "parts" to arrayOf("Части элемента", "Parts of the element"),
        "parts_hint" to arrayOf("Можно и прямо на экране: зажмите нужную часть.", "Or long-press the part right on the screen."),
        "all_colors" to arrayOf("Все цвета элемента", "All element colors"),
        "hide_all_colors" to arrayOf("Свернуть все цвета", "Collapse all colors"),
        "text_size" to arrayOf("Размер текста", "Text size"),
        "text_size_messages" to arrayOf("Размер текста сообщений (как в «Оформлении»)", "Message text size (same as in Appearance)"),
        "scoped_header" to arrayOf("Для разных чатов по-разному", "Different per chat"),
        "color_here" to arrayOf("Свой цвет", "Own color"),
        "scope_info" to arrayOf("Куда сохраняются настройки ниже: везде, для типа чатов, папки или только этого чата. Цвета темы всегда общие.", "Where the settings below are saved: everywhere, a chat type, a folder or just this chat. Theme colors are always shared."),
        "inherited" to arrayOf("Унаследовано:", "Inherited:"),
        "reset_colors" to arrayOf("Сбросить цвета элемента", "Reset element colors"),
        "hue" to arrayOf("Оттенок", "Hue"),
        "opacity" to arrayOf("Прозрачность", "Opacity"),
        "collapse" to arrayOf("Свернуть", "Collapse"),
        "customize" to arrayOf("Кастомизация · NextEngine", "Customization · NextEngine"),
        "customize_info" to arrayOf("Зажмите элемент, чтобы изменить его", "Long-press any element to edit it"),
        "done" to arrayOf("Готово", "Done"),
        "save_to" to arrayOf("Сохранять в", "Save to"),
        "scope_global" to arrayOf("Везде", "Everywhere"),
        "scope_chat" to arrayOf("Этот чат", "This chat"),
        "scope_folder" to arrayOf("Папка «%s»", "Folder “%s”"),
        "cat_contacts" to arrayOf("Все контакты", "All contacts"),
        "cat_non_contacts" to arrayOf("Все не контакты", "All non-contacts"),
        "cat_groups" to arrayOf("Все группы", "All groups"),
        "cat_channels" to arrayOf("Все каналы", "All channels"),
        "cat_bots" to arrayOf("Все боты", "All bots"),
        "from" to arrayOf("из: %s", "from: %s"),
        "not_set" to arrayOf("по умолчанию", "default"),
        "theme_colors" to arrayOf("Цвета темы", "Theme colors"),
        "theme_colors_info" to arrayOf("Цвета темы общие: меняются везде, где Telegram их использует, а не только здесь. Сохраняются для текущей темы. Долгий тап — вернуть как было.", "Theme colors are shared: they change everywhere Telegram uses them, not only here. Saved for the current theme. Long-press to restore."),
        "no_spec" to arrayOf("Для этого элемента ещё нет заготовки — доступны только цвета темы.", "No prepared editor for this element yet — only theme colors are available."),
        "parent" to arrayOf("Выделить родительский элемент", "Select parent element"),
        "hide_by_tap" to arrayOf("Скрыть части касанием", "Hide parts by tapping"),
        "hide_by_tap_hint" to arrayOf("Нажмите на часть, чтобы скрыть или показать", "Tap a part to hide or show it"),
        "reset_element" to arrayOf("Сбросить элемент на этом уровне", "Reset element at this level"),
        "long_press_reset" to arrayOf("Зажмите строку, чтобы сбросить значение на выбранном уровне", "Long-press a row to reset it at the selected level"),
        "color" to arrayOf("Цвет", "Color"),
        "hidden" to arrayOf("Скрыть", "Hide"),
        "seconds" to arrayOf("Показывать секунды", "Show seconds"),
        "msg_in" to arrayOf("Входящее сообщение", "Incoming message"),
        "msg_out" to arrayOf("Исходящее сообщение", "Outgoing message"),
        "part_time" to arrayOf("Время", "Time"),
        "cancel" to arrayOf("Отмена", "Cancel"),
        "reset" to arrayOf("Сбросить", "Reset"),
        "hex" to arrayOf("Цвет в HEX", "HEX color"),
        "ng_settings_info" to arrayOf("Кастомизация, режим призрака, профиль", "Customization, ghost mode, profile"),
        "ng_engine" to arrayOf("Интерфейс · NextEngine", "Interface · NextEngine"),
        "ng_engine_open" to arrayOf("Редактировать интерфейс", "Edit interface"),
        "ng_engine_info" to arrayOf("Включает режим редактирования: зажмите любой элемент интерфейса, чтобы изменить его. Правки сохраняются глобально, для категории чатов, папки или одного чата.", "Turns on edit mode: long-press any element to change it. Edits can apply everywhere, to a chat category, a folder or one chat."),
        "ng_ghost" to arrayOf("Режим призрака", "Ghost mode"),
        "ng_ghost_enabled" to arrayOf("Включить режим призрака", "Enable ghost mode"),
        "ng_ghost_read" to arrayOf("Не отправлять «прочитано»", "Don't send read receipts"),
        "ng_ghost_online" to arrayOf("Не показывать «в сети»", "Don't show online"),
        "ng_ghost_typing" to arrayOf("Не показывать «печатает»", "Don't show typing"),
        "ng_ghost_stories" to arrayOf("Не отмечать просмотр историй", "Don't mark stories as viewed"),
        "ng_ghost_info" to arrayOf("Правила Telegram API запрещают подмену статусов прочтения — со своим api_id это риск отзыва ключа. Сервер всё равно помечает чат прочитанным, когда вы отправляете в него сообщение.", "Telegram API terms forbid tampering with read statuses — with your own api_id this risks key revocation. The server still marks a chat as read when you send a message to it."),
        "ng_profile" to arrayOf("Профиль NextGram", "NextGram profile"),
        "ng_profile_off" to arrayOf("Сервер профилей не настроен в этой сборке (NG_BACKEND_URL).", "Profile server is not configured in this build (NG_BACKEND_URL)."),
        "ng_profile_connect" to arrayOf("Подключить профиль", "Connect profile"),
        "ng_profile_connecting" to arrayOf("Подключаем…", "Connecting…"),
        "ng_profile_connect_info" to arrayOf("Приложение запустит служебного бота @%s от вашего имени — так сервер убедится, что аккаунт ваш. Расширенный профиль видят только пользователи NextGram.", "The app will start the service bot @%s on your behalf so the server can verify the account. The extended profile is visible only to NextGram users."),
        "ng_profile_particles" to arrayOf("Частицы в профиле", "Profile particles"),
        "ng_profile_color" to arrayOf("Цвет частиц", "Particles color"),
        "ng_profile_density" to arrayOf("Плотность", "Density"),
        "ng_profile_disconnect" to arrayOf("Отключить профиль", "Disconnect profile"),
        "ng_profile_saved" to arrayOf("Сохранено", "Saved"),
        "ng_profile_error" to arrayOf("Не получилось: %s", "Failed: %s"),
        "ng_off" to arrayOf("Выкл.", "Off"),
        "ng_stars" to arrayOf("Звёзды", "Stars"),
        "ng_snow" to arrayOf("Снег", "Snow"),
        "ng_bubbles" to arrayOf("Пузыри", "Bubbles"),
        "ng_hearts" to arrayOf("Сердца", "Hearts"),
        "ng_density_low" to arrayOf("Реже", "Sparse"),
        "ng_density_mid" to arrayOf("Обычно", "Normal"),
        "ng_density_high" to arrayOf("Гуще", "Dense"),
    )

    @JvmStatic
    fun get(key: String): String {
        val v = strings[key] ?: return key
        return if (isRussian()) v[0] else v[1]
    }

    @JvmStatic
    fun format(key: String, vararg args: Any?): String = String.format(get(key), *args)

    /** Строка на языке интерфейса (для подписей, заданных прямо в коде). */
    @JvmStatic
    fun pick(ru: String, en: String): String = if (isRussian()) ru else en

    @JvmStatic
    fun isRussian(): Boolean = try {
        LocaleController.getInstance().currentLocaleInfo?.shortName?.startsWith("ru") == true
    } catch (e: Throwable) {
        false
    }
}
