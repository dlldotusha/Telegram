# Material 3 Expressive layer for DelushaGram: hero type, shape containers, bento, personas.

def persona_css(name, h):
    """Tonal palette of a Delusha user's chosen profile hue, per theme."""
    o = lambda L, C, hh=h: f"oklch({L} {C} {hh})"
    return (
        f".persona-{name}{{--p-c:{o(.92,.05)};--p-on:{o(.30,.07)};--p-acc:{o(.52,.11)};--p-onacc:{o(.99,.005)};--p-pat:{o(.80,.09)};--p-soft:{o(.96,.025)}}}\n"
        f"[data-theme=dark] .persona-{name}{{--p-c:{o(.33,.045)};--p-on:{o(.90,.04)};--p-acc:{o(.80,.09)};--p-onacc:{o(.27,.06)};--p-pat:{o(.44,.07)};--p-soft:{o(.24,.025)}}}\n"
        f"[data-theme=amoled] .persona-{name}{{--p-c:{o(.28,.04)};--p-on:{o(.90,.04)};--p-acc:{o(.80,.09)};--p-onacc:{o(.27,.06)};--p-pat:{o(.38,.06)};--p-soft:{o(.17,.02)}}}\n"
    )

PERSONAS = {'me': 295, 'anya': 190, 'lyokha': 140, 'dasha': 30}

CSS = r"""
/* ---------- Material 3 Expressive layer ---------- */
.dg-xl{margin:0;font:900 52px/52px var(--font-sans);letter-spacing:-1.6px;font-variation-settings:"wdth" 151,"opsz" 72}
.dg-xxl{margin:0;font:900 64px/62px var(--font-sans);letter-spacing:-2px;font-variation-settings:"wdth" 151,"opsz" 96}
.dg-count{display:inline-grid;place-items:center;width:44px;height:44px;background:var(--primary);color:var(--on-primary);font:800 17px/1 var(--font-sans);font-variant-numeric:tabular-nums}
.dg-xhead{display:flex;align-items:center;gap:10px;padding:6px 16px 4px 20px}
.dg-xhead .grow{flex:1}
/* persona: a Delusha user's own profile colours */
.persona{--p-c:var(--primary-container);--p-on:var(--on-primary-container);--p-acc:var(--primary);--p-onacc:var(--on-primary);--p-pat:var(--primary);--p-soft:var(--surface-container-low)}
.dg-av.persona,.persona .dg-av.pv{--av-bg:var(--p-c);--av-fg:var(--p-on)}
.dg-av.reg{border-radius:50%;clip-path:none}
/* pinned row */
.dg-pins{display:flex;gap:10px;padding:10px 16px 6px;overflow:hidden}
.dg-pin{flex:none;width:76px;display:flex;flex-direction:column;align-items:center;gap:6px;font:600 12px/16px var(--font-sans);color:var(--on-surface);text-align:center;position:relative}
.dg-pin .dg-av{width:68px;height:68px;font-size:24px}
.dg-pin .dg-badge{position:absolute;right:2px;top:0;box-shadow:0 0 0 3px var(--surface)}
/* connected segmented chips */
.dg-seg{display:flex;gap:2px;padding:8px 16px 10px;overflow:hidden}
.dg-seg button{flex:none;border:0;height:40px;padding:0 16px;border-radius:8px;background:var(--surface-container-high);color:var(--on-surface-variant);font:700 13.5px/1 var(--font-sans);display:flex;align-items:center;gap:6px;cursor:pointer;transition:border-radius 380ms var(--dg-spring)}
.dg-seg button:first-child{border-radius:20px 8px 8px 20px}
.dg-seg button[aria-pressed="true"]{border-radius:20px;background:var(--primary);color:var(--on-primary)}
.dg-seg .n{font:800 11px/18px var(--font-sans);min-width:18px;height:18px;padding:0 5px;border-radius:9px;background:var(--primary-container);color:var(--on-primary-container);text-align:center}
.dg-seg button[aria-pressed="true"] .n{background:var(--on-primary);color:var(--primary)}
/* grouped chat list */
.dg-cgroup{display:flex;flex-direction:column;gap:2px;margin:0 10px}
.dg-cgroup .dg-chat{background:var(--surface-container-low);border-radius:8px}
.dg-cgroup .dg-chat:first-child{border-radius:28px 28px 8px 8px}
.dg-cgroup .dg-chat:last-child{border-radius:8px 8px 28px 28px}
.dg-cgroup .dg-chat.unread .nm{font-weight:800}
.dg-cgroup .dg-chat.unread .pv{color:var(--on-surface)}
/* shaped FAB */
.dg-fabx{width:68px;height:68px;border:0;display:grid;place-items:center;background:var(--primary);color:var(--on-primary);cursor:pointer}
.dg-fabx .dg-ic{font-size:28px}
.dg-fabwrap{filter:drop-shadow(0 4px 10px rgba(0,0,0,.22));display:grid}
/* bubbles: bigger radii, persona tint */
.dg-b.in.persona{background:var(--p-c);color:var(--p-on)}
.dg-b.in.persona .quote{background:color-mix(in srgb,var(--p-acc) 16%,transparent)}
.dg-b.big{border-radius:28px!important;padding:4px}
/* profile hero */
.dg-hero{position:relative;height:250px;overflow:hidden;background:var(--p-c);border-radius:0 0 44px 44px}
.dg-hero.grad{background:radial-gradient(circle at 20% 20%,var(--p-pat),transparent 55%),radial-gradient(circle at 90% 80%,var(--p-acc),transparent 50%),var(--p-c)}
.dg-hero .pat{position:absolute;display:block;background:var(--p-pat);opacity:.55}
.dg-hero .top{position:absolute;left:0;right:0;top:36px;display:flex;justify-content:space-between;padding:4px 10px;z-index:2}
.dg-hero .top .dg-ib{background:var(--surface-glass);color:var(--on-surface);-webkit-backdrop-filter:blur(12px);backdrop-filter:blur(12px)}
.dg-ring{display:grid;place-items:center;background:var(--surface);margin:-92px auto 0;position:relative;z-index:3}
.dg-ring .dg-av{width:156px;height:156px;font-size:58px}
.dg-pname{margin:12px 0 0;font:900 34px/36px var(--font-sans);letter-spacing:-.8px;font-variation-settings:"wdth" 132,"opsz" 40;text-align:center;display:flex;align-items:center;justify-content:center;gap:8px}
.dg-pname .bdg{width:26px;height:26px;background:var(--p-acc);color:var(--p-onacc);display:grid;place-items:center}
.dg-pname .bdg .dg-ic{font-size:16px}
.dg-pstat{text-align:center;font:600 14px/20px var(--font-sans);color:var(--p-acc)}
.persona .dg-bgroup .dg-btn.filled{background:var(--p-acc);color:var(--p-onacc)}
.persona .dg-bgroup .dg-btn.tonal{background:var(--p-c);color:var(--p-on)}
/* bento */
.dg-bento{display:grid;grid-template-columns:1fr 1fr;gap:6px}
.dg-card{background:var(--surface-container);border-radius:24px;padding:14px 16px;min-width:0;display:flex;flex-direction:column;gap:2px}
.dg-card.span2{grid-column:span 2}
.dg-card b{font:700 16px/22px var(--font-sans);overflow-wrap:anywhere}
.dg-card span{font:500 12.5px/16px var(--font-sans);color:var(--on-surface-variant)}
.dg-card.acc{background:var(--p-soft)}
.dg-card .row{display:flex;align-items:center;gap:8px}
.dg-mbento{display:grid;grid-template-columns:repeat(4,1fr);grid-auto-rows:84px;gap:4px}
.dg-mbento i{display:block;border-radius:10px}
.dg-mbento i.big{grid-column:span 2;grid-row:span 2;border-radius:32px 10px 10px 10px}
/* big toggle tiles */
.dg-ttiles{display:grid;grid-template-columns:1fr 1fr;gap:6px}
.dg-ttile{height:140px;border-radius:32px;padding:16px;display:flex;flex-direction:column;justify-content:space-between;background:var(--surface-container);color:var(--on-surface)}
.dg-ttile .ti{width:52px;height:52px;display:grid;place-items:center;background:var(--surface-container-highest);color:var(--on-surface-variant)}
.dg-ttile .ti .dg-ic{font-size:28px}
.dg-ttile .bt{display:flex;align-items:flex-end;justify-content:space-between;gap:8px}
.dg-ttile b{font:800 17px/20px var(--font-sans);font-variation-settings:"wdth" 115}
.dg-ttile.on{background:var(--tertiary-container);color:var(--on-tertiary-container)}
.dg-ttile.on .ti{background:var(--tertiary);color:var(--on-tertiary)}
.dg-ttile.on2{background:var(--secondary-container);color:var(--on-secondary-container)}
.dg-ttile.on2 .ti{background:var(--primary);color:var(--on-primary)}
/* profile editor pickers */
.dg-pick{display:flex;gap:10px;overflow:hidden;padding:4px 2px}
.dg-pick i{flex:none;display:block;width:52px;height:52px;background:var(--outline);opacity:.6}
.dg-pick i.on{opacity:1}
.dg-pick i.on{background:var(--primary);outline:3px solid var(--primary);outline-offset:3px}
.dg-pick.col i{width:44px;height:44px}
.dg-bgs{display:grid;grid-template-columns:repeat(3,1fr);gap:6px}
.dg-bgs div{height:76px;border-radius:18px;position:relative;overflow:hidden;background:var(--p-c);display:flex;align-items:flex-end;padding:8px 10px;font:700 12px/16px var(--font-sans);color:var(--p-on)}
.dg-bgs div.on{outline:3px solid var(--primary);outline-offset:2px}
.dg-bgs div i{position:absolute;display:block;background:var(--p-pat);opacity:.6}
.dg-names{display:grid;grid-template-columns:repeat(3,1fr);gap:6px}
.dg-names div{height:64px;border-radius:18px;background:var(--surface-container-high);display:grid;place-items:center;font:900 24px/1 var(--font-sans);color:var(--on-surface)}
.dg-names div.on{background:var(--primary-container);color:var(--on-primary-container);border-radius:32px}
.dg-sec{display:flex;flex-direction:column;gap:10px;padding:14px 16px;background:var(--surface-container-low);border-radius:8px}
.dg-secs{display:flex;flex-direction:column;gap:2px;margin:0 12px}
.dg-secs .dg-sec:first-child{border-radius:28px 28px 8px 8px}
.dg-secs .dg-sec:last-child{border-radius:8px 8px 28px 28px}
.dg-sec .hd{font:800 13px/16px var(--font-sans);letter-spacing:.3px;color:var(--primary);text-transform:none}
.dg-cluster{position:absolute;pointer-events:none}
.dgb{display:inline-block;vertical-align:-3px;flex:none;margin-left:4px}
.dgb .bbg{fill:var(--primary-container)}
.dgb .bp1{fill:var(--primary)}.dgb .bp1o{fill:none;stroke:var(--primary)}
.dgb .bp2{fill:var(--tertiary)}.dgb .bp2o{fill:none;stroke:var(--tertiary)}
.dgb .hole{fill:var(--primary-container)}
.dgb-user .bbg{fill:var(--surface-container-highest)}
.dgb-user .bo{fill:none;stroke:var(--on-surface-variant)}
.dg-logo .lp1{fill:var(--primary)}.dg-logo .lp1o{fill:none;stroke:var(--primary)}
.dg-logo .lp2{fill:color-mix(in srgb,var(--primary) 55%,var(--surface))}.dg-logo .lp2o{fill:none;stroke:color-mix(in srgb,var(--primary) 55%,var(--surface))}
.dg-logo .lp3{fill:var(--tertiary)}.dg-logo .lp3o{fill:none;stroke:var(--tertiary)}
.dg-logo .hole{fill:var(--surface)}
.dg-anim{display:grid;grid-template-columns:repeat(4,1fr);gap:6px}
.dg-anim div{height:76px;border-radius:18px;background:var(--surface-container-high);display:grid;place-items:center;position:relative;color:var(--on-surface-variant)}
.dg-anim div i{display:block;width:34px;height:34px;background:var(--outline)}
.dg-anim div .dg-ic{position:absolute;right:8px;bottom:8px;font-size:18px}
.dg-anim div.on{background:var(--primary-container);border-radius:32px;color:var(--on-primary-container)}
.dg-anim div.on i{background:var(--primary)}
.dgon{position:absolute;right:-3px;bottom:-3px;width:20px;height:20px;border-radius:7px;background:var(--online);box-shadow:0 0 0 3px var(--surface);-webkit-mask:none}
.dgon::after{content:"";position:absolute;inset:4px 5px;background:var(--surface);-webkit-mask:url("data:image/svg+xml,%3Csvg%20xmlns%3D%22http%3A//www.w3.org/2000/svg%22%20viewBox%3D%220%200%209.6%2012%22%3E%3Cg%20fill%3D%22%23000%22%3E%3Crect%20x%3D%220%22%20y%3D%220%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%222.4%22%20y%3D%220%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%224.8%22%20y%3D%220%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%220%22%20y%3D%222.4%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%227.2%22%20y%3D%222.4%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%220%22%20y%3D%224.8%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%227.2%22%20y%3D%224.8%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%220%22%20y%3D%227.2%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%227.2%22%20y%3D%227.2%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%220%22%20y%3D%229.6%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%222.4%22%20y%3D%229.6%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%224.8%22%20y%3D%229.6%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3C/g%3E%3C/svg%3E") center/contain no-repeat;mask:url("data:image/svg+xml,%3Csvg%20xmlns%3D%22http%3A//www.w3.org/2000/svg%22%20viewBox%3D%220%200%209.6%2012%22%3E%3Cg%20fill%3D%22%23000%22%3E%3Crect%20x%3D%220%22%20y%3D%220%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%222.4%22%20y%3D%220%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%224.8%22%20y%3D%220%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%220%22%20y%3D%222.4%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%227.2%22%20y%3D%222.4%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%220%22%20y%3D%224.8%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%227.2%22%20y%3D%224.8%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%220%22%20y%3D%227.2%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%227.2%22%20y%3D%227.2%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%220%22%20y%3D%229.6%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%222.4%22%20y%3D%229.6%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3Crect%20x%3D%224.8%22%20y%3D%229.6%22%20width%3D%222%22%20height%3D%222%22%20rx%3D%22.4%22/%3E%3C/g%3E%3C/svg%3E") center/contain no-repeat}
.dg-card .dg-ic{font:normal 22px/1 "Material Symbols Rounded";color:inherit;font-variation-settings:"FILL" 1,"wght" 500,"GRAD" 0,"opsz" 24}
.dg-appbar h1.dg-xl,h1.dg-xl{font:900 52px/52px var(--font-sans);letter-spacing:-1.6px;font-variation-settings:"wdth" 151,"opsz" 72}
.dg-appbar h1.dg-xxl,h1.dg-xxl{font:900 64px/62px var(--font-sans);letter-spacing:-2px;font-variation-settings:"wdth" 151,"opsz" 96}
.dg-card span:not(.dg-ic){display:block}
.dg-ring.sm .dg-av{width:80px;height:80px;font-size:32px}
.dg-cluster i{position:absolute;display:block}
"""

def all_css():
    return CSS + ''.join(persona_css(k, v) for k, v in PERSONAS.items())
