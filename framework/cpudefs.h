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
typedef struct NamedRegisterValue NamedRegisterValue;

struct NamedRegisterValue {
    uint8_t * value;
    const char * name;
    const char * description;
};

struct RegisterDefinition {
    const char *    name;          /* pointer to register name */
    size_t          offset;        /* offset to entry in REG_SET */
    size_t          size;          /* register size in bytes */
    int16_t         dwarf_id;      /* ID of the register in DWARF sections, or -1 */
    int16_t         eh_frame_id;   /* ID of the register in .eh_frame section, or -1 */
    uint8_t         big_endian;    /* 0 - little endian, 1 -  big endian */
    uint8_t         fp_value;      /* true if the register value is a floating-point value */
    uint8_t         no_read;       /* true if context value can not be read */
    uint8_t         no_write;      /* true if context value can not be written */
    uint8_t         read_once;     /* true if reading the context (register) destroys its current value */
    uint8_t         write_once;    /* true if register value can not be overwritten - every write counts */
    uint8_t         side_effects;  /* true if writing the context can change values of other registers */
    uint8_t         volatile_value;/* true if the register value can change even when target is stopped */
    uint8_t         left_to_right; /* true if the lowest numbered bit should be shown to user as the left-most bit */
    int             first_bit;     /* bit numbering base (0 or 1) to use when showing bits to user */
    int *           bits;          /* if context is a bit field, contains the field bit numbers in the parent register definition, -1 marks end of the list */
    RegisterDefinition * parent;   /* parent register definition, NULL for top level definitions */
    NamedRegisterValue ** values;  /* predefined names (mnemonics) for some of register values */
    ContextAddress  memory_address;/* the address of a memory mapped register */
    const char *    memory_context;/* the context ID of a memory context in which a memory mapped register is located */
    const char *    role;          /* the role the register plays in a program execution */
};

/* Stack tracing command codes */
#define SFT_CMD_NUMBER          1
#define SFT_CMD_REGISTER        2
#define SFT_CMD_FP              3
#define SFT_CMD_DEREF           4
#define SFT_CMD_ADD             5
#define SFT_CMD_SUB             6
#define SFT_CMD_AND             7
#define SFT_CMD_OR              8

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

typedef struct StackFrame {
    int is_top_frame;
    Context * ctx;
    ContextAddress fp;      /* frame address */
    RegisterData * regs;    /* register values */
} StackFrame;

typedef struct RegisterIdScope {
    uint16_t machine;
    uint8_t os_abi;
    uint8_t big_endian;
    uint8_t id_type;
} RegisterIdScope;

/* Return array of CPU register definitions. Last item in the array has name == NULL */
extern RegisterDefinition * get_reg_definitions(Context * ctx);

/* Search register definition for given register ID, return NULL if not found */
extern RegisterDefinition * get_reg_by_id(Context * ctx, unsigned id, RegisterIdScope * scope);

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
extern const char * frame2id(Context * ctx, int frame);

/* Get stack frame for TCF ID */
extern int id2frame(const char * id, Context ** ctx, int * frame);

/* Get TCF ID of a register */
extern const char * register2id(Context * ctx, int frame, RegisterDefinition * reg);

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

/* Execute stack tracing command sequence */
extern uint64_t evaluate_stack_trace_commands(Context * ctx, StackFrame * frame, StackTracingCommandSequence * cmds);

#endif /* ENABLE_DebugContext */

#endif /* D_cpudefs */
