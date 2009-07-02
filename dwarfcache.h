/*******************************************************************************
 * Copyright (c) 2006-2009 Wind River Systems, Inc. and others.
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

#include "config.h"

#if ENABLE_ELF

#include "context.h"
#include "tcf_elf.h"
#include "dwarfio.h"

typedef struct FileInfo FileInfo;
typedef struct LocationInfo LocationInfo;
typedef struct ObjectInfo ObjectInfo;
typedef struct PropertyValue PropertyValue;
typedef struct LineNumbersState LineNumbersState;
typedef struct CompUnit CompUnit;
typedef struct SymbolSection SymbolSection;
typedef struct DWARFCache DWARFCache;


struct FileInfo {
    char * mName;
    char * mDir;
    U4_T mModTime;
    U4_T mSize;
};

#define SYM_HASH_SIZE 1023

struct SymbolSection {
    ELF_File * mFile;
    unsigned mIndex;
    char * mStrPool;
    size_t mStrPoolSize;
    unsigned sym_cnt;
    Elf_Sym * mSymPool;    /* pointer to ELF section data: array of Elf32_Sym or Elf64_Sym */
    size_t mSymPoolSize;
    unsigned mSymbolHash[SYM_HASH_SIZE];
    unsigned * mHashNext;
};

struct ObjectInfo {
    ObjectInfo * mHashNext;
    ObjectInfo * mListNext;
    ObjectInfo * mSibling;
    ObjectInfo * mChildren;
    ObjectInfo * mParent;

    U2_T mTag;
    U8_T mID;

    ContextAddress mLowPC;
    ContextAddress mHighPC;

    U2_T mEncoding;
    ObjectInfo * mType;
    CompUnit * mCompUnit;
    char * mName;
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
    int (*mAccessFunc)(PropertyValue *, int, U8_T *);
};

#define LINE_IsStmt         0x01
#define LINE_BasicBlock     0x02
#define LINE_PrologueEnd    0x04
#define LINE_EpilogueBegin  0x08
#define LINE_EndSequence    0x10

struct LineNumbersState {
    ContextAddress mAddress;
    U4_T mFile;
    U4_T mLine;
    U2_T mColumn;
    U1_T mFlags;
    U1_T mISA;
};

struct CompUnit {
    ELF_File * mFile;
    ELF_Section * mSection;

    U8_T mID;
    ContextAddress mLowPC;
    ContextAddress mHighPC;

    DIO_UnitDescriptor mDesc;

    U8_T mDebugRangesOffs;
    U8_T mLineInfoOffs;
    char * mName;
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

    CompUnit * mBaseTypes;
    ObjectInfo * mChildren;
};

#define SYM_CACHE_MAGIC         0x84625490

struct DWARFCache {
    int magic;
    ELF_File * mFile;
    int mErrorCode;
    char mErrorMsg[256];
    CompUnit ** mCompUnits;
    unsigned mCompUnitsCnt;
    ELF_Section * mDebugRanges;
    ELF_Section * mDebugARanges;
    ELF_Section * mDebugLine;
    ELF_Section * mDebugLoc;
    SymbolSection ** mSymSections;
    unsigned mSymSectionsCnt;
    unsigned mSymSectionsLen;
    ObjectInfo ** mObjectHash;
    ObjectInfo * mObjectList;
    Elf_Sym ** mSymbolHash;
    unsigned mSymbolTableLen;
    DWARFCache * mLineInfoNext;
};

/* Return DWARF cache for given file, create and populate the cache if needed, throw an exception if error */
extern DWARFCache * get_dwarf_cache(ELF_File * file);

extern unsigned calc_symbol_name_hash(char * s);

/* Load line number information for given compilation unit, throw an exception if error */
extern void load_line_numbers(DWARFCache * cache, CompUnit * unit);

/* Find ObjectInfo by ID */
extern ObjectInfo * find_object(DWARFCache * cache, U8_T ID);

/* Read a property of a DWARF object, on error set errno and return -1 */
extern int read_dwarf_object_property(Context * ctx, int frame, ObjectInfo * obj, int attr_tag, PropertyValue * value);

/* Read and evaluate a property of a DWARF object, on error set errno and return -1 */
extern int read_and_evaluate_dwarf_object_property(Context * ctx, int frame, U8_T base, ObjectInfo * obj, int attr_tag, PropertyValue * value);

extern U8_T get_numeric_property_value(PropertyValue * Value);

#endif /* ENABLE_ELF */

#endif /* D_dwarfcache */
