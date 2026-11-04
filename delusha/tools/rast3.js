const {chromium}=require('playwright');
(async()=>{const b=await chromium.launch();const p=await b.newPage({viewport:{width:400,height:400}});
const chars='ДАМЛКЮРВИ0123456789+';
let html='<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Roboto+Flex:opsz,wdth,wght@8..144,25..151,100..1000&display=swap"><style>body{margin:0}.b{width:240px;height:240px;display:flex;align-items:center;justify-content:center;background:#fff}.b span{font-family:"Roboto Flex";font-size:250px;font-weight:700;line-height:1;color:#000}</style>';
[...chars].forEach((c,i)=>html+=`<div class="b" id="c${i}"><span>${c}</span></div>`);
await p.setContent(html,{waitUntil:'networkidle'});await p.evaluate(()=>document.fonts.ready);await p.waitForTimeout(400);
for(let i=0;i<chars.length;i++)await p.locator('#c'+i).screenshot({path:'gl2/'+i+'.png'});
await b.close()})();
