# Pixel effects (glass, glow, ring, gradient) and pixel buttons with glyph morphing.
import json, random
import pixel as PX

FX_CSS = r"""
/* ---------- pixel effects: each pixel its own, the picture stays clear ---------- */
.fp.gl{background:color-mix(in srgb,var(--primary) 72%,transparent);box-shadow:inset 0 0 0 .6px color-mix(in srgb,#fff 60%,transparent)}
.fp.gw{box-shadow:0 0 3px 0 color-mix(in srgb,var(--primary) 90%,transparent)}
.fp.rg{background:color-mix(in srgb,var(--primary) 40%,transparent);box-shadow:inset 0 0 0 1px var(--primary)}
.fp.gd{background:linear-gradient(135deg,color-mix(in srgb,var(--primary) 75%,#fff),var(--primary))}
.fp.g.gw{box-shadow:none;animation:none}
.pt.gl{background:color-mix(in srgb,var(--surface-container-highest) 45%,transparent);box-shadow:inset 0 0 0 1px color-mix(in srgb,#fff 45%,transparent),inset 1px 1px 0 color-mix(in srgb,#fff 60%,transparent)}
.pt.gw{background:color-mix(in srgb,var(--primary) 30%,var(--surface-container-highest));box-shadow:0 0 6px 1px color-mix(in srgb,var(--primary) 45%,transparent)}
[data-theme=dark] .pt.gl,[data-theme=amoled] .pt.gl{box-shadow:inset 0 0 0 1px color-mix(in srgb,#fff 18%,transparent),inset 1px 1px 0 color-mix(in srgb,#fff 25%,transparent)}
@keyframes px-glow{0%,100%{filter:brightness(1)}50%{filter:brightness(1.25)}}
.fp.gw{animation:px-glow 2.4s ease-in-out infinite}
.fp.gw:nth-child(3n){animation-delay:-.8s}.fp.gw:nth-child(3n+1){animation-delay:-1.6s}
.pxb{position:relative;flex:none;cursor:pointer;touch-action:none;filter:drop-shadow(0 4px 10px rgba(0,0,0,.18))}
.pxb .bgl,.pxb .gyl{position:absolute;inset:0}
.pxb .fp{left:0;top:0;will-change:transform}
.pxrec{display:flex;align-items:center;gap:8px;font:500 15px/20px var(--font-sans);color:var(--on-surface);font-variant-numeric:tabular-nums}
.pxrec i{width:10px;height:10px;border-radius:3px;background:var(--error);animation:px-blink 1s steps(2) infinite}
@keyframes px-blink{50%{opacity:.2}}
@media (prefers-reduced-motion:reduce){.fp.gw{animation:none}}
"""

def fab_fx(glyph='edit', n=17, pitch=3.9, size=3.3, seed=3):
    """Pixel cookie button 17x17 with a 13x13 glyph and per-pixel effects (static HTML)."""
    rnd = random.Random(seed); g = set((c + 2, r + 2) for c, r in PX.glyph_cells(glyph)); out = []
    for c, r in PX.rrect_cells(n):
        on = (c, r) in g; k = rnd.random()
        if on: cls = 'fp g'
        else: cls = 'fp' + (' gl' if k < .14 else ' gw' if k < .24 else ' rg' if k < .31 else ' gd' if k < .41 else ' c' if k < .47 else '')
        out.append(f'<i class="{cls}" style="left:{c*pitch:.1f}px;top:{r*pitch:.1f}px;width:{size}px;height:{size}px"></i>')
    return ''.join(out)

def bar_fx(cols, rows, pitch=8, seed=5):
    rnd = random.Random(seed); out = []
    for c, r in PX.rounded_mask(cols, rows, 3):
        k = rnd.random()
        cls = 'pt' + (' gl' if k < .16 else ' r' if k < .23 else ' d' if k < .28 else ' c' if k < .33 else ' gw' if k < .355 else '')
        out.append(f'<i class="{cls}" style="left:{c*pitch}px;top:{r*pitch}px"></i>')
    return ''.join(out)

GLYPHS_JS = 'var PXG=' + json.dumps({k: PX.glyph_cells(k) for k in PX.GLYPHS}) + ';'
COOKIE_JS = 'var PXC=[];for(var _r=0;_r<17;_r++)for(var _c=0;_c<17;_c++)PXC.push([_c,_r]);function pxIn(c,r,n,rad){var x=c+.5,y=r+.5,dx=Math.max(rad-x,0,x-(n-rad)),dy=Math.max(rad-y,0,y-(n-rad));return dx*dx+dy*dy<=rad*rad}'

JS = GLYPHS_JS + COOKIE_JS + r"""
function pxButton(host,o){o=o||{};var P=o.pitch||3.9,S=o.size||3.3,N=17,glyph=o.glyph||'mic';host.classList.add('pxb');host.style.width=host.style.height=(N*P)+'px';
 var bgl=document.createElement('div');bgl.className='bgl';host.appendChild(bgl);var gyl=document.createElement('div');gyl.className='gyl';host.appendChild(gyl);
 var bg=PXC.map(function(cr){var e=document.createElement('i'),k=Math.random();e.className='fp'+(k<.14?' gl':k<.24?' gw':k<.31?' rg':k<.41?' gd':k<.47?' c':'');e.style.width=e.style.height=S+'px';bgl.appendChild(e);
  var it={e:e,x:cr[0]*P,y:cr[1]*P,c:cr[0],r:cr[1],sc:1};it.s=new Spring(1,function(v){it.sc=v;place(it)},'spatial-fast');return it});
 function place(it){it.e.style.transform='translate('+it.x+'px,'+it.y+'px) scale('+it.sc+')'}
 var rad=4.5;function setRadius(R,anim){rad=R;bg.forEach(function(it){var on=pxIn(it.c,it.r,N,R);var d=Math.hypot(it.c-8,it.r-8);if(anim)setTimeout(function(){it.s.to(on?1:0)},(12-d)*8);else it.s.set(on?1:0)})}
 setRadius(4.5,false);
 var gp=[];
 function mk(x,y){var e=document.createElement('i');e.className='fp g';e.style.width=e.style.height=S+'px';gyl.appendChild(e);
  var it={e:e,x:x,y:y,sc:0};it.px=new Spring(x,function(v){it.x=v;place(it)},'spatial-default');it.py=new Spring(y,function(v){it.y=v;place(it)},'spatial-default');it.s=new Spring(0,function(v){it.sc=v;place(it)},'spatial-fast');return it}
 function setGlyph(name,animate){var cells=PXG[name].map(function(c){return [(c[0]+2)*P,(c[1]+2)*P]});var used=[],next=[];
  cells.forEach(function(t){var best=-1,bd=1e9;gp.forEach(function(p,i){if(used[i])return;var d=Math.hypot(p.x-t[0],p.y-t[1]);if(d<bd){bd=d;best=i}});
   if(best>=0){used[best]=1;var p=gp[best];next.push(p);if(animate){p.px.to(t[0]);p.py.to(t[1]);p.s.to(1)}else{p.px.set(t[0]);p.py.set(t[1]);p.s.set(1)}}
   else{var q=mk(8.5*P,8.5*P);next.push(q);if(animate){q.px.set(8*P);q.py.set(8*P);setTimeout(function(){q.px.to(t[0]);q.py.to(t[1]);q.s.to(1)},40)}else{q.px.set(t[0]);q.py.set(t[1]);q.s.set(1)}}});
  gp.forEach(function(p,i){if(!used[i]){p.s.to(0,'effects-fast',function(){p.e.remove()})}});gp=next;host.dataset.glyph=name}
 setGlyph(glyph,false);
 var lvl=0,raf=0;function level(on){if(on&&!raf){var t0=performance.now();raf=requestAnimationFrame(function f(t){var v=.35+.65*Math.abs(Math.sin((t-t0)/170)*Math.sin((t-t0)/410+1));lvl=v;
   bg.forEach(function(it,i){if(!pxIn(it.c,it.r,N,rad))return;var d=Math.hypot(it.x-8*P,it.y-8*P)/(8.5*P);it.sc=1+v*.55*(1-d)*(.6+.4*Math.sin(i*1.7+(t-t0)/90));place(it);it.e.style.filter='brightness('+(1+v*.5*(1-d))+')'});raf=requestAnimationFrame(f)})}
  if(!on&&raf){cancelAnimationFrame(raf);raf=0;bg.forEach(function(it){it.e.style.filter='';it.s.set(it.sc).to(pxIn(it.c,it.r,N,rad)?1:0)})}}
 function burst(){bg.forEach(function(it){if(!pxIn(it.c,it.r,N,rad))return;var d=Math.hypot(it.x-8*P,it.y-8*P);setTimeout(function(){it.s.set(1).kick(-16).to(1)},d*5)})}
 return {setGlyph:setGlyph,level:level,burst:burst,setRadius:setRadius,host:host}}
"""
