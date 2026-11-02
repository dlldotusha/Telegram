package org.nextgram.ghost;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.tgnet.tl.TL_account;
import org.telegram.tgnet.tl.TL_stories;

/**
 * Режим призрака: не отправлять на сервер то, по чему другие видят твою активность.
 *
 * Работает на уровне запросов: хук в ConnectionsManager.sendRequestInternal
 * спрашивает {@link #intercept}. Запрос либо не отправляется вовсе (колбэк не
 * вызывается — так интерфейс не получает «ответ» с чужими pts), либо
 * переписывается (статус «в сети» → «не в сети»).
 *
 * Важно: правила Telegram API (п. 1.4) прямо запрещают подмену статусов
 * прочтения. Со своим api_id это риск отзыва ключа. По умолчанию выключено.
 *
 * Чего режим не может: сервер сам помечает чат прочитанным, когда ты
 * отправляешь в него сообщение.
 */
public final class GhostMode {

    private static final String PREFS = "nextgram_ghost";

    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_READ = "hide_read";
    public static final String KEY_ONLINE = "hide_online";
    public static final String KEY_TYPING = "hide_typing";
    public static final String KEY_STORIES = "hide_stories";

    private static volatile boolean loaded;
    private static volatile boolean enabled, hideRead, hideOnline, hideTyping, hideStories;

    private GhostMode() {
    }

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static void ensureLoaded() {
        if (loaded || ApplicationLoader.applicationContext == null) {
            return;
        }
        SharedPreferences p = prefs();
        enabled = p.getBoolean(KEY_ENABLED, false);
        hideRead = p.getBoolean(KEY_READ, true);
        hideOnline = p.getBoolean(KEY_ONLINE, true);
        hideTyping = p.getBoolean(KEY_TYPING, true);
        hideStories = p.getBoolean(KEY_STORIES, true);
        loaded = true;
    }

    public static boolean get(String key) {
        ensureLoaded();
        switch (key) {
            case KEY_ENABLED:
                return enabled;
            case KEY_READ:
                return hideRead;
            case KEY_ONLINE:
                return hideOnline;
            case KEY_TYPING:
                return hideTyping;
            case KEY_STORIES:
                return hideStories;
        }
        return false;
    }

    public static void set(String key, boolean value) {
        ensureLoaded();
        prefs().edit().putBoolean(key, value).apply();
        switch (key) {
            case KEY_ENABLED:
                enabled = value;
                break;
            case KEY_READ:
                hideRead = value;
                break;
            case KEY_ONLINE:
                hideOnline = value;
                break;
            case KEY_TYPING:
                hideTyping = value;
                break;
            case KEY_STORIES:
                hideStories = value;
                break;
        }
        if (enabled && hideOnline && (KEY_ENABLED.equals(key) || KEY_ONLINE.equals(key)) && value) {
            goOfflineNow();
        }
    }

    /** Сразу показать «не в сети» во всех аккаунтах, не дожидаясь очередного пинга статуса. */
    private static void goOfflineNow() {
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            if (!UserConfig.getInstance(a).isClientActivated()) {
                continue;
            }
            TL_account.updateStatus req = new TL_account.updateStatus();
            req.offline = true;
            ConnectionsManager.getInstance(a).sendRequest(req, null);
        }
    }

    /**
     * Хук из ConnectionsManager. true — запрос не отправлять.
     * Вызывается на stageQueue для каждого запроса, поэтому только проверки полей.
     */
    public static boolean intercept(TLObject request) {
        if (!loaded) {
            ensureLoaded();
        }
        if (!enabled || request == null) {
            return false;
        }
        if (hideOnline && request instanceof TL_account.updateStatus) {
            ((TL_account.updateStatus) request).offline = true;
            return false;
        }
        if (hideRead && isReadReceipt(request)) {
            return true;
        }
        if (hideTyping && (request instanceof TLRPC.TL_messages_setTyping || request instanceof TLRPC.TL_messages_setEncryptedTyping)) {
            return true;
        }
        if (hideStories && (request instanceof TL_stories.TL_stories_readStories || request instanceof TL_stories.TL_stories_incrementStoryViews)) {
            return true;
        }
        return false;
    }

    private static boolean isReadReceipt(TLObject r) {
        return r instanceof TLRPC.TL_messages_readHistory
                || r instanceof TLRPC.TL_channels_readHistory
                || r instanceof TLRPC.TL_messages_readMessageContents
                || r instanceof TLRPC.TL_channels_readMessageContents
                || r instanceof TLRPC.TL_messages_readEncryptedHistory
                || r instanceof TLRPC.TL_messages_readDiscussion;
    }
}
