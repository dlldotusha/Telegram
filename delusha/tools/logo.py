import math
def dgrid(W,H,s):
    """Pixel letter D: W x H cells, stroke s cells. Returns set of (c,r)."""
    R=H/2; cx=W-R
    out=set()
    for r in range(H):
        for c in range(W):
            x=c+.5; y=r+.5
            def inside(x,y,inset):
                if x<inset or y<inset or y>H-inset: return False
                if x<=cx: return True
                return (x-cx)**2+(y-R)**2 <= (R-inset)**2
            if inside(x,y,0) and not inside(x,y,s):
                out.add((c,r))
    return out
def ascii(g,W,H):
    return '\n'.join(''.join('#' if (c,r) in g else '.' for c in range(W)) for r in range(H))

import random
def svg_mark(fill, cell=10, gap=1.6, rx=2.2, W=9, H=11, s=3, scatter=False, bg=None, pad=0, size=None):
    g=sorted(dgrid(W,H,s))
    rects=[]
    random.seed(4)
    for (c,r) in g:
        x=pad+c*cell+gap/2; y=pad+r*cell+gap/2; w=cell-gap
        rects.append(f'<rect x="{x:.1f}" y="{y:.1f}" width="{w:.1f}" height="{w:.1f}" rx="{rx}"/>')
    extra=''
    if scatter:
        # a few pixels breaking away from the bowl, shrinking as they go
        pts=[(9.6,2.2,.8),(10.6,4.6,.65),(11.4,1.4,.5),(10.2,8.4,.7),(11.6,6.6,.45),(12.3,3.6,.35),(11.2,9.8,.5)]
        for (c,r,k) in pts:
            w=(cell-gap)*k; x=pad+c*cell; y=pad+r*cell
            extra+=f'<rect x="{x:.1f}" y="{y:.1f}" width="{w:.1f}" height="{w:.1f}" rx="{rx*k:.1f}"/>'
    vw=pad*2+(W+(4 if scatter else 0))*cell; vh=pad*2+H*cell
    b=f'<rect width="{vw}" height="{vh}" fill="{bg}"/>' if bg else ''
    sz=f' width="{size}" height="{size*vh/vw:.0f}"' if size else ''
    return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {vw} {vh}"{sz}>{b}<g fill="{fill}">{"".join(rects)}{extra}</g></svg>'

def svg_badge(bg='#2e9e5b', ink='#ffffff', ring=None):
    # 18x18 online badge: rounded square, 4x5 pixel D
    g=dgrid(4,5,1); cell=2.4; ox=(18-4*cell)/2; oy=(18-5*cell)/2
    rects=''.join(f'<rect x="{ox+c*cell+.25:.2f}" y="{oy+r*cell+.25:.2f}" width="{cell-.5:.2f}" height="{cell-.5:.2f}" rx=".5"/>' for c,r in g)
    rg=f'<rect x="-3" y="-3" width="24" height="24" rx="9" fill="{ring}"/>' if ring else ''
    vb='-3 -3 24 24' if ring else '0 0 18 18'
    return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="{vb}">{rg}<rect width="18" height="18" rx="6" fill="{bg}"/><g fill="{ink}">{rects}</g></svg>'

if __name__=='__main__':
    open('logo/mark-primary.svg','w').write(svg_mark('#6741b1'))
    open('logo/mark-white.svg','w').write(svg_mark('#ffffff'))
    open('logo/mark-scatter.svg','w').write(svg_mark('#6741b1',scatter=True))
    open('logo/badge-online.svg','w').write(svg_badge())
    # adaptive icon foreground on background (108 grid, mark inside 66 safe zone)
    m=svg_mark('#e7ddff',cell=4.4,gap=.8,rx=1)
    inner=m[m.index('<g'):m.rindex('</svg>')]
    open('logo/app-icon.svg','w').write(f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108"><rect width="108" height="108" fill="#4a3290"/><g transform="translate({54-9*4.4/2+2:.1f},{54-11*4.4/2:.1f})">{inner}</g></svg>')
    print('ok')

# ---------- varied pixels: every pixel has its own form, fill and overlap
STYLES = ['fill']*6 + ['ring', 'dot', 'circle', 'overlap', 'half', 'inset', 'cookie', 'circle']
def _cookie(cx, cy, R, n=8, a=.12, steps=32):
    pts = []
    for i in range(steps):
        t = i/steps*2*math.pi; r = (1+a*math.cos(n*t))/(1+a)*R
        pts.append(f"{cx+r*math.cos(t):.2f},{cy+r*math.sin(t):.2f}")
    return "M" + " L".join(pts) + "Z"

def pixels_varied(W=9, H=11, s=3, cell=10, ox=0, oy=0, seed=7, cls=('p1', 'p2', 'p3'), simple=False):
    """SVG elements for a pixel D whose pixels differ: filled, outlined, round, overlapping, split."""
    rnd = random.Random(seed); g = sorted(dgrid(W, H, s)); out = []
    sw = cell*.14; inner = cell*(.9 if simple else .84); pad = (cell-inner)/2
    for (c, r) in g:
        x = ox+c*cell+pad; y = oy+r*cell+pad; cx = x+inner/2; cy = y+inner/2
        st = rnd.choice(STYLES if not simple else ['fill', 'fill', 'fill', 'circle'])
        k = (cls[0] if len(cls)<3 else rnd.choice([cls[0]]*4+[cls[1]]*2+[cls[2]])) if not simple else rnd.choice([cls[0]]*3+[cls[-1]])
        if len(cls)==2 and not simple: k = rnd.choice([cls[0]]*3+[cls[1]])
        rx = inner*.22
        if st == 'fill':
            out.append(f'<rect class="{k}" x="{x:.2f}" y="{y:.2f}" width="{inner:.2f}" height="{inner:.2f}" rx="{rx:.2f}"/>')
        elif st == 'ring':
            out.append(f'<rect class="{k}o" x="{x+sw/2:.2f}" y="{y+sw/2:.2f}" width="{inner-sw:.2f}" height="{inner-sw:.2f}" rx="{rx:.2f}" stroke-width="{sw:.2f}"/>')
        elif st == 'dot':
            out.append(f'<rect class="{k}o" x="{x+sw/2:.2f}" y="{y+sw/2:.2f}" width="{inner-sw:.2f}" height="{inner-sw:.2f}" rx="{rx:.2f}" stroke-width="{sw:.2f}"/><circle class="{k}" cx="{cx:.2f}" cy="{cy:.2f}" r="{inner*.18:.2f}"/>')
        elif st == 'circle':
            out.append(f'<circle class="{k}" cx="{cx:.2f}" cy="{cy:.2f}" r="{inner/2:.2f}"/>')
        elif st == 'overlap':
            d = inner*.16; w = inner-d
            out.append(f'<rect class="{k}" x="{x:.2f}" y="{y:.2f}" width="{w:.2f}" height="{w:.2f}" rx="{rx*.8:.2f}" opacity=".85"/><rect class="{cls[(cls.index(k)+1)%len(cls)]}" x="{x+d:.2f}" y="{y+d:.2f}" width="{w:.2f}" height="{w:.2f}" rx="{rx*.8:.2f}" opacity=".75"/>')
        elif st == 'half':
            out.append(f'<rect class="{k}o" x="{x+sw/2:.2f}" y="{y+sw/2:.2f}" width="{inner-sw:.2f}" height="{inner-sw:.2f}" rx="{rx:.2f}" stroke-width="{sw:.2f}"/><path class="{k}" d="M{x:.2f},{y+inner:.2f} L{x+inner:.2f},{y:.2f} L{x+inner:.2f},{y+inner-rx:.2f} Q{x+inner:.2f},{y+inner:.2f} {x+inner-rx:.2f},{y+inner:.2f} Z"/>')
        elif st == 'inset':
            out.append(f'<rect class="{k}" x="{x:.2f}" y="{y:.2f}" width="{inner:.2f}" height="{inner:.2f}" rx="{rx:.2f}"/><rect class="hole" x="{x+inner*.3:.2f}" y="{y+inner*.3:.2f}" width="{inner*.4:.2f}" height="{inner*.4:.2f}" rx="{inner*.08:.2f}"/>')
        elif st == 'cookie':
            out.append(f'<path class="{k}" d="{_cookie(cx, cy, inner/2)}"/>')
    return ''.join(out)

def svg_varied(colors=('#6741b1', '#9a6cf0', '#1f7a72'), hole='#efe9fb', W=9, H=11, s=3, cell=10, seed=7, simple=False, size=None, bg=None):
    style = ''.join(f'.p{i+1}{{fill:{c}}}.p{i+1}o{{fill:none;stroke:{c}}}' for i, c in enumerate(colors)) + f'.hole{{fill:{hole}}}'
    vw, vh = W*cell, H*cell
    b = f'<rect width="{vw}" height="{vh}" fill="{bg}"/>' if bg else ''
    sz = f' width="{size}" height="{size*vh/vw:.0f}"' if size else ''
    return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {vw} {vh}"{sz}><style>{style}</style>{b}{pixels_varied(W,H,s,cell,seed=seed,simple=simple)}</svg>'

def badge_inline(kind, size=18, live=False):
    """Inline SVG badge using theme classes (bundle.css): sup = supporter, user = Delusha user, online."""
    if kind == 'sup':
        px = pixels_varied(4, 5, 1, 2.4, ox=2.2, oy=1.0, seed=5, cls=('bp1', 'bp2'), simple=True)
        return f'<svg class="dgb dgb-sup{" live" if live else ""}" viewBox="0 0 14 14" width="{size}" height="{size}"><rect class="bbg" width="14" height="14" rx="4.5"/>{px}</svg>'
    if kind == 'user':
        g = dgrid(4, 5, 1)
        px = ''.join(f'<rect class="bo" x="{2.2+c*2.4+.35:.2f}" y="{1+r*2.4+.35:.2f}" width="1.7" height="1.7" rx=".4" stroke-width=".55"/>' for c, r in g)
        return f'<svg class="dgb dgb-user" viewBox="0 0 14 14" width="{size}" height="{size}"><rect class="bbg" width="14" height="14" rx="4.5"/>{px}</svg>'
