# CPU architecture support

Architecture-specific code lives here.

| Architecture | Status |
| --- | --- |
| x86_64 | Initial freestanding kernel build |
| x86 (32-bit) | Planned |
| ARM32 / AArch32 | Planned |
| ARM64 / AArch64 | Planned |

The first milestone intentionally brings up one target at a time. The common
kernel core will be kept separate from architecture-specific boot, interrupt,
CPU, and memory-management code.

A successful x86_64 compile does not imply that the other targets are supported.
Each target needs its own entry code, linker layout, interrupt setup, and
hardware testing.
