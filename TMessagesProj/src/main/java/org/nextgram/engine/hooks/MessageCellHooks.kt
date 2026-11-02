package org.nextgram.engine.hooks

import org.nextgram.engine.NextEngine
import org.nextgram.engine.core.ResolvedElement
import org.nextgram.engine.edit.EditMode
import org.telegram.messenger.LocaleController
import org.telegram.messenger.MessageObject
import org.telegram.messenger.time.FastDateFormat
import kotlin.math.roundToInt

/**
 * Хуки для ChatMessageCell. В апстримном файле только однострочные вызовы
 * сюда — вся логика здесь, чтобы мердж новых версий Telegram был дешёвым.
 */
object MessageCellHooks {

    const val ELEMENT_IN = "chat.message.in"
    const val ELEMENT_OUT = "chat.message.out"

    const val PART_TIME = "time"
    const val PROP_HIDDEN = "hidden"
    const val PROP_COLOR = "color"
    const val PROP_SECONDS = "seconds"

    private val secondsFormatters = HashMap<String, FastDateFormat>()

    @JvmStatic
    fun elementId(mo: MessageObject?): String = if (mo != null && mo.isOutOwner) ELEMENT_OUT else ELEMENT_IN

    @JvmStatic
    fun style(mo: MessageObject?): ResolvedElement =
        if (mo == null) ResolvedElement.EMPTY else NextEngine.resolve(mo.currentAccount, mo.dialogId, elementId(mo))

    /** Форматтер времени сообщения: стандартный или с секундами. */
    @JvmStatic
    fun dayFormatter(mo: MessageObject?): FastDateFormat {
        val base = LocaleController.getInstance().formatterDay
        return if (style(mo).getBool(PART_TIME, PROP_SECONDS, false)) withSeconds(base) else base
    }

    private fun withSeconds(base: FastDateFormat): FastDateFormat {
        val pattern = base.pattern
        val key = "$pattern|${base.locale}"
        synchronized(secondsFormatters) {
            return secondsFormatters.getOrPut(key) {
                val i = pattern.indexOf("mm")
                val withSec = if (i >= 0 && !pattern.contains("ss")) pattern.substring(0, i + 2) + ":ss" + pattern.substring(i + 2) else pattern
                FastDateFormat.getInstance(withSec, base.locale)
            }
        }
    }

    /**
     * Скрыть время совсем (вместе с подписью автора в каналах — Telegram рисует их
     * одной строкой). В режиме редактирования время остаётся, но рисуется
     * блёкло и под спойлером, чтобы его можно было вернуть.
     */
    @JvmStatic
    fun hideTime(mo: MessageObject?): Boolean = !EditMode.isActive() && isTimeHidden(mo)

    @JvmStatic
    fun isTimeHidden(mo: MessageObject?): Boolean = style(mo).getBool(PART_TIME, PROP_HIDDEN, false)

    /** Цвет времени поверх цвета темы. */
    @JvmStatic
    fun timeColor(mo: MessageObject?, themeColor: Int): Int {
        val s = style(mo)
        var color = s.getColor(PART_TIME, PROP_COLOR) ?: themeColor
        if (EditMode.isActive() && s.getBool(PART_TIME, PROP_HIDDEN, false)) {
            val alpha = ((color ushr 24) * 0.35f).roundToInt()
            color = (color and 0x00FFFFFF) or (alpha shl 24)
        }
        return color
    }
}
