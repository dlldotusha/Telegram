package org.nextgram.engine;

import org.nextgram.engine.core.ConfigCodec;
import org.nextgram.engine.core.ResolvedElement;
import org.nextgram.engine.core.StyleConfig;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Точка входа в кастомизацию: хранит конфиг, отдаёт итоговые стили элементов
 * и сохраняет правки на диск.
 *
 * Все чтения стиля идут через {@link #resolve} — он кэширует результат каскада
 * на чат, так что хуки в отрисовке почти ничего не стоят.
 */
public final class NextEngine {

    public interface Editor {
        void edit(StyleConfig config);
    }

    private static final String FILE_NAME = "nextengine.json";
    /** Членство чата в папках и категория могут меняться — кэш живёт недолго. */
    private static final long CACHE_TTL_MS = 30_000;

    private static volatile StyleConfig config;
    private static volatile int generation;
    private static final ArrayList<Runnable> listeners = new ArrayList<>();

    private static final class CacheEntry {
        final ResolvedElement value;
        final int generation;
        final long time;

        CacheEntry(ResolvedElement value, int generation, long time) {
            this.value = value;
            this.generation = generation;
            this.time = time;
        }
    }

    private static final HashMap<String, CacheEntry> cache = new HashMap<>();
    private static final Runnable saveRunnable = NextEngine::saveNow;

    private NextEngine() {
    }

    /** Увеличивается при каждой правке; по нему инвалидируются кэши. */
    public static int generation() {
        return generation;
    }

    /** Конфиг только для чтения. Менять — через {@link #edit}. */
    public static StyleConfig config() {
        StyleConfig c = config;
        if (c == null) {
            synchronized (NextEngine.class) {
                c = config;
                if (c == null) {
                    c = load();
                    config = c;
                }
            }
        }
        return c;
    }

    /** Можно ли уже читать конфиг (контекст приложения создан). */
    static boolean isReady() {
        return config != null || ApplicationLoader.applicationContext != null;
    }

    /** Правка конфига. Только с UI-потока. */
    public static void edit(Editor editor) {
        StyleConfig next = config().copy();
        editor.edit(next);
        config = next;
        generation++;
        synchronized (cache) {
            cache.clear();
        }
        Utilities.globalQueue.cancelRunnable(saveRunnable);
        Utilities.globalQueue.postRunnable(saveRunnable, 300);
        for (Runnable r : new ArrayList<>(listeners)) {
            r.run();
        }
    }

    public static void addListener(Runnable r) {
        listeners.add(r);
    }

    public static void removeListener(Runnable r) {
        listeners.remove(r);
    }

    /** Стиль элемента в чате. dialogId == 0 — элемент вне чатов, только global. */
    public static ResolvedElement resolve(int account, long dialogId, String element) {
        if (!isReady()) {
            return ResolvedElement.EMPTY;
        }
        StyleConfig c = config();
        if (c.scopes().isEmpty()) {
            return ResolvedElement.EMPTY;
        }
        String key = account + ":" + dialogId + ":" + element;
        long now = System.currentTimeMillis();
        int gen = generation;
        synchronized (cache) {
            CacheEntry e = cache.get(key);
            if (e != null && e.generation == gen && now - e.time < CACHE_TTL_MS) {
                return e.value;
            }
        }
        List<String> chain = ScopeResolver.chain(account, dialogId);
        ResolvedElement value = c.resolve(chain, element);
        synchronized (cache) {
            if (cache.size() > 2000) {
                cache.clear();
            }
            cache.put(key, new CacheEntry(value, gen, now));
        }
        return value;
    }

    /** Сбросить кэш каскада (например, после изменения папок). */
    public static void invalidateCache() {
        synchronized (cache) {
            cache.clear();
        }
    }

    private static File file() {
        return new File(ApplicationLoader.getFilesDirFixed(), FILE_NAME);
    }

    private static StyleConfig load() {
        File f = file();
        if (!f.exists()) {
            return new StyleConfig();
        }
        try (InputStream in = new FileInputStream(f)) {
            byte[] data = new byte[(int) f.length()];
            int off = 0;
            while (off < data.length) {
                int r = in.read(data, off, data.length - off);
                if (r < 0) {
                    break;
                }
                off += r;
            }
            return ConfigCodec.fromJson(new String(data, 0, off, StandardCharsets.UTF_8));
        } catch (Throwable e) {
            // Не теряем битый файл молча: откладываем его рядом, чтобы можно было разобраться.
            FileLog.e(e);
            File broken = new File(f.getParentFile(), FILE_NAME + ".broken");
            //noinspection ResultOfMethodCallIgnored
            f.renameTo(broken);
            return new StyleConfig();
        }
    }

    private static void saveNow() {
        StyleConfig c = config;
        if (c == null) {
            return;
        }
        String json = ConfigCodec.toJson(c);
        File f = file();
        File tmp = new File(f.getParentFile(), FILE_NAME + ".tmp");
        try (OutputStream out = new FileOutputStream(tmp)) {
            out.write(json.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (Throwable e) {
            FileLog.e(e);
            return;
        }
        if (!tmp.renameTo(f)) {
            FileLog.e("customize: can't replace " + f);
        }
    }

    /** Экспорт конфига (для обмена темами со своими). */
    public static String exportJson() {
        return ConfigCodec.toJson(config());
    }

    /** Импорт конфига целиком. Бросает исключение на битом JSON, текущий конфиг не трогается. */
    public static void importJson(String json) throws Exception {
        final StyleConfig imported = ConfigCodec.fromJson(json);
        AndroidUtilities.runOnUIThread(() -> {
            config = imported;
            edit(c -> { });
        });
    }
}
