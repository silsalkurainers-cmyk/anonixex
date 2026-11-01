const $=s=>document.querySelector(s), colors=["#77e0ff","#b491ff","#78f5c2","#ffab87","#ffd577","#f485be","#e2ebff","#a0beff"];
const base={setup:false,accent:0,glass:true,labels:true,clock:true,search:true,columns:4,iconSize:48,textScale:100,wallpaper:"",favorites:[],hidden:[]};
let s={...base,...JSON.parse(localStorage.getItem("anonixex-settings")||"{}")},page=s.setup?"home":"setup",step=0,apps=[],query="";
const save=()=>localStorage.setItem("anonixex-settings",JSON.stringify(s)),esc=x=>String(x).replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));
const btn=(t,a,k="soft")=>'<button class="button '+k+'" data-action="'+a+'">'+t+'</button>';
const card=x=>'<section class="glass-card">'+x+'</section>';
const palette=()=>'<div class="palette">'+colors.map((c,i)=>'<button data-accent="'+i+'" class="'+(s.accent===i?"selected":"")+'" style="--swatch:'+c+'"></button>').join("")+'</div>';
const nav=x=>'<nav class="bottom-nav">'+["home","apps","studio"].map(p=>'<button data-page="'+p+'" class="'+(x===p?"active":"")+'">'+(p==="home"?"⌂":p==="apps"?"▦":"◈")+'<span>'+p[0].toUpperCase()+p.slice(1)+'</span></button>').join("")+'</nav>';
const tile=a=>'<button class="app-tile" data-launch="'+esc(a.path)+'"><span class="app-icon">'+esc(a.name[0].toUpperCase())+'</span>'+(s.labels?'<span class="app-name">'+esc(a.name)+'</span>':"")+'</button>';
const head=(t,d)=>'<header class="topbar"><div><div class="brand">A N O N I X E X</div><h1>'+t+'</h1><p>'+d+'</p></div><div class="top-actions">'+btn("◉","control")+btn("⚙","studio")+'</div></header>';
function render(){
 document.documentElement.style.setProperty("--accent",colors[s.accent]);document.documentElement.style.setProperty("--icon-size",s.iconSize+"px");document.documentElement.style.setProperty("--columns",s.columns);document.documentElement.style.setProperty("--text-scale",s.textScale/100);document.body.classList.toggle("flat",!s.glass);
 let bg=s.wallpaper?'background-image:linear-gradient(120deg,#050912c9,#050912df),url("'+s.wallpaper+'")':"";
 let content=page==="setup"?setup():page==="home"?home():page==="apps"?library():page==="studio"?studio():page==="control"?control():kernel();
 $("#app").innerHTML='<main class="shell" style="'+bg+'">'+content+'</main>';
 if(page==="home")clock();
}
function setup(){
 let parts=[
 card('<div class="eyebrow">01 / THE EXPERIENCE</div><h2>Liquid glass.<br>Deep space.<br>Your rules.</h2><p>A home screen, app library, quick controls and deep customization. No ads. No account. No tracking.</p>'),
 '<div class="eyebrow">02 / PICK YOUR COLOR</div><h2>Choose your atmosphere.</h2>'+palette(),
 card('<div class="eyebrow">03 / YOUR WALLPAPER</div><h2>Make it yours.</h2><p>Choose an image from your PC for your Anonixex backdrop.</p>'+btn("Choose a picture","wallpaper","primary")+'<p class="hint">'+(s.wallpaper?"Custom wallpaper selected":"Or keep the default atmosphere.")+'</p>'),
 card('<div class="eyebrow">04 / READY WHEN YOU ARE</div><h2>Your desktop.<br>Your universe.</h2><p>This is a desktop companion, not a replacement for Windows or its kernel.</p>')
 ];
 return '<div class="setup-wrap"><div class="brand">A N O N I X E X <span>DESKTOP</span></div><div class="setup-progress">'+[0,1,2,3].map(i=>'<i class="'+(i<=step?"done":"")+'"></i>').join("")+'</div>'+parts[step]+'<div class="setup-actions">'+(step?btn("Back","back"):"")+btn(step===3?"Enter Anonixex":"Continue","next","primary")+'</div><div class="signature">made by hacer62828</div></div>';
}
function home(){
 let fav=s.favorites.map(p=>apps.find(a=>a.path===p)).filter(Boolean),recent=apps.filter(a=>!s.hidden.includes(a.path)&&a.name.toLowerCase().includes(query.toLowerCase())).slice(0,8);
 return '<div class="content">'+head("Good to see you.","Your desktop, reimagined around you.")+(s.clock?card('<div class="clock" id="clock">--:--</div><div class="date" id="date"></div>'):"")+'<div class="spotlight"><div class="eyebrow">PERSONAL SPACE / 001</div><h2>Everything,<br>in its own orbit.</h2><p>A little more you. A little less ordinary.</p><div class="orb"></div></div>'+(s.search?searchBox("home-search"):"")+'<div class="section-head"><span>PINNED</span><small>Quick launch</small></div><div class="favorite-row">'+(fav.map(tile).join("")||'<div class="empty">Pin your favourite apps from the App Library.</div>')+'</div><div class="section-head"><span>APP LIBRARY</span><button data-page="apps">All apps ↗</button></div><div class="app-grid">'+(recent.map(tile).join("")||'<div class="empty">No Start Menu shortcuts found yet.</div>')+'</div><div class="signature">made by hacer62828</div></div>'+nav("home");
}
function searchBox(id){return '<div class="searchbox"><span>⌕</span><input id="'+id+'" placeholder="Search apps" value="'+esc(query)+'"></div>';}
function library(){
 let list=apps.filter(a=>!s.hidden.includes(a.path)&&a.name.toLowerCase().includes(query.toLowerCase()));
 return '<div class="content">'+head("App library.","Windows Start Menu shortcuts.")+searchBox("app-search")+'<div class="app-grid library-grid">'+list.map(a=>tile(a)+'<button class="tiny-action" data-pin="'+esc(a.path)+'">'+(s.favorites.includes(a.path)?"Unpin":"Pin")+'</button><button class="tiny-action" data-hide="'+esc(a.path)+'">Hide</button>').join("")+'</div>'+card('<div class="eyebrow">APP DISCOVERY</div><p>Anonixex reads shortcuts from your Windows Start Menu. It does not scan private app data.</p>')+'</div>'+nav("apps");
}
function toggle(label,key){return '<div class="setting-row"><span>'+label+'</span><button class="toggle '+(s[key]?"on":"")+'" data-toggle="'+key+'">'+(s[key]?"ON":"OFF")+'</button></div>';}
function slider(label,key,min,max){return '<label class="slider-row"><span>'+label+' <b>'+s[key]+(key==="textScale"?"%":"")+'</b></span><input type="range" min="'+min+'" max="'+max+'" value="'+s[key]+'" data-slider="'+key+'"></label>';}
function sysButtons(){return [["Display & brightness","display"],["Network & Wi-Fi","network"],["Bluetooth","bluetooth"],["Sound","sound"],["Notifications","notifications"],["Battery & power","battery"],["Privacy & security","privacy"],["Storage","storage"],["Installed apps","apps"],["Windows Update","updates"],["Windows Settings","settings"]].map(x=>btn(x[0],"sys:"+x[1])).join("");}
function studio(){
 return '<div class="content">'+head("Make it yours.","Your look. Your layout. Your rules.")+card('<div class="eyebrow">01 / VISUAL IDENTITY</div><h3>Accent palette</h3>'+palette()+toggle("Liquid glass panels","glass")+toggle("App labels","labels")+toggle("Home clock","clock")+toggle("Home search","search"))+card('<div class="eyebrow">02 / LAYOUT & TYPOGRAPHY</div>'+slider("App columns","columns",3,8)+slider("Icon size","iconSize",32,76)+slider("Text scale","textScale",85,125))+card('<div class="eyebrow">03 / YOUR BACKDROP</div><h3>Wallpaper studio</h3>'+btn("Choose picture from PC","wallpaper","primary")+btn("Remove custom wallpaper","remove"))+card('<div class="eyebrow">04 / APP LIBRARY</div><h3>Organize your space</h3><p>'+s.hidden.length+' hidden shortcuts · '+s.favorites.length+' pinned apps</p>'+btn("Restore hidden apps","unhide")+btn("Reset appearance","reset"))+card('<div class="eyebrow">05 / SYSTEM</div><h3>Windows integration</h3>'+sysButtons()+'<div class="button-row">'+btn("Kernel Lab","kernel")+'</div>')+'<div class="signature">made by hacer62828</div></div>'+nav("studio");
}
function control(){return '<div class="content">'+head("Control center.","Quick access to Windows settings.")+card('<div class="eyebrow">CONNECTIVITY</div>'+btn("Wi-Fi & networks","sys:network")+btn("Bluetooth & devices","sys:bluetooth"))+card('<div class="eyebrow">DEVICE</div>'+btn("Display & brightness","sys:display")+btn("Sound","sys:sound")+btn("Battery & power","sys:battery")+btn("Notifications","sys:notifications")+btn("Privacy & security","sys:privacy"))+btn("Back to home","home","primary")+'</div>'+nav("home");}
function kernel(){return '<div class="content">'+head("Kernel Lab.","A live readout of this PC.")+card('<div class="eyebrow">RUNNING DEVICE</div><h3 id="sys-title">Reading system information…</h3><p id="sys-info"></p>')+card('<div class="eyebrow">THE ANONIXEX KERNEL PROJECT</div><p>This app reports the kernel currently running on your PC. The original Anonixex kernel is a separate experimental project. This desktop app does not install or modify your operating system kernel.</p>')+btn("Back to Studio","studio","primary")+'</div>'+nav("studio");}
function clock(){let d=new Date();if($("#clock"))$("#clock").textContent=d.toLocaleTimeString([],{hour:"2-digit",minute:"2-digit"});if($("#date"))$("#date").textContent=d.toLocaleDateString([],{weekday:"long",day:"numeric",month:"long"});}
async function wallpaper(){let data=await window.anonixex.chooseWallpaper();if(data){s.wallpaper=data;save();render();}}
document.addEventListener("click",async e=>{
 let b=e.target.closest("button");if(!b)return;
 if(b.dataset.page){page=b.dataset.page;render();return;}
 if(b.dataset.action){let a=b.dataset.action;
 if(a==="back"){step=Math.max(0,step-1);render();}else if(a==="next"){if(step<3){step++;render();}else{s.setup=true;save();page="home";render();}}
 else if(a==="wallpaper")wallpaper();else if(a==="control"){page="control";render();}else if(a==="studio"){page="studio";render();}else if(a==="home"){page="home";render();}
 else if(a==="remove"){s.wallpaper="";save();render();}else if(a==="unhide"){s.hidden=[];save();render();}else if(a==="reset"){s={...base,setup:true};save();page="home";render();}
 else if(a==="kernel"){page="kernel";render();let i=await window.anonixex.getSystemInfo();$("#sys-title").textContent=i.type+" "+i.release;$("#sys-info").innerHTML="Architecture: "+esc(i.arch)+"<br>CPU: "+esc(i.cpu)+"<br>CPU cores: "+i.cores+"<br>Memory: "+i.memory+"<br>Device: "+esc(i.hostname);}
 else if(a.startsWith("sys:"))window.anonixex.openSystem(a.slice(4));
 }
 if(b.dataset.accent!==undefined){s.accent=+b.dataset.accent;save();render();}
 if(b.dataset.toggle){s[b.dataset.toggle]=!s[b.dataset.toggle];save();render();}
 if(b.dataset.pin){let p=b.dataset.pin;s.favorites=s.favorites.includes(p)?s.favorites.filter(x=>x!==p):[...s.favorites,p];save();render();}
 if(b.dataset.hide){s.hidden.push(b.dataset.hide);s.favorites=s.favorites.filter(x=>x!==b.dataset.hide);save();render();}
 if(b.dataset.launch){let err=await window.anonixex.launchApp(b.dataset.launch);if(err)alert("Windows could not open this shortcut: "+err);}
});
document.addEventListener("input",e=>{
 if(e.target.id==="home-search"||e.target.id==="app-search"){query=e.target.value;let id=e.target.id,pos=e.target.selectionStart;render();let n=$("#"+id);if(n){n.focus();n.setSelectionRange(pos,pos);}}
 if(e.target.dataset.slider){let k=e.target.dataset.slider;s[k]=+e.target.value;save();let b=e.target.parentElement.querySelector("b");if(b)b.textContent=s[k]+(k==="textScale"?"%":"");}
});
(async()=>{try{apps=await window.anonixex.getApps();}catch(e){apps=[];}render();setInterval(()=>{if(page==="home")clock();},1000);})();
