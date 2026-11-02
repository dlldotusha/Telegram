package org.nextgram.ui

import android.view.View
import android.widget.Toast
import org.nextgram.engine.EngineStrings
import org.nextgram.engine.edit.ColorPickerDialog
import org.nextgram.engine.edit.EditMode
import org.nextgram.ghost.GhostMode
import org.nextgram.profiles.NextProfiles
import org.nextgram.profiles.NgProfile
import org.telegram.messenger.BuildConfig
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.AlertDialog
import org.telegram.ui.Cells.TextColorCell
import org.telegram.ui.Components.UItem
import org.telegram.ui.Components.UniversalAdapter
import org.telegram.ui.Components.UniversalFragment

/**
 * Настройки NextGram на родных компонентах Telegram (UniversalFragment/UItem) —
 * выглядят и перекрашиваются так же, как остальные экраны настроек.
 */
class NextGramSettingsActivity : UniversalFragment() {

    private var connecting = false

    override fun getTitle(): CharSequence = "NextGram"

    override fun fillItems(items: ArrayList<UItem>, adapter: UniversalAdapter) {
        // ---------------- NextEngine
        items += UItem.asHeader(s("ng_engine"))
        items += UItem.asButton(ID_EDIT, R.drawable.msg_customize, s("ng_engine_open")).accent()
        items += UItem.asShadow(s("ng_engine_info"))

        // ---------------- Режим призрака
        val ghost = GhostMode.get(GhostMode.KEY_ENABLED)
        items += UItem.asHeader(s("ng_ghost"))
        items += UItem.asCheck(ID_GHOST, s("ng_ghost_enabled")).setChecked(ghost)
        items += UItem.asCheck(ID_GHOST_READ, s("ng_ghost_read")).setChecked(GhostMode.get(GhostMode.KEY_READ)).setEnabled(ghost)
        items += UItem.asCheck(ID_GHOST_ONLINE, s("ng_ghost_online")).setChecked(GhostMode.get(GhostMode.KEY_ONLINE)).setEnabled(ghost)
        items += UItem.asCheck(ID_GHOST_TYPING, s("ng_ghost_typing")).setChecked(GhostMode.get(GhostMode.KEY_TYPING)).setEnabled(ghost)
        items += UItem.asCheck(ID_GHOST_STORIES, s("ng_ghost_stories")).setChecked(GhostMode.get(GhostMode.KEY_STORIES)).setEnabled(ghost)
        items += UItem.asShadow(s("ng_ghost_info"))

        // ---------------- Профиль NextGram
        items += UItem.asHeader(s("ng_profile"))
        when {
            !NextProfiles.isConfigured -> items += UItem.asShadow(s("ng_profile_off"))
            !NextProfiles.isConnected(currentAccount) -> {
                items += UItem.asButton(ID_CONNECT, R.drawable.msg_customize, s(if (connecting) "ng_profile_connecting" else "ng_profile_connect")).accent()
                items += UItem.asShadow(EngineStrings.format("ng_profile_connect_info", BuildConfig.NG_BOT_USERNAME))
            }
            else -> {
                val p = NextProfiles.own(currentAccount)
                items += UItem.asButton(ID_PARTICLES, s("ng_profile_particles"), particlesName(p.particles))
                if (p.particles != null) {
                    val cell = TextColorCell(context).apply { setTextAndColor(s("ng_profile_color"), p.particlesColor, true) }
                    items += UItem.asCustom(ID_COLOR, cell)
                    val chosen = when {
                        p.density < 0.8f -> 0
                        p.density > 1.3f -> 2
                        else -> 1
                    }
                    items += UItem.asSlideView(arrayOf(s("ng_density_low"), s("ng_density_mid"), s("ng_density_high")), chosen) { idx ->
                        save(NextProfiles.own(currentAccount).apply { density = DENSITIES[idx] })
                    }
                }
                items += UItem.asShadow(EngineStrings.format("ng_profile_connect_info", BuildConfig.NG_BOT_USERNAME))
                items += UItem.asButton(ID_DISCONNECT, s("ng_profile_disconnect")).red()
                items += UItem.asShadow(null)
            }
        }
    }

    override fun onClick(item: UItem, view: View, position: Int, x: Float, y: Float) {
        when (item.id) {
            ID_EDIT -> {
                EditMode.enter(parentActivity)
                finishFragment()
            }
            ID_GHOST -> toggle(GhostMode.KEY_ENABLED)
            ID_GHOST_READ -> toggle(GhostMode.KEY_READ)
            ID_GHOST_ONLINE -> toggle(GhostMode.KEY_ONLINE)
            ID_GHOST_TYPING -> toggle(GhostMode.KEY_TYPING)
            ID_GHOST_STORIES -> toggle(GhostMode.KEY_STORIES)
            ID_CONNECT -> connect()
            ID_PARTICLES -> chooseParticles()
            ID_COLOR -> {
                val p = NextProfiles.own(currentAccount)
                ColorPickerDialog.show(context, s("ng_profile_color"), p.particlesColor) { picked ->
                    save(NextProfiles.own(currentAccount).apply { particlesColor = picked ?: 0xFFFFFFFF.toInt() })
                }
            }
            ID_DISCONNECT -> NextProfiles.disconnect(currentAccount) { _, _ -> update() }
        }
    }

    override fun onLongClick(item: UItem, view: View, position: Int, x: Float, y: Float): Boolean = false

    override fun onResume() {
        super.onResume()
        update()
    }

    private fun update() {
        (listView?.getAdapter() as? UniversalAdapter)?.update(true)
    }

    private fun toggle(key: String) {
        if (key != GhostMode.KEY_ENABLED && !GhostMode.get(GhostMode.KEY_ENABLED)) return
        GhostMode.set(key, !GhostMode.get(key))
        update()
    }

    private fun connect() {
        if (connecting) return
        connecting = true
        update()
        NextProfiles.connect(currentAccount) { ok, error ->
            connecting = false
            if (!ok) toast(EngineStrings.format("ng_profile_error", error))
            update()
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

    private fun save(p: NgProfile) {
        NextProfiles.saveOwn(currentAccount, p) { ok, error ->
            if (!ok) toast(EngineStrings.format("ng_profile_error", error))
            update()
        }
        update()
    }

    private fun toast(text: String) {
        parentActivity?.let { Toast.makeText(it, text, Toast.LENGTH_LONG).show() }
    }

    private companion object {
        const val ID_EDIT = 1
        const val ID_GHOST = 10
        const val ID_GHOST_READ = 11
        const val ID_GHOST_ONLINE = 12
        const val ID_GHOST_TYPING = 13
        const val ID_GHOST_STORIES = 14
        const val ID_CONNECT = 20
        const val ID_PARTICLES = 21
        const val ID_COLOR = 22
        const val ID_DISCONNECT = 23
        val DENSITIES = floatArrayOf(0.5f, 1f, 1.6f)

        fun s(key: String) = EngineStrings.get(key)

        fun particlesName(type: String?) = if (type == null) s("ng_off") else s("ng_$type")
    }
}
