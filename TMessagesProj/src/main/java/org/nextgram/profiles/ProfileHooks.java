package org.nextgram.profiles;

import android.view.ViewGroup;
import android.widget.FrameLayout;

/** Хук для ProfileActivity: слой частиц поверх фона шапки. */
public final class ProfileHooks {

    private ProfileHooks() {
    }

    /**
     * Вызывается сразу после добавления фона шапки (topView) в разметку профиля.
     * Слой добавляется следующим — то есть поверх фона, но под аватаркой и именем.
     */
    public static void attach(ViewGroup parent, int account, long userId, ParticlesView.HeightProvider height) {
        if (userId <= 0 || !NextProfiles.isConfigured()) {
            return;
        }
        ParticlesView view = new ParticlesView(parent.getContext(), height);
        view.setVisibility(ParticlesView.GONE);
        parent.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        NextProfiles.get(account, userId, (profile, error) -> view.setProfile(profile));
    }
}
