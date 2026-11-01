#include <stdint.h>

/*
 * Anonixex common kernel entry prototype.
 *
 * This source is deliberately freestanding and contains no PC-specific
 * UART or port-I/O code. Phone hardware support still requires platform
 * boot code, a device tree, interrupt controllers, timers, and drivers.
 */

#if defined(__aarch64__)
static inline void cpu_wait(void)
{
    __asm__ volatile ("wfi");
}
#elif defined(__arm__)
static inline void cpu_wait(void)
{
    __asm__ volatile ("wfi");
}
#elif defined(__i386__) || defined(__x86_64__)
static inline void cpu_wait(void)
{
    __asm__ volatile ("hlt");
}
#else
#error "Unsupported CPU architecture"
#endif

__attribute__((noreturn))
void kmain(void)
{
    /*
     * The first milestone is only a compile-tested entry point.
     * A platform boot layer must initialize memory and provide output.
     */
    for (;;) {
        cpu_wait();
    }
}
