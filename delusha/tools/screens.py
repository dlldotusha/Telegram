import json, os, math, random, sys
D = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, D)
import expressive
from logo import badge_inline, pixels_varied
P = os.path.join(D, 'ds', 'project')
T = json.load(open(os.path.join(P, 'tokens.json')))

def v(x): return f"var(--{x[1:-1]})" if x.startswith('{') else x
th = {}
for t in T['color']['tokens']:
    val = t['value'] if isinstance(t['value'], dict) else {k: t['value'] for k in ('light', 'dark', 'amoled')}
    for k, x in val.items(): th.setdefault(k, []).append(f"--{t['name']}:{v(x)}")
root = ''.join(f"--{t['name']}:{t['value']};" for f in ['spacing', 'radius', 'size', 'state', 'shadow', 'blur'] for t in T[f]['tokens'])
tokcss = f":root{{{root}--font-sans:{T['type']['families']['sans']}}}\n:root{{{';'.join(th['light'])}}}\n" + ''.join(
    f"[data-theme={k}]{{{';'.join(vals)}}}\n" for k, vals in th.items())
bundle = '\n'.join(l for l in open(os.path.join(P, 'components', 'bundle.css')).read().split('\n') if not l.startswith('@import'))

def ic(n, f=False, s=None):
    st = f' style="font-size:{s}px"' if s else ''
    return f'<span class="dg-ic{" fill" if f else ""}"{st}>{n}</span>'

SB = f'''<div class="sb"><span>9:41</span><span class="cam"></span><span class="sbi">{ic("signal_cellular_alt",1,17)}{ic("wifi",1,17)}{ic("battery_full",1,17)}</span></div>'''
GB = '<div class="gb"><i></i></div>'
def screen(theme, body, cls=''):
    return f'<div class="screen {cls}" data-theme="{theme}">{SB}{body}{GB}</div>'

random.seed(11)
SH = ['sh-cookie9', 'sh-clover4', 'sh-flower8', 'sh-sunny', 'sh-pent', 'sh-cookie4', 'sh-cookie12', 'sh-cookie7']
def pattern(n, w, h, smin, smax, cls='pat'):
    return ''.join(f'<i class="{cls} {SH[k % 8]}" style="left:{random.randint(-30, w-20)}px;top:{random.randint(-30, h-20)}px;width:{(s:=random.randint(smin, smax))}px;height:{s}px;transform:rotate({random.randint(0, 90)}deg)"></i>' for k in range(n))

WALL = '<div class="dg-wall">' + ''.join(f'<i class="{SH[k % 8]}" style="left:{random.randint(-10,390)}px;top:{random.randint(20,880)}px;width:{(s:=random.randint(26,54))}px;height:{s}px;transform:rotate({random.randint(0,90)}deg)"></i>' for k in range(26)) + '</div>'

# avatar helpers: Delusha users -> their shape + persona colours; regular -> circle
def dav(letter, persona, shape, cls=''):
    return f'<span class="dg-av persona persona-{persona} {shape} {cls}">{letter}</span>'
def rav(letter, tone, cls=''):
    return f'<span class="dg-av {tone} reg {cls}">{letter}</span>'

SUP = badge_inline('sup', 18)
USERB = badge_inline('user', 20)
def row(av, name, prev, time, extra='', badge='', unread=False, online=False, dg=False):
    sup = 'persona' in av and 'persona-me' not in av or 'persona-me' in av
    if online: av = f'<span class="dg-avwrap">{av}<span class="{"dgon" if (dg or "persona" in av) else "on"}"></span></span>'
    if sup: name = name + SUP
    return f'<div class="dg-chat{" unread" if unread else ""}">{av}<div class="mid"><div class="nm">{name}{extra}</div><div class="pv">{prev}</div></div><div class="rt">{time}{badge}</div></div>'

CHATS = '<div class="dg-cgroup">' + ''.join([
    row(dav('А', 'anya', 'sh-cookie12'), 'Аня', '<em>печатает…</em>', '9:40', online=True),
    row(rav('М', 'e'), 'Мама', 'Купи хлеба по дороге', '9:38', badge='<span class="dg-badge">2</span>', unread=True, online=True),
    row(dav('Л', 'lyokha', 'sh-clover4'), 'Лёха', ic('mic') + 'Голосовое, 0:42', '8:55', badge='<span class="dg-badge">1</span>', unread=True),
    row(rav(ic('campaign', 1), 'sc'), 'Delusha Dev', 'Сборка 0.1 собрана, 48 МБ', 'Пн', ' ' + ic('verified', 1) + '<span class="dg-ic mute">volume_off</span>', badge='<span class="dg-badge muted">14</span>'),
    row(rav('К', 't'), 'Кофе по пятницам', '<span class="who">Даша:</span> кто завтра?', 'Вс', badge='<span class="dg-badge mention">@</span>', unread=True),
    row(rav('Д', 't'), 'Даша', 'ок, до завтра', 'Сб', online=True, dg=True),
    row(rav('Ю', ''), 'Юля', ic('photo') + 'Фото', 'Пт'),
]) + '</div>'

PINS = f'''<div class="dg-pins">
 <div class="dg-pin"><span class="dg-av sh-cookie4" style="--av-bg:var(--primary);--av-fg:var(--on-primary)">{ic("bookmark",1,30)}</span>Избранное</div>
 <div class="dg-pin">{dav('А','anya','sh-cookie12')}<span>Аня{badge_inline('sup',13)}</span></div>
 <div class="dg-pin">{rav('М','e')}<span class="dg-badge">2</span>Мама</div>
 <div class="dg-pin">{dav('Л','lyokha','sh-clover4')}<span class="dg-badge">1</span><span>Лёха{badge_inline('sup',13)}</span></div>
 <div class="dg-pin">{rav('Р','sc')}Работа</div>
</div>'''

SEG = '''<div class="dg-seg"><button aria-pressed="true">Все <span class="n">12</span></button><button aria-pressed="false">Личные</button><button aria-pressed="false">Группы <span class="n">3</span></button><button aria-pressed="false">Каналы</button><button aria-pressed="false" style="border-radius:8px 20px 20px 8px">Боты</button></div>'''

def head(ghost_on=False, show_ghost=True):
    g = f'<button class="dg-ghost{" on" if ghost_on else ""}">{ic("visibility_off",1)}Призрак</button>' if show_ghost else ''
    return f'<div class="dg-xhead" style="padding-top:2px"><span class="grow"></span>{g}</div>'

NAV = f'''<div class="nav"><div class="dg-bottombar"><button class="bar dg-glass">{dav('Д','me','sh-cookie9')}{ic("search")}<span class="ph">Поиск</span></button><span class="dg-fabwrap"><button class="dg-fabx sh-cookie9" aria-label="Новый чат">{ic("edit",1)}</button></span></div></div>'''

CHATS_SCREEN = head() + PINS + SEG + CHATS + NAV

def wave(width=150, progress=.4, amp=3.5, period=20):
    pts = [f"{x+2},{12+amp*math.sin(2*math.pi*x/period):.2f}" for x in range(0, width + 1, 2)]
    cut = int(width * progress)
    act = "M" + " L".join(p for p in pts if float(p.split(',')[0]) - 2 <= cut)
    return f'<svg class="dg-wave" width="{width+4}" height="24" viewBox="0 0 {width+4} 24"><path class="trk" d="M{cut+8},12 L{width+2},12"/><path class="act" d="{act}"/></svg>'

CHATHEAD = f'''<div class="chead dg-glass"><div class="dg-appbar small" style="padding-top:0;background:transparent"><button class="dg-ib" aria-label="Назад">{ic("arrow_back")}</button>{dav('А','anya','sh-cookie12','s')}<span class="ttl" style="font-weight:800;font-variation-settings:'wdth' 115">Аня Соколова{SUP}<small>в сети</small></span><button class="dg-ib" aria-label="Звонок">{ic("call")}</button><button class="dg-ib" aria-label="Ещё">{ic("more_vert")}</button></div></div>'''
A = 'persona persona-anya'
MSGS = f'''<div class="dg-msgs chatmsgs">
 <div class="dg-b out first">Смотри, что я собрал</div>
 <div class="dg-b out last">Форк телеги, весь на пружинах<span class="meta">9:20 {ic("done_all")}</span></div>
 <span class="dg-day">Сегодня</span>
 <div class="dg-b in first {A}">Ты видела новый дизайн?</div>
 <div class="dg-b in {A}">Аватарки как печеньки</div>
 <div class="dg-b in last photo {A}"><span class="ph"></span><span class="cap">Вот так у меня выглядит<span class="meta">9:31</span></span></div>
 <div class="dg-b out first"><span class="quote"><b>Аня</b>Аватарки как печеньки</span>Это Material Expressive</div>
 <div class="dg-b out last">И удалённые теперь не пропадают<span class="meta">9:32 {ic("done_all")}</span><br><span class="react">❤ 2</span></div>
 <div class="dg-b in first deleted"><span class="tag">{ic("delete")}удалено</span>ой, это не тебе<span class="meta">9:33</span></div>
 <div class="dg-b in last edited {A}"><span class="tag">{ic("history")}изменено, 2 раза</span>Приду к восьми<span class="meta">9:40</span></div>
 <div class="dg-b out first last dg-voice"><span class="play sh-cookie7">{ic("play_arrow",1)}</span>{wave()}<span class="len">0:17</span></div>
</div>'''
COMPOSER = f'''<div class="dg-composer comp"><button class="dg-ib tonal att" aria-label="Вложение">{ic("add",0,26)}</button><div class="dg-field dg-glass"><button class="dg-ib" aria-label="Эмодзи">{ic("mood")}</button><span class="ph">Сообщение</span><button class="dg-ib" aria-label="Камера">{ic("photo_camera")}</button></div><button class="dg-send sh-cookie9" aria-label="Голосовое">{ic("mic",1)}</button></div>'''
CHAT = WALL + MSGS + CHATHEAD + COMPOSER

MENU = f'''<div class="dg-menuwrap ctx"><div class="dg-reacts"><span>❤</span><span>👍</span><span>😂</span><span>🔥</span><span>🥲</span><span style="font-size:20px">{ic("expand_more")}</span></div></div>
<div class="dg-menu ctxm">
 <div class="grp"><div class="mi">{ic("reply")}Ответить</div><div class="mi">{ic("content_copy")}Копировать</div><div class="mi">{ic("translate")}Перевести</div><div class="mi">{ic("forward")}Переслать</div></div>
 <div class="grp"><div class="mi">{ic("repeat")}Повторить</div><div class="mi">{ic("push_pin")}Закрепить</div><div class="mi">{ic("data_object")}Детали</div></div>
 <div class="grp"><div class="mi danger">{ic("delete")}Удалить</div></div>
</div>
<div class="dg-b out first last lifted">И удалённые теперь не пропадают<span class="meta">9:32 {ic("done_all")}</span><br><span class="react">❤ 2</span></div>'''

CAR = f'''<div class="dg-scrim zs"></div><div class="carv">
 <div class="cv" style="top:0;transform:scale(.9);opacity:.45"><span class="tm">9:35</span><div class="dg-b in first last {A}">Приду к семи</div></div>
 <div class="cv" style="top:150px"><span class="tm">9:37</span><div class="dg-b in first last {A}">Приду к половине восьмого</div></div>
 <div class="cv" style="top:300px;transform:scale(.9);opacity:.45"><span class="tm">9:40</span><div class="dg-b in first last {A}">Приду к восьми</div></div>
 <div class="dg-dots vdots"><i></i><i class="on"></i><i></i></div>
</div>'''

PROFILE = f'''<div class="persona persona-anya">
<div class="dg-hero">{pattern(14, 412, 250, 50, 120)}<div class="top"><button class="dg-ib" aria-label="Назад">{ic("arrow_back")}</button><button class="dg-ib" aria-label="Ещё">{ic("more_vert")}</button></div></div>
<div class="dg-ring sh-cookie12" style="width:172px;height:172px">{dav('А','anya','sh-cookie12')}</div>
<h2 class="dg-pname">Аня Соколова{badge_inline('sup',28)}</h2>
<div class="dg-pstat">в сети</div>
<div style="padding:16px 16px 0"><div class="dg-bgroup tall"><button class="dg-btn filled" aria-pressed="true">{ic("chat_bubble",1)}Чат</button><button class="dg-btn tonal">{ic("notifications_off")}Звук</button><button class="dg-btn tonal">{ic("call")}Звонок</button><button class="dg-btn tonal">{ic("videocam")}Видео</button></div></div>
<div class="dg-bento" style="padding:12px 16px 0">
 <div class="dg-card span2 acc"><b>Рисую интерфейсы, пью матчу</b></div>
 <div class="dg-card"><span>Ник</span><b>@anya_sk</b></div>
 <div class="dg-card"><span>ID</span><b class="tnum">482 913 557</b></div>
 <div class="dg-card span2"><div class="row" style="justify-content:space-between"><div><span>Звонки</span><b>Вчера, 12 мин</b></div><div style="display:flex;gap:6px;color:var(--p-acc)">{ic("call_received",1)}{ic("call_made",1)}{ic("call_missed",1)}</div></div></div>
</div>
<div class="dg-mbento" style="padding:12px 16px 0"><i class="big" style="background:linear-gradient(135deg,oklch(0.75 0.10 200),oklch(0.62 0.12 250))"></i><i style="background:linear-gradient(160deg,oklch(0.85 0.08 85),oklch(0.66 0.12 40))"></i><i class="sh-cookie9" style="background:linear-gradient(200deg,oklch(0.8 0.07 160),oklch(0.55 0.09 200))"></i><i style="background:linear-gradient(45deg,oklch(0.65 0.12 25),oklch(0.82 0.08 70))"></i><i style="border-radius:10px 10px 32px 10px;background:linear-gradient(300deg,oklch(0.5 0.1 270),oklch(0.78 0.07 220))"></i></div>
</div>'''

def li(icon, title, sub=None, tail='sw', lead='', on=True):
    s = f'<span>{sub}</span>' if sub else ''
    t = {'sw': f'<input type="checkbox" class="dg-sw"{" checked" if on else ""}>', 'chev': ic('chevron_right')}.get(tail, f'<span class="val">{tail}</span>')
    return f'<div class="dg-li"><span class="lead {lead}">{ic(icon, 1 if lead=="t" else 0)}</span><span class="tx"><b>{title}</b>{s}</span>{t}</div>'

CLUSTER = '''<div class="dg-cluster" style="right:-24px;top:-6px;width:210px;height:150px">
 <i class="sh-cookie9" style="left:80px;top:0;width:130px;height:130px;background:var(--primary-container)"></i>
 <i class="sh-clover4" style="left:20px;top:56px;width:80px;height:80px;background:var(--tertiary-container)"></i>
 <i class="sh-sunny" style="left:150px;top:96px;width:48px;height:48px;background:var(--primary)"></i></div>'''

SETTINGS = f'''<div style="position:relative">{CLUSTER}<div class="dg-appbar" style="padding:0 4px 4px;background:transparent;position:relative"><div class="row"><button class="dg-ib" aria-label="Назад">{ic("arrow_back")}</button></div><svg class="dg-logo" viewBox="0 0 90 110" width="118" height="144" style="margin:10px 20px 6px">{pixels_varied(9,11,3,10,seed=7,cls=('lp1','lp2','lp3'))}</svg></div></div>
<div style="padding:12px 12px 0;display:flex;flex-direction:column;gap:14px">
 <div class="dg-ttiles"><div class="dg-ttile on"><span class="ti sh-cookie9">{ic("visibility_off",1)}</span><div class="bt"><b>Призрак</b><input type="checkbox" class="dg-sw" checked></div></div><div class="dg-ttile on2"><span class="ti sh-clover4">{ic("history",1)}</span><div class="bt"><b>История</b><input type="checkbox" class="dg-sw" checked></div></div></div>
 <div class="dg-list"><div class="dg-li"><span class="lead" style="background:none;width:44px">{dav('Д','me','sh-cookie9','s')}</span><span class="tx"><b>Деля{SUP}</b><span>@delusha</span></span>{ic("chevron_right")}</div><div class="dg-li"><span class="lead e">{ic("delete_history")}</span><span class="tx"><b>Удалённые сообщения</b><span>1 284 сообщения, 212 МБ</span></span>{ic("chevron_right")}</div></div>
 <div class="dg-list">{li("done_all","Читать после ответа")}{li("auto_stories","Смотреть истории тайно",on=False)}</div>
 <div class="dg-list">{li("swipe","Свайпы по чатам",None,"chev")}{li("archive","Архив",None,"chev")}{li("favorite","Анимации реакций","Полные","chev")}</div>
</div>'''

EDITOR = f'''<div class="dg-appbar" style="padding:0 4px 0;background:transparent"><div class="row"><button class="dg-ib" aria-label="Назад">{ic("arrow_back")}</button></div></div>
<div class="persona persona-me" style="margin:0 12px;border-radius:32px;overflow:hidden;background:var(--surface-container-low)">
 <div class="dg-hero" style="height:120px;border-radius:0 0 28px 28px">{pattern(9, 388, 120, 30, 70)}</div>
 <div style="display:flex;align-items:flex-end;gap:12px;padding:0 16px 14px;margin-top:-44px;position:relative;z-index:2">
  <span class="dg-ring sm sh-cookie9" style="width:92px;height:92px;margin:0">{dav('Д','me','sh-cookie9')}</span>
  <div style="padding-bottom:6px"><div style="font:900 24px/26px var(--font-sans);font-variation-settings:'wdth' 132">Деля{badge_inline('sup',20)}</div><div style="font:600 13px/18px var(--font-sans);color:var(--p-acc)">@delusha</div></div>
 </div>
</div>
<div class="dg-secs" style="margin-top:12px">
 <div class="dg-sec"><div class="dg-pick"><i class="sh-circle"></i><i class="sh-cookie9 on"></i><i class="sh-clover4"></i><i class="sh-flower8"></i><i class="sh-sunny"></i><i class="sh-pent"></i></div></div>
 <div class="dg-sec"><div class="dg-pick col"><i class="sh-cookie9 on" style="background:oklch(0.6 0.13 295)"></i><i class="sh-cookie9" style="background:oklch(0.6 0.10 190)"></i><i class="sh-cookie9" style="background:oklch(0.6 0.12 140)"></i><i class="sh-cookie9" style="background:oklch(0.64 0.13 50)"></i><i class="sh-cookie9" style="background:oklch(0.6 0.14 0)"></i><i class="sh-cookie9" style="background:oklch(0.6 0.12 240)"></i><i class="sh-cookie9" style="background:oklch(0.5 0.02 295)"></i></div></div>
 <div class="dg-sec persona persona-me"><div class="dg-bgs"><div class="on">{pattern(5,120,76,20,40,'')}</div><div style="background:radial-gradient(circle at 20% 20%,var(--p-pat),transparent 60%),radial-gradient(circle at 90% 90%,var(--p-acc),transparent 55%),var(--p-c)"></div><div></div></div></div>
 <div class="dg-sec"><div class="dg-anim"><div><i class="sh-cookie9"></i>{ic("pause",1)}</div><div class="on"><i class="sh-cookie9"></i>{ic("air")}</div><div><i class="sh-clover4"></i>{ic("animation")}</div><div><i class="sh-flower8"></i>{ic("autorenew")}</div></div></div>
 <div class="dg-sec"><div class="dg-names"><div style="font-variation-settings:'wdth' 60">Аа</div><div style="font-variation-settings:'wdth' 100">Аа</div><div class="on" style="font-variation-settings:'wdth' 151">Аа</div></div></div>
</div>'''

NEWCHAT = f'''<div class="dg-appbar" style="padding:0 4px 0;background:transparent"><div class="row"><button class="dg-ib" aria-label="Назад">{ic("arrow_back")}</button></div></div>
<div class="dg-search dg-glass" style="margin:0 16px">{ic("search")}<span class="ph">Кому</span></div>
<div class="dg-ntiles"><div class="nt"><span class="ti sh-cookie9">{ic("group",1)}</span>Группа</div><div class="nt"><span class="ti sh-clover4">{ic("campaign",1)}</span>Канал</div><div class="nt"><span class="ti sh-flower8">{ic("lock",1)}</span>Секретный</div><div class="nt"><span class="ti sh-pent">{ic("person_add",1)}</span>Контакт</div></div>
<div class="dg-cgroup">
 {row(dav('А','anya','sh-cookie12'),'Аня Соколова','в сети','',online=True)}
 {row(dav('Л','lyokha','sh-clover4'),'Лёха','был 5 минут назад','')}
 {row(rav('Д','t'),'Даша','была вчера','')}
 {row(rav('В','t'),'Вика','была недавно','')}
 {row(rav('И','sc'),'Игорь','был на этой неделе','')}
 {row(rav('М','e'),'Мама','в сети','',online=True)}
 {row(rav('Ю',''),'Юля','была недавно','')}
</div>'''

MYPROFILE = f'''<div class="persona persona-me">
<div class="dg-hero">{pattern(14, 412, 250, 50, 120)}<div class="top"><button class="dg-ib" aria-label="Назад">{ic("arrow_back")}</button><span style="display:flex;gap:8px"><button class="dg-ib" aria-label="Изменить профиль">{ic("edit")}</button><button class="dg-ib" aria-label="Настройки">{ic("settings")}</button></span></div></div>
<div class="dg-ring sh-cookie9" style="width:172px;height:172px">{dav('Д','me','sh-cookie9')}</div>
<h2 class="dg-pname">Деля{badge_inline('sup',28)}</h2>
<div class="dg-pstat">@delusha</div>
<div class="dg-accrow"><div class="ac on">{dav('Д','me','sh-cookie9','s')}{ic("check",1,18)}</div><div class="ac">{rav('Р','sc','s')}<span class="dg-badge">3</span></div><div class="ac add">{ic("add")}</div></div>
<div class="dg-bento" style="padding:14px 16px 0">
 <div class="dg-card span2 acc"><b>Собираю свой телеграм</b></div>
 <div class="dg-card"><span>Избранное</span><b>128</b></div>
 <div class="dg-card"><span>ID</span><b class="tnum">731 004 218</b></div>
</div>
</div>'''

def call(av, name, kind, when, dur):
    icn = {'in': ('call_received', 'var(--primary)'), 'out': ('call_made', 'var(--primary)'), 'miss': ('call_missed', 'var(--error)')}[kind]
    return f'<div class="dg-chat">{av}<div class="mid"><div class="nm">{name}</div><div class="pv"><span class="dg-ic fill" style="color:{icn[1]}">{icn[0]}</span>{when}</div></div><div class="rt">{dur}<span class="dg-ic fill" style="font-size:22px;color:var(--primary)">call</span></div></div>'
SEARCH = f'''<div class="dg-search dg-glass" style="margin:4px 16px 0">{ic("arrow_back")}<span class="ph" style="color:var(--on-surface)">а</span>{ic("close")}</div>
<div class="dg-seg"><button aria-pressed="false">Чаты</button><button aria-pressed="false">Сообщения</button><button aria-pressed="false">Медиа</button><button aria-pressed="true" style="border-radius:20px">{ic("call",1,18)}Звонки</button></div>
<div class="dg-cgroup">
 {call(dav('А','anya','sh-cookie12'),'Аня Соколова','in','Вчера, 21:14','12:03')}
 {call(rav('М','e'),'Мама','miss','Вчера, 18:02','')}
 {call(dav('А','anya','sh-cookie12'),'Аня Соколова','out','Пн, 10:40','3:17')}
 {call(dav('Л','lyokha','sh-clover4'),'Лёха','in','Вс, 23:55','48:20')}
 {call(rav('А','t'),'Артём','out','12 окт','0:42')}
</div>'''

LOCKED = EDITOR.replace('<div class="dg-secs" style="margin-top:12px">','<div class="lockcta"><button class="dg-btn filled m">'+ic("favorite",1)+'Поддержать</button></div><div class="dg-secs locked" style="margin-top:12px">')
DASHA = f'''<div class="dg-hero" style="height:200px;background:var(--surface-container)"><div class="top"><button class="dg-ib" aria-label="Назад">{ic("arrow_back")}</button><button class="dg-ib" aria-label="Ещё">{ic("more_vert")}</button></div></div>
<div class="dg-ring" style="width:168px;height:168px;border-radius:50%;margin-top:-84px">{rav('Д','t')}</div>
<h2 class="dg-pname">Даша{USERB}</h2>
<div class="dg-pstat" style="color:var(--primary)">в сети</div>
<div style="padding:16px 16px 0"><div class="dg-bgroup tall"><button class="dg-btn filled" aria-pressed="true">{ic("chat_bubble",1)}Чат</button><button class="dg-btn tonal">{ic("notifications_off")}Звук</button><button class="dg-btn tonal">{ic("call")}Звонок</button><button class="dg-btn tonal">{ic("videocam")}Видео</button></div></div>
<div class="dg-bento" style="padding:12px 16px 0"><div class="dg-card"><span>Ник</span><b>@dasha_m</b></div><div class="dg-card"><span>ID</span><b class="tnum">615 220 093</b></div></div>'''
S = {
 '01-chats': screen('light', CHATS_SCREEN),
 '02-chat': screen('light', CHAT, 'chat'),
 '03-menu': screen('dark', CHAT + '<div class="dg-scrim zs"></div>' + MENU, 'chat'),
 '04-history': screen('dark', CHAT + CAR, 'chat'),
 '05-profile': screen('light', PROFILE),
 '06-settings': screen('dark', SETTINGS),
 '07-ghost-amoled': screen('amoled', '<div class="gwash">' + head(True) + PINS + '</div>' + SEG + CHATS + NAV),
 '08-new-chat': screen('light', NEWCHAT),
 '09-my-profile': screen('light', MYPROFILE),
 '10-profile-editor': screen('dark', EDITOR),
 '11-search-calls': screen('dark', SEARCH),
 '12-editor-locked': screen('light', LOCKED),
 '13-profile-user': screen('light', DASHA),
}

CSS = r"""
html,body{margin:0;background:#202024}
body{display:flex;flex-wrap:wrap;gap:40px;padding:40px;font-family:var(--font-sans)}
.screen>*{flex-shrink:0}
.screen{position:relative;width:412px;height:915px;overflow:hidden;background:var(--surface);color:var(--on-surface);display:flex;flex-direction:column;font-size:14px;line-height:20px}
.sb{height:36px;flex:none;display:flex;align-items:center;justify-content:space-between;padding:0 24px 0 28px;font:600 14px/1 var(--font-sans);color:var(--on-surface);position:relative;z-index:13}
.cam{width:12px;height:12px;border-radius:50%;background:#0b0b0d}
.sbi{display:flex;gap:4px}
.gb{position:absolute;left:0;right:0;bottom:0;height:24px;display:flex;justify-content:center;align-items:center;z-index:20}
.gb i{width:108px;height:4px;border-radius:2px;background:var(--on-surface);opacity:.55}
.dg-chat .mute{font-size:16px!important;color:var(--on-surface-variant)!important}
.nav{position:absolute;left:12px;right:12px;bottom:32px;z-index:9}
.chat .sb{position:absolute;top:0;left:0;right:0}
.chead{position:absolute;left:0;right:0;top:0;z-index:6;padding-top:36px;border-radius:0 0 28px 28px}
.chatmsgs{position:absolute;inset:0;padding:112px 14px 100px!important;overflow:hidden;z-index:1;justify-content:flex-end}
.dg-b.photo{padding:4px;width:236px}
.dg-b.photo .ph{display:block;height:150px;border-radius:22px 22px 8px 8px;background:radial-gradient(circle at 30% 30%,oklch(0.85 0.1 80),transparent 60%),linear-gradient(135deg,oklch(0.7 0.11 300),oklch(0.62 0.12 200))}
.dg-b.photo .cap{display:block;padding:6px 10px 4px}
.comp{position:absolute;left:0;right:0;bottom:0;z-index:6;padding:6px 10px 34px;align-items:center}
.comp .att{width:52px;height:52px;border-radius:26px;background:var(--surface-glass);-webkit-backdrop-filter:blur(var(--blur-glass));backdrop-filter:blur(var(--blur-glass));color:var(--on-surface)}
.zs{z-index:10}
.lifted{position:absolute;right:14px;top:268px;z-index:12;box-shadow:var(--elevation-3);max-width:80%}
.ctx{position:absolute;right:14px;top:206px;z-index:12;width:auto;align-items:flex-end}
.ctxm{position:absolute;right:14px;top:372px;z-index:12;width:236px}
.carv{position:absolute;left:0;right:0;top:250px;height:420px;z-index:12}
.carv .cv{left:24px;transform-origin:0 50%}
.vdots{position:absolute;right:22px;top:150px;flex-direction:column}
.vdots i.on{width:8px!important;height:24px}
.dg-ntiles{display:grid;grid-template-columns:repeat(4,1fr);gap:6px;padding:12px 16px}
.dg-ntiles .nt{height:92px;border-radius:24px;background:var(--surface-container);display:flex;flex-direction:column;align-items:center;justify-content:center;gap:8px;font:700 12.5px/16px var(--font-sans)}
.dg-ntiles .ti{width:44px;height:44px;display:grid;place-items:center;background:var(--primary-container);color:var(--on-primary-container)}
.dg-accrow{display:flex;justify-content:center;gap:10px;padding:14px 16px 0}
.dg-accrow .ac{position:relative;height:52px;padding:0 14px 0 6px;border-radius:26px;background:var(--surface-container);display:flex;align-items:center;gap:6px}
.dg-accrow .ac.on{background:var(--p-c);color:var(--p-on)}
.dg-accrow .ac.add{padding:0 14px;color:var(--on-surface-variant)}
.dg-accrow .dg-badge{position:absolute;top:-4px;right:-4px}
.cv{position:absolute;width:240px;display:flex;flex-direction:column;align-items:flex-start;gap:6px}
.cv .tm{font:600 12px/16px var(--font-sans);padding:4px 10px;border-radius:12px;background:var(--surface-glass);color:var(--on-surface)}
.cv .dg-b{max-width:none;width:100%;box-shadow:var(--elevation-2)}
.accsheet{position:absolute;left:0;right:0;bottom:0;z-index:12;padding-bottom:40px!important}
.gwash{background:color-mix(in srgb,var(--ghost) 70%,var(--surface));border-radius:0 0 32px 32px;padding-bottom:4px;margin-bottom:4px}
.gwash .dg-pin{color:var(--on-ghost)}
.gwash .dg-pin .dg-badge{box-shadow:0 0 0 3px color-mix(in srgb,var(--ghost) 70%,var(--surface))}
.dg-ghost.on{background:var(--tertiary);color:var(--on-tertiary)}
.tnum{font-variant-numeric:tabular-nums}
.lockcta{display:flex;justify-content:center;padding:14px 16px 0}
.lockcta .dg-btn{width:100%;height:64px;border-radius:32px;font-size:18px}
.dg-secs.locked{opacity:.38;pointer-events:none;filter:saturate(.4)}
.dg-chat .dg-av{width:52px;height:52px}
"""

FONTS = '<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Roboto+Flex:opsz,wdth,wght@8..144,25..151,100..1000&display=swap"><link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Rounded:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200&display=block">'
html = f'<!doctype html><html><head><meta charset="utf-8">{FONTS}<style>{tokcss}{bundle}{expressive.all_css()}{CSS}</style></head><body>' + ''.join(f'<div id="s-{k}">{v_}</div>' for k, v_ in S.items()) + '</body></html>'
open(os.path.join(D, 'screens.html'), 'w').write(html)
print(list(S))
