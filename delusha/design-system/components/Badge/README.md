# Badge

Счётчик непрочитанного. `primary` — обычный чат, `muted` (`surface-container-highest`) — чат без звука, `mention` (`tertiary`) — упоминание или ответ тебе, точка — новое в разделе без числа.

**Что даёт код:** число (больше 999 показывается как «999+»), тип.

**Android:** рисуется в `DialogCell` через `DgBadge.draw(canvas, …)`, без лишних View.
