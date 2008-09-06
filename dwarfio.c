/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
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
 * This module implements low-level functions for reading DWARF debug information.
 *
 * Functions in this module use exceptions to report errors, see exceptions.h
 */

#include "mdep.h"
#include "config.h"

#if ENABLE_ELF

#include <assert.h>
#include <string.h>
#include "dwarfio.h"
#include "dwarf.h"
#include "myalloc.h"
#include "exceptions.h"

#define ABBREV_TABLE_SIZE       127

struct DIO_Abbreviation {
    U2_T mTag;
    U1_T mChildren;
    U4_T mAttrLen;
    U2_T mAttrs[2];
};

typedef struct DIO_Abbreviation DIO_Abbreviation;

struct DIO_AbbrevSet {
    U8_T mOffset;
    U4_T mSize;
    DIO_Abbreviation ** mTable;
    struct DIO_AbbrevSet * mNext;
};

typedef struct DIO_AbbrevSet DIO_AbbrevSet;

struct DIO_Cache {
    U1_T * mStringTable;
    U4_T mStringTableSize;
    DIO_AbbrevSet ** mAbbrevTable;
};

typedef struct DIO_Cache DIO_Cache;

U2_T dio_gVersion = 0;
U1_T dio_g64bit = 0;
U1_T dio_gAddressSize = 4;
U8_T dio_gUnitPos = 0;
U4_T dio_gUnitSize = 0;
U8_T dio_gEntryPos = 0;

U8_T dio_gFormRef = 0;
U8_T dio_gFormData = 0;
size_t dio_gFormDataSize = 0;
void * dio_gFormDataAddr = NULL;

static ELF_Section * sSection;
static U1_T sBigEndian;
static DIO_Abbreviation ** sAbbrevTable = NULL;
static U4_T sAbbrevTableSize = 0;
static U1_T * sData;
static U8_T sDataPos;
static U8_T sDataLen;

static void dio_CloseELF(ELF_File * File) {
    U4_T n, m;
    DIO_Cache * Cache = (DIO_Cache *)File->dwarf_io_cache;

    if (Cache == NULL) return;
    if (Cache->mAbbrevTable != NULL) {
        for (n = 0; n < ABBREV_TABLE_SIZE; n++) {
            DIO_AbbrevSet * Set = Cache->mAbbrevTable[n];
            while (Set != NULL) {
                DIO_AbbrevSet * Next = Set->mNext;
                for (m = 0; m < Set->mSize; m++) {
                    loc_free(Set->mTable[m]);
                }
                loc_free(Set->mTable);
                loc_free(Set);
                Set = Next;
            }
        }
        loc_free(Cache->mAbbrevTable);
    }
    loc_free(Cache);
    File->dwarf_io_cache = NULL;
}

static DIO_Cache * dio_GetCache(ELF_File * File) {
    static int Inited = 0;
    DIO_Cache * Cache = (DIO_Cache *)File->dwarf_io_cache;

    if (!Inited) {
        elf_add_close_listener(dio_CloseELF);
        Inited = 1;
    }
    if (Cache == NULL) {
        Cache = (DIO_Cache *)(File->dwarf_io_cache = loc_alloc_zero(sizeof(DIO_Cache)));
    }
    return Cache;
}

void dio_EnterSection(ELF_Section * Section, U8_T Offset) {
    if (elf_load(Section)) exception(errno);
    sSection = Section;
    sData = Section->data;
    sDataPos = Offset;
    sDataLen = Section->size;
    sBigEndian = Section->file->big_endian;
    dio_gVersion = 0;
    dio_g64bit = 0;
    dio_gAddressSize = 4;
    dio_gUnitPos = 0;
    dio_gUnitSize = 0;
    dio_gEntryPos = 0;
}

void dio_EnterSectionData(ELF_File * File, U1_T * Data, U8_T Offset, U8_T Size) {
    sSection = NULL;
    sData = Data;
    sDataPos = Offset;
    sDataLen = Size;
    sBigEndian = File->big_endian;
    dio_gVersion = 0;
    dio_g64bit = 0;
    dio_gAddressSize = 4;
    dio_gUnitPos = 0;
    dio_gUnitSize = 0;
    dio_gEntryPos = 0;
}

void dio_ExitSection() {
    sSection = NULL;
    sDataPos = 0;
    sDataLen = 0;
    sData = NULL;
}

U8_T dio_GetPos() {
    return sDataPos;
}

void dio_Skip(I8_T Bytes) {
    if (sDataPos + Bytes > sDataLen) exception(ERR_EOF);
    sDataPos += Bytes;
}

void dio_Read(U1_T * Buf, U4_T Size) {
    if (sDataPos + Size > sDataLen) exception(ERR_EOF);
    memcpy(Buf, sData + sDataPos, Size);
    sDataPos += Size;
}

static U1_T dio_ReadU1F(void) {
    if (sDataPos >= sDataLen) exception(ERR_EOF);
    return sData[sDataPos++];
}

U1_T dio_ReadU1(void) {
    return sDataPos < sDataLen ? sData[sDataPos++] : dio_ReadU1F();
}

#define dio_ReadU1() (sDataPos < sDataLen ? sData[sDataPos++] : dio_ReadU1F())

U2_T dio_ReadU2(void) {
    U1_T x0 = dio_ReadU1();
    U1_T x1 = dio_ReadU1();
    return sBigEndian ? (x0 << 8) | x1 : x0 | (x1 << 8);
}

U4_T dio_ReadU4(void) {
    U2_T x0 = dio_ReadU2();
    U2_T x1 = dio_ReadU2();
    return sBigEndian ? (x0 << 16) | x1 : x0 | (x1 << 16);
}

U8_T dio_ReadU8(void) {
    U8_T x0 = dio_ReadU4();
    U8_T x1 = dio_ReadU4();
    return sBigEndian ? (x0 << 32) | x1 : x0 | (x1 << 32);
}

U4_T dio_ReadULEB128(void) {
    U4_T Res = 0;
    int i = 0;
    for (;; i += 7) {
        U1_T n = dio_ReadU1();
        Res |= (n & 0x7Fu) << i;
        if ((n & 0x80) == 0) break;
    }
    return Res;
}

I4_T dio_ReadSLEB128(void) {
    U4_T Res = 0;
    int i = 0;
    for (;; i += 7) {
        U1_T n = dio_ReadU1();
        Res |= (n & 0x7Fu) << i;
        if ((n & 0x80) == 0) {
            Res |= -(n & 0x40) << i;
            break;
        }
    }
    return (I4_T)Res;
}

U8_T dio_ReadU8LEB128(void) {
    U8_T Res = 0;
    int i = 0;
    for (;; i += 7) {
        U1_T n = dio_ReadU1();
        Res |= (n & 0x7Fu) << i;
        if ((n & 0x80) == 0) break;
    }
    return Res;
}

I8_T dio_ReadS8LEB128(void) {
    U8_T Res = 0;
    int i = 0;
    for (;; i += 7) {
        U1_T n = dio_ReadU1();
        Res |= (n & 0x7Fu) << i;
        if ((n & 0x80) == 0) {
            Res |= -(n & 0x40) << i;
            break;
        }
    }
    return (I8_T)Res;
}

U8_T dio_ReadUX(int Size) {
    switch (Size) {
    case 2:
        return dio_ReadU2();
    case 4:
        return dio_ReadU4();
    case 8:
        return dio_ReadU8();
    default:
        assert(0);
        return 0;
    }
}

U8_T dio_ReadAddress(void) {
    switch (dio_gAddressSize) {
    case 2:
        return dio_ReadU2();
    case 4:
        return dio_ReadU4();
    case 8:
        return dio_ReadU8();
    default:
        assert(0);
        return 0;
    }
}

char * dio_ReadString(void) {
    char * Res = (char *)(sData + sDataPos);
    U4_T Length = 0;
    while (dio_ReadU1() != 0) Length++;
    if (Length == 0) return NULL;
    return Res;
}

static U1_T * dio_LoadStringTable(U4_T * StringTableSize) {
    ELF_File * File = sSection->file;
    DIO_Cache * Cache = dio_GetCache(File);

    if (Cache->mStringTable == NULL) {
        U4_T ID;
        ELF_Section * Section = NULL;

        for (ID = 1; ID < File->section_cnt; ID++) {
            if (strcmp(File->sections[ID]->name, ".debug_str") == 0) {
                if (Section != NULL) {
                    str_exception(ERR_INV_DWARF, "more then one .debug_str section in a file");
                }
                Section = File->sections[ID];
                assert(Section->file == File);
            }
        }

        if (Section == NULL) {
            str_exception(ERR_INV_DWARF, "section .debug_str not found");
        }

        Cache->mStringTableSize = (size_t)Section->size;
        if (elf_load(Section) < 0) {
            str_exception(ERR_INV_DWARF, "invalid .debug_str section");
        }
        Cache->mStringTable = Section->data;
    }

    *StringTableSize = Cache->mStringTableSize;
    return Cache->mStringTable;
}

static void dio_ReadFormAddr(void) {
    dio_gFormRef = dio_ReadAddress();
}

static void dio_ReadFormBlock(U4_T Size) {
    dio_gFormDataAddr = sData + sDataPos;
    dio_gFormDataSize = Size;
    if (sDataPos + Size > sDataLen) exception(ERR_EOF);
    sDataPos += Size;
}

static void dio_ReadFormData(U1_T Size, U8_T Data) {
    dio_gFormDataAddr = sData + sDataPos - Size;
    dio_gFormData = Data;
    dio_gFormDataSize = Size;
}

static void dio_ReadFormRef(void) {
    dio_gFormRef = dio_ReadU4();
}

static void dio_ReadFormRelRef(U8_T Offset) {
    if (dio_gUnitSize > 0 && Offset >= dio_gUnitSize) {
        str_exception(ERR_INV_DWARF, "invalid REF attribute value");
    }
    dio_gFormRef = sSection->addr + dio_gUnitPos + Offset;
}

static void dio_ReadFormRefAddr(void) {
    U4_T Size = dio_gAddressSize;
    if (dio_gVersion >= 3) Size = dio_g64bit ? 8 : 4;
    dio_gFormRef = dio_ReadUX(Size);
}

static void dio_ReadFormString(void) {
    dio_gFormDataAddr = sData + sDataPos;
    dio_gFormDataSize = 1;
    while (dio_ReadU1()) dio_gFormDataSize++;
}

static void dio_ReadFormStringRef(void) {
    U8_T Offset = dio_ReadUX(dio_g64bit ? 8 : 4);
    U4_T StringTableSize = 0;
    U1_T * StringTable = dio_LoadStringTable(&StringTableSize);
    dio_gFormDataAddr = StringTable + Offset;
    dio_gFormDataSize = 1;
    for (;;) {
        if (Offset >= StringTableSize) {
            str_exception(ERR_INV_DWARF, "invalid FORM_STRP attribute");
        }
        if (StringTable[Offset++] == 0) break;
        dio_gFormDataSize++;
    }
}

static void dio_ReadAttribute(U2_T Attr, U2_T Form) {
    dio_gFormDataAddr = NULL;
    dio_gFormDataSize = 0;
    dio_gFormData = 0;
    dio_gFormRef = 0;
    switch (Form) {
    case FORM_ADDR      : dio_ReadFormAddr(); break;
    case FORM_REF       : dio_ReadFormRef(); break;
    case FORM_BLOCK1    : dio_ReadFormBlock(dio_ReadU1()); break;
    case FORM_BLOCK2    : dio_ReadFormBlock(dio_ReadU2()); break;
    case FORM_BLOCK4    : dio_ReadFormBlock(dio_ReadU4()); break;
    case FORM_BLOCK     : dio_ReadFormBlock(dio_ReadULEB128()); break;
    case FORM_DATA1     : dio_ReadFormData(1, dio_ReadU1()); break;
    case FORM_DATA2     : dio_ReadFormData(2, dio_ReadU2()); break;
    case FORM_DATA4     : dio_ReadFormData(4, dio_ReadU4()); break;
    case FORM_DATA8     : dio_ReadFormData(8, dio_ReadU8()); break;
    case FORM_SDATA     : dio_ReadFormData(8, dio_ReadS8LEB128()); dio_gFormDataAddr = NULL; break;
    case FORM_UDATA     : dio_ReadFormData(8, dio_ReadU8LEB128()); dio_gFormDataAddr = NULL; break;
    case FORM_FLAG      : dio_ReadFormData(1, dio_ReadU1()); break;
    case FORM_STRING    : dio_ReadFormString(); break;
    case FORM_STRP      : dio_ReadFormStringRef(); break;
    case FORM_REF_ADDR  : dio_ReadFormRefAddr(); break;
    case FORM_REF1      : dio_ReadFormRelRef(dio_ReadU1()); break;
    case FORM_REF2      : dio_ReadFormRelRef(dio_ReadU2()); break;
    case FORM_REF4      : dio_ReadFormRelRef(dio_ReadU4()); break;
    case FORM_REF8      : dio_ReadFormRelRef(dio_ReadU8()); break;
    case FORM_REF_UDATA : dio_ReadFormRelRef(dio_ReadULEB128()); break;
    default: str_exception(ERR_INV_DWARF, "invalid FORM");
    }
}

void dio_ReadEntry(DIO_EntryCallBack CallBack) {
    DIO_Abbreviation * Abbr = NULL;
    U2_T Tag = 0;
    U4_T AttrPos = 0;
    U4_T EntrySize = 0;
    int Init = 1;
    dio_gEntryPos = dio_GetPos();
    if (dio_gVersion >= 2) {
        U4_T AbbrCode = dio_ReadULEB128();
        if (AbbrCode == 0) return;
        if (AbbrCode >= sAbbrevTableSize || sAbbrevTable[AbbrCode] == NULL) {
            str_exception(ERR_INV_DWARF, "invalid abbreviation table");
        }
        Abbr =  sAbbrevTable[AbbrCode];
        Tag = Abbr->mTag;
    }
    else {
        EntrySize = dio_ReadU4();
        if (EntrySize < 8) {
            while (EntrySize > 4) {
                dio_ReadU1();
                EntrySize--;
            }
            return;
        }
        Tag = dio_ReadU2();
    }
    for (;;) {
        U2_T Attr = 0;
        U2_T Form = 0;
        if (Init) {
            Form = 1;
            Init = 0;
        }
        else if (Abbr != NULL) {
            if (AttrPos < Abbr->mAttrLen) {
                Attr = Abbr->mAttrs[AttrPos++];
                Form = Abbr->mAttrs[AttrPos++];
                if (Form == FORM_INDIRECT) Form = (U2_T)dio_ReadULEB128();
            }
        }
        else {
            if (dio_GetPos() < dio_gEntryPos + EntrySize) {
                Attr = dio_ReadU2();
                Form = Attr & 0xF;
                Attr = (Attr & 0xfff0) >> 4;
            }
        }
        if (Attr != 0 && Form != 0) dio_ReadAttribute(Attr, Form);
        if (Tag == TAG_compile_unit) {
            if (Attr == AT_sibling && dio_gUnitSize == 0) {
                dio_ChkRef(Form);
                assert(dio_gVersion == 1);
                dio_gUnitSize = (U4_T)(dio_gFormRef - sSection->addr - dio_gUnitPos);
                assert(dio_gUnitPos < dio_GetPos());
                assert(dio_gUnitPos + dio_gUnitSize >= dio_GetPos());
            }
            else if (Attr == 0 && Form == 0) {
                if (dio_gUnitSize == 0) str_exception(ERR_INV_DWARF, "missing compilation unit sibling attribute");
            }
        }
        CallBack(Tag, Attr, Form);
        if (Attr == 0 && Form == 0) break;
    }
}

static void dio_FindAbbrevTable(U4_T Offset, DIO_Abbreviation *** AbbrevTable, U4_T * AbbrevTableSize);

void dio_ReadUnit(DIO_EntryCallBack CallBack) {
    dio_gUnitPos = dio_GetPos();
    dio_g64bit = 0;
    if (dio_gVersion >= 2) {
        dio_gUnitSize = dio_ReadU4();
        if (dio_gUnitSize == 0xffffffffu) {
            dio_g64bit = 1;
            str_exception(ERR_INV_DWARF, "64-bit DWARF is not supported yet");
        }
        else {
            dio_gUnitSize += 4;
        }
        dio_gVersion = dio_ReadU2();
        dio_FindAbbrevTable(dio_ReadU4(), &sAbbrevTable, &sAbbrevTableSize);
        dio_gAddressSize = dio_ReadU1();
    }
    else {
        dio_gUnitSize = 0;
        dio_gVersion = 1;
        dio_gAddressSize = 4;
        sAbbrevTable = NULL;
        sAbbrevTableSize = 0;
    }
    while (dio_gUnitSize == 0 || dio_GetPos() < dio_gUnitPos + dio_gUnitSize) {
        dio_ReadEntry(CallBack);
    }
}

#define dio_AbbrevTableHash(Offset) (((unsigned)(Offset)) / 16 % ABBREV_TABLE_SIZE)

void dio_LoadAbbrevTable(ELF_File * File) {
    U4_T ID;
    U8_T TableOffset = 0;
    ELF_Section * Section = NULL;
    static U2_T * AttrBuf = NULL;
    static U4_T AttrBufSize = 0;
    DIO_Abbreviation ** AbbrevTable = NULL;
    U4_T AbbrevTableSize = 0;
    DIO_Cache * Cache = dio_GetCache(File);

    if (Cache->mAbbrevTable != NULL) return;
    Cache->mAbbrevTable = (DIO_AbbrevSet **)loc_alloc_zero(sizeof(DIO_AbbrevSet *) * ABBREV_TABLE_SIZE);

    assert(sSection == NULL);
    for (ID = 1; ID < File->section_cnt; ID++) {
        if (strcmp(File->sections[ID]->name, ".debug_abbrev") == 0) {
            if (Section != NULL) {
                str_exception(ERR_INV_DWARF, "more then one .debug_abbrev section in a file");
            }
            Section = File->sections[ID];
        }
    }
    if (Section == NULL) return;
    dio_EnterSection(Section, 0);
    for (;;) {
        U4_T AttrPos = 0;
        U2_T Tag = 0;
        U1_T Children = 0;
        U4_T ID = dio_ReadULEB128();
        if (ID == 0) {
            /* End of compilation unit */
            U4_T Hash = dio_AbbrevTableHash(TableOffset);
            DIO_AbbrevSet * AbbrevSet = (DIO_AbbrevSet *)loc_alloc_zero(sizeof(DIO_AbbrevSet));
            AbbrevSet->mOffset = TableOffset;
            AbbrevSet->mTable = AbbrevTable;
            AbbrevSet->mSize = AbbrevTableSize;
            AbbrevSet->mNext = Cache->mAbbrevTable[Hash];
            Cache->mAbbrevTable[Hash] = AbbrevSet;
            AbbrevTable = NULL;
            AbbrevTableSize = 0;
            if (dio_GetPos() >= Section->size) break;
            TableOffset = dio_GetPos();
            continue;
        }
        if (ID >= 0x1000000) str_exception(ERR_INV_DWARF, "invalid abbreviation table");
        if (ID >= AbbrevTableSize) {
            U4_T Size = AbbrevTableSize;
            AbbrevTableSize = ID + 1024u;
            AbbrevTable = (DIO_Abbreviation **)loc_realloc(AbbrevTable, sizeof(DIO_Abbreviation *) * AbbrevTableSize);
            memset(AbbrevTable + Size, 0, sizeof(DIO_Abbreviation *) * (AbbrevTableSize - Size));
        }
        Tag = (U2_T)dio_ReadULEB128();
        Children = (U2_T)dio_ReadU1() != 0;
        for (;;) {
            U4_T Attr = dio_ReadULEB128();
            U4_T Form = dio_ReadULEB128();
            if (Attr >= 0x10000 || Form >= 0x10000) str_exception(ERR_INV_DWARF, "invalid abbreviation table");
            if (Attr == 0 && Form == 0) {
                DIO_Abbreviation * Abbr = AbbrevTable[ID];
                assert(Abbr == NULL);
                Abbr = (DIO_Abbreviation *)loc_alloc_zero(sizeof(DIO_Abbreviation) + sizeof(U2_T) * (AttrPos - 2));
                Abbr->mTag = Tag;
                Abbr->mChildren = Children;
                Abbr->mAttrLen = AttrPos;
                memcpy(Abbr->mAttrs, AttrBuf, sizeof(U2_T) * AttrPos);
                AbbrevTable[ID] = Abbr;
                break;
            }
            if (AttrBufSize < AttrPos + 2) {
                AttrBufSize = AttrPos + 256;
                AttrBuf = (U2_T *)loc_realloc(AttrBuf, sizeof(U2_T) * AttrBufSize);
            }
            AttrBuf[AttrPos++] = (U2_T)Attr;
            AttrBuf[AttrPos++] = (U2_T)Form;
        }
    }
    assert(AbbrevTable == NULL);
    assert(AbbrevTableSize == 0);
    dio_ExitSection();
}

static void dio_FindAbbrevTable(U4_T Offset, DIO_Abbreviation *** AbbrevTable, U4_T * AbbrevTableSize) {
    DIO_Cache * Cache = dio_GetCache(sSection->file);
    if (Cache->mAbbrevTable != NULL) {
        U4_T Hash = dio_AbbrevTableHash(Offset);
        DIO_AbbrevSet * AbbrevSet = Cache->mAbbrevTable[Hash];
        while (AbbrevSet != NULL) {
            if (AbbrevSet->mOffset == Offset) {
                *AbbrevTable = AbbrevSet->mTable;
                *AbbrevTableSize = AbbrevSet->mSize;
                return;
            }
            AbbrevSet = AbbrevSet->mNext;
        }
    }
    str_exception(ERR_INV_DWARF, "invalid abbreviation table offset");
    *AbbrevTable = NULL;
    *AbbrevTableSize = 0;
}

void dio_ChkFlag(U2_T Form) {
    switch (Form) {
    case FORM_FLAG      :
        return;
    }
    str_exception(ERR_INV_DWARF, "FORM_FLAG expected");
}

void dio_ChkRef(U2_T Form) {
    switch (Form) {
    case FORM_REF       :
    case FORM_REF_ADDR  :
    case FORM_REF1      :
    case FORM_REF2      :
    case FORM_REF4      :
    case FORM_REF8      :
    case FORM_REF_UDATA :
        return;
    }
    str_exception(ERR_INV_DWARF, "FORM_REF expected");
}

void dio_ChkAddr(U2_T Form) {
    switch (Form) {
    case FORM_ADDR      :
        return;
    }
    str_exception(ERR_INV_DWARF, "FORM_ADDR expected");
}

void dio_ChkData(U2_T Form) {
    switch (Form) {
    case FORM_DATA1     :
    case FORM_DATA2     :
    case FORM_DATA4     :
    case FORM_DATA8     :
    case FORM_SDATA     :
    case FORM_UDATA     :
        return;
    }
    str_exception(ERR_INV_DWARF, "FORM_DATA expected");
}

void dio_ChkBlock(U2_T Form, U1_T ** Buf, size_t * Size) {
    switch (Form) {
    case FORM_BLOCK1    :
    case FORM_BLOCK2    :
    case FORM_BLOCK4    :
    case FORM_BLOCK     :
    case FORM_DATA1     :
    case FORM_DATA2     :
    case FORM_DATA4     :
    case FORM_DATA8     :
    case FORM_STRING    :
    case FORM_STRP      :
        *Size = dio_gFormDataSize;
        *Buf = dio_gFormDataAddr;
        break;
    default:
        str_exception(ERR_INV_DWARF, "FORM_BLOCK expected");
    }
}

void dio_ChkString(U2_T Form) {
    if (Form == FORM_STRING) return;
    if (Form == FORM_STRP) return;
    str_exception(ERR_INV_DWARF, "FORM_STRING expected");
}

#endif
