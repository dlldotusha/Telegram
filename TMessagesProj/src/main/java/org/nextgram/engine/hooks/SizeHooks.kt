package org.nextgram.engine.hooks

import android.content.Context
import org.nextgram.engine.NextEngine
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.SharedConfig
import org.telegram.ui.ActionBar.Theme
import kotlin.math.roundToInt

/**
 * Размеры текста. Telegram задаёт размеры шрифтов общими кистями в Theme
 * и DialogCell — хуки стоят ровно там и умножают базовый размер на коэффициент.
 *
 * Размер текста сообщений — это родная настройка Telegram («Размер текста»
 * в «Оформлении», SharedConfig.fontSize), здесь она просто доступна из редактора.
 *
 * Размеры общие для всего приложения (как и сами кисти Telegram).
 */
object SizeHooks {

    /** Элемент конфига, в котором хранятся коэффициенты: часть = id размера, свойство = "scale". */
    const val ELEMENT = "sizes"
    const val PROP = "scale"

    const val CHAT_TEXT = "chat.text"
    const val CHAT_TIME = "chat.time"
    const val CHAT_NAME = "chat.name"
    const val CHAT_REPLY = "chat.reply"
    const val CHAT_SERVICE = "chat.service"
    const val DIALOG_NAME = "dialogs.name"
    const val DIALOG_MESSAGE = "dialogs.message"
    const val DIALOG_TIME = "dialogs.time"
    const val DIALOG_COUNTER = "dialogs.counter"

    const val MIN = 0.7f
    const val MAX = 1.6f

    @JvmStatic
    fun scale(id: String): Float {
        if (id == CHAT_TEXT) return SharedConfig.fontSize / 16f
        return NextEngine.resolve(0, 0L, ELEMENT).getFloat(id, PROP, 1f).coerceIn(MIN, MAX)
    }

    /** Записать коэффициент и применить к кистям. */
    @JvmStatic
    fun setScale(id: String, value: Float?) {
        if (id == CHAT_TEXT) {
            setMessageFontSize(((value ?: 1f) * 16f).roundToInt().coerceIn(12, 30))
        } else {
            NextEngine.edit { it.set(org.nextgram.engine.core.Scopes.GLOBAL, ELEMENT, id, PROP, value?.toDouble()) }
        }
        reapply()
    }

    /** То же, что делает ползунок «Размер текста» в настройках Telegram. */
    private fun setMessageFontSize(size: Int) {
        if (size == SharedConfig.fontSize) return
        SharedConfig.fontSize = size
        SharedConfig.fontSizeIsDefault = false
        ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Context.MODE_PRIVATE)
            .edit().putInt("fons_size", size).apply()
    }

    /** Пересчитать размеры кистей: Telegram сам вызовет хуки ниже. */
    private fun reapply() {
        try {
            Theme.createCommonMessageResources()
            Theme.createChatResources(ApplicationLoader.applicationContext, true)
        } catch (e: Throwable) {
            org.telegram.messenger.FileLog.e(e)
        }
    }

    private fun dp(v: Float) = AndroidUtilities.dp(v).toFloat()

    /** Хук в конце Theme.createCommonMessageResources/createChatResources. */
    @JvmStatic
    fun applyChatSizes() {
        if (!NextEngine.isReady) return
        val smaller = (2 * SharedConfig.fontSize + 10) / 3f
        val name = scale(CHAT_NAME)
        val reply = scale(CHAT_REPLY)
        Theme.chat_namePaint?.textSize = dp(smaller * name)
        Theme.chat_forwardNamePaint?.textSize = dp(smaller * name)
        Theme.chat_replyNamePaint?.textSize = dp(smaller * reply)
        Theme.chat_replyTextPaint?.textSize = dp(smaller * reply)
        Theme.chat_timePaint?.textSize = dp(12f * scale(CHAT_TIME))
        val service = scale(CHAT_SERVICE)
        val base = maxOf(16, SharedConfig.fontSize).toFloat()
        Theme.chat_actionTextPaint?.textSize = dp((base - 2) * service)
        Theme.chat_actionTextPaint2?.textSize = dp((base - 2) * service)
        Theme.chat_actionTextPaint3?.textSize = dp((base - 3) * service)
    }

    /** Хук в DialogCell.buildLayout — там Telegram каждый раз выставляет размеры кистей списка чатов. */
    @JvmStatic
    fun applyDialogSizes() {
        if (!NextEngine.isReady) return
        val name = scale(DIALOG_NAME)
        val message = scale(DIALOG_MESSAGE)
        Theme.dialogs_namePaint?.let { p ->
            p.getOrNull(0)?.textSize = dp(17f * name)
            p.getOrNull(1)?.textSize = dp(16f * name)
        }
        Theme.dialogs_nameEncryptedPaint?.let { p ->
            p.getOrNull(0)?.textSize = dp(17f * name)
            p.getOrNull(1)?.textSize = dp(16f * name)
        }
        Theme.dialogs_messagePaint?.let { p ->
            p.getOrNull(0)?.textSize = dp(16f * message)
            p.getOrNull(1)?.textSize = dp(15f * message)
        }
        Theme.dialogs_messagePrintingPaint?.let { p ->
            p.getOrNull(0)?.textSize = dp(16f * message)
            p.getOrNull(1)?.textSize = dp(15f * message)
        }
        Theme.dialogs_messageNamePaint?.textSize = dp(14f * message)
        val time = scale(DIALOG_TIME)
        Theme.dialogs_timePaint?.textSize = dp(12f * time)
        Theme.dialogs_timePaintBold?.textSize = dp(12f * time)
        Theme.dialogs_timePaintBoldAccent?.textSize = dp(12f * time)
        val counter = scale(DIALOG_COUNTER)
        Theme.dialogs_countTextPaint?.textSize = dp(12f * counter)
        Theme.dialogs_countTextPaint2?.textSize = dp(13f * counter)
    }
}
