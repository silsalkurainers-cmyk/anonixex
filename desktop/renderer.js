const $=s=>document.querySelector(s);
const accents=["#91e8ff","#b8a0ff","#91f0c5","#ffb38d","#ffdb85","#f39acb","#e7efff","#8faeff"];
const defaults={setup:false,accent:0,glass:92,blur:28,opacity:78,radius:28,columns:4,iconSize:42,textScale:100,labels:true,clock:true,search:true,wallpaper:"",favorite:"",islandUrl:"",islandOpen:false};
let state={...defaults,...JSON.parse(localStorage.getItem("anonixex-settings")||"{}")};
let page=state.setup?"home":"setup",step=0,apps=[],query="",terminalHistory=["Anonixex shell 0.1 · isolated app command environment","Type 'help' to see supported commands."];
const save=()=>localStorage.setItem("anonixex-settings",JSON.stringify(state));
const esc=s=>String(s??"").replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c]));
const button=(label,action,kind="glass")=>'<button class="btn '+kind+'" data-action="'+action+'">'+label+'</button>';
const palette=()=>'<div class="palette">'+accents.map((c,i)=>'<button title="Accent '+(i+1)+'" data-accent="'+i+'" style="--swatch:'+c+'" class="'+(state.accent===i?"selected":"")+'"></button>').join("")+'</div>';
const nav=()=>'<nav class="nav"><button data-page="home" class="'+(page==="home"?"active":"")+'">⌂</button><button data-page="library" class="'+(page==="library"?"active":"")+'">▦</button><button data-page="studio" class="'+(page==="studio"?"active":"")+'">◈</button><button data-page="terminal" class="'+(page==="terminal"?"active":"")+'">›_</button></nav>';
const tile=a=>'<button class="tile" data-launch="'+esc(a.path)+'"><span class="appicon">'+esc(a.name.slice(0,1).toUpperCase())+'</span>'+(state.labels?'<span class="appname">'+esc(a.name)+'</span>':"")+'</button>';
function shell(content){return '<main class="phone" style="--accent:'+accents[state.accent]+';--glass:'+state.opacity+'%;--blur:'+state.blur+'px;--radius:'+state.radius+'px;--columns:'+state.columns+';--icon:'+state.iconSize+'px;--scale:'+state.textScale/100+';--glass-strength:'+state.glass/100+'">'+content+'</main>';}
function island(){let fav=apps.find(a=>a.path===state.favorite);return '<div class="island-wrap"><button class="island '+(state.islandOpen?"expanded":"")+'" id="island" aria-label="Dynamic Island"><i></i><i></i><span>'+(state.islandOpen?"ANONIXEX":"")+'</span></button>'+(state.islandOpen?'<div class="island-panel"><div class="island-title">QUICK ACCESS</div>'+(fav?'<button class="quick" data-launch="'+esc(fav.path)+'">◉ '+esc(fav.name)+'</button>':'<p>No app assigned yet. Set one in Studio.</p>')+(state.islandUrl?'<button class="quick" data-link="'+esc(state.islandUrl)+'">↗ Open saved link</button>':"")+'</div>':"")+'</div>';}
function clock(){return '<div class="clock-block"><div class="clock" id="clock">--:--</div><div class="date" id="date"></div></div>';}
function search(){return '<div class="search"><span>⌕</span><input id="search" placeholder="Search apps" value="'+esc(query)+'"></div>';}
function home(){
 const fav=apps.find(a=>a.path===state.favorite),visible=apps.filter(a=>a.name.toLowerCase().includes(query.toLowerCase())).slice(0,8);
 return island()+'<div class="screen home">'+(state.clock?clock():"")+'<div class="welcome"><span class="eyebrow">ANONIXEX</span><h1>Your space.</h1></div>'+(state.search?search():"")+(fav?'<div class="section"><div class="section-label">FAVOURITE</div><div class="app-grid">'+tile(fav)+'</div></div>':"")+'<div class="section"><div class="section-label">RECENT APPS</div><div class="app-grid">'+visible.map(tile).join("")+'</div></div><div class="swipe-hint">SWIPE UP FOR APP LIBRARY</div></div>'+nav();
}
function library(){
 const list=apps.filter(a=>a.name.toLowerCase().includes(query.toLowerCase()));
 return island()+'<div class="screen library"><div class="eyebrow">YOUR PHONE · YOUR APPS</div><h1>App library</h1>'+search()+'<div class="app-grid">'+list.map(tile).join("")+'</div><p class="muted">'+list.length+' shortcuts found</p></div>'+nav();
}
function range(label,key,min,max,unit=""){return '<label class="range"><span>'+label+' <b id="v-'+key+'">'+state[key]+unit+'</b></span><input type="range" min="'+min+'" max="'+max+'" value="'+state[key]+'" data-range="'+key+'"></label>';}
function toggle(label,key){return '<div class="setting"><span>'+label+'</span><button data-toggle="'+key+'" class="switch '+(state[key]?"on":"")+'">'+(state[key]?"ON":"OFF")+'</button></div>';}
function studio(){
 return island()+'<div class="screen studio"><div class="eyebrow">PERSONALIZATION</div><h1>Studio</h1><section class="glass-card"><h2>Appearance</h2><div class="label">Accent colour</div>'+palette()+range("Glass intensity","glass",0,100,"%")+range("Glass blur","blur",0,42,"px")+range("Panel transparency","opacity",25,96,"%")+range("Corner roundness","radius",14,42,"px")+'</section><section class="glass-card"><h2>Home screen</h2>'+toggle("App labels","labels")+toggle("Clock","clock")+toggle("Search","search")+range("Grid columns","columns",3,6)+range("Icon size","iconSize",30,62,"px")+range("Text scale","textScale",85,125,"%")+'</section><section class="glass-card"><h2>Dynamic Island</h2><p>Double-tap the island to reveal quick actions.</p><label class="field">Quick app<select id="favorite"><option value="">None</option>'+apps.map(a=>'<option value="'+esc(a.path)+'" '+(state.favorite===a.path?"selected":"")+'>'+esc(a.name)+'</option>').join("")+'</select></label><label class="field">Quick link<input id="island-url" placeholder="https://example.com" value="'+esc(state.islandUrl)+'"></label>'+button("Save quick actions","save-island","primary")+'</section><section class="glass-card"><h2>Wallpaper</h2>'+button("Choose from PC","wallpaper","primary")+button("Remove wallpaper","remove-wallpaper")+'</section><section class="glass-card"><h2>Windows beta</h2><p>This is a beta companion running on Windows, not a replacement OS or kernel.</p>'+["display","network","bluetooth","sound","notifications","battery","privacy","settings"].map(k=>button(k[0].toUpperCase()+k.slice(1),"sys:"+k)).join("")+'</section><div class="signature">made by hacer62828</div></div>'+nav();
}
function terminal(){return island()+'<div class="screen terminal"><div class="eyebrow">ANONIXEX CORE · BETA</div><h1>Terminal</h1><div class="terminal-window" id="term-out">'+terminalHistory.map(x=>'<div>'+esc(x)+'</div>').join("")+'</div><form id="term-form"><span>anonixex $</span><input id="term-input" autocomplete="off" spellcheck="false" placeholder="type help"></form><p class="muted">Arch-inspired commands run inside Anonixex only. They do not execute arbitrary Windows or Android shell commands.</p></div>'+nav();}
function setup(){const pages=[
 '<div class="eyebrow">01 · ANONIXEX</div><h1>Less noise.<br>More you.</h1><p>A phone-first home experience with glass surfaces, gestures and your own quick actions.</p>',
 '<div class="eyebrow">02 · APPEARANCE</div><h1>Set the tone.</h1>'+palette(),
 '<div class="eyebrow">03 · WALLPAPER</div><h1>Your backdrop.</h1>'+button("Choose wallpaper","wallpaper","primary"),
 '<div class="eyebrow">04 · QUICK ACCESS</div><h1>Make the island yours.</h1><p>You can assign an app and a saved web link later in Studio.</p>'
 ];return '<div class="setup">'+pages[step]+'<div class="setup-progress">'+[0,1,2,3].map(i=>'<i class="'+(i<=step?"done":"")+'"></i>').join("")+'</div><div class="setup-actions">'+(step?button("Back","back"):"")+button(step===3?"Start Anonixex":"Continue","next","primary")+'</div><div class="signature">made by hacer62828</div></div>';}
function render(){document.body.classList.toggle("no-glass",state.glass===0);let c=page==="setup"?setup():page==="home"?home():page==="library"?library():page==="studio"?studio():terminal();$("#app").innerHTML=shell(c);updateClock();}
function updateClock(){const d=new Date();if($("#clock"))$("#clock").textContent=d.toLocaleTimeString([],{hour:"2-digit",minute:"2-digit"});if($("#date"))$("#date").textContent=d.toLocaleDateString([],{weekday:"long",day:"numeric",month:"long"});}
async function chooseWallpaper(){const data=await window.anonixex.chooseWallpaper();if(data){state.wallpaper=data;save();document.querySelector(".phone").style.backgroundImage='linear-gradient(#060a14aa,#060a14bb),url("'+data+'")';}}
function runCommand(raw){
 const [cmd,...args]=raw.trim().split(/\s+/),arg=args.join(" ");
 const out=x=>terminalHistory.push(String(x));
 if(!cmd)return;
 switch(cmd){
 case "help":out("help clear neofetch uname date uptime apps open <name> theme [name] wallpaper settings about ls pwd whoami free lscpu pkg list pkg search <term>");break;
 case "clear":terminalHistory=[];break;
 case "neofetch":out("  /\u005c_/\\\\   ANONIXEX BETA");out(" ( o.o )  "+(navigator.userAgent.includes("Windows")?"Windows desktop":"Android runtime"));out("  > ^ <   Liquid UI · local profile");break;
 case "uname":out("Anonixex-runtime 0.1 · userspace shell");break;
 case "date":out(new Date().toString());break;
 case "uptime":out("Session uptime: "+Math.floor(performance.now()/1000)+" seconds");break;
 case "apps":out(apps.map(a=>a.name).join(" · ")||"No shortcuts discovered");break;
 case "open":{const a=apps.find(x=>x.name.toLowerCase()===arg.toLowerCase()||x.name.toLowerCase().includes(arg.toLowerCase()));if(a){window.anonixex.launchApp(a.path);out("Launching "+a.name);}else out("App not found: "+arg);break;}
 case "theme":if(arg==="list")out("cyan violet mint peach amber pink ice blue");else{const n=["cyan","violet","mint","peach","amber","pink","ice","blue"].indexOf(arg);if(n>=0){state.accent=n;save();out("Accent set to "+arg);}else out("Usage: theme list | theme cyan");}break;
 case "wallpaper":out(state.wallpaper?"A custom wallpaper is active. Use Studio to change it.":"No custom wallpaper selected.");break;
 case "settings":page="studio";render();return;
 case "about":out("Anonixex phone-first launcher UI · beta companion. Not a kernel or standalone OS.");break;
 case "ls":out("home/  apps/  settings/  themes/  wallpaper/");break;
 case "pwd":out("/anonixex");break;
 case "whoami":out("user");break;
 case "free":window.anonixex.getSystemInfo().then(i=>{terminalHistory.push("Memory: "+i.memory);if(page==="terminal")render();});out("Reading host memory…");break;
 case "lscpu":window.anonixex.getSystemInfo().then(i=>{terminalHistory.push(i.cpu+" · "+i.cores+" cores · "+i.arch);if(page==="terminal")render();});out("Reading host CPU…");break;
 case "pkg":out(arg==="list"?"Installed Anonixex components: shell, launcher-ui, glass-theme, island, app-library":"Package manager is a local preview; no system packages are installed.");break;
 default:out("Command not found: "+cmd+". Type help.");
 }
 if(page==="terminal")render();
}
document.addEventListener("click",async e=>{
 const b=e.target.closest("button");if(!b)return;
 if(b.dataset.page){page=b.dataset.page;state.islandOpen=false;render();return;}
 if(b.dataset.accent!==undefined){state.accent=+b.dataset.accent;save();render();return;}
 if(b.dataset.toggle){state[b.dataset.toggle]=!state[b.dataset.toggle];save();render();return;}
 if(b.dataset.launch){const err=await window.anonixex.launchApp(b.dataset.launch);if(err)terminalHistory.push("Launch failed: "+err);return;}
 if(b.dataset.link){const ok=await window.anonixex.openLink(b.dataset.link);if(!ok)alert("Enter a valid http or https link in Studio.");return;}
 if(b.dataset.action){const a=b.dataset.action;
 if(a==="next"){if(step<3){step++;render();}else{state.setup=true;save();page="home";render();}}
 else if(a==="back"){step=Math.max(0,step-1);render();}
 else if(a==="wallpaper")await chooseWallpaper();
 else if(a==="remove-wallpaper"){state.wallpaper="";save();document.querySelector(".phone").style.backgroundImage="";render();}
 else if(a==="save-island"){state.favorite=$("#favorite").value;state.islandUrl=$("#island-url").value.trim();if(state.islandUrl&&!/^https?:\/\//i.test(state.islandUrl)){alert("Quick link must begin with http:// or https://");return;}save();render();}
 else if(a.startsWith("sys:"))window.anonixex.openSystem(a.slice(4));
 }
});
document.addEventListener("dblclick",e=>{if(e.target.closest("#island")){state.islandOpen=!state.islandOpen;render();}});
document.addEventListener("input",e=>{
 if(e.target.id==="search"){query=e.target.value;const pos=e.target.selectionStart;render();const n=$("#search");if(n){n.focus();n.setSelectionRange(pos,pos);}return;}
 if(e.target.dataset.range){state[e.target.dataset.range]=+e.target.value;save();const k=e.target.dataset.range;const el=$("#v-"+k);if(el)el.textContent=state[k]+(k==="blur"||k==="radius"||k==="iconSize"?"px":k==="opacity"||k==="glass"||k==="textScale"?"%":"");}
});
document.addEventListener("submit",e=>{if(e.target.id==="term-form"){e.preventDefault();const raw=$("#term-input").value;terminalHistory.push("anonixex $ "+raw);runCommand(raw);}});
let touchY=0;document.addEventListener("touchstart",e=>{touchY=e.touches[0].clientY;},{passive:true});document.addEventListener("touchend",e=>{const dy=touchY-e.changedTouches[0].clientY;if(dy>85&&page==="home"){page="library";query="";render();}},{passive:true});
(async()=>{try{apps=await window.anonixex.getApps();}catch{}render();setInterval(updateClock,1000);if(state.wallpaper){const p=document.querySelector(".phone");if(p)p.style.backgroundImage='linear-gradient(#060a14aa,#060a14bb),url("'+state.wallpaper+'")';}})();
