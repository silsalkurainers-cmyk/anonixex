# Anonixex

Anonixex is a phone-first launcher and an early-stage original OS/kernel research project. The Android app is the main experience. The Windows companion is a beta preview for testing the shared phone-style interface.

## Releases

GitHub Releases contains:
- **Android APK:** the primary native Android launcher.
- **Windows x64 BETA:** experimental portable companion for Windows 10/11 64-bit PCs. It is for testing only and is not a standalone OS.

The release workflow keeps the APK and publishes the Windows executable as a prerelease/beta asset. Existing Android APK releases are not deleted.

## Android launcher

The native Android app in `app/` includes:
- Android app discovery, app launching, search, pinning, hiding and app information.
- Gallery wallpaper picker and first-run setup.
- Dark, translucent glass-inspired surfaces with accent palette, glass opacity and corner-roundness controls.
- Grid columns, icon size, text scaling, labels, clock and search customization.
- Dynamic Island-style pill. Double-tap it to expand or collapse its quick-action panel.
- Configure an installed app and an HTTP/HTTPS bookmark as Dynamic Island shortcuts.
- Swipe upward from the lower part of the home screen to open the app library.
- Anonixex Terminal with a limited built-in command environment.
- Control Center links to Android system settings.
- No ads, analytics SDK, account system or internet permission.

The terminal supports app-specific commands such as `help`, `neofetch`, `uname`, `date`, `uptime`, `apps`, `open <name>`, `theme list`, `theme <color>`, `wallpaper`, `settings`, `about`, `ls`, `pwd`, `whoami`, `free`, `lscpu`, and `pkg list`. These commands operate on Anonixex features; they do not execute arbitrary Linux or Android shell commands.

## Windows x64 beta companion

The Electron app in `desktop/` is a phone-shaped preview designed to test the same visual direction:
- Portrait phone-like frame, rounded edges, translucent layered glass and configurable blur/opacity.
- Dynamic Island that expands/collapses on double-click, with a pinned Windows shortcut and saved HTTP/HTTPS link.
- Swipe-up gesture for the app library, app search and Start Menu shortcut launching.
- Setup, wallpaper picker, eight accent colours and Studio customization.
- Grid columns, icon size, text scale, panel roundness, glass strength, blur and opacity.
- Arch-inspired Anonixex terminal commands running inside the app's own command interpreter.
- Windows Settings shortcuts.

The Windows build is explicitly beta software. It does not replace Windows, install a kernel, or execute Arch Linux commands on the Windows host. It reads Start Menu/Desktop shortcuts rather than scanning private application data. No ads, analytics or account system are included.

### Run from source

Install Node.js 22 or newer:

```bash
cd desktop
npm install
npm start
```

### Build the portable Windows x64 beta

```bash
cd desktop
npm install
npm run dist:win
```

The executable is written to `desktop/dist/`.

## Kernel and runtime clarification

A normal Android or Windows app cannot install its own hardware kernel while it is running as an application. Android apps run on the device's existing Linux kernel; Windows apps run on the Windows NT kernel. Anonixex Terminal is a built-in command environment for launcher operations, not a complete Arch Linux shell.

The separate `src/` and `arch/` directories contain an early original-kernel compilation prototype. ARMv7-A and AArch64 are the primary phone targets; x86 and x86_64 are optional development targets. The prototype currently compiles object files only and is not a bootable OS image. A real OS needs a boot chain, memory management, scheduling, graphics, input, storage, networking, power management and device-specific drivers.

Use the Android app as a normal launcher and the Windows app as a beta companion. Do not flash experimental kernel code to a daily-use device.
