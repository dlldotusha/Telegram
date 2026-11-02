package org.nextgram.ui;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.nextgram.engine.EngineStrings;
import org.nextgram.engine.edit.ColorPickerDialog;
import org.nextgram.engine.edit.EditMode;
import org.nextgram.ghost.GhostMode;
import org.nextgram.profiles.NextProfiles;
import org.nextgram.profiles.NgProfile;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextColorCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;

/** Настройки NextGram: интерфейс (NextEngine), режим призрака, профиль для своих. */
public class NextGramSettingsActivity extends BaseFragment {

    private LinearLayout list;
    private boolean connecting;

    private static String s(String key) {
        return EngineStrings.get(key);
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("NextGram");
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        FrameLayout root = new FrameLayout(context);
        root.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        ScrollView scroll = new ScrollView(context);
        list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0));
        root.addView(scroll, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        fragmentView = root;
        rebuild();
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (list != null) {
            rebuild();
        }
    }

    private void rebuild() {
        Context context = getContext();
        if (context == null || list == null) {
            return;
        }
        list.removeAllViews();

        // ---------------- NextEngine
        header(context, s("ng_engine"));
        TextSettingsCell edit = textRow(context, s("ng_engine_open"), null, false);
        edit.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4));
        edit.setOnClickListener(v -> {
            EditMode.enter(getParentActivity());
            finishFragment();
        });
        info(context, s("ng_engine_info"));

        // ---------------- Режим призрака
        header(context, s("ng_ghost"));
        boolean ghost = GhostMode.get(GhostMode.KEY_ENABLED);
        check(context, s("ng_ghost_enabled"), GhostMode.KEY_ENABLED, true, true);
        check(context, s("ng_ghost_read"), GhostMode.KEY_READ, ghost, true);
        check(context, s("ng_ghost_online"), GhostMode.KEY_ONLINE, ghost, true);
        check(context, s("ng_ghost_typing"), GhostMode.KEY_TYPING, ghost, true);
        check(context, s("ng_ghost_stories"), GhostMode.KEY_STORIES, ghost, false);
        info(context, s("ng_ghost_info"));

        // ---------------- Профиль NextGram
        header(context, s("ng_profile"));
        if (!NextProfiles.isConfigured()) {
            info(context, s("ng_profile_off"));
        } else if (!NextProfiles.isConnected(currentAccount)) {
            TextSettingsCell connect = textRow(context, connecting ? s("ng_profile_connecting") : s("ng_profile_connect"), null, false);
            connect.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4));
            connect.setOnClickListener(v -> connect());
            info(context, EngineStrings.format("ng_profile_connect_info", BuildConfig.NG_BOT_USERNAME));
        } else {
            NgProfile p = NextProfiles.own(currentAccount);
            textRow(context, s("ng_profile_particles"), particlesName(p.particles), true).setOnClickListener(v -> chooseParticles());
            TextColorCell color = new TextColorCell(context);
            color.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            color.setTextAndColor(s("ng_profile_color"), p.particlesColor, true);
            color.setBackground(Theme.createSelectorWithBackgroundDrawable(Theme.getColor(Theme.key_windowBackgroundWhite), Theme.getColor(Theme.key_listSelector)));
            color.setOnClickListener(v -> ColorPickerDialog.show(context, s("ng_profile_color"), p.particlesColor, picked -> {
                NgProfile n = NextProfiles.own(currentAccount);
                n.particlesColor = picked == null ? 0xFFFFFFFF : picked;
                save(n);
            }));
            list.addView(color);
            textRow(context, s("ng_profile_density"), densityName(p.density), true).setOnClickListener(v -> chooseDensity());
            TextSettingsCell off = textRow(context, s("ng_profile_disconnect"), null, false);
            off.setTextColor(Theme.getColor(Theme.key_text_RedRegular));
            off.setOnClickListener(v -> NextProfiles.disconnect(currentAccount, (ok, err) -> rebuild()));
            info(context, EngineStrings.format("ng_profile_connect_info", BuildConfig.NG_BOT_USERNAME));
        }
    }

    // ------------------------------------------------------------------ действия

    private void connect() {
        if (connecting) {
            return;
        }
        connecting = true;
        rebuild();
        NextProfiles.connect(currentAccount, (ok, error) -> {
            connecting = false;
            if (!Boolean.TRUE.equals(ok)) {
                toast(EngineStrings.format("ng_profile_error", error));
            }
            rebuild();
        });
    }

    private void chooseParticles() {
        String[] ids = NgProfile.PARTICLE_TYPES;
        CharSequence[] items = new CharSequence[ids.length + 1];
        items[0] = s("ng_off");
        for (int i = 0; i < ids.length; i++) {
            items[i + 1] = particlesName(ids[i]);
        }
        new AlertDialog.Builder(getContext())
                .setTitle(s("ng_profile_particles"))
                .setItems(items, (d, which) -> {
                    NgProfile n = NextProfiles.own(currentAccount);
                    n.particles = which == 0 ? null : ids[which - 1];
                    save(n);
                })
                .show();
    }

    private void chooseDensity() {
        final float[] values = {0.5f, 1f, 1.6f};
        CharSequence[] items = {s("ng_density_low"), s("ng_density_mid"), s("ng_density_high")};
        new AlertDialog.Builder(getContext())
                .setTitle(s("ng_profile_density"))
                .setItems(items, (d, which) -> {
                    NgProfile n = NextProfiles.own(currentAccount);
                    n.density = values[which];
                    save(n);
                })
                .show();
    }

    private void save(NgProfile p) {
        NextProfiles.saveOwn(currentAccount, p, (ok, error) -> {
            if (!Boolean.TRUE.equals(ok)) {
                toast(EngineStrings.format("ng_profile_error", error));
            }
            rebuild();
        });
        rebuild();
    }

    private void toast(String text) {
        if (getParentActivity() != null) {
            Toast.makeText(getParentActivity(), text, Toast.LENGTH_LONG).show();
        }
    }

    private static String particlesName(String type) {
        if (type == null) {
            return s("ng_off");
        }
        return s("ng_" + type);
    }

    private static String densityName(float d) {
        return d < 0.8f ? s("ng_density_low") : d > 1.3f ? s("ng_density_high") : s("ng_density_mid");
    }

    // ------------------------------------------------------------------ ячейки

    private void header(Context context, String text) {
        HeaderCell h = new HeaderCell(context);
        h.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        h.setText(text);
        list.addView(h);
    }

    private void info(Context context, String text) {
        TextInfoPrivacyCell cell = new TextInfoPrivacyCell(context);
        cell.setText(text);
        cell.setBackground(Theme.getThemedDrawableByKey(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
        list.addView(cell);
    }

    private TextSettingsCell textRow(Context context, String text, String value, boolean divider) {
        TextSettingsCell cell = new TextSettingsCell(context);
        cell.setBackground(Theme.createSelectorWithBackgroundDrawable(Theme.getColor(Theme.key_windowBackgroundWhite), Theme.getColor(Theme.key_listSelector)));
        if (value == null) {
            cell.setText(text, divider);
        } else {
            cell.setTextAndValue(text, value, divider);
        }
        list.addView(cell);
        return cell;
    }

    private void check(Context context, String text, String key, boolean enabled, boolean divider) {
        TextCheckCell cell = new TextCheckCell(context);
        cell.setBackground(Theme.createSelectorWithBackgroundDrawable(Theme.getColor(Theme.key_windowBackgroundWhite), Theme.getColor(Theme.key_listSelector)));
        cell.setTextAndCheck(text, GhostMode.get(key), divider);
        cell.setEnabled(enabled, null);
        cell.setOnClickListener(v -> {
            if (!enabled) {
                return;
            }
            GhostMode.set(key, !GhostMode.get(key));
            rebuild();
        });
        list.addView(cell);
    }
}
