/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
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

#if ((SERVICE_LineNumbers) || (SERVICE_Symbols)) && !defined(WIN32)

#include <assert.h>
#include <stdio.h>
#include "dwarf.h"
#include "dwarfio.h"
#include "dwarfcache.h"
#include "exceptions.h"
#include "myalloc.h"
#include "symbols.h"

static DWARFCache * sCache;
static ELF_Section * sDebugSection;
static ObjectInfo ** sObjectHash;
static ObjectInfo * sObjectList;
static ObjectInfo * sObjectListTail;
static Elf_Sym ** sSymbolHash;
static unsigned sSymbolTableLen;
static CompUnit * sCompUnit;
static unsigned sCompUnitsMax;
static ObjectInfo * sParentObject;
static ObjectInfo * sPrevSibling;

typedef struct ExprValue {
    int mBase;
    U8_T mOffset;
} ExprValue;

static ExprValue * sExprStack = NULL;
static unsigned sExprStackLen = 0;
static unsigned sExprStackMax = 0;

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
    for (n = 0; n < sCache->sym_sections_cnt; n++) {
        SymbolSection * tbl = sCache->sym_sections[n];
        if (sym < tbl->mSymPool) continue;
        if (sym >= tbl->mSymPool + tbl->mSymPoolSize) continue;
        assert(Name < tbl->mStrPoolSize);
        return tbl->mStrPool + Name;
    }
    return NULL;
}

static U8_T get_symbol_address(Elf_Sym * x) {
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

static Elf_Sym * find_symbol_entry(U8_T Addr) {
    if (Addr != 0) {
        unsigned i = 0;
        unsigned j = sSymbolTableLen - 1;
        while (i <= j) {
            unsigned k = (i + j) / 2;
            U8_T AddrK = get_symbol_address(sSymbolHash[k]);
            if (AddrK < Addr) {
                i = k + 1;
            }
            else if (AddrK > Addr) {
                j = k - 1;
            }
            else {
                while (k > i && get_symbol_address(sSymbolHash[k - 1]) == Addr) k--;
                return sSymbolHash[k];
            }
        }
    }
    return NULL;
}

static ObjectInfo * find_object_info(U8_T ID) {
    U4_T Hash = (U4_T)ID % TYPE_HASH_SIZE;
    ObjectInfo * Info = sObjectHash[Hash];
    while (Info != NULL) {
        if (Info->mID == ID) return Info;
        Info = Info->mHashNext;
    }
    Info = (ObjectInfo *)loc_alloc_zero(sizeof(ObjectInfo));
    Info->mHashNext = sObjectHash[Hash];
    sObjectHash[Hash] = Info;
    if (sObjectList == NULL) sObjectList = Info;
    else sObjectListTail->mListNext = Info;
    sObjectListTail = Info;
    Info->mID = ID;
    return Info;
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

static I8_T read_SLEB128(U1_T * Buf, U4_T * Pos) {
    U8_T Res = 0;
    int i = 0;
    for (;; i += 7) {
        U1_T n = Buf[(*Pos)++];
        Res |= (n & 0x7Fu) << i;
        if ((n & 0x80) == 0) {
            Res |= -(n & 0x40) << i;
            break;
        }
    }
    return (I8_T)Res;
}

static U8_T read_Address(U1_T * Buf, U4_T * Pos) {
    U8_T Res = dio_ReadAddrBuf(Buf + *Pos);
    *Pos += dio_gAddressSize;
    return Res;
}

static int read_location(U1_T * Buf, U4_T Size, U2_T * Base, U8_T * Offset) {
    U4_T Pos = 0;
    sExprStackLen = 0;
    while (Pos < Size) {
        if (sExprStackLen >= sExprStackMax) {
            sExprStackMax = sExprStackMax == 0 ? 8 : sExprStackMax * 2;
            sExprStack = loc_realloc(sExprStack, sizeof(ExprValue) * sExprStackMax);
        }
        switch (Buf[Pos++]) {
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
            sExprStack[sExprStackLen].mBase = SYM_BASE_ABS;
            sExprStack[sExprStackLen].mOffset = Buf[Pos - 1] - OP_lit0;
            sExprStackLen++;
            break;
        case OP_addr:
            sExprStack[sExprStackLen].mBase = SYM_BASE_ABS;
            sExprStack[sExprStackLen].mOffset = read_Address(Buf, &Pos);
            sExprStackLen++;
            break;
        case OP_fbreg:
            sExprStack[sExprStackLen].mBase = SYM_BASE_FP;
            sExprStack[sExprStackLen].mOffset = read_SLEB128(Buf, &Pos);
            sExprStackLen++;
            break;
        default:
            return 0;
        }
    }
    assert(sExprStackLen == 1);
    *Base = sExprStack->mBase;
    *Offset = sExprStack->mOffset;
    return 1;
}

static U8_T get_object_value_size(ObjectInfo * Info) {
    if (Info->mSize != 0) return Info->mSize;
    switch (Info->mTag) {
    case TAG_pointer_type:
    case TAG_reference_type:
        return Info->mCompUnit->mAddressSize;
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
    return 0;
}

static void read_tag_com_unit(U2_T Attr, U2_T Form) {
    static CompUnit * Unit;
    switch (Attr) {
    case 0:
        if (Form) {
            if (sCache->mCompUnitsCnt >= sCompUnitsMax) {
                sCompUnitsMax = sCompUnitsMax == 0 ? 16 : sCompUnitsMax * 2;
                sCache->mCompUnits = loc_realloc(sCache->mCompUnits, sizeof(CompUnit *) * sCompUnitsMax);
            }
            Unit = sCache->mCompUnits[sCache->mCompUnitsCnt++] = loc_alloc_zero(sizeof(CompUnit));
            Unit->mFile = sCache->mFile;
            Unit->mDebugRangesOffs = ~(U8_T)0;
            Unit->mVersion = dio_gVersion;
            Unit->mAddressSize = dio_gAddressSize;
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
    }
}

static void read_object_attributes(U2_T Tag, U2_T Attr, U2_T Form) {
    static ObjectInfo * Info;
    static U8_T Sibling;
    static U8_T HighPC;
    static size_t LocationSize;
    static U1_T * LocationBuf;

    switch (Attr) {
    case 0:
        if (Form) {
            Info = find_object_info(sDebugSection->addr + dio_gEntryPos);
            Info->mTag = Tag;
            Info->mCompUnit = sCompUnit;
            Sibling = 0;
            HighPC = 0;
            LocationSize = 0;
            LocationBuf = NULL;
        }
        else {
            if (LocationSize > 0) {
                read_location(LocationBuf, LocationSize, &Info->mLocBase, &Info->mLocOffset);
            }
            if (Info->mLocBase == SYM_BASE_ABS) {
                Info->mSymbol = find_symbol_entry(Info->mLocOffset);
                if (HighPC != 0) Info->mSize = HighPC - Info->mLocOffset;
            }
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
        dio_ChkData(Form);
        Info->mSize = dio_gFormData;
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
        Info->mLocBase = SYM_BASE_ABS;
        Info->mLocOffset = dio_gFormRef;
        break;
    case AT_high_pc:
        dio_ChkAddr(Form);
        HighPC = dio_gFormRef;
        break;
    case AT_location:
        dio_ChkBlock(Form, &LocationBuf, &LocationSize);
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
    U8_T AddrX = get_symbol_address(*(Elf_Sym **)X);
    U8_T AddrY = get_symbol_address(*(Elf_Sym **)Y);
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
            if (sCache->sym_sections == NULL) {
                sCache->sym_sections_len = 8;
                sCache->sym_sections = loc_alloc(sizeof(SymbolSection *) * sCache->sym_sections_len);
            }
            else if (sCache->sym_sections_cnt >= sCache->sym_sections_len) {
                sCache->sym_sections_len *= 8;
                sCache->sym_sections = loc_realloc(sCache->sym_sections, sizeof(SymbolSection *) * sCache->sym_sections_len);
            }
            sCache->sym_sections[sCache->sym_sections_cnt++] = tbl;
            if (sym_sec->link >= File->section_cnt || (str_sec = File->sections[sym_sec->link]) == NULL) {
                exception(EINVAL);
            }
            if (elf_load(sym_sec, &sym_data) < 0) {
                exception(errno);
            }
            if (elf_load(str_sec, &str_data) < 0) {
                exception(errno);
            }
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
                    if (get_symbol_address((Elf_Sym *)s) != 0) cnt++;
                    Name = s->st_name;
                }
                else {
                    Elf32_Sym * s = (Elf32_Sym *)tbl->mSymPool + i;
                    if (get_symbol_address((Elf_Sym *)s) != 0) cnt++;
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
    for (idx = 0; idx < sCache->sym_sections_cnt; idx++) {
        SymbolSection * tbl = sCache->sym_sections[idx];
        unsigned i;
        for (i = 0; i < tbl->sym_cnt; i++) {
            if (File->elf64) {
                Elf_Sym * s = (Elf_Sym *)((Elf64_Sym *)tbl->mSymPool + i);
                if (get_symbol_address(s) != 0) sCache->mSymbolHash[cnt++] = s;
            }
            else {
                Elf_Sym * s = (Elf_Sym *)((Elf32_Sym *)tbl->mSymPool + i);
                if (get_symbol_address(s) != 0) sCache->mSymbolHash[cnt++] = s;
            }
        }
    }
    assert(sCache->mSymbolTableLen == cnt);
    qsort(sCache->mSymbolHash, sCache->mSymbolTableLen, sizeof(Elf_Sym *), symbol_sort_func);
}

static void load_debug_sections(void) {
    unsigned idx;
    ELF_File * File = sCache->mFile;
    ObjectInfo * Info;

    sObjectHash = sCache->mObjectHash;
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
            while (dio_GetPos() < sec->size) dio_ReadUnit(entry_callback);
            dio_ExitSection();
            sParentObject = NULL;
            sPrevSibling = NULL;
            sCompUnit = NULL;
            sDebugSection = NULL;
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
    }
    Info = sObjectList;
    while (Info != NULL) {
        if (Info->mSize == 0) Info->mSize = get_object_value_size(Info);
        if (Info->mConstValueAddr == NULL && Info->mConstValueSize > 0) {
            assert(Info->mConstValueSize == sizeof(Info->mConstValue));
            if (Info->mSize > 0 && Info->mSize <= Info->mConstValueSize) {
                Info->mConstValueAddr = (U1_T *)&Info->mConstValue;
                Info->mConstValueSize = Info->mSize;
                if (File->big_endian) Info->mConstValueAddr += sizeof(Info->mConstValue) - Info->mSize;
            }
        }
        Info = Info->mListNext;
    }
    sCache->mObjectList = sObjectList;
    sObjectHash = NULL;
    sSymbolHash = NULL;
    sSymbolTableLen = 0;
    sObjectList = NULL;
    sObjectListTail = NULL;
    sCompUnitsMax = 0;
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
        for (i = 0; i < Cache->sym_sections_cnt; i++) {
            SymbolSection * tbl = Cache->sym_sections[i];
            loc_free(tbl->mHashNext);
            loc_free(tbl);
        }
        /* TODO: free object and type records */
        loc_free(Cache->mObjectHash);
        loc_free(Cache->mSymbolHash);
        loc_free(Cache);
        File->dwarf_dt_cache = NULL;
    }
}

DWARFCache * get_dwarf_cache(ELF_File * File) {
    if (File->dwarf_dt_cache == NULL) {
        Trap trap;
        if (set_trap(&trap)) {
            dio_LoadAbbrevTable(File);
            sCache = (DWARFCache *)(File->dwarf_dt_cache = loc_alloc_zero(sizeof(DWARFCache)));
            sCache->magic = SYM_CACHE_MAGIC;
            sCache->mFile = File;
            sCache->mObjectHash = loc_alloc_zero(sizeof(ObjectInfo *) * TYPE_HASH_SIZE);
            load_symbol_tables();
            load_debug_sections();
            sCache = NULL;
            if (!sCloseListenerOK) {
                elf_add_close_listener(free_dwarf_cache);
                sCloseListenerOK = 1;
            }
            clear_trap(&trap);
        }
        else {
            free_dwarf_cache(File);
            str_exception(trap.error, trap.msg);
        }
    }
    return (DWARFCache *)File->dwarf_dt_cache;
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
    dio_gUnitPos = Unit->mLineInfoOffs;
    dio_EnterSection(Cache->mDebugLine, dio_gUnitPos);
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
        state.mIsStmt = is_stmt_default;
        while (dio_GetPos() < dio_gUnitPos + unit_size) {
            U1_T opcode = dio_ReadU1();
            if (opcode >= opcode_base) {
                state.mLine += (unsigned)((int)((opcode - opcode_base) % line_range) + line_base);
                state.mAddress += (opcode - opcode_base) / line_range * min_instruction_length;
                add_state(Unit, &state);
                state.mBasicBlock = 0;
                state.mPrologueEnd = 0;
                state.mEpilogueBegin = 0;
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
                    state.mEndSequence = 1;
                    add_state(Unit, &state);
                    memset(&state, 0, sizeof(state));
                    state.mFile = 1;
                    state.mLine = 1;
                    state.mIsStmt = is_stmt_default;
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
                    state.mBasicBlock = 0;
                    state.mPrologueEnd = 0;
                    state.mEpilogueBegin = 0;
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
                    state.mIsStmt = !state.mIsStmt;
                    break;
                case DW_LNS_set_basic_block:
                    state.mBasicBlock = 1;
                    break;
                case DW_LNS_const_add_pc:
                    state.mAddress += (255 - opcode_base) / line_range * min_instruction_length;
                    break;
                case DW_LNS_fixed_advance_pc:
                    state.mAddress += dio_ReadU2();
                    break;
                case DW_LNS_set_prologue_end:
                    state.mPrologueEnd = 1;
                    break;
                case DW_LNS_set_epilogue_begin:
                    state.mEpilogueBegin = 1;
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


#ifndef  NDEBUG

static char * tag2str(ObjectInfo * Info) {
    static char str[64];
    switch (Info->mTag) {
    case TAG_padding                : return "padding";
    case TAG_array_type             : return "array type";
    case TAG_class_type             : return "class type";
    case TAG_entry_point            : return "entry point";
    case TAG_enumeration_type       : return "enumeration type";
    case TAG_formal_parameter       : return "formal parameter";
    case TAG_global_subroutine      : return "global subroutine";
    case TAG_global_variable        : return "global variable";
    case TAG_imported_declaration   : return "imported declaration";
    case TAG_label                  : return "label";
    case TAG_lexical_block          : return "lexical block";
    case TAG_local_variable         : return "local variable";
    case TAG_member                 : return "member";
    case TAG_pointer_type           : return "pointer type";
    case TAG_reference_type         : return "reference type";
    case TAG_compile_unit           : 
        if (Info->mCompUnit->mVersion < 2) return "compile unit";
        return "source file";
    case TAG_string_type            : return "string type";
    case TAG_structure_type         : return "structure type";
    case TAG_subroutine             : return "subroutine";
    case TAG_subroutine_type        : return "subroutine type";
    case TAG_typedef                : return "typedef";
    case TAG_union_type             : return "union type";
    case TAG_unspecified_parameters : return "unspecified parameters";
    case TAG_variant                : return "variant";
    case TAG_common_block           : return "common block";
    case TAG_common_inclusion       : return "common inclusion";
    case TAG_inheritance            : return "inheritance";
    case TAG_inlined_subroutine     : return "inlined subroutine";
    case TAG_module                 : return "module";
    case TAG_ptr_to_member_type     : return "ptr to member type";
    case TAG_set_type               : return "set type";
    case TAG_subrange_type          : return "subrange type";
    case TAG_with_stmt              : return "with stmt";
    case TAG_access_declaration     : return "access declaration";
    case TAG_base_type              : return "base type";
    case TAG_catch_block            : return "catch block";
    case TAG_const_type             : return "const type";
    case TAG_constant               : return "constant";
    case TAG_enumerator             : return "enumerator";
    case TAG_file_type              : return "file type";
    case TAG_friend                 : return "friend";
    case TAG_namelist               : return "namelist";
    case TAG_namelist_item          : return "namelist item";
    case TAG_packed_type            : return "packed type";
    case TAG_subprogram             : return "subprogram";
    case TAG_template_type_param    : return "template type param";
    case TAG_template_value_param   : return "template value param";
    case TAG_thrown_type            : return "thrown type";
    case TAG_try_block              : return "try block";
    case TAG_variant_part           : return "variant part";
    case TAG_variable               : return "variable";
    case TAG_volatile_type          : return "volatile type";
    case TAG_dwarf_procedure        : return "dwarf procedure";
    case TAG_restrict_type          : return "restrict type";
    case TAG_interface_type         : return "interface type";
    case TAG_namespace              : return "namespace";
    case TAG_imported_module        : return "imported module";
    case TAG_unspecified_type       : return "unspecified type";
    case TAG_partial_unit           : return "partial unit";
    case TAG_imported_unit          : return "imported unit";
    case TAG_mutable_type           : return "mutable type";
    case TAG_condition              : return "condition";
    case TAG_shared_type            : return "shared type";
    case TAG_wrs_thrown_object      : return "wrs thrown object";
    case TAG_wrs_throw_breakpoint   : return "wrs throw breakpoint";
    case TAG_wrs_catch_breakpoint   : return "wrs catch breakpoint";
    }
    if (Info->mTag >= TAG_lo_user)
        sprintf(str, "user tag 0x%04X", Info->mTag);
    else
        sprintf(str, "undefined tag 0x%04X", Info->mTag);
    return str;
}

static void dump_object_list(int Level, ObjectInfo * Info);

static void dump_object_info(int Level, ObjectInfo * Info) {
    char Pad[128];
    int i = 0;
    while (i < sizeof(Pad) - 1 && i < Level * 2) Pad[i++] = ' ';
    Pad[i] = 0;
    printf("%s%016llx %s\n", Pad, Info->mID, tag2str(Info));
    if (Info->mName != NULL) printf("%s  Name: %s\n", Pad, Info->mName);
    if (Info->mLocBase != 0) printf("%s  Location: %d 0x%016llx\n", Pad, Info->mLocBase, Info->mLocOffset);
    if (Info->mDeclFile >= 1 && Info->mDeclFile <= Info->mCompUnit->mFilesCnt) {
        FileInfo * File = Info->mCompUnit->mFiles + (Info->mDeclFile - 1);
        printf("%s  Declaration: %s:%s:%d\n", Pad, File->mDir, File->mName, Info->mDeclLine);
    }
    dump_object_list(Level + 1, Info->mChildren);
}

static void dump_object_list(int Level, ObjectInfo * Info) {
    while (Info != NULL) {
        dump_object_info(Level, Info);
        Info = Info->mSibling;
    }
}

void dump_dwarf_data(char * file_name) {
    unsigned i;
    DWARFCache * Cache;
    ELF_File * File = elf_open(file_name);
    if (File == NULL) exception(errno);
    Cache = get_dwarf_cache(File);
    for (i = 0; i < Cache->mCompUnitsCnt; i++) {
        CompUnit * Unit = Cache->mCompUnits[i];
        printf("Unit 0x%08x..0x%08x\n", Unit->mLowPC, Unit->mHighPC);
        dump_object_list(1, Unit->mChildren);
    }
    elf_close(File);
}

#endif

#endif
