package org.nextgram.profiles

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

/** Хук для ProfileActivity: слой частиц поверх фона шапки. */
object ProfileHooks {

    /**
     * Вызывается сразу после добавления фона шапки (topView) в разметку профиля.
     * Слой добавляется следующим — поверх фона, но под аватаркой и именем.
     */
    @JvmStatic
    fun attach(parent: ViewGroup, account: Int, userId: Long, height: ParticlesView.HeightProvider) {
        if (userId <= 0 || !NextProfiles.isConfigured) return
        val view = ParticlesView(parent.context, height).apply { visibility = View.GONE }
        parent.addView(view, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        NextProfiles.get(account, userId) { profile, _ -> view.setProfile(profile) }
    }
}
