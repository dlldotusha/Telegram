# Pixel filter, second layer: pixel avatar, pixel unread counter, mosaic glass (pixel blur).
import json, math, os, random
import pixel as PX
import json as _j

D = os.path.dirname(os.path.abspath(__file__))
CH = json.load(open(os.path.join(D, 'chars.json')))

def shape_mask(n, kind):
    """Rasterize the ORIGINAL avatar shape (circle or an M3 shape) into n x n cells."""
    S = {'circle': (1, 0, 0), 'cookie9': (9, .065, 0), 'cookie12': (12, .045, 0), 'clover4': (4, .26, math.pi/4), 'flower8': (8, .13, 0), 'sunny': (10, .06, 0), 'pent': (5, .12, -math.pi/2), 'cookie4': (4, .07, math.pi/4), 'cookie7': (7, .075, 0)}
    k, a, rot = S[kind]; R = n / 2; cells = []
    for r in range(n):
        for c in range(n):
            x = c + .5 - R; y = r + .5 - R; t = math.atan2(y, x)
            rr = (1 + a * math.cos(k * (t - rot))) / (1 + a) * R
            if math.hypot(x, y) <= rr: cells.append((c, r))
    return cells

def avatar_px(letter, shape='circle', cls='', size=52, n=13, seed=2):
    """Avatar through the pixel filter: shape mask in the avatar colour, letter rasterized 7x7 on top."""
    rnd = random.Random(seed); p = size / n; s = p * .86
    let = set((c + 3, r + 3) for c, r in CH['letters9'].get(letter, []))
    out = []
    for c, r in shape_mask(n, shape):
        on = (c, r) in let; k = rnd.random()
        fx = '' if on else (' gl' if k < .16 else ' rg' if k < .24 else ' c' if k < .3 else '')
        out.append(f'<i class="{"l" if on else "b"}{fx}" style="left:{c*p:.2f}px;top:{r*p:.2f}px;width:{s:.2f}px;height:{s:.2f}px"></i>')
    return f'<span class="dg-av pxav {cls}" style="width:{size}px;height:{size}px">{"".join(out)}</span>'

def digits_px(text, pitch=3.2, size=2.7):
    """Pixel digits (5x7 each, rasterized from Roboto Flex 700)."""
    out = []; x0 = 0
    for ch in text:
        for c, r in CH['digits7'][ch]:
            out.append(f'<i style="left:{(x0+c)*pitch:.1f}px;top:{r*pitch:.1f}px;width:{size}px;height:{size}px"></i>')
        x0 += 6
    w = (x0 - 1) * pitch; h = 7 * pitch
    return f'<span class="pxd" style="width:{w:.1f}px;height:{h:.1f}px">{"".join(out)}</span>'

def unread_px(count):
    return f'<span class="pxun">{PX.glyph_svg("unread", 22)}{digits_px(str(count))}</span>'

def mosaic(w, h, pitch=10, size=8.6, radius=0, seed=9, cls='mz'):
    """Mosaic glass: each pixel blurs what is under it on its own (pixel blur)."""
    rnd = random.Random(seed); cols = int(w // pitch); rows = int(h // pitch); out = []
    for r in range(rows):
        for c in range(cols):
            if radius:
                x = c + .5; y = r + .5; rad = radius
                dx = max(rad - x, 0, x - (cols - rad)); dy = max(rad - y, 0, y - (rows - rad))
                if dx * dx + dy * dy > rad * rad: continue
            k = rnd.random(); fx = ' hi' if k < .08 else ' lo' if k < .14 else ''
            out.append(f'<i class="{fx.strip()}" style="left:{c*pitch}px;top:{r*pitch}px;width:{size}px;height:{size}px"></i>')
    return f'<div class="{cls}" style="width:{cols*pitch}px;height:{rows*pitch}px">{"".join(out)}</div>'

JSDATA = 'var PXD=' + _j.dumps(CH['digits7']) + ';'

CSS = r"""
/* ---------- pixel filter II ---------- */
.dg-av.pxav{position:relative;display:block;background:none!important;clip-path:none!important;border-radius:0!important;flex:none}
.pxav i{position:absolute;border-radius:22%}
.pxav i.b{background:color-mix(in srgb,var(--av-bg,var(--primary-container)) 72%,var(--av-fg,var(--on-primary-container)))}
.pxav i.l{background:var(--av-fg,var(--on-primary-container))}
.pxav i.gl{background:color-mix(in srgb,var(--av-bg,var(--primary-container)) 55%,transparent);box-shadow:inset 0 0 0 .5px color-mix(in srgb,#fff 60%,transparent)}
.pxav i.rg{background:transparent;box-shadow:inset 0 0 0 1px var(--av-bg,var(--primary-container))}
.pxav i.c{border-radius:50%}
.pxd{position:relative;display:inline-block;flex:none}
.pxd i{position:absolute;border-radius:1px;background:currentColor}
.pxun{display:inline-flex;align-items:center;gap:8px;color:var(--primary);height:44px;padding:0 4px}
/* mosaic glass = pixel blur */
.mz{position:absolute;left:0;top:0;pointer-events:none}
.mz i{position:absolute;border-radius:2px;background:color-mix(in srgb,var(--surface) 52%,transparent);-webkit-backdrop-filter:blur(7px) saturate(1.6);backdrop-filter:blur(7px) saturate(1.6)}
.mz i.hi{background:color-mix(in srgb,var(--surface) 30%,transparent)}
.mz i.lo{background:color-mix(in srgb,var(--surface) 72%,transparent)}
.mz.scrim i{background:color-mix(in srgb,var(--scrim) 22%,transparent);-webkit-backdrop-filter:blur(9px);backdrop-filter:blur(9px)}
.mz.scrim i.hi{background:color-mix(in srgb,var(--scrim) 10%,transparent)}
.mz.scrim i.lo{background:color-mix(in srgb,var(--scrim) 34%,transparent)}
/* bar tiles become pixel glass too */
.px-bar .pt{-webkit-backdrop-filter:blur(6px) saturate(1.6);backdrop-filter:blur(6px) saturate(1.6);background:color-mix(in srgb,var(--surface-container-highest) 82%,transparent)}
.px-bar .blur{display:block;inset:4px 2px;border-radius:24px;background:color-mix(in srgb,var(--surface) 35%,transparent)}
@keyframes px-twinkle{0%,100%{filter:none}40%{filter:brightness(1.6) drop-shadow(0 0 3px var(--primary))}}
.px-bar .pt.tw{animation:px-twinkle 1.4s ease-in-out}
"""

JS = r"""
function pxDigits(host,text,pitch,size){pitch=pitch||3.2;size=size||2.7;var cells=[],x0=0;text.split('').forEach(function(ch){(PXD[ch]||[]).forEach(function(c){cells.push([(x0+c[0])*pitch,c[1]*pitch])});x0+=6});
 var old=[].slice.call(host.children);host.style.width=((x0-1)*pitch)+'px';
 old.forEach(function(e,i){var s=new Spring(0,function(p){e.style.transform='translateY('+(p*14)+'px)';e.style.opacity=1-C(p)},'effects-fast');setTimeout(function(){s.to(1,null,function(){e.parentNode&&e.parentNode.removeChild(e)})},i*4)});
 cells.forEach(function(c,i){var e=document.createElement('i');e.style.left=c[0]+'px';e.style.top=c[1]+'px';e.style.width=e.style.height=size+'px';host.appendChild(e);var s=new Spring(0,function(p){e.style.transform='translateY('+((1-p)*-14)+'px) scale('+L(.3,1,C(p))+')';e.style.opacity=C(p)},'spatial-fast');s.set(0);setTimeout(function(){s.to(1)},60+i*6)})}

function pxTwinkle(tiles,every){setInterval(function(){for(var n=0;n<3;n++){var t=tiles[Math.floor(Math.random()*tiles.length)];if(!t)continue;t.classList.remove('tw');void t.offsetWidth;t.classList.add('tw')}},every||500)}
"""
