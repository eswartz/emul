/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
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

#ifndef D_cpudefs
#define D_cpudefs

#include <config.h>

typedef struct Context Context;

/* Type to represent byte address inside context memory */
#if ENABLE_ContextProxy
typedef uint64_t ContextAddress;
#else
typedef uintptr_t ContextAddress;
#endif

#if ENABLE_DebugContext

#define REGNUM_DWARF    1
#define REGNUM_EH_FRAME 2

typedef struct RegisterData RegisterData;

typedef struct RegisterDefinition RegisterDefinition;

struct RegisterDefinition {
    const char * name;           /* pointer to register name */
    size_t       offset;         /* offset to entry in REG_SET */
    size_t       size;           /* register size in bytes */
    int16_t      dwarf_id;       /* ID of the register in DWARF sections, or -1 */
    int16_t      eh_frame_id;    /* ID of the register in .eh_frame section, or -1 */
    uint8_t      traceable;      /* register value can be traced using .eh_frame of .debug_frame */
    uint8_t      big_endian;     /* 0 - little endian, 1 -  big endian */
};

/* Stack tracing command codes */
#define SFT_CMD_NUMBER          1
#define SFT_CMD_REGISTER        2
#define SFT_CMD_FP              3
#define SFT_CMD_DEREF           4
#define SFT_CMD_ADD             5

/* Stack tracing command */
typedef struct StackTracingCommand {
    int cmd;
    int64_t num;
    size_t size;
    int big_endian;
    RegisterDefinition * reg;
} StackTracingCommand;

/* Stack tracing command sequence */
typedef struct StackTracingCommandSequence {
    RegisterDefinition * reg;
    int cmds_cnt;
    int cmds_max;
    StackTracingCommand cmds[1];
} StackTracingCommandSequence;

/* Complete stack tracing info for a range of instruction addresses */
typedef struct StackTracingInfo {
    ContextAddress addr;
    ContextAddress size;
    StackTracingCommandSequence * fp;
    StackTracingCommandSequence ** regs;
    int reg_cnt;
} StackTracingInfo;

#define STACK_BOTTOM_FRAME  0
#define STACK_NO_FRAME      (-1)
#define STACK_TOP_FRAME     (-2)

typedef struct StackFrame StackFrame;

struct StackFrame {
    int is_top_frame;
    Context * ctx;
    ContextAddress fp;      /* frame address */
    RegisterData * regs;    /* register values */
};

/* Return array of CPU register definitions. Last item in the array has name == NULL */
extern RegisterDefinition * get_reg_definitions(Context * ctx);

/* Search register definition for given register ID, return NULL if not found */
extern RegisterDefinition * get_reg_by_id(Context * ctx, unsigned id, unsigned numbering_convention);

/* Return register definition of instruction pointer */
extern RegisterDefinition * get_PC_definition(Context * ctx);

/* Read register value from stack frame data, return 0 on success, return -1 and set errno if register is not available  */
extern int read_reg_value(StackFrame * frame, RegisterDefinition * reg_def, uint64_t * value);

/* Write register value into stack frame data, return 0 on success, return -1 and set errno if register is not available  */
extern int write_reg_value(StackFrame * frame, RegisterDefinition * reg_def, uint64_t value);

/* Read register bytes from stack frame data, return 0 on success, return -1 and set errno if register is not available  */
extern int read_reg_bytes(StackFrame * frame, RegisterDefinition * reg_def, unsigned offs, unsigned size, uint8_t * buf);

/* Write register bytes into stack frame data, return 0 on success, return -1 and set errno if register is not available  */
extern int write_reg_bytes(StackFrame * frame, RegisterDefinition * reg_def, unsigned offs, unsigned size, uint8_t * buf);

/* Get instruction pointer (PC) value */
extern ContextAddress get_regs_PC(Context * ctx);

/* Set instruction pointer (PC) value */
extern void set_regs_PC(Context * ctx, ContextAddress y);

/* Get TCF ID of a stack frame */
extern char * frame2id(Context * ctx, int frame);

/* Get stack frame for TCF ID */
extern int id2frame(const char * id, Context ** ctx, int * frame);

/* Get TCF ID of a register */
extern char * register2id(Context * ctx, int frame, RegisterDefinition * reg);

/* Get register for TCF ID */
extern int id2register(const char * id, Context ** ctx, int * frame, RegisterDefinition ** reg_def);

/* Get breakpoint instruction code and size */
extern uint8_t * get_break_instruction(Context * ctx, size_t * size);

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
extern int crawl_stack_frame(StackFrame * frame, StackFrame * down);

/*
 * Execute stack tracing command sequence.
 */
extern uint64_t evaluate_stack_trace_commands(Context * ctx, StackFrame * frame, StackTracingCommandSequence * cmds);

#endif /* ENABLE_DebugContext */

#endif /* D_cpudefs */
