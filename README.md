# Anonixex

**Anonixex is an original operating-system and kernel project.** The long-term
goal is a portable OS for phones and computers, with architecture support for
32-bit and 64-bit processors.

## Current status

The first source milestone is a freestanding x86_64 kernel entry that writes
diagnostic text to the serial port and halts safely. It is not yet a complete
operating system and does not yet boot on a phone.

The initial build uses the Limine boot protocol as a temporary bootloader
interface. Limine is not the Anonixex kernel; the kernel source is our own.

## Build

The x86_64 kernel can be compiled with:

```sh
make
```

The output is `build/anonixex-kernel.elf`. GitHub Actions runs this build on
pushes to `main` and on pull requests.

## Architecture roadmap

- x86_64: initial freestanding build
- x86 32-bit: planned
- ARM32 / AArch32: planned
- ARM64 / AArch64: planned

## Important

A successful compile is not proof that the kernel boots correctly. The next
milestone is to package it with a bootloader, run it in an emulator, and verify
the serial output before adding memory management, interrupts, scheduling,
drivers, and userspace.
