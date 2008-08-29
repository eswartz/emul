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

#if (SERVICE_Symbols) && !defined(WIN32)

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
#include "stacktrace.h"
#include "symbols.h"

static int find_in_object_tree(ObjectInfo * list, ContextAddress ip, char * name, Symbol * sym) {
    int found = 0;
    ObjectInfo * obj = list;
    while (obj != NULL) {
        if (obj->mName != NULL && strcmp(obj->mName, name) == 0) {
            memset(sym, 0, sizeof(Symbol));
            sym->module_id = (ModuleID)(unsigned)obj->mCompUnit->mFile;
            sym->object_id = (SymbolID)(unsigned)obj;
            sym->type_id = (SymbolID)(unsigned)obj->mType;
            switch (obj->mTag) {
            case TAG_global_subroutine:
            case TAG_subroutine:
            case TAG_subprogram:
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
            sym->base = obj->mLocBase;
            sym->address = obj->mLocOffset;
            sym->size = (size_t)obj->mSize;
            found = 1;
        }
        switch (obj->mTag) {
        case TAG_enumeration_type:
            found = find_in_object_tree(obj->mChildren, ip, name, sym);
            break;
        case TAG_global_subroutine:
        case TAG_subroutine:
        case TAG_subprogram:
        case TAG_lexical_block:
            if (ip == 0 || obj->mLocBase != SYM_BASE_ABS || obj->mSize == 0) break;
            if (obj->mLocOffset <= ip && obj->mLocOffset + obj->mSize > ip) {
                if (find_in_object_tree(obj->mChildren, ip, name, sym)) return 1;
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
        sym->base = SYM_BASE_ABS;
        sym->address = (ContextAddress)ptr;
        
        if (SYM_IS_UNDF(type)) sym->storage = "UNDEF";
        else if (SYM_IS_COMMON(type)) sym->storage = "COMMON";
        else if (SYM_IS_GLOBAL(type)) sym->storage = "GLOBAL";
        else if (SYM_IS_LOCAL(type)) sym->storage = "LOCAL";
        
        if (SYM_IS_TEXT(type)) sym->section = ".text";
        else if (SYM_IS_DATA(type)) sym->section = ".data";
        else if (SYM_IS_BSS(type)) sym->section = ".bss";
        assert(!SYM_IS_ABS(type) || sym->section == NULL);

        if (SYM_IS_TEXT(type)) {
            sym->sym_class = SYM_CLASS_FUNCTION;
        }
        else {
            sym->sym_class = SYM_CLASS_REFERENCE;
        }
        found = 1;
    }
    
#endif

    if (!found) {
        ContextAddress ip = 0;
        ELF_File * file = elf_list_first(ctx, 0, ~(ContextAddress)0);
        if (file == NULL) error = errno;
        memset(sym, 0, sizeof(Symbol));
    
        if (error == 0 && frame != STACK_NO_FRAME) {
            if (get_frame_info(ctx, frame, &ip, NULL, NULL) < 0) error = errno;
        }
    
        while (error == 0 && file != NULL) {
            Trap trap;
            if (set_trap(&trap)) {
                unsigned m = 0;
                unsigned h = calc_symbol_name_hash(name);
                DWARFCache * cache = get_dwarf_cache(file);
                if (ip != 0) {
                    unsigned i;
                    for (i = 0; i < cache->mCompUnitsCnt; i++) {
                        CompUnit * unit = cache->mCompUnits[i];
                        if (unit->mLowPC <= ip && unit->mHighPC > ip) {
                            found = find_in_object_tree(unit->mChildren, ip, name, sym);
                            if (found) break;
                        }
                    }
                }
                if (!found) {
                    while (m < cache->sym_sections_cnt && !found) {
                        SymbolSection * tbl = cache->sym_sections[m++];
                        int n = tbl->mSymbolHash[h];
                        while (n && !found) {
                            Elf32_Sym * s = (Elf32_Sym *)tbl->mSymPool + n;
                            if (strcmp(name, tbl->mStrPool + s->st_name) == 0) {
                                found = 1;
                                sym->base = SYM_BASE_ABS;
                                switch (ELF32_ST_BIND(s->st_info)) {
                                case STB_LOCAL: sym->storage = "LOCAL"; break;
                                case STB_GLOBAL: sym->storage = "GLOBAL"; break;
                                case STB_WEAK: sym->storage = "WEAK"; break;
                                }
                                switch (ELF32_ST_TYPE(s->st_info)) {
                                case STT_FUNC:
                                    sym->address = (ContextAddress)s->st_value;
                                    sym->sym_class = SYM_CLASS_FUNCTION;
                                    break;
                                case STT_OBJECT:
                                    sym->address = (ContextAddress)s->st_value;
                                    sym->size = s->st_size;
                                    sym->sym_class = SYM_CLASS_REFERENCE;
                                    break;
                                }
                                if (s->st_shndx > 0 && s->st_shndx < file->section_cnt) {
                                    ELF_Section * sec = file->sections[s->st_shndx];
                                    if (sec != NULL) sym->section = sec->name;
                                }
                                sym->module_id = (ModuleID)(unsigned)file;
                            }
                            n = tbl->mHashNext[n];
                        }
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

int enumerate_symbols(Context * ctx, int frame, EnumerateSymbolsCallBack * call_back, void * args) {
    /* TODO: ELF: enumerate symbols */
    return 0;
}

int get_symbol_class(Context * ctx, ModuleID module_id, SymbolID symbol_id, int * type_class) {
    ELF_File * file = (ELF_File *)(unsigned)module_id;
    if (file != NULL) {
        DWARFCache * cache = (DWARFCache *)file->dwarf_dt_cache;
        ObjectInfo * obj = (ObjectInfo *)(unsigned)symbol_id;
        assert(cache->magic == SYM_CACHE_MAGIC);
        while (obj != NULL) {
            switch (obj->mTag) {
            case TAG_global_subroutine:
            case TAG_subroutine:
            case TAG_subprogram:
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
            }
        }
    }
    *type_class = TYPE_CLASS_UNKNOWN;
    return 0;
}

int get_symbol_name(Context * ctx, ModuleID module_id, SymbolID symbol_id, char ** name) {
    errno = ERR_UNSUPPORTED;
    return -1;
}

int get_symbol_size(Context * ctx, ModuleID module_id, SymbolID symbol_id, uns64 * size) {
    errno = ERR_UNSUPPORTED;
    return -1;
}

int get_symbol_base_type(Context * ctx, ModuleID module_id, SymbolID symbol_id, SymbolID * base_type) {
    errno = ERR_UNSUPPORTED;
    return -1;
}

int get_symbol_index_type(Context * ctx, ModuleID module_id, SymbolID symbol_id, SymbolID * index_type) {
    errno = ERR_UNSUPPORTED;
    return -1;
}

int get_symbol_length(Context * ctx, ModuleID module_id, SymbolID symbol_id, unsigned long * length) {
    errno = ERR_UNSUPPORTED;
    return -1;
}

int get_symbol_children(Context * ctx, ModuleID module_id, SymbolID symbol_id, SymbolID ** children) {
    errno = ERR_UNSUPPORTED;
    return -1;
}

int get_symbol_offset(Context * ctx, ModuleID module_id, SymbolID symbol_id, unsigned long * offset) {
    errno = ERR_UNSUPPORTED;
    return -1;
}

int get_symbol_value(Context * ctx, ModuleID module_id, SymbolID symbol_id, size_t * size, void * value) {
    ELF_File * file = (ELF_File *)(unsigned)module_id;
    if (file != NULL) {
        DWARFCache * cache = (DWARFCache *)file->dwarf_dt_cache;
        ObjectInfo * obj = (ObjectInfo *)(unsigned)symbol_id;
        assert(cache->magic == SYM_CACHE_MAGIC);
        if (obj != NULL && obj->mConstValueAddr != NULL) {
            if (*size < obj->mConstValueSize) {
                errno = ERR_BUFFER_OVERFLOW;
                return -1;
            }
            memcpy(value, obj->mConstValueAddr, obj->mConstValueSize);
            *size = obj->mConstValueSize;
            return 0;
        }
    }
    errno = ERR_UNSUPPORTED;
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

void ini_symbols_service(void) {
}

#endif

