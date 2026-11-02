package org.nextgram.engine.edit

import org.nextgram.engine.EngineStrings
import org.nextgram.engine.ScopeResolver
import org.nextgram.engine.core.Scopes
import org.telegram.messenger.UserConfig

/** Уровень, на который можно сохранить правку, с подписью для интерфейса. */
class ScopeOption internal constructor(val kind: Scopes.Kind, val scope: String, val label: String) {

    companion object {

        /** Варианты для элемента: Везде → категория → папки чата → этот чат. */
        @JvmStatic
        fun forTarget(t: EditTarget?): List<ScopeOption> {
            val out = arrayListOf(ScopeOption(Scopes.Kind.GLOBAL, Scopes.GLOBAL, EngineStrings.get("scope_global")))
            if (t == null || t.dialogId == 0L) return out
            val userDialogId = ScopeResolver.userDialogId(t.account, t.dialogId)
            val category = ScopeResolver.category(t.account, userDialogId)
            if (category != null) {
                out += ScopeOption(Scopes.Kind.CATEGORY, category.scope, EngineStrings.get("cat_" + category.id))
            }
            val self = UserConfig.getInstance(t.account).getClientUserId()
            for (f in ScopeResolver.folderObjects(t.account, t.dialogId, userDialogId, category)) {
                out += ScopeOption(Scopes.Kind.FOLDER, Scopes.folder(self, f.id), EngineStrings.format("scope_folder", f.name))
            }
            out += ScopeOption(Scopes.Kind.CHAT, Scopes.chat(t.dialogId), EngineStrings.get("scope_chat"))
            return out
        }

        /** Подпись для ключа уровня (для строки «из: …»). */
        @JvmStatic
        fun labelFor(scope: String?, known: List<ScopeOption>): String {
            if (scope == null) return EngineStrings.get("not_set")
            known.firstOrNull { it.scope == scope }?.let { return it.label }
            return when (Scopes.kindOf(scope)) {
                Scopes.Kind.GLOBAL -> EngineStrings.get("scope_global")
                Scopes.Kind.CHAT -> EngineStrings.get("scope_chat")
                else -> scope
            }
        }
    }
}
