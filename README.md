# Anonixex

Anonixex is being developed in stages: customizable apps first, followed by an original phone operating system and kernel. The current Android and desktop apps are usable interface prototypes, not a standalone OS.

## Downloads

The GitHub Releases page publishes both builds together:

- **Android:** `Anonixex-Launcher.apk` — native Android launcher.
- **Windows x64:** `Anonixex-Desktop-0.1.0-x64.exe` — portable 64-bit Windows desktop companion.

The Windows build is currently for 64-bit Windows 10/11 PCs. It is a separate desktop application and does not replace Windows. It does not require administrator access to run.

## Android launcher

The native Android app is in `app/` and includes:

- App drawer, search, app launching, pin/hide actions and app information.
- Custom gallery wallpaper and a four-step first-run setup.
- Liquid-glass inspired interface, clock, spotlight card and pinned dock.
- Eight accent colors, glass toggle, grid sizing, icon sizing, text scaling, labels, clock and search controls.
- Control Center shortcuts to Android settings.
- Kernel Lab showing the actual Android version, kernel version, CPU ABI and device model.
- No advertisements, analytics SDK, account system or internet permission.

Android apps run in a permission sandbox. An ordinary launcher cannot silently control every app, read private app data or change protected system settings.

## Windows desktop companion

The Electron app is in `desktop/` and includes:

- Four-step welcome/setup experience.
- Futuristic dark liquid-glass home screen, clock/date, spotlight and navigation.
- Windows Start Menu shortcut discovery, app search and app launching.
- Pinned apps and hidden-app management.
- Gallery/file-picker wallpaper selection.
- Eight accent colors, glass toggle, app labels, clock/search toggles, icon sizing, text scaling and app-grid sizing.
- Control Center shortcuts that open the corresponding Windows Settings pages.
- Kernel Lab displaying Windows kernel release, CPU architecture/model, core count, memory and hostname.
- Local preferences stored on the PC; no ads, analytics, account system or app network service.

The Windows version reads Start Menu shortcuts rather than scanning private application data. Windows may still apply its normal permissions when an app is launched.

### Run the desktop app from source

Install Node.js 22 or newer, then:

```bash
cd desktop
npm install
npm start
```

### Build the portable Windows x64 executable

```bash
cd desktop
npm install
npm run dist:win
```

The executable is written to `desktop/dist/`.

## Build and releases

GitHub Actions builds the Android APK and the Windows x64 portable executable in parallel. A release is published only after both builds succeed, and includes both files. The Android APK is retained; the desktop build is an additional download.

## Original Anonixex kernel

The separate `src/` and `arch/` code is an early original-kernel compilation prototype. ARMv7-A (32-bit) and AArch64 (64-bit) are the primary phone CPU targets; x86 and x86_64 are optional development targets.

These kernel targets currently compile object files only. They are not bootable phone or PC images and are not used by either launcher app. A real OS still needs a boot chain, memory management, process scheduling, graphics, input, storage, networking, power management and device-specific drivers.

## Development safety

Use the Android launcher as a normal app and the Windows build as a desktop application. Do not flash the experimental kernel to a daily-use phone or PC. Kernel bring-up should begin in an emulator or on a spare development device with a documented recovery path.
