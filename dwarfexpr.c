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
#include "mdep.h"
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

#define MD_READ     1
#define MD_WRITE    2
#define MD_ADDRESS  3
#define MD_FBASE    4

static int sMode;
static U1_T * sDataBuf;
static size_t sDataBufSize;
static int sDataDone;

static Context * sLocContext;
static ObjectInfo * sLocObject;
static int sLocFrame;
static U8_T sBaseAddresss;
static ContextAddress sLocIP;
static ContextAddress sLocFP;

static U8_T * sExprStack = NULL;
static unsigned sExprStackLen = 0;
static unsigned sExprStackMax = 0;

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

/*
  I386 GNU DWARF register numbers:
   0: AX
   1: CX
   2: DX
   3: BX
   4: SP
   5: BP
   6: SI
   7: DI
   8: IP
   9: Flags
  11..18: I387 ST0..ST7
  21..28: XMM0..XMM7
  29..36: MM0..MM7
  37: FCTRL
  38: FSTAT
  39: MXCSR
  40: ES
  41: CS
  42: SS
  43: DS
  44: FS
  45: GS
*/
static int get_register(unsigned rg, U8_T * value) {
#if defined(__linux__) && defined(__i386__) || \
    defined(_WRS_KERNEL) && (CPU_FAMILY==SIMNT || CPU_FAMILY==I80X86)
    if (is_top_frame(sLocContext, sLocFrame)) {
        switch (rg) {
        case 0:
            *value = sLocContext->regs.eax;
            return 0;
        case 1:
            *value = sLocContext->regs.ecx;
            return 0;
        case 2:
            *value = sLocContext->regs.edx;
            return 0;
        case 3:
            *value = sLocContext->regs.ebx;
            return 0;
        case 4:
            *value = sLocContext->regs.esp;
            return 0;
        case 5:
            *value = sLocContext->regs.ebp;
            return 0;
        case 6:
            *value = sLocContext->regs.esi;
            return 0;
        case 7:
            *value = sLocContext->regs.edi;
            return 0;
        case 8:
#ifdef _WRS_KERNEL
            *value = (unsigned)sLocContext->regs.pc;
#else            
            *value = sLocContext->regs.eip;
#endif            
            return 0;
        case 9:
            *value = sLocContext->regs.eflags;
            return 0;
        }
    }
    if (sLocIP == 0 && get_frame_info(sLocContext, sLocFrame, &sLocIP, NULL, &sLocFP) < 0) return -1;
    switch (rg){
    case 5:
        *value = sLocFP;
        return 0;
    case 8:
        *value = sLocIP;
        return 0;
    }
    trace(LOG_ALWAYS, "get_register: Unsupported DWARF register number %d", rg); 
    errno = ERR_UNSUPPORTED;
    return -1;
#else
#error "Unknown DWARF registers mapping"
#endif
}

static int set_register(unsigned rg, U8_T value) {
#if defined(__linux__) && defined(__i386__) || \
    defined(_WRS_KERNEL) && (CPU_FAMILY==SIMNT || CPU_FAMILY==I80X86)
    if (is_top_frame(sLocContext, sLocFrame)) {
        switch (rg) {
        case 0:
            sLocContext->regs.eax = (unsigned long)value;
            return 0;
        case 1:
            sLocContext->regs.ecx = (unsigned long)value;
            return 0;
        case 2:
            sLocContext->regs.edx = (unsigned long)value;
            return 0;
        case 3:
            sLocContext->regs.ebx = (unsigned long)value;
            return 0;
        case 4:
            sLocContext->regs.esp = (unsigned long)value;
            return 0;
        case 5:
            sLocContext->regs.ebp = (unsigned long)value;
            return 0;
        case 6:
            sLocContext->regs.esi = (unsigned long)value;
            return 0;
        case 7:
            sLocContext->regs.edi = (unsigned long)value;
            return 0;
        case 8:
#ifdef _WRS_KERNEL
            sLocContext->regs.pc = (void *)(unsigned long)value;
#else            
            sLocContext->regs.eip = (unsigned long)value;
#endif            
            return 0;
        case 9:
            sLocContext->regs.eflags = (unsigned long)value;
            return 0;
        }
    }
    trace(LOG_ALWAYS, "set_register: Unsupported DWARF register number %d", rg); 
    errno = ERR_UNSUPPORTED;
    return -1;

#else
#error "Unknown DWARF registers mapping"
#endif
}

static int register_access_expression(unsigned rg) {
    U8_T Data = 0;

    assert(!sDataDone);
    switch (sMode) {
    case MD_FBASE:
        if (get_register(rg, &Data) < 0) return -1;
        sExprStack[sExprStackLen++] = Data;
        return 0;
    case MD_READ:
        if (get_register(rg, &Data) < 0) return -1;
        switch (sDataBufSize) {
        case 1:
            *sDataBuf = (U1_T)Data;
            break;
        case 2:
            *(U2_T *)sDataBuf = (U2_T)Data;
            break;
        case 4:
            *(U4_T *)sDataBuf = (U4_T)Data;
            break;
        case 8:
            *(U8_T *)sDataBuf = Data;
            break;
        default:
            errno = ERR_INV_DWARF;
            return -1;
        }
        break;
    case MD_WRITE:
        switch (sDataBufSize) {
        case 1:
            Data = *sDataBuf;
            break;
        case 2:
            Data = *(U2_T *)sDataBuf;
            break;
        case 4:
            Data = *(U4_T *)sDataBuf;
            break;
        case 8:
            Data = *(U8_T *)sDataBuf;
            break;
        default:
            errno = ERR_INV_DWARF;
            return -1;
        }
        set_register(rg, Data);
        break;
    default:
        errno = ERR_INV_DWARF;
        return -1;
    }
    sDataDone = 1;
    return 0;
}

static void enter_section(CompUnit * Unit, U1_T * Data, U8_T Offset, U8_T Size) {
    dio_EnterSectionData(Unit->mFile, Data, Offset, Size);
    dio_gVersion = Unit->mVersion;
    dio_g64bit = Unit->mFile->elf64;
    dio_gAddressSize = Unit->mAddressSize;
}

static int evaluate_expression(U1_T * Buf, size_t Size);

static int evaluate_location(const LocationInfo * Loc) {
    if (Loc->mList) {
        U1_T * Addr = NULL;
        U8_T Offset = 0;
        U8_T Base = 0;
        U8_T BaseMark = 0;
        DWARFCache * Cache = (DWARFCache *)sLocObject->mCompUnit->mFile->dwarf_dt_cache;
        assert(Cache->magic == SYM_CACHE_MAGIC);
        if (Cache->mDebugLoc == NULL) {
            errno = ERR_INV_DWARF;
            return -1;
        }
        enter_section(sLocObject->mCompUnit, Loc->mAddr, 0, Loc->mSize);
        switch (Loc->mSize) {
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
        if (elf_load(Cache->mDebugLoc) < 0) return -1;
        Addr = Cache->mDebugLoc->data;
        Base = sLocObject->mCompUnit->mLowPC;
        enter_section(sLocObject->mCompUnit, Addr, Offset, Cache->mDebugLoc->size);
        while (1) {
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
                if (sLocIP == 0 && get_frame_info(sLocContext, sLocFrame, &sLocIP, NULL, &sLocFP) < 0) return -1;
                if (sLocIP >= Base + Addr0 && sLocIP < Base + Addr1) {
                    return evaluate_expression(Addr + dio_GetPos(), Size);
                }
                dio_Skip(Size);
            }
        }
        errno = ERR_INV_ADDRESS;
        return -1;
    }
    return evaluate_expression(Loc->mAddr, Loc->mSize);
}

static int evaluate_expression(U1_T * Buf, size_t Size) {
    if (Size == 0) {
        errno = ERR_INV_DWARF;
        return -1;
    }
    enter_section(sLocObject->mCompUnit, Buf, 0, Size);
    while (dio_GetPos() < Size) {
        U1_T Op = dio_ReadU1();
        U8_T Data = 0;

        if (sExprStackLen >= sExprStackMax) {
            sExprStackMax *= 2;
            sExprStack = loc_realloc(sExprStack, sizeof(U8_T) * sExprStackMax);
        }
        switch (Op) {
        case OP_addr:
            sExprStack[sExprStackLen++] = dio_ReadAddress();
            break;
        case OP_deref:
            check_e_stack(1);
            {
                U1_T Tmp[8];
                ContextAddress Addr = (ContextAddress)sExprStack[sExprStackLen - 1];
                if (context_read_mem(sLocContext, Addr, Tmp, dio_gAddressSize) < 0) return -1;
                check_breakpoints_on_memory_read(sLocContext, Addr, Tmp, dio_gAddressSize);
                switch (dio_gAddressSize)  {
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
                if (context_read_mem(sLocContext, Addr, Tmp, dio_gAddressSize) < 0) return -1;
                check_breakpoints_on_memory_read(sLocContext, Addr, Tmp, dio_gAddressSize);
                switch (dio_gAddressSize)  {
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
                if (sExprStackLen != 0 || dio_GetPos() < Size) {
                    errno = ERR_INV_DWARF;
                    return -1;
                }
                if (register_access_expression(n) < 0) return -1;
            }
            break;
        case OP_regx:
            {
                unsigned n = dio_ReadULEB128();
                if (sExprStackLen != 0 || dio_GetPos() < Size) {
                    errno = ERR_INV_DWARF;
                    return -1;
                }
                if (register_access_expression(n) < 0) return -1;
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
            if (get_register(Op - OP_breg0, sExprStack + sExprStackLen) < 0) return -1;
            sExprStack[sExprStackLen++] += dio_ReadS8LEB128();
            break;
        case OP_fbreg:
            {
                U8_T Pos = dio_GetPos();
                ObjectInfo * Obj = sLocObject;
                int Mode = sMode;
                sMode = MD_FBASE;
                sLocObject = get_parent_function(sLocObject);
                if (sLocObject == NULL || sLocObject->mFrameBase.mAddr == NULL) {
                    errno = ERR_INV_DWARF;
                    return -1;
                }
                if (evaluate_location(&sLocObject->mFrameBase) < 0) return -1;
                sLocObject = Obj;
                sMode = Mode;
                enter_section(sLocObject->mCompUnit, Buf, Pos, Size);
                assert(sExprStackLen > 0);
                sExprStack[sExprStackLen - 1] += dio_ReadS8LEB128();
            }
            break;
        case OP_bregx:
            if (get_register(dio_ReadULEB128(), sExprStack + sExprStackLen) < 0) return -1;
            sExprStack[sExprStackLen++] += dio_ReadS8LEB128();
            break;
        case OP_nop:
            break;
        case OP_push_object_address:
            sExprStack[sExprStackLen++] = sBaseAddresss;
            break;
        case OP_piece:
        case OP_deref_size:
        case OP_xderef_size:
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

static int evaluate(Context * Ctx, int Frame, ObjectInfo * Info) {
    Trap trap;
    int error = 0;

    sDataDone = 0;
    sExprStackLen = 0;
    sLocContext = Ctx;
    sLocFrame = Frame;
    sLocObject = Info;
    sLocIP = 0;
    sLocFP = 0;

    if (sExprStack == NULL) {
        sExprStackMax = 8;
        sExprStack = loc_alloc(sizeof(U8_T) * sExprStackMax);
    }
    if (Info->mTag == TAG_member) {
        sExprStack[sExprStackLen++] = sBaseAddresss;
    }
    if (set_trap(&trap)) {
        if (evaluate_location(&Info->mLocation) < 0) error = errno;
        clear_trap(&trap);
    }
    else {
        error = trap.error;
    }
    if (!error && sExprStackLen != 1) {
        error = ERR_INV_DWARF;
    }

    if (!error) {
        switch (sMode) {
        case MD_ADDRESS:
            assert(sDataDone == 0);
            assert(sDataBufSize == sizeof(U8_T));
            *(U8_T *)sDataBuf = *sExprStack;
            break;
        case MD_READ:
            if (!sDataDone) {
                ContextAddress Addr = (ContextAddress)*sExprStack;
                if (context_read_mem(sLocContext, Addr, sDataBuf, sDataBufSize) < 0) error = errno;
                if (!error) check_breakpoints_on_memory_read(sLocContext, Addr, sDataBuf, sDataBufSize);
            }
            break;
        case MD_WRITE:
            if (!sDataDone) {
                ContextAddress Addr = (ContextAddress)*sExprStack;
                check_breakpoints_on_memory_write(sLocContext, Addr, sDataBuf, sDataBufSize);
                if (context_write_mem(sLocContext, Addr, sDataBuf, sDataBufSize) < 0) error = errno;
            }
            break;
        }
    }
    
    sExprStackLen = 0;
    sLocContext = NULL;
    sLocFrame = 0;
    sLocObject = NULL;
    sLocIP = 0;
    sLocFP = 0;

    errno = error;
    return error ? -1 : 0;
}

int dwarf_expression_addr(Context * Ctx, int Frame, U8_T Base, ObjectInfo * Info, U8_T * address) {

    if (Info->mLowPC != 0) {
        *address = Info->mLowPC;
        return 0;
    }

    sMode = MD_ADDRESS;
    sDataBuf = (U1_T *)address;
    sDataBufSize = sizeof(U8_T);
    sBaseAddresss = Base;
    return evaluate(Ctx, Frame, Info);
}

int dwarf_expression_read(Context * Ctx, int Frame, ObjectInfo * Info, U1_T * Buf, size_t Size) {
    sMode = MD_READ;
    sDataBuf = Buf;
    sDataBufSize = Size;
    sBaseAddresss = 0;
    return evaluate(Ctx, Frame, Info);
}

int dwarf_expression_write(Context * Ctx, int Frame, ObjectInfo * Info, U1_T * Buf, size_t Size) {
    sMode = MD_WRITE;
    sDataBuf = Buf;
    sDataBufSize = Size;
    sBaseAddresss = 0;
    return evaluate(Ctx, Frame, Info);
}

#endif

