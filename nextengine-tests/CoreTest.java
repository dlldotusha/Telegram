import org.nextgram.engine.core.Colors;
import org.nextgram.engine.core.ConfigCodec;
import org.nextgram.engine.core.ResolvedElement;
import org.nextgram.engine.core.Scopes;
import org.nextgram.engine.core.StyleConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Тесты ядра на обычной JVM, без Android.
 * Запуск: nextengine-tests/run.sh
 */
public class CoreTest {

    static int passed, failed;

    static void check(boolean cond, String name) {
        if (cond) {
            passed++;
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }

    static final String MSG = "chat.message.in";

    public static void main(String[] args) throws Exception {
        cascadeByProperty();
        chainOrder();
        folderPriority();
        resetAndPrune();
        colors();
        jsonRoundTrip();
        brokenJson();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    static void cascadeByProperty() {
        StyleConfig c = new StyleConfig();
        c.set(Scopes.GLOBAL, MSG, "time", "seconds", true);
        c.set(Scopes.GLOBAL, MSG, "time", "color", 0xFF111111);
        c.set(Scopes.Category.CHANNELS.scope(), MSG, "time", "hidden", true);
        c.set(Scopes.chat(-100L), MSG, "time", "color", 0xFF222222);

        List<String> channelChat = Scopes.chain(Scopes.Category.CHANNELS, null, Collections.singletonList(Scopes.chat(-100L)));
        ResolvedElement r = c.resolve(channelChat, MSG);
        check(r.getBool("time", "seconds", false), "секунды наследуются с global");
        check(r.getBool("time", "hidden", false), "скрытие из категории каналов");
        check(r.getColor("time", "color") == 0xFF222222, "цвет чата перекрывает global");
        check(Scopes.chat(-100L).equals(r.sourceOf("time", "color")), "источник цвета — чат");
        check(Scopes.GLOBAL.equals(r.sourceOf("time", "seconds")), "источник секунд — global");

        List<String> group = Scopes.chain(Scopes.Category.GROUPS, null, Collections.singletonList(Scopes.chat(-200L)));
        ResolvedElement g = c.resolve(group, MSG);
        check(!g.getBool("time", "hidden", false), "в группе время не скрыто");
        check(g.getColor("time", "color") == 0xFF111111, "в группе цвет из global");

        check(c.resolve(channelChat, "other.element") == ResolvedElement.EMPTY, "чужой элемент пустой");
    }

    static void chainOrder() {
        List<String> chain = Scopes.chain(Scopes.Category.CONTACTS,
                Arrays.asList("folder:1:5", "folder:1:7"),
                Arrays.asList(Scopes.chat(42), Scopes.chat(-9_000_000_000L)));
        check(chain.equals(Arrays.asList("global", "type:contacts", "folder:1:7", "folder:1:5", "chat:42", "chat:-9000000000")),
                "порядок цепочки: " + chain);
        check(Scopes.kindOf("folder:1:5") == Scopes.Kind.FOLDER, "kind folder");
        check(Scopes.kindOf("chat:1") == Scopes.Kind.CHAT, "kind chat");
        check(Scopes.kindOf("type:bots") == Scopes.Kind.CATEGORY, "kind category");
        check(Scopes.Category.byId("non_contacts") == Scopes.Category.NON_CONTACTS, "category byId");
    }

    static void folderPriority() {
        StyleConfig c = new StyleConfig();
        c.set("folder:1:5", MSG, "time", "color", 0xFF000005);
        c.set("folder:1:7", MSG, "time", "color", 0xFF000007);
        // папка 5 выше в списке папок → приоритетнее
        List<String> chain = Scopes.chain(null, Arrays.asList("folder:1:5", "folder:1:7"), null);
        check(c.resolve(chain, MSG).getColor("time", "color") == 0xFF000005, "верхняя папка побеждает");
    }

    static void resetAndPrune() {
        StyleConfig c = new StyleConfig();
        c.set(Scopes.chat(1), MSG, "time", "hidden", true);
        c.set(Scopes.chat(1), MSG, "time", "hidden", null);
        check(c.scopes().isEmpty(), "пустые уровни удаляются");
        c.set(Scopes.GLOBAL, MSG, null, "hidden", false);
        check(Boolean.FALSE.equals(c.get(Scopes.GLOBAL, MSG, null, "hidden")), "свойство самого элемента");
        c.clearElement(Scopes.GLOBAL, MSG);
        check(c.scopes().isEmpty(), "clearElement");
        c.set(Scopes.GLOBAL, MSG, "time", "hidden", null);
        check(c.scopes().isEmpty(), "сброс несуществующего не создаёт узлы");
    }

    static void colors() {
        check("#FF88AAFF".equals(Colors.format(0xFF88AAFF)), "format");
        check(Colors.parse("#88AAFF") == 0xFF88AAFF, "parse rgb");
        check(Colors.parse("#8088AAFF") == 0x8088AAFF, "parse argb");
        check(Colors.parse("#00000000") == 0, "прозрачный — валидный цвет");
        check(Colors.parse("zz") == null, "мусор");
        check(Colors.parse(true) == null, "не строка");
        StyleConfig c = new StyleConfig();
        c.set(Scopes.GLOBAL, MSG, "time", "color", 0x11223344);
        check("#11223344".equals(c.get(Scopes.GLOBAL, MSG, "time", "color")), "цвет хранится строкой");
    }

    static void jsonRoundTrip() throws Exception {
        StyleConfig c = new StyleConfig();
        c.set(Scopes.GLOBAL, MSG, "time", "seconds", true);
        c.set(Scopes.Category.CHANNELS.scope(), MSG, "time", "hidden", true);
        c.set(Scopes.chat(-1001234), MSG, "time", "color", 0xFF88AAFF);
        c.set(Scopes.GLOBAL, MSG, null, "scale", 1.25);
        c.setThemeColor("Blue", "chat_inTimeText", 0xFF6A7B8C);

        StyleConfig back = ConfigCodec.fromJson(ConfigCodec.toJson(c));
        check(ConfigCodec.toJson(back).equals(ConfigCodec.toJson(c)) || sameContent(c, back), "round trip");
        List<String> chain = Scopes.chain(Scopes.Category.CHANNELS, null, Collections.singletonList(Scopes.chat(-1001234)));
        ResolvedElement r = back.resolve(chain, MSG);
        check(r.getBool("time", "seconds", false) && r.getBool("time", "hidden", false), "булевы после JSON");
        check(r.getColor("time", "color") == 0xFF88AAFF, "цвет после JSON");
        check(Math.abs(r.getFloat(null, "scale", 0) - 1.25f) < 1e-6, "число после JSON");
        check("#FF6A7B8C".equals(back.themeColors("Blue").get("chat_inTimeText")), "цвета темы после JSON");

        StyleConfig copy = c.copy();
        copy.set(Scopes.GLOBAL, MSG, "time", "seconds", null);
        check(c.resolve(chain, MSG).getBool("time", "seconds", false), "copy независима");
    }

    static boolean sameContent(StyleConfig a, StyleConfig b) {
        // JSON-java на JVM не сохраняет порядок ключей, сравниваем по сути
        if (!a.scopes().equals(b.scopes())) {
            return false;
        }
        for (String s : a.scopes()) {
            for (String part : new String[]{"time", null}) {
                for (String prop : new String[]{"seconds", "hidden", "color", "scale"}) {
                    Object x = a.get(s, MSG, part, prop), y = b.get(s, MSG, part, prop);
                    if (x == null ? y != null : !(x.equals(y) || (x instanceof Number && y instanceof Number && ((Number) x).doubleValue() == ((Number) y).doubleValue()))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    static void brokenJson() throws Exception {
        boolean threw = false;
        try {
            ConfigCodec.fromJson("{not json");
        } catch (Exception e) {
            threw = true;
        }
        check(threw, "битый JSON бросает исключение");

        StyleConfig c = ConfigCodec.fromJson("{\"version\":99,\"layers\":{\"global\":{\"x\":{\"parts\":{\"p\":{\"a\":[1,2],\"b\":true}}}}},\"themeColors\":{\"T\":{\"k\":\"bad\",\"k2\":\"#FFFFFF\"}}}");
        check(Boolean.TRUE.equals(c.get("global", "x", "p", "b")), "неизвестная версия читается");
        check(c.get("global", "x", "p", "a") == null, "массивы игнорируются");
        check(c.themeColors("T").size() == 1, "битый цвет темы отброшен");
    }
}
