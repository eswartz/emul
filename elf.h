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
 * This module implements reading and caching of ELF files.
 */
#ifndef D_elf
#define D_elf

#include "mdep.h"
#if !defined(WIN32)
#  include <elf.h>
#endif
#include "context.h"

typedef unsigned char U1_T;
typedef signed char I1_T;
typedef unsigned short U2_T;
typedef signed short I2_T;
typedef unsigned int U4_T;
typedef signed int I4_T;
typedef uns64 U8_T;
typedef int64 I8_T;

struct ELF_File;
struct ELF_Section;
typedef struct ELF_Section ELF_Section;
typedef struct ELF_File ELF_File;

struct ELF_File {
    ELF_File * next;
    U4_T ref_cnt;

    char * name;
    dev_t dev;
    ino_t ino;
    time_t mtime;
    int fd;

    int big_endian;
    U4_T section_cnt;
    ELF_Section ** sections;

    void * libelf_cache;
    void * dwarf_cache;
    void * sym_cache;
    void * line_numbers_cache;

    int listed;
};

struct ELF_Section {
    ELF_File * file;
    U4_T index;
    char * name;
    U4_T type;
    U4_T flags;
    U8_T offset;
    U8_T size;
    U8_T addr;
    U4_T link;
    U4_T info;     
};

/*
 * Iterate context ELF files that are mapped in context memory in given address range (inclusive).
 * Returns the file descriptior on success. If error, returns NULL and sets errno.
 */
extern ELF_File * elf_list_first(Context * ctx, ContextAddress addr0, ContextAddress addr1);
extern ELF_File * elf_list_next(Context * ctx);

/*
 * Finish iteration of context ELF files.
 * Clients should always call elf_list_done() after calling elf_list_first().
 */
extern void elf_list_done(Context * ctx);

/*
 * Read section data from ELF file.
 * '*rd_len' is set to number of bytes transfered into the buffer.
 * Returns zero on success. If error, returns -1 and sets errno.
 */
extern int elf_read(ELF_Section * section, U8_T offset, U1_T * buf, U4_T size, U4_T * rd_len);

/*
 * Load section data into memory.
 * '*address' is set to section data address in memory.
 * Data will stay in memory at least until file is closed.
 * Returns zero on success. If error, returns -1 and sets errno.
 */
extern int elf_load(ELF_Section * section, U1_T ** address);

/*
 * Register ELF file close callback.
 * The callback is called each time an ELF file data is about to be disposed.
 * Service implementation can use the callback to deallocate
 * cached data related to the file.
 */
typedef void (*ELFCloseListener)(ELF_File *);
extern void elf_add_close_listener(ELFCloseListener listener);

#endif

