# Pixel language of DelushaGram: glyphs, pixel bar, pixel FAB (static HTML) + JS for live pixel motion.
import math, random

GLYPHS = {
 'search': [".###...", "#...#..", "#...#..", "#...#..", ".###...", "....##.", ".....##"],
 'edit':   [".....##", "....###", "...###.", "..###..", ".###...", "##.#...", "###...."],
 'camera': ["..###..", "#######", "##...##", "#..#..#", "##...##", "#######", "......."],
 'ghost':  ["#......", ".#.###.", "#.#...#", "#..#..#", "#...#.#", ".###.#.", "......#"],
 'add':    ["...#...", "...#...", "...#...", "#######", "...#...", "...#...", "...#..."],
 'back':   ["...#...", "..#....", ".#.....", "#######", ".#.....", "..#....", "...#..."],
 'more':   ["...#...", ".......", ".......", "...#...", ".......", ".......", "...#..."],
 'send':   ["#......", "###....", "#####..", "#######", "#####..", "###....", "#......"],
 'mic':    ["..###..", "..###..", "..###..", "#.###.#", "#.....#", ".#####.", "...#..."],
}

def glyph_cells(name):
    g = GLYPHS[name]
    return [(c, r) for r, row in enumerate(g) for c, ch in enumerate(row) if ch == '#']

def glyph_svg(name, size=24, seed=1, cls='pg'):
    """Pixel icon: 7x7 cells, mostly filled squares, some circles. Colour = currentColor."""
    rnd = random.Random(seed + hash(name) % 97)
    cell = 10; inner = 9.2; pad = (cell - inner) / 2; out = []
    for c, r in glyph_cells(name):
        x = c * cell + pad; y = r * cell + pad; st = rnd.random()
        if st < .88: out.append(f'<rect x="{x:.1f}" y="{y:.1f}" width="{inner}" height="{inner}" rx="2"/>')
        elif st < .97: out.append(f'<circle cx="{x+inner/2:.1f}" cy="{y+inner/2:.1f}" r="{inner/2:.1f}"/>')
        else: out.append(f'<rect x="{x+.8:.1f}" y="{y+.8:.1f}" width="{inner-1.6:.1f}" height="{inner-1.6:.1f}" rx="2" fill="none" stroke="currentColor" stroke-width="1.6"/>')
    return f'<svg class="{cls}" viewBox="0 0 70 70" width="{size}" height="{size}" fill="currentColor" aria-hidden="true">{"".join(out)}</svg>'

def rounded_mask(cols, rows, cut=1):
    """Cells of a rounded rectangle (corners trimmed)."""
    cells = []
    for r in range(rows):
        for c in range(cols):
            dc = min(c, cols - 1 - c); dr = min(r, rows - 1 - r)
            if dc + dr < cut: continue
            cells.append((c, r))
    return cells

def bar_pixels(cols, rows, pitch=12, size=10, seed=5):
    rnd = random.Random(seed); out = []
    for c, r in rounded_mask(cols, rows, 2):
        k = rnd.random(); cls = 'pt' + (' r' if k < .1 else ' d' if k < .16 else ' c' if k < .22 else '')
        out.append(f'<i class="{cls}" style="left:{c*pitch}px;top:{r*pitch}px"></i>')
    return ''.join(out)

def cookie_cells(n=9):
    R = n / 2; cells = []
    for r in range(n):
        for c in range(n):
            x = c + .5 - R; y = r + .5 - R; t = math.atan2(y, x)
            rr = (1 + .1 * math.cos(9 * t)) / 1.1 * R
            if math.hypot(x, y) <= rr: cells.append((c, r))
    return cells

def fab_pixels(glyph='edit', n=9, pitch=7.4, size=6.4, seed=3):
    """Pixel cookie FAB with a 7x7 glyph centered (offset 1,1)."""
    rnd = random.Random(seed); g = set((c + 1, r + 1) for c, r in glyph_cells(glyph)); out = []
    for c, r in cookie_cells(n):
        on = (c, r) in g; k = rnd.random()
        cls = 'fp' + (' g' if on else '') + (' c' if (k < .14 and not on) else '')
        out.append(f'<i class="{cls}" style="left:{c*pitch:.1f}px;top:{r*pitch:.1f}px;width:{size}px;height:{size}px"></i>')
    return ''.join(out)

CSS = r"""
/* ---------- Pixel language ---------- */
.pg{display:block;flex:none}
.px-bar{position:relative;height:60px;flex:1;min-width:0}
.px-bar .blur{position:absolute;inset:0;border-radius:18px;-webkit-backdrop-filter:blur(var(--blur-glass)) saturate(1.5);backdrop-filter:blur(var(--blur-glass)) saturate(1.5)}
.px-bar .tiles{position:absolute;inset:0;pointer-events:none}
.px-bar .content{z-index:1}
.pt{position:absolute;width:10px;height:10px;border-radius:3px;background:color-mix(in srgb,var(--surface-container-highest) 90%,transparent)}
.pt.r{background:transparent;box-shadow:inset 0 0 0 1.5px var(--outline-variant)}
.pt.d{background:var(--outline-variant);box-shadow:inset 0 0 0 3px color-mix(in srgb,var(--surface-container-highest) 90%,transparent)}
.pt.c{border-radius:50%}
.px-bar .content{position:absolute;inset:0;display:flex;align-items:center;gap:12px;padding:0 18px 0 4px;color:var(--on-surface-variant);font:400 16px/24px var(--font-sans)}
.px-bar .content .dg-av{width:52px;height:52px;font-size:18px}
.px-fab{position:relative;width:66px;height:66px;flex:none;filter:drop-shadow(0 4px 10px rgba(0,0,0,.2))}
.fp{position:absolute;border-radius:2px;background:var(--primary)}
.fp.c{border-radius:50%}
.fp.g{background:var(--on-primary)}
.px-bottom{position:absolute;left:12px;right:12px;bottom:30px;display:flex;align-items:center;gap:10px;z-index:9}
.px-cam{position:absolute;right:20px;bottom:110px;z-index:9;width:46px;height:46px;border-radius:14px;display:grid;place-items:center;color:var(--on-surface);background:var(--surface-glass);-webkit-backdrop-filter:blur(var(--blur-glass));backdrop-filter:blur(var(--blur-glass));box-shadow:var(--elevation-2)}
.dg-ghost .pg{width:18px;height:18px}
.px-top{position:absolute;left:28px;top:20px;width:320px;height:48px;z-index:8;display:none}
.px-top .pt{left:0;top:0}
.px-topc{position:absolute;inset:0;display:flex;align-items:center;gap:12px;padding:0 16px;color:var(--on-surface);font:400 16px/24px var(--font-sans)}
.px-dyn{position:absolute;left:0;top:0;border-radius:3px;pointer-events:none;will-change:transform;z-index:20}
"""

JS = r"""
function pxBar(host,o){o=o||{};var P=o.pitch||12,S=o.size||10,cols=Math.floor(host.clientWidth/P),rows=o.rows||5,tiles=[];
 var box=document.createElement('div');box.className='tiles';host.insertBefore(box,host.querySelector('.content'));
 for(var r=0;r<rows;r++)for(var c=0;c<cols;c++){var dc=Math.min(c,cols-1-c),dr=Math.min(r,rows-1-r);if(dc+dr<2)continue;
  var t=document.createElement('i'),k=Math.random();t.className='pt'+(k<.1?' r':k<.16?' d':k<.22?' c':'');box.appendChild(t);
  var tx=c*P,ty=r*P;(function(t,tx,ty){var s=new Spring(1,function(p){t.style.transform='translate('+L(t._fx,tx,p)+'px,'+L(t._fy,ty,p)+'px) scale('+L(.2,1,C(p))+')';t.style.opacity=C(p*1.4)},'spatial-default');t._fx=tx;t._fy=ty;t._s=s;t._tx=tx;t._ty=ty;s.set(1);tiles.push(t)})(t,tx,ty)}
 return tiles}
function pxGather(tiles,fx,fy,open,delayK){tiles.forEach(function(t){var d=Math.hypot(t._tx-fx,t._ty-fy);t._fx=fx+(Math.random()-.5)*20;t._fy=fy+(Math.random()-.5)*20;
 if(open){t._s.set(0);setTimeout(function(){t._s.to(1,'spatial-default')},d*(delayK||.9)+Math.random()*60)}else{setTimeout(function(){t._s.to(0,'spatial-fast')},Math.max(0,260-d*.8)*.6)}})}
function pxRipple(tiles,cx,cy,R){tiles.forEach(function(t){var d=Math.hypot(t._tx+5-cx,t._ty+5-cy);if(d>R)return;setTimeout(function(){var s=new Spring(1,function(x){t.style.scale=x},'spatial-fast');s.set(1).kick(18*(1-d/R)).to(1)},d*2.2)})}
function pxShatter(el,root,color){var r=rel(el,root),G=9,ps=[];el.style.visibility='hidden';
 for(var y=0;y<r.h;y+=G)for(var x=0;x<r.w;x+=G){var d=document.createElement('i');d.className='px-dyn';d.style.width=d.style.height=(G-1.5)+'px';d.style.background=color;root.appendChild(d);
  ps.push({el:d,x:r.x+x,y:r.y+y,vx:(x/r.w-.5)*260+(Math.random()-.5)*160,vy:-120-Math.random()*220,rot:0,vr:(Math.random()-.5)*720,delay:(x/r.w)*140+Math.random()*60})}
 var t0=performance.now(),last=t0;requestAnimationFrame(function f(now){var dt=Math.min(.04,(now-last)/1000);last=now;var alive=0;
  ps.forEach(function(p){var age=now-t0-p.delay;if(age<0){p.el.style.transform='translate('+p.x+'px,'+p.y+'px)';alive++;return}
   p.vy+=900*dt;p.x+=p.vx*dt;p.y+=p.vy*dt;p.rot+=p.vr*dt;var a=1-age/900;if(a>0)alive++;p.el.style.opacity=Math.max(0,a);p.el.style.transform='translate('+p.x+'px,'+p.y+'px) rotate('+p.rot+'deg) scale('+Math.max(.2,a)+')'});
  if(alive)requestAnimationFrame(f);else ps.forEach(function(p){p.el.remove()})})}
"""
