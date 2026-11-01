const { app, BrowserWindow, ipcMain, dialog, shell } = require("electron");
const path = require("node:path");
const fs = require("node:fs/promises");
const os = require("node:os");
let mainWindow;
function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1180, height: 900, minWidth: 900, minHeight: 700,
    backgroundColor: "#070a12", title: "Anonixex BETA", autoHideMenuBar: true,
    webPreferences: { preload: path.join(__dirname, "preload.js"), contextIsolation: true, nodeIntegration: false, sandbox: true }
  });
  mainWindow.loadFile(path.join(__dirname, "index.html"));
}
app.whenReady().then(() => { createWindow(); app.on("activate", () => { if (!BrowserWindow.getAllWindows().length) createWindow(); }); });
app.on("window-all-closed", () => { if (process.platform !== "darwin") app.quit(); });
async function walkShortcuts(dir, depth = 0) {
  if (depth > 4) return [];
  let entries; try { entries = await fs.readdir(dir, { withFileTypes: true }); } catch { return []; }
  const out = [];
  for (const e of entries) {
    const full = path.join(dir, e.name);
    if (e.isDirectory()) out.push(...await walkShortcuts(full, depth + 1));
    else if (e.isFile() && (e.name.toLowerCase().endsWith(".lnk") || e.name.toLowerCase().endsWith(".url"))) {
      out.push({ name: path.basename(e.name, path.extname(e.name)), path: full, kind: e.name.toLowerCase().endsWith(".url") ? "link" : "app" });
    }
  }
  return out;
}
ipcMain.handle("anonix:get-apps", async () => {
  if (process.platform !== "win32") return [];
  const dirs = [
    path.join(process.env.APPDATA || "", "Microsoft", "Windows", "Start Menu", "Programs"),
    path.join(process.env.ProgramData || "C:\\ProgramData", "Microsoft", "Windows", "Start Menu", "Programs"),
    path.join(os.homedir(), "Desktop")
  ];
  const all = (await Promise.all(dirs.map(d => walkShortcuts(d)))).flat();
  const unique = new Map();
  for (const item of all) if (!unique.has(item.path.toLowerCase())) unique.set(item.path.toLowerCase(), item);
  return [...unique.values()].sort((a,b) => a.name.localeCompare(b.name));
});
ipcMain.handle("anonix:wallpaper", async () => {
  const result = await dialog.showOpenDialog(mainWindow, { title: "Choose Anonixex wallpaper", properties: ["openFile"], filters: [{ name: "Images", extensions: ["png","jpg","jpeg","webp","bmp"] }] });
  if (result.canceled || !result.filePaths[0]) return null;
  const file = result.filePaths[0], ext = path.extname(file).toLowerCase();
  const mime = ext === ".jpg" || ext === ".jpeg" ? "image/jpeg" : ext === ".webp" ? "image/webp" : ext === ".bmp" ? "image/bmp" : "image/png";
  return "data:" + mime + ";base64," + (await fs.readFile(file)).toString("base64");
});
ipcMain.handle("anonix:system-info", async () => ({
  platform: process.platform, release: os.release(), type: os.type(), arch: os.arch(), hostname: os.hostname(),
  cpu: os.cpus()[0]?.model || "Unknown CPU", cores: os.cpus().length,
  memory: Math.round(os.totalmem()/1024/1024/1024) + " GB", version: app.getVersion()
}));
ipcMain.handle("anonix:launch-app", async (_event, shortcutPath) => {
  if (typeof shortcutPath !== "string" || (!shortcutPath.toLowerCase().endsWith(".lnk") && !shortcutPath.toLowerCase().endsWith(".url"))) return "Invalid shortcut";
  const result = await shell.openPath(shortcutPath);
  return result || null;
});
ipcMain.handle("anonix:open-system", async (_event, target) => {
  const pages = { settings:"ms-settings:", display:"ms-settings:display", network:"ms-settings:network", bluetooth:"ms-settings:bluetooth", personalization:"ms-settings:personalization", sound:"ms-settings:sound", notifications:"ms-settings:notifications", battery:"ms-settings:powersleep", privacy:"ms-settings:privacy", storage:"ms-settings:storagesense", apps:"ms-settings:appsfeatures", updates:"ms-settings:windowsupdate" };
  if (!pages[target]) return false;
  await shell.openExternal(pages[target]); return true;
});
ipcMain.handle("anonix:open-link", async (_event, raw) => {
  if (typeof raw !== "string" || raw.length > 2048) return false;
  try { const u = new URL(raw); if (!["https:","http:"].includes(u.protocol)) return false; await shell.openExternal(u.toString()); return true; } catch { return false; }
});
