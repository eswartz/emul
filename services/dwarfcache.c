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
#include <services/stacktrace.h>

#define OBJ_HASH(Cache,ID)            (((U4_T)(ID) + ((U4_T)(ID) >> 8)) % Cache->mObjectHashSize)

static DWARFCache * sCache;
static ELF_Section * sDebugSection;
static DIO_UnitDescriptor sUnitDesc;
static CompUnit * sCompUnit;
static ObjectInfo * sParentObject;
static ObjectInfo * sPrevSibling;

static int sCloseListenerOK = 0;

unsigned calc_symbol_name_hash(const char * s) {
    unsigned h = 0;
    while (*s) {
        unsigned g;
        if (s[0] == '@' && s[1] == '@') break;
        h = (h << 4) + (unsigned char)*s++;
        g = h & 0xf0000000;
        if (g) h ^= g >> 24;
        h &= ~g;
    }
    return h % SYM_HASH_SIZE;
}

int cmp_symbol_names(const char * x, const char * y) {
    while (*x && *x == *y) {
        x++;
        y++;
    }
    if (*x == 0 && *y == '@' && y[1] == '@') return 0;
    if (*y == 0 && *x == '@' && x[1] == '@') return 0;
    if (*x < *y) return -1;
    if (*x > *y) return +1;
    return 0;
}

unsigned calc_file_name_hash(const char * s) {
    unsigned l = strlen(s);
    unsigned h = 0;
    while (l > 0) {
        unsigned g;
        unsigned char ch = s[--l];
        if (ch == '/') break;
        if (ch == '\\') break;
        h = (h << 4) + ch;
        g = h & 0xf0000000;
        if (g) h ^= g >> 24;
        h &= ~g;
    }
    return h;
}

void unpack_elf_symbol_info(SymbolSection * section, U4_T index, SymbolInfo * info) {
    memset(info, 0, sizeof(SymbolInfo));
    if (index >= section->mSymCount) str_exception(ERR_INV_FORMAT, "Invalid ELF symbol index");
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
            if (s.st_name >= section->mStrPoolSize) str_exception(ERR_INV_FORMAT, "Invalid ELF string pool index");
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
            if (s.st_name >= section->mStrPoolSize) str_exception(ERR_INV_FORMAT, "Invalid ELF string pool index");
            info->mName = section->mStrPool + s.st_name;
        }
        info->mBind = ELF32_ST_BIND(s.st_info);
        info->mType = ELF32_ST_TYPE(s.st_info);
        info->mValue = s.st_value;
        info->mSize = s.st_size;
    }
}

ObjectInfo * find_object(DWARFCache * Cache, U8_T ID) {
    if (Cache->mObjectHash != NULL) {
        ObjectInfo * Info = Cache->mObjectHash[OBJ_HASH(Cache, ID)];

        while (Info != NULL) {
            if (Info->mID == ID) return Info;
            Info = Info->mHashNext;
        }
    }
    return NULL;
}

static ObjectInfo * add_object_info(U8_T ID) {
    ObjectInfo * Info = find_object(sCache, ID);
    if (Info == NULL) {
        U4_T Hash;
        if (ID < sDebugSection->addr + dio_gEntryPos) str_exception(ERR_INV_DWARF, "Invalid entry reference");
        if (ID > sDebugSection->addr + sDebugSection->size) str_exception(ERR_INV_DWARF, "Invalid entry reference");
        if (sCache->mObjectHash == NULL) {
            sCache->mObjectHashSize = (unsigned)(sDebugSection->size / 251);
            if (sCache->mObjectHashSize < 101) sCache->mObjectHashSize = 101;
            sCache->mObjectHash = (ObjectInfo **)loc_alloc_zero(sizeof(ObjectInfo *) * sCache->mObjectHashSize);
        }
        Hash = OBJ_HASH(sCache, ID);
        if (sCache->mObjectArrayPos >= OBJECT_ARRAY_SIZE) {
            ObjectArray * Buf = (ObjectArray *)loc_alloc_zero(sizeof(ObjectArray));
            Buf->mNext = sCache->mObjectList;
            sCache->mObjectList = Buf;
            sCache->mObjectArrayPos = 0;
        }
        Info = sCache->mObjectList->mArray + sCache->mObjectArrayPos++;
        Info->mHashNext = sCache->mObjectHash[Hash];
        sCache->mObjectHash[Hash] = Info;
        Info->mID = ID;
    }
    return Info;
}

static CompUnit * add_comp_unit(U8_T ID) {
    ObjectInfo * Info = add_object_info(ID);
    if (Info->mCompUnit == NULL) {
        CompUnit * Unit = (CompUnit *)loc_alloc_zero(sizeof(CompUnit));
        Unit->mObject = Info;
        Info->mCompUnit = Unit;
    }
    return Info->mCompUnit;
}

static void read_mod_fund_type(U2_T Form, ObjectInfo ** Type) {
    U1_T * Buf;
    size_t BufSize;
    size_t BufPos;
    dio_ChkBlock(Form, &Buf, &BufSize);
    *Type = add_object_info(sDebugSection->addr + dio_GetPos() - 1);
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
        Mod = add_object_info(sDebugSection->addr + dio_GetPos() - BufSize + BufPos);
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
    *Type = add_object_info(sDebugSection->addr + Ref);
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
        Mod = add_object_info(sDebugSection->addr + dio_GetPos() - BufSize + BufPos);
        Mod->mTag = Tag;
        Mod->mCompUnit = sCompUnit;
        Mod->mType = *Type;
        *Type = Mod;
    }
}

static void read_object_info(U2_T Tag, U2_T Attr, U2_T Form) {
    static ObjectInfo * Info;
    static ObjectInfo * Spec;
    static ObjectInfo * AOrg;
    static U8_T Sibling;
    static int HasChildren;

    switch (Attr) {
    case 0:
        if (Form) {
            assert((sParentObject == NULL) == (Tag == TAG_compile_unit));
            if (Tag == TAG_compile_unit) {
                CompUnit * Unit = add_comp_unit(sDebugSection->addr + dio_gEntryPos);
                Unit->mFile = sCache->mFile;
                Unit->mDebugRangesOffs = ~(U8_T)0;
                Unit->mRegIdScope.big_endian = sCache->mFile->big_endian;
                Unit->mRegIdScope.machine = sCache->mFile->machine;
                Unit->mRegIdScope.os_abi = sCache->mFile->os_abi;
                Unit->mRegIdScope.id_type = REGNUM_DWARF;
                Info = Unit->mObject;
                sCompUnit = Unit;
            }
            else {
                Info = add_object_info(sDebugSection->addr + dio_gEntryPos);
                Info->mCompUnit = sCompUnit;
            }
            assert(Info->mTag == 0);
            Info->mTag = Tag;
            Info->mParent = sParentObject;
            HasChildren = Form == DWARF_ENTRY_HAS_CHILDREN;
            Sibling = 0;
            Spec = NULL;
            AOrg = NULL;
        }
        else {
            if (Spec != NULL) {
                if (Info->mName == NULL) Info->mName = Spec->mName;
                if (Info->mType == NULL) Info->mType = Spec->mType;
            }
            if (AOrg != NULL) {
                if (Info->mName == NULL) Info->mName = AOrg->mName;
                if (Info->mType == NULL) Info->mType = AOrg->mType;
            }
            if (Tag == TAG_compile_unit && Sibling == 0) Sibling = sUnitDesc.mUnitOffs + sUnitDesc.mUnitSize;
            if (Tag == TAG_enumerator && Info->mType == NULL) Info->mType = sParentObject;
            if (sPrevSibling != NULL) sPrevSibling->mSibling = Info;
            else if (sParentObject != NULL) sParentObject->mChildren = Info;
            else sCache->mCompUnits = Info;
            sPrevSibling = Info;
            if (Sibling != 0 || HasChildren) {
                U8_T SiblingPos = Sibling;
                ObjectInfo * Parent = sParentObject;
                ObjectInfo * PrevSibling = sPrevSibling;
                sParentObject = Info;
                sPrevSibling = NULL;
                for (;;) {
                    if (SiblingPos > 0 && dio_GetPos() >= SiblingPos) break;
                    if (!dio_ReadEntry(read_object_info, 0)) break;
                }
                if (SiblingPos > dio_GetPos()) dio_Skip(SiblingPos - dio_GetPos());
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
        Info->mType = add_object_info(dio_gFormData);
        break;
    case AT_fund_type:
        dio_ChkData(Form);
        Info->mType = add_object_info(sDebugSection->addr + dio_GetPos() - dio_gFormDataSize);
        Info->mType->mTag = TAG_fund_type;
        Info->mCompUnit = sCompUnit;
        Info->mType->mFundType = (U2_T)dio_gFormData;
        break;
    case AT_user_def_type:
        dio_ChkRef(Form);
        Info->mType = add_object_info(dio_gFormData);
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
    case AT_specification_v2:
        dio_ChkRef(Form);
        Spec = add_object_info(dio_gFormData);
        break;
    case AT_abstract_origin:
        dio_ChkRef(Form);
        AOrg = add_object_info(dio_gFormData);
        break;
    }
    if (Tag == TAG_compile_unit) {
        CompUnit * Unit = Info->mCompUnit;
        switch (Attr) {
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
        case AT_comp_dir:
            dio_ChkString(Form);
            Unit->mDir = (char *)dio_gFormDataAddr;
            break;
        case AT_stmt_list:
            dio_ChkData(Form);
            Unit->mLineInfoOffs = dio_gFormData;
            break;
        case AT_base_types:
            Unit->mBaseTypes = add_comp_unit(dio_gFormData);
            break;
        }
    }
}

static void load_symbol_tables(void) {
    unsigned idx;
    ELF_File * file = sCache->mFile;
    unsigned sym_size = file->elf64 ? sizeof(Elf64_Sym) : sizeof(Elf32_Sym);

    for (idx = 1; idx < file->section_cnt; idx++) {
        ELF_Section * sym_sec = file->sections + idx;
        if (sym_sec->size == 0) continue;
        if (sym_sec->type == SHT_SYMTAB) {
            ELF_Section * str_sec;
            SymbolSection * tbl = (SymbolSection *)loc_alloc_zero(sizeof(SymbolSection));
            if (sCache->mSymSectionsCnt >= sCache->mSymSectionsMax) {
                sCache->mSymSectionsMax = sCache->mSymSectionsMax == 0 ? 16 : sCache->mSymSectionsMax * 2;
                sCache->mSymSections = (SymbolSection **)loc_realloc(sCache->mSymSections, sizeof(SymbolSection *) * sCache->mSymSectionsMax);
            }
            tbl->mIndex = sCache->mSymSectionsCnt++;
            sCache->mSymSections[tbl->mIndex] = tbl;
            if (sym_sec->link == 0 || sym_sec->link >= file->section_cnt) exception(EINVAL);
            str_sec = file->sections + sym_sec->link;
            if (elf_load(sym_sec) < 0) exception(errno);
            if (elf_load(str_sec) < 0) exception(errno);
            tbl->mFile = file;
            tbl->mStrPool = (char *)str_sec->data;
            tbl->mStrPoolSize = (size_t)str_sec->size;
            tbl->mSymPool = (ElfX_Sym *)sym_sec->data;
            tbl->mSymPoolSize = (size_t)sym_sec->size;
            tbl->mSymCount = (unsigned)(sym_sec->size / sym_size);
        }
    }
}

static int cmp_addr_ranges(const void * x, const void * y) {
    UnitAddressRange * rx = (UnitAddressRange *)x;
    UnitAddressRange * ry = (UnitAddressRange *)y;
    if (rx->mAddr < ry->mAddr) return -1;
    if (rx->mAddr > ry->mAddr) return +1;
    return 0;
}

static void add_addr_range(ELF_Section * sec, CompUnit * unit, ContextAddress addr, ContextAddress size) {
    UnitAddressRange * range = NULL;
    if (sCache->mAddrRangesCnt >= sCache->mAddrRangesMax) {
        sCache->mAddrRangesMax = sCache->mAddrRangesMax == 0 ? 64 : sCache->mAddrRangesMax * 2;
        sCache->mAddrRanges = (UnitAddressRange *)loc_realloc(sCache->mAddrRanges, sizeof(UnitAddressRange) * sCache->mAddrRangesMax);
    }
    range = sCache->mAddrRanges + sCache->mAddrRangesCnt++;
    memset(range, 0, sizeof(UnitAddressRange));
    range->mSection = sec;
    range->mAddr = addr;
    range->mSize = size;
    range->mUnit = unit;
}

static void load_addr_ranges() {
    Trap trap;
    unsigned idx;
    ELF_File * file = sCache->mFile;
    ELF_Section * debug_ranges = NULL;

    memset(&trap, 0, sizeof(trap));
    for (idx = 1; idx < file->section_cnt; idx++) {
        ELF_Section * sec = file->sections + idx;
        if (sec->size == 0) continue;
        if (sec->name == NULL) continue;
        if (strcmp(sec->name, ".debug_ranges") == 0) {
            debug_ranges = sec;
        }
        else if (strcmp(sec->name, ".debug_aranges") == 0) {
            ObjectInfo * info = sCache->mCompUnits;
            dio_EnterSection(NULL, sec, 0);
            if (set_trap(&trap)) {
                while (dio_GetPos() < sec->size) {
                    int dwarf64 = 0;
                    U8_T size = dio_ReadU4();
                    U8_T next = 0;
                    if (size == 0xffffffffu) {
                        dwarf64 = 1;
                        size = dio_ReadU8();
                    }
                    next = dio_GetPos() + size;
                    if (dio_ReadU2() != 2) {
                        dio_Skip(next - dio_GetPos());
                    }
                    else {
                        U8_T offs = dwarf64 ? dio_ReadU8() : (U8_T)dio_ReadU4();
                        U1_T addr_size = dio_ReadU1();
                        U1_T segm_size = dio_ReadU1();
                        if (segm_size != 0) str_exception(ERR_INV_DWARF, "segment descriptors are not supported");
                        while (info != NULL && info->mCompUnit->mDesc.mUnitOffs != offs) info = info->mSibling;
                        if (info == NULL) {
                            info = sCache->mCompUnits;
                            while (info != NULL && info->mCompUnit->mDesc.mUnitOffs != offs) info = info->mSibling;
                        }
                        if (info == NULL) str_exception(ERR_INV_DWARF, "invalid .debug_aranges section");
                        info->mCompUnit->mARangesFound = 1;
                        while (dio_GetPos() % (addr_size * 2) != 0) dio_Skip(1);
                        for (;;) {
                            ELF_Section * range_sec = NULL;
                            ContextAddress addr = dio_ReadAddressX(&range_sec, addr_size);
                            ContextAddress size = dio_ReadUX(addr_size);
                            if (addr == 0 && size == 0) break;
                            if (size == 0) continue;
                            add_addr_range(range_sec, info->mCompUnit, addr, size);
                        }
                    }
                }
                clear_trap(&trap);
            }
            dio_ExitSection();
            if (trap.error) break;
        }
    }
    if (trap.error) exception(trap.error);
    if (sCache->mCompUnits != NULL) {
        ObjectInfo * info = sCache->mCompUnits;
        while (info != NULL) {
            CompUnit * unit = info->mCompUnit;
            ContextAddress base = unit->mLowPC;
            ContextAddress size = unit->mHighPC - unit->mLowPC;
            info = info->mSibling;
            if (unit->mARangesFound) continue;
            if (size == 0) continue;
            if (unit->mDebugRangesOffs != ~(U8_T)0 && debug_ranges != NULL) {
                dio_EnterSection(&unit->mDesc, debug_ranges, unit->mDebugRangesOffs);
                for (;;) {
                    ELF_Section * sec = NULL;
                    U8_T x = dio_ReadAddress(&sec);
                    U8_T y = dio_ReadAddress(&sec);
                    if (x == 0 && y == 0) break;
                    if (sec != unit->mTextSection) exception(ERR_INV_DWARF);
                    if (x == ((U8_T)1 << unit->mDesc.mAddressSize * 8) - 1) {
                        base = (ContextAddress)y;
                    }
                    else {
                        x = base + x;
                        y = base + y;
                        add_addr_range(sec, unit, x, y - x);
                    }
                }
                dio_ExitSection();
            }
            else {
                add_addr_range(unit->mTextSection, unit, base, size);
            }
        }
    }
    if (sCache->mAddrRangesCnt > 1) {
        qsort(sCache->mAddrRanges, sCache->mAddrRangesCnt, sizeof(UnitAddressRange), cmp_addr_ranges);
    }
}

static void load_pub_names(ELF_Section * sec, PubNamesTable * tbl) {
    tbl->mMax = (unsigned)(sec->size / 16) + 16;
    tbl->mHash = (unsigned *)loc_alloc_zero(sizeof(unsigned) * SYM_HASH_SIZE);
    tbl->mNext = (PubNamesInfo *)loc_alloc(sizeof(PubNamesInfo) * tbl->mMax);
    memset(tbl->mNext + tbl->mCnt++, 0, sizeof(PubNamesInfo));
    dio_EnterSection(NULL, sec, 0);
    while (dio_GetPos() < sec->size) {
        int dwarf64 = 0;
        U8_T size = dio_ReadU4();
        U8_T next = 0;
        U8_T unit_offs = 0;
        U8_T unit_size = 0;
        if (size == 0xffffffffu) {
            dwarf64 = 1;
            size = dio_ReadU8();
        }
        next = dio_GetPos() + size;
        dio_ReadU2(); /* version */
        unit_offs = dwarf64 ? dio_ReadU8() : (U8_T)dio_ReadU4();
        unit_size = dwarf64 ? dio_ReadU8() : (U8_T)dio_ReadU4();
        for (;;) {
            unsigned h;
            PubNamesInfo * info = NULL;
            U8_T obj_offs = dwarf64 ? dio_ReadU8() : (U8_T)dio_ReadU4();
            if (obj_offs == 0) break;
            if (tbl->mCnt >= tbl->mMax) {
                tbl->mMax = tbl->mMax * 3 / 2;
                tbl->mNext = (PubNamesInfo *)loc_realloc(tbl->mNext, sizeof(PubNamesInfo) * tbl->mMax);
            }
            info = tbl->mNext + tbl->mCnt;
            h = calc_symbol_name_hash(dio_ReadString());
            info->mID = sec->addr + unit_offs + obj_offs;
            info->mNext = tbl->mHash[h];
            tbl->mHash[h] = tbl->mCnt++;
        }
        assert(next >= dio_GetPos());
        dio_Skip(next - dio_GetPos());
    }
    dio_ExitSection();
}

static void load_debug_sections(void) {
    Trap trap;
    unsigned idx;
    ELF_Section * pub_names = NULL;
    ELF_Section * pub_types = NULL;
    ELF_File * file = sCache->mFile;

    memset(&trap, 0, sizeof(trap));

    for (idx = 1; idx < file->section_cnt; idx++) {
        ELF_Section * sec = file->sections + idx;
        if (sec->size == 0) continue;
        if (sec->name == NULL) continue;
        if (strcmp(sec->name, ".debug") == 0 || strcmp(sec->name, ".debug_info") == 0) {
            sDebugSection = sec;
            sParentObject = NULL;
            sPrevSibling = NULL;
            dio_EnterSection(NULL, sec, 0);
            if (set_trap(&trap)) {
                while (dio_GetPos() < sec->size) {
                    dio_ReadUnit(&sUnitDesc, read_object_info);
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
        else if (strcmp(sec->name, ".line") == 0) {
            sCache->mDebugLineV1 = sec;
        }
        else if (strcmp(sec->name, ".debug_line") == 0) {
            sCache->mDebugLine = sec;
        }
        else if (strcmp(sec->name, ".debug_loc") == 0) {
            sCache->mDebugLoc = sec;
        }
        else if (strcmp(sec->name, ".debug_ranges") == 0) {
            sCache->mDebugRanges = sec;
        }
        else if (strcmp(sec->name, ".debug_frame") == 0) {
            sCache->mDebugFrame = sec;
        }
        else if (strcmp(sec->name, ".eh_frame") == 0) {
            sCache->mEHFrame = sec;
        }
        else if (strcmp(sec->name, ".debug_pubnames") == 0) {
            pub_names = sec;
        }
        else if (strcmp(sec->name, ".debug_pubtypes") == 0) {
            pub_types = sec;
        }
    }

    if (pub_names) load_pub_names(pub_names, &sCache->mPubNames);
    if (pub_types) load_pub_names(pub_types, &sCache->mPubTypes);

    if (trap.error) exception(trap.error);
}

static U2_T gop_gAttr = 0;
static U2_T gop_gForm = 0;
static U8_T gop_gFormData = 0;
static size_t gop_gFormDataSize = 0;
static void * gop_gFormDataAddr = NULL;
static ELF_Section * gop_gFormSection = NULL;
static U8_T gop_gSpecification = 0;
static U8_T gop_gAbstractOrigin = 0;

static void get_object_property_callback(U2_T Tag, U2_T Attr, U2_T Form) {
    if (Attr == AT_specification_v2) gop_gSpecification = dio_gFormData;
    if (Attr == AT_abstract_origin) gop_gAbstractOrigin = dio_gFormData;
    if (Attr != gop_gAttr) return;
    gop_gForm = Form;
    gop_gFormData = dio_gFormData;
    gop_gFormDataSize = dio_gFormDataSize;
    gop_gFormDataAddr = dio_gFormDataAddr;
    gop_gFormSection = dio_gFormSection;
}

U8_T get_numeric_property_value(PropertyValue * Value) {
    U8_T Res = 0;

    if (Value->mRegister != NULL) {
        str_exception(ERR_INV_CONTEXT, "register variable");
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

static void read_dwarf_object_property(Context * Ctx, int Frame, ObjectInfo * Obj, U2_T Attr, PropertyValue * Value) {

    if (Obj->mTag == TAG_fund_type) {
        /* TAG_fund_type is virtual DWARF object that is created by DWARF reader. It has no properties. */
        exception(ERR_SYM_NOT_FOUND);
    }

    memset(Value, 0, sizeof(PropertyValue));
    Value->mContext = Ctx;
    Value->mFrame = Frame;
    Value->mObject = Obj;
    Value->mAttr = Attr;
    Value->mBigEndian = Obj->mCompUnit->mFile->big_endian;

    sCompUnit = Obj->mCompUnit;
    sCache = (DWARFCache *)sCompUnit->mFile->dwarf_dt_cache;
    sDebugSection = sCompUnit->mDesc.mSection;
    dio_EnterSection(&sCompUnit->mDesc, sDebugSection, Obj->mID - sDebugSection->addr);
    for (;;) {
        gop_gAttr = Attr;
        gop_gForm = 0;
        gop_gSpecification = 0;
        gop_gAbstractOrigin = 0;
        dio_ReadEntry(get_object_property_callback, Attr);
        dio_ExitSection();
        if (gop_gForm != 0) break;
        if (gop_gSpecification != 0) dio_EnterSection(&sCompUnit->mDesc, sDebugSection, gop_gSpecification - sDebugSection->addr);
        else if (gop_gAbstractOrigin != 0) dio_EnterSection(&sCompUnit->mDesc, sDebugSection, gop_gAbstractOrigin - sDebugSection->addr);
        else break;
    }

    switch (Value->mForm = gop_gForm) {
    case FORM_REF       :
    case FORM_REF_ADDR  :
    case FORM_REF1      :
    case FORM_REF2      :
    case FORM_REF4      :
    case FORM_REF8      :
    case FORM_REF_UDATA :
        if (Attr == AT_import) {
            Value->mValue = gop_gFormData;
        }
        else {
            PropertyValue ValueAddr;
            ObjectInfo * RefObj = find_object(sCache, gop_gFormData);

            if (RefObj == NULL) exception(ERR_INV_DWARF);
            read_and_evaluate_dwarf_object_property(Ctx, Frame, 0, RefObj, AT_location, &ValueAddr);
            if (ValueAddr.mRegister != NULL) {
                static U1_T Buf[8];
                StackFrame * Frame = NULL;
                if (get_frame_info(ValueAddr.mContext, ValueAddr.mFrame, &Frame) < 0) exception(errno);
                if (read_reg_bytes(Frame, ValueAddr.mRegister, 0, ValueAddr.mRegister->size, Buf) < 0) exception(errno);
                Value->mAddr = Buf;
                Value->mSize = ValueAddr.mSize;
                Value->mBigEndian = ValueAddr.mBigEndian;
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
    case FORM_STRP      :
        Value->mAddr = (U1_T *)gop_gFormDataAddr;
        Value->mSize = gop_gFormDataSize;
        break;
    case FORM_SDATA     :
    case FORM_UDATA     :
        Value->mValue = gop_gFormData;
        break;
    case FORM_ADDR      :
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

    sCompUnit = NULL;
    sCache = NULL;
    sDebugSection = NULL;
}

void read_and_evaluate_dwarf_object_property(Context * Ctx, int Frame, U8_T Base, ObjectInfo * Obj, U2_T Attr, PropertyValue * Value) {
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

    while (Unit->mStatesCnt > 0) {
        loc_free(Unit->mStates[--Unit->mStatesCnt].mFileName);
    }
    loc_free(Unit->mStates);
    loc_free(Unit->mStatesIndex);
    Unit->mStates = NULL;
    Unit->mStatesMax = 0;
    Unit->mStatesIndex = NULL;
}

static void free_dwarf_cache(ELF_File * file) {
    DWARFCache * Cache = (DWARFCache *)file->dwarf_dt_cache;
    if (Cache != NULL) {
        unsigned i;
        assert(Cache->magic == DWARF_CACHE_MAGIC);
        Cache->magic = 0;
        while (Cache->mCompUnits != NULL) {
            CompUnit * Unit = Cache->mCompUnits->mCompUnit;
            Cache->mCompUnits = Cache->mCompUnits->mSibling;
            free_unit_cache(Unit);
            loc_free(Unit);
        }
        for (i = 0; i < Cache->mSymSectionsCnt; i++) {
            SymbolSection * tbl = Cache->mSymSections[i];
            loc_free(tbl->mSymNamesHash);
            loc_free(tbl->mSymNamesNext);
            loc_free(tbl);
        }
        while (Cache->mObjectList != NULL) {
            ObjectArray * Buf = Cache->mObjectList;
            Cache->mObjectList = Buf->mNext;
            loc_free(Buf);
        }
        loc_free(Cache->mObjectHash);
        loc_free(Cache->mSymSections);
        loc_free(Cache->mAddrRanges);
        loc_free(Cache->mFrameInfoRanges);
        loc_free(Cache->mPubNames.mHash);
        loc_free(Cache->mPubNames.mNext);
        loc_free(Cache->mPubTypes.mHash);
        loc_free(Cache->mPubTypes.mNext);
        loc_free(Cache);
        file->dwarf_dt_cache = NULL;
    }
}

DWARFCache * get_dwarf_cache(ELF_File * file) {
    DWARFCache * Cache = (DWARFCache *)file->dwarf_dt_cache;
    if (Cache == NULL) {
        Trap trap;
        if (!sCloseListenerOK) {
            elf_add_close_listener(free_dwarf_cache);
            sCloseListenerOK = 1;
        }
        sCache = Cache = (DWARFCache *)(file->dwarf_dt_cache = loc_alloc_zero(sizeof(DWARFCache)));
        sCache->magic = DWARF_CACHE_MAGIC;
        sCache->mFile = file;
        sCache->mObjectArrayPos = OBJECT_ARRAY_SIZE;
        if (set_trap(&trap)) {
            dio_LoadAbbrevTable(file);
            load_symbol_tables();
            load_debug_sections();
            load_addr_ranges();
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

static void add_file(CompUnit * Unit, FileInfo * file) {
    file->mNameHash = calc_file_name_hash(file->mName);
    if (Unit->mFilesCnt >= Unit->mFilesMax) {
        Unit->mFilesMax = Unit->mFilesMax == 0 ? 16 : Unit->mFilesMax * 2;
        Unit->mFiles = (FileInfo *)loc_realloc(Unit->mFiles, sizeof(FileInfo) * Unit->mFilesMax);
    }
    if (file->mDir == NULL) file->mDir = Unit->mDir;
    Unit->mFiles[Unit->mFilesCnt++] = *file;
}

static void add_state(CompUnit * Unit, LineNumbersState * state) {
    if (Unit->mStatesCnt >= Unit->mStatesMax) {
        Unit->mStatesMax = Unit->mStatesMax == 0 ? 128 : Unit->mStatesMax * 2;
        Unit->mStates = (LineNumbersState *)loc_realloc(Unit->mStates, sizeof(LineNumbersState) * Unit->mStatesMax);
    }
    Unit->mStates[Unit->mStatesCnt++] = *state;
}

static int state_address_comparator(const void * x1, const void * x2) {
    LineNumbersState * s1 = (LineNumbersState *)x1;
    LineNumbersState * s2 = (LineNumbersState *)x2;
    if (s1->mAddress < s2->mAddress) return -1;
    if (s1->mAddress > s2->mAddress) return +1;
    return 0;
}

static int state_text_pos_comparator(const void * x1, const void * x2) {
    LineNumbersState * s1 = *(LineNumbersState **)x1;
    LineNumbersState * s2 = *(LineNumbersState **)x2;
    if (s1->mFile < s2->mFile) return -1;
    if (s1->mFile > s2->mFile) return +1;
    if (s1->mLine < s2->mLine) return -1;
    if (s1->mLine > s2->mLine) return +1;
    if (s1->mColumn < s2->mColumn) return -1;
    if (s1->mColumn > s2->mColumn) return +1;
    if (s1->mAddress < s2->mAddress) return -1;
    if (s1->mAddress > s2->mAddress) return +1;
    return 0;
}

static void compute_reverse_lookup_indices(CompUnit * Unit) {
    U4_T i;
    qsort(Unit->mStates, Unit->mStatesCnt, sizeof(LineNumbersState), state_address_comparator);
    Unit->mStatesIndex = (LineNumbersState **)loc_alloc(sizeof(LineNumbersState *) * Unit->mStatesCnt);
    for (i = 0; i < Unit->mStatesCnt; i++) Unit->mStatesIndex[i] = Unit->mStates + i;
    qsort(Unit->mStatesIndex, Unit->mStatesCnt, sizeof(LineNumbersState *), state_text_pos_comparator);
    for (i = 1; i < Unit->mStatesCnt; i++) {
        LineNumbersState * s = Unit->mStatesIndex[i - 1];
        LineNumbersState * n = Unit->mStatesIndex[i];
        s->mNext = n - Unit->mStates;
    }
}

static void load_line_numbers_v1(CompUnit * Unit, U4_T unit_size) {
    LineNumbersState state;
    ELF_Section * s = NULL;
    ContextAddress addr = 0;
    U4_T line = 0;

    memset(&state, 0, sizeof(state));
    addr = (ContextAddress)dio_ReadAddress(&s);
    while (dio_GetPos() < Unit->mLineInfoOffs + unit_size) {
        state.mLine = dio_ReadU4();
        state.mColumn = dio_ReadU2();
        if (state.mColumn == 0xffffu) state.mColumn = 0;
        state.mAddress = addr + dio_ReadU4();
        if (state.mLine == 0) {
            state.mLine = line + 1;
            state.mColumn = 0;
        }
        add_state(Unit, &state);
        line = state.mLine;
    }
}

static void load_line_numbers_v2(CompUnit * Unit, U8_T unit_size, int dwarf64) {
    U8_T header_pos = 0;
    U1_T opcode_base = 0;
    U1_T opcode_size[256];
    U8_T header_size = 0;
    U1_T min_instruction_length = 0;
    U1_T is_stmt_default = 0;
    I1_T line_base = 0;
    U1_T line_range = 0;
    LineNumbersState state;

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
        FileInfo file;
        memset(&file, 0, sizeof(file));
        file.mName = dio_ReadString();
        if (file.mName == NULL) break;
        dir = dio_ReadULEB128();
        if (dir > 0 && dir <= Unit->mDirsCnt) file.mDir = Unit->mDirs[dir - 1];
        file.mModTime = dio_ReadULEB128();
        file.mSize = dio_ReadULEB128();
        add_file(Unit, &file);
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
                FileInfo file;
                memset(&file, 0, sizeof(file));
                file.mName = dio_ReadString();
                dir = dio_ReadULEB128();
                if (dir > 0 && dir <= Unit->mDirsCnt) file.mDir = Unit->mDirs[dir - 1];
                file.mModTime = dio_ReadULEB128();
                file.mSize = dio_ReadULEB128();
                add_file(Unit, &file);
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
}

void load_line_numbers(CompUnit * Unit) {
    Trap trap;
    DWARFCache * Cache = (DWARFCache *)Unit->mFile->dwarf_dt_cache;
    ELF_Section * LineInfoSection = Unit->mDesc.mVersion <= 1 ? Cache->mDebugLineV1 : Cache->mDebugLine;
    if (LineInfoSection == NULL) return;
    if (Unit->mStates != NULL || Unit->mFiles != NULL || Unit->mDirs != NULL) return;
    if (elf_load(LineInfoSection)) exception(errno);
    dio_EnterSection(&Unit->mDesc, LineInfoSection, Unit->mLineInfoOffs);
    if (set_trap(&trap)) {
        U8_T unit_size = 0;
        FileInfo file;
        memset(&file, 0, sizeof(file));
        file.mDir = Unit->mDir;
        file.mName = Unit->mObject->mName;
        add_file(Unit, &file);
        /* Read header */
        unit_size = dio_ReadU4();
        if (Unit->mDesc.mVersion <= 1) {
            /* DWARF 1.1 */
            load_line_numbers_v1(Unit, (U4_T)unit_size);
        }
        else {
            /* DWARF 2+ */
            int dwarf64 = 0;
            if (unit_size == 0xffffffffu) {
                unit_size = dio_ReadU8();
                unit_size += 12;
                dwarf64 = 1;
            }
            else {
                unit_size += 4;
            }
            load_line_numbers_v2(Unit, unit_size, dwarf64);
        }
        dio_ExitSection();
        compute_reverse_lookup_indices(Unit);
        clear_trap(&trap);
    }
    else {
        dio_ExitSection();
        free_unit_cache(Unit);
        exception(trap.error);
    }
}

UnitAddressRange * find_comp_unit_addr_range(DWARFCache * cache, ContextAddress addr_min, ContextAddress addr_max) {
    unsigned l = 0;
    unsigned h = cache->mAddrRangesCnt;
    while (l < h) {
        unsigned k = (h + l) / 2;
        UnitAddressRange * rk = cache->mAddrRanges + k;
        if (rk->mAddr < addr_max && rk->mAddr + rk->mSize > addr_min) {
            int first = 1;
            if (k > 0) {
                UnitAddressRange * rp = rk - 1;
                first = rp->mAddr + rp->mSize <= addr_min;
            }
            if (first) return rk;
        }
        if (rk->mAddr >= addr_min) h = k;
        else l = k + 1;
    }
    return NULL;
}

#endif /* ENABLE_ELF */
