const {chromium}=require('playwright');
(async()=>{const b=await chromium.launch();const p=await b.newPage({viewport:{width:400,height:400}});
const icons={search:'search',edit:'edit',camera:'photo_camera',ghost:'visibility_off',add:'add',back:'arrow_back',more:'more_vert',send:'send',mic:'mic',video:'videocam',stop:'stop',lock:'lock'};
let html='<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Material+Symbols+Rounded:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200&display=block"><style>body{margin:0}.b{width:264px;height:264px;display:flex;align-items:center;justify-content:center;background:#fff}.b span{font-family:"Material Symbols Rounded";font-size:264px;line-height:1;font-variation-settings:"FILL" 1,"wght" 700,"opsz" 20;color:#000}</style>';
for(const k in icons)html+=`<div class="b" id="${k}"><span>${icons[k]}</span></div>`;
await p.setContent(html,{waitUntil:'networkidle'});await p.evaluate(()=>document.fonts.ready);await p.waitForTimeout(400);
for(const k in icons)await p.locator('#'+k).screenshot({path:'gl/'+k+'.png'});
await b.close()})();
