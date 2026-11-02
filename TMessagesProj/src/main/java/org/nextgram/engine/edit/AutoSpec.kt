package org.nextgram.engine.edit

import android.graphics.RectF
import android.view.View
import org.telegram.messenger.UserConfig

/**
 * Автоматический режим для элементов без заготовки. Telegram почти всё рисует
 * сам на канвасе, поэтому внутрь такого View без заготовки не залезть —
 * доступны только цвета темы, найденные через ThemeDescription экрана.
 */
object AutoSpec : EditorSpec {
    override fun handles(view: View) = true
    override fun elementId(view: View) = "auto:" + view.javaClass.name
    override fun title(view: View): String = view.javaClass.simpleName.ifEmpty { view.javaClass.name }
    override fun dialogId(view: View) = 0L
    override fun account(view: View) = UserConfig.selectedAccount
    override fun parts(view: View) = emptyList<EditorSpec.Part>()
    override fun partBounds(view: View, partId: String, out: RectF) = false
    override fun props(view: View) = emptyList<EditorSpec.Prop>()
    override fun apply(view: View) = view.invalidate()
}
