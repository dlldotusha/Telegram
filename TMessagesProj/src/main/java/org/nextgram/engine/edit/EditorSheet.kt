package org.nextgram.engine.edit

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.FrameLayout
import org.nextgram.engine.EngineStrings
import org.nextgram.engine.NextEngine
import org.nextgram.engine.hooks.SizeHooks
import org.telegram.messenger.AndroidUtilities
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
import kotlin.math.roundToInt

/**
 * Свойства выбранного элемента на родных компонентах Telegram.
 *
 * Сверху — часть, которую зажали (время, текст, пузырь…): её цвета, размер текста,
 * и если есть — настройки, которые можно сохранить по уровням (везде / тип чатов /
 * папка / чат). Ниже — остальные части элемента и все его цвета.
 * Тап — изменить, долгий тап — вернуть как было.
 */
internal class EditorSheet(context: Context, private val target: EditTarget) : BottomSheet(context, false) {

    private val parts: List<EditorSpec.Part>
    private val props: List<EditorSpec.Prop>
    private val groups: List<ThemeCatalog.ColorGroup>
    private var showAllColors = false

    private val clicks = HashMap<Int, () -> Unit>()
    private val longClicks = HashMap<Int, () -> Unit>()
    private var nextId = 1
    private val listView: UniversalRecyclerView

    init {
        setApplyBottomPadding(false)
        val view = target.view()
        parts = view?.let { target.spec.parts(it) } ?: emptyList()
        props = view?.let { target.spec.props(it) } ?: emptyList()
        groups = if (target.isAuto) {
            view?.let { AutoSpec.allGroups(it) } ?: emptyList()
        } else {
            view?.let { target.spec.colorGroups(it) }?.takeIf { it.isNotEmpty() } ?: (view?.let { AutoSpec.screenGroups(it) } ?: emptyList())
        }
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

    private fun id(onClick: () -> Unit, onLong: (() -> Unit)? = null): Int {
        val id = nextId++
        clicks[id] = onClick
        if (onLong != null) longClicks[id] = onLong
        return id
    }

    // ------------------------------------------------------------------ содержимое

    private fun fill(items: ArrayList<UItem>) {
        clicks.clear()
        longClicks.clear()
        nextId = 1
        val focus = parts.firstOrNull { it.id == target.part }

        if (focus != null) {
            items += UItem.asHeader(focus.title)
            focus.colors.forEach { items += colorRow(it) }
            focus.sizeId?.let { sizeId -> addSize(items, sizeId) }
            addScoped(items, focus)
            items += UItem.asShadow(EngineStrings.get("theme_colors_info"))
        }

        // Остальные части элемента
        val others = parts.filter { it.id != focus?.id }
        if (others.isNotEmpty()) {
            items += UItem.asHeader(EngineStrings.get(if (focus != null) "other_parts" else "parts"))
            for (p in others) items += UItem.asButton(id({ EditMode.selectPart(p.id) }), p.title)
            items += UItem.asShadow(EngineStrings.get("parts_hint"))
        }
        // Настройки по уровням, не привязанные к части
        if (focus == null) addScoped(items, null)

        // Все цвета элемента
        if (groups.isNotEmpty()) {
            val expanded = showAllColors || parts.isEmpty()
            if (parts.isNotEmpty()) {
                items += UItem.asButton(id({ showAllColors = !showAllColors; rebuild() }), R.drawable.msg_palette,
                    EngineStrings.get(if (expanded) "hide_all_colors" else "all_colors"))
            }
            if (expanded) {
                for (group in groups) {
                    items += UItem.asHeader(group.title)
                    group.items.forEach { items += colorRow(it) }
                }
                items += UItem.asShadow(EngineStrings.get("theme_colors_info"))
            } else {
                items += UItem.asShadow(null)
            }
        }

        if (target.isAuto) {
            target.parent()?.let { parent ->
                items += UItem.asButton(id({ EditMode.select(parent) }), EngineStrings.get("parent") + " · " + parent.title)
                items += UItem.asShadow(if (groups.isEmpty()) EngineStrings.get("no_spec") else null)
            }
        }

        // Сброс
        if (props.isNotEmpty()) {
            val scope = EditMode.currentScope(ScopeOption.forTarget(target))
            items += UItem.asButton(id({ NextEngine.edit { it.clearElement(scope.scope, target.elementId) } }), EngineStrings.get("reset_element")).red()
        }
        val allKeys = (groups.flatMap { it.items } + parts.flatMap { it.colors }).map { it.key }.distinct()
        if (allKeys.isNotEmpty()) {
            items += UItem.asButton(id({ allKeys.forEach { ThemeApply.setColor(it, null) }; rebuild() }), EngineStrings.get("reset_colors")).red()
        }
        items += UItem.asShadow(null)
    }

    private fun colorRow(item: ThemeCatalog.ColorItem): UItem {
        val color = ThemeApply.color(item.key)
        val cell = TextColorCell(context).apply { setTextAndColor(item.title, color, false) }
        return UItem.asCustom(id({
            ColorPickerDialog.show(context, item.title, color) { picked -> setThemeColor(item.key, picked) }
        }, { setThemeColor(item.key, null) }), cell)
    }

    private fun addSize(items: ArrayList<UItem>, sizeId: String) {
        items += UItem.asHeader(EngineStrings.get(if (sizeId == SizeHooks.CHAT_TEXT) "text_size_messages" else "text_size"))
        val value = (SizeHooks.scale(sizeId) * 100).roundToInt()
        items += UItem.asIntSlideView(1, (SizeHooks.MIN * 100).roundToInt(), value, (SizeHooks.MAX * 100).roundToInt(),
            { v -> "$v%" },
            { v -> SizeHooks.setScale(sizeId, v / 100f) })
    }

    /** Настройки, которые можно сохранить по уровням (секунды, свой цвет, скрытие…). */
    private fun addScoped(items: ArrayList<UItem>, part: EditorSpec.Part?) {
        val list = props.filter { it.part == part?.id }
        if (list.isEmpty() && part?.hideable != true) return
        val style = target.style()
        val scopes = ScopeOption.forTarget(target)
        val scope = EditMode.currentScope(scopes)
        items += UItem.asShadow(null)
        items += UItem.asHeader(EngineStrings.get("scoped_header"))
        items += UItem.asButton(id({ chooseScope(scopes) }), EngineStrings.get("save_to"), scope.label)
        val inherited = ArrayList<String>()
        for (prop in list) {
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
                        ColorPickerDialog.show(context, prop.title, color ?: ThemeApply.color(org.telegram.ui.ActionBar.Theme.key_chat_inTimeText)) { picked -> write(scope, prop, picked) }
                    }, { write(scope, prop, null) }), cell)
                }
            }
        }
        if (part?.hideable == true) {
            items += UItem.asButton(id({ EditMode.startHideByTap() }), R.drawable.msg_customize, EngineStrings.get("hide_by_tap")).accent()
        }
        val info = StringBuilder(EngineStrings.get("scope_info"))
        if (inherited.isNotEmpty()) info.append("\n\n").append(EngineStrings.get("inherited")).append(' ').append(inherited.joinToString("; "))
        items += UItem.asShadow(info.toString())
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
}
