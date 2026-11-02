package org.nextgram.engine.edit

import android.view.View
import org.nextgram.engine.EngineStrings
import org.telegram.ui.ActionBar.ActionBar
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.ActionBar.ThemeColors
import org.telegram.ui.Cells.ChatActionCell
import org.telegram.ui.Cells.DialogCell
import org.telegram.ui.Cells.HeaderCell
import org.telegram.ui.Cells.NotificationsCheckCell
import org.telegram.ui.Cells.RadioCell
import org.telegram.ui.Cells.ShadowSectionCell
import org.telegram.ui.Cells.TextCell
import org.telegram.ui.Cells.TextCheckCell
import org.telegram.ui.Cells.TextDetailSettingsCell
import org.telegram.ui.Cells.TextInfoPrivacyCell
import org.telegram.ui.Cells.TextSettingsCell
import org.telegram.ui.Components.ChatActivityEnterView
import org.telegram.ui.Components.glass.GlassTabView
import org.telegram.ui.MainTabsLayout
import org.telegram.ui.SettingsActivity

/**
 * Каталог заготовок: какие цвета темы за что отвечают в каждом элементе,
 * с понятными названиями. Цвета темы глобальные — поменял «фон пузыря» здесь,
 * он поменялся во всех чатах и везде, где Telegram использует этот цвет.
 *
 * Добавить элемент = добавить CatalogSpec в [specs]: как его узнать (класс view)
 * и какие группы цветов показать.
 */
object ThemeCatalog {

    /** Цвет темы с подписью для человека. */
    class ColorItem(val key: Int, private val ru: String, private val en: String) {
        val title: String get() = EngineStrings.pick(ru, en)
        val keyName: String? get() = ThemeColors.getStringName(key)
    }

    class ColorGroup(private val ru: String, private val en: String, val items: List<ColorItem>) {
        val title: String get() = EngineStrings.pick(ru, en)
    }

    private fun c(key: Int, ru: String, en: String) = ColorItem(key, ru, en)
    private fun g(ru: String, en: String, vararg items: ColorItem) = ColorGroup(ru, en, items.toList())

    // ------------------------------------------------------------------ сообщения

    fun messageGroups(out: Boolean): List<ColorGroup> = if (out) listOf(
        g("Пузырь", "Bubble",
            c(Theme.key_chat_outBubble, "Фон пузыря", "Bubble background"),
            c(Theme.key_chat_outBubbleGradient1, "Градиент пузыря — второй цвет", "Bubble gradient — 2nd color"),
            c(Theme.key_chat_outBubbleSelected, "Фон выделенного пузыря", "Selected bubble"),
            c(Theme.key_chat_outBubbleShadow, "Тень пузыря", "Bubble shadow")),
        g("Текст", "Text",
            c(Theme.key_chat_messageTextOut, "Текст сообщения", "Message text"),
            c(Theme.key_chat_messageLinkOut, "Ссылки", "Links"),
            c(Theme.key_chat_outForwardedNameText, "«Переслано от»", "“Forwarded from”"),
            c(Theme.key_chat_outViaBotNameText, "Имя бота «через @»", "“via @bot”"),
            c(Theme.key_chat_outReplyLine, "Полоска ответа", "Reply line"),
            c(Theme.key_chat_outReplyNameText, "Имя в ответе", "Reply name"),
            c(Theme.key_chat_outReplyMessageText, "Текст в ответе", "Reply text"),
            c(Theme.key_chat_outPreviewLine, "Полоска превью ссылки", "Link preview line"),
            c(Theme.key_chat_outSiteNameText, "Название сайта в превью", "Site name in preview")),
        g("Время и статус", "Time and status",
            c(Theme.key_chat_outTimeText, "Время", "Time"),
            c(Theme.key_chat_outSentCheck, "Галочка «доставлено»", "Delivered check"),
            c(Theme.key_chat_outSentCheckRead, "Галочки «прочитано»", "Read checks"),
            c(Theme.key_chat_outSentClock, "Часики «отправляется»", "Sending clock"),
            c(Theme.key_chat_outViews, "Просмотры", "Views")),
        g("Файлы, музыка и голосовые", "Files, music and voice",
            c(Theme.key_chat_outFileNameText, "Имя файла", "File name"),
            c(Theme.key_chat_outFileInfoText, "Размер файла", "File size"),
            c(Theme.key_chat_outLoader, "Кнопка загрузки", "Download button"),
            c(Theme.key_chat_outVoiceSeekbar, "Волна голосового", "Voice waveform"),
            c(Theme.key_chat_outVoiceSeekbarFill, "Прослушанная часть", "Played part"),
            c(Theme.key_chat_outAudioTitleText, "Название трека", "Track title"),
            c(Theme.key_chat_outAudioPerformerText, "Исполнитель", "Performer")),
    ) else listOf(
        g("Пузырь", "Bubble",
            c(Theme.key_chat_inBubble, "Фон пузыря", "Bubble background"),
            c(Theme.key_chat_inBubbleSelected, "Фон выделенного пузыря", "Selected bubble"),
            c(Theme.key_chat_inBubbleShadow, "Тень пузыря", "Bubble shadow")),
        g("Текст", "Text",
            c(Theme.key_chat_messageTextIn, "Текст сообщения", "Message text"),
            c(Theme.key_chat_messageLinkIn, "Ссылки", "Links"),
            c(Theme.key_chat_inForwardedNameText, "«Переслано от»", "“Forwarded from”"),
            c(Theme.key_chat_inViaBotNameText, "Имя бота «через @»", "“via @bot”"),
            c(Theme.key_chat_inReplyLine, "Полоска ответа", "Reply line"),
            c(Theme.key_chat_inReplyNameText, "Имя в ответе", "Reply name"),
            c(Theme.key_chat_inReplyMessageText, "Текст в ответе", "Reply text"),
            c(Theme.key_chat_inPreviewLine, "Полоска превью ссылки", "Link preview line"),
            c(Theme.key_chat_inSiteNameText, "Название сайта в превью", "Site name in preview")),
        g("Время и статус", "Time and status",
            c(Theme.key_chat_inTimeText, "Время", "Time"),
            c(Theme.key_chat_inViews, "Просмотры", "Views")),
        g("Файлы, музыка и голосовые", "Files, music and voice",
            c(Theme.key_chat_inFileNameText, "Имя файла", "File name"),
            c(Theme.key_chat_inFileInfoText, "Размер файла", "File size"),
            c(Theme.key_chat_inLoader, "Кнопка загрузки", "Download button"),
            c(Theme.key_chat_inVoiceSeekbar, "Волна голосового", "Voice waveform"),
            c(Theme.key_chat_inVoiceSeekbarFill, "Прослушанная часть", "Played part"),
            c(Theme.key_chat_inAudioTitleText, "Название трека", "Track title"),
            c(Theme.key_chat_inAudioPerformerText, "Исполнитель", "Performer")),
    )

    // ------------------------------------------------------------------ остальные элементы

    private val dialogCell = listOf(
        g("Название и текст", "Name and text",
            c(Theme.key_chats_name, "Название чата", "Chat name"),
            c(Theme.key_chats_secretName, "Название секретного чата", "Secret chat name"),
            c(Theme.key_chats_message, "Последнее сообщение", "Last message"),
            c(Theme.key_chats_nameMessage, "Имя отправителя в превью", "Sender name in preview"),
            c(Theme.key_chats_attachMessage, "«Фото», «Видео» и другие вложения", "“Photo”, “Video” and other media"),
            c(Theme.key_chats_actionMessage, "Служебные события в превью", "Service events in preview"),
            c(Theme.key_chats_draft, "Черновик", "Draft"),
            c(Theme.key_chats_date, "Время", "Time")),
        g("Счётчики и значки", "Counters and icons",
            c(Theme.key_chats_unreadCounter, "Счётчик непрочитанных — фон", "Unread counter — background"),
            c(Theme.key_chats_unreadCounterMuted, "Счётчик в чатах без звука — фон", "Muted counter — background"),
            c(Theme.key_chats_unreadCounterText, "Цифры счётчика", "Counter digits"),
            c(Theme.key_chats_mentionIcon, "Значок упоминания @", "Mention icon"),
            c(Theme.key_chats_pinnedIcon, "Значок закрепа", "Pin icon"),
            c(Theme.key_chats_muteIcon, "Значок «без звука»", "Mute icon"),
            c(Theme.key_chats_sentCheck, "Галочка «доставлено»", "Delivered check"),
            c(Theme.key_chats_sentReadCheck, "Галочки «прочитано»", "Read checks"),
            c(Theme.key_chats_sentClock, "Часики «отправляется»", "Sending clock"),
            c(Theme.key_chats_sentError, "Ошибка отправки", "Send error"),
            c(Theme.key_chats_verifiedBackground, "Галочка верификации — фон", "Verified badge — background"),
            c(Theme.key_chats_verifiedCheck, "Галочка верификации — знак", "Verified badge — check"),
            c(Theme.key_chats_onlineCircle, "Кружок «в сети»", "Online dot")),
        g("Фон", "Background",
            c(Theme.key_windowBackgroundWhite, "Фон списка", "List background"),
            c(Theme.key_chats_pinnedOverlay, "Подсветка закреплённых", "Pinned chats tint"),
            c(Theme.key_listSelector, "Подсветка при нажатии", "Press highlight")),
    )

    private val serviceMessage = listOf(
        g("Плашка", "Pill",
            c(Theme.key_chat_serviceBackground, "Фон плашки (даты, события)", "Pill background (dates, events)"),
            c(Theme.key_chat_serviceText, "Текст", "Text"),
            c(Theme.key_chat_serviceLink, "Ссылки", "Links"),
            c(Theme.key_chat_serviceIcon, "Иконки", "Icons")),
    )

    private val enterView = listOf(
        g("Панель", "Panel",
            c(Theme.key_chat_messagePanelBackground, "Фон панели", "Panel background"),
            c(Theme.key_chat_messagePanelShadow, "Тень над панелью", "Shadow above panel"),
            c(Theme.key_chat_messagePanelText, "Текст", "Text"),
            c(Theme.key_chat_messagePanelHint, "Подсказка «Сообщение»", "“Message” hint"),
            c(Theme.key_chat_messagePanelCursor, "Курсор", "Cursor"),
            c(Theme.key_chat_messagePanelIcons, "Иконки (эмодзи, скрепка)", "Icons (emoji, attach)"),
            c(Theme.key_chat_messagePanelSend, "Кнопка «Отправить»", "Send button"),
            c(Theme.key_chat_messagePanelVoiceBackground, "Кнопка записи — фон", "Record button — background")),
        g("Ответ и пересылка", "Reply and forward",
            c(Theme.key_chat_replyPanelLine, "Полоска", "Line"),
            c(Theme.key_chat_replyPanelName, "Имя", "Name"),
            c(Theme.key_chat_replyPanelIcons, "Иконка", "Icon"),
            c(Theme.key_chat_replyPanelClose, "Крестик", "Close")),
    )

    private val actionBar = listOf(
        g("Шапка", "Header",
            c(Theme.key_actionBarDefault, "Фон", "Background"),
            c(Theme.key_actionBarDefaultTitle, "Заголовок", "Title"),
            c(Theme.key_actionBarDefaultSubtitle, "Подзаголовок", "Subtitle"),
            c(Theme.key_actionBarDefaultIcon, "Иконки", "Icons"),
            c(Theme.key_actionBarDefaultSelector, "Подсветка при нажатии", "Press highlight")),
        g("Меню «⋮» и поиск", "“⋮” menu and search",
            c(Theme.key_actionBarDefaultSubmenuBackground, "Фон меню", "Menu background"),
            c(Theme.key_actionBarDefaultSubmenuItem, "Пункты меню", "Menu items"),
            c(Theme.key_actionBarDefaultSubmenuItemIcon, "Иконки пунктов", "Menu icons"),
            c(Theme.key_actionBarDefaultSearch, "Текст поиска", "Search text"),
            c(Theme.key_actionBarDefaultSearchPlaceholder, "Подсказка поиска", "Search hint")),
        g("Вкладки", "Tabs",
            c(Theme.key_actionBarTabActiveText, "Активная вкладка", "Active tab"),
            c(Theme.key_actionBarTabUnactiveText, "Неактивная вкладка", "Inactive tab"),
            c(Theme.key_actionBarTabLine, "Полоска под активной", "Active tab line")),
    )

    private val settingsCell = listOf(
        g("Пункт", "Row",
            c(Theme.key_windowBackgroundWhite, "Фон пункта", "Row background"),
            c(Theme.key_windowBackgroundWhiteBlackText, "Текст", "Text"),
            c(Theme.key_windowBackgroundWhiteValueText, "Значение справа", "Value on the right"),
            c(Theme.key_windowBackgroundWhiteGrayText2, "Описание под текстом", "Text below"),
            c(Theme.key_windowBackgroundWhiteGrayIcon, "Иконки", "Icons"),
            c(Theme.key_windowBackgroundWhiteBlueText4, "Синие пункты («Добавить…»)", "Blue rows (“Add…”)"),
            c(Theme.key_text_RedRegular, "Красные пункты («Удалить…»)", "Red rows (“Delete…”)"),
            c(Theme.key_divider, "Разделитель", "Divider"),
            c(Theme.key_listSelector, "Подсветка при нажатии", "Press highlight")),
        g("Переключатели", "Switches",
            c(Theme.key_switchTrack, "Выключен", "Off"),
            c(Theme.key_switchTrackChecked, "Включён", "On")),
        g("Разделы", "Sections",
            c(Theme.key_windowBackgroundWhiteBlueHeader, "Заголовки разделов", "Section headers"),
            c(Theme.key_windowBackgroundWhiteGrayText4, "Пояснения под разделами", "Section footers"),
            c(Theme.key_windowBackgroundGray, "Фон между разделами", "Background between sections")),
    )

    private val bottomTabs = listOf(
        g("Нижние вкладки", "Bottom tabs",
            c(Theme.key_glass_tabSelected, "Выбранная вкладка", "Selected tab"),
            c(Theme.key_glass_tabSelectedText, "Текст выбранной вкладки", "Selected tab text"),
            c(Theme.key_glass_tabUnselected, "Остальные вкладки", "Other tabs"),
            c(Theme.key_glass_defaultIcon, "Иконки", "Icons"),
            c(Theme.key_glass_defaultText, "Текст", "Text")),
    )

    private val profileHeader = listOf(
        g("Шапка профиля", "Profile header",
            c(Theme.key_avatar_backgroundActionBarBlue, "Фон шапки", "Header background"),
            c(Theme.key_profile_title, "Имя", "Name"),
            c(Theme.key_profile_status, "Статус", "Status"),
            c(Theme.key_avatar_subtitleInProfileBlue, "Подпись под именем", "Subtitle"),
            c(Theme.key_profile_actionBackground, "Круглая кнопка — фон", "Round button — background"),
            c(Theme.key_profile_actionIcon, "Круглая кнопка — иконка", "Round button — icon"),
            c(Theme.key_profile_verifiedBackground, "Галочка верификации — фон", "Verified badge — background"),
            c(Theme.key_profile_verifiedCheck, "Галочка верификации — знак", "Verified badge — check")),
    )

    /** Заготовки без настроек по уровням — только цвета темы. Порядок важен: первая подходящая побеждает. */
    val specs: List<EditorSpec> = listOf(
        CatalogSpec("dialog", "Чат в списке", "Chat in the list", dialogCell) { it is DialogCell },
        CatalogSpec("service", "Служебное сообщение", "Service message", serviceMessage) { it is ChatActionCell },
        CatalogSpec("enter", "Поле ввода сообщения", "Message input", enterView) { it is ChatActivityEnterView },
        CatalogSpec("tabs", "Нижние вкладки", "Bottom tabs", bottomTabs) { it is MainTabsLayout || it is GlassTabView },
        CatalogSpec("actionbar", "Верхняя панель", "Top bar", actionBar) { it is ActionBar },
        CatalogSpec("profile", "Шапка профиля", "Profile header", profileHeader) {
            it.javaClass.name.startsWith("org.telegram.ui.ProfileActivity$")
        },
        CatalogSpec("settings", "Пункт настроек", "Settings row", settingsCell) {
            it is TextCell || it is TextSettingsCell || it is TextCheckCell || it is HeaderCell ||
                it is TextInfoPrivacyCell || it is ShadowSectionCell || it is NotificationsCheckCell ||
                it is TextDetailSettingsCell || it is RadioCell || it is SettingsActivity.SettingCell
        },
    )

    // ------------------------------------------------------------------ названия для авторежима

    private val tokensRu = mapOf(
        "chat" to "чат", "chats" to "список чатов", "in" to "входящие", "out" to "исходящие",
        "bubble" to "пузырь", "text" to "текст", "background" to "фон", "icon" to "иконка", "icons" to "иконки",
        "selected" to "(выделено)", "time" to "время", "name" to "имя", "message" to "сообщение", "link" to "ссылка",
        "button" to "кнопка", "line" to "линия", "shadow" to "тень", "panel" to "панель", "hint" to "подсказка",
        "title" to "заголовок", "subtitle" to "подзаголовок", "gray" to "серый", "blue" to "синий", "red" to "красный",
        "green" to "зелёный", "window" to "окно", "action" to "действие", "bar" to "панель", "counter" to "счётчик",
        "unread" to "непрочитанные", "muted" to "без звука", "check" to "галочка", "sent" to "отправлено",
        "read" to "прочитано", "clock" to "часики", "error" to "ошибка", "pinned" to "закреп", "archive" to "архив",
        "search" to "поиск", "placeholder" to "подсказка", "menu" to "меню", "item" to "пункт", "tab" to "вкладка",
        "active" to "активная", "unactive" to "неактивная", "voice" to "голосовое", "audio" to "аудио", "file" to "файл",
        "progress" to "прогресс", "seekbar" to "шкала", "fill" to "заполнение", "reply" to "ответ",
        "forwarded" to "пересланное", "preview" to "превью", "service" to "служебное", "emoji" to "эмодзи",
        "sticker" to "стикер", "stickers" to "стикеры", "switch" to "переключатель", "track" to "дорожка",
        "divider" to "разделитель", "profile" to "профиль", "avatar" to "аватар", "dialog" to "окно",
        "header" to "заголовок", "value" to "значение", "input" to "ввод", "field" to "поле", "cursor" to "курсор",
        "send" to "отправить", "record" to "запись", "location" to "геопозиция", "contact" to "контакт",
        "loader" to "загрузка", "views" to "просмотры", "date" to "дата", "draft" to "черновик", "online" to "в сети",
        "circle" to "кружок", "verified" to "верификация", "secret" to "секретный", "list" to "список",
        "selector" to "подсветка", "checked" to "включено", "unchecked" to "выключено", "radio" to "переключатель",
        "checkbox" to "флажок", "player" to "плеер", "close" to "закрыть", "overlay" to "наложение", "gradient" to "градиент",
        "white" to "", "default" to "", "regular" to "", "bold" to "жирный", "glass" to "", "key" to "",
    )

    /** Человеческое название цвета темы по имени ключа: «chat_inTimeText» → «Чат входящие время текст». */
    @JvmStatic
    fun humanName(key: Int): String {
        val raw = ThemeColors.getStringName(key) ?: return "#$key"
        val words = raw.split('_')
            .flatMap { it.split(Regex("(?<=[a-z])(?=[A-Z0-9])")) }
            .map { it.lowercase() }
            .filter { it.isNotEmpty() }
        val ru = EngineStrings.isRussian()
        val out = words.mapNotNull { w -> if (ru) (tokensRu[w] ?: w).ifEmpty { null } else w }
        val text = out.joinToString(" ").ifEmpty { raw }
        return text.replaceFirstChar { it.uppercase() }
    }
}

/** Заготовка из каталога: элемент без своих настроек по уровням, только цвета темы. */
class CatalogSpec(
    private val id: String,
    private val ru: String,
    private val en: String,
    private val groups: List<ThemeCatalog.ColorGroup>,
    private val matcher: (View) -> Boolean,
) : EditorSpec {
    override fun handles(view: View) = matcher(view)
    override fun elementId(view: View) = "catalog:$id"
    override fun title(view: View) = EngineStrings.pick(ru, en)
    override fun dialogId(view: View) = 0L
    override fun account(view: View) = org.telegram.messenger.UserConfig.selectedAccount
    override fun parts(view: View) = emptyList<EditorSpec.Part>()
    override fun partBounds(view: View, partId: String, out: android.graphics.RectF) = false
    override fun props(view: View) = emptyList<EditorSpec.Prop>()
    override fun colorGroups(view: View) = groups
    override fun apply(view: View) = view.invalidate()
}
