import json, math, sys
def oklch(L,C,H):
    a=C*math.cos(math.radians(H)); b=C*math.sin(math.radians(H))
    l_=L+0.3963377774*a+0.2158037573*b; m_=L-0.1055613458*a-0.0638541728*b; s_=L-0.0894841775*a-1.2914855480*b
    l,m,s=l_**3,m_**3,s_**3
    r=4.0767416621*l-3.3077115913*m+0.2309699292*s; g=-1.2684380046*l+2.6097574011*m-0.3413193965*s; bb=-0.0041960863*l-0.7034186147*m+1.7076147010*s
    def f(x):
        x=max(0,min(1,x)); return 12.92*x if x<=0.0031308 else 1.055*x**(1/2.4)-0.055
    return '#%02x%02x%02x'%tuple(round(f(v)*255) for v in (r,g,bb))
H=295; T=195
L={ # light, dark, amoled  as (L,C,H)
'primary':[(.48,.17,H),(.82,.11,H),(.82,.11,H)],
'on-primary':[(.99,.005,H),(.28,.10,H),(.28,.10,H)],
'primary-container':[(.90,.07,H),(.38,.11,H),(.34,.11,H)],
'on-primary-container':[(.30,.10,H),(.92,.06,H),(.92,.06,H)],
'secondary':[(.50,.05,H),(.82,.04,H),(.82,.04,H)],
'on-secondary':[(.99,.005,H),(.30,.04,H),(.30,.04,H)],
'secondary-container':[(.91,.035,H),(.33,.035,H),(.28,.035,H)],
'on-secondary-container':[(.32,.04,H),(.90,.03,H),(.90,.03,H)],
'tertiary':[(.47,.09,T),(.82,.08,T),(.82,.08,T)],
'on-tertiary':[(.99,.005,T),(.28,.06,T),(.28,.06,T)],
'tertiary-container':[(.91,.06,T),(.37,.06,T),(.32,.06,T)],
'on-tertiary-container':[(.32,.06,T),(.92,.05,T),(.92,.05,T)],
'error':[(.53,.19,27),(.78,.13,27),(.78,.13,27)],
'on-error':[(.99,.005,27),(.30,.10,27),(.30,.10,27)],
'error-container':[(.93,.045,27),(.36,.09,27),(.30,.08,27)],
'on-error-container':[(.36,.12,27),(.92,.04,27),(.92,.04,27)],
'surface':[(.985,.007,H),(.165,.012,H),(0,0,H)],
'surface-container-lowest':[(1,0,H),(.13,.01,H),(0,0,H)],
'surface-container-low':[(.965,.011,H),(.19,.014,H),(.13,.008,H)],
'surface-container':[(.945,.014,H),(.21,.015,H),(.165,.01,H)],
'surface-container-high':[(.925,.017,H),(.245,.017,H),(.20,.012,H)],
'surface-container-highest':[(.905,.02,H),(.28,.018,H),(.235,.014,H)],
'on-surface':[(.22,.015,H),(.93,.01,H),(.95,.008,H)],
'on-surface-variant':[(.45,.02,H),(.77,.02,H),(.77,.02,H)],
'outline':[(.58,.02,H),(.62,.02,H),(.60,.02,H)],
'outline-variant':[(.86,.02,H),(.36,.02,H),(.30,.015,H)],
'inverse-surface':[(.30,.015,H),(.92,.01,H),(.92,.01,H)],
'inverse-on-surface':[(.95,.01,H),(.25,.015,H),(.25,.015,H)],
'inverse-primary':[(.82,.11,H),(.48,.17,H),(.48,.17,H)],
'online':[(.55,.15,150),(.80,.15,150),(.80,.15,150)],
}
usage={
'primary':'Главный акцент: активные элементы, FAB-иконка на светлом, ссылки, счётчик непрочитанных, исходящий пузырь (светлая тема).',
'on-primary':'Текст и иконки на `primary`.',
'primary-container':'Заливка FAB, выбранного аватара «я», исходящий пузырь в тёмных темах.',
'on-primary-container':'Текст и иконки на `primary-container`.',
'secondary':'Второстепенные акценты: неактивные переключатели групп, бордеры выбранного.',
'on-secondary':'Текст на `secondary`.',
'secondary-container':'Выбранная вкладка плавающей панели, выбранный чип папки, тональные кнопки.',
'on-secondary-container':'Текст и иконки на `secondary-container`.',
'tertiary':'Метка «изменено», акценты функций форка (призрак, история).',
'on-tertiary':'Текст на `tertiary`.',
'tertiary-container':'Плашка «Призрак», иконки разделов форка в настройках.',
'on-tertiary-container':'Текст и иконки на `tertiary-container`.',
'error':'Опасные действия: «Удалить», ошибки ввода.',
'on-error':'Текст на `error`.',
'error-container':'Пузырь удалённого сообщения, фон подтверждения удаления.',
'on-error-container':'Текст на `error-container`.',
'surface':'Фон всех экранов.',
'surface-container-lowest':'Фон под карточками в списках с высоким контрастом.',
'surface-container-low':'Фон нижних листов, закреплённые чаты.',
'surface-container':'Строки списков настроек, шапка чата.',
'surface-container-high':'Чип даты в чате, меню.',
'surface-container-highest':'Плавающая панель, поиск, поле ввода, входящий пузырь.',
'on-surface':'Основной текст на любом `surface*`.',
'on-surface-variant':'Вторичный текст: превью сообщения, время, подписи, неактивные иконки на любом `surface*`.',
'outline':'Бордеры полей и выключенных переключателей (3:1 к фону).',
'outline-variant':'Декоративные разделители, не несущие смысла.',
'inverse-surface':'Фон снэкбара.',
'inverse-on-surface':'Текст снэкбара.',
'inverse-primary':'Кнопка действия в снэкбаре.',
'online':'Точка «в сети» на аватаре. Всегда рядом с текстом статуса, не единственный признак.',
}
themes=['light','dark','amoled']
toks=[]
for k,v in L.items():
    toks.append({'name':k,'value':{t:oklch(*v[i]) for i,t in enumerate(themes)},'usage':usage[k]})
toks.append({'name':'scrim','value':{t:'#000000' for t in themes},'usage':'Затемнение под листами и диалогами, 32% непрозрачности.'})
alias=[
('bubble-out',{'light':'{primary}','dark':'{primary-container}','amoled':'{primary-container}'},'Исходящий пузырь. В тёмных темах приглушён до контейнера, чтобы не слепить ночью.'),
('on-bubble-out',{'light':'{on-primary}','dark':'{on-primary-container}','amoled':'{on-primary-container}'},'Текст исходящего пузыря.'),
('bubble-in','{surface-container-highest}','Входящий пузырь.'),
('on-bubble-in','{on-surface}','Текст входящего пузыря.'),
('bubble-deleted','{error-container}','Пузырь сообщения, которое собеседник удалил (история форка).'),
('on-bubble-deleted','{on-error-container}','Текст и метка «удалено».'),
('ghost','{tertiary-container}','Плашка включённого режима призрака.'),
('on-ghost','{on-tertiary-container}','Текст и иконка плашки призрака.'),
('focus-ring','{primary}','Кольцо фокуса клавиатуры/D-pad: 3dp, отступ 2dp.'),
]
for n,v,u in alias:
    toks.append({'name':n,'value':v,'usage':u})
# contrast check
def lum(h):
    c=[int(h[i:i+2],16)/255 for i in (1,3,5)]
    c=[x/12.92 if x<=0.04045 else ((x+0.055)/1.055)**2.4 for x in c]
    return 0.2126*c[0]+0.7152*c[1]+0.0722*c[2]
def cr(a,b):
    la,lb=sorted([lum(a),lum(b)],reverse=True); return (la+0.05)/(lb+0.05)
V={t['name']:t['value'] for t in toks if isinstance(t['value'],dict) and not str(list(t['value'].values())[0]).startswith('{')}
pairs=[('on-primary','primary'),('on-primary-container','primary-container'),('on-secondary-container','secondary-container'),('on-tertiary-container','tertiary-container'),('on-error-container','error-container'),('on-error','error'),('on-tertiary','tertiary'),('inverse-on-surface','inverse-surface')]
for s in ['surface','surface-container-low','surface-container','surface-container-high','surface-container-highest']:
    pairs+= [('on-surface',s),('on-surface-variant',s),('primary',s)]
pairs+=[('outline','surface'),('outline','surface-container-highest'),('tertiary','error-container'),('primary','surface-container-highest')]
bad=0
for t in themes:
    for a,b in pairs:
        r=cr(V[a][t],V[b][t]); lim=3 if a in('outline','primary') and b.startswith('surface') and a=='outline' else 4.5
        if r<lim: print('FAIL',t,a,b,round(r,2)); bad+=1
print('checked',len(pairs)*3,'bad',bad)
json.dump(toks,open(sys.argv[1],'w'),ensure_ascii=False)
