package org.nextgram.profiles;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Профили «для своих». Сервер — backend/worker. Адрес сервера и username бота
 * задаются при сборке (NG_BACKEND_URL, NG_BOT_USERNAME); без них функция скрыта.
 *
 * Чужие профили может запрашивать только подключённый пользователь NextGram —
 * так список пользователей клиента не утекает наружу.
 */
public final class NextProfiles {

    public interface Callback<T> {
        void done(T result, String error);
    }

    private static final String PREFS = "nextgram_profiles";
    private static final long CACHE_TTL_MS = 10 * 60_000;
    private static final int POLL_ATTEMPTS = 40;
    private static final long POLL_INTERVAL_MS = 1500;

    private static final class Entry {
        final NgProfile profile; // null — у пользователя нет профиля NextGram
        final long time;

        Entry(NgProfile profile, long time) {
            this.profile = profile;
            this.time = time;
        }
    }

    private static final HashMap<Long, Entry> cache = new HashMap<>();
    private static final HashMap<Long, ArrayList<Callback<NgProfile>>> pending = new HashMap<>();

    private NextProfiles() {
    }

    public static boolean isConfigured() {
        return !TextUtils.isEmpty(BuildConfig.NG_BACKEND_URL) && !TextUtils.isEmpty(BuildConfig.NG_BOT_USERNAME);
    }

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static long selfId(int account) {
        return UserConfig.getInstance(account).getClientUserId();
    }

    public static boolean isConnected(int account) {
        return isConfigured() && token(account) != null;
    }

    private static String token(int account) {
        return prefs().getString("token_" + selfId(account), null);
    }

    /** Свой профиль (локальная копия того, что лежит на сервере). */
    public static NgProfile own(int account) {
        String raw = prefs().getString("own_" + selfId(account), null);
        if (raw == null) {
            return new NgProfile();
        }
        try {
            NgProfile p = NgProfile.fromJson(new JSONObject(raw));
            return p == null ? new NgProfile() : p;
        } catch (Exception e) {
            return new NgProfile();
        }
    }

    // ------------------------------------------------------------------ подключение

    /**
     * Подтверждение Telegram ID: клиент от имени пользователя запускает бота
     * с одноразовым параметром, бот видит настоящий from.id и выдаёт токен.
     */
    public static void connect(int account, Callback<Boolean> cb) {
        if (!isConfigured()) {
            cb.done(false, "not_configured");
            return;
        }
        Utilities.globalQueue.postRunnable(() -> {
            try {
                JSONObject start = http("POST", "/v1/auth/start", null, null);
                final String nonce = start.getString("nonce");
                final String bot = start.optString("bot", BuildConfig.NG_BOT_USERNAME);
                AndroidUtilities.runOnUIThread(() -> startBot(account, bot, nonce, cb));
            } catch (Exception e) {
                FileLog.e(e);
                AndroidUtilities.runOnUIThread(() -> cb.done(false, e.getMessage()));
            }
        });
    }

    private static void startBot(int account, String botUsername, String nonce, Callback<Boolean> cb) {
        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
        req.username = botUsername;
        ConnectionsManager.getInstance(account).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (!(response instanceof TLRPC.TL_contacts_resolvedPeer)) {
                cb.done(false, error != null ? error.text : "bot_not_found");
                return;
            }
            TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
            MessagesController mc = MessagesController.getInstance(account);
            mc.putUsers(res.users, false);
            TLRPC.User bot = null;
            for (TLRPC.User u : res.users) {
                if (res.peer != null && u.id == res.peer.user_id) {
                    bot = u;
                }
            }
            if (bot == null) {
                cb.done(false, "bot_not_found");
                return;
            }
            TLRPC.TL_messages_startBot sb = new TLRPC.TL_messages_startBot();
            sb.bot = mc.getInputUser(bot);
            sb.peer = mc.getInputPeer(bot.id);
            sb.start_param = "ng_" + nonce;
            sb.random_id = Utilities.random.nextLong();
            ConnectionsManager.getInstance(account).sendRequest(sb, (r, e) -> {
                if (r instanceof TLRPC.Updates) {
                    mc.processUpdates((TLRPC.Updates) r, false);
                }
                if (e != null) {
                    AndroidUtilities.runOnUIThread(() -> cb.done(false, e.text));
                    return;
                }
                Utilities.globalQueue.postRunnable(() -> poll(account, nonce, 0, cb));
            });
        }));
    }

    private static void poll(int account, String nonce, int attempt, Callback<Boolean> cb) {
        try {
            JSONObject r = http("GET", "/v1/auth/poll?nonce=" + nonce, null, null);
            if ("ok".equals(r.optString("status"))) {
                long uid = Long.parseLong(r.getString("user_id"));
                if (uid != selfId(account)) {
                    // Бота запустили из другого аккаунта — так быть не должно.
                    AndroidUtilities.runOnUIThread(() -> cb.done(false, "wrong_account"));
                    return;
                }
                prefs().edit().putString("token_" + uid, r.getString("token")).apply();
                AndroidUtilities.runOnUIThread(() -> cb.done(true, null));
                return;
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        if (attempt + 1 >= POLL_ATTEMPTS) {
            AndroidUtilities.runOnUIThread(() -> cb.done(false, "timeout"));
            return;
        }
        Utilities.globalQueue.postRunnable(() -> poll(account, nonce, attempt + 1, cb), POLL_INTERVAL_MS);
    }

    /** Отключиться: профиль и токен удаляются на сервере. */
    public static void disconnect(int account, Callback<Boolean> cb) {
        final String token = token(account);
        final long self = selfId(account);
        Utilities.globalQueue.postRunnable(() -> {
            try {
                if (token != null) {
                    http("DELETE", "/v1/me", null, token);
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
            prefs().edit().remove("token_" + self).remove("own_" + self).apply();
            synchronized (cache) {
                cache.remove(self);
            }
            AndroidUtilities.runOnUIThread(() -> cb.done(true, null));
        });
    }

    /** Сохранить свой профиль на сервере. */
    public static void saveOwn(int account, NgProfile profile, Callback<Boolean> cb) {
        final String token = token(account);
        final long self = selfId(account);
        prefs().edit().putString("own_" + self, profile.toJson().toString()).apply();
        synchronized (cache) {
            cache.put(self, new Entry(profile.isEmpty() ? null : profile.copy(), System.currentTimeMillis()));
        }
        if (token == null) {
            cb.done(false, "not_connected");
            return;
        }
        Utilities.globalQueue.postRunnable(() -> {
            try {
                http("PUT", "/v1/me", profile.toJson().toString(), token);
                AndroidUtilities.runOnUIThread(() -> cb.done(true, null));
            } catch (Exception e) {
                FileLog.e(e);
                AndroidUtilities.runOnUIThread(() -> cb.done(false, e.getMessage()));
            }
        });
    }

    // ------------------------------------------------------------------ чужие профили

    /** Профиль пользователя (из кэша или с сервера). Колбэк на UI-потоке; null — профиля нет. */
    public static void get(int account, long userId, Callback<NgProfile> cb) {
        if (userId == selfId(account)) {
            NgProfile own = own(account);
            cb.done(isConnected(account) && !own.isEmpty() ? own : null, null);
            return;
        }
        final String token = isConfigured() ? token(account) : null;
        if (token == null) {
            cb.done(null, "not_connected");
            return;
        }
        synchronized (cache) {
            Entry e = cache.get(userId);
            if (e != null && System.currentTimeMillis() - e.time < CACHE_TTL_MS) {
                cb.done(e.profile, null);
                return;
            }
            ArrayList<Callback<NgProfile>> waiting = pending.get(userId);
            if (waiting != null) {
                waiting.add(cb);
                return;
            }
            waiting = new ArrayList<>();
            waiting.add(cb);
            pending.put(userId, waiting);
        }
        Utilities.globalQueue.postRunnable(() -> {
            NgProfile result = null;
            boolean ok = false;
            try {
                JSONObject body = new JSONObject();
                body.put("ids", new JSONArray().put(String.valueOf(userId)));
                JSONObject r = http("POST", "/v1/profiles", body.toString(), token);
                JSONObject all = r.optJSONObject("profiles");
                result = all == null ? null : NgProfile.fromJson(all.optJSONObject(String.valueOf(userId)));
                if (result != null && result.isEmpty()) {
                    result = null;
                }
                ok = true;
            } catch (Exception e) {
                FileLog.e(e);
            }
            final NgProfile profile = result;
            ArrayList<Callback<NgProfile>> callbacks;
            synchronized (cache) {
                if (ok) {
                    cache.put(userId, new Entry(profile, System.currentTimeMillis()));
                }
                callbacks = pending.remove(userId);
            }
            if (callbacks != null) {
                AndroidUtilities.runOnUIThread(() -> {
                    for (Callback<NgProfile> c : callbacks) {
                        c.done(profile, null);
                    }
                });
            }
        });
    }

    // ------------------------------------------------------------------ http

    private static JSONObject http(String method, String path, String body, String token) throws Exception {
        String base = BuildConfig.NG_BACKEND_URL;
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        HttpURLConnection c = (HttpURLConnection) new URL(base + path).openConnection();
        try {
            c.setRequestMethod(method);
            c.setConnectTimeout(10_000);
            c.setReadTimeout(15_000);
            c.setRequestProperty("Accept", "application/json");
            if (token != null) {
                c.setRequestProperty("Authorization", "Bearer " + token);
            }
            if (body != null) {
                c.setDoOutput(true);
                c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                try (OutputStream out = c.getOutputStream()) {
                    out.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }
            int code = c.getResponseCode();
            InputStream in = code >= 400 ? c.getErrorStream() : c.getInputStream();
            String text = in == null ? "{}" : readAll(in);
            if (code >= 400 && code != 410) {
                throw new IllegalStateException("HTTP " + code + ": " + text);
            }
            return new JSONObject(text);
        } finally {
            c.disconnect();
        }
    }

    private static String readAll(InputStream in) throws Exception {
        try (InputStream s = in) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int r;
            while ((r = s.read(buf)) > 0) {
                out.write(buf, 0, r);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
