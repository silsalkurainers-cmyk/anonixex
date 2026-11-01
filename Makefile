CC := clang
BUILD_DIR := build

COMMON_CFLAGS := -std=c11 -O2 -Wall -Wextra -Werror \
                 -ffreestanding -fno-builtin -fno-stack-protector \
                 -fno-pic -fno-pie

.PHONY: all phone phone32 phone64 pc pc32 pc64 clean

# The phone targets are ARM. 32-bit ARM and 64-bit ARM are separate builds.
all: phone

phone: phone32 phone64
phone32: $(BUILD_DIR)/anonixex-arm32.o
phone64: $(BUILD_DIR)/anonixex-arm64.o

pc: pc32 pc64
pc32: $(BUILD_DIR)/anonixex-x86.o
pc64: $(BUILD_DIR)/anonixex-x86_64.o

$(BUILD_DIR):
	mkdir -p $(BUILD_DIR)

$(BUILD_DIR)/anonixex-arm32.o: src/kernel.c | $(BUILD_DIR)
	$(CC) --target=armv7a-none-eabi $(COMMON_CFLAGS) -mcpu=cortex-a7 -c $< -o $@

$(BUILD_DIR)/anonixex-arm64.o: src/kernel.c | $(BUILD_DIR)
	$(CC) --target=aarch64-none-elf $(COMMON_CFLAGS) -mcpu=cortex-a53 -c $< -o $@

$(BUILD_DIR)/anonixex-x86.o: src/kernel.c | $(BUILD_DIR)
	$(CC) --target=i386-unknown-none-elf $(COMMON_CFLAGS) -c $< -o $@

$(BUILD_DIR)/anonixex-x86_64.o: src/kernel.c | $(BUILD_DIR)
	$(CC) --target=x86_64-unknown-none-elf $(COMMON_CFLAGS) -mno-red-zone -mcmodel=kernel -c $< -o $@

clean:
	rm -rf $(BUILD_DIR)
