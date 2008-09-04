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

#define SYM_FLAG ((U8_T)1 << 63)

static void object2symbol(Context * ctx, ObjectInfo * obj, Symbol * sym) {
    memset(sym, 0, sizeof(Symbol));
    sym->ctx = ctx;
    sym->module_id = (ModuleID)(unsigned)obj->mCompUnit->mFile;
    sym->symbol_id = (SymbolID)(unsigned)obj;
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

int find_symbol(Context * ctx, int frame, char * name, Symbol * sym) {
    int error = 0;
    int found = 0;

#if defined(_WRS_KERNEL)
    
    char * ptr;
    SYM_TYPE type;

    memset(sym, 0, sizeof(Symbol));
    if (symFindByName(sysSymTbl, name, &ptr, &type) != OK) {
        error = errno;
        assert(error != 0);
        if (error == S_symLib_SYMBOL_NOT_FOUND) error = 0;
    }
    else {
        sym->ctx = ctx;
        sym->symbol_id = (SymbolID)(unsigned)ptr;
        
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
        ELF_File * file = NULL;
        
        if (frame != STACK_NO_FRAME) {
            if (get_frame_info(ctx, frame, &ip, NULL, NULL) < 0) error = errno;
        }
    
        if (error == 0) {
            file = elf_list_first(ctx, ip, ip == 0 ? ~(ContextAddress)0 : ip + 1);
            if (file == NULL) error = errno;
        }
    
        while (error == 0 && file != NULL) {
            Trap trap;
            if (set_trap(&trap)) {
                DWARFCache * cache = get_dwarf_cache(file);
                if (ip != 0) {
                    unsigned i;
                    for (i = 0; i < cache->mCompUnitsCnt; i++) {
                        CompUnit * unit = cache->mCompUnits[i];
                        if (unit->mLowPC <= ip && unit->mHighPC > ip) {
                            found = find_in_object_tree(ctx, unit->mChildren, ip, name, sym);
                            if (found) break;
                            if (unit->mBaseTypes != NULL) {
                                found = find_in_object_tree(ctx, unit->mBaseTypes->mChildren, ip, name, sym);
                                if (found) break;
                            }
                        }
                    }
                }
                if (!found) {
                    unsigned m = 0;
                    unsigned h = calc_symbol_name_hash(name);
                    while (m < cache->sym_sections_cnt && !found) {
                        SymbolSection * tbl = cache->sym_sections[m];
                        unsigned n = tbl->mSymbolHash[h];
                        while (n && !found) {
                            if (file->elf64) {
                                Elf64_Sym * s = (Elf64_Sym *)tbl->mSymPool + n;
                                if (strcmp(name, tbl->mStrPool + s->st_name) == 0) {
                                    found = 1;
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
                                    sym->module_id = (ModuleID)(unsigned)file;
                                    sym->symbol_id = (SymbolID)(((U8_T)m << 32) | (U8_T)n | SYM_FLAG);
                                }
                            }
                            else {
                                Elf32_Sym * s = (Elf32_Sym *)tbl->mSymPool + n;
                                if (strcmp(name, tbl->mStrPool + s->st_name) == 0) {
                                    found = 1;
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
                                    sym->module_id = (ModuleID)(unsigned)file;
                                    sym->symbol_id = (SymbolID)(((U8_T)m << 32) | (U8_T)n | SYM_FLAG);
                                }
                            }
                            n = tbl->mHashNext[n];
                        }
                        m++;
                    }
                }
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
    ELF_File * file = NULL;
    
    if (frame != STACK_NO_FRAME) {
        if (get_frame_info(ctx, frame, &ip, NULL, NULL) < 0) error = errno;
    }

    if (error == 0) {
        file = elf_list_first(ctx, ip, ip == 0 ? ~(ContextAddress)0 : ip + 1);
        if (file == NULL) error = errno;
    }
    

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

    if (error) {
        errno = error;
        return -1;
    }
    return 0;
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

void ini_symbols_service(void) {
}

/*************** Functions for retrieving symbol properties ***************************************/

static ELF_File * file;
static DWARFCache * cache;
static ObjectInfo * obj;
static SymbolSection * tbl;
static unsigned sym_index;
static Elf32_Sym * sym32;
static Elf64_Sym * sym64;

static int unpack(const Symbol * sym) {
    file = (ELF_File *)(unsigned)sym->module_id;
    cache = NULL;
    obj = NULL;
    tbl = NULL;
    sym_index = 0;
    sym32 = NULL;
    sym64 = NULL;
    if (file != NULL && sym->symbol_id != 0) {
        cache = (DWARFCache *)file->dwarf_dt_cache;
        if (cache == NULL || cache->magic != SYM_CACHE_MAGIC) {
            errno = ERR_INV_CONTEXT;
            return -1;
        }
        if (sym->symbol_id & SYM_FLAG) {
            unsigned m = (unsigned)(sym->symbol_id >> 32) & 0xffffff;
            obj = NULL;
            tbl = cache->sym_sections[m];
            sym_index = (unsigned)sym->symbol_id;
        }
        else {
            obj = (ObjectInfo *)(unsigned)sym->symbol_id;
            tbl = obj->mSymbolSection;
            sym_index = obj->mSymbol;
        }
        if (tbl != NULL) {
            if (file->elf64) {
                sym64 = (Elf64_Sym *)tbl->mSymPool + sym_index;
            }
            else {
                sym32 = (Elf32_Sym *)tbl->mSymPool + sym_index;
            }
        }
        return 0;
    }
    errno = ERR_INV_CONTEXT;
    return -1;
}

int get_symbol_type_class(const Symbol * sym, int * type_class) {
#if !defined(_WRS_KERNEL)
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
        case TAG_constant:
            obj = obj->mType;
            break;
        default:
            obj = NULL;
            break;
        }
    }
#endif    
    *type_class = TYPE_CLASS_UNKNOWN;
    return 0;
}

int get_symbol_name(const Symbol * sym, char ** name) {
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
    if (unpack(sym) < 0) return -1;
    if (obj != NULL) {
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

int get_symbol_base_type(const Symbol * sym, SymbolID * base_type) {
    errno = ERR_UNSUPPORTED;
    return -1;
}

int get_symbol_index_type(const Symbol * sym, SymbolID * index_type) {
    errno = ERR_UNSUPPORTED;
    return -1;
}

int get_symbol_length(const Symbol * sym, unsigned long * length) {
    errno = ERR_UNSUPPORTED;
    return -1;
}

int get_symbol_children(const Symbol * sym, SymbolID ** children) {
    errno = ERR_UNSUPPORTED;
    return -1;
}

int get_symbol_offset(const Symbol * sym, unsigned long * offset) {
    errno = ERR_UNSUPPORTED;
    return -1;
}

int get_symbol_value(const Symbol * sym, size_t * size, void * value) {
    if (unpack(sym) < 0) return -1;
    if (obj != NULL && obj->mConstValueAddr != NULL) {
        if (*size < obj->mConstValueSize) {
            errno = ERR_BUFFER_OVERFLOW;
            return -1;
        }
        memcpy(value, obj->mConstValueAddr, obj->mConstValueSize);
        *size = obj->mConstValueSize;
        return 0;
    }
    errno = ERR_INV_CONTEXT;
    return -1;
}

int get_symbol_address(const Symbol * sym, int frame, ContextAddress * address) {
#if defined(_WRS_KERNEL)
    *address = (ContextAddress)sym->symbol_id;
    return 0;
#else
    if (unpack(sym) < 0) return -1;
    if (obj != NULL) {
        if ((*address = (ContextAddress)dwarf_expression_addr(sym->ctx, frame, obj)) == 0) return -1;
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
    errno = ERR_INV_CONTEXT;
    return -1;
#endif    
}

#endif

