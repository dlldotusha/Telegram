import org.nextgram.engine.core.Colors
import org.nextgram.engine.core.ConfigCodec
import org.nextgram.engine.core.ResolvedElement
import org.nextgram.engine.core.Scopes
import org.nextgram.engine.core.StyleConfig
import kotlin.math.abs
import kotlin.system.exitProcess

/** Тесты ядра NextEngine на обычной JVM, без Android. Запуск: nextengine-tests/run.sh */

private var passed = 0
private var failed = 0

private fun check(cond: Boolean, name: String) {
    if (cond) passed++ else { failed++; println("FAIL: $name") }
}

private const val MSG = "chat.message.in"

fun main() {
    cascadeByProperty()
    chainOrder()
    folderPriority()
    resetAndPrune()
    colors()
    jsonRoundTrip()
    brokenJson()
    println("$passed passed, $failed failed")
    if (failed > 0) exitProcess(1)
}

private fun cascadeByProperty() {
    val c = StyleConfig()
    c.set(Scopes.GLOBAL, MSG, "time", "seconds", true)
    c.set(Scopes.GLOBAL, MSG, "time", "color", 0xFF111111.toInt())
    c.set(Scopes.Category.CHANNELS.scope, MSG, "time", "hidden", true)
    c.set(Scopes.chat(-100L), MSG, "time", "color", 0xFF222222.toInt())

    val channelChat = Scopes.chain(Scopes.Category.CHANNELS, null, listOf(Scopes.chat(-100L)))
    val r = c.resolve(channelChat, MSG)
    check(r.getBool("time", "seconds", false), "секунды наследуются с global")
    check(r.getBool("time", "hidden", false), "скрытие из категории каналов")
    check(r.getColor("time", "color") == 0xFF222222.toInt(), "цвет чата перекрывает global")
    check(r.sourceOf("time", "color") == Scopes.chat(-100L), "источник цвета — чат")
    check(r.sourceOf("time", "seconds") == Scopes.GLOBAL, "источник секунд — global")

    val group = Scopes.chain(Scopes.Category.GROUPS, null, listOf(Scopes.chat(-200L)))
    val g = c.resolve(group, MSG)
    check(!g.getBool("time", "hidden", false), "в группе время не скрыто")
    check(g.getColor("time", "color") == 0xFF111111.toInt(), "в группе цвет из global")

    check(c.resolve(channelChat, "other.element") === ResolvedElement.EMPTY, "чужой элемент пустой")
}

private fun chainOrder() {
    val chain = Scopes.chain(
        Scopes.Category.CONTACTS,
        listOf("folder:1:5", "folder:1:7"),
        listOf(Scopes.chat(42), Scopes.chat(-9_000_000_000L)),
    )
    check(chain == listOf("global", "type:contacts", "folder:1:7", "folder:1:5", "chat:42", "chat:-9000000000"), "порядок цепочки: $chain")
    check(Scopes.kindOf("folder:1:5") == Scopes.Kind.FOLDER, "kind folder")
    check(Scopes.kindOf("chat:1") == Scopes.Kind.CHAT, "kind chat")
    check(Scopes.kindOf("type:bots") == Scopes.Kind.CATEGORY, "kind category")
    check(Scopes.Category.byId("non_contacts") == Scopes.Category.NON_CONTACTS, "category byId")
}

private fun folderPriority() {
    val c = StyleConfig()
    c.set("folder:1:5", MSG, "time", "color", 0xFF000005.toInt())
    c.set("folder:1:7", MSG, "time", "color", 0xFF000007.toInt())
    // папка 5 выше в списке папок → приоритетнее
    val chain = Scopes.chain(null, listOf("folder:1:5", "folder:1:7"), null)
    check(c.resolve(chain, MSG).getColor("time", "color") == 0xFF000005.toInt(), "верхняя папка побеждает")
}

private fun resetAndPrune() {
    val c = StyleConfig()
    c.set(Scopes.chat(1), MSG, "time", "hidden", true)
    c.set(Scopes.chat(1), MSG, "time", "hidden", null)
    check(c.scopes.isEmpty(), "пустые уровни удаляются")
    c.set(Scopes.GLOBAL, MSG, null, "hidden", false)
    check(c.get(Scopes.GLOBAL, MSG, null, "hidden") == false, "свойство самого элемента")
    c.clearElement(Scopes.GLOBAL, MSG)
    check(c.scopes.isEmpty(), "clearElement")
    c.set(Scopes.GLOBAL, MSG, "time", "hidden", null)
    check(c.scopes.isEmpty(), "сброс несуществующего не создаёт узлы")
}

private fun colors() {
    check(Colors.format(0xFF88AAFF.toInt()) == "#FF88AAFF", "format")
    check(Colors.parse("#88AAFF") == 0xFF88AAFF.toInt(), "parse rgb")
    check(Colors.parse("#8088AAFF") == 0x8088AAFF.toInt(), "parse argb")
    check(Colors.parse("#00000000") == 0, "прозрачный — валидный цвет")
    check(Colors.parse("zz") == null, "мусор")
    check(Colors.parse(true) == null, "не строка")
    val c = StyleConfig()
    c.set(Scopes.GLOBAL, MSG, "time", "color", 0x11223344)
    check(c.get(Scopes.GLOBAL, MSG, "time", "color") == "#11223344", "цвет хранится строкой")
}

private fun jsonRoundTrip() {
    val c = StyleConfig()
    c.set(Scopes.GLOBAL, MSG, "time", "seconds", true)
    c.set(Scopes.Category.CHANNELS.scope, MSG, "time", "hidden", true)
    c.set(Scopes.chat(-1001234), MSG, "time", "color", 0xFF88AAFF.toInt())
    c.set(Scopes.GLOBAL, MSG, null, "scale", 1.25)
    c.setThemeColor("Blue", "chat_inTimeText", 0xFF6A7B8C.toInt())

    val back = ConfigCodec.fromJson(ConfigCodec.toJson(c))
    check(back.scopes == c.scopes, "round trip: уровни")
    val chain = Scopes.chain(Scopes.Category.CHANNELS, null, listOf(Scopes.chat(-1001234)))
    val r = back.resolve(chain, MSG)
    check(r.getBool("time", "seconds", false) && r.getBool("time", "hidden", false), "булевы после JSON")
    check(r.getColor("time", "color") == 0xFF88AAFF.toInt(), "цвет после JSON")
    check(abs(r.getFloat(null, "scale", 0f) - 1.25f) < 1e-6, "число после JSON")
    check(back.themeColors("Blue")["chat_inTimeText"] == "#FF6A7B8C", "цвета темы после JSON")

    val copy = c.copy()
    copy.set(Scopes.GLOBAL, MSG, "time", "seconds", null)
    check(c.resolve(chain, MSG).getBool("time", "seconds", false), "copy независима")
}

private fun brokenJson() {
    val threw = runCatching { ConfigCodec.fromJson("{not json") }.isFailure
    check(threw, "битый JSON бросает исключение")

    val c = ConfigCodec.fromJson("""{"version":99,"layers":{"global":{"x":{"parts":{"p":{"a":[1,2],"b":true}}}}},"themeColors":{"T":{"k":"bad","k2":"#FFFFFF"}}}""")
    check(c.get("global", "x", "p", "b") == true, "неизвестная версия читается")
    check(c.get("global", "x", "p", "a") == null, "массивы игнорируются")
    check(c.themeColors("T").size == 1, "битый цвет темы отброшен")
}
