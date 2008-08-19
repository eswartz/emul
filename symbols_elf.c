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
#else
#  include <elf.h>
#  include <libelf.h>
#  include <fcntl.h>
#endif
#include <errno.h>
#include <assert.h>
#include <stdlib.h>
#include <stdio.h>
#include "errors.h"
#include "elf.h"
#include "myalloc.h"
#include "events.h"
#include "symbols.h"

#define SYM_HASH_SIZE 1023
#define SYM_CACHE_MAGIC 0x84625490

typedef struct SymbolCache SymbolCache;
typedef struct SymbolTable SymbolTable;

struct SymbolCache {
    int magic;
    SymbolTable ** tables;
    int tables_cnt;
    int tables_len;
};

struct SymbolTable {
    int index;
    char * str;
    int str_size;
    int sym_cnt;
    void * syms;    /* pointer to ELF section data: Elf32_Sym* or Elf64_Sym* */
    int hash[SYM_HASH_SIZE];
    int * hash_next;
};

static ELF_File ** files = NULL;
static int files_cnt = 0;
static int files_len = 0;

static void unlock_files(void * arg) {
    int i;
    for (i = 0; i < files_cnt; i++) files[i]->ref_cnt--;
    files_cnt = 0;
}

static void lock_file(ELF_File * file) {
    if (files == NULL) {
        files_cnt = 0;
        files_len = 256;
        files = loc_alloc(sizeof(ELF_File *) * files_len);
    }
    else if (files_cnt >= files_len) {
        files_len *= 2;
        files = loc_realloc(files, sizeof(ELF_File *) * files_len);
    }
    if (files_cnt == 0) post_event(unlock_files, NULL);
    files[files_cnt++] = file;
    file->ref_cnt++;
}

static int calc_hash(char * s) {
    unsigned h = 0;
    while (*s) {
        unsigned g;
        h = (h << 4) + *s++;
        if (g = h & 0xf0000000) h ^= g >> 24;
        h &= ~g;
    }
    return h % SYM_HASH_SIZE;
}

static void free_sym_cache(ELF_File * file) {
    SymbolCache * cache = (SymbolCache *)file->sym_cache;
    if (cache != NULL) {
        int i = 0;
        assert(cache->magic == SYM_CACHE_MAGIC);
        cache->magic = 0;
        while (i < cache->tables_cnt) {
            SymbolTable * tbl = cache->tables[i++];
            loc_free(tbl->hash_next);
            loc_free(tbl);
        }
        loc_free(cache);
        file->sym_cache = NULL;
    }
}

static int load_symbol_tables(ELF_File * file) {
    int error = 0;
    unsigned idx;
    SymbolCache * cache;

    assert(file->sym_cache == NULL);
    cache = (SymbolCache *)(file->sym_cache = loc_alloc_zero(sizeof(SymbolCache)));
    cache->magic = SYM_CACHE_MAGIC;

    for (idx = 0; idx < file->section_cnt; idx++) {
        ELF_Section * sym_sec = file->sections[idx];
        if (sym_sec == NULL) continue;
        if (sym_sec->type == SHT_SYMTAB && sym_sec->size > 0) {
            int i;
            ELF_Section * str_sec;
            U1_T * str_data = NULL;
            U1_T * sym_data = NULL;
            SymbolTable * tbl = (SymbolTable *)loc_alloc_zero(sizeof(SymbolTable));
            if (cache->tables == NULL) {
                cache->tables_len = 8;
                cache->tables = loc_alloc(sizeof(SymbolTable *) * cache->tables_len);
            }
            else if (cache->tables_cnt >= cache->tables_len) {
                cache->tables_len *= 8;
                cache->tables = loc_realloc(cache->tables, sizeof(SymbolTable *) * cache->tables_len);
            }
            tbl->index = cache->tables_cnt;
            cache->tables[cache->tables_cnt++] = tbl;
            if (sym_sec->link >= file->section_cnt || (str_sec = file->sections[sym_sec->link]) == NULL) {
                error = EINVAL;
                break;
            }
            if (elf_load(sym_sec, &sym_data) < 0) {
                error = errno;
                assert(error != 0);
                break;
            }
            if (elf_load(str_sec, &str_data) < 0) {
                error = errno;
                assert(error != 0);
                break;
            }
            tbl->str = (char *)str_data;
            tbl->str_size = str_sec->size;
            tbl->syms = sym_data;
            tbl->sym_cnt = sym_sec->size / sizeof(Elf32_Sym);
            tbl->hash_next = (int *)loc_alloc(tbl->sym_cnt * sizeof(int));
            for (i = 0; i < tbl->sym_cnt; i++) {
                Elf32_Sym * s = (Elf32_Sym *)tbl->syms + i;
                assert(s->st_name < tbl->str_size);
                if (s->st_name == 0) {
                    tbl->hash_next[i] = 0;
                }
                else {
                    int h = calc_hash(tbl->str + s->st_name);
                    tbl->hash_next[i] = tbl->hash[h];
                    tbl->hash[h] = i;
                }
            }
        }
    }

    if (error != 0) {
        free_sym_cache(file);
        errno = error;
        return -1;
    }

    return 0;
}

int find_symbol(Context * ctx, int frame, char * name, Symbol * sym) {
    int error = 0;

#if defined(_WRS_KERNEL)
    
    char * ptr;
    SYM_TYPE type;

    memset(sym, 0, sizeof(Symbol));
    if (symFindByName(sysSymTbl, name, &ptr, &type) != OK) {
        error = errno;
        if (error == S_symLib_SYMBOL_NOT_FOUND) error = ERR_SYM_NOT_FOUND;
        assert(error != 0);
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
    }

#else

    int found = 0;
    ELF_File * file = elf_list_first(ctx, 0, ~(ContextAddress)0);
    if (file == NULL) error = errno;
    memset(sym, 0, sizeof(Symbol));

    while (file != NULL) {
        if (file->sym_cache == NULL) {
            if (load_symbol_tables(file) < 0) error = errno;
        }
        if (error == 0) {
            int m = 0;
            int h = calc_hash(name);
            SymbolCache * cache = (SymbolCache *)file->sym_cache;
            while (m < cache->tables_cnt && !found) {
                SymbolTable * tbl = cache->tables[m++];
                int n = tbl->hash[h];
                while (n && !found) {
                    Elf32_Sym * s = (Elf32_Sym *)tbl->syms + n;
                    if (strcmp(name, tbl->str + s->st_name) == 0) {
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
                        lock_file(file);
                        sym->module_id = (ModuleID)(unsigned)file;
                    }
                    n = tbl->hash_next[n];
                }
            }
        }
        if (error != 0) break;
        if (found) break;
        file = elf_list_next(ctx);
        if (file == NULL) error = errno;
    }
    elf_list_done(ctx);

    if (error == 0 && !found) error = ERR_SYM_NOT_FOUND;

#endif

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
    errno = ERR_UNSUPPORTED;
    return -1;
}

ContextAddress is_plt_section(Context * ctx, ContextAddress addr) {
#if defined(_WRS_KERNEL)
    return 0;
#else
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
                res = sec->addr;
                break;
            }
        }
        if (res != 0) break;
        file = elf_list_next(ctx);
    }
    elf_list_done(ctx);
    return res;
#endif
}

void ini_symbols_service(void) {
    elf_add_close_listener(free_sym_cache);
}

#endif

