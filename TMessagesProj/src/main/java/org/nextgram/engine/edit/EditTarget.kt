package org.nextgram.engine.edit

import android.graphics.RectF
import android.view.View
import org.nextgram.engine.NextEngine
import org.nextgram.engine.core.ResolvedElement
import java.lang.ref.WeakReference

/** Выбранный в режиме редактирования элемент. View держим слабо: ячейки переиспользуются. */
class EditTarget internal constructor(view: View, val spec: EditorSpec, part: String? = null) {

    /** Выбранная видимая часть (время, текст, пузырь…) или null — элемент целиком. */
    var part: String? = part
        internal set

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
        var p = view()?.parent as? View
        while (p != null) {
            // Корень окна (DecorView) и контейнеры на весь экран выбирать бессмысленно.
            if (p.parent !is View) return null
            val root = p.rootView
            if (p.width.toLong() * p.height * 10 > root.width.toLong() * root.height * 9) return null
            // Пропускаем обёртки, у которых нечего менять.
            SpecRegistry.targetFor(p)?.let { return it }
            p = p.parent as? View
        }
        return null
    }

    fun partInfo(): EditorSpec.Part? {
        val v = view() ?: return null
        val id = part ?: return null
        return spec.parts(v).firstOrNull { it.id == id }
    }

    /** Границы выбранной части (или всего элемента) на экране. */
    fun screenBounds(out: RectF): Boolean {
        val v = view() ?: return false
        val loc = IntArray(2)
        v.getLocationOnScreen(loc)
        val id = part
        if (id != null && spec.partBounds(v, id, out)) {
            out.offset(loc[0].toFloat(), loc[1].toFloat())
            return true
        }
        out.set(loc[0].toFloat(), loc[1].toFloat(), (loc[0] + v.width).toFloat(), (loc[1] + v.height).toFloat())
        return true
    }
}
