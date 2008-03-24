/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

/*
 * Symbols service.
 */
#include "mdep.h"
#include "config.h"

#if SERVICE_Symbols

#if defined(_WRS_KERNEL)
#  include <symLib.h>
#  include <sysSymTbl.h>
#elif defined(WIN32)
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
#include "symbols.h"

#define SYM_HASH_SIZE 1023

typedef struct SymbolTable SymbolTable;

struct SymbolTable {
    SymbolTable * next;
    char * str;
    int str_size;
    int sym_cnt;
    void * syms;    /* pointer to ELF section data: Elf32_Sym* or Elf64_Sym* */
    int hash[SYM_HASH_SIZE];
    int * hash_next;
};

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
    SymbolTable * tables = (SymbolTable *)file->sym_cache;
    while (tables != NULL) {
        SymbolTable * tbl = tables;
        tables = tbl->next;
        loc_free(tbl->hash_next);
        loc_free(tbl);
    }
}

static int load_tables(ELF_File * file) {
    int error = 0;

#ifdef ELFMAG
    unsigned idx;
    for (idx = 0; idx < file->section_cnt; idx++) {
        ELF_Section * sym_sec = file->sections[idx];
        if (sym_sec == NULL) continue;
        if (sym_sec->type == SHT_SYMTAB && sym_sec->size > 0) {
            int i;
            ELF_Section * str_sec;
            U1_T * str_data = NULL;
            U1_T * sym_data = NULL;
            SymbolTable * tbl = (SymbolTable *)loc_alloc_zero(sizeof(SymbolTable));
            tbl->next = (SymbolTable *)file->sym_cache;
            file->sym_cache = tbl;
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

#else
    error = EINVAL;
#endif

    if (error != 0) {
        free_sym_cache(file);
        errno = error;
        return -1;
    }

    return 0;
}

int find_symbol(Context * ctx, char * name, Symbol * sym) {
    int error = 0;

#if defined(WIN32)
    // TODO symbols for WIN32

    extern void tcf_test_func0(void);
    extern void tcf_test_func1(void);
    extern void tcf_test_func2(void);
    extern char * tcf_test_array;

    memset(sym, 0, sizeof(Symbol));
    sym->section = ".text";
    sym->storage = "GLOBAL";
    sym->abs = 1;
    if (strcmp(name, "tcf_test_func0") == 0) {
        sym->value = (unsigned long)tcf_test_func0;
    }
    else if (strcmp(name, "tcf_test_func1") == 0) {
        sym->value = (unsigned long)tcf_test_func1;
    }
    else if (strcmp(name, "tcf_test_func2") == 0) {
        sym->value = (unsigned long)tcf_test_func2;
    }
    else if (strcmp(name, "tcf_test_array") == 0) {
        sym->value = (unsigned long)&tcf_test_array;
    }
    else {
        error = EINVAL;
    }

#elif defined(_WRS_KERNEL)
    
    char * ptr;
    SYM_TYPE type;

    memset(sym, 0, sizeof(Symbol));
    if (symFindByName(sysSymTbl, name, &ptr, &type) != OK) {
        error = errno;
        if (error == S_symLib_SYMBOL_NOT_FOUND) error = ERR_SYM_NOT_FOUND;
        assert(error != 0);
    }
    else {
        sym->abs = 1;
        sym->value = (unsigned long)ptr;
        
        if (SYM_IS_UNDF(type)) sym->storage = "UNDEF";
        else if (SYM_IS_COMMON(type)) sym->storage = "COMMON";
        else if (SYM_IS_GLOBAL(type)) sym->storage = "GLOBAL";
        else if (SYM_IS_LOCAL(type)) sym->storage = "LOCAL";
        
        if (SYM_IS_TEXT(type)) sym->section = ".text";
        else if (SYM_IS_DATA(type)) sym->section = ".data";
        else if (SYM_IS_BSS(type)) sym->section = ".bss";
        assert(!SYM_IS_ABS(type) || sym->section == NULL);
    }

#else

    char fnm[FILE_PATH_SIZE];
    int found = 0;
    ELF_File * file;

    memset(sym, 0, sizeof(Symbol));
    snprintf(fnm, sizeof(fnm), "/proc/%d/exe", ctx->mem);
    file = elf_open(fnm);
    if (file == NULL) error = errno;

    if (error == 0 && file->sym_cache == NULL) {
        if (load_tables(file) < 0) error = errno;
    }

    if (error == 0) {
        int h = calc_hash(name);
        SymbolTable * tbl = (SymbolTable *)file->sym_cache;
        while (tbl != NULL && !found) {
            int n = tbl->hash[h];
            while (n && !found) {
                Elf32_Sym * s = (Elf32_Sym *)tbl->syms + n;
                if (strcmp(name, tbl->str + s->st_name) == 0) {
                    found = 1;
                    sym->abs = 1;
                    sym->value = s->st_value;
                    switch (ELF32_ST_BIND(s->st_info)) {
                    case STB_LOCAL: sym->storage = "LOCAL"; break;
                    case STB_GLOBAL: sym->storage = "GLOBAL"; break;
                    case STB_WEAK: sym->storage = "WEAK"; break;
                    }
                    if (s->st_shndx > 0 && s->st_shndx < file->section_cnt) {
                        static char sec_name[128];
                        ELF_Section * sec = file->sections[s->st_shndx];
                        if (sec != NULL && sec->name != NULL) {
                            sym->section = strncpy(sec_name, sec->name, sizeof(sec_name));
                        }
                    }
                }
                n = tbl->hash_next[n];
            }
            tbl = tbl->next;
        }
    }

    if (error == 0 && !found) error = ERR_SYM_NOT_FOUND;

#endif

    if (error) {
        errno = error;
        return -1;
    }
    return 0;
}

void ini_symbols_service(void) {
    elf_add_close_listener(free_sym_cache);
}

#endif

