/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

/*
 * Symbols service - ELF version.
 */

#include <config.h>

#if SERVICE_Symbols && !ENABLE_SymbolsProxy && ENABLE_ELF

#if defined(_WRS_KERNEL)
#  include <symLib.h>
#  include <sysSymTbl.h>
#endif

#include <errno.h>
#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include <framework/errors.h>
#include <framework/myalloc.h>
#include <framework/events.h>
#include <framework/exceptions.h>
#include <services/tcf_elf.h>
#include <services/dwarf.h>
#include <services/dwarfcache.h>
#include <services/dwarfexpr.h>
#include <services/dwarfframe.h>
#include <services/stacktrace.h>
#include <services/symbols.h>
#if ENABLE_RCBP_TEST
#  include <main/test.h>
#endif

struct Symbol {
    unsigned magic;
    ObjectInfo * obj;
    ObjectInfo * var; /* 'this' object if the symbol represents implicit 'this' reference */
    ELF_Section * tbl;
    int has_size;
    int has_address;
    ContextAddress size;
    ContextAddress address;
    int sym_class;
    Context * ctx;
    int frame;
    unsigned index;
    unsigned dimension;
    unsigned cardinal;
    ContextAddress length;
    Symbol * base;
};

#define is_cardinal_type_pseudo_symbol(s) (s->sym_class == SYM_CLASS_TYPE && s->obj == NULL && s->base == NULL)

#include <services/symbols_alloc.h>

static Context * sym_ctx;
static int sym_frame;
static ContextAddress sym_ip;

static int get_sym_context(Context * ctx, int frame, ContextAddress addr) {
    if (frame == STACK_NO_FRAME) {
        ctx = context_get_group(ctx, CONTEXT_GROUP_PROCESS);
        sym_ip = addr;
    }
    else if (frame == STACK_TOP_FRAME) {
        if (!ctx->stopped) {
            errno = ERR_IS_RUNNING;
            return -1;
        }
        if (ctx->exited) {
            errno = ERR_ALREADY_EXITED;
            return -1;
        }
        sym_ip = get_regs_PC(ctx);
    }
    else {
        U8_T ip = 0;
        StackFrame * info = NULL;
        if (get_frame_info(ctx, frame, &info) < 0) return -1;
        if (read_reg_value(info, get_PC_definition(ctx), &ip) < 0) return -1;
        sym_ip = (ContextAddress)ip;
    }
    sym_ctx = ctx;
    sym_frame = frame;
    return 0;
}

/* Map ELF symbol table entry value to run-time address in given context address space */
static int syminfo2address(Context * ctx, ELF_SymbolInfo * info, ContextAddress * address) {
    switch (info->type) {
    case STT_OBJECT:
    case STT_FUNC:
        {
            U8_T value = info->value;
            ELF_File * file = info->sym_section->file;
            ELF_Section * sec = NULL;
            if (info->section_index == SHN_UNDEF) {
                errno = ERR_INV_ADDRESS;
                return -1;
            }
            if (info->section_index == SHN_ABS) {
                *address = (ContextAddress)value;
                return 0;
            }
            if (info->section_index == SHN_COMMON) {
                errno = ERR_INV_ADDRESS;
                return -1;
            }
            if (file->type == ET_REL && info->section != NULL) {
                sec = info->section;
                value += sec->addr;
            }
            *address = elf_map_to_run_time_address(ctx, file, sec, (ContextAddress)value);
            if (*address == 0 && file->type == ET_EXEC) *address = (ContextAddress)value;
            return 0;
        }
    }
    errno = ERR_INV_ADDRESS;
    return -1;
}

static int is_frame_based_object(Symbol * sym) {
    int res = 0;
    ContextAddress addr = 0;
    ContextAddress size = 0;
    Context * org_ctx = sym_ctx;
    int org_frame = sym_frame;
    ContextAddress org_ip = sym_ip;

    if (sym->sym_class == SYM_CLASS_REFERENCE) {
        if (get_symbol_address(sym, &addr) < 0) {
            res = 1;
        }
        else {
            sym->has_address = 1;
            sym->address = addr;
        }
    }

    if (!res) {
        if (get_symbol_size(sym, &size) < 0) {
            res = 1;
        }
        else {
            sym->has_size = 1;
            sym->size = size;
        }
    }

    sym_ctx = org_ctx;
    sym_frame = org_frame;
    sym_ip = org_ip;
    return res;
}

static void object2symbol(ObjectInfo * obj, Symbol ** res) {
    Symbol * sym = alloc_symbol();
    sym->obj = obj;
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
    case TAG_mod_pointer:
    case TAG_mod_reference:
    case TAG_string_type:
    case TAG_structure_type:
    case TAG_subroutine_type:
    case TAG_union_type:
    case TAG_ptr_to_member_type:
    case TAG_set_type:
    case TAG_subrange_type:
    case TAG_base_type:
    case TAG_fund_type:
    case TAG_file_type:
    case TAG_packed_type:
    case TAG_thrown_type:
    case TAG_const_type:
    case TAG_volatile_type:
    case TAG_restrict_type:
    case TAG_interface_type:
    case TAG_unspecified_type:
    case TAG_mutable_type:
    case TAG_shared_type:
    case TAG_typedef:
        sym->sym_class = SYM_CLASS_TYPE;
        break;
    case TAG_global_variable:
    case TAG_inheritance:
    case TAG_member:
    case TAG_formal_parameter:
    case TAG_unspecified_parameters:
    case TAG_local_variable:
    case TAG_variable:
        sym->sym_class = SYM_CLASS_REFERENCE;
        break;
    case TAG_constant:
    case TAG_enumerator:
        sym->sym_class = SYM_CLASS_VALUE;
        break;
    }
    sym->frame = STACK_NO_FRAME;
    sym->ctx = context_get_group(sym_ctx, CONTEXT_GROUP_PROCESS);
    if (sym_frame != STACK_NO_FRAME && is_frame_based_object(sym)) {
        sym->frame = sym_frame;
        sym->ctx = sym_ctx;
    }
    *res = sym;
}

static ObjectInfo * get_object_type(ObjectInfo * obj) {
    if (obj != NULL) {
        switch (obj->mTag) {
        case TAG_global_subroutine:
        case TAG_subroutine:
        case TAG_subprogram:
        case TAG_entry_point:
        case TAG_enumerator:
        case TAG_formal_parameter:
        case TAG_unspecified_parameters:
        case TAG_global_variable:
        case TAG_local_variable:
        case TAG_variable:
        case TAG_inheritance:
        case TAG_member:
        case TAG_constant:
            obj = obj->mType;
            break;
        }
    }
    return obj;
}

static int is_modified_type(ObjectInfo * obj) {
    if (obj != NULL && obj->mType != NULL) {
        switch (obj->mTag) {
        case TAG_subrange_type:
        case TAG_packed_type:
        case TAG_const_type:
        case TAG_volatile_type:
        case TAG_restrict_type:
        case TAG_shared_type:
        case TAG_typedef:
            return 1;
        }
    }
    return 0;
}

static ObjectInfo * get_original_type(ObjectInfo * obj) {
    obj = get_object_type(obj);
    while (is_modified_type(obj)) {
        obj = obj->mType;
    }
    return obj;
}

static int get_num_prop(ObjectInfo * obj, int at, U8_T * res) {
    Trap trap;
    PropertyValue v;

    if (!set_trap(&trap)) return 0;
    read_and_evaluate_dwarf_object_property(sym_ctx, sym_frame, 0, obj, at, &v);
    *res = get_numeric_property_value(&v);
    clear_trap(&trap);
    return 1;
}

/* Check run-time 'addr' belongs to an object address range(s) */
static int check_in_range(ObjectInfo * obj, ContextAddress rt_offs, ContextAddress addr) {
    Trap trap;

    if (obj->u.mAddr.mHighPC > obj->u.mAddr.mLowPC) {
        ContextAddress lt_addr = addr - rt_offs;
        return lt_addr >= obj->u.mAddr.mLowPC && lt_addr < obj->u.mAddr.mHighPC;
    }

    if (set_trap(&trap)) {
        CompUnit * unit = obj->mCompUnit;
        DWARFCache * cache = get_dwarf_cache(unit->mFile);
        ELF_Section * debug_ranges = cache->mDebugRanges;
        if (debug_ranges != NULL) {
            ContextAddress lt_addr = addr - rt_offs;
            ContextAddress base = unit->mLowPC;
            PropertyValue v;
            U8_T offs = 0;
            int res = 0;

            read_and_evaluate_dwarf_object_property(sym_ctx, sym_frame, 0, obj, AT_ranges, &v);
            offs = get_numeric_property_value(&v);
            dio_EnterSection(&unit->mDesc, debug_ranges, offs);
            for (;;) {
                ELF_Section * sec = NULL;
                U8_T x = dio_ReadAddress(&sec);
                U8_T y = dio_ReadAddress(&sec);
                if (x == 0 && y == 0) break;
                if (x == ((U8_T)1 << unit->mDesc.mAddressSize * 8) - 1) {
                    base = (ContextAddress)y;
                }
                else {
                    x = base + x;
                    y = base + y;
                    if (x <= lt_addr && lt_addr < y) {
                        res = 1;
                        break;
                    }
                }
            }
            dio_ExitSection();
            clear_trap(&trap);
            return res;
        }
        clear_trap(&trap);
    }
    return 0;
}

static int find_in_object_tree(ObjectInfo * list, ContextAddress rt_offs, ContextAddress ip, const char * name, Symbol ** sym) {
    Symbol * sym_imp = NULL;  /* Imported from a namespace */
    Symbol * sym_enu = NULL;  /* Enumeration constant */
    Symbol * sym_cur = NULL;  /* Found in current scope */
    Symbol * sym_base = NULL; /* Found in base class (inherited) */
    Symbol * sym_this = NULL; /* Found in 'this' reference */
    ObjectInfo * obj = list;
    while (obj != NULL) {
        if (obj->mName != NULL) {
            U8_T v = 0;
            if (strcmp(obj->mName, name) == 0) {
                object2symbol(obj, &sym_cur);
            }
            if (sym_frame != STACK_NO_FRAME && strcmp(obj->mName, "this") == 0 && get_num_prop(obj, AT_artificial, &v) && v != 0) {
                ObjectInfo * type = get_original_type(obj);
                if ((type->mTag == TAG_pointer_type || type->mTag == TAG_mod_pointer) && type->mType != NULL) {
                    type = get_original_type(type->mType);
                    find_in_object_tree(type->mChildren, 0, 0, name, &sym_this);
                    if (sym_this != NULL) {
                        sym_this->ctx = sym_ctx;
                        sym_this->frame = sym_frame;
                        sym_this->var = obj;
                    }
                }
            }
        }
        switch (obj->mTag) {
        case TAG_enumeration_type:
            find_in_object_tree(obj->mChildren, 0, 0, name, &sym_enu);
            break;
        case TAG_global_subroutine:
        case TAG_subroutine:
        case TAG_subprogram:
        case TAG_entry_point:
        case TAG_lexical_block:
        case TAG_inlined_subroutine:
            if (ip != 0 && check_in_range(obj, rt_offs, ip)) {
                if (find_in_object_tree(obj->mChildren, rt_offs, ip, name, sym)) return 1;
            }
            break;
        case TAG_inheritance:
            find_in_object_tree(obj->mType->mChildren, 0, 0, name, &sym_base);
            break;
        case TAG_imported_module:
            {
                PropertyValue p;
                ObjectInfo * module;
                read_and_evaluate_dwarf_object_property(sym_ctx, sym_frame, 0, obj, AT_import, &p);
                module = find_object(get_dwarf_cache(obj->mCompUnit->mFile), p.mValue);
                if (module != NULL) find_in_object_tree(module->mChildren, 0, 0, name, &sym_imp);
            }
            break;
        }
        obj = obj->mSibling;
    }
    if (*sym == NULL) *sym = sym_cur;
    if (*sym == NULL) *sym = sym_base;
    if (*sym == NULL) *sym = sym_this;
    if (*sym == NULL) *sym = sym_enu;
    if (*sym == NULL) *sym = sym_imp;
    return *sym != NULL;
}

static int find_in_dwarf(const char * name, Symbol ** sym) {
    ContextAddress rt_addr = 0;
    UnitAddressRange * range = elf_find_unit(sym_ctx, sym_ip, sym_ip, &rt_addr);
    *sym = NULL;
    if (range != NULL) {
        CompUnit * unit = range->mUnit;
        if (find_in_object_tree(unit->mObject->mChildren, rt_addr - range->mAddr, sym_ip, name, sym)) return 1;
        if (unit->mBaseTypes != NULL) {
            if (find_in_object_tree(unit->mBaseTypes->mObject->mChildren, 0, 0, name, sym)) return 1;
        }
    }
    return 0;
}

static int find_by_name_in_pub_names(DWARFCache * cache, PubNamesTable * tbl, char * name, Symbol ** sym) {
    unsigned n = tbl->mHash[calc_symbol_name_hash(name)];
    while (n != 0) {
        U8_T id = tbl->mNext[n].mID;
        ObjectInfo * obj = find_object(cache, id);
        if (obj == NULL || obj->mName == NULL) str_exception(ERR_INV_DWARF, "Invalid .debug_pubnames section");
        if (strcmp(obj->mName, name) == 0) {
            object2symbol(obj, sym);
            return 1;
        }
        n = tbl->mNext[n].mNext;
    }
    return 0;
}

static void create_symbol_names_hash(ELF_Section * tbl) {
    unsigned i;
    unsigned sym_size = tbl->file->elf64 ? sizeof(Elf64_Sym) : sizeof(Elf32_Sym);
    unsigned sym_cnt = (unsigned)(tbl->size / sym_size);
    tbl->sym_names_hash = (unsigned *)loc_alloc_zero(SYM_HASH_SIZE * sizeof(unsigned));
    tbl->sym_names_next = (unsigned *)loc_alloc_zero(sym_cnt * sizeof(unsigned));
    for (i = 0; i < sym_cnt; i++) {
        ELF_SymbolInfo sym;
        unpack_elf_symbol_info(tbl, i, &sym);
        if (sym.bind == STB_GLOBAL && sym.name != NULL && sym.section_index != SHN_UNDEF) {
            unsigned h = calc_symbol_name_hash(sym.name);
            tbl->sym_names_next[i] = tbl->sym_names_hash[h];
            tbl->sym_names_hash[h] = i;
        }
    }
}

static int find_by_name_in_sym_table(DWARFCache * cache, char * name, Symbol ** res) {
    unsigned m = 0;
    unsigned h = calc_symbol_name_hash(name);
    unsigned cnt = 0;
    Context * prs = context_get_group(sym_ctx, CONTEXT_GROUP_PROCESS);
    for (m = 1; m < cache->mFile->section_cnt; m++) {
        unsigned n;
        ELF_Section * tbl = cache->mFile->sections + m;
        if (tbl->sym_count == 0) continue;
        if (tbl->sym_names_hash == NULL) create_symbol_names_hash(tbl);
        n = tbl->sym_names_hash[h];
        while (n) {
            ELF_SymbolInfo sym_info;
            unpack_elf_symbol_info(tbl, n, &sym_info);
            if (cmp_symbol_names(name, sym_info.name) == 0) {
                int found = 0;
                ContextAddress addr = 0;
                if (sym_info.section_index != SHN_ABS && syminfo2address(prs, &sym_info, &addr) == 0) {
                    UnitAddressRange * range = elf_find_unit(sym_ctx, addr, addr, NULL);
                    if (range != NULL) {
                        ObjectInfo * obj = range->mUnit->mObject->mChildren;
                        while (obj != NULL) {
                            switch (obj->mTag) {
                            case TAG_global_subroutine:
                            case TAG_global_variable:
                            case TAG_subroutine:
                            case TAG_subprogram:
                            case TAG_variable:
                                if (obj->mName != NULL && strcmp(obj->mName, name) == 0) {
                                    object2symbol(obj, res);
                                    found = 1;
                                    cnt++;
                                }
                                break;
                            }
                            obj = obj->mSibling;
                        }
                    }
                }
                if (!found) {
                    Symbol * sym = alloc_symbol();
                    sym->frame = STACK_NO_FRAME;
                    sym->ctx = prs;
                    sym->tbl = tbl;
                    sym->index = n;
                    switch (sym_info.type) {
                    case STT_FUNC:
                        sym->sym_class = SYM_CLASS_FUNCTION;
                        break;
                    case STT_OBJECT:
                        sym->sym_class = SYM_CLASS_REFERENCE;
                        break;
                    default:
                        sym->sym_class = SYM_CLASS_VALUE;
                        break;
                    }
                    *res = sym;
                    cnt++;
                }
            }
            n = tbl->sym_names_next[n];
        }
    }
    return cnt == 1;
}

int find_symbol_by_name(Context * ctx, int frame, ContextAddress ip, char * name, Symbol ** res) {
    int error = 0;
    int found = 0;

    assert(ctx != NULL);

#if defined(_WRS_KERNEL)
    {
        char * ptr;
        SYM_TYPE type;

        if (symFindByName(sysSymTbl, name, &ptr, &type) != OK) {
            error = errno;
            assert(error != 0);
            if (error == S_symLib_SYMBOL_NOT_FOUND) error = 0;
        }
        else {
            Symbol * sym = alloc_symbol();
            sym->ctx = context_get_group(ctx, CONTEXT_GROUP_PROCESS);
            sym->frame = STACK_NO_FRAME;
            sym->address = (ContextAddress)ptr;
            sym->has_address = 1;

            if (SYM_IS_TEXT(type)) {
                sym->sym_class = SYM_CLASS_FUNCTION;
            }
            else {
                sym->sym_class = SYM_CLASS_REFERENCE;
            }
            *res = sym;
            found = 1;
        }
    }
#endif

    if (error == 0 && !found && get_sym_context(ctx, frame, ip) < 0) error = errno;

    if (error == 0 && !found && sym_ip != 0) {
        Trap trap;
        if (set_trap(&trap)) {
            found = find_in_dwarf(name, res);
            clear_trap(&trap);
        }
        else {
            error = trap.error;
        }
    }

    if (error == 0 && !found) {
        ELF_File * file = elf_list_first(sym_ctx, 0, ~(ContextAddress)0);
        if (file == NULL) error = errno;
        while (error == 0 && file != NULL) {
            Trap trap;
            if (set_trap(&trap)) {
                DWARFCache * cache = get_dwarf_cache(file);
                if (cache->mPubNames.mHash != NULL) {
                    found = find_by_name_in_pub_names(cache, &cache->mPubNames, name, res);
                    if (!found && cache->mPubTypes.mHash != NULL) {
                        found = find_by_name_in_pub_names(cache, &cache->mPubTypes, name, res);
                    }
                }
                if (!found) {
                    found = find_by_name_in_sym_table(cache, name, res);
                }
                clear_trap(&trap);
            }
            else {
                error = trap.error;
                break;
            }
            if (found) break;
            file = elf_list_next(sym_ctx);
            if (file == NULL) error = errno;
        }
        elf_list_done(sym_ctx);
    }

    if (error == 0 && !found && sym_ip != 0) {
        Trap trap;
        if (set_trap(&trap)) {
            const char * s = NULL;
            if (strcmp(name, "signed") == 0) s = "int";
            else if (strcmp(name, "signed int") == 0) s = "int";
            else if (strcmp(name, "unsigned") == 0) s = "unsigned int";
            else if (strcmp(name, "short") == 0) s = "short int";
            else if (strcmp(name, "signed short") == 0) s = "short int";
            else if (strcmp(name, "signed short int") == 0) s = "short int";
            else if (strcmp(name, "unsigned short") == 0) s = "unsigned short int";
            else if (strcmp(name, "long") == 0) s = "long int";
            else if (strcmp(name, "signed long") == 0) s = "long int";
            else if (strcmp(name, "signed long int") == 0) s = "long int";
            else if (strcmp(name, "unsigned long") == 0) s = "unsigned long int";
            else if (strcmp(name, "long long") == 0) s = "long long int";
            else if (strcmp(name, "signed long long") == 0) s = "long long int";
            else if (strcmp(name, "signed long long int") == 0) s = "long long int";
            else if (strcmp(name, "unsigned long long") == 0) s = "unsigned long long int";
            else if (strcmp(name, "char") == 0) s = "signed char";
            if (s != NULL) {
                found = find_in_dwarf(s, res);
                if (!found) {
                    s = NULL;
                    if (strcmp(name, "char") == 0) s = "unsigned char";
                    if (s != NULL) found = find_in_dwarf(s, res);
                }
            }
            clear_trap(&trap);
        }
        else {
            error = trap.error;
        }
    }

#if ENABLE_RCBP_TEST
    if (!found) {
        int sym_class = 0;
        void * address = NULL;
        found = find_test_symbol(ctx, name, &address, &sym_class) >= 0;
        if (found) {
            Symbol * sym = alloc_symbol();
            sym->ctx = context_get_group(ctx, CONTEXT_GROUP_PROCESS);
            sym->frame = STACK_NO_FRAME;
            sym->address = (ContextAddress)address;
            sym->has_address = 1;
            sym->sym_class = sym_class;
            *res = sym;
        }
    }
#endif

    if (error == 0 && !found) error = ERR_SYM_NOT_FOUND;

    assert(error || (*res != NULL && (*res)->ctx != NULL));

    if (error) {
        errno = error;
        return -1;
    }
    return 0;
}

int find_symbol_in_scope(Context * ctx, int frame, ContextAddress ip, Symbol * scope, char * name, Symbol ** res) {
    int error = 0;
    int found = 0;

    if (get_sym_context(ctx, frame, ip) < 0) error = errno;

    if (!error && scope == NULL && sym_ip != 0) {
        ELF_File * file = elf_list_first(sym_ctx, sym_ip, sym_ip);
        if (file == NULL) error = errno;
        while (error == 0 && file != NULL) {
            Trap trap;
            if (set_trap(&trap)) {
                DWARFCache * cache = get_dwarf_cache(file);
                UnitAddressRange * range = find_comp_unit_addr_range(cache, sym_ip, sym_ip);
                if (range != NULL) {
                    found = find_in_object_tree(range->mUnit->mObject->mChildren, 0, 0, name, res);
                }
                if (!found) {
                    found = find_by_name_in_sym_table(cache, name, res);
                }
                clear_trap(&trap);
            }
            else {
                error = trap.error;
                break;
            }
            if (found) break;
            file = elf_list_next(sym_ctx);
            if (file == NULL) error = errno;
        }
        elf_list_done(sym_ctx);
    }

    if (!found && !error && scope != NULL && scope->obj != NULL) {
        Trap trap;
        if (set_trap(&trap)) {
            found = find_in_object_tree(scope->obj->mChildren, 0, 0, name, res);
            clear_trap(&trap);
        }
        else {
            error = trap.error;
        }
    }

    if (error == 0 && !found) error = ERR_SYM_NOT_FOUND;

    assert(error || (*res != NULL && (*res)->ctx != NULL));

    if (error) {
        errno = error;
        return -1;
    }
    return 0;
}

static int find_by_addr_in_unit(ObjectInfo * obj, int level, ContextAddress rt_offs, ContextAddress addr, Symbol ** res) {
    while (obj != NULL) {
        switch (obj->mTag) {
        case TAG_global_subroutine:
        case TAG_subroutine:
        case TAG_subprogram:
        case TAG_entry_point:
        case TAG_lexical_block:
        case TAG_inlined_subroutine:
            if (check_in_range(obj, rt_offs, addr)) {
                object2symbol(obj, res);
                return 1;
            }
            if (check_in_range(obj, rt_offs, sym_ip)) {
                return find_by_addr_in_unit(obj->mChildren, level + 1, rt_offs, addr, res);
            }
            break;
        case TAG_formal_parameter:
        case TAG_unspecified_parameters:
        case TAG_local_variable:
            if (sym_frame == STACK_NO_FRAME) break;
        case TAG_variable:
            {
                U8_T lc = 0;
                /* Ignore location evaluation errors. For example, the error can be caused by
                 * the object not being mapped into the context memory */
                if (get_num_prop(obj, AT_location, &lc) && lc <= addr) {
                    U8_T sz = 0;
                    if (!get_num_prop(obj, AT_byte_size, &sz)) {
                        /* If object size unknown, continue search */
                        if (get_error_code(errno) == ERR_SYM_NOT_FOUND) break;
                        exception(errno);
                    }
                    if (lc + sz > addr) {
                        object2symbol(obj, res);
                        return 1;
                    }
                }
            }
            break;
        }
        obj = obj->mSibling;
    }
    return 0;
}

static int find_by_addr_in_sym_tables(ContextAddress addr, Symbol ** res) {
    ELF_File * file = NULL;
    ELF_Section * section = NULL;
    ELF_SymbolInfo sym_info;
    ContextAddress lt_addr = elf_map_to_link_time_address(sym_ctx, addr, &file, &section);
    elf_find_symbol_by_address(section, lt_addr, &sym_info);
    while (sym_info.sym_section != NULL) {
        int sym_class = SYM_CLASS_UNKNOWN;
        assert(sym_info.section == section);
        switch (sym_info.type) {
        case STT_FUNC:
            sym_class = SYM_CLASS_FUNCTION;
            break;
        case STT_OBJECT:
            sym_class = SYM_CLASS_REFERENCE;
            break;
        }
        if (sym_class != SYM_CLASS_UNKNOWN) {
            ContextAddress sym_addr = sym_info.value;
            if (file->type == ET_REL) sym_addr += section->addr;
            assert(sym_addr <= lt_addr);
            if (sym_addr + sym_info.size > lt_addr) {
                Symbol * sym = alloc_symbol();
                sym->frame = STACK_NO_FRAME;
                sym->ctx = context_get_group(sym_ctx, CONTEXT_GROUP_PROCESS);
                sym->tbl = sym_info.sym_section;
                sym->index = sym_info.sym_index;
                sym->sym_class = sym_class;
                *res = sym;
                return 1;
            }
            return 0;
        }
        elf_prev_symbol_by_address(&sym_info);
    }
    return 0;
}

int find_symbol_by_addr(Context * ctx, int frame, ContextAddress addr, Symbol ** res) {
    Trap trap;
    int found = 0;
    ContextAddress rt_addr = 0;
    UnitAddressRange * range = NULL;
    if (!set_trap(&trap)) return -1;
    if (frame == STACK_TOP_FRAME && (frame = get_top_frame(ctx)) < 0) exception(errno);
    if (get_sym_context(ctx, frame, addr) < 0) exception(errno);
    range = elf_find_unit(sym_ctx, addr, addr, &rt_addr);
    if (range != NULL) found = find_by_addr_in_unit(range->mUnit->mObject->mChildren,
        0, rt_addr - range->mAddr, addr, res);
    if (!found) found = find_by_addr_in_sym_tables(addr, res);
    if (!found && sym_ip != 0) {
        /* Search in compilation unit that contains stack frame PC */
        range = elf_find_unit(sym_ctx, sym_ip, sym_ip, &rt_addr);
        if (range != NULL) found = find_by_addr_in_unit(range->mUnit->mObject->mChildren,
            0, rt_addr - range->mAddr, addr, res);
    }
    if (!found) exception(ERR_SYM_NOT_FOUND);
    clear_trap(&trap);
    return 0;
}

static void enumerate_local_vars(ObjectInfo * obj, int level, ContextAddress rt_offs,
                                 EnumerateSymbolsCallBack * call_back, void * args) {
    while (obj != NULL) {
        switch (obj->mTag) {
        case TAG_global_subroutine:
        case TAG_subroutine:
        case TAG_subprogram:
        case TAG_entry_point:
        case TAG_lexical_block:
        case TAG_inlined_subroutine:
            if (check_in_range(obj, rt_offs, sym_ip)) {
                enumerate_local_vars(obj->mChildren, level + 1, rt_offs, call_back, args);
            }
            break;
        case TAG_formal_parameter:
        case TAG_unspecified_parameters:
        case TAG_local_variable:
        case TAG_variable:
            if (level > 0) {
                Context * org_ctx = sym_ctx;
                int org_frame = sym_frame;
                ContextAddress org_ip = sym_ip;
                Symbol * sym = NULL;
                object2symbol(obj, &sym);
                call_back(args, sym);
                sym_ctx = org_ctx;
                sym_frame = org_frame;
                sym_ip = org_ip;
            }
            break;
        }
        obj = obj->mSibling;
    }
}

int enumerate_symbols(Context * ctx, int frame, EnumerateSymbolsCallBack * call_back, void * args) {
    Trap trap;
    if (!set_trap(&trap)) return -1;
    if (frame == STACK_TOP_FRAME && (frame = get_top_frame(ctx)) < 0) exception(errno);
    if (get_sym_context(ctx, frame, 0) < 0) exception(errno);
    if (sym_ip != 0) {
        ContextAddress rt_addr = 0;
        UnitAddressRange * range = elf_find_unit(sym_ctx, sym_ip, sym_ip, &rt_addr);
        if (range != NULL) enumerate_local_vars(range->mUnit->mObject->mChildren,
            0, rt_addr - range->mAddr, call_back, args);
    }
    clear_trap(&trap);
    return 0;
}

const char * symbol2id(const Symbol * sym) {
    static char id[256];

    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base) {
        char base[256];
        assert(sym->ctx == sym->base->ctx);
        assert(sym->frame == STACK_NO_FRAME);
        assert(sym->sym_class == SYM_CLASS_TYPE);
        strcpy(base, symbol2id(sym->base));
        snprintf(id, sizeof(id), "@P%"PRIX64".%s", (uint64_t)sym->length, base);
    }
    else {
        ELF_File * file = NULL;
        uint64_t obj_index = 0;
        uint64_t var_index = 0;
        unsigned tbl_index = 0;
        int frame = sym->frame;
        if (sym->obj != NULL) file = sym->obj->mCompUnit->mFile;
        if (sym->tbl != NULL) file = sym->tbl->file;
        if (sym->obj != NULL) obj_index = sym->obj->mID;
        if (sym->var != NULL) var_index = sym->var->mID;
        if (sym->tbl != NULL) tbl_index = sym->tbl->index;
        if (frame == STACK_TOP_FRAME) frame = get_top_frame(sym->ctx);
        assert(sym->var == NULL || sym->var->mCompUnit->mFile == file);
        snprintf(id, sizeof(id), "@S%X.%lX.%lX.%"PRIX64".%"PRIX64".%"PRIX64".%X.%d.%X.%X.%X.%s",
            sym->sym_class,
            file ? (unsigned long)file->dev : 0ul,
            file ? (unsigned long)file->ino : 0ul,
            file ? file->mtime : (int64_t)0,
            obj_index, var_index, tbl_index,
            frame, sym->index,
            sym->dimension, sym->cardinal,
            sym->ctx->id);
    }
    return id;
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

static int read_int(const char ** s) {
    int neg = 0;
    int res = 0;
    const char * p = *s;
    if (*p == '-') {
        neg = 1;
        p++;
    }
    for (;;) {
        if (*p >= '0' && *p <= '9') res = res * 10 + (*p - '0');
        else break;
        p++;
    }
    *s = p;
    return neg ? -res : res;
}

int id2symbol(const char * id, Symbol ** res) {
    Symbol * sym = alloc_symbol();
    dev_t dev = 0;
    ino_t ino = 0;
    int64_t mtime;
    uint64_t obj_index = 0;
    uint64_t var_index = 0;
    unsigned tbl_index = 0;
    ELF_File * file = NULL;
    const char * p;
    Trap trap;

    *res = sym;
    if (id != NULL && id[0] == '@' && id[1] == 'P') {
        p = id + 2;
        sym->length = (ContextAddress)read_hex(&p);
        if (*p == '.') p++;
        if (id2symbol(p, &sym->base)) return -1;
        sym->ctx = sym->base->ctx;
        sym->frame = STACK_NO_FRAME;
        sym->sym_class = SYM_CLASS_TYPE;
        return 0;
    }
    else if (id != NULL && id[0] == '@' && id[1] == 'S') {
        p = id + 2;
        sym->sym_class = (int)read_hex(&p);
        if (*p == '.') p++;
        dev = (dev_t)read_hex(&p);
        if (*p == '.') p++;
        ino = (ino_t)read_hex(&p);
        if (*p == '.') p++;
        mtime = (int64_t)read_hex(&p);
        if (*p == '.') p++;
        obj_index = read_hex(&p);
        if (*p == '.') p++;
        var_index = read_hex(&p);
        if (*p == '.') p++;
        tbl_index = (unsigned)read_hex(&p);
        if (*p == '.') p++;
        sym->frame = read_int(&p);
        if (*p == '.') p++;
        sym->index = (unsigned)read_hex(&p);
        if (*p == '.') p++;
        sym->dimension = (unsigned)read_hex(&p);
        if (*p == '.') p++;
        sym->cardinal = (unsigned)read_hex(&p);
        if (*p == '.') p++;
        sym->ctx = id2ctx(p);
        if (sym->ctx == NULL) {
            errno = ERR_INV_CONTEXT;
            return -1;
        }
        if (dev == 0 && ino == 0 && mtime == 0) return 0;
        file = elf_open_inode(sym->ctx, dev, ino, mtime);
        if (file == NULL) return -1;
        if (set_trap(&trap)) {
            DWARFCache * cache = get_dwarf_cache(file);
            if (obj_index) {
                sym->obj = find_object(cache, obj_index);
                if (sym->obj == NULL) exception(ERR_INV_CONTEXT);
            }
            if (var_index) {
                sym->var = find_object(cache, var_index);
                if (sym->var == NULL) exception(ERR_INV_CONTEXT);
            }
            if (tbl_index) {
                if (tbl_index >= file->section_cnt) exception(ERR_INV_CONTEXT);
                sym->tbl = file->sections + tbl_index;
            }
            clear_trap(&trap);
            return 0;
        }
    }
    else {
        errno = ERR_INV_CONTEXT;
    }
    return -1;
}

ContextAddress is_plt_section(Context * ctx, ContextAddress addr) {
    ELF_File * file = NULL;
    ELF_Section * sec = NULL;
    ContextAddress res = elf_map_to_link_time_address(ctx, addr, &file, &sec);
    if (res == 0 || sec == NULL) return 0;
    if (sec->name == NULL) return 0;
    if (strcmp(sec->name, ".plt") != 0) return 0;
    return sec->addr + (addr - res);
}

int get_stack_tracing_info(Context * ctx, ContextAddress rt_addr, StackTracingInfo ** info) {
    /* TODO: no debug info exists for linux-gate.so, need to read stack tracing information from the kernel  */
    /* TODO: support for separate debug info files */
    ELF_File * file = NULL;
    ELF_Section * sec = NULL;
    ContextAddress lt_addr = 0;
    int error = 0;
    Trap trap;

    *info = NULL;

    lt_addr = elf_map_to_link_time_address(ctx, rt_addr, &file, &sec);
    if (file != NULL) {
        assert(rt_addr == elf_map_to_run_time_address(ctx, file, sec, lt_addr));
        if (set_trap(&trap)) {
            get_dwarf_stack_frame_info(ctx, file, sec, lt_addr);
            if (dwarf_stack_trace_fp->cmds_cnt > 0) {
                static StackTracingInfo buf;
                buf.addr = (ContextAddress)dwarf_stack_trace_addr - lt_addr + rt_addr;
                buf.size = (ContextAddress)dwarf_stack_trace_size;
                buf.fp = dwarf_stack_trace_fp;
                buf.regs = dwarf_stack_trace_regs;
                buf.reg_cnt = dwarf_stack_trace_regs_cnt;
                *info = &buf;
            }
            clear_trap(&trap);
        }
        else {
            error = trap.error;
        }
    }

    if (error) {
        errno = error;
        return -1;
    }
    return 0;
}

int get_next_stack_frame(StackFrame * frame, StackFrame * down) {
    int error = 0;
    uint64_t ip = 0;
    Context * ctx = frame->ctx;
    StackTracingInfo * info = NULL;

    if (read_reg_value(frame, get_PC_definition(ctx), &ip) < 0) {
        if (frame->is_top_frame) error = errno;
    }
    else if (get_stack_tracing_info(ctx, ip, &info) < 0) {
        error = errno;
    }
    else if (info != NULL) {
        Trap trap;
        if (set_trap(&trap)) {
            int i;
            frame->fp = (ContextAddress)evaluate_stack_trace_commands(ctx, frame, info->fp);
            for (i = 0; i < info->reg_cnt; i++) {
                uint64_t v = evaluate_stack_trace_commands(ctx, frame, info->regs[i]);
                if (write_reg_value(down, info->regs[i]->reg, v) < 0) exception(errno);
            }
            clear_trap(&trap);
        }
        else {
            frame->fp = 0;
        }
    }
    if (error) {
        errno = error;
        return -1;
    }
    return 0;
}

void ini_symbols_lib(void) {
}

/*************** Functions for retrieving symbol properties ***************************************/

static int unpack(const Symbol * sym) {
    ELF_File * file = NULL;
    assert(sym->base == NULL);
    assert(!is_cardinal_type_pseudo_symbol(sym));
    if (get_sym_context(sym->ctx, sym->frame, 0) < 0) return -1;
    if (sym->obj != NULL) file = sym->obj->mCompUnit->mFile;
    if (sym->tbl != NULL) file = sym->tbl->file;
    if (file != NULL) {
        DWARFCache * cache = (DWARFCache *)file->dwarf_dt_cache;
        if (cache == NULL || cache->magic != DWARF_CACHE_MAGIC) {
            errno = ERR_INV_CONTEXT;
            return -1;
        }
    }
    return 0;
}

static U8_T get_default_lower_bound(ObjectInfo * obj) {
    switch (obj->mCompUnit->mLanguage) {
    case LANG_FORTRAN77:
    case LANG_FORTRAN90:
    case LANG_FORTRAN95:
        return 1;
    }
    return 0;
}

static U8_T get_object_length(ObjectInfo * obj) {
    U8_T x, y;

    if (get_num_prop(obj, AT_count, &x)) return x;
    if (get_num_prop(obj, AT_upper_bound, &x)) {
        if (!get_num_prop(obj, AT_lower_bound, &y)) {
            y = get_default_lower_bound(obj);
        }
        return x + 1 - y;
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

static void alloc_cardinal_type_pseudo_symbol(Context * ctx, unsigned size, Symbol ** type) {
    *type = alloc_symbol();
    (*type)->ctx = context_get_group(ctx, CONTEXT_GROUP_PROCESS);
    (*type)->frame = STACK_NO_FRAME;
    (*type)->sym_class = SYM_CLASS_TYPE;
    (*type)->cardinal = size;
}

static int map_to_sym_table(ObjectInfo * obj, Symbol ** sym) {
    U8_T v = 0;
    int found = 0;
    if (get_num_prop(obj, AT_external, &v) && v != 0) {
        Trap trap;
        if (set_trap(&trap)) {
            PropertyValue p;
            DWARFCache * cache = get_dwarf_cache(obj->mCompUnit->mFile);
            read_and_evaluate_dwarf_object_property(sym_ctx, sym_frame, 0, obj, AT_MIPS_linkage_name, &p);
            if (p.mAddr != NULL) found = find_by_name_in_sym_table(cache, (char *)p.mAddr, sym);
            clear_trap(&trap);
        }
    }
    return found;
}

int get_symbol_class(const Symbol * sym, int * sym_class) {
    assert(sym->magic == SYMBOL_MAGIC);
    *sym_class = sym->sym_class;
    return 0;
}

int get_symbol_type(const Symbol * sym, Symbol ** type) {
    ObjectInfo * obj = sym->obj;
    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base || is_cardinal_type_pseudo_symbol(sym)) {
        *type = (Symbol *)sym;
        return 0;
    }
    if (sym->sym_class == SYM_CLASS_FUNCTION) {
        *type = alloc_symbol();
        (*type)->ctx = sym->ctx;
        (*type)->frame = STACK_NO_FRAME;
        (*type)->sym_class = SYM_CLASS_TYPE;
        (*type)->base = (Symbol *)sym;
        return 0;
    }
    if (unpack(sym) < 0) return -1;
    obj = sym->sym_class == SYM_CLASS_TYPE ?
        get_original_type(obj) : get_object_type(obj);
    if (obj == NULL) {
        *type = NULL;
    }
    else if (obj == sym->obj) {
        *type = (Symbol *)sym;
    }
    else {
        object2symbol(obj, type);
    }
    return 0;
}

int get_symbol_type_class(const Symbol * sym, int * type_class) {
    U8_T x;
    ObjectInfo * obj = sym->obj;
    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base) {
        if (sym->base->sym_class == SYM_CLASS_FUNCTION) *type_class = TYPE_CLASS_FUNCTION;
        else if (sym->length > 0) *type_class = TYPE_CLASS_ARRAY;
        else *type_class = TYPE_CLASS_POINTER;
        return 0;
    }
    if (is_cardinal_type_pseudo_symbol(sym)) {
        *type_class = TYPE_CLASS_CARDINAL;
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
        case TAG_mod_pointer:
        case TAG_mod_reference:
            *type_class = TYPE_CLASS_POINTER;
            return 0;
        case TAG_class_type:
        case TAG_structure_type:
        case TAG_union_type:
        case TAG_interface_type:
            *type_class = TYPE_CLASS_COMPOSITE;
            return 0;
        case TAG_base_type:
            if (get_num_prop(obj, AT_encoding, &x)) {
                switch ((int)x) {
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
            }
            *type_class = TYPE_CLASS_UNKNOWN;
            return 0;
        case TAG_fund_type:
            switch (obj->u.mFundType) {
            case FT_boolean:
                *type_class = TYPE_CLASS_INTEGER;
                return 0;
            case FT_char:
                *type_class = TYPE_CLASS_INTEGER;
                return 0;
            case FT_dbl_prec_float:
            case FT_ext_prec_float:
            case FT_float:
                *type_class = TYPE_CLASS_REAL;
                return 0;
            case FT_signed_char:
            case FT_signed_integer:
            case FT_signed_long:
            case FT_signed_short:
            case FT_short:
            case FT_integer:
            case FT_long:
                *type_class = TYPE_CLASS_INTEGER;
                return 0;
            case FT_unsigned_char:
            case FT_unsigned_integer:
            case FT_unsigned_long:
            case FT_unsigned_short:
                *type_class = TYPE_CLASS_CARDINAL;
                return 0;
            case FT_pointer:
                *type_class = TYPE_CLASS_POINTER;
                return 0;
            case FT_void:
                *type_class = TYPE_CLASS_CARDINAL;
                return 0;
            case FT_label:
            case FT_complex:
            case FT_dbl_prec_complex:
            case FT_ext_prec_complex:
                break;
            }
            *type_class = TYPE_CLASS_UNKNOWN;
            return 0;
        case TAG_subrange_type:
        case TAG_packed_type:
        case TAG_volatile_type:
        case TAG_restrict_type:
        case TAG_shared_type:
        case TAG_const_type:
        case TAG_typedef:
        case TAG_formal_parameter:
        case TAG_unspecified_parameters:
        case TAG_global_variable:
        case TAG_local_variable:
        case TAG_variable:
        case TAG_inheritance:
        case TAG_member:
        case TAG_constant:
            obj = obj->mType;
            break;
        default:
            obj = NULL;
            break;
        }
    }
    if (sym->tbl != NULL) {
        ELF_SymbolInfo info;
        unpack_elf_symbol_info(sym->tbl, sym->index, &info);
        if (info.type == STT_FUNC) {
            *type_class = TYPE_CLASS_FUNCTION;
            return 0;
        }
    }
    *type_class = TYPE_CLASS_UNKNOWN;
    return 0;
}

int get_symbol_update_policy(const Symbol * sym, char ** id, int * policy) {
    assert(sym->magic == SYMBOL_MAGIC);
    *id = sym->ctx->id;
    *policy = context_has_state(sym->ctx) ? UPDATE_ON_EXE_STATE_CHANGES : UPDATE_ON_MEMORY_MAP_CHANGES;
    return 0;
}

int get_symbol_name(const Symbol * sym, char ** name) {
    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base || is_cardinal_type_pseudo_symbol(sym)) {
        *name = NULL;
    }
    else if (sym->obj != NULL) {
        *name = sym->obj->mName;
    }
    else if (sym->tbl != NULL) {
        ELF_SymbolInfo info;
        unpack_elf_symbol_info(sym->tbl, sym->index, &info);
        *name = info.name;
    }
    else {
        *name = NULL;
    }
    return 0;
}

int get_symbol_size(const Symbol * sym, ContextAddress * size) {
    ObjectInfo * obj = sym->obj;
    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base) {
        if (sym->length > 0) {
            if (get_symbol_size(sym->base, size)) return -1;
            *size *= sym->length;
        }
        else {
            Symbol * base = sym->base;
            while (base->obj == NULL && base->base != NULL) base = base->base;
            if (base->obj != NULL) *size = base->obj->mCompUnit->mDesc.mAddressSize;
            else *size = context_word_size(sym->ctx);
        }
        return 0;
    }
    if (is_cardinal_type_pseudo_symbol(sym)) {
        *size = sym->cardinal;
        return 0;
    }
    if (sym->has_size != 0) {
        *size = sym->size;
        return 0;
    }
    if (unpack(sym) < 0) return -1;
    *size = 0;
    if (obj != NULL) {
        Trap trap;
        int ok = 0;
        U8_T sz = 0;

        if (!set_trap(&trap)) return -1;
        if (sym->dimension == 0) ok = get_num_prop(obj, AT_byte_size, &sz);
        if (!ok && sym->sym_class == SYM_CLASS_FUNCTION) {
            if (obj->u.mAddr.mHighPC > obj->u.mAddr.mLowPC) {
                ok = 1;
                sz = obj->u.mAddr.mHighPC - obj->u.mAddr.mLowPC;
            }
        }
        else if (!ok) {
            ObjectInfo * ref = NULL;
            if (sym->sym_class == SYM_CLASS_REFERENCE) {
                ref = obj;
                if (obj->mType != NULL) {
                    obj = obj->mType;
                    if (sym->dimension == 0) ok = get_num_prop(obj, AT_byte_size, &sz);
                }
            }
            while (!ok && obj->mType != NULL) {
                if (!is_modified_type(obj) && obj->mTag != TAG_enumeration_type) break;
                obj = obj->mType;
                if (sym->dimension == 0) ok = get_num_prop(obj, AT_byte_size, &sz);
            }
            if (!ok && obj->mTag == TAG_array_type) {
                unsigned i = 0;
                U8_T length = 1;
                ObjectInfo * elem_type = obj->mType;
                ObjectInfo * idx = obj->mChildren;
                while (idx != NULL) {
                    if (i++ >= sym->dimension) length *= get_object_length(idx);
                    idx = idx->mSibling;
                }
                ok = get_num_prop(obj, AT_stride_size, &sz);
                if (ok) {
                    sz = (sz * length + 7) / 8;
                }
                else {
                    if (elem_type == NULL) str_exception(ERR_OTHER, "Unknown array element type");
                    ok = get_num_prop(elem_type, AT_byte_size, &sz);
                    while (!ok && elem_type->mType != NULL) {
                        if (!is_modified_type(elem_type) && elem_type->mTag != TAG_enumeration_type) break;
                        elem_type = elem_type->mType;
                        ok = get_num_prop(elem_type, AT_byte_size, &sz);
                    }
                    if (ok) sz *= length;
                }
            }
            if (!ok && ref && ref->mTag != TAG_member && ref->mTag != TAG_inheritance) {
                Trap trap;
                if (set_trap(&trap)) {
                    PropertyValue v;
                    read_and_evaluate_dwarf_object_property(sym_ctx, sym_frame, 0, ref, AT_location, &v);
                    if (v.mRegister) {
                        sz = v.mRegister->size;
                        ok = 1;
                    }
                    clear_trap(&trap);
                }
            }
            if (!ok && ref != NULL) {
                Symbol * elf_sym = NULL;
                ContextAddress elf_sym_size = 0;
                if (map_to_sym_table(ref, &elf_sym) && get_symbol_size(elf_sym, &elf_sym_size) == 0) {
                    sz = elf_sym_size;
                    ok = 1;
                }
            }
        }
        if (!ok) str_exception(ERR_INV_DWARF, "Object has no size attribute");
        *size = (ContextAddress)sz;
        clear_trap(&trap);
    }
    else if (sym->tbl != NULL) {
        ELF_SymbolInfo info;
        unpack_elf_symbol_info(sym->tbl, sym->index, &info);
        switch (info.type) {
        case STT_OBJECT:
        case STT_FUNC:
            *size = (ContextAddress)info.size;
            break;
        default:
            *size = info.sym_section->file->elf64 ? 8 : 4;
            break;
        }
    }
    else {
        errno = set_errno(ERR_OTHER, "Debug info not available");
        return -1;
    }
    return 0;
}

int get_symbol_base_type(const Symbol * sym, Symbol ** base_type) {
    ObjectInfo * obj = sym->obj;
    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base) {
        if (sym->base->sym_class == SYM_CLASS_FUNCTION) {
            if (sym->base->obj != NULL && sym->base->obj->mType != NULL) {
                if (unpack(sym->base) < 0) return -1;
                object2symbol(sym->base->obj->mType, base_type);
            }
            else {
                /* Function return type is 'void' */
                alloc_cardinal_type_pseudo_symbol(sym->ctx, 0, base_type);
            }
            return 0;
        }
        *base_type = sym->base;
        return 0;
    }
    if (is_cardinal_type_pseudo_symbol(sym)) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (unpack(sym) < 0) return -1;
    obj = get_original_type(obj);
    if (obj != NULL) {
        if (obj->mTag == TAG_array_type) {
            int i = sym->dimension;
            ObjectInfo * idx = obj->mChildren;
            while (i > 0 && idx != NULL) {
                idx = idx->mSibling;
                i--;
            }
            if (idx != NULL && idx->mSibling != NULL) {
                *base_type = alloc_symbol();
                **base_type = *sym;
                (*base_type)->dimension++;
                return 0;
            }
        }
        if ((obj->mTag == TAG_pointer_type || obj->mTag == TAG_mod_pointer) && obj->mType == NULL) {
            /* pointer to void */
            alloc_cardinal_type_pseudo_symbol(sym->ctx, 0, base_type);
            return 0;
        }
        obj = obj->mType;
        if (obj != NULL) {
            object2symbol(obj, base_type);
            return 0;
        }
    }
    errno = ERR_UNSUPPORTED;
    return -1;
}

int get_symbol_index_type(const Symbol * sym, Symbol ** index_type) {
    ObjectInfo * obj = sym->obj;
    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base) {
        if (sym->base->sym_class == SYM_CLASS_FUNCTION) {
            errno = ERR_INV_CONTEXT;
            return -1;
        }
        alloc_cardinal_type_pseudo_symbol(sym->ctx, context_word_size(sym->ctx), index_type);
        return 0;
    }
    if (is_cardinal_type_pseudo_symbol(sym)) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (unpack(sym) < 0) return -1;
    obj = get_original_type(obj);
    if (obj != NULL && obj->mTag == TAG_array_type) {
        int i = sym->dimension;
        ObjectInfo * idx = obj->mChildren;
        while (i > 0 && idx != NULL) {
            idx = idx->mSibling;
            i--;
        }
        if (idx != NULL) {
            object2symbol(idx, index_type);
            return 0;
        }
    }
    errno = ERR_UNSUPPORTED;
    return -1;
}

int get_symbol_length(const Symbol * sym, ContextAddress * length) {
    ObjectInfo * obj = sym->obj;
    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base) {
        if (sym->base->sym_class == SYM_CLASS_FUNCTION) {
            errno = ERR_INV_CONTEXT;
            return -1;
        }
        *length = sym->length == 0 ? 1 : sym->length;
        return 0;
    }
    if (is_cardinal_type_pseudo_symbol(sym)) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (unpack(sym) < 0) return -1;
    obj = get_original_type(obj);
    if (obj != NULL && obj->mTag == TAG_array_type) {
        int i = sym->dimension;
        ObjectInfo * idx = obj->mChildren;
        while (i > 0 && idx != NULL) {
            idx = idx->mSibling;
            i--;
        }
        if (idx != NULL) {
            Trap trap;
            if (!set_trap(&trap)) return -1;
            *length = (ContextAddress)get_object_length(idx);
            clear_trap(&trap);
            return 0;
        }
    }
    errno = ERR_UNSUPPORTED;
    return -1;
}

int get_symbol_lower_bound(const Symbol * sym, int64_t * value) {
    ObjectInfo * obj = sym->obj;
    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base) {
        if (sym->base->sym_class == SYM_CLASS_FUNCTION) {
            errno = ERR_INV_CONTEXT;
            return -1;
        }
        *value = 0;
        return 0;
    }
    if (is_cardinal_type_pseudo_symbol(sym)) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (unpack(sym) < 0) return -1;
    obj = get_original_type(obj);
    if (obj != NULL && obj->mTag == TAG_array_type) {
        int i = sym->dimension;
        ObjectInfo * idx = obj->mChildren;
        while (i > 0 && idx != NULL) {
            idx = idx->mSibling;
            i--;
        }
        if (idx != NULL) {
            if (get_num_prop(obj, AT_lower_bound, (U8_T *)value)) return 0;
            if (get_error_code(errno) != ERR_SYM_NOT_FOUND) return -1;
            *value = get_default_lower_bound(obj);
            return 0;
        }
    }
    errno = ERR_UNSUPPORTED;
    return -1;
}

int get_symbol_children(const Symbol * sym, Symbol *** children, int * count) {
    int n = 0;
    static Symbol ** buf = NULL;
    static int buf_len = 0;
    ObjectInfo * obj = sym->obj;
    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base) {
        if (sym->base->sym_class == SYM_CLASS_FUNCTION && sym->base->obj == NULL) {
            *children = NULL;
            *count = 0;
            errno = ERR_SYM_NOT_FOUND;
            return -1;
        }
        if (sym->base->sym_class == SYM_CLASS_FUNCTION) {
            ObjectInfo * i = sym->base->obj->mChildren;
            if (unpack(sym->base) < 0) return -1;
            while (i != NULL) {
                if (i->mTag == TAG_formal_parameter || i->mTag == TAG_unspecified_parameters) n++;
                i = i->mSibling;
            }
            if (buf_len < n) {
                buf = (Symbol **)loc_realloc(buf, sizeof(Symbol *) * n);
                buf_len = n;
            }
            n = 0;
            i = obj->mChildren;
            while (i != NULL) {
                if (i->mTag == TAG_formal_parameter || i->mTag == TAG_unspecified_parameters) {
                    Symbol * x = NULL;
                    Symbol * y = NULL;
                    object2symbol(i, &x);
                    if (get_symbol_type(x, &y) < 0) return -1;
                    buf[n++] = y;
                }
                i = i->mSibling;
            }
            *children = buf;
            *count = n;
            return 0;
        }
        *children = NULL;
        *count = 0;
        return 0;
    }
    if (is_cardinal_type_pseudo_symbol(sym)) {
        *children = NULL;
        *count = 0;
        return 0;
    }
    if (unpack(sym) < 0) return -1;
    obj = get_original_type(obj);
    if (obj != NULL) {
        ObjectInfo * i = obj->mChildren;
        while (i != NULL) {
            i = i->mSibling;
            n++;
        }
        if (buf_len < n) {
            buf = (Symbol **)loc_realloc(buf, sizeof(Symbol *) * n);
            buf_len = n;
        }
        n = 0;
        i = obj->mChildren;
        while (i != NULL) {
            Symbol * x = NULL;
            object2symbol(i, &x);
            buf[n++] = x;
            i = i->mSibling;
        }
    }
    *children = buf;
    *count = n;
    return 0;
}

int get_symbol_offset(const Symbol * sym, ContextAddress * offset) {
    ObjectInfo * obj = sym->obj;
    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base || is_cardinal_type_pseudo_symbol(sym)) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (unpack(sym) < 0) return -1;
    if (obj != NULL && (obj->mTag == TAG_member || obj->mTag == TAG_inheritance)) {
        U8_T v;
        if (!get_num_prop(obj, AT_data_member_location, &v)) return -1;
        *offset = (ContextAddress)v;
        return 0;
    }
    errno = ERR_INV_CONTEXT;
    return -1;
}

int get_symbol_value(const Symbol * sym, void ** value, size_t * size, int * big_endian) {
    ObjectInfo * obj = sym->obj;
    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base || is_cardinal_type_pseudo_symbol(sym) || sym->var) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (unpack(sym) < 0) return -1;
    if (obj != NULL) {
        Trap trap;
        PropertyValue v;
        static U1_T * bf = NULL;
        static size_t bf_size = 0;
        if (set_trap(&trap)) {
            read_and_evaluate_dwarf_object_property(sym_ctx, sym_frame, 0, obj, AT_const_value, &v);
            if (v.mAddr != NULL) {
                *size = v.mSize;
                *value = v.mAddr;
            }
            else if (v.mRegister != NULL || v.mPieces != NULL) {
                str_exception(ERR_INV_CONTEXT, "Constant DWARF attribute value expected");
            }
            else {
                U8_T n = v.mValue;
                size_t i = 0;
                if (bf_size < sizeof(v.mValue)) {
                    bf_size = sizeof(v.mValue);
                    bf = (U1_T *)loc_realloc(bf, bf_size);
                }
                for (i = 0; i < sizeof(v.mValue); i++) {
                    bf[v.mBigEndian ? sizeof(v.mValue) - i - 1 : i] = n & 0xffu;
                    n = n >> 8;
                }
                *size = sizeof(v.mValue);
                *value = bf;
            }
            *big_endian = v.mBigEndian;
            clear_trap(&trap);
            return 0;
        }
        else if (trap.error != ERR_SYM_NOT_FOUND) {
            return -1;
        }
        if (set_trap(&trap)) {
            read_and_evaluate_dwarf_object_property(sym_ctx, sym_frame, 0, obj, AT_location, &v);
            if (v.mPieces != NULL) {
                U4_T n = 0;
                U4_T bf_offs = 0;
                while (n < v.mPieceCnt) {
                    U4_T i;
                    U1_T pbf[32];
                    PropertyValuePiece * piece = v.mPieces + n++;
                    U4_T piece_size = (piece->mBitSize + 7) / 8;
                    if (piece_size > sizeof(pbf)) exception(ERR_BUFFER_OVERFLOW);
                    if (bf_size < bf_offs / 8 + piece_size + 1) {
                        bf_size = bf_offs / 8 + piece_size + 1;
                        bf = (U1_T *)loc_realloc(bf, bf_size);
                    }
                    if (piece->mRegister) {
                        StackFrame * frame = NULL;
                        RegisterDefinition * def = piece->mRegister;
                        if (get_frame_info(v.mContext, v.mFrame, &frame) < 0) exception(errno);
                        if (read_reg_bytes(frame, def, 0, piece_size, pbf) < 0) exception(errno);
                    }
                    else {
                        if (context_read_mem(v.mContext, piece->mAddress, pbf, piece_size) < 0) exception(errno);
                    }
                    if (!piece->mBigEndian != !v.mBigEndian) swap_bytes(pbf, piece_size);
                    for (i = piece->mBitOffset; i < piece->mBitOffset + piece->mBitSize;  i++) {
                        if (pbf[i / 8] & (1u << (i % 8))) {
                            bf[bf_offs / 8] |=  (1u << (bf_offs % 8));
                        }
                        else {
                            bf[bf_offs / 8] &= ~(1u << (bf_offs % 8));
                        }
                        bf_offs++;
                    }
                }
                while (bf_offs % 8) {
                    bf[bf_offs / 8] &= ~(1u << (bf_offs % 8));
                    bf_offs++;
                }
                *value = bf;
                *size = bf_offs / 8;
                *big_endian = v.mBigEndian;
            }
            else if (v.mRegister != NULL) {
                StackFrame * frame = NULL;
                RegisterDefinition * def = v.mRegister;
                ContextAddress sym_size = def->size;
                unsigned val_offs = 0;
                unsigned val_size = 0;

                if (get_symbol_size(sym, &sym_size) < 0) exception(errno);
                if (bf_size < sym_size) {
                    bf_size = (size_t)sym_size;
                    bf = (U1_T *)loc_realloc(bf, bf_size);
                }
                if (get_frame_info(v.mContext, v.mFrame, &frame) < 0) exception(errno);
                val_size = def->size < sym_size ? (unsigned)def->size : (unsigned)sym_size;
                if (def->big_endian) val_offs = (unsigned)def->size - val_size;
                if (read_reg_bytes(frame, def, val_offs, val_size, bf) < 0) exception(errno);
                *value = bf;
                *size = val_size;
                *big_endian = def->big_endian;
            }
            else {
                exception(ERR_INV_CONTEXT);
            }
            clear_trap(&trap);
            return 0;
        }
        else if (trap.error != ERR_SYM_NOT_FOUND) {
            return -1;
        }
        set_errno(ERR_OTHER, "Object location or value info not available");
        return -1;
    }
    if (sym->tbl != NULL) {
        ELF_SymbolInfo info;
        unpack_elf_symbol_info(sym->tbl, sym->index, &info);
        switch (info.type) {
        case STT_OBJECT:
        case STT_FUNC:
            set_errno(ERR_OTHER, "Symbol represents an address");
            return -1;
        }
        if (info.sym_section->file->elf64) {
            static U8_T buf = 0;
            buf = info.value;
            *value = &buf;
            *size = 8;
        }
        else {
            static U4_T buf = 0;
            buf = (U4_T)info.value;
            *value = &buf;
            *size = 4;
        }
        *big_endian = big_endian_host();
        return 0;
    }
    errno = ERR_INV_CONTEXT;
    return -1;
}

static int calc_member_offset(ObjectInfo * type, ObjectInfo * member, ContextAddress * offs) {
    PropertyValue v;
    ObjectInfo * obj = NULL;
    if (member->mParent == type) {
        read_and_evaluate_dwarf_object_property(sym_ctx, sym_frame, 0, member, AT_data_member_location, &v);
        *offs = (ContextAddress)get_numeric_property_value(&v);
        return 1;
    }
    obj = type->mChildren;
    while (obj != NULL) {
        if (obj->mTag == TAG_inheritance && calc_member_offset(obj->mType, member, offs)) {
            read_and_evaluate_dwarf_object_property(sym_ctx, sym_frame, 0, obj, AT_data_member_location, &v);
            *offs += (ContextAddress)get_numeric_property_value(&v);
            return 1;
        }
        obj = obj->mSibling;
    }
    return 0;
}

int get_symbol_address(const Symbol * sym, ContextAddress * address) {
    ObjectInfo * obj = sym->obj;
    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base || is_cardinal_type_pseudo_symbol(sym)) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (sym->has_address) {
        *address = sym->address;
        return 0;
    }
    if (unpack(sym) < 0) return -1;
    if (sym->var != NULL) {
        /* The symbol represents a member of a class instance */
        Trap trap;
        PropertyValue v;
        ContextAddress base = 0;
        ContextAddress offs = 0;
        ObjectInfo * type = get_original_type(sym->var);
        if (!set_trap(&trap)) return -1;
        if ((type->mTag != TAG_pointer_type && type->mTag != TAG_mod_pointer) || type->mType == NULL) exception(ERR_INV_CONTEXT);
        read_and_evaluate_dwarf_object_property(sym_ctx, sym_frame, 0, sym->var, AT_location, &v);
        if (v.mPieces != NULL) {
            str_exception(ERR_UNSUPPORTED, "Unsupported location of 'this' pointer");
        }
        else if (v.mRegister != NULL) {
            U8_T rv = 0;
            StackFrame * frame = NULL;
            if (get_frame_info(v.mContext, v.mFrame, &frame) < 0) exception(errno);
            if (read_reg_value(frame, v.mRegister, &rv) < 0) exception(errno);
            base = (ContextAddress)rv;
        }
        else {
            if (elf_read_memory_word(sym_ctx, sym->var->mCompUnit->mFile,
                (ContextAddress)get_numeric_property_value(&v), &base) < 0) exception(errno);
        }
        type = get_original_type(type->mType);
        if (!calc_member_offset(type, obj, &offs)) exception(ERR_INV_CONTEXT);
        clear_trap(&trap);
        *address = base + offs;
        return 0;
    }
    if (obj != NULL && obj->mTag != TAG_member && obj->mTag != TAG_inheritance) {
        U8_T v;
        Symbol * s = NULL;
        if (get_num_prop(obj, AT_location, &v)) {
            *address = (ContextAddress)v;
            return 0;
        }
        if (get_error_code(errno) != ERR_SYM_NOT_FOUND) return -1;
        if (get_num_prop(obj, AT_low_pc, &v)) {
            *address = (ContextAddress)v;
            return 0;
        }
        if (get_error_code(errno) != ERR_SYM_NOT_FOUND) return -1;
        if (map_to_sym_table(obj, &s)) return get_symbol_address(s, address);
        set_errno(ERR_OTHER, "No object location info found in DWARF data");
        return -1;
    }
    if (sym->tbl != NULL) {
        ELF_SymbolInfo info;
        unpack_elf_symbol_info(sym->tbl, sym->index, &info);
        return syminfo2address(sym_ctx, &info, address);
    }

    errno = ERR_INV_CONTEXT;
    return -1;
}

int get_symbol_register(const Symbol * sym, Context ** ctx, int * frame, RegisterDefinition ** reg) {
    ObjectInfo * obj = sym->obj;
    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base || is_cardinal_type_pseudo_symbol(sym) || sym->has_address) {
        errno = ERR_INV_CONTEXT;
        return -1;
    }
    if (unpack(sym) < 0) return -1;
    if (obj != NULL && obj->mTag != TAG_member && obj->mTag != TAG_inheritance) {
        Trap trap;
        if (set_trap(&trap)) {
            PropertyValue v;
            read_and_evaluate_dwarf_object_property(sym_ctx, sym_frame, 0, obj, AT_location, &v);
            *ctx = sym_ctx;
            *frame = sym_frame;
            *reg = v.mRegister;
            clear_trap(&trap);
            return 0;
        }
    }

    errno = ERR_INV_CONTEXT;
    return -1;
}

int get_symbol_flags(const Symbol * sym, SYM_FLAGS * flags) {
    U8_T v = 0;
    ObjectInfo * i = NULL;
    ObjectInfo * obj = sym->obj;
    *flags = 0;
    assert(sym->magic == SYMBOL_MAGIC);
    if (sym->base || is_cardinal_type_pseudo_symbol(sym)) return 0;
    if (unpack(sym) < 0) return -1;
    i = obj;
    while (i != NULL) {
        switch (i->mTag) {
        case TAG_subrange_type:
            *flags |= SYM_FLAG_SUBRANGE_TYPE;
            i = i->mType;
            break;
        case TAG_packed_type:
            *flags |= SYM_FLAG_PACKET_TYPE;
            i = i->mType;
            break;
        case TAG_const_type:
            *flags |= SYM_FLAG_CONST_TYPE;
            i = i->mType;
            break;
        case TAG_volatile_type:
            *flags |= SYM_FLAG_VOLATILE_TYPE;
            i = i->mType;
            break;
        case TAG_restrict_type:
            *flags |= SYM_FLAG_RESTRICT_TYPE;
            i = i->mType;
            break;
        case TAG_shared_type:
            *flags |= SYM_FLAG_SHARED_TYPE;
            i = i->mType;
            break;
        case TAG_typedef:
            if (i == obj) *flags |= SYM_FLAG_TYPEDEF;
            i = i->mType;
            break;
        case TAG_reference_type:
        case TAG_mod_reference:
            *flags |= SYM_FLAG_REFERENCE;
            i = NULL;
            break;
        case TAG_union_type:
            *flags |= SYM_FLAG_UNION_TYPE;
            i = NULL;
            break;
        case TAG_class_type:
            *flags |= SYM_FLAG_CLASS_TYPE;
            i = NULL;
            break;
        case TAG_interface_type:
            *flags |= SYM_FLAG_INTERFACE_TYPE;
            i = NULL;
            break;
        case TAG_unspecified_parameters:
            *flags |= SYM_FLAG_PARAMETER;
            *flags |= SYM_FLAG_VARARG;
            i = NULL;
            break;
        case TAG_formal_parameter:
        case TAG_variable:
        case TAG_constant:
        case TAG_base_type:
            if (i->mTag == TAG_formal_parameter) {
                *flags |= SYM_FLAG_PARAMETER;
                if (get_num_prop(i, AT_is_optional, &v) && v != 0) *flags |= SYM_FLAG_OPTIONAL;
            }
            if (i->mTag == TAG_variable && get_num_prop(obj, AT_external, &v) && v != 0) {
                *flags |= SYM_FLAG_EXTERNAL;
            }
            if (get_num_prop(i, AT_endianity, &v)) {
                if (v == DW_END_big) *flags |= SYM_FLAG_BIG_ENDIAN;
                if (v == DW_END_little) *flags |= SYM_FLAG_LITTLE_ENDIAN;
            }
            i = NULL;
            break;
        default:
            i = NULL;
            break;
        }
    }
    if (obj != NULL && sym->sym_class == SYM_CLASS_TYPE && !(*flags & (SYM_FLAG_BIG_ENDIAN|SYM_FLAG_LITTLE_ENDIAN))) {
        *flags |= obj->mCompUnit->mFile->big_endian ? SYM_FLAG_BIG_ENDIAN : SYM_FLAG_LITTLE_ENDIAN;
    }
    return 0;
}

int get_array_symbol(const Symbol * sym, ContextAddress length, Symbol ** ptr) {
    assert(sym->magic == SYMBOL_MAGIC);
    assert(sym->sym_class == SYM_CLASS_TYPE);
    assert(sym->frame == STACK_NO_FRAME);
    assert(sym->ctx == context_get_group(sym->ctx, CONTEXT_GROUP_PROCESS));
    *ptr = alloc_symbol();
    (*ptr)->ctx = sym->ctx;
    (*ptr)->frame = STACK_NO_FRAME;
    (*ptr)->sym_class = SYM_CLASS_TYPE;
    (*ptr)->base = (Symbol *)sym;
    (*ptr)->length = length;
    return 0;
}

#endif /* SERVICE_Symbols && ENABLE_ELF */
