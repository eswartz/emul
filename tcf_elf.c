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

#include "config.h"

#if ENABLE_ELF

#include <assert.h>
#include <string.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <stdio.h>
#include "tcf_elf.h"
#include "myalloc.h"
#include "exceptions.h"
#include "expressions.h"
#include "breakpoints.h"
#include "memorymap.h"
#include "events.h"
#include "trace.h"

#if defined(_WRS_KERNEL)
#elif defined(WIN32)
#else
#  include <sys/mman.h>
#  define USE_MMAP
#endif

#define MAX_CACHED_FILES 32
#define MAX_FILE_AGE 60

static ELF_File * files = NULL;
static ELFCloseListener * listeners = NULL;
static U4_T listeners_cnt = 0;
static U4_T listeners_max = 0;
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
            ELF_Section * s = file->sections + n;
#ifdef USE_MMAP
            if (s->mmap_addr != NULL) munmap(s->mmap_addr, s->mmap_size);
#endif
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
#define SWAP(x) swap_bytes(&(x), sizeof(x))
static void swap_bytes(void * buf, size_t size) {
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
    ELF_File * prev = NULL;
    ELF_File * file = files;
    unsigned str_index = 0;

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
            if (error == 0 && hdr.e_type != ET_EXEC && hdr.e_type != ET_DYN) error = ERR_INV_FORMAT;
            if (hdr.e_type != ET_EXEC) file->pic = 1;
            if (error == 0 && hdr.e_version != EV_CURRENT) error = ERR_INV_FORMAT;
            if (error == 0 && hdr.e_shoff == 0) error = ERR_INV_FORMAT;
            if (error == 0 && lseek(file->fd, hdr.e_shoff, SEEK_SET) == (off_t)-1) error = errno;
            if (error == 0) {
                unsigned cnt = 0;
                file->sections = loc_alloc_zero(sizeof(ELF_Section) * hdr.e_shnum);
                file->section_cnt = hdr.e_shnum;
                while (error == 0 && cnt < hdr.e_shnum) {
                    Elf32_Shdr shdr;
                    memset(&shdr, 0, sizeof(shdr));
                    if (error == 0 && read(file->fd, (char *)&shdr, hdr.e_shentsize) < 0) error = errno;
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
                        cnt++;
                    }
                }
            }
            if (error == 0 && lseek(file->fd, hdr.e_phoff, SEEK_SET) == (off_t)-1) error = errno;
            if (error == 0) {
                unsigned cnt = 0;
                file->pheaders = loc_alloc_zero(sizeof(ELF_PHeader) * hdr.e_phnum);
                file->pheader_cnt = hdr.e_phnum;
                while (error == 0 && cnt < hdr.e_phnum) {
                    Elf32_Phdr phdr;
                    memset(&phdr, 0, sizeof(phdr));
                    if (error == 0 && read(file->fd, (char *)&phdr, hdr.e_phentsize) < 0) error = errno;
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
            if (error == 0 && hdr.e_type != ET_EXEC && hdr.e_type != ET_DYN) error = ERR_INV_FORMAT;
            if (hdr.e_type != ET_EXEC) file->pic = 1;
            if (error == 0 && hdr.e_version != EV_CURRENT) error = ERR_INV_FORMAT;
            if (error == 0 && hdr.e_shoff == 0) error = ERR_INV_FORMAT;
            if (error == 0 && lseek(file->fd, hdr.e_shoff, SEEK_SET) == (off_t)-1) error = errno;
            if (error == 0) {
                unsigned cnt = 0;
                file->sections = loc_alloc_zero(sizeof(ELF_Section) * hdr.e_shnum);
                file->section_cnt = hdr.e_shnum;
                while (error == 0 && cnt < hdr.e_shnum) {
                    Elf64_Shdr shdr;
                    memset(&shdr, 0, sizeof(shdr));
                    if (error == 0 && read(file->fd, (char *)&shdr, hdr.e_shentsize) < 0) error = errno;
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
                        cnt++;
                    }
                }
            }
            if (error == 0 && lseek(file->fd, hdr.e_phoff, SEEK_SET) == (off_t)-1) error = errno;
            if (error == 0) {
                unsigned cnt = 0;
                file->pheaders = loc_alloc_zero(sizeof(ELF_PHeader) * hdr.e_phnum);
                file->pheader_cnt = hdr.e_phnum;
                while (error == 0 && cnt < hdr.e_phnum) {
                    Elf64_Phdr phdr;
                    memset(&phdr, 0, sizeof(phdr));
                    if (error == 0 && read(file->fd, (char *)&phdr, hdr.e_phentsize) < 0) error = errno;
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
        else {
            error = ERR_INV_FORMAT;
        }
        if (error == 0 && str_index != 0 && str_index < file->section_cnt) {
            ELF_Section * str = file->sections + str_index;
            file->str_pool = loc_alloc((size_t)str->size);
            if (lseek(file->fd, str->offset, SEEK_SET) == (off_t)-1) error = errno;
            if (error == 0 && read(file->fd, file->str_pool, (size_t)str->size) < 0) error = errno;
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
    trace(LOG_ELF, "Section %s in ELF file %s is mapped to %#lx", s->name, s->file->name, s->data);
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

static ELF_File * open_memory_region_file(MemoryRegion * r) {
    ELF_File * prev = NULL;
    ELF_File * file = files;

    r->file = NULL;

    if (r->ino == 0) return NULL;
    while (file != NULL) {
        if (file->dev == r->dev && file->ino == r->ino) {
            if (prev != NULL) {
                prev->next = file->next;
                file->next = files;
                files = file;
            }
            file->ref_cnt++;
            file->age = 0;
            r->file= file;
            return file;
        }
        prev = file;
        file = file->next;
    }

    if (r->file_name == NULL) return NULL;
    file = elf_open(r->file_name);
    r->file = file;
    return file;
}

ELF_File * elf_list_first(Context * ctx, ContextAddress addr0, ContextAddress addr1) {
    unsigned i;
    MemoryRegion * regions;
    unsigned region_cnt;

    elf_list_ctx = ctx;
    elf_list_addr0 = addr0;
    elf_list_addr1 = addr1;
    memory_map_get_regions(ctx, &regions, &region_cnt);
    for (i = 0; i < region_cnt; i++) {
        MemoryRegion * r = regions + i;
        if (r->addr <= addr1 && r->addr + r->size >= addr0) {
            assert(r->file == NULL);
            if (r->ino != 0) {
                ELF_File * file = open_memory_region_file(r);
                if (file != NULL) {
                    assert(!file->listed);
                    file->listed = 1;
                    elf_list_pos = i + 1;
                    return file;
                }
            }
        }
    }
    errno = 0;
    return NULL;
}

ELF_File * elf_list_next(Context * ctx) {
    unsigned i;
    MemoryRegion * regions;
    unsigned region_cnt;

    assert(ctx == elf_list_ctx);
    memory_map_get_regions(ctx, &regions, &region_cnt);
    for (i = elf_list_pos; i < region_cnt; i++) {
        MemoryRegion * r = regions + i;
        if (r->addr <= elf_list_addr1 && r->addr + r->size >= elf_list_addr0) {
            assert(r->file == NULL);
            if (r->ino != 0) {
                ELF_File * file = open_memory_region_file(r);
                if (file != NULL && !file->listed) {
                    file->listed = 1;
                    elf_list_pos = i + 1;
                    return file;
                }
            }
        }
    }
    errno = 0;
    return NULL;
}

void elf_list_done(Context * ctx) {
    unsigned i;
    MemoryRegion * regions;
    unsigned region_cnt;

    assert(ctx == elf_list_ctx);
    elf_list_ctx = NULL;
    memory_map_get_regions(ctx, &regions, &region_cnt);
    for (i = 0; i < region_cnt; i++) {
        MemoryRegion * r = regions + i;
        ELF_File * file = (ELF_File *)r->file;
        if (file != NULL) {
            file->listed = 0;
            elf_close(file);
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

ContextAddress elf_map_to_run_time_address(Context * ctx, ELF_File * file, ContextAddress addr) {
    unsigned i, j;
    MemoryRegion * regions;
    unsigned region_cnt;

    if (!file->pic) return addr;
    memory_map_get_regions(ctx, &regions, &region_cnt);
    for (i = 0; i < region_cnt; i++) {
        MemoryRegion * r = regions + i;
        if (r->dev == file->dev && r->ino == file->ino) {
            for (j = 0; j < file->pheader_cnt; j++) {
                ELF_PHeader * p = file->pheaders + j;
                if (p->type != PT_LOAD) continue;
                if (p->offset < r->file_offs || p->offset + p->mem_size > r->file_offs + r->size) continue;
                if (addr < p->address || addr >= p->address + p->mem_size) continue;
                if (!(p->flags & PF_W) != !(r->flags & MM_FLAG_W)) continue;
                return (ContextAddress)(addr - p->address + p->offset - r->file_offs + r->addr);
            }
        }
    }
    return 0;
}

#if SERVICE_Expressions && ENABLE_DebugContext

static int get_dynamic_tag(Context * ctx, ELF_File * file, int tag, ContextAddress * addr) {
    unsigned i, j;

    for (i = 1; i < file->section_cnt; i++) {
        ELF_Section * sec = file->sections + i;
        if (sec->size == 0) continue;
        if (sec->name == NULL) continue;
        if (strcmp(sec->name, ".dynamic") == 0) {
            if (elf_load(sec) < 0) return -1;
            if (file->elf64) {
                unsigned cnt = (unsigned)(sec->size / sizeof(Elf64_Dyn));
                for (j = 0; j < cnt; j++) {
                    Elf64_Dyn * dyn = (Elf64_Dyn *)sec->data + j;
                    if (file->byte_swap) SWAP(dyn->d_tag);
                    if (dyn->d_tag == DT_NULL) break;
                    if (dyn->d_tag == tag) {
                        if (addr != NULL) {
                            char buf[sizeof(dyn->d_un.d_ptr)];
                            ContextAddress sec_addr = elf_map_to_run_time_address(ctx, file, (ContextAddress)sec->addr);
                            ContextAddress entry_addr = sec_addr + j * sizeof(Elf64_Dyn) + offsetof(Elf64_Dyn, d_un.d_ptr);
                            if (context_read_mem(ctx, entry_addr, buf, sizeof(buf)) < 0) return -1;
                            if (file->byte_swap) SWAP(buf);
                            *addr = (ContextAddress)*(Elf64_Addr *)buf;
                        }
                        return 0;
                    }
                }
            }
            else {
                unsigned cnt = (unsigned)(sec->size / sizeof(Elf32_Dyn));
                for (j = 0; j < cnt; j++) {
                    Elf32_Dyn * dyn = (Elf32_Dyn *)sec->data + j;
                    if (file->byte_swap) SWAP(dyn->d_tag);
                    if (dyn->d_tag == DT_NULL) break;
                    if (dyn->d_tag == tag) {
                        if (addr != NULL) {
                            char buf1[sizeof(dyn->d_tag)];
                            char buf2[sizeof(dyn->d_un.d_ptr)];
                            ContextAddress sec_addr = elf_map_to_run_time_address(ctx, file, (ContextAddress)sec->addr);
                            ContextAddress entry_addr = sec_addr + j * sizeof(Elf32_Dyn);
                            if (context_read_mem(ctx, entry_addr, buf1, sizeof(buf1)) < 0) return -1;
                            if (file->byte_swap) SWAP(buf1);
                            if (*(Elf32_Sword *)buf1 != tag) break;
                            if (context_read_mem(ctx, entry_addr + offsetof(Elf32_Dyn, d_un.d_ptr), buf2, sizeof(buf2)) < 0) return -1;
                            if (file->byte_swap) SWAP(buf2);
                            *addr = (ContextAddress)*(Elf32_Addr *)buf2;
                        }
                        return 0;
                    }
                }
            }
        }
    }
    errno = ENOENT;
    return -1;
}

static int sym_name_cmp(char * x, char * y) {
    while (*x && *x == *y) {
        x++;
        y++;
    }
    if (*x == 0 && *y == 0) return 0;
    if (*x == '@' && *(x + 1) == '@' && *y == 0) return 0;
    if (*x < *y) return -1;
    return 1;
}

static int get_global_symbol_address(Context * ctx, ELF_File * file, char * name, ContextAddress * addr) {
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
                    Elf64_Sym * sym = (Elf64_Sym *)sec->data + j;
                    if (ELF64_ST_BIND(sym->st_info) != STB_GLOBAL) continue;
                    if (file->byte_swap) SWAP(sym->st_name);
                    if (sym_name_cmp((char *)str->data + sym->st_name, name) != 0) continue;
                    switch (ELF64_ST_TYPE(sym->st_info)) {
                    case STT_OBJECT:
                    case STT_FUNC:
                        if (file->byte_swap) SWAP(sym->st_value);
                        *addr = elf_map_to_run_time_address(ctx, file, (ContextAddress)sym->st_value);
                        if (*addr != 0) return 0;
                    }
                }
            }
            else {
                unsigned cnt = (unsigned)(sec->size / sizeof(Elf32_Sym));
                for (j = 0; j < cnt; j++) {
                    Elf32_Sym * sym = (Elf32_Sym *)sec->data + j;
                    if (ELF32_ST_BIND(sym->st_info) != STB_GLOBAL) continue;
                    if (file->byte_swap) SWAP(sym->st_name);
                    if (sym_name_cmp((char *)str->data + sym->st_name, name) != 0) continue;
                    switch (ELF32_ST_TYPE(sym->st_info)) {
                    case STT_OBJECT:
                    case STT_FUNC:
                        if (file->byte_swap) SWAP(sym->st_value);
                        *addr = elf_map_to_run_time_address(ctx, file, (ContextAddress)sym->st_value);
                        if (*addr != 0) return 0;
                    }
                }
            }
        }
    }
    errno = ENOENT;
    return -1;
}

static int read_memory_word(Context * ctx, ContextAddress addr, ContextAddress * word) {
    switch (context_word_size(ctx)) {
    case 8:
        {
            char buf[8];
            if (context_read_mem(ctx, addr, buf, sizeof(buf)) < 0) return -1;
            *word = (ContextAddress)*(uint64_t *)buf;
            return 0;
        }
    case 4:
        {
            char buf[4];
            if (context_read_mem(ctx, addr, buf, sizeof(buf)) < 0) return -1;
            *word = (ContextAddress)*(uint32_t *)buf;
            return 0;
        }
    default:
        assert(0);
    }
    errno = ERR_UNSUPPORTED;
    return -1;
}

/*
 * Return run-time address of the debug structrure that is normally pointed by DT_DEBUG entry in ".dynamic" section.
 * Return 0 if the structure could not be found.
 */
static ContextAddress elf_get_debug_structure_address(Context * ctx) {
    ELF_File * file = NULL;
    ContextAddress addr = 0;

    if (ctx->parent != NULL) ctx = ctx->parent;
    if (ctx->debug_structure_searched) return ctx->debug_structure_address;
    ctx->debug_structure_address = 0;
    ctx->debug_structure_searched = 1;

    for (file = elf_list_first(ctx, 0, ~(ContextAddress)0); file != NULL; file = elf_list_next(ctx)) {
        if (file->pic) continue;
        if (get_dynamic_tag(ctx, file, DT_MIPS_RLD_MAP, &addr) == 0) {
            if (read_memory_word(ctx, addr, &addr) < 0) continue;
            break;
        }
        if (get_dynamic_tag(ctx, file, DT_DEBUG, &addr) == 0) break;
        if (get_global_symbol_address(ctx, file, "_r_debug", &addr) == 0) break;
    }
    elf_list_done(ctx);
    ctx->debug_structure_address = addr;
    return addr;
}

static int expression_identifier_callback(Context * ctx, int frame, char * name, Value * v) {
    if (ctx == NULL) return 0;
    if (strcmp(name, "$loader_brk") == 0) {
        v->address = elf_get_debug_structure_address(ctx);
        if (v->address != 0) {
            v->type_class = TYPE_CLASS_POINTER;
            v->size = context_word_size(ctx);
            switch (v->size) {
            case 4: v->address += 8; break;
            case 8: v->address += 16; break;
            default: assert(0);
            }
            v->remote = 1;
            return 1;
        }
    }
    if (strcmp(name, "$loader_state") == 0) {
        v->address = elf_get_debug_structure_address(ctx);
        if (v->address != 0) {
            v->type_class = TYPE_CLASS_CARDINAL;
            v->size = context_word_size(ctx);
            switch (v->size) {
            case 4: v->address += 12; break;
            case 8: v->address += 24; break;
            default: assert(0);
            }
            v->remote = 1;
            return 1;
        }
    }
    return 0;
}

static void eventpoint_at_loader(Context * ctx, void * args) {
    typedef enum { RT_CONSISTENT, RT_ADD, RT_DELETE } r_state;
    ContextAddress addr = elf_get_debug_structure_address(ctx);
    unsigned size = context_word_size(ctx);
    ContextAddress state = 0;

    if (ctx->parent != NULL) ctx = ctx->parent;

    switch (size) {
    case 4: addr += 12; break;
    case 8: addr += 24; break;
    default: assert(0);
    }
    if (read_memory_word(ctx, addr, &state) < 0) {
        int error = errno;
        trace(LOG_ALWAYS, "Can't read loader state flag: %d %s", error, errno_to_str(error));
        ctx->pending_intercept = 1;
        ctx->loader_state = 0;
        return;
    }
    switch (state) {
    case RT_CONSISTENT:
        if (ctx->loader_state == RT_ADD) {
            memory_map_event_module_loaded(ctx);
        }
        else if (ctx->loader_state == RT_DELETE) {
            memory_map_event_module_unloaded(ctx);
        }
        break;
    case RT_ADD:
        break;
    case RT_DELETE:
        /* TODO: need to call memory_map_event_code_section_ummapped() */
        break;
    }
    ctx->loader_state = state;
}

#endif /* SERVICE_Expressions && ENABLE_DebugContext */

void ini_elf(void) {
#if SERVICE_Expressions && ENABLE_DebugContext
    add_identifier_callback(expression_identifier_callback);
    create_eventpoint("$loader_brk", eventpoint_at_loader, NULL);
#endif /* SERVICE_Expressions && ENABLE_DebugContext */
}

#endif /* ENABLE_ELF */
