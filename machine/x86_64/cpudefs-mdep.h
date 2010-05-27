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
 * This module provides CPU specific definitions for X86.
 */

#ifdef _WRS_KERNEL
#  if CPU_FAMILY == SIMNT || CPU_FAMILY == I80X86
#    define __i386__ 1
#    define eip pc
#    undef BREAK_INST
#  endif
#  include <system/VxWorks/context-vxworks.h>
#endif

#if defined(__i386__) || defined(__x86_64__)

#include <regset.h>

#define REG_OFFSET(name) offsetof(REG_SET, name)

RegisterDefinition regs_index[] = {
#if defined(WIN32) && defined(__i386__)
#   define REG_SP Esp
#   define REG_BP Ebp
#   define REG_IP Eip
    { "eax",    REG_OFFSET(Eax),      4,  0,  0,  0},
    { "ecx",    REG_OFFSET(Ecx),      4,  1,  1,  0},
    { "edx",    REG_OFFSET(Edx),      4,  2,  2,  0},
    { "ebx",    REG_OFFSET(Ebx),      4,  3,  3,  0},
    { "esp",    REG_OFFSET(Esp),      4,  4,  4,  1},
    { "ebp",    REG_OFFSET(Ebp),      4,  5,  5,  1},
    { "esi",    REG_OFFSET(Esi),      4,  6,  6,  0},
    { "edi",    REG_OFFSET(Edi),      4,  7,  7,  0},
    { "eip",    REG_OFFSET(Eip),      4,  8,  8,  1},
    { "eflags", REG_OFFSET(EFlags),   4,  9,  9,  0},
    { "cs",     REG_OFFSET(SegCs),    4, -1, -1,  0},
    { "ss",     REG_OFFSET(SegSs),    4, -1, -1,  0},

#elif defined(__APPLE__) && defined(__i386__)
#   define REG_SP __esp
#   define REG_BP __ebp
#   define REG_IP __eip
    { "eax",    REG_OFFSET(__eax),    4,  0,  0,  0},
    { "ecx",    REG_OFFSET(__ecx),    4,  1,  1,  0},
    { "edx",    REG_OFFSET(__edx),    4,  2,  2,  0},
    { "ebx",    REG_OFFSET(__ebx),    4,  3,  3,  0},
    { "esp",    REG_OFFSET(__esp),    4,  4,  4,  1},
    { "ebp",    REG_OFFSET(__ebp),    4,  5,  5,  1},
    { "esi",    REG_OFFSET(__esi),    4,  6,  6,  0},
    { "edi",    REG_OFFSET(__edi),    4,  7,  7,  0},
    { "eip",    REG_OFFSET(__eip),    4,  8,  8,  1},
    { "eflags", REG_OFFSET(__eflags), 4,  9,  9,  0},

#elif defined(__APPLE__) && defined(__x86_64__)
#   define REG_SP __rsp
#   define REG_BP __rbp
#   define REG_IP __rip
    { "rax",    REG_OFFSET(__rax),    8,  0,  0,  0},
    { "rdx",    REG_OFFSET(__rdx),    8,  1,  1,  0},
    { "rcx",    REG_OFFSET(__rcx),    8,  2,  2,  0},
    { "rbx",    REG_OFFSET(__rbx),    8,  3,  3,  0},
    { "rsi",    REG_OFFSET(__rsi),    8,  4,  4,  0},
    { "rdi",    REG_OFFSET(__rdi),    8,  5,  5,  0},
    { "rbp",    REG_OFFSET(__rbp),    8,  6,  6,  1},
    { "rsp",    REG_OFFSET(__rsp),    8,  7,  7,  1},
    { "r8",     REG_OFFSET(__r8),     8,  8,  8,  0},
    { "r9",     REG_OFFSET(__r9),     8,  9,  9,  0},
    { "r10",    REG_OFFSET(__r10),    8, 10, 10,  0},
    { "r11",    REG_OFFSET(__r11),    8, 11, 11,  0},
    { "r12",    REG_OFFSET(__r12),    8, 12, 12,  0},
    { "r13",    REG_OFFSET(__r13),    8, 13, 13,  0},
    { "r14",    REG_OFFSET(__r14),    8, 14, 14,  0},
    { "r15",    REG_OFFSET(__r15),    8, 15, 15,  0},
    { "rip",    REG_OFFSET(__rip),    8, -1, -1,  1},
    { "eflags", REG_OFFSET(__rflags), 4, 49, -1,  0},

#elif (defined(__FreeBSD__) || defined(__NetBSD__)) && defined(__i386__)
#   define REG_SP r_esp
#   define REG_BP r_ebp
#   define REG_IP r_eip
    { "eax",    REG_OFFSET(r_eax),    4,  0,  0,  0},
    { "ecx",    REG_OFFSET(r_ecx),    4,  1,  1,  0},
    { "edx",    REG_OFFSET(r_edx),    4,  2,  2,  0},
    { "ebx",    REG_OFFSET(r_ebx),    4,  3,  3,  0},
    { "esp",    REG_OFFSET(r_esp),    4,  4,  4,  1},
    { "ebp",    REG_OFFSET(r_ebp),    4,  5,  5,  1},
    { "esi",    REG_OFFSET(r_esi),    4,  6,  6,  0},
    { "edi",    REG_OFFSET(r_edi),    4,  7,  7,  0},
    { "eip",    REG_OFFSET(r_eip),    4,  8,  8,  1},
    { "eflags", REG_OFFSET(r_eflags), 4,  9,  9,  0},

#elif defined(__x86_64__)
#   define REG_SP rsp
#   define REG_BP rbp
#   define REG_IP rip
    { "rax",    REG_OFFSET(rax),      8,  0,  0,  0},
    { "rdx",    REG_OFFSET(rdx),      8,  1,  1,  0},
    { "rcx",    REG_OFFSET(rcx),      8,  2,  2,  0},
    { "rbx",    REG_OFFSET(rbx),      8,  3,  3,  0},
    { "rsi",    REG_OFFSET(rsi),      8,  4,  4,  0},
    { "rdi",    REG_OFFSET(rdi),      8,  5,  5,  0},
    { "rbp",    REG_OFFSET(rbp),      8,  6,  6,  1},
    { "rsp",    REG_OFFSET(rsp),      8,  7,  7,  1},
    { "r8",     REG_OFFSET(r8),       8,  8,  8,  0},
    { "r9",     REG_OFFSET(r9),       8,  9,  9,  0},
    { "r10",    REG_OFFSET(r10),      8, 10, 10,  0},
    { "r11",    REG_OFFSET(r11),      8, 11, 11,  0},
    { "r12",    REG_OFFSET(r12),      8, 12, 12,  0},
    { "r13",    REG_OFFSET(r13),      8, 13, 13,  0},
    { "r14",    REG_OFFSET(r14),      8, 14, 14,  0},
    { "r15",    REG_OFFSET(r15),      8, 15, 15,  0},
    { "rip",    REG_OFFSET(rip),      8, -1, -1,  1},
    { "eflags", REG_OFFSET(eflags),   4, 49, -1,  0},
    { "es",     REG_OFFSET(es),       4, 50, -1,  0},
    { "cs",     REG_OFFSET(cs),       4, 51, -1,  0},
    { "ss",     REG_OFFSET(ss),       4, 52, -1,  0},
    { "ds",     REG_OFFSET(ds),       4, 53, -1,  0},
    { "fs",     REG_OFFSET(fs),       4, 54, -1,  0},
    { "gs",     REG_OFFSET(gs),       4, 55, -1,  0},
    { "fs_base", REG_OFFSET(fs_base), 4, 58, -1,  0},
    { "gs_base", REG_OFFSET(gs_base), 4, 59, -1,  0},

#else
#   define REG_SP esp
#   define REG_BP ebp
#   define REG_IP eip
    { "eax",    REG_OFFSET(eax),      4,  0,  0,  0},
    { "ecx",    REG_OFFSET(ecx),      4,  1,  1,  0},
    { "edx",    REG_OFFSET(edx),      4,  2,  2,  0},
    { "ebx",    REG_OFFSET(ebx),      4,  3,  3,  0},
    { "esp",    REG_OFFSET(esp),      4,  4,  4,  1},
    { "ebp",    REG_OFFSET(ebp),      4,  5,  5,  1},
    { "esi",    REG_OFFSET(esi),      4,  6,  6,  0},
    { "edi",    REG_OFFSET(edi),      4,  7,  7,  0},
    { "eip",    REG_OFFSET(eip),      4,  8,  8,  1},
    { "eflags", REG_OFFSET(eflags),   4,  9,  9,  0},

#endif

    { NULL,     0,                    0,  0,  0,  0},
};

#ifndef _WRS_KERNEL
#define JMPD08      0xeb
#define JMPD32      0xe9
#define PUSH_EBP    0x55
#define ENTER       0xc8
#define RET         0xc3
#define RETADD      0xc2
#endif
#define GRP5        0xff
#define JMPN        0x25
#define MOV_ESP00   0x89
#define MOV_ESP01   0xe5
#define MOV_ESP10   0x8b
#define MOV_ESP11   0xec
#define REXW        0x48

static int read_stack(Context * ctx, ContextAddress addr, void * buf, size_t size) {
    if (addr == 0) {
        errno = ERR_INV_ADDRESS;
        return -1;
    }
#ifdef _WRS_KERNEL
    {
        WIND_TCB * tcb = taskTcb(get_context_task_id(ctx));
        if (addr < (ContextAddress)tcb->pStackEnd || addr > (ContextAddress)tcb->pStackBase) {
            errno = ERR_INV_ADDRESS;
            return -1;
        }
    }
#endif
    return context_read_mem(ctx, addr, buf, size);
}

static int read_reg(StackFrame * frame, RegisterDefinition * def, ContextAddress * addr) {
    uint64_t v = 0;
    int r = read_reg_value(frame, def, &v);
    *addr = (ContextAddress)v;
    return r;
}

/*
 * trace_jump - resolve any JMP instructions to final destination
 *
 * This routine returns a pointer to the next non-JMP instruction to be
 * executed if the PC were at the specified <adrs>.  That is, if the instruction
 * at <adrs> is not a JMP, then <adrs> is returned.  Otherwise, if the
 * instruction at <adrs> is a JMP, then the destination of the JMP is
 * computed, which then becomes the new <adrs> which is tested as before.
 * Thus we will eventually return the address of the first non-JMP instruction
 * to be executed.
 *
 * The need for this arises because compilers may put JMPs to instructions
 * that we are interested in, instead of the instruction itself.  For example,
 * optimizers may replace a stack pop with a JMP to a stack pop.  Or in very
 * UNoptimized code, the first instruction of a subroutine may be a JMP to
 * a PUSH %EBP MOV %ESP %EBP, instead of a PUSH %EBP MOV %ESP %EBP (compiler
 * may omit routine "post-amble" at end of parsing the routine!).  We call
 * this routine anytime we are looking for a specific kind of instruction,
 * to help handle such cases.
 *
 * RETURNS: The address that a chain of branches points to.
 */
static ContextAddress trace_jump(Context * ctx, ContextAddress addr) {
    int cnt = 0;
    /* while instruction is a JMP, get destination adrs */
    while (cnt < 100) {
        unsigned char instr;    /* instruction opcode at <addr> */
        ContextAddress dest;    /* Jump destination address */
        if (context_read_mem(ctx, addr, &instr, 1) < 0) break;

        /* If instruction is a JMP, get destination adrs */
        if (instr == JMPD08) {
            signed char disp08;
            if (context_read_mem(ctx, addr + 1, &disp08, 1) < 0) break;
            dest = addr + 2 + disp08;
        }
        else if (instr == JMPD32) {
            int disp32;
            assert(sizeof(disp32) == 4);
            if (context_read_mem(ctx, addr + 1, &disp32, 4) < 0) break;
            dest = addr + 5 + disp32;
        }
        else if (instr == GRP5) {
            ContextAddress ptr;
            if (context_read_mem(ctx, addr + 1, &instr, 1) < 0) break;
            if (instr != JMPN) break;
            if (context_read_mem(ctx, addr + 2, &ptr, sizeof(ptr)) < 0) break;
            if (context_read_mem(ctx, ptr, &dest, sizeof(dest)) < 0) break;
        }
        else {
            break;
        }
        if (dest == addr) break;
        addr = dest;
        cnt++;
    }
    return addr;
}

static int func_entry(unsigned char * code) {
    if (*code != PUSH_EBP) return 0;
    code++;
    if (*code == REXW) code++;
    if (code[0] == MOV_ESP00 && code[1] == MOV_ESP01) return 1;
    if (code[0] == MOV_ESP10 && code[1] == MOV_ESP11) return 1;
    return 0;
}

int crawl_stack_frame(StackFrame * frame, StackFrame * down) {

    static RegisterDefinition * pc_def = NULL;
    static RegisterDefinition * sp_def = NULL;
    static RegisterDefinition * bp_def = NULL;

    ContextAddress reg_pc = 0;
    ContextAddress reg_sp = 0;
    ContextAddress reg_bp = 0;

    ContextAddress dwn_pc = 0;
    ContextAddress dwn_sp = 0;
    ContextAddress dwn_bp = 0;

    Context * ctx = frame->ctx;

    if (pc_def == NULL) {
        RegisterDefinition * r;
        for (r = get_reg_definitions(ctx); r->name != NULL; r++) {
            if (r->offset == offsetof(REG_SET, REG_IP)) pc_def = r;
            if (r->offset == offsetof(REG_SET, REG_SP)) sp_def = r;
            if (r->offset == offsetof(REG_SET, REG_BP)) bp_def = r;
        }
    }

    if (read_reg(frame, pc_def, &reg_pc) < 0) return 0;
    if (read_reg(frame, sp_def, &reg_sp) < 0) return 0;
    if (read_reg(frame, bp_def, &reg_bp) < 0) return 0;

    if (frame->is_top_frame) {
        /* Top frame */
        ContextAddress addr = trace_jump(ctx, reg_pc);
#if ENABLE_Symbols
        ContextAddress plt = is_plt_section(ctx, addr);
#else
        ContextAddress plt = 0;
#endif

        /*
         * we don't have a stack frame in a few restricted but useful cases:
         *  1) we are at a PUSH %EBP MOV %ESP %EBP or RET or ENTER instruction,
         *  2) we are the first instruction of a subroutine (this may NOT be
         *     a PUSH %EBP MOV %ESP %EBP instruction with some compilers)
         *  3) we are inside PLT entry
         */
        if (plt) {
            /* TODO: support for large code model PLT */
            if (addr - plt == 0) {
                dwn_sp = reg_sp + sizeof(ContextAddress) * 2;
            }
            else if (addr - plt < 16) {
                dwn_sp = reg_sp + sizeof(ContextAddress) * 3;
            }
            else if ((addr - plt - 16) % 16 < 11) {
                dwn_sp = reg_sp + sizeof(ContextAddress);
            }
            else {
                dwn_sp = reg_sp + sizeof(ContextAddress) * 2;
            }
            dwn_bp = reg_bp;
        }
        else {
            unsigned char code[5];

            if (context_read_mem(ctx, addr - 1, code, sizeof(code)) < 0) return -1;

            if (func_entry(code + 1) || code[1] == ENTER || code[1] == RET || code[1] == RETADD) {
                dwn_sp = reg_sp + sizeof(ContextAddress);
                dwn_bp = reg_bp;
            }
            else if (func_entry(code)) {
                dwn_sp = reg_sp + sizeof(ContextAddress) * 2;
                dwn_bp = reg_bp;
            }
            else {
                dwn_sp = reg_bp + sizeof(ContextAddress) * 2;
                if (read_stack(ctx, reg_bp, &dwn_bp, sizeof(ContextAddress)) < 0) dwn_bp = 0;
            }
        }
    }
    else {
        dwn_sp = reg_bp + sizeof(ContextAddress) * 2;
        if (read_stack(ctx, reg_bp, &dwn_bp, sizeof(ContextAddress)) < 0) dwn_bp = 0;
    }

    if (read_stack(ctx, dwn_sp - sizeof(ContextAddress), &dwn_pc, sizeof(ContextAddress)) < 0) dwn_pc = 0;

    if (dwn_bp < reg_sp) dwn_bp = 0;

    if (dwn_pc != 0 && write_reg_value(down, pc_def, dwn_pc) < 0) return -1;
    if (dwn_sp != 0 && write_reg_value(down, sp_def, dwn_sp) < 0) return -1;
    if (dwn_bp != 0 && write_reg_value(down, bp_def, dwn_bp) < 0) return -1;

    frame->fp = dwn_sp;

    return 0;
}

RegisterDefinition * get_PC_definition(Context * ctx) {
    static RegisterDefinition * reg_def = NULL;
    if (reg_def == NULL) {
        RegisterDefinition * r;
        for (r = get_reg_definitions(ctx); r->name != NULL; r++) {
            if (r->offset == offsetof(REG_SET, REG_IP)) {
                reg_def = r;
                break;
            }
        }
    }
    return reg_def;
}

unsigned char BREAK_INST[] = { 0xcc };

#else

#  error "Unknown CPU"

#endif
