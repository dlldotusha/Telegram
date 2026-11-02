package org.nextgram.engine.edit;

import org.nextgram.engine.EngineStrings;
import org.nextgram.engine.ScopeResolver;
import org.nextgram.engine.core.Scopes;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.List;

/** Уровень, на который можно сохранить правку, с подписью для интерфейса. */
public final class ScopeOption {

    public final Scopes.Kind kind;
    public final String scope;
    public final String label;

    ScopeOption(Scopes.Kind kind, String scope, String label) {
        this.kind = kind;
        this.scope = scope;
        this.label = label;
    }

    /** Варианты для элемента: Везде → категория → папки чата → этот чат. */
    public static List<ScopeOption> forTarget(EditTarget t) {
        ArrayList<ScopeOption> out = new ArrayList<>();
        out.add(new ScopeOption(Scopes.Kind.GLOBAL, Scopes.GLOBAL, EngineStrings.get("scope_global")));
        if (t == null || t.dialogId == 0) {
            return out;
        }
        long userDialogId = t.dialogId;
        if (DialogObject.isEncryptedDialog(t.dialogId)) {
            TLRPC.EncryptedChat ec = MessagesController.getInstance(t.account).getEncryptedChat(DialogObject.getEncryptedChatId(t.dialogId));
            if (ec != null) {
                userDialogId = ec.user_id;
            }
        }
        Scopes.Category category = ScopeResolver.category(t.account, userDialogId);
        if (category != null) {
            out.add(new ScopeOption(Scopes.Kind.CATEGORY, category.scope(), EngineStrings.get("cat_" + category.id)));
        }
        long self = UserConfig.getInstance(t.account).getClientUserId();
        for (MessagesController.DialogFilter f : ScopeResolver.folderObjects(t.account, t.dialogId, userDialogId, category)) {
            out.add(new ScopeOption(Scopes.Kind.FOLDER, Scopes.folder(self, f.id), EngineStrings.format("scope_folder", f.name)));
        }
        out.add(new ScopeOption(Scopes.Kind.CHAT, Scopes.chat(t.dialogId), EngineStrings.get("scope_chat")));
        return out;
    }

    /** Подпись для ключа уровня (для строки «из: …»). */
    public static String labelFor(String scope, List<ScopeOption> known) {
        if (scope == null) {
            return EngineStrings.get("not_set");
        }
        for (ScopeOption o : known) {
            if (o.scope.equals(scope)) {
                return o.label;
            }
        }
        switch (Scopes.kindOf(scope)) {
            case GLOBAL:
                return EngineStrings.get("scope_global");
            case CHAT:
                return EngineStrings.get("scope_chat");
            default:
                return scope;
        }
    }
}
