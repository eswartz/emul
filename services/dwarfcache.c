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
 * This module implements caching of DWARF debug information.
 *
 * Functions in this module use exceptions to report errors, see exceptions.h
 */

#include <config.h>

#if ENABLE_ELF

#include <assert.h>
#include <framework/exceptions.h>
#include <framework/myalloc.h>
#include <services/dwarf.h>
#include <services/dwarfio.h>
#include <services/dwarfcache.h>
#include <services/dwarfexpr.h>

#define OBJ_HASH_SIZE          (0x10000-1)

static DWARFCache * sCache;
static ELF_Section * sDebugSection;
static DIO_UnitDescriptor sUnitDesc;
static ObjectInfo * sObjectList;
static ObjectInfo * sObjectListTail;
static CompUnit * sCompUnit;
static unsigned sCompUnitsMax;
static ObjectInfo * sParentObject;
static ObjectInfo * sPrevSibling;

static int sCloseListenerOK = 0;

unsigned calc_symbol_name_hash(const char * s) {
    unsigned h = 0;
    while (*s) {
        unsigned g;
        h = (h << 4) + *s++;
        g = h & 0xf0000000;
        if (g) h ^= g >> 24;
        h &= ~g;
    }
    return h % SYM_HASH_SIZE;
}

void unpack_elf_symbol_info(SymbolSection * section, U4_T index, SymbolInfo * info) {
    memset(info, 0, sizeof(SymbolInfo));
    if (index >= section->mSymCount) exception(ERR_INV_FORMAT);
    info->mSymSection = section;
    if (section->mFile->elf64) {
        Elf64_Sym s = ((Elf64_Sym *)section->mSymPool)[index];
        if (section->mFile->byte_swap) {
            SWAP(s.st_name);
            SWAP(s.st_shndx);
            SWAP(s.st_size);
            SWAP(s.st_value);
        }
        info->mSectionIndex = s.st_shndx;
        if (s.st_shndx > 0 && s.st_shndx < section->mFile->section_cnt) {
            info->mSection = section->mFile->sections + s.st_shndx;
        }
        if (s.st_name > 0) {
            if (s.st_name >= section->mStrPoolSize) exception(ERR_INV_FORMAT);
            info->mName = section->mStrPool + s.st_name;
        }
        info->mBind = ELF64_ST_BIND(s.st_info);
        info->mType = ELF64_ST_TYPE(s.st_info);
        info->mValue = s.st_value;
        info->mSize = s.st_size;
    }
    else {
        Elf32_Sym s = ((Elf32_Sym *)section->mSymPool)[index];
        if (section->mFile->byte_swap) {
            SWAP(s.st_name);
            SWAP(s.st_shndx);
            SWAP(s.st_size);
            SWAP(s.st_value);
        }
        info->mSectionIndex = s.st_shndx;
        if (s.st_shndx > 0 && s.st_shndx < section->mFile->section_cnt) {
            info->mSection = section->mFile->sections + s.st_shndx;
        }
        if (s.st_name > 0) {
            if (s.st_name >= section->mStrPoolSize) exception(ERR_INV_FORMAT);
            info->mName = section->mStrPool + s.st_name;
        }
        info->mBind = ELF32_ST_BIND(s.st_info);
        info->mType = ELF32_ST_TYPE(s.st_info);
        info->mValue = s.st_value;
        info->mSize = s.st_size;
    }
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
        sCache->mCompUnits = (CompUnit **)loc_realloc(sCache->mCompUnits, sizeof(CompUnit *) * sCompUnitsMax);
    }
    Unit = (CompUnit *)loc_alloc_zero(sizeof(CompUnit));
    Unit->mID = ID;
    sCache->mCompUnits[sCache->mCompUnitsCnt++] = Unit;
    return Unit;
}

static ObjectInfo * find_object_info(U8_T ID) {
    ObjectInfo * Info = find_object(sCache, ID);
    if (Info == NULL) {
        U4_T Hash = (U4_T)ID % OBJ_HASH_SIZE;
        if (ID < sDebugSection->addr + dio_gEntryPos) str_exception(ERR_INV_DWARF, "Invalid entry reference");
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

static void entry_callback(U2_T Tag, U2_T Attr, U2_T Form);

static void read_mod_fund_type(U2_T Form, ObjectInfo ** Type) {
    U1_T * Buf;
    size_t BufSize;
    size_t BufPos;
    dio_ChkBlock(Form, &Buf, &BufSize);
    *Type = find_object_info(sDebugSection->addr + dio_GetPos() - 1);
    (*Type)->mTag = TAG_fund_type;
    (*Type)->mCompUnit = sCompUnit;
    (*Type)->mFundType = Buf[BufSize - 1];
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
    size_t BufSize;
    size_t BufPos;
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
            Unit->mSection = sDebugSection;
            Unit->mDebugRangesOffs = ~(U8_T)0;
        }
        else {
            assert(sParentObject == NULL);
            sCompUnit = Unit;
            sPrevSibling = NULL;
        }
        break;
    case AT_low_pc:
        dio_ChkAddr(Form);
        Unit->mLowPC = (ContextAddress)dio_gFormData;
        Unit->mTextSection = dio_gFormSection;
        break;
    case AT_high_pc:
        dio_ChkAddr(Form);
        Unit->mHighPC = (ContextAddress)dio_gFormData;
        break;
    case AT_ranges:
        dio_ChkData(Form);
        Unit->mDebugRangesOffs = dio_gFormData;
        break;
    case AT_name:
        dio_ChkString(Form);
        Unit->mName = (char *)dio_gFormDataAddr;
        break;
    case AT_comp_dir:
        dio_ChkString(Form);
        Unit->mDir = (char *)dio_gFormDataAddr;
        break;
    case AT_stmt_list:
        dio_ChkData(Form);
        Unit->mLineInfoOffs = dio_gFormData;
        break;
    case AT_base_types:
        Unit->mBaseTypes = find_comp_unit(dio_gFormData);
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
            assert(Info->mTag == 0);
            Info->mTag = Tag;
            Info->mCompUnit = sCompUnit;
            Info->mParent = sParentObject;
            Sibling = 0;
        }
        else {
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
        Sibling = dio_gFormData - sDebugSection->addr;
        break;
    case AT_type:
        dio_ChkRef(Form);
        Info->mType = find_object_info(dio_gFormData);
        break;
    case AT_fund_type:
        dio_ChkData(Form);
        Info->mType = find_object_info(sDebugSection->addr + dio_GetPos() - dio_gFormDataSize);
        Info->mType->mTag = TAG_fund_type;
        Info->mCompUnit = sCompUnit;
        Info->mType->mFundType = (U2_T)dio_gFormData;
        break;
    case AT_user_def_type:
        dio_ChkRef(Form);
        Info->mType = find_object_info(dio_gFormData);
        break;
    case AT_mod_fund_type:
        read_mod_fund_type(Form, &Info->mType);
        break;
    case AT_mod_u_d_type:
        read_mod_user_def_type(Form, &Info->mType);
        break;
    case AT_name:
        dio_ChkString(Form);
        Info->mName = (char *)dio_gFormDataAddr;
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

static void load_symbol_tables(void) {
    unsigned idx;
    ELF_File * File = sCache->mFile;
    unsigned sym_size = File->elf64 ? sizeof(Elf64_Sym) : sizeof(Elf32_Sym);

    for (idx = 1; idx < File->section_cnt; idx++) {
        ELF_Section * sym_sec = File->sections + idx;
        if (sym_sec->size == 0) continue;
        if (sym_sec->type == SHT_SYMTAB) {
            unsigned i;
            ELF_Section * str_sec;
            SymbolSection * tbl = (SymbolSection *)loc_alloc_zero(sizeof(SymbolSection));
            if (sCache->mSymSections == NULL) {
                sCache->mSymSectionsLen = 8;
                sCache->mSymSections = (SymbolSection **)loc_alloc(sizeof(SymbolSection *) * sCache->mSymSectionsLen);
            }
            else if (sCache->mSymSectionsCnt >= sCache->mSymSectionsLen) {
                sCache->mSymSectionsLen *= 8;
                sCache->mSymSections = (SymbolSection **)loc_realloc(sCache->mSymSections, sizeof(SymbolSection *) * sCache->mSymSectionsLen);
            }
            tbl->mIndex = sCache->mSymSectionsCnt++;
            sCache->mSymSections[tbl->mIndex] = tbl;
            if (sym_sec->link == 0 || sym_sec->link >= File->section_cnt) exception(EINVAL);
            str_sec = File->sections + sym_sec->link;
            if (elf_load(sym_sec) < 0) exception(errno);
            if (elf_load(str_sec) < 0) exception(errno);
            tbl->mFile = File;
            tbl->mStrPool = (char *)str_sec->data;
            tbl->mStrPoolSize = (size_t)str_sec->size;
            tbl->mSymPool = (ElfX_Sym *)sym_sec->data;
            tbl->mSymPoolSize = (size_t)sym_sec->size;
            tbl->mSymCount = (unsigned)(sym_sec->size / sym_size);
            tbl->mHashNext = (unsigned *)loc_alloc(tbl->mSymCount * sizeof(unsigned));
            for (i = 0; i < tbl->mSymCount; i++) {
                SymbolInfo sym;
                unpack_elf_symbol_info(tbl, i, &sym);
                if (sym.mName == NULL) {
                    tbl->mHashNext[i] = 0;
                }
                else {
                    unsigned h = calc_symbol_name_hash(sym.mName);
                    tbl->mHashNext[i] = tbl->mSymbolHash[h];
                    tbl->mSymbolHash[h] = i;
                }
            }
        }
    }
}

static void load_debug_sections(void) {
    Trap trap;
    unsigned idx;
    ELF_File * File = sCache->mFile;

    memset(&trap, 0, sizeof(trap));
    sObjectList = NULL;
    sObjectListTail = NULL;
    sCompUnitsMax = 0;

    for (idx = 1; idx < File->section_cnt; idx++) {
        ELF_Section * sec = File->sections + idx;
        if (sec->size == 0) continue;
        if (sec->name == NULL) continue;
        if (strcmp(sec->name, ".debug") == 0 || strcmp(sec->name, ".debug_info") == 0) {
            sDebugSection = sec;
            sParentObject = NULL;
            sPrevSibling = NULL;
            dio_EnterSection(NULL, sec, 0);
            if (set_trap(&trap)) {
                while (dio_GetPos() < sec->size) {
                    dio_ReadUnit(&sUnitDesc, entry_callback);
                    sCompUnit->mDesc = sUnitDesc;
                }
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
        else if (strcmp(sec->name, ".debug_frame") == 0) {
            sCache->mDebugFrame = sec;
        }
        else if (strcmp(sec->name, ".eh_frame") == 0) {
            sCache->mEHFrame = sec;
        }
    }

    if (sObjectList == NULL) {
        loc_free(sCache->mObjectHash);
        sCache->mObjectHash = NULL;
    }
    sCache->mObjectList = sObjectList;
    sObjectList = NULL;
    sObjectListTail = NULL;
    sCompUnitsMax = 0;
    if (trap.error) exception(trap.error);
}

static U2_T gop_gAttr = 0;
static U2_T gop_gForm = 0;
static U8_T gop_gFormData = 0;
static size_t gop_gFormDataSize = 0;
static void * gop_gFormDataAddr = NULL;
static ELF_Section * gop_gFormSection = NULL;

static void get_object_property_callback(U2_T Tag, U2_T Attr, U2_T Form) {
    if (Attr != gop_gAttr) return;
    gop_gForm = Form;
    gop_gFormData = dio_gFormData;
    gop_gFormDataSize = dio_gFormDataSize;
    gop_gFormDataAddr = dio_gFormDataAddr;
    gop_gFormSection = dio_gFormSection;
}

U8_T get_numeric_property_value(PropertyValue * Value) {
    U8_T Res = 0;

    if (Value->mAccessFunc != NULL) {
        if (Value->mAccessFunc(Value, 0, &Res) < 0) exception(errno);
    }
    else if (Value->mAddr != NULL) {
        size_t i;
        for (i = 0; i < Value->mSize; i++) {
            Res = (Res << 8) | Value->mAddr[Value->mBigEndian ? i : Value->mSize - i - 1];
        }
    }
    else {
        Res = Value->mValue;
    }
    return Res;
}

static void read_dwarf_object_property(Context * Ctx, int Frame, ObjectInfo * Obj, int Attr, PropertyValue * Value) {

    if (Obj->mTag == TAG_fund_type) {
        /* TAG_fund_type is virtual DWARF object that is created by DWARF reader. It has no properties. */
        exception(ERR_SYM_NOT_FOUND);
    }

    memset(Value, 0, sizeof(PropertyValue));
    Value->mContext = Ctx;
    Value->mFrame = Frame;
    Value->mObject = Obj;
    Value->mAttr = (U2_T)Attr;
    Value->mBigEndian = Obj->mCompUnit->mFile->big_endian;

    sCompUnit = Obj->mCompUnit;
    sCache = (DWARFCache *)sCompUnit->mFile->dwarf_dt_cache;
    sDebugSection = sCompUnit->mSection;
    dio_EnterSection(&sCompUnit->mDesc, sDebugSection, Obj->mID - sDebugSection->addr);
    gop_gAttr = (U2_T)Attr;
    gop_gForm = 0;
    dio_ReadEntry(get_object_property_callback);
    dio_ExitSection();
    sCompUnit = NULL;
    sCache = NULL;
    sDebugSection = NULL;

    switch (Value->mForm = gop_gForm) {
    case FORM_REF       :
    case FORM_REF_ADDR  :
    case FORM_REF1      :
    case FORM_REF2      :
    case FORM_REF4      :
    case FORM_REF8      :
    case FORM_REF_UDATA :
        {
            PropertyValue ValueAddr;
            ObjectInfo * RefObj = find_object(sCache, gop_gFormData);

            if (RefObj == NULL) exception(ERR_INV_DWARF);
            read_and_evaluate_dwarf_object_property(Ctx, Frame, 0, RefObj, AT_location, &ValueAddr);
            if (ValueAddr.mAccessFunc != NULL) {
                ValueAddr.mAccessFunc(&ValueAddr, 0, &Value->mValue);
            }
            else {
                static U1_T Buf[8];
                PropertyValue ValueSize;
                ContextAddress Addr;
                size_t Size;

                Addr = (ContextAddress)get_numeric_property_value(&ValueAddr);
                read_and_evaluate_dwarf_object_property(Ctx, Frame, Addr, RefObj, AT_byte_size, &ValueSize);
                Size = (size_t)get_numeric_property_value(&ValueSize);
                if (Size < 1 || Size > sizeof(Buf)) exception(ERR_INV_DATA_TYPE);
                if (context_read_mem(Ctx, Addr, Buf, Size) < 0) exception(errno);
                Value->mAddr = Buf;
                Value->mSize = Size;
            }
        }
        break;
    case FORM_DATA1     :
    case FORM_DATA2     :
    case FORM_DATA4     :
    case FORM_DATA8     :
    case FORM_FLAG      :
    case FORM_BLOCK1    :
    case FORM_BLOCK2    :
    case FORM_BLOCK4    :
    case FORM_BLOCK     :
        Value->mAddr = (U1_T *)gop_gFormDataAddr;
        Value->mSize = gop_gFormDataSize;
        break;
    case FORM_SDATA     :
    case FORM_UDATA     :
        Value->mValue = gop_gFormData;
        break;
    case FORM_ADDR:
        Value->mValue = elf_map_to_run_time_address(Ctx, Obj->mCompUnit->mFile, gop_gFormSection, (ContextAddress)gop_gFormData);
        break;
    default:
        if (Attr == AT_data_member_location && Obj->mTag == TAG_member && Obj->mParent->mTag == TAG_union_type) {
            Value->mForm = FORM_UDATA;
            Value->mValue = 0;
            break;
        }
        exception(ERR_SYM_NOT_FOUND);
    }
}

void read_and_evaluate_dwarf_object_property(Context * Ctx, int Frame, U8_T Base, ObjectInfo * Obj, int Attr, PropertyValue * Value) {
    read_dwarf_object_property(Ctx, Frame, Obj, Attr, Value);
    assert(Value->mContext == Ctx);
    assert(Value->mFrame == Frame);
    assert(Value->mObject == Obj);
    assert(Value->mAttr == Attr);
    if (Attr == AT_location || Attr == AT_data_member_location || Attr == AT_frame_base) {
        switch (Value->mForm) {
        case FORM_DATA4     :
        case FORM_DATA8     :
        case FORM_BLOCK1    :
        case FORM_BLOCK2    :
        case FORM_BLOCK4    :
        case FORM_BLOCK     :
            dwarf_evaluate_expression(Base, Value);
            break;
        }
    }
    else if (Attr == AT_count || Attr == AT_byte_size || Attr == AT_lower_bound || Attr == AT_upper_bound) {
        switch (Value->mForm) {
        case FORM_BLOCK1    :
        case FORM_BLOCK2    :
        case FORM_BLOCK4    :
        case FORM_BLOCK     :
            dwarf_evaluate_expression(Base, Value);
            break;
        }
    }
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
        assert(Cache->magic == DWARF_CACHE_MAGIC);
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
        loc_free(Cache->mSymSections);
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
        sCache->magic = DWARF_CACHE_MAGIC;
        sCache->mFile = File;
        sCache->mObjectHash = (ObjectInfo **)loc_alloc_zero(sizeof(ObjectInfo *) * OBJ_HASH_SIZE);
        if (set_trap(&trap)) {
            dio_LoadAbbrevTable(File);
            load_symbol_tables();
            load_debug_sections();
            clear_trap(&trap);
        }
        else {
            sCache->mErrorReport = get_error_report(trap.error);
        }
        sCache = NULL;
    }
    if (Cache->mErrorReport) exception(set_error_report_errno(Cache->mErrorReport));
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
    if (Unit->mFiles != NULL || Unit->mDirs != NULL) return;
    if (elf_load(Cache->mDebugLine)) exception(errno);
    dio_EnterSection(&Unit->mDesc, Cache->mDebugLine, Unit->mLineInfoOffs);
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
        int dwarf64 = 0;
        LineNumbersState state;

        /* Read header */
        unit_size = dio_ReadU4();
        if (unit_size == 0xffffffffu) {
            unit_size = dio_ReadU8();
            unit_size += 12;
            dwarf64 = 1;
        }
        else {
            unit_size += 4;
        }
        dio_ReadU2(); /* line info version */
        header_size = dwarf64 ? dio_ReadU8() : (U8_T)dio_ReadU4();
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

        /* Read source files info */
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
        while (dio_GetPos() < Unit->mLineInfoOffs + unit_size) {
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
                    {
                        ELF_Section * s = NULL;
                        state.mAddress = (ContextAddress)dio_ReadAddress(&s);
                        if (s != Unit->mTextSection) state.mAddress = 0;
                    }
                    break;
                default:
                    dio_Skip(op_size - 1);
                    break;
                }
                if (dio_GetPos() != op_pos + op_size)
                    str_exception(ERR_INV_DWARF, "Invalid line info op size");
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
                    state.mColumn = (U2_T)dio_ReadULEB128();
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
        exception(trap.error);
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

#endif /* ENABLE_ELF */
