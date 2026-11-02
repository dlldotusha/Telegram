package org.nextgram.engine.core

/**
 * Уровни, на которых можно сохранять правки. От общего к частному:
 *
 *   global → type:<категория> → folder:<аккаунт>:<папка> → chat:<id>
 *
 * Категории совпадают с категориями папок Telegram (контакты, не контакты,
 * группы, каналы, боты). У папок id свои в каждом аккаунте, поэтому в ключе
 * есть id пользователя аккаунта. Id чатов одинаковые во всех аккаунтах.
 */
object Scopes {

    const val GLOBAL = "global"
    private const val TYPE_PREFIX = "type:"
    private const val FOLDER_PREFIX = "folder:"
    private const val CHAT_PREFIX = "chat:"

    enum class Kind { GLOBAL, CATEGORY, FOLDER, CHAT, UNKNOWN }

    enum class Category(val id: String) {
        CONTACTS("contacts"),
        NON_CONTACTS("non_contacts"),
        GROUPS("groups"),
        CHANNELS("channels"),
        BOTS("bots");

        val scope: String get() = TYPE_PREFIX + id

        companion object {
            fun byId(id: String?): Category? = entries.firstOrNull { it.id == id }
        }
    }

    fun folder(selfUserId: Long, filterId: Int) = "$FOLDER_PREFIX$selfUserId:$filterId"

    fun chat(dialogId: Long) = "$CHAT_PREFIX$dialogId"

    fun kindOf(scope: String): Kind = when {
        scope == GLOBAL -> Kind.GLOBAL
        scope.startsWith(TYPE_PREFIX) -> Kind.CATEGORY
        scope.startsWith(FOLDER_PREFIX) -> Kind.FOLDER
        scope.startsWith(CHAT_PREFIX) -> Kind.CHAT
        else -> Kind.UNKNOWN
    }

    /**
     * Цепочка уровней для чата, от общего к частному (последний побеждает).
     *
     * @param category категория чата или null, если неизвестна
     * @param foldersHighFirst папки чата в порядке списка папок пользователя:
     *   первая — самая приоритетная
     * @param chatScopesGeneralFirst уровни конкретного чата от общего к частному
     *   (для секретного чата: сначала чат с пользователем, потом сам секретный чат)
     */
    fun chain(category: Category?, foldersHighFirst: List<String>?, chatScopesGeneralFirst: List<String>?): List<String> =
        buildList {
            add(GLOBAL)
            category?.let { add(it.scope) }
            foldersHighFirst?.let { addAll(it.asReversed()) }
            chatScopesGeneralFirst?.let { addAll(it) }
        }

    /** Цепочка для элементов вне чатов (настройки, список чатов и т.д.). */
    fun globalChain(): List<String> = listOf(GLOBAL)
}
