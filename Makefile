CC := clang
LD := ld.lld

BUILD_DIR := build
KERNEL := $(BUILD_DIR)/anonixex-kernel.elf
OBJECTS := $(BUILD_DIR)/kernel.o

CFLAGS := --target=x86_64-unknown-none-elf \
          -std=c11 -O2 -Wall -Wextra -Werror \
          -ffreestanding -fno-builtin -fno-stack-protector \
          -fno-pic -fno-pie -mno-red-zone -mcmodel=kernel

LDFLAGS := -nostdlib -static -z max-page-size=0x1000 -T linker.ld

.PHONY: all clean
all: $(KERNEL)

$(BUILD_DIR):
	mkdir -p $(BUILD_DIR)

$(BUILD_DIR)/kernel.o: src/kernel.c | $(BUILD_DIR)
	$(CC) $(CFLAGS) -c $< -o $@

$(KERNEL): $(OBJECTS) linker.ld
	$(LD) $(LDFLAGS) -o $@ $(OBJECTS)

clean:
	rm -rf $(BUILD_DIR)
