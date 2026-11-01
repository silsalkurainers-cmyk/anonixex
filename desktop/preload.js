const { contextBridge, ipcRenderer } = require("electron");

contextBridge.exposeInMainWorld("anonixex", {
  getApps: () => ipcRenderer.invoke("anonix:get-apps"),
  chooseWallpaper: () => ipcRenderer.invoke("anonix:wallpaper"),
  getSystemInfo: () => ipcRenderer.invoke("anonix:system-info"),
  launchApp: (shortcutPath) => ipcRenderer.invoke("anonix:launch-app", shortcutPath),
  openSystem: (target) => ipcRenderer.invoke("anonix:open-system", target)
});
