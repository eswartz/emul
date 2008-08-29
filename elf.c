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
#include "events.h"
#include "trace.h"

#if defined(_WRS_KERNEL)
#elif defined(WIN32)
#else
#  include <libelf.h>
#  define USE_LIBELF
#endif

typedef struct MemoryRegion MemoryRegion;

struct MemoryRegion {
    ContextAddress addr;
    unsigned long size;
    char * file_name;
    ELF_File * file;
};

typedef struct MemoryMap MemoryMap;

struct MemoryMap {
    int error;
    int region_cnt;
    int region_max;
    MemoryRegion * regions;
};

#define MAX_CACHED_FILES 16

static ELF_File * files = NULL;
static ELFCloseListener * listeners = NULL;
static U4_T listeners_cnt = 0;
static U4_T listeners_max = 0;
static int context_listener_added = 0;
static int elf_cleanup_posted = 0;

static void elf_dispose(ELF_File * file) {
    U4_T n;
    trace(LOG_ELF, "Dispose ELF file cache %s", file->name);
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
    loc_free(file->name);
    loc_free(file);
}

static void elf_cleanup_event(void * arg) {
    int cnt = 0;
    ELF_File * prev = NULL;
    ELF_File * file = files;

    assert(elf_cleanup_posted);
    elf_cleanup_posted = 0;
    while (file != NULL) {
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
}

ELF_File * elf_open(char * file_name) {
    int error = 0;
    struct_stat st;
    ELF_File * prev = NULL;
    ELF_File * file = files;

    if (!elf_cleanup_posted) {
        post_event_with_delay(elf_cleanup_event, NULL, 1000000);
        elf_cleanup_posted = 1;
    }

    if (stat(file_name, &st) < 0) {
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
            return file;
        }
        prev = file;
        file = file->next;
    }

    trace(LOG_ELF, "Create ELF file cache %s", file_name);

    file = (ELF_File *)loc_alloc_zero(sizeof(ELF_File));
    file->name = loc_strdup(file_name);
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

void elf_close(ELF_File * file) {
    assert(file != NULL);
    assert(file->ref_cnt > 0);
    file->ref_cnt--;
}

static void dispose_memory_map(MemoryMap * map) {
    int i;

    for (i = 0; i < map->region_cnt; i++) {
        MemoryRegion * r = map->regions + i;
        assert(r->file == NULL);
        loc_free(r->file_name);
    }
    loc_free(map->regions);
    loc_free(map);
}

#if !defined(_WRS_KERNEL)
static void event_context_changed_or_exited(Context * ctx, void * client_data) {
    if (ctx->memory_map == NULL) return;
    dispose_memory_map((MemoryMap *)ctx->memory_map);
    ctx->memory_map = NULL;
}

static MemoryMap * get_memory_map(Context * ctx) {
    char maps_file_name[FILE_PATH_SIZE];
    MemoryMap * map;
    FILE * file;

    assert(ctx->pid == ctx->mem);
    if (ctx->memory_map != NULL) return (MemoryMap *)ctx->memory_map;
    if (!context_listener_added) {
        static ContextEventListener listener = {
            NULL,
            event_context_changed_or_exited,
            NULL,
            NULL,
            event_context_changed_or_exited
        };
        add_context_event_listener(&listener, NULL);
        context_listener_added = 1;
    }

    map = loc_alloc_zero(sizeof(MemoryMap));
    snprintf(maps_file_name, sizeof(maps_file_name), "/proc/%d/maps", ctx->pid);
    if ((file = fopen(maps_file_name, "r")) == NULL) {
        map->error = errno;
    }
    else {
        for (;;) {
            unsigned long addr0 = 0;
            unsigned long addr1 = 0;
            unsigned long offset = 0;
            unsigned long inode = 0;
            char permissions[16];
            char device[16];
            char file_name[FILE_PATH_SIZE];
            MemoryRegion * r = NULL;

            int cnt = fscanf(file, "%lx-%lx %s %lx %s %lx",
                &addr0, &addr1, permissions, &offset, device, &inode);

            file_name[0] = 0;
            if (cnt > 0 && cnt != EOF) {
                cnt += fscanf(file, " %[^\n]\n", file_name);
            }
            if (cnt == 0 || cnt == EOF) break;
            
            if (map->region_cnt >= map->region_max) {
                map->region_max += 8;
                map->regions = (MemoryRegion *)loc_realloc(map->regions, sizeof(MemoryRegion) * map->region_max);
            }
            r = map->regions + map->region_cnt++;
            memset(r, 0, sizeof(MemoryRegion));
            r->addr = addr0;
            r->size = addr1 - addr0;
            if (inode != 0 && file_name[0]) r->file_name = loc_strdup(file_name);
        }

        fclose(file);
    }
    ctx->memory_map = map;
    return map;
}
#endif

ELF_File * elf_list_first(Context * ctx, ContextAddress addr0, ContextAddress addr1) {
#if !defined(_WRS_KERNEL)
    int i;
    MemoryMap * map = NULL;

    if (ctx->pid != ctx->mem) ctx = ctx->parent;
    assert(ctx->pid == ctx->mem);
    ctx->elf_list_addr0 = addr0;
    ctx->elf_list_addr1 = addr1;
    map = get_memory_map(ctx);
    if (map == NULL) return NULL;
    for (i = 0; i < map->region_cnt; i++) {
        MemoryRegion * r = map->regions + i;
        if (r->addr <= addr1 && r->addr + r->size >= addr0) {
            assert(r->file == NULL);
            if (r->file_name != NULL) {
                r->file = elf_open(r->file_name);
                if (r->file != NULL) {
                    assert(!r->file->listed);
                    r->file->listed = 1;
                    ctx->elf_list_pos = i + 1;
                    return r->file;
                }
            }
        }
    }
#endif
    errno = 0;
    return NULL;
}

ELF_File * elf_list_next(Context * ctx) {
#if !defined(_WRS_KERNEL)
    int i;
    MemoryMap * map = NULL;

    if (ctx->pid != ctx->mem) ctx = ctx->parent;
    assert(ctx->pid == ctx->mem);
    map = (MemoryMap *)ctx->memory_map;
    for (i = ctx->elf_list_pos; i < map->region_cnt; i++) {
        MemoryRegion * r = map->regions + i;
        if (r->addr <= ctx->elf_list_addr1 && r->addr + r->size >= ctx->elf_list_addr0) {
            assert(r->file == NULL);
            if (r->file_name != NULL) {
                r->file = elf_open(r->file_name);
                if (r->file != NULL && !r->file->listed) {
                    r->file->listed = 1;
                    ctx->elf_list_pos = i + 1;
                    return r->file;
                }
            }
        }
    }
#endif
    errno = 0;
    return NULL;
}

void elf_list_done(Context * ctx) {
#if !defined(_WRS_KERNEL)
    int i;
    MemoryMap * map = NULL;

    if (ctx->pid != ctx->mem) ctx = ctx->parent;
    assert(ctx->pid == ctx->mem);
    map = (MemoryMap *)ctx->memory_map;
    if (map == NULL) return;
    for (i = 0; i < map->region_cnt; i++) {
        MemoryRegion * r = map->regions + i;
        if (r->file != NULL) {
            r->file->listed = 0;
            elf_close(r->file);
            r->file = NULL;
        }
    }
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

void elf_add_close_listener(ELFCloseListener listener) {
    if (listeners_cnt >= listeners_max) {
        listeners_max = listeners_max == 0 ? 16 : listeners_max * 2;
        listeners = (ELFCloseListener *)loc_realloc(listeners, sizeof(ELFCloseListener) * listeners_max);
    }
    listeners[listeners_cnt++] = listener;
}

#endif
