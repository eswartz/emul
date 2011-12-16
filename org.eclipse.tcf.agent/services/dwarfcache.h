/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc. and others.
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

#if ENABLE_ELF && ENABLE_DebugContext

#include <services/tcf_elf.h>
#include <services/dwarfio.h>
#include <framework/errors.h>

typedef struct FileInfo FileInfo;
typedef struct ObjectInfo ObjectInfo;
typedef struct PubNamesInfo PubNamesInfo;
typedef struct PubNamesTable PubNamesTable;
typedef struct ObjectArray ObjectArray;
typedef struct SymbolInfo SymbolInfo;
typedef struct PropertyValuePiece PropertyValuePiece;
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
    FileInfo * mNextInHash;
    CompUnit * mCompUnit;
};

#define TAG_fund_type     0x2000
#define TAG_index_range   0x2001
#define TAG_mod_pointer   0x2002
#define TAG_mod_reference 0x2003

struct ObjectInfo {
    ObjectInfo * mHashNext;
    ObjectInfo * mSibling;
    ObjectInfo * mChildren;
    ObjectInfo * mParent;

    U8_T mID; /* Link-time debug information entry address: address of .debug_info section + offset in the section */
    U2_T mTag;
    CompUnit * mCompUnit;
    ObjectInfo * mType;
    char * mName;

    union {
        U2_T mFundType;
        struct {
            ContextAddress mLowPC;
            ContextAddress mHighPC;
        } mAddr;
        struct {
            U2_T mFmt;
            union {
                I8_T mValue;
                struct {
                    U1_T * mAddr;
                    size_t mSize;
                } mExpr;
            } mLow;
            union {
                I8_T mValue;
                struct {
                    U1_T * mAddr;
                    size_t mSize;
                } mExpr;
            } mHigh;
        } mRange;
    } u;
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

struct PropertyValuePiece {
    int mBigEndian;
    ContextAddress mAddress;
    RegisterDefinition * mRegister;
    U4_T mBitOffset;
    U4_T mBitSize;
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
    PropertyValuePiece * mPieces;
    U4_T mPieceCnt;
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

    U2_T mLanguage;
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
    U1_T mLineInfoLoaded;

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
    unsigned mFileInfoHashSize;
    FileInfo ** mFileInfoHash;
};

/* Return DWARF cache for given file, create and populate the cache if needed, throw an exception if error */
extern DWARFCache * get_dwarf_cache(ELF_File * file);

/* Return file name hash. The hash is used to search FileInfo. */
extern unsigned calc_file_name_hash(const char * s);

/* Load line number information for given compilation unit, throw an exception if error */
extern void load_line_numbers(CompUnit * unit);

/* Find ObjectInfo by ID */
extern ObjectInfo * find_object(DWARFCache * cache, U8_T ID);

/* Search and return first compilation unit address range in given link-time address range 'addr_min'..'addr_max' (inclusive). */
extern UnitAddressRange * find_comp_unit_addr_range(DWARFCache * cache, ContextAddress addr_min, ContextAddress addr_max);

/*
 * Read and evaluate a property of a DWARF object, perform ELF relocations if any.
 * FORM_ADDR values are mapped to run-time address space.
 */
extern void read_and_evaluate_dwarf_object_property(Context * ctx, int frame, U8_T base, ObjectInfo * obj, U2_T attr_tag, PropertyValue * value);

/* Convert PropertyValue to a number */
extern U8_T get_numeric_property_value(PropertyValue * Value);

/*
 * Search and return first compilation unit address range in given run-time address range 'addr_min'..'addr_max' (inclusive).
 * If 'range_rt_addr' not NULL, *range_rt_addr is assigned run-time address of the range.
 */
extern struct UnitAddressRange * elf_find_unit(Context * ctx, ContextAddress addr_min, ContextAddress addr_max, ContextAddress * range_rt_addr);

#endif /* ENABLE_ELF && ENABLE_DebugContext */

#endif /* D_dwarfcache */
