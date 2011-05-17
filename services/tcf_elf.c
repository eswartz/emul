/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
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

#include <config.h>

#if ENABLE_ELF

#include <stddef.h>
#include <assert.h>
#include <string.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <stdio.h>
#include <framework/myalloc.h>
#include <framework/exceptions.h>
#include <framework/events.h>
#include <framework/trace.h>
#include <services/tcf_elf.h>
#include <services/memorymap.h>
#include <services/dwarfcache.h>
#include <services/pathmap.h>

#if defined(_WRS_KERNEL)
#elif defined(WIN32)
#else
#  include <sys/mman.h>
#  define USE_MMAP
#endif

#define MIN_FILE_AGE 3
#define MAX_FILE_AGE 60

typedef struct FileINode {
    struct FileINode * next;
    char * name;
    ino_t ino;
} FileINode;

static ELF_File * files = NULL;
static FileINode * inodes = NULL;
static ELFCloseListener * listeners = NULL;
static U4_T listeners_cnt = 0;
static U4_T listeners_max = 0;
static int elf_cleanup_posted = 0;
static ino_t elf_ino_cnt = 0;

static Context *    elf_list_ctx;
static unsigned     elf_list_pos;
static MemoryMap    elf_list;

static MemoryMap    elf_map;

void elf_add_close_listener(ELFCloseListener listener) {
    if (listeners_cnt >= listeners_max) {
        listeners_max = listeners_max == 0 ? 16 : listeners_max * 2;
        listeners = (ELFCloseListener *)loc_realloc(listeners, sizeof(ELFCloseListener) * listeners_max);
    }
    listeners[listeners_cnt++] = listener;
}

static void elf_dispose(ELF_File * file) {
    U4_T n;
    trace(LOG_ELF, "Dispose ELF file cache %s", file->name);
    for (n = 0; n < listeners_cnt; n++) {
        listeners[n](file);
    }
    if (file->fd >= 0) close(file->fd);
    if (file->sections != NULL) {
        for (n = 0; n < file->section_cnt; n++) {
            ELF_Section * s = file->sections + n;
#ifdef USE_MMAP
            if (s->mmap_addr != NULL) {
                s->data = NULL;
                munmap(s->mmap_addr, s->mmap_size);
            }
#endif
            loc_free(s->data);
            loc_free(s->symbols);
        }
        loc_free(file->sections);
    }
    release_error_report(file->error);
    loc_free(file->pheaders);
    loc_free(file->str_pool);
    loc_free(file->debug_info_file_name);
    loc_free(file->name);
    loc_free(file);
}

static void elf_cleanup_event(void * arg) {
    ELF_File * prev = NULL;
    ELF_File * file = files;

    assert(elf_cleanup_posted);
    elf_cleanup_posted = 0;
    while (file != NULL) {
        file->age++;
        if (file->age > MAX_FILE_AGE || (file->age > MIN_FILE_AGE && list_is_empty(&context_root))) {
            ELF_File * next = file->next;
            elf_dispose(file);
            file = next;
            if (prev != NULL) prev->next = file;
            else files = file;
        }
        else {
            prev = file;
            file = file->next;
        }
    }

    file = files;
    while (file != NULL) {
        struct stat st;
        if (!file->mtime_changed && stat(file->name, &st) == 0) {
            if (st.st_ino == 0) st.st_ino = file->ino;
            if (file->dev == st.st_dev && file->ino == st.st_ino && file->mtime != st.st_mtime) {
                file->mtime_changed = 1;
            }
        }
        file = file->next;
    }

    if (files != NULL) {
        post_event_with_delay(elf_cleanup_event, NULL, 1000000);
        elf_cleanup_posted = 1;
    }
    else if (list_is_empty(&context_root)) {
        while (inodes != NULL) {
            FileINode * n = inodes;
            inodes = n->next;
            loc_free(n->name);
            loc_free(n);
        }
    }
}

static ino_t add_ino(const char * fnm, ino_t ino) {
    FileINode * n = (FileINode *)loc_alloc_zero(sizeof(*n));
    n->next = inodes;
    n->name = loc_strdup(fnm);
    n->ino = ino;
    inodes = n;
    return ino;
}

static ino_t elf_ino(const char * fnm) {
    /*
     * Number of the information node (the inode) for the file is used as file ID.
     * Since some file systems don't support inodes, this function is used in such cases
     * to generate virtual inode numbers to be used as file IDs.
     */
    char * abs = NULL;
    FileINode * n = inodes;
    while (n != NULL) {
        if (strcmp(n->name, fnm) == 0) return n->ino;
        n = n->next;
    }
    abs = canonicalize_file_name(fnm);
    if (abs == NULL) return add_ino(fnm, 0);
    n = inodes;
    while (n != NULL) {
        if (strcmp(n->name, abs) == 0) {
            free(abs);
            return add_ino(fnm, n->ino);
        }
        n = n->next;
    }
    if (elf_ino_cnt == 0) elf_ino_cnt++;
    add_ino(fnm, elf_ino_cnt);
    if (strcmp(abs, fnm) != 0) add_ino(abs, elf_ino_cnt);
    free(abs);
    return elf_ino_cnt++;
}

static ELF_File * find_open_file_by_inode(dev_t dev, ino_t ino, int64_t mtime) {
    ELF_File * prev = NULL;
    ELF_File * file = files;
    while (file != NULL) {
        if (file->dev == dev && file->ino == ino &&
            (mtime ? file->mtime == mtime : !file->mtime_changed)) {
            if (prev != NULL) {
                prev->next = file->next;
                file->next = files;
                files = file;
            }
            file->age = 0;
            return file;
        }
        prev = file;
        file = file->next;
    }
    return NULL;
}

static ELF_File * find_open_file_by_name(const char * name) {
    ELF_File * prev = NULL;
    ELF_File * file = files;
    while (file != NULL) {
        if (strcmp(name, file->name) == 0) {
            if (prev != NULL) {
                prev->next = file->next;
                file->next = files;
                files = file;
            }
            file->age = 0;
            return file;
        }
        prev = file;
        file = file->next;
    }
    return NULL;
}

void swap_bytes(void * buf, size_t size) {
    size_t i, j, n;
    char * p = (char *)buf;
    n = size >> 1;
    for (i = 0, j = size - 1; i < n; i++, j--) {
        char x = p[i];
        p[i] = p[j];
        p[j] = x;
    }
}

static char * get_debug_info_file_name(ELF_File * file, int * error) {
    unsigned idx;

    for (idx = 1; idx < file->section_cnt; idx++) {
        ELF_Section * sec = file->sections + idx;
        if (sec->size == 0) continue;
        if (sec->type == SHT_NOTE && (sec->flags & SHF_ALLOC)) {
            unsigned offs = 0;
            if (elf_load(sec) < 0) {
                *error = errno;
                return NULL;
            }
            while (offs < sec->size) {
                U4_T name_sz = *(U4_T *)((U1_T *)sec->data + offs);
                U4_T desc_sz = *(U4_T *)((U1_T *)sec->data + offs + 4);
                U4_T type = *(U4_T *)((U1_T *)sec->data + offs + 8);
                char * name = NULL;
                offs += 12;
                if (file->byte_swap) {
                    SWAP(name_sz);
                    SWAP(desc_sz);
                    SWAP(type);
                }
                name = (char *)((U1_T *)sec->data + offs);
                offs += name_sz;
                while (offs % 4 != 0) offs++;
                if (type == 3 && strcmp(name, "GNU") == 0) {
                    char fnm[FILE_PATH_SIZE];
                    struct stat buf;
                    char id[64];
                    size_t id_size = 0;
                    U1_T * desc = (U1_T *)sec->data + offs;
                    U4_T i = 0;
                    while (i < desc_sz) {
                        U1_T j = (desc[i] >> 4) & 0xf;
                        U1_T k = desc[i++] & 0xf;
                        id[id_size++] = j < 10 ? '0' + j : 'a' + j - 10;
                        id[id_size++] = k < 10 ? '0' + k : 'a' + k - 10;
                    }
                    id[id_size++] = 0;
                    trace(LOG_ELF, "Found GNU build ID %s", id);
                    snprintf(fnm, sizeof(fnm), "/usr/lib/debug/.build-id/%.2s/%s.debug", id, id + 2);
                    if (stat(fnm, &buf) == 0) return loc_strdup(fnm);
#if SERVICE_PathMap
                    {
                        char * lnm = path_map_to_local(NULL, fnm);
                        if (lnm != NULL) return loc_strdup(lnm);
                    }
#endif
                    return NULL;
                }
                offs += desc_sz;
                while (offs % 4 != 0) offs++;
            }
        }
        else if (sec->name != NULL && strcmp(sec->name, ".gnu_debuglink") == 0) {
            if (elf_load(sec) < 0) {
                *error = errno;
                return NULL;
            }
            else {
                /* TODO: check debug info CRC */
                char fnm[FILE_PATH_SIZE];
                struct stat buf;
                char * name = (char *)sec->data;
                int l = strlen(file->name);
                while (l > 0 && file->name[l - 1] != '/' && file->name[l - 1] != '\\') l--;
                if (strcmp(file->name + l, name) != 0) {
                    snprintf(fnm, sizeof(fnm), "%.*s%s", l, file->name, name);
                    if (stat(fnm, &buf) == 0) return loc_strdup(fnm);
                }
                snprintf(fnm, sizeof(fnm), "%.*s.debug/%s", l, file->name, name);
                if (stat(fnm, &buf) == 0) return loc_strdup(fnm);
                snprintf(fnm, sizeof(fnm), "/usr/lib/debug%.*s%s", l, file->name, name);
                if (stat(fnm, &buf) == 0) return loc_strdup(fnm);
#if SERVICE_PathMap
                {
                    char * lnm = path_map_to_local(NULL, fnm);
                    if (lnm != NULL) return loc_strdup(lnm);
                }
#endif
                return NULL;
            }
        }
    }
    return NULL;
}

static ELF_File * create_elf_cache(const char * file_name) {
    struct stat st;
    int error = 0;
    ELF_File * file = NULL;
    unsigned str_index = 0;

    trace(LOG_ELF, "Create ELF file cache %s", file_name);

    file = (ELF_File *)loc_alloc_zero(sizeof(ELF_File));
    file->name = loc_strdup(file_name);
    file->fd = -1;

    if (stat(file_name, &st) < 0) {
        error = errno;
        memset(&st, 0, sizeof(st));
    }
    else if (st.st_ino == 0) {
        st.st_ino = elf_ino(file_name);
    }

    file->dev = st.st_dev;
    file->ino = st.st_ino;
    file->mtime = st.st_mtime;

    if (error == 0 && (file->fd = open(file->name, O_RDONLY | O_BINARY, 0)) < 0) error = errno;

    if (error == 0) {
        Elf32_Ehdr hdr;
        memset(&hdr, 0, sizeof(hdr));
        if (read(file->fd, (char *)&hdr, sizeof(hdr)) < 0) error = errno;
        if (error == 0 && strncmp((char *)hdr.e_ident, ELFMAG, SELFMAG) != 0) {
            error = set_errno(ERR_INV_FORMAT, "Unsupported ELF identification code");
        }
        if (error == 0) {
            if (hdr.e_ident[EI_DATA] == ELFDATA2LSB) {
                file->big_endian = 0;
            }
            else if (hdr.e_ident[EI_DATA] == ELFDATA2MSB) {
                file->big_endian = 1;
            }
            else {
                error = set_errno(ERR_INV_FORMAT, "Invalid ELF data encoding ID");
            }
            file->byte_swap = file->big_endian != big_endian_host();
        }
        if (error != 0) {
            /* Nothing */
        }
        else if (hdr.e_ident[EI_CLASS] == ELFCLASS32) {
            if (file->byte_swap) {
                SWAP(hdr.e_type);
                SWAP(hdr.e_machine);
                SWAP(hdr.e_version);
                SWAP(hdr.e_entry);
                SWAP(hdr.e_phoff);
                SWAP(hdr.e_shoff);
                SWAP(hdr.e_flags);
                SWAP(hdr.e_ehsize);
                SWAP(hdr.e_phentsize);
                SWAP(hdr.e_phnum);
                SWAP(hdr.e_shentsize);
                SWAP(hdr.e_shnum);
                SWAP(hdr.e_shstrndx);
            }
            file->type = hdr.e_type;
            file->machine = hdr.e_machine;
            file->os_abi = hdr.e_ident[EI_OSABI];
            if (error == 0 && hdr.e_type != ET_EXEC && hdr.e_type != ET_DYN && hdr.e_type != ET_REL) {
                error = set_errno(ERR_INV_FORMAT, "Invalid ELF type ID");
            }
            if (error == 0 && hdr.e_version != EV_CURRENT) {
                error = set_errno(ERR_INV_FORMAT, "Unsupported ELF version");
            }
            if (error == 0 && hdr.e_shoff == 0) {
                error = set_errno(ERR_INV_FORMAT, "Invalid section header table's file offset");
            }
            if (error == 0 && lseek(file->fd, hdr.e_shoff, SEEK_SET) == (off_t)-1) error = errno;
            if (error == 0) {
                unsigned cnt = 0;
                file->sections = (ELF_Section *)loc_alloc_zero(sizeof(ELF_Section) * hdr.e_shnum);
                file->section_cnt = hdr.e_shnum;
                while (error == 0 && cnt < hdr.e_shnum) {
                    int rd = 0;
                    Elf32_Shdr shdr;
                    memset(&shdr, 0, sizeof(shdr));
                    if (error == 0 && sizeof(shdr) < hdr.e_shentsize) error = ERR_INV_FORMAT;
                    if (error == 0 && (rd = read(file->fd, (char *)&shdr, hdr.e_shentsize)) < 0) error = errno;
                    if (error == 0 && rd != hdr.e_shentsize) error = ERR_INV_FORMAT;
                    if (error == 0) {
                        ELF_Section * sec = file->sections + cnt;
                        if (file->byte_swap) {
                            SWAP(shdr.sh_name);
                            SWAP(shdr.sh_type);
                            SWAP(shdr.sh_flags);
                            SWAP(shdr.sh_addr);
                            SWAP(shdr.sh_offset);
                            SWAP(shdr.sh_size);
                            SWAP(shdr.sh_link);
                            SWAP(shdr.sh_info);
                            SWAP(shdr.sh_addralign);
                            SWAP(shdr.sh_entsize);
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
                        sec->entsize = shdr.sh_entsize;
                        cnt++;
                    }
                }
            }
            if (error == 0 && lseek(file->fd, hdr.e_phoff, SEEK_SET) == (off_t)-1) error = errno;
            if (error == 0) {
                unsigned cnt = 0;
                file->pheaders = (ELF_PHeader *)loc_alloc_zero(sizeof(ELF_PHeader) * hdr.e_phnum);
                file->pheader_cnt = hdr.e_phnum;
                while (error == 0 && cnt < hdr.e_phnum) {
                    int rd = 0;
                    Elf32_Phdr phdr;
                    memset(&phdr, 0, sizeof(phdr));
                    if (error == 0 && sizeof(phdr) < hdr.e_phentsize) error = ERR_INV_FORMAT;
                    if (error == 0 && (rd = read(file->fd, (char *)&phdr, hdr.e_phentsize)) < 0) error = errno;
                    if (error == 0 && rd != hdr.e_phentsize) error = ERR_INV_FORMAT;
                    if (error == 0) {
                        ELF_PHeader * p = file->pheaders + cnt;
                        if (file->byte_swap) {
                            SWAP(phdr.p_type);
                            SWAP(phdr.p_offset);
                            SWAP(phdr.p_vaddr);
                            SWAP(phdr.p_paddr);
                            SWAP(phdr.p_filesz);
                            SWAP(phdr.p_memsz);
                            SWAP(phdr.p_flags);
                            SWAP(phdr.p_align);
                        }
                        p->type = phdr.p_type;
                        p->offset = phdr.p_offset;
                        p->address = phdr.p_vaddr;
                        p->file_size = phdr.p_filesz;
                        p->mem_size = phdr.p_memsz;
                        p->flags = phdr.p_flags;
                        p->align = phdr.p_align;
                        cnt++;
                    }
                }
            }
            str_index = hdr.e_shstrndx;
        }
        else if (hdr.e_ident[EI_CLASS] == ELFCLASS64) {
            Elf64_Ehdr hdr;
            file->elf64 = 1;
            memset(&hdr, 0, sizeof(hdr));
            if (error == 0 && lseek(file->fd, 0, SEEK_SET) == (off_t)-1) error = errno;
            if (error == 0 && read(file->fd, (char *)&hdr, sizeof(hdr)) < 0) error = errno;
            if (file->byte_swap) {
                SWAP(hdr.e_type);
                SWAP(hdr.e_machine);
                SWAP(hdr.e_version);
                SWAP(hdr.e_entry);
                SWAP(hdr.e_phoff);
                SWAP(hdr.e_shoff);
                SWAP(hdr.e_flags);
                SWAP(hdr.e_ehsize);
                SWAP(hdr.e_phentsize);
                SWAP(hdr.e_phnum);
                SWAP(hdr.e_shentsize);
                SWAP(hdr.e_shnum);
                SWAP(hdr.e_shstrndx);
            }
            file->type = hdr.e_type;
            file->machine = hdr.e_machine;
            file->os_abi = hdr.e_ident[EI_OSABI];
            if (error == 0 && hdr.e_type != ET_EXEC && hdr.e_type != ET_DYN && hdr.e_type != ET_REL) {
                error = set_errno(ERR_INV_FORMAT, "Invalid ELF type ID");
            }
            if (error == 0 && hdr.e_version != EV_CURRENT) {
                error = set_errno(ERR_INV_FORMAT, "Unsupported ELF version");
            }
            if (error == 0 && hdr.e_shoff == 0) {
                error = set_errno(ERR_INV_FORMAT, "Invalid section header table's file offset");
            }
            if (error == 0 && lseek(file->fd, hdr.e_shoff, SEEK_SET) == (off_t)-1) error = errno;
            if (error == 0) {
                unsigned cnt = 0;
                file->sections = (ELF_Section *)loc_alloc_zero(sizeof(ELF_Section) * hdr.e_shnum);
                file->section_cnt = hdr.e_shnum;
                while (error == 0 && cnt < hdr.e_shnum) {
                    int rd = 0;
                    Elf64_Shdr shdr;
                    memset(&shdr, 0, sizeof(shdr));
                    if (error == 0 && sizeof(shdr) < hdr.e_shentsize) error = ERR_INV_FORMAT;
                    if (error == 0 && (rd = read(file->fd, (char *)&shdr, hdr.e_shentsize)) < 0) error = errno;
                    if (error == 0 && rd != hdr.e_shentsize) error = ERR_INV_FORMAT;
                    if (error == 0) {
                        ELF_Section * sec = file->sections + cnt;
                        if (file->byte_swap) {
                            SWAP(shdr.sh_name);
                            SWAP(shdr.sh_type);
                            SWAP(shdr.sh_flags);
                            SWAP(shdr.sh_addr);
                            SWAP(shdr.sh_offset);
                            SWAP(shdr.sh_size);
                            SWAP(shdr.sh_link);
                            SWAP(shdr.sh_info);
                            SWAP(shdr.sh_addralign);
                            SWAP(shdr.sh_entsize);
                        }
                        sec->file = file;
                        sec->index = cnt;
                        sec->name_offset = shdr.sh_name;
                        sec->type = shdr.sh_type;
                        sec->offset = shdr.sh_offset;
                        sec->size = shdr.sh_size;
                        sec->flags = (U4_T)shdr.sh_flags;
                        sec->addr = shdr.sh_addr;
                        sec->link = shdr.sh_link;
                        sec->info = shdr.sh_info;
                        sec->entsize = (U4_T)shdr.sh_entsize;
                        cnt++;
                    }
                }
            }
            if (error == 0 && lseek(file->fd, hdr.e_phoff, SEEK_SET) == (off_t)-1) error = errno;
            if (error == 0) {
                unsigned cnt = 0;
                file->pheaders = (ELF_PHeader *)loc_alloc_zero(sizeof(ELF_PHeader) * hdr.e_phnum);
                file->pheader_cnt = hdr.e_phnum;
                while (error == 0 && cnt < hdr.e_phnum) {
                    int rd = 0;
                    Elf64_Phdr phdr;
                    memset(&phdr, 0, sizeof(phdr));
                    if (error == 0 && sizeof(phdr) < hdr.e_phentsize) error = ERR_INV_FORMAT;
                    if (error == 0 && (rd = read(file->fd, (char *)&phdr, hdr.e_phentsize)) < 0) error = errno;
                    if (error == 0 && rd != hdr.e_phentsize) error = ERR_INV_FORMAT;
                    if (error == 0) {
                        ELF_PHeader * p = file->pheaders + cnt;
                        if (file->byte_swap) {
                            SWAP(phdr.p_type);
                            SWAP(phdr.p_offset);
                            SWAP(phdr.p_vaddr);
                            SWAP(phdr.p_paddr);
                            SWAP(phdr.p_filesz);
                            SWAP(phdr.p_memsz);
                            SWAP(phdr.p_flags);
                            SWAP(phdr.p_align);
                        }
                        p->type = phdr.p_type;
                        p->offset = phdr.p_offset;
                        p->address = phdr.p_vaddr;
                        p->file_size = phdr.p_filesz;
                        p->mem_size = phdr.p_memsz;
                        p->flags = phdr.p_flags;
                        p->align = (U4_T)phdr.p_align;
                        cnt++;
                    }
                }
            }
            str_index = hdr.e_shstrndx;
        }
        else {
            error = set_errno(ERR_INV_FORMAT, "Invalid ELF class ID");
        }
        if (error == 0 && str_index != 0 && str_index < file->section_cnt) {
            int rd = 0;
            ELF_Section * str = file->sections + str_index;
            file->str_pool = (char *)loc_alloc((size_t)str->size);
            if (str->offset == 0 || str->size == 0) error = set_errno(ERR_INV_FORMAT, "Invalid ELF string pool offset or size");
            if (error == 0 && lseek(file->fd, str->offset, SEEK_SET) == (off_t)-1) error = errno;
            if (error == 0 && (rd = read(file->fd, file->str_pool, (size_t)str->size)) < 0) error = errno;
            if (error == 0 && rd != (int)str->size) error = set_errno(ERR_INV_FORMAT, "Cannot read ELF string pool");
            if (error == 0) {
                unsigned i;
                for (i = 1; i < file->section_cnt; i++) {
                    ELF_Section * sec = file->sections + i;
                    sec->name = file->str_pool + sec->name_offset;
                }
            }
        }
    }
    if (error == 0) {
        file->debug_info_file_name = get_debug_info_file_name(file, &error);
        if (file->debug_info_file_name) trace(LOG_ELF, "Debug info file found %s", file->debug_info_file_name);
    }
    if (error != 0) {
        trace(LOG_ELF, "Error opening ELF file: %d %s", error, errno_to_str(error));
        file->error = get_error_report(error);
    }
    if (!elf_cleanup_posted) {
        post_event_with_delay(elf_cleanup_event, NULL, 1000000);
        elf_cleanup_posted = 1;
    }
    file->next = files;
    return files = file;
}

ELF_File * elf_open(const char * file_name) {
    ELF_File * file = find_open_file_by_name(file_name);
    if (file == NULL) file = create_elf_cache(file_name);
    if (file->error == NULL) return file;
    set_error_report_errno(file->error);
    return NULL;
}

int elf_load(ELF_Section * s) {

    if (s->data != NULL) return 0;
    if (s->size == 0) return 0;

    s->relocate = 0;
    if (s->type != SHT_REL && s->type != SHT_REL && s->type != SHT_RELA) {
        unsigned i;
        for (i = 1; i < s->file->section_cnt; i++) {
            ELF_Section * r = s->file->sections + i;
            if (r->entsize == 0 || r->size == 0) continue;
            if (r->type != SHT_REL && r->type != SHT_RELA) continue;
            if (r->info == s->index) {
                s->relocate = 1;
                break;
            }
        }
    }

#ifdef USE_MMAP
    {
        long page = sysconf(_SC_PAGE_SIZE);
        off_t offs = (off_t)s->offset;
        offs -= offs % page;
        s->mmap_size = (size_t)(s->offset - offs) + s->size;
        s->mmap_addr = mmap(0, s->mmap_size, PROT_READ, MAP_PRIVATE, s->file->fd, offs);
        if (s->mmap_addr == MAP_FAILED) {
            s->mmap_addr = NULL;
            trace(LOG_ALWAYS, "Cannot mmap section %s in ELF file %s", s->name, s->file->name);
        }
        else {
            s->data = (char *)s->mmap_addr + (size_t)(s->offset - offs);
            trace(LOG_ELF, "Section %s in ELF file %s is mapped to %#lx", s->name, s->file->name, s->data);
        }
    }
#endif

    if (s->data == NULL) {
        ELF_File * file = s->file;
        if (lseek(file->fd, s->offset, SEEK_SET) == (off_t)-1) return -1;
        s->data = loc_alloc((size_t)s->size);
        if (read(file->fd, s->data, (size_t)s->size) < 0) {
            int err = errno;
            loc_free(s->data);
            s->data = NULL;
            errno = err;
            return -1;
        }
        trace(LOG_ELF, "Section %s in ELF file %s is loaded", s->name, s->file->name);
    }
    return 0;
}

static ELF_File * open_memory_region_file(MemoryRegion * r, int * error) {
    ELF_File * file = NULL;
    ino_t ino = r->ino;
    dev_t dev = r->dev;

    if (r->file_name == NULL) return NULL;
    if (dev != 0) {
        if (ino == 0) ino = elf_ino(r->file_name);
        if (ino != 0) file = find_open_file_by_inode(dev, ino, 0);
    }
    if (file == NULL) file = find_open_file_by_name(r->file_name);
    if (file == NULL) file = create_elf_cache(r->file_name);
    if (r->dev != 0 && file->dev != r->dev) return NULL;
    if (r->ino != 0 && file->ino != r->ino) return NULL;
    if (file->error == NULL) return file;
    if (error != NULL && *error == 0) {
        int no = set_error_report_errno(file->error);
        if (get_error_code(no) != ERR_INV_FORMAT) *error = no;
    }
    return NULL;
}

static void add_region(MemoryMap * map, MemoryRegion * r) {
    if (map->region_cnt >= map->region_max) {
        map->region_max += 8;
        map->regions = (MemoryRegion *)loc_realloc(map->regions, sizeof(MemoryRegion ) * map->region_max);
    }
    map->regions[map->region_cnt++] = *r;
}

static void search_regions(MemoryMap * map, ContextAddress addr0, ContextAddress addr1, MemoryMap * res) {
    unsigned i;
    for (i = 0; i < map->region_cnt; i++) {
        MemoryRegion * r = map->regions + i;
        if (r->file_name == NULL) continue;
        if (r->addr == 0 && r->size == 0 && r->file_offs == 0 && r->sect_name == NULL) {
            int error = 0;
            ELF_File * file = open_memory_region_file(r, &error);
            if (file != NULL) {
                unsigned j;
                for (j = 0; j < file->pheader_cnt; j++) {
                    ELF_PHeader * p = file->pheaders + j;
                    if (p->type != PT_LOAD) continue;
                    if (p->address <= addr1 && p->address + p->mem_size > addr0) {
                        MemoryRegion x;
                        memset(&x, 0, sizeof(x));
                        x.addr = p->address;
                        x.size = p->mem_size;
                        x.dev = file->dev;
                        x.ino = file->ino;
                        x.file_name = file->name;
                        x.file_offs = p->offset;
                        x.flags = MM_FLAG_R | MM_FLAG_W | MM_FLAG_X;
                        add_region(res, &x);;
                    }
                }
            }
        }
        else if (r->addr <= addr1 && r->addr + r->size > addr0) {
            add_region(res, r);;
        }
    }
}

static int get_map(Context * ctx, ContextAddress addr0, ContextAddress addr1, MemoryMap * map) {
    MemoryMap * client_map = NULL;
    MemoryMap * target_map = NULL;

    map->region_cnt = 0;
    ctx = context_get_group(ctx, CONTEXT_GROUP_PROCESS);
    if (memory_map_get(ctx, &client_map, &target_map) < 0) return -1;
    search_regions(client_map, addr0, addr1, map);
    search_regions(target_map, addr0, addr1, map);
    return 0;
}

ELF_File * elf_open_inode(Context * ctx, dev_t dev, ino_t ino, int64_t mtime) {
    unsigned i;
    int error = 0;
    ELF_File * file = find_open_file_by_inode(dev, ino, mtime);
    if (file != NULL) {
        if (file->error == NULL) return file;
        set_error_report_errno(file->error);
        return NULL;
    }
    if (get_map(ctx, 0, ~(ContextAddress)0, &elf_map) < 0) return NULL;
    for (i = 0; i < elf_map.region_cnt; i++) {
        MemoryRegion * r = elf_map.regions + i;
        file = open_memory_region_file(r, &error);
        if (file == NULL) continue;
        if (file->dev == dev && file->ino == ino && file->mtime == mtime) return file;
        if (file->debug_info_file_name == NULL) continue;
        assert(!file->debug_info_file);
        file = elf_open(file->debug_info_file_name);
        if (file == NULL) {
            error = errno;
            continue;
        }
        if (file->dev == dev && file->ino == ino && file->mtime == mtime) return file;
    }
    if (error == 0) error = ENOENT;
    errno = error;
    return NULL;
}

ELF_File * elf_list_first(Context * ctx, ContextAddress addr_min, ContextAddress addr_max) {
    elf_list_ctx = ctx;
    elf_list_pos = 0;
    if (get_map(ctx, addr_min, addr_max, &elf_list) < 0) return NULL;
    if (elf_list.region_cnt > 0) {
        ELF_File * f = files;
        while (f != NULL) {
            f->listed = 0;
            f = f->next;
        }
        return elf_list_next(ctx);
    }
    errno = 0;
    return NULL;
}

ELF_File * elf_list_next(Context * ctx) {
    assert(ctx == elf_list_ctx);
    assert(elf_list.region_cnt > 0);
    while (elf_list_pos < elf_list.region_cnt) {
        int error = 0;
        ELF_File * file = open_memory_region_file(elf_list.regions + elf_list_pos++, &error);
        if (file != NULL) {
            if (file->listed) continue;
            file->listed = 1;
            return file;
        }
        if (error) {
            errno = error;
            return NULL;
        }
    }
    errno = 0;
    return NULL;
}

void elf_list_done(Context * ctx) {
    assert(ctx == elf_list_ctx);
    elf_list_ctx = NULL;
    elf_list_pos = 0;
    elf_list.region_cnt = 0;
}

UnitAddressRange * elf_find_unit(Context * ctx, ContextAddress addr_min, ContextAddress addr_max, ContextAddress * range_rt_addr) {
    unsigned i, j;
    UnitAddressRange * range = NULL;
    int error = 0;

    if (get_map(ctx, addr_min, addr_max, &elf_map) < 0) return NULL;
    for (i = 0; range == NULL && i < elf_map.region_cnt; i++) {
        ContextAddress link_addr_min, link_addr_max;
        MemoryRegion * r = elf_map.regions + i;
        ELF_File * file = NULL;
        assert(r->addr <= addr_max);
        assert(r->addr + r->size > addr_min);
        file = open_memory_region_file(r, &error);
        if (error) exception(error);
        if (r->sect_name == NULL) {
            for (j = 0; range == NULL && j < file->pheader_cnt; j++) {
                U8_T offs_min = 0;
                U8_T offs_max = 0;
                ELF_PHeader * p = file->pheaders + j;
                if (p->type != PT_LOAD) continue;
                if (r->flags) {
                    if ((p->flags & PF_R) && !(r->flags & MM_FLAG_R)) continue;
                    if ((p->flags & PF_W) && !(r->flags & MM_FLAG_W)) continue;
                    if ((p->flags & PF_X) && !(r->flags & MM_FLAG_X)) continue;
                }
                offs_min = addr_min - r->addr + r->file_offs;
                offs_max = addr_max - r->addr + r->file_offs;
                if (p->offset >= offs_max || p->offset + p->mem_size <= offs_min) continue;
                link_addr_min = offs_min - p->offset + p->address;
                link_addr_max = offs_max - p->offset + p->address;
                if (link_addr_min < p->address) link_addr_min = p->address;
                if (link_addr_max >= p->address + p->mem_size) link_addr_max = p->address + p->mem_size;
                range = find_comp_unit_addr_range(get_dwarf_cache(file), link_addr_min, link_addr_max);
                if (range == NULL && file->debug_info_file_name != NULL && !file->debug_info_file) {
                    ELF_File * debug = elf_open(file->debug_info_file_name);
                    if (debug == NULL) exception(errno);
                    debug->debug_info_file = 1;
                    if (j < debug->pheader_cnt) {
                        p = debug->pheaders + j;
                        link_addr_min = offs_min - p->offset + p->address;
                        link_addr_max = offs_max - p->offset + p->address;
                        if (link_addr_min < p->address) link_addr_min = p->address;
                        if (link_addr_max >= p->address + p->mem_size) link_addr_max = p->address + p->mem_size;
                        range = find_comp_unit_addr_range(get_dwarf_cache(debug), link_addr_min, link_addr_max);
                    }
                }
                if (range != NULL && range_rt_addr != NULL) {
                    *range_rt_addr = range->mAddr - p->address + p->offset - r->file_offs + r->addr;
                }
            }
        }
        else {
            unsigned idx;
            for (idx = 1; range == NULL && idx < file->section_cnt; idx++) {
                ELF_Section * sec = file->sections + idx;
                if (sec->name != NULL && strcmp(sec->name, r->sect_name) == 0) {
                    link_addr_min = addr_min - r->addr + sec->addr;
                    link_addr_max = addr_max - r->addr + sec->addr;
                    if (link_addr_min < sec->addr) link_addr_min = sec->addr;
                    if (link_addr_max >= sec->addr + sec->size) link_addr_max = sec->addr + sec->size;
                    range = find_comp_unit_addr_range(get_dwarf_cache(file), link_addr_min, link_addr_max);
                    if (range != NULL && range_rt_addr != NULL) {
                        *range_rt_addr = range->mAddr - sec->addr + r->addr;
                    }
                }
            }
        }
    }
/* TODO: lazy reading of comp unit objects */
#if 0
    if (range != NULL) {
        load_comp_unit_children(range->mUnit);
        if (range->mUnit->mBaseTypes != NULL) {
            load_comp_unit_children(range->mUnit->mBaseTypes);
        }
    }
#endif
    return range;
}

ContextAddress elf_map_to_run_time_address(Context * ctx, ELF_File * file, ELF_Section * sec, ContextAddress addr) {
    unsigned i;

    /* Note: 'addr' is link-time address - it cannot be used as get_map() argument */
    if (get_map(ctx, 0, ~(ContextAddress)0, &elf_map) < 0) return 0;
    for (i = 0; i < elf_map.region_cnt; i++) {
        MemoryRegion * r = elf_map.regions + i;
        int same_file = 0;
        if (r->dev == 0) {
            same_file = strcmp(file->name, r->file_name) == 0;
        }
        else {
           ino_t ino = r->ino;
           if (ino == 0) ino = elf_ino(r->file_name);
           same_file = file->ino == ino && file->dev == r->dev;
        }
        if (!same_file) {
            /* Check if the memory map entry has a separate debug info file */
            int error = 0;
            ELF_File * exec = NULL;
            if (!file->debug_info_file) continue;
            exec = open_memory_region_file(r, &error);
            if (exec == NULL) continue;
            if (exec->debug_info_file_name == NULL) continue;
            if (strcmp(exec->debug_info_file_name, file->name) != 0) continue;
        }
        if (r->sect_name == NULL) {
            unsigned j;
            if (file->pheader_cnt == 0 && file->type == ET_EXEC) return addr;
            for (j = 0; j < file->pheader_cnt; j++) {
                U8_T offs;
                ELF_PHeader * p = file->pheaders + j;
                if (p->type != PT_LOAD) continue;
                if (addr < p->address || addr >= p->address + p->mem_size) continue;
                if (r->flags) {
                    if ((p->flags & PF_R) && !(r->flags & MM_FLAG_R)) continue;
                    if ((p->flags & PF_W) && !(r->flags & MM_FLAG_W)) continue;
                    if ((p->flags & PF_X) && !(r->flags & MM_FLAG_X)) continue;
                }
                offs = addr - p->address + p->offset;
                if (offs < r->file_offs || offs >= r->file_offs + r->size) continue;
                return (ContextAddress)(offs - r->file_offs + r->addr);
            }
        }
        else if (sec != NULL && strcmp(sec->name, r->sect_name) == 0) {
            return (ContextAddress)(addr - sec->addr + r->addr);
        }
    }
    return 0;
}

ContextAddress elf_map_to_link_time_address(Context * ctx, ContextAddress addr, ELF_File ** file, ELF_Section ** sec) {
    unsigned i;

    if (get_map(ctx, addr, addr, &elf_map) < 0) return 0;
    for (i = 0; i < elf_map.region_cnt; i++) {
        MemoryRegion * r = elf_map.regions + i;
        ELF_File * f = NULL;
        assert(r->addr <= addr);
        assert(r->addr + r->size > addr);
        f = open_memory_region_file(r, NULL);
        if (f == NULL) continue;
        if (r->sect_name == NULL) {
            unsigned j;
            if (f->pheader_cnt == 0 && f->type == ET_EXEC) {
                *file = f;
                for (j = 1; j < f->section_cnt; j++) {
                    ELF_Section * s = f->sections + j;
                    if ((s->flags & SHF_ALLOC) == 0) continue;
                    if (s->addr <= addr && s->addr + s->size > addr) {
                        *sec = s;
                        return addr;
                    }
                }
                *sec = NULL;
                return addr;
            }
            for (j = 0; j < f->pheader_cnt; j++) {
                U8_T offs = addr - r->addr + r->file_offs;
                ELF_PHeader * p = f->pheaders + j;
                if (p->type != PT_LOAD) continue;
                if (offs < p->offset || offs >= p->offset + p->mem_size) continue;
                if (r->flags) {
                    if ((p->flags & PF_R) && !(r->flags & MM_FLAG_R)) continue;
                    if ((p->flags & PF_W) && !(r->flags & MM_FLAG_W)) continue;
                    if ((p->flags & PF_X) && !(r->flags & MM_FLAG_X)) continue;
                }
                *file = f;
                addr = (ContextAddress)(offs - p->offset + p->address);
                for (j = 1; j < f->section_cnt; j++) {
                    ELF_Section * s = f->sections + j;
                    if ((s->flags & SHF_ALLOC) == 0) continue;
                    if (s->addr + s->size <= p->address) continue;
                    if (s->addr >= p->address + p->mem_size) continue;
                    if (s->addr <= addr && s->addr + s->size > addr) {
                        *sec = s;
                        return addr;
                    }
                }
                *sec = NULL;
                return addr;
            }
        }
        else {
            unsigned j;
            for (j = 1; j < f->section_cnt; j++) {
                ELF_Section * s = f->sections + j;
                if (strcmp(s->name, r->sect_name) == 0) {
                    *file = f;
                    *sec = s;
                    return (ContextAddress)(addr - r->addr + s->addr);
                }
            }
        }
    }
    return 0;
}

static int get_dynamic_tag(Context * ctx, ELF_File * file, int tag, ContextAddress * addr) {
    unsigned i, j;

    for (i = 1; i < file->section_cnt; i++) {
        ELF_Section * sec = file->sections + i;
        if (sec->size == 0) continue;
        if (sec->name == NULL) continue;
        if (strcmp(sec->name, ".dynamic") == 0) {
            ContextAddress sec_addr = elf_map_to_run_time_address(ctx, file, sec, (ContextAddress)sec->addr);
            if (elf_load(sec) < 0) return -1;
            if (file->elf64) {
                unsigned cnt = (unsigned)(sec->size / sizeof(Elf64_Dyn));
                for (j = 0; j < cnt; j++) {
                    Elf64_Dyn dyn = *((Elf64_Dyn *)sec->data + j);
                    if (file->byte_swap) SWAP(dyn.d_tag);
                    if (dyn.d_tag == DT_NULL) break;
                    if (dyn.d_tag == tag) {
                        if (context_read_mem(ctx, sec_addr + j * sizeof(dyn), &dyn, sizeof(dyn)) < 0) return -1;
                        if (file->byte_swap) {
                            SWAP(dyn.d_tag);
                            SWAP(dyn.d_un.d_ptr);
                        }
                        if (dyn.d_tag != tag) continue;
                        if (addr != NULL) *addr = (ContextAddress)dyn.d_un.d_ptr;
                        return 0;
                    }
                }
            }
            else {
                unsigned cnt = (unsigned)(sec->size / sizeof(Elf32_Dyn));
                for (j = 0; j < cnt; j++) {
                    Elf32_Dyn dyn = *((Elf32_Dyn *)sec->data + j);
                    if (file->byte_swap) SWAP(dyn.d_tag);
                    if (dyn.d_tag == DT_NULL) break;
                    if (dyn.d_tag == tag) {
                        if (context_read_mem(ctx, sec_addr + j * sizeof(dyn), &dyn, sizeof(dyn)) < 0) return -1;
                        if (file->byte_swap) {
                            SWAP(dyn.d_tag);
                            SWAP(dyn.d_un.d_ptr);
                        }
                        if (dyn.d_tag != tag) continue;
                        if (addr != NULL) *addr = (ContextAddress)dyn.d_un.d_ptr;
                        return 0;
                    }
                }
            }
        }
    }
    errno = ENOENT;
    return -1;
}

static int sym_name_cmp(const char * x, const char * y) {
    while (*x && *x == *y) {
        x++;
        y++;
    }
    if (*x == 0 && *y == 0) return 0;
    if (*x == '@' && *(x + 1) == '@' && *y == 0) return 0;
    if (*x < *y) return -1;
    return 1;
}

static int get_global_symbol_address(Context * ctx, ELF_File * file, const char * name, ContextAddress * addr) {
    unsigned i, j;

    for (i = 1; i < file->section_cnt; i++) {
        ELF_Section * sec = file->sections + i;
        if (sec->size == 0) continue;
        if (sec->name == NULL) continue;
        if (sec->type == SHT_SYMTAB) {
            ELF_Section * str = NULL;
            if (sec->link == 0 || sec->link >= file->section_cnt) {
                errno = EINVAL;
                return -1;
            }
            str = file->sections + sec->link;
            if (elf_load(sec) < 0) return -1;
            if (elf_load(str) < 0) return -1;
            if (file->elf64) {
                unsigned cnt = (unsigned)(sec->size / sizeof(Elf64_Sym));
                for (j = 0; j < cnt; j++) {
                    Elf64_Sym sym = *((Elf64_Sym *)sec->data + j);
                    if (ELF64_ST_BIND(sym.st_info) != STB_GLOBAL) continue;
                    if (file->byte_swap) SWAP(sym.st_name);
                    if (sym_name_cmp((char *)str->data + sym.st_name, name) != 0) continue;
                    switch (ELF64_ST_TYPE(sym.st_info)) {
                    case STT_OBJECT:
                    case STT_FUNC:
                        if (file->byte_swap) SWAP(sym.st_value);
                        *addr = elf_map_to_run_time_address(ctx, file, NULL, (ContextAddress)sym.st_value);
                        if (*addr != 0) return 0;
                    }
                }
            }
            else {
                unsigned cnt = (unsigned)(sec->size / sizeof(Elf32_Sym));
                for (j = 0; j < cnt; j++) {
                    Elf32_Sym sym = *((Elf32_Sym *)sec->data + j);
                    if (ELF32_ST_BIND(sym.st_info) != STB_GLOBAL) continue;
                    if (file->byte_swap) SWAP(sym.st_name);
                    if (sym_name_cmp((char *)str->data + sym.st_name, name) != 0) continue;
                    switch (ELF32_ST_TYPE(sym.st_info)) {
                    case STT_OBJECT:
                    case STT_FUNC:
                        if (file->byte_swap) SWAP(sym.st_value);
                        *addr = elf_map_to_run_time_address(ctx, file, NULL, (ContextAddress)sym.st_value);
                        if (*addr != 0) return 0;
                    }
                }
            }
        }
    }
    errno = ENOENT;
    return -1;
}

int elf_read_memory_word(Context * ctx, ELF_File * file, ContextAddress addr, ContextAddress * word) {
    U1_T buf[8];
    size_t size = file->elf64 ? 8 : 4;
    size_t i = 0;
    U8_T n = 0;

    if (context_read_mem(ctx, addr, buf, size) < 0) return -1;
    for (i = 0; i < size; i++) {
        n = (n << 8) | buf[file->big_endian ? i : size - i - 1];
    }
    *word = (ContextAddress)n;
    return 0;
}

ContextAddress elf_get_debug_structure_address(Context * ctx, ELF_File ** file_ptr) {
    ELF_File * file = NULL;
    ContextAddress addr = 0;

    for (file = elf_list_first(ctx, 0, ~(ContextAddress)0); file != NULL; file = elf_list_next(ctx)) {
        if (file->type != ET_EXEC) continue;
        if (file_ptr != NULL) *file_ptr = file;
#ifdef DT_MIPS_RLD_MAP
        if (get_dynamic_tag(ctx, file, DT_MIPS_RLD_MAP, &addr) == 0) {
            if (elf_read_memory_word(ctx, file, addr, &addr) < 0) continue;
            break;
        }
#endif
        if (get_dynamic_tag(ctx, file, DT_DEBUG, &addr) == 0) break;
        if (get_global_symbol_address(ctx, file, "_r_debug", &addr) == 0) break;
    }
    elf_list_done(ctx);

    return addr;
}

void ini_elf(void) {
}

#endif /* ENABLE_ELF */
