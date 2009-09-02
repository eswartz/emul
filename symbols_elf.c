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
 * Symbols service - ELF version.
 */

#include "config.h"

#if SERVICE_Symbols && ENABLE_ELF

#if defined(_WRS_KERNEL)
#  include <symLib.h>
#  include <sysSymTbl.h>
#endif

#include <errno.h>
#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include "errors.h"
#include "tcf_elf.h"
#include "dwarf.h"
#include "myalloc.h"
#include "events.h"
#include "exceptions.h"
#include "dwarfcache.h"
#include "dwarfexpr.h"
#include "stacktrace.h"
#include "test.h"
#include "symbols.h"

typedef struct SymLocation {
#if defined(_WRS_KERNEL)
    char * addr;
#endif
    ObjectInfo * obj;
    SymbolSection * tbl;
    void * address;
    unsigned index;
    unsigned dimension;
    unsigned pointer;
} SymLocation;

static void object2symbol(Context * ctx, ObjectInfo * obj, Symbol * sym) {
    SymLocation * loc = (SymLocation *)sym->location;
    memset(sym, 0, sizeof(Symbol));
    sym->ctx = ctx;
    loc->obj = obj;
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
            {
                ContextAddress addr0 = elf_map_to_run_time_address(ctx, obj->mCompUnit->mFile, obj->mLowPC);
                ContextAddress addr1 = obj->mHighPC - obj->mLowPC + addr0;
                if (addr0 != 0 && addr0 <= ip && addr1 > ip) {
                    if (find_in_object_tree(ctx, obj->mChildren, ip, name, sym)) return 1;
                }
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
        ContextAddress addr0 = elf_map_to_run_time_address(ctx, unit->mFile, unit->mLowPC);
        ContextAddress addr1 = unit->mHighPC - unit->mLowPC + addr0;
        if (addr0 != 0 && addr0 <= ip && addr1 > ip) {
            if (find_in_object_tree(ctx, unit->mChildren, ip, name, sym)) return 1;
            if (unit->mBaseTypes != NULL) {
                if (find_in_object_tree(ctx, unit->mBaseTypes->mChildren, ip, name, sym)) return 1;
            }
            return 0;
        }
    }
    return 0;
}

static int find_in_sym_table(DWARFCache * cache, Context * ctx, char * name, Symbol * sym) {
    unsigned m = 0;
    unsigned h = calc_symbol_name_hash(name);
    SymLocation * loc = (SymLocation *)sym->location;
    while (m < cache->mSymSectionsCnt) {
        SymbolSection * tbl = cache->mSymSections[m];
        unsigned n = tbl->mSymbolHash[h];
        while (n) {
            U8_T st_name = cache->mFile->elf64 ?
                ((Elf64_Sym *)tbl->mSymPool + n)->st_name :
                ((Elf32_Sym *)tbl->mSymPool + n)->st_name;
            if (strcmp(name, tbl->mStrPool + st_name) == 0) {
                int st_type = cache->mFile->elf64 ?
                    ELF64_ST_TYPE(((Elf64_Sym *)tbl->mSymPool + n)->st_info) :
                    ELF32_ST_TYPE(((Elf32_Sym *)tbl->mSymPool + n)->st_info);
                memset(sym, 0, sizeof(Symbol));
                sym->ctx = ctx;
                switch (st_type) {
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

    assert(ctx != NULL);

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

    if (!found) {
        SymLocation * loc = (SymLocation *)sym->location;
        found = find_test_symbol(ctx, name, sym, &loc->address) >= 0;
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
            {
                ContextAddress addr0 = elf_map_to_run_time_address(ctx, obj->mCompUnit->mFile, obj->mLowPC);
                ContextAddress addr1 = obj->mHighPC - obj->mLowPC + addr0;
                if (addr0 != 0 && addr0 <= ip && addr1 > ip) {
                    enumerate_local_vars(ctx, obj->mChildren, ip, level + 1, call_back, args);
                }
            }
            break;
        case TAG_formal_parameter:
        case TAG_local_variable:
        case TAG_variable:
            if (level > 0 && obj->mName != NULL) {
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
                        ContextAddress addr0 = elf_map_to_run_time_address(ctx, unit->mFile, unit->mLowPC);
                        ContextAddress addr1 = unit->mHighPC - unit->mLowPC + addr0;
                        if (addr0 != 0 && addr0 <= ip && addr1 > ip) {
                            enumerate_local_vars(ctx, unit->mChildren, ip, 0, call_back, args);
                            break;
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
    for (;;) {
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
    for (;;) {
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
            if (tbl_index > cache->mSymSectionsCnt) exception(ERR_INV_CONTEXT);
            loc->tbl = cache->mSymSections[tbl_index - 1];
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
        for (idx = 1; idx < file->section_cnt; idx++) {
            ELF_Section * sec = file->sections + idx;
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

static int get_num_prop(Context * ctx, int frame, ObjectInfo * obj, int at, U8_T * res) {
    PropertyValue v;

    if (read_and_evaluate_dwarf_object_property(ctx, frame, 0, obj, at, &v) < 0) return 0;
    *res = get_numeric_property_value(&v);
    return 1;
}

static U8_T get_object_length(Context * ctx, int frame, ObjectInfo * obj) {
    U8_T x, y;

    if (get_num_prop(ctx, frame, obj, AT_count, &x)) return x;
    if (get_num_prop(ctx, frame, obj, AT_upper_bound, &x)) {
        if (get_num_prop(ctx, frame, obj, AT_lower_bound, &y)) return x + 1 - y;
        return x + 1;
    }
    if (obj->mTag == TAG_enumeration_type) {
        ObjectInfo * c = obj->mChildren;
        x = 0;
        while (c != NULL) {
            x++;
            c = c->mSibling;
        }
        return x;
    }
    return 0;
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

int get_symbol_size(const Symbol * sym, int frame, size_t * size) {
    if (((SymLocation *)sym->location)->pointer) {
        *size = sizeof(void *);
        return 0;
    }
    if (unpack(sym) < 0) return -1;
    *size = 0;
    if (obj != NULL) {
        Trap trap;
        int ok = 0;
        U8_T sz = 0;

        if (!set_trap(&trap)) return -1;
        if (dimension == 0) ok = get_num_prop(sym->ctx, frame, obj, AT_byte_size, &sz);
        if (!ok && sym->sym_class == SYM_CLASS_REFERENCE && obj->mType != NULL) {
            obj = obj->mType;
            if (dimension == 0) ok = get_num_prop(sym->ctx, frame, obj, AT_byte_size, &sz);
        }
        while (!ok && obj->mType != NULL) {
            if (obj->mTag != TAG_typedef && obj->mTag != TAG_enumeration_type) break;
            obj = obj->mType;
            if (dimension == 0) ok = get_num_prop(sym->ctx, frame, obj, AT_byte_size, &sz);
        }
        if (!ok && obj->mTag == TAG_array_type) {
            size_t length = 1;
            int i = dimension;
            ObjectInfo * idx = obj->mChildren;
            while (i > 0 && idx != NULL) {
                idx = idx->mSibling;
                i--;
            }
            if (idx == NULL) exception(ERR_INV_CONTEXT);
            while (idx != NULL) {
                length *= (size_t)get_object_length(sym->ctx, frame, idx);
                idx = idx->mSibling;
            }
            if (obj->mType == NULL) exception(ERR_INV_CONTEXT);
            obj = obj->mType;
            ok = get_num_prop(sym->ctx, frame, obj, AT_byte_size, &sz);
            while (!ok && obj->mType != NULL) {
                if (obj->mTag != TAG_typedef && obj->mTag != TAG_enumeration_type) break;
                obj = obj->mType;
                ok = get_num_prop(sym->ctx, frame, obj, AT_byte_size, &sz);
            }
            if (ok) sz *= length;
        }
        if (ok) *size = (size_t)sz;
        clear_trap(&trap);
    }
    else if (sym32 != NULL) {
        *size = (size_t)sym32->st_size;
    }
    else if (sym64 != NULL) {
        *size = (size_t)sym64->st_size;
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

int get_symbol_length(const Symbol * sym, int frame, unsigned long * length) {
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
                Trap trap;
                if (!set_trap(&trap)) return -1;
                *length = (unsigned long)get_object_length(sym->ctx, frame, idx);
                clear_trap(&trap);
                return 0;
            }
        }
    }
    errno = ERR_UNSUPPORTED;
    return -1;
}

int get_symbol_children(const Symbol * sym, Symbol ** children, int * count) {
    int n = 0;
    if (((SymLocation *)sym->location)->pointer) {
        *children = NULL;
        *count = 0;
        return 0;
    }
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
        U8_T v;
        if (!get_num_prop(sym->ctx, STACK_NO_FRAME, obj, AT_data_member_location, &v)) return -1;
        *offset = (unsigned long)v;
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
    if (obj != NULL) {
        PropertyValue v;
        if (read_and_evaluate_dwarf_object_property(sym->ctx, STACK_NO_FRAME, 0, obj, AT_const_value, &v) < 0) return -1;
        if (v.mAddr != NULL) {
            *size = v.mSize;
            *value = loc_alloc(v.mSize);
            memcpy(*value, v.mAddr, v.mSize);
        }
        else {
            size_t sz = sizeof(v.mValue);
            U1_T * bf = loc_alloc(sz);
            U8_T n = v.mValue;
            size_t i = 0;
            for (i = 0; i < sz; i++) {
                bf[v.mBigEndian ? sz - i - 1 : i] = n & 0xffu;
                n = n >> 8;
            }
            *size = sz;
            *value = bf;
        }
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
    if (((SymLocation *)sym->location)->address != NULL) {
        *address = (ContextAddress)((SymLocation *)sym->location)->address;
        return 0;
    }
    if (unpack(sym) < 0) return -1;
    if (obj != NULL && obj->mTag != TAG_member) {
        U8_T v;
        if (!get_num_prop(sym->ctx, frame, obj, AT_location, &v)) return -1;
        *address = (ContextAddress)v;
        return 0;
    }
    if (sym32 != NULL) {
        switch (ELF32_ST_TYPE(sym32->st_info)) {
        case STT_OBJECT:
        case STT_FUNC:
            *address = elf_map_to_run_time_address(sym->ctx, file, (ContextAddress)sym32->st_value);
            return 0;
        }
    }
    if (sym64 != NULL) {
        switch (ELF64_ST_TYPE(sym64->st_info)) {
        case STT_OBJECT:
        case STT_FUNC:
            *address = elf_map_to_run_time_address(sym->ctx, file, (ContextAddress)sym64->st_value);
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

#endif /* SERVICE_Symbols && ENABLE_ELF */

