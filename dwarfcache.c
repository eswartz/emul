/*******************************************************************************
 * Copyright (c) 2007-2009 Wind River Systems, Inc. and others.
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
 * This module implements caching of DWARF debug information.
 *
 * Functions in this module use exceptions to report errors, see exceptions.h
 */

#include "mdep.h"
#include "config.h"

#if ENABLE_ELF

#include <assert.h>
#include <stdio.h>
#include "dwarf.h"
#include "dwarfio.h"
#include "dwarfcache.h"
#include "dwarfexpr.h"
#include "exceptions.h"
#include "myalloc.h"

#define OBJ_HASH_SIZE          (0x10000-1)

static DWARFCache * sCache;
static ELF_Section * sDebugSection;
static ObjectInfo * sObjectList;
static ObjectInfo * sObjectListTail;
static Elf_Sym ** sSymbolHash;
static unsigned sSymbolTableLen;
static CompUnit * sCompUnit;
static unsigned sCompUnitsMax;
static ObjectInfo * sParentObject;
static ObjectInfo * sPrevSibling;

static int sCloseListenerOK = 0;

unsigned calc_symbol_name_hash(char * s) {
    unsigned h = 0;
    while (*s) {
        unsigned g;
        h = (h << 4) + *s++;
        if (g = h & 0xf0000000) h ^= g >> 24;
        h &= ~g;
    }
    return h % SYM_HASH_SIZE;
}

static char * get_elf_symbol_name(unsigned n) {
    Elf_Sym * sym = sSymbolHash[n];
    U8_T Name = 0;
    if (sCache->mFile->elf64) {
        Elf64_Sym * s = (Elf64_Sym *)sym;
        Name = s->st_name;
    }
    else {
        Elf32_Sym * s = (Elf32_Sym *)sym;
        Name = s->st_name;
    }
    for (n = 0; n < sCache->mSymSectionsCnt; n++) {
        SymbolSection * tbl = sCache->mSymSections[n];
        if (sym < tbl->mSymPool) continue;
        if (sym >= tbl->mSymPool + tbl->mSymPoolSize) continue;
        assert(Name < tbl->mStrPoolSize);
        return tbl->mStrPool + Name;
    }
    return NULL;
}

static U8_T get_elf_symbol_address(Elf_Sym * x) {
    if (sCache->mFile->elf64) {
        Elf64_Sym * s = (Elf64_Sym *)x;
        switch (ELF64_ST_TYPE(s->st_info)) {
        case STT_OBJECT:
        case STT_FUNC:
            return s->st_value;
        }
    }
    else {
        Elf32_Sym * s = (Elf32_Sym *)x;
        switch (ELF32_ST_TYPE(s->st_info)) {
        case STT_OBJECT:
        case STT_FUNC:
            return s->st_value;
        }
    }
    return 0;
}

static DynamicProperty get_object_value_size(ObjectInfo * Info) {
    if (Info->mSize.mForm == 0) {
        switch (Info->mTag) {
        case TAG_pointer_type:
        case TAG_reference_type:
            Info->mSize.mForm = FORM_UDATA;
            Info->mSize.mData.mValue = Info->mCompUnit->mAddressSize;
            break;
        case TAG_subrange_type:
        case TAG_volatile_type:
        case TAG_const_type:
        case TAG_typedef:
        case TAG_formal_parameter:
        case TAG_global_variable:
        case TAG_local_variable:
        case TAG_variable:
        case TAG_constant:
        case TAG_enumerator:
            if (Info->mType != NULL) return get_object_value_size(Info->mType);
            break;
        }
    }
    return Info->mSize;
}

static CompUnit * find_comp_unit(U8_T ID) {
    unsigned i;
    CompUnit * Unit;

    for (i = 0; i < sCache->mCompUnitsCnt; i++) {
        Unit = sCache->mCompUnits[i];
        if (Unit->mID == ID) return Unit;
    }
    if (sCache->mCompUnitsCnt >= sCompUnitsMax) {
        sCompUnitsMax = sCompUnitsMax == 0 ? 16 : sCompUnitsMax * 2;
        sCache->mCompUnits = loc_realloc(sCache->mCompUnits, sizeof(CompUnit *) * sCompUnitsMax);
    }
    Unit = loc_alloc_zero(sizeof(CompUnit));
    Unit->mID = ID;
    sCache->mCompUnits[sCache->mCompUnitsCnt++] = Unit;
    return Unit;
}

static ObjectInfo * find_object_info(U8_T ID) {
    ObjectInfo * Info = find_object(sCache, ID);
    if (Info == NULL) {
        U4_T Hash = (U4_T)ID % OBJ_HASH_SIZE;
        Info = (ObjectInfo *)loc_alloc_zero(sizeof(ObjectInfo));
        Info->mHashNext = sCache->mObjectHash[Hash];
        sCache->mObjectHash[Hash] = Info;
        if (sObjectList == NULL) sObjectList = Info;
        else sObjectListTail->mListNext = Info;
        sObjectListTail = Info;
        Info->mID = ID;
    }
    return Info;
}

static void find_symbol_entry(ObjectInfo * Info) {
    U8_T Addr = Info->mLowPC;
    if (Addr != 0) {
        unsigned i = 0;
        unsigned j = sSymbolTableLen - 1;
        while (i <= j) {
            unsigned k = (i + j) / 2;
            U8_T AddrK = get_elf_symbol_address(sSymbolHash[k]);
            if (AddrK < Addr) {
                i = k + 1;
            }
            else if (AddrK > Addr) {
                j = k - 1;
            }
            else {
                unsigned n;
                while (k > i && get_elf_symbol_address(sSymbolHash[k - 1]) == Addr) k--;
                Info->mSymbol = k;
                for (n = 0; n < sCache->mSymSectionsCnt; n++) {
                    SymbolSection * tbl = sCache->mSymSections[n];
                    if (sSymbolHash[k] < tbl->mSymPool) continue;
                    if (sSymbolHash[k] >= tbl->mSymPool + tbl->mSymPoolSize) continue;
                    Info->mSymbolSection = tbl;
                    break;
                }
                break;
            }
        }
    }
}

static void set_dynamic_property(DynamicProperty * Prop, U2_T Form) {
    Prop->mForm = Form;
    switch (Form) {
    case FORM_REF       :
    case FORM_REF_ADDR  :
    case FORM_REF1      :
    case FORM_REF2      :
    case FORM_REF4      :
    case FORM_REF8      :
    case FORM_REF_UDATA :
        Prop->mData.mObj = find_object_info(dio_gFormRef);
        break;
    case FORM_DATA1     :
    case FORM_DATA2     :
    case FORM_DATA4     :
    case FORM_DATA8     :
    case FORM_SDATA     :
    case FORM_UDATA     :
    case FORM_FLAG      :
        Prop->mData.mValue = dio_gFormData;
        break;
    default:
        dio_ChkBlock(Form, &Prop->mData.mExpr.mAddr, &Prop->mData.mExpr.mSize);
        break;
    }
}

U8_T evaluate_dynamic_property(Context * ctx, int frame, ObjectInfo * obj, DynamicProperty * Prop) {
    U8_T Res = 0;
    size_t Size = 0;
    U1_T Buf[8];
    int big_endian;
    unsigned i;

    switch (Prop->mForm) {
    case FORM_REF       :
    case FORM_REF_ADDR  :
    case FORM_REF1      :
    case FORM_REF2      :
    case FORM_REF4      :
    case FORM_REF8      :
    case FORM_REF_UDATA :
        big_endian = obj->mCompUnit->mFile->big_endian;
        Size = (size_t)evaluate_dynamic_property(ctx, frame, Prop->mData.mObj, &Prop->mData.mObj->mSize);
        if (Size < 1 || Size > sizeof(Buf)) exception(ERR_INV_DATA_TYPE);
        if (dwarf_expression_read(ctx, frame, Prop->mData.mObj, Buf, (size_t)Size) < 0) {
            exception(errno);
        }
        for (i = 0; i < Size; i++) {
            Res = (Res << 8) | Buf[big_endian ? i : Size - i - 1];
        }
        break;
    case FORM_DATA1     :
    case FORM_DATA2     :
    case FORM_DATA4     :
    case FORM_DATA8     :
    case FORM_SDATA     :
    case FORM_UDATA     :
    case FORM_FLAG      :
        Res = Prop->mData.mValue;
        break;
    default:
        if (dwarf_dynamic_property_expression(ctx, frame, obj, Prop->mData.mExpr.mAddr, Prop->mData.mExpr.mSize, &Res) < 0) {
            exception(errno);
        }
        break;
    }
    return Res;
}

static void entry_callback(U2_T Tag, U2_T Attr, U2_T Form);

static void read_mod_fund_type(U2_T Form, ObjectInfo ** Type) {
    U1_T * Buf;
    U4_T BufSize;
    U4_T BufPos;
    dio_ChkBlock(Form, &Buf, &BufSize);
    *Type = find_object_info(sDebugSection->addr + dio_GetPos() - 1);
    (*Type)->mTag = TAG_lo_user;
    (*Type)->mCompUnit = sCompUnit;
    (*Type)->mEncoding = Buf[BufSize - 1];
    BufPos = BufSize - 1;
    while (BufPos > 0) {
        U2_T Tag = 0;
        ObjectInfo * Mod = NULL;
        switch (Buf[--BufPos]) {
        case MOD_volatile:
        case MOD_const:
            continue;
        case MOD_pointer_to:
            Tag = TAG_pointer_type;
            break;
        case MOD_reference_to:
            Tag = TAG_reference_type;
            break;
        }
        Mod = find_object_info(sDebugSection->addr + dio_GetPos() - BufSize + BufPos);
        Mod->mTag = Tag;
        Mod->mCompUnit = sCompUnit;
        Mod->mType = *Type;
        *Type = Mod;
    }
}

static void read_mod_user_def_type(U2_T Form, ObjectInfo ** Type) {
    U1_T * Buf;
    U4_T BufSize;
    U4_T BufPos;
    int i;
    U4_T Ref = 0;
    dio_ChkBlock(Form, &Buf, &BufSize);
    for (i = 0; i < 4; i++) {
        Ref |= (U4_T)Buf[BufSize - 4 +
            (sDebugSection->file->big_endian ? 3 - i : i)] << (i * 8);
    }
    *Type = find_object_info(sDebugSection->addr + Ref);
    BufPos = BufSize - 4;
    while (BufPos > 0) {
        U2_T Tag = 0;
        ObjectInfo * Mod = NULL;
        switch (Buf[--BufPos]) {
        case MOD_volatile:
        case MOD_const:
            continue;
        case MOD_pointer_to:
            Tag = TAG_pointer_type;
            break;
        case MOD_reference_to:
            Tag = TAG_reference_type;
            break;
        }
        Mod = find_object_info(sDebugSection->addr + dio_GetPos() - BufSize + BufPos);
        Mod->mTag = Tag;
        Mod->mCompUnit = sCompUnit;
        Mod->mType = *Type;
        *Type = Mod;
    }
}

static void read_tag_com_unit(U2_T Attr, U2_T Form) {
    static CompUnit * Unit;
    switch (Attr) {
    case 0:
        if (Form) {
            Unit = find_comp_unit(sDebugSection->addr + dio_gEntryPos);
            Unit->mFile = sCache->mFile;
            Unit->mDebugRangesOffs = ~(U8_T)0;
            Unit->mVersion = dio_gVersion;
            Unit->mAddressSize = dio_gAddressSize;
            Unit->mSection = sDebugSection;
        }
        else {
            assert(sParentObject == NULL);
            sCompUnit = Unit;
            sPrevSibling = NULL;
        }
        break;
    case AT_low_pc:
        dio_ChkAddr(Form);
        Unit->mLowPC = (ContextAddress)dio_gFormRef;
        break;
    case AT_high_pc:
        dio_ChkAddr(Form);
        Unit->mHighPC = (ContextAddress)dio_gFormRef;
        break;
    case AT_ranges:
        dio_ChkData(Form);
        Unit->mDebugRangesOffs = dio_gFormData;
        break;
    case AT_name:
        dio_ChkString(Form);
        Unit->mName = dio_gFormDataAddr;
        break;
    case AT_comp_dir:
        dio_ChkString(Form);
        Unit->mDir = dio_gFormDataAddr;
        break;
    case AT_stmt_list:
        dio_ChkData(Form);
        Unit->mLineInfoOffs = dio_gFormData;
        break;
    case AT_base_types:
        Unit->mBaseTypes = find_comp_unit(dio_gFormRef);
        break;
    }
}

static void read_object_attributes(U2_T Tag, U2_T Attr, U2_T Form) {
    static ObjectInfo * Info;
    static U8_T Sibling;

    switch (Attr) {
    case 0:
        if (Form) {
            Info = find_object_info(sDebugSection->addr + dio_gEntryPos);
            Info->mTag = Tag;
            Info->mCompUnit = sCompUnit;
            Info->mParent = sParentObject;
            /* TODO: Default AT_lower_bound value is language dependand */
            Sibling = 0;
        }
        else {
            find_symbol_entry(Info);
            if (Tag == TAG_enumerator && Info->mType == NULL) Info->mType = sParentObject;
            if (sPrevSibling != NULL) sPrevSibling->mSibling = Info;
            else if (sParentObject != NULL) sParentObject->mChildren = Info;
            else sCompUnit->mChildren = Info;
            sPrevSibling = Info;
            if (Sibling != 0) {
                U8_T SiblingPos = Sibling;
                ObjectInfo * Parent = sParentObject;
                ObjectInfo * PrevSibling = sPrevSibling;
                sParentObject = Info;
                sPrevSibling = NULL;
                while (dio_GetPos() < SiblingPos) dio_ReadEntry(entry_callback);
                sParentObject = Parent;
                sPrevSibling = PrevSibling;
            }
            if (Tag == TAG_enumeration_type && Info->mLength.mForm == 0 && Info->mChildren != NULL) {
                ObjectInfo * Obj = Info->mChildren;
                Info->mLength.mForm = FORM_UDATA;
                while (Obj != NULL) {
                    Info->mLength.mData.mValue++;
                    Obj = Obj->mSibling;
                }
            }
        }
        break;
    case AT_sibling:
        dio_ChkRef(Form);
        Sibling = dio_gFormRef - sDebugSection->addr;
        break;
    case AT_type:
        dio_ChkRef(Form);
        Info->mType = find_object_info(dio_gFormRef);
        break;
    case AT_fund_type:
        dio_ChkData(Form);
        Info->mType = find_object_info(sDebugSection->addr + dio_GetPos() - dio_gFormDataSize);
        Info->mType->mTag = TAG_lo_user;
        Info->mCompUnit = sCompUnit;
        Info->mType->mEncoding = (U2_T)dio_gFormData;
        break;
    case AT_user_def_type:
        dio_ChkRef(Form);
        Info->mType = find_object_info(dio_gFormRef);
        break;
    case AT_mod_fund_type:
        read_mod_fund_type(Form, &Info->mType);
        break;
    case AT_mod_u_d_type:
        read_mod_user_def_type(Form, &Info->mType);
        break;
    case AT_encoding:
        dio_ChkData(Form);
        Info->mEncoding = (U2_T)dio_gFormData;
        break;
    case AT_byte_size:
        set_dynamic_property(&Info->mSize, Form);
        break;
    case AT_stride_size:
        dio_ChkData(Form);
        Info->mBitStride = (U1_T)dio_gFormData;
        break;
    case AT_ordering:
        dio_ChkData(Form);
        Info->mOrdering = (U1_T)dio_gFormData;
        break;
    case AT_declaration:
        dio_ChkFlag(Form);
        Info->mDeclaration = (U1_T)dio_gFormData;
        break;
    case AT_external:
        dio_ChkFlag(Form);
        Info->mExternal = (U1_T)dio_gFormData;
        break;
    case AT_prototyped:
        if (dio_gVersion == 1) {
            Info->mPrototyped = 1;
        }
        else {
            dio_ChkFlag(Form);
            Info->mPrototyped = (U1_T)dio_gFormData;
        }
        break;
    case AT_specification_v1:
    case AT_specification_v2:
        dio_ChkRef(Form);
        Info->mSpecification = find_object_info(dio_gFormRef);
        break;
    case AT_low_pc:
        dio_ChkAddr(Form);
        Info->mLowPC = dio_gFormRef;
        break;
    case AT_high_pc:
        dio_ChkAddr(Form);
        Info->mHighPC = dio_gFormRef;
        break;
    case AT_location:
    case AT_data_member_location:
        dio_ChkBlock(Form, &Info->mLocation.mAddr, &Info->mLocation.mSize);
        Info->mLocation.mList = Form == FORM_DATA4 || Form == FORM_DATA8;
        break;
    case AT_frame_base:
        dio_ChkBlock(Form, &Info->mFrameBase.mAddr, &Info->mFrameBase.mSize);
        Info->mFrameBase.mList = Form == FORM_DATA4 || Form == FORM_DATA8;
        break;
    case AT_const_value:
        if (Form == FORM_SDATA || Form == FORM_UDATA) {
            Info->mConstValue = dio_gFormData;
            Info->mConstValueSize = sizeof(dio_gFormData);
        }
        else {
            dio_ChkBlock(Form, &Info->mConstValueAddr, &Info->mConstValueSize);
        }
        break;
    case AT_name:
        dio_ChkString(Form);
        Info->mName = dio_gFormDataAddr;
        break;
    case AT_lower_bound:
        set_dynamic_property(&Info->mLowBound, Form);
        break;
    case AT_upper_bound:
        set_dynamic_property(&Info->mUpperBound, Form);
        break;
    case AT_count:
        set_dynamic_property(&Info->mLength, Form);
        break;
    case AT_decl_file:
        dio_ChkData(Form);
        Info->mDeclFile = (U4_T)dio_gFormData;
        break;
    case AT_decl_line:
        dio_ChkData(Form);
        Info->mDeclLine = (U4_T)dio_gFormData;
        break;
    }
}

static void entry_callback(U2_T Tag, U2_T Attr, U2_T Form) {
    switch (Tag) {
    case TAG_compile_unit           :
        read_tag_com_unit(Attr, Form);
        break;
    default:
        read_object_attributes(Tag, Attr, Form);
        break;
    }
}

static int symbol_sort_func(const void * X, const void * Y) {
    U8_T AddrX = get_elf_symbol_address(*(Elf_Sym **)X);
    U8_T AddrY = get_elf_symbol_address(*(Elf_Sym **)Y);
    if (AddrX < AddrY) return -1;
    if (AddrX > AddrY) return +1;
    return 0;
}
                
static void load_symbol_tables(void) {
    unsigned idx;
    unsigned cnt = 0;
    ELF_File * File = sCache->mFile;
    unsigned sym_size = File->elf64 ? sizeof(Elf64_Sym) : sizeof(Elf32_Sym);

    for (idx = 0; idx < File->section_cnt; idx++) {
        ELF_Section * sym_sec = File->sections[idx];
        if (sym_sec == NULL) continue;
        if (sym_sec->size == 0) continue;
        if (sym_sec->type == SHT_SYMTAB) {
            unsigned i;
            ELF_Section * str_sec;
            U1_T * str_data = NULL;
            U1_T * sym_data = NULL;
            SymbolSection * tbl = (SymbolSection *)loc_alloc_zero(sizeof(SymbolSection));
            if (sCache->mSymSections == NULL) {
                sCache->mSymSectionsLen = 8;
                sCache->mSymSections = loc_alloc(sizeof(SymbolSection *) * sCache->mSymSectionsLen);
            }
            else if (sCache->mSymSectionsCnt >= sCache->mSymSectionsLen) {
                sCache->mSymSectionsLen *= 8;
                sCache->mSymSections = loc_realloc(sCache->mSymSections, sizeof(SymbolSection *) * sCache->mSymSectionsLen);
            }
            tbl->mIndex = sCache->mSymSectionsCnt++;
            sCache->mSymSections[tbl->mIndex] = tbl;
            if (sym_sec->link >= File->section_cnt || (str_sec = File->sections[sym_sec->link]) == NULL) {
                exception(EINVAL);
            }
            if (elf_load(sym_sec) < 0) exception(errno);
            if (elf_load(str_sec) < 0) exception(errno);
            sym_data = sym_sec->data;
            str_data = str_sec->data;
            tbl->mFile = File;
            tbl->mStrPool = (char *)str_data;
            tbl->mStrPoolSize = (size_t)str_sec->size;
            tbl->mSymPool = (Elf_Sym *)sym_data;
            tbl->mSymPoolSize = (size_t)sym_sec->size;
            tbl->sym_cnt = (unsigned)(sym_sec->size / sym_size);
            tbl->mHashNext = (unsigned *)loc_alloc(tbl->sym_cnt * sizeof(unsigned));
            for (i = 0; i < tbl->sym_cnt; i++) {
                U8_T Name = 0;
                if (File->elf64) {
                    Elf64_Sym * s = (Elf64_Sym *)tbl->mSymPool + i;
                    if (get_elf_symbol_address((Elf_Sym *)s) != 0) cnt++;
                    Name = s->st_name;
                }
                else {
                    Elf32_Sym * s = (Elf32_Sym *)tbl->mSymPool + i;
                    if (get_elf_symbol_address((Elf_Sym *)s) != 0) cnt++;
                    Name = s->st_name;
                }
                assert(Name < tbl->mStrPoolSize);
                if (Name == 0) {
                    tbl->mHashNext[i] = 0;
                }
                else {
                    unsigned h = calc_symbol_name_hash(tbl->mStrPool + Name);
                    tbl->mHashNext[i] = tbl->mSymbolHash[h];
                    tbl->mSymbolHash[h] = i;
                }
            }
        }
    }
    sCache->mSymbolHash = loc_alloc(sizeof(void *) * cnt);
    sCache->mSymbolTableLen = cnt;
    cnt = 0;
    for (idx = 0; idx < sCache->mSymSectionsCnt; idx++) {
        SymbolSection * tbl = sCache->mSymSections[idx];
        unsigned i;
        for (i = 0; i < tbl->sym_cnt; i++) {
            if (File->elf64) {
                Elf_Sym * s = (Elf_Sym *)((Elf64_Sym *)tbl->mSymPool + i);
                if (get_elf_symbol_address(s) != 0) sCache->mSymbolHash[cnt++] = s;
            }
            else {
                Elf_Sym * s = (Elf_Sym *)((Elf32_Sym *)tbl->mSymPool + i);
                if (get_elf_symbol_address(s) != 0) sCache->mSymbolHash[cnt++] = s;
            }
        }
    }
    assert(sCache->mSymbolTableLen == cnt);
    qsort(sCache->mSymbolHash, sCache->mSymbolTableLen, sizeof(Elf_Sym *), symbol_sort_func);
}

static void load_debug_sections(void) {
    Trap trap;
    unsigned idx;
    ELF_File * File = sCache->mFile;
    ObjectInfo * Info;

    memset(&trap, 0, sizeof(trap));
    sSymbolHash = sCache->mSymbolHash;
    sSymbolTableLen = sCache->mSymbolTableLen;
    sObjectList = NULL;
    sObjectListTail = NULL;
    sCompUnitsMax = 0;

    for (idx = 0; idx < File->section_cnt; idx++) {
        ELF_Section * sec = File->sections[idx];
        if (sec == NULL) continue;
        if (sec->size == 0) continue;
        if (sec->name == NULL) continue;
        if (strcmp(sec->name, ".debug") == 0 || strcmp(sec->name, ".debug_info") == 0) {
            sDebugSection = sec;
            sParentObject = NULL;
            sPrevSibling = NULL;
            dio_EnterSection(sec, 0);
            dio_gVersion = strcmp(sec->name, ".debug") == 0 ? 1 : 2;
            if (set_trap(&trap)) {
                while (dio_GetPos() < sec->size) dio_ReadUnit(entry_callback);
                clear_trap(&trap);
            }
            dio_ExitSection();
            sParentObject = NULL;
            sPrevSibling = NULL;
            sCompUnit = NULL;
            sDebugSection = NULL;
            if (trap.error) break;
        }
        else if (strcmp(sec->name, ".debug_ranges") == 0) {
            sCache->mDebugRanges = sec;
        }
        else if (strcmp(sec->name, ".debug_aranges") == 0) {
            sCache->mDebugARanges = sec;
        }
        else if (strcmp(sec->name, ".debug_line") == 0) {
            sCache->mDebugLine = sec;
        }
        else if (strcmp(sec->name, ".debug_loc") == 0) {
            sCache->mDebugLoc = sec;
        }
    }

    Info = sObjectList;
    while (Info != NULL) {
        if (Info->mSize.mForm == 0) Info->mSize = get_object_value_size(Info);
        if (Info->mConstValueAddr == NULL && Info->mConstValueSize > 0) {
            U8_T Size = 0;
            assert(Info->mConstValueSize == sizeof(Info->mConstValue));
            switch (Info->mSize.mForm) {
            case FORM_DATA1     :
            case FORM_DATA2     :
            case FORM_DATA4     :
            case FORM_DATA8     :
            case FORM_SDATA     :
            case FORM_UDATA     :
                Size = Info->mSize.mData.mValue;
                if (Size > 0 && Size <= Info->mConstValueSize) {
                    Info->mConstValueAddr = (U1_T *)&Info->mConstValue;
                    Info->mConstValueSize = (size_t)Size;
                    if (File->big_endian) Info->mConstValueAddr += sizeof(Info->mConstValue) - Size;
                }
            }
        }
        Info = Info->mListNext;
    }
    if (sObjectList == NULL) {
        loc_free(sCache->mObjectHash);
        sCache->mObjectHash = NULL;
    }
    sCache->mObjectList = sObjectList;
    sSymbolHash = NULL;
    sSymbolTableLen = 0;
    sObjectList = NULL;
    sObjectListTail = NULL;
    sCompUnitsMax = 0;
    if (trap.error) str_exception(trap.error, trap.msg);
}

static void free_unit_cache(CompUnit * Unit) {
    Unit->mFilesCnt = 0;
    Unit->mFilesMax = 0;
    loc_free(Unit->mFiles);
    Unit->mFiles = NULL;

    Unit->mDirsCnt = 0;
    Unit->mDirsMax = 0;
    loc_free(Unit->mDirs);
    Unit->mDirs = NULL;

    Unit->mStatesCnt = 0;
    Unit->mStatesMax = 0;
    loc_free(Unit->mStates);
    Unit->mStates = NULL;
}

static void free_dwarf_cache(ELF_File * File) {
    DWARFCache * Cache = (DWARFCache *)File->dwarf_dt_cache;
    if (Cache != NULL) {
        unsigned i;
        assert(Cache->magic == SYM_CACHE_MAGIC);
        Cache->magic = 0;
        for (i = 0; i < Cache->mCompUnitsCnt; i++) {
            CompUnit * Unit = Cache->mCompUnits[i];
            free_unit_cache(Unit);
            loc_free(Unit);
        }
        loc_free(Cache->mCompUnits);
        for (i = 0; i < Cache->mSymSectionsCnt; i++) {
            SymbolSection * tbl = Cache->mSymSections[i];
            loc_free(tbl->mHashNext);
            loc_free(tbl);
        }
        while (Cache->mObjectList != NULL) {
            ObjectInfo * Info = Cache->mObjectList;
            Cache->mObjectList = Info->mListNext;
            loc_free(Info);
        }
        loc_free(Cache->mObjectHash);
        loc_free(Cache->mSymbolHash);
        loc_free(Cache);
        File->dwarf_dt_cache = NULL;
    }
}

DWARFCache * get_dwarf_cache(ELF_File * File) {
    DWARFCache * Cache = (DWARFCache *)File->dwarf_dt_cache;
    if (Cache == NULL) {
        Trap trap;
        if (!sCloseListenerOK) {
            elf_add_close_listener(free_dwarf_cache);
            sCloseListenerOK = 1;
        }
        sCache = Cache = (DWARFCache *)(File->dwarf_dt_cache = loc_alloc_zero(sizeof(DWARFCache)));
        sCache->magic = SYM_CACHE_MAGIC;
        sCache->mFile = File;
        sCache->mObjectHash = loc_alloc_zero(sizeof(ObjectInfo *) * OBJ_HASH_SIZE);
        if (set_trap(&trap)) {
            dio_LoadAbbrevTable(File);
            load_symbol_tables();
            load_debug_sections();
            clear_trap(&trap);
        }
        else {
            sCache->mErrorCode = trap.error;
            strncpy(sCache->mErrorMsg, trap.msg, sizeof(sCache->mErrorMsg));
        }
        sCache = NULL;
    }
    if (Cache->mErrorCode) str_exception(Cache->mErrorCode, Cache->mErrorMsg);
    return Cache;
}

static void add_dir(CompUnit * Unit, char * Name) {
    if (Unit->mDirsCnt >= Unit->mDirsMax) {
        Unit->mDirsMax = Unit->mDirsMax == 0 ? 16 : Unit->mDirsMax * 2;
        Unit->mDirs = (char **)loc_realloc(Unit->mDirs, sizeof(char *) * Unit->mDirsMax);
    }
    Unit->mDirs[Unit->mDirsCnt++] = Name;
}

static void add_file(CompUnit * Unit, FileInfo * File) {
    if (Unit->mFilesCnt >= Unit->mFilesMax) {
        Unit->mFilesMax = Unit->mFilesMax == 0 ? 16 : Unit->mFilesMax * 2;
        Unit->mFiles = (FileInfo *)loc_realloc(Unit->mFiles, sizeof(FileInfo) * Unit->mFilesMax);
    }
    if (File->mDir == NULL) File->mDir = Unit->mDir;
    Unit->mFiles[Unit->mFilesCnt++] = *File;
}

static void add_state(CompUnit * Unit, LineNumbersState * state) {
    if (Unit->mStatesCnt >= Unit->mStatesMax) {
        Unit->mStatesMax = Unit->mStatesMax == 0 ? 128 : Unit->mStatesMax * 2;
        Unit->mStates = (LineNumbersState *)loc_realloc(Unit->mStates, sizeof(LineNumbersState) * Unit->mStatesMax);
    }
    Unit->mStates[Unit->mStatesCnt++] = *state;
}

void load_line_numbers(DWARFCache * Cache, CompUnit * Unit) {
    Trap trap;
    if (Unit->mFiles != NULL && Unit->mDirs != NULL) return;
    dio_EnterSection(Cache->mDebugLine, Unit->mLineInfoOffs);
    dio_gVersion = Unit->mVersion;
    dio_g64bit = Unit->mFile->elf64;
    dio_gAddressSize = Unit->mAddressSize;
    dio_gUnitPos = Unit->mLineInfoOffs;
    if (set_trap(&trap)) {
        U8_T header_pos = 0;
        U1_T opcode_base = 0;
        U1_T opcode_size[256];
        U8_T header_size = 0;
        U1_T min_instruction_length = 0;
        U1_T is_stmt_default = 0;
        I1_T line_base = 0;
        U1_T line_range = 0;
        U8_T unit_size = 0;
        LineNumbersState state;
        
        /* Read header */
        unit_size = dio_ReadU4();
        if (unit_size == 0xffffffffu) {
            unit_size = dio_ReadU8();
            unit_size += 12;
        }
        else {
            unit_size += 4;
        }
        dio_ReadU2(); /* line info version */
        header_size = Cache->mFile->elf64 ? dio_ReadU8() : (U8_T)dio_ReadU4();
        header_pos = dio_GetPos();
        min_instruction_length = dio_ReadU1();
        is_stmt_default = dio_ReadU1() != 0;
        line_base = (I1_T)dio_ReadU1();
        line_range = dio_ReadU1();
        opcode_base = dio_ReadU1();
        memset(opcode_size, 0, sizeof(opcode_size));
        dio_Read(opcode_size + 1, opcode_base - 1);

        /* Read directory names */
        for (;;) {
            char * Name = dio_ReadString();
            if (Name == NULL) break;
            add_dir(Unit, Name);
        }

        /* Read source sFileLocks info */
        for (;;) {
            U4_T dir = 0;
            FileInfo File;
            memset(&File, 0, sizeof(File));
            File.mName = dio_ReadString();
            if (File.mName == NULL) break;
            dir = dio_ReadULEB128();
            if (dir > 0 && dir <= Unit->mDirsCnt) File.mDir = Unit->mDirs[dir - 1];
            File.mModTime = dio_ReadULEB128();
            File.mSize = dio_ReadULEB128();
            add_file(Unit, &File);
        }

        /* Run the program */
        if (header_pos + header_size != dio_GetPos())
            str_exception(ERR_INV_DWARF, "Invalid line info header");
        memset(&state, 0, sizeof(state));
        state.mFile = 1;
        state.mLine = 1;
        if (is_stmt_default) state.mFlags |= LINE_IsStmt;
        while (dio_GetPos() < dio_gUnitPos + unit_size) {
            U1_T opcode = dio_ReadU1();
            if (opcode >= opcode_base) {
                state.mLine += (unsigned)((int)((opcode - opcode_base) % line_range) + line_base);
                state.mAddress += (opcode - opcode_base) / line_range * min_instruction_length;
                add_state(Unit, &state);
                state.mFlags &= ~(LINE_BasicBlock | LINE_PrologueEnd | LINE_EpilogueBegin);
            }
            else if (opcode == 0) {
                U4_T op_size = dio_ReadULEB128();
                U8_T op_pos = dio_GetPos();
                switch (dio_ReadU1()) {
                case DW_LNE_define_file: {
                    U4_T dir = 0;
                    FileInfo File;
                    memset(&File, 0, sizeof(File));
                    File.mName = dio_ReadString();
                    dir = dio_ReadULEB128();
                    if (dir > 0 && dir <= Unit->mDirsCnt) File.mDir = Unit->mDirs[dir - 1];
                    File.mModTime = dio_ReadULEB128();
                    File.mSize = dio_ReadULEB128();
                    add_file(Unit, &File);
                    break;
                }
                case DW_LNE_end_sequence:
                    state.mFlags |= LINE_EndSequence;
                    add_state(Unit, &state);
                    memset(&state, 0, sizeof(state));
                    state.mFile = 1;
                    state.mLine = 1;
                    if (is_stmt_default) state.mFlags |= LINE_IsStmt;
                    else state.mFlags &= ~LINE_IsStmt;
                    break;
                case DW_LNE_set_address:
                    state.mAddress = (ContextAddress)dio_ReadAddress();
                    break;
                default:
                    dio_Skip(op_size - 1);
                    break;
                }
                assert(dio_GetPos() == op_pos + op_size);
            }
            else {
                switch (opcode) {
                case DW_LNS_copy:
                    add_state(Unit, &state);
                    state.mFlags &= ~(LINE_BasicBlock | LINE_PrologueEnd | LINE_EpilogueBegin);
                    break;
                case DW_LNS_advance_pc:
                    state.mAddress += (ContextAddress)(dio_ReadU8LEB128() * min_instruction_length);
                    break;
                case DW_LNS_advance_line:
                    state.mLine += dio_ReadSLEB128();
                    break;
                case DW_LNS_set_file:
                    state.mFile = dio_ReadULEB128();
                    break;
                case DW_LNS_set_column:
                    state.mColumn = dio_ReadULEB128();
                    break;
                case DW_LNS_negate_stmt:
                    state.mFlags ^= LINE_IsStmt;
                    break;
                case DW_LNS_set_basic_block:
                    state.mFlags |= LINE_BasicBlock;
                    break;
                case DW_LNS_const_add_pc:
                    state.mAddress += (255 - opcode_base) / line_range * min_instruction_length;
                    break;
                case DW_LNS_fixed_advance_pc:
                    state.mAddress += dio_ReadU2();
                    break;
                case DW_LNS_set_prologue_end:
                    state.mFlags |= LINE_PrologueEnd;
                    break;
                case DW_LNS_set_epilogue_begin:
                    state.mFlags |= LINE_EpilogueBegin;
                    break;
                case DW_LNS_set_isa:
                    state.mISA = (U1_T)dio_ReadULEB128();
                    break;
                default:
                    str_exception(ERR_INV_DWARF, "Invalid line info op code");
                    break;
                }
            }
        }
        dio_ExitSection();
        clear_trap(&trap);
    }
    else {
        dio_ExitSection();
        free_unit_cache(Unit);
        str_exception(trap.error, trap.msg);
    }
}

ObjectInfo * find_object(DWARFCache * Cache, U8_T ID) {
    U4_T Hash = (U4_T)ID % OBJ_HASH_SIZE;
    ObjectInfo * Info = Cache->mObjectHash[Hash];

    while (Info != NULL) {
        if (Info->mID == ID) return Info;
        Info = Info->mHashNext;
    }
    return NULL;
}

#endif
