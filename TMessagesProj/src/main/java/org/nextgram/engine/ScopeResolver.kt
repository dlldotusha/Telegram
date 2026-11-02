package org.nextgram.engine

import org.nextgram.engine.core.Scopes
import org.telegram.messenger.ChatObject
import org.telegram.messenger.ContactsController
import org.telegram.messenger.DialogObject
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig

/** Определяет, на какие уровни конфига распространяется конкретный чат. */
object ScopeResolver {

    @JvmStatic
    fun chain(account: Int, dialogId: Long): List<String> {
        if (dialogId == 0L) return Scopes.globalChain()
        val userDialogId = userDialogId(account, dialogId)
        val chatScopes = ArrayList<String>(2)
        // Секретный чат наследует настройки обычного чата с этим человеком.
        if (userDialogId != dialogId) chatScopes.add(Scopes.chat(userDialogId))
        chatScopes.add(Scopes.chat(dialogId))
        val category = category(account, userDialogId)
        return Scopes.chain(category, folders(account, dialogId, userDialogId, category), chatScopes)
    }

    /** Для секретного чата — id обычного чата с этим пользователем, иначе сам dialogId. */
    @JvmStatic
    fun userDialogId(account: Int, dialogId: Long): Long {
        if (!DialogObject.isEncryptedDialog(dialogId)) return dialogId
        val ec = MessagesController.getInstance(account).getEncryptedChat(DialogObject.getEncryptedChatId(dialogId))
        return ec?.user_id ?: dialogId
    }

    @JvmStatic
    fun category(account: Int, dialogId: Long): Scopes.Category? {
        val mc = MessagesController.getInstance(account)
        return when {
            DialogObject.isUserDialog(dialogId) -> {
                val user = mc.getUser(dialogId) ?: return null
                when {
                    user.bot -> Scopes.Category.BOTS
                    user.self || user.contact || ContactsController.getInstance(account).isContact(dialogId) -> Scopes.Category.CONTACTS
                    else -> Scopes.Category.NON_CONTACTS
                }
            }
            DialogObject.isChatDialog(dialogId) -> {
                val chat = mc.getChat(-dialogId) ?: return null
                if (ChatObject.isChannel(chat) && !chat.megagroup) Scopes.Category.CHANNELS else Scopes.Category.GROUPS
            }
            else -> null
        }
    }

    /** Ключи папок, в которые входит чат, в порядке списка папок (первая — самая приоритетная). */
    @JvmStatic
    fun folders(account: Int, dialogId: Long, userDialogId: Long, category: Scopes.Category?): List<String> {
        val self = UserConfig.getInstance(account).getClientUserId()
        return folderObjects(account, dialogId, userDialogId, category).map { Scopes.folder(self, it.id) }
    }

    @JvmStatic
    fun folderObjects(account: Int, dialogId: Long, userDialogId: Long, category: Scopes.Category?): List<MessagesController.DialogFilter> {
        val mc = MessagesController.getInstance(account)
        return mc.dialogFilters.filter { f ->
            f != null && !f.isDefault &&
                (inFolder(mc, f, dialogId, category) || userDialogId != dialogId && inFolder(mc, f, userDialogId, category))
        }
    }

    /**
     * Членство в папке без учёта «исключить прочитанные/без звука»: иначе стиль
     * чата мигал бы каждый раз, когда чат прочитан или замьючен.
     */
    private fun inFolder(mc: MessagesController, f: MessagesController.DialogFilter, dialogId: Long, category: Scopes.Category?): Boolean {
        if (f.neverShow.contains(dialogId)) return false
        if (f.alwaysShow.contains(dialogId)) return true
        val d = mc.dialogs_dict.get(dialogId)
        if (d != null && d.folder_id != 0 && f.flags and MessagesController.DIALOG_FILTER_FLAG_EXCLUDE_ARCHIVED != 0) return false
        val flag = when (category ?: return false) {
            Scopes.Category.CONTACTS -> MessagesController.DIALOG_FILTER_FLAG_CONTACTS
            Scopes.Category.NON_CONTACTS -> MessagesController.DIALOG_FILTER_FLAG_NON_CONTACTS
            Scopes.Category.GROUPS -> MessagesController.DIALOG_FILTER_FLAG_GROUPS
            Scopes.Category.CHANNELS -> MessagesController.DIALOG_FILTER_FLAG_CHANNELS
            Scopes.Category.BOTS -> MessagesController.DIALOG_FILTER_FLAG_BOTS
        }
        return f.flags and flag != 0
    }
}
