# Anonixex

Anonixex is an original operating-system and kernel project. Its primary
product goal is a phone OS with its own kernel, not an Android/Linux
distribution. Desktop/PC targets are optional development targets.

## CPU targets

| Target | Purpose | Status |
| --- | --- | --- |
| ARMv7-A (32-bit) | Older 32-bit phones and ARM development boards | Compile prototype |
| AArch64 (64-bit) | Modern 64-bit phones and ARM development boards | Compile prototype |
| x86 (32-bit) | Optional PC/emulator development | Compile prototype |
| x86_64 (64-bit) | Optional PC/emulator development | Compile prototype |

## Build

Install Clang and Make, then run:

```sh
make phone
```

This compiles the common freestanding kernel entry for both ARM phone CPU
architectures. To compile all four CPU targets, run `make phone pc`.

GitHub Actions compiles each target separately and uploads its object file.

## Important current limitations

These are architecture compile prototypes, not bootable phone images. They
do not yet initialize a phone, display a screen, use touch, access cellular
modems, Wi-Fi, cameras, storage, or battery management. A real phone port also
needs a supported device's boot chain, memory map/device tree, interrupt
controller, timers, display and input drivers, storage drivers, and power
management. Each phone model has different hardware and firmware requirements.

The kernel is original project code. A bootloader or firmware interface may
be used to transfer control to it; that does not make the kernel Android or
Linux-based.
