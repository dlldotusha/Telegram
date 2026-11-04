import math
N=64
def poly(n,a,rot=0):
    p=[]
    for i in range(N):
        t=i/N*2*math.pi; r=(1+a*math.cos(n*(t-rot)))/(1+a)*50
        p.append(f"{50+r*math.cos(t):.1f}% {50+r*math.sin(t):.1f}%")
    return "polygon("+",".join(p)+")"
S={'circle':(1,0),'cookie4':(4,.07,math.pi/4),'cookie7':(7,.075),'cookie9':(9,.065),'cookie12':(12,.045),'clover4':(4,.26,math.pi/4),'flower8':(8,.13),'sunny':(10,.06),'pent':(5,.12,-math.pi/2)}
def css():
    out="".join(f".sh-{k}{{clip-path:{poly(*v)}}}\n" for k,v in S.items())
    out+="@keyframes dg-morph{0%%{clip-path:%s;transform:rotate(0)}25%%{clip-path:%s}50%%{clip-path:%s}75%%{clip-path:%s}100%%{clip-path:%s;transform:rotate(360deg)}}\n"%(poly(*S['cookie7']),poly(*S['clover4']),poly(*S['flower8']),poly(*S['pent']),poly(*S['cookie7']))
    return out
def svgpath(n,a,cx,cy,R,rot=0,steps=96):
    pts=[]
    for i in range(steps):
        t=i/steps*2*math.pi; r=(1+a*math.cos(n*(t-rot)))/(1+a)*R
        pts.append(f"{cx+r*math.cos(t):.1f},{cy+r*math.sin(t):.1f}")
    return "M"+" L".join(pts)+"Z"
