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
 * Symbols service - Windows version
 */

#include <config.h>

#if SERVICE_Symbols && !ENABLE_SymbolsProxy && defined(_MSC_VER) && !ENABLE_ELF

#include <errno.h>
#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include <wchar.h>
#include <framework/errors.h>
#include <framework/events.h>
#include <framework/myalloc.h>
#include <framework/trace.h>
#include <services/symbols.h>
#include <services/stacktrace.h>
#include <system/Windows/windbgcache.h>
#include <system/Windows/context-win32.h>
#if ENABLE_RCBP_TEST
#  include <main/test.h>
#endif

#define SYM_SEARCH_PATH ""
/* Path could contain "http://msdl.microsoft.com/download/symbols",
   but access to Microsoft debug info server is too slow,
   and dbghelp.dll caching is inadequate
*/

#ifndef MAX_SYM_NAME
#  define MAX_SYM_NAME 2000
#endif

typedef struct TypeInfo {
    char * name;
    unsigned char size;
    unsigned char sign;
    unsigned char real;
} TypeInfo;

static const TypeInfo basic_type_info[] = {
    { "void",                   0,                  0, 0 },
    { "char",                   sizeof(char),       1, 0 },
    { "unsigned char",          sizeof(char),       0, 0 },
    { "signed char",            sizeof(char),       1, 0 },
    { "short",                  sizeof(short),      1, 0 },
    { "unsigned short",         sizeof(short),      0, 0 },
    { "signed short",           sizeof(short),      1, 0 },
    { "short int",              sizeof(short),      1, 0 },
    { "unsigned short int",     sizeof(short),      0, 0 },
    { "signed short int",       sizeof(short),      1, 0 },
    { "int",                    sizeof(int),        1, 0 },
    { "unsigned",               sizeof(int),        0, 0 },
    { "unsigned int",           sizeof(int),        0, 0 },
    { "signed int",             sizeof(int),        1, 0 },
    { "long",                   sizeof(long),       1, 0 },
    { "unsigned long",          sizeof(long),       0, 0 },
    { "signed long",            sizeof(long),       1, 0 },
    { "long int",               sizeof(long),       1, 0 },
    { "unsigned long int",      sizeof(long),       0, 0 },
    { "signed long int",        sizeof(long),       1, 0 },
    { "long long",              sizeof(int64_t),    1, 0 },
    { "unsigned long long",     sizeof(int64_t),    0, 0 },
    { "signed long long",       sizeof(int64_t),    1, 0 },
    { "long long int",          sizeof(int64_t),    1, 0 },
    { "unsigned long long int", sizeof(int64_t),    0, 0 },
    { "signed long long int",   sizeof(int64_t),    1, 0 },
    { "float",                  sizeof(float),      1, 1 },
    { "double",                 sizeof(double),     1, 1 },
    { "long double",            sizeof(long double), 1, 1 },
    { NULL }
};

#define BST_UNSIGNED 11

struct Symbol {
    unsigned magic;
    Context * ctx;
    unsigned frame;
    int sym_class;
    ULONG64 module;
    ULONG index;
    const TypeInfo * info;
    const Symbol * base;
    size_t length;
    void * address;
};

#include <services/symbols_alloc.h>

typedef struct SymbolCacheEntry {
    HANDLE process;
    ULONG64 pc;
    char name[MAX_SYM_NAME];
    Symbol sym;
    ErrorReport * error;
} SymbolCacheEntry;

#define SYMBOL_CACHE_SIZE 153
static SymbolCacheEntry symbol_cache[SYMBOL_CACHE_SIZE];

static char * tmp_buf = NULL;
static int tmp_buf_size = 0;

static int get_stack_frame(Context * ctx, int frame, IMAGEHLP_STACK_FRAME * stack_frame) {
    memset(stack_frame, 0, sizeof(IMAGEHLP_STACK_FRAME));
    if (frame != STACK_NO_FRAME && ctx->parent != NULL) {
        uint64_t v = 0;
        StackFrame * frame_info;
        if (get_frame_info(ctx, frame, &frame_info) < 0) return -1;
        if (read_reg_value(frame_info, get_PC_definition(ctx), &v) < 0) return -1;
        stack_frame->InstructionOffset = v;
    }
    return 0;
}

static int get_sym_info(const Symbol * sym, DWORD index, SYMBOL_INFO ** res) {
    static ULONG64 buffer[(sizeof(SYMBOL_INFO) + MAX_SYM_NAME * sizeof(TCHAR) + sizeof(ULONG64) - 1) / sizeof(ULONG64)];
    SYMBOL_INFO * info = (SYMBOL_INFO *)buffer;
    HANDLE process = get_context_handle(sym->ctx->parent == NULL ? sym->ctx : sym->ctx->parent);

    info->SizeOfStruct = sizeof(SYMBOL_INFO);
    info->MaxNameLen = MAX_SYM_NAME;
    if (!SymFromIndex(process, sym->module, index, info)) {
        set_win32_errno(GetLastError());
        return -1;
    }
    *res = info;
    return 0;
}

static int get_type_info(const Symbol * sym, int info_tag, void * info) {
    HANDLE process = get_context_handle(sym->ctx->parent == NULL ? sym->ctx : sym->ctx->parent);
    if (!SymGetTypeInfo(process, sym->module, sym->index, info_tag, info)) {
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
        sym->sym_class = SYM_CLASS_FUNCTION;
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

static int is_frame_relative(SYMBOL_INFO * info) {
    return (info->Flags & SYMFLAG_FRAMEREL) || (info->Flags & SYMFLAG_REGREL);
}

static void syminfo2symbol(Context * ctx, int frame, SYMBOL_INFO * info, Symbol * sym) {
    sym->module = info->ModBase;
    sym->index = info->Index;
    if (is_frame_relative(info)) {
        assert(frame >= 0);
        sym->frame = frame - STACK_NO_FRAME;
    }
    else {
        ctx = ctx->mem;
    }
    sym->ctx = ctx;
    tag2symclass(sym, info->Tag);
}

static int get_type_tag(Symbol * type, DWORD * tag) {
    DWORD dword;
    for (;;) {
        if (get_type_info(type, TI_GET_SYMTAG, &dword) < 0) return -1;
        if (dword != SymTagTypedef && dword != SymTagFunction && dword != SymTagData) break;
        if (get_type_info(type, TI_GET_TYPE, &dword) < 0) return -1;
        type->index = dword;
    }
    type->sym_class = SYM_CLASS_TYPE;
    *tag = dword;
    return 0;
}

const char * symbol2id(const Symbol * sym) {
    static char buf[256];
    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base) {
        char base[sizeof(buf)];
        assert(sym->ctx == sym->base->ctx);
        assert(sym->sym_class == SYM_CLASS_TYPE);
        strcpy(base, symbol2id(sym->base));
        snprintf(buf, sizeof(buf), "PTR%"PRIX64".%s", (uint64_t)sym->length, base);
    }
    else {
        int i = sym->info ? sym->info - basic_type_info + 1 : 0;
        snprintf(buf, sizeof(buf), "SYM%"PRIX64".%lX.%X.%X.%s",
            (uint64_t)sym->module, sym->index, sym->frame, i, sym->ctx->id);
    }
    return buf;
}

static uint64_t read_hex(const char ** s) {
    uint64_t res = 0;
    const char * p = *s;
    for (;;) {
        if (*p >= '0' && *p <= '9') res = (res << 4) | (*p - '0');
        else if (*p >= 'A' && *p <= 'F') res = (res << 4) | (*p - 'A' + 10);
        else break;
        p++;
    }
    *s = p;
    return res;
}

int id2symbol(const char * id, Symbol ** res) {
    Symbol * sym = NULL;
    Context * ctx = NULL;
    ULONG64 module = 0;
    ULONG index = 0;
    unsigned frame = 0;
    const Symbol * base = NULL;
    const TypeInfo * info = NULL;
    size_t length = 0;
    const char * p;

    if (id != NULL && id[0] == 'P' && id[1] == 'T' && id[2] == 'R') {
        p = id + 3;
        length = (size_t)read_hex(&p);
        if (*p == '.') p++;
        if (id2symbol(p, (Symbol **)&base)) return -1;
        ctx = base->ctx;
    }
    else if (id != NULL && id[0] == 'S' && id[1] == 'Y' && id[2] == 'M') {
        unsigned idx = 0;
        p = id + 3;
        module = (ULONG64)read_hex(&p);
        if (*p == '.') p++;
        index = (ULONG)read_hex(&p);
        if (*p == '.') p++;
        frame = (unsigned)read_hex(&p);
        if (*p == '.') p++;
        idx = (unsigned)read_hex(&p);
        if (idx) info = basic_type_info + (idx - 1);
        if (*p == '.') p++;
        ctx = id2ctx(p);
    }
    else {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (ctx == NULL) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    sym = alloc_symbol();
    sym->ctx = ctx;
    sym->module = module;
    sym->index = index;
    sym->frame = frame;
    sym->base = base;
    sym->info = info;
    sym->length = length;
    if (sym->base || sym->info) {
        sym->sym_class = SYM_CLASS_TYPE;
    }
    else {
        DWORD dword = 0;
        if (get_type_info(sym, TI_GET_SYMTAG, &dword) < 0) return -1;
        tag2symclass(sym, dword);
    }
    *res = sym;
    return 0;
}

int get_symbol_class(const Symbol * sym, int * sym_class) {
    assert(sym->magic == SYMBOL_MAGIC);
    *sym_class = sym->sym_class;
    return 0;
}

int get_symbol_type_class(const Symbol * sym, int * type_class) {
    int res = TYPE_CLASS_UNKNOWN;
    Symbol type = *sym;
    DWORD tag = 0;
    DWORD base = 0;

    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base) {
        *type_class = sym->length == 0 ? TYPE_CLASS_POINTER : TYPE_CLASS_ARRAY;
        return 0;
    }
    if (sym->info) {
        if (sym->info->real) {
            *type_class = TYPE_CLASS_REAL;
        }
        else if (sym->info->sign) {
            *type_class = TYPE_CLASS_INTEGER;
        }
        else {
            *type_class = TYPE_CLASS_CARDINAL;
        }
        return 0;
    }
    if (get_type_tag(&type, &tag)) return -1;

    switch (tag) {
    case SymTagFunction:
    case SymTagPublicSymbol:
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

int get_symbol_update_policy(const Symbol * sym, char ** id, int * policy) {
    assert(sym->magic == SYMBOL_MAGIC);
    *id = sym->ctx->id;
    *policy = context_has_state(sym->ctx) ? UPDATE_ON_EXE_STATE_CHANGES : UPDATE_ON_MEMORY_MAP_CHANGES;
    return 0;
}

int get_symbol_name(const Symbol * sym, char ** name) {
    WCHAR * ptr = NULL;

    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base) {
        *name = NULL;
        return 0;
    }
    if (sym->info) {
        *name = sym->info->name;
        return 0;
    }
    *name = NULL;
    if (get_type_info(sym, TI_GET_SYMNAME, &ptr) < 0) ptr = NULL;
    if (ptr != NULL && wcscmp(ptr, L"<unnamed-tag>") == 0) ptr = NULL;
    if (ptr != NULL) {
        int len = 0;
        int err = 0;
        if (tmp_buf == NULL) {
            tmp_buf_size = 256;
            tmp_buf = loc_alloc(tmp_buf_size);
        }
        for (;;) {
            len = WideCharToMultiByte(CP_UTF8, 0, ptr, -1, tmp_buf, tmp_buf_size - 1, NULL, NULL);
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
        tmp_buf[len] = 0;
        *name = tmp_buf;
    }
    else {
        DWORD tag = 0;
        Symbol type = *sym;
        if (get_type_tag(&type, &tag)) return -1;
        if (tag == SymTagBaseType) {
            ContextAddress size = 0;
            int type_class = 0;
            unsigned char sign = 0;
            unsigned char real = 0;
            const TypeInfo * p = basic_type_info;
            if (get_symbol_size(&type, &size)) return -1;
            if (get_symbol_type_class(&type, &type_class)) return -1;
            if (type_class == TYPE_CLASS_INTEGER) sign = 1;
            else if (type_class == TYPE_CLASS_REAL) real = sign = 1;
            while (p->name != NULL) {
                if (p->size == size && p->sign == sign && p->real == real) {
                    *name = p->name;
                    break;
                }
                p++;
            }
        }
    }
    return 0;
}

int get_symbol_size(const Symbol * sym, ContextAddress * size) {
    uint64_t res = 0;
    Symbol type = *sym;
    DWORD tag = 0;

    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base) {
        if (sym->length > 0) {
            if (get_symbol_size(sym->base, size)) return -1;
            *size *= sym->length;
        }
        else {
            *size = sizeof(void *);
        }
        return 0;
    }
    if (sym->info) {
        *size = sym->info->size;
        return 0;
    }
    if (get_type_tag(&type, &tag)) return -1;
    if (get_type_info(&type, TI_GET_LENGTH, &res) < 0) return -1;

    *size = (ContextAddress)res;
    return 0;
}

int get_symbol_type(const Symbol * sym, Symbol ** type) {
    DWORD tag = 0;

    assert(sym->magic == SYMBOL_MAGIC);
    *type = alloc_symbol();
    **type = *sym;
    if (!(*type)->base && !(*type)->info) {
        if (get_type_tag(*type, &tag)) return -1;
    }
    assert((*type)->sym_class == SYM_CLASS_TYPE);
    return 0;
}

int get_symbol_base_type(const Symbol * sym, Symbol ** type) {
    DWORD tag = 0;
    DWORD index = 0;

    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base) {
        *type = (Symbol *)sym->base;
        return 0;
    }
    if (sym->info) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    *type = alloc_symbol();
    **type = *sym;
    if (get_type_tag(*type, &tag)) return -1;
    if (get_type_info(*type, TI_GET_TYPE, &index) < 0) return -1;
    (*type)->index = index;

    return 0;
}

int get_symbol_index_type(const Symbol * sym, Symbol ** type) {
    DWORD tag = 0;
    DWORD index = 0;

    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base) {
        *type = alloc_symbol();
        (*type)->ctx = sym->ctx;
        (*type)->sym_class = SYM_CLASS_TYPE;
        (*type)->info = basic_type_info + BST_UNSIGNED;
        assert((*type)->info->size == sizeof(int));
        assert((*type)->info->sign == 0);
        assert((*type)->info->real == 0);
        return 0;
    }
    if (sym->info) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    *type = alloc_symbol();
    **type = *sym;
    if (get_type_tag(*type, &tag)) return -1;
    if (get_type_info(*type, TI_GET_ARRAYINDEXTYPEID, &index) < 0) return -1;
    (*type)->index = index;

    return 0;
}

int get_symbol_length(const Symbol * sym, ContextAddress * length) {
    DWORD res = 0;
    Symbol type = *sym;
    DWORD tag = 0;

    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base) {
        *length = sym->length == 0 ? 1 : sym->length;
        return 0;
    }
    if (sym->info) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (get_type_tag(&type, &tag)) return -1;
    if (get_type_info(&type, TI_GET_COUNT, &res) < 0) return -1;

    *length = res;
    return 0;
}

int get_symbol_lower_bound(const Symbol * sym, int64_t * value) {
    Symbol type = *sym;
    DWORD tag = 0;

    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base) {
        *value = 0;
        return 0;
    }
    if (sym->info) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (get_type_tag(&type, &tag)) return -1;
    switch (tag) {
    case SymTagArrayType:
        /* TODO: Windows array symbol lower bound value */
        *value = 0;
        return 0;
    }
    errno = ERR_INV_CONTEXT;
    return -1;
}

int get_symbol_children(const Symbol * sym, Symbol *** children, int * count) {

    static const DWORD FINDCHILDREN_BUF_SIZE = 64;
    static TI_FINDCHILDREN_PARAMS * params = NULL;
    static Symbol ** buf = NULL;
    static unsigned buf_len = 0;

    DWORD cnt = 0;
    Symbol type = *sym;
    DWORD tag = 0;

    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base || sym->info) {
        *children = NULL;
        *count = 0;
        return 0;
    }
    if (get_type_tag(&type, &tag)) return -1;
    if (get_type_info(&type, TI_GET_CHILDRENCOUNT, &cnt) < 0) return -1;
    if (params == NULL) params = loc_alloc(sizeof(TI_FINDCHILDREN_PARAMS) + (FINDCHILDREN_BUF_SIZE - 1) * sizeof(ULONG));

    if (buf_len < cnt) {
        buf = loc_realloc(buf, sizeof(Symbol *) * cnt);
        buf_len = cnt;
    }
    params->Start = 0;
    while (params->Start < cnt) {
        DWORD i = cnt - (DWORD)params->Start;
        params->Count = i > FINDCHILDREN_BUF_SIZE ? FINDCHILDREN_BUF_SIZE : i;
        if (get_type_info(&type, TI_FINDCHILDREN, params) < 0) return -1;
        for (i = 0; params->Start < cnt; i++) {
            DWORD dword = 0;
            Symbol * x = alloc_symbol();
            *x = *sym;
            x->index = params->ChildId[i];
            if (get_type_info(x, TI_GET_SYMTAG, &dword) < 0) return -1;
            tag2symclass(x, dword);
            buf[params->Start++] = x;
        }
    }

    *children = buf;
    *count = cnt;
    return 0;
}

int get_symbol_offset(const Symbol * sym, ContextAddress * offset) {
    DWORD dword = 0;

    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base || sym->info) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (get_type_info(sym, TI_GET_OFFSET, &dword) < 0) return -1;
    *offset = dword;
    return 0;
}

int get_symbol_value(const Symbol * sym, void ** value, size_t * size) {
    static VARIANT data;
    VARTYPE vt;
    void * data_addr = &data.bVal;
    size_t data_size = 0;

    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base || sym->info) {
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
    *value = data_addr;

    return 0;
}

int get_symbol_address(const Symbol * sym, ContextAddress * addr) {
    SYMBOL_INFO * info = NULL;

    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->address != NULL) {
        *addr = (ContextAddress)sym->address;
        return 0;
    }
    if (sym->base || sym->info) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (get_sym_info(sym, sym->index, &info) < 0) return -1;
    *addr = (ContextAddress)info->Address;

    if (is_frame_relative(info)) {
        StackFrame * frame_info;
        int frame = sym->frame + STACK_NO_FRAME;
        if (get_frame_info(sym->ctx, frame, &frame_info) < 0) return -1;
        *addr += frame_info->fp - sizeof(ContextAddress) * 2;
    }

    return 0;
}

int get_array_symbol(const Symbol * sym, ContextAddress length, Symbol ** ptr) {
    assert(sym->magic == SYMBOL_MAGIC);
    *ptr = alloc_symbol();
    (*ptr)->ctx = sym->ctx;
    (*ptr)->sym_class = SYM_CLASS_TYPE;
    (*ptr)->base = (Symbol *)sym;
    (*ptr)->length = length;
    return 0;
}

static unsigned symbol_hash(HANDLE process, ULONG64 pc, PCSTR name) {
    int i;
    unsigned h = (unsigned)(uintptr_t)process >> 4;
    h += (unsigned)(uintptr_t)pc;
    for (i = 0; name[i]; i++) h += name[i];
    h = h + h / SYMBOL_CACHE_SIZE;
    return h % SYMBOL_CACHE_SIZE;
}

static int find_cache_symbol(HANDLE process, ULONG64 pc, PCSTR name, Symbol * sym) {
    SymbolCacheEntry * entry = symbol_cache + symbol_hash(process, pc, name);
    assert(process != NULL);
    if (entry->process != process) return 0;
    if (entry->pc != pc) return 0;
    if (strcmp(entry->name, name)) return 0;
    if (entry->error == NULL) *sym = entry->sym;
    set_error_report_errno(entry->error);
    return 1;
}

static void add_cache_symbol(HANDLE process, ULONG64 pc, PCSTR name, Symbol * sym, int error) {
    SymbolCacheEntry * entry = symbol_cache + symbol_hash(process, pc, name);
    assert(process != NULL);
    entry->process = process;
    entry->pc = pc;
    strcpy(entry->name, name);
    release_error_report(entry->error);
    entry->error = get_error_report(error);
    if (!error) entry->sym = *sym;
    else memset(&entry->sym, 0, sizeof(Symbol));
}

static int find_pe_symbol(Context * ctx, int frame, char * name, Symbol * sym) {
    ULONG64 buffer[(sizeof(SYMBOL_INFO) + MAX_SYM_NAME * sizeof(TCHAR) + sizeof(ULONG64) - 1) / sizeof(ULONG64)];
    SYMBOL_INFO * info = (SYMBOL_INFO *)buffer;
    IMAGEHLP_STACK_FRAME stack_frame;
    HANDLE process = get_context_handle(ctx->parent == NULL ? ctx : ctx->parent);
    DWORD64 module;

    if (frame == STACK_TOP_FRAME) frame = get_top_frame(ctx);
    if (frame == STACK_TOP_FRAME) return -1;
    if (get_stack_frame(ctx, frame, &stack_frame) < 0) return -1;
    if (find_cache_symbol(process, stack_frame.InstructionOffset, name, sym)) return errno ? -1 : 0;

    memset(info, 0, sizeof(SYMBOL_INFO));
    info->SizeOfStruct = sizeof(SYMBOL_INFO);
    info->MaxNameLen = MAX_SYM_NAME;

    if (!SymSetContext(process, &stack_frame, NULL)) {
        DWORD err = GetLastError();
        if (err == ERROR_SUCCESS) {
            /* Don't know why Windows do that */
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
        syminfo2symbol(ctx, frame, info, sym);
        add_cache_symbol(process, stack_frame.InstructionOffset, name, sym, 0);
        return 0;
    }
    module = SymGetModuleBase64(process, stack_frame.InstructionOffset);
    if (module != 0 && SymGetTypeFromName(process, module, name, info)) {
        syminfo2symbol(ctx, frame, info, sym);
        add_cache_symbol(process, stack_frame.InstructionOffset, name, sym, 0);
        return 0;
    }
    if (set_win32_errno(GetLastError()) == 0) {
        errno = ERR_SYM_NOT_FOUND;
        add_cache_symbol(process, stack_frame.InstructionOffset, name, NULL, errno);
    }
    return -1;
}

static int find_basic_type_symbol(Context * ctx, char * name, Symbol * sym) {
    const TypeInfo * p = basic_type_info;
    while (p->name != NULL) {
        if (strcmp(p->name, name) == 0) {
            sym->ctx = ctx->mem;
            sym->sym_class = SYM_CLASS_TYPE;
            sym->info = p;
            return 0;
        }
        p++;
    }
    errno = ERR_SYM_NOT_FOUND;
    return -1;
}

int find_symbol(Context * ctx, int frame, char * name, Symbol ** sym) {
    *sym = alloc_symbol();
    (*sym)->ctx = ctx;
    if (find_pe_symbol(ctx, frame, name, *sym) >= 0) return 0;
    if (get_error_code(errno) != ERR_SYM_NOT_FOUND) return -1;
#if ENABLE_RCBP_TEST
    if (find_test_symbol(ctx, name, &(*sym)->address, &(*sym)->sym_class) >= 0) {
        (*sym)->ctx = ctx->mem;
        return 0;
    }
#endif
    if (find_basic_type_symbol(ctx, name, *sym) >= 0) return 0;
    return -1;
}

typedef struct EnumerateSymbolsContext {
    Context * ctx;
    int frame;
    EnumerateSymbolsCallBack * call_back;
    void * args;
} EnumerateSymbolsContext;

static BOOL CALLBACK enumerate_symbols_proc(SYMBOL_INFO * info, ULONG symbol_size, VOID * user_context) {
    EnumerateSymbolsContext * enum_context = (EnumerateSymbolsContext *)user_context;
    Symbol * sym = alloc_symbol();
    syminfo2symbol(enum_context->ctx, enum_context->frame, info, sym);
    enum_context->call_back(enum_context->args, sym);
    return TRUE;
}

int enumerate_symbols(Context * ctx, int frame, EnumerateSymbolsCallBack * call_back, void * args) {
    ULONG64 buffer[(sizeof(SYMBOL_INFO) + MAX_SYM_NAME * sizeof(TCHAR) + sizeof(ULONG64) - 1) / sizeof(ULONG64)];
    SYMBOL_INFO * symbol = (SYMBOL_INFO *)buffer;
    IMAGEHLP_STACK_FRAME stack_frame;
    EnumerateSymbolsContext enum_context;
    HANDLE process = get_context_handle(ctx->parent == NULL ? ctx : ctx->parent);

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
    enum_context.frame = frame;
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

int get_stack_tracing_info(Context * ctx, ContextAddress addr, StackTracingInfo ** info) {
    *info = NULL;
    return 0;
}

int get_next_stack_frame(StackFrame * frame, StackFrame * down) {
    return 0;
}

static void event_context_created(Context * ctx, void * client_data) {
    if (ctx->parent != NULL) return;
    assert(ctx->mem == ctx);
    if (!SymInitialize(get_context_handle(ctx), SYM_SEARCH_PATH, FALSE)) {
        set_win32_errno(GetLastError());
        trace(LOG_ALWAYS, "SymInitialize() error: %d: %s",
            errno, errno_to_str(errno));
    }
    if (!SymLoadModule64(get_context_handle(ctx), get_context_file_handle(ctx),
            NULL, NULL, get_context_base_address(ctx), 0)) {
        set_win32_errno(GetLastError());
        trace(LOG_ALWAYS, "SymLoadModule() error: %d: %s",
            errno, errno_to_str(errno));
    }
}

static void event_context_exited(Context * ctx, void * client_data) {
    unsigned i;
    HANDLE handle = get_context_handle(ctx);
    for (i = 0; i < SYMBOL_CACHE_SIZE; i++) {
        if (symbol_cache[i].sym.ctx == ctx) {
            release_error_report(symbol_cache[i].error);
            memset(symbol_cache + i, 0, sizeof(SymbolCacheEntry));
        }
    }
    if (ctx->parent != NULL) return;
    assert(handle != NULL);
    if (!SymUnloadModule64(handle, get_context_base_address(ctx))) {
        set_win32_errno(GetLastError());
        trace(LOG_ALWAYS, "SymUnloadModule() error: %d: %s",
            errno, errno_to_str(errno));
    }
    if (!SymCleanup(handle)) {
        set_win32_errno(GetLastError());
        trace(LOG_ALWAYS, "SymCleanup() error: %d: %s",
            errno, errno_to_str(errno));
    }
}

static void event_context_changed(Context * ctx, void * client_data) {
    HANDLE handle = get_context_handle(ctx);
    if (is_context_module_loaded(ctx)) {
        unsigned i;
        assert(ctx->mem == ctx);
        assert(handle != NULL);
        if (!SymLoadModule64(handle, get_context_module_handle(ctx),
                NULL, NULL, get_context_module_address(ctx), 0)) {
            set_win32_errno(GetLastError());
            trace(LOG_ALWAYS, "SymLoadModule() error: %d: %s",
                errno, errno_to_str(errno));
        }
        for (i = 0; i < SYMBOL_CACHE_SIZE; i++) {
            if (symbol_cache[i].process == handle && symbol_cache[i].error) symbol_cache[i].process = NULL;
        }
    }
    if (is_context_module_unloaded(ctx)) {
        unsigned i;
        assert(ctx->mem == ctx);
        assert(handle != NULL);
        for (i = 0; i < SYMBOL_CACHE_SIZE; i++) {
            if (symbol_cache[i].process == handle) symbol_cache[i].process = NULL;
        }
        if (!SymUnloadModule64(handle, get_context_module_address(ctx))) {
            set_win32_errno(GetLastError());
            trace(LOG_ALWAYS, "SymUnloadModule() error: %d: %s",
                errno, errno_to_str(errno));
        }
    }
}

void ini_symbols_lib(void) {
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


#endif /* SERVICE_Symbols && defined(_MSC_VER) */

