package org.nextgram.engine.edit

import android.graphics.RectF
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import org.nextgram.engine.EngineStrings
import org.telegram.messenger.UserConfig
import org.telegram.ui.ActionBar.SimpleTextView
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.ActionBar.ThemeColors

/**
 * Автоматический режим для элементов без заготовки. Доступны цвета темы,
 * найденные через ThemeDescription экрана, и цвета темы, совпадающие с тем,
 * чем этот view сейчас нарисован (цвет текста, фон).
 */
object AutoSpec : EditorSpec {
    override fun handles(view: View) = true
    override fun elementId(view: View) = "auto:" + view.javaClass.name

    /** Человеческое название по типу view. В релизе имена классов сокращены R8 — их не показываем. */
    override fun title(view: View): String {
        val text: CharSequence? = when (view) {
            is EditText -> null
            is TextView -> view.text
            is SimpleTextView -> view.text
            else -> null
        }
        if (!text.isNullOrBlank()) {
            val t = text.toString().replace('\n', ' ').trim()
            return EngineStrings.pick("Текст", "Text") + " «" + (if (t.length > 24) t.take(24) + "…" else t) + "»"
        }
        return when (view) {
            is EditText -> EngineStrings.pick("Поле ввода", "Input field")
            is ImageView -> EngineStrings.pick("Иконка или картинка", "Icon or image")
            else -> EngineStrings.get("screen_element")
        }
    }

    override fun dialogId(view: View) = 0L
    override fun account(view: View) = UserConfig.selectedAccount
    override fun parts(view: View) = emptyList<EditorSpec.Part>()
    override fun partBounds(view: View, partId: String, out: RectF) = false
    override fun props(view: View) = emptyList<EditorSpec.Prop>()
    override fun apply(view: View) = view.invalidate()

    /** Есть ли у view что менять. Пустые слои, картинки и контейнеры без своих цветов не выбираются. */
    fun hasColors(view: View): Boolean = allGroups(view).isNotEmpty()

    /** Цвета из описаний экрана + цвета, которыми view нарисован сейчас (без повторов). */
    fun allGroups(view: View): List<ThemeCatalog.ColorGroup> {
        val fromScreen = screenGroups(view)
        val seen = fromScreen.flatMap { g -> g.items.map { it.key } }.toHashSet()
        return fromScreen + colorGroups(view).mapNotNull { g ->
            val items = g.items.filter { seen.add(it.key) }
            if (items.isEmpty()) null else ThemeCatalog.ColorGroup(g.title, g.title, items)
        }
    }

    /** Цвета темы экрана, которые касаются этого view (из ThemeDescription открытого экрана). */
    fun screenGroups(view: View): List<ThemeCatalog.ColorGroup> {
        val all = try {
            org.telegram.ui.LaunchActivity.getLastFragment()?.themeDescriptions?.filterNotNull()
        } catch (e: Throwable) {
            org.telegram.messenger.FileLog.e(e)
            null
        } ?: return emptyList()
        val keys = LinkedHashSet<Int>()
        var v: View? = view
        var depth = 0
        while (depth < 4 && v != null && keys.isEmpty()) {
            for (d in all) {
                val key = d.currentKey
                if (key >= 0 && ThemeColors.getStringName(key) != null && matches(d.customView, d.customListClasses, v)) keys += key
            }
            v = v.parent as? View
            depth++
        }
        if (keys.isEmpty()) return emptyList()
        val items = keys.map { ThemeCatalog.ColorItem(it, ThemeCatalog.humanName(it), ThemeCatalog.humanName(it)) }
        return listOf(ThemeCatalog.ColorGroup(EngineStrings.get("theme_colors"), EngineStrings.get("theme_colors"), items))
    }

    private fun matches(owner: View?, classes: Array<out Class<*>?>?, target: View): Boolean {
        if (classes == null || classes.isEmpty()) return owner === target
        if (classes.none { it != null && it.isInstance(target) }) return false
        if (owner == null || owner === target) return true
        var p = target.parent
        while (p != null) {
            if (p === owner) return true
            p = p.parent
        }
        return false
    }

    /** Цвета темы, которыми этот view нарисован сейчас (по точному совпадению цвета). */
    override fun colorGroups(view: View): List<ThemeCatalog.ColorGroup> {
        val groups = ArrayList<ThemeCatalog.ColorGroup>()
        MoreCatalog.textColor(view)?.let { color ->
            matching(color).takeIf { it.isNotEmpty() }?.let {
                groups += ThemeCatalog.ColorGroup("Цвет этого текста", "This text color", it)
            }
        }
        MoreCatalog.backgroundColor(view)?.let { color ->
            matching(color).takeIf { it.isNotEmpty() }?.let {
                groups += ThemeCatalog.ColorGroup("Цвет фона", "Background color", it)
            }
        }
        return groups
    }

    private fun matching(color: Int): List<ThemeCatalog.ColorItem> {
        if (android.graphics.Color.alpha(color) == 0) return emptyList()
        val out = ArrayList<ThemeCatalog.ColorItem>()
        for (key in 0 until Theme.colorsCount) {
            if (ThemeColors.getStringName(key) == null) continue
            val c = try { Theme.getColor(key) } catch (e: Throwable) { continue }
            if (c == color) {
                val name = ThemeCatalog.humanName(key)
                out += ThemeCatalog.ColorItem(key, name, name)
                if (out.size >= 6) break
            }
        }
        return out
    }
}
