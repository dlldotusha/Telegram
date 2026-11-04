# Pixel motion cards appended to motion.CARDS
import pixel as _px
from motion import CARDS, ic

CARDS.pop('MotionMenu', None)

CARDS['MotionPixelBar'] = (620, 'Нижняя панель из пикселей знака', f'''<div class="mo-wrap">
<div class="mo-phone" id="ph" style="height:560px">
 <div class="mo-list" id="rest" style="padding-bottom:110px"></div>
 <div class="px-top" id="top"></div>
 <div class="px-bottom" id="bb" style="bottom:16px"><div class="px-bar" id="bar"><div class="blur"></div><div class="content" id="cnt"><span class="dg-av persona persona-me sh-cookie9" id="me">Д</span>{_px.glyph_svg('search',24)}<span>Поиск</span></div></div><div class="px-fab" id="fab">{_px.fab_pixels('edit')}</div></div>
</div>
<div class="mo-side"><span class="mo-hint">{ic("touch_app")}Прокрути список, нажми «новое сообщение», нажми «Поиск»</span>
<div class="mo-btns"><button class="dg-btn tonal" id="again">{ic("refresh")}Собрать заново</button></div>
<p><b>Панель сделана из тех же пикселей, что и знак.</b> При запуске она собирается из аватара, при прокрутке вниз пиксели стягиваются к аватару и кнопке, при прокрутке вверх разлетаются обратно.</p>
<p>Нажатие пускает по пикселям волну. «Поиск» рассыпается и собирается полем наверху.</p>
<p><span class="mo-tok">spatial-default</span> сборка, <span class="mo-tok">spatial-fast</span> волна и сжатие</p></div>
</div>''', _px.JS + r"""
var ph=$('#ph'),bar=$('#bar'),cnt=$('#cnt'),fab=$('#fab'),topEl=$('#top'),rest=$('#rest');
for(var k=0;k<14;k++){var d=[['А','Аня','печатает…','t'],['М','Мама','Купи хлеба по дороге','e'],['Л','Лёха','Голосовое, 0:42','sc'],['К','Кофе по пятницам','Даша: кто завтра?','']][k%4];var r=document.createElement('div');r.className='dg-chat';r.innerHTML='<span class="dg-av '+d[3]+'">'+d[0]+'</span><div class="mid"><div class="nm">'+d[1]+'</div><div class="pv">'+d[2]+'</div></div><div class="rt">9:4'+(k%10)+'</div>';rest.appendChild(r)}
rest.style.overflowY='auto';rest.style.height='560px';rest.style.scrollbarWidth='none';
var tiles=pxBar(bar,{rows:7});
var fps=$$('.fp',fab).map(function(p){var s=new Spring(1,function(x){p.style.transform='scale('+x+')'},'spatial-fast');s.set(1);return {p:p,s:s,x:parseFloat(p.style.left),y:parseFloat(p.style.top)}});
var cs=new Spring(1,function(p){cnt.style.opacity=C((p-.4)/.6)},'effects-default');
function assemble(){pxGather(tiles,26,30,true,.9);cs.set(0);cs.to(1);fps.forEach(function(f){f.s.set(0);setTimeout(function(){f.s.to(1)},120+Math.hypot(f.x-30,f.y-30)*6)})}
assemble();$('#again').addEventListener('click',assemble);
var collapsed=false,lastY=0;
rest.addEventListener('scroll',function(){var y=rest.scrollTop,d=y-lastY;lastY=y;if(d>4&&!collapsed){collapsed=true;squeeze(true)}else if(d<-4&&collapsed){collapsed=false;squeeze(false)}});
function squeeze(on){var meX=26,fx=bar.clientWidth+40;tiles.forEach(function(t){var toMe=t._tx<bar.clientWidth/2;t._fx=toMe?meX:fx;t._fy=24;var d=Math.abs(t._tx-(toMe?meX:fx));
 if(on)setTimeout(function(){t._s.to(0,'spatial-fast')},Math.max(0,200-d*.6));else setTimeout(function(){t._s.to(1,'spatial-default')},d*.6)});
 $$('.pg,span:not(.dg-av)',cnt).forEach(function(e){e.style.transition='opacity .18s';e.style.opacity=on?0:1})}
fab.addEventListener('click',function(e){buzz();var b=fab.getBoundingClientRect();var cx=e.clientX-b.left,cy=e.clientY-b.top;
 fps.forEach(function(f){var d=Math.hypot(f.x+3-cx,f.y+3-cy);setTimeout(function(){f.s.set(1).kick(-14).to(1)},d*5)});
 pxRipple(tiles,bar.clientWidth+30,30,260)});
bar.addEventListener('click',function(e){if(e.target.closest('#me'))return;buzz();var r=rel(bar,ph);
 tiles.forEach(function(t){t._s.to(0,'effects-fast')});cnt.style.opacity=0;
 var cols=39,G=8;topEl.innerHTML='';topEl.style.display='block';
 for(var j=0;j<6;j++)for(var i=0;i<cols;i++){var d=document.createElement('i');d.className='pt';topEl.appendChild(d);var tx=i*G,ty=j*G;
  (function(d,tx,ty){var sx=r.x-28+Math.random()*r.w,sy=r.y-20+Math.random()*50;var s=new Spring(0,function(p){d.style.transform='translate('+L(sx,tx,p)+'px,'+L(sy,ty,p)+'px)';d.style.opacity=C(p*1.5)},'spatial-default');s.set(0);setTimeout(function(){s.to(1)},Math.random()*160)})(d,tx,ty)}
 setTimeout(function(){topEl.insertAdjacentHTML('beforeend','<div class="px-topc">'+$('#cnt .pg').outerHTML+'<span>а</span></div>')},320);
 setTimeout(function(){topEl.style.transition='opacity .25s';topEl.style.opacity=0;setTimeout(function(){topEl.style.display='none';topEl.style.opacity=1;topEl.style.transition='';assemble()},260)},1600)});
""", '''# MotionPixelBar

Нижняя панель главного экрана, собранная из тех же пикселей, что и знак.

- **Сборка:** при запуске пиксели вылетают из аватара лесенкой по расстоянию (`spatial-default`), затем проявляется содержимое; пиксели FAB-печеньки появляются от центра.
- **Прокрутка вниз:** пиксели левой половины стягиваются в аватар, правой — в FAB (`spatial-fast`), подписи гаснут. Остаются только аватар и кнопка. Прокрутка вверх разворачивает панель обратно.
- **Нажатие:** вместо ряби — пиксельная волна. Пиксели вокруг точки касания и пиксели FAB вздрагивают от толчка с задержкой по расстоянию.
- **Поиск:** панель рассыпается, пиксели взлетают наверх и складываются в поле поиска; при закрытии поиск рассыпается и панель собирается снова.
- При «Меньше движения» пиксели не летают: панель просто появляется и исчезает.

**Android:** `DgPixelBar` рисует пиксели одним `View` на `Canvas`, у каждого пикселя `SpringAnimation` по позиции и масштабу; состояние «сжата» следит за `RecyclerView.OnScrollListener`.
''')

CARDS['MotionShatter'] = (470, 'Удаление: сообщение рассыпается на пиксели', f'''<div class="mo-wrap">
<div class="mo-phone" id="ph" style="height:420px"><div class="dg-wall"></div><div class="dg-msgs" id="ms" style="position:absolute;left:0;right:0;bottom:0"></div></div>
<div class="mo-side"><span class="mo-hint">{ic("touch_app")}Нажми на своё сообщение, чтобы удалить</span>
<div class="mo-btns"><button class="dg-btn tonal" id="again">{ic("refresh")}Вернуть</button></div>
<p><b>Своё удалённое сообщение рассыпается на пиксели</b> слева направо, они подлетают, кружатся и падают. Лента закрывает пустое место пружиной.</p>
<p><span class="mo-tok">spatial-default</span> лента, пиксели на физике с гравитацией</p></div>
</div>''', _px.JS + r"""
var ph=$('#ph'),ms=$('#ms');
function fill(){ms.innerHTML='<div class="dg-b in first last">Ты где?<span class="meta">9:31</span></div><div class="dg-b out first">Уже бегу</div><div class="dg-b out">Буду через пять минут, возьму тебе кофе</div><div class="dg-b out last">И круассан<span class="meta">9:32</span></div><div class="dg-b in first last">Жду</div>';
 $$('.dg-b.out',ms).forEach(function(b){b.style.cursor='pointer';b.addEventListener('click',function(){del(b)})})}
function del(b){buzz();var all=$$('.dg-b,.dg-day',ms);var before=all.map(function(x){return rel(x,ph).y});
 pxShatter(b,ph,getComputedStyle(b).backgroundColor);
 setTimeout(function(){b.style.display='none';var after=all.map(function(x){return rel(x,ph).y});
  var s=new Spring(0,function(p){all.forEach(function(x,i){if(x!==b)x.style.transform='translateY('+((before[i]-after[i])*(1-p))+'px)'})},'spatial-default');s.to(1)},240)}
fill();$('#again').addEventListener('click',fill);
""", '''# MotionShatter

Удаление своего сообщения: пузырь рассыпается на пиксели его цвета (сетка 9 px), слева направо, с задержкой по позиции. Каждый пиксель подлетает, вращается и падает под гравитацией, затухая за 0,9 с. Через 240 мс лента закрывает пустое место пружиной `spatial-default`.

Чужие удалённые сообщения не рассыпаются: они остаются в истории форка и только вздрагивают (MotionHistory).

**Android:** снимок пузыря в `Bitmap`, разрезанный на ячейки, рисуется поверх `ChatActivity` в одном `View`; физика в цикле `Choreographer`.
''')

import pixel_fx as _fx
CARDS['MotionPixelButtons'] = (560, 'Главные кнопки из пикселей: отправка, голосовое, кружок', f'''<div class="mo-wrap">
<div class="mo-phone" id="ph" style="height:480px"><div class="dg-wall"></div>
 <div class="dg-msgs" id="ms" style="position:absolute;left:0;right:0;top:0;bottom:84px"><div class="dg-b in first last">Запиши голосовое, как будет минутка</div></div>
 <div style="position:absolute;left:0;right:0;bottom:0;display:flex;align-items:center;gap:8px;padding:8px 10px 14px">
  <div class="dg-field dg-glass" id="fld" style="flex:1"><button class="dg-ib" aria-label="Эмодзи">{ic("mood")}</button><input id="inp" placeholder="Сообщение" aria-label="Сообщение" style="flex:1;min-width:0;border:0;background:transparent;outline:0;font:400 16px/22px var(--font-sans);color:var(--on-surface);padding:15px 4px"><button class="dg-ib" aria-label="Прикрепить">{ic("attach_file")}</button></div>
  <div class="pxrec" id="rec" style="display:none;flex:1;padding-left:14px"><i></i><span id="tm">0:00</span></div>
  <div id="pb"></div>
 </div>
</div>
<div class="mo-side"><span class="mo-hint">{ic("touch_app")}Напиши текст, нажми кнопку; тап по микрофону, зажми его</span>
<p><b>Кнопка собрана из пикселей знака</b>, и у каждого пикселя свой эффект: заливка, стекло, свечение, рамка, градиент. Вместе картинка остаётся чёткой.</p>
<p>Пустое поле — микрофон. Начал печатать — пиксели микрофона перелетают в самолётик. Тап по микрофону — пиксели складываются в камеру (кружок) и обратно. Зажал — кнопка растёт, пиксели дышат в такт голосу и светятся.</p>
<p><span class="mo-tok">spatial-default</span> морфинг глифа, <span class="mo-tok">spatial-fast</span> отклик</p></div>
</div>''', _fx.JS + r"""
var ph=$('#ph'),inp=$('#inp'),ms=$('#ms'),fld=$('#fld'),rec=$('#rec'),tm=$('#tm');
var B=pxButton($('#pb'),{glyph:'mic',pitch:3.4,size:2.9});var mode='mic';
var sc=new Spring(1,function(v){B.host.style.transform='scale('+v+')'},'spatial-fast');
inp.addEventListener('input',function(){var want=inp.value.trim()?'send':mode;if(B.host.dataset.glyph!==want)B.setGlyph(want,true)});
function add(html,cls){var b=document.createElement('div');b.className='dg-b out first last '+(cls||'');b.innerHTML=html;b.style.transformOrigin='100% 100%';ms.appendChild(b);var s=new Spring(.6,function(v){b.style.scale=v},'spatial-fast');s.to(1);while(ms.children.length>7)ms.removeChild(ms.firstElementChild)}
var downT=0,holdTimer=null,recording=false,t0=0,tick=null;
B.host.addEventListener('pointerdown',function(e){B.host.setPointerCapture(e.pointerId);downT=performance.now();sc.to(.88);
 if(B.host.dataset.glyph==='send')return;
 holdTimer=setTimeout(function(){recording=true;buzz();sc.to(1.45,'spatial-default');B.level(true);fld.style.display='none';rec.style.display='flex';t0=performance.now();tick=setInterval(function(){var s=Math.floor((performance.now()-t0)/1000);tm.textContent='0:'+String(s).padStart(2,'0')},250)},260)});
function up(){clearTimeout(holdTimer);
 if(recording){recording=false;clearInterval(tick);B.level(false);sc.to(1,'spatial-default');rec.style.display='none';fld.style.display='';var s=Math.max(1,Math.round((performance.now()-t0)/1000));add(mode==='mic'?'Голосовое, 0:'+String(s).padStart(2,'0'):'Кружок, 0:'+String(s).padStart(2,'0'));return}
 sc.to(1);var g=B.host.dataset.glyph;
 if(g==='send'){buzz();B.burst();add(inp.value.replace(/</g,'&lt;'));inp.value='';B.setGlyph(mode,true)}
 else if(performance.now()-downT<260){buzz();mode=mode==='mic'?'camera':'mic';B.setGlyph(mode,true)}}
B.host.addEventListener('pointerup',up);B.host.addEventListener('pointercancel',up);
""", '''# MotionPixelButtons

Главные кнопки чата собраны из пикселей знака: отправить, голосовое, кружок. Фон — печенька 17 × 17 пикселей, глиф — 13 × 13 поверх, растеризован из Material Symbols.

- **У каждого пикселя свой эффект**: обычная заливка (большинство), стекло (полупрозрачный с бликом), свечение (мягко пульсирует), рамка, градиент, круг. Глиф всегда сплошной цвета `on-primary`, поэтому знак читается.
- **Морфинг глифа.** Пиксели не исчезают, а перелетают на новые места (`spatial-default`): микрофон ↔ самолётик, когда в поле появляется или пропадает текст; микрофон ↔ камера по тапу (голосовое или кружок). Лишние пиксели гаснут, недостающие вылетают из центра.
- **Отправка:** короткий импульс по пикселям от центра наружу.
- **Запись** (зажать): кнопка растёт до 145%, пиксели дышат в такт громкости и светятся сильнее у центра, слева таймер с мигающим пиксельным индикатором. Отпустил — запись уходит, кнопка возвращается.
- Те же правила для FAB «новое сообщение» и камеры историй.

**Android:** `DgPixelButton` на `Canvas`: список пикселей с эффектами (`RenderEffect` для стекла и свечения на Android 12+, иначе заливка), `SpringAnimation` на позиции пикселей при смене глифа, уровень громкости из `MediaRecorder.getMaxAmplitude()`.
''')
