/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
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
 * This module implements ELF relocation records handling for reading DWARF debug information.
 *
 * Functions in this module use exceptions to report errors, see exceptions.h
 */

#include <config.h>

#if ENABLE_ELF

#include <assert.h>
#include <framework/exceptions.h>
#include <services/dwarfreloc.h>

static ELF_Section * section = NULL;
static ELF_Section * relocs = NULL;
static ELF_Section * symbols = NULL;
static ELF_Section ** destination = NULL;

static U8_T reloc_offset = 0;
static U4_T reloc_type = 0;
static U4_T reloc_addend = 0;
static U4_T sym_index = 0;

static void * data_buf = NULL;
static size_t data_size = 0;

#define elf_relocate elf_relocate_i386
#include <machine/i386/elf-mdep.h>
#undef elf_relocate

typedef struct ElfRelocateFunc {
    int machine;
    void (*func)(void);
} ElfRelocateFunc;

static ElfRelocateFunc elf_relocate_funcs[] = {
    { EM_386, elf_relocate_i386 },
    { EM_NONE, NULL }
};

static void relocate(void * r) {
    ElfRelocateFunc * func;
    if (!relocs->file->elf64) {
        if (relocs->type == SHT_REL) {
            Elf32_Rel bf = *(Elf32_Rel *)r;
            if (relocs->file->byte_swap) {
                SWAP(bf.r_offset);
                SWAP(bf.r_info);
            }
            sym_index = ELF32_R_SYM(bf.r_info);
            reloc_type = ELF32_R_TYPE(bf.r_info);
            reloc_addend = 0;
        }
        else {
            Elf32_Rela bf = *(Elf32_Rela *)r;
            if (relocs->file->byte_swap) {
                SWAP(bf.r_offset);
                SWAP(bf.r_info);
                SWAP(bf.r_addend);
            }
            sym_index = ELF32_R_SYM(bf.r_info);
            reloc_type = ELF32_R_TYPE(bf.r_info);
            reloc_addend = bf.r_addend;
        }
    }
    else {
        exception(ERR_INV_FORMAT);
    }
    func = elf_relocate_funcs;
    while (func->machine != section->file->machine) {
        if (func->func == NULL) str_exception(ERR_INV_FORMAT, "Unsupported ELF machine code");
        func++;
    }
    func->func();
}

void drl_relocate(ELF_Section * s, U8_T offset, void * buf, size_t size, ELF_Section ** dst) {
    unsigned i;
    ELF_Section * d = NULL;

    if (dst == NULL) dst = &d;
    else *dst = NULL;
    if (!s->relocate) return;

    section = s;
    destination = dst;
    reloc_offset = offset;
    data_buf = buf;
    data_size = size;
    for (i = 1; i < s->file->section_cnt; i++) {
        ELF_Section * r = s->file->sections + i;
        if (r->size == 0) continue;
        if (r->type != SHT_REL && r->type != SHT_RELA) continue;
        if (r->info == s->index) {
            uint8_t * p;
            uint8_t * q;
            relocs = r;
            symbols = s->file->sections + r->link;
            if (elf_load(relocs) < 0) exception(errno);
            if (elf_load(symbols) < 0) exception(errno);
            if (r->entsize == 0 || r->size % r->entsize != 0) str_exception(ERR_INV_FORMAT, "Invalid sh_entsize");
            p = (uint8_t *)r->data;
            q = p + r->size;
            while (p < q) {
                unsigned n = (q - p) / r->entsize / 2;
                uint8_t * x = p + n * r->entsize;
                assert(x < q);
                if (r->file->elf64) {
                    U8_T offs = *(U8_T *)x;
                    if (r->file->byte_swap) SWAP(offs);
                    if (s->file->type == ET_EXEC) offs -= s->addr;
                    if (offset > offs) {
                        p = x + r->entsize;
                        continue;
                    }
                    if (offset < offs) {
                        q = x;
                        continue;
                    }
                }
                else {
                    U4_T offs = *(U4_T *)x;
                    if (r->file->byte_swap) SWAP(offs);
                    if (s->file->type == ET_EXEC) offs -= (U4_T)s->addr;
                    if (offset > offs) {
                        p = x + r->entsize;
                        continue;
                    }
                    if (offset < offs) {
                        q = x;
                        continue;
                    }
                }
                relocate(x);
                return;
            }
        }
    }
}

#endif /* ENABLE_ELF */
