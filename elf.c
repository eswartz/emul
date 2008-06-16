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
#include "mdep.h"
#include "config.h"

#if ((SERVICE_LineNumbers) || (SERVICE_Symbols)) && !defined(WIN32)

#include <assert.h>
#include <string.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <stdio.h>
#include "elf.h"
#include "myalloc.h"
#include "exceptions.h"

#if defined(_WRS_KERNEL)
#elif defined(WIN32)
#else
#  include <libelf.h>
#  define USE_LIBELF
#endif

#define MAX_CACHED_FILES 8

static ELF_File * files = NULL;
static ELFCloseListener * listeners = NULL;
static U4_T listeners_cnt = 0;
static U4_T listeners_max = 0;

static void elf_dispose(ELF_File * file) {
    U4_T n;
    assert(file->ref_cnt == 0);
    for (n = 0; n < listeners_cnt; n++) {
        listeners[n](file);
    }
#ifdef USE_LIBELF
    if (file->libelf_cache != NULL) elf_end(file->libelf_cache);
#endif
    if (file->fd >= 0) close(file->fd);
    if (file->sections != NULL) {
        for (n = 0; n < file->section_cnt; n++) {
            loc_free(file->sections[n]);
        }
        loc_free(file->sections);
    }
    free(file->name);
    loc_free(file);
}

ELF_File * elf_open(char * file_name) {
    int cnt = 0;
    int error = 0;
    struct_stat st;
    ELF_File * prev = NULL;
    ELF_File * file = files;

    file_name = canonicalize_file_name(file_name);
    if (file_name == NULL) return NULL;
    if (stat(file_name, &st) < 0) {
        error = errno;
        free(file_name);
        errno = error;
        return NULL;
    }
    while (file != NULL) {
        if (strcmp(file->name, file_name) == 0 &&
                file->dev == st.st_dev &&
                file->ino == st.st_ino &&
                file->mtime == st.st_mtime) {
            if (prev != NULL) {
                prev->next = file->next;
                file->next = files;
                files = file;
            }
            file->ref_cnt++;
            free(file_name);
            return file;
        }
        if (cnt >= MAX_CACHED_FILES && file->ref_cnt == 0) {
            prev->next = file->next;
            elf_dispose(file);
            file = prev->next;
        }
        else {
            prev = file;
            file = file->next;
            cnt++;
        }
    }

    file = (ELF_File *)loc_alloc_zero(sizeof(ELF_File));
    file->name = file_name;
    file->dev = st.st_dev;
    file->ino = st.st_ino;
    file->mtime = st.st_mtime;
#ifdef _WRS_KERNEL
    if ((file->fd = open(file->name, O_RDONLY, 0)) < 0) {
#else        
    if ((file->fd = open(file->name, O_RDONLY)) < 0) {
#endif
        error = errno;
    }

#ifdef USE_LIBELF
    if (error == 0) {
        Elf * elf;
        Elf32_Ehdr * ehdr;
        /* Obtain the ELF descriptor */
        (void)elf_version(EV_CURRENT);
        if ((elf = elf_begin(file->fd, ELF_C_READ, NULL)) == NULL) {
            error = errno;
        }
        else {
            file->libelf_cache = elf;
            if ((ehdr = elf32_getehdr(elf)) == NULL) {
                error = errno;
            }
        }
        if (error == 0) {
            size_t snum = 0;
            size_t shstrndx = 0;
            if (elf_getshnum(elf, &snum) < 0) error = errno;
            if (error == 0) {
                file->sections = (ELF_Section **)loc_alloc_zero(sizeof(ELF_Section *) * snum);
                file->section_cnt = snum;
                if (elf_getshstrndx(elf, &shstrndx) < 0) error = errno;
            }
            if (error == 0) {
                /* Iterate sections */
                Elf_Scn * scn = NULL;
                while ((scn = elf_nextscn(elf, scn)) != NULL) {
                    Elf32_Shdr * shdr = NULL;
                    char * name = NULL;
                    ELF_Section * sec = NULL;
                    if ((shdr = elf32_getshdr(scn)) == NULL) {
                        error = errno;
                        break;
                    }
                    if ((name = elf_strptr(elf, shstrndx, shdr->sh_name)) == NULL) {
                        error = errno;
                        break;
                    }
                    sec = (ELF_Section *)loc_alloc(sizeof(ELF_Section));
                    sec->file = file;
                    sec->index = elf_ndxscn(scn);
                    sec->name = name;
                    sec->type = shdr->sh_type;
                    sec->offset = shdr->sh_offset;
                    sec->size = shdr->sh_size;
                    sec->flags = shdr->sh_flags;
                    sec->addr = shdr->sh_addr;
                    sec->link = shdr->sh_link;
                    sec->info = shdr->sh_info;
                    assert(sec->index < snum);
                    file->sections[sec->index] = sec;
                }
            }
        }
    }
#else
    if (error == 0) {
        error = EINVAL;
    }
#endif
    if (error != 0) {
        elf_dispose(file);
        errno = error;
        return NULL;
    }
    file->ref_cnt = 1;
    file->next = files;
    return files = file;
}

ELF_File * elf_open_main(Context * ctx) {
#if defined(_WRS_KERNEL)
    exception(EINVAL);
#else
    char fnm[FILE_PATH_SIZE];
    snprintf(fnm, sizeof(fnm), "/proc/%d/exe", ctx->mem);
    return elf_open(fnm);
#endif
}

int elf_load(ELF_Section * section, U1_T ** address) {
#ifdef USE_LIBELF
    Elf * elf = (Elf *)section->file->libelf_cache;
    Elf_Scn * scn = elf_getscn(elf, section->index);
    Elf_Data * data = elf_getdata(scn, NULL);
    if (data == NULL) return -1;
    assert(data->d_buf != NULL && data->d_size == section->size);
    *address = data->d_buf;
    return 0;
#else
    *address = NULL;
    errno = EINVAL;
    return -1;
#endif
}

int elf_read(ELF_Section * section, U8_T offset, U1_T * buf, U4_T size, U4_T * rd_len) {
#ifdef USE_LIBELF
    U4_T rd = size;
    Elf * elf = (Elf *)section->file->libelf_cache;
    Elf_Scn * scn = elf_getscn(elf, section->index);
    Elf_Data * data = elf_getdata(scn, NULL);
    if (data == NULL) return -1;
    assert(data->d_buf != NULL && data->d_size == section->size);
    assert(offset < section->size);
    if (offset + rd > section->size) rd = (U4_T)(section->size - offset);
    memcpy(buf, data->d_buf + offset, rd);
    *rd_len = rd;
    return 0;
#else
    *rd_len = 0;
    errno = EINVAL;
    return -1;
#endif
}

void elf_close(ELF_File * file) {
    assert(file != NULL);
    assert(file->ref_cnt > 0);
    file->ref_cnt--;
}

void elf_add_close_listener(ELFCloseListener listener) {
    if (listeners_cnt >= listeners_max) {
        listeners_max = listeners_max == 0 ? 16 : listeners_max * 2;
        listeners = (ELFCloseListener *)loc_realloc(listeners, sizeof(ELFCloseListener) * listeners_max);
    }
    listeners[listeners_cnt++] = listener;
}

#endif
