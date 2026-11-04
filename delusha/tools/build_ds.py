import json, os, sys, datetime
sys.path.insert(0, os.path.dirname(__file__))
from gen_shapes import css as shapes_css, svgpath
import motion
import expressive

OUT = os.path.join(os.path.dirname(__file__), 'ds', 'project')
def w(path, text):
    p = os.path.join(OUT, path)
    os.makedirs(os.path.dirname(p), exist_ok=True)
    open(p, 'w').write(text)

# ---------------------------------------------------------------- tokens
colors = json.load(open(os.path.join(os.path.dirname(__file__), 'colors.json')))

def st(name, size, lh, wt, ls=0, usage=None, sample=None, opsz=None):
    d = {"name": name, "fontSize": f"{size}px", "lineHeight": f"{lh}px", "fontWeight": wt, "letterSpacing": f"{ls}px"}
    if usage: d["usage"] = usage
    if sample: d["sample"] = sample
    if opsz: d["opticalSize"] = opsz
    return d

tokens = {
 "name": "DelushaGram", "version": 1,
 "color": {"themes": [{"id": "light", "name": "Светлая"}, {"id": "dark", "name": "Тёмная"}, {"id": "amoled", "name": "AMOLED"}],
           "note": "Значения по умолчанию от сида «Сирень». На Android 12+ все роли пересчитываются из обоев (Monet), имена ролей не меняются.",
           "tokens": colors},
 "type": {
  "families": {"sans": "\"Roboto Flex\", Roboto, system-ui, sans-serif", "icons": "\"Material Symbols Rounded\""},
  "groups": [
   {"name": "Экспрессив", "family": "sans", "styles": [
     st("hero-title", 40, 44, 850, -1, "Заголовок экрана-раздела в развёрнутой шапке (Delusha, Настройки). Ширина wdth 140.", "Delusha", 48),
     st("screen-title", 34, 38, 800, -0.8, "Большой заголовок главных экранов (Чаты, Звонки). Ширина wdth 125.", "Чаты", 40),
     st("profile-name", 26, 30, 800, -0.5, "Имя на экране профиля. Ширина wdth 120.", "Аня Соколова", 32)]},
   {"name": "Display и Headline", "family": "sans", "styles": [
     st("display-large", 57, 64, 400, -0.25), st("display-medium", 45, 52, 400), st("display-small", 36, 44, 400),
     st("headline-large", 32, 40, 400), st("headline-medium", 28, 36, 400), st("headline-small", 24, 32, 400, 0, "Заголовки диалогов и листов.")]},
   {"name": "Title", "family": "sans", "styles": [
     st("title-large", 22, 28, 400, 0, "Заголовок свёрнутой шапки."), st("title-medium", 16, 24, 500, 0.15), st("title-small", 14, 20, 500, 0.1)]},
   {"name": "Body", "family": "sans", "styles": [
     st("body-large", 16, 24, 400, 0.5), st("body-medium", 14, 20, 400, 0.25, "Подписи строк, текст листов."), st("body-small", 12, 16, 400, 0.4)]},
   {"name": "Label", "family": "sans", "styles": [
     st("label-large", 14, 20, 500, 0.1, "Кнопки, вкладки панели."), st("label-medium", 12, 16, 500, 0.5, "Чипы, бейджи, метки «удалено»."), st("label-small", 11, 16, 500, 0.5)]},
   {"name": "Чат", "family": "sans", "styles": [
     st("message", 16, 22, 400, 0, "Текст сообщения. Меняется ползунком размера текста в настройках, по умолчанию 16.", "Приду к восьми"),
     st("message-meta", 11, 14, 500, 0.3, "Время и галочки внутри пузыря.", "9:35"),
     st("chat-name", 16, 22, 650, 0, "Имя в строке списка чатов.", "Мама"),
     st("chat-preview", 14, 20, 400, 0.1, "Превью последнего сообщения.", "Купи хлеба по дороге")]},
  ]},
 "spacing": {"tokens": [
   {"name": "space-2xs", "value": "2px", "usage": "Зазор между соединёнными элементами: группы кнопок, пункты списка, пузыри одной серии."},
   {"name": "space-xs", "value": "4px", "usage": "Иконка и текст внутри чипа."},
   {"name": "space-s", "value": "8px", "usage": "Между иконками в тулбаре, внутри строки."},
   {"name": "space-m", "value": "12px", "usage": "Аватар и текст в строке чата, отступ пузырей от края."},
   {"name": "space-l", "value": "16px", "usage": "Боковой отступ экрана, внутренние поля карточек."},
   {"name": "space-xl", "value": "24px", "usage": "Между секциями настроек."},
   {"name": "space-2xl", "value": "32px", "usage": "Над крупным заголовком."},
   {"name": "space-3xl", "value": "48px", "usage": "Пустые состояния."}]},
 "radius": {"tokens": [
   {"name": "shape-none", "value": "0px", "usage": "Полноэкранные медиа."},
   {"name": "shape-xs", "value": "4px", "usage": "Внутренние углы пунктов в группе списка."},
   {"name": "shape-s", "value": "8px", "usage": "Внутренние углы группы кнопок, нажатая кнопка S."},
   {"name": "shape-bubble-joined", "value": "6px", "usage": "Углы пузыря, которыми он стыкуется с соседним из той же серии."},
   {"name": "shape-m", "value": "12px", "usage": "Выбранная иконка-кнопка, превью медиа, меню."},
   {"name": "shape-l", "value": "16px", "usage": "FAB, нажатая кнопка M."},
   {"name": "shape-l-plus", "value": "20px", "usage": "Внешние углы групп списка, пузыри."},
   {"name": "shape-xl", "value": "28px", "usage": "Нижние листы, диалоги, шапка чата снизу."},
   {"name": "shape-xl-plus", "value": "32px", "usage": "Плавающая панель, крупные кнопки."},
   {"name": "shape-2xl", "value": "48px", "usage": "Кнопка XL, медиа в профиле."},
   {"name": "shape-full", "value": "9999px", "usage": "Пилюли: кнопки в покое, поиск, поле ввода, чипы выбора."}]},
 "shadow": {"tokens": [
   {"name": "elevation-1", "value": "0 1px 2px rgba(0,0,0,0.3), 0 1px 3px 1px rgba(0,0,0,0.15)", "usage": "Карточки при перетаскивании."},
   {"name": "elevation-2", "value": "0 1px 2px rgba(0,0,0,0.3), 0 2px 6px 2px rgba(0,0,0,0.15)", "usage": "Шапка при прокрутке под ней."},
   {"name": "elevation-3", "value": "0 1px 3px rgba(0,0,0,0.3), 0 4px 8px 3px rgba(0,0,0,0.15)", "usage": "Плавающая панель, FAB, меню сообщения."},
   {"name": "elevation-4", "value": "0 2px 3px rgba(0,0,0,0.3), 0 6px 10px 4px rgba(0,0,0,0.15)", "usage": "FAB при наведении."},
   {"name": "elevation-5", "value": "0 4px 4px rgba(0,0,0,0.3), 0 8px 12px 6px rgba(0,0,0,0.15)", "usage": "Перетаскиваемый элемент."}]},
 "spring": {"note": "Пружины Material 3 Expressive для androidx SpringAnimation. Пространственные для позиции, размера и формы; эффекты для цвета и прозрачности.", "tokens": [
   {"name": "spatial-fast", "value": "damping 0.6 stiffness 800", "usage": "Морфинг формы кнопок и аватаров при нажатии."},
   {"name": "spatial-default", "value": "damping 0.8 stiffness 380", "usage": "Открытие меню, переход выбранной вкладки, FAB-меню."},
   {"name": "spatial-slow", "value": "damping 0.8 stiffness 200", "usage": "Переход между экранами, развёртывание шапки."},
   {"name": "effects-fast", "value": "damping 1 stiffness 3800", "usage": "Слой нажатия, смена иконки."},
   {"name": "effects-default", "value": "damping 1 stiffness 1600", "usage": "Смена цвета выбранного."},
   {"name": "effects-slow", "value": "damping 1 stiffness 800", "usage": "Затемнение под листом."},
   {"name": "spatial-calm", "value": "damping 1 stiffness 380", "usage": "То же, что spatial-default, когда включено «Меньше движения»: без отскока."}]},
 "size": {"tokens": [
   {"name": "touch-target", "value": "48px", "usage": "Минимальная зона касания любого элемента."},
   {"name": "icon", "value": "24px", "usage": "Иконки в тулбарах и строках."},
   {"name": "icon-s", "value": "20px", "usage": "Иконки в чипах и кнопках."},
   {"name": "avatar-s", "value": "40px", "usage": "Шапка чата, упоминания."},
   {"name": "avatar-m", "value": "52px", "usage": "Список чатов."},
   {"name": "avatar-xl", "value": "124px", "usage": "Профиль."},
   {"name": "row-chat", "value": "72px", "usage": "Высота строки чата (обычная плотность). Компактная 60px."},
   {"name": "toolbar", "value": "64px", "usage": "Плавающая панель навигации."},
   {"name": "fab", "value": "56px", "usage": "FAB."},
   {"name": "composer", "value": "52px", "usage": "Поле ввода и кнопка отправки."},
   {"name": "bubble-max", "value": "80%", "usage": "Максимальная ширина пузыря от ширины чата."}]},
 "blur": {"note": "Размытие под полупрозрачными поверхностями. На Android 12+ RenderEffect, ниже собственный blur Telegram.", "tokens": [
   {"name": "blur-glass", "value": "24px", "usage": "Нижняя панель, шапка чата, поле ввода поверх `surface-glass`."},
   {"name": "blur-scrim", "value": "10px", "usage": "Фон под меню сообщения, листами и меню аккаунта."}]},
 "state": {"tokens": [
   {"name": "state-hover", "value": "0.08", "usage": "Слой наведения (мышь, стилус)."},
   {"name": "state-focus", "value": "0.10", "usage": "Слой фокуса."},
   {"name": "state-pressed", "value": "0.10", "usage": "Слой нажатия поверх цвета содержимого."},
   {"name": "state-dragged", "value": "0.16", "usage": "Перетаскивание."},
   {"name": "disabled-content", "value": "0.38", "usage": "Прозрачность текста и иконок выключенного элемента."},
   {"name": "disabled-container", "value": "0.12", "usage": "Прозрачность заливки выключенного элемента."}]},
}
w('tokens.json', json.dumps(tokens, ensure_ascii=False, indent=1))

# ---------------------------------------------------------------- bundle.css
SPR = "cubic-bezier(.34,1.56,.64,1)"
bundle = """@import url("https://fonts.googleapis.com/css2?family=Roboto+Flex:opsz,wdth,wght@8..144,25..151,100..1000&display=swap");
@import url("https://fonts.googleapis.com/css2?family=Material+Symbols+Rounded:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200&display=block");
/* DelushaGram components. Web stand-ins for the Android views; springs approximated with an overshooting cubic-bezier. */
:root{--dg-spring:%(spr)s;--dg-ease:cubic-bezier(.2,0,0,1)}
body{margin:0;background:var(--surface);color:var(--on-surface);font-family:var(--font-sans);font-size:14px;line-height:20px;-webkit-font-smoothing:antialiased}
*{box-sizing:border-box}
.dg-ic{font-family:"Material Symbols Rounded";font-weight:normal;font-style:normal;font-size:24px;line-height:1;letter-spacing:normal;text-transform:none;display:inline-block;white-space:nowrap;-webkit-font-smoothing:antialiased;font-variation-settings:"FILL" 0,"wght" 450,"GRAD" 0,"opsz" 24;user-select:none;flex:none}
.dg-ic.fill{font-variation-settings:"FILL" 1,"wght" 500,"GRAD" 0,"opsz" 24}
.dg-stage{padding:16px;display:flex;flex-wrap:wrap;gap:16px;align-items:center}
.dg-stage.col{flex-direction:column;align-items:stretch}
.dg-cap{font:500 11px/16px var(--font-sans);letter-spacing:.5px;color:var(--on-surface-variant);text-transform:uppercase}
.dg-phone{width:360px;max-width:100%%;background:var(--surface);border-radius:28px;overflow:hidden;position:relative;box-shadow:inset 0 0 0 1px var(--outline-variant)}
:focus-visible{outline:3px solid var(--focus-ring);outline-offset:2px}
@media (prefers-reduced-motion:reduce){*{transition-duration:0s!important;animation:none!important}}
""" % {"spr": SPR} + shapes_css() + """
/* Button */
.dg-btn{position:relative;overflow:hidden;border:0;cursor:pointer;display:inline-flex;align-items:center;justify-content:center;gap:8px;height:40px;padding:0 16px;border-radius:20px;font:500 14px/20px var(--font-sans);letter-spacing:.1px;transition:border-radius 380ms var(--dg-spring),background-color 200ms var(--dg-ease),color 200ms var(--dg-ease)}
.dg-btn::before{content:"";position:absolute;inset:0;background:currentColor;opacity:0;transition:opacity 120ms}
.dg-btn:hover::before{opacity:var(--state-hover)}.dg-btn:active::before{opacity:var(--state-pressed)}
.dg-btn .dg-ic{font-size:20px}
.dg-btn:active{border-radius:var(--shape-s)}
.dg-btn.xs{height:32px;padding:0 12px;border-radius:16px;font-size:13px}.dg-btn.xs:active{border-radius:var(--shape-s)}
.dg-btn.m{height:56px;padding:0 24px;border-radius:28px;font-size:16px;line-height:24px}.dg-btn.m .dg-ic{font-size:24px}.dg-btn.m:active{border-radius:var(--shape-l)}
.dg-btn.l{height:96px;padding:0 48px;border-radius:48px;font-size:24px;line-height:32px;font-weight:500}.dg-btn.l .dg-ic{font-size:32px}.dg-btn.l:active{border-radius:var(--shape-xl)}
.dg-btn.filled{background:var(--primary);color:var(--on-primary)}
.dg-btn.tonal{background:var(--secondary-container);color:var(--on-secondary-container)}
.dg-btn.outlined{background:transparent;color:var(--on-surface-variant);box-shadow:inset 0 0 0 1px var(--outline)}
.dg-btn.text{background:transparent;color:var(--primary);padding:0 12px}
.dg-btn.danger{background:var(--error);color:var(--on-error)}
.dg-btn.toggle[aria-pressed="false"]{background:var(--surface-container-highest);color:var(--on-surface-variant)}
.dg-btn.toggle[aria-pressed="true"]{background:var(--primary);color:var(--on-primary);border-radius:var(--shape-m)}
.dg-btn[disabled]{background:color-mix(in srgb,var(--on-surface) 12%%,transparent);color:color-mix(in srgb,var(--on-surface) 38%%,transparent);box-shadow:none;cursor:default}
.dg-btn[disabled]::before{display:none}
/* Button group (connected) */
.dg-bgroup{display:flex;gap:var(--space-2xs)}
.dg-bgroup .dg-btn{border-radius:var(--shape-s);flex:1}
.dg-bgroup .dg-btn:first-child{border-radius:20px var(--shape-s) var(--shape-s) 20px}
.dg-bgroup .dg-btn:last-child{border-radius:var(--shape-s) 20px 20px var(--shape-s)}
.dg-bgroup .dg-btn[aria-pressed="true"]{border-radius:20px}
.dg-bgroup.tall .dg-btn{height:64px;flex-direction:column;gap:2px;font-size:12px;line-height:16px}
.dg-bgroup.tall .dg-btn:first-child{border-radius:32px var(--shape-s) var(--shape-s) 32px}
.dg-bgroup.tall .dg-btn:last-child{border-radius:var(--shape-s) 32px 32px var(--shape-s)}
.dg-bgroup.tall .dg-btn[aria-pressed="true"]{border-radius:32px}
/* Icon button */
.dg-ib{position:relative;width:40px;height:40px;border:0;padding:0;border-radius:20px;display:inline-grid;place-items:center;background:transparent;color:var(--on-surface-variant);cursor:pointer;transition:border-radius 380ms var(--dg-spring),background-color 200ms}
.dg-ib::after{content:"";position:absolute;inset:-4px}
.dg-ib::before{content:"";position:absolute;inset:0;border-radius:inherit;background:currentColor;opacity:0}
.dg-ib:hover::before{opacity:var(--state-hover)}.dg-ib:active::before{opacity:var(--state-pressed)}
.dg-ib:active{border-radius:var(--shape-s)}
.dg-ib.filled{background:var(--primary);color:var(--on-primary)}
.dg-ib.tonal{background:var(--secondary-container);color:var(--on-secondary-container)}
.dg-ib.outlined{box-shadow:inset 0 0 0 1px var(--outline)}
.dg-ib[aria-pressed="true"]{border-radius:var(--shape-m);background:var(--primary);color:var(--on-primary)}
.dg-ib[aria-pressed="true"] .dg-ic{font-variation-settings:"FILL" 1,"wght" 500,"GRAD" 0,"opsz" 24}
.dg-ib.wide{width:52px}
/* FAB */
.dg-fab{position:relative;overflow:hidden;border:0;cursor:pointer;width:56px;height:56px;border-radius:var(--shape-l);background:var(--primary-container);color:var(--on-primary-container);display:inline-grid;place-items:center;box-shadow:var(--elevation-3);transition:border-radius 380ms var(--dg-spring)}
.dg-fab::before{content:"";position:absolute;inset:0;background:currentColor;opacity:0}
.dg-fab:active::before{opacity:var(--state-pressed)}
.dg-fab.l{width:96px;height:96px;border-radius:var(--shape-xl)}.dg-fab.l .dg-ic{font-size:36px}
.dg-fab.ext{width:auto;padding:0 20px 0 16px;display:inline-flex;gap:12px;font:500 16px/24px var(--font-sans)}
.dg-fabmenu{display:flex;flex-direction:column;align-items:flex-end;gap:4px}
.dg-fabmenu .item{height:56px;border-radius:28px;padding:0 24px 0 18px;display:inline-flex;align-items:center;gap:12px;background:var(--primary-container);color:var(--on-primary-container);font:500 16px/24px var(--font-sans);box-shadow:var(--elevation-3)}
.dg-fabmenu .close{width:56px;height:56px;border-radius:50%%;background:var(--primary);color:var(--on-primary);display:grid;place-items:center;margin-top:4px;box-shadow:var(--elevation-3)}
/* Glass */
.dg-glass{background:var(--surface-glass);-webkit-backdrop-filter:blur(var(--blur-glass)) saturate(1.6);backdrop-filter:blur(var(--blur-glass)) saturate(1.6)}
.dg-scrim{position:absolute;inset:0;background:color-mix(in srgb,var(--scrim) 24%%,transparent);-webkit-backdrop-filter:blur(var(--blur-scrim));backdrop-filter:blur(var(--blur-scrim))}
/* Bottom bar: account, search, new chat */
.dg-bottombar{display:flex;align-items:center;gap:10px}
.dg-bottombar .bar{flex:1;min-width:0;height:64px;border-radius:32px;display:flex;align-items:center;gap:10px;padding:0 18px 0 6px;box-shadow:var(--elevation-2);color:var(--on-surface-variant);font:400 16px/24px var(--font-sans);cursor:pointer;border:0}
.dg-bottombar .bar .dg-av{width:52px;height:52px;font-size:18px}
.dg-bottombar .bar .ph{flex:1;text-align:left}
.dg-bottombar .dg-fab{width:64px;height:64px;border-radius:var(--shape-l-plus);box-shadow:var(--elevation-2)}
/* Sheet */
.dg-sheet{background:var(--surface-container-low);border-radius:var(--shape-xl) var(--shape-xl) 0 0;padding:10px 16px 24px;display:flex;flex-direction:column;gap:14px;box-shadow:var(--elevation-3)}
.dg-sheet .handle{align-self:center;width:32px;height:4px;border-radius:2px;background:var(--on-surface-variant);opacity:.5}
.dg-tiles{display:grid;grid-template-columns:1fr 1fr 1fr;gap:6px}
.dg-tile{border:0;cursor:pointer;height:96px;border-radius:var(--shape-l-plus);background:var(--surface-container-high);color:var(--on-surface);display:flex;flex-direction:column;align-items:flex-start;justify-content:space-between;padding:14px;font:600 14px/18px var(--font-sans);text-align:left;transition:border-radius 380ms var(--dg-spring)}
.dg-tile:active{border-radius:var(--shape-m)}
.dg-tile .ti{width:40px;height:40px;display:grid;place-items:center;background:var(--secondary-container);color:var(--on-secondary-container)}
.dg-tile.t .ti{background:var(--tertiary-container);color:var(--on-tertiary-container)}
.dg-acc{display:flex;align-items:center;gap:12px;padding:8px 12px;border-radius:var(--shape-xs);background:var(--surface-container)}
.dg-acc:first-child{border-radius:var(--shape-l-plus) var(--shape-l-plus) var(--shape-xs) var(--shape-xs)}
.dg-acc:last-child{border-radius:var(--shape-xs) var(--shape-xs) var(--shape-l-plus) var(--shape-l-plus)}
.dg-acc .tx{flex:1;font:600 15px/20px var(--font-sans)}
.dg-acc .tx span{display:block;font:400 13px/18px var(--font-sans);color:var(--on-surface-variant)}
.dg-acc>.dg-ic{color:var(--primary)}
.dg-accs{display:flex;flex-direction:column;gap:var(--space-2xs)}
/* History carousel */
.dg-car{position:relative;height:200px;overflow:visible}
.dg-car .v{position:absolute;top:30px;width:240px;display:flex;flex-direction:column;align-items:flex-start;gap:6px;will-change:transform}
.dg-car .v .tm{font:600 12px/16px var(--font-sans);color:var(--on-surface);padding:4px 10px;border-radius:12px;background:var(--surface-glass);font-variant-numeric:tabular-nums}
.dg-car .v .dg-b{max-width:none;width:100%%;box-shadow:var(--elevation-2)}
.dg-b mark{background:var(--tertiary-container);color:var(--on-tertiary-container);border-radius:6px;padding:0 3px}
.dg-dots{display:flex;gap:6px;justify-content:center;align-items:center}
.dg-dots i{display:block;width:8px;height:8px;border-radius:4px;background:var(--on-surface-variant);opacity:.45}
.dg-dots i.on{width:24px;background:var(--primary);opacity:1}
/* Chat wallpaper */
.dg-wall{position:absolute;inset:0;background:radial-gradient(circle at 18%% 12%%,color-mix(in srgb,var(--primary-container) 75%%,transparent),transparent 46%%),radial-gradient(circle at 88%% 58%%,color-mix(in srgb,var(--tertiary-container) 70%%,transparent),transparent 50%%),radial-gradient(circle at 30%% 92%%,color-mix(in srgb,var(--secondary-container) 80%%,transparent),transparent 45%%),var(--surface)}
.dg-wall i{position:absolute;display:block;background:var(--on-surface);opacity:.05}
.dg-appbar.small.dg-glass{background:var(--surface-glass)}
.dg-field.dg-glass,.dg-search.dg-glass{background:var(--surface-glass)}
/* Floating toolbar (legacy tabs) */
.dg-navwrap{display:flex;align-items:center;justify-content:center;gap:10px}
.dg-toolbar{height:64px;border-radius:32px;background:var(--surface-container-highest);box-shadow:var(--elevation-3);display:flex;align-items:center;gap:2px;padding:0 8px}
.dg-tb{border:0;background:transparent;cursor:pointer;height:48px;min-width:48px;border-radius:24px;display:flex;align-items:center;justify-content:center;gap:6px;color:var(--on-surface-variant);padding:0 12px;font:600 14px/20px var(--font-sans);transition:background-color 200ms,padding 380ms var(--dg-spring)}
.dg-tb[aria-current="page"]{background:var(--secondary-container);color:var(--on-secondary-container);padding:0 18px 0 14px}
.dg-tb[aria-current="page"] .dg-ic{font-variation-settings:"FILL" 1,"wght" 500,"GRAD" 0,"opsz" 24}
.dg-tb .lbl{display:none}.dg-tb[aria-current="page"] .lbl{display:inline}
.dg-tb .dot{position:absolute;margin:-18px 0 0 18px;min-width:16px;height:16px;border-radius:8px;background:var(--error);color:var(--on-error);font:600 10px/16px var(--font-sans);padding:0 4px}
.dg-tb{position:relative}
/* Top app bar */
.dg-appbar{padding:8px 4px 16px;display:flex;flex-direction:column;gap:4px;background:var(--surface)}
.dg-appbar .row{display:flex;align-items:center;gap:4px;min-height:48px}
.dg-appbar .grow{flex:1}
.dg-appbar h1{margin:8px 12px 0;font:800 34px/38px var(--font-sans);letter-spacing:-.8px;font-variation-settings:"wdth" 125,"opsz" 40}
.dg-appbar h1.hero{font-size:40px;line-height:44px;font-weight:850;letter-spacing:-1px;font-variation-settings:"wdth" 140,"opsz" 48}
.dg-appbar .sub{margin:0 12px;color:var(--on-surface-variant);font:400 14px/20px var(--font-sans)}
.dg-appbar.small{padding:4px;flex-direction:row;align-items:center;background:var(--surface-container);border-radius:0 0 var(--shape-xl) var(--shape-xl)}
.dg-appbar.small .ttl{flex:1;min-width:0;font:500 22px/28px var(--font-sans)}
.dg-appbar.small .ttl small{display:block;font:500 12.5px/16px var(--font-sans);color:var(--primary)}
/* Search bar */
.dg-search{height:56px;border-radius:28px;background:var(--surface-container-highest);display:flex;align-items:center;gap:12px;padding:0 8px 0 16px;color:var(--on-surface-variant);font:400 16px/24px var(--font-sans)}
.dg-search .ph{flex:1}
/* Chips */
.dg-chips{display:flex;gap:6px;overflow-x:auto;scrollbar-width:none}
.dg-chip{flex:none;border:0;cursor:pointer;height:36px;padding:0 14px;display:inline-flex;align-items:center;gap:6px;border-radius:var(--shape-m);background:var(--surface-container);color:var(--on-surface-variant);font:600 13px/16px var(--font-sans);transition:border-radius 380ms var(--dg-spring),background-color 200ms}
.dg-chip[aria-pressed="true"]{background:var(--secondary-container);color:var(--on-secondary-container);border-radius:18px}
.dg-chip .n{font:700 11px/18px var(--font-sans);min-width:18px;height:18px;padding:0 5px;border-radius:9px;background:var(--primary);color:var(--on-primary);text-align:center}
/* List group */
.dg-list{display:flex;flex-direction:column;gap:var(--space-2xs)}
.dg-list .hd{font:700 12.5px/16px var(--font-sans);letter-spacing:.2px;color:var(--primary);padding:0 16px 6px}
.dg-li{display:flex;align-items:center;gap:14px;min-height:56px;padding:10px 16px;background:var(--surface-container);border-radius:var(--shape-xs)}
.dg-list .dg-li:nth-child(2){border-top-left-radius:var(--shape-l-plus);border-top-right-radius:var(--shape-l-plus)}
.dg-list .dg-li:last-child{border-bottom-left-radius:var(--shape-l-plus);border-bottom-right-radius:var(--shape-l-plus)}
.dg-li .lead{width:40px;height:40px;border-radius:14px;display:grid;place-items:center;background:var(--secondary-container);color:var(--on-secondary-container)}
.dg-li .lead.t{background:var(--tertiary-container);color:var(--on-tertiary-container)}
.dg-li .lead.e{background:var(--error-container);color:var(--on-error-container)}
.dg-li .tx{flex:1;min-width:0}
.dg-li .tx b{display:block;font:600 15px/20px var(--font-sans)}
.dg-li .tx span{display:block;font:400 13px/18px var(--font-sans);color:var(--on-surface-variant)}
.dg-li .val{font:500 14px/20px var(--font-sans);color:var(--primary)}
/* Switch */
.dg-sw{flex:none;appearance:none;-webkit-appearance:none;margin:0;cursor:pointer;width:52px;height:32px;border-radius:16px;background:var(--surface-container-highest);border:2px solid var(--outline);position:relative;transition:background-color 200ms}
.dg-sw::after{content:"";position:absolute;width:16px;height:16px;border-radius:50%%;background:var(--outline);top:6px;left:6px;transition:all 380ms var(--dg-spring)}
.dg-sw:active::after{width:28px;height:28px;top:0;left:0}
.dg-sw:checked{background:var(--primary);border-color:var(--primary)}
.dg-sw:checked::after{width:24px;height:24px;top:2px;left:22px;background:var(--on-primary)}
.dg-sw:checked:active::after{width:28px;height:28px;top:0;left:20px}
.dg-sw::before{content:"check";font-family:"Material Symbols Rounded";position:absolute;left:25px;top:3px;font-size:18px;line-height:20px;color:var(--primary);z-index:1;opacity:0;transition:opacity 150ms;font-variation-settings:"wght" 700}
.dg-sw:checked::before{opacity:1}
/* Avatar */
.dg-av{flex:none;position:relative;display:inline-grid;place-items:center;width:52px;height:52px;font:700 18px/1 var(--font-sans);background:var(--av-bg,var(--primary-container));color:var(--av-fg,var(--on-primary-container));border-radius:50%%}
.dg-av.s{width:40px;height:40px;font-size:16px}.dg-av.xl{width:124px;height:124px;font-size:44px}
.dg-av.t{--av-bg:var(--tertiary-container);--av-fg:var(--on-tertiary-container)}
.dg-av.sc{--av-bg:var(--secondary-container);--av-fg:var(--on-secondary-container)}
.dg-av.e{--av-bg:var(--error-container);--av-fg:var(--on-error-container)}
.dg-avwrap{position:relative;display:inline-block;line-height:0}
.dg-avwrap .on{position:absolute;right:1px;bottom:1px;width:14px;height:14px;border-radius:50%%;background:var(--online);box-shadow:0 0 0 3px var(--surface)}
.dg-avwrap.story{padding:3px;border-radius:50%%;background:conic-gradient(var(--primary),var(--tertiary),var(--primary))}
.dg-avwrap.story .dg-av{box-shadow:0 0 0 2px var(--surface)}
.dg-morph{animation:dg-morph 9s ease-in-out infinite}
/* Badge */
.dg-badge{display:inline-grid;place-items:center;min-width:22px;height:22px;padding:0 7px;border-radius:11px;background:var(--primary);color:var(--on-primary);font:700 12px/1 var(--font-sans);font-variant-numeric:tabular-nums}
.dg-badge.muted{background:var(--surface-container-highest);color:var(--on-surface-variant)}
.dg-badge.mention{background:var(--tertiary);color:var(--on-tertiary);padding:0;width:22px}
.dg-badge.dot{min-width:8px;width:8px;height:8px;padding:0}
/* Chat row */
.dg-chat{display:flex;gap:12px;align-items:center;min-height:72px;padding:10px 12px;border-radius:24px;cursor:pointer;position:relative}
.dg-chat::before{content:"";position:absolute;inset:0;border-radius:inherit;background:var(--on-surface);opacity:0}
.dg-chat:hover::before{opacity:var(--state-hover)}.dg-chat:active::before{opacity:var(--state-pressed)}
.dg-chat.pinned{background:var(--surface-container-low)}
.dg-chat .mid{flex:1;min-width:0}
.dg-chat .nm{display:flex;align-items:center;gap:4px;font:650 16px/22px var(--font-sans)}
.dg-chat .nm .dg-ic{font-size:16px;color:var(--primary)}
.dg-chat .pv{display:flex;align-items:center;gap:4px;color:var(--on-surface-variant);font:400 14px/20px var(--font-sans);white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.dg-chat .pv .dg-ic{font-size:16px}
.dg-chat .pv em{font-style:normal;color:var(--primary);font-weight:600}
.dg-chat .pv .who{color:var(--on-surface);font-weight:500}
.dg-chat .rt{display:flex;flex-direction:column;align-items:flex-end;gap:6px;font:500 12px/16px var(--font-sans);color:var(--on-surface-variant);font-variant-numeric:tabular-nums}
.dg-chat .rt .dg-ic{font-size:16px;color:var(--primary)}
/* Message bubbles */
.dg-msgs{display:flex;flex-direction:column;gap:var(--space-2xs);padding:12px}
.dg-day{align-self:center;font:700 12px/16px var(--font-sans);padding:5px 12px;border-radius:12px;background:var(--surface-container-high);color:var(--on-surface-variant);margin:4px 0 8px}
.dg-b{max-width:var(--bubble-max);padding:8px 14px;font:400 16px/22px var(--font-sans);position:relative;overflow-wrap:anywhere}
.dg-b .meta{float:right;margin:6px 0 -4px 10px;font:500 11px/14px var(--font-sans);letter-spacing:.3px;opacity:.75;display:inline-flex;align-items:center;gap:2px;font-variant-numeric:tabular-nums}
.dg-b .meta .dg-ic{font-size:15px}
.dg-b.in{align-self:flex-start;background:var(--bubble-in);color:var(--on-bubble-in);border-radius:var(--shape-bubble-joined) var(--shape-l-plus) var(--shape-l-plus) var(--shape-bubble-joined)}
.dg-b.in.first{border-top-left-radius:var(--shape-l-plus)}
.dg-b.in.last{border-bottom-left-radius:var(--shape-l-plus);margin-bottom:8px}
.dg-b.out{align-self:flex-end;background:var(--bubble-out);color:var(--on-bubble-out);border-radius:var(--shape-l-plus) var(--shape-bubble-joined) var(--shape-bubble-joined) var(--shape-l-plus)}
.dg-b.out.first{border-top-right-radius:var(--shape-l-plus)}
.dg-b.out.last{border-bottom-right-radius:var(--shape-l-plus);margin-bottom:8px}
.dg-b.deleted{background:var(--bubble-deleted);color:var(--on-bubble-deleted)}
.dg-b .tag{display:flex;align-items:center;gap:4px;font:700 12px/16px var(--font-sans);letter-spacing:.3px;margin-bottom:2px}
.dg-b .tag .dg-ic{font-size:16px}
.dg-b.edited .tag{color:var(--tertiary)}
.dg-b .quote{display:block;margin:2px -6px 6px;padding:6px 10px;border-radius:12px;background:color-mix(in srgb,currentColor 12%%,transparent);font:400 14px/18px var(--font-sans)}
.dg-b .quote b{display:block;font-weight:700}
.dg-b .react{display:inline-flex;align-items:center;gap:4px;margin-top:6px;height:26px;padding:0 10px;border-radius:13px;background:var(--surface);color:var(--on-surface);font:700 12.5px/1 var(--font-sans)}
.dg-voice{display:flex;align-items:center;gap:10px;padding:8px 14px 8px 8px}
.dg-voice .play{width:40px;height:40px;background:var(--on-bubble-out);color:var(--bubble-out);display:grid;place-items:center}
.dg-voice .len{font:500 12px/16px var(--font-sans);opacity:.8;font-variant-numeric:tabular-nums}
/* Wavy progress */
.dg-wave{display:block;overflow:visible}
.dg-wave .trk{fill:none;stroke:currentColor;opacity:.3;stroke-width:4;stroke-linecap:round}
.dg-wave .act{fill:none;stroke:currentColor;stroke-width:4;stroke-linecap:round}
/* Loading indicator */
.dg-loading{width:48px;height:48px;display:inline-grid;place-items:center}
.dg-loading i{display:block;width:38px;height:38px;background:var(--primary);animation:dg-morph 4.5s cubic-bezier(.4,0,.2,1) infinite}
.dg-loading.contained{border-radius:50%%;background:var(--primary-container)}
.dg-loading.contained i{width:30px;height:30px;background:var(--on-primary-container)}
/* Composer */
.dg-composer{display:flex;align-items:flex-end;gap:8px;padding:8px 10px}
.dg-field{flex:1;min-height:52px;border-radius:26px;background:var(--surface-container-highest);display:flex;align-items:center;gap:4px;padding:0 4px;color:var(--on-surface-variant)}
.dg-field .ph{flex:1;font:400 16px/22px var(--font-sans);padding:15px 4px}
.dg-field .ph.typed{color:var(--on-surface)}
.dg-send{flex:none;width:52px;height:52px;border:0;cursor:pointer;background:var(--primary);color:var(--on-primary);display:grid;place-items:center}
/* Message menu */
.dg-menuwrap{display:flex;flex-direction:column;gap:8px;align-items:flex-start;width:280px}
.dg-reacts{display:flex;gap:2px;padding:6px;border-radius:28px;background:var(--surface-container-high);box-shadow:var(--elevation-3);font-size:24px;line-height:1}
.dg-reacts span{width:40px;height:40px;display:grid;place-items:center;border-radius:20px}
.dg-reacts span:first-child{background:var(--secondary-container)}
.dg-menu{display:flex;flex-direction:column;gap:var(--space-2xs);width:100%%;filter:drop-shadow(0 4px 8px rgba(0,0,0,.18))}
.dg-menu .grp{background:var(--surface-container-high);border-radius:var(--shape-xs);padding:4px}
.dg-menu .grp:first-child{border-top-left-radius:var(--shape-l-plus);border-top-right-radius:var(--shape-l-plus)}
.dg-menu .grp:last-child{border-bottom-left-radius:var(--shape-l-plus);border-bottom-right-radius:var(--shape-l-plus)}
.dg-menu .mi{display:flex;align-items:center;gap:12px;height:44px;padding:0 12px;border-radius:12px;font:500 14px/20px var(--font-sans);color:var(--on-surface)}
.dg-menu .mi .dg-ic{font-size:20px;color:var(--on-surface-variant)}
.dg-menu .mi.sel{background:var(--tertiary-container);color:var(--on-tertiary-container)}
.dg-menu .mi.sel .dg-ic{color:inherit}
.dg-menu .mi.danger,.dg-menu .mi.danger .dg-ic{color:var(--error)}
.dg-menu .mi .k{margin-left:auto;font:500 12px/16px var(--font-sans);color:var(--on-surface-variant)}
/* Snackbar */
.dg-snack{display:flex;align-items:center;gap:8px;min-height:48px;padding:4px 4px 4px 16px;border-radius:var(--shape-m);background:var(--inverse-surface);color:var(--inverse-on-surface);font:400 14px/20px var(--font-sans);box-shadow:var(--elevation-3);max-width:360px}
.dg-snack .tx{flex:1}
.dg-snack button{border:0;background:transparent;color:var(--inverse-primary);font:600 14px/20px var(--font-sans);height:40px;padding:0 12px;border-radius:20px;cursor:pointer}
/* Ghost pill */
.dg-ghost{display:inline-flex;align-items:center;gap:6px;height:32px;padding:0 12px 0 8px;border-radius:16px;background:var(--ghost);color:var(--on-ghost);font:700 12.5px/16px var(--font-sans);border:0;cursor:pointer}
.dg-ghost .dg-ic{font-size:18px}
"""
bundle = bundle.replace('%%', '%') + motion.MOTION_CSS + expressive.all_css()
w('components/bundle.css', bundle)

# ---------------------------------------------------------------- helpers for previews
def ic(name, fill=False):
    return f'<span class="dg-ic{" fill" if fill else ""}">{name}</span>'

def wave_svg(width=120, progress=0.45, amp=3.5, period=20):
    import math
    pts = []
    for x in range(0, width + 1, 2):
        pts.append(f"{x+2},{12 + amp*math.sin(2*math.pi*x/period):.2f}")
    full = "M" + " L".join(pts)
    cut = int(width * progress)
    act = "M" + " L".join(p for p in pts if float(p.split(',')[0]) - 2 <= cut)
    return (f'<svg class="dg-wave" width="{width+4}" height="24" viewBox="0 0 {width+4} 24" aria-hidden="true">'
            f'<path class="trk" d="M{cut+8},12 L{width+2},12"/><path class="act" d="{act}"/></svg>')

def card(group, height, body, script="", subtitle=None):
    sub = f' subtitle="{subtitle}"' if subtitle else ''
    return (f'<!-- @dsCard group="{group}" height={height}{sub} -->\n<!doctype html>\n<html>\n<head><meta charset="utf-8"><link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Roboto+Flex:opsz,wdth,wght@8..144,25..151,100..1000&display=swap"><link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Rounded:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200&display=block"></head>\n<body>\n'
            f'{body}\n' + (f'<script>{script}</script>\n' if script else '') + '</body>\n</html>\n')

TOGGLE_JS = """document.querySelectorAll('[data-toggle]').forEach(function(b){b.addEventListener('click',function(){var g=b.closest('[data-single]');if(g){g.querySelectorAll('[data-toggle]').forEach(function(x){x.setAttribute(x.hasAttribute('aria-current')?'aria-current':'aria-pressed',x===b?(x.hasAttribute('aria-current')?'page':'true'):(x.hasAttribute('aria-current')?'false':'false'))})}else{b.setAttribute('aria-pressed',b.getAttribute('aria-pressed')==='true'?'false':'true')}})});"""

comps = {}

comps['Button'] = (card('Действия', 260, f'''<div class="dg-stage col">
 <div class="dg-stage" style="padding:0"><button class="dg-btn filled">{ic("send")}Отправить</button><button class="dg-btn tonal">Ответить</button><button class="dg-btn outlined">Отмена</button><button class="dg-btn text">Подробнее</button><button class="dg-btn danger">{ic("delete")}Удалить</button><button class="dg-btn filled" disabled>Недоступно</button></div>
 <div class="dg-stage" style="padding:0"><button class="dg-btn filled xs">XS</button><button class="dg-btn filled">S</button><button class="dg-btn filled m">{ic("call")}M</button><button class="dg-btn tonal l">{ic("videocam")}L</button></div>
 <div class="dg-stage" style="padding:0"><span class="dg-cap">Переключатель:</span><button class="dg-btn toggle" data-toggle aria-pressed="true">{ic("visibility_off")}Призрак</button><button class="dg-btn toggle" data-toggle aria-pressed="false">{ic("notifications_off")}Без звука</button></div>
</div>''', TOGGLE_JS), '''# Button

Кнопка запускает одно действие; в покое это пилюля, при нажатии углы сжимаются (морфинг формы), отпускание возвращает их пружиной `spatial-fast`.

**Что даёт код:** текст (глагол: «Отправить», «Удалить»), необязательная иконка слева, стиль, размер, обработчик.

| Стиль | Когда |
|---|---|
| `filled` | Одно главное действие экрана. Не больше одной на экран. |
| `tonal` | Важное, но не главное: «Ответить», «Поделиться». |
| `outlined` | Альтернатива рядом с главной: «Отмена». |
| `text` | Ссылочное действие в диалоге или строке. |
| `danger` | Только необратимое: удаление, выход из аккаунта. |
| `toggle` | Включает и выключает состояние. Выбранная становится квадратнее и заливается `primary`. |

**Размеры:** XS 32, S 40 (по умолчанию), M 56, L 96. Зона касания не меньше `touch-target` даже у XS: добираем невидимыми полями.

**Удобство**
- Делай: подпись глаголом в 1–2 слова, иконку только если она добавляет смысл.
- Делай: опасная кнопка всегда справа в диалоге и с подтверждением.
- Не делай: две `filled` рядом; кнопки без подписи там, где действие неочевидно.

**Android:** `DgButton` (`ru.amoboba.delushagram.ui.components`), форма через `MaterialShapeDrawable` + `SpringAnimation` по `spatial-fast`, хаптика `KEYBOARD_TAP` на нажатие.
''')

comps['ButtonGroup'] = (card('Действия', 200, f'''<div class="dg-stage col" style="max-width:360px">
 <div class="dg-bgroup tall" data-single>
  <button class="dg-btn filled" data-toggle aria-pressed="true">{ic("chat_bubble",True)}Чат</button>
  <button class="dg-btn tonal" data-toggle aria-pressed="false">{ic("notifications_off")}Звук</button>
  <button class="dg-btn tonal" data-toggle aria-pressed="false">{ic("call")}Звонок</button>
  <button class="dg-btn tonal" data-toggle aria-pressed="false">{ic("videocam")}Видео</button>
 </div>
 <div class="dg-bgroup" data-single>
  <button class="dg-btn toggle" data-toggle aria-pressed="true">Медиа</button>
  <button class="dg-btn toggle" data-toggle aria-pressed="false">Файлы</button>
  <button class="dg-btn toggle" data-toggle aria-pressed="false">Голосовые</button>
  <button class="dg-btn toggle" data-toggle aria-pressed="false">Ссылки</button>
 </div>
</div>''', TOGGLE_JS), '''# ButtonGroup

Соединённая группа кнопок: 2–5 связанных действий или вариантов, стоящих вплотную с зазором `space-2xs`; внешние углы полные, внутренние `shape-s`.

**Что даёт код:** список кнопок (подпись + иконка), режим (действия или одиночный выбор), выбранный индекс.

- **Действия** (профиль: Чат, Звук, Звонок, Видео): высокий вариант 64, иконка над подписью. Главное действие `filled`, остальные `tonal`.
- **Выбор** (вкладки медиа в профиле, сегменты): выбранный элемент полностью скругляется и заливается `primary`; переход пружиной `spatial-default`.

**Удобство:** ширина делится поровну, каждая кнопка не уже `touch-target`. Больше пяти не ставь: превращай в чипы с прокруткой.

**Android:** `DgButtonGroup` (горизонтальный `LinearLayout`, сам пересчитывает углы детей при выборе).
''')

comps['IconButton'] = (card('Действия', 120, f'''<div class="dg-stage">
 <button class="dg-ib" aria-label="Назад">{ic("arrow_back")}</button>
 <button class="dg-ib" aria-label="Поиск">{ic("search")}</button>
 <button class="dg-ib tonal" aria-label="Звонок">{ic("call")}</button>
 <button class="dg-ib filled" aria-label="Отправить">{ic("send",True)}</button>
 <button class="dg-ib outlined" aria-label="Ещё">{ic("more_vert")}</button>
 <button class="dg-ib" data-toggle aria-pressed="true" aria-label="Закрепить">{ic("push_pin")}</button>
 <button class="dg-ib wide tonal" aria-label="Фильтры">{ic("tune")}</button>
</div>''', TOGGLE_JS), '''# IconButton

Кнопка-иконка 40×40 с зоной касания 48: для действий, понятных без подписи (назад, поиск, звонок, меню).

**Что даёт код:** иконку Material Symbols, `contentDescription` (обязателен, читается TalkBack и показывается подсказкой при долгом нажатии), стиль, состояние выбора.

- Стили: стандартная (в шапках), `tonal`, `filled`, `outlined`; ширины узкая 40 и широкая 52.
- Переключаемая: выбранная превращается из круга в скруглённый квадрат `shape-m`, иконка становится залитой (`FILL 1`).
- В шапке не больше трёх иконок справа, остальное в меню «Ещё».

**Android:** `DgIconButton`; иконки векторные из Material Symbols Rounded, два файла на иконку (контур и заливка).
''')

comps['Fab'] = (card('Действия', 300, f'''<div class="dg-stage" style="align-items:flex-end;gap:28px">
 <button class="dg-fab" aria-label="Новое сообщение">{ic("edit",True)}</button>
 <button class="dg-fab ext">{ic("edit",True)}Новый чат</button>
 <button class="dg-fab l" aria-label="Камера">{ic("photo_camera",True)}</button>
 <div class="dg-fabmenu">
  <span class="item">{ic("person_add")}Новый контакт</span>
  <span class="item">{ic("group")}Группа</span>
  <span class="item">{ic("campaign")}Канал</span>
  <span class="close">{ic("close")}</span>
 </div>
</div>'''), '''# Fab

Плавающая кнопка главного действия экрана: «новое сообщение» в списке чатов, «новый звонок» в звонках.

**Что даёт код:** иконку, подпись (для расширенной), обработчик; для FAB-меню список из 2–6 пунктов.

- Размеры: 56 (по умолчанию, `shape-l`), L 96 (`shape-xl`) для камеры и редких крупных действий, расширенная с подписью.
- FAB-меню: тап разворачивает пункты-пилюли снизу вверх пружиной `spatial-default`, сама кнопка морфит в круг с крестиком. Повторный тап или тап мимо закрывает.
- Стоит справа от плавающей панели, на одной линии с ней (см. FloatingToolbar).
- При прокрутке вниз расширенная сворачивается в обычную, при прокрутке вверх разворачивается.

**Android:** `DgFab`, `DgFabMenu`.
''')

comps['FloatingToolbar'] = (card('Навигация', 150, f'''<div class="dg-stage col" style="max-width:420px;position:relative;overflow:hidden;border-radius:28px;padding:0">
 <div style="padding:12px 8px 90px">
  <div class="dg-chat"><span class="dg-av e sh-clover4">М</span><div class="mid"><div class="nm">Мама</div><div class="pv">Купи хлеба по дороге</div></div><div class="rt">9:38<span class="dg-badge">2</span></div></div>
 </div>
 <div class="dg-bottombar" style="position:absolute;left:12px;right:12px;bottom:12px">
  <button class="bar dg-glass"><span class="dg-av sh-cookie9">Д</span>{ic("search")}<span class="ph">Поиск</span></button>
  <span class="dg-fabwrap"><button class="dg-fabx sh-cookie9" aria-label="Новый чат">{ic("edit",True)}</button></span>
 </div>
</div>'''), '''# FloatingToolbar

Нижняя панель главного экрана: свой аватар, поиск и «новый чат». Вкладок нет: всё редкое разложено туда, где оно нужно (см. «Где что живёт» в README).

**Что даёт код:** аватар текущего аккаунта, обработчики поиска и FAB.

- Пилюля 64 на `surface-glass` с размытием `blur-glass`: список чатов виден под ней, панель не съедает экран.
- Тап по аватару открывает твой профиль (там аккаунты, редактор профиля и настройки). Долгий тап сразу переключает на следующий аккаунт: вибрация, аватар морфит в форму другого аккаунта.
- Тап по пилюле поиска: она переезжает наверх и становится полем поиска (пружина `spatial-default`), клавиатура открывается сразу.
- FAB 68 в форме печеньки `primary`: новый чат (контакты, группа, канал, секретный чат). Долгий тап открывает FAB-меню.
- Отступ от низа 16 + системная навигация. При прокрутке вниз панель сжимается до аватара и FAB, при прокрутке вверх разворачивается.

**Удобство:** всё, что нужно каждый день, в зоне большого пальца; ничего не надо искать в боковом меню.

**Android:** `DgBottomBar` поверх `DialogsActivity`; боковое меню (`DrawerLayoutContainer`) отключается, его пункты разъезжаются по местам.
''')

comps['TopAppBar'] = (card('Навигация', 330, f'''<div class="dg-stage col" style="gap:20px">
 <div class="dg-phone"><div class="dg-appbar">
  <div class="row"><button class="dg-ib" aria-label="Назад">{ic("arrow_back")}</button><span class="grow"></span></div>
  <h1 class="dg-xxl" style="margin:8px 16px 0">Delusha</h1>
 </div></div>
 <div class="dg-phone" style="position:relative;height:96px"><div class="dg-wall"></div><div class="dg-appbar small dg-glass" style="position:relative">
  <button class="dg-ib" aria-label="Назад">{ic("arrow_back")}</button>
  <span class="dg-av s t sh-cookie12">А</span>
  <span class="ttl">Аня<small>в сети</small></span>
  <button class="dg-ib" aria-label="Звонок">{ic("call")}</button><button class="dg-ib" aria-label="Ещё">{ic("more_vert")}</button>
 </div></div>
</div>'''), '''# TopAppBar

Шапка экрана в двух видах: крупная (главные разделы, настройки) и компактная (чат, вложенные экраны).

**Главный экран заголовка не имеет**: сверху только плашка призрака, ниже закреплённые чаты.

**Крупная (вложенные разделы: Delusha, Мой профиль):** заголовок `dg-xxl` (64, ширина 151, вес 900) с кластером декоративных форм справа; при прокрутке сжимается в компактную с `title-large`, пружина `spatial-slow`.

**Компактная (чат):** назад, аватар `avatar-s`, имя и статус, до двух действий. Фон `surface-glass` с размытием `blur-glass`: сообщения уходят под шапку и просвечивают. Нижние углы `shape-xl`.

**Что даёт код:** заголовок, подзаголовок/статус, список действий, режим.

**Удобство:** статус «печатает…» и «в сети» окрашен `primary`, «был(а) недавно» `on-surface-variant`. Тап по шапке чата открывает профиль.

**Android:** обёртка над `ActionBar` Telegram: `DgTopAppBar` переопределяет отрисовку, не ломая меню и анимации поиска.
''')

comps['SearchBar'] = (card('Навигация', 100, f'''<div class="dg-stage col" style="max-width:380px"><div class="dg-search dg-glass">{ic("search")}<span class="ph">Поиск по чатам и сообщениям</span><button class="dg-ib" aria-label="Фильтры">{ic("tune")}</button></div></div>'''), '''# SearchBar

Поиск живёт в нижней панели, в пилюле рядом с аватаром. По тапу пилюля переезжает наверх и становится полем поиска.

**Что даёт код:** подсказку, обработчик фильтров. Тап раскрывает полноэкранный поиск с историей; фильтры (тип медиа, дата, чат) чипами под полем.

**Удобство:** поиск всегда под пальцем; в развёрнутом виде поле наверху, результаты под ним, клавиатура снизу.

**Android:** `DgSearchBar`, переход в существующий поиск `DialogsActivity` с общим элементом.
''')

comps['FolderChips'] = (card('Навигация', 90, f'''<div class="dg-stage"><div class="dg-chips" data-single>
 <button class="dg-chip" data-toggle aria-pressed="true">Все <span class="n">12</span></button>
 <button class="dg-chip" data-toggle aria-pressed="false">Личные</button>
 <button class="dg-chip" data-toggle aria-pressed="false">Группы <span class="n">3</span></button>
 <button class="dg-chip" data-toggle aria-pressed="false">Каналы</button>
 <button class="dg-chip" data-toggle aria-pressed="false">Боты</button>
</div></div>''', TOGGLE_JS), '''# FolderChips

Папки чатов чипами с горизонтальной прокруткой; выбранный скругляется полностью и заливается `secondary-container`.

**Что даёт код:** список папок, счётчик непрочитанных на каждой, выбранная.

- Свайп по списку чатов влево-вправо переключает папки, чипы едут следом.
- Долгий тап по чипу: прочитать всё, изменить, порядок.
- Счётчик скрывается, если в папке нет непрочитанных.
- Настройки вида (фича 38): чипы (по умолчанию) или вкладки с подчёркиванием-пилюлей; иконки папок слева от названия, только иконки или только текст; скрыть «Все чаты».

**Android:** перерисовка `FilterTabsView` в стиле чипов.
''')

comps['ListGroup'] = (card('Списки', 330, f'''<div class="dg-stage col" style="max-width:380px">
 <div class="dg-list">
  <span class="hd">Приватность</span>
  <label class="dg-li"><span class="lead t">{ic("visibility_off",True)}</span><span class="tx"><b>Режим призрака</b></span><input type="checkbox" class="dg-sw" checked aria-label="Режим призрака"></label>
  <label class="dg-li"><span class="lead">{ic("done_all")}</span><span class="tx"><b>Читать после ответа</b></span><input type="checkbox" class="dg-sw" checked aria-label="Читать после ответа"></label>
  <div class="dg-li"><span class="lead">{ic("schedule_send")}</span><span class="tx"><b>Офлайн через</b></span><span class="val">30 с</span></div>
 </div>
 <div class="dg-list">
  <span class="hd">История</span>
  <div class="dg-li"><span class="lead e">{ic("delete_history")}</span><span class="tx"><b>Удалённые сообщения</b><span>1 284 сообщения, 212 МБ</span></span>{ic("chevron_right")}</div>
 </div>
</div>'''), '''# ListGroup

Группа строк настроек: строки идут с зазором `space-2xs`, у группы общие внешние углы `shape-l-plus`, внутренние `shape-xs`. Заменяет списки Telegram с разделителями.

**Что даёт код:** заголовок группы (необязателен), строки. Строка: ведущая иконка в тональной плашке, заголовок, подпись (необязательна), хвост: переключатель, значение `primary` или шеврон.

- Ведущая плашка: `secondary-container` обычные пункты, `tertiary-container` функции форка, `error-container` опасные или разрушительные.
- Тап по всей строке переключает переключатель, не только по нему.
- Подпись под заголовком только для значения (30 с, 212 МБ, «Полные»), никогда не для объяснения.

**Удобство:** не больше 6 строк в группе; вложенные экраны только если пунктов больше 8.

**Android:** `DgListGroup` + `DgListItem` поверх `RecyclerListView`; группировку углов считает адаптер по позиции.
''')

comps['Switch'] = (card('Списки', 90, f'''<div class="dg-stage"><input type="checkbox" class="dg-sw" checked aria-label="Включено"><input type="checkbox" class="dg-sw" aria-label="Выключено"><input type="checkbox" class="dg-sw" disabled aria-label="Недоступно" style="opacity:.38"></div>'''), '''# Switch

Переключатель Material 3: включён — трек `primary`, ползунок `on-primary` с галочкой; выключен — трек `surface-container-highest`, бордер и ползунок `outline`.

**Что даёт код:** состояние, подпись строки (переключатель не используется без подписи), обработчик.

- Нажатие раздувает ползунок до 28, отпускание двигает его пружиной `spatial-fast`.
- Изменение применяется сразу, без кнопки «Сохранить».
- Выключенный (недоступный) элемент 38% прозрачности и подпись, почему недоступен.

**Android:** `DgSwitch` вместо `Switch` Telegram, рисуется на `Canvas`, хаптика `CLOCK_TICK`.
''')

comps['Avatar'] = (card('Идентичность', 220, f'''<div class="dg-stage" style="gap:20px">
 <span class="dg-av e">М</span><span class="dg-av t">К</span><span class="dg-av sc">И</span>
 <span class="dg-av persona persona-anya sh-cookie12">А</span><span class="dg-av persona persona-lyokha sh-clover4">Л</span><span class="dg-av persona persona-dasha sh-flower8">Д</span>
 <span class="dg-avwrap"><span class="dg-av persona persona-anya sh-cookie12">А</span><span class="on"></span></span>
 <span class="dg-av xl persona persona-me sh-cookie9 dg-morph">Д</span>
</div>'''), '''# Avatar

Аватар пользователя, группы или канала. **У обычных пользователей Telegram он круглый**, как везде, и смотрящий его не настраивает. **Пользователи DelushaGram настраивают свой аватар сами** в профиле (форма, цвет, анимация), и все остальные пользователи DelushaGram видят его именно так (см. «Профиль Delusha» в README).

**Что даёт код:** фото или инициалы, профиль Delusha собеседника (если есть), размер, флаги «в сети» и «есть история».

- Обычный пользователь: круг, плашка-контейнер по id (как цвета Telegram, но из ролей-контейнеров).
- Пользователь Delusha: его форма из библиотеки M3 и его тон (`persona`: контейнер, акцент, узор строятся из выбранного им оттенка теми же правилами, что и роли темы, в каждой из трёх тем).
- Анимация, выбранная владельцем (стоит, дышит, морф, крутится), играет только на его профиле и в шапке чата с ним, в списках аватар стоит.
- Размеры: `avatar-s` 40, `avatar-m` 52, закреплённые 68, профиль 156.
- «В сети»: точка `online` с обводкой цветом фона, плюс текст статуса рядом.

**Android:** `DgAvatarDrawable` оборачивает `AvatarDrawable`/`ImageReceiver`; форма через `RoundedPolygon` (androidx.graphics.shapes), морфинг через `Morph`.
''')

comps['Badge'] = (card('Идентичность', 80, f'''<div class="dg-stage"><span class="dg-badge">2</span><span class="dg-badge">128</span><span class="dg-badge muted">14</span><span class="dg-badge mention">@</span><span class="dg-badge dot"></span></div>'''), '''# Badge

Счётчик непрочитанного. `primary` — обычный чат, `muted` (`surface-container-highest`) — чат без звука, `mention` (`tertiary`) — упоминание или ответ тебе, точка — новое в разделе без числа.

**Что даёт код:** число (больше 999 показывается как «999+»), тип.

**Android:** рисуется в `DialogCell` через `DgBadge.draw(canvas, …)`, без лишних View.
''')

comps['ChatRow'] = (card('Списки', 330, f'''<div class="dg-stage col" style="max-width:420px;padding:12px 0">
 <div class="dg-cgroup">
 <div class="dg-chat"><span class="dg-avwrap"><span class="dg-av persona persona-anya sh-cookie12">А</span><span class="on"></span></span><div class="mid"><div class="nm">Аня</div><div class="pv"><em>печатает…</em></div></div><div class="rt">9:40</div></div>
 <div class="dg-chat unread"><span class="dg-av e">М</span><div class="mid"><div class="nm">Мама</div><div class="pv">Купи хлеба по дороге</div></div><div class="rt">9:38<span class="dg-badge">2</span></div></div>
 <div class="dg-chat unread"><span class="dg-av persona persona-lyokha sh-clover4">Л</span><div class="mid"><div class="nm">Лёха</div><div class="pv">{ic("mic")}Голосовое, 0:42</div></div><div class="rt">8:55<span class="dg-badge">1</span></div></div>
 <div class="dg-chat"><span class="dg-av t">К</span><div class="mid"><div class="nm">Кофе по пятницам {ic("volume_off")}</div><div class="pv"><span class="who">Даша:</span> кто завтра?</div></div><div class="rt">Вс<span class="dg-badge muted">14</span></div></div>
 </div>
</div>'''), '''# ChatRow

Строка в списке чатов. Строки собраны в одну группу (`dg-cgroup`): зазор 2, внешние углы 28, внутренние 8. Непрочитанные выделены жирным именем и тёмным превью.

**Что даёт код:** диалог (имя, аватар, последнее сообщение, автор в группе, время, непрочитанные, флаги: закреплён, без звука, черновик, печатает, отправлено/прочитано).

- Высота `row-chat` 72 (компактная плотность 60), скругление 24 у подсветки нажатия и фона закреплённых.
- Закреплённые живут не в списке, а рядом сверху крупными аватарами (PinnedChats). Избранное всегда первое.
- Превью: «печатает…» и черновик `primary`, автор в группе `on-surface` 500, остальное `on-surface-variant`.
- Галочки прочтения справа рядом со временем, `primary`.
- Долгий тап: выделение (аватар морфит в галочку), панель действий снизу.
- Свайпы влево и вправо настраиваются отдельно: архив, закрепить, прочитать, без звука, удалить, ничего (см. MotionSwipe).
- Архив прячется над списком и вытягивается свайпом вниз, либо показан всегда, либо скрыт (см. MotionPull).

**Android:** перерисовка `DialogCell` по этим токенам; верстка ячейки остаётся телеграмовской, меняются отступы, цвета, форма аватара и бейджи.
''')

comps['MessageBubble'] = (card('Чат', 470, f'''<div class="dg-phone" style="margin:12px"><div class="dg-msgs">
 <span class="dg-day">Сегодня</span>
 <div class="dg-b in first">Ты видела новый дизайн?</div>
 <div class="dg-b in last">Аватарки как печеньки<span class="meta">9:31</span></div>
 <div class="dg-b out first"><span class="quote"><b>Аня</b>Аватарки как печеньки</span>Это Material Expressive</div>
 <div class="dg-b out last">И удалённые теперь не пропадают<span class="meta">9:32 {ic("done_all")}</span><br><span class="react">❤ 2</span></div>
 <div class="dg-b in first deleted"><span class="tag">{ic("delete")}удалено</span>ой, это не тебе<span class="meta">9:33</span></div>
 <div class="dg-b in last edited"><span class="tag">{ic("history")}изменено, 2 раза</span>Приду к восьми<span class="meta">9:35</span></div>
 <div class="dg-b out first last dg-voice"><span class="play sh-cookie7">{ic("play_arrow",True)}</span>{wave_svg()}<span class="len">0:17</span></div>
</div></div>'''), '''# MessageBubble

Пузырь сообщения без хвостика. Подряд идущие сообщения одного автора — серия: зазор `space-2xs`, стыкующиеся углы `shape-bubble-joined`, внешние `shape-l-plus`; после серии отступ 8.

**Что даёт код:** сообщение (текст, медиа, ответ, реакции, время, статус), позицию в серии (первое, среднее, последнее, одиночное), флаги форка: удалено, изменено (число версий).

- Входящие `bubble-in`, исходящие `bubble-out` (в тёмных темах приглушены до `primary-container`, чтобы не слепить ночью).
- Удалённое (история форка): `bubble-deleted`, метка «удалено» с иконкой; тап открывает детали удаления.
- Изменённое: метка «изменено, N раза» (`tertiary`, с правильным склонением: 1 раз, 2 раза, 5 раз). Тап открывает вертикальную карусель версий: те же пузыри, сверху старые, снизу новые, листаются свайпом вверх-вниз, изменённые слова подсвечены (см. MotionHistory).
- Ответ: цитата в плашке 12% от цвета текста, со скруглением 12, без боковой полоски.
- Ширина не больше `bubble-max` (80%), время и галочки обтекаются текстом.
- Голосовое: кнопка-печенька и волнистый прогресс (WavyProgress).

**Удобство:** свайп пузыря влево — ответить, двойной тап — реакция по умолчанию, долгий тап — меню (MessageMenu). Размер текста `message` меняется ползунком.

**Android:** `ChatMessageCell` рисует фон через `DgBubbleDrawable` вместо `Theme.chat_msgInDrawable`; группировку Telegram уже считает (`pinnedTop/pinnedBottom`), берём её.
''')

comps['Composer'] = (card('Чат', 160, f'''<div class="dg-stage col" style="max-width:400px;padding:8px">
 <div class="dg-composer"><div class="dg-field"><button class="dg-ib" aria-label="Эмодзи">{ic("mood")}</button><span class="ph">Сообщение</span><button class="dg-ib" aria-label="Вложение">{ic("attach_file")}</button></div><button class="dg-send sh-cookie9" aria-label="Голосовое">{ic("mic",True)}</button></div>
 <div class="dg-composer"><div class="dg-field"><button class="dg-ib" aria-label="Эмодзи">{ic("mood")}</button><span class="ph typed">Приду к восьми, возьму торт</span><button class="dg-ib" aria-label="Вложение">{ic("attach_file")}</button></div><button class="dg-send sh-cookie9" aria-label="Отправить">{ic("send",True)}</button></div>
</div>'''), '''# Composer

Поле ввода внизу чата: пилюля `surface-container-highest` высотой `composer` и отдельная кнопка-печенька `primary` справа.

**Что даёт код:** текст черновика, состояние (пусто, набран текст, запись голосового, редактирование, ответ), обработчики.

- Пусто: кнопка — микрофон (тап переключает на кружок, удержание — запись).
- Есть текст: иконка морфит в «отправить», форма поворачивается на 1/9 оборота пружиной `spatial-fast`.
- Ответ или редактирование: над полем плашка с цитатой и крестиком, в той же ширине.
- Поле растёт до 6 строк, дальше прокрутка внутри.
- Поле на `surface-glass` с размытием: сообщения уходят под него и просвечивают.
- Долгий тап по «отправить»: без звука, отложить, отправить когда будет в сети.

**Android:** перерисовка `ChatActivityEnterView` (фон поля и кнопка отправки), логика ввода не трогается.
''')

comps['MessageMenu'] = (card('Чат', 470, f'''<div class="dg-stage"><div class="dg-menuwrap">
 <div class="dg-reacts"><span>❤</span><span>👍</span><span>😂</span><span>🔥</span><span>😢</span><span style="font-size:20px">{ic("expand_more")}</span></div>
 <div class="dg-menu">
  <div class="grp"><div class="mi">{ic("reply")}Ответить</div><div class="mi">{ic("content_copy")}Копировать</div><div class="mi">{ic("translate")}Перевести</div><div class="mi">{ic("forward")}Переслать</div></div>
  <div class="grp"><div class="mi sel">{ic("history")}История правок<span class="k">2</span></div><div class="mi">{ic("repeat")}Повторить</div><div class="mi">{ic("data_object")}Детали</div></div>
  <div class="grp"><div class="mi danger">{ic("delete")}Удалить</div></div>
 </div>
</div></div>'''), '''# MessageMenu

Меню по долгому тапу на сообщении: полоса реакций сверху и меню из групп с зазором `space-2xs` (внешние углы `shape-l-plus`), как в Material 3 Expressive.

**Что даёт код:** сообщение, доступные действия, порядок групп.

- Группа 1 — частые: Ответить, Копировать, Перевести, Переслать.
- Группа 2 — функции форка: История правок (с числом версий, выделена `tertiary-container`), Повторить, Детали (JSON).
- Группа 3 — опасное: Удалить (`error`).
- Остальное (закрепить, выбрать, сохранить, ссылка) уходит в «Ещё», если пунктов больше 9.
- Появляется из точки нажатия пружиной `spatial-default`. Чат за меню размывается (`blur-scrim`) и затемняется на 24%, выбранное сообщение остаётся резким и приподнимается.

**Удобство:** первая реакция одним тапом; меню не перекрывает сообщение, к которому относится.

**Android:** замена `ActionBarPopupWindow` в `ChatActivity.createMenu`, список действий берётся из существующего.
''')

comps['WavyProgress'] = (card('Обратная связь', 110, f'''<div class="dg-stage" style="color:var(--primary);gap:28px">{wave_svg(200,0.3)}{wave_svg(200,0.75)}<span style="color:var(--on-surface-variant)" class="dg-cap">загрузка 75%</span></div>'''), '''# WavyProgress

Линейный прогресс с волной у заполненной части (Material 3 Expressive): загрузка файлов, воспроизведение голосовых.

**Что даёт код:** доля 0–1 или неопределённый режим, цвет (по умолчанию `primary`).

- Заполненная часть волнистая, оставшаяся — прямая 30% прозрачности, с разрывом 6 между ними.
- Волна бежит, пока идёт процесс; на паузе выпрямляется пружиной `spatial-slow`.
- При «Меньше движения» волна статична.

**Android:** `LinearWavyProgressIndicator` из Material Components 1.13 или своя `DgWavyProgress` на `Path`.
''')

comps['LoadingIndicator'] = (card('Обратная связь', 100, '''<div class="dg-stage" style="gap:28px"><span class="dg-loading" role="progressbar" aria-label="Загрузка"><i></i></span><span class="dg-loading contained" role="progressbar" aria-label="Загрузка"><i></i></span></div>'''), '''# LoadingIndicator

Индикатор загрузки до 5 секунд: форма перетекает печенье → клевер → цветок → пятиугольник и вращается. Заменяет круговой спиннер.

**Что даёт код:** размер (48 по умолчанию), вариант: без фона или в круге `primary-container`.

- Для коротких ожиданий: открытие чата, загрузка истории, «Соединение…» в шапке.
- Дольше 5 секунд или известна доля — WavyProgress.

**Android:** `LoadingIndicator` из Material Components 1.13 (`com.google.android.material.loadingindicator`).
''')

comps['Snackbar'] = (card('Обратная связь', 90, f'''<div class="dg-stage"><div class="dg-snack"><span class="tx">Режим призрака включён</span><button>Отменить</button></div></div>'''), '''# Snackbar

Короткое сообщение о результате действия внизу экрана, над плавающей панелью. Фон `inverse-surface`, текст `inverse-on-surface`, действие `inverse-primary`.

**Что даёт код:** текст (что произошло, в прошедшем времени), необязательное действие (обычно «Отменить»).

- 4 секунды, с действием 6; свайп вбок закрывает.
- Не для ошибок, требующих решения: для них диалог.

**Android:** `DgSnackbar` на базе `BulletinFactory` Telegram (перекраска и форма `shape-m`).
''')


comps['PinnedChats'] = (card('Навигация', 130, f'''<div class="dg-pins">
 <div class="dg-pin"><span class="dg-av sh-cookie4" style="--av-bg:var(--primary);--av-fg:var(--on-primary)">{ic("bookmark",True)}</span>Избранное</div>
 <div class="dg-pin"><span class="dg-av persona persona-anya sh-cookie12">А</span>Аня</div>
 <div class="dg-pin"><span class="dg-av e">М</span><span class="dg-badge">2</span>Мама</div>
 <div class="dg-pin"><span class="dg-av persona persona-lyokha sh-clover4">Л</span><span class="dg-badge">1</span>Лёха</div>
 <div class="dg-pin"><span class="dg-av sc">Р</span>Работа</div>
</div>'''), '''# PinnedChats

Закреплённые чаты крупными аватарами 68 в ряд над списком. Заменяет заголовок «Чаты»: верх главного экрана занят тем, что нужно чаще всего.

- Избранное всегда первое, в форме `cookie4` на `primary`.
- Счётчик непрочитанного на углу аватара, с обводкой цветом фона.
- Ряд прокручивается вбок, если закреплённых больше пяти. Долгий тап: открепить, переставить.
- Тап по закреплённому открывает чат той же трансформацией контейнера, что и строка (MotionOpen).

**Android:** горизонтальный `RecyclerListView` над `DialogsActivity.listView`.
''')

comps['ProfileHero'] = (card('Профиль', 520, f'''<div class="persona persona-anya" style="width:380px;margin:12px;border-radius:28px;overflow:hidden;background:var(--surface)">
 <div class="dg-hero" style="height:170px"><i class="pat sh-cookie9" style="left:20px;top:10px;width:90px;height:90px"></i><i class="pat sh-clover4" style="left:220px;top:-20px;width:110px;height:110px"></i><i class="pat sh-flower8" style="left:300px;top:90px;width:80px;height:80px"></i><i class="pat sh-sunny" style="left:120px;top:100px;width:60px;height:60px"></i></div>
 <div class="dg-ring sh-cookie12" style="width:132px;height:132px;margin-top:-66px"><span class="dg-av persona persona-anya sh-cookie12" style="width:118px;height:118px;font-size:44px">А</span></div>
 <h2 class="dg-pname" style="font-size:28px">Аня Соколова<span class="bdg sh-sunny">{ic("auto_awesome",True)}</span></h2>
 <div class="dg-pstat">в сети</div>
 <div class="dg-bento" style="padding:14px 14px 16px"><div class="dg-card span2 acc"><b>Рисую интерфейсы, пью матчу</b></div><div class="dg-card"><span>Ник</span><b>@anya_sk</b></div><div class="dg-card"><span>Звонки</span><b>Вчера, 12 мин</b></div></div>
</div>'''), '''# ProfileHero

Шапка профиля пользователя DelushaGram: всё оформлено так, как он сам настроил (ProfileEditor). Обычные пользователи получают ту же раскладку в цветах темы, с круглым аватаром и однотонной шапкой.

- Шапка 250 с нижними углами 44: фон по выбору владельца (формы, градиент или цвет) в его тоне `persona`.
- Аватар 156 в его форме, в «кольце» той же формы цвета фона, наезжает на шапку.
- Имя `900`, ширина по выбору владельца (узкое, обычное, широкое), рядом значок-форма.
- Кнопки Чат, Звук, Звонок, Видео группой в его акценте.
- Ниже бенто-карточки: о себе (широкая), ник, ID, звонки с ним, общие медиа сеткой разных размеров.

**Android:** `DgProfileHero` заменяет шапку `ProfileActivity`.
''')

comps['ProfileEditor'] = (card('Профиль', 520, f'''<div class="dg-secs" style="width:380px;margin:12px">
 <div class="dg-sec"><span class="hd">Форма</span><div class="dg-pick"><i class="sh-circle"></i><i class="sh-cookie9 on"></i><i class="sh-clover4"></i><i class="sh-flower8"></i><i class="sh-sunny"></i></div></div>
 <div class="dg-sec"><span class="hd">Цвет</span><div class="dg-pick col"><i class="sh-cookie9 on" style="background:oklch(0.6 0.13 295)"></i><i class="sh-cookie9" style="background:oklch(0.6 0.10 190)"></i><i class="sh-cookie9" style="background:oklch(0.6 0.12 140)"></i><i class="sh-cookie9" style="background:oklch(0.64 0.13 50)"></i><i class="sh-cookie9" style="background:oklch(0.6 0.14 0)"></i><i class="sh-cookie9" style="background:oklch(0.6 0.12 240)"></i></div></div>
 <div class="dg-sec"><span class="hd">Аватар</span><div class="dg-seg" style="padding:0"><button aria-pressed="false">Стоит</button><button aria-pressed="true">Дышит</button><button aria-pressed="false">Морф</button><button aria-pressed="false" style="border-radius:8px 20px 20px 8px">Крутится</button></div></div>
 <div class="dg-sec"><span class="hd">Имя</span><div class="dg-names"><div style="font-variation-settings:'wdth' 60">Аа</div><div style="font-variation-settings:'wdth' 100">Аа</div><div class="on" style="font-variation-settings:'wdth' 151">Аа</div></div></div>
</div>'''), '''# ProfileEditor

Редактор своего профиля Delusha: открывается карандашом на своём профиле. Сверху живое превью, ниже секции группой с общими краями. Всё применяется сразу.

Настройки (все параметрические, без картинок, чтобы помещались в описание профиля):

| Что | Варианты |
|---|---|
| Форма аватара | круг и 8 форм библиотеки M3 |
| Анимация аватара | стоит, дышит, морф между двумя формами, крутится |
| Цвет профиля | 16 оттенков; из него строится весь тон профиля и цвет твоих пузырей у собеседника |
| Шапка | формы, градиент, цвет; плотность узора |
| Имя | ширина шрифта (узкое, обычное, широкое), вес |
| Значок | форма рядом с именем или без |
| Эффект открытия | частицы-формы, волна, без |
| Пузыри у собеседника | в твоём цвете или в его теме |

**Android:** `DgProfileEditorActivity`; запись в профиль через `account.updateProfile` (см. «Профиль Delusha» в README).
''')

comps['ToggleTile'] = (card('Списки', 180, f'''<div class="dg-stage" style="max-width:420px"><div class="dg-ttiles" style="width:100%"><div class="dg-ttile on"><span class="ti sh-cookie9">{ic("visibility_off",True)}</span><div class="bt"><b>Призрак</b><input type="checkbox" class="dg-sw" checked aria-label="Призрак"></div></div><div class="dg-ttile"><span class="ti sh-clover4">{ic("history",True)}</span><div class="bt"><b>История</b><input type="checkbox" class="dg-sw" aria-label="История"></div></div></div></div>'''), '''# ToggleTile

Крупная плитка-переключатель 140 высотой с углами 32 для главных функций форка в начале настроек Delusha: Призрак, История.

- Включена: фон `tertiary-container` (или `secondary-container`), иконка в форме на насыщенном цвете. Выключена: `surface-container`, иконка серая.
- Тап по всей плитке переключает; иконка морфит форму (`spatial-fast`), фон перетекает (`effects-default`).
- Две плитки в ряд, не больше четырёх на экран.

**Android:** `DgToggleTile`.
''')

for name, (h, sub, body, js, readme) in motion.CARDS.items():
    comps[name] = (card('Движение', h, body, motion.ENGINE + js, sub), readme)

for name, (prev, readme) in comps.items():
    w(f'components/{name}/preview.html', prev)
    w(f'components/{name}/README.md', readme)

# ---------------------------------------------------------------- cover
blocks = []
cover = f'''<!-- @dsCard height=300 -->
<!doctype html>
<html>
<head><meta charset="utf-8"><link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Roboto+Flex:opsz,wdth,wght@8..144,25..151,100..1000&display=swap"><link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Rounded:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200&display=block">
<style>
html,body{{margin:0;background:var(--surface)}}
.cv{{position:relative;width:960px;height:300px;overflow:hidden;background:var(--surface)}}
svg{{position:absolute;inset:0}}
.pc{{fill:var(--primary-container)}} .p{{fill:var(--primary)}} .tc{{fill:var(--tertiary-container)}} .sc{{fill:var(--secondary-container)}} .t{{fill:var(--tertiary)}} .is{{fill:var(--inverse-surface)}} .ec{{fill:var(--error-container)}}
.slab{{rx:var(--shape-xl-plus)}} .pill{{rx:48px}} .sq{{rx:var(--shape-l-plus)}}
.name{{position:absolute;left:40px;bottom:66px;margin:0;font:850 64px/0.95 var(--font-sans);letter-spacing:-1.6px;color:var(--on-surface);font-variation-settings:"wdth" 110,"opsz" 72}}
.tag{{position:absolute;left:42px;bottom:36px;width:440px;font:400 14px/20px var(--font-sans);color:var(--on-surface-variant)}}
</style></head>
<body>
<div class="cv">
<svg width="960" height="300" viewBox="0 0 960 300" aria-hidden="true">
<!-- blocks: primary-container slab 248x300 (shape-xl-plus), primary cookie9 d=168, tertiary-container pill 96 tall, secondary-container square 120 (shape-l-plus), inverse-surface clover 88
     arrangement: one tall slab bleeding off top and bottom with satellites stacked right of x=480; error stays off the cover (state colour only)
     pattern: a literal motif, the M3 Expressive shape library (cookie, clover, flower), because the README makes shape morphing the brand's signature
     scales: sides 48/96/120 from space-3xl multiples; radii shape-l-plus 20, shape-xl-plus 32, pill = half of 96 -->
<rect class="pc slab" x="520" y="-40" width="248" height="380"/>
<path class="p" d="{svgpath(9,.065,644,150,84)}"/>
<rect class="tc pill" x="784" y="24" width="200" height="96"/>
<rect class="sc sq" x="784" y="140" width="120" height="120"/>
<path class="is" d="{svgpath(4,.26,870,200,44,0.7853981634)}"/>
<path class="t" d="{svgpath(8,.13,836,72,30)}"/>
</svg>
<h1 class="name">DelushaGram</h1>
<div class="tag">Личный Telegram на Material 3 Expressive</div>
</div>
</body>
</html>
'''
w('components/Cover/preview.html', cover)

# ---------------------------------------------------------------- README
readme = r'''DelushaGram — личный форк Telegram для Android, переделанный с нуля по Material 3 Expressive. Главное правило: **удобство важнее красоты**. Красота — формы, цвет и пружинное движение; удобство — предсказуемые места, крупные цели и минимум тапов до частых действий. Если они спорят, побеждает удобство.

## Удобство: обязательные правила

- **Нижняя треть экрана — для частых действий.** Нижняя панель (`FloatingToolbar`: аккаунт, поиск, новый чат), поле ввода, меню сообщений и листы. Наверху только заголовок и плашка призрака.
- **Всё лежит там, где нужно.** Отдельного меню-свалки нет:

| Что | Где |
|---|---|
| Избранное | первым в закреплённых (`PinnedChats`) |
| Контакты | на экране «новый чат» (FAB) |
| Звонки | фильтр «Звонки» в поиске, кнопка в шапке чата, история звонков в профиле собеседника |
| Аккаунты | на своём профиле (тап по аватару внизу); долгий тап по аватару переключает сразу |
| Настройки | шестерёнка на своём профиле |
| Delusha | первый раздел настроек, призрак ещё и в шапке главного экрана |
| Тема | в настройках и плиткой в шторке |
- **Зона касания не меньше `touch-target` (48).** Даже у маленьких иконок, бейджей и чипов. Между соседними целями не меньше 8.
- **Каждый жест продублирован видимой кнопкой.** Свайп ответа дублируется пунктом «Ответить» в `MessageMenu`, свайп папок — чипами `FolderChips`, долгий тап по аватару (смена аккаунта) — списком аккаунтов на своём профиле.
- **Режим призрака за один тап** из шапки списка чатов (`dg-ghost`), из шторки (плитка быстрых настроек) и из настроек Delusha. Его состояние всегда видно в шапке.
- **Места не прыгают.** Одно и то же действие всегда в одной точке: «Назад» слева сверху, «Ещё» справа сверху, главное действие — FAB справа снизу.
- **Обратная связь на всё.** Нажатие: слой `state-pressed` + морфинг формы + лёгкая вибрация. Результат: `Snackbar` с «Отменить» для обратимых действий, диалог подтверждения для необратимых.
- **Текст масштабируется до 200%.** Ничего не обрезается, строки переносятся, высоты элементов растут.
- **«Меньше движения»** (системная настройка или своя в форке): пружины `spatial-*` заменяются на `spatial-calm`, морфинг аватаров и бегущие волны останавливаются.
- **Две плотности.** Обычная (`row-chat` 72) и компактная (60) для тех, кто хочет больше чатов на экране. Отступы сжимаются, цели касания — нет.

## Голос интерфейса

- Пишем по-русски, коротко, на «ты» там, где обращаемся к человеку («Ты ещё не сохранял удалённые»), без восклицательных знаков.
- Кнопки — глагол: «Отправить», «Удалить», «Включить призрака». Не «ОК».
- Статусы строчными, как в Telegram: «в сети», «печатает…», «был(а) недавно».
- **Никакого поясняющего текста.** Ни плашек «тебя не видно», ни подсказок «потяни, чтобы…», ни описаний под пунктами. Состояние показывают цвет, иконка, форма и вибрация. Подпись под пунктом только для значения: 30 с, 212 МБ, «Полные».
- Эмодзи только в пользовательском контенте и реакциях, не в интерфейсе.
- **Никаких точек-разделителей** (`·`, `•`) в строках интерфейса. Части соединяем запятой или разносим по строкам: «изменено, 2 раза», «1 284 сообщения, 212 МБ». Число со словом склоняется: 1 раз, 2 раза, 5 раз.

## Цвет

- Все цвета — роли Material 3 из `tokens.json`. Ни одного литерального цвета в коде компонентов: только `primary`, `on-surface` и т.д.
- **По умолчанию берётся системный акцентный цвет** (Material You, Android 12+): все роли строятся из него, приложение выглядит в тон остальной системе. Значения в `tokens.json` от сида «Сирень» — запасные для Android 8–11 и для случая, когда в настройках выбран свой цвет.
- **Палитра спокойная.** Насыщенный `primary` только для мелких акцентов (счётчики, ссылки, переключатели); крупные заливки — контейнеры с низкой насыщенностью. Никаких кислотных плашек на пол-экрана.
- Три темы: `light`, `dark`, `amoled` (настоящий чёрный `surface` для OLED). Переключение по системе или вручную.
- Текст: `on-surface` на любом `surface*`, `on-surface-variant` для вторичного. Каждая пара `on-X` / `X` держит не меньше 4.5:1 во всех трёх темах.
- Сообщения: `bubble-in` / `on-bubble-in`, `bubble-out` / `on-bubble-out`. Исходящие — `primary-container`, мягкий тон акцента, а не яркая заливка.
- Обои чата по умолчанию: три размытых пятна `primary-container`, `tertiary-container`, `secondary-container` на `surface` и узор из форм M3 с прозрачностью 5%.
- Функции форка окрашены в `tertiary`: плашка `ghost`, метка «изменено», иконки разделов форка. Удалённое — `bubble-deleted` (`error-container`). Так видно, что это не стандартный Telegram.
- `error` только для необратимого и ошибок. Никогда как украшение.
- Фокус клавиатуры и D-pad: кольцо `focus-ring` 3dp с отступом 2dp.

## Экспрессивность (не просто Material 3)

Material 3 Expressive отличается от обычного M3 смелостью. На каждом экране есть свой **главный момент**, и он сделан формой, размером и цветом, а не текстом.

- **Формы как контейнеры.** Аватары, FAB, кнопка отправки, иконки плиток, значки, счётчики стоят в формах библиотеки M3 (печенье, клевер, цветок, солнце), а не в кругах и квадратах. Круг только у обычных пользователей и у мелочей.
- **Контраст масштаба.** Крупное очень крупное (`dg-xxl` 64, ширина 151, вес 900; имя в профиле 34), мелкое спокойное. Средних «на всякий случай» заголовков нет. Подписи разделов вроде «Чаты» не пишем, если и так ясно, где ты.
- **Группы с общими краями.** Списки, секции, меню, кнопки собраны в группы: зазор 2, внешние углы 28, внутренние 8. Так выглядит список чатов, настройки, меню сообщения.
- **Бенто.** Информация разного веса — карточками разного размера в сетке (профиль, медиа).
- **Цвет блоками.** Крупные плоскости цвета (шапка профиля, плитки «Призрак» и «История», выбранные сегменты), но в спокойных контейнерных тонах; насыщенный цвет только на мелочах.
- **Декоративные формы.** Рядом с крупными заголовками и в шапках — кластер из 2–3 больших форм в контейнерных цветах.
- **Движение формы.** Нажатое сжимается, выбранное становится круглее или квадратнее, переходы на пружинах (см. «Движение»).

## Профиль Delusha

Пользователи DelushaGram оформляют свой профиль сами (ProfileEditor), и **все остальные пользователи DelushaGram видят его так же**. У обычных пользователей Telegram и в официальных клиентах профиль выглядит как обычно.

- **Как это передаётся.** Без своих серверов: настройки кодируются в 12 невидимых символов Unicode Tags (U+E0020–U+E007E) в конце поля «О себе». Официальные клиенты их не показывают, DelushaGram читает и вырезает перед показом. Цена: от лимита «О себе» (70 символов) остаётся 58.
- **Формат:** `DG` + версия + форма + анимация + оттенок + шапка + плотность узора + ширина имени + вес + значок + эффект + пузыри. Всё числами, без картинок.
- **Кэш:** разобранные настройки хранятся рядом с `UserFull`, обновляются вместе с ним.
- **Что видят другие пользователи Delusha:** форму и тон аватара везде (списки, чат, закреплённые), полную шапку на профиле, анимацию аватара на профиле и в шапке чата, твои пузыри в твоём цвете (если ты так выбрал), эффект при открытии профиля.

## Размытие

- Размываем только то, что **парит над контентом**: нижнюю панель, шапку чата, поле ввода (все на `surface-glass` + `blur-glass` 24) и фон под меню сообщения, каруселью правок и листами (`dg-scrim`: `blur-scrim` 10 + затемнение 24%).
- **Не размываем** строки списков, пузыри, карточки настроек и всё, где лежит текст для чтения: под стеклом текст только проходит, а не читается.
- Стекло всегда с запасом непрозрачности (66–72%), чтобы текст на нём держал контраст при любом контенте снизу.
- В настройках «Размытие» можно выключить: тогда `surface-glass` становится непрозрачным `surface-container-high`.
- **Android:** API 31+ `RenderEffect.createBlurEffect` на снимке контента в 1/4 разрешения; ниже — собственное размытие Telegram (`SizeNotifierFrameLayout`, `BlurredFrameLayout` уже есть в коде). Цель: размытие не стоит ни одного кадра.

## Форма

- Шкала `shape-*` от `shape-xs` (4) до `shape-full`. Крупнее элемент — крупнее радиус.
- **Морфинг формы — это обратная связь.** В покое кнопки круглые (`shape-full`), при нажатии углы сжимаются (кнопка S до `shape-s`, M до `shape-l`), выбранное переключаемое становится квадратнее (`shape-m`).
- **Соединённые группы:** элементы с зазором `space-2xs`, общие внешние углы, внутренние `shape-xs` / `shape-s`. Так собраны `ListGroup`, `ButtonGroup`, `MessageMenu`, серии пузырей.
- **Библиотека форм M3 Expressive** (печенье 4/7/9/12, клевер, цветок, солнце, пятиугольник) — для аватаров, кнопки отправки, индикатора загрузки и иконок разделов. Не больше одной «необычной» формы на элемент списка.
- Разделителей-линий нет: группы разделяются фоном и зазорами.

## Типографика

- Один шрифт — **Roboto Flex** (переменный: вес, ширина, оптический размер); в настройках можно включить системный.
- Экспрессивные стили (`hero-title`, `screen-title`, `profile-name`) — широкие (wdth 120–140) и жирные (800–850): заголовки разделов и имя в профиле. Не больше одного такого заголовка на экран.
- Интерфейс — шкала M3: `title-*`, `body-*`, `label-*`.
- Чат: `message` (16/22, размер меняется ползунком), `message-meta`, `chat-name`, `chat-preview`.
- Цифры в счётчиках, времени и размерах — моноширинные (`tnum`).

## Движение — главное в DelushaGram

Анимации здесь не украшение, а характер приложения. Каждое действие отвечает движением, которое видно, чувствуется пальцем и никогда не тормозит само действие.

- **Только пружины**, никаких фиксированных длительностей и кривых (`spring` в `tokens.json`). Длительность получается сама из расстояния и скорости пальца.
- `spatial-*` (с перелётом) — позиция, размер, форма. `effects-*` (без перелёта) — цвет и прозрачность.
- **Прерываемость.** Любую анимацию можно перехватить: новый тап или жест подхватывает пружину с текущей скоростью и разворачивает её. Ничего не ждёт окончания.
- **Жест управляет напрямую.** Пока палец на экране, элемент едет за ним 1:1 (свайп чата, архив, назад). Отпустил — пружина стартует со скоростью пальца.
- **Пороги чувствуются.** Перешёл порог жеста: вибрация, иконка подпрыгивает, цвет заливает фон. За порогом сопротивление (резинка).
- **Вход медленнее выхода.** Открытие `spatial-slow`/`spatial-default`, закрытие на ступень быстрее. Уходящее не задерживает.
- **Лесенка для групп.** Пункты списка и меню появляются с шагом 35–40 мс, исчезают обратным порядком быстрее.
- **Одна главная вещь движется**, остальное следует за ней или стоит.
- **Действие мгновенно, анимация поверх.** Сообщение уходит в сеть в момент нажатия, экран открывается сразу, анимация только показывает это.
- **Производительность.** Анимируются только свойства RenderThread (`translation`, `scale`, `alpha`, `rotation`) и отрисовка в `onDraw`; ни одной перекладки на кадр. Цель — 120 Гц без пропусков на Snapdragon 7-й серии, 60 Гц на старых.
- **«Меньше движения»:** пружины `spatial-*` → `spatial-calm` (без перелёта), морфинг аватаров, частицы и волны выключены, переходы остаются, но короче.

### Фирменные анимации

| Где | Что происходит | Карточка |
|---|---|---|
| Отправка | Поле ввода превращается в пузырь и встаёт в ленту | MotionSend |
| Открытие чата | Строка раскрывается в экран, аватар перелетает в шапку | MotionOpen |
| Свайпы по чатам | Заливка от иконки, порог с вибрацией, схлопывание | MotionSwipe |
| Архив | Вытягивается вниз, печенька крутится и морфит | MotionPull |
| Призрак | Цвет заливает шапку кругом, глаз закрывается | MotionGhost |
| Новый чат | FAB морфит в круг, пункты вылетают лесенкой | MotionFab |
| Реакции | Сердце и частицы-формы, число прокручивается | MotionReactions |
| История | Удалённое вздрагивает, версии правок листаются вертикальной каруселью | MotionHistory |
| Нажатие | Углы кнопок и иконок сжимаются, отпускание с отскоком | Button, IconButton |

## Отступы и слои

- Сетка 4: `space-2xs` 2 … `space-3xl` 48. Боковой отступ экрана `space-l` (16), между секциями `space-xl` (24).
- Тени только у плавающего: `elevation-2` у нижней панели и FAB, `elevation-3` у меню и листов. Всё остальное различается тоном `surface-container-*`.

## Иконки

- **Material Symbols Rounded**, 24 (`icon`), в чипах и кнопках 20 (`icon-s`), вес 450.
- Контур в покое, заливка (`FILL 1`) у выбранного: вкладка панели, переключаемая иконка-кнопка.
- В Android — векторные drawable, по два на иконку (контур и заливка), сгенерированные из Material Symbols (Apache 2.0). Иконки Telegram, у которых нет аналога (секретный чат, бусты, звёзды), перерисовываются в той же сетке и толщине.

## Как это ложится в код (поддерживаемость)

- **Источник истины — `tokens.json`.** Скрипт `delusha/tools/gen_tokens.py` собирает из него `DgTokens.java` (цвета по темам, радиусы, отступы, пружины). Значения руками в Java не пишутся.
- **Мост к темам Telegram.** Telegram красит всё через ключи `Theme.key_*` (их больше тысячи). `DgThemeBridge` раз и навсегда сопоставляет каждый ключ роли M3. Так старые экраны, которые мы не перерисовывали, всё равно получают наши цвета.
- **Новые компоненты — отдельный пакет** `ru.amoboba.delushagram.ui.components` (`DgButton`, `DgFloatingToolbar`, `DgListGroup`…). Существующие ячейки Telegram (`DialogCell`, `ChatMessageCell`) не переписываются, а получают хуки отрисовки.
- **Каждая правка в файлах Telegram обёрнута** `// DG-BEGIN <фича>` … `// DG-END`. При обновлении на новую версию Telegram конфликтующие места находятся одним `grep`, а логика форка лежит в своих файлах.
- **Функции форка** (призрак, история) — в пакете `ru.amoboba.delushagram.features`, с одной точкой входа в сетевой слой (`ConnectionsManager.sendRequest`).
'''
w('README.md', readme)

# ---------------------------------------------------------------- index
now = datetime.datetime.now(datetime.timezone.utc).strftime('%Y-%m-%dT%H:%M:%SZ')
index = {"v": 3, "layout": "files", "createdOnFiles": {"v": 1, "at": now}, "title": "DelushaGram", "namespace": "Delusha",
         "libraries": [], "sections": {}, "groups": [], "assetGroups": {}, "blobs": {}, "docs": {"readme": "project/README.md", "sections": []},
         "lastChange": {"by": "Claude", "at": now, "via": "Claude Code", "note": "Первая версия дизайн-системы DelushaGram"}}
w('design-system.json', json.dumps(index, ensure_ascii=False, indent=1))
print('ok', sum(len(f) for _, _, f in os.walk(OUT)))
