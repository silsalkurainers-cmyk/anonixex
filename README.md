# Anonixex

Anonixex is being developed in two stages: first a customizable Android launcher APK, then an original phone operating system and kernel. The launcher is the first usable product and the future OS will grow from its interface and app experience.

## Anonixex Launcher (early prototype)

The Android app is in `app/`. It is designed to be selectable as the device's Home app and currently includes:

- App drawer populated from apps that expose Android launcher activities.
- App search and one-tap app launching.
- Long-press an app to open its Android app-info page.
- Custom wallpaper selected by the user.
- Four accent color choices, 3–6 app grid columns, and app-label visibility setting.
- No advertisements, analytics SDK, account system, or network permission.

The app deliberately does not request unrestricted access to the phone. Android apps run in a permission sandbox; a launcher can show and start launchable apps, but it cannot silently control every app, read private app data, or change protected system settings. Features that need access to user data must use Android's permission prompts and system pickers.

### Build the APK

GitHub Actions builds a debug APK on pushes that change the Android app. Open the **Build Anonixex Android APK** workflow, select the successful run, and download the `anonixex-launcher-debug` artifact. The APK is named `app-debug.apk`.

## Original Anonixex kernel

The separate `src/` and `arch/` code is the early original-kernel compilation prototype. ARMv7-A (32-bit) and AArch64 (64-bit) are the primary phone CPU targets; x86 and x86_64 are optional development targets.

These kernel targets currently compile object files only. They are not bootable phone images and are not used by the Android launcher APK. A real phone OS still needs a boot chain, memory management, process scheduling, graphics, input, storage, networking, power management, and device-specific drivers.

## Development safety

Use the launcher as a normal Android app. Do not flash the experimental kernel to a daily-use phone. Kernel bring-up should begin in an emulator or on a spare development device with a documented recovery path.
