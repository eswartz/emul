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
 * Symbols service - Windows version
 */

#include "config.h"

#if SERVICE_Symbols && defined(WIN32)

#include <errno.h>
#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include "errors.h"
#include "tcf_elf.h"
#include "myalloc.h"
#include "symbols.h"
#include "stacktrace.h"
#include "windbgcache.h"
#include "trace.h"
#include "test.h"

#define SYM_SEARCH_PATH ""
/* Path could contain "http://msdl.microsoft.com/download/symbols",
   but access to Microsoft debug info server is too slow,
   and dbghelp.dll caching is inadequate
*/

#ifndef MAX_SYM_NAME
#  define MAX_SYM_NAME 2000
#endif

typedef struct SymLocation {
    ULONG64 module;
    ULONG index;
    unsigned pointer;
    void * address;
} SymLocation;

static char * tmp_buf = NULL;
static int tmp_buf_size = 0;

static int get_stack_frame(Context * ctx, int frame, IMAGEHLP_STACK_FRAME * stack_frame) {
    memset(stack_frame, 0, sizeof(IMAGEHLP_STACK_FRAME));
    if (frame != STACK_NO_FRAME && ctx->parent != NULL) {
        ContextAddress ip = 0;
        if (get_frame_info(ctx, frame, &ip, NULL, NULL) < 0) return -1;
        stack_frame->InstructionOffset = ip;
    }
    return 0;
}

static int get_sym_info(const Symbol * sym, DWORD index, SYMBOL_INFO ** res) {
    static ULONG64 buffer[(sizeof(SYMBOL_INFO) + MAX_SYM_NAME * sizeof(TCHAR) + sizeof(ULONG64) - 1) / sizeof(ULONG64)];
    SYMBOL_INFO * info = (SYMBOL_INFO *)buffer;
    HANDLE process = sym->ctx->parent == NULL ? sym->ctx->handle : sym->ctx->parent->handle;

    info->SizeOfStruct = sizeof(SYMBOL_INFO);
    info->MaxNameLen = MAX_SYM_NAME;
    if (!SymFromIndex(process, ((SymLocation *)sym->location)->module, index, info)) {
        set_win32_errno(GetLastError());
        return -1;
    }
    *res = info;
    return 0;
}

static int get_type_info(const Symbol * sym, int info_tag, void * info) {
    HANDLE process = sym->ctx->parent == NULL ? sym->ctx->handle : sym->ctx->parent->handle;
    const SymLocation * loc = (const SymLocation *)sym->location;
    if (!SymGetTypeInfo(process, loc->module, loc->index, info_tag, info)) {
        set_win32_errno(GetLastError());
        return -1;
    }
    return 0;
}

static void tag2symclass(Symbol * sym, int tag) {
    DWORD dword;
    sym->sym_class = SYM_CLASS_UNKNOWN;
    switch (tag) {
    case SymTagFunction:
        sym->sym_class = SYM_CLASS_FUNCTION;
        break;
    case SymTagData:
        if (get_type_info(sym, TI_GET_DATAKIND, &dword) == 0) {
            if (dword == DataIsConstant) {
                sym->sym_class = SYM_CLASS_VALUE;
                break;
            }
        }
        sym->sym_class = SYM_CLASS_REFERENCE;
        break;
    case SymTagPublicSymbol:
        sym->sym_class = SYM_CLASS_REFERENCE;
        break;
    case SymTagUDT:
    case SymTagEnum:
    case SymTagFunctionType:
    case SymTagPointerType:
    case SymTagArrayType:
    case SymTagBaseType:
    case SymTagTypedef:
    case SymTagBaseClass:
    case SymTagFunctionArgType:
    case SymTagCustomType:
    case SymTagManagedType:
        sym->sym_class = SYM_CLASS_TYPE;
        break;
    }
}

static void syminfo2symbol(Context * ctx, SYMBOL_INFO * info, Symbol * sym) {
    SymLocation * loc = (SymLocation *)sym->location;
    memset(sym, 0, sizeof(Symbol));
    sym->ctx = ctx;
    loc->module = info->ModBase;
    loc->index = info->Index;
    tag2symclass(sym, info->Tag);
}

static int get_type_tag(Symbol * type, DWORD * tag) {
    DWORD dword;
    for (;;) {
        if (get_type_info(type, TI_GET_SYMTAG, &dword) < 0) return -1;
        if (dword != SymTagTypedef && dword != SymTagFunction && dword != SymTagData) break;
        if (get_type_info(type, TI_GET_TYPE, &dword) < 0) return -1;
        ((SymLocation *)type->location)->index = dword;
    }
    type->sym_class = SYM_CLASS_TYPE;
    *tag = dword;
    return 0;
}

char * symbol2id(const Symbol * sym) {
    static char buf[256];
    const SymLocation * loc = (const SymLocation *)sym->location;
    snprintf(buf, sizeof(buf), "SYM%llX.%lX.%X.%s",
        loc->module, loc->index, loc->pointer, container_id(sym->ctx));
    return buf;
}

int id2symbol(char * id, Symbol * sym) {
    ULONG64 module = 0;
    ULONG index = 0;
    unsigned pointer = 0;
    SymLocation * loc = (SymLocation *)sym->location;
    char * p;

    if (id == NULL || id[0] != 'S' || id[1] != 'Y' || id[2] != 'M') {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    p = id + 3;
    for (;;) {
        if (*p >= '0' && *p <= '9') module = (module << 4) | (*p - '0');
        else if (*p >= 'A' && *p <= 'F') module = (module << 4) | (*p - 'A' + 10);
        else break;
        p++;
    }
    if (*p++ != '.') {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    for (;;) {
        if (*p >= '0' && *p <= '9') index = (index << 4) | (*p - '0');
        else if (*p >= 'A' && *p <= 'F') index = (index << 4) | (*p - 'A' + 10);
        else break;
        p++;
    }
    if (*p++ != '.') {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    for (;;) {
        if (*p >= '0' && *p <= '9') pointer = (pointer << 4) | (*p - '0');
        else if (*p >= 'A' && *p <= 'F') pointer = (pointer << 4) | (*p - 'A' + 10);
        else break;
        p++;
    }
    if (*p++ != '.') {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    memset(sym, 0, sizeof(Symbol));
    sym->ctx = id2ctx(p);
    loc->module = module;
    loc->index = index;
    loc->pointer = pointer;
    if (sym->ctx == NULL) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (loc->pointer) {
        sym->sym_class = SYM_CLASS_TYPE;
    }
    else {
        DWORD dword = 0;
        if (get_type_info(sym, TI_GET_SYMTAG, &dword) < 0) return -1;
        tag2symclass(sym, dword);
    }
    return 0;
}

int get_symbol_type_class(const Symbol * sym, int * type_class) {
    int res = TYPE_CLASS_UNKNOWN;
    Symbol type = *sym;
    DWORD tag = 0;
    DWORD base = 0;

    if (((SymLocation *)sym->location)->pointer) {
        *type_class = TYPE_CLASS_POINTER;
        return 0;
    }
    if (get_type_tag(&type, &tag)) return -1;

    switch (tag) {
    case SymTagFunction:
        res = TYPE_CLASS_FUNCTION;
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
    case SymTagUDT:
        res = TYPE_CLASS_COMPOSITE;
        break;
    case SymTagBaseType:
        if (get_type_info(&type, TI_GET_BASETYPE, &base) < 0) return -1;
        switch (base) {
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
    }

    *type_class = res;
    return 0;
}

int get_symbol_name(const Symbol * sym, char ** name) {
    WCHAR * ptr = NULL;
    char * res = NULL;

    if (((SymLocation *)sym->location)->pointer) {
        *name = NULL;
        return 0;
    }
    if (get_type_info(sym, TI_GET_SYMNAME, &ptr) < 0) return -1;
    if (ptr != NULL) {
        int len = 0;
        int err = 0;
        if (tmp_buf == NULL) {
            tmp_buf_size = 256;
            tmp_buf = loc_alloc(tmp_buf_size);
        }
        for (;;) {
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

int get_symbol_size(const Symbol * sym, int frame, size_t * size) {
    uint64_t res = 0;
    Symbol type = *sym;
    DWORD tag = 0;

    if (((SymLocation *)sym->location)->pointer) {
        *size = sizeof(void *);
        return 0;
    }
    if (get_type_tag(&type, &tag)) return -1;
    if (get_type_info(&type, TI_GET_LENGTH, &res) < 0) return -1;

    *size = (size_t)res;
    return 0;
}

int get_symbol_type(const Symbol * sym, Symbol * type) {
    DWORD tag = 0;

    *type = *sym;
    if (!((SymLocation *)type->location)->pointer) {
        if (get_type_tag(type, &tag)) return -1;
    }
    assert(type->sym_class == SYM_CLASS_TYPE);
    return 0;
}

int get_symbol_base_type(const Symbol * sym, Symbol * type) {
    DWORD tag = 0;
    DWORD index = 0;

    *type = *sym;
    if (((SymLocation *)type->location)->pointer) {
        ((SymLocation *)type->location)->pointer--;
        return 0;
    }
    if (get_type_tag(type, &tag)) return -1;
    if (get_type_info(type, TI_GET_TYPE, &index) < 0) return -1;
    ((SymLocation *)type->location)->index = index;

    return 0;
}

int get_symbol_index_type(const Symbol * sym, Symbol * type) {
    DWORD tag = 0;
    DWORD index = 0;

    if (((SymLocation *)sym->location)->pointer) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    *type = *sym;
    if (get_type_tag(type, &tag)) return -1;
    if (get_type_info(type, TI_GET_ARRAYINDEXTYPEID, &index) < 0) return -1;
    ((SymLocation *)type->location)->index = index;

    return 0;
}

int get_symbol_length(const Symbol * sym, int frame, unsigned long * length) {
    DWORD res = 0;
    Symbol type = *sym;
    DWORD tag = 0;

    if (((SymLocation *)sym->location)->pointer) {
        *length = 1;
        return 0;
    }
    if (get_type_tag(&type, &tag)) return -1;
    if (get_type_info(&type, TI_GET_COUNT, &res) < 0) return -1;

    *length = res;
    return 0;
}

int get_symbol_children(const Symbol * sym, Symbol ** children, int * count) {

    static const DWORD FINDCHILDREN_BUF_SIZE = 64;
    static TI_FINDCHILDREN_PARAMS * params = NULL;

    DWORD cnt = 0;
    Symbol * res = NULL;
    Symbol type = *sym;
    DWORD tag = 0;

    if (((SymLocation *)sym->location)->pointer) {
        *children = NULL;
        *count = 0;
        return 0;
    }
    if (get_type_tag(&type, &tag)) return -1;
    if (get_type_info(&type, TI_GET_CHILDRENCOUNT, &cnt) < 0) return -1;
    if (params == NULL) params = loc_alloc(sizeof(TI_FINDCHILDREN_PARAMS) + (FINDCHILDREN_BUF_SIZE - 1) * sizeof(ULONG));

    params->Start = 0;
    res = loc_alloc_zero(cnt * sizeof(Symbol));
    while (params->Start < cnt) {
        DWORD i = cnt - (DWORD)params->Start;
        params->Count = i > FINDCHILDREN_BUF_SIZE ? FINDCHILDREN_BUF_SIZE : i;
        if (get_type_info(&type, TI_FINDCHILDREN, params) < 0) return -1;
        for (i = 0; params->Start < cnt; i++) {
            DWORD dword = 0;
            Symbol * x = res + params->Start++;
            *x = *sym;
            ((SymLocation *)x->location)->index = params->ChildId[i];
            if (get_type_info(x, TI_GET_SYMTAG, &dword) < 0) return -1;
            tag2symclass(x, dword);
        }
    }

    *children = res;
    *count = cnt;
    return 0;
}

int get_symbol_offset(const Symbol * sym, unsigned long * offset) {
    DWORD dword = 0;

    if (((SymLocation *)sym->location)->pointer) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (get_type_info(sym, TI_GET_OFFSET, &dword) < 0) return -1;
    *offset = dword;
    return 0;
}

int get_symbol_value(const Symbol * sym, void ** value, size_t * size) {
    VARIANT data;
    VARTYPE vt;
    void * data_addr = &data.bVal;
    size_t data_size = 0;

    if (((SymLocation *)sym->location)->pointer) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    assert(data_addr == &data.lVal);
    if (get_type_info(sym, TI_GET_VALUE, &data) < 0) return -1;

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

    *size = data_size;
    *value = loc_alloc(data_size);
    memcpy(*value, data_addr, data_size);

    return 0;
}

int get_symbol_address(const Symbol * sym, int frame, ContextAddress * addr) {
    SYMBOL_INFO * info = NULL;

    if (((SymLocation *)sym->location)->address != NULL) {
        *addr = (ContextAddress)((SymLocation *)sym->location)->address;
        return 0;
    }
    if (((SymLocation *)sym->location)->pointer) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (get_sym_info(sym, ((SymLocation *)sym->location)->index, &info) < 0) return -1;
    *addr = (ContextAddress)info->Address;

    if ((info->Flags & SYMFLAG_FRAMEREL) || (info->Flags & SYMFLAG_REGREL)) {
        ContextAddress fp = 0;
        if (get_frame_info(sym->ctx, frame, NULL, NULL, &fp) < 0) return -1;
        *addr += fp;
    }

    return 0;
}

int get_symbol_pointer(const Symbol * sym, Symbol * ptr) {
    DWORD tag = 0;

    *ptr = *sym;
    if (!((SymLocation *)ptr->location)->pointer) {
        if (get_type_tag(ptr, &tag)) return -1;
    }
    assert(ptr->sym_class == SYM_CLASS_TYPE);
    ((SymLocation *)ptr->location)->pointer++;
    return 0;
}

static int find_pe_symbol(Context * ctx, int frame, char * name, Symbol * sym) {
    ULONG64 buffer[(sizeof(SYMBOL_INFO) + MAX_SYM_NAME * sizeof(TCHAR) + sizeof(ULONG64) - 1) / sizeof(ULONG64)];
    SYMBOL_INFO * info = (SYMBOL_INFO *)buffer;
    IMAGEHLP_STACK_FRAME stack_frame;
    HANDLE process = ctx->parent == NULL ? ctx->handle : ctx->parent->handle;
    DWORD64 module;

    memset(info, 0, sizeof(SYMBOL_INFO));
    info->SizeOfStruct = sizeof(SYMBOL_INFO);
    info->MaxNameLen = MAX_SYM_NAME;

    if (get_stack_frame(ctx, frame, &stack_frame) < 0) {
        return -1;
    }

    if (!SymSetContext(process, &stack_frame, NULL)) {
        DWORD err = GetLastError();
        if (err == ERROR_SUCCESS) {
            /* Don't know why Windows does that */
        }
        else if (err == ERROR_MOD_NOT_FOUND && frame != STACK_NO_FRAME) {
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

    if (SymFromName(process, name, info)) {
        syminfo2symbol(ctx, info, sym);
        return 0;
    }
    module = SymGetModuleBase64(process, stack_frame.InstructionOffset);
    if (module != 0) {
        if (SymGetTypeFromName(process, module, name, info)) {
            syminfo2symbol(ctx, info, sym);
            return 0;
        }
    }
    if (set_win32_errno(GetLastError()) == 0) errno = ERR_SYM_NOT_FOUND;
    return -1;
}

int find_symbol(Context * ctx, int frame, char * name, Symbol * sym) {
    if (find_pe_symbol(ctx, frame, name, sym) < 0) {
        int err = errno;
        SymLocation * loc = (SymLocation *)sym->location;
        if (find_test_symbol(ctx, name, sym, &loc->address) >= 0) return 0;
        errno = err;
        return -1;
    }
    return 0;
}

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

int enumerate_symbols(Context * ctx, int frame, EnumerateSymbolsCallBack * call_back, void * args) {
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
        if (err == ERROR_SUCCESS) {
            /* Don't know why Windows does that */
        }
        else {
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
    if (!SymLoadModule64(ctx->handle, ctx->file_handle, NULL, NULL, ctx->base_address, 0)) {
        set_win32_errno(GetLastError());
        trace(LOG_ALWAYS, "SymLoadModule() error: %d: %s",
            errno, errno_to_str(errno));
    }
}

static void event_context_exited(Context * ctx, void * client_data) {
    if (ctx->parent != NULL) return;
    assert(ctx->handle != NULL);
    if (!SymUnloadModule64(ctx->handle, ctx->base_address)) {
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
        if (!SymLoadModule64(ctx->handle, ctx->module_handle, NULL, NULL, ctx->module_address, 0)) {
            set_win32_errno(GetLastError());
            trace(LOG_ALWAYS, "SymLoadModule() error: %d: %s",
                errno, errno_to_str(errno));
        }
    }
    if (ctx->module_unloaded) {
        assert(ctx->pid == ctx->mem);
        assert(ctx->handle != NULL);
        if (!SymUnloadModule64(ctx->handle, ctx->module_address)) {
            set_win32_errno(GetLastError());
            trace(LOG_ALWAYS, "SymUnloadModule() error: %d: %s",
                errno, errno_to_str(errno));
        }
    }
}

extern void ini_symbols_lib(void);

void ini_symbols_lib(void) {
    static ContextEventListener listener = {
        event_context_created,
        event_context_exited,
        NULL,
        NULL,
        event_context_changed
    };
    assert(sizeof(SymLocation) <= sizeof(((Symbol *)0)->location));
    add_context_event_listener(&listener, NULL);
    SymSetOptions(SYMOPT_UNDNAME | SYMOPT_LOAD_LINES | SYMOPT_DEFERRED_LOADS);
}


#endif /* SERVICE_Symbols && defined(WIN32) */

