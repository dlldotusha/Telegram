package org.nextgram.engine.edit

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import org.nextgram.engine.EngineColors
import org.nextgram.engine.EngineStrings
import org.nextgram.engine.NextEngine
import org.nextgram.engine.core.ResolvedElement
import org.telegram.messenger.FileLog
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BottomSheet
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.ActionBar.ThemeColors
import org.telegram.ui.ActionBar.ThemeDescription
import org.telegram.ui.Cells.HeaderCell
import org.telegram.ui.Cells.TextCheckCell
import org.telegram.ui.Cells.TextColorCell
import org.telegram.ui.Cells.TextInfoPrivacyCell
import org.telegram.ui.Cells.TextSettingsCell
import org.telegram.ui.Components.LayoutHelper
import org.telegram.ui.LaunchActivity

/**
 * Свойства выбранного элемента: уровень сохранения, свойства заготовки по частям,
 * цвета темы, которые касаются этого элемента.
 *
 * Тап по строке — изменить на выбранном уровне, долгий тап — сбросить на нём.
 * Под каждой строкой видно, с какого уровня пришло текущее значение.
 */
internal class EditorSheet(context: Context, private val target: EditTarget) : BottomSheet(context, false) {

    private val list = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }

    /** Цвета темы под этим элементом: ключ → все описания экрана с этим ключом. */
    private val themeKeys = LinkedHashMap<Int, ArrayList<ThemeDescription>>()
    private val screenDescriptions = ArrayList<ThemeDescription>()

    /** Части и свойства берём при открытии: ячейка может уехать из списка, пока панель открыта. */
    private val parts: List<EditorSpec.Part>
    private val props: List<EditorSpec.Prop>

    init {
        setApplyBottomPadding(false)
        val view = target.view()
        parts = view?.let { target.spec.parts(it) } ?: emptyList()
        props = view?.let { target.spec.props(it) } ?: emptyList()
        collectThemeColors()
        val scroll = ScrollView(context)
        scroll.addView(list, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0))
        setCustomView(scroll)
        setOnDismissListener(DialogInterface.OnDismissListener { EditMode.onSheetDismissed(this) })
        rebuild()
    }

    // ------------------------------------------------------------------ данные

    private fun collectThemeColors() {
        val fragment = LaunchActivity.getLastFragment() ?: return
        val view = target.view() ?: return
        val all = try {
            fragment.themeDescriptions
        } catch (e: Throwable) {
            FileLog.e(e)
            null
        } ?: return
        screenDescriptions.addAll(all.filterNotNull())
        // Ищем описания для самого элемента; если их нет — поднимаемся к предкам (не выше 4 уровней).
        var v: View? = view
        var depth = 0
        while (depth < 4 && v != null && themeKeys.isEmpty()) {
            for (d in screenDescriptions) {
                val key = d.currentKey
                if (key >= 0 && ThemeColors.getStringName(key) != null && matches(d, v)) themeKeys.getOrPut(key) { ArrayList() }
            }
            v = v.parent as? View
            depth++
        }
        for (d in screenDescriptions) themeKeys[d.currentKey]?.add(d)
    }

    private fun matches(d: ThemeDescription, target: View): Boolean {
        val v = d.customView
        val classes = d.customListClasses
        if (classes == null || classes.isEmpty()) return v === target
        if (classes.none { it != null && it.isInstance(target) }) return false
        if (v == null || v === target) return true
        var p = target.parent
        while (p != null) {
            if (p === v) return true
            p = p.parent
        }
        return false
    }

    // ------------------------------------------------------------------ интерфейс

    fun rebuild() {
        val context = context
        list.removeAllViews()
        val scopes = ScopeOption.forTarget(target)
        val scope = EditMode.currentScope(scopes)
        val style = target.style()

        list.addView(HeaderCell(context).apply { setText(target.title) })

        // Уровень сохранения
        list.addView(TextSettingsCell(context).apply {
            setTextAndValue(EngineStrings.get("save_to"), scope.label, true)
            background = Theme.getSelectorDrawable(false)
            setOnClickListener { chooseScope(scopes) }
        })

        if (!target.isAuto) {
            // Свойства заготовки, сгруппированные по частям
            val partTitles = parts.associate { it.id to it.title }
            var lastPart: String? = "\u0000"
            props.forEachIndexed { i, prop ->
                if (prop.part != lastPart) {
                    list.addView(HeaderCell(context).apply { setText(prop.part?.let { partTitles[it] ?: it } ?: target.title) })
                    lastPart = prop.part
                }
                list.addView(propRow(context, prop, style, scopes, scope, divider = i + 1 < props.size))
            }
            if (parts.any { it.hideable }) {
                list.addView(TextSettingsCell(context).apply {
                    setText(EngineStrings.get("hide_by_tap"), false)
                    setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4))
                    background = Theme.getSelectorDrawable(false)
                    setOnClickListener { EditMode.startHideByTap() }
                })
            }
            list.addView(TextInfoPrivacyCell(context).apply { setText(EngineStrings.get("long_press_reset")) })
        } else {
            list.addView(TextInfoPrivacyCell(context).apply { setText(EngineStrings.get("no_spec")) })
            target.parent()?.let { parent ->
                list.addView(TextSettingsCell(context).apply {
                    setText(EngineStrings.get("parent") + " (" + parent.title + ")", false)
                    background = Theme.getSelectorDrawable(false)
                    setOnClickListener { EditMode.select(parent) }
                })
            }
        }

        // Цвета темы
        if (themeKeys.isNotEmpty()) {
            list.addView(HeaderCell(context).apply { setText(EngineStrings.get("theme_colors")) })
            var n = 0
            for ((key, descriptions) in themeKeys) {
                list.addView(themeColorRow(context, key, descriptions, ++n < themeKeys.size))
            }
            list.addView(TextInfoPrivacyCell(context).apply { setText(EngineStrings.get("theme_colors_info")) })
        }

        if (!target.isAuto) {
            list.addView(TextSettingsCell(context).apply {
                setText(EngineStrings.get("reset_element"), false)
                setTextColor(Theme.getColor(Theme.key_text_RedRegular))
                background = Theme.getSelectorDrawable(false)
                setOnClickListener { NextEngine.edit { it.clearElement(scope.scope, target.elementId) } }
            })
        }
    }

    private fun propRow(
        context: Context,
        prop: EditorSpec.Prop,
        style: ResolvedElement,
        scopes: List<ScopeOption>,
        scope: ScopeOption,
        divider: Boolean,
    ): View {
        val source = style.sourceOf(prop.part, prop.key)
        val from = if (source == null) EngineStrings.get("not_set") else EngineStrings.format("from", ScopeOption.labelFor(source, scopes))
        val row: View = when (prop.type) {
            EditorSpec.Prop.Type.BOOL -> {
                val value = style.getBool(prop.part, prop.key, prop.defaultBool)
                TextCheckCell(context).apply {
                    setTextAndValueAndCheck(prop.title, from, value, false, divider)
                    setOnClickListener { write(scope, prop, !value) }
                }
            }
            EditorSpec.Prop.Type.COLOR -> {
                val color = style.getColor(prop.part, prop.key)
                TextColorCell(context).apply {
                    setTextAndColor(prop.title + " · " + from, color ?: 0, divider)
                    setOnClickListener {
                        ColorPickerDialog.show(context, prop.title, color ?: 0xFF808080.toInt()) { picked -> write(scope, prop, picked) }
                    }
                }
            }
        }
        row.background = Theme.getSelectorDrawable(false)
        row.setOnLongClickListener {
            write(scope, prop, null)
            true
        }
        return row
    }

    private fun write(scope: ScopeOption, prop: EditorSpec.Prop, value: Any?) {
        NextEngine.edit { it.set(scope.scope, target.elementId, prop.part, prop.key, value) }
    }

    private fun themeColorRow(context: Context, key: Int, descriptions: List<ThemeDescription>, divider: Boolean): View {
        val name = ThemeColors.getStringName(key)
        val color = descriptions.firstOrNull()?.getSetColor() ?: Theme.getColor(key)
        return TextColorCell(context).apply {
            setTextAndColor(if (EngineColors.has(key)) "$name ✎" else name, color, divider)
            background = Theme.getSelectorDrawable(false)
            setOnClickListener { ColorPickerDialog.show(context, name, color) { picked -> setThemeColor(key, name, picked) } }
            setOnLongClickListener {
                setThemeColor(key, name, null)
                true
            }
        }
    }

    private fun setThemeColor(key: Int, name: String, argb: Int?) {
        val themeKey = EngineColors.themeKey()
        NextEngine.edit { it.setThemeColor(themeKey, name, argb) }
        // Применяем сразу ко всем описаниям экрана с этим ключом, не трогая файл темы.
        for (d in screenDescriptions) {
            if (d.currentKey == key) {
                try {
                    d.setColor(d.getSetColor(), false, false)
                } catch (e: Throwable) {
                    FileLog.e(e)
                }
            }
        }
        try {
            Theme.applyCommonTheme()
            Theme.applyDialogsTheme()
            Theme.applyChatTheme(false, false)
        } catch (e: Throwable) {
            FileLog.e(e)
        }
        rebuild()
    }

    private fun chooseScope(scopes: List<ScopeOption>) {
        val items: Array<CharSequence> = scopes.map { it.label }.toTypedArray()
        AlertDialog.Builder(context)
            .setTitle(EngineStrings.get("save_to"))
            .setItems(items) { _, which ->
                EditMode.setPreferredScope(scopes[which])
                rebuild()
            }
            .show()
    }
}
