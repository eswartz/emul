/*
  (c) 1994-2001 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
*/

/*
  $Id$
 */

#ifndef __MEMORY_H__
#define __MEMORY_H__

#include "v9t9_endian.h"
#include "sysdeps.h"
#include "command.h"

#include "centry.h"

/*
  All CPU memory arrays in V9t9 are arranged by words, arranged in
  host-endian order for efficiency.  So, accessing a word from memory
  is very simple.  Accessing a byte may mean toggling the lowest
  address bit.  
*/

/*  access words/bytes from host-endian word arrays  */

#define WORD(ar,ad) (*(u16 *)((ar)+((ad)&0xfffe)))

#if SWAPPED_ENDIAN
#define BYTE(ar,ad) (*(u8 *)((ar)+((ad)^1)))
#else
#define BYTE(ar,ad) (*(u8 *)((ar)+(ad)))
#endif

#define FLAT_BYTE(ar,ad) (*(u8 *)((ar)+(ad)))

struct mrstruct;

typedef u16 (*mrword)(const struct mrstruct *mr, const u32 addr);
typedef s8 (*mrbyte)(const struct mrstruct *mr, u32 addr);

typedef void (*mwword)(const struct mrstruct *mr, u32 addr,u16 val);
typedef void (*mwbyte)(const struct mrstruct *mr, u32 addr,s8 val);

struct EmuData;

#define NUMDOMAINS 	4

typedef enum
{
	md_cpu,
	md_graphics,
	md_video,
	md_speech
}	mem_domain;

typedef struct mrstruct
{
	u8		*areamemory;	/* actual memory for area, except for empty mem */
	u8		*arearead;		/* if non-NULL, we can statically read */
	u8		*areawrite;		/* if non-NULL, we can statically write */
	mrword	read_word;		/* these routines are used before the */
	mrbyte	read_byte;		/* memory maps when using the memory_xxx_xxx */
	mwword	write_word;		/* functions, but the memory is used first */
	mwbyte	write_byte;		/* when the MEMORY_XXX_XXX macros are used. */

	/* debugger info */
	struct bkpt *bkpt;		/* breakpoint info if debugging memory */

	/* compiler stats */
	int		read_hits;		/* number of read requests */
	int		write_hits;		/* number of write requests */
	int		exec_hits;		/* number of executed words */
}	mrstruct;

#define AREA_IS_MEMORY_MAPPED(a) \
		((a)->read_word || (a)->read_byte || \
		(a)->write_word || (a)->write_byte)

/*	This must remain 64K, even if mega-memory expansion
	is emulated.  All the public routines expect to be passed
	16-bit addresses. */
#define PHYSMEMORYSIZE 65536

#if !FOR_COMPILER
extern u8 zeroes[PHYSMEMORYSIZE];
#endif

/*	Size of an area of memory.  This is the minimum
	size of memory for which each address therein is
	expected to act "the same". */
#define AREASIZE 1024
#define AREASHIFT 10

#define AREACLIP(addr)	((addr) & (~0<<AREASHIFT) & 0xffff)

#define NUMAREAS 	(PHYSMEMORYSIZE >> AREASHIFT)

typedef mrstruct	mrmap[NUMDOMAINS][NUMAREAS];
//extern mrstruct 	__areahandlers[NUMDOMAINS][NUMAREAS];
extern mrmap		__areahandlers;

//	shorthand for CPU memory 
#define SET_AREA_HANDLER(addr,size,handler) set_area_handler(md_cpu, addr,size,handler)

void	set_area_handler(mem_domain domain, u16 addr, u32 size, mrstruct *handler);

#define FOREACH_AREA(domain, area) do { \
	int i; for (i = 0; i < NUMAREAS; i++) { \
	mrstruct *area = &__areahandlers[domain][i];
#define END_FOREACH_AREA } } while(0)

/*********************************************/

/*
	These routines are global and available for use by any
	other module.  Choose one of the two functions depending
	on side effects.  For routines that are strictly for 
	static reads or writes (for example, in a debugger), use
	the macro versions.  For routines used during emulation,
	use the function ones.
*/

//	more CPU shorthands

#if !FOR_COMPILER

#define THE_AREA(dmn, addr) (&__areahandlers[dmn][((addr) & (PHYSMEMORYSIZE-1)) >> AREASHIFT])

#define AREA_SETUP(dmn, addr) \
	mrstruct *area = THE_AREA(dmn, addr); \
	my_assert((dmn) >= md_cpu && (dmn) < NUMDOMAINS &&(size_t)area > addr) 

#define HAS_RAM_ACCESS(dmn, addr) \
	(THE_AREA(dmn, addr)->arearead != NULL && \
	THE_AREA(dmn, addr)->areawrite != NULL)

#define HAS_ROM_ACCESS(dmn, addr) \
	(THE_AREA(dmn, addr)->arearead != NULL)

/*	These macros are for strict static access to memory.
	No routines will be called, so there will be no side effects.
	Unfortunately, though, a lot of memory looks like zeroes. */	
#define AREA_READ_WORD(area, addr) 	\
	((area)->arearead ? \
		WORD((area)->arearead, (addr & (AREASIZE-1))) : 0)
#define AREA_READ_BYTE(area, addr) 	\
	((area)->arearead ? \
		BYTE((area)->arearead, (addr & (AREASIZE-1))) : 0)
#define AREA_WRITE_WORD(area, addr, val) 	\
	do { if ((area)->areawrite) \
		WORD((area)->areawrite, (addr & (AREASIZE-1))) = val; } while (0)
#define AREA_WRITE_BYTE(area, addr, val) 	\
	do { if ((area)->areawrite) \
		BYTE((area)->areawrite, (addr & (AREASIZE-1))) = val; } while (0)

#define DOMAIN_READ_WORD(dmn, addr)  \
		AREA_READ_WORD(THE_AREA(dmn,addr), addr)
#define DOMAIN_READ_BYTE(dmn, addr)  \
		AREA_READ_BYTE(THE_AREA(dmn,addr), addr)
#define DOMAIN_WRITE_WORD(dmn, addr, val)  \
		AREA_WRITE_WORD(THE_AREA(dmn, addr), addr, val)
#define DOMAIN_WRITE_BYTE(dmn, addr, val)  \
		AREA_WRITE_BYTE(THE_AREA(dmn, addr), addr, val)

#define MEMORY_READ_WORD(addr) 	DOMAIN_READ_WORD(md_cpu, addr)
#define MEMORY_READ_BYTE(addr) 	DOMAIN_READ_BYTE(md_cpu, addr)
#define MEMORY_WRITE_WORD(addr, val) DOMAIN_WRITE_WORD(md_cpu, addr, val)
#define MEMORY_WRITE_BYTE(addr, val) DOMAIN_WRITE_BYTE(md_cpu, addr, val)

u16		domain_read_word_mr(mrstruct *mr, mem_domain md, u32 addr);
void	domain_write_word_mr(mrstruct *mr, mem_domain md, u32 addr, u16 val);
s8		domain_read_byte_mr(mrstruct *mr, mem_domain md, u32 addr);
void	domain_write_byte_mr(mrstruct *mr, mem_domain md, u32 addr, s8 val);

u16		domain_read_word(mem_domain md, u32 addr);
void	domain_write_word(mem_domain md, u32 addr, u16 val);
s8		domain_read_byte(mem_domain md, u32 addr);
void	domain_write_byte(mem_domain md, u32 addr, s8 val);

INLINE	u16		memory_read_word(u32 addr) { 
	return domain_read_word(md_cpu, addr); 
}
INLINE	void	memory_write_word(u32 addr, u16 val) {
	domain_write_word(md_cpu, addr, val);
}
INLINE	s8		memory_read_byte(u32 addr) {
	return domain_read_byte(md_cpu, addr);
}
INLINE	void	memory_write_byte(u32 addr, s8 val) {
	domain_write_byte(md_cpu, addr, val);
}


//	Empty ROM memory handler

extern mrstruct	zero_memory_handler;

#endif	/*!FOR_COMPILER*/

/******************************************/

#if !FOR_COMPILER

//	32K memory expansion

extern int	isexpram;				/* is there expansion RAM? */

//	Alternate >8000->83FF mapping

extern int	isenhconsoleram;		/* is there extra console RAM >8000->82FF? */

extern void memory_init(void);
extern void memory_ram_init(void);

#endif /*!FOR_COMPILER*/

/**********************************/

#if !FOR_COMPILER

#if 0
#define FLAT_MEMORY_PTR(dmn, addr)		\
		(THE_AREA(dmn, addr)->areamemory ? \
				THE_AREA(dmn, addr)->areamemory + ((addr) & (AREASIZE-1)) : \
				 zeroes)
#else
#define FLAT_MEMORY_PTR(dmn, addr)		\
			(THE_AREA(dmn, addr)->areamemory + ((addr) & (AREASIZE-1)))
#endif

#define registerptr(reg)	(u16 *)FLAT_MEMORY_PTR(md_cpu, (wp+reg+reg) & 0xffff)

/*
INLINE u16 *registerptr(int reg) {
	extern uaddr wp;
	mrstruct *area = THE_AREA(md_cpu, wp+reg+reg);
	return &WORD(area->areamemory, (wp+reg+reg) & (AREASIZE - 1));
}
*/

#endif /*!FOR_COMPILER*/

/*
	These enums and struct define a higher-level organization of
	the memory map, used to allow radical customization of the
	emulated computer's architecture.  

	A MemoryEntry bridges the gap between ROM, RAM, and nonvolatile
	RAM.  By associating a primary purpose with an area of memory,
	this allows us to transparently map ROMs from disk into an
	area of memory as well as maintain on disk an image of an
	area of RAM.  

	The mementlist can be used to find out what the contents of 
	memory are without knowing about the specific routines referenced
	in an mrstruct through the use of the flags.  
 */

enum
{
	/* MEMENT_ROM = 0, */	/* entry is ROM */
	MEMENT_RAM = 1,			/* entry is RAM */
	MEMENT_STORED = 3,		/* RAM entry stored to disk */

	MEMENT_BANKING = 4+8,	/* mask for banking */
	/* MEMENT_NOT_BANKED = 0, */
	MEMENT_BANK_1 = 4, 		/* bank #0 */
	MEMENT_BANK_2 = 8,		/* bank #1 */

	MEMENT_CART = 16,		/* this entry is part of a module */

	MEMENT_DOMAIN_SHIFT = 5,
	MEMENT_DOMAIN 	= 7<<5,	/* mask for domain of memory */
	MEMENT_CONSOLE 	= md_cpu<<5,	/* console ROM/RAM */
	MEMENT_GRAPHICS = md_graphics<<5,	/* GROM/GRAM */
	MEMENT_VIDEO	= md_video<<5, /* video */
	MEMENT_SPEECH	= md_speech<<5,	/* speech */
//	MEMENT_DSR		= 4<<5, /* DSR ROM */

	MEMENT_USER		= 256	/* allocated via DefineMemory, destroy as needed 
							 * (when new memory overlaps this), else caller
							 * will destroy */
};

typedef struct MemoryEntry {
	int			flags;		/* memory flags MEMENT_xxx */

	u32		   	addr;		/* start address */
	u32			offs;		/* offset of entry into original entry */
	s32			size;		/* size in bytes, 0 = unknown, -xxx = magnitude of maximum */
	u32			realsize;	/* size in bytes if loaded */
	char		*name;		/* name of entry for debugging */
	char		*filename;	/* file for loading/saving */
	u32			fileoffs;	/* offset into file */

	mrstruct	memact;		/* how the memory acts, memory referenced must be malloc'ed */
	struct MemoryEntry *next; /* next entry in database */
} MemoryEntry;

extern	MemoryEntry *mementlist;  /* active memory list */

/*	Handlers for bank-switched module */
extern mrstruct *memory_module_bank_handlers[2];
extern int memory_module_bank;
void
memory_set_module_bank(u8 bank);

/*	Free memory map */
void 	memory_free_list(void);

/*	Initialize memory map */
int		memory_init_list(void);

/*  Create a new memory entry 

	flags: 	bitmask of MEMENT_xxx
	addr:  	address of new memory
	size:	size of memory in bytes, 
				or if negative, the magnitude is the maximum size
	name:	user name for memory segment
	filename:  location of ROM or RAM on disk (MEMENT_ROM/MEMENT_STORED)
	fileoffs:  offset into file where memory is stored
	memact: actions on memory access to area
*/
MemoryEntry *memory_new_entry(int flags, u32 addr, s32 size,
							  char *name, char *filename, u32 fileoffs,
							  mrstruct *memact);

MemoryEntry *memory_lookup_entry(u32 addr, mem_domain md, int live);

/*	Destroy entry (free memory) */
void	memory_destroy_entry(MemoryEntry *ent);

/*	Save a volatile entry in the memory map to disk */
int		memory_save_entry(MemoryEntry *ent);

/*	Load a volatile entry in the memory map from disk */
int		memory_load_entry(MemoryEntry *ent);

/*	Add entry to memory list and destroy entries this overrides */
int		memory_add_entry_to_list(MemoryEntry *ent);

/*	Remove entry from memory list, return ent->next */
MemoryEntry *
memory_remove_entry_from_list(MemoryEntry *prev, MemoryEntry *ent);

/*	Map an entry into the memory map */
int		memory_map_entry(MemoryEntry *ent);

/*	Unmap an entry from the memory map */
void	memory_unmap_entry(MemoryEntry *ent);

/*	Save volatile memory */
void	memory_volatile_save(void);

/*	Load volatile memory  */
int		memory_volatile_load(void);

/*	Load all memory  */
int		memory_complete_load(void);

/*	Do it all */
int		memory_insert_new_entry(int flags, u32 addr, s32 size,
								char *name, char *filename, u32 fileoffs,
								mrstruct *memact);

/*	Add a handler to be notified when the memory map changes */
typedef void (*MemoryCallback)(void);
typedef void (*MemoryEntryCallback)(MemoryEntry *ent);

void	memory_register_callback(MemoryCallback func);
void 	memory_register_entry_callback(MemoryEntryCallback func);

/*	DefineMemory */
DECL_SYMBOL_ACTION(memory_define_entry);
/*	ListMemory */
DECL_SYMBOL_ACTION(memory_dump);
/*	DefaultMemoryMap */
DECL_SYMBOL_ACTION(memory_default_list);

#include "cexit.h"

#endif
