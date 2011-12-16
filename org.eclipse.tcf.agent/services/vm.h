/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
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


#ifndef D_vm
#define D_vm

#include <config.h>

#if ENABLE_DebugContext

#include <framework/context.h>

typedef struct VMState {
    /* Evaluation context */
    Context * ctx;
    int stack_frame;
    int big_endian;
    size_t addr_size;
    uint64_t object_address;
    RegisterIdScope reg_id_scope;

    /* Code to execute */
    uint8_t * code;
    size_t code_pos;
    size_t code_len;

    /* VM callback */
    void (*client_op)(uint8_t op);

    /* Result */
    RegisterDefinition * reg;
    uint32_t piece_offs;
    uint32_t piece_bits;

    /* Stack */
    unsigned stk_pos;
    unsigned stk_max;
    uint64_t * stk;
} VMState;

extern int evaluate_vm_expression(VMState * state);

#endif /* ENABLE_DebugContext */

#endif /* D_vm */
