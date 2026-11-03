package org.nextgram.engine.edit

import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import org.nextgram.engine.EngineStrings
import org.nextgram.engine.edit.ThemeCatalog.ColorGroup
import org.nextgram.engine.edit.ThemeCatalog.ColorItem
import org.nextgram.engine.hooks.SizeHooks
import org.telegram.messenger.AndroidUtilities
import org.telegram.ui.ActionBar.ActionBarMenuSubItem
import org.telegram.ui.ActionBar.ActionBarPopupWindow
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BottomSheet
import org.telegram.ui.ActionBar.SimpleTextView
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Cells.AboutLinkCell
import org.telegram.ui.Cells.BotHelpCell
import org.telegram.ui.Cells.ChatUnreadCell
import org.telegram.ui.Cells.CheckBoxCell
import org.telegram.ui.Cells.CollapseTextCell
import org.telegram.ui.Cells.DialogsEmptyCell
import org.telegram.ui.Cells.DialogsHintCell
import org.telegram.ui.Cells.DividerCell
import org.telegram.ui.Cells.EditTextSettingsCell
import org.telegram.ui.Cells.FeaturedStickerSetCell2
import org.telegram.ui.Cells.GraySectionCell
import org.telegram.ui.Cells.GroupCreateUserCell
import org.telegram.ui.Cells.HintDialogCell
import org.telegram.ui.Cells.LanguageCell
import org.telegram.ui.Cells.LetterSectionCell
import org.telegram.ui.Cells.ManageChatTextCell
import org.telegram.ui.Cells.ManageChatUserCell
import org.telegram.ui.Cells.MentionCell
import org.telegram.ui.Cells.PollEditTextCell
import org.telegram.ui.Cells.ProfileSearchCell
import org.telegram.ui.Cells.RadioColorCell
import org.telegram.ui.Cells.SessionCell
import org.telegram.ui.Cells.ShareDialogCell
import org.telegram.ui.Cells.SharedAudioCell
import org.telegram.ui.Cells.SharedDocumentCell
import org.telegram.ui.Cells.SharedLinkCell
import org.telegram.ui.Cells.StickerSetCell
import org.telegram.ui.Cells.TextCheckBoxCell
import org.telegram.ui.Cells.TextCheckCell2
import org.telegram.ui.Cells.TextColorCell
import org.telegram.ui.Cells.TextDetailCell
import org.telegram.ui.Cells.TextInfoCell
import org.telegram.ui.Cells.TextRadioCell
import org.telegram.ui.Cells.TextRightIconCell
import org.telegram.ui.Cells.UserCell
import org.telegram.ui.Cells.UserCell2
import org.telegram.ui.ChatActivity
import org.telegram.ui.Components.BottomPagerTabs
import org.telegram.ui.Components.Bulletin
import org.telegram.ui.Components.ChatActivityEnterTopView
import org.telegram.ui.Components.ChatActivityTopPanelLayout
import org.telegram.ui.Components.ChatAvatarContainer
import org.telegram.ui.Components.ChatGreetingsView
import org.telegram.ui.Components.CheckBox2
import org.telegram.ui.Components.CheckBoxSquare
import org.telegram.ui.Components.EditTextBoldCursor
import org.telegram.ui.Components.EmojiView
import org.telegram.ui.Components.FilterTabsView
import org.telegram.ui.Components.FragmentContextView
import org.telegram.ui.Components.FragmentFloatingButton
import org.telegram.ui.Components.FragmentSearchField
import org.telegram.ui.Components.MentionsContainerView
import org.telegram.ui.Components.RadioButton
import org.telegram.ui.Components.ReactionsContainerLayout
import org.telegram.ui.Components.RecyclerListView
import org.telegram.ui.Components.ScrollSlidingTextTabStrip
import org.telegram.ui.Components.SearchField
import org.telegram.ui.Components.SeekBarView
import org.telegram.ui.Components.SuggestEmojiView
import org.telegram.ui.Components.TopicsTabsView
import org.telegram.ui.Components.UndoView
import org.telegram.ui.Components.UnreadCounterTextView
import org.telegram.ui.Components.ViewPagerFixed
import org.telegram.ui.Components.chat.layouts.ChatActivitySideControlsButtonsLayout
import org.telegram.ui.DialogsActivity
import org.telegram.ui.Stories.DialogStoriesCell
import org.telegram.ui.Stories.recorder.ButtonWithCounterView
import org.telegram.ui.bots.BotCommandsMenuView
import org.telegram.ui.bots.BotKeyboardView

/**
 * Заготовки для остальных элементов Telegram: шапка чата, закреп, кнопки «вниз»,
 * клавиатура бота, эмодзи, меню, окна, уведомления, плеер, списки людей, файлы,
 * стикеры, поиск, вкладки, галочки, переключатели, ползунки, кнопки и экраны целиком.
 *
 * Каждая заготовка — это части «как их назвал бы человек» и цвета темы каждой части.
 * Цвета темы общие: изменил здесь — поменялось везде, где Telegram их использует.
 */
object MoreCatalog {

    // ------------------------------------------------------------------ DSL

    /** Часть элемента: подпись, как её найти на экране и её цвета. */
    class PartDef(val part: EditorSpec.Part, val find: (View, RectF) -> Boolean)

    private fun c(key: Int, ru: String, en: String) = ColorItem(key, ru, en)

    private fun part(id: String, ru: String, en: String, find: (View, RectF) -> Boolean, vararg colors: ColorItem,
                     size: String? = null) =
        PartDef(EditorSpec.Part(id, EngineStrings.pick(ru, en), false, colors.toList(), size), find)

    private fun spec(id: String, ru: String, en: String, kind: EditorSpec.Kind, parts: List<PartDef>,
                     extra: List<ColorGroup> = emptyList(), matcher: (View) -> Boolean): EditorSpec {
        // «Все цвета» — по частям, плюс то, что не относится ни к одной части.
        val groups = parts.filter { it.part.colors.isNotEmpty() }.map { p ->
            ColorGroup(p.part.title, p.part.title, p.part.colors)
        } + extra
        return CatalogSpec(id, ru, en, groups, parts.map { it.part },
            { v, partId, out -> parts.firstOrNull { it.part.id == partId }?.find?.invoke(v, out) ?: false },
            kind, matcher)
    }

    private fun el(id: String, ru: String, en: String, vararg parts: PartDef, matcher: (View) -> Boolean) =
        spec(id, ru, en, EditorSpec.Kind.ELEMENT, parts.toList(), matcher = matcher)

    private fun widget(id: String, ru: String, en: String, vararg parts: PartDef, matcher: (View) -> Boolean) =
        spec(id, ru, en, EditorSpec.Kind.WIDGET, parts.toList(), matcher = matcher)

    private fun screen(id: String, ru: String, en: String, vararg parts: PartDef, matcher: (View) -> Boolean) =
        spec(id, ru, en, EditorSpec.Kind.CONTAINER, parts.toList(), matcher = matcher)

    // ------------------------------------------------------------------ поиск частей

    private val whole: (View, RectF) -> Boolean = { v, out -> out.set(0f, 0f, v.width.toFloat(), v.height.toFloat()); true }

    private fun bounds(root: View, child: View?, out: RectF) = ThemeCatalog.childBounds(root, child, out)

    private fun descendants(v: View, out: MutableList<View> = ArrayList()): List<View> {
        if (v is ViewGroup) for (i in 0 until v.childCount) {
            val c = v.getChildAt(i)
            if (c.visibility == View.VISIBLE && c.width > 0 && c.height > 0) {
                out += c
                descendants(c, out)
            }
        }
        return out
    }

    /** Первый видимый потомок, подходящий под условие. */
    private fun first(pred: (View) -> Boolean): (View, RectF) -> Boolean = { v, out ->
        bounds(v, descendants(v).firstOrNull(pred), out)
    }

    /** Все видимые потомки под условием — одной рамкой. */
    private fun all(pred: (View) -> Boolean): (View, RectF) -> Boolean = { v, out ->
        val r = RectF()
        var any = false
        for (d in descendants(v)) if (pred(d) && bounds(v, d, r)) {
            if (!any) out.set(r) else out.union(r)
            any = true
        }
        any
    }

    private fun view(get: (View) -> View?): (View, RectF) -> Boolean = { v, out -> bounds(v, get(v), out) }

    /** Текст, нарисованный цветом темы [keys]: так находим «имя», «статус», «кнопку» в ячейках из TextView. */
    private fun textOf(vararg keys: Int): (View, RectF) -> Boolean = all { d ->
        val color = textColor(d) ?: return@all false
        keys.any { Theme.getColor(it) == color }
    }

    internal fun textColor(v: View): Int? = when (v) {
        is EditText -> null
        is TextView -> v.currentTextColor
        is SimpleTextView -> v.textColor
        else -> null
    }

    private val texts = all { it is TextView && it !is EditText || it is SimpleTextView }
    private val icons = all { it is ImageView }
    private val fields = first { it is EditText }

    /** Область, нарисованная на канвасе: доли ширины/высоты. Для RTL зеркалим по горизонтали. */
    private fun area(l: Float, t: Float, r: Float, b: Float): (View, RectF) -> Boolean = { v, out ->
        val w = v.width.toFloat()
        val h = v.height.toFloat()
        if (org.telegram.messenger.LocaleController.isRTL) out.set(w * (1 - r), h * t, w * (1 - l), h * b)
        else out.set(w * l, h * t, w * r, h * b)
        w > 0 && h > 0
    }

    /** Ячейки «аватар + две строки», нарисованные на канвасе: аватар слева, имя сверху, подпись снизу. */
    private fun avatarArea(dpWidth: Float): (View, RectF) -> Boolean = { v, out ->
        val w = AndroidUtilities.dp(dpWidth).toFloat().coerceAtMost(v.width.toFloat())
        if (org.telegram.messenger.LocaleController.isRTL) out.set(v.width - w, 0f, v.width.toFloat(), v.height.toFloat())
        else out.set(0f, 0f, w, v.height.toFloat())
        v.width > 0
    }

    private fun lineArea(dpStart: Float, top: Boolean): (View, RectF) -> Boolean = { v, out ->
        val s = AndroidUtilities.dp(dpStart).toFloat()
        val half = v.height / 2f
        val t = if (top) 0f else half
        val b = if (top) half else v.height.toFloat()
        if (org.telegram.messenger.LocaleController.isRTL) out.set(0f, t, v.width - s, b) else out.set(s, t, v.width.toFloat(), b)
        v.width > 0
    }

    // ------------------------------------------------------------------ общие наборы цветов

    private val listText = arrayOf(
        c(Theme.key_windowBackgroundWhiteBlackText, "Основной текст в списках", "Main text in lists"))
    private val listStatus = arrayOf(
        c(Theme.key_windowBackgroundWhiteGrayText, "Серый статус («был(а) недавно»)", "Gray status (“last seen”)"),
        c(Theme.key_windowBackgroundWhiteGrayText3, "Серая подпись", "Gray caption"),
        c(Theme.key_windowBackgroundWhiteBlueText, "Синий статус («в сети»)", "Blue status (“online”)"))
    private val listRow = arrayOf(
        c(Theme.key_windowBackgroundWhite, "Фон строк в списках", "List row background"),
        c(Theme.key_listSelector, "Подсветка при нажатии", "Press highlight"),
        c(Theme.key_divider, "Разделитель", "Divider"))
    private val dialogText = arrayOf(
        c(Theme.key_dialogTextBlack, "Текст в окнах", "Text in dialogs"),
        c(Theme.key_dialogTextGray2, "Серый текст в окнах", "Gray text in dialogs"),
        c(Theme.key_dialogTextLink, "Ссылки в окнах", "Links in dialogs"))
    private val checks = arrayOf(
        c(Theme.key_checkbox, "Галочка — фон", "Check — background"),
        c(Theme.key_checkboxCheck, "Галочка — знак", "Check — mark"),
        c(Theme.key_checkboxDisabled, "Галочка — недоступна", "Check — disabled"),
        c(Theme.key_dialogRoundCheckBox, "Круглая галочка в окнах — фон", "Round check in dialogs — background"),
        c(Theme.key_dialogRoundCheckBoxCheck, "Круглая галочка в окнах — знак", "Round check in dialogs — mark"))
    private val glassIcons = c(Theme.key_glass_defaultIcon, "Иконки на «стеклянных» кнопках", "Icons on “glass” buttons")
    private val glassText = c(Theme.key_glass_defaultText, "Текст на «стеклянных» кнопках", "Text on “glass” buttons")
    private val accentButton = arrayOf(
        c(Theme.key_featuredStickers_addButton, "Синяя кнопка — фон", "Accent button — background"),
        c(Theme.key_featuredStickers_addButtonPressed, "Синяя кнопка — нажатая", "Accent button — pressed"),
        c(Theme.key_featuredStickers_buttonText, "Синяя кнопка — текст", "Accent button — text"))
    private val tabs = arrayOf(
        c(Theme.key_actionBarTabActiveText, "Выбранная вкладка", "Selected tab"),
        c(Theme.key_actionBarTabUnactiveText, "Остальные вкладки", "Other tabs"),
        c(Theme.key_actionBarTabLine, "Полоска под выбранной", "Selected tab line"),
        c(Theme.key_actionBarTabSelector, "Подсветка при нажатии", "Press highlight"))
    private val profileTabs = arrayOf(
        c(Theme.key_profile_tabSelectedText, "Выбранная вкладка", "Selected tab"),
        c(Theme.key_profile_tabText, "Остальные вкладки", "Other tabs"),
        c(Theme.key_profile_tabSelectedLine, "Полоска под выбранной", "Selected tab line"),
        c(Theme.key_profile_tabSelector, "Подсветка при нажатии", "Press highlight"))
    private val unreadTabs = arrayOf(
        c(Theme.key_chats_tabUnreadActiveBackground, "Счётчик на выбранной вкладке", "Counter on selected tab"),
        c(Theme.key_chats_tabUnreadUnactiveBackground, "Счётчик на остальных", "Counter on other tabs"))
    private val menu = arrayOf(
        c(Theme.key_actionBarDefaultSubmenuBackground, "Фон меню", "Menu background"),
        c(Theme.key_actionBarDefaultSubmenuItem, "Текст пунктов", "Item text"),
        c(Theme.key_actionBarDefaultSubmenuItemIcon, "Иконки пунктов", "Item icons"),
        c(Theme.key_actionBarDefaultSubmenuSeparator, "Разделитель групп", "Group separator"),
        c(Theme.key_dialogButtonSelector, "Подсветка при нажатии", "Press highlight"))
    private val inputs = arrayOf(
        c(Theme.key_windowBackgroundWhiteBlackText, "Текст", "Text"),
        c(Theme.key_windowBackgroundWhiteHintText, "Подсказка в пустом поле", "Hint in empty field"),
        c(Theme.key_windowBackgroundWhiteInputField, "Линия под полем", "Field underline"),
        c(Theme.key_windowBackgroundWhiteInputFieldActivated, "Линия под полем — при вводе", "Field underline — focused"),
        c(Theme.key_dialogInputField, "Линия под полем в окнах", "Field underline in dialogs"),
        c(Theme.key_dialogInputFieldActivated, "Линия в окнах — при вводе", "Underline in dialogs — focused"),
        c(Theme.key_dialogTextHint, "Подсказка в окнах", "Hint in dialogs"))

    // ------------------------------------------------------------------ элементы

    private val people: (View) -> Boolean = {
        it is UserCell || it is UserCell2 || it is ManageChatUserCell || it is GroupCreateUserCell
    }

    val specs: List<EditorSpec> = listOf(

        // ---------- экран чата
        el("chat_header", "Шапка чата", "Chat header",
            part("avatar", "Аватар", "Avatar", view { (it as ChatAvatarContainer).avatarImageView }),
            part("title", "Название", "Title", view { (it as ChatAvatarContainer).titleTextView },
                c(Theme.key_actionBarDefaultTitle, "Цвет названия", "Title color"),
                c(Theme.key_profile_verifiedBackground, "Галочка верификации — фон", "Verified badge — background"),
                c(Theme.key_profile_verifiedCheck, "Галочка верификации — знак", "Verified badge — check")),
            part("subtitle", "Статус («в сети», «печатает…»)", "Status (“online”, “typing…”)", view { (it as ChatAvatarContainer).subtitleTextView },
                c(Theme.key_actionBarDefaultSubtitle, "Обычный статус", "Regular status"),
                c(Theme.key_chat_status, "«Печатает…», «в сети»", "“Typing…”, “online”")),
            part("all", "Фон шапки", "Header background", whole, c(Theme.key_actionBarDefault, "Фон", "Background")),
        ) { it is ChatAvatarContainer },

        el("chat_top_panel", "Плашки над чатом (закреп, «добавить контакт»)", "Panels above chat (pinned, “add contact”)",
            part("title", "Заголовок («Закреплённое сообщение»)", "Title (“Pinned message”)", textOf(Theme.key_chat_topPanelTitle),
                c(Theme.key_chat_topPanelTitle, "Цвет заголовка", "Title color")),
            part("message", "Текст закрепа", "Pinned text", textOf(Theme.key_chat_topPanelMessage),
                c(Theme.key_chat_topPanelMessage, "Цвет текста", "Text color")),
            part("buttons", "Крестик и список закрепов", "Close and pinned list", icons,
                c(Theme.key_chat_topPanelClose, "Цвет иконок", "Icon color")),
            part("all", "Фон плашки", "Panel background", whole,
                c(Theme.key_chat_topPanelBackground, "Фон", "Background"),
                c(Theme.key_chat_topPanelLine, "Полоска слева", "Line on the left")),
        ) { it is ChatActivityTopPanelLayout },

        el("chat_side_buttons", "Кнопки «вниз», «@» и реакций", "“Down”, “@” and reaction buttons",
            part("all", "Круглые кнопки", "Round buttons", whole,
                glassIcons,
                c(Theme.key_chat_messagePanelBackground, "Фон «стекла»", "“Glass” background"),
                c(Theme.key_chat_goDownButtonCounterBackground, "Счётчик — фон", "Counter — background"),
                c(Theme.key_chat_goDownButtonCounter, "Счётчик — цифры", "Counter — digits")),
        ) { it is ChatActivitySideControlsButtonsLayout },

        el("chat_unread", "Плашка «Непрочитанные сообщения»", "“Unread messages” bar",
            part("all", "Плашка", "Bar", whole,
                c(Theme.key_chat_unreadMessagesStartBackground, "Фон", "Background"),
                c(Theme.key_chat_unreadMessagesStartText, "Текст", "Text"),
                c(Theme.key_chat_unreadMessagesStartArrowIcon, "Стрелка", "Arrow")),
        ) { it is ChatUnreadCell },

        el("chat_reply_panel", "Ответ, пересылка, правка над полем", "Reply, forward, edit above input",
            part("text", "Имя и текст", "Name and text", texts,
                c(Theme.key_chat_replyPanelName, "Имя", "Name"),
                c(Theme.key_chat_messageLinkIn, "Ссылки", "Links")),
            part("icons", "Иконки и крестик", "Icons and close", icons,
                c(Theme.key_chat_replyPanelIcons, "Иконка слева", "Left icon"),
                c(Theme.key_chat_replyPanelClose, "Крестик", "Close"), glassIcons),
            part("all", "Полоска и фон", "Line and background", whole,
                c(Theme.key_chat_replyPanelLine, "Полоска", "Line"),
                c(Theme.key_chat_messagePanelBackground, "Фон", "Background")),
        ) { it is ChatActivityEnterTopView },

        el("bot_keyboard", "Клавиатура бота", "Bot keyboard",
            part("buttons", "Кнопки", "Buttons", texts,
                c(Theme.key_chat_botKeyboardButtonText, "Текст кнопок", "Button text"),
                c(Theme.key_chat_botKeyboardButtonBackground, "Фон кнопок", "Button background"),
                c(Theme.key_chat_botKeyboardButtonBackgroundPressed, "Нажатая кнопка", "Pressed button")),
            part("all", "Фон клавиатуры", "Keyboard background", whole,
                c(Theme.key_chat_emojiPanelBackground, "Фон", "Background")),
        ) { it is BotKeyboardView },

        el("bot_menu", "Кнопка «Меню» бота", "Bot “Menu” button",
            part("all", "Кнопка", "Button", whole,
                c(Theme.key_chat_messagePanelVoiceBackground, "Фон", "Background"),
                c(Theme.key_chat_messagePanelVoiceDuration, "Текст и иконка", "Text and icon")),
        ) { it is BotCommandsMenuView },

        el("emoji_panel", "Панель эмодзи, стикеров и GIF", "Emoji, stickers and GIF panel",
            part("all", "Панель", "Panel", whole,
                c(Theme.key_chat_emojiPanelBackground, "Фон", "Background"),
                c(Theme.key_chat_emojiPanelIcon, "Иконки разделов", "Section icons"),
                c(Theme.key_chat_emojiPanelIconSelected, "Выбранный раздел", "Selected section"),
                c(Theme.key_chat_emojiPanelStickerPackSelectorLine, "Полоска под выбранным", "Selected line"),
                c(Theme.key_chat_emojiPanelStickerSetName, "Названия наборов", "Set names"),
                c(Theme.key_chat_emojiPanelEmptyText, "Текст «ничего нет»", "“Nothing here” text"),
                c(Theme.key_chat_emojiPanelBackspace, "Кнопка «стереть»", "Backspace"),
                c(Theme.key_chat_emojiPanelShadowLine, "Линия-разделитель", "Divider line"),
                c(Theme.key_chat_emojiSearchBackground, "Поиск — фон", "Search — background"),
                c(Theme.key_chat_emojiSearchIcon, "Поиск — иконка", "Search — icon"),
                glassIcons),
        ) { it is EmojiView },

        el("mentions", "Подсказки (@упоминания, команды)", "Suggestions (@mentions, commands)",
            part("all", "Список подсказок", "Suggestion list", whole,
                c(Theme.key_chat_messagePanelBackground, "Фон", "Background"), *listText,
                c(Theme.key_windowBackgroundWhiteGrayText3, "@имя и описание", "@username and description")),
        ) { it is MentionsContainerView },

        el("mention_row", "Подсказка в списке", "Suggestion row",
            part("name", "Имя", "Name", textOf(Theme.key_windowBackgroundWhiteBlackText), *listText),
            part("username", "@имя / описание", "@username / description", textOf(Theme.key_windowBackgroundWhiteGrayText3),
                c(Theme.key_windowBackgroundWhiteGrayText3, "Цвет", "Color")),
        ) { it is MentionCell },

        el("emoji_suggest", "Подсказка эмодзи над полем", "Emoji suggestion above input",
            part("all", "Плашка", "Panel", whole, c(Theme.key_chat_stickersHintPanel, "Фон", "Background"))
        ) { it is SuggestEmojiView },

        el("reactions", "Панель реакций", "Reactions bar",
            part("all", "Панель", "Bar", whole,
                c(Theme.key_actionBarDefaultSubmenuBackground, "Фон", "Background"),
                c(Theme.key_listSelector, "Подсветка при нажатии", "Press highlight"),
                c(Theme.key_windowBackgroundWhiteGrayIcon, "Стрелка «ещё»", "“More” arrow")),
        ) { it is ReactionsContainerLayout },

        el("channel_button", "Кнопка внизу («Включить звук», «Вступить»)", "Bottom button (“Unmute”, “Join”)",
            part("all", "Кнопка", "Button", whole, glassText,
                c(Theme.key_chat_fieldOverlayText, "Текст (старый стиль)", "Text (classic style)"),
                c(Theme.key_chat_messagePanelBackground, "Фон", "Background"),
                c(Theme.key_chat_goDownButtonCounterBackground, "Счётчик", "Counter")),
        ) { it is UnreadCounterTextView },

        el("greeting", "Приветствие в пустом чате", "Empty chat greeting",
            part("all", "Плашка", "Pill", whole,
                c(Theme.key_chat_serviceBackground, "Фон", "Background"),
                c(Theme.key_chat_serviceText, "Текст", "Text")),
        ) { it is ChatGreetingsView },

        el("bot_info", "Описание бота", "Bot description",
            part("all", "Пузырь описания", "Description bubble", whole,
                c(Theme.key_chat_inBubble, "Фон (как у входящих)", "Background (as incoming)"),
                c(Theme.key_chat_messageTextIn, "Текст", "Text"),
                c(Theme.key_chat_messageLinkIn, "Ссылки", "Links")),
        ) { it is BotHelpCell },

        el("player", "Плеер и звонок над чатами", "Player and call bar",
            part("title", "Название и исполнитель", "Title and performer", texts,
                c(Theme.key_inappPlayerTitle, "Название", "Title"),
                c(Theme.key_inappPlayerPerformer, "Исполнитель", "Performer"),
                c(Theme.key_returnToCallText, "Текст звонка", "Call text")),
            part("buttons", "Кнопки", "Buttons", icons,
                c(Theme.key_inappPlayerPlayPause, "Пауза / play", "Play / pause"),
                c(Theme.key_inappPlayerClose, "Крестик", "Close")),
            part("all", "Фон", "Background", whole,
                c(Theme.key_inappPlayerBackground, "Фон плеера", "Player background"),
                c(Theme.key_returnToCallBackground, "Фон звонка", "Call background"),
                c(Theme.key_returnToCallMutedBackground, "Фон звонка (микрофон выкл.)", "Call background (muted)")),
        ) { it is FragmentContextView },

        // ---------- список чатов
        el("folder_tabs", "Вкладки папок", "Folder tabs",
            part("all", "Вкладки", "Tabs", whole, *tabs, *unreadTabs,
                c(Theme.key_actionBarDefault, "Фон", "Background")),
        ) { it is FilterTabsView || it is ViewPagerFixed.TabsView },

        el("topic_tabs", "Вкладки тем", "Topic tabs",
            part("all", "Вкладки", "Tabs", whole, *listText,
                c(Theme.key_windowBackgroundWhiteGrayText2, "Остальные темы", "Other topics"),
                c(Theme.key_chats_unreadCounter, "Счётчик", "Counter"),
                c(Theme.key_chats_unreadCounterText, "Цифры счётчика", "Counter digits"),
                c(Theme.key_windowBackgroundWhite, "Фон", "Background")),
        ) { it is TopicsTabsView },

        el("stories", "Истории", "Stories",
            part("all", "Лента историй", "Stories row", whole,
                c(Theme.key_actionBarDefaultTitle, "Подписи", "Captions"),
                c(Theme.key_actionBarDefault, "Фон", "Background"),
                c(Theme.key_actionBarDefaultArchived, "Фон в архиве", "Background in archive")),
        ) { it is DialogStoriesCell },

        el("fab", "Круглая кнопка (новое сообщение)", "Floating button (new message)",
            part("all", "Кнопка", "Button", whole,
                c(Theme.key_chats_actionIcon, "Иконка", "Icon"),
                c(Theme.key_featuredStickers_addButton, "Фон", "Background"),
                c(Theme.key_featuredStickers_addButtonPressed, "Фон при нажатии", "Pressed background"),
                c(Theme.key_actionBarDefaultIcon, "Иконка маленькой кнопки", "Small button icon"),
                c(Theme.key_windowBackgroundWhite, "Фон маленькой кнопки", "Small button background")),
        ) { it is FragmentFloatingButton },

        el("dialogs_hint", "Подсказка над списком чатов", "Hint above chat list",
            part("title", "Заголовок", "Title", textOf(Theme.key_windowBackgroundWhiteBlackText), *listText),
            part("text", "Текст", "Text", textOf(Theme.key_windowBackgroundWhiteGrayText),
                c(Theme.key_windowBackgroundWhiteGrayText, "Цвет", "Color")),
            part("all", "Фон", "Background", whole, c(Theme.key_windowBackgroundWhite, "Фон", "Background")),
        ) { it is DialogsHintCell },

        el("dialogs_empty", "Пустой список чатов", "Empty chat list",
            part("all", "Текст", "Text", whole,
                c(Theme.key_chats_nameMessage_threeLines, "Заголовок", "Title"),
                c(Theme.key_chats_message, "Текст", "Text")),
        ) { it is DialogsEmptyCell },

        el("search_chat_row", "Чат в поиске", "Chat in search",
            part("avatar", "Аватар", "Avatar", avatarArea(72f)),
            part("name", "Название", "Name", lineArea(72f, true),
                c(Theme.key_chats_name, "Цвет названия", "Name color"),
                c(Theme.key_chats_secretName, "Секретный чат", "Secret chat"),
                c(Theme.key_chats_verifiedBackground, "Галочка верификации", "Verified badge")),
            part("status", "Подпись", "Caption", lineArea(72f, false),
                c(Theme.key_windowBackgroundWhiteGrayText3, "Серая подпись", "Gray caption"),
                c(Theme.key_windowBackgroundWhiteBlueText3, "Синяя подпись («в сети»)", "Blue caption (“online”)"),
                c(Theme.key_chats_unreadCounter, "Счётчик — фон", "Counter — background"),
                c(Theme.key_chats_unreadCounterText, "Счётчик — цифры", "Counter — digits")),
            part("all", "Фон строки", "Row background", whole, *listRow),
        ) { it is ProfileSearchCell },

        el("recent_chat", "Недавний чат в поиске", "Recent chat in search",
            part("all", "Аватар и имя", "Avatar and name", whole, *listText,
                c(Theme.key_chats_onlineCircle, "Кружок «в сети»", "Online dot"),
                c(Theme.key_chats_unreadCounter, "Счётчик", "Counter")),
        ) { it is HintDialogCell },

        el("search_field", "Поле поиска", "Search field",
            part("field", "Ввод", "Input", fields,
                c(Theme.key_dialogSearchText, "Текст поиска", "Search text"),
                c(Theme.key_dialogSearchHint, "Подсказка «Поиск»", "“Search” hint"),
                c(Theme.key_windowBackgroundWhiteBlackText, "Текст поиска (в списках)", "Search text (in lists)")),
            part("all", "Фон поля", "Field background", whole,
                c(Theme.key_dialogSearchBackground, "Фон", "Background"),
                c(Theme.key_dialogSearchIcon, "Лупа и крестик", "Magnifier and clear"),
                c(Theme.key_windowBackgroundWhite, "Фон (в списках)", "Background (in lists)")),
        ) { it is SearchField || it is FragmentSearchField },

        // ---------- люди, файлы, стикеры
        el("person", "Человек в списке", "Person in a list",
            part("name", "Имя", "Name", textOf(Theme.key_windowBackgroundWhiteBlackText), *listText),
            part("status", "Статус", "Status",
                textOf(Theme.key_windowBackgroundWhiteGrayText, Theme.key_windowBackgroundWhiteBlueText, Theme.key_windowBackgroundWhiteGrayText3),
                *listStatus),
            part("avatar", "Аватар", "Avatar", first { it is org.telegram.ui.Components.BackupImageView }),
            part("all", "Фон строки", "Row background", whole, *listRow,
                c(Theme.key_profile_creatorIcon, "Метка «владелец» / «админ»", "“Owner” / “admin” tag")),
        ) { people(it) },

        el("session", "Устройство (активные сеансы)", "Device (active sessions)",
            part("all", "Строка", "Row", whole, *listText,
                c(Theme.key_windowBackgroundWhiteGrayText3, "Подробности", "Details"),
                c(Theme.key_windowBackgroundWhiteValueText, "«В сети»", "“Online”")),
        ) { it is SessionCell },

        el("file", "Файл, ссылка или музыка", "File, link or music",
            part("all", "Строка", "Row", whole, *listText,
                c(Theme.key_windowBackgroundWhiteGrayText3, "Размер и дата", "Size and date"),
                c(Theme.key_windowBackgroundWhiteLinkText, "Ссылка", "Link"),
                c(Theme.key_files_folderIcon, "Иконка папки", "Folder icon"),
                c(Theme.key_files_folderIconBackground, "Фон иконки", "Icon background"),
                c(Theme.key_files_iconText, "Текст на иконке", "Icon text"),
                c(Theme.key_windowBackgroundWhite, "Фон", "Background")),
        ) { it is SharedDocumentCell || it is SharedLinkCell || it is SharedAudioCell },

        el("sticker_set", "Набор стикеров", "Sticker set",
            part("title", "Название", "Title", textOf(Theme.key_windowBackgroundWhiteBlackText), *listText),
            part("count", "Количество", "Count", textOf(Theme.key_windowBackgroundWhiteGrayText2),
                c(Theme.key_windowBackgroundWhiteGrayText2, "Цвет", "Color")),
            part("button", "Кнопка «Добавить»", "“Add” button", first { it is TextView && textColor(it) == Theme.getColor(Theme.key_featuredStickers_buttonText) },
                *accentButton,
                c(Theme.key_featuredStickers_removeButtonText, "«Удалить»", "“Remove”")),
            part("all", "Фон", "Background", whole, *listRow,
                c(Theme.key_stickers_menu, "Иконка «⋮»", "“⋮” icon")),
        ) { it is StickerSetCell || it is FeaturedStickerSetCell2 },

        el("share_chat", "Чат в окне «Поделиться»", "Chat in Share sheet",
            part("all", "Аватар и имя", "Avatar and name", whole,
                c(Theme.key_dialogTextBlack, "Имя", "Name"),
                c(Theme.key_dialogRoundCheckBox, "Галочка — фон", "Check — background"),
                c(Theme.key_dialogRoundCheckBoxCheck, "Галочка — знак", "Check — mark"),
                c(Theme.key_chats_onlineCircle, "Кружок «в сети»", "Online dot")),
        ) { it is ShareDialogCell },

        el("section_gray", "Заголовок раздела (серая полоса)", "Section header (gray bar)",
            part("all", "Полоса", "Bar", whole,
                c(Theme.key_graySection, "Фон", "Background"),
                c(Theme.key_graySectionText, "Текст", "Text")),
        ) { it is GraySectionCell },

        el("section_letter", "Буква в списке контактов", "Letter in contacts",
            part("all", "Буква", "Letter", whole, c(Theme.key_windowBackgroundWhiteGrayText4, "Цвет", "Color")),
        ) { it is LetterSectionCell },

        el("info_row", "Строка информации (телефон, имя, о себе)", "Info row (phone, username, bio)",
            part("value", "Значение", "Value", textOf(Theme.key_windowBackgroundWhiteBlackText), *listText,
                c(Theme.key_chat_messageLinkIn, "Ссылки", "Links")),
            part("label", "Подпись («Телефон», «О себе»)", "Label (“Phone”, “Bio”)", textOf(Theme.key_windowBackgroundWhiteGrayText2),
                c(Theme.key_windowBackgroundWhiteGrayText2, "Цвет", "Color")),
            part("all", "Фон", "Background", whole, *listRow),
        ) { it is TextDetailCell || it is AboutLinkCell },

        el("list_row", "Пункт списка", "List row",
            part("text", "Текст", "Text", texts, *listText, *dialogText,
                c(Theme.key_windowBackgroundWhiteGrayText2, "Описание", "Description"),
                c(Theme.key_windowBackgroundWhiteValueText, "Значение", "Value")),
            part("control", "Галочка / переключатель", "Check / switch", first { it is CheckBox2 || it is CheckBoxSquare || it is RadioButton || it is org.telegram.ui.Components.Switch },
                *checks,
                c(Theme.key_checkboxSquareBackground, "Квадратная галочка — фон", "Square check — background"),
                c(Theme.key_checkboxSquareCheck, "Квадратная галочка — знак", "Square check — mark"),
                c(Theme.key_radioBackground, "Кружок выбора", "Radio"),
                c(Theme.key_radioBackgroundChecked, "Кружок выбора — выбран", "Radio — checked"),
                c(Theme.key_switchTrack, "Переключатель выкл.", "Switch off"),
                c(Theme.key_switchTrackChecked, "Переключатель вкл.", "Switch on")),
            part("field", "Поле ввода", "Input field", fields, *inputs),
            part("all", "Фон", "Background", whole, *listRow,
                c(Theme.key_dialogBackground, "Фон в окнах", "Background in dialogs")),
        ) {
            it is CheckBoxCell || it is TextCheckCell2 || it is TextRadioCell || it is LanguageCell ||
                it is TextColorCell || it is EditTextSettingsCell || it is PollEditTextCell || it is TextCheckBoxCell ||
                it is RadioColorCell || it is TextRightIconCell || it is ManageChatTextCell || it is CollapseTextCell ||
                it is TextInfoCell
        },

        el("divider", "Разделитель", "Divider",
            part("all", "Линия", "Line", whole, c(Theme.key_divider, "Цвет", "Color")),
        ) { it is DividerCell },

        el("tabs", "Вкладки", "Tabs",
            part("all", "Вкладки", "Tabs", whole, *tabs, *profileTabs),
        ) { it is ScrollSlidingTextTabStrip },

        el("pager_tabs", "Нижние вкладки (медиа)", "Bottom tabs (media)",
            part("all", "Вкладки", "Tabs", whole, *listText,
                c(Theme.key_windowBackgroundWhiteGrayText6, "Невыбранные", "Unselected"),
                c(Theme.key_windowBackgroundWhite, "Фон", "Background")),
        ) { it is BottomPagerTabs },

        // ---------- меню, окна, уведомления
        el("menu_item", "Пункт меню", "Menu item",
            part("text", "Текст", "Text", view { (it as ActionBarMenuSubItem).textView },
                c(Theme.key_actionBarDefaultSubmenuItem, "Цвет текста", "Text color")),
            part("icon", "Иконка", "Icon", view { (it as ActionBarMenuSubItem).imageView },
                c(Theme.key_actionBarDefaultSubmenuItemIcon, "Цвет иконки", "Icon color")),
            part("all", "Фон пункта", "Item background", whole,
                c(Theme.key_actionBarDefaultSubmenuBackground, "Фон меню", "Menu background"),
                c(Theme.key_dialogButtonSelector, "Подсветка при нажатии", "Press highlight")),
        ) { it is ActionBarMenuSubItem },

        screen("menu", "Всплывающее меню", "Popup menu",
            part("all", "Меню", "Menu", whole, *menu),
        ) { it is ActionBarPopupWindow.ActionBarPopupWindowLayout },

        el("alert", "Окно с вопросом", "Dialog",
            part("buttons", "Кнопки («Отмена», «ОК»)", "Buttons (“Cancel”, “OK”)", textOf(Theme.key_dialogButton, Theme.key_text_RedBold),
                c(Theme.key_dialogButton, "Обычные кнопки", "Regular buttons"),
                c(Theme.key_text_RedBold, "Красные кнопки («Удалить»)", "Red buttons (“Delete”)"),
                c(Theme.key_dialogButtonSelector, "Подсветка при нажатии", "Press highlight")),
            part("text", "Заголовок и текст", "Title and text", textOf(Theme.key_dialogTextBlack, Theme.key_dialogTextGray2, Theme.key_dialogTextGray3),
                *dialogText,
                c(Theme.key_dialogTextGray3, "Бледный текст", "Faint text")),
            part("all", "Фон окна", "Dialog background", whole,
                c(Theme.key_dialogBackground, "Фон", "Background"),
                c(Theme.key_dialogIcon, "Иконки", "Icons"),
                c(Theme.key_dialogLineProgress, "Полоса загрузки", "Progress bar"),
                c(Theme.key_dialogLineProgressBackground, "Полоса загрузки — фон", "Progress bar — background")),
        ) { it is AlertDialog.AlertDialogView },

        screen("sheet", "Окно снизу", "Bottom sheet",
            part("all", "Окно", "Sheet", whole,
                c(Theme.key_dialogBackground, "Фон", "Background"),
                c(Theme.key_dialogBackgroundGray, "Серый фон", "Gray background"), *dialogText,
                c(Theme.key_sheet_scrollUp, "Полоска-ручка сверху", "Drag handle"),
                c(Theme.key_dialogIcon, "Иконки", "Icons"),
                c(Theme.key_dialogShadowLine, "Тень", "Shadow"), *accentButton),
        ) { it is BottomSheet.ContainerView },

        el("bulletin", "Уведомление внизу («Скопировано» и т. п.)", "Bottom notice (“Copied” etc.)",
            part("button", "Кнопка («Отменить»)", "Button (“Undo”)", textOf(Theme.key_undo_cancelColor),
                c(Theme.key_undo_cancelColor, "Цвет кнопки", "Button color")),
            part("all", "Плашка", "Notice", whole,
                c(Theme.key_undo_background, "Фон", "Background"),
                c(Theme.key_undo_infoColor, "Текст и иконка", "Text and icon")),
        ) { it is Bulletin.Layout || it is UndoView },

        // ---------- мелкие элементы
        widget("checkbox", "Галочка", "Check",
            part("all", "Галочка", "Check", whole, *checks,
                c(Theme.key_checkboxSquareBackground, "Квадратная — фон", "Square — background"),
                c(Theme.key_checkboxSquareCheck, "Квадратная — знак", "Square — mark"),
                c(Theme.key_checkboxSquareUnchecked, "Квадратная — рамка", "Square — frame"),
                c(Theme.key_dialogCheckboxSquareBackground, "Квадратная в окнах — фон", "Square in dialogs — background"),
                c(Theme.key_dialogCheckboxSquareCheck, "Квадратная в окнах — знак", "Square in dialogs — mark")),
        ) { it is CheckBox2 || it is CheckBoxSquare },

        widget("radio", "Кружок выбора", "Radio button",
            part("all", "Кружок", "Radio", whole,
                c(Theme.key_radioBackground, "Не выбран", "Unchecked"),
                c(Theme.key_radioBackgroundChecked, "Выбран", "Checked"),
                c(Theme.key_dialogRadioBackground, "В окнах — не выбран", "In dialogs — unchecked"),
                c(Theme.key_dialogRadioBackgroundChecked, "В окнах — выбран", "In dialogs — checked")),
        ) { it is RadioButton },

        widget("seekbar", "Ползунок", "Slider",
            part("all", "Ползунок", "Slider", whole,
                c(Theme.key_player_progress, "Заполненная часть и кружок", "Filled part and thumb"),
                c(Theme.key_player_progressBackground, "Пустая часть", "Empty part"),
                c(Theme.key_player_progressCachedBackground, "Загруженная часть", "Buffered part")),
        ) { it is SeekBarView },

        widget("input", "Поле ввода", "Input field",
            part("all", "Поле", "Field", whole, *inputs),
        ) { it is EditTextBoldCursor },

        el("button", "Кнопка", "Button",
            part("all", "Кнопка", "Button", whole, *accentButton,
                c(Theme.key_buttonNeutral, "Серая кнопка — фон", "Neutral button — background"),
                c(Theme.key_buttonNeutralText, "Серая кнопка — текст", "Neutral button — text")),
        ) { it is ButtonWithCounterView },

        // ---------- экраны целиком (выбираются, если под пальцем ничего конкретнее)
        screen("chat_screen", "Экран чата", "Chat screen",
            part("all", "Чат", "Chat", whole,
                c(Theme.key_chat_inBubble, "Входящие — пузырь", "Incoming — bubble"),
                c(Theme.key_chat_messageTextIn, "Входящие — текст", "Incoming — text"),
                c(Theme.key_chat_outBubble, "Исходящие — пузырь", "Outgoing — bubble"),
                c(Theme.key_chat_messageTextOut, "Исходящие — текст", "Outgoing — text"),
                c(Theme.key_chat_serviceBackground, "Плашки дат и событий", "Date and event pills"),
                c(Theme.key_chat_serviceText, "Текст на плашках", "Pill text"),
                c(Theme.key_chat_messagePanelBackground, "Панель ввода и «стекло»", "Input panel and “glass”"),
                glassIcons,
                c(Theme.key_chat_selectedBackground, "Подсветка выбранных сообщений", "Selected messages highlight")),
        ) { it is ChatActivity.ChatActivityFragmentView },

        screen("dialogs_screen", "Список чатов", "Chat list",
            part("all", "Список", "List", whole,
                c(Theme.key_windowBackgroundWhite, "Фон", "Background"),
                c(Theme.key_chats_name, "Названия чатов", "Chat names"),
                c(Theme.key_chats_message, "Сообщения", "Messages"),
                c(Theme.key_chats_date, "Время", "Time"),
                c(Theme.key_chats_unreadCounter, "Счётчики", "Counters"),
                c(Theme.key_divider, "Разделители", "Dividers"),
                c(Theme.key_chats_archiveBackground, "Архив — фон аватара", "Archive — avatar background"),
                c(Theme.key_chats_archiveIcon, "Архив — иконка", "Archive — icon"),
                c(Theme.key_chats_archivePullDownBackground, "Фон при вытягивании архива", "Pull-down archive background"),
                c(Theme.key_chats_archivePullDownBackgroundActive, "Фон архива — готов открыться", "Archive pull — ready")),
        ) { it is DialogsActivity.DialogsRecyclerView },

        screen("list_screen", "Экран", "Screen",
            part("all", "Экран", "Screen", whole,
                c(Theme.key_windowBackgroundGray, "Фон между разделами", "Background between sections"),
                c(Theme.key_windowBackgroundWhite, "Фон пунктов", "Row background"),
                c(Theme.key_windowBackgroundWhiteBlackText, "Текст пунктов", "Row text"),
                c(Theme.key_windowBackgroundWhiteBlueHeader, "Заголовки разделов", "Section headers"),
                c(Theme.key_windowBackgroundWhiteGrayText4, "Пояснения под разделами", "Section footers"),
                c(Theme.key_divider, "Разделители", "Dividers"),
                c(Theme.key_listSelector, "Подсветка при нажатии", "Press highlight"),
                c(Theme.key_fastScrollActive, "Быстрая прокрутка", "Fast scroll")),
        ) { it is RecyclerListView },
    )

    /** Цвет фона view, если это простой цвет. */
    internal fun backgroundColor(v: View): Int? = (v.background as? ColorDrawable)?.color
}
