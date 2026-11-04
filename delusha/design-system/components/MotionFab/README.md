# MotionFab

Раскрытие FAB-меню «новый чат».

- Затемнение `scrim` до 32% пружиной `effects-slow`.
- Кнопка морфит из `shape-l` (16) в круг и перекрашивается `primary-container` → `primary`, карандаш поворачивается на 90° и сменяется крестиком (`spatial-fast`).
- Пункты вылетают снизу вверх с интервалом 35 мс: сдвиг 24 → 0, масштаб 0.6 → 1 от правого края, `spatial-default`.
- Закрытие быстрее и в обратном порядке (сверху вниз, 20 мс, `spatial-fast`): уходящее не должно задерживать.

**Android:** `DgFabMenu` (свой `FrameLayout` поверх `DialogsActivity`), пружины на `translationY`, `scaleX/Y`, `alpha` — только свойства RenderThread, без relayout.
