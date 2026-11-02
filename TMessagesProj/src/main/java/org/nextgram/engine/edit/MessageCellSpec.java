package org.nextgram.engine.edit;

import android.graphics.RectF;
import android.view.View;

import org.nextgram.engine.EngineStrings;
import org.nextgram.engine.hooks.MessageCellHooks;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Cells.ChatMessageCell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Заготовка для сообщения в чате. Пока одна часть — время: секунды, цвет, скрытие.
 * Новые части добавляются сюда же: id части, границы и свойства, плюс хук в ячейке.
 */
public final class MessageCellSpec implements EditorSpec {

    @Override
    public boolean handles(View view) {
        return view instanceof ChatMessageCell && ((ChatMessageCell) view).getMessageObject() != null;
    }

    private static MessageObject mo(View view) {
        return ((ChatMessageCell) view).getMessageObject();
    }

    @Override
    public String elementId(View view) {
        return MessageCellHooks.elementId(mo(view));
    }

    @Override
    public String title(View view) {
        MessageObject m = mo(view);
        return EngineStrings.get(m != null && m.isOutOwner() ? "msg_out" : "msg_in");
    }

    @Override
    public long dialogId(View view) {
        MessageObject m = mo(view);
        return m == null ? 0 : m.getDialogId();
    }

    @Override
    public int account(View view) {
        MessageObject m = mo(view);
        return m == null ? UserConfig.selectedAccount : m.currentAccount;
    }

    @Override
    public List<Part> parts(View view) {
        ArrayList<Part> parts = new ArrayList<>();
        parts.add(new Part(MessageCellHooks.PART_TIME, EngineStrings.get("part_time"), true));
        return parts;
    }

    @Override
    public boolean partBounds(View view, String partId, RectF out) {
        if (MessageCellHooks.PART_TIME.equals(partId)) {
            if (!((ChatMessageCell) view).getCustomTimeBounds(out)) {
                return false;
            }
            // палец толще текста — расширяем зону попадания
            out.inset(-org.telegram.messenger.AndroidUtilities.dp(6), -org.telegram.messenger.AndroidUtilities.dp(6));
            return true;
        }
        return false;
    }

    @Override
    public List<Prop> props(View view) {
        String time = MessageCellHooks.PART_TIME;
        ArrayList<Prop> props = new ArrayList<>();
        props.add(new Prop(time, MessageCellHooks.PROP_SECONDS, Prop.Type.BOOL, EngineStrings.get("seconds"), false));
        props.add(new Prop(time, MessageCellHooks.PROP_COLOR, Prop.Type.COLOR, EngineStrings.get("color"), false));
        props.add(new Prop(time, MessageCellHooks.PROP_HIDDEN, Prop.Type.BOOL, EngineStrings.get("hidden"), false));
        return Collections.unmodifiableList(props);
    }

    @Override
    public void apply(View view) {
        ChatMessageCell cell = (ChatMessageCell) view;
        if (cell.getMessageObject() != null) {
            cell.forceResetMessageObject();
            cell.requestLayout();
            cell.invalidate();
        }
    }
}
