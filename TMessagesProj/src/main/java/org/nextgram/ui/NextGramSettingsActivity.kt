package org.nextgram.ui

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import org.nextgram.engine.EngineStrings
import org.nextgram.engine.edit.ColorPickerDialog
import org.nextgram.engine.edit.EditMode
import org.nextgram.ghost.GhostMode
import org.nextgram.profiles.NextProfiles
import org.nextgram.profiles.NgProfile
import org.telegram.messenger.BuildConfig
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.ActionBar
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.ActionBar.Theme
import org.telegram.ui.Cells.HeaderCell
import org.telegram.ui.Cells.TextCheckCell
import org.telegram.ui.Cells.TextColorCell
import org.telegram.ui.Cells.TextInfoPrivacyCell
import org.telegram.ui.Cells.TextSettingsCell
import org.telegram.ui.Components.LayoutHelper

/** Настройки NextGram: интерфейс (NextEngine), режим призрака, профиль для своих. */
class NextGramSettingsActivity : BaseFragment() {

    private var list: LinearLayout? = null
    private var connecting = false

    override fun createView(context: Context): View {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back)
        actionBar.setAllowOverlayTitle(true)
        actionBar.setTitle("NextGram")
        actionBar.setActionBarMenuOnItemClick(object : ActionBar.ActionBarMenuOnItemClick() {
            override fun onItemClick(id: Int) {
                if (id == -1) finishFragment()
            }
        })

        val root = FrameLayout(context).apply { setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray)) }
        val l = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        list = l
        root.addView(ScrollView(context).apply {
            addView(l, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0))
        }, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT.toFloat()))
        fragmentView = root
        rebuild()
        return root
    }

    override fun onResume() {
        super.onResume()
        rebuild()
    }

    private fun rebuild() {
        val context = context ?: return
        val l = list ?: return
        l.removeAllViews()

        // ---------------- NextEngine
        header(l, s("ng_engine"))
        textRow(l, s("ng_engine_open"), null, false).apply {
            setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4))
            setOnClickListener {
                EditMode.enter(parentActivity)
                finishFragment()
            }
        }
        info(l, s("ng_engine_info"))

        // ---------------- Режим призрака
        header(l, s("ng_ghost"))
        val ghost = GhostMode.get(GhostMode.KEY_ENABLED)
        check(l, s("ng_ghost_enabled"), GhostMode.KEY_ENABLED, enabled = true, divider = true)
        check(l, s("ng_ghost_read"), GhostMode.KEY_READ, ghost, true)
        check(l, s("ng_ghost_online"), GhostMode.KEY_ONLINE, ghost, true)
        check(l, s("ng_ghost_typing"), GhostMode.KEY_TYPING, ghost, true)
        check(l, s("ng_ghost_stories"), GhostMode.KEY_STORIES, ghost, false)
        info(l, s("ng_ghost_info"))

        // ---------------- Профиль NextGram
        header(l, s("ng_profile"))
        when {
            !NextProfiles.isConfigured -> info(l, s("ng_profile_off"))
            !NextProfiles.isConnected(currentAccount) -> {
                textRow(l, s(if (connecting) "ng_profile_connecting" else "ng_profile_connect"), null, false).apply {
                    setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4))
                    setOnClickListener { connect() }
                }
                info(l, EngineStrings.format("ng_profile_connect_info", BuildConfig.NG_BOT_USERNAME))
            }
            else -> {
                val p = NextProfiles.own(currentAccount)
                textRow(l, s("ng_profile_particles"), particlesName(p.particles), true).setOnClickListener { chooseParticles() }
                l.addView(TextColorCell(context).apply {
                    setTextAndColor(s("ng_profile_color"), p.particlesColor, true)
                    background = selector()
                    setOnClickListener {
                        ColorPickerDialog.show(context, s("ng_profile_color"), p.particlesColor) { picked ->
                            save(NextProfiles.own(currentAccount).apply { particlesColor = picked ?: 0xFFFFFFFF.toInt() })
                        }
                    }
                })
                textRow(l, s("ng_profile_density"), densityName(p.density), true).setOnClickListener { chooseDensity() }
                textRow(l, s("ng_profile_disconnect"), null, false).apply {
                    setTextColor(Theme.getColor(Theme.key_text_RedRegular))
                    setOnClickListener { NextProfiles.disconnect(currentAccount) { _, _ -> rebuild() } }
                }
                info(l, EngineStrings.format("ng_profile_connect_info", BuildConfig.NG_BOT_USERNAME))
            }
        }
    }

    // ------------------------------------------------------------------ действия

    private fun connect() {
        if (connecting) return
        connecting = true
        rebuild()
        NextProfiles.connect(currentAccount) { ok, error ->
            connecting = false
            if (!ok) toast(EngineStrings.format("ng_profile_error", error))
            rebuild()
        }
    }

    private fun chooseParticles() {
        val ids = NgProfile.PARTICLE_TYPES
        val items: Array<CharSequence> = (listOf(s("ng_off")) + ids.map { particlesName(it) }).toTypedArray()
        AlertDialog.Builder(context)
            .setTitle(s("ng_profile_particles"))
            .setItems(items) { _, which ->
                save(NextProfiles.own(currentAccount).apply { particles = if (which == 0) null else ids[which - 1] })
            }
            .show()
    }

    private fun chooseDensity() {
        val values = floatArrayOf(0.5f, 1f, 1.6f)
        val items: Array<CharSequence> = arrayOf(s("ng_density_low"), s("ng_density_mid"), s("ng_density_high"))
        AlertDialog.Builder(context)
            .setTitle(s("ng_profile_density"))
            .setItems(items) { _, which -> save(NextProfiles.own(currentAccount).apply { density = values[which] }) }
            .show()
    }

    private fun save(p: NgProfile) {
        NextProfiles.saveOwn(currentAccount, p) { ok, error ->
            if (!ok) toast(EngineStrings.format("ng_profile_error", error))
            rebuild()
        }
        rebuild()
    }

    private fun toast(text: String) {
        parentActivity?.let { Toast.makeText(it, text, Toast.LENGTH_LONG).show() }
    }

    // ------------------------------------------------------------------ ячейки

    private fun selector() = Theme.createSelectorWithBackgroundDrawable(
        Theme.getColor(Theme.key_windowBackgroundWhite), Theme.getColor(Theme.key_listSelector),
    )

    private fun header(l: LinearLayout, text: String) {
        l.addView(HeaderCell(l.context).apply {
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite))
            setText(text)
        })
    }

    private fun info(l: LinearLayout, text: String) {
        l.addView(TextInfoPrivacyCell(l.context).apply {
            setText(text)
            background = Theme.getThemedDrawableByKey(l.context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow)
        })
    }

    private fun textRow(l: LinearLayout, text: String, value: String?, divider: Boolean): TextSettingsCell =
        TextSettingsCell(l.context).apply {
            background = selector()
            if (value == null) setText(text, divider) else setTextAndValue(text, value, divider)
            l.addView(this)
        }

    private fun check(l: LinearLayout, text: String, key: String, enabled: Boolean, divider: Boolean) {
        l.addView(TextCheckCell(l.context).apply {
            background = selector()
            setTextAndCheck(text, GhostMode.get(key), divider)
            setEnabled(enabled, null)
            setOnClickListener {
                if (!enabled) return@setOnClickListener
                GhostMode.set(key, !GhostMode.get(key))
                rebuild()
            }
        })
    }

    private companion object {
        fun s(key: String) = EngineStrings.get(key)

        fun particlesName(type: String?) = if (type == null) s("ng_off") else s("ng_$type")

        fun densityName(d: Float) = when {
            d < 0.8f -> s("ng_density_low")
            d > 1.3f -> s("ng_density_high")
            else -> s("ng_density_mid")
        }
    }
}
