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
#ifndef D_elf
#define D_elf

#include <config.h>

#if ENABLE_ELF

#if !defined(WIN32) && !defined(__APPLE__)
#  include <elf.h>
#endif
#include <framework/context.h>

#if defined(WIN32) || defined(__APPLE__)

#define EI_MAG0        0
#define EI_MAG1        1
#define EI_MAG2        2
#define EI_MAG3        3
#define EI_CLASS       4
#define EI_DATA        5
#define EI_VERSION     6
#define EI_OSABI       7
#define EI_ABIVERSION  8
#define EI_PAD         9
#define EI_NIDENT     16

#define ELFMAG0 0x7F
#define ELFMAG1  'E'
#define ELFMAG2  'L'
#define ELFMAG3  'F'
#define ELFMAG   "\177ELF"
#define SELFMAG  4

#define ELFCLASSNONE   0
#define ELFCLASS32     1
#define ELFCLASS64     2

#define ELFDATANONE    0
#define ELFDATA2LSB    1
#define ELFDATA2MSB    2

#define EM_NONE         0
#define EM_M32          1 /* AT&T WE 32100 */
#define EM_SPARC        2 /* SPARC */
#define EM_386          3 /* Intel Architecture */
#define EM_68K          4 /* Motorola 68000 */
#define EM_88K          5 /* Motorola 88000 */
#define EM_860          7 /* Intel 80860 */
#define EM_MIPS         8 /* MIPS RS3000 Big-Endian */
#define EM_MIPS_RS4_BE 10 /* MIPS RS4000 Big-Endian */

#define ET_NONE         0
#define ET_REL          1
#define ET_EXEC         2
#define ET_DYN          3
#define ET_CORE         4
#define ET_LOOS    0xFE00
#define ET_HIOS    0xFEFF
#define ET_LOPROC  0xFF00
#define ET_HIPROC  0xFFFF

#define EV_CURRENT      1

#define SHN_UNDEF       0
#define SHN_ABS    0xfff1
#define SHN_COMMON 0xfff2

#define SHT_NULL        0
#define SHT_PROGBITS    1
#define SHT_SYMTAB      2
#define SHT_STRTAB      3
#define SHT_RELA        4
#define SHT_HASH        5
#define SHT_DYNAMIC     6
#define SHT_NOTE        7
#define SHT_NOBITS      8
#define SHT_REL         9
#define SHT_SHLIB      10
#define SHT_DYNSYM     11

#define STN_UNDEF       0

#define STB_LOCAL       0
#define STB_GLOBAL      1
#define STB_WEAK        2

#define STT_NOTYPE      0
#define STT_OBJECT      1
#define STT_FUNC        2
#define STT_SECTION     3
#define STT_FILE        4

#define PT_NULL         0
#define PT_LOAD         1
#define PT_DYNAMIC      2
#define PT_INTERP       3
#define PT_NOTE         4
#define PT_SHLIB        5
#define PT_PHDR         6
#define PT_TLS          7

#define PF_X            (1 << 0)
#define PF_W            (1 << 1)
#define PF_R            (1 << 2)

#define DT_NULL         0
#define DT_NEEDED       1
#define DT_PLTRELSZ     2
#define DT_PLTGOT       3
#define DT_HASH         4
#define DT_STRTAB       5
#define DT_SYMTAB       6
#define DT_RELA         7
#define DT_RELASZ       8
#define DT_RELAENT      9
#define DT_STRSZ        10
#define DT_SYMENT       11
#define DT_INIT         12
#define DT_FINI         13
#define DT_SONAME       14
#define DT_RPATH        15
#define DT_SYMBOLIC     16
#define DT_REL          17
#define DT_RELSZ        18
#define DT_RELENT       19
#define DT_PLTREL       20
#define DT_DEBUG        21
#define DT_TEXTREL      22
#define DT_JMPREL       23
#define DT_BIND_NOW     24
#define DT_INIT_ARRAY   25
#define DT_FINI_ARRAY   26
#define DT_INIT_ARRAYSZ 27
#define DT_FINI_ARRAYSZ 28
#define DT_RUNPATH      29
#define DT_FLAGS        30
#define DT_ENCODING     32
#define DT_PREINIT_ARRAY 32
#define DT_PREINIT_ARRAYSZ 33
#define DT_NUM          34
#define DT_LOOS         0x6000000d
#define DT_HIOS         0x6ffff000
#define DT_LOPROC       0x70000000
#define DT_HIPROC       0x7fffffff

typedef uint32_t    Elf32_Addr;
typedef uint16_t    Elf32_Half;
typedef uint32_t    Elf32_Off;
typedef int32_t     Elf32_Sword;
typedef uint32_t    Elf32_Word;

typedef struct Elf32_Ehdr {
    unsigned char e_ident[EI_NIDENT];
    Elf32_Half    e_type;
    Elf32_Half    e_machine;
    Elf32_Word    e_version;
    Elf32_Addr    e_entry;
    Elf32_Off     e_phoff;
    Elf32_Off     e_shoff;
    Elf32_Word    e_flags;
    Elf32_Half    e_ehsize;
    Elf32_Half    e_phentsize;
    Elf32_Half    e_phnum;
    Elf32_Half    e_shentsize;
    Elf32_Half    e_shnum;
    Elf32_Half    e_shstrndx;
} Elf32_Ehdr;

typedef struct Elf32_Shdr {
    Elf32_Word sh_name;
    Elf32_Word sh_type;
    Elf32_Word sh_flags;
    Elf32_Addr sh_addr;
    Elf32_Off  sh_offset;
    Elf32_Word sh_size;
    Elf32_Word sh_link;
    Elf32_Word sh_info;
    Elf32_Word sh_addralign;
    Elf32_Word sh_entsize;
} Elf32_Shdr;

#define SHF_ALLOC           0x00000002

typedef struct Elf32_Phdr {
    Elf32_Word p_type;
    Elf32_Off  p_offset;
    Elf32_Addr p_vaddr;
    Elf32_Addr p_paddr;
    Elf32_Word p_filesz;
    Elf32_Word p_memsz;
    Elf32_Word p_flags;
    Elf32_Word p_align;
} Elf32_Phdr;

typedef struct Elf32_Sym {
    Elf32_Word    st_name;
    Elf32_Addr    st_value;
    Elf32_Word    st_size;
    unsigned char st_info;
    unsigned char st_other;
    Elf32_Half    st_shndx;
} Elf32_Sym;

#define ELF32_ST_BIND(i)   ((i)>>4)
#define ELF32_ST_TYPE(i)   ((i)&0xf)

typedef struct {
    Elf32_Addr r_offset;
    Elf32_Word r_info;
} Elf32_Rel;

typedef struct {
    Elf32_Addr r_offset;
    Elf32_Word r_info;
    Elf32_Sword r_addend;
} Elf32_Rela;

#define ELF32_R_SYM(i)  ((i)>>8)
#define ELF32_R_TYPE(i) ((unsigned char)(i))

typedef struct {
    Elf32_Sword d_tag;
    union {
        Elf32_Word d_val;
        Elf32_Addr d_ptr;
    } d_un;
} Elf32_Dyn;

#endif

#if defined(_WRS_KERNEL) || defined(WIN32) || defined(__APPLE__)

typedef uint64_t        Elf64_Addr;
typedef uint16_t        Elf64_Half;
typedef uint32_t        Elf64_Word;
typedef int32_t         Elf64_Sword;
typedef uint64_t        Elf64_Xword;
typedef int64_t         Elf64_Sxword;
typedef uint64_t        Elf64_Off;
typedef uint16_t        Elf64_Section;
typedef Elf64_Half      Elf64_Versym;
typedef uint16_t        Elf64_Quarter;

typedef struct {
    uint8_t       e_ident[EI_NIDENT];
    Elf64_Half    e_type;
    Elf64_Half    e_machine;
    Elf64_Word    e_version;
    Elf64_Addr    e_entry;
    Elf64_Off     e_phoff;
    Elf64_Off     e_shoff;
    Elf64_Word    e_flags;
    Elf64_Half    e_ehsize;
    Elf64_Half    e_phentsize;
    Elf64_Half    e_phnum;
    Elf64_Half    e_shentsize;
    Elf64_Half    e_shnum;
    Elf64_Half    e_shstrndx;
} Elf64_Ehdr;

typedef struct {
    Elf64_Word    sh_name;
    Elf64_Word    sh_type;
    Elf64_Xword   sh_flags;
    Elf64_Addr    sh_addr;
    Elf64_Off     sh_offset;
    Elf64_Xword   sh_size;
    Elf64_Word    sh_link;
    Elf64_Word    sh_info;
    Elf64_Xword   sh_addralign;
    Elf64_Xword   sh_entsize;
} Elf64_Shdr;

typedef struct {
    Elf64_Word    st_name;
    uint8_t       st_info;
    uint8_t       st_other;
    Elf64_Section st_shndx;
    Elf64_Addr    st_value;
    Elf64_Xword   st_size;
} Elf64_Sym;

#define ELF64_ST_BIND(info)             ((info) >> 4)
#define ELF64_ST_TYPE(info)             ((info) & 0xf)

typedef struct {
    Elf64_Sxword d_tag;
    union {
        Elf64_Xword d_val;
        Elf64_Addr d_ptr;
    } d_un;
} Elf64_Dyn;

typedef struct {
    Elf64_Word    p_type;
    Elf64_Word    p_flags;
    Elf64_Off     p_offset;
    Elf64_Addr    p_vaddr;
    Elf64_Addr    p_paddr;
    Elf64_Xword   p_filesz;
    Elf64_Xword   p_memsz;
    Elf64_Xword   p_align;
} Elf64_Phdr;

#endif

typedef struct ElfX_Sym {
    union {
        Elf32_Sym Elf32;
        Elf64_Sym Elf64;
    } u;
} ElfX_Sym;

typedef uint8_t  U1_T;
typedef int8_t   I1_T;
typedef uint16_t U2_T;
typedef int16_t  I2_T;
typedef uint32_t U4_T;
typedef int32_t  I4_T;
typedef uint64_t U8_T;
typedef int64_t  I8_T;

typedef struct ELF_File ELF_File;
typedef struct ELF_Section ELF_Section;
typedef struct ELF_PHeader ELF_PHeader;

struct ELF_File {
    ELF_File * next;
    U4_T ref_cnt;

    char * name;
    dev_t dev;
    ino_t ino;
    int64_t mtime;
    int mtime_changed;
    int fd;

    int big_endian; /* 0 - least significant first, 1 - most significat first */
    int byte_swap;  /* > 0 if file endianness not same as the agent endianness */
    int elf64;
    int type;
    int machine;

    unsigned section_cnt;
    ELF_Section * sections;
    char * str_pool;

    unsigned pheader_cnt;
    ELF_PHeader * pheaders;

    void * dwarf_io_cache;
    void * dwarf_dt_cache;

    int age;
    int listed;
};

struct ELF_Section {
    ELF_File * file;
    U4_T index;
    unsigned name_offset;
    char * name;
    void * data;
    U4_T type;
    U4_T flags;
    U8_T offset;
    U8_T size;
    U8_T addr;
    U4_T link;
    U4_T info;
    U4_T entsize;

    void * mmap_addr;
    size_t mmap_size;

    int relocate;
};

struct ELF_PHeader {
    U4_T type;
    U8_T offset;
    U8_T address;
    U8_T file_size;
    U8_T mem_size;
    U4_T flags;
    U4_T align;
};

/*
 * Swap bytes in a buffer.
 * The function is used when ELF file endianness mismatch agent endianness.
 */
extern void swap_bytes(void * buf, size_t size);
#define SWAP(x) swap_bytes(&(x), sizeof(x))

/*
 * Open ELF file for reading.
 * Same file can be opened mutiple times, each call to elf_open() increases reference counter.
 * File must be closed after usage by calling elf_close().
 * Returns the file descriptior on success. If error, returns NULL and sets errno.
 */
extern ELF_File * elf_open(char * file_name);

/*
 * Close ELF file.
 * Each call of elf_close() decrements reference counter.
 * The file will be kept in a cache for some time even after all references are closed.
 */
extern void elf_close(ELF_File * file);

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
 * Load section data into memory.
 * section->data is set to section data address in memory.
 * Data will stay in memory at least until file is closed.
 * Returns zero on success. If error, returns -1 and sets errno.
 */
extern int elf_load(ELF_Section * section);

/*
 * Register ELF file close callback.
 * The callback is called each time an ELF file data is about to be disposed.
 * Service implementation can use the callback to deallocate
 * cached data related to the file.
 */
typedef void (*ELFCloseListener)(ELF_File *);
extern void elf_add_close_listener(ELFCloseListener listener);

/*
 * Map link-time address in an ELF file to run-time address in a context.
 * Return 0 if the address is not currently mapped.
 */
extern ContextAddress elf_map_to_run_time_address(Context * ctx, ELF_File * file, ELF_Section * section, ContextAddress addr);

/*
 * Read a word from context memory. Word size and endianess are determened by ELF file.
 */
extern int elf_read_memory_word(Context * ctx, ELF_File * file, ContextAddress addr, ContextAddress * word);

/*
 * Return run-time address of the debug structrure that is normally pointed by DT_DEBUG entry in ".dynamic" section.
 * "file" is assigned a file that contains DT_DEBUG entry.
 * Return 0 if the structure could not be found.
 */
extern ContextAddress elf_get_debug_structure_address(Context * ctx, ELF_File ** file);

/*
 * Initialize ELF support module.
 */
extern void ini_elf(void);

#endif /* ENABLE_ELF */

#endif /* D_elf */


