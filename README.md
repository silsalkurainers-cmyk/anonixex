# Anonixex

Anonixex is being developed in two stages: first a customizable Android launcher APK, then an original phone operating system and kernel. The launcher is the first usable product and the future OS will grow from its interface and app experience. The current Android launcher is not yet a standalone operating system.

## Anonixex Launcher (early prototype)

The Android app is in `app/`. It is designed to be selectable as the device's Home app and currently includes:

- App drawer populated from apps that expose Android launcher activities.
- App search and one-tap app launching.
- Long-press an app to open its Android app-info page.
- Custom wallpaper selected by the user.
- First-run welcome/setup screen.
- Redesigned home screen with app tiles, search, and a separate customization page.
- Futuristic liquid-glass visual system with layered translucent panels and a redesigned app icon.
- Four-step first-run setup for the welcome experience, accent palette, gallery wallpaper, and default Home selection.
- Home screen with clock/date, spotlight card, search, pinned-app dock, app library, control center, and subtle “made by hacer62828” signature.
- Expanded customization studio: eight accent colors, glass toggle, app grid (3–6 columns), icon sizing, text scaling, app labels, clock, search, wallpaper selection/removal, hidden apps, and layout reset.
- App library with search, app launching, long-press pin/hide/app-info actions, and persistent preferences.
- Kernel Lab showing the device's actual Android version, kernel version, CPU ABI and model, plus an explanation of the separate experimental Anonixex kernel.
- No advertisements, analytics SDK, account system, or network permission.

The app deliberately does not request unrestricted access to the phone. Android apps run in a permission sandbox; a launcher can show and start launchable apps, but it cannot silently control every app, read private app data, or change protected system settings. Features that need access to user data must use Android's permission prompts and system pickers.

### Build the APK

GitHub Actions builds the Android APK on pushes that change the app and publishes the raw `Anonixex-Launcher.apk` file as a GitHub Release asset. Download the `.apk` directly from the Releases page; the separate Actions artifact is ZIP-wrapped by GitHub.

## Future OS installer and storage footprint

A genuine phone OS installer and a 15 GB system image are not available yet. The current kernel code only compiles small target objects; it is not a bootable operating system. The launcher now has a first-run setup screen, but that is not a firmware/OS installer. A real installer requires a bootable image, supported device hardware, verified boot and partition handling, drivers, and a tested recovery path. The project will not pad the download to 15 GB just to hit a size target; storage use should come from real OS components and optional apps, not fake bulk.

## Original Anonixex kernel

The separate `src/` and `arch/` code is the early original-kernel compilation prototype. ARMv7-A (32-bit) and AArch64 (64-bit) are the primary phone CPU targets; x86 and x86_64 are optional development targets.

These kernel targets currently compile object files only. They are not bootable phone images and are not used by the Android launcher APK. A real phone OS still needs a boot chain, memory management, process scheduling, graphics, input, storage, networking, power management, and device-specific drivers.

## Development safety

Use the launcher as a normal Android app. Do not flash the experimental kernel to a daily-use phone. Kernel bring-up should begin in an emulator or on a spare development device with a documented recovery path.
