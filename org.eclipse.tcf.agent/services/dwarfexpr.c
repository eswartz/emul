/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
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

/*
 * This module implements DWARF expressions evaluation.
 */

#include <config.h>

#if ENABLE_ELF && ENABLE_DebugContext

#include <assert.h>
#include <stdio.h>
#include <framework/events.h>
#include <framework/myalloc.h>
#include <framework/exceptions.h>
#include <framework/errors.h>
#include <framework/trace.h>
#include <services/dwarf.h>
#include <services/dwarfio.h>
#include <services/dwarfexpr.h>
#include <services/stacktrace.h>
#include <services/vm.h>

typedef struct ValuePieces {
    U4_T mCnt;
    U4_T mMax;
    PropertyValuePiece * mArray;
    struct ValuePieces * mNext;
} ValuePieces;

static VMState sState;
static ELF_Section * sSection = NULL;
static U8_T sSectionOffs = 0;
static PropertyValue * sValue = NULL;
static ValuePieces * sValuePieces = NULL;

static ValuePieces * sFreeValuePieces = NULL;
static ValuePieces * sBusyValuePieces = NULL;
static unsigned sValuePiecesCnt = 0;
static int sValuePiecesPosted = 0;

static void free_value_pieces(void * args) {
    while (sBusyValuePieces != NULL) {
        ValuePieces * Pieces = sBusyValuePieces;
        sBusyValuePieces = Pieces->mNext;
        Pieces->mNext = sFreeValuePieces;
        sFreeValuePieces = Pieces;
    }
    while (sFreeValuePieces != NULL && sValuePiecesCnt > 16) {
        ValuePieces * Pieces = sFreeValuePieces;
        sFreeValuePieces = sFreeValuePieces->mNext;
        sValuePiecesCnt--;
        loc_free(Pieces->mArray);
        loc_free(Pieces);
    }
}

static ValuePieces * alloc_value_pieces(void) {
    ValuePieces * Pieces = sFreeValuePieces;
    if (Pieces == NULL) {
        Pieces = (ValuePieces *)loc_alloc_zero(sizeof(ValuePieces));
        sValuePiecesCnt++;
    }
    else {
        sFreeValuePieces = sFreeValuePieces->mNext;
    }
    Pieces->mNext = sBusyValuePieces;
    sBusyValuePieces = Pieces;
    if (!sValuePiecesPosted && sValuePiecesCnt > 8) {
        post_event(free_value_pieces, NULL);
        sValuePiecesPosted = 1;
    }
    Pieces->mCnt = 0;
    return Pieces;
}

static StackFrame * get_stack_frame(PropertyValue * sValue) {
    StackFrame * Info = NULL;
    if (sValue->mFrame == STACK_NO_FRAME) return NULL;
    if (get_frame_info(sValue->mContext, sValue->mFrame, &Info) < 0) exception(errno);
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

static U8_T read_address(void) {
    U8_T addr = 0;
    ELF_Section * section = NULL;
    CompUnit * Unit = sValue->mObject->mCompUnit;

    addr = dio_ReadAddress(&section);
    addr = elf_map_to_run_time_address(sState.ctx, Unit->mFile, section, (ContextAddress)addr);
    if (addr == 0) str_exception(ERR_INV_ADDRESS, "Object has no RT address");
    return addr;
}

static U8_T get_fbreg(void) {
    PropertyValue FP;
    CompUnit * Unit = sValue->mObject->mCompUnit;
    ObjectInfo * Parent = get_parent_function(sValue->mObject);
    U8_T addr = 0;

    if (Parent == NULL) str_exception(ERR_INV_DWARF, "OP_fbreg: no parent function");
    memset(&FP, 0, sizeof(FP));

    {
        PropertyValue * OrgValue = sValue;
        ValuePieces * OrgValuePieces = sValuePieces;
        ELF_Section * OrgSection = sSection;
        U8_T OrgSectionOffs = sSectionOffs;
        VMState OrgState = sState;

        read_and_evaluate_dwarf_object_property(sState.ctx, sState.stack_frame, 0, Parent, AT_frame_base, &FP);

        assert(sState.ctx == OrgState.ctx);
        assert(sState.addr_size == OrgState.addr_size);
        assert(sState.big_endian == OrgState.big_endian);

        sState.code = OrgState.code;
        sState.code_pos = OrgState.code_pos;
        sState.code_len = OrgState.code_len;
        sState.object_address = OrgState.object_address;
        sSectionOffs = OrgSectionOffs;
        sSection = OrgSection;
        sValuePieces = OrgValuePieces;
        sValue = OrgValue;
    }

    if (FP.mRegister != NULL) {
        if (read_reg_value(get_stack_frame(&FP), FP.mRegister, &addr) < 0) exception(errno);
    }
    else {
        addr = get_numeric_property_value(&FP);
    }
    dio_EnterSection(&Unit->mDesc, sSection, sSectionOffs + sState.code_pos);
    return addr + dio_ReadS8LEB128();
}

static void client_op(uint8_t op) {
    dio_SetPos(sSectionOffs + sState.code_pos);
    switch (op) {
    case OP_addr:
        sState.stk[sState.stk_pos++] = read_address();
        break;
    case OP_fbreg:
        if (sState.stack_frame == STACK_NO_FRAME) str_exception(ERR_INV_CONTEXT, "Invalid stack frame");
        sState.stk[sState.stk_pos++] = get_fbreg();
        break;
    default:
        trace(LOG_ALWAYS, "Unsupported DWARF expression op 0x%02x", op);
        str_exception(ERR_UNSUPPORTED, "Unsupported DWARF expression op");
    }
    sState.code_pos = (size_t)(dio_GetPos() - sSectionOffs);
}

static void evaluate_expression(ELF_Section * Section, U1_T * Buf, size_t Size) {
    int error = 0;
    CompUnit * Unit = sValue->mObject->mCompUnit;

    sState.code = Buf;
    sState.code_len = Size;
    sState.code_pos = 0;
    sSection = Section;
    sSectionOffs = Buf - (U1_T *)Section->data;
    dio_EnterSection(&Unit->mDesc, sSection, sSectionOffs);
    if (evaluate_vm_expression(&sState) < 0) error = errno;
    if (!error && sState.piece_bits) {
        sValuePieces = alloc_value_pieces();
        while (!error) {
            PropertyValuePiece * Piece;
            if (sValuePieces->mCnt >= sValuePieces->mMax) {
                sValuePieces->mMax += 8;
                sValuePieces->mArray = (PropertyValuePiece *)loc_realloc(sValuePieces->mArray,
                    sizeof(PropertyValuePiece) * sValuePieces->mMax);
            }
            Piece = sValuePieces->mArray + sValuePieces->mCnt++;
            memset(Piece, 0, sizeof(PropertyValuePiece));
            if (sState.reg) {
                Piece->mRegister = sState.reg;
                Piece->mBigEndian = sState.reg->big_endian;
            }
            else {
                Piece->mAddress = sState.stk[--sState.stk_pos];
                Piece->mBigEndian = sState.big_endian;
            }
            Piece->mBitOffset = sState.piece_offs;
            Piece->mBitSize = sState.piece_bits;
            if (sState.code_pos >= sState.code_len) break;
            if (evaluate_vm_expression(&sState) < 0) error = errno;
        }
    }
    dio_ExitSection();
    assert(error || sState.code_pos == sState.code_len);
    if (error) exception(error);
}

static void evaluate_location(void) {
    U8_T IP = 0;
    U8_T Offset = 0;
    U8_T Base = 0;
    CompUnit * Unit = sValue->mObject->mCompUnit;
    DWARFCache * Cache = (DWARFCache *)Unit->mFile->dwarf_dt_cache;
    U8_T AddrMax = ~(U8_T)0;

    assert(Cache->magic == DWARF_CACHE_MAGIC);
    if (Cache->mDebugLoc == NULL) str_exception(ERR_INV_DWARF, "Missing .debug_loc section");
    dio_EnterSection(&Unit->mDesc, Unit->mDesc.mSection, sValue->mAddr - (U1_T *)Unit->mDesc.mSection->data);
    Offset = dio_ReadUX(sValue->mSize);
    dio_ExitSection();
    Base = Unit->mLowPC;
    if (Unit->mDesc.mAddressSize < 8) AddrMax = ((U8_T)1 << Unit->mDesc.mAddressSize * 8) - 1;
    if (read_reg_value(get_stack_frame(sValue), get_PC_definition(sValue->mContext), &IP) < 0) exception(errno);
    dio_EnterSection(&Unit->mDesc, Cache->mDebugLoc, Offset);
    for (;;) {
        ELF_Section * S0 = NULL;
        ELF_Section * S1 = NULL;
        U8_T Addr0 = dio_ReadAddress(&S0);
        U8_T Addr1 = dio_ReadAddress(&S1);
        if (S0 == NULL) S0 = Unit->mTextSection;
        if (S1 == NULL) S1 = Unit->mTextSection;
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
            U8_T RTAddr0 = elf_map_to_run_time_address(sValue->mContext, Unit->mFile, S0, (ContextAddress)(Base + Addr0));
            U8_T RTAddr1 = Addr1 - Addr0 + RTAddr0;
            if (RTAddr0 != 0 && IP >= RTAddr0 && IP < RTAddr1) {
                U1_T * Buf = dio_GetDataPtr();
                dio_ExitSection();
                evaluate_expression(Cache->mDebugLoc, Buf, Size);
                return;
            }
            dio_Skip(Size);
        }
    }
    dio_ExitSection();
    str_exception(ERR_OTHER, "Object is not available at this location in the code");
}

void dwarf_evaluate_expression(U8_T BaseAddress, PropertyValue * v) {
    unsigned stk_pos = 0;
    CompUnit * Unit = v->mObject->mCompUnit;

    sValue = v;
    sValuePieces = NULL;
    sState.ctx = sValue->mContext;
    sState.addr_size = Unit->mDesc.mAddressSize;
    sState.big_endian = Unit->mFile->big_endian;
    sState.stack_frame = sValue->mFrame;
    sState.reg_id_scope = Unit->mRegIdScope;
    sState.object_address = BaseAddress;
    sState.client_op = client_op;;

    if (sValue->mAttr != AT_frame_base) sState.stk_pos = 0;
    stk_pos = sState.stk_pos;

    if (sValue->mAttr == AT_data_member_location) {
        if (sState.stk_pos >= sState.stk_max) {
            sState.stk_max += 8;
            sState.stk = (U8_T *)loc_alloc(sizeof(U8_T) * sState.stk_max);
        }
        sState.stk[sState.stk_pos++] = BaseAddress;
    }
    if (sValue->mRegister != NULL || sValue->mAddr == NULL || sValue->mSize == 0) {
        str_exception(ERR_INV_DWARF, "invalid DWARF expression reference");
    }
    if (sValue->mForm == FORM_DATA4 || sValue->mForm == FORM_DATA8) {
        if (sValue->mFrame == STACK_NO_FRAME) str_exception(ERR_INV_CONTEXT, "need stack frame");
        evaluate_location();
    }
    else {
        evaluate_expression(Unit->mDesc.mSection, sValue->mAddr, sValue->mSize);
    }

    sValue->mAddr = NULL;
    sValue->mValue = 0;
    sValue->mSize = 0;
    sValue->mBigEndian = sState.big_endian;
    sValue->mRegister = NULL;
    sValue->mPieces = NULL;
    sValue->mPieceCnt = 0;

    if (sValuePieces) {
        sValue->mPieces = sValuePieces->mArray;
        sValue->mPieceCnt = sValuePieces->mCnt;
    }
    else if (sState.reg) {
        sValue->mSize = sState.reg->size;
        sValue->mBigEndian = sState.reg->big_endian;
        sValue->mRegister = sState.reg;
    }
    else {
        sValue->mValue = sState.stk[--sState.stk_pos];
    }

    if (sState.stk_pos != stk_pos) {
        str_exception(ERR_INV_DWARF, "Invalid DWARF expression stack");
    }
}

#endif /* ENABLE_ELF && ENABLE_DebugContext */
