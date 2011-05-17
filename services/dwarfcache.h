/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
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
 * Cached data stays in memory at least until end of the current event dispatch cycle.
 * To lock data for longer period of time clients can use ELF_File.ref_cnt.
 *
 * Functions in this module use exceptions to report errors, see exceptions.h
 */
#ifndef D_dwarfcache
#define D_dwarfcache

#include <config.h>

#if ENABLE_ELF

#include <services/tcf_elf.h>
#include <services/dwarfio.h>
#include <framework/errors.h>

typedef struct FileInfo FileInfo;
typedef struct LocationInfo LocationInfo;
typedef struct ObjectInfo ObjectInfo;
typedef struct PubNamesInfo PubNamesInfo;
typedef struct PubNamesTable PubNamesTable;
typedef struct ObjectArray ObjectArray;
typedef struct SymbolInfo SymbolInfo;
typedef struct PropertyValue PropertyValue;
typedef struct LineNumbersState LineNumbersState;
typedef struct CompUnit CompUnit;
typedef struct SymbolSection SymbolSection;
typedef struct UnitAddressRange UnitAddressRange;
typedef struct FrameInfoRange FrameInfoRange;
typedef struct DWARFCache DWARFCache;

struct FileInfo {
    char * mName;
    char * mDir;
    U4_T mModTime;
    U4_T mSize;
    unsigned mNameHash;
};

#define SYM_HASH_SIZE (32 * MEM_USAGE_FACTOR - 1)

struct SymbolSection {
    ELF_File * mFile;
    unsigned mIndex;
    char * mStrPool;
    size_t mStrPoolSize;
    unsigned mSymCount;
    ElfX_Sym * mSymPool;    /* pointer to ELF section data: array of Elf32_Sym or Elf64_Sym */
    size_t mSymPoolSize;
    unsigned * mSymNamesHash;
    unsigned * mSymNamesNext;
};

struct SymbolInfo {
    SymbolSection * mSymSection;
    U4_T mSectionIndex;
    ELF_Section * mSection;
    char * mName;
    U1_T mBind;
    U1_T mType;
    U8_T mValue;
    U8_T mSize;
};

#define TAG_fund_type 0x2000

struct ObjectInfo {
    ObjectInfo * mHashNext;
    ObjectInfo * mSibling;
    ObjectInfo * mChildren;
    ObjectInfo * mParent;

    U8_T mID; /* Link-time debug information entry address: address of .debug_info section + offset in the section */
    U2_T mTag;

    U2_T mFundType;
    ObjectInfo * mType;
    CompUnit * mCompUnit;
    char * mName;
};

#define OBJECT_ARRAY_SIZE 128

struct ObjectArray {
    ObjectArray * mNext;
    ObjectInfo mArray[OBJECT_ARRAY_SIZE];
};

struct PubNamesInfo {
    unsigned mNext;
    U8_T mID;
};

struct PubNamesTable {
    unsigned * mHash;
    PubNamesInfo * mNext;
    unsigned mCnt;
    unsigned mMax;
};

struct PropertyValue {
    Context * mContext;
    int mFrame;
    ObjectInfo * mObject;
    U2_T mAttr;
    U2_T mForm;
    U8_T mValue;
    U1_T * mAddr;
    size_t mSize;
    int mBigEndian;
    RegisterDefinition * mRegister;
};

#define LINE_IsStmt         0x01
#define LINE_BasicBlock     0x02
#define LINE_PrologueEnd    0x04
#define LINE_EpilogueBegin  0x08
#define LINE_EndSequence    0x10

struct LineNumbersState {
    ContextAddress mAddress;
    char * mFileName;
    U4_T mNext;
    U4_T mFile;
    U4_T mLine;
    U2_T mColumn;
    U1_T mFlags;
    U1_T mISA;
};

struct CompUnit {
    ObjectInfo * mObject;

    ELF_File * mFile;
    ELF_Section * mTextSection;

    ContextAddress mLowPC;
    ContextAddress mHighPC;

    DIO_UnitDescriptor mDesc;
    RegisterIdScope mRegIdScope;

    U8_T mDebugRangesOffs;
    U8_T mLineInfoOffs;
    char * mDir;

    U4_T mFilesCnt;
    U4_T mFilesMax;
    FileInfo * mFiles;

    U4_T mDirsCnt;
    U4_T mDirsMax;
    char ** mDirs;

    U4_T mStatesCnt;
    U4_T mStatesMax;
    LineNumbersState * mStates;
    LineNumbersState ** mStatesIndex;

    CompUnit * mBaseTypes;

    U1_T mARangesFound;
};

/* Address range of a compilation unit. A unit can occupy multiple address ranges. */
struct UnitAddressRange {
    CompUnit * mUnit;       /* Compilation unit */
    ELF_Section * mSection; /* ELF file secdtion that contains the range */
    ContextAddress mAddr;   /* Link-time start address of the range */
    ContextAddress mSize;   /* Size of the range */
};

struct FrameInfoRange {
    ContextAddress mAddr;
    ContextAddress mSize;
    U8_T mOffset;
};

#define DWARF_CACHE_MAGIC 0x34625490

struct DWARFCache {
    int magic;
    ELF_File * mFile;
    ErrorReport * mErrorReport;
    ObjectInfo * mCompUnits;
    ELF_Section * mDebugLineV1;
    ELF_Section * mDebugLine;
    ELF_Section * mDebugLoc;
    ELF_Section * mDebugRanges;
    ELF_Section * mDebugFrame;
    ELF_Section * mEHFrame;
    SymbolSection ** mSymSections;
    unsigned mSymSectionsCnt;
    unsigned mSymSectionsMax;
    ObjectInfo ** mObjectHash;
    unsigned mObjectHashSize;
    ObjectArray * mObjectList;
    unsigned mObjectArrayPos;
    UnitAddressRange * mAddrRanges;
    unsigned mAddrRangesCnt;
    unsigned mAddrRangesMax;
    PubNamesTable mPubNames;
    PubNamesTable mPubTypes;
    FrameInfoRange * mFrameInfoRanges;
    unsigned mFrameInfoRangesCnt;
    unsigned mFrameInfoRangesMax;
};

/* Return DWARF cache for given file, create and populate the cache if needed, throw an exception if error */
extern DWARFCache * get_dwarf_cache(ELF_File * file);

/* Return symbol name hash. The hash is used to build mSymNamesHash table. */
extern unsigned calc_symbol_name_hash(const char * s);

/* Compare symbol names. */
extern int cmp_symbol_names(const char * x, const char * y);

/* Return file name hash. The hash is used to search FileInfo. */
extern unsigned calc_file_name_hash(const char * s);

/* Load line number information for given compilation unit, throw an exception if error */
extern void load_line_numbers(CompUnit * unit);

/* Find ObjectInfo by ID */
extern ObjectInfo * find_object(DWARFCache * cache, U8_T ID);

/* Search and return first compilation unit address range in given link-time address range 'addr_min'..'addr_max'. */
extern UnitAddressRange * find_comp_unit_addr_range(DWARFCache * cache, ContextAddress addr_min, ContextAddress addr_max);

/* Get SymbolInfo */
extern void unpack_elf_symbol_info(SymbolSection * section, U4_T index, SymbolInfo * info);

/*
 * Read and evaluate a property of a DWARF object, perform ELF relocations if any.
 * FORM_ADDR values are mapped to run-time address space.
 */
extern void read_and_evaluate_dwarf_object_property(Context * ctx, int frame, U8_T base, ObjectInfo * obj, U2_T attr_tag, PropertyValue * value);

/* Convert PropertyValue to a number */
extern U8_T get_numeric_property_value(PropertyValue * Value);

#endif /* ENABLE_ELF */

#endif /* D_dwarfcache */
