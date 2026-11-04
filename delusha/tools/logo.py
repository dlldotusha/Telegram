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
