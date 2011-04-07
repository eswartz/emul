/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
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

#include <config.h>

#if ENABLE_ELF

#include <assert.h>
#include <stdio.h>
#include <framework/myalloc.h>
#include <framework/exceptions.h>
#include <framework/errors.h>
#include <framework/trace.h>
#include <services/dwarf.h>
#include <services/dwarfio.h>
#include <services/dwarfexpr.h>
#include <services/stacktrace.h>

static U8_T * sExprStack = NULL;
static unsigned sExprStackLen = 0;
static unsigned sExprStackMax = 0;

#define check_e_stack(n) { if (sExprStackLen < n) str_exception(ERR_INV_DWARF, "invalid DWARF expression stack"); }

static StackFrame * get_stack_frame(PropertyValue * Value) {
    StackFrame * Info = NULL;
    if (Value->mFrame == STACK_NO_FRAME) return NULL;
    if (get_frame_info(Value->mContext, Value->mFrame, &Info) < 0) exception(errno);
    return Info;
}

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

static U8_T read_memory(PropertyValue * Value, U8_T Addr, size_t Size) {
    size_t i;
    U8_T n = 0;
    U1_T buf[8];

    if (context_read_mem(Value->mContext, Addr, buf, Size) < 0) exception(errno);
    for (i = 0; i < Size; i++) {
        n = (n << 8) | buf[Value->mBigEndian ? i : Size - i - 1];
    }
    return n;
}

static void evaluate_expression(U8_T BaseAddress, PropertyValue * Value, ELF_Section * Section, U1_T * Buf, size_t Size) {
    U8_T StartPos = Buf - (U1_T *)Section->data;
    CompUnit * Unit = Value->mObject->mCompUnit;
    if (Size == 0) str_exception(ERR_INV_DWARF, "DWARF expression size = 0");
    dio_EnterSection(&Unit->mDesc, Section, StartPos);
    while (dio_GetPos() - StartPos < Size) {
        U1_T Op = dio_ReadU1();
        U8_T Data = 0;

        if (sExprStackLen >= sExprStackMax) {
            sExprStackMax *= 2;
            sExprStack = (U8_T *)loc_realloc(sExprStack, sizeof(U8_T) * sExprStackMax);
        }
        switch (Op) {
        case OP_addr:
            {
                ELF_Section * section = NULL;
                Data = dio_ReadAddress(&section);
                sExprStack[sExprStackLen] = elf_map_to_run_time_address(
                    Value->mContext, Unit->mFile, section, (ContextAddress)Data);
                if (sExprStack[sExprStackLen] == 0) str_exception(ERR_INV_ADDRESS, "Object has no RT address");
                sExprStackLen++;
            }
            break;
        case OP_deref:
            check_e_stack(1);
            {
                U8_T Addr = sExprStack[sExprStackLen - 1];
                size_t Size = Unit->mDesc.mAddressSize;
                sExprStack[sExprStackLen - 1] = read_memory(Value, Addr, Size);
            }
            break;
        case OP_deref_size:
            check_e_stack(1);
            {
                U8_T Addr = sExprStack[sExprStackLen - 1];
                U1_T Size = dio_ReadU1();
                sExprStack[sExprStackLen - 1] = read_memory(Value, Addr, Size);
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
                U8_T Addr = sExprStack[sExprStackLen - 1];
                size_t Size = Unit->mDesc.mAddressSize;
                sExprStack[sExprStackLen - 2] = read_memory(Value, Addr, Size);
                sExprStackLen--;
            }
            break;
        case OP_xderef_size:
            check_e_stack(2);
            {
                U8_T Addr = sExprStack[sExprStackLen - 1];
                U1_T Size = dio_ReadU1();
                sExprStack[sExprStackLen - 2] = read_memory(Value, Addr, Size);
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
                RegisterDefinition * def = NULL;
                if (dio_GetPos() - StartPos < Size) str_exception(ERR_INV_DWARF, "OP_reg must be last instruction");
                def = get_reg_by_id(Value->mContext, n, &Unit->mRegIdScope);
                if (def == NULL) exception(errno);
                Value->mSize = def->size;
                Value->mBigEndian = def->big_endian;
                Value->mRegister = def;
            }
            break;
        case OP_regx:
            {
                unsigned n = dio_ReadULEB128();
                RegisterDefinition * def = NULL;
                if (dio_GetPos() - StartPos < Size) str_exception(ERR_INV_DWARF, "OP_regx must be last instruction");
                def = get_reg_by_id(Value->mContext, n, &Unit->mRegIdScope);
                if (def == NULL) exception(errno);
                Value->mSize = def->size;
                Value->mBigEndian = def->big_endian;
                Value->mRegister = def;
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
            {
                RegisterDefinition * def = get_reg_by_id(Value->mContext, Op - OP_breg0, &Value->mObject->mCompUnit->mRegIdScope);
                if (def == NULL) str_exception(errno, "Cannot evaluate DWARF expression");
                if (read_reg_value(get_stack_frame(Value), def, sExprStack + sExprStackLen) < 0) exception(errno);
                sExprStack[sExprStackLen++] += dio_ReadS8LEB128();
            }
            break;
        case OP_fbreg:
            {
                U8_T Pos = dio_GetPos();
                PropertyValue FP;
                ObjectInfo * Parent = get_parent_function(Value->mObject);

                if (Parent == NULL) str_exception(ERR_INV_DWARF, "OP_fbreg: no parent function");
                memset(&FP, 0, sizeof(FP));
                read_and_evaluate_dwarf_object_property(Value->mContext, Value->mFrame, 0, Parent, AT_frame_base, &FP);

                dio_EnterSection(&Unit->mDesc, Section, Pos);
                sExprStack[sExprStackLen++] = get_numeric_property_value(&FP);
                assert(sExprStackLen > 0);
                sExprStack[sExprStackLen - 1] += dio_ReadS8LEB128();
            }
            break;
        case OP_call_frame_cfa:
            {
                StackFrame * frame = get_stack_frame(Value);
                if (frame == NULL) str_exception(ERR_INV_ADDRESS, "Stack frame address not available");
                sExprStack[sExprStackLen++] = frame->fp;
            }
            break;
        case OP_bregx:
            {
                RegisterDefinition * def = get_reg_by_id(Value->mContext, dio_ReadULEB128(), &Value->mObject->mCompUnit->mRegIdScope);
                if (def == NULL) str_exception(errno, "Cannot evaluate DWARF expression");
                if (read_reg_value(get_stack_frame(Value), def, sExprStack + sExprStackLen) < 0) exception(errno);
                sExprStack[sExprStackLen++] += dio_ReadS8LEB128();
            }
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
            str_exception(ERR_UNSUPPORTED, "Unsupported DWARF expression op");
        }
    }
    dio_ExitSection();
}

static void evaluate_location(U8_T BaseAddresss, PropertyValue * Value) {
    U8_T IP = 0;
    U8_T Offset = 0;
    U8_T Base = 0;
    CompUnit * Unit = Value->mObject->mCompUnit;
    DWARFCache * Cache = (DWARFCache *)Unit->mFile->dwarf_dt_cache;
    U8_T AddrMax = ~(U8_T)0;

    assert(Cache->magic == DWARF_CACHE_MAGIC);
    if (Cache->mDebugLoc == NULL) str_exception(ERR_INV_DWARF, "Missing .debug_loc section");
    dio_EnterSection(&Unit->mDesc, Unit->mDesc.mSection, Value->mAddr - (U1_T *)Unit->mDesc.mSection->data);
    Offset = dio_ReadUX(Value->mSize);
    dio_ExitSection();
    Base = Unit->mLowPC;
    if (Unit->mDesc.mAddressSize < 8) AddrMax = ((U8_T)1 << Unit->mDesc.mAddressSize * 8) - 1;
    if (read_reg_value(get_stack_frame(Value), get_PC_definition(Value->mContext), &IP) < 0) exception(errno);
    dio_EnterSection(&Unit->mDesc, Cache->mDebugLoc, Offset);
    for (;;) {
        ELF_Section * S0 = NULL;
        ELF_Section * S1 = NULL;
        U8_T Addr0 = dio_ReadAddress(&S0);
        U8_T Addr1 = dio_ReadAddress(&S1);
        if (Addr0 == AddrMax) {
            Base = Addr1;
        }
        else if (Addr0 == 0 && Addr1 == 0) {
            break;
        }
        else if (S0 != S1 || Addr0 > Addr1) {
            str_exception(ERR_INV_DWARF, "Invalid .debug_loc section");
        }
        else {
            U2_T Size = dio_ReadU2();
            U8_T RTAddr0 = elf_map_to_run_time_address(Value->mContext, Unit->mFile, S0, (ContextAddress)(Base + Addr0));
            U8_T RTAddr1 = Addr1 - Addr0 + RTAddr0;
            if (RTAddr0 != 0 && IP >= RTAddr0 && IP < RTAddr1) {
                U1_T * Buf = dio_GetDataPtr();
                dio_ExitSection();
                evaluate_expression(BaseAddresss, Value, Cache->mDebugLoc, Buf, Size);
                return;
            }
            dio_Skip(Size);
        }
    }
    dio_ExitSection();
    str_exception(ERR_OTHER, "Object is not available at this location in the code");
}

void dwarf_evaluate_expression(U8_T BaseAddress, PropertyValue * Value) {

    if (Value->mAttr != AT_frame_base) sExprStackLen = 0;

    if (sExprStack == NULL) {
        sExprStackMax = 8;
        sExprStack = (U8_T *)loc_alloc(sizeof(U8_T) * sExprStackMax);
    }
    if (Value->mAttr == AT_data_member_location) {
        sExprStack[sExprStackLen++] = BaseAddress;
    }
    if (Value->mRegister != NULL || Value->mAddr == NULL || Value->mSize == 0) {
        str_exception(ERR_INV_DWARF, "invalid DWARF expression reference");
    }
    if (Value->mForm == FORM_DATA4 || Value->mForm == FORM_DATA8) {
        if (Value->mFrame == STACK_NO_FRAME) str_exception(ERR_INV_CONTEXT, "need stack frame");
        evaluate_location(BaseAddress, Value);
    }
    else {
        evaluate_expression(BaseAddress, Value, Value->mObject->mCompUnit->mDesc.mSection, Value->mAddr, Value->mSize);
    }
    if (Value->mAttr != AT_frame_base && sExprStackLen != (Value->mRegister == NULL ? 1u : 0u)) {
        str_exception(ERR_INV_DWARF, "invalid DWARF expression stack");
    }

    if (Value->mRegister == NULL) {
        assert(sExprStackLen > 0);
        Value->mValue = sExprStack[--sExprStackLen];
        Value->mSize = 0;
    }
    Value->mAddr = NULL;

    if (Value->mAttr != AT_frame_base) sExprStackLen = 0;
}

#endif /* ENABLE_ELF */
