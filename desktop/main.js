const { app, BrowserWindow, ipcMain, dialog, shell } = require("electron");
const path = require("node:path");
const fs = require("node:fs/promises");
const os = require("node:os");

let mainWindow;

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1440, height: 920, minWidth: 900, minHeight: 650,
    backgroundColor: "#080d19",
    title: "Anonixex",
    autoHideMenuBar: true,
    webPreferences: {
      preload: path.join(__dirname, "preload.js"),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true
    }
  });
  mainWindow.loadFile(path.join(__dirname, "index.html"));
}
app.whenReady().then(() => {
  createWindow();
  app.on("activate", () => { if (BrowserWindow.getAllWindows().length === 0) createWindow(); });
});
app.on("window-all-closed", () => { if (process.platform !== "darwin") app.quit(); });

async function walkShortcuts(dir, depth = 0) {
  if (depth > 3) return [];
  let entries;
  try { entries = await fs.readdir(dir, { withFileTypes: true }); } catch { return []; }
  const found = [];
  for (const entry of entries) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) found.push(...await walkShortcuts(full, depth + 1));
    else if (entry.isFile() && entry.name.toLowerCase().endsWith(".lnk")) {
      found.push({ name: path.basename(entry.name, ".lnk"), path: full });
    }
  }
  return found;
}

ipcMain.handle("anonixex:get-apps", async () => {
  if (process.platform !== "win32") return [];
  const dirs = [
    path.join(process.env.APPDATA || "", "Microsoft", "Windows", "Start Menu", "Programs"),
    path.join(process.env.ProgramData || "C:\\ProgramData", "Microsoft", "Windows", "Start Menu", "Programs")
  ];
  const all = (await Promise.all(dirs.map(d => walkShortcuts(d)))).flat();
  const unique = new Map();
  for (const item of all) if (!unique.has(item.name.toLowerCase())) unique.set(item.name.toLowerCase(), item);
  return [...unique.values()].sort((a,b) => a.name.localeCompare(b.name));
});

ipcMain.handle("anonix:wallpaper", async () => {
  const result = await dialog.showOpenDialog(mainWindow, {
    title: "Choose Anonixex wallpaper",
    properties: ["openFile"],
    filters: [{ name: "Images", extensions: ["png", "jpg", "jpeg", "webp", "bmp"] }]
  });
  if (result.canceled || !result.filePaths[0]) return null;
  const file = result.filePaths[0];
  const ext = path.extname(file).toLowerCase();
  const mime = ext === ".jpg" || ext === ".jpeg" ? "image/jpeg" : ext === ".webp" ? "image/webp" : ext === ".bmp" ? "image/bmp" : "image/png";
  const data = await fs.readFile(file);
  return "data:" + mime + ";base64," + data.toString("base64");
});

ipcMain.handle("anonix:system-info", async () => ({
  platform: process.platform,
  release: os.release(),
  type: os.type(),
  arch: os.arch(),
  hostname: os.hostname(),
  cpu: os.cpus()[0]?.model || "Unknown CPU",
  cores: os.cpus().length,
  memory: Math.round(os.totalmem() / 1024 / 1024 / 1024) + " GB",
  version: app.getVersion()
}));

ipcMain.handle("anonix:launch-app", async (_event, shortcutPath) => {
  if (typeof shortcutPath !== "string" || !shortcutPath.toLowerCase().endsWith(".lnk")) return "Invalid shortcut";
  return await shell.openPath(shortcutPath);
});

ipcMain.handle("anonix:open-system", async (_event, target) => {
  const pages = {
    settings: "ms-settings:",
    display: "ms-settings:display",
    network: "ms-settings:network",
    bluetooth: "ms-settings:bluetooth",
    personalization: "ms-settings:personalization",
    sound: "ms-settings:sound",
    notifications: "ms-settings:notifications",
    battery: "ms-settings:powersleep",
    privacy: "ms-settings:privacy",
    storage: "ms-settings:storagesense",
    apps: "ms-settings:appsfeatures",
    updates: "ms-settings:windowsupdate"
  };
  const uri = pages[target];
  if (!uri) return false;
  await shell.openExternal(uri);
  return true;
});
