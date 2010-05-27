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
#include <services/tcf_elf.h>
#include <framework/myalloc.h>
#include <framework/exceptions.h>
#include <services/memorymap.h>
#include <framework/events.h>
#include <framework/trace.h>

#if defined(_WRS_KERNEL)
#elif defined(WIN32)
#else
#  include <sys/mman.h>
#  define USE_MMAP
#endif

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

static Context *      elf_list_ctx;
static int            elf_list_pos;
static ContextAddress elf_list_addr0;
static ContextAddress elf_list_addr1;
static MemoryRegion * elf_list_regions;
static unsigned       elf_list_region_cnt;
static ELF_File **    elf_list_files;


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
            ELF_Section * s = file->sections + n;
#ifdef USE_MMAP
            if (s->mmap_addr != NULL) {
                s->data = NULL;
                munmap(s->mmap_addr, s->mmap_size);
            }
#endif
            loc_free(s->data);
        }
        loc_free(file->sections);
    }
    loc_free(file->pheaders);
    loc_free(file->str_pool);
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
        if (file->ref_cnt == 0 && file->age > MAX_FILE_AGE) {
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
    else {
        while (inodes != NULL) {
            FileINode * n = inodes;
            inodes = n->next;
            loc_free(n->name);
            loc_free(n);
        }
    }
}

static ino_t add_ino(char * fnm, ino_t ino) {
    FileINode * n = (FileINode *)loc_alloc_zero(sizeof(*n));
    n->next = inodes;
    n->name = loc_strdup(fnm);
    n->ino = ino;
    inodes = n;
    return ino;
}

static ino_t elf_ino(char * fnm) {
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

static ELF_File * find_open_file(dev_t dev, ino_t ino, int64_t mtime) {
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
            file->ref_cnt++;
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

ELF_File * elf_open(char * file_name) {
    int error = 0;
    struct stat st;
    ELF_File * file = NULL;
    unsigned str_index = 0;

    if (!elf_cleanup_posted) {
        post_event_with_delay(elf_cleanup_event, NULL, 1000000);
        elf_cleanup_posted = 1;
    }

    if (stat(file_name, &st) < 0) return NULL;
    if (st.st_ino == 0 && (st.st_ino = elf_ino(file_name)) == 0) return NULL;

    file = find_open_file(st.st_dev, st.st_ino, st.st_mtime);
    if (file != NULL) return file;

    trace(LOG_ELF, "Create ELF file cache %s", file_name);

    file = (ELF_File *)loc_alloc_zero(sizeof(ELF_File));
    file->name = loc_strdup(file_name);
    file->dev = st.st_dev;
    file->ino = st.st_ino;
    file->mtime = st.st_mtime;
    if ((file->fd = open(file->name, O_RDONLY | O_BINARY, 0)) < 0) error = errno;

    if (error == 0) {
        Elf32_Ehdr hdr;
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
            file->byte_swap = 1;
            if ((*(char *)&file->byte_swap != 1) == file->big_endian) file->byte_swap = 0;
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
            if (error == 0 && hdr.e_type != ET_EXEC && hdr.e_type != ET_DYN && hdr.e_type != ET_REL) error = ERR_INV_FORMAT;
            if (error == 0 && hdr.e_version != EV_CURRENT) error = ERR_INV_FORMAT;
            if (error == 0 && hdr.e_shoff == 0) error = ERR_INV_FORMAT;
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
            if (error == 0 && hdr.e_type != ET_EXEC && hdr.e_type != ET_DYN && hdr.e_type != ET_REL) error = ERR_INV_FORMAT;
            if (error == 0 && hdr.e_version != EV_CURRENT) error = ERR_INV_FORMAT;
            if (error == 0 && hdr.e_shoff == 0) error = ERR_INV_FORMAT;
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
            error = ERR_INV_FORMAT;
        }
        if (error == 0 && str_index != 0 && str_index < file->section_cnt) {
            int rd = 0;
            ELF_Section * str = file->sections + str_index;
            file->str_pool = (char *)loc_alloc((size_t)str->size);
            if (str->offset == 0 || str->size == 0) error = ERR_INV_FORMAT;
            if (error == 0 && lseek(file->fd, str->offset, SEEK_SET) == (off_t)-1) error = errno;
            if (error == 0 && (rd = read(file->fd, file->str_pool, (size_t)str->size)) < 0) error = errno;
            if (error == 0 && rd != (int)str->size) error = ERR_INV_FORMAT;
            if (error == 0) {
                unsigned i;
                for (i = 1; i < file->section_cnt; i++) {
                    ELF_Section * sec = file->sections + i;
                    sec->name = file->str_pool + sec->name_offset;
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

void elf_close(ELF_File * file) {
    assert(file != NULL);
    assert(file->ref_cnt > 0);
    file->ref_cnt--;
}

static ELF_File * open_memory_region_file(unsigned n, int * error) {
    ELF_File * file = NULL;
    MemoryRegion * r = elf_list_regions + n;

    assert(n < elf_list_region_cnt);
    elf_list_files[n] = NULL;
    if (r->file_name == NULL) return NULL;
    file = find_open_file(r->dev, r->ino ? r->ino : elf_ino(r->file_name), 0);
    if (file == NULL) {
        file = elf_open(r->file_name);
        if (file == NULL && *error == 0) *error = errno;
        if (file == NULL) return NULL;
        if (r->dev != 0 && file->dev != r->dev) return NULL;
        if (r->ino != 0 && file->ino != r->ino) return NULL;
    }
    return elf_list_files[n] = file;
}

ELF_File * elf_list_first(Context * ctx, ContextAddress addr0, ContextAddress addr1) {
    unsigned i;
    int error = 0;

    if (elf_list_files != NULL) {
        loc_free(elf_list_files);
        elf_list_files = NULL;
    }
    elf_list_ctx = ctx;
    elf_list_addr0 = addr0;
    elf_list_addr1 = addr1;
    memory_map_get_regions(ctx, &elf_list_regions, &elf_list_region_cnt);
    if (elf_list_region_cnt == 0) return NULL;
    elf_list_files = (ELF_File **)loc_alloc_zero(sizeof(ELF_File *) * elf_list_region_cnt);
    for (i = 0; i < elf_list_region_cnt; i++) {
        MemoryRegion * r = elf_list_regions + i;
        if (r->addr <= addr1 && r->addr + r->size >= addr0) {
            if (r->file_name != NULL) {
                ELF_File * file = open_memory_region_file(i, &error);
                if (file != NULL) {
                    assert(!file->listed);
                    file->listed = 1;
                    elf_list_pos = i + 1;
                    return file;
                }
            }
        }
    }
    errno = error;
    return NULL;
}

ELF_File * elf_list_next(Context * ctx) {
    unsigned i;
    int error = 0;

    assert(ctx == elf_list_ctx);
    assert(elf_list_region_cnt > 0);
    assert(elf_list_files != NULL);

    for (i = elf_list_pos; i < elf_list_region_cnt; i++) {
        MemoryRegion * r = elf_list_regions + i;
        if (r->addr <= elf_list_addr1 && r->addr + r->size >= elf_list_addr0) {
            assert(elf_list_files[i] == NULL);
            if (r->file_name != NULL) {
                ELF_File * file = open_memory_region_file(i, &error);
                if (file != NULL && !file->listed) {
                    file->listed = 1;
                    elf_list_pos = i + 1;
                    return file;
                }
            }
        }
    }
    errno = error;
    return NULL;
}

void elf_list_done(Context * ctx) {
    unsigned i;

    assert(ctx == elf_list_ctx);
    for (i = 0; i < elf_list_region_cnt; i++) {
        ELF_File * file = elf_list_files[i];
        if (file != NULL) {
            file->listed = 0;
            elf_close(file);
        }
    }
    elf_list_ctx = NULL;
    elf_list_regions = NULL;
    elf_list_region_cnt = 0;
    if (elf_list_files != NULL) {
        loc_free(elf_list_files);
        elf_list_files = NULL;
    }
}

void elf_add_close_listener(ELFCloseListener listener) {
    if (listeners_cnt >= listeners_max) {
        listeners_max = listeners_max == 0 ? 16 : listeners_max * 2;
        listeners = (ELFCloseListener *)loc_realloc(listeners, sizeof(ELFCloseListener) * listeners_max);
    }
    listeners[listeners_cnt++] = listener;
}

ContextAddress elf_map_to_run_time_address(Context * ctx, ELF_File * file, ELF_Section * sec, ContextAddress addr) {
    unsigned i, j;
    MemoryRegion * regions;
    unsigned region_cnt;

    if (file->type == ET_EXEC) return addr;
    memory_map_get_regions(ctx, &regions, &region_cnt);
    for (i = 0; i < region_cnt; i++) {
        MemoryRegion * r = regions + i;
        ino_t ino = r->ino;
        if (r->file_name == NULL) continue;
        if (ino == 0 && (ino = elf_ino(r->file_name)) == 0) continue;
        if (file->dev == r->dev && file->ino == ino) {
            if (r->sect_name == NULL) {
                for (j = 0; j < file->pheader_cnt; j++) {
                    ELF_PHeader * p = file->pheaders + j;
                    if (p->type != PT_LOAD) continue;
                    if (p->offset < r->file_offs || p->offset + p->mem_size > r->file_offs + r->size) continue;
                    if (addr < p->address || addr >= p->address + p->mem_size) continue;
                    if (!(p->flags & PF_W) != !(r->flags & MM_FLAG_W)) continue;
                    return (ContextAddress)(addr - p->address + p->offset - r->file_offs + r->addr);
                }
            }
            else if (sec != NULL && strcmp(sec->name, r->sect_name) == 0) {
                return (ContextAddress)(addr - sec->addr + r->addr);
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
