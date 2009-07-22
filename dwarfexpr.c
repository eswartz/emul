/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
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
 * This module implements DWARF expressions evaluation.
 */

#include "config.h"

#if ENABLE_ELF

#include <assert.h>
#include <stdio.h>
#include "myalloc.h"
#include "dwarf.h"
#include "dwarfio.h"
#include "dwarfexpr.h"
#include "stacktrace.h"
#include "breakpoints.h"
#include "exceptions.h"
#include "errors.h"
#include "trace.h"

static U8_T * sExprStack = NULL;
static unsigned sExprStackLen = 0;
static unsigned sExprStackMax = 0;
static int sKeepStack = 0;

#define check_e_stack(n) if (sExprStackLen < n) { errno = ERR_INV_DWARF; return -1; }

static ObjectInfo * get_parent_function(ObjectInfo * Info) {
    while (Info != NULL) {
        switch (Info->mTag) {
        case TAG_global_subroutine:
        case TAG_subroutine:
        case TAG_subprogram:
        case TAG_entry_point:
            return Info;
        }
        Info = Info->mParent;
    }
    return NULL;
}

static int get_register(Context * Ctx, int Frame, unsigned rg, U8_T * value) {
#if defined(__linux__) && defined(__i386__) || \
    defined(_WRS_KERNEL) && (CPU_FAMILY==SIMNT || CPU_FAMILY==I80X86)
    ContextAddress IP, FP;
    if (is_top_frame(Ctx, Frame)) {
        switch (rg) {
        case 0:
            *value = Ctx->regs.eax;
            return 0;
        case 1:
            *value = Ctx->regs.ecx;
            return 0;
        case 2:
            *value = Ctx->regs.edx;
            return 0;
        case 3:
            *value = Ctx->regs.ebx;
            return 0;
        case 4:
            *value = Ctx->regs.esp;
            return 0;
        case 5:
            *value = Ctx->regs.ebp;
            return 0;
        case 6:
            *value = Ctx->regs.esi;
            return 0;
        case 7:
            *value = Ctx->regs.edi;
            return 0;
        case 8:
#ifdef _WRS_KERNEL
            *value = (unsigned)Ctx->regs.pc;
#else
            *value = Ctx->regs.eip;
#endif
            return 0;
        case 9:
            *value = Ctx->regs.eflags;
            return 0;
        }
    }
    if (get_frame_info(Ctx, Frame, &IP, NULL, &FP) < 0) return -1;
    switch (rg) {
    case 5:
        *value = FP;
        return 0;
    case 8:
        *value = IP;
        return 0;
    }
#elif defined(__linux__) && defined(__x86_64__)
    ContextAddress RA, FP;
    if (is_top_frame(Ctx, Frame)) {
        switch (rg) {
        case 0:
            *value = Ctx->regs.rax;
            return 0;
        case 1:
            *value = Ctx->regs.rbx;
            return 0;
        case 2:
            *value = Ctx->regs.rcx;
            return 0;
        case 3:
            *value = Ctx->regs.rdx;
            return 0;
        case 4:
            *value = Ctx->regs.rsi;
            return 0;
        case 5:
            *value = Ctx->regs.rdi;
            return 0;
        case 6:
            *value = Ctx->regs.rbp;
            return 0;
        case 7:
            *value = Ctx->regs.rsp;
            return 0;
        case 8:
            *value = Ctx->regs.r8;
            return 0;
        case 9:
            *value = Ctx->regs.r9;
            return 0;
        case 10:
            *value = Ctx->regs.r10;
            return 0;
        case 11:
            *value = Ctx->regs.r11;
            return 0;
        case 12:
            *value = Ctx->regs.r11;
            return 0;
        case 13:
            *value = Ctx->regs.r13;
            return 0;
        case 14:
            *value = Ctx->regs.r14;
            return 0;
        case 15:
            *value = Ctx->regs.r15;
            return 0;
        case 16:
            if (get_frame_info(Ctx, Frame, NULL, &RA, NULL) < 0) return -1;
            *value = RA;
            return 0;
        }
    }
    if (get_frame_info(Ctx, Frame, NULL, &RA, &FP) < 0) return -1;
    switch (rg) {
    case 6:
        *value = FP;
        return 0;
    case 16:
        *value = RA;
        return 0;
    }
#elif defined(__APPLE__) && defined(__i386__)
    ContextAddress IP, FP;
    if (is_top_frame(Ctx, Frame)) {
        switch (rg) {
        case 0:
            *value = Ctx->regs.__eax;
            return 0;
        case 1:
            *value = Ctx->regs.__ecx;
            return 0;
        case 2:
            *value = Ctx->regs.__edx;
            return 0;
        case 3:
            *value = Ctx->regs.__ebx;
            return 0;
        case 4:
            *value = Ctx->regs.__esp;
            return 0;
        case 5:
            *value = Ctx->regs.__ebp;
            return 0;
        case 6:
            *value = Ctx->regs.__esi;
            return 0;
        case 7:
            *value = Ctx->regs.__edi;
            return 0;
        case 8:
            *value = Ctx->regs.__eip;
            return 0;
        case 9:
            *value = Ctx->regs.__eflags;
            return 0;
        }
    }
    if (get_frame_info(Ctx, Frame, &IP, NULL, &FP) < 0) return -1;
    switch (rg) {
    case 5:
        *value = FP;
        return 0;
    case 8:
        *value = IP;
        return 0;
    }
#else
#error "Unknown DWARF registers mapping"
#endif
    trace(LOG_ALWAYS, "get_register: Unsupported DWARF register number %d", rg);
    errno = ERR_UNSUPPORTED;
    return -1;
}

static int set_register(Context * Ctx, int Frame, unsigned rg, U8_T value) {
#if defined(__linux__) && defined(__i386__) || \
   defined(_WRS_KERNEL) && (CPU_FAMILY==SIMNT || CPU_FAMILY==I80X86)
    if (is_top_frame(Ctx, Frame)) {
        switch (rg) {
        case 0:
            Ctx->regs.eax = (unsigned long)value;
            return 0;
        case 1:
            Ctx->regs.ecx = (unsigned long)value;
            return 0;
        case 2:
            Ctx->regs.edx = (unsigned long)value;
            return 0;
        case 3:
            Ctx->regs.ebx = (unsigned long)value;
            return 0;
        case 4:
            Ctx->regs.esp = (unsigned long)value;
            return 0;
        case 5:
            Ctx->regs.ebp = (unsigned long)value;
            return 0;
        case 6:
            Ctx->regs.esi = (unsigned long)value;
            return 0;
        case 7:
            Ctx->regs.edi = (unsigned long)value;
            return 0;
        case 8:
#ifdef _WRS_KERNEL
            Ctx->regs.pc = (void *)(unsigned long)value;
#else
            Ctx->regs.eip = (unsigned long)value;
#endif
            return 0;
        case 9:
            Ctx->regs.eflags = (unsigned long)value;
            return 0;
        }
    }
#elif defined(__linux__) && defined(__x86_64__)
    if (is_top_frame(Ctx, Frame)) {
        switch (rg) {
        case 0:
            Ctx->regs.rax = (unsigned long)value;
            return 0;
        case 1:
            Ctx->regs.rbx = (unsigned long)value;
            return 0;
        case 2:
            Ctx->regs.rcx = (unsigned long)value;
            return 0;
        case 3:
            Ctx->regs.rdx = (unsigned long)value;
            return 0;
        case 4:
            Ctx->regs.rsi = (unsigned long)value;
            return 0;
        case 5:
            Ctx->regs.rdi = (unsigned long)value;
            return 0;
        case 6:
            Ctx->regs.rbp = (unsigned long)value;
            return 0;
        case 7:
            Ctx->regs.rsp = (unsigned long)value;
            return 0;
        case 8:
            Ctx->regs.r8 = (unsigned long)value;
            return 0;
        case 9:
            Ctx->regs.r9 = (unsigned long)value;
            return 0;
        case 10:
            Ctx->regs.r10 = (unsigned long)value;
            return 0;
        case 11:
            Ctx->regs.r11 = (unsigned long)value;
            return 0;
        case 12:
            Ctx->regs.r12 = (unsigned long)value;
            return 0;
        case 13:
            Ctx->regs.r13 = (unsigned long)value;
            return 0;
        case 14:
            Ctx->regs.r14 = (unsigned long)value;
            return 0;
        case 15:
            Ctx->regs.r15 = (unsigned long)value;
            return 0;
        }
    }
#elif defined(__APPLE__) && defined(__i386__)
    if (is_top_frame(Ctx, Frame)) {
        switch (rg) {
        case 0:
            Ctx->regs.__eax = (unsigned long)value;
            return 0;
        case 1:
            Ctx->regs.__ecx = (unsigned long)value;
            return 0;
        case 2:
            Ctx->regs.__edx = (unsigned long)value;
            return 0;
        case 3:
            Ctx->regs.__ebx = (unsigned long)value;
            return 0;
        case 4:
            Ctx->regs.__esp = (unsigned long)value;
            return 0;
        case 5:
            Ctx->regs.__ebp = (unsigned long)value;
            return 0;
        case 6:
            Ctx->regs.__esi = (unsigned long)value;
            return 0;
        case 7:
            Ctx->regs.__edi = (unsigned long)value;
            return 0;
        case 8:
            Ctx->regs.__eip = (unsigned long)value;
            return 0;
        case 9:
            Ctx->regs.__eflags = (unsigned long)value;
            return 0;
        }
    }
#else
#error "Unknown DWARF registers mapping"
#endif
    trace(LOG_ALWAYS, "set_register: Unsupported DWARF register number %d", rg);
    errno = ERR_UNSUPPORTED;
    return -1;
}

static int register_access_func(PropertyValue * Value, int write, U8_T * Data) {
    if (write) return set_register(Value->mContext, Value->mFrame, (unsigned)Value->mValue, *Data);
    return get_register(Value->mContext, Value->mFrame, (unsigned)Value->mValue, Data);
}

static int evaluate_expression(U8_T BaseAddress, PropertyValue * Value, U1_T * Buf, size_t Size) {
    if (Size == 0) {
        errno = ERR_INV_DWARF;
        return -1;
    }
    dio_EnterDataSection(&Value->mObject->mCompUnit->mDesc, Buf, 0, Size);
    while (dio_GetPos() < Size) {
        U1_T Op = dio_ReadU1();
        U8_T Data = 0;

        if (sExprStackLen >= sExprStackMax) {
            sExprStackMax *= 2;
            sExprStack = loc_realloc(sExprStack, sizeof(U8_T) * sExprStackMax);
        }
        switch (Op) {
        case OP_addr:
            if ((sExprStack[sExprStackLen++] = elf_map_to_run_time_address(
                    Value->mContext, Value->mObject->mCompUnit->mFile, dio_ReadAddress())) == 0) {
                errno = ERR_INV_DWARF;
                return -1;
            }
            break;
        case OP_deref:
            check_e_stack(1);
            {
                U1_T Tmp[8];
                ContextAddress Addr = (ContextAddress)sExprStack[sExprStackLen - 1];
                size_t Size = Value->mObject->mCompUnit->mDesc.mAddressSize;
                if (context_read_mem(Value->mContext, Addr, Tmp, Size) < 0) return -1;
                check_breakpoints_on_memory_read(Value->mContext, Addr, Tmp, Size);
                switch (Size)  {
                case 1: Data = *Tmp; break;
                case 2: Data = *(U2_T *)Tmp; break;
                case 4: Data = *(U4_T *)Tmp; break;
                case 8: Data = *(U8_T *)Tmp; break;
                default: assert(0);
                }
                sExprStack[sExprStackLen - 1] = Data;
            }
            break;
        case OP_deref_size:
            check_e_stack(1);
            {
                U1_T Tmp[8];
                ContextAddress Addr = (ContextAddress)sExprStack[sExprStackLen - 1];
                U1_T Size = dio_ReadU1();
                if (context_read_mem(Value->mContext, Addr, Tmp, Size) < 0) return -1;
                check_breakpoints_on_memory_read(Value->mContext, Addr, Tmp, Size);
                switch (Size)  {
                case 1: Data = *Tmp; break;
                case 2: Data = *(U2_T *)Tmp; break;
                case 4: Data = *(U4_T *)Tmp; break;
                case 8: Data = *(U8_T *)Tmp; break;
                default: assert(0);
                }
                sExprStack[sExprStackLen - 1] = Data;
            }
            break;
        case OP_const1u:
            sExprStack[sExprStackLen++] = dio_ReadU1();
            break;
        case OP_const1s:
            sExprStack[sExprStackLen++] = (I1_T)dio_ReadU1();
            break;
        case OP_const2u:
            sExprStack[sExprStackLen++] = dio_ReadU2();
            break;
        case OP_const2s:
            sExprStack[sExprStackLen++] = (I2_T)dio_ReadU2();
            break;
        case OP_const4u:
            sExprStack[sExprStackLen++] = dio_ReadU4();
            break;
        case OP_const4s:
            sExprStack[sExprStackLen++] = (I4_T)dio_ReadU4();
            break;
        case OP_const8u:
            sExprStack[sExprStackLen++] = dio_ReadU8();
            break;
        case OP_const8s:
            sExprStack[sExprStackLen++] = (I8_T)dio_ReadU8();
            break;
        case OP_constu:
            sExprStack[sExprStackLen++] = dio_ReadU8LEB128();
            break;
        case OP_consts:
            sExprStack[sExprStackLen++] = dio_ReadS8LEB128();
            break;
        case OP_dup:
            check_e_stack(1);
            sExprStack[sExprStackLen] = sExprStack[sExprStackLen - 1];
            sExprStackLen++;
            break;
        case OP_drop:
            check_e_stack(1);
            sExprStackLen--;
            break;
        case OP_over:
            check_e_stack(2);
            sExprStack[sExprStackLen] = sExprStack[sExprStackLen - 2];
            sExprStackLen++;
            break;
        case OP_pick:
            {
                unsigned n = dio_ReadU1();
                check_e_stack(n + 1);
                sExprStack[sExprStackLen] = sExprStack[sExprStackLen - n - 1];
                sExprStackLen++;
            }
            break;
        case OP_swap:
            check_e_stack(2);
            Data = sExprStack[sExprStackLen - 1];
            sExprStack[sExprStackLen - 1] = sExprStack[sExprStackLen - 2];
            sExprStack[sExprStackLen - 2] = Data;
            break;
        case OP_rot:
            check_e_stack(3);
            Data = sExprStack[sExprStackLen - 1];
            sExprStack[sExprStackLen - 1] = sExprStack[sExprStackLen - 2];
            sExprStack[sExprStackLen - 2] = sExprStack[sExprStackLen - 3];
            sExprStack[sExprStackLen - 3] = Data;
            break;
        case OP_xderef:
            check_e_stack(2);
            {
                U1_T Tmp[8];
                ContextAddress Addr = (ContextAddress)sExprStack[sExprStackLen - 1];
                size_t Size = Value->mObject->mCompUnit->mDesc.mAddressSize;
                if (context_read_mem(Value->mContext, Addr, Tmp, Size) < 0) return -1;
                check_breakpoints_on_memory_read(Value->mContext, Addr, Tmp, Size);
                switch (Size)  {
                case 1: Data = *Tmp; break;
                case 2: Data = *(U2_T *)Tmp; break;
                case 4: Data = *(U4_T *)Tmp; break;
                case 8: Data = *(U8_T *)Tmp; break;
                default: assert(0);
                }
                sExprStack[sExprStackLen - 2] = Data;
                sExprStackLen--;
            }
            break;
        case OP_xderef_size:
            check_e_stack(2);
            {
                U1_T Tmp[8];
                ContextAddress Addr = (ContextAddress)sExprStack[sExprStackLen - 1];
                U1_T Size = dio_ReadU1();
                if (context_read_mem(Value->mContext, Addr, Tmp, Size) < 0) return -1;
                check_breakpoints_on_memory_read(Value->mContext, Addr, Tmp, Size);
                switch (Size)  {
                case 1: Data = *Tmp; break;
                case 2: Data = *(U2_T *)Tmp; break;
                case 4: Data = *(U4_T *)Tmp; break;
                case 8: Data = *(U8_T *)Tmp; break;
                default: assert(0);
                }
                sExprStack[sExprStackLen - 2] = Data;
                sExprStackLen--;
            }
            break;
        case OP_abs:
            check_e_stack(1);
            if ((I8_T)sExprStack[sExprStackLen - 1] < 0) {
                sExprStack[sExprStackLen - 1] = ~sExprStack[sExprStackLen - 1] + 1;
            }
            break;
        case OP_and:
            check_e_stack(2);
            sExprStackLen--;
            sExprStack[sExprStackLen - 1] = sExprStack[sExprStackLen - 1] & sExprStack[sExprStackLen];
            break;
        case OP_div:
            check_e_stack(2);
            sExprStackLen--;
            sExprStack[sExprStackLen - 1] /= sExprStack[sExprStackLen];
            break;
        case OP_minus:
            check_e_stack(2);
            sExprStackLen--;
            sExprStack[sExprStackLen - 1] -= sExprStack[sExprStackLen];
            break;
        case OP_mod:
            check_e_stack(2);
            sExprStackLen--;
            sExprStack[sExprStackLen - 1] %= sExprStack[sExprStackLen];
            break;
        case OP_mul:
            check_e_stack(2);
            sExprStackLen--;
            sExprStack[sExprStackLen - 1] *= sExprStack[sExprStackLen];
            break;
        case OP_neg:
            check_e_stack(1);
            sExprStack[sExprStackLen - 1] = ~sExprStack[sExprStackLen - 1] + 1;
            break;
        case OP_not:
            check_e_stack(1);
            sExprStack[sExprStackLen - 1] = ~sExprStack[sExprStackLen - 1];
            break;
        case OP_or:
            check_e_stack(2);
            sExprStackLen--;
            sExprStack[sExprStackLen - 1] = sExprStack[sExprStackLen - 1] | sExprStack[sExprStackLen];
            break;
        case OP_plus:
            check_e_stack(2);
            sExprStackLen--;
            sExprStack[sExprStackLen - 1] += sExprStack[sExprStackLen];
            break;
        case OP_plus_uconst:
            check_e_stack(1);
            sExprStack[sExprStackLen - 1] += dio_ReadU8LEB128();
            break;
        case OP_shl:
            check_e_stack(2);
            sExprStackLen--;
            sExprStack[sExprStackLen - 1] <<= sExprStack[sExprStackLen];
            break;
        case OP_shr:
            check_e_stack(2);
            sExprStackLen--;
            sExprStack[sExprStackLen - 1] >>= sExprStack[sExprStackLen];
            break;
        case OP_shra:
            {
                U8_T Cnt;
                check_e_stack(2);
                Data = sExprStack[sExprStackLen - 2];
                Cnt = sExprStack[sExprStackLen - 1];
                while (Cnt > 0) {
                    int s = (Data & ((U8_T)1 << 63)) != 0;
                    Data >>= 1;
                    if (s) Data |= (U8_T)1 << 63;
                    Cnt--;
                }
                sExprStack[sExprStackLen - 2] = Data;
                sExprStackLen--;
            }
            break;
        case OP_xor:
            check_e_stack(2);
            sExprStackLen--;
            sExprStack[sExprStackLen - 1] = sExprStack[sExprStackLen - 1] ^ sExprStack[sExprStackLen];
            break;
        case OP_bra:
            {
                I2_T Offs = (I2_T)dio_ReadU2();
                check_e_stack(1);
                if (sExprStack[sExprStackLen - 1]) dio_Skip(Offs);
                sExprStackLen--;
            }
            break;
        case OP_eq:
            check_e_stack(2);
            sExprStackLen--;
            sExprStack[sExprStackLen - 1] = sExprStack[sExprStackLen - 1] == sExprStack[sExprStackLen];
            break;
        case OP_ge:
            check_e_stack(2);
            sExprStackLen--;
            sExprStack[sExprStackLen - 1] = sExprStack[sExprStackLen - 1] >= sExprStack[sExprStackLen];
            break;
        case OP_gt:
            check_e_stack(2);
            sExprStackLen--;
            sExprStack[sExprStackLen - 1] = sExprStack[sExprStackLen - 1] > sExprStack[sExprStackLen];
            break;
        case OP_le:
            check_e_stack(2);
            sExprStackLen--;
            sExprStack[sExprStackLen - 1] = sExprStack[sExprStackLen - 1] <= sExprStack[sExprStackLen];
            break;
        case OP_lt:
            check_e_stack(2);
            sExprStackLen--;
            sExprStack[sExprStackLen - 1] = sExprStack[sExprStackLen - 1] < sExprStack[sExprStackLen];
            break;
        case OP_ne:
            check_e_stack(2);
            sExprStackLen--;
            sExprStack[sExprStackLen - 1] = sExprStack[sExprStackLen - 1] != sExprStack[sExprStackLen];
            break;
        case OP_skip:
            dio_Skip((I2_T)dio_ReadU2());
            break;
        case OP_lit0:
        case OP_lit1:
        case OP_lit2:
        case OP_lit3:
        case OP_lit4:
        case OP_lit5:
        case OP_lit6:
        case OP_lit7:
        case OP_lit8:
        case OP_lit9:
        case OP_lit10:
        case OP_lit11:
        case OP_lit12:
        case OP_lit13:
        case OP_lit14:
        case OP_lit15:
        case OP_lit16:
        case OP_lit17:
        case OP_lit18:
        case OP_lit19:
        case OP_lit20:
        case OP_lit21:
        case OP_lit22:
        case OP_lit23:
        case OP_lit24:
        case OP_lit25:
        case OP_lit26:
        case OP_lit27:
        case OP_lit28:
        case OP_lit29:
        case OP_lit30:
        case OP_lit31:
            sExprStack[sExprStackLen++] = Op - OP_lit0;
            break;
        case OP_reg0:
        case OP_reg1:
        case OP_reg2:
        case OP_reg3:
        case OP_reg4:
        case OP_reg5:
        case OP_reg6:
        case OP_reg7:
        case OP_reg8:
        case OP_reg9:
        case OP_reg10:
        case OP_reg11:
        case OP_reg12:
        case OP_reg13:
        case OP_reg14:
        case OP_reg15:
        case OP_reg16:
        case OP_reg17:
        case OP_reg18:
        case OP_reg19:
        case OP_reg20:
        case OP_reg21:
        case OP_reg22:
        case OP_reg23:
        case OP_reg24:
        case OP_reg25:
        case OP_reg26:
        case OP_reg27:
        case OP_reg28:
        case OP_reg29:
        case OP_reg30:
        case OP_reg31:
            {
                unsigned n = Op - OP_reg0;
                if (dio_GetPos() < Size) {
                    errno = ERR_INV_DWARF;
                    return -1;
                }
                Value->mValue = n;
                Value->mAccessFunc = register_access_func;
            }
            break;
        case OP_regx:
            {
                unsigned n = dio_ReadULEB128();
                if (dio_GetPos() < Size) {
                    errno = ERR_INV_DWARF;
                    return -1;
                }
                Value->mValue = n;
                Value->mAccessFunc = register_access_func;
            }
            break;
        case OP_breg0:
        case OP_breg1:
        case OP_breg2:
        case OP_breg3:
        case OP_breg4:
        case OP_breg5:
        case OP_breg6:
        case OP_breg7:
        case OP_breg8:
        case OP_breg9:
        case OP_breg10:
        case OP_breg11:
        case OP_breg12:
        case OP_breg13:
        case OP_breg14:
        case OP_breg15:
        case OP_breg16:
        case OP_breg17:
        case OP_breg18:
        case OP_breg19:
        case OP_breg20:
        case OP_breg21:
        case OP_breg22:
        case OP_breg23:
        case OP_breg24:
        case OP_breg25:
        case OP_breg26:
        case OP_breg27:
        case OP_breg28:
        case OP_breg29:
        case OP_breg30:
        case OP_breg31:
            if (get_register(Value->mContext, Value->mFrame, Op - OP_breg0, sExprStack + sExprStackLen) < 0) return -1;
            sExprStack[sExprStackLen++] += dio_ReadS8LEB128();
            break;
        case OP_fbreg:
            {
                U8_T Pos = dio_GetPos();
                PropertyValue FP;
                ObjectInfo * Parent = get_parent_function(Value->mObject);
                int error = 0;

                sKeepStack++;
                if (Parent == NULL) error = ERR_INV_DWARF;
                if (!error && read_and_evaluate_dwarf_object_property(Value->mContext, Value->mFrame, 0, Parent, AT_frame_base, &FP) < 0) error = errno;
                sKeepStack--;

                dio_EnterDataSection(&Value->mObject->mCompUnit->mDesc, Buf, Pos, Size);
                if (error) {
                    errno = error;
                    return -1;
                }
                if (FP.mAccessFunc != NULL) {
                    if (FP.mAccessFunc(&FP, 0, sExprStack + sExprStackLen++) < 0) return -1;
                }
                else {
                    sExprStack[sExprStackLen++] = get_numeric_property_value(&FP);
                }
                assert(sExprStackLen > 0);
                sExprStack[sExprStackLen - 1] += dio_ReadS8LEB128();
            }
            break;
        case OP_bregx:
            if (get_register(Value->mContext, Value->mFrame, dio_ReadULEB128(), sExprStack + sExprStackLen) < 0) return -1;
            sExprStack[sExprStackLen++] += dio_ReadS8LEB128();
            break;
        case OP_nop:
            break;
        case OP_push_object_address:
            sExprStack[sExprStackLen++] = BaseAddress;
            break;
        case OP_piece:
        case OP_call2:
        case OP_call4:
        case OP_call_ref:
        case OP_bit_piece:
        case OP_reg:
        case OP_basereg:
        case OP_const:
        case OP_deref2:
        case OP_add:
        default:
            trace(LOG_ALWAYS, "Unsupported DWARF expression op 0x%02x", Op);
            errno = ERR_UNSUPPORTED;
            return -1;
        }
    }
    dio_ExitSection();
    return 0;
}

static int evaluate_location(U8_T BaseAddresss, PropertyValue * Value) {
    U1_T * Addr = NULL;
    U8_T Offset = 0;
    U8_T Base = 0;
    U8_T BaseMark = 0;
    ContextAddress IP = 0;
    DWARFCache * Cache = (DWARFCache *)Value->mObject->mCompUnit->mFile->dwarf_dt_cache;

    assert(Cache->magic == SYM_CACHE_MAGIC);
    if (Cache->mDebugLoc == NULL) {
        errno = ERR_INV_DWARF;
        return -1;
    }
    if (elf_load(Cache->mDebugLoc) < 0) return -1;
    if (get_frame_info(Value->mContext, Value->mFrame, &IP, NULL, NULL) < 0) return -1;
    dio_EnterDataSection(&Value->mObject->mCompUnit->mDesc, Value->mAddr, 0, Value->mSize);
    switch (Value->mSize) {
    case 4:
        Offset = dio_ReadU4();
        BaseMark = ~(U4_T)0;
        break;
    case 8:
        Offset = dio_ReadU8();
        BaseMark = ~(U8_T)0;
        break;
    default:
        errno = ERR_INV_DWARF;
        return -1;
    }
    Addr = Cache->mDebugLoc->data;
    Base = Value->mObject->mCompUnit->mLowPC;
    dio_EnterDataSection(&Value->mObject->mCompUnit->mDesc, Addr, Offset, Cache->mDebugLoc->size);
    for (;;) {
        U8_T Addr0 = dio_ReadAddress();
        U8_T Addr1 = dio_ReadAddress();
        if (Addr0 == BaseMark) {
            Base = Addr1;
        }
        else if (Addr0 == 0 && Addr1 == 0) {
            break;
        }
        else {
            U2_T Size = dio_ReadU2();
            ContextAddress RTAddr0 = elf_map_to_run_time_address(Value->mContext, Cache->mFile, Base + Addr0);
            ContextAddress RTAddr1 = Addr1 - Addr0 + RTAddr0;
            if (RTAddr0 != 0 && IP >= RTAddr0 && IP < RTAddr1) {
                return evaluate_expression(BaseAddresss, Value, Addr + dio_GetPos(), Size);
            }
            dio_Skip(Size);
        }
    }
    dio_ExitSection();
    errno = ERR_INV_ADDRESS;
    return -1;
}

int dwarf_evaluate_expression(U8_T BaseAddress, PropertyValue * Value) {
    Trap trap;
    int error = 0;

    assert(sKeepStack >= 0);
    if (!sKeepStack) sExprStackLen = 0;

    if (sExprStack == NULL) {
        sExprStackMax = 8;
        sExprStack = loc_alloc(sizeof(U8_T) * sExprStackMax);
    }
    if (Value->mAttr == AT_data_member_location) {
        sExprStack[sExprStackLen++] = BaseAddress;
    }
    if (set_trap(&trap)) {
        if (Value->mAccessFunc != NULL || Value->mAddr == NULL || Value->mSize == 0) {
            error = ERR_INV_DWARF;
        }
        else if (Value->mForm == FORM_DATA4 || Value->mForm == FORM_DATA8) {
            if (evaluate_location(BaseAddress, Value) < 0) error = errno;
        }
        else {
            if (evaluate_expression(BaseAddress, Value, Value->mAddr, Value->mSize) < 0) error = errno;
        }
        clear_trap(&trap);
    }
    else {
        error = trap.error;
    }

    if (!error && !sKeepStack && sExprStackLen != (Value->mAccessFunc == NULL ? 1 : 0)) error = ERR_INV_DWARF;

    if (!error) {
        if (Value->mAccessFunc == NULL) {
            assert(sExprStackLen > 0);
            Value->mValue = sExprStack[--sExprStackLen];
        }
        Value->mAddr = NULL;
        Value->mSize = 0;
    }

    if (!sKeepStack) sExprStackLen = 0;

    errno = error;
    return error ? -1 : 0;
}

#endif /* ENABLE_ELF */


