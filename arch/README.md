# Architecture support

Anonixex is phone-first. ARM is the primary architecture family.

| Architecture | Intended role | Current status |
| --- | --- | --- |
| ARMv7-A / AArch32 | 32-bit phones and boards | Freestanding C compile prototype |
| ARMv8-A / AArch64 | 64-bit phones and boards | Freestanding C compile prototype |
| x86 / i386 | Optional PC testing | Freestanding C compile prototype |
| x86_64 | Optional PC testing | Freestanding C compile prototype |

The common kernel source must not contain PC-only port I/O. CPU-specific
entry, exception vectors, interrupt controllers, timers, memory management,
and platform drivers belong in architecture/platform directories.

A successful object compilation only proves that the source compiles for that
CPU target. It does not mean the OS boots or supports a real phone.
