package org.nextgram.engine.edit

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.FrameLayout
import org.nextgram.engine.EngineStrings
import org.nextgram.engine.NextEngine
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.FileLog
import org.telegram.messenger.R
import org.telegram.tgnet.ConnectionsManager
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BottomSheet
import org.telegram.ui.ActionBar.ThemeColors
import org.telegram.ui.Cells.TextColorCell
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.Components.UItem
import org.telegram.ui.Components.UniversalAdapter
import org.telegram.ui.Components.UniversalRecyclerView
import org.telegram.ui.LaunchActivity

/**
 * Свойства выбранного элемента. Собрано на родных компонентах Telegram
 * (UniversalRecyclerView + UItem), поэтому выглядит и перекрашивается как
 * остальные настройки.
 *
 * Сверху — свойства этого элемента, которые можно сохранить на уровень
 * (везде / категория / папка / чат). Ниже — цвета темы элемента: они глобальные.
 * Тап — изменить, долгий тап — сбросить.
 */
internal class EditorSheet(context: Context, private val target: EditTarget) : BottomSheet(context, false) {

    private val parts: List<EditorSpec.Part>
    private val props: List<EditorSpec.Prop>
    private val groups: List<ThemeCatalog.ColorGroup>

    private val clicks = HashMap<Int, () -> Unit>()
    private val longClicks = HashMap<Int, () -> Unit>()
    private var nextId = 1
    private val listView: UniversalRecyclerView

    init {
        setApplyBottomPadding(false)
        val view = target.view()
        parts = view?.let { target.spec.parts(it) } ?: emptyList()
        props = view?.let { target.spec.props(it) } ?: emptyList()
        groups = view?.let { target.spec.colorGroups(it) }?.takeIf { it.isNotEmpty() } ?: autoGroups(view)
        setTitle(target.title, true)

        listView = UniversalRecyclerView(
            context, target.account, ConnectionsManager.generateClassGuid(), true,
            { items, _ -> fill(items) },
            { item, _, _, _, _ -> clicks[item.id]?.invoke() },
            { item, _, _, _, _ -> longClicks[item.id]?.let { it(); true } ?: false },
            null,
        )
        val container = FrameLayout(context)
        container.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT.toFloat()))
        container.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (AndroidUtilities.displaySize.y * 0.62f).toInt())
        setCustomView(container)
        setOnDismissListener(DialogInterface.OnDismissListener { EditMode.onSheetDismissed(this) })
    }

    fun rebuild() {
        (listView.getAdapter() as UniversalAdapter).update(true)
    }

    // ------------------------------------------------------------------ содержимое

    private fun id(onClick: () -> Unit, onLong: (() -> Unit)? = null): Int {
        val id = nextId++
        clicks[id] = onClick
        if (onLong != null) longClicks[id] = onLong
        return id
    }

    private fun fill(items: ArrayList<UItem>) {
        clicks.clear()
        longClicks.clear()
        nextId = 1
        val context = context
        val style = target.style()
        val scopes = ScopeOption.forTarget(target)
        val scope = EditMode.currentScope(scopes)

        // Свойства этого элемента (по уровням)
        if (props.isNotEmpty()) {
            items += UItem.asButton(id({ chooseScope(scopes) }), EngineStrings.get("save_to"), scope.label)
            items += UItem.asShadow(EngineStrings.get("scope_info"))

            val partTitles = parts.associate { it.id to it.title }
            val inherited = ArrayList<String>()
            for ((part, partProps) in props.groupBy { it.part }) {
                items += UItem.asHeader(part?.let { partTitles[it] ?: it } ?: target.title)
                for (prop in partProps) {
                    val source = style.sourceOf(prop.part, prop.key)
                    if (source != null && source != scope.scope) inherited += prop.title + " — " + ScopeOption.labelFor(source, scopes)
                    when (prop.type) {
                        EditorSpec.Prop.Type.BOOL -> {
                            val value = style.getBool(prop.part, prop.key, prop.defaultBool)
                            items += UItem.asCheck(id({ write(scope, prop, !value) }, { write(scope, prop, null) }), prop.title).setChecked(value)
                        }
                        EditorSpec.Prop.Type.COLOR -> {
                            val color = style.getColor(prop.part, prop.key)
                            val cell = TextColorCell(context).apply { setTextAndColor(prop.title, color ?: 0, false) }
                            items += UItem.asCustom(id({
                                ColorPickerDialog.show(context, prop.title, color ?: 0xFF808080.toInt()) { picked -> write(scope, prop, picked) }
                            }, { write(scope, prop, null) }), cell)
                        }
                    }
                }
            }
            if (parts.any { it.hideable }) {
                items += UItem.asButton(id({ EditMode.startHideByTap() }), R.drawable.msg_customize, EngineStrings.get("hide_by_tap")).accent()
            }
            val info = StringBuilder(EngineStrings.get("long_press_reset"))
            if (inherited.isNotEmpty()) info.append("\n\n").append(EngineStrings.get("inherited")).append(' ').append(inherited.joinToString("; "))
            items += UItem.asShadow(info.toString())
        }

        // Цвета темы элемента (глобальные)
        for (group in groups) {
            items += UItem.asHeader(group.title)
            for (item in group.items) {
                val color = ThemeApply.color(item.key)
                val cell = TextColorCell(context).apply { setTextAndColor(item.title, color, false) }
                items += UItem.asCustom(id({
                    ColorPickerDialog.show(context, item.title, color) { picked -> setThemeColor(item.key, picked) }
                }, { setThemeColor(item.key, null) }), cell)
            }
        }
        if (groups.isNotEmpty()) items += UItem.asShadow(EngineStrings.get("theme_colors_info"))

        if (target.isAuto) {
            target.parent()?.let { parent ->
                items += UItem.asButton(id({ EditMode.select(parent) }), EngineStrings.get("parent") + " · " + parent.title)
                items += UItem.asShadow(if (groups.isEmpty()) EngineStrings.get("no_spec") else null)
            }
        }

        // Сброс
        if (props.isNotEmpty()) {
            items += UItem.asButton(id({ NextEngine.edit { it.clearElement(scope.scope, target.elementId) } }), EngineStrings.get("reset_element")).red()
        }
        if (groups.isNotEmpty()) {
            items += UItem.asButton(id({ groups.flatMap { it.items }.forEach { ThemeApply.setColor(it.key, null) }; rebuild() }), EngineStrings.get("reset_colors")).red()
        }
        items += UItem.asShadow(null)
    }

    // ------------------------------------------------------------------ действия

    private fun write(scope: ScopeOption, prop: EditorSpec.Prop, value: Any?) {
        NextEngine.edit { it.set(scope.scope, target.elementId, prop.part, prop.key, value) }
    }

    private fun setThemeColor(key: Int, argb: Int?) {
        ThemeApply.setColor(key, argb)
        rebuild()
    }

    private fun chooseScope(scopes: List<ScopeOption>) {
        AlertDialog.Builder(context)
            .setTitle(EngineStrings.get("save_to"))
            .setItems(scopes.map { it.label as CharSequence }.toTypedArray()) { _, which ->
                EditMode.setPreferredScope(scopes[which])
                rebuild()
            }
            .show()
    }

    // ------------------------------------------------------------------ авторежим

    /** Для элементов без заготовки: цвета темы экрана, которые касаются этого view, с понятными названиями. */
    private fun autoGroups(view: View?): List<ThemeCatalog.ColorGroup> {
        if (view == null) return emptyList()
        val all = try {
            LaunchActivity.getLastFragment()?.themeDescriptions?.filterNotNull()
        } catch (e: Throwable) {
            FileLog.e(e)
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
}
