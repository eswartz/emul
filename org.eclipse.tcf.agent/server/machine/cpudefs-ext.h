/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

#include <services/context-proxy.h>

RegisterDefinition * get_reg_by_id(Context * ctx, unsigned id, RegisterIdScope * scope) {
    RegisterDefinition * defs = get_reg_definitions(ctx);
    while (defs != NULL && defs->name != NULL) {
        switch (scope->id_type) {
        case REGNUM_DWARF:
            if (defs->dwarf_id == (int)id) return defs;
            break;
        case REGNUM_EH_FRAME:
            if (defs->eh_frame_id == (int)id) return defs;
            break;
        }
        defs++;
    }
    set_errno(ERR_OTHER, "Invalid register ID");
    return NULL;
}

int read_reg_bytes(StackFrame * frame, RegisterDefinition * reg_def, unsigned offs, unsigned size, uint8_t * buf) {
    if (reg_def == NULL || frame == NULL) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    else {
        size_t i;
        uint8_t * r_addr = frame->regs->data + reg_def->offset;
        uint8_t * m_addr = frame->regs->mask + reg_def->offset;
        for (i = 0; i < size; i++) {
            if (m_addr[offs + i] != 0xff) {
                errno = ERR_INV_CONTEXT;
                return -1;
            }
        }
        if (offs + size > reg_def->size) {
            errno = ERR_INV_DATA_SIZE;
            return -1;
        }
        memcpy(buf, r_addr + offs, size);
    }
    return 0;
}

int write_reg_bytes(StackFrame * frame, RegisterDefinition * reg_def, unsigned offs, unsigned size, uint8_t * buf) {
    if (reg_def == NULL || frame == NULL) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    else {
        uint8_t * r_addr = frame->regs->data + reg_def->offset;
        uint8_t * m_addr = frame->regs->mask + reg_def->offset;

        if (offs + size > reg_def->size) {
            errno = ERR_INV_DATA_SIZE;
            return -1;
        }
        memcpy(r_addr + offs, buf, size);
        memset(m_addr + offs, 0xff, size);
    }
    return 0;
}
