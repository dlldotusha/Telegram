package org.nextgram.engine.hooks;

import org.nextgram.engine.NextEngine;
import org.nextgram.engine.core.ResolvedElement;
import org.nextgram.engine.edit.EditMode;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.time.FastDateFormat;

import java.util.HashMap;

/**
 * Хуки для ChatMessageCell. В апстримном файле только однострочные вызовы
 * сюда — вся логика здесь, чтобы мердж новых версий Telegram был дешёвым.
 */
public final class MessageCellHooks {

    public static final String ELEMENT_IN = "chat.message.in";
    public static final String ELEMENT_OUT = "chat.message.out";

    public static final String PART_TIME = "time";
    public static final String PROP_HIDDEN = "hidden";
    public static final String PROP_COLOR = "color";
    public static final String PROP_SECONDS = "seconds";

    private static final HashMap<String, FastDateFormat> secondsFormatters = new HashMap<>();

    private MessageCellHooks() {
    }

    public static String elementId(MessageObject mo) {
        return mo != null && mo.isOutOwner() ? ELEMENT_OUT : ELEMENT_IN;
    }

    public static ResolvedElement style(MessageObject mo) {
        if (mo == null) {
            return ResolvedElement.EMPTY;
        }
        return NextEngine.resolve(mo.currentAccount, mo.getDialogId(), elementId(mo));
    }

    /** Форматтер времени сообщения: стандартный или с секундами. */
    public static FastDateFormat dayFormatter(MessageObject mo) {
        FastDateFormat base = LocaleController.getInstance().getFormatterDay();
        if (!style(mo).getBool(PART_TIME, PROP_SECONDS, false)) {
            return base;
        }
        return withSeconds(base);
    }

    private static FastDateFormat withSeconds(FastDateFormat base) {
        String pattern = base.getPattern();
        String key = pattern + "|" + base.getLocale();
        synchronized (secondsFormatters) {
            FastDateFormat f = secondsFormatters.get(key);
            if (f == null) {
                int i = pattern.indexOf("mm");
                String withSec = i >= 0 && !pattern.contains("ss")
                        ? pattern.substring(0, i + 2) + ":ss" + pattern.substring(i + 2)
                        : pattern;
                f = FastDateFormat.getInstance(withSec, base.getLocale());
                secondsFormatters.put(key, f);
            }
            return f;
        }
    }

    /**
     * Скрыть время совсем (вместе с подписью автора в каналах — Telegram рисует их
     * одной строкой). В режиме редактирования время остаётся, но рисуется
     * блёкло и под спойлером, чтобы его можно было вернуть.
     */
    public static boolean hideTime(MessageObject mo) {
        return !EditMode.isActive() && isTimeHidden(mo);
    }

    public static boolean isTimeHidden(MessageObject mo) {
        return style(mo).getBool(PART_TIME, PROP_HIDDEN, false);
    }

    /** Цвет времени поверх цвета темы. */
    public static int timeColor(MessageObject mo, int themeColor) {
        ResolvedElement s = style(mo);
        Integer c = s.getColor(PART_TIME, PROP_COLOR);
        int color = c != null ? c : themeColor;
        if (EditMode.isActive() && s.getBool(PART_TIME, PROP_HIDDEN, false)) {
            int alpha = Math.round((color >>> 24) * 0.35f);
            color = (color & 0x00FFFFFF) | (alpha << 24);
        }
        return color;
    }
}
