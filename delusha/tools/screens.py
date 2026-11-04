# DelushaGram screens laid out after the real Telegram 12.9.2 client (MainTabsActivity, DialogsActivity,
# ChatActivity, ProfileActivity, SettingsActivity, ContactsActivity) with the DelushaGram design on top.
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
tokcss = f":root{{{root}--font-sans:{T['type']['families']['sans']}}}\n:root{{{';'.join(th['light'])}}}\n" + ''.join(f"[data-theme={k}]{{{';'.join(vals)}}}\n" for k, vals in th.items())
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
def pattern(n, w, h, smin, smax):
    return ''.join(f'<i class="pat {SH[k % 8]}" style="left:{random.randint(-30, w-20)}px;top:{random.randint(-30, h-20)}px;width:{(s:=random.randint(smin, smax))}px;height:{s}px;transform:rotate({random.randint(0, 90)}deg)"></i>' for k in range(n))
WALL = '<div class="dg-wall">' + ''.join(f'<i class="{SH[k % 8]}" style="left:{random.randint(-10,390)}px;top:{random.randint(20,880)}px;width:{(s:=random.randint(26,54))}px;height:{s}px;transform:rotate({random.randint(0,90)}deg)"></i>' for k in range(26)) + '</div>'

def logo(size, seed=7):
    return f'<svg class="dg-logo" viewBox="0 0 90 110" width="{size}" height="{size*110/90:.0f}">{pixels_varied(9,11,3,10,seed=seed,cls=("g1","g2","g3"))}</svg>'

def dav(letter, persona, shape, cls=''):
    return f'<span class="dg-av persona persona-{persona} {shape} {cls}">{letter}</span>'
def rav(letter, tone, cls=''):
    return f'<span class="dg-av {tone} reg {cls}">{letter}</span>'
SUP = lambda live=False, size=18: badge_inline('sup', size, live)
USERB = badge_inline('user', 20)

def row(av, name, prev, time, badge='', unread=False, online=False, dg=False, sup=False, pinned=False, extra=''):
    if online: av = f'<span class="dg-avwrap">{av}<span class="{"dgon" if dg else "on"}"></span></span>'
    if sup: name += SUP(online)
    pin = ic('push_pin', 1, 16) if pinned else ''
    return f'<div class="dg-chat{" unread" if unread else ""}{" pinned" if pinned else ""}">{av}<div class="mid"><div class="nm">{name}{extra}</div><div class="pv">{prev}</div></div><div class="rt">{time}<span style="display:flex;gap:4px;align-items:center">{pin}{badge}</span></div></div>'

CHATS = '<div class="dg-cgroup">' + ''.join([
    row(dav('А', 'anya', 'sh-cookie12'), 'Аня', '<em>печатает…</em>', '9:40', online=True, dg=True, sup=True, pinned=True),
    row(rav('М', 'e'), 'Мама', 'Купи хлеба по дороге', '9:38', badge='<span class="dg-badge">2</span>', unread=True, online=True, pinned=True),
    row(dav('Л', 'lyokha', 'sh-clover4'), 'Лёха', ic('mic') + 'Голосовое, 0:42', '8:55', badge='<span class="dg-badge">1</span>', unread=True, sup=True),
    row(rav(ic('campaign', 1), 'sc'), 'Delusha Dev', 'Сборка 0.1 собрана, 48 МБ', 'Пн', badge='<span class="dg-badge muted">14</span>', extra=' ' + ic('verified', 1) + '<span class="dg-ic mute">volume_off</span>'),
    row(rav('К', 't'), 'Кофе по пятницам', '<span class="who">Даша:</span> кто завтра?', 'Вс', badge='<span class="dg-badge mention">@</span>', unread=True),
    row(rav('Д', 't'), 'Даша', 'ок, до завтра', 'Сб', online=True, dg=True),
    row(rav('Ю', ''), 'Юля', ic('photo') + 'Фото', 'Пт'),
]) + '</div>'

def story(av, name, seen=False, mine=False):
    plus = f'<span class="splus">{ic("add",0,16)}</span>' if mine else ''
    return f'<div class="st{" seen" if seen else ""}{" mine" if mine else ""}"><span class="ring">{av}{plus}</span><span class="sn">{name}</span></div>'
STORIES = '<div class="dg-stories">' + story(dav('Д','me','sh-cookie9'), 'Моя история', mine=True) + story(dav('А','anya','sh-cookie12'), 'Аня') + story(rav('В','t'), 'Вика') + story(dav('Л','lyokha','sh-clover4'), 'Лёха', seen=True) + story(rav('И','sc'), 'Игорь', seen=True) + '</div>'

SEG = '''<div class="dg-seg"><button aria-pressed="true">Все <span class="n">12</span></button><button aria-pressed="false">Личные</button><button aria-pressed="false">Группы <span class="n">3</span></button><button aria-pressed="false">Каналы</button><button aria-pressed="false" style="border-radius:8px 20px 20px 8px">Боты</button></div>'''

def topbar(ghost=False):
    g = f'<button class="dg-ghost on">{ic("visibility_off",1)}Призрак</button>' if ghost else ''
    return f'<div class="dbar">{logo(24)}{g}<span class="grow"></span><button class="dg-ib" aria-label="Поиск">{ic("search")}</button><button class="dg-ib" aria-label="Ещё">{ic("more_vert")}</button></div>'

def tabs(active):
    items = [('chats', ic('chat_bubble', active == 'chats'), 'Чаты', '<span class="tbn">12</span>'),
             ('settings', ic('settings', active == 'settings'), 'Настройки', ''),
             ('profile', dav('Д', 'me', 'sh-cookie9', 'tav'), 'Профиль', '')]
    out = ''.join(f'<button class="tb{" on" if k == active else ""}">{i}<span class="tl">{l}</span>{n}</button>' for k, i, l, n in items)
    return f'<div class="tabs dg-glass">{out}</div>'

FABS = f'''<div class="fabs"><button class="sfab" aria-label="История">{ic("photo_camera",1,22)}</button><span class="dg-fabwrap"><button class="dg-fabx sh-cookie9" aria-label="Новое сообщение">{ic("edit",1)}</button></span></div>'''

def wave(width=150, progress=.4, amp=3.5, period=20):
    pts = [f"{x+2},{12+amp*math.sin(2*math.pi*x/period):.2f}" for x in range(0, width + 1, 2)]
    cut = int(width * progress)
    act = "M" + " L".join(p for p in pts if float(p.split(',')[0]) - 2 <= cut)
    return f'<svg class="dg-wave" width="{width+4}" height="24" viewBox="0 0 {width+4} 24"><path class="trk" d="M{cut+8},12 L{width+2},12"/><path class="act" d="{act}"/></svg>'

CHATHEAD = f'''<div class="chead dg-glass"><div class="dg-appbar small" style="padding-top:0;background:transparent"><button class="dg-ib" aria-label="Назад">{ic("arrow_back")}</button>{dav('А','anya','sh-cookie12','s')}<span class="ttl">Аня{SUP(True)}<small>в сети</small></span><button class="dg-ib" aria-label="Поиск">{ic("search")}</button><button class="dg-ib" aria-label="Звонок">{ic("call")}</button><button class="dg-ib" aria-label="Ещё">{ic("more_vert")}</button></div>
<div class="pinbar"><span class="pl"><b>Закреплённое сообщение</b><span>Встречаемся в восемь у кофейни</span></span>{ic("push_pin",0,20)}</div></div>'''
A = 'persona persona-anya'
MSGS = f'''<div class="dg-msgs chatmsgs">
 <div class="dg-b out first">Смотри, что я собрал</div>
 <div class="dg-b out last">Форк телеги, весь на пружинах<span class="meta">9:20 {ic("done_all")}</span></div>
 <span class="dg-day">Сегодня</span>
 <div class="dg-b in first {A}">Ты видела новый дизайн?</div>
 <div class="dg-b in last photo {A}"><span class="ph"></span><span class="cap">Вот так у меня выглядит<span class="meta">9:31</span></span></div>
 <div class="dg-b out first"><span class="quote"><b>Аня</b>Вот так у меня выглядит</span>Это Material Expressive</div>
 <div class="dg-b out last">И удалённые теперь не пропадают<span class="meta">9:32 {ic("done_all")}</span><br><span class="react">❤ 2</span></div>
 <div class="dg-b in first deleted"><span class="tag">{ic("delete")}удалено</span>ой, это не тебе<span class="meta">9:33</span></div>
 <div class="dg-b in last edited {A}"><span class="tag">{ic("history")}изменено, 2 раза</span>Приду к восьми<span class="meta">9:40</span></div>
 <div class="dg-b out first last dg-voice"><span class="play sh-cookie7">{ic("play_arrow",1)}</span>{wave()}<span class="len">0:17</span></div>
</div>'''
COMPOSER = f'''<div class="dg-composer comp"><div class="dg-field dg-glass"><button class="dg-ib" aria-label="Эмодзи">{ic("mood")}</button><span class="ph">Сообщение</span><button class="dg-ib" aria-label="Прикрепить">{ic("attach_file")}</button></div><button class="dg-send sh-cookie9" aria-label="Голосовое">{ic("mic",1)}</button></div>'''
CHAT = WALL + MSGS + CHATHEAD + COMPOSER

MENU = f'''<div class="dg-menuwrap ctx"><div class="dg-reacts"><span>❤</span><span>👍</span><span>😂</span><span>🔥</span><span>🥲</span><span style="font-size:20px">{ic("expand_more")}</span></div></div>
<div class="dg-menu ctxm">
 <div class="grp"><div class="mi">{ic("reply")}Ответить</div><div class="mi">{ic("content_copy")}Копировать</div><div class="mi">{ic("translate")}Перевести</div><div class="mi">{ic("forward")}Переслать</div></div>
 <div class="grp"><div class="mi">{ic("push_pin")}Закрепить</div><div class="mi">{ic("edit")}Изменить</div><div class="mi">{ic("check_circle")}Выбрать</div></div>
 <div class="grp"><div class="mi">{ic("repeat")}Повторить</div><div class="mi">{ic("data_object")}Детали</div></div>
 <div class="grp"><div class="mi danger">{ic("delete")}Удалить</div></div>
</div>
<div class="dg-b out first last lifted">И удалённые теперь не пропадают<span class="meta">9:32 {ic("done_all")}</span><br><span class="react">❤ 2</span></div>'''

CAR = f'''<div class="dg-scrim zs"></div><div class="carv">
 <div class="cv" style="top:0;transform:scale(.9);opacity:.45"><span class="tm">9:35</span><div class="dg-b in first last {A}">Приду к семи</div></div>
 <div class="cv" style="top:150px"><span class="tm">9:37</span><div class="dg-b in first last {A}">Приду к половине восьмого</div></div>
 <div class="cv" style="top:300px;transform:scale(.9);opacity:.45"><span class="tm">9:40</span><div class="dg-b in first last {A}">Приду к восьми</div></div>
 <div class="dg-dots vdots"><i></i><i class="on"></i><i></i></div>
</div>'''

def info(val, lab, tail=''):
    return f'<div class="dg-li info"><span class="tx"><b>{val}</b><span>{lab}</span></span>{tail}</div>'
MEDIA = '''<div class="dg-mbento" style="padding:10px 16px 0"><i class="big" style="background:linear-gradient(135deg,oklch(0.75 0.10 200),oklch(0.62 0.12 250))"></i><i style="background:linear-gradient(160deg,oklch(0.85 0.08 85),oklch(0.66 0.12 40))"></i><i class="sh-cookie9" style="background:linear-gradient(200deg,oklch(0.8 0.07 160),oklch(0.55 0.09 200))"></i><i style="background:linear-gradient(45deg,oklch(0.65 0.12 25),oklch(0.82 0.08 70))"></i><i style="border-radius:10px 10px 32px 10px;background:linear-gradient(300deg,oklch(0.5 0.1 270),oklch(0.78 0.07 220))"></i></div>'''
def mtabs(*names):
    b = ''.join(f'<button aria-pressed="{"true" if i == 1 else "false"}"{" style=\"border-radius:8px 20px 20px 8px\"" if i == len(names)-1 else ""}>{n}</button>' for i, n in enumerate(names))
    return f'<div class="dg-seg" style="padding:16px 16px 0">{b}</div>'

def hero(persona, avatar, name_html, status, top_right, shape='sh-cookie12', ring=True):
    r = ' ringed' if ring else ''
    return f'''<div class="dg-hero">{pattern(14, 412, 250, 50, 120)}<div class="top">{top_right}</div></div>
<div class="dg-ring {shape}{r}" style="width:172px;height:172px">{avatar}</div>
<h2 class="dg-pname">{name_html}</h2><div class="dg-pstat">{status}</div>'''

PROFILE = f'''<div class="persona persona-anya">{hero('anya', dav('А','anya','sh-cookie12'), 'Аня Соколова' + SUP(True, 26), 'в сети', f'<button class="dg-ib" aria-label="Назад">{ic("arrow_back")}</button><span style="display:flex;gap:8px"><button class="dg-ib" aria-label="Видеозвонок">{ic("videocam")}</button><button class="dg-ib" aria-label="Ещё">{ic("more_vert")}</button></span>')}
<div style="padding:16px 16px 0"><div class="dg-bgroup tall"><button class="dg-btn filled" aria-pressed="true">{ic("chat_bubble",1)}Сообщение</button><button class="dg-btn tonal">{ic("notifications")}Звук</button><button class="dg-btn tonal">{ic("call")}Звонок</button><button class="dg-btn tonal">{ic("videocam")}Видео</button></div></div>
<div class="dg-list" style="padding:12px 16px 0">{info('+7 999 123-45-67','Мобильный')}{info('Рисую интерфейсы, пью матчу','О себе')}{info('@anya_sk','Имя пользователя', ic("qr_code_2"))}{info('14 марта','День рождения')}{info('<span class="tnum">482 913 557</span>','ID', ic("content_copy"))}</div>
{mtabs('Истории','Медиа','Файлы','Ссылки','Голосовые')}{MEDIA}</div>'''

DASHA = f'''<div class="plainhero">{hero('', rav('Д','t'), 'Даша' + USERB, 'в сети', f'<button class="dg-ib" aria-label="Назад">{ic("arrow_back")}</button><span style="display:flex;gap:8px"><button class="dg-ib" aria-label="Видеозвонок">{ic("videocam")}</button><button class="dg-ib" aria-label="Ещё">{ic("more_vert")}</button></span>', shape='', ring=False)}</div>
<div style="padding:16px 16px 0"><div class="dg-bgroup tall"><button class="dg-btn filled" aria-pressed="true">{ic("chat_bubble",1)}Сообщение</button><button class="dg-btn tonal">{ic("notifications")}Звук</button><button class="dg-btn tonal">{ic("call")}Звонок</button><button class="dg-btn tonal">{ic("videocam")}Видео</button></div></div>
<div class="dg-list" style="padding:12px 16px 0">{info('@dasha_m','Имя пользователя', ic("qr_code_2"))}{info('Учусь на дизайнера','О себе')}{info('<span class="tnum">615 220 093</span>','ID', ic("content_copy"))}</div>
{mtabs('Истории','Медиа','Файлы','Ссылки')}{MEDIA}'''

MYPROFILE = f'''<div class="persona persona-me">{hero('me', dav('Д','me','sh-cookie9'), 'Деля' + SUP(True, 26), 'в сети', f'<span></span><button class="dg-ib" aria-label="Ещё">{ic("more_vert")}</button>', shape='sh-cookie9')}
<div style="padding:16px 16px 0"><div class="dg-bgroup tall"><button class="dg-btn tonal">{ic("add_a_photo")}Фото</button><button class="dg-btn tonal">{ic("edit")}Изменить</button><button class="dg-btn filled" aria-pressed="true">{ic("palette",1)}Оформление</button><button class="dg-btn tonal">{ic("settings")}Настройки</button></div></div>
<div class="dg-list" style="padding:12px 16px 0">{info('+7 912 000-11-22','Мобильный')}{info('@delusha','Имя пользователя', ic("qr_code_2"))}{info('Собираю свой телеграм','О себе')}</div>
{mtabs('Истории','Подарки','Медиа','Файлы')}</div>''' + tabs('profile')

def li(icon, title, sub=None, tail='chev', lead='', on=True):
    s = f'<span>{sub}</span>' if sub else ''
    t = {'sw': f'<input type="checkbox" class="dg-sw"{" checked" if on else ""}>', 'chev': ic('chevron_right'), '': ''}.get(tail, f'<span class="val">{tail}</span>')
    return f'<div class="dg-li"><span class="lead {lead}">{ic(icon, lead in ("t","acc"))}</span><span class="tx"><b>{title}</b>{s}</span>{t}</div>'

SETTINGS = f'''<div class="dbar"><span class="grow"></span><button class="dg-ib" aria-label="Поиск">{ic("search")}</button><button class="dg-ib" aria-label="Ещё">{ic("more_vert")}</button></div>
<div class="setwrap">
 <div class="dg-list"><span class="hd">Аккаунты</span>
  <div class="dg-li"><span class="lead" style="background:none">{dav('Д','me','sh-cookie9','s')}</span><span class="tx"><b>Деля{SUP(True)}</b><span>+7 912 000-11-22</span></span>{ic("check_circle",1)}</div>
  <div class="dg-li"><span class="lead" style="background:none">{rav('Р','sc','s')}</span><span class="tx"><b>Работа</b><span>+7 912 333-44-55</span></span><span class="dg-badge">3</span></div>
  <div class="dg-li"><span class="lead">{ic("add")}</span><span class="tx"><b>Добавить аккаунт</b></span></div></div>
 <div class="dg-list"><div class="dg-li"><span class="lead logo-lead">{logo(20)}</span><span class="tx"><b>DelushaGram</b></span>{ic("chevron_right")}</div></div>
 <div class="dg-list">{li("person","Аккаунт")}{li("chat","Настройки чатов")}{li("lock","Конфиденциальность")}{li("notifications","Уведомления и звуки")}{li("database","Данные и память")}{li("folder","Папки с чатами")}{li("devices","Устройства")}{li("battery_saver","Энергосбережение")}{li("translate","Язык","Русский")}</div>
 <div class="dg-list">{li("star","Telegram Premium")}{li("kid_star","Мои звёзды")}{li("business_center","Telegram для бизнеса")}{li("redeem","Отправить подарок")}</div>
</div>''' + tabs('settings')

DELUSHA = f'''<div class="dbar"><button class="dg-ib" aria-label="Назад">{ic("arrow_back")}</button><span class="grow"></span></div>
<div style="display:flex;justify-content:center;padding:4px 0 14px">{logo(96, seed=23)}</div>
<div class="setwrap" style="gap:12px">
 <div class="dg-ttiles"><div class="dg-ttile on"><span class="ti sh-cookie9">{ic("visibility_off",1)}</span><div class="bt"><b>Призрак</b><input type="checkbox" class="dg-sw" checked></div></div><div class="dg-ttile on2"><span class="ti sh-clover4">{ic("history",1)}</span><div class="bt"><b>История</b><input type="checkbox" class="dg-sw" checked></div></div></div>
 <div class="dg-list"><div class="dg-li"><span class="lead" style="background:none">{dav('Д','me','sh-cookie9','s')}</span><span class="tx"><b>Оформление профиля{SUP()}</b></span>{ic("chevron_right")}</div>{li("delete_history","Удалённые сообщения","1 284 сообщения, 212 МБ",lead="e")}</div>
 <div class="dg-list">{li("done_all","Читать после ответа",tail="sw")}{li("auto_stories","Смотреть истории тайно",tail="sw",on=False)}{li("swipe","Свайпы по чатам")}{li("archive","Архив","Свайпом вниз")}{li("favorite","Анимации реакций","Полные")}{li("blur_on","Размытие",tail="sw")}</div>
</div>'''

EDITOR = f'''<div class="dbar"><button class="dg-ib" aria-label="Назад">{ic("arrow_back")}</button><span class="grow"></span></div>
<div class="persona persona-me" style="margin:0 12px;border-radius:32px;overflow:hidden;background:var(--surface-container-low)">
 <div class="dg-hero" style="height:120px;border-radius:0 0 28px 28px">{pattern(9, 388, 120, 30, 70)}</div>
 <div style="display:flex;align-items:flex-end;gap:12px;padding:0 16px 14px;margin-top:-44px;position:relative;z-index:2">
  <span class="dg-ring sm sh-cookie9" style="width:92px;height:92px;margin:0">{dav('Д','me','sh-cookie9')}</span>
  <div style="padding-bottom:6px"><div style="font:600 22px/28px var(--font-sans)">Деля{SUP(True,20)}</div><div style="font:500 13px/18px var(--font-sans);color:var(--p-acc)">@delusha</div></div>
 </div>
</div>
<div class="dg-secs" style="margin-top:12px">
 <div class="dg-sec"><div class="dg-pick"><i class="sh-circle"></i><i class="sh-cookie9 on"></i><i class="sh-clover4"></i><i class="sh-flower8"></i><i class="sh-sunny"></i><i class="sh-pent"></i></div></div>
 <div class="dg-sec"><div class="dg-pick col"><i class="sh-cookie9 on" style="background:oklch(0.6 0.13 295)"></i><i class="sh-cookie9" style="background:oklch(0.6 0.10 190)"></i><i class="sh-cookie9" style="background:oklch(0.6 0.12 140)"></i><i class="sh-cookie9" style="background:oklch(0.64 0.13 50)"></i><i class="sh-cookie9" style="background:oklch(0.6 0.14 0)"></i><i class="sh-cookie9" style="background:oklch(0.6 0.12 240)"></i><i class="sh-cookie9" style="background:oklch(0.5 0.02 295)"></i></div></div>
 <div class="dg-sec persona persona-me"><div class="dg-bgs"><div class="on">{''.join(f'<i class="{SH[k]}" style="left:{[10,60,90,30,70][k]}px;top:{[8,-6,30,40,50][k]}px;width:{[28,36,24,30,22][k]}px;height:{[28,36,24,30,22][k]}px"></i>' for k in range(5))}</div><div style="background:radial-gradient(circle at 20% 20%,var(--p-pat),transparent 60%),radial-gradient(circle at 90% 90%,var(--p-acc),transparent 55%),var(--p-c)"></div><div></div></div></div>
 <div class="dg-sec"><div class="dg-anim"><div><i class="sh-cookie9"></i>{ic("pause",1)}</div><div class="on"><i class="sh-cookie9"></i>{ic("air")}</div><div><i class="sh-clover4"></i>{ic("animation")}</div><div><i class="sh-flower8"></i>{ic("autorenew")}</div></div></div>
 <div class="dg-sec"><div class="dg-names"><div style="font-weight:400">Аа</div><div class="on" style="font-weight:600">Аа</div><div style="font-weight:800">Аа</div></div></div>
</div>'''
LOCKED = EDITOR.replace(SUP(True,20), '').replace('<div class="dg-secs" style="margin-top:12px">', '<div class="lockcta"><button class="dg-btn filled m">' + ic("favorite", 1) + 'Поддержать</button></div><div class="dg-secs locked" style="margin-top:12px">')

def call(av, name, kind, when, dur, sup=False):
    icn = {'in': ('call_received', 'var(--online)'), 'out': ('call_made', 'var(--online)'), 'miss': ('call_missed', 'var(--error)')}[kind]
    if sup: name += SUP()
    return f'<div class="dg-chat">{av}<div class="mid"><div class="nm">{name}</div><div class="pv"><span class="dg-ic fill" style="color:{icn[1]}">{icn[0]}</span>{when}{(", " + dur) if dur else ""}</div></div><div class="rt"><span class="dg-ic fill" style="font-size:24px;color:var(--primary)">call</span></div></div>'
SEARCH = f'''<div class="dg-search dg-glass" style="margin:4px 16px 0">{ic("arrow_back")}<span class="ph" style="color:var(--on-surface)">а</span>{ic("close")}</div>
<div class="dg-seg"><button aria-pressed="false">Чаты</button><button aria-pressed="false">Каналы</button><button aria-pressed="false">Медиа</button><button aria-pressed="false">Ссылки</button><button aria-pressed="true" style="border-radius:20px">{ic("call",1,18)}Звонки</button></div>
<div class="dg-cgroup">
 {call(dav('А','anya','sh-cookie12'),'Аня Соколова','in','Вчера, 21:14','12 мин',True)}
 {call(rav('М','e'),'Мама','miss','Вчера, 18:02','')}
 {call(dav('А','anya','sh-cookie12'),'Аня Соколова','out','Пн, 10:40','3 мин',True)}
 {call(dav('Л','lyokha','sh-clover4'),'Лёха','in','Вс, 23:55','48 мин',True)}
 {call(rav('А','t'),'Артём','out','12 окт','1 мин')}
</div>'''

NEWMSG = f'''<div class="dbar"><button class="dg-ib" aria-label="Назад">{ic("arrow_back")}</button><span class="grow"></span><button class="dg-ib" aria-label="Сортировка">{ic("sort")}</button></div>
<div class="dg-search dg-glass" style="margin:0 16px">{ic("search")}<span class="ph">Поиск</span></div>
<div class="dg-ntiles" style="grid-template-columns:repeat(3,1fr)"><div class="nt"><span class="ti sh-cookie9">{ic("group",1)}</span>Новая группа</div><div class="nt"><span class="ti sh-clover4">{ic("person_add",1)}</span>Новый контакт</div><div class="nt"><span class="ti sh-flower8">{ic("campaign",1)}</span>Новый канал</div></div>
<div class="dg-cgroup">
 {row(dav('А','anya','sh-cookie12'),'Аня Соколова','в сети','',online=True,dg=True,sup=True)}
 {row(rav('М','e'),'Мама','в сети','',online=True)}
 {row(rav('Д','t'),'Даша','в сети','',online=True,dg=True)}
 {row(dav('Л','lyokha','sh-clover4'),'Лёха','был 5 минут назад','',sup=True)}
 {row(rav('В','t'),'Вика','была недавно','')}
 {row(rav('И','sc'),'Игорь','был на этой неделе','')}
 {row(rav('Ю',''),'Юля','была недавно','')}
</div>'''

S = {
 '01-chats': screen('light', topbar() + STORIES + SEG + CHATS + FABS + tabs('chats')),
 '02-chat': screen('light', CHAT, 'chat'),
 '03-menu': screen('dark', CHAT + '<div class="dg-scrim zs"></div>' + MENU, 'chat'),
 '04-history': screen('dark', CHAT + CAR, 'chat'),
 '05-profile': screen('light', PROFILE),
 '06-settings': screen('dark', SETTINGS),
 '07-delusha': screen('amoled', DELUSHA),
 '08-my-profile': screen('light', MYPROFILE),
 '09-profile-editor': screen('dark', EDITOR),
 '10-editor-locked': screen('light', LOCKED),
 '11-profile-user': screen('light', DASHA),
 '12-new-message': screen('light', NEWMSG),
 '13-search-calls': screen('dark', SEARCH),
 '14-ghost-amoled': screen('amoled', '<div class="gwash">' + topbar(True) + STORIES + '</div>' + SEG + CHATS + FABS + tabs('chats')),
}

CSS = r"""
html,body{margin:0;background:#202024}
body{display:flex;flex-wrap:wrap;gap:40px;padding:40px;font-family:var(--font-sans)}
.screen>*{flex-shrink:0}
.screen{position:relative;width:412px;height:915px;overflow:hidden;background:var(--surface);color:var(--on-surface);display:flex;flex-direction:column;font-size:14px;line-height:20px}
.sb{height:36px;flex:none;display:flex;align-items:center;justify-content:space-between;padding:0 24px 0 28px;font:500 14px/1 var(--font-sans);color:var(--on-surface);position:relative;z-index:13}
.cam{width:12px;height:12px;border-radius:50%;background:#0b0b0d}
.sbi{display:flex;gap:4px}
.gb{position:absolute;left:0;right:0;bottom:0;height:24px;display:flex;justify-content:center;align-items:center;z-index:20}
.gb i{width:108px;height:4px;border-radius:2px;background:var(--on-surface);opacity:.55}
.dbar{display:flex;align-items:center;gap:6px;padding:4px 8px 4px 18px;min-height:52px}
.dbar .grow{flex:1}
.dg-chat .mute{font-size:16px!important;color:var(--on-surface-variant)!important}
.dg-chat .rt .dg-ic{color:var(--on-surface-variant)}
.dg-chat .dg-av{width:52px;height:52px}
/* stories row */
.dg-stories{display:flex;gap:6px;padding:6px 12px 2px;overflow:hidden}
.dg-stories .st{flex:none;width:74px;display:flex;flex-direction:column;align-items:center;gap:5px;font:500 12px/16px var(--font-sans);color:var(--on-surface)}
.dg-stories .ring{position:relative;width:66px;height:66px;border-radius:50%;display:grid;place-items:center;background:conic-gradient(var(--primary),var(--tertiary),var(--primary))}
.dg-stories .st.seen .ring{background:var(--outline-variant)}
.dg-stories .st.mine .ring{background:none}
.dg-stories .ring .dg-av{width:58px;height:58px;font-size:20px;box-shadow:0 0 0 3px var(--surface)}
.dg-stories .ring .dg-av.reg{border-radius:50%}
.dg-stories .sn{max-width:72px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.dg-stories .splus{position:absolute;right:0;bottom:0;width:22px;height:22px;border-radius:50%;background:var(--primary);color:var(--on-primary);display:grid;place-items:center;box-shadow:0 0 0 3px var(--surface)}
.gwash .dg-stories .dg-av{box-shadow:0 0 0 3px color-mix(in srgb,var(--ghost) 70%,var(--surface))}
/* bottom tabs (MainTabsActivity) */
.tabs{position:absolute;left:16px;right:16px;bottom:30px;height:68px;border-radius:34px;display:flex;align-items:center;justify-content:space-around;padding:0 8px;z-index:9;box-shadow:var(--elevation-2)}
.tb{position:relative;border:0;background:transparent;height:52px;min-width:64px;border-radius:26px;display:flex;align-items:center;justify-content:center;gap:8px;padding:0 14px;color:var(--on-surface-variant);font:500 14px/20px var(--font-sans)}
.tb .tl{display:none}
.tb.on{background:var(--secondary-container);color:var(--on-secondary-container);padding:0 20px 0 16px}
.tb.on .tl{display:inline;font-weight:600}
.tb .tbn{position:absolute;top:4px;left:36px;min-width:18px;height:18px;padding:0 5px;border-radius:9px;background:var(--primary);color:var(--on-primary);font:600 11px/18px var(--font-sans);text-align:center}
.tb.on .tbn{position:static;background:var(--on-secondary-container);color:var(--secondary-container)}
.tb .tav{width:32px;height:32px;font-size:13px}
.fabs{position:absolute;right:18px;bottom:116px;z-index:9;display:flex;flex-direction:column;align-items:center;gap:12px}
.sfab{width:44px;height:44px;border:0;border-radius:16px;background:var(--surface-container-highest);color:var(--on-surface);display:grid;place-items:center;box-shadow:var(--elevation-2)}
/* chat */
.chat .sb{position:absolute;top:0;left:0;right:0}
.chead{position:absolute;left:0;right:0;top:0;z-index:6;padding-top:36px;border-radius:0 0 28px 28px}
.chead .ttl{font-weight:600}
.pinbar{display:flex;align-items:center;gap:10px;margin:0 12px 10px;padding:8px 14px;border-radius:16px;background:color-mix(in srgb,var(--primary-container) 55%,transparent);color:var(--on-surface)}
.pinbar .pl{flex:1;min-width:0;display:flex;flex-direction:column}
.pinbar b{font:600 12.5px/16px var(--font-sans);color:var(--primary)}
.pinbar .pl span{font:400 14px/20px var(--font-sans);white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.chatmsgs{position:absolute;inset:0;padding:170px 14px 100px!important;overflow:hidden;z-index:1;justify-content:flex-end}
.dg-b.photo{padding:4px;width:236px}
.dg-b.photo .ph{display:block;height:140px;border-radius:22px 22px 8px 8px;background:radial-gradient(circle at 30% 30%,oklch(0.85 0.1 80),transparent 60%),linear-gradient(135deg,oklch(0.7 0.11 300),oklch(0.62 0.12 200))}
.dg-b.photo .cap{display:block;padding:6px 10px 4px}
.comp{position:absolute;left:0;right:0;bottom:0;z-index:6;padding:6px 10px 34px;align-items:center}
.zs{z-index:10}
.lifted{position:absolute;right:14px;top:300px;z-index:12;box-shadow:var(--elevation-3);max-width:80%}
.ctx{position:absolute;right:14px;top:238px;z-index:12;width:auto;align-items:flex-end}
.ctxm{position:absolute;right:14px;top:404px;z-index:12;width:236px}
.carv{position:absolute;left:0;right:0;top:250px;height:420px;z-index:12}
.cv{position:absolute;width:240px;display:flex;flex-direction:column;align-items:flex-start;gap:6px;left:24px;transform-origin:0 50%}
.cv .tm{font:500 12px/16px var(--font-sans);padding:4px 10px;border-radius:12px;background:var(--surface-glass);color:var(--on-surface)}
.cv .dg-b{max-width:none;width:100%;box-shadow:var(--elevation-2)}
.vdots{position:absolute;right:22px;top:150px;flex-direction:column}
.vdots i.on{width:8px!important;height:24px}
/* profile */
.dg-ring.ringed{background:conic-gradient(var(--p-acc),var(--tertiary),var(--p-acc))}
.dg-ring.ringed .dg-av{box-shadow:0 0 0 0 transparent}
.plainhero .dg-hero{background:var(--surface-container);height:200px}
.plainhero .dg-hero .pat{display:none}
.plainhero .dg-ring{border-radius:50%;margin-top:-84px;width:168px!important;height:168px!important}
.plainhero .dg-pstat{color:var(--primary)}
.dg-li.info{min-height:60px}
.dg-li.info>.dg-ic{color:var(--primary)}
.tnum{font-variant-numeric:tabular-nums}
.setwrap{padding:0 12px;display:flex;flex-direction:column;gap:14px}
.setwrap .dg-li{min-height:54px;padding:8px 16px}
.logo-lead{background:var(--surface-container-highest)!important}
.dg-ntiles{display:grid;gap:6px;padding:12px 16px}
.dg-ntiles .nt{height:92px;border-radius:24px;background:var(--surface-container);display:flex;flex-direction:column;align-items:center;justify-content:center;gap:8px;font:500 12.5px/16px var(--font-sans);text-align:center}
.dg-ntiles .ti{width:44px;height:44px;display:grid;place-items:center;background:var(--primary-container);color:var(--on-primary-container)}
.lockcta{display:flex;justify-content:center;padding:14px 16px 0}
.lockcta .dg-btn{width:100%;height:64px;border-radius:32px;font-size:18px}
.dg-secs.locked{opacity:.38;pointer-events:none;filter:saturate(.4)}
.gwash{background:color-mix(in srgb,var(--ghost) 70%,var(--surface));border-radius:0 0 32px 32px;padding-bottom:6px;margin-bottom:4px}
.gwash .st{color:var(--on-ghost)}
.dg-ghost.on{background:var(--tertiary);color:var(--on-tertiary)}
.dg-pname .dgb{margin-left:8px;vertical-align:-4px}
"""

FONTS = '<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Roboto+Flex:opsz,wdth,wght@8..144,25..151,100..1000&display=swap"><link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Rounded:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200&display=block">'
html = f'<!doctype html><html><head><meta charset="utf-8">{FONTS}<style>{tokcss}{bundle}{expressive.all_css()}{CSS}</style></head><body>' + ''.join(f'<div id="s-{k}">{v_}</div>' for k, v_ in S.items()) + '</body></html>'
open(os.path.join(D, 'screens.html'), 'w').write(html)
print(list(S))
