# Interactive motion cards for the DelushaGram design system.
# Every demo runs on the same spring solver as the Android SpringAnimation tokens.

ENGINE = r"""
var RM=matchMedia('(prefers-reduced-motion: reduce)').matches;
var SPR={'spatial-fast':[.6,800],'spatial-default':[.8,380],'spatial-slow':[.8,200],'effects-fast':[1,3800],'effects-default':[1,1600],'effects-slow':[1,800],'spatial-calm':[1,380]};
function Spring(x,cb,name,eps){this.x=x;this.v=0;this.t=x;this.cb=cb;this.eps=eps||0.001;this.use(name||'spatial-default');}
Spring.prototype.use=function(n){var s=SPR[n];this.z=s[0];this.k=s[1];return this};
Spring.prototype.set=function(x){this.x=this.t=x;this.v=0;if(this.r){cancelAnimationFrame(this.r);this.r=0}this.cb(x);return this};
Spring.prototype.kick=function(v){this.v=v;return this};
Spring.prototype.to=function(t,n,done){if(n)this.use(n);this.t=t;this.done=done||null;var me=this;
 if(RM){this.set(t);if(done)done();return this}
 if(!this.r){this.l=performance.now();this.r=requestAnimationFrame(function f(now){var dt=Math.min(.05,(now-me.l)/1000);me.l=now;var c=2*me.z*Math.sqrt(me.k),steps=Math.max(1,Math.ceil(dt/.002)),h=dt/steps;
  for(var i=0;i<steps;i++){var a=-me.k*(me.x-me.t)-c*me.v;me.v+=a*h;me.x+=me.v*h}
  if(Math.abs(me.v)<me.eps*10&&Math.abs(me.x-me.t)<me.eps){me.x=me.t;me.v=0;me.r=0;me.cb(me.x);var d=me.done;me.done=null;if(d)d();return}
  me.cb(me.x);me.r=requestAnimationFrame(f)})}
 return this};
function L(a,b,p){return a+(b-a)*p}
function C(p){return Math.max(0,Math.min(1,p))}
function $(s,r){return (r||document).querySelector(s)}
function $$(s,r){return Array.prototype.slice.call((r||document).querySelectorAll(s))}
function rel(el,root){var a=el.getBoundingClientRect(),b=root.getBoundingClientRect();return{x:a.left-b.left,y:a.top-b.top,w:a.width,h:a.height}}
function buzz(){try{navigator.vibrate&&navigator.vibrate(8)}catch(e){}}
"""

MOTION_CSS = r"""
/* ---------- Motion demos ---------- */
.mo-phone{position:relative;width:360px;max-width:100%;border-radius:28px;overflow:hidden;background:var(--surface);box-shadow:inset 0 0 0 1px var(--outline-variant);user-select:none;-webkit-user-select:none}
.mo-hint{font:500 12px/16px var(--font-sans);letter-spacing:.3px;color:var(--on-surface-variant);display:flex;align-items:center;gap:6px}
.mo-hint .dg-ic{font-size:16px}
.mo-wrap{display:flex;gap:20px;align-items:flex-start;flex-wrap:wrap;padding:16px}
.mo-side{display:flex;flex-direction:column;gap:10px;max-width:300px;min-width:0}
.mo-side p{margin:0;font:400 14px/20px var(--font-sans);color:var(--on-surface-variant)}
.mo-side b{color:var(--on-surface)}
.mo-tok{display:inline-block;font:500 12px/18px ui-monospace,Menlo,monospace;padding:0 6px;border-radius:6px;background:var(--surface-container-high);color:var(--on-surface)}
/* curves */
.mo-lanes{display:flex;flex-direction:column;gap:6px}
.mo-lane{display:flex;align-items:center;gap:12px}
.mo-lane .nm{width:120px;flex:none;font:500 12px/16px ui-monospace,Menlo,monospace;color:var(--on-surface-variant)}
.mo-track{flex:1;height:40px;border-radius:20px;background:var(--surface-container);position:relative}
.mo-dot{position:absolute;left:4px;top:4px;width:32px;height:32px;background:var(--primary)}
.mo-dot.fx{background:var(--tertiary)}
/* send */
.mo-msgs{position:absolute;left:0;right:0;top:64px;bottom:72px;display:flex;flex-direction:column;justify-content:flex-end;gap:2px;padding:8px 12px;overflow:hidden}
.mo-ghost{position:absolute;z-index:5;padding:8px 14px;font:400 16px/22px var(--font-sans);overflow:hidden;white-space:nowrap;transition:background-color 260ms cubic-bezier(.2,0,0,1),color 260ms cubic-bezier(.2,0,0,1)}
.mo-composer{position:absolute;left:0;right:0;bottom:0}
.mo-composer input{flex:1;min-width:0;border:0;background:transparent;outline:0;font:400 16px/22px var(--font-sans);color:var(--on-surface);padding:15px 4px}
.mo-composer input::placeholder{color:var(--on-surface-variant)}
.mo-head{position:absolute;left:0;right:0;top:0;z-index:4}
.mo-typing{display:inline-flex;gap:4px;padding:14px 16px}
.mo-typing i{width:7px;height:7px;border-radius:50%;background:var(--on-surface-variant);animation:mo-bob 1s infinite ease-in-out}
.mo-typing i:nth-child(2){animation-delay:.15s}.mo-typing i:nth-child(3){animation-delay:.3s}
@keyframes mo-bob{0%,60%,100%{transform:translateY(0);opacity:.5}30%{transform:translateY(-5px);opacity:1}}
/* tabs */
.mo-tabsstage{height:300px}
.mo-title{position:absolute;left:24px;top:28px;right:24px;height:44px;overflow:hidden}
.mo-title span{position:absolute;left:0;top:0;font:800 34px/40px var(--font-sans);letter-spacing:-.8px;font-variation-settings:"wdth" 125,"opsz" 40;white-space:nowrap}
.mo-rows{position:absolute;left:12px;right:12px;top:84px;display:flex;flex-direction:column;gap:6px}
.mo-row{height:44px;border-radius:22px;background:var(--surface-container);display:flex;align-items:center;gap:10px;padding:0 8px}
.mo-row i{width:30px;height:30px;background:var(--a,var(--primary-container));display:block}
.mo-row b{height:10px;border-radius:5px;background:var(--surface-container-highest);display:block}
.mo-bar{position:absolute;left:0;right:0;bottom:16px;display:flex;justify-content:center}
.mo-tab{position:relative;height:48px;border:0;background:transparent;color:var(--on-surface-variant);cursor:pointer;display:flex;align-items:center;padding:0;overflow:hidden;border-radius:24px}
.mo-tab .pill{position:absolute;inset:0;border-radius:24px;background:var(--secondary-container)}
.mo-tab .in{position:relative;display:flex;align-items:center;padding-left:12px;height:48px}
.mo-tab .lb{display:block;overflow:hidden;white-space:nowrap;font:700 14px/20px var(--font-sans);color:var(--on-secondary-container)}
/* swipe */
.mo-list{display:flex;flex-direction:column;padding:6px}
.mo-sw{position:relative;border-radius:24px;overflow:hidden}
.mo-act{position:absolute;inset:0;border-radius:24px;display:flex;align-items:center;justify-content:space-between;padding:0 24px;background:var(--surface-container)}
.mo-act .wash{position:absolute;inset:0;border-radius:24px}
.mo-act .ico{position:relative;width:44px;height:44px;display:grid;place-items:center}
.mo-act .ico .dg-ic{font-size:26px}
.mo-sw .dg-chat{background:var(--surface);touch-action:pan-y;cursor:grab;will-change:transform}
.mo-sw .dg-chat.pinned{background:var(--surface-container-low)}
.mo-sw .dg-chat:active{cursor:grabbing}
.mo-snack{position:absolute;left:12px;right:12px;bottom:12px;z-index:6}
/* pull */
.mo-pull{height:400px;touch-action:none;cursor:grab}
.mo-arch{overflow:hidden;height:0}
.mo-arch .dg-chat{background:var(--surface-container-low);margin:0 6px}
.mo-arch .dg-av{transition:clip-path 320ms cubic-bezier(.34,1.56,.64,1)}
/* ghost */
.mo-gh{height:330px}
.mo-gbar{position:relative;padding:12px 12px 14px 20px;display:flex;align-items:center;gap:6px;overflow:hidden}
.mo-wash{position:absolute;inset:0;background:var(--ghost);pointer-events:none}
.mo-gbar h3{position:relative;flex:1;margin:0;font:800 30px/36px var(--font-sans);letter-spacing:-.6px;font-variation-settings:"wdth" 125,"opsz" 36;transition:color 260ms}
.mo-gbtn{position:relative;height:44px;border:0;cursor:pointer;border-radius:22px;background:var(--surface-container-highest);color:var(--on-surface-variant);display:flex;align-items:center;padding:0 10px;overflow:hidden;transition:background-color 260ms,color 260ms}
.mo-gbtn.on{background:var(--tertiary);color:var(--on-tertiary)}
.mo-gbtn .lb{overflow:hidden;white-space:nowrap;font:700 13px/16px var(--font-sans);display:block}
.mo-gbtn .ib{width:24px;height:24px;position:relative;flex:none}
.mo-gbtn .ib .dg-ic{position:absolute;inset:0}
.mo-gnote{position:relative;overflow:hidden;height:0}
.mo-gnote div{margin:0 12px;padding:10px 14px;border-radius:16px;background:var(--surface-container-high);font:400 13px/18px var(--font-sans);color:var(--on-surface-variant);display:flex;gap:8px;align-items:center}
/* fab */
.mo-fab{height:420px}
.mo-scrim{position:absolute;inset:0;background:var(--scrim);opacity:0;pointer-events:none;z-index:2}
.mo-scrim.on{pointer-events:auto}
.mo-fabzone{position:absolute;right:16px;bottom:16px;z-index:3;display:flex;flex-direction:column;align-items:flex-end;gap:6px}
.mo-fabzone .item{height:56px;border-radius:28px;padding:0 24px 0 18px;display:inline-flex;align-items:center;gap:12px;background:var(--primary-container);color:var(--on-primary-container);font:500 16px/24px var(--font-sans);box-shadow:var(--elevation-3);transform-origin:100% 50%;opacity:0;cursor:pointer;border:0}
.mo-fabbtn{width:56px;height:56px;border:0;cursor:pointer;display:grid;place-items:center;box-shadow:var(--elevation-3);background:var(--primary-container);color:var(--on-primary-container);transition:background-color 220ms,color 220ms}
.mo-fabbtn.on{background:var(--primary);color:var(--on-primary)}
/* open chat */
.mo-open{height:480px}
.mo-over{position:absolute;inset:0;background:var(--surface);z-index:5;display:none}
.mo-over .ct{position:absolute;inset:0}
.mo-avghost{position:absolute;z-index:6;display:none}
/* reactions */
.mo-rx{height:300px}
.mo-rx .dg-b{cursor:pointer}
.mo-chip{display:block;overflow:hidden;height:0}
.mo-chip span{display:inline-flex;align-items:center;gap:4px;margin-top:6px;height:26px;padding:0 10px;border-radius:13px;background:var(--surface);color:var(--on-surface);font:700 12.5px/1 var(--font-sans);transform-origin:0 50%}
.mo-num{display:inline-block;position:relative;height:14px;overflow:hidden;min-width:8px}
.mo-num i{position:absolute;left:0;top:0;font-style:normal}
.mo-p{position:absolute;pointer-events:none;z-index:7;width:12px;height:12px}
.mo-heart{position:absolute;pointer-events:none;z-index:8;font-size:44px;line-height:1}
/* history */
.mo-hist{height:330px}
.mo-tagrow{overflow:hidden;height:0}
.mo-ver{overflow:hidden;height:0}
.mo-ver div{font:400 13px/18px var(--font-sans);padding:4px 0 0;opacity:.8;display:flex;gap:6px}
.mo-ver div s{opacity:.7}
.mo-btns{display:flex;gap:8px;flex-wrap:wrap}
"""

def ic(name, fill=False):
    return f'<span class="dg-ic{" fill" if fill else ""}">{name}</span>'

CARDS = {}

# ------------------------------------------------------------------ 1. Spring curves
CARDS['MotionSprings'] = (520, 'Шкала пружин: позиция и форма с отскоком, цвет без', f'''<div class="dg-stage col" style="gap:14px">
 <canvas id="cv" width="1600" height="360" style="width:100%;max-width:800px;height:auto;aspect-ratio:1600/360"></canvas>
 <div class="mo-lanes" id="lanes"></div>
 <div><button class="dg-btn filled" id="go">{ic("play_arrow",True)}Запустить</button></div>
</div>''', r"""
var names=['spatial-fast','spatial-default','spatial-slow','effects-fast','effects-default','effects-slow'];
var lanes=$('#lanes'),springs=[],on=0;
names.forEach(function(n,i){var d=document.createElement('div');d.className='mo-lane';d.innerHTML='<span class="nm">'+n+'</span><div class="mo-track"><i class="mo-dot sh-cookie9'+(i>2?' fx':'')+'"></i></div>';lanes.appendChild(d);
 var dot=d.querySelector('.mo-dot'),tr=d.querySelector('.mo-track');
 springs.push(new Spring(0,function(p){dot.style.transform='translateX('+(p*(tr.clientWidth-40))+'px) rotate('+(p*120)+'deg)'},n));});
function draw(){var cv=$('#cv'),g=cv.getContext('2d'),cs=getComputedStyle(document.documentElement);var W=cv.width,H=cv.height,pad=40,top=30,bot=50;
 g.clearRect(0,0,W,H);var y0=H-bot,y1=top+40;g.strokeStyle=cs.getPropertyValue('--outline-variant');g.lineWidth=2;
 [0,1].forEach(function(v){var y=L(y0,y1,v);g.beginPath();g.moveTo(pad,y);g.lineTo(W-pad,y);g.stroke()});
 g.fillStyle=cs.getPropertyValue('--on-surface-variant');g.font='500 22px system-ui';g.fillText('0',8,y0+8);g.fillText('1',8,y1+8);g.fillText('0 мс',pad,H-12);g.fillText('600 мс',W-pad-80,H-12);
 names.forEach(function(n,i){var s=SPR[n],x=0,v=0,c=2*s[0]*Math.sqrt(s[1]),h=.001;g.beginPath();
  for(var t=0;t<=.6;t+=h){var a=-s[1]*(x-1)-c*v;v+=a*h;x+=v*h;var px=L(pad,W-pad,t/.6),py=L(y0,y1,x);if(t===0)g.moveTo(px,py);else g.lineTo(px,py)}
  g.strokeStyle=cs.getPropertyValue(i>2?'--tertiary':'--primary');g.globalAlpha=1-(i%3)*.28;g.lineWidth=5;g.stroke();g.globalAlpha=1});}
draw();new MutationObserver(draw).observe(document.documentElement,{attributes:true});
$('#go').addEventListener('click',function(){on=1-on;springs.forEach(function(s){s.to(on)});});
""", '''# MotionSprings

Шкала пружин DelushaGram: шесть токенов `spring`, их отклик во времени и живые дорожки.

- Синие кривые `spatial-*` перелетают цель и возвращаются: так двигаются позиция, размер и форма. Это и даёт «живость».
- Бирюзовые `effects-*` приходят без перелёта: так меняются цвет и прозрачность (перелёт по цвету выглядит как мигание).
- `fast` — отклик на касание (до ~150 мс до цели), `default` — появление элементов (~300 мс), `slow` — переходы экранов (~450 мс).

**Правило:** никаких фиксированных длительностей и кривых Безье в коде; только эти пружины. Длительность получается сама и зависит от расстояния и скорости пальца.

**Android:** `DgSprings.SPATIAL_FAST` и т.д. → `SpringForce().setDampingRatio(z).setStiffness(k)`; для Compose-частей `spring(dampingRatio, stiffness)`.
''')

# ------------------------------------------------------------------ 2. Send morph
CARDS['MotionSend'] = (520, 'Отправка: поле ввода превращается в пузырь', f'''<div class="mo-wrap">
<div class="mo-phone" id="ph" style="height:460px">
 <div class="mo-head"><div class="dg-appbar small"><button class="dg-ib" aria-label="Назад">{ic("arrow_back")}</button><span class="dg-av s t sh-cookie12">А</span><span class="ttl">Аня<small id="st">в сети</small></span><button class="dg-ib" aria-label="Ещё">{ic("more_vert")}</button></div></div>
 <div class="mo-msgs" id="msgs">
  <div class="dg-b in first">Покажи анимацию отправки</div>
  <div class="dg-b in last">Ту, где поле превращается в пузырь</div>
 </div>
 <div class="mo-composer"><div class="dg-composer"><div class="dg-field" id="field"><button class="dg-ib" aria-label="Эмодзи">{ic("mood")}</button><input id="inp" value="Смотри, как летит" placeholder="Сообщение" aria-label="Сообщение"><button class="dg-ib" aria-label="Вложение">{ic("attach_file")}</button></div><button class="dg-send sh-cookie9" id="send" aria-label="Отправить"><span class="dg-ic fill" id="sico">send</span></button></div></div>
</div>
<div class="mo-side"><span class="mo-hint">{ic("touch_app")}Нажми «отправить» или Enter, напиши своё</span>
<p><b>Пузырь вылетает из поля ввода</b>: та же плашка меняет размер, углы и цвет и встаёт в ленту. Лента подвигается пружиной, кнопка поворачивается на 1/9 оборота и морфит иконку.</p>
<p>Ответ собеседника появляется из точки «печатает…» с отскоком.</p>
<p><span class="mo-tok">spatial-default</span> геометрия · <span class="mo-tok">effects-default</span> цвет · <span class="mo-tok">spatial-fast</span> кнопка</p></div>
</div>''', r"""
var ph=$('#ph'),msgs=$('#msgs'),inp=$('#inp'),field=$('#field'),send=$('#send'),sico=$('#sico'),st=$('#st');
var replies=['Вау','Это пружина?','Сделай так же при открытии чата','Огонь','Ещё раз'],ri=0,busy=0;
var rot=new Spring(0,function(x){send.style.transform='rotate('+x+'deg)'},'spatial-fast',.05);
var isc=new Spring(1,function(x){sico.style.transform='scale('+x+')'},'spatial-fast');
function setIcon(name){if(sico.textContent===name)return;isc.to(.4,'effects-fast',function(){sico.textContent=name;isc.to(1,'spatial-fast')})}
function sync(){setIcon(inp.value.trim()?'send':'mic')}
inp.addEventListener('input',sync);
function flip(add){var olds=$$('.dg-b,.mo-tw',msgs),before=olds.map(function(b){return rel(b,ph).y});add();var after=olds.map(function(b){return rel(b,ph).y});
 var p=new Spring(0,function(x){olds.forEach(function(b,i){b.style.transform='translateY('+((before[i]-after[i])*(1-x))+'px)'})},'spatial-default');p.to(1);
 while(msgs.children.length>9)msgs.removeChild(msgs.firstElementChild)}
function lastIs(cls){var l=msgs.lastElementChild;return l&&l.classList.contains(cls)?l:null}
function doSend(){var text=inp.value.trim();if(!text)return;buzz();
 var fr=rel(field,ph);var b=document.createElement('div');b.className='dg-b out first last';b.textContent=text;b.style.visibility='hidden';
 flip(function(){var prev=lastIs('out');if(prev){prev.classList.remove('last');b.classList.remove('first')}msgs.appendChild(b)});
 var to=rel(b,ph);var g=document.createElement('div');g.className='mo-ghost';g.textContent=text;
 g.style.background='var(--surface-container-highest)';g.style.color='var(--on-surface)';ph.appendChild(g);
 var x0=fr.x+40,y0=fr.y,w0=Math.min(fr.w-80,to.w+30),h0=fr.h;
 var s=new Spring(0,function(p){g.style.left=L(x0,to.x,p)+'px';g.style.top=L(y0,to.y,p)+'px';g.style.width=Math.max(20,L(w0,to.w,p))+'px';g.style.height=Math.max(20,L(h0,to.h,p))+'px';g.style.borderRadius=L(26,20,C(p))+'px'},'spatial-default');
 requestAnimationFrame(function(){g.style.background='var(--bubble-out)';g.style.color='var(--on-bubble-out)'});
 s.to(1,null,function(){b.style.visibility='';g.remove()});
 rot.to(rot.t+40,'spatial-fast');inp.value='';sync();
 clearTimeout(window._rt);window._rt=setTimeout(reply,900)}
function reply(){st.innerHTML='<em style="font-style:normal">печатает…</em>';var tw=document.createElement('div');tw.className='dg-b in first last mo-tw';tw.innerHTML='<span class="mo-typing"><i></i><i></i><i></i></span>';tw.style.padding='0';tw.style.transformOrigin='0 100%';
 flip(function(){msgs.appendChild(tw)});var sc=new Spring(0,function(x){tw.style.scale=Math.max(0,x)},'spatial-fast');sc.to(1);
 setTimeout(function(){st.textContent='в сети';var b=document.createElement('div');b.className='dg-b in first last';b.textContent=replies[ri++%replies.length];b.style.transformOrigin='0 100%';
  tw.replaceWith(b);var s2=new Spring(.7,function(x){b.style.scale=x},'spatial-fast');s2.to(1)},1300)}
send.addEventListener('click',function(){if(inp.value.trim())doSend();else{inp.value='Смотри, как летит';sync();inp.focus()}});
inp.addEventListener('keydown',function(e){if(e.key==='Enter'){e.preventDefault();doSend()}});
""", '''# MotionSend

Анимация отправки сообщения — главная подпись DelushaGram, её видишь сотни раз в день.

1. Текст из поля превращается в пузырь: плашка того же цвета, что поле, летит на место нового сообщения, по пути меняет ширину, высоту, углы (26 → 20) и цвет (`surface-container-highest` → `bubble-out`).
2. Лента подвигается вверх на высоту нового пузыря (FLIP), той же пружиной `spatial-default`.
3. Кнопка отправки поворачивается на 40° (1/9 оборота печеньки, форма выглядит той же) пружиной `spatial-fast`, иконка «отправить» сжимается и сменяется микрофоном.
4. Ответ: пузырь «печатает…» выпрыгивает из нижнего левого угла (`spatial-fast`, масштаб 0 → 1), потом заменяется текстом с небольшим отскоком.

**Важно для удобства:** сообщение отправляется в сеть в момент нажатия, анимация идёт поверх и ничего не задерживает. Повторное нажатие во время полёта отправляет следующее сразу.

**Android:** в `ChatActivity` уже есть переход текста из поля (`ChatActivityEnterView` → `ChatMessageCell`, `MessageEnterTransitionContainer`); заменяем его интерполяцию на `SpringAnimation` по токенам и добавляем морф формы фона.
''')

# ------------------------------------------------------------------ 3. Tabs
CARDS['MotionTabs'] = (380, 'Плавающая панель: переключение разделов', f'''<div class="mo-wrap">
<div class="mo-phone mo-tabsstage" id="ph">
 <div class="mo-title" id="title"><span id="t0">Чаты</span><span id="t1"></span></div>
 <div class="mo-rows" id="rows"></div>
 <div class="mo-bar"><div class="dg-toolbar" id="bar"></div></div>
</div>
<div class="mo-side"><span class="mo-hint">{ic("touch_app")}Переключай разделы</span>
<p><b>Выбранная вкладка раздувается в пилюлю с подписью</b>, прошлая сдувается в иконку. Ширины меняются одновременно, поэтому панель «перетекает».</p>
<p>Заголовок уезжает вверх, новый приходит снизу, строки списка появляются лесенкой по 40 мс.</p>
<p><span class="mo-tok">spatial-default</span> ширина и заголовок · <span class="mo-tok">spatial-fast</span> иконка · <span class="mo-tok">effects-default</span> цвет</p></div>
</div>''', r"""
var tabs=[['chat_bubble','Чаты'],['call','Звонки'],['person','Контакты']],cur=0,bar=$('#bar'),ts=[];
tabs.forEach(function(t,i){var b=document.createElement('button');b.className='mo-tab';b.setAttribute('aria-label',t[1]);
 b.innerHTML='<span class="pill"></span><span class="in"><span class="dg-ic">'+t[0]+'</span><span class="lb"><span style="padding:0 18px 0 6px;display:inline-block">'+t[1]+'</span></span></span>';bar.appendChild(b);
 var lb=b.querySelector('.lb'),pill=b.querySelector('.pill'),ico=b.querySelector('.dg-ic');var lw=lb.firstChild.offsetWidth;
 var s=new Spring(i===cur?1:0,function(a){lb.style.width=(a*lw)+'px';b.style.width=(48+Math.max(0,a)*lw)+'px';lb.style.opacity=C(a);pill.style.opacity=C(a);pill.style.transform='scale('+L(.5,1,C(a))+')';b.style.color=a>.5?'var(--on-secondary-container)':'';},'spatial-default');
 var ic=new Spring(1,function(x){ico.style.transform='scale('+x+')'},'spatial-fast');
 ico.style.fontVariationSettings=i===cur?'"FILL" 1':'';
 ts.push({s:s,ic:ic,ico:ico});b.addEventListener('click',function(){go(i)});});
var t0=$('#t0'),t1=$('#t1'),rows=$('#rows'),ttl=new Spring(1,function(){},'spatial-default');
var cols=['var(--primary-container)','var(--tertiary-container)','var(--secondary-container)'];
function fill(i){rows.innerHTML='';var sh=['sh-cookie9','sh-clover4','sh-flower8','sh-pent'];for(var k=0;k<4;k++){var r=document.createElement('div');r.className='mo-row';r.innerHTML='<i class="'+sh[(k+i)%4]+'" style="--a:'+cols[(k+i)%3]+'"></i><span style="display:flex;flex-direction:column;gap:6px"><b style="width:'+(90+((k*37+i*23)%80))+'px"></b><b style="width:'+(140+((k*53+i*17)%60))+'px;opacity:.6"></b></span>';rows.appendChild(r);
 (function(r,k){r.style.opacity=0;var s=new Spring(0,function(p){r.style.opacity=C(p);r.style.transform='translateY('+(1-p)*24+'px)'},'spatial-default');setTimeout(function(){s.to(1)},k*40)})(r,k)}}
fill(0);
function go(i){if(i===cur)return;buzz();var old=cur;cur=i;ts.forEach(function(t,k){t.s.to(k===i?1:0);t.ico.style.fontVariationSettings=k===i?'"FILL" 1':''});ts[i].ic.set(.6).to(1,'spatial-fast');
 t1.textContent=tabs[i][1];var a=t0,b=t1;new Spring(0,function(p){a.style.transform='translateY('+(-p*44)+'px)';a.style.opacity=1-C(p);b.style.transform='translateY('+((1-p)*44)+'px)';b.style.opacity=C(p)},'spatial-default').to(1,null,function(){t0.textContent=tabs[i][1];t0.style.transform='';t0.style.opacity=1;t1.style.opacity=0});fill(i)}
t1.style.opacity=0;
""", '''# MotionTabs

Переключение разделов в плавающей панели.

- Выбранная вкладка расширяется до пилюли с подписью (`spatial-default`), предыдущая одновременно сжимается в иконку: суммарная ширина почти постоянна, панель «перетекает».
- Иконка выбранной сжимается до 60% и отпрыгивает обратно (`spatial-fast`), контур сменяется заливкой.
- Заголовок раздела уезжает вверх с затуханием, новый приходит снизу.
- Строки списка появляются лесенкой: каждая следующая через 40 мс, сдвиг 24 → 0.
- Лёгкая вибрация на смену раздела.

**Android:** `DgFloatingToolbar` анимирует ширины детей через `SpringAnimation` на кастомном свойстве `expansion`; смена фрагментов в `DialogsActivity` без стандартного перехода, заголовок — `DgTopAppBar.setTitleAnimated()`.
''')

# ------------------------------------------------------------------ 4. Chat swipe
CARDS['MotionSwipe'] = (420, 'Свайпы по чатам: закрепить и архив', f'''<div class="mo-wrap">
<div class="mo-phone" id="ph" style="height:350px"><div class="mo-list" id="list"></div><div class="mo-snack" id="snk" style="display:none"><div class="dg-snack"><span class="tx">Чат в архиве</span><button id="undo">Отменить</button></div></div></div>
<div class="mo-side"><span class="mo-hint">{ic("swipe")}Тяни строку влево или вправо</span>
<p><b>Влево — в архив, вправо — закрепить.</b> Пока не дотянул до порога, под строкой серый фон. За порогом цвет заливает фон кругом от иконки, иконка подпрыгивает, телефон коротко вибрирует.</p>
<p>Отпустил раньше — строка возвращается пружиной со скоростью пальца. Дальше порога строка тянется туже.</p>
<p><span class="mo-tok">spatial-fast</span> иконка · <span class="mo-tok">spatial-default</span> возврат и схлопывание</p></div>
</div>''', r"""
var data=[['М','Мама','Купи хлеба по дороге','9:38','e','sh-clover4'],['А','Аня','печатает…','9:40','t','sh-cookie12'],['Л','Лёха','Голосовое, 0:42','8:55','sc','sh-flower8'],['К','Кофе по пятницам','Даша: кто завтра?','Вс','','sh-pent']];
var list=$('#list'),snk=$('#snk'),last=null;
data.forEach(function(d){var w=document.createElement('div');w.className='mo-sw';
 w.innerHTML='<div class="mo-act"><span class="wash"></span><span class="ico l"><span class="dg-ic">push_pin</span></span><span class="ico r"><span class="dg-ic">archive</span></span></div><div class="dg-chat"><span class="dg-av '+d[4]+' '+d[5]+'">'+d[0]+'</span><div class="mid"><div class="nm">'+d[1]+'<span class="dg-ic fill pin" style="display:none">push_pin</span></div><div class="pv">'+d[2]+'</div></div><div class="rt">'+d[3]+'</div></div>';
 list.appendChild(w);setup(w)});
function setup(w){var row=w.querySelector('.dg-chat'),wash=w.querySelector('.wash'),il=w.querySelector('.ico.l'),ir=w.querySelector('.ico.r'),pin=w.querySelector('.pin'),act=w.querySelector('.mo-act');
 var W=function(){return w.clientWidth},T=function(){return W()*.32};var dir=0,armed=false;
 var x=new Spring(0,function(v){row.style.transform='translateX('+v+'px)';var d=v>0?1:v<0?-1:0;if(d!==dir){dir=d;wash.style.background=d>0?'var(--primary-container)':'var(--tertiary-container)';}
  il.style.opacity=v>0?C(v/40):0;ir.style.opacity=v<0?C(-v/40):0;},'spatial-default',.1);
 var wr=new Spring(0,function(r){var ic=dir>0?il:ir,a=rel(ic,w);wash.style.clipPath='circle('+Math.max(0,r)+'px at '+(a.x+a.w/2)+'px '+(a.y+a.h/2)+'px)'},'spatial-default',.1);
 var sc=new Spring(1,function(s){il.style.transform=ir.style.transform='scale('+s+')'},'spatial-fast');
 var col=function(on){il.style.color=on?'var(--on-primary-container)':'var(--on-surface-variant)';ir.style.color=on?'var(--on-tertiary-container)':'var(--on-surface-variant)'};
 wash.style.clipPath='circle(0px at 0 0)';
 var sx=0,dx=0,lastX=0,lastT=0,vel=0,drag=false;
 row.addEventListener('pointerdown',function(e){drag=true;sx=e.clientX-x.x;lastX=e.clientX;lastT=performance.now();row.setPointerCapture(e.pointerId)});
 row.addEventListener('pointermove',function(e){if(!drag)return;var now=performance.now();vel=(e.clientX-lastX)/Math.max(1,now-lastT)*1000;lastX=e.clientX;lastT=now;
  var raw=e.clientX-sx,t=T(),a=Math.abs(raw),v=a<=t?a:t+(a-t)*.35;v*=raw<0?-1:1;x.set(v);
  var on=a>t;if(on!==armed){armed=on;buzz();sc.set(on?.6:1.2).to(1,'spatial-fast');col(on);wr.to(on?W()*1.2:0,'spatial-default')}});
 function up(){if(!drag)return;drag=false;
  if(armed&&x.x<0){x.kick(vel).to(-W()-20,'spatial-default',function(){collapse(w,true)});}
  else if(armed&&x.x>0){x.kick(vel).to(0,'spatial-default');w.querySelector('.dg-chat').classList.toggle('pinned');var on=row.classList.contains('pinned');pin.style.display=on?'':'none';if(on){var ps=new Spring(0,function(s){pin.style.transform='scale('+s+')'},'spatial-fast');ps.to(1)}}
  else x.kick(vel).to(0,'spatial-default');
  armed=false;col(false);wr.to(0,'effects-default')}
 row.addEventListener('pointerup',up);row.addEventListener('pointercancel',up);
 w._reset=function(){x.set(-W()-20);wr.set(0);x.to(0,'spatial-default')}}
function collapse(w,out){var h=w.offsetHeight;var s=new Spring(out?1:0,function(p){w.style.height=Math.max(0,p*h)+'px';w.style.opacity=C(p)},'spatial-default',.002);
 if(out){s.to(0,null,function(){w.style.display='none'});last={w:w,h:h};snk.style.display='';var ss=new Spring(0,function(p){snk.style.transform='translateY('+(1-p)*80+'px)'},'spatial-default');ss.to(1);clearTimeout(window._sk);window._sk=setTimeout(restore,3000)}}
function restore(){if(!last)return;var w=last.w,h=last.h;last=null;w.style.display='';var s=new Spring(0,function(p){w.style.height=Math.max(0,p*h)+'px';w.style.opacity=C(p)},'spatial-default',.002);s.to(1,null,function(){w.style.height=''});w._reset();
 var ss=new Spring(1,function(p){snk.style.transform='translateY('+(1-p)*80+'px)'},'spatial-default');ss.to(0,null,function(){snk.style.display='none'})}
$('#undo').addEventListener('click',restore);
""", '''# MotionSwipe

Свайпы по строкам списка чатов (фича 37). Действия настраиваются: для свайпа влево и вправо по отдельности (архив, закрепить, прочитать, без звука, удалить, ничего).

- До порога (32% ширины) строка едет за пальцем 1:1, под ней `surface-container` и серая иконка.
- Пересёк порог: короткая вибрация, иконка сжимается и отпрыгивает (`spatial-fast`), цвет действия заливает фон кругом из центра иконки.
- Дальше порога сопротивление 35% (резинка), чтобы было понятно, что уже достаточно.
- Отпустил до порога: строка возвращается пружиной `spatial-default`, стартуя со скоростью пальца.
- Архив: строка уезжает за край, место схлопывается, снизу выезжает `Snackbar` «Чат в архиве · Отменить»; отмена проигрывает всё в обратную сторону.

**Удобство:** каждый свайп продублирован в меню долгого тапа по чату.

**Android:** `DialogsActivity` уже имеет `SwipeController`; заменяем отрисовку подложки и анимацию возврата на пружины, порог и действия берём из настроек форка.
''')

# ------------------------------------------------------------------ 5. Pull to archive
CARDS['MotionPull'] = (470, 'Архив свайпом вниз', f'''<div class="mo-wrap">
<div class="mo-phone mo-pull" id="ph">
 <div class="dg-appbar" style="padding-bottom:6px"><h1 style="margin-top:4px">Чаты</h1></div>
 <div class="mo-arch" id="arch"><div class="dg-chat"><span class="dg-av sc sh-cookie9" id="aav"><span class="dg-ic fill">archive</span></span><div class="mid"><div class="nm">Архив</div><div class="pv" id="atx">Потяни, чтобы открыть</div></div><div class="rt"><span class="dg-badge muted">7</span></div></div></div>
 <div class="mo-list" id="rest"></div>
</div>
<div class="mo-side"><span class="mo-hint">{ic("swipe_down")}Тяни список вниз</span>
<p><b>Архив вытягивается сверху, как ящик.</b> Иконка-печенька крутится за пальцем, на пороге морфит в круг и подпрыгивает, текст меняется на «Отпусти».</p>
<p>Можно выключить архив совсем или закрепить его открытым.</p>
<p><span class="mo-tok">spatial-default</span> ящик · <span class="mo-tok">spatial-fast</span> иконка</p></div>
</div>''', r"""
var rest=$('#rest'),arch=$('#arch'),aav=$('#aav'),atx=$('#atx'),ph=$('#ph');
[['Д','Даша','ок, до завтра','10:02','t','sh-cookie12'],['Л','Лёха','Голосовое, 0:42','8:55','sc','sh-flower8'],['М','Мама','Купи хлеба по дороге','9:38','e','sh-clover4']].forEach(function(d){var r=document.createElement('div');r.className='dg-chat';r.innerHTML='<span class="dg-av '+d[4]+' '+d[5]+'">'+d[0]+'</span><div class="mid"><div class="nm">'+d[1]+'</div><div class="pv">'+d[2]+'</div></div><div class="rt">'+d[3]+'</div>';rest.appendChild(r)});
var H=76,open=false,armed=false,sy=0,base=0,drag=false;
var hs=new Spring(0,function(h){arch.style.height=Math.max(0,h)+'px';aav.style.transform='rotate('+(h*2.2)+'deg) scale('+L(.6,1,C(h/H))+')'},'spatial-default',.1);
var bump=new Spring(1,function(s){aav.style.scale=s},'spatial-fast');
ph.addEventListener('pointerdown',function(e){drag=true;sy=e.clientY;base=hs.x;ph.setPointerCapture(e.pointerId)});
ph.addEventListener('pointermove',function(e){if(!drag)return;var d=base+(e.clientY-sy),v=d<=H?d:H+(d-H)*.4;hs.set(Math.max(0,v));
 var on=v>=H&&!open;if(on!==armed){armed=on;buzz();aav.classList.toggle('sh-cookie9',!on);aav.classList.toggle('sh-circle',on);bump.set(.7).to(1);atx.textContent=on?'Отпусти, чтобы закрепить':'Потяни, чтобы открыть'}});
function up(){if(!drag)return;drag=false;if(armed||(open&&hs.x>H*.5)){open=true;hs.to(H)}else{open=false;hs.to(0)}armed=false;aav.classList.add('sh-cookie9');aav.classList.remove('sh-circle');atx.textContent=open?'7 чатов · тяни вверх, чтобы спрятать':'Потяни, чтобы открыть'}
ph.addEventListener('pointerup',up);ph.addEventListener('pointercancel',up);
""", '''# MotionPull

Архив свайпом вниз по списку чатов (фича 40). Варианты в настройках: тянуть вниз (по умолчанию), всегда показан, скрыт совсем.

- Строка архива вытягивается сверху за пальцем, иконка-печенька крутится (2.2° на пиксель) и растёт.
- На пороге (высота строки) — вибрация, печенька морфит в круг, иконка подпрыгивает (`spatial-fast`), подпись «Отпусти, чтобы закрепить».
- Дальше порога сопротивление 40%.
- Отпустил после порога: ящик фиксируется пружиной `spatial-default`. До порога: прячется.
- Тянуть вверх по открытому архиву — прячет обратно.

**Android:** в `DialogsActivity` есть `PullForegroundDrawable` для архива; переписываем его отрисовку (печенька, морф через `androidx.graphics.shapes.Morph`) и меняем анимацию фиксации на пружину.
''')

# ------------------------------------------------------------------ 6. Ghost toggle
CARDS['MotionGhost'] = (400, 'Включение режима призрака', f'''<div class="mo-wrap">
<div class="mo-phone mo-gh" id="ph">
 <div class="mo-gbar" id="bar"><span class="mo-wash" id="wash"></span><h3 id="ttl">Чаты</h3>
  <button class="mo-gbtn" id="gb" aria-pressed="false" aria-label="Режим призрака"><span class="ib"><span class="dg-ic" id="i1">visibility</span><span class="dg-ic fill" id="i2" style="opacity:0">visibility_off</span></span><span class="lb" id="glb"><span style="padding:0 6px 0 8px;display:inline-block">Призрак</span></span></button>
  <span class="dg-av s sh-cookie9" style="position:relative">Д</span></div>
 <div class="mo-gnote" id="note"><div><span class="dg-ic" style="font-size:20px">visibility_off</span>Тебя не видно: «прочитано», онлайн и «печатает» не уходят</div></div>
 <div class="mo-list" id="rest"></div>
</div>
<div class="mo-side"><span class="mo-hint">{ic("touch_app")}Нажми на глаз</span>
<p><b>Призрак включается одним тапом и это видно.</b> Цвет заливает шапку кругом от кнопки, глаз закрывается, кнопка раскрывается в пилюлю с подписью, под шапкой выезжает пояснение.</p>
<p><span class="mo-tok">spatial-slow</span> заливка · <span class="mo-tok">spatial-fast</span> иконка · <span class="mo-tok">spatial-default</span> пилюля и пояснение</p></div>
</div>''', r"""
var rest=$('#rest');[['М','Мама','Купи хлеба по дороге','9:38','e','sh-clover4'],['А','Аня','Приду к восьми','9:40','t','sh-cookie12']].forEach(function(d){var r=document.createElement('div');r.className='dg-chat';r.innerHTML='<span class="dg-av '+d[4]+' '+d[5]+'">'+d[0]+'</span><div class="mid"><div class="nm">'+d[1]+'</div><div class="pv">'+d[2]+'</div></div><div class="rt">'+d[3]+'</div>';rest.appendChild(r)});
var bar=$('#bar'),wash=$('#wash'),gb=$('#gb'),ttl=$('#ttl'),glb=$('#glb'),note=$('#note'),i1=$('#i1'),i2=$('#i2'),on=false;
var lw=glb.firstChild.offsetWidth,nh=note.firstChild.offsetHeight+10;
var r=new Spring(0,function(v){var b=rel(gb,bar);wash.style.clipPath='circle('+Math.max(0,v)+'px at '+(b.x+22)+'px '+(b.y+b.h/2)+'px)'},'spatial-slow',.2);
var pw=new Spring(0,function(a){glb.style.width=Math.max(0,a*lw)+'px';glb.style.opacity=C(a)},'spatial-default');
var nt=new Spring(0,function(a){note.style.height=Math.max(0,a*nh)+'px';note.firstChild.style.opacity=C(a)},'spatial-default');
var ic=new Spring(0,function(a){i1.style.opacity=1-C(a);i2.style.opacity=C(a);i1.style.transform='rotate('+(a*-90)+'deg) scale('+L(1,.3,C(a))+')';i2.style.transform='rotate('+((1-a)*90)+'deg) scale('+L(.3,1,C(a))+')'},'spatial-fast');
pw.set(0);nt.set(0);r.set(0);
gb.addEventListener('click',function(){on=!on;buzz();gb.setAttribute('aria-pressed',on);gb.classList.toggle('on',on);ttl.style.color=on?'var(--on-ghost)':'';
 r.to(on?bar.clientWidth*1.15:0,on?'spatial-slow':'spatial-default');ic.to(on?1:0);pw.to(on?1:0);setTimeout(function(){nt.to(on?1:0)},on?90:0)});
""", '''# MotionGhost

Включение и выключение режима призрака — заметное событие, чтобы никогда не гадать, включён ли он.

- Цвет `ghost` заливает шапку кругом из центра кнопки (`spatial-slow`, около 450 мс), заголовок перекрашивается в `on-ghost`.
- Глаз поворачивается на −90° и сжимается, перечёркнутый глаз приходит поворотом с другой стороны (`spatial-fast`).
- Кнопка раскрывается в пилюлю с подписью «Призрак» (`spatial-default`).
- Через 90 мс под шапкой выезжает пояснение, что именно сейчас скрыто.
- Выключение — всё в обратную сторону, заливка сворачивается в кнопку.

**Android:** заливка — `ViewAnimationUtils.createCircularReveal` нельзя на пружине, поэтому рисуем круг сами в `DgTopAppBar.onDraw` с `SpringAnimation` на радиусе. Плитка в шторке (`TileService`) и ярлык меняют то же состояние.
''')

# ------------------------------------------------------------------ 7. FAB menu
CARDS['MotionFab'] = (480, 'FAB-меню: новый чат, группа, канал', f'''<div class="mo-wrap">
<div class="mo-phone mo-fab" id="ph">
 <div class="dg-appbar" style="padding-bottom:6px"><h1 style="margin-top:4px">Чаты</h1></div>
 <div class="mo-list" id="rest"></div>
 <div class="mo-scrim" id="scrim"></div>
 <div class="mo-fabzone" id="zone">
  <button class="item">{ic("lock")}Секретный чат</button>
  <button class="item">{ic("campaign")}Канал</button>
  <button class="item">{ic("group")}Группа</button>
  <button class="item">{ic("person_add")}Контакт</button>
  <button class="mo-fabbtn" id="fab" aria-label="Новый чат" aria-expanded="false"><span class="dg-ic fill" id="fi">edit</span></button>
 </div>
</div>
<div class="mo-side"><span class="mo-hint">{ic("touch_app")}Нажми на карандаш</span>
<p><b>Пункты вылетают из кнопки лесенкой</b> снизу вверх через 35 мс, каждый с отскоком. Кнопка морфит из скруглённого квадрата в круг, карандаш поворачивается в крестик.</p>
<p>Тап мимо закрывает, пункты убегают обратно быстрее, сверху вниз.</p>
<p><span class="mo-tok">spatial-default</span> пункты · <span class="mo-tok">spatial-fast</span> кнопка · <span class="mo-tok">effects-slow</span> затемнение</p></div>
</div>''', r"""
var rest=$('#rest');[['М','Мама','Купи хлеба по дороге','9:38','e','sh-clover4'],['А','Аня','Приду к восьми','9:40','t','sh-cookie12'],['Л','Лёха','Голосовое, 0:42','8:55','sc','sh-flower8'],['К','Кофе по пятницам','Даша: кто завтра?','Вс','','sh-pent']].forEach(function(d){var r=document.createElement('div');r.className='dg-chat';r.innerHTML='<span class="dg-av '+d[4]+' '+d[5]+'">'+d[0]+'</span><div class="mid"><div class="nm">'+d[1]+'</div><div class="pv">'+d[2]+'</div></div><div class="rt">'+d[3]+'</div>';rest.appendChild(r)});
var items=$$('.item').reverse(),fab=$('#fab'),fi=$('#fi'),scrim=$('#scrim'),on=false;
var ss=items.map(function(it){return new Spring(0,function(p){it.style.opacity=C(p*1.4);it.style.transform='translateY('+((1-p)*24)+'px) scale('+L(.6,1,p)+')';it.style.pointerEvents=p>.5?'auto':'none'},'spatial-default')});
var fs=new Spring(0,function(p){fab.style.borderRadius=L(16,28,p)+'px';fi.style.transform='rotate('+(p*90)+'deg)'},'spatial-fast');
var sc=new Spring(0,function(p){scrim.style.opacity=C(p)*.32},'effects-slow');
fs.set(0);ss.forEach(function(s){s.set(0)});
var swapT;function toggle(v){on=v;buzz();fab.classList.toggle('on',on);fab.setAttribute('aria-expanded',on);scrim.classList.toggle('on',on);fs.to(on?1:0);sc.to(on?1:0,on?'effects-slow':'effects-default');
 clearTimeout(swapT);swapT=setTimeout(function(){fi.textContent=on?'close':'edit'},60);
 ss.forEach(function(s,i){var d=on?i*35:(ss.length-1-i)*20;setTimeout(function(){s.to(on?1:0,on?'spatial-default':'spatial-fast')},d)})}
fab.addEventListener('click',function(){toggle(!on)});scrim.addEventListener('click',function(){toggle(false)});
items.forEach(function(it){it.addEventListener('click',function(){toggle(false)})});
""", '''# MotionFab

Раскрытие FAB-меню «новый чат».

- Затемнение `scrim` до 32% пружиной `effects-slow`.
- Кнопка морфит из `shape-l` (16) в круг и перекрашивается `primary-container` → `primary`, карандаш поворачивается на 90° и сменяется крестиком (`spatial-fast`).
- Пункты вылетают снизу вверх с интервалом 35 мс: сдвиг 24 → 0, масштаб 0.6 → 1 от правого края, `spatial-default`.
- Закрытие быстрее и в обратном порядке (сверху вниз, 20 мс, `spatial-fast`): уходящее не должно задерживать.

**Android:** `DgFabMenu` (свой `FrameLayout` поверх `DialogsActivity`), пружины на `translationY`, `scaleX/Y`, `alpha` — только свойства RenderThread, без relayout.
''')

# ------------------------------------------------------------------ 8. Open chat (container transform)
CARDS['MotionOpen'] = (540, 'Открытие чата: строка раскрывается в экран', f'''<div class="mo-wrap">
<div class="mo-phone mo-open" id="ph">
 <div id="home"><div class="dg-appbar" style="padding-bottom:6px"><h1 style="margin-top:4px">Чаты</h1></div><div class="mo-list" id="rest"></div></div>
 <div class="mo-over" id="over"><div class="ct" id="ct">
  <div class="dg-appbar small"><button class="dg-ib" id="back" aria-label="Назад">{ic("arrow_back")}</button><span class="dg-av s" id="hav" style="visibility:hidden"></span><span class="ttl"><span id="hn"></span><small>в сети</small></span><button class="dg-ib" aria-label="Звонок">{ic("call")}</button></div>
  <div class="dg-msgs" style="position:absolute;left:0;right:0;bottom:72px"><span class="dg-day">Сегодня</span><div class="dg-b in first" id="m1"></div><div class="dg-b in last">Ответь, как сможешь<span class="meta">9:41</span></div><div class="dg-b out first last">Уже открыл<span class="meta">9:42 {ic("done_all")}</span></div></div>
  <div style="position:absolute;left:0;right:0;bottom:0" class="dg-composer"><div class="dg-field"><button class="dg-ib" aria-label="Эмодзи">{ic("mood")}</button><span class="ph">Сообщение</span></div><span class="dg-send sh-cookie9">{ic("mic",True)}</span></div>
 </div></div>
 <span class="dg-av mo-avghost" id="ag"></span>
</div>
<div class="mo-side"><span class="mo-hint">{ic("touch_app")}Открой любой чат, потом «назад»</span>
<p><b>Строка сама становится экраном чата</b>: её фон растягивается на весь экран, аватар перелетает в шапку и уменьшается, список за ней отступает вглубь.</p>
<p>Нажатие «назад» в середине анимации разворачивает её с текущей скорости, без рывка.</p>
<p><span class="mo-tok">spatial-slow</span> открытие · <span class="mo-tok">spatial-default</span> закрытие</p></div>
</div>''', r"""
var rest=$('#rest'),ph=$('#ph'),home=$('#home'),over=$('#over'),ct=$('#ct'),ag=$('#ag'),hav=$('#hav'),hn=$('#hn'),m1=$('#m1');
var data=[['М','Мама','Купи хлеба по дороге','9:38','e','sh-clover4'],['А','Аня','Приду к восьми','9:40','t','sh-cookie12'],['Л','Лёха','Голосовое, 0:42','8:55','sc','sh-flower8'],['К','Кофе по пятницам','Даша: кто завтра?','Вс','','sh-pent'],['Д','Даша','ок, до завтра','Сб','t','sh-sunny']];
var from=null,fromAv=null,cur=null;
data.forEach(function(d){var r=document.createElement('div');r.className='dg-chat';r.innerHTML='<span class="dg-av '+d[4]+' '+d[5]+'">'+d[0]+'</span><div class="mid"><div class="nm">'+d[1]+'</div><div class="pv">'+d[2]+'</div></div><div class="rt">'+d[3]+'</div>';rest.appendChild(r);
 r.addEventListener('click',function(){open(r,d)})});
var p=new Spring(0,function(x){if(!from)return;var W=ph.clientWidth,H=ph.clientHeight,c=C(x);
 var t=L(from.y,0,x),l=L(from.x,0,x),rr=L(W-from.x-from.w,0,x),b=L(H-from.y-from.h,0,x),rad=L(24,28,c);
 over.style.clipPath='inset('+t+'px '+rr+'px '+b+'px '+l+'px round '+rad+'px)';
 over.style.background=c<.15?'var(--surface-container-low)':'var(--surface)';
 ct.style.opacity=C((x-.3)/.45);ct.style.transform='translateY('+((1-c)*24)+'px)';
 home.style.transform='scale('+L(1,.92,c)+')';home.style.opacity=L(1,.5,c);
 var to=rel(hav,ph);ag.style.left=L(fromAv.x,to.x,x)+'px';ag.style.top=L(fromAv.y,to.y,x)+'px';ag.style.width=ag.style.height=L(fromAv.w,to.w,x)+'px';ag.style.fontSize=L(18,16,c)+'px';
},'spatial-slow',.002);
function open(r,d){buzz();cur=r;from=rel(r,ph);var av=r.querySelector('.dg-av');fromAv=rel(av,ph);ag.className='dg-av mo-avghost '+d[4]+' '+d[5];ag.textContent=d[0];ag.style.display='grid';av.style.visibility='hidden';
 hav.className='dg-av s '+d[4]+' '+d[5];hn.textContent=d[1];m1.textContent=d[2];over.style.display='block';p.to(1,'spatial-slow')}
$('#back').addEventListener('click',function(){if(!cur)return;p.to(0,'spatial-default',function(){over.style.display='none';ag.style.display='none';cur.querySelector('.dg-av').style.visibility='';home.style.transform='';home.style.opacity=''})});
""", '''# MotionOpen

Открытие чата из списка — трансформация контейнера (container transform).

- Фон нажатой строки растягивается до размеров экрана, углы 24 → 28, по пути цвет `surface-container-low` → `surface`.
- Аватар перелетает из строки в шапку чата, уменьшаясь 52 → 40, и сохраняет форму.
- Содержимое чата проявляется во второй половине (с 30% хода) и приподнимается на 24.
- Список за чатом отступает: масштаб 0.92, яркость вниз.
- Открытие `spatial-slow`, закрытие `spatial-default` (возвращаться всегда быстрее). «Назад» посреди открытия разворачивает пружину с текущей скоростью.
- Жест «назад» Android 14+ (predictive back): ход пальца напрямую двигает этот же прогресс, отпустил — пружина доводит.

**Android:** свой переход в `ActionBarLayout` для пары `DialogsActivity` → `ChatActivity`: снимок строки, `SpringAnimation` на прогрессе 0…1, общий аватар через `DgAvatarDrawable`. Другие экраны пока на стандартном переходе Telegram.
''')

# ------------------------------------------------------------------ 9. Reactions
CARDS['MotionReactions'] = (380, 'Реакция двойным тапом', f'''<div class="mo-wrap">
<div class="mo-phone mo-rx" id="ph"><div class="dg-msgs" style="position:absolute;left:0;right:0;bottom:0">
 <div class="dg-b in first last" id="b1">Двойной тап сюда<span class="meta">9:41</span><span class="mo-chip" id="c1"><span>❤ <span class="mo-num"><i id="n1">1</i></span></span></span></div>
 <div class="dg-b out first last" id="b2">И сюда тоже<span class="meta">9:42 {ic("done_all")}</span><span class="mo-chip" id="c2"><span>❤ <span class="mo-num"><i id="n2">1</i></span></span></span></div>
</div></div>
<div class="mo-side"><span class="mo-hint">{ic("touch_app")}Двойной тап (или двойной клик) по пузырю</span>
<p><b>Сердце вспыхивает в точке касания</b> и разлетается формами из библиотеки M3, под пузырём выпрыгивает реакция, число прокручивается.</p>
<p>Анимации реакций можно выключить в настройках, тогда реакция просто появляется.</p>
<p><span class="mo-tok">spatial-fast</span> сердце и плашка · частицы на физике с затуханием</p></div>
</div>''', r"""
var ph=$('#ph');
function rx(b,c,n){var on=false,h=32;var sp=c.firstElementChild;
 var hs=new Spring(0,function(a){c.style.height=Math.max(0,a*h)+'px';sp.style.transform='scale('+Math.max(0,a)+')'},'spatial-fast');
 b.addEventListener('dblclick',function(e){e.preventDefault();on=!on;buzz();hs.to(on?1:0,on?'spatial-fast':'spatial-default');if(on){burst(e);roll(n)}});
 var lastTap=0;b.addEventListener('pointerup',function(e){if(e.pointerType==='mouse')return;var t=performance.now();if(t-lastTap<300){b.dispatchEvent(new MouseEvent('dblclick',{clientX:e.clientX,clientY:e.clientY,bubbles:true}))}lastTap=t})}
function roll(el){var o=parseInt(el.textContent,10),v=o+1;var nw=document.createElement('i');nw.textContent=v;el.parentNode.appendChild(nw);var s=new Spring(0,function(p){el.style.transform='translateY('+(-p*14)+'px)';nw.style.transform='translateY('+((1-p)*14)+'px)'},'spatial-default');s.to(1,null,function(){el.textContent=v;el.style.transform='';nw.remove()})}
function burst(e){var r=ph.getBoundingClientRect(),x=e.clientX-r.left,y=e.clientY-r.top;
 var h=document.createElement('span');h.className='mo-heart';h.textContent='❤';h.style.left=(x-22)+'px';h.style.top=(y-26)+'px';h.style.color='var(--error)';ph.appendChild(h);
 var hs=new Spring(0,function(s){h.style.transform='scale('+Math.max(0,s)+')'},'spatial-fast');hs.to(1.3,null,function(){hs.to(0,'spatial-default',function(){h.remove()})});
 if(RM)return;var shapes=['sh-cookie9','sh-flower8','sh-clover4','sh-circle','sh-sunny'],cols=['var(--primary)','var(--tertiary)','var(--error)','var(--primary-container)'],ps=[];
 for(var i=0;i<12;i++){var p=document.createElement('i');p.className='mo-p '+shapes[i%5];p.style.background=cols[i%4];ph.appendChild(p);var a=i/12*Math.PI*2+Math.random()*.3,sp=180+Math.random()*160;ps.push({el:p,x:x-6,y:y-6,vx:Math.cos(a)*sp,vy:Math.sin(a)*sp-60,r:Math.random()*360,s:.6+Math.random()*.8})}
 var t0=performance.now(),last=t0;requestAnimationFrame(function f(now){var dt=(now-last)/1000;last=now;var k=(now-t0)/700;
  ps.forEach(function(q){q.vx*=Math.pow(.02,dt);q.vy=q.vy*Math.pow(.02,dt)+400*dt;q.x+=q.vx*dt;q.y+=q.vy*dt;q.r+=240*dt;q.el.style.transform='translate('+q.x+'px,'+q.y+'px) rotate('+q.r+'deg) scale('+(q.s*(1-k))+')';q.el.style.left=q.el.style.top='0'});
  if(k<1)requestAnimationFrame(f);else ps.forEach(function(q){q.el.remove()})})}
rx($('#b1'),$('#c1'),$('#n1'));rx($('#b2'),$('#c2'),$('#n2'));
""", '''# MotionReactions

Реакция двойным тапом по сообщению.

- В точке касания вспыхивает сердце: масштаб 0 → 1.3 (`spatial-fast`), потом схлопывается.
- 12 частиц из библиотеки форм M3 (печенье, цветок, клевер, круг, солнце) в цветах `primary`, `tertiary`, `error` разлетаются по кругу, гаснут и падают за 700 мс.
- Плашка реакции выпрыгивает под пузырём (высота и масштаб, `spatial-fast`), число прокручивается вверх.
- Повторный двойной тап снимает реакцию: плашка сжимается `spatial-default`.

**Настройка (фича 36):** «Анимации реакций» — полные / только плашка / выкл. Премиум-эффекты стикеров и реакций выключаются отдельно.

**Android:** частицы рисуются в одном `View` поверх `ChatActivity` (без создания View на частицу), формы — кэшированные `Path` из `RoundedPolygon`.
''')

# ------------------------------------------------------------------ 10. History marks
CARDS['MotionHistory'] = (420, 'Удалённые и изменённые сообщения', f'''<div class="mo-wrap">
<div class="mo-phone mo-hist" id="ph"><div class="dg-msgs" style="position:absolute;left:0;right:0;bottom:0">
 <div class="dg-b out first last">Во сколько встречаемся?<span class="meta">9:30 {ic("done_all")}</span></div>
 <div class="dg-b in first" id="del"><div class="mo-tagrow" id="dtag"><span class="tag">{ic("delete")}удалено</span></div>ой, это не тебе<span class="meta">9:33</span></div>
 <div class="dg-b in last" id="ed"><div class="mo-tagrow" id="etag"><span class="tag" style="cursor:pointer">{ic("history")}изменено · 2 версии</span></div><span id="etx">Приду к семи</span><span class="meta">9:35</span><div class="mo-ver" id="ver"><div><s>Приду к семи</s> 9:35</div><div>Приду к восьми · сейчас</div></div></div>
</div></div>
<div class="mo-side"><span class="mo-hint">{ic("touch_app")}Сыграй, что сделал собеседник</span>
<div class="mo-btns"><button class="dg-btn tonal" id="bd">{ic("delete")}Удалил</button><button class="dg-btn tonal" id="be">{ic("edit")}Изменил</button><button class="dg-btn text" id="br">Сначала</button></div>
<p><b>Удалённое не исчезает, а вздрагивает</b> и перекрашивается, сверху выезжает метка. <b>Изменённое</b> плавно меняет текст, метка «изменено» открывает все версии.</p>
<p><span class="mo-tok">spatial-fast</span> встряска · <span class="mo-tok">spatial-default</span> метки и текст · <span class="mo-tok">effects-default</span> цвет</p></div>
</div>''', r"""
var del=$('#del'),dtag=$('#dtag'),ed=$('#ed'),etag=$('#etag'),etx=$('#etx'),ver=$('#ver');
del.style.transition='background-color 300ms cubic-bezier(.2,0,0,1),color 300ms cubic-bezier(.2,0,0,1)';
var th=dtag.firstChild.offsetHeight+2,eh=etag.firstChild.offsetHeight+2,vh=ver.scrollHeight;dtag.style.height=etag.style.height='0px';
var shake=new Spring(0,function(x){del.style.transform='translateX('+x+'px)'},'spatial-fast',.05);
var dt=new Spring(0,function(a){dtag.style.height=Math.max(0,a*th)+'px';dtag.style.opacity=C(a)},'spatial-default');
var et=new Spring(0,function(a){etag.style.height=Math.max(0,a*eh)+'px';etag.style.opacity=C(a)},'spatial-default');
var vs=new Spring(0,function(a){ver.style.height=Math.max(0,a*vh)+'px';ver.style.opacity=C(a)},'spatial-default');ver.style.height='0px';
var tx=new Spring(1,function(a){etx.style.opacity=C(a);etx.style.filter='blur('+((1-C(a))*4)+'px)'},'effects-default');
$('#bd').addEventListener('click',function(){buzz();del.classList.add('deleted');shake.set(0).kick(-700).to(0);dt.to(1)});
$('#be').addEventListener('click',function(){if(etx.textContent==='Приду к восьми')return;buzz();tx.to(0,'effects-fast',function(){etx.textContent='Приду к восьми';tx.to(1,'effects-default')});et.to(1)});
etag.addEventListener('click',function(){vs.to(vs.t?0:1)});
$('#br').addEventListener('click',function(){del.classList.remove('deleted');dt.to(0);et.to(0);vs.to(0);etx.textContent='Приду к семи'});
""", '''# MotionHistory

Как форк показывает удалённые и изменённые собеседником сообщения (история сообщений).

**Удалено:**
- Пузырь не пропадает. Он вздрагивает по горизонтали: пружина `spatial-fast` с начальным толчком 700 px/с, затухает за 3 колебания.
- Цвет `bubble-in` → `bubble-deleted` (`effects-default`).
- Сверху выезжает метка «удалено» с иконкой корзины (высота 0 → строка, `spatial-default`).

**Изменено:**
- Старый текст расплывается и гаснет, новый проявляется (размытие 4 → 0), высота пузыря подстраивается пружиной.
- Появляется метка «изменено · N версии» (`tertiary`). Тап по ней раскрывает список версий внутри пузыря: старые зачёркнуты, с временем.

**Android:** события удаления и правки приходят в `MessagesController` (`updateDeleteMessages`, `updateEditMessage`), форк сохраняет копию в свою БД и вызывает `ChatMessageCell.animateHistoryMark(type)`.
''')
