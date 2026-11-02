package org.nextgram.engine.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Уровни, на которых можно сохранять правки. От общего к частному:
 *
 *   global → type:&lt;категория&gt; → folder:&lt;аккаунт&gt;:&lt;папка&gt; → chat:&lt;id&gt;
 *
 * Категории совпадают с категориями папок Telegram (контакты, не контакты,
 * группы, каналы, боты). У папок id свои в каждом аккаунте, поэтому в ключе
 * есть id пользователя аккаунта. Id чатов одинаковые во всех аккаунтах.
 */
public final class Scopes {

    public static final String GLOBAL = "global";
    static final String TYPE_PREFIX = "type:";
    static final String FOLDER_PREFIX = "folder:";
    static final String CHAT_PREFIX = "chat:";

    public enum Kind { GLOBAL, CATEGORY, FOLDER, CHAT, UNKNOWN }

    public enum Category {
        CONTACTS("contacts"),
        NON_CONTACTS("non_contacts"),
        GROUPS("groups"),
        CHANNELS("channels"),
        BOTS("bots");

        public final String id;

        Category(String id) {
            this.id = id;
        }

        public String scope() {
            return TYPE_PREFIX + id;
        }

        public static Category byId(String id) {
            for (Category c : values()) {
                if (c.id.equals(id)) {
                    return c;
                }
            }
            return null;
        }
    }

    private Scopes() {
    }

    public static String folder(long selfUserId, int filterId) {
        return FOLDER_PREFIX + selfUserId + ":" + filterId;
    }

    public static String chat(long dialogId) {
        return CHAT_PREFIX + dialogId;
    }

    public static Kind kindOf(String scope) {
        if (GLOBAL.equals(scope)) {
            return Kind.GLOBAL;
        } else if (scope.startsWith(TYPE_PREFIX)) {
            return Kind.CATEGORY;
        } else if (scope.startsWith(FOLDER_PREFIX)) {
            return Kind.FOLDER;
        } else if (scope.startsWith(CHAT_PREFIX)) {
            return Kind.CHAT;
        }
        return Kind.UNKNOWN;
    }

    /**
     * Цепочка уровней для чата, от общего к частному (последний побеждает).
     *
     * @param category             категория чата или null, если неизвестна
     * @param foldersHighFirst     папки, в которые входит чат, в порядке списка папок
     *                             пользователя: первая папка — самая приоритетная
     * @param chatScopesGeneralFirst уровни конкретного чата, от общего к частному
     *                             (для секретного чата: сначала чат с пользователем,
     *                             потом сам секретный чат)
     */
    public static List<String> chain(Category category, List<String> foldersHighFirst, List<String> chatScopesGeneralFirst) {
        ArrayList<String> chain = new ArrayList<>();
        chain.add(GLOBAL);
        if (category != null) {
            chain.add(category.scope());
        }
        if (foldersHighFirst != null) {
            for (int i = foldersHighFirst.size() - 1; i >= 0; i--) {
                chain.add(foldersHighFirst.get(i));
            }
        }
        if (chatScopesGeneralFirst != null) {
            chain.addAll(chatScopesGeneralFirst);
        }
        return chain;
    }

    /** Цепочка для элементов вне чатов (настройки, список чатов и т.д.). */
    public static List<String> globalChain() {
        ArrayList<String> chain = new ArrayList<>();
        chain.add(GLOBAL);
        return chain;
    }
}
