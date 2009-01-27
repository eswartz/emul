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

#if ENABLE_ELF

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
#  include <sys/mman.h>
#  define USE_MMAP
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
    int region_cnt;
    int region_max;
    MemoryRegion * regions;
};

#define MAX_CACHED_FILES 32
#define MAX_FILE_AGE 60

static ELF_File * files = NULL;
static ELFCloseListener * listeners = NULL;
static U4_T listeners_cnt = 0;
static U4_T listeners_max = 0;
static int context_listener_added = 0;
static int elf_cleanup_posted = 0;

static Context *      elf_list_ctx;
static int            elf_list_pos;
static ContextAddress elf_list_addr0;
static ContextAddress elf_list_addr1;


static void elf_dispose(ELF_File * file) {
    U4_T n;
    trace(LOG_ELF, "Dispose ELF file cache %s", file->name);
    assert(file->ref_cnt == 0);
    for (n = 0; n < listeners_cnt; n++) {
        listeners[n](file);
    }
    if (file->fd >= 0) close(file->fd);
    if (file->sections != NULL) {
        for (n = 0; n < file->section_cnt; n++) {
            ELF_Section * s = file->sections[n];
            if (s == NULL) continue;
#ifdef USE_MMAP
            if (s->mmap_addr != NULL) munmap(s->mmap_addr, s->mmap_size);
#endif
            loc_free(file->sections[n]);
        }
        loc_free(file->sections);
    }
    loc_free(file->str_pool);
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
        file->age++;
        if (file->ref_cnt == 0 && (file->age > MAX_FILE_AGE || cnt >= MAX_CACHED_FILES)) {
            ELF_File * next = file->next;
            elf_dispose(file);
            file = next;
            if (prev != NULL) prev->next = file;
            else files = file;
        }
        else {
            prev = file;
            file = file->next;
            cnt++;
        }
    }
    if (cnt > 0) {
        post_event_with_delay(elf_cleanup_event, NULL, 1000000);
        elf_cleanup_posted = 1;
    }
}

/* Swap bytes if ELF file endianness mismatch agent endianness */
static void swap_bytes(void * buf, size_t size) {
    size_t i;
    char * p = (char *)buf;
    for (i = 0; i < size / 2; i++) {
        char x = p[i];
        p[i] = p[size - i - 1];
        p[size - i - 1] = x;
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
            file->age = 0;
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
    if ((file->fd = open(file->name, O_RDONLY, 0)) < 0) error = errno;

    if (error == 0) {
        Elf32_Ehdr hdr;
        int swap = 0;
        memset(&hdr, 0, sizeof(hdr));
        if (read(file->fd, (char *)&hdr, sizeof(hdr)) < 0) error = errno;
        if (error == 0 && strncmp((char *)hdr.e_ident, ELFMAG, SELFMAG) != 0) error = ERR_INV_FORMAT;
        if (error == 0) {
            if (hdr.e_ident[EI_DATA] == ELFDATA2LSB) {
                file->big_endian = 0;
            }
            else if (hdr.e_ident[EI_DATA] == ELFDATA2MSB) {
                file->big_endian = 1;
            }
            else {
                error = ERR_INV_FORMAT;
            }
            swap = ((*(unsigned *)hdr.e_ident & 0xff) != ELFMAG0) != file->big_endian;
        }
        if (error != 0) {
            /* Nothing */
        }
        else if (hdr.e_ident[EI_CLASS] == ELFCLASS32) {
            if (error == 0 && swap) {
                swap_bytes(&hdr.e_type, sizeof(hdr.e_type));
                swap_bytes(&hdr.e_machine, sizeof(hdr.e_machine));
                swap_bytes(&hdr.e_version, sizeof(hdr.e_version));
                swap_bytes(&hdr.e_entry, sizeof(hdr.e_entry));
                swap_bytes(&hdr.e_phoff, sizeof(hdr.e_phoff));
                swap_bytes(&hdr.e_shoff, sizeof(hdr.e_shoff));
                swap_bytes(&hdr.e_flags, sizeof(hdr.e_flags));
                swap_bytes(&hdr.e_ehsize, sizeof(hdr.e_ehsize));
                swap_bytes(&hdr.e_phentsize, sizeof(hdr.e_phentsize));
                swap_bytes(&hdr.e_phnum, sizeof(hdr.e_phnum));
                swap_bytes(&hdr.e_shentsize, sizeof(hdr.e_shentsize));
                swap_bytes(&hdr.e_shnum, sizeof(hdr.e_shnum));
                swap_bytes(&hdr.e_shstrndx, sizeof(hdr.e_shstrndx));
            }
            if (error == 0 && hdr.e_type != ET_EXEC && hdr.e_type != ET_DYN) error = ERR_INV_FORMAT;
            if (error == 0 && hdr.e_version != EV_CURRENT) error = ERR_INV_FORMAT;
            if (error == 0 && hdr.e_shoff == 0) error = ERR_INV_FORMAT;
            if (error == 0 && lseek(file->fd, hdr.e_shoff, SEEK_SET) == (off_t)-1) error = errno;
            if (error == 0) {
                unsigned cnt = 0;
                file->sections = loc_alloc_zero(sizeof(ELF_Section *) * hdr.e_shnum);
                file->section_cnt = hdr.e_shnum;
                while (error == 0 && cnt < hdr.e_shnum) {
                    Elf32_Shdr shdr;
                    memset(&shdr, 0, sizeof(shdr));
                    if (error == 0 && read(file->fd, (char *)&shdr, hdr.e_shentsize) < 0) error = errno;
                    if (cnt == 0) {
                        file->sections[cnt++] = NULL;
                    }
                    else if (error == 0) {
                        ELF_Section * sec = loc_alloc_zero(sizeof(ELF_Section));
                        if (swap) {
                            swap_bytes(&shdr.sh_name, sizeof(shdr.sh_name));
                            swap_bytes(&shdr.sh_type, sizeof(shdr.sh_type));
                            swap_bytes(&shdr.sh_flags, sizeof(shdr.sh_flags));
                            swap_bytes(&shdr.sh_addr, sizeof(shdr.sh_addr));
                            swap_bytes(&shdr.sh_offset, sizeof(shdr.sh_offset));
                            swap_bytes(&shdr.sh_size, sizeof(shdr.sh_size));
                            swap_bytes(&shdr.sh_link, sizeof(shdr.sh_link));
                            swap_bytes(&shdr.sh_info, sizeof(shdr.sh_info));
                            swap_bytes(&shdr.sh_addralign, sizeof(shdr.sh_addralign));
                            swap_bytes(&shdr.sh_entsize, sizeof(shdr.sh_entsize));
                        }
                        sec->file = file;
                        sec->index = cnt;
                        sec->name_offset = shdr.sh_name;
                        sec->type = shdr.sh_type;
                        sec->offset = shdr.sh_offset;
                        sec->size = shdr.sh_size;
                        sec->flags = shdr.sh_flags;
                        sec->addr = shdr.sh_addr;
                        sec->link = shdr.sh_link;
                        sec->info = shdr.sh_info;
                        file->sections[cnt++] = sec;
                    }
                }
            }
        }
        else if (hdr.e_ident[EI_CLASS] == ELFCLASS64) {
            /* TODO ELF64 */
            error = ERR_INV_FORMAT;
        }
        else {
            error = ERR_INV_FORMAT;
        }
        if (error == 0) {
            ELF_Section * str = file->sections[hdr.e_shstrndx];
            if (str != NULL) {
                file->str_pool = loc_alloc((size_t)str->size);
                if (lseek(file->fd, str->offset, SEEK_SET) == (off_t)-1) error = errno;
                if (error == 0 && read(file->fd, file->str_pool, (size_t)str->size) < 0) error = errno;
                if (error == 0) {
                    unsigned i;
                    for (i = 1; i < file->section_cnt; i++) {
                        ELF_Section * sec = file->sections[i];
                        sec->name = file->str_pool + sec->name_offset;
                    }
                }
            }
        }
    }
    if (error != 0) {
        trace(LOG_ELF, "Error openning ELF file: %d %s", error, errno_to_str(error));
        elf_dispose(file);
        errno = error;
        return NULL;
    }
    file->ref_cnt = 1;
    file->next = files;
    return files = file;
}

int elf_load(ELF_Section * s) {
    if (s->data != NULL) return 0;
    if (s->size == 0) return 0;
#ifdef USE_MMAP
    {
        long page = sysconf(_SC_PAGE_SIZE);
        off_t offs = (off_t)s->offset;
        offs -= offs % page;
        s->mmap_size = (size_t)(s->offset - offs) + s->size;
        s->mmap_addr = mmap(0, s->mmap_size, PROT_READ, MAP_PRIVATE, s->file->fd, offs);
        if (s->mmap_addr == MAP_FAILED) {
            s->mmap_addr = NULL;
            return -1;
        }
        s->data = (char *)s->mmap_addr + (size_t)(s->offset - offs);
    }
    trace(LOG_ELF, "Section %s in ELF file %s is mapped to 0x%08x", s->name, s->file->name, s->data);
    return 0;
#else
    errno = ERR_UNSUPPORTED;
    return -1;
#endif
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

#if defined(_WRS_KERNEL)

static MemoryMap * get_memory_map(Context * ctx) {
    errno = 0;
    return NULL;
}

#else

static void event_context_changed_or_exited(Context * ctx, void * client_data) {
    if (ctx->memory_map == NULL) return;
    dispose_memory_map((MemoryMap *)ctx->memory_map);
    ctx->memory_map = NULL;
}

static MemoryMap * get_memory_map(Context * ctx) {
    char maps_file_name[FILE_PATH_SIZE];
    MemoryMap * map = NULL;
    FILE * file;

    if (ctx->pid != ctx->mem) ctx = ctx->parent;
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

    snprintf(maps_file_name, sizeof(maps_file_name), "/proc/%d/maps", ctx->pid);
    if ((file = fopen(maps_file_name, "r")) == NULL) return NULL;
    map = loc_alloc_zero(sizeof(MemoryMap));
    for (;;) {
        unsigned long addr0 = 0;
        unsigned long addr1 = 0;
        unsigned long offset = 0;
        unsigned long inode = 0;
        char permissions[16];
        char device[16];
        char file_name[FILE_PATH_SIZE];
        MemoryRegion * r = NULL;
        unsigned i = 0;

        int cnt = fscanf(file, "%lx-%lx %s %lx %s %lx",
            &addr0, &addr1, permissions, &offset, device, &inode);
        if (cnt == 0 || cnt == EOF) break;

        while (1) {
            int ch = fgetc(file);
            if (ch == '\n' || ch == EOF) break;
            if (i < FILE_PATH_SIZE - 1 && (ch != ' ' || i > 0)) {
                file_name[i++] = ch;
            }
        }
        file_name[i++] = 0;
        
        if (map->region_cnt >= map->region_max) {
            map->region_max += 8;
            map->regions = (MemoryRegion *)loc_realloc(map->regions, sizeof(MemoryRegion) * map->region_max);
        }
        r = map->regions + map->region_cnt++;
        memset(r, 0, sizeof(MemoryRegion));
        r->addr = addr0;
        r->size = addr1 - addr0;
        if (inode != 0 && file_name[0]) {
            r->file_name = loc_strdup(file_name);
        }
    }
    fclose(file);
    ctx->memory_map = map;
    return map;
}

#endif

ELF_File * elf_list_first(Context * ctx, ContextAddress addr0, ContextAddress addr1) {
    int i;
    MemoryMap * map = NULL;

    elf_list_ctx = ctx;
    elf_list_addr0 = addr0;
    elf_list_addr1 = addr1;
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
                    elf_list_pos = i + 1;
                    return r->file;
                }
            }
        }
    }
    errno = 0;
    return NULL;
}

ELF_File * elf_list_next(Context * ctx) {
    int i;
    MemoryMap * map = NULL;

    assert(ctx == elf_list_ctx);
    map = get_memory_map(ctx);
    if (map == NULL) return NULL;
    for (i = elf_list_pos; i < map->region_cnt; i++) {
        MemoryRegion * r = map->regions + i;
        if (r->addr <= elf_list_addr1 && r->addr + r->size >= elf_list_addr0) {
            assert(r->file == NULL);
            if (r->file_name != NULL) {
                r->file = elf_open(r->file_name);
                if (r->file != NULL && !r->file->listed) {
                    r->file->listed = 1;
                    elf_list_pos = i + 1;
                    return r->file;
                }
            }
        }
    }
    errno = 0;
    return NULL;
}

void elf_list_done(Context * ctx) {
    int i;
    MemoryMap * map = NULL;

    assert(ctx == elf_list_ctx);
    elf_list_ctx = NULL;
    map = get_memory_map(ctx);
    if (map == NULL) return;
    for (i = 0; i < map->region_cnt; i++) {
        MemoryRegion * r = map->regions + i;
        if (r->file != NULL) {
            r->file->listed = 0;
            elf_close(r->file);
            r->file = NULL;
        }
    }
}

void elf_add_close_listener(ELFCloseListener listener) {
    if (listeners_cnt >= listeners_max) {
        listeners_max = listeners_max == 0 ? 16 : listeners_max * 2;
        listeners = (ELFCloseListener *)loc_realloc(listeners, sizeof(ELFCloseListener) * listeners_max);
    }
    listeners[listeners_cnt++] = listener;
}

#endif
