package org.nextgram.engine.edit;

import android.graphics.RectF;
import android.view.View;

import org.telegram.messenger.UserConfig;

import java.util.Collections;
import java.util.List;

/**
 * Автоматический режим для элементов без заготовки. Telegram почти всё рисует
 * сам на канвасе, поэтому внутрь такого View без заготовки не залезть —
 * доступны только цвета темы, найденные через ThemeDescription экрана.
 */
public final class AutoSpec implements EditorSpec {

    public static final AutoSpec INSTANCE = new AutoSpec();

    private AutoSpec() {
    }

    @Override
    public boolean handles(View view) {
        return true;
    }

    @Override
    public String elementId(View view) {
        return "auto:" + view.getClass().getName();
    }

    @Override
    public String title(View view) {
        String name = view.getClass().getSimpleName();
        return name.isEmpty() ? view.getClass().getName() : name;
    }

    @Override
    public long dialogId(View view) {
        return 0;
    }

    @Override
    public int account(View view) {
        return UserConfig.selectedAccount;
    }

    @Override
    public List<Part> parts(View view) {
        return Collections.emptyList();
    }

    @Override
    public boolean partBounds(View view, String partId, RectF out) {
        return false;
    }

    @Override
    public List<Prop> props(View view) {
        return Collections.emptyList();
    }

    @Override
    public void apply(View view) {
        view.invalidate();
    }
}
