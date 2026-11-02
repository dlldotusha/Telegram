package org.telegram.custom.edit;

import android.content.Context;
import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import org.telegram.custom.CustomColors;
import org.telegram.custom.CustomStrings;
import org.telegram.custom.Customize;
import org.telegram.custom.core.ResolvedElement;
import org.telegram.messenger.FileLog;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeColors;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextColorCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Свойства выбранного элемента: уровень сохранения, свойства заготовки по частям,
 * цвета темы, которые касаются этого элемента.
 *
 * Тап по строке — изменить на выбранном уровне, долгий тап — сбросить на нём.
 * Под каждой строкой видно, с какого уровня пришло текущее значение.
 */
final class EditorSheet extends BottomSheet {

    private final EditTarget target;
    private final LinearLayout list;
    /** Цвета темы под этим элементом: ключ → все описания экрана с этим ключом. */
    private final LinkedHashMap<Integer, ArrayList<ThemeDescription>> themeKeys = new LinkedHashMap<>();
    private final ArrayList<ThemeDescription> screenDescriptions = new ArrayList<>();
    /** Части и свойства берём при открытии: ячейка может уехать из списка, пока панель открыта. */
    private final List<EditorSpec.Part> parts;
    private final List<EditorSpec.Prop> props;

    EditorSheet(Context context, EditTarget target) {
        super(context, false);
        this.target = target;
        setApplyBottomPadding(false);
        View view = target.view();
        parts = view == null ? new ArrayList<>() : target.spec.parts(view);
        props = view == null ? new ArrayList<>() : target.spec.props(view);

        collectThemeColors();

        ScrollView scroll = new ScrollView(context);
        list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0));
        setCustomView(scroll);
        setOnDismissListener(d -> EditMode.onSheetDismissed(this));
        rebuild();
    }

    // ------------------------------------------------------------------ данные

    private void collectThemeColors() {
        BaseFragment fragment = LaunchActivity.getLastFragment();
        View view = target.view();
        if (fragment == null || view == null) {
            return;
        }
        ArrayList<ThemeDescription> all;
        try {
            all = fragment.getThemeDescriptions();
        } catch (Throwable e) {
            FileLog.e(e);
            return;
        }
        if (all == null) {
            return;
        }
        screenDescriptions.addAll(all);
        // Ищем описания для самого элемента; если их нет — поднимаемся к предкам (не выше 4 уровней).
        View v = view;
        for (int depth = 0; depth < 4 && v != null && themeKeys.isEmpty(); depth++) {
            for (ThemeDescription d : all) {
                if (d != null && matches(d, v)) {
                    int key = d.getCurrentKey();
                    if (key < 0 || ThemeColors.getStringName(key) == null) {
                        continue;
                    }
                    ArrayList<ThemeDescription> l = themeKeys.get(key);
                    if (l == null) {
                        l = new ArrayList<>();
                        themeKeys.put(key, l);
                    }
                }
            }
            ViewParent p = v.getParent();
            v = p instanceof View ? (View) p : null;
        }
        for (ThemeDescription d : all) {
            if (d != null) {
                ArrayList<ThemeDescription> l = themeKeys.get(d.getCurrentKey());
                if (l != null) {
                    l.add(d);
                }
            }
        }
    }

    private static boolean matches(ThemeDescription d, View target) {
        View v = d.getCustomView();
        Class[] classes = d.getCustomListClasses();
        if (classes == null || classes.length == 0) {
            return v == target;
        }
        boolean classMatch = false;
        for (Class c : classes) {
            if (c != null && c.isInstance(target)) {
                classMatch = true;
                break;
            }
        }
        if (!classMatch) {
            return false;
        }
        if (v == null || v == target) {
            return true;
        }
        for (ViewParent p = target.getParent(); p != null; p = p.getParent()) {
            if (p == v) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ интерфейс

    void rebuild() {
        Context context = getContext();
        list.removeAllViews();
        List<ScopeOption> scopes = ScopeOption.forTarget(target);
        ScopeOption scope = EditMode.currentScope(scopes);
        ResolvedElement style = target.style();

        HeaderCell title = new HeaderCell(context);
        title.setText(target.title);
        list.addView(title);

        // Уровень сохранения
        TextSettingsCell scopeCell = new TextSettingsCell(context);
        scopeCell.setTextAndValue(CustomStrings.get("save_to"), scope.label, true);
        scopeCell.setBackground(Theme.getSelectorDrawable(false));
        scopeCell.setOnClickListener(v -> chooseScope(scopes));
        list.addView(scopeCell);

        // Свойства заготовки, сгруппированные по частям
        if (!target.isAuto()) {
            Map<String, String> partTitles = new LinkedHashMap<>();
            boolean anyHideable = false;
            for (EditorSpec.Part p : parts) {
                partTitles.put(p.id, p.title);
                anyHideable |= p.hideable;
            }
            String lastPart = "\u0000";
            for (int i = 0; i < props.size(); i++) {
                EditorSpec.Prop prop = props.get(i);
                String part = prop.part;
                if (part == null ? lastPart != null : !part.equals(lastPart)) {
                    HeaderCell h = new HeaderCell(context);
                    h.setText(part == null ? target.title : partTitles.containsKey(part) ? partTitles.get(part) : part);
                    list.addView(h);
                    lastPart = part;
                }
                boolean divider = i + 1 < props.size();
                list.addView(propRow(context, prop, style, scopes, scope, divider));
            }
            if (anyHideable) {
                TextSettingsCell hide = new TextSettingsCell(context);
                hide.setText(CustomStrings.get("hide_by_tap"), false);
                hide.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4));
                hide.setBackground(Theme.getSelectorDrawable(false));
                hide.setOnClickListener(v -> EditMode.startHideByTap());
                list.addView(hide);
            }
            TextInfoPrivacyCell hint = new TextInfoPrivacyCell(context);
            hint.setText(CustomStrings.get("long_press_reset"));
            list.addView(hint);
        } else {
            TextInfoPrivacyCell info = new TextInfoPrivacyCell(context);
            info.setText(CustomStrings.get("no_spec"));
            list.addView(info);
            final EditTarget parent = target.parent();
            if (parent != null) {
                TextSettingsCell up = new TextSettingsCell(context);
                up.setText(CustomStrings.get("parent") + " (" + parent.title + ")", false);
                up.setBackground(Theme.getSelectorDrawable(false));
                up.setOnClickListener(v -> EditMode.select(parent));
                list.addView(up);
            }
        }

        // Цвета темы
        if (!themeKeys.isEmpty()) {
            HeaderCell h = new HeaderCell(context);
            h.setText(CustomStrings.get("theme_colors"));
            list.addView(h);
            int n = 0;
            for (Map.Entry<Integer, ArrayList<ThemeDescription>> e : themeKeys.entrySet()) {
                list.addView(themeColorRow(context, e.getKey(), e.getValue(), ++n < themeKeys.size()));
            }
            TextInfoPrivacyCell info = new TextInfoPrivacyCell(context);
            info.setText(CustomStrings.get("theme_colors_info"));
            list.addView(info);
        }

        if (!target.isAuto()) {
            TextSettingsCell reset = new TextSettingsCell(context);
            reset.setText(CustomStrings.get("reset_element"), false);
            reset.setTextColor(Theme.getColor(Theme.key_text_RedRegular));
            reset.setBackground(Theme.getSelectorDrawable(false));
            reset.setOnClickListener(v -> Customize.edit(c -> c.clearElement(scope.scope, target.elementId)));
            list.addView(reset);
        }
    }

    private View propRow(Context context, EditorSpec.Prop prop, ResolvedElement style, List<ScopeOption> scopes, ScopeOption scope, boolean divider) {
        String source = style.sourceOf(prop.part, prop.key);
        String from = source == null ? CustomStrings.get("not_set") : CustomStrings.format("from", ScopeOption.labelFor(source, scopes));
        View row;
        if (prop.type == EditorSpec.Prop.Type.BOOL) {
            boolean value = style.getBool(prop.part, prop.key, prop.defaultBool);
            TextCheckCell cell = new TextCheckCell(context);
            cell.setTextAndValueAndCheck(prop.title, from, value, false, divider);
            cell.setOnClickListener(v -> write(scope, prop, !value));
            row = cell;
        } else {
            Integer color = style.getColor(prop.part, prop.key);
            TextColorCell cell = new TextColorCell(context);
            cell.setTextAndColor(prop.title + " · " + from, color == null ? 0 : color, divider);
            cell.setOnClickListener(v -> ColorPickerDialog.show(getContext(), prop.title, color == null ? 0xFF808080 : color, picked -> write(scope, prop, picked)));
            row = cell;
        }
        row.setBackground(Theme.getSelectorDrawable(false));
        row.setOnLongClickListener(v -> {
            write(scope, prop, null);
            return true;
        });
        return row;
    }

    private void write(ScopeOption scope, EditorSpec.Prop prop, Object value) {
        Customize.edit(c -> c.set(scope.scope, target.elementId, prop.part, prop.key, value));
        // Сбросили на уровне, а значение всё ещё приходит с более общего — для BOOL это ожидаемо,
        // пользователь видит источник в строке «из: …».
    }

    private View themeColorRow(Context context, int key, ArrayList<ThemeDescription> descriptions, boolean divider) {
        String name = ThemeColors.getStringName(key);
        int color = descriptions.isEmpty() ? Theme.getColor(key) : descriptions.get(0).getSetColor();
        boolean overridden = CustomColors.has(key);
        TextColorCell cell = new TextColorCell(context);
        cell.setTextAndColor(overridden ? name + " ✎" : name, color, divider);
        cell.setBackground(Theme.getSelectorDrawable(false));
        cell.setOnClickListener(v -> ColorPickerDialog.show(getContext(), name, color, picked -> setThemeColor(key, name, picked)));
        cell.setOnLongClickListener(v -> {
            setThemeColor(key, name, null);
            return true;
        });
        return cell;
    }

    private void setThemeColor(int key, String name, Integer argb) {
        final String themeKey = CustomColors.themeKey();
        Customize.edit(c -> c.setThemeColor(themeKey, name, argb));
        // Применяем сразу ко всем описаниям экрана с этим ключом, не трогая файл темы.
        for (ThemeDescription d : screenDescriptions) {
            if (d != null && d.getCurrentKey() == key) {
                try {
                    d.setColor(d.getSetColor(), false, false);
                } catch (Throwable e) {
                    FileLog.e(e);
                }
            }
        }
        try {
            Theme.applyCommonTheme();
            Theme.applyDialogsTheme();
            Theme.applyChatTheme(false, false);
        } catch (Throwable e) {
            FileLog.e(e);
        }
        rebuild();
    }

    private void chooseScope(List<ScopeOption> scopes) {
        CharSequence[] items = new CharSequence[scopes.size()];
        for (int i = 0; i < items.length; i++) {
            items[i] = scopes.get(i).label;
        }
        new AlertDialog.Builder(getContext())
                .setTitle(CustomStrings.get("save_to"))
                .setItems(items, (dialog, which) -> {
                    EditMode.setPreferredScope(scopes.get(which));
                    rebuild();
                })
                .show();
    }
}
