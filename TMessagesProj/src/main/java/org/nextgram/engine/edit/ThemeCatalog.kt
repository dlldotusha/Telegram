package org.nextgram.engine.edit

import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import org.nextgram.engine.EngineStrings
import org.nextgram.engine.hooks.SizeHooks
import org.telegram.ui.ActionBar.SimpleTextView
import org.telegram.ui.Components.AnimatedTextView
import org.telegram.ui.Components.Switch
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

    private fun p(id: String, ru: String, en: String, vararg colors: ColorItem, size: String? = null, hideable: Boolean = false) =
        EditorSpec.Part(id, EngineStrings.pick(ru, en), hideable, colors.toList(), size)

    /** Части сообщения в порядке проверки попадания: сначала мелкие, в конце пузырь целиком. */
    fun messageParts(out: Boolean): List<EditorSpec.Part> = if (out) listOf(
        p("time", "Время", "Time",
            c(Theme.key_chat_outTimeText, "Цвет времени", "Time color"),
            c(Theme.key_chat_outSentCheck, "Галочка «доставлено»", "Delivered check"),
            c(Theme.key_chat_outSentCheckRead, "Галочки «прочитано»", "Read checks"),
            c(Theme.key_chat_outSentClock, "Часики «отправляется»", "Sending clock"),
            c(Theme.key_chat_outViews, "Просмотры", "Views"),
            size = SizeHooks.CHAT_TIME, hideable = true),
        p("reply", "Ответ на сообщение", "Reply",
            c(Theme.key_chat_outReplyLine, "Полоска", "Line"),
            c(Theme.key_chat_outReplyNameText, "Имя", "Name"),
            c(Theme.key_chat_outReplyMessageText, "Текст", "Text"),
            size = SizeHooks.CHAT_REPLY),
        p("name", "Имя отправителя", "Sender name",
            c(Theme.key_chat_outForwardedNameText, "«Переслано от»", "“Forwarded from”"),
            c(Theme.key_chat_outViaBotNameText, "Имя бота «через @»", "“via @bot”"),
            size = SizeHooks.CHAT_NAME),
        p("text", "Текст сообщения", "Message text",
            c(Theme.key_chat_messageTextOut, "Цвет текста", "Text color"),
            c(Theme.key_chat_messageLinkOut, "Ссылки", "Links"),
            size = SizeHooks.CHAT_TEXT),
        p("bubble", "Пузырь", "Bubble",
            c(Theme.key_chat_outBubble, "Фон пузыря", "Bubble background"),
            c(Theme.key_chat_outBubbleGradient1, "Второй цвет градиента", "Gradient 2nd color"),
            c(Theme.key_chat_outBubbleSelected, "Фон выделенного пузыря", "Selected bubble"),
            c(Theme.key_chat_outBubbleShadow, "Тень пузыря", "Bubble shadow")),
    ) else listOf(
        p("time", "Время", "Time",
            c(Theme.key_chat_inTimeText, "Цвет времени", "Time color"),
            c(Theme.key_chat_inViews, "Просмотры", "Views"),
            size = SizeHooks.CHAT_TIME, hideable = true),
        p("reply", "Ответ на сообщение", "Reply",
            c(Theme.key_chat_inReplyLine, "Полоска", "Line"),
            c(Theme.key_chat_inReplyNameText, "Имя", "Name"),
            c(Theme.key_chat_inReplyMessageText, "Текст", "Text"),
            size = SizeHooks.CHAT_REPLY),
        p("name", "Имя отправителя", "Sender name",
            c(Theme.key_chat_inForwardedNameText, "«Переслано от»", "“Forwarded from”"),
            c(Theme.key_chat_inViaBotNameText, "Имя бота «через @»", "“via @bot”"),
            size = SizeHooks.CHAT_NAME),
        p("text", "Текст сообщения", "Message text",
            c(Theme.key_chat_messageTextIn, "Цвет текста", "Text color"),
            c(Theme.key_chat_messageLinkIn, "Ссылки", "Links"),
            size = SizeHooks.CHAT_TEXT),
        p("bubble", "Пузырь", "Bubble",
            c(Theme.key_chat_inBubble, "Фон пузыря", "Bubble background"),
            c(Theme.key_chat_inBubbleSelected, "Фон выделенного пузыря", "Selected bubble"),
            c(Theme.key_chat_inBubbleShadow, "Тень пузыря", "Bubble shadow")),
    )

    private val dialogParts = listOf(
        p("counter", "Счётчик непрочитанных", "Unread counter",
            c(Theme.key_chats_unreadCounter, "Фон счётчика", "Counter background"),
            c(Theme.key_chats_unreadCounterMuted, "Фон в чатах без звука", "Muted counter background"),
            c(Theme.key_chats_unreadCounterText, "Цифры", "Digits"),
            c(Theme.key_chats_mentionIcon, "Значок упоминания @", "Mention icon"),
            size = SizeHooks.DIALOG_COUNTER),
        p("time", "Время и галочки", "Time and checks",
            c(Theme.key_chats_date, "Время", "Time"),
            c(Theme.key_chats_sentCheck, "Галочка «доставлено»", "Delivered check"),
            c(Theme.key_chats_sentReadCheck, "Галочки «прочитано»", "Read checks"),
            c(Theme.key_chats_sentClock, "Часики «отправляется»", "Sending clock"),
            size = SizeHooks.DIALOG_TIME),
        p("name", "Название чата", "Chat name",
            c(Theme.key_chats_name, "Цвет названия", "Name color"),
            c(Theme.key_chats_secretName, "Секретный чат", "Secret chat"),
            c(Theme.key_chats_verifiedBackground, "Галочка верификации — фон", "Verified badge — background"),
            c(Theme.key_chats_verifiedCheck, "Галочка верификации — знак", "Verified badge — check"),
            c(Theme.key_chats_muteIcon, "Значок «без звука»", "Mute icon"),
            size = SizeHooks.DIALOG_NAME),
        p("message", "Последнее сообщение", "Last message",
            c(Theme.key_chats_message, "Текст", "Text"),
            c(Theme.key_chats_nameMessage, "Имя отправителя", "Sender name"),
            c(Theme.key_chats_attachMessage, "«Фото», «Видео» и т. п.", "“Photo”, “Video” etc."),
            c(Theme.key_chats_actionMessage, "Служебные события", "Service events"),
            c(Theme.key_chats_draft, "Черновик", "Draft"),
            size = SizeHooks.DIALOG_MESSAGE),
        p("avatar", "Аватар", "Avatar",
            c(Theme.key_chats_onlineCircle, "Кружок «в сети»", "Online dot")),
        p("all", "Фон строки", "Row background",
            c(Theme.key_windowBackgroundWhite, "Фон списка", "List background"),
            c(Theme.key_chats_pinnedOverlay, "Подсветка закреплённых", "Pinned chats tint"),
            c(Theme.key_chats_pinnedIcon, "Значок закрепа", "Pin icon"),
            c(Theme.key_listSelector, "Подсветка при нажатии", "Press highlight")),
    )

    private val actionBarParts = listOf(
        p("title", "Заголовок", "Title", c(Theme.key_actionBarDefaultTitle, "Цвет заголовка", "Title color")),
        p("subtitle", "Подзаголовок", "Subtitle", c(Theme.key_actionBarDefaultSubtitle, "Цвет подзаголовка", "Subtitle color")),
        p("back", "Кнопка «Назад»", "Back button", c(Theme.key_actionBarDefaultIcon, "Иконки", "Icons"), c(Theme.key_actionBarDefaultSelector, "Подсветка при нажатии", "Press highlight")),
        p("menu", "Кнопки справа", "Buttons on the right", c(Theme.key_actionBarDefaultIcon, "Иконки", "Icons"), c(Theme.key_actionBarDefaultSelector, "Подсветка при нажатии", "Press highlight")),
        p("all", "Фон панели", "Bar background", c(Theme.key_actionBarDefault, "Фон", "Background")),
    )

    private val settingsParts = listOf(
        p("switch", "Переключатель", "Switch", c(Theme.key_switchTrack, "Выключен", "Off"), c(Theme.key_switchTrackChecked, "Включён", "On")),
        p("icon", "Иконка", "Icon", c(Theme.key_windowBackgroundWhiteGrayIcon, "Цвет иконок", "Icon color")),
        p("value", "Значение справа", "Value on the right", c(Theme.key_windowBackgroundWhiteValueText, "Цвет значения", "Value color")),
        p("description", "Описание под текстом", "Description below",
            c(Theme.key_windowBackgroundWhiteGrayText2, "Цвет описания", "Description color"),
            size = SizeHooks.SETTINGS_SUB),
        p("text", "Текст пункта", "Row text",
            c(Theme.key_windowBackgroundWhiteBlackText, "Обычный текст", "Regular text"),
            c(Theme.key_windowBackgroundWhiteBlueText4, "Синие пункты («Добавить…»)", "Blue rows (“Add…”)"),
            c(Theme.key_text_RedRegular, "Красные пункты («Удалить…»)", "Red rows (“Delete…”)"),
            size = SizeHooks.SETTINGS_TEXT),
        p("all", "Фон пункта", "Row background",
            c(Theme.key_windowBackgroundWhite, "Фон пункта", "Row background"),
            c(Theme.key_windowBackgroundGray, "Фон между разделами", "Background between sections"),
            c(Theme.key_divider, "Разделитель", "Divider"),
            c(Theme.key_listSelector, "Подсветка при нажатии", "Press highlight")),
    )

    private val headerParts = listOf(
        p("text", "Заголовок раздела", "Section header",
            c(Theme.key_windowBackgroundWhiteBlueHeader, "Цвет заголовка", "Header color"),
            size = SizeHooks.SETTINGS_HEADER),
        p("all", "Фон", "Background", c(Theme.key_windowBackgroundWhite, "Фон", "Background")),
    )

    private val footerParts = listOf(
        p("text", "Пояснение под разделом", "Section footer",
            c(Theme.key_windowBackgroundWhiteGrayText4, "Цвет текста", "Text color"),
            c(Theme.key_windowBackgroundWhiteLinkText, "Ссылки", "Links"),
            size = SizeHooks.SETTINGS_FOOTER),
        p("all", "Фон (тень между разделами)", "Background (shadow between sections)",
            c(Theme.key_windowBackgroundGray, "Фон", "Background"),
            c(Theme.key_windowBackgroundGrayShadow, "Тень", "Shadow")),
    )

    private val enterParts = listOf(
        p("field", "Поле ввода", "Input field",
            c(Theme.key_chat_messagePanelText, "Текст", "Text"),
            c(Theme.key_chat_messagePanelHint, "Подсказка «Сообщение»", "“Message” hint"),
            c(Theme.key_chat_messagePanelCursor, "Курсор", "Cursor")),
        p("emoji", "Кнопка эмодзи", "Emoji button",
            c(Theme.key_glass_defaultIcon, "Иконки на «стеклянных» кнопках", "Icons on “glass” buttons")),
        p("attach", "Скрепка", "Attach button",
            c(Theme.key_glass_defaultIcon, "Иконки на «стеклянных» кнопках", "Icons on “glass” buttons")),
        p("send", "Отправить / запись голосового", "Send / voice record",
            c(Theme.key_chat_messagePanelSend, "Кнопка «Отправить»", "Send button"),
            c(Theme.key_chat_messagePanelVoiceBackground, "Кнопка записи — фон", "Record button — background"),
            c(Theme.key_chat_messagePanelVoicePressed, "Кнопка записи — нажатая", "Record button — pressed"),
            c(Theme.key_chat_messagePanelVoiceLock, "Замок записи", "Record lock"),
            c(Theme.key_chat_recordedVoiceDot, "Красная точка записи", "Recording dot"),
            c(Theme.key_chat_recordTime, "Время записи", "Record time")),
        p("buttons", "Другие кнопки панели", "Other panel buttons",
            c(Theme.key_glass_defaultIcon, "Иконки на «стеклянных» кнопках", "Icons on “glass” buttons")),
        p("all", "Фон панели", "Panel background",
            c(Theme.key_chat_messagePanelBackground, "Фон («стекло»)", "Background (“glass”)"),
            c(Theme.key_chat_messagePanelShadow, "Тень над панелью", "Shadow above panel")),
    )

    private val serviceParts = listOf(
        p("all", "Плашка", "Pill",
            c(Theme.key_chat_serviceBackground, "Фон плашки", "Pill background"),
            c(Theme.key_chat_serviceText, "Текст", "Text"),
            c(Theme.key_chat_serviceLink, "Ссылки", "Links"),
            c(Theme.key_chat_serviceIcon, "Иконки", "Icons"),
            size = SizeHooks.CHAT_SERVICE),
    )

    // ------------------------------------------------------------------ поиск частей

    /** Границы дочернего view в координатах root. */
    fun childBounds(root: View, child: View?, out: RectF): Boolean {
        if (child == null || !child.isShown || child.width == 0) return false
        val a = IntArray(2)
        val b = IntArray(2)
        root.getLocationOnScreen(a)
        child.getLocationOnScreen(b)
        out.set((b[0] - a[0]).toFloat(), (b[1] - a[1]).toFloat(), (b[0] - a[0] + child.width).toFloat(), (b[1] - a[1] + child.height).toFloat())
        return true
    }

    private fun whole(root: View, out: RectF): Boolean {
        out.set(0f, 0f, root.width.toFloat(), root.height.toFloat())
        return true
    }

    private fun children(v: View): List<View> =
        if (v is ViewGroup) (0 until v.childCount).map { v.getChildAt(it) }.filter { it.isShown } else emptyList()

    private fun isText(v: View) = v is SimpleTextView || v is AnimatedTextView || v is TextView && v !is EditText

    /** Пункт настроек: текст — левый текстовый ребёнок, значение — правый, иконка, переключатель. */
    private fun settingsBounds(root: View, part: String, out: RectF): Boolean {
        val kids = children(root)
        val texts = kids.filter { isText(it) }.sortedBy { it.left }
        val main = texts.minByOrNull { it.top }
        return when (part) {
            "switch" -> childBounds(root, kids.firstOrNull { it is Switch }, out)
            "icon" -> childBounds(root, kids.firstOrNull { it is ImageView && it !is Switch }, out)
            // справа от основного текста — значение, под ним — описание
            "value" -> main != null && childBounds(root, texts.lastOrNull { it !== main && it.left >= main.right - 1 }, out)
            "description" -> main != null && childBounds(root, texts.firstOrNull { it !== main && it.top >= main.bottom - 2 }, out)
            "text" -> if (texts.isEmpty() && root is TextView) whole(root, out) else childBounds(root, main ?: texts.firstOrNull(), out)
            "all" -> whole(root, out)
            else -> false
        }
    }

    private fun actionBarBounds(root: View, part: String, out: RectF): Boolean {
        val ab = root as ActionBar
        return when (part) {
            "title" -> childBounds(root, ab.titleTextView, out)
            "subtitle" -> childBounds(root, ab.subtitleTextView, out)
            "back" -> childBounds(root, ab.backButton, out)
            "menu" -> childBounds(root, ab.menu, out)
            "all" -> whole(root, out)
            else -> false
        }
    }

    private fun enterBounds(root: View, part: String, out: RectF): Boolean {
        fun find(v: View, pred: (View) -> Boolean): View? {
            if (pred(v)) return v
            for (c in children(v)) find(c, pred)?.let { return it }
            return null
        }
        val enter = root as ChatActivityEnterView
        return when (part) {
            "field" -> childBounds(root, find(root) { it is EditText }, out)
            "emoji" -> childBounds(root, enter.emojiButton, out)
            "attach" -> childBounds(root, enter.attachButton, out)
            "send" -> childBounds(root, enter.sendButton, out) || childBounds(root, enter.audioVideoButtonContainer, out)
            // кнопки — объединение всех иконок панели; поле ввода проверяется раньше, поэтому не перекрывается
            "buttons" -> {
                val icons = ArrayList<View>()
                fun collect(v: View) {
                    if (v is ImageView && v.isShown) icons += v
                    children(v).forEach { collect(it) }
                }
                collect(root)
                val r = RectF()
                var any = false
                for (icon in icons) if (childBounds(root, icon, r)) {
                    if (!any) out.set(r) else out.union(r)
                    any = true
                }
                any
            }
            "all" -> whole(root, out)
            else -> false
        }
    }

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
        g("Архив", "Archive",
            c(Theme.key_chats_archiveBackground, "Фон аватара архива", "Archive avatar background"),
            c(Theme.key_chats_archiveIcon, "Иконка архива", "Archive icon"),
            c(Theme.key_chats_archiveText, "Текст на архиве", "Text on archive"),
            c(Theme.key_chats_archivePinBackground, "Фон закреплённого архива", "Pinned archive background")),
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
            c(Theme.key_glass_defaultIcon, "Иконки (эмодзи, скрепка) — общие для «стеклянных» кнопок", "Icons (emoji, attach) — shared by “glass” buttons"),
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

    /** Заготовки без настроек по уровням (только цвета и размеры). Порядок важен: первая подходящая побеждает. */
    val specs: List<EditorSpec> = listOf(
        CatalogSpec("dialog", "Чат в списке", "Chat in the list", dialogCell, dialogParts,
            { v, part, out -> part == "all" && whole(v, out) || (v as DialogCell).getCustomPartBounds(part, out) }) { it is DialogCell },
        CatalogSpec("service", "Служебное сообщение", "Service message", serviceMessage, serviceParts,
            { v, _, out -> whole(v, out) }) { it is ChatActionCell },
        CatalogSpec("enter", "Поле ввода сообщения", "Message input", enterView, enterParts, ::enterBounds) { it is ChatActivityEnterView },
        CatalogSpec("tabs", "Нижние вкладки", "Bottom tabs", bottomTabs) { it is MainTabsLayout || it is GlassTabView },
        CatalogSpec("actionbar", "Верхняя панель", "Top bar", actionBar, actionBarParts, ::actionBarBounds) { it is ActionBar },
        CatalogSpec("profile", "Шапка профиля", "Profile header", profileHeader) {
            it.javaClass.name.startsWith("org.telegram.ui.ProfileActivity$")
        },
        CatalogSpec("section_header", "Заголовок раздела", "Section header", emptyList(), headerParts, ::settingsBounds) { it is HeaderCell },
        CatalogSpec("section_footer", "Пояснение под разделом", "Section footer", emptyList(), footerParts, ::settingsBounds) {
            it is TextInfoPrivacyCell || it is ShadowSectionCell
        },
        CatalogSpec("settings", "Пункт настроек", "Settings row", settingsCell, settingsParts, ::settingsBounds) {
            it is TextCell || it is TextSettingsCell || it is TextCheckCell || it is NotificationsCheckCell ||
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

/**
 * Заготовка из каталога: элемент без своих настроек по уровням — цвета темы
 * и размеры, разложенные по видимым частям.
 */
class CatalogSpec(
    private val id: String,
    private val ru: String,
    private val en: String,
    private val groups: List<ThemeCatalog.ColorGroup>,
    private val partList: List<EditorSpec.Part> = emptyList(),
    private val bounds: (View, String, RectF) -> Boolean = { _, _, _ -> false },
    override val kind: EditorSpec.Kind = EditorSpec.Kind.ELEMENT,
    private val matcher: (View) -> Boolean,
) : EditorSpec {
    override fun handles(view: View) = matcher(view)
    override fun elementId(view: View) = "catalog:$id"
    override fun title(view: View) = EngineStrings.pick(ru, en)
    override fun dialogId(view: View) = 0L
    override fun account(view: View) = org.telegram.messenger.UserConfig.selectedAccount
    override fun parts(view: View) = partList
    override fun partBounds(view: View, partId: String, out: RectF) = bounds(view, partId, out)
    override fun props(view: View) = emptyList<EditorSpec.Prop>()
    override fun colorGroups(view: View): List<ThemeCatalog.ColorGroup> = groups.ifEmpty {
        partList.filter { it.colors.isNotEmpty() }.map { ThemeCatalog.ColorGroup(it.title, it.title, it.colors) }
    }
    override fun apply(view: View) {
        view.requestLayout()
        view.invalidate()
    }
}
