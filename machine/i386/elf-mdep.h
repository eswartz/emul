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
 * This module provides CPU specific ELF definitions for X86.
 */

#define R_386_NONE  0
#define R_386_32    1
#define R_386_PC32  2

static void elf_relocate(void) {
    U4_T value = 0;
    if (sym_index != STN_UNDEF) {
        Elf32_Sym bf = ((Elf32_Sym *)symbols->data)[sym_index];
        if (symbols->file->byte_swap) {
            SWAP(bf.st_name);
            SWAP(bf.st_value);
            SWAP(bf.st_size);
            SWAP(bf.st_info);
            SWAP(bf.st_other);
            SWAP(bf.st_shndx);
        }
        if (symbols->file->type != ET_EXEC) {
            switch (bf.st_shndx) {
            case SHN_ABS:
                value = bf.st_value;
                break;
            case SHN_COMMON:
            case SHN_UNDEF:
                str_exception(ERR_INV_FORMAT, "Invalid relocation record");
                break;
            default:
                if (bf.st_shndx >= symbols->file->section_cnt) str_exception(ERR_INV_FORMAT, "Invalid relocation record");
                value = (U4_T)(symbols->file->sections + bf.st_shndx)->addr + bf.st_value;
                *destination = symbols->file->sections + bf.st_shndx;
                break;
            }
        }
        else {
            value = bf.st_value;
        }
    }
    if (relocs->type == SHT_REL) {
        U4_T x = *(U4_T *)((char *)section->data + reloc_offset);
        if (section->file->type != ET_REL) str_exception(ERR_INV_FORMAT, "Invalid relocation record");
        if (section->file->byte_swap) SWAP(x);
        assert(reloc_addend == 0);
        reloc_addend = x;
    }
    switch (reloc_type) {
    case R_386_NONE:
        *destination = NULL;
        break;
    case R_386_32:
        if (data_size < 4) str_exception(ERR_INV_FORMAT, "Invalid relocation record");
        *(U4_T *)data_buf = value + reloc_addend;
        if (section->file->byte_swap) SWAP(*(U4_T *)data_buf);
        break;
    case R_386_PC32:
        if (data_size < 4) str_exception(ERR_INV_FORMAT, "Invalid relocation record");
        *(U4_T *)data_buf = (U4_T)(section->addr + reloc_offset) + value + reloc_addend;
        if (section->file->byte_swap) SWAP(*(U4_T *)data_buf);
        break;
    default:
        str_exception(ERR_INV_FORMAT, "Unsupported relocation type");
    }
}
