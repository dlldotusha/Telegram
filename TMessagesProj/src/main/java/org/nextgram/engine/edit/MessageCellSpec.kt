package org.nextgram.engine.edit

import android.graphics.RectF
import android.view.View
import org.nextgram.engine.EngineStrings
import org.nextgram.engine.hooks.MessageCellHooks
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.MessageObject
import org.telegram.messenger.UserConfig
import org.telegram.ui.Cells.ChatMessageCell

/**
 * Заготовка для сообщения в чате. Пока одна часть — время: секунды, цвет, скрытие.
 * Новые части добавляются сюда же: id части, границы и свойства, плюс хук в ячейке.
 */
class MessageCellSpec : EditorSpec {

    private fun mo(view: View): MessageObject? = (view as ChatMessageCell).messageObject

    override fun handles(view: View) = view is ChatMessageCell && view.messageObject != null

    override fun elementId(view: View) = MessageCellHooks.elementId(mo(view))

    override fun title(view: View): String =
        EngineStrings.get(if (mo(view)?.isOutOwner == true) "msg_out" else "msg_in")

    override fun dialogId(view: View): Long = mo(view)?.dialogId ?: 0L

    override fun account(view: View): Int = mo(view)?.currentAccount ?: UserConfig.selectedAccount

    override fun parts(view: View) = listOf(
        EditorSpec.Part(MessageCellHooks.PART_TIME, EngineStrings.get("part_time"), hideable = true),
    )

    override fun partBounds(view: View, partId: String, out: RectF): Boolean {
        if (partId != MessageCellHooks.PART_TIME) return false
        if (!(view as ChatMessageCell).getCustomTimeBounds(out)) return false
        // палец толще текста — расширяем зону попадания
        out.inset(-AndroidUtilities.dp(6f).toFloat(), -AndroidUtilities.dp(6f).toFloat())
        return true
    }

    override fun props(view: View): List<EditorSpec.Prop> {
        val time = MessageCellHooks.PART_TIME
        return listOf(
            EditorSpec.Prop(time, MessageCellHooks.PROP_SECONDS, EditorSpec.Prop.Type.BOOL, EngineStrings.get("seconds")),
            EditorSpec.Prop(time, MessageCellHooks.PROP_COLOR, EditorSpec.Prop.Type.COLOR, EngineStrings.get("color")),
            EditorSpec.Prop(time, MessageCellHooks.PROP_HIDDEN, EditorSpec.Prop.Type.BOOL, EngineStrings.get("hidden")),
        )
    }

    override fun colorGroups(view: View) = ThemeCatalog.messageGroups(out = mo(view)?.isOutOwner == true)

    override fun apply(view: View) {
        val cell = view as ChatMessageCell
        if (cell.messageObject != null) {
            cell.forceResetMessageObject()
            cell.requestLayout()
            cell.invalidate()
        }
    }
}
