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
 * Symbols service.- Windows version
 */
#include "mdep.h"
#include "config.h"

#if (SERVICE_Symbols) && defined(WIN32)

#include <errno.h>
#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#if defined(__CYGWIN__)
#  include <imagehlp.h>
#else
#  define _NO_CVCONST_H
#  include <dbghelp.h>
#endif
#include "errors.h"
#include "elf.h"
#include "myalloc.h"
#include "symbols.h"
#include "stacktrace.h"
#include "trace.h"

#define SYM_SEARCH_PATH "http://msdl.microsoft.com/download/symbols"

#ifndef MAX_SYM_NAME
#  define MAX_SYM_NAME 2000
#endif

#if defined(__CYGWIN__)
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

#endif

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

static char * tmp_buf = NULL;
static int tmp_buf_size = 0;

#if !defined(__CYGWIN__)

static int get_stack_frame(Context * ctx, int frame, IMAGEHLP_STACK_FRAME * stack_frame) {
    memset(stack_frame, 0, sizeof(IMAGEHLP_STACK_FRAME));
    if (frame != STACK_NO_FRAME && ctx->parent != NULL) {
        ContextAddress ip = 0;
        if (get_frame_info(ctx, frame, &ip, NULL, NULL) < 0) return -1;
        stack_frame->InstructionOffset = ip;
    }
    return 0;
}

static int syminfo2symbol(Context * ctx, SYMBOL_INFO * info, Symbol * symbol) {
    memset(symbol, 0, sizeof(Symbol));

    symbol->ctx = ctx;
    symbol->module_id = info->ModBase;
    symbol->object_id = info->Index;
    symbol->type_id = info->TypeIndex;
    symbol->address = (ContextAddress)info->Address;

    if (info->Flags & SYMFLAG_FRAMEREL) {
        symbol->base = SYM_BASE_FP;
    }
    else if (info->Flags & SYMFLAG_REGREL) {
        symbol->base = SYM_BASE_FP;
    }
    else {
        symbol->base = SYM_BASE_ABS;
    }

    if (info->Flags & SYMFLAG_LOCAL) {
        symbol->storage = "LOCAL";
    }
    else {
        symbol->storage = "GLOBAL";
    }

    if (info->Flags & SYMFLAG_CONSTANT) {
        symbol->sym_class = SYM_CLASS_VALUE;
    }
    else if (info->Flags & SYMFLAG_FUNCTION) {
        symbol->sym_class = SYM_CLASS_FUNCTION;
    }
    else if (info->Tag == SymTagFunction) {
        symbol->sym_class = SYM_CLASS_FUNCTION;
    }
    else if (info->Tag == SymTagTypedef) {
        symbol->sym_class = SYM_CLASS_TYPE;
    }
    else {
        symbol->sym_class = SYM_CLASS_REFERENCE;
    }


    symbol->size = info->Size;

    return 0;
}

#endif

static int get_symbol_info(Context * ctx, ModuleID module_id, SymbolID symbol_id, int info_tag, void * info) {
#if !defined(__CYGWIN__)
    HANDLE process = ctx->parent == NULL ? ctx->handle : ctx->parent->handle;
    if (!SymGetTypeInfo(process, (DWORD64)module_id, (ULONG)symbol_id, info_tag, info)) {
        set_win32_errno(GetLastError());
        return -1;
    }
    return 0;
#else
    errno = ERR_UNSUPPORTED;
    return -1;
#endif
}

int get_symbol_class(Context * ctx, ModuleID module_id, SymbolID symbol_id, int * type_class) {
    DWORD dword = 0;
    int res = TYPE_CLASS_UNKNOWN;

    while (1) {
        if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_SYMTAG, &dword) < 0) return -1;
        if (dword != SymTagTypedef) break;
        if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_TYPE, &dword) < 0) return -1;
        symbol_id = dword;
    }

    switch (dword) {
    case SymTagNull:
    case SymTagExe:
    case SymTagCompiland:
    case SymTagCompilandDetails:
    case SymTagCompilandEnv:
        break;
    case SymTagFunction:
        res = TYPE_CLASS_FUNCTION;
        break;
    case SymTagBlock:
    case SymTagData:
    case SymTagAnnotation:
    case SymTagLabel:
    case SymTagPublicSymbol:
    case SymTagUDT:
        break;
    case SymTagEnum:
        res = TYPE_CLASS_ENUMERATION;
        break;
    case SymTagFunctionType:
        res = TYPE_CLASS_FUNCTION;
        break;
    case SymTagPointerType:
        res = TYPE_CLASS_POINTER;
        break;
    case SymTagArrayType:
        res = TYPE_CLASS_ARRAY;
        break;
    case SymTagBaseType:
        if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_BASETYPE, &dword) < 0) return -1;
        switch (dword) {
        case btNoType:
            break;
        case btVoid:
        case btChar:
        case btWChar:
        case btInt:
        case btBool:
        case btLong:
        case btBit:
            res = TYPE_CLASS_INTEGER;
            break;
        case btUInt:
        case btULong:
            res = TYPE_CLASS_CARDINAL;
            break;
        case btFloat:
            res = TYPE_CLASS_REAL;
            break;
        case btBCD:
        case btCurrency:
        case btDate:
        case btVariant:
        case btComplex:
        case btBSTR:
        case btHresult:
            break;
        }
        break;
    case SymTagTypedef:
    case SymTagBaseClass:
    case SymTagFriend:
    case SymTagFunctionArgType:
    case SymTagFuncDebugStart:
    case SymTagFuncDebugEnd:
    case SymTagUsingNamespace:
    case SymTagVTableShape:
    case SymTagVTable:
    case SymTagCustom:
    case SymTagThunk:
    case SymTagCustomType:
    case SymTagManagedType:
    case SymTagDimension:
    case SymTagMax:
        break;
    }

    *type_class = res;
    return 0;
}

int get_symbol_name(Context * ctx, ModuleID module_id, SymbolID symbol_id, char ** name) {
    WCHAR * ptr = NULL;
    char * res = NULL;

    if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_SYMNAME, &ptr) < 0) return -1;
    if (ptr != NULL) {
        int len = 0;
        int err = 0;
        if (tmp_buf == NULL) {
            tmp_buf_size = 256;
            tmp_buf = loc_alloc(tmp_buf_size);
        }
        while (1) {
            len = WideCharToMultiByte(CP_UTF8, 0, ptr, -1, tmp_buf, tmp_buf_size, NULL, NULL);
            if (len != 0) break;
            err = GetLastError();
            if (err != ERROR_INSUFFICIENT_BUFFER) {
                set_win32_errno(err);
                return -1;
            }
            tmp_buf_size *= 2;
            tmp_buf = loc_realloc(tmp_buf, tmp_buf_size);
        }
        HeapFree(GetProcessHeap(), 0, ptr);
        res = loc_alloc(len + 1);
        memcpy(res, tmp_buf, len);
        res[len] = 0;
    }
    *name = res;
    return 0;
}

int get_symbol_size(Context * ctx, ModuleID module_id, SymbolID symbol_id, uns64 * size) {
    DWORD dword = 0;
    uns64 res = 0;

    while (1) {
        if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_SYMTAG, &dword) < 0) return -1;
        if (dword != SymTagTypedef) break;
        if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_TYPE, &dword) < 0) return -1;
        symbol_id = dword;
    }

    if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_LENGTH, &res) < 0) return -1;

    *size = res;
    return 0;
}

int get_symbol_base_type(Context * ctx, ModuleID module_id, SymbolID symbol_id, SymbolID * base_type) {
    DWORD dword = 0;
    DWORD res = 0;

    while (1) {
        if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_SYMTAG, &dword) < 0) return -1;
        if (dword != SymTagTypedef) break;
        if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_TYPE, &dword) < 0) return -1;
        symbol_id = dword;
    }

    if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_TYPE, &res) < 0) return -1;

    *base_type = res;
    return 0;
}

int get_symbol_index_type(Context * ctx, ModuleID module_id, SymbolID symbol_id, SymbolID * index_type) {
    DWORD dword = 0;
    DWORD res = 0;

    while (1) {
        if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_SYMTAG, &dword) < 0) return -1;
        if (dword != SymTagTypedef) break;
        if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_TYPE, &dword) < 0) return -1;
        symbol_id = dword;
    }

    if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_ARRAYINDEXTYPEID, &res) < 0) return -1;

    *index_type = res;
    return 0;
}

int get_symbol_length(Context * ctx, ModuleID module_id, SymbolID symbol_id, unsigned long * length) {
    DWORD dword = 0;
    DWORD res = 0;

    while (1) {
        if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_SYMTAG, &dword) < 0) return -1;
        if (dword != SymTagTypedef) break;
        if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_TYPE, &dword) < 0) return -1;
        symbol_id = dword;
    }

    if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_COUNT, &res) < 0) return -1;

    *length = res;
    return 0;
}

int get_symbol_children(Context * ctx, ModuleID module_id, SymbolID symbol_id, SymbolID ** children) {

    static const DWORD FINDCHILDREN_BUF_SIZE = 64;
    static TI_FINDCHILDREN_PARAMS * params = NULL;

    DWORD dword = 0;
    DWORD cnt = 0;
    SymbolID * res = NULL;

    while (1) {
        if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_SYMTAG, &dword) < 0) return -1;
        if (dword != SymTagTypedef) break;
        if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_TYPE, &dword) < 0) return -1;
        symbol_id = dword;
    }

    if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_CHILDRENCOUNT, &cnt) < 0) return -1;
    if (params == NULL) params = loc_alloc(sizeof(TI_FINDCHILDREN_PARAMS) + (FINDCHILDREN_BUF_SIZE - 1) * sizeof(ULONG));

    params->Start = 0;
    res = loc_alloc_zero(cnt * sizeof(SymbolID));
    while (params->Start < cnt) {
        DWORD i = cnt - (DWORD)params->Start;
        params->Count = i > FINDCHILDREN_BUF_SIZE ? FINDCHILDREN_BUF_SIZE : i;
        if (get_symbol_info(ctx, module_id, symbol_id, TI_FINDCHILDREN, params) < 0) return -1;
        for (i = 0; params->Start < cnt; i++) res[params->Start++] = params->ChildId[i];
    }

    *children = res;
    return 0;
}

int get_symbol_offset(Context * ctx, ModuleID module_id, SymbolID symbol_id, unsigned long * offset) {
    DWORD dword = 0;

    if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_OFFSET, &dword) < 0) return -1;
    *offset = dword;
    return 0;
}

int get_symbol_value(Context * ctx, ModuleID module_id, SymbolID symbol_id, size_t * size, void * value) {
    VARIANT data;
    VARTYPE vt;
    void * data_addr = &data.bVal;
    size_t data_size = 0;

    assert(data_addr == &data.lVal);
    if (get_symbol_info(ctx, module_id, symbol_id, TI_GET_VALUE, &data) < 0) return -1;

    vt = data.vt;
    if (vt & VT_BYREF) {
        data_addr = *(void **)data_addr;
        vt &= ~VT_BYREF;
    }

    switch (vt) {
        /*    VOID           */ case 0:             break;
        /*    CHAR           */ case VT_I1:         data_size = 1; break;
        /*    SHORT          */ case VT_I2:         data_size = 2; break;
        /*    LONG           */ case VT_I4:         data_size = 4; break;
        /*    LONGLONG       */ case VT_I8:         data_size = 8; break;
        /*    INT            */ case VT_INT:        data_size = sizeof(int); break;
        /*    BYTE           */ case VT_UI1:        data_size = 1; break;
        /*    USHORT         */ case VT_UI2:        data_size = 2; break;
        /*    ULONG          */ case VT_UI4:        data_size = 4; break;
        /*    ULONGLONG      */ case VT_UI8:        data_size = 8; break;
        /*    UINT           */ case VT_UINT:       data_size = sizeof(unsigned int); break;
        /*    FLOAT          */ case VT_R4:         data_size = 4; break;
        /*    DOUBLE         */ case VT_R8:         data_size = 8; break;
        /*    VARIANT_BOOL   */ case VT_BOOL:       data_size = sizeof(BOOL); break;
        /*    SCODE          */ case VT_ERROR:      data_size = sizeof(ERROR); break;
        /*    CY             */ case VT_CY:         data_size = sizeof(CY); break;
        /*    DATE           */ case VT_DATE:       data_size = sizeof(DATE); break;
        /*    BSTR           */ case VT_BSTR:       data_size = sizeof(BSTR); break;
        /*    IUnknown *     */ case VT_UNKNOWN:    data_size = sizeof(IUnknown *); break;
        /*    IDispatch *    */ case VT_DISPATCH:   data_size = sizeof(IDispatch *); break;
        /*    SAFEARRAY *    */ case VT_ARRAY:      data_size = sizeof(SAFEARRAY *); break;
        /*    VARIANT        */ case VT_VARIANT:    data_size = sizeof(VARIANT); break;
        /*    DECIMAL        */ case VT_DECIMAL:    data_size = sizeof(DECIMAL); break;
    }

    if (*size < data_size) {
        errno = ERR_BUFFER_OVERFLOW;
        return -1;
    }
    memcpy(value, data_addr, data_size);
    *size = data_size;

    return 0;
}

int find_symbol(Context * ctx, int frame, char * name, Symbol * sym) {

#if defined(__CYGWIN__)

    /* TODO SymFromName() is not working in CYGWIN */

    extern void tcf_test_func0();
    extern void tcf_test_func1();
    extern void tcf_test_func2();
    extern char * tcf_test_array;

    int error = 0;

    memset(sym, 0, sizeof(Symbol));
    sym->section = ".text";
    sym->storage = "GLOBAL";
    sym->base = SYM_BASE_ABS;
    if (strcmp(name, "tcf_test_func0") == 0) {
        sym->address = (ContextAddress)tcf_test_func0;
        sym->sym_class = SYM_CLASS_FUNCTION;
    }
    else if (strcmp(name, "tcf_test_func1") == 0) {
        sym->address = (ContextAddress)tcf_test_func1;
        sym->sym_class = SYM_CLASS_FUNCTION;
    }
    else if (strcmp(name, "tcf_test_func2") == 0) {
        sym->address = (ContextAddress)tcf_test_func2;
        sym->sym_class = SYM_CLASS_FUNCTION;
    }
    else if (strcmp(name, "tcf_test_array") == 0) {
        sym->address = (ContextAddress)&tcf_test_array;
        sym->sym_class = SYM_CLASS_REFERENCE;
    }
    else {
        error = EINVAL;
    }
    if (error) {
        errno = error;
        return -1;
    }
    return 0;

#else

    ULONG64 buffer[(sizeof(SYMBOL_INFO) + MAX_SYM_NAME * sizeof(TCHAR) + sizeof(ULONG64) - 1) / sizeof(ULONG64)];
    SYMBOL_INFO * info = (SYMBOL_INFO *)buffer;
    IMAGEHLP_STACK_FRAME stack_frame;
    HANDLE process = ctx->parent == NULL ? ctx->handle : ctx->parent->handle;

    info->SizeOfStruct = sizeof(SYMBOL_INFO);
    info->MaxNameLen = MAX_SYM_NAME;

    if (get_stack_frame(ctx, frame, &stack_frame) < 0) {
        return -1;
    }

    if (!SymSetContext(process, &stack_frame, NULL)) {
        DWORD err = GetLastError();
        if (err != ERROR_SUCCESS) {
            if (err == ERROR_MOD_NOT_FOUND && frame != STACK_NO_FRAME) {
                /* No local symbols data, search global scope */
                if (get_stack_frame(ctx, STACK_NO_FRAME, &stack_frame) < 0) {
                    return -1;
                }
                if (!SymSetContext(process, &stack_frame, NULL)) {
                    DWORD err = GetLastError();
                    if (err != ERROR_SUCCESS) {
                        set_win32_errno(err);
                        return -1;
                    }
                }
            }
            else {
                set_win32_errno(err);
                return -1;
            }
        }
    }

    if (!SymFromName(process, name, info)) {
        set_win32_errno(GetLastError());
        return -1;
    }
    syminfo2symbol(ctx, info, sym);
    return 0;

#endif
}

#if !defined(__CYGWIN__)

typedef struct EnumerateSymbolsContext {
    Context * ctx;
    EnumerateSymbolsCallBack * call_back;
    void * args;
} EnumerateSymbolsContext;

static BOOL CALLBACK enumerate_symbols_proc(SYMBOL_INFO * info, ULONG symbol_size, VOID * user_context) {
    EnumerateSymbolsContext * enum_context = (EnumerateSymbolsContext *)user_context;
    Symbol symbol;
    syminfo2symbol(enum_context->ctx, info, &symbol);
    enum_context->call_back(enum_context->args, info->Name, &symbol);
    return TRUE;
}

#endif

int enumerate_symbols(Context * ctx, int frame, EnumerateSymbolsCallBack * call_back, void * args) {
#if defined(__CYGWIN__)

    /* TODO SymEnumSymbols() is not working in CYGWIN */

    return 0;

#else

    ULONG64 buffer[(sizeof(SYMBOL_INFO) + MAX_SYM_NAME * sizeof(TCHAR) + sizeof(ULONG64) - 1) / sizeof(ULONG64)];
    SYMBOL_INFO * symbol = (SYMBOL_INFO *)buffer;
    IMAGEHLP_STACK_FRAME stack_frame;
    EnumerateSymbolsContext enum_context;
    HANDLE process = ctx->parent == NULL ? ctx->handle : ctx->parent->handle;

    symbol->SizeOfStruct = sizeof(SYMBOL_INFO);
    symbol->MaxNameLen = MAX_SYM_NAME;

    if (get_stack_frame(ctx, frame, &stack_frame) < 0) {
        return -1;
    }

    if (!SymSetContext(process, &stack_frame, NULL)) {
        DWORD err = GetLastError();
        if (err != ERROR_SUCCESS) {
            set_win32_errno(err);
            return -1;
        }
    }

    enum_context.ctx = ctx;
    enum_context.call_back = call_back;
    enum_context.args = args;

    if (!SymEnumSymbols(process, 0, NULL, enumerate_symbols_proc, &enum_context)) {
        set_win32_errno(GetLastError());
        return -1;
    }

    return 0;

#endif
}


ContextAddress is_plt_section(Context * ctx, ContextAddress addr) {
    return 0;
}

static void event_context_created(Context * ctx, void * client_data) {
    if (ctx->parent != NULL) return;
    assert(ctx->pid == ctx->mem);
    if (!SymInitialize(ctx->handle, SYM_SEARCH_PATH, FALSE)) {
        set_win32_errno(GetLastError());
        trace(LOG_ALWAYS, "SymInitialize() error: %d: %s",
            errno, errno_to_str(errno));
    }
    if (!SymLoadModule(ctx->handle, ctx->file_handle, NULL, NULL, (DWORD)ctx->base_address, 0)) {
        set_win32_errno(GetLastError());
        trace(LOG_ALWAYS, "SymLoadModule() error: %d: %s",
            errno, errno_to_str(errno));
    }
}

static void event_context_exited(Context * ctx, void * client_data) {
    if (ctx->parent != NULL) return;
    assert(ctx->handle != NULL);
    if (!SymUnloadModule(ctx->handle, (DWORD)ctx->base_address)) {
        set_win32_errno(GetLastError());
        trace(LOG_ALWAYS, "SymUnloadModule() error: %d: %s",
            errno, errno_to_str(errno));
    }
    if (!SymCleanup(ctx->handle)) {
        set_win32_errno(GetLastError());
        trace(LOG_ALWAYS, "SymCleanup() error: %d: %s",
            errno, errno_to_str(errno));
    }
}

static void event_context_changed(Context * ctx, void * client_data) {
    if (ctx->module_loaded) {
        assert(ctx->pid == ctx->mem);
        if (!SymLoadModule(ctx->handle, ctx->module_handle, NULL, NULL, (DWORD)ctx->module_address, 0)) {
            set_win32_errno(GetLastError());
            trace(LOG_ALWAYS, "SymLoadModule() error: %d: %s",
                errno, errno_to_str(errno));
        }
    }
    if (ctx->module_unloaded) {
        assert(ctx->pid == ctx->mem);
        assert(ctx->handle != NULL);
        if (!SymUnloadModule(ctx->handle, (DWORD)ctx->module_address)) {
            set_win32_errno(GetLastError());
            trace(LOG_ALWAYS, "SymUnloadModule() error: %d: %s",
                errno, errno_to_str(errno));
        }
    }
}

void ini_symbols_service(void) {
    static ContextEventListener listener = {
        event_context_created,
        event_context_exited,
        NULL,
        NULL,
        event_context_changed
    };
    add_context_event_listener(&listener, NULL);
    SymSetOptions(SYMOPT_UNDNAME | SYMOPT_LOAD_LINES | SYMOPT_DEFERRED_LOADS);
}


#endif

