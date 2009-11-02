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

#if SERVICE_Symbols && defined(_MSC_VER)

#include <errno.h>
#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include <wchar.h>
#include "errors.h"
#include "events.h"
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
    size_t bst_index;
    size_t ptr_index;
    size_t length;
    void * address;
} SymLocation;

#define LOC(sym) ((SymLocation *)(sym)->location)

struct TypeInfo {
    char * name;
    unsigned char size;
    unsigned char sign;
    unsigned char real;
};

static const struct TypeInfo basic_type_info[] = {
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

#define BST_UNSIGNED 12

typedef struct SymbolCacheEntry {
    HANDLE process;
    ULONG64 pc;
    char name[MAX_SYM_NAME];
    Symbol sym;
    int error;
} SymbolCacheEntry;

#define SYMBOL_CACHE_SIZE 101
static SymbolCacheEntry symbol_cache[SYMBOL_CACHE_SIZE];

static char * tmp_buf = NULL;
static int tmp_buf_size = 0;

static Symbol * sym_buf = NULL;
static size_t sym_buf_pos = 0;
static size_t sym_buf_len = 0;
static int sym_buf_event_posted = 0;

static void sym_buf_event(void * arg) {
    sym_buf_pos = 0;
    sym_buf_event_posted = 0;
}

static size_t add_to_sym_buf(Symbol * sym) {
    if (sym_buf_pos >= sym_buf_len) {
        sym_buf_len = sym_buf_len == 0 ? 16 : sym_buf_len * 2;
        sym_buf = loc_realloc(sym_buf, sym_buf_len * sizeof(Symbol));
    }
    sym_buf[sym_buf_pos++] = *sym;
    if (!sym_buf_event_posted) {
        post_event(sym_buf_event, NULL);
        sym_buf_event_posted = 1;
    }
    return sym_buf_pos;
}

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
    if (!SymFromIndex(process, LOC(sym)->module, index, info)) {
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
    memset(sym, 0, sizeof(Symbol));
    sym->ctx = ctx;
    LOC(sym)->module = info->ModBase;
    LOC(sym)->index = info->Index;
    tag2symclass(sym, info->Tag);
}

static int get_type_tag(Symbol * type, DWORD * tag) {
    DWORD dword;
    for (;;) {
        if (get_type_info(type, TI_GET_SYMTAG, &dword) < 0) return -1;
        if (dword != SymTagTypedef && dword != SymTagFunction && dword != SymTagData) break;
        if (get_type_info(type, TI_GET_TYPE, &dword) < 0) return -1;
        LOC(type)->index = dword;
    }
    type->sym_class = SYM_CLASS_TYPE;
    *tag = dword;
    return 0;
}

char * symbol2id(const Symbol * sym) {
    static char buf[256];
    const SymLocation * loc = (const SymLocation *)sym->location;
    if (loc->ptr_index) {
        char base[sizeof(buf)];
        assert(loc->ptr_index <= sym_buf_pos);
        assert(sym->ctx == sym_buf[loc->ptr_index - 1].ctx);
        assert(sym->sym_class == SYM_CLASS_TYPE);
        strcpy(base, symbol2id(sym_buf + loc->ptr_index - 1));
        snprintf(buf, sizeof(buf), "PTR%X.%s", loc->length, base);
    }
    else {
        snprintf(buf, sizeof(buf), "SYM%llX.%lX.%X.%s",
            loc->module, loc->index, loc->bst_index, container_id(sym->ctx));
    }
    return buf;
}

int id2symbol(char * id, Symbol * sym) {
    ULONG64 module = 0;
    ULONG index = 0;
    size_t ptr_index = 0;
    size_t bst_index = 0;
    size_t length = 0;
    char * p;

    memset(sym, 0, sizeof(Symbol));
    if (id != NULL && id[0] == 'P' && id[1] == 'T' && id[2] == 'R') {
        p = id + 3;
        for (;;) {
            if (*p >= '0' && *p <= '9') length = (length << 4) | (*p - '0');
            else if (*p >= 'A' && *p <= 'F') length = (length << 4) | (*p - 'A' + 10);
            else break;
            p++;
        }
        if (*p++ != '.') {
            errno = ERR_INV_CONTEXT;
            return -1;
        }
        if (id2symbol(p, sym)) return -1;
        ptr_index = add_to_sym_buf(sym);
    }
    else if (id != NULL && id[0] == 'S' && id[1] == 'Y' && id[2] == 'M') {
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
            if (*p >= '0' && *p <= '9') bst_index = (bst_index << 4) | (*p - '0');
            else if (*p >= 'A' && *p <= 'F') bst_index = (bst_index << 4) | (*p - 'A' + 10);
            else break;
            p++;
        }
        if (*p++ != '.') {
            errno = ERR_INV_CONTEXT;
            return -1;
        }
        sym->ctx = id2ctx(p);
    }
    else {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    LOC(sym)->module = module;
    LOC(sym)->index = index;
    LOC(sym)->ptr_index = ptr_index;
    LOC(sym)->bst_index = bst_index;
    LOC(sym)->length = length;
    if (sym->ctx == NULL) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (LOC(sym)->ptr_index || LOC(sym)->bst_index) {
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

    if (LOC(sym)->ptr_index) {
        *type_class = LOC(sym)->length == 0 ? TYPE_CLASS_POINTER : TYPE_CLASS_ARRAY;
        return 0;
    }
    if (LOC(sym)->bst_index) {
        const struct TypeInfo * info = basic_type_info + LOC(sym)->bst_index - 1;
        if (info->real) {
            *type_class = TYPE_CLASS_REAL;
        }
        else if (info->sign) {
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

    if (LOC(sym)->ptr_index) {
        *name = NULL;
        return 0;
    }
    if (LOC(sym)->bst_index) {
        const struct TypeInfo * info = basic_type_info + LOC(sym)->bst_index - 1;
        *name = loc_strdup(info->name);
        return 0;
    }
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
    else {
        DWORD tag = 0;
        Symbol type = *sym;
        if (get_type_tag(&type, &tag)) return -1;
        if (tag == SymTagBaseType) {
            size_t size = 0;
            int type_class = 0;
            unsigned char sign = 0;
            unsigned char real = 0;
            const struct TypeInfo * p = basic_type_info + 1;
            if (get_symbol_size(&type, STACK_NO_FRAME, &size)) return -1;
            if (get_symbol_type_class(&type, &type_class)) return -1;
            if (type_class == TYPE_CLASS_INTEGER) sign = 1;
            else if (type_class == TYPE_CLASS_REAL) real = sign = 1;
            while (p->name != NULL) {
                if (p->size == size && p->sign == sign && p->real == real) {
                    res = loc_strdup(p->name);
                    break;
                }
                p++;
            }
        }
    }
    *name = res;
    return 0;
}

int get_symbol_size(const Symbol * sym, int frame, size_t * size) {
    uint64_t res = 0;
    Symbol type = *sym;
    DWORD tag = 0;

    if (LOC(sym)->ptr_index) {
        if (LOC(sym)->length > 0) {
            if (get_symbol_size(sym_buf + LOC(sym)->ptr_index - 1, frame, size)) return -1;
            *size *= LOC(sym)->length;
        }
        else {
            *size = sizeof(void *);
        }
        return 0;
    }
    if (LOC(sym)->bst_index) {
        *size = basic_type_info[LOC(sym)->bst_index - 1].size;
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
    if (!LOC(type)->ptr_index && !LOC(type)->bst_index) {
        if (get_type_tag(type, &tag)) return -1;
    }
    assert(type->sym_class == SYM_CLASS_TYPE);
    return 0;
}

int get_symbol_base_type(const Symbol * sym, Symbol * type) {
    DWORD tag = 0;
    DWORD index = 0;

    if (LOC(sym)->ptr_index) {
        *type = sym_buf[LOC(sym)->ptr_index - 1];
        return 0;
    }
    if (LOC(sym)->bst_index) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    *type = *sym;
    if (get_type_tag(type, &tag)) return -1;
    if (get_type_info(type, TI_GET_TYPE, &index) < 0) return -1;
    LOC(type)->index = index;

    return 0;
}

int get_symbol_index_type(const Symbol * sym, Symbol * type) {
    DWORD tag = 0;
    DWORD index = 0;

    if (LOC(sym)->ptr_index) {
        memset(type, 0, sizeof(Symbol));
        type->ctx = sym->ctx;
        type->sym_class = SYM_CLASS_TYPE;
        LOC(type)->bst_index = BST_UNSIGNED;
        assert(basic_type_info[LOC(type)->bst_index - 1].size == sizeof(int));
        assert(basic_type_info[LOC(type)->bst_index - 1].sign == 0);
        assert(basic_type_info[LOC(type)->bst_index - 1].real == 0);
        return 0;
    }
    if (LOC(sym)->bst_index) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    *type = *sym;
    if (get_type_tag(type, &tag)) return -1;
    if (get_type_info(type, TI_GET_ARRAYINDEXTYPEID, &index) < 0) return -1;
    LOC(type)->index = index;

    return 0;
}

int get_symbol_length(const Symbol * sym, int frame, unsigned long * length) {
    DWORD res = 0;
    Symbol type = *sym;
    DWORD tag = 0;

    if (LOC(sym)->ptr_index) {
        *length = LOC(sym)->length == 0 ? 1 : LOC(sym)->length;
        return 0;
    }
    if (LOC(sym)->bst_index) {
        errno = ERR_INV_CONTEXT;
        return -1;
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

    if (LOC(sym)->ptr_index || LOC(sym)->bst_index) {
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
            LOC(x)->index = params->ChildId[i];
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

    if (LOC(sym)->ptr_index || LOC(sym)->bst_index) {
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

    if (LOC(sym)->ptr_index || LOC(sym)->bst_index) {
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

    if (LOC(sym)->address != NULL) {
        *addr = (ContextAddress)LOC(sym)->address;
        return 0;
    }
    if (LOC(sym)->ptr_index || LOC(sym)->bst_index) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (get_sym_info(sym, LOC(sym)->index, &info) < 0) return -1;
    *addr = (ContextAddress)info->Address;

    if ((info->Flags & SYMFLAG_FRAMEREL) || (info->Flags & SYMFLAG_REGREL)) {
        ContextAddress fp = 0;
        if (get_frame_info(sym->ctx, frame, NULL, NULL, &fp) < 0) return -1;
        *addr += fp;
    }

    return 0;
}

int get_pointer_symbol(const Symbol * sym, Symbol * ptr) {
    return get_array_symbol(sym, 0, ptr);
}

int get_array_symbol(const Symbol * sym, size_t length, Symbol * ptr) {
    Symbol type = *sym;
    if (!LOC(&type)->ptr_index && !LOC(&type)->bst_index) {
        DWORD tag = 0;
        if (get_type_tag(&type, &tag)) return -1;
    }
    assert(type.sym_class == SYM_CLASS_TYPE);
    memset(ptr, 0, sizeof(Symbol));
    ptr->ctx = type.ctx;
    ptr->sym_class = SYM_CLASS_TYPE;
    LOC(ptr)->ptr_index = add_to_sym_buf(&type);
    LOC(ptr)->length = length;
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

static int find_cache_symbol(HANDLE process, ULONG64 pc, PCSTR name, Symbol * sym, int * error) {
    SymbolCacheEntry * entry = symbol_cache + symbol_hash(process, pc, name);
    assert(process != NULL);
    if (entry->process != process) return 0;
    if (entry->pc != pc) return 0;
    if (strcmp(entry->name, name)) return 0;
    *sym = entry->sym;
    *error = entry->error;
    return 1;
}

static void add_cache_symbol(HANDLE process, ULONG64 pc, PCSTR name, Symbol * sym, int error) {
    SymbolCacheEntry * entry = symbol_cache + symbol_hash(process, pc, name);
    assert(process != NULL);
    entry->process = process;
    entry->pc = pc;
    strcpy(entry->name, name);
    entry->error = error;
    if (!error) entry->sym = *sym;
    else memset(&entry->sym, 0, sizeof(Symbol));
}

static int find_pe_symbol(Context * ctx, int frame, char * name, Symbol * sym) {
    ULONG64 buffer[(sizeof(SYMBOL_INFO) + MAX_SYM_NAME * sizeof(TCHAR) + sizeof(ULONG64) - 1) / sizeof(ULONG64)];
    SYMBOL_INFO * info = (SYMBOL_INFO *)buffer;
    IMAGEHLP_STACK_FRAME stack_frame;
    HANDLE process = ctx->parent == NULL ? ctx->handle : ctx->parent->handle;
    DWORD64 module;

    if (get_stack_frame(ctx, frame, &stack_frame) < 0) return -1;
    if (find_cache_symbol(process, stack_frame.InstructionOffset, name, sym, &errno)) return errno ? -1 : 0;

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
        syminfo2symbol(ctx, info, sym);
        add_cache_symbol(process, stack_frame.InstructionOffset, name, sym, 0);
        return 0;
    }
    module = SymGetModuleBase64(process, stack_frame.InstructionOffset);
    if (module != 0 && SymGetTypeFromName(process, module, name, info)) {
        syminfo2symbol(ctx, info, sym);
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
    size_t i = 0;
    const struct TypeInfo * p = basic_type_info;
    while (p->name != NULL) {
        if (strcmp(p->name, name) == 0) break;
        p++;
        i++;
    }
    if (p->name != NULL) {
        memset(sym, 0, sizeof(*sym));
        sym->ctx = ctx;
        sym->sym_class = SYM_CLASS_TYPE;
        LOC(sym)->bst_index = i + 1;
        return 0;
    }
    errno = ERR_SYM_NOT_FOUND;
    return -1;
}

int find_symbol(Context * ctx, int frame, char * name, Symbol * sym) {
    if (find_pe_symbol(ctx, frame, name, sym) >= 0) return 0;
    if (errno != ERR_SYM_NOT_FOUND) return -1;
    if (find_test_symbol(ctx, name, sym, &LOC(sym)->address) >= 0) return 0;
    if (find_basic_type_symbol(ctx, name, sym) >= 0) return 0;
    return -1;
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
    unsigned i;
    for (i = 0; i < SYMBOL_CACHE_SIZE; i++) {
        if (symbol_cache[i].sym.ctx == ctx) symbol_cache[i].process = NULL;
    }
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
        unsigned i;
        assert(ctx->pid == ctx->mem);
        assert(ctx->handle != NULL);
        if (!SymLoadModule64(ctx->handle, ctx->module_handle, NULL, NULL, ctx->module_address, 0)) {
            set_win32_errno(GetLastError());
            trace(LOG_ALWAYS, "SymLoadModule() error: %d: %s",
                errno, errno_to_str(errno));
        }
        for (i = 0; i < SYMBOL_CACHE_SIZE; i++) {
            if (symbol_cache[i].process == ctx->handle && symbol_cache[i].error) symbol_cache[i].process = NULL;
        }
    }
    if (ctx->module_unloaded) {
        unsigned i;
        assert(ctx->pid == ctx->mem);
        assert(ctx->handle != NULL);
        for (i = 0; i < SYMBOL_CACHE_SIZE; i++) {
            if (symbol_cache[i].process == ctx->handle) symbol_cache[i].process = NULL;
        }
        if (!SymUnloadModule64(ctx->handle, ctx->module_address)) {
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
    assert(sizeof(SymLocation) <= sizeof(((Symbol *)0)->location));
    add_context_event_listener(&listener, NULL);
    SymSetOptions(SYMOPT_UNDNAME | SYMOPT_LOAD_LINES | SYMOPT_DEFERRED_LOADS);
}


#endif /* SERVICE_Symbols && defined(_MSC_VER) */

