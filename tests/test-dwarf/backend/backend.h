#ifndef D_backend
#define D_backend

#include <config.h>
#include <framework/channel.h>

#define MAX_REGS 64

struct RegisterData {
    uint8_t data[MAX_REGS * 8];
    uint8_t mask[MAX_REGS * 8];
};

#endif /* D_backend */
