/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

/*
 * This module contains definitions of target CPU registers and stack frames.
 */

#include "config.h"

#if ENABLE_DebugContext

#include <stddef.h>
#include <stdio.h>
#include <assert.h>
#include "cpudefs.h"
#include "errors.h"
#include "context.h"
#include "myalloc.h"
#include "exceptions.h"
#include "breakpoints.h"
#include "symbols.h"

#if ENABLE_ContextProxy

/* Register definitions are provided by context proxy */

#else

#include "cpudefs-mdep.h"

RegisterDefinition * get_reg_definitions(Context * ctx) {
    return regs_index;
}

size_t get_break_size(void) {
    return sizeof(BREAK_INST);
}

static RegisterDefinition * get_reg_by_dwarf_id(unsigned id) {
    static RegisterDefinition ** map = NULL;
    static unsigned map_length = 0;

    if (map == NULL) {
        RegisterDefinition * r;
        for (r = get_reg_definitions(NULL); r->name != NULL; r++) {
            if (r->dwarf_id >= (int)map_length) map_length = r->dwarf_id + 1;
        }
        map = (RegisterDefinition **)loc_alloc_zero(sizeof(RegisterDefinition *) * map_length);
        for (r = get_reg_definitions(NULL); r->name != NULL; r++) {
            if (r->dwarf_id >= 0) map[r->dwarf_id] = r;
        }
    }
    return id < map_length ? map[id] : NULL;
}

static RegisterDefinition * get_reg_by_eh_frame_id(unsigned id) {
    static RegisterDefinition ** map = NULL;
    static unsigned map_length = 0;

    if (map == NULL) {
        RegisterDefinition * r;
        for (r = get_reg_definitions(NULL); r->name != NULL; r++) {
            if (r->eh_frame_id >= (int)map_length) map_length = r->eh_frame_id + 1;
        }
        map = (RegisterDefinition **)loc_alloc_zero(sizeof(RegisterDefinition *) * map_length);
        for (r = get_reg_definitions(NULL); r->name != NULL; r++) {
            if (r->eh_frame_id >= 0) map[r->eh_frame_id] = r;
        }
    }
    return id < map_length ? map[id] : NULL;
}

RegisterDefinition * get_reg_by_id(Context * ctx, unsigned id, unsigned munbering_convention) {
    switch (munbering_convention) {
    case REGNUM_DWARF: return get_reg_by_dwarf_id(id);
    case REGNUM_EH_FRAME: return get_reg_by_eh_frame_id(id);
    }
    return NULL;
}

#endif /* !ENABLE_ContextProxy */

int read_reg_value(RegisterDefinition * reg_def, StackFrame * frame, uint64_t * value) {
    if (reg_def != NULL && frame != NULL) {
        size_t size = reg_def->size;
        if (size <= 8) {
            static uint8_t ones[] = { 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff };
            uint8_t * r_addr = (uint8_t *)frame->regs + reg_def->offset;
            uint8_t * m_addr = (uint8_t *)frame->mask + reg_def->offset;
            assert(reg_def->offset + size <= frame->regs_size);
            if (memcmp(m_addr, ones, size) == 0) {
                if (value != NULL) {
                    switch (size) {
                    case 1: *value = *(uint8_t *)r_addr; break;
                    case 2: *value = *(uint16_t *)r_addr; break;
                    case 4: *value = *(uint32_t *)r_addr; break;
                    case 8: *value = *(uint64_t *)r_addr; break;
                    }
                }
                return 0;
            }
        }
        else {
            errno = ERR_INV_DATA_SIZE;
            return -1;
        }
    }
    errno = ERR_INV_CONTEXT;
    return -1;
}

int write_reg_value(RegisterDefinition * reg_def, StackFrame * frame, uint64_t value) {
    if (reg_def != NULL && frame != NULL) {
        size_t size = reg_def->size;
        if (size <= 8) {
            uint8_t * r_addr = (uint8_t *)frame->regs + reg_def->offset;
            uint8_t * m_addr = (uint8_t *)frame->mask + reg_def->offset;
            assert(reg_def->offset + size <= frame->regs_size);
            memset(m_addr, 0xff, size);
            switch (size) {
            case 1: *(uint8_t *)r_addr = (uint8_t)value; break;
            case 2: *(uint16_t *)r_addr = (uint16_t)value; break;
            case 4: *(uint32_t *)r_addr = (uint32_t)value; break;
            case 8: *(uint64_t *)r_addr = (uint64_t)value; break;
            }
            return 0;
        }
        else {
            errno = ERR_INV_DATA_SIZE;
            return -1;
        }
    }
    errno = ERR_INV_CONTEXT;
    return -1;
}

int id2frame(const char * id, Context ** ctx, int * frame) {
    int f = 0;
    Context * c = NULL;

    if (*id++ != 'F') {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (*id++ != 'P') {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    while (*id != '.') {
        if (*id < '0' || *id > '9') {
            errno = ERR_INV_CONTEXT;
            return -1;
        }
        f = f * 10 + (*id++ - '0');
    }
    id++;
    c = id2ctx(id);
    if (c == NULL) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    *ctx = c;
    *frame = f;
    return 0;
}

char * frame2id(Context * ctx, int frame) {
    static char id[256];

    assert(frame >= 0);
    if (!context_has_state(ctx)) {
        errno = ERR_INV_CONTEXT;
        return NULL;
    }
    snprintf(id, sizeof(id), "FP%d.%s", frame, ctx->id);
    return id;
}

char * register2id(Context * ctx, int frame, RegisterDefinition * reg) {
    static char id[256];
    RegisterDefinition * defs = get_reg_definitions(ctx);
    if (frame < 0) {
        snprintf(id, sizeof(id), "R%d.%s", (int)(reg - defs), ctx->id);
    }
    else {
        snprintf(id, sizeof(id), "R%d@%d.%s", (int)(reg - defs), frame, ctx->id);
    }
    return id;
}

int id2register(const char * id, Context ** ctx, int * frame, RegisterDefinition ** reg_def) {
    int r = 0;

    *ctx = NULL;
    *frame = STACK_TOP_FRAME;
    *reg_def = NULL;
    if (*id++ != 'R') {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    while (*id != '.' && *id != '@') {
        if (*id >= '0' && *id <= '9') {
            r = r * 10 + (*id++ - '0');
        }
        else {
            errno = ERR_INV_CONTEXT;
            return -1;
        }
    }
    if (*id == '@') {
        int n = 0;
        id++;
        while (*id != '.') {
            if (*id >= '0' && *id <= '9') {
                n = n * 10 + (*id++ - '0');
            }
            else {
                errno = ERR_INV_CONTEXT;
                return -1;
            }
        }
        *frame = n;
    }
    id++;
    *ctx = id2ctx(id);
    if (*ctx == NULL) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if ((*ctx)->exited) {
        errno = ERR_ALREADY_EXITED;
        return -1;
    }
    *reg_def = get_reg_definitions(*ctx) + r;
    return 0;
}

static void stack_trace_error(void) {
    str_exception(ERR_OTHER, "Invalid stack trace program");
}

uint64_t evaluate_stack_trace_commands(Context * ctx, StackFrame * frame, StackTracingCommandSequence * cmds) {
    static uint64_t * stk = NULL;
    static int stk_size = 0;

    int i;
    int stk_pos = 0;

    for (i = 0; i < cmds->cmds_cnt; i++) {
        StackTracingCommand * cmd = cmds->cmds + i;
        if (stk_pos >= stk_size) {
            stk_size += 4;
            stk = (uint64_t *)loc_realloc(stk, sizeof(uint64_t) * stk_size);
        }
        switch (cmd->cmd) {
        case SFT_CMD_NUMBER:
            stk[stk_pos++] = cmd->num;
            break;
        case SFT_CMD_REGISTER:
            if (read_reg_value(cmd->reg, frame, stk + stk_pos) < 0) exception(errno);
            stk_pos++;
            break;
        case SFT_CMD_FP:
            stk[stk_pos++] = frame->fp;
            break;
        case SFT_CMD_DEREF:
            if (stk_pos < 1) stack_trace_error();
            {
                size_t j;
                size_t size = cmd->size;
                uint64_t n = 0;
                uint8_t buf[8];

                if (context_read_mem(ctx, (ContextAddress)stk[stk_pos - 1], buf, size) < 0) exception(errno);
                for (j = 0; j < size; j++) {
                    n = (n << 8) | buf[cmd->big_endian ? j : size - j - 1];
                }
                stk[stk_pos - 1] = n;
            }
            break;
        case SFT_CMD_ADD:
            if (stk_pos < 2) stack_trace_error();
            stk[stk_pos - 2] = stk[stk_pos - 2] + stk[stk_pos - 1];
            stk_pos--;
            break;
        default:
            stack_trace_error();
            break;
        }
    }
    if (stk_pos != 1) stack_trace_error();
    return stk[0];
}

#endif /* ENABLE_DebugContext */
