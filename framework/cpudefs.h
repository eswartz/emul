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

#ifndef D_cpudefs
#define D_cpudefs

#include "config.h"

typedef uintptr_t ContextAddress; /* Type to represent byte address inside context memory */

#if ENABLE_DebugContext

#include <regset.h>

typedef struct RegisterDefinition {
    char *      name;           /* pointer to register name */
    int         offset;         /* offset to entry in REG_SET */
    int         size;           /* register size in bytes */
    int         dwarf_id;       /* ID of the register in DWARF sections */
    int         eh_frame_id;    /* ID of the register in .eh_frame section */
    int         traceable;      /* register value can be traced using .eh_frame of .debug_frame */
} RegisterDefinition;

typedef struct StackFrame {
    int is_top_frame;
    ContextAddress fp;  /* frame address */
    REG_SET regs;       /* registers */
    REG_SET mask;       /* registers valid bits mask */
} StackFrame;

/* Return array of CPU regiter definitions. LAst item in the array has name == NULL */
extern RegisterDefinition * get_reg_definitions(void);

/* Search register definition for given DWARF register ID, return NULL if not found */
extern RegisterDefinition * get_reg_by_dwarf_id(int id);

/* Search register definition for given .eh_frame section register ID, return NULL if not found */
extern RegisterDefinition * get_reg_by_eh_frame_id(int id);

/* Return register definition of instruction pointer */
extern RegisterDefinition * get_PC_definition(void);

/* Read register value from stack frame data, return 0 on success, return -1 and set errno if register is not available  */
extern int read_reg_value(RegisterDefinition * reg_def, StackFrame * frame, uint64_t * value);

/* Write register value into stack frame data, return 0 on success, return -1 and set errno if register is not available  */
extern int write_reg_value(RegisterDefinition * reg_def, StackFrame * frame, uint64_t value);

/* Get instruction pointer (PC) value */
#define get_regs_PC(x) get_regs_PC_func(&(x))

/* Set instruction pointer (PC) value */
#define set_regs_PC(x,y) set_regs_PC_func(&(x), (ContextAddress)(y))

extern ContextAddress get_regs_PC_func(REG_SET * regs);
extern void set_regs_PC_func(REG_SET * regs, ContextAddress pc);

#if !defined(_WRS_KERNEL)
extern unsigned char BREAK_INST[];  /* breakpoint instruction */
#define BREAK_SIZE get_break_size() /* breakpoint instruction size */
extern size_t get_break_size(void);
#endif

struct Context;

/*
 * Retrieve stack frame information by examining stack data in memory.
 *
 * "frame" is current frame info, it should have frame->regs and frame->mask filled with
 * proper values before this function is called.
 *
 * "down" is next frame - moving from stack top to the bottom.
 *
 * The function uses register values in current frame to calculate frame address "frame->fp",
 * and calculate register values in the next frame.
 */
extern int crawl_stack_frame(struct Context * ctx, StackFrame * frame, StackFrame * down);

#endif /* ENABLE_DebugContext */

#endif /* D_cpudefs */
