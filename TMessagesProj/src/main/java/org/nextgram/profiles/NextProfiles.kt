package org.nextgram.profiles

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.BuildConfig
import org.telegram.messenger.FileLog
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.messenger.Utilities
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.TLRPC
import java.net.HttpURLConnection
import java.net.URL

/**
 * Профили «для своих». Сервер — backend/worker. Адрес сервера и username бота
 * задаются при сборке (NG_BACKEND_URL, NG_BOT_USERNAME); без них функция скрыта.
 *
 * Чужие профили может запрашивать только подключённый пользователь NextGram —
 * так список пользователей клиента не утекает наружу.
 */
object NextProfiles {

    /** Колбэк всегда на UI-потоке. error == null — успех. */
    fun interface Callback<T> {
        fun done(result: T, error: String?)
    }

    private const val PREFS = "nextgram_profiles"
    private const val CACHE_TTL_MS = 10 * 60_000L
    private const val POLL_ATTEMPTS = 40
    private const val POLL_INTERVAL_MS = 1500L

    /** profile == null — у пользователя нет профиля NextGram. */
    private class Entry(val profile: NgProfile?, val time: Long)

    private val cache = HashMap<Long, Entry>()
    private val pending = HashMap<Long, MutableList<Callback<NgProfile?>>>()

    @JvmStatic
    val isConfigured: Boolean
        get() = BuildConfig.NG_BACKEND_URL.isNotEmpty() && BuildConfig.NG_BOT_USERNAME.isNotEmpty()

    private fun prefs(): SharedPreferences =
        ApplicationLoader.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun selfId(account: Int) = UserConfig.getInstance(account).getClientUserId()

    private fun token(account: Int): String? = prefs().getString("token_${selfId(account)}", null)

    @JvmStatic
    fun isConnected(account: Int) = isConfigured && token(account) != null

    /** Свой профиль (локальная копия того, что лежит на сервере). */
    @JvmStatic
    fun own(account: Int): NgProfile {
        val raw = prefs().getString("own_${selfId(account)}", null) ?: return NgProfile()
        return runCatching { NgProfile.fromJson(JSONObject(raw)) }.getOrNull() ?: NgProfile()
    }

    // ------------------------------------------------------------------ подключение

    /**
     * Подтверждение Telegram ID: клиент от имени пользователя запускает бота
     * с одноразовым параметром, бот видит настоящий from.id и выдаёт токен.
     */
    @JvmStatic
    fun connect(account: Int, cb: Callback<Boolean>) {
        if (!isConfigured) {
            cb.done(false, "not_configured")
            return
        }
        Utilities.globalQueue.postRunnable {
            try {
                val start = http("POST", "/v1/auth/start", null, null)
                val nonce = start.getString("nonce")
                val bot = start.optString("bot", BuildConfig.NG_BOT_USERNAME)
                AndroidUtilities.runOnUIThread { startBot(account, bot, nonce, cb) }
            } catch (e: Exception) {
                FileLog.e(e)
                AndroidUtilities.runOnUIThread { cb.done(false, e.message ?: "error") }
            }
        }
    }

    private fun startBot(account: Int, botUsername: String, nonce: String, cb: Callback<Boolean>) {
        val req = TLRPC.TL_contacts_resolveUsername().apply { username = botUsername }
        ConnectionsManager.getInstance(account).sendRequest(req) { response, error ->
            AndroidUtilities.runOnUIThread {
                val res = response as? TLRPC.TL_contacts_resolvedPeer
                val mc = MessagesController.getInstance(account)
                val bot = res?.users?.firstOrNull { res.peer != null && it.id == res.peer.user_id }
                if (res == null || bot == null) {
                    cb.done(false, error?.text ?: "bot_not_found")
                    return@runOnUIThread
                }
                mc.putUsers(res.users, false)
                val sb = TLRPC.TL_messages_startBot().apply {
                    this.bot = mc.getInputUser(bot)
                    peer = mc.getInputPeer(bot.id)
                    start_param = "ng_$nonce"
                    random_id = Utilities.random.nextLong()
                }
                ConnectionsManager.getInstance(account).sendRequest(sb) { r, e ->
                    if (r is TLRPC.Updates) mc.processUpdates(r, false)
                    if (e != null) {
                        AndroidUtilities.runOnUIThread { cb.done(false, e.text) }
                    } else {
                        Utilities.globalQueue.postRunnable { poll(account, nonce, 0, cb) }
                    }
                }
            }
        }
    }

    private fun poll(account: Int, nonce: String, attempt: Int, cb: Callback<Boolean>) {
        try {
            val r = http("GET", "/v1/auth/poll?nonce=$nonce", null, null)
            if (r.optString("status") == "ok") {
                val uid = r.getString("user_id").toLong()
                if (uid != selfId(account)) {
                    // Бота запустили из другого аккаунта — так быть не должно.
                    AndroidUtilities.runOnUIThread { cb.done(false, "wrong_account") }
                    return
                }
                prefs().edit().putString("token_$uid", r.getString("token")).apply()
                AndroidUtilities.runOnUIThread { cb.done(true, null) }
                return
            }
        } catch (e: Exception) {
            FileLog.e(e)
        }
        if (attempt + 1 >= POLL_ATTEMPTS) {
            AndroidUtilities.runOnUIThread { cb.done(false, "timeout") }
            return
        }
        Utilities.globalQueue.postRunnable({ poll(account, nonce, attempt + 1, cb) }, POLL_INTERVAL_MS)
    }

    /** Отключиться: профиль и токен удаляются на сервере. */
    @JvmStatic
    fun disconnect(account: Int, cb: Callback<Boolean>) {
        val token = token(account)
        val self = selfId(account)
        Utilities.globalQueue.postRunnable {
            try {
                if (token != null) http("DELETE", "/v1/me", null, token)
            } catch (e: Exception) {
                FileLog.e(e)
            }
            prefs().edit().remove("token_$self").remove("own_$self").apply()
            synchronized(cache) { cache.remove(self) }
            AndroidUtilities.runOnUIThread { cb.done(true, null) }
        }
    }

    /** Сохранить свой профиль на сервере. */
    @JvmStatic
    fun saveOwn(account: Int, profile: NgProfile, cb: Callback<Boolean>) {
        val token = token(account)
        val self = selfId(account)
        prefs().edit().putString("own_$self", profile.toJson().toString()).apply()
        synchronized(cache) { cache[self] = Entry(if (profile.isEmpty) null else profile.copy(), System.currentTimeMillis()) }
        if (token == null) {
            cb.done(false, "not_connected")
            return
        }
        Utilities.globalQueue.postRunnable {
            try {
                http("PUT", "/v1/me", profile.toJson().toString(), token)
                AndroidUtilities.runOnUIThread { cb.done(true, null) }
            } catch (e: Exception) {
                FileLog.e(e)
                AndroidUtilities.runOnUIThread { cb.done(false, e.message ?: "error") }
            }
        }
    }

    // ------------------------------------------------------------------ чужие профили

    /** Профиль пользователя (из кэша или с сервера). null — профиля нет. */
    @JvmStatic
    fun get(account: Int, userId: Long, cb: Callback<NgProfile?>) {
        if (userId == selfId(account)) {
            val own = own(account)
            cb.done(if (isConnected(account) && !own.isEmpty) own else null, null)
            return
        }
        val token = if (isConfigured) token(account) else null
        if (token == null) {
            cb.done(null, "not_connected")
            return
        }
        synchronized(cache) {
            val e = cache[userId]
            if (e != null && System.currentTimeMillis() - e.time < CACHE_TTL_MS) {
                cb.done(e.profile, null)
                return
            }
            pending[userId]?.let {
                it += cb
                return
            }
            pending[userId] = mutableListOf(cb)
        }
        Utilities.globalQueue.postRunnable {
            var result: NgProfile? = null
            var ok = false
            try {
                val body = JSONObject().put("ids", JSONArray().put(userId.toString()))
                val r = http("POST", "/v1/profiles", body.toString(), token)
                result = NgProfile.fromJson(r.optJSONObject("profiles")?.optJSONObject(userId.toString()))?.takeIf { !it.isEmpty }
                ok = true
            } catch (e: Exception) {
                FileLog.e(e)
            }
            val callbacks = synchronized(cache) {
                if (ok) cache[userId] = Entry(result, System.currentTimeMillis())
                pending.remove(userId)
            }
            val profile = result
            if (callbacks != null) AndroidUtilities.runOnUIThread { callbacks.forEach { it.done(profile, null) } }
        }
    }

    // ------------------------------------------------------------------ http

    private fun http(method: String, path: String, body: String?, token: String?): JSONObject {
        val base = BuildConfig.NG_BACKEND_URL.trimEnd('/')
        val c = URL(base + path).openConnection() as HttpURLConnection
        try {
            c.requestMethod = method
            c.connectTimeout = 10_000
            c.readTimeout = 15_000
            c.setRequestProperty("Accept", "application/json")
            if (token != null) c.setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                c.doOutput = true
                c.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                c.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val code = c.responseCode
            val text = (if (code >= 400) c.errorStream else c.inputStream)?.use { it.readBytes().toString(Charsets.UTF_8) } ?: "{}"
            if (code >= 400 && code != 410) throw IllegalStateException("HTTP $code: $text")
            return JSONObject(text)
        } finally {
            c.disconnect()
        }
    }
}
