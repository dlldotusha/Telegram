# Generative, animated, grayscale pixel-D logo (JS). Needs Spring/$ from motion.ENGINE.
LOGO_JS = r"""
function dgLogo(svg, o){o=o||{};var W=9,H=11,S=3,cell=10,R=H/2,cx=W-R,cells=[];
 function inside(x,y,i){if(x<i||y<i||y>H-i)return false;if(x<=cx)return true;return (x-cx)*(x-cx)+(y-R)*(y-R)<=(R-i)*(R-i)}
 for(var r=0;r<H;r++)for(var c=0;c<W;c++){var x=c+.5,y=r+.5;if(inside(x,y,0)&&!inside(x,y,S))cells.push([c,r])}
 var ST=['fill','fill','fill','fill','fill','ring','dot','circle','overlap','half','inset','cookie','circle'];
 var TN=['g1','g1','g1','g1','g2','g2','g3'];
 var rnd=o.seed!=null?(function(s){return function(){s=(s*16807)%2147483647;return (s-1)/2147483646}})(o.seed):Math.random;
 function pick(a){return a[Math.floor(rnd()*a.length)]}
 var NS='http://www.w3.org/2000/svg',inner=cell*.84,pad=(cell-inner)/2,sw=cell*.14,rx=inner*.22;
 function el(t,a){var e=document.createElementNS(NS,t);for(var k in a)e.setAttribute(k,a[k]);return e}
 function cookie(R){var p=[];for(var i=0;i<32;i++){var t=i/32*Math.PI*2,rr=(1+.12*Math.cos(8*t))/1.12*R;p.push((inner/2+rr*Math.cos(t)).toFixed(2)+','+(inner/2+rr*Math.sin(t)).toFixed(2))}return 'M'+p.join(' L')+'Z'}
 function draw(g,st,k){while(g.firstChild)g.removeChild(g.firstChild);var k2=k==='g1'?'g2':'g1';
  if(st==='fill')g.appendChild(el('rect',{class:k,width:inner,height:inner,rx:rx}));
  else if(st==='ring')g.appendChild(el('rect',{class:k+'o',x:sw/2,y:sw/2,width:inner-sw,height:inner-sw,rx:rx,'stroke-width':sw}));
  else if(st==='dot'){g.appendChild(el('rect',{class:k+'o',x:sw/2,y:sw/2,width:inner-sw,height:inner-sw,rx:rx,'stroke-width':sw}));g.appendChild(el('circle',{class:k,cx:inner/2,cy:inner/2,r:inner*.18}))}
  else if(st==='circle')g.appendChild(el('circle',{class:k,cx:inner/2,cy:inner/2,r:inner/2}));
  else if(st==='overlap'){var d=inner*.16,w=inner-d;g.appendChild(el('rect',{class:k,width:w,height:w,rx:rx*.8,opacity:.85}));g.appendChild(el('rect',{class:k2,x:d,y:d,width:w,height:w,rx:rx*.8,opacity:.75}))}
  else if(st==='half'){g.appendChild(el('rect',{class:k+'o',x:sw/2,y:sw/2,width:inner-sw,height:inner-sw,rx:rx,'stroke-width':sw}));g.appendChild(el('path',{class:k,d:'M0,'+inner+' L'+inner+',0 L'+inner+','+(inner-rx)+' Q'+inner+','+inner+' '+(inner-rx)+','+inner+' Z'}))}
  else if(st==='inset'){g.appendChild(el('rect',{class:k,width:inner,height:inner,rx:rx}));g.appendChild(el('rect',{class:'hole',x:inner*.3,y:inner*.3,width:inner*.4,height:inner*.4,rx:inner*.08}))}
  else g.appendChild(el('path',{class:k,d:cookie(inner/2)}));}
 svg.setAttribute('viewBox','0 0 '+W*cell+' '+H*cell);
 var px=cells.map(function(cr){var g=el('g',{});svg.appendChild(g);var bx=cr[0]*cell+pad,by=cr[1]*cell+pad;
  var sp=new Spring(1,function(s){g.setAttribute('transform','translate('+(bx+inner/2*(1-s))+','+(by+inner/2*(1-s))+') scale('+s+')')},'spatial-fast');
  draw(g,pick(ST),pick(TN));sp.set(1);return {g:g,sp:sp}});
 if(o.animate===false||RM)return;
 setInterval(function(){for(var n=0;n<(o.per||3);n++){var p=pick(px);(function(p){p.sp.to(.35,'effects-fast',function(){draw(p.g,pick(ST),pick(TN));p.sp.to(1,'spatial-fast')})})(p)}},o.every||700);
}
"""
LOGO_CSS = r"""
/* grayscale logo: no accent */
.dg-logo{--lg1:#1b1b1d;--lg2:#6a6a70;--lg3:#a9a9ae;--lgh:#ffffff}
[data-theme=dark] .dg-logo,[data-theme=amoled] .dg-logo,.dg-logo.inv{--lg1:#f4f4f5;--lg2:#a9a9ae;--lg3:#6a6a70;--lgh:#141416}
[data-theme=amoled] .dg-logo{--lgh:#000}
.dg-logo .g1{fill:var(--lg1)}.dg-logo .g1o{fill:none;stroke:var(--lg1)}
.dg-logo .g2{fill:var(--lg2)}.dg-logo .g2o{fill:none;stroke:var(--lg2)}
.dg-logo .g3{fill:var(--lg3)}.dg-logo .g3o{fill:none;stroke:var(--lg3)}
.dg-logo .hole{fill:var(--lgh)}
"""
