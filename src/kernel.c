#include <stdint.h>

/*
 * Anonixex kernel bootstrap.
 *
 * This first milestone targets x86_64 and uses the Limine boot protocol
 * only as a temporary way to enter our independently written kernel.
 * Limine is a bootloader, not our kernel.
 */

__attribute__((used, section(".limine_requests_start")))
static volatile uint64_t limine_requests_start_marker[4] = {
    0xf6b8f4b39de7d1ae,
    0xfab91a6940fcb9cf,
    0x785c6ed015d3e316,
    0x181e920a7852b9d9
};

__attribute__((used, section(".limine_requests")))
static volatile uint64_t limine_base_revision[3] = {
    0xf9562b2d5c95a6c8,
    0x6a7b384944536bdc,
    3
};

__attribute__((used, section(".limine_requests_end")))
static volatile uint64_t limine_requests_end_marker[2] = {
    0xadc0e0531bb10d03,
    0x9572709f31764c62
};

static inline void outb(uint16_t port, uint8_t value)
{
    __asm__ volatile ("outb %0, %1" : : "a"(value), "Nd"(port));
}

static inline uint8_t inb(uint16_t port)
{
    uint8_t value;
    __asm__ volatile ("inb %1, %0" : "=a"(value) : "Nd"(port));
    return value;
}

static void serial_init(void)
{
    outb(0x3F9, 0x00);
    outb(0x3FB, 0x80);
    outb(0x3F8, 0x03);
    outb(0x3F9, 0x00);
    outb(0x3FB, 0x03);
    outb(0x3FA, 0xC7);
    outb(0x3FC, 0x0B);
}

static void serial_write_char(char character)
{
    while ((inb(0x3FD) & 0x20U) == 0U) {
        __asm__ volatile ("pause");
    }
    outb(0x3F8, (uint8_t)character);
}

static void serial_write(const char *message)
{
    while (*message != '\0') {
        if (*message == '\n') {
            serial_write_char('\r');
        }
        serial_write_char(*message);
        ++message;
    }
}

__attribute__((noreturn))
void kmain(void)
{
    __asm__ volatile ("cli");
    serial_init();

    serial_write("\n");
    serial_write("  ANONIXEX KERNEL\n");
    serial_write("  Original kernel project\n");
    serial_write("  Architecture: x86_64\n");
    serial_write("  Boot stage: kernel entry reached\n");
    serial_write("\n");
    serial_write("System halted safely. More kernel subsystems are coming.\n");

    for (;;) {
        __asm__ volatile ("hlt");
    }
}
