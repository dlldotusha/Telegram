package org.nextgram.engine.edit

import android.graphics.RectF
import android.view.View
import org.nextgram.engine.NextEngine
import org.nextgram.engine.core.ResolvedElement
import java.lang.ref.WeakReference

/** Выбранный в режиме редактирования элемент. View держим слабо: ячейки переиспользуются. */
class EditTarget internal constructor(view: View, val spec: EditorSpec) {

    private val viewRef = WeakReference(view)
    val elementId: String = spec.elementId(view)
    val dialogId: Long = spec.dialogId(view)
    val account: Int = spec.account(view)
    val title: String = spec.title(view)

    /** Сам view, если он всё ещё показывает тот же элемент (ячейка могла уехать в пул). */
    fun view(): View? {
        val v = viewRef.get() ?: return null
        if (!v.isAttachedToWindow || !spec.handles(v) || spec.elementId(v) != elementId || spec.dialogId(v) != dialogId) return null
        return v
    }

    val isAuto: Boolean get() = spec === AutoSpec

    fun style(): ResolvedElement = NextEngine.resolve(account, dialogId, elementId)

    /** Родительский элемент — для авторежима, когда палец попал во вложенный view. */
    fun parent(): EditTarget? {
        val p = view()?.parent as? View ?: return null
        return SpecRegistry.targetFor(p)
    }

    /** Границы элемента на экране. */
    fun screenBounds(out: RectF): Boolean {
        val v = view() ?: return false
        val loc = IntArray(2)
        v.getLocationOnScreen(loc)
        out.set(loc[0].toFloat(), loc[1].toFloat(), (loc[0] + v.width).toFloat(), (loc[1] + v.height).toFloat())
        return true
    }
}
