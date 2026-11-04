import json, os
D = os.path.dirname(os.path.abspath(__file__))
P = os.path.join(D, 'ds', 'project')
T = json.load(open(os.path.join(P, 'tokens.json')))

def v(x): return f"var(--{x[1:-1]})" if x.startswith('{') else x
th = {}
for t in T['color']['tokens']:
    val = t['value'] if isinstance(t['value'], dict) else {k: t['value'] for k in ('light','dark','amoled')}
    for k, x in val.items(): th.setdefault(k, []).append(f"--{t['name']}:{v(x)}")
root = ''.join(f"--{t['name']}:{t['value']};" for f in ['spacing', 'radius', 'size', 'state', 'shadow', 'blur'] for t in T[f]['tokens'])
tokcss = f":root{{{root}--font-sans:{T['type']['families']['sans']}}}\n" + ''.join(
    f"[data-theme={k}]{{{';'.join(vals)}}}\n" for k, vals in th.items()) + f":root{{{';'.join(th['light'])}}}\n"
bundle = open(os.path.join(P, 'components', 'bundle.css')).read()
bundle = '\n'.join(l for l in bundle.split('\n') if not l.startswith('@import'))

def ic(n, f=False, s=None):
    st = f' style="font-size:{s}px"' if s else ''
    return f'<span class="dg-ic{" fill" if f else ""}"{st}>{n}</span>'

SB = f'''<div class="sb"><span>9:41</span><span class="cam"></span><span class="sbi">{ic("signal_cellular_alt",1,17)}{ic("wifi",1,17)}{ic("battery_full",1,17)}</span></div>'''
GB = '<div class="gb"><i></i></div>'

def row(av, cls, shape, name, prev, time, extra='', badge='', pinned=False, online=False):
    a = f'<span class="dg-av {cls} {shape}">{av}</span>'
    if online: a = f'<span class="dg-avwrap">{a}<span class="on"></span></span>'
    return f'<div class="dg-chat{" pinned" if pinned else ""}">{a}<div class="mid"><div class="nm">{name}{extra}</div><div class="pv">{prev}</div></div><div class="rt">{time}{badge}</div></div>'

CHATS = ''.join([
    row(ic('bookmark', 1), '', 'sh-cookie4', 'Избранное', 'ссылка на сборку 0.1', '9:12', ' ' + ic('push_pin', 1), pinned=True),
    row('А', 't', 'sh-cookie12', 'Аня', '<em>печатает…</em>', '9:40', online=True),
    row('М', 'e', 'sh-clover4', 'Мама', 'Купи хлеба по дороге', '9:38', badge='<span class="dg-badge">2</span>', online=True),
    row('Л', 'sc', 'sh-flower8', 'Лёха', ic('mic') + 'Голосовое, 0:42', '8:55', badge='<span class="dg-badge">1</span>'),
    row(ic('campaign', 1), 'sc', 'sh-sunny', 'Delusha Dev', 'Сборка 0.1 собрана, 48 МБ', 'Пн', ' ' + ic('verified', 1) + '<span class="dg-ic mute">volume_off</span>', badge='<span class="dg-badge muted">14</span>'),
    row('К', '', 'sh-pent', 'Кофе по пятницам', '<span class="who">Даша:</span> кто завтра?', 'Вс', badge='<span class="dg-badge mention">@</span>'),
    row('Р', 't', 'sh-cookie9', 'Работа', '<em>Черновик:</em> скину отчёт вечером', 'Сб'),
    row('Ю', 'e', 'sh-cookie7', 'Юля', ic('photo') + 'Фото', 'Пт'),
])

def appbar_chats(ghost=True, on=False):
    g = f'<button class="dg-ghost{" on" if on else ""}" style="margin-right:12px">{ic("visibility_off",1)}Призрак</button>' if ghost else ''
    return f'''<div class="dg-appbar" style="padding-bottom:4px;background:transparent"><div class="row"><span class="grow"></span>{g}</div><h1 style="margin-top:0">Чаты</h1></div>'''

CHIPS = '''<div class="dg-chips" style="padding:12px 16px 8px"><button class="dg-chip" aria-pressed="true">Все <span class="n">12</span></button><button class="dg-chip" aria-pressed="false">Личные</button><button class="dg-chip" aria-pressed="false">Группы <span class="n">3</span></button><button class="dg-chip" aria-pressed="false">Каналы</button><button class="dg-chip" aria-pressed="false">Боты</button></div>'''

NAV = f'''<div class="nav"><div class="dg-bottombar"><button class="bar dg-glass"><span class="dg-av sh-cookie9">Д</span>{ic("search")}<span class="ph">Поиск</span></button><button class="dg-fab" aria-label="Новый чат">{ic("edit",1)}</button></div></div>'''
WALL = '''<div class="dg-wall"><i class="sh-cookie9" style="left:155px;top:174px;width:38px;height:46px;transform:rotate(6deg)"></i><i class="sh-clover4" style="left:27px;top:860px;width:43px;height:29px;transform:rotate(46deg)"></i><i class="sh-flower8" style="left:288px;top:79px;width:42px;height:32px;transform:rotate(4deg)"></i><i class="sh-sunny" style="left:34px;top:464px;width:39px;height:28px;transform:rotate(30deg)"></i><i class="sh-pent" style="left:36px;top:584px;width:39px;height:27px;transform:rotate(72deg)"></i><i class="sh-cookie4" style="left:53px;top:248px;width:46px;height:46px;transform:rotate(74deg)"></i><i class="sh-cookie9" style="left:21px;top:610px;width:44px;height:38px;transform:rotate(6deg)"></i><i class="sh-clover4" style="left:103px;top:67px;width:43px;height:53px;transform:rotate(17deg)"></i><i class="sh-flower8" style="left:138px;top:449px;width:30px;height:43px;transform:rotate(15deg)"></i><i class="sh-sunny" style="left:282px;top:335px;width:43px;height:52px;transform:rotate(87deg)"></i><i class="sh-pent" style="left:82px;top:125px;width:44px;height:44px;transform:rotate(81deg)"></i><i class="sh-cookie4" style="left:86px;top:401px;width:29px;height:43px;transform:rotate(8deg)"></i><i class="sh-cookie9" style="left:278px;top:81px;width:45px;height:32px;transform:rotate(63deg)"></i><i class="sh-clover4" style="left:338px;top:564px;width:39px;height:50px;transform:rotate(40deg)"></i><i class="sh-flower8" style="left:228px;top:619px;width:40px;height:37px;transform:rotate(38deg)"></i><i class="sh-sunny" style="left:117px;top:833px;width:31px;height:48px;transform:rotate(31deg)"></i><i class="sh-pent" style="left:31px;top:608px;width:35px;height:42px;transform:rotate(63deg)"></i><i class="sh-cookie4" style="left:165px;top:766px;width:40px;height:35px;transform:rotate(77deg)"></i><i class="sh-cookie9" style="left:27px;top:140px;width:42px;height:39px;transform:rotate(21deg)"></i><i class="sh-clover4" style="left:377px;top:370px;width:30px;height:41px;transform:rotate(53deg)"></i><i class="sh-flower8" style="left:10px;top:704px;width:28px;height:50px;transform:rotate(71deg)"></i><i class="sh-sunny" style="left:283px;top:828px;width:54px;height:52px;transform:rotate(40deg)"></i><i class="sh-pent" style="left:164px;top:731px;width:37px;height:45px;transform:rotate(63deg)"></i><i class="sh-cookie4" style="left:286px;top:836px;width:40px;height:28px;transform:rotate(11deg)"></i><i class="sh-cookie9" style="left:128px;top:505px;width:48px;height:47px;transform:rotate(8deg)"></i><i class="sh-clover4" style="left:21px;top:768px;width:48px;height:35px;transform:rotate(82deg)"></i></div>'''

def wave(width=150, progress=.4, amp=3.5, period=20):
    import math
    pts = [f"{x+2},{12+amp*math.sin(2*math.pi*x/period):.2f}" for x in range(0, width + 1, 2)]
    cut = int(width * progress)
    act = "M" + " L".join(p for p in pts if float(p.split(',')[0]) - 2 <= cut)
    return f'<svg class="dg-wave" width="{width+4}" height="24" viewBox="0 0 {width+4} 24"><path class="trk" d="M{cut+8},12 L{width+2},12"/><path class="act" d="{act}"/></svg>'

CHATHEAD = f'''<div class="chead dg-glass"><div class="dg-appbar small" style="padding-top:0;background:transparent"><button class="dg-ib" aria-label="Назад">{ic("arrow_back")}</button><span class="dg-av s t sh-cookie12">А</span><span class="ttl">Аня Соколова<small>в сети</small></span><button class="dg-ib" aria-label="Звонок">{ic("call")}</button><button class="dg-ib" aria-label="Ещё">{ic("more_vert")}</button></div></div>'''

MSGS = f'''<div class="dg-msgs chatmsgs">
 <div class="dg-b out first">Смотри, что я собрал</div>
 <div class="dg-b out last">Форк телеги, весь на пружинах<span class="meta">9:20 {ic("done_all")}</span></div>
 <span class="dg-day">Сегодня</span>
 <div class="dg-b in first">Ты видела новый дизайн?</div>
 <div class="dg-b in">Аватарки как печеньки</div>
 <div class="dg-b in last photo"><span class="ph"></span><span class="cap">Вот так у меня выглядит<span class="meta">9:31</span></span></div>
 <div class="dg-b out first"><span class="quote"><b>Аня</b>Аватарки как печеньки</span>Это Material Expressive</div>
 <div class="dg-b out last" id="sel">И удалённые теперь не пропадают<span class="meta">9:32 {ic("done_all")}</span><br><span class="react">❤ 2</span></div>
 <div class="dg-b in first deleted"><span class="tag">{ic("delete")}удалено</span>ой, это не тебе<span class="meta">9:33</span></div>
 <div class="dg-b in last edited"><span class="tag">{ic("history")}изменено, 2 раза</span>Приду к восьми<span class="meta">9:40</span></div>
 <div class="dg-b out first last dg-voice"><span class="play sh-cookie7">{ic("play_arrow",1)}</span>{wave()}<span class="len">0:17</span></div>
</div>'''

COMPOSER = f'''<div class="dg-composer comp"><div class="dg-field dg-glass"><button class="dg-ib" aria-label="Эмодзи">{ic("mood")}</button><span class="ph">Сообщение</span><button class="dg-ib" aria-label="Вложение">{ic("attach_file")}</button><button class="dg-ib" aria-label="Камера">{ic("photo_camera")}</button></div><button class="dg-send sh-cookie9" aria-label="Голосовое">{ic("mic",1)}</button></div>'''

def screen(theme, body, cls=''):
    return f'<div class="screen {cls}" data-theme="{theme}">{SB}{body}{GB}</div>'

S = {}
S['01-chats'] = screen('light', appbar_chats() + CHIPS + f'<div class="dg-list-chats">{CHATS}</div>' + NAV)

S['02-chat'] = screen('light', WALL + MSGS + CHATHEAD + COMPOSER, 'chat')

MENU = f'''<div class="dg-menuwrap ctx"><div class="dg-reacts"><span>❤</span><span>👍</span><span>😂</span><span>🔥</span><span>🥲</span><span style="font-size:20px">{ic("expand_more")}</span></div></div>
<div class="dg-menu ctxm">
 <div class="grp"><div class="mi">{ic("reply")}Ответить</div><div class="mi">{ic("content_copy")}Копировать</div><div class="mi">{ic("translate")}Перевести</div><div class="mi">{ic("forward")}Переслать</div></div>
 <div class="grp"><div class="mi">{ic("repeat")}Повторить</div><div class="mi">{ic("push_pin")}Закрепить</div><div class="mi">{ic("data_object")}Детали</div></div>
 <div class="grp"><div class="mi danger">{ic("delete")}Удалить</div></div>
</div>
<div class="dg-b out first last lifted">И удалённые теперь не пропадают<span class="meta">9:32 {ic("done_all")}</span><br><span class="react">❤ 2</span></div>'''
S['03-menu'] = screen('dark', WALL + MSGS + CHATHEAD + COMPOSER + '<div class="dg-scrim zs"></div>' + MENU, 'chat')

CAR = f'''<div class="dg-scrim zs"></div><div class="carx">
 <div class="cv" style="left:-162px;transform:scale(.88);opacity:.5"><span class="tm">9:35</span><div class="dg-b in first last">Приду к семи</div></div>
 <div class="cv" style="left:86px"><span class="tm">9:37</span><div class="dg-b in first last">Приду к <mark>половине</mark> <mark>восьмого</mark></div></div>
 <div class="cv" style="left:334px;transform:scale(.88);opacity:.5"><span class="tm">9:40</span><div class="dg-b in first last">Приду к <mark>восьми</mark></div></div>
 <div class="dg-dots" style="position:absolute;left:0;right:0;top:150px"><i></i><i class="on"></i><i></i></div>
</div>'''
S['04-history'] = screen('dark', WALL + MSGS + CHATHEAD + COMPOSER + CAR, 'chat')

PROFILE = f'''<div class="dg-appbar" style="padding:0 4px"><div class="row"><button class="dg-ib" aria-label="Назад">{ic("arrow_back")}</button><span class="grow"></span><button class="dg-ib" aria-label="Изменить">{ic("edit")}</button><button class="dg-ib" aria-label="Ещё">{ic("more_vert")}</button></div></div>
<div class="prof"><span class="dg-av xl t sh-cookie12">А</span><h2>Аня Соколова</h2><div class="st">в сети</div>
<div class="dg-bgroup tall" style="align-self:stretch;margin:18px 0 16px">
 <button class="dg-btn filled" aria-pressed="true">{ic("chat_bubble",1)}Чат</button><button class="dg-btn tonal">{ic("notifications_off")}Звук</button><button class="dg-btn tonal">{ic("call")}Звонок</button><button class="dg-btn tonal">{ic("videocam")}Видео</button></div></div>
<div class="dg-list" style="padding:0 16px">
 <div class="dg-li info"><span class="tx"><b>@anya_sk</b><span>Имя пользователя</span></span>{ic("qr_code_2")}</div>
 <div class="dg-li info"><span class="tx"><b>Рисую интерфейсы, пью матчу</b><span>О себе</span></span></div>
 <div class="dg-li info"><span class="tx"><b class="tnum">482 913 557</b><span>ID, дата-центр 2, регистрация около 2019</span></span>{ic("content_copy")}</div>
</div>
<div class="dg-bgroup" style="padding:16px 16px 10px"><button class="dg-btn toggle" aria-pressed="true">Медиа</button><button class="dg-btn toggle" aria-pressed="false">Файлы</button><button class="dg-btn toggle" aria-pressed="false">Голосовые</button><button class="dg-btn toggle" aria-pressed="false">Ссылки</button></div>
<div class="grid"><i></i><i></i><i></i><i></i><i></i><i></i></div>'''
S['05-profile'] = screen('light', PROFILE)

def li(icon, title, sub=None, tail='sw', lead='', on=True):
    s = f'<span>{sub}</span>' if sub else ''
    t = {'sw': f'<input type="checkbox" class="dg-sw"{" checked" if on else ""}>', 'chev': ic('chevron_right')}.get(tail, f'<span class="val">{tail}</span>')
    return f'<div class="dg-li"><span class="lead {lead}">{ic(icon, 1 if lead=="t" else 0)}</span><span class="tx"><b>{title}</b>{s}</span>{t}</div>'

SETTINGS = f'''<div class="dg-appbar" style="padding:0 4px 8px"><div class="row"><button class="dg-ib" aria-label="Назад">{ic("arrow_back")}</button><span class="grow"></span><button class="dg-ib" aria-label="Поиск">{ic("search")}</button></div><h1 class="hero">Delusha</h1></div>
<div class="setwrap">
<div class="dg-list"><span class="hd">Приватность</span>{li("visibility_off","Режим призрака",lead="t")}{li("done_all","Читать после ответа")}{li("auto_stories","Смотреть истории тайно",on=False)}</div>
<div class="dg-list"><span class="hd">История</span>{li("delete_history","Удалённые сообщения","1 284 сообщения, 212 МБ","chev",lead="e")}{li("history_edu","Сохранять правки")}</div>
<div class="dg-list"><span class="hd">Чаты</span>{li("swipe","Свайпы по чатам",None,"chev")}{li("archive","Архив",None,"chev")}{li("favorite","Анимации реакций","Полные","chev")}</div>
<div class="dg-list"><span class="hd">Вид</span>{li("blur_on","Размытие")}<div class="dg-li"><span class="lead">{ic("interests")}</span><span class="tx"><b>Форма аватаров</b><span class="shp"><i class="sh-circle"></i><i class="sh-cookie9 on"></i><i class="sh-clover4"></i><i class="sh-flower8"></i><i class="sh-sunny"></i><i class="sh-pent"></i></span></span></div><div class="dg-li"><span class="lead">{ic("palette")}</span><span class="tx"><b>Цвет</b><span class="seeds"><i class="on" style="--s:oklch(0.62 0.16 295)"></i><i style="--s:oklch(0.62 0.13 200)"></i><i style="--s:oklch(0.62 0.15 150)"></i><i style="--s:oklch(0.65 0.16 45)"></i><i style="--s:oklch(0.6 0.17 350)"></i><i class="wall">{ic("wallpaper",0,18)}</i></span></span></div></div>
</div>'''
S['06-settings'] = screen('dark', SETTINGS, 'set')

GHOSTSCR = f'''<div class="gwash">{appbar_chats(on=True)}</div>''' + CHIPS + f'<div class="dg-list-chats">{CHATS}</div>' + NAV
S['07-ghost-amoled'] = screen('amoled', GHOSTSCR)

ACC = f'''<div class="dg-scrim zs"></div><div class="dg-sheet accsheet"><i class="handle"></i>
 <div class="dg-accs">
  <div class="dg-acc"><span class="dg-av s sh-cookie9">Д</span><span class="tx">Деля<span>@delusha</span></span>{ic("check_circle",1)}</div>
  <div class="dg-acc"><span class="dg-av s sc sh-pent">Р</span><span class="tx">Работа<span>@delusha_work</span></span><span class="dg-badge">3</span></div>
  <div class="dg-acc"><span class="dg-av s" style="--av-bg:var(--surface-container-highest);--av-fg:var(--on-surface-variant)">{ic("add")}</span><span class="tx">Добавить аккаунт</span></div>
 </div>
 <div class="dg-tiles">
  <button class="dg-tile"><span class="ti sh-cookie9">{ic("person",1)}</span>Контакты</button>
  <button class="dg-tile"><span class="ti sh-clover4">{ic("call",1)}</span>Звонки</button>
  <button class="dg-tile"><span class="ti sh-flower8">{ic("bookmark",1)}</span>Избранное</button>
  <button class="dg-tile t"><span class="ti sh-sunny">{ic("auto_awesome",1)}</span>Delusha</button>
  <button class="dg-tile"><span class="ti sh-pent">{ic("dark_mode",1)}</span>Тема</button>
  <button class="dg-tile"><span class="ti sh-cookie12">{ic("settings",1)}</span>Настройки</button>
 </div>
</div>'''
S['08-account'] = screen('light', appbar_chats() + CHIPS + f'<div class="dg-list-chats">{CHATS}</div>' + NAV + ACC)

CSS = r"""
html,body{margin:0;background:#202024}
body{display:flex;flex-wrap:wrap;gap:40px;padding:40px;font-family:var(--font-sans)}
.screen{position:relative;width:412px;height:915px;overflow:hidden;background:var(--surface);color:var(--on-surface);display:flex;flex-direction:column;font-size:14px;line-height:20px}
.sb{height:36px;flex:none;display:flex;align-items:center;justify-content:space-between;padding:0 24px 0 28px;font:600 14px/1 var(--font-sans);color:var(--on-surface)}
.cam{width:12px;height:12px;border-radius:50%;background:#0b0b0d}
.sbi{display:flex;gap:4px}
.gb{position:absolute;left:0;right:0;bottom:0;height:24px;display:flex;justify-content:center;align-items:center;z-index:20}
.gb i{width:108px;height:4px;border-radius:2px;background:var(--on-surface);opacity:.55}
.dg-list-chats{padding:2px 8px 0;display:flex;flex-direction:column}
.dg-chat .mute{font-size:16px!important;color:var(--on-surface-variant)!important}
.fade{position:absolute;left:0;right:0;bottom:0;height:150px;background:linear-gradient(transparent,var(--surface) 72%);z-index:8}
.nav{position:absolute;left:12px;right:12px;bottom:32px;z-index:9}
.chat{background:var(--surface)}
.chatmsgs{flex:1;justify-content:flex-end;padding:12px 14px}
.dg-b.photo{padding:4px;width:236px}
.dg-b.photo .ph{display:block;height:150px;border-radius:16px 16px 6px 6px;background:radial-gradient(circle at 30% 30%,oklch(0.85 0.1 80),transparent 60%),linear-gradient(135deg,oklch(0.7 0.13 300),oklch(0.62 0.14 200))}
.dg-b.photo .cap{display:block;padding:6px 10px 4px}
.comp{padding:6px 10px 34px}
.zs{z-index:10}
.sb{position:relative;z-index:13}
.chat .sb{position:absolute;top:0;left:0;right:0;z-index:13}
.chead{position:absolute;left:0;right:0;top:0;z-index:6;padding-top:36px;border-radius:0 0 28px 28px}
.chatmsgs{position:absolute;inset:0;padding:112px 14px 100px!important;overflow:hidden;z-index:1}
.comp{position:absolute;left:0;right:0;bottom:0;z-index:6}
.carx{position:absolute;left:0;right:0;top:300px;height:200px;z-index:12}
.cv{position:absolute;top:0;width:240px;display:flex;flex-direction:column;align-items:flex-start;gap:6px}
.cv .tm{font:600 12px/16px var(--font-sans);padding:4px 10px;border-radius:12px;background:var(--surface-glass);color:var(--on-surface)}
.cv .dg-b{max-width:none;width:100%;box-shadow:var(--elevation-2)}
.accsheet{position:absolute;left:0;right:0;bottom:0;z-index:12;padding-bottom:40px!important}
.lifted{position:absolute;right:14px;top:268px;z-index:12;box-shadow:var(--elevation-3);max-width:80%}
.ctx{position:absolute;right:14px;top:206px;z-index:12;width:auto;align-items:flex-end}
.ctxm{position:absolute;right:14px;top:372px;z-index:12;width:236px}
.sheet{position:absolute;left:0;right:0;bottom:0;z-index:12;background:var(--surface-container-low);border-radius:28px 28px 0 0;padding:10px 16px 40px;display:flex;flex-direction:column;gap:14px;box-shadow:var(--elevation-3)}
.handle{align-self:center;width:32px;height:4px;border-radius:2px;background:var(--on-surface-variant);opacity:.5}
.sh-h{display:flex;align-items:center;gap:8px;padding:4px 4px 0}
.sh-h div{flex:1}
.sh-h b{display:block;font:400 24px/32px var(--font-sans)}
.sh-h div span{display:block;font:400 14px/20px var(--font-sans);color:var(--on-surface-variant)}
.vers{display:flex;flex-direction:column;gap:2px}
.ver{display:grid;grid-template-columns:48px 1fr auto;align-items:center;gap:8px;background:var(--surface-container-high);padding:14px 16px;border-radius:6px}
.ver:first-child{border-radius:22px 22px 6px 6px}.ver:last-child{border-radius:6px 6px 22px 22px}
.ver .vt{font:600 13px/16px var(--font-sans);color:var(--on-surface-variant);font-variant-numeric:tabular-nums}
.ver .vb{font:400 16px/22px var(--font-sans)}
.ver:not(.cur) .vb{color:var(--on-surface-variant);text-decoration:line-through;text-decoration-color:color-mix(in srgb,var(--on-surface-variant) 60%,transparent)}
.ver .vk{font:600 12px/16px var(--font-sans);color:var(--on-surface-variant)}
.ver.cur{background:var(--tertiary-container);color:var(--on-tertiary-container)}
.ver.cur .vt,.ver.cur .vk{color:var(--on-tertiary-container)}
.sh-btns{display:flex;gap:8px}.sh-btns .dg-btn{flex:1}
.prof{display:flex;flex-direction:column;align-items:center;padding:0 16px;text-align:center}
.prof .dg-av.xl{width:132px;height:132px;font-size:48px}
.prof h2{margin:14px 0 0;font:800 28px/32px var(--font-sans);letter-spacing:-.5px;font-variation-settings:"wdth" 120,"opsz" 32}
.prof .st{font:600 14px/20px var(--font-sans);color:var(--primary);margin-top:2px}
.dg-li.info{min-height:60px}
.dg-li.info>.dg-ic{color:var(--primary)}
.tnum{font-variant-numeric:tabular-nums}
.grid{display:grid;grid-template-columns:repeat(3,1fr);gap:3px;padding:0 16px}
.grid i{aspect-ratio:1;border-radius:6px;display:block}
.grid i:nth-child(1){border-radius:20px 6px 6px 6px;background:linear-gradient(135deg,oklch(0.75 0.12 300),oklch(0.6 0.15 360))}
.grid i:nth-child(2){background:linear-gradient(160deg,oklch(0.85 0.09 85),oklch(0.66 0.14 40))}
.grid i:nth-child(3){border-radius:6px 20px 6px 6px;background:linear-gradient(200deg,oklch(0.8 0.08 180),oklch(0.55 0.1 230))}
.grid i:nth-child(4){background:linear-gradient(120deg,oklch(0.7 0.1 150),oklch(0.5 0.1 190))}
.grid i:nth-child(5){background:linear-gradient(45deg,oklch(0.62 0.16 25),oklch(0.8 0.1 70))}
.grid i:nth-child(6){background:linear-gradient(300deg,oklch(0.5 0.12 270),oklch(0.78 0.08 220))}
.set .setwrap{padding:8px 16px 0;display:flex;flex-direction:column;gap:18px}
.set .dg-li{min-height:58px;padding:9px 16px}
.shp,.seeds{display:flex!important;gap:8px;margin-top:8px}
.shp i{width:28px;height:28px;background:var(--on-surface-variant);opacity:.4;display:block}
.shp i.on{background:var(--primary);opacity:1}
.seeds i{width:28px;height:28px;border-radius:50%;background:var(--s);display:grid;place-items:center}
.seeds i.on{border-radius:9px;box-shadow:0 0 0 2px var(--surface-container),0 0 0 4px var(--s)}
.dg-li .tx .seeds .dg-ic{display:inline-block;font:normal 18px/1 "Material Symbols Rounded";color:inherit}
.seeds i.wall{background:var(--surface-container-highest);color:var(--on-surface-variant)}
.gwash{background:color-mix(in srgb,var(--ghost) 70%,var(--surface));color:var(--on-ghost);border-radius:0 0 28px 28px;padding-bottom:14px}
.gwash .dg-appbar h1{color:var(--on-ghost)}
.gwash .dg-ib{color:var(--on-ghost)}
.dg-ghost.on{background:var(--tertiary);color:var(--on-tertiary)}
.gnote{margin:4px 16px 0;display:flex;gap:10px;align-items:center;padding:10px 14px;border-radius:16px;background:color-mix(in srgb,var(--on-ghost) 10%,transparent);font:400 13px/18px var(--font-sans)}
"""

FONTS = '<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Roboto+Flex:opsz,wdth,wght@8..144,25..151,100..1000&display=swap"><link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Rounded:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200&display=block">'
html = f'<!doctype html><html><head><meta charset="utf-8">{FONTS}<style>{tokcss}{bundle}{CSS}</style></head><body>' + ''.join(f'<div id="s-{k}">{v_}</div>' for k, v_ in S.items()) + '</body></html>'
open(os.path.join(D, 'screens.html'), 'w').write(html)
print(list(S))
