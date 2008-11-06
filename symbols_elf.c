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
 * Symbols service.- ELF version
 */
#include "mdep.h"
#include "config.h"

#if (SERVICE_Symbols) && (ENABLE_ELF)
#if defined(_WRS_KERNEL)
#  include <symLib.h>
#  include <sysSymTbl.h>
#endif

#include <errno.h>
#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include "errors.h"
#include "elf.h"
#include "dwarf.h"
#include "myalloc.h"
#include "events.h"
#include "exceptions.h"
#include "dwarfcache.h"
#include "dwarfexpr.h"
#include "stacktrace.h"
#include "symbols.h"

typedef struct SymLocation {
#if defined(_WRS_KERNEL)
    char * addr;
#endif
    ObjectInfo * obj;
    SymbolSection * tbl;
    unsigned index;
    unsigned dimension;
    unsigned pointer;
} SymLocation;

static void object2symbol(Context * ctx, ObjectInfo * obj, Symbol * sym) {
    SymLocation * loc = (SymLocation *)sym->location;
    memset(sym, 0, sizeof(Symbol));
    sym->ctx = ctx;
    loc->obj = obj;
    loc->tbl = obj->mSymbolSection;
    loc->index = obj->mSymbol;
    switch (obj->mTag) {
    case TAG_global_subroutine:
    case TAG_subroutine:
    case TAG_subprogram:
    case TAG_entry_point:
        sym->sym_class = SYM_CLASS_FUNCTION;
        break;
    case TAG_array_type:
    case TAG_class_type:
    case TAG_enumeration_type:
    case TAG_pointer_type:
    case TAG_reference_type:
    case TAG_string_type:
    case TAG_structure_type:
    case TAG_subroutine_type:
    case TAG_union_type:
    case TAG_ptr_to_member_type:
    case TAG_set_type:
    case TAG_subrange_type:
    case TAG_base_type:
    case TAG_file_type:
    case TAG_packed_type:
    case TAG_thrown_type:
    case TAG_volatile_type:
    case TAG_restrict_type:
    case TAG_interface_type:
    case TAG_unspecified_type:
    case TAG_mutable_type:
    case TAG_shared_type:
    case TAG_typedef:
        sym->sym_class = SYM_CLASS_TYPE;
        break;
    case TAG_formal_parameter:
    case TAG_global_variable:
    case TAG_local_variable:
    case TAG_variable:
    case TAG_member:
        sym->sym_class = SYM_CLASS_REFERENCE;
        break;
    case TAG_constant:
    case TAG_enumerator:
        sym->sym_class = SYM_CLASS_VALUE;
        break;
    }
}

static int find_in_object_tree(Context * ctx, ObjectInfo * list, ContextAddress ip, char * name, Symbol * sym) {
    int found = 0;
    ObjectInfo * obj = list;
    while (obj != NULL) {
        if (obj->mName != NULL && strcmp(obj->mName, name) == 0) {
            object2symbol(ctx, obj, sym);
            found = 1;
        }
        switch (obj->mTag) {
        case TAG_enumeration_type:
            found = find_in_object_tree(ctx, obj->mChildren, ip, name, sym);
            break;
        case TAG_global_subroutine:
        case TAG_subroutine:
        case TAG_subprogram:
        case TAG_entry_point:
        case TAG_lexical_block:
            if (obj->mLowPC <= ip && obj->mHighPC > ip) {
                if (find_in_object_tree(ctx, obj->mChildren, ip, name, sym)) return 1;
            }
            break;
        }
        obj = obj->mSibling;
    }
    return found;
}

static int find_in_dwarf(DWARFCache * cache, Context * ctx, char * name, ContextAddress ip, Symbol * sym) {
    unsigned i;
    for (i = 0; i < cache->mCompUnitsCnt; i++) {
        CompUnit * unit = cache->mCompUnits[i];
        if (unit->mLowPC <= ip && unit->mHighPC > ip) {
            if (find_in_object_tree(ctx, unit->mChildren, ip, name, sym)) return 1;
            if (unit->mBaseTypes != NULL) {
                if (find_in_object_tree(ctx, unit->mBaseTypes->mChildren, ip, name, sym)) return 1;
            }
        }
    }
    return 0;
}

static int find_in_sym_table(DWARFCache * cache, Context * ctx, char * name, Symbol * sym) {
    unsigned m = 0;
    unsigned h = calc_symbol_name_hash(name);
    SymLocation * loc = (SymLocation *)sym->location;
    while (m < cache->sym_sections_cnt) {
        SymbolSection * tbl = cache->sym_sections[m];
        unsigned n = tbl->mSymbolHash[h];
        while (n) {
            if (cache->mFile->elf64) {
                Elf64_Sym * s = (Elf64_Sym *)tbl->mSymPool + n;
                if (strcmp(name, tbl->mStrPool + s->st_name) == 0) {
                    memset(sym, 0, sizeof(Symbol));
                    sym->ctx = ctx;
                    switch (ELF64_ST_TYPE(s->st_info)) {
                    case STT_FUNC:
                        sym->sym_class = SYM_CLASS_FUNCTION;
                        break;
                    case STT_OBJECT:
                        sym->sym_class = SYM_CLASS_REFERENCE;
                        break;
                    }
                    assert(m <= 0xff);
                    loc->tbl = tbl;
                    loc->index = n;
                    return 1;
                }
            }
            else {
                Elf32_Sym * s = (Elf32_Sym *)tbl->mSymPool + n;
                if (strcmp(name, tbl->mStrPool + s->st_name) == 0) {
                    memset(sym, 0, sizeof(Symbol));
                    sym->ctx = ctx;
                    switch (ELF32_ST_TYPE(s->st_info)) {
                    case STT_FUNC:
                        sym->sym_class = SYM_CLASS_FUNCTION;
                        break;
                    case STT_OBJECT:
                        sym->sym_class = SYM_CLASS_REFERENCE;
                        break;
                    }
                    loc->tbl = tbl;
                    loc->index = n;
                    return 1;
                }
            }
            n = tbl->mHashNext[n];
        }
        m++;
    }
    return 0;
}

int find_symbol(Context * ctx, int frame, char * name, Symbol * sym) {
    int error = 0;
    int found = 0;

#if defined(_WRS_KERNEL)

    char * ptr;
    SYM_TYPE type;

    if (symFindByName(sysSymTbl, name, &ptr, &type) != OK) {
        error = errno;
        assert(error != 0);
        if (error == S_symLib_SYMBOL_NOT_FOUND) error = 0;
    }
    else {
        memset(sym, 0, sizeof(Symbol));
        sym->ctx = ctx;
        ((SymLocation *)sym->location)->addr = ptr;

        if (SYM_IS_TEXT(type)) {
            sym->sym_class = SYM_CLASS_FUNCTION;
        }
        else {
            sym->sym_class = SYM_CLASS_REFERENCE;
        }
        found = 1;
    }

#endif

    if (error == 0 && !found) {
        ContextAddress ip = 0;

        if (frame != STACK_NO_FRAME) {
            if (get_frame_info(ctx, frame, &ip, NULL, NULL) < 0) error = errno;
        }

        if (error == 0) {
            ELF_File * file = elf_list_first(ctx, ip, ip == 0 ? ~(ContextAddress)0 : ip + 1);
            if (file == NULL) error = errno;
            while (error == 0 && file != NULL) {
                Trap trap;
                if (set_trap(&trap)) {
                    DWARFCache * cache = get_dwarf_cache(file);
                    if (ip != 0) found = find_in_dwarf(cache, ctx, name, ip, sym);
                    if (!found) found = find_in_sym_table(cache, ctx, name, sym);
                    clear_trap(&trap);
                }
                else {
                    error = trap.error;
                    break;
                }
                if (found) break;
                file = elf_list_next(ctx);
                if (file == NULL) error = errno;
            }
            elf_list_done(ctx);
        }
    }

    if (error == 0 && !found) error = ERR_SYM_NOT_FOUND;

    if (error) {
        errno = error;
        return -1;
    }
    return 0;
}

static void enumerate_local_vars(Context * ctx, ObjectInfo * obj, ContextAddress ip, int level, EnumerateSymbolsCallBack * call_back, void * args) {
    Symbol sym;
    while (obj != NULL) {
        switch (obj->mTag) {
        case TAG_global_subroutine:
        case TAG_subroutine:
        case TAG_subprogram:
        case TAG_entry_point:
        case TAG_lexical_block:
            if (obj->mLowPC <= ip && obj->mHighPC > ip) {
                enumerate_local_vars(ctx, obj->mChildren, ip, level + 1, call_back, args);
            }
            break;
        case TAG_formal_parameter:
        case TAG_local_variable:
        case TAG_variable:
            if (level > 0) {
                object2symbol(ctx, obj, &sym);
                call_back(args, obj->mName, &sym);
            }
            break;
        }
        obj = obj->mSibling;
    }
}

int enumerate_symbols(Context * ctx, int frame, EnumerateSymbolsCallBack * call_back, void * args) {
    int error = 0;
    ContextAddress ip = 0;

    if (frame != STACK_NO_FRAME) {
        if (get_frame_info(ctx, frame, &ip, NULL, NULL) < 0) error = errno;
    }

    if (error == 0) {
        ELF_File * file = elf_list_first(ctx, ip, ip == 0 ? ~(ContextAddress)0 : ip + 1);
        if (file == NULL) error = errno;
        while (error == 0 && file != NULL) {
            Trap trap;
            if (set_trap(&trap)) {
                DWARFCache * cache = get_dwarf_cache(file);
                if (ip != 0) {
                    unsigned i;
                    for (i = 0; i < cache->mCompUnitsCnt; i++) {
                        CompUnit * unit = cache->mCompUnits[i];
                        if (unit->mLowPC <= ip && unit->mHighPC > ip) {
                            enumerate_local_vars(ctx, unit->mChildren, ip, 0, call_back, args);
                        }
                    }
                }
                clear_trap(&trap);
            }
            else {
                error = trap.error;
                break;
            }
            file = elf_list_next(ctx);
            if (file == NULL) error = errno;
        }
        elf_list_done(ctx);
    }

    if (error) {
        errno = error;
        return -1;
    }
    return 0;
}

char * symbol2id(const Symbol * sym) {
    static char id[256];
    const SymLocation * loc = (SymLocation *)sym->location;
    ELF_File * file = NULL;
    unsigned long long obj_index = 0;
    unsigned tbl_index = 0;

    if (loc->obj != NULL) file = loc->obj->mCompUnit->mFile;
    if (loc->tbl != NULL) file = loc->tbl->mFile;
    if (file == NULL) return "SYM";
    if (loc->obj != NULL) obj_index = loc->obj->mID;
    if (loc->tbl != NULL) tbl_index = loc->tbl->mIndex + 1;
    snprintf(id, sizeof(id), "SYM%X.%lX.%lX.%X.%llX.%X.%X.%X.%X.%s",
        sym->sym_class, (unsigned long)file->dev, (unsigned long)file->ino,
        (unsigned)file->mtime & 0xffff, obj_index, tbl_index,
        loc->index, loc->dimension, loc->pointer, container_id(sym->ctx));
    return id;
}

static unsigned read_hex(char ** s) {
    unsigned res = 0;
    char * p = *s;
    while (1) {
        if (*p >= '0' && *p <= '9') res = (res << 4) | (*p - '0');
        else if (*p >= 'A' && *p <= 'F') res = (res << 4) | (*p - 'A' + 10);
        else break;
        p++;
    }
    *s = p;
    return res;
}

static unsigned long long read_hex_ll(char ** s) {
    unsigned long long res = 0;
    char * p = *s;
    while (1) {
        if (*p >= '0' && *p <= '9') res = (res << 4) | (*p - '0');
        else if (*p >= 'A' && *p <= 'F') res = (res << 4) | (*p - 'A' + 10);
        else break;
        p++;
    }
    *s = p;
    return res;
}

int id2symbol(char * id, Symbol * sym) {
    dev_t dev = 0;
    ino_t ino = 0;
    unsigned mtime = 0;
    unsigned long long obj_index = 0;
    unsigned tbl_index = 0;
    SymLocation * loc = (SymLocation *)sym->location;
    ELF_File * file = NULL;
    char * p;
    Trap trap;

    memset(sym, 0, sizeof(Symbol));
    if (id == NULL || id[0] != 'S' || id[1] != 'Y' || id[2] != 'M') {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    p = id + 3;
    if (*p == 0) return 0;
    sym->sym_class = read_hex(&p);
    if (*p == '.') p++;
    dev = read_hex(&p);
    if (*p == '.') p++;
    ino = read_hex(&p);
    if (*p == '.') p++;
    mtime = read_hex(&p);
    if (*p == '.') p++;
    obj_index = read_hex_ll(&p);
    if (*p == '.') p++;
    tbl_index = read_hex(&p);
    if (*p == '.') p++;
    loc->index = read_hex(&p);
    if (*p == '.') p++;
    loc->dimension = read_hex(&p);
    if (*p == '.') p++;
    loc->pointer = read_hex(&p);
    if (*p == '.') p++;
    sym->ctx = id2ctx(p);
    if (sym->ctx == NULL) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    file = elf_list_first(sym->ctx, 0, ~(ContextAddress)0);
    if (file == NULL) return -1;
    while (file->dev != dev || file->ino != ino || ((unsigned)file->mtime & 0xffff) != mtime) {
        file = elf_list_next(sym->ctx);
        if (file == NULL) break;
    }
    elf_list_done(sym->ctx);
    if (file == NULL) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (set_trap(&trap)) {
        DWARFCache * cache = get_dwarf_cache(file);
        if (obj_index) {
            loc->obj = find_object(cache, obj_index);
            if (loc->obj == NULL) exception(ERR_INV_CONTEXT);
        }
        if (tbl_index) {
            if (tbl_index > cache->sym_sections_cnt) exception(ERR_INV_CONTEXT);
            loc->tbl = cache->sym_sections[tbl_index - 1];
        }
        clear_trap(&trap);
        return 0;
    }
    return -1;
}

ContextAddress is_plt_section(Context * ctx, ContextAddress addr) {
    ContextAddress res = 0;
    ELF_File * file = elf_list_first(ctx, addr, addr);
    while (file != NULL) {
        unsigned idx;
        for (idx = 0; idx < file->section_cnt; idx++) {
            ELF_Section * sec = file->sections[idx];
            if (sec == NULL) continue;
            if (sec->name == NULL) continue;
            if (strcmp(sec->name, ".plt") != 0) continue;
            if (addr >= sec->addr && addr < sec->addr + sec->size) {
                res = (ContextAddress)sec->addr;
                break;
            }
        }
        if (res != 0) break;
        file = elf_list_next(ctx);
    }
    elf_list_done(ctx);
    return res;
}

extern void ini_symbols_lib(void);

void ini_symbols_lib(void) {
    assert(sizeof(SymLocation) <= sizeof(((Symbol *)0)->location));
}

/*************** Functions for retrieving symbol properties ***************************************/

static ELF_File * file;
static DWARFCache * cache;
static ObjectInfo * obj;
static SymbolSection * tbl;
static unsigned sym_index;
static unsigned dimension;
static Elf32_Sym * sym32;
static Elf64_Sym * sym64;

static int unpack(const Symbol * sym) {
    const SymLocation * loc = (const SymLocation *)sym->location;
    file = NULL;
    cache = NULL;
    obj = loc->obj;
    tbl = loc->tbl;
    sym_index = loc->index;
    dimension = loc->dimension;
    sym32 = NULL;
    sym64 = NULL;
    if (obj != NULL) file = obj->mCompUnit->mFile;
    if (tbl != NULL) file = tbl->mFile;
    if (file != NULL) {
        cache = (DWARFCache *)file->dwarf_dt_cache;
        if (cache == NULL || cache->magic != SYM_CACHE_MAGIC) {
            errno = ERR_INV_CONTEXT;
            return -1;
        }
        if (tbl != NULL) {
            if (file->elf64) {
                sym64 = (Elf64_Sym *)tbl->mSymPool + sym_index;
            }
            else {
                sym32 = (Elf32_Sym *)tbl->mSymPool + sym_index;
            }
        }
    }
    return 0;
}

static ObjectInfo * get_object_type(ObjectInfo * obj) {
    while (obj != NULL) {
        switch (obj->mTag) {
        case TAG_global_subroutine:
        case TAG_subroutine:
        case TAG_subprogram:
        case TAG_entry_point:
        case TAG_enumerator:
        case TAG_formal_parameter:
        case TAG_global_variable:
        case TAG_local_variable:
        case TAG_variable:
        case TAG_member:
        case TAG_constant:
            obj = obj->mType;
            break;
        case TAG_typedef:
            if (obj->mType == NULL) return obj;
            obj = obj->mType;
            break;
        default:
            return obj;
        }
    }
    return NULL;
}

int get_symbol_type(const Symbol * sym, Symbol * type) {
    if (((SymLocation *)sym->location)->pointer) {
        *type = *sym;
        return 0;
    }
    if (unpack(sym) < 0) return -1;
    obj = get_object_type(obj);
    if (obj != NULL) object2symbol(sym->ctx, obj, type);
    else memset(type, 0, sizeof(Symbol));
    return 0;
}

int get_symbol_type_class(const Symbol * sym, int * type_class) {
    if (((SymLocation *)sym->location)->pointer) {
        *type_class = TYPE_CLASS_POINTER;
        return 0;
    }
    if (unpack(sym) < 0) return -1;
    while (obj != NULL) {
        switch (obj->mTag) {
        case TAG_global_subroutine:
        case TAG_subroutine:
        case TAG_subprogram:
        case TAG_entry_point:
        case TAG_subroutine_type:
            *type_class = TYPE_CLASS_FUNCTION;
            return 0;
        case TAG_array_type:
        case TAG_string_type:
            *type_class = TYPE_CLASS_ARRAY;
            return 0;
        case TAG_enumeration_type:
        case TAG_enumerator:
            *type_class = TYPE_CLASS_ENUMERATION;
            return 0;
        case TAG_pointer_type:
        case TAG_reference_type:
            *type_class = TYPE_CLASS_POINTER;
            return 0;
        case TAG_class_type:
        case TAG_structure_type:
        case TAG_union_type:
        case TAG_interface_type:
            *type_class = TYPE_CLASS_COMPOSITE;
            return 0;
        case TAG_base_type:
            switch (obj->mEncoding) {
            case ATE_address:
                *type_class = TYPE_CLASS_POINTER;
                return 0;
            case ATE_boolean:
                *type_class = TYPE_CLASS_INTEGER;
                return 0;
            case ATE_float:
                *type_class = TYPE_CLASS_REAL;
                return 0;
            case ATE_signed:
            case ATE_signed_char:
                *type_class = TYPE_CLASS_INTEGER;
                return 0;
            case ATE_unsigned:
            case ATE_unsigned_char:
                *type_class = TYPE_CLASS_CARDINAL;
                return 0;
            }
            return 0;
        case TAG_subrange_type:
        case TAG_packed_type:
        case TAG_volatile_type:
        case TAG_restrict_type:
        case TAG_typedef:
        case TAG_formal_parameter:
        case TAG_global_variable:
        case TAG_local_variable:
        case TAG_variable:
        case TAG_member:
        case TAG_constant:
            obj = obj->mType;
            break;
        default:
            obj = NULL;
            break;
        }
    }
    *type_class = TYPE_CLASS_UNKNOWN;
    return 0;
}

int get_symbol_name(const Symbol * sym, char ** name) {
    if (((SymLocation *)sym->location)->pointer) {
        *name = NULL;
        return 0;
    }
    if (unpack(sym) < 0) return -1;
    if (obj != NULL) {
        *name = obj->mName == NULL ? NULL : loc_strdup(obj->mName);
    }
    else if (sym32 != NULL) {
        *name = sym32->st_name == 0 ? NULL : loc_strdup(tbl->mStrPool + sym32->st_name);
    }
    else if (sym64 != NULL) {
        *name = sym64->st_name == 0 ? NULL : loc_strdup(tbl->mStrPool + sym64->st_name);
    }
    else {
        *name = NULL;
    }
    return 0;
}

int get_symbol_size(const Symbol * sym, size_t * size) {
    if (((SymLocation *)sym->location)->pointer) {
        *size = sizeof(void *);
        return 0;
    }
    if (unpack(sym) < 0) return -1;
    if (obj != NULL) {
        if (sym->sym_class == SYM_CLASS_REFERENCE && obj->mSize == 0 && obj->mType != NULL) obj = obj->mType;
        while (obj->mSize == 0 && obj->mType != NULL) {
            if (obj->mTag != TAG_typedef &&
                obj->mTag != TAG_enumeration_type)
                break;
            obj = obj->mType;
        }
        if (obj->mTag == TAG_array_type) {
            int i = dimension;
            unsigned length = 1;
            ObjectInfo * idx = obj->mChildren;
            while (i > 0 && idx != NULL) {
                idx = idx->mSibling;
                i--;
            }
            if (idx == NULL) {
                errno = ERR_INV_CONTEXT;
                return -1;
            }
            while (idx != NULL) {
                length *= idx->mLength;
                idx = idx->mSibling;
            }
            if (obj->mType != NULL) {
                obj = obj->mType;
                while (obj->mSize == 0 && obj->mType != NULL) {
                    if (obj->mTag != TAG_typedef &&
                        obj->mTag != TAG_enumeration_type)
                        break;
                    obj = obj->mType;
                }
                *size = (size_t)(length * obj->mSize);
                return 0;
            }
            errno = ERR_INV_CONTEXT;
            return -1;
        }
        *size = (size_t)obj->mSize;
    }
    else if (sym32 != NULL) {
        *size = (size_t)sym32->st_size;
    }
    else if (sym64 != NULL) {
        *size = (size_t)sym64->st_size;
    }
    else {
        *size = 0;
    }
    return 0;
}

int get_symbol_base_type(const Symbol * sym, Symbol * base_type) {
    if (((SymLocation *)sym->location)->pointer) {
        *base_type = *sym;
        ((SymLocation *)base_type->location)->pointer--;
        return 0;
    }
    if (unpack(sym) < 0) return -1;
    if (obj != NULL) {
        obj = get_object_type(obj);
        if (obj->mTag == TAG_array_type) {
            int i = dimension;
            ObjectInfo * idx = obj->mChildren;
            while (i > 0 && idx != NULL) {
                idx = idx->mSibling;
                i--;
            }
            if (idx != NULL && idx->mSibling != NULL) {
                *base_type = *sym;
                ((SymLocation *)base_type->location)->dimension++;
                return 0;
            }
        }
        obj = obj->mType;
        if (obj != NULL) {
            obj = get_object_type(obj);
            object2symbol(sym->ctx, obj, base_type);
            return 0;
        }
    }
    errno = ERR_UNSUPPORTED;
    return -1;
}

int get_symbol_index_type(const Symbol * sym, Symbol * index_type) {
    if (((SymLocation *)sym->location)->pointer) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (unpack(sym) < 0) return -1;
    if (obj != NULL) {
        obj = get_object_type(obj);
        if (obj->mTag == TAG_array_type) {
            int i = dimension;
            ObjectInfo * idx = obj->mChildren;
            while (i > 0 && idx != NULL) {
                idx = idx->mSibling;
                i--;
            }
            if (idx != NULL) {
                object2symbol(sym->ctx, idx, index_type);
                return 0;
            }
        }
    }
    errno = ERR_UNSUPPORTED;
    return -1;
}

int get_symbol_length(const Symbol * sym, unsigned long * length) {
    if (((SymLocation *)sym->location)->pointer) {
        *length = 1;
        return 0;
    }
    if (unpack(sym) < 0) return -1;
    if (obj != NULL) {
        obj = get_object_type(obj);
        if (obj->mTag == TAG_array_type) {
            int i = dimension;
            ObjectInfo * idx = obj->mChildren;
            while (i > 0 && idx != NULL) {
                idx = idx->mSibling;
                i--;
            }
            if (idx != NULL) {
                *length = idx->mLength;
                return 0;
            }
        }
    }
    errno = ERR_UNSUPPORTED;
    return -1;
}

int get_symbol_children(const Symbol * sym, Symbol ** children, int * count) {
    if (((SymLocation *)sym->location)->pointer) {
        *children = NULL;
        *count = 0;
        return 0;
    }
    int n = 0;
    if (unpack(sym) < 0) return -1;
    *children = NULL;
    if (obj != NULL) {
        ObjectInfo * i = obj->mChildren;
        while (i != NULL) {
            i = i->mSibling;
            n++;
        }
        *children = loc_alloc_zero(sizeof(Symbol) * n);
        n = 0;
        i = obj->mChildren;
        while (i != NULL) {
            object2symbol(sym->ctx, i, *children + n);
            i = i->mSibling;
            n++;
        }
    }
    *count = n;
    return 0;
}

int get_symbol_offset(const Symbol * sym, unsigned long * offset) {
    if (((SymLocation *)sym->location)->pointer) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (unpack(sym) < 0) return -1;
    if (obj != NULL && obj->mTag == TAG_member) {
        U8_T addr = 0;
        if (dwarf_expression_addr(sym->ctx, STACK_NO_FRAME, 0, obj, &addr) < 0) return -1;
        *offset = (unsigned long)addr;
        return 0;
    }
    errno = ERR_INV_CONTEXT;
    return -1;
}

int get_symbol_value(const Symbol * sym, void ** value, size_t * size) {
    if (((SymLocation *)sym->location)->pointer) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (unpack(sym) < 0) return -1;
    if (obj != NULL && obj->mConstValueAddr != NULL) {
        *size = obj->mConstValueSize;
        *value = loc_alloc(obj->mConstValueSize);
        memcpy(*value, obj->mConstValueAddr, obj->mConstValueSize);
        return 0;
    }
    errno = ERR_INV_CONTEXT;
    return -1;
}

int get_symbol_address(const Symbol * sym, int frame, ContextAddress * address) {
    if (((SymLocation *)sym->location)->pointer) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (unpack(sym) < 0) return -1;
    if (obj != NULL && obj->mTag != TAG_member) {
        U8_T addr = 0;
        if (dwarf_expression_addr(sym->ctx, frame, 0, obj, &addr) < 0) return -1;
        *address = (ContextAddress)addr;
        return 0;
    }
    if (sym32 != NULL) {
        switch (ELF32_ST_TYPE(sym32->st_info)) {
        case STT_OBJECT:
        case STT_FUNC:
            *address = (ContextAddress)sym32->st_value;
            return 0;
        }
    }
    if (sym64 != NULL) {
        switch (ELF64_ST_TYPE(sym64->st_info)) {
        case STT_OBJECT:
        case STT_FUNC:
            *address = (ContextAddress)sym64->st_value;
            return 0;
        }
    }
#if defined(_WRS_KERNEL)
    if ((*address = (ContextAddress)((SymLocation *)sym->location)->addr) != 0) return 0;
#endif
    errno = ERR_INV_CONTEXT;
    return -1;
}

int get_symbol_pointer(const Symbol * sym, Symbol * ptr) {
    *ptr = *sym;
    if (!((SymLocation *)ptr->location)->pointer) {
        if (unpack(ptr) < 0) return -1;
        obj = get_object_type(obj);
        if (obj != NULL) {
                object2symbol(ptr->ctx, obj, ptr);
        }
        else {
                memset(ptr, 0, sizeof(Symbol));
                ptr->sym_class = SYM_CLASS_TYPE;
        }
    }
    assert(ptr->sym_class == SYM_CLASS_TYPE);
    ((SymLocation *)ptr->location)->pointer++;
    return 0;
}
#endif

