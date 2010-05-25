/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
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
 * This module provides access to Windows Portable Executable debug information.
 */

#ifdef __cplusplus
extern "C" {
#endif

#ifndef D_windbgcache
#define D_windbgcache

#include <config.h>

#if defined(WIN32)

#if defined(__GNUC__)
#  include <imagehlp.h>
#else
#  define _NO_CVCONST_H
#  include <dbghelp.h>
#endif

#if defined(__GNUC__)
typedef enum _IMAGEHLP_SYMBOL_TYPE_INFO {
    TI_GET_SYMTAG,
    TI_GET_SYMNAME,
    TI_GET_LENGTH,
    TI_GET_TYPE,
    TI_GET_TYPEID,
    TI_GET_BASETYPE,
    TI_GET_ARRAYINDEXTYPEID,
    TI_FINDCHILDREN,
    TI_GET_DATAKIND,
    TI_GET_ADDRESSOFFSET,
    TI_GET_OFFSET,
    TI_GET_VALUE,
    TI_GET_COUNT,
    TI_GET_CHILDRENCOUNT,
    TI_GET_BITPOSITION,
    TI_GET_VIRTUALBASECLASS,
    TI_GET_VIRTUALTABLESHAPEID,
    TI_GET_VIRTUALBASEPOINTEROFFSET,
    TI_GET_CLASSPARENTID,
    TI_GET_NESTED,
    TI_GET_SYMINDEX,
    TI_GET_LEXICALPARENT,
    TI_GET_ADDRESS,
    TI_GET_THISADJUST,
    TI_GET_UDTKIND,
    TI_IS_EQUIV_TO,
    TI_GET_CALLING_CONVENTION,
    TI_IS_CLOSE_EQUIV_TO,
    TI_GTIEX_REQS_VALID,
    TI_GET_VIRTUALBASEOFFSET,
    TI_GET_VIRTUALBASEDISPINDEX,
    TI_GET_IS_REFERENCE,
    IMAGEHLP_SYMBOL_TYPE_INFO_MAX,
} IMAGEHLP_SYMBOL_TYPE_INFO;

typedef struct _TI_FINDCHILDREN_PARAMS {
    ULONG Count;
    ULONG Start;
    ULONG ChildId[1];
} TI_FINDCHILDREN_PARAMS;

enum SymTagEnum {
    SymTagNull,
    SymTagExe,
    SymTagCompiland,
    SymTagCompilandDetails,
    SymTagCompilandEnv,
    SymTagFunction,
    SymTagBlock,
    SymTagData,
    SymTagAnnotation,
    SymTagLabel,
    SymTagPublicSymbol,
    SymTagUDT,
    SymTagEnum,
    SymTagFunctionType,
    SymTagPointerType,
    SymTagArrayType,
    SymTagBaseType,
    SymTagTypedef,
    SymTagBaseClass,
    SymTagFriend,
    SymTagFunctionArgType,
    SymTagFuncDebugStart,
    SymTagFuncDebugEnd,
    SymTagUsingNamespace,
    SymTagVTableShape,
    SymTagVTable,
    SymTagCustom,
    SymTagThunk,
    SymTagCustomType,
    SymTagManagedType,
    SymTagDimension,
    SymTagMax
};

#define SYMFLAG_VALUEPRESENT     0x00000001
#define SYMFLAG_REGISTER         0x00000008
#define SYMFLAG_REGREL           0x00000010
#define SYMFLAG_FRAMEREL         0x00000020
#define SYMFLAG_PARAMETER        0x00000040
#define SYMFLAG_LOCAL            0x00000080
#define SYMFLAG_CONSTANT         0x00000100
#define SYMFLAG_EXPORT           0x00000200
#define SYMFLAG_FORWARDER        0x00000400
#define SYMFLAG_FUNCTION         0x00000800
#define SYMFLAG_VIRTUAL          0x00001000
#define SYMFLAG_THUNK            0x00002000
#define SYMFLAG_TLSREL           0x00004000
#define SYMFLAG_SLOT             0x00008000
#define SYMFLAG_ILREL            0x00010000
#define SYMFLAG_METADATA         0x00020000
#define SYMFLAG_CLR_TOKEN        0x00040000

typedef struct _SYMBOL_INFO {
    ULONG       SizeOfStruct;
    ULONG       TypeIndex;
    ULONG64     Reserved[2];
    ULONG       Index;
    ULONG       Size;
    ULONG64     ModBase;
    ULONG       Flags;
    ULONG64     Value;
    ULONG64     Address;
    ULONG       Register;
    ULONG       Scope;
    ULONG       Tag;
    ULONG       NameLen;
    ULONG       MaxNameLen;
    CHAR        Name[1];
} SYMBOL_INFO, *PSYMBOL_INFO;

typedef struct _IMAGEHLP_STACK_FRAME {
    ULONG64 InstructionOffset;
    ULONG64 ReturnOffset;
    ULONG64 FrameOffset;
    ULONG64 StackOffset;
    ULONG64 BackingStoreOffset;
    ULONG64 FuncTableEntry;
    ULONG64 Params[4];
    ULONG64 Reserved[5];
    BOOL    Virtual;
    ULONG   Reserved2;
} IMAGEHLP_STACK_FRAME, *PIMAGEHLP_STACK_FRAME;

typedef VOID IMAGEHLP_CONTEXT, *PIMAGEHLP_CONTEXT;

typedef BOOL (CALLBACK *PSYM_ENUMERATESYMBOLS_CALLBACK)(PSYMBOL_INFO pSymInfo, ULONG SymbolSize, PVOID UserContext);

#endif /* defined(__GNUC__) */

enum BasicType {
   btNoType   = 0,
   btVoid     = 1,
   btChar     = 2,
   btWChar    = 3,
   btInt      = 6,
   btUInt     = 7,
   btFloat    = 8,
   btBCD      = 9,
   btBool     = 10,
   btLong     = 13,
   btULong    = 14,
   btCurrency = 25,
   btDate     = 26,
   btVariant  = 27,
   btComplex  = 28,
   btBit      = 29,
   btBSTR     = 30,
   btHresult  = 31
};

enum DataKind {
   DataIsUnknown,
   DataIsLocal,
   DataIsStaticLocal,
   DataIsParam,
   DataIsObjectPtr,
   DataIsFileStatic,
   DataIsGlobal,
   DataIsMember,
   DataIsStaticMember,
   DataIsConstant
};

#define SymInitialize LocSymInitialize
#define SymSetOptions LocSymSetOptions
#define SymGetLineFromName LocSymGetLineFromName
#define SymGetLineFromAddr LocSymGetLineFromAddr
#define SymGetLineNext LocSymGetLineNext
#define SymGetTypeInfo LocSymGetTypeInfo
#define SymFromIndex LocSymFromIndex
#define SymSetContext LocSymSetContext
#define SymFromName LocSymFromName
#define SymEnumSymbols LocSymEnumSymbols
#define SymGetTypeFromName LocSymGetTypeFromName
#define SymGetModuleBase64 LocSymGetModuleBase64
#define SymLoadModule64 LocSymLoadModule64
#define SymUnloadModule64 LocSymUnloadModule64
#define SymCleanup LocSymCleanup

#define EnumerateLoadedModulesW64 LocEnumerateLoadedModulesW64

extern BOOL SymInitialize(HANDLE hProcess, PCSTR UserSearchPath, BOOL fInvadeProcess);
extern BOOL SymSetOptions(DWORD Options);
extern BOOL SymGetLineFromName(HANDLE hProcess, PCSTR ModuleName, PCSTR FileName, DWORD dwLineNumber, PLONG plDisplacement, PIMAGEHLP_LINE Line);
extern BOOL SymGetLineFromAddr(HANDLE hProcess, DWORD dwAddr, PDWORD pdwDisplacement, PIMAGEHLP_LINE Line);
extern BOOL SymGetLineNext(HANDLE hProcess, PIMAGEHLP_LINE Line);
extern BOOL SymGetTypeInfo(HANDLE hProcess, DWORD64 ModBase, ULONG TypeId, IMAGEHLP_SYMBOL_TYPE_INFO GetType, PVOID pInfo);
extern BOOL SymFromIndex(HANDLE hProcess, ULONG64 BaseOfDll, DWORD Index, PSYMBOL_INFO Symbol);
extern BOOL SymSetContext(HANDLE hProcess, PIMAGEHLP_STACK_FRAME StackFrame, PIMAGEHLP_CONTEXT Context);
extern BOOL SymFromName(HANDLE hProcess, PCSTR Name, PSYMBOL_INFO Symbol);
extern BOOL SymEnumSymbols(HANDLE hProcess, ULONG64 BaseOfDll, PCSTR Mask, PSYM_ENUMERATESYMBOLS_CALLBACK EnumSymbolsCallback, PVOID UserContext);
extern BOOL SymGetTypeFromName(HANDLE hProcess, ULONG64 BaseOfDll, PCSTR Name, PSYMBOL_INFO Symbol);
extern DWORD64 SymGetModuleBase64(HANDLE hProcess, ULONG64 Address);
extern BOOL SymLoadModule64(HANDLE hProcess, HANDLE hFile, PCSTR ImageName, PCSTR ModuleName, DWORD64 BaseOfDll, DWORD SizeOfDll);
extern BOOL SymUnloadModule64(HANDLE hProcess, DWORD64 BaseOfDll);
extern BOOL SymCleanup(HANDLE hProcess);

extern BOOL LocEnumerateLoadedModulesW64(HANDLE hProcess, PENUMLOADED_MODULES_CALLBACKW64 Callback, PVOID UserContext);

#endif /* defined(WIN32) */
#endif /* D_windbgcache */

#ifdef __cplusplus
}
#endif

