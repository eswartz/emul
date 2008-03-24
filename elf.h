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
 * This module implements reading and caching of ELF files.
 */
#ifndef D_elf
#define D_elf

#include "mdep.h"
//#include <sys/types.h>
#if !defined(WIN32)
#  include <elf.h>
#endif

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
 * Open ELF file for reading.
 * Same file can be opened mutiple times, each call to elf_open() increases reference counter.
 * File must be closed after usage by calling elf_close().
 * Returns the file descriptior on success. If error, returns NULL and sets errno.
 */
extern ELF_File * elf_open(char * file_name);

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
 * Close ELF file.
 * Each call of elf_close() decrements reference counter.
 * The file be kept in cache for some time even after all references are closed.
 */
extern void elf_close(ELF_File * file);

/*
 * Register ELF file close callback.
 * The callback is called each time an ELF file data is about to be disposed.
 * Service implementation can use the callback to deallocate
 * cached data related to the file.
 */
typedef void (*ELFCloseListener)(ELF_File *);
extern void elf_add_close_listener(ELFCloseListener listener);

#endif

