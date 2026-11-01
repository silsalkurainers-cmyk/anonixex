const { contextBridge, ipcRenderer } = require("electron");
contextBridge.exposeInMainWorld("anonixex", {
  getApps: () => ipcRenderer.invoke("anonix:get-apps"),
  chooseWallpaper: () => ipcRenderer.invoke("anonix:wallpaper"),
  getSystemInfo: () => ipcRenderer.invoke("anonix:system-info"),
  launchApp: p => ipcRenderer.invoke("anonix:launch-app", p),
  openSystem: p => ipcRenderer.invoke("anonix:open-system", p),
  openLink: u => ipcRenderer.invoke("anonix:open-link", u)
});
