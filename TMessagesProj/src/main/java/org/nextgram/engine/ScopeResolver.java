package org.nextgram.engine;

import org.nextgram.engine.core.Scopes;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.List;

/** Определяет, на какие уровни конфига распространяется конкретный чат. */
public final class ScopeResolver {

    private ScopeResolver() {
    }

    public static List<String> chain(int account, long dialogId) {
        if (dialogId == 0) {
            return Scopes.globalChain();
        }
        long userDialogId = dialogId;
        ArrayList<String> chatScopes = new ArrayList<>(2);
        MessagesController mc = MessagesController.getInstance(account);
        if (DialogObject.isEncryptedDialog(dialogId)) {
            // Секретный чат наследует настройки обычного чата с этим человеком.
            TLRPC.EncryptedChat ec = mc.getEncryptedChat(DialogObject.getEncryptedChatId(dialogId));
            if (ec != null) {
                userDialogId = ec.user_id;
                chatScopes.add(Scopes.chat(userDialogId));
            }
        }
        chatScopes.add(Scopes.chat(dialogId));

        Scopes.Category category = category(account, userDialogId);
        return Scopes.chain(category, folders(account, dialogId, userDialogId, category), chatScopes);
    }

    public static Scopes.Category category(int account, long dialogId) {
        MessagesController mc = MessagesController.getInstance(account);
        if (DialogObject.isUserDialog(dialogId)) {
            TLRPC.User user = mc.getUser(dialogId);
            if (user == null) {
                return null;
            }
            if (user.bot) {
                return Scopes.Category.BOTS;
            }
            boolean contact = user.self || user.contact || ContactsController.getInstance(account).isContact(dialogId);
            return contact ? Scopes.Category.CONTACTS : Scopes.Category.NON_CONTACTS;
        } else if (DialogObject.isChatDialog(dialogId)) {
            TLRPC.Chat chat = mc.getChat(-dialogId);
            if (chat == null) {
                return null;
            }
            return ChatObject.isChannel(chat) && !chat.megagroup ? Scopes.Category.CHANNELS : Scopes.Category.GROUPS;
        }
        return null;
    }

    /** Ключи папок, в которые входит чат, в порядке списка папок (первая — самая приоритетная). */
    public static List<String> folders(int account, long dialogId, long userDialogId, Scopes.Category category) {
        ArrayList<String> out = new ArrayList<>();
        for (MessagesController.DialogFilter f : folderObjects(account, dialogId, userDialogId, category)) {
            out.add(Scopes.folder(UserConfig.getInstance(account).getClientUserId(), f.id));
        }
        return out;
    }

    public static List<MessagesController.DialogFilter> folderObjects(int account, long dialogId, long userDialogId, Scopes.Category category) {
        ArrayList<MessagesController.DialogFilter> out = new ArrayList<>();
        MessagesController mc = MessagesController.getInstance(account);
        ArrayList<MessagesController.DialogFilter> filters = mc.dialogFilters;
        for (int i = 0, n = filters.size(); i < n; i++) {
            MessagesController.DialogFilter f = filters.get(i);
            if (f == null || f.isDefault()) {
                continue;
            }
            if (inFolder(mc, f, dialogId, category) || userDialogId != dialogId && inFolder(mc, f, userDialogId, category)) {
                out.add(f);
            }
        }
        return out;
    }

    /**
     * Членство в папке без учёта «исключить прочитанные/без звука»: иначе стиль
     * чата мигал бы каждый раз, когда чат прочитан или замьючен.
     */
    private static boolean inFolder(MessagesController mc, MessagesController.DialogFilter f, long dialogId, Scopes.Category category) {
        if (f.neverShow.contains(dialogId)) {
            return false;
        }
        if (f.alwaysShow.contains(dialogId)) {
            return true;
        }
        TLRPC.Dialog d = mc.dialogs_dict.get(dialogId);
        if (d != null && d.folder_id != 0 && (f.flags & MessagesController.DIALOG_FILTER_FLAG_EXCLUDE_ARCHIVED) != 0) {
            return false;
        }
        if (category == null) {
            return false;
        }
        int flag;
        switch (category) {
            case CONTACTS:
                flag = MessagesController.DIALOG_FILTER_FLAG_CONTACTS;
                break;
            case NON_CONTACTS:
                flag = MessagesController.DIALOG_FILTER_FLAG_NON_CONTACTS;
                break;
            case GROUPS:
                flag = MessagesController.DIALOG_FILTER_FLAG_GROUPS;
                break;
            case CHANNELS:
                flag = MessagesController.DIALOG_FILTER_FLAG_CHANNELS;
                break;
            case BOTS:
                flag = MessagesController.DIALOG_FILTER_FLAG_BOTS;
                break;
            default:
                return false;
        }
        return (f.flags & flag) != 0;
    }
}
