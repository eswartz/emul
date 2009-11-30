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
 * This module contains definitions of target CPU registers.
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
#include "breakpoints.h"
#include "symbols.h"

#if defined(_WRS_KERNEL)

/* VxWork has its own register definitions and stack crawling function */

static RegisterDefinition * regs_index = NULL;

RegisterDefinition * get_reg_definitions(void) {
    if (regs_index == NULL) {
        int cnt = 0;
        REG_INDEX * r;
        for (r = taskRegName; r->regName; r++) cnt++;
        regs_index = loc_alloc_zero(sizeof(RegisterDefinition) * (cnt + 1));
        cnt = 0;
        for (r = taskRegName; r->regName; r++) {
            regs_index[cnt].name = r->regName;
            regs_index[cnt].offset = r->regOff;
#if defined(_WRS_REG_INDEX_REGWIDTH) || (CPU_FAMILY == COLDFIRE)
            regs_index[cnt].size = regWidth;
#else
            regs_index[cnt].size = 4;
#endif
            regs_index[cnt].dwarf_id = -1;
            regs_index[cnt].eh_frame_id = -1;
            cnt++;
        }
    }
    return regs_index;
}

RegisterDefinition * get_PC_definition(void) {
    static RegisterDefinition * reg_def = NULL;
    if (reg_def == NULL) {
        RegisterDefinition * r;
        for (r = get_reg_definitions(); r->name != NULL; r++) {
            if (r->offset == offsetof(REG_SET, reg_pc)) {
                reg_def = r;
                break;
            }
        }
    }
    return reg_def;
}


ContextAddress get_regs_PC(RegisterData * regs) {
    return (ContextAddress)((REG_SET *)regs)->reg_pc;
}

void set_regs_PC(RegisterData * regs, ContextAddress pc) {
    ((REG_SET *)regs)->reg_pc = (void *)pc;
}

#else /* _WRS_KERNEL */

#include "cpudefs-mdep.h"

RegisterDefinition * get_reg_definitions(void) {
    return regs_index;
}

#endif /* _WRS_KERNEL */

static RegisterDefinition * get_reg_by_dwarf_id(unsigned id) {
    static RegisterDefinition ** map = NULL;
    static unsigned map_length = 0;

    if (map == NULL) {
        RegisterDefinition * r;
        for (r = get_reg_definitions(); r->name != NULL; r++) {
            if (r->dwarf_id >= (int)map_length) map_length = r->dwarf_id + 1;
        }
        map = loc_alloc_zero(sizeof(RegisterDefinition *) * map_length);
        for (r = get_reg_definitions(); r->name != NULL; r++) {
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
        for (r = get_reg_definitions(); r->name != NULL; r++) {
            if (r->eh_frame_id >= (int)map_length) map_length = r->eh_frame_id + 1;
        }
        map = loc_alloc_zero(sizeof(RegisterDefinition *) * map_length);
        for (r = get_reg_definitions(); r->name != NULL; r++) {
            if (r->eh_frame_id >= 0) map[r->eh_frame_id] = r;
        }
    }
    return id < map_length ? map[id] : NULL;
}

RegisterDefinition * get_reg_by_id(unsigned id, unsigned munbering_convention) {
    switch (munbering_convention) {
    case REGNUM_DWARF: return get_reg_by_dwarf_id(id);
    case REGNUM_EH_FRAME: return get_reg_by_eh_frame_id(id);
    }
    return NULL;
}

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

size_t get_break_size(void) {
    return sizeof(BREAK_INST);
}

#endif /* ENABLE_DebugContext */
