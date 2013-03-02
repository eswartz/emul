/*
  memory.c						-- 99/4A memory management

  (c) 1994-2011 Edward Swartz

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

#include <stdio.h>
#include <unistd.h>
#include <ctype.h>

#include "v9t9_common.h"
#include "v9t9_endian.h"
#include "memory.h"
#include "vdp.h"
#include "grom.h"
#include "speech.h"
#include "sound.h"
#include "moduledb.h"

#define _L	LOG_MEMORY | LOG_INFO

int         isexpram;		/* is there expansion RAM? */
int         isenhconsoleram;	/* is >8000 - >82FF real RAM? */

/**********************************************/

/*
    Below we define a table of mrstructs which map CPU addresses to 
	areas of memory, including means of dealing with special memory
	types through the use of read/write functions.

	It is advantageous to store memory in the
	areamemory/arearead/areawrite pointers and let the MEMORY_xxx or
	memory_xxx routines access it directly, for speed purposes, but
	for some memory-mapped areas it is saner to have a routine manage
	reads and writes to the memory.  You must use a memory handling
	routine when:

		(1) word accesses are not the same as two simultaneous byte accesses 
		(2) reading memory and writing memory are not orthogonal, i.e.,
		writing/reading memory has side effects (memory-mapped)
		
	If the contents of memory are bank-switched or toggled on and off
	(such as the DSR), it's most speed efficient to change handlers to
	remap the contents (through changing the arearead and areawrite
	pointers) rather than copying contents in and out of a static
	areamemory.

	The emulator will make no assumptions about the semantics of
	memory which has read or write routines attached to an area.  */

/*
	Area handlers are the main gateway to the memory bus.  Each
	AREASIZE section of memory has various properties which define how
	to read from and write to it.  mrstruct->areamemory contains the
	memory for the area.  Pure RAM will have mrstruct->arearead and
	mrstruct->areawrite be set to this.  Memory mapped I/O areas or
	ROM will leave one or both of the arearead or areawrite pointers
	NULL and instead define a routine, {read|write}_{byte|word} to
	handle the access to areamemory.

	Note, that while the arearead or areawrite pointers points to an
	AREASIZE sized block, the read/write byte/word routines are always
	passed a full 16-bit address.  This allows a common function to
	control access to several contiguous areas.
*/
mrstruct    __areahandlers[NUMDOMAINS][NUMAREAS];

void        set_area_handler(mem_domain md, u16 addr, u32 size, mrstruct * handler)
{
	mrstruct    tmp = *handler;

	if ((size < AREASIZE) || (addr & (AREASIZE - 1)))
		logger(_L | LOG_FATAL,
			 _("set_area_handler:  attempt made to set a memory handler on an illegal boundary\n"
			 "(0x%04X...0x%04X), the minimum granularity is %d bytes\n"), addr,
			 addr + size - 1, AREASIZE);

	if (handler->arearead == NULL &&
		handler->read_byte == NULL && handler->read_word != NULL)
		logger(_L | LOG_FATAL,
			 _("set_area_handler:  cannot have a handler define read_word without read_byte\n"));
	if (handler->areawrite == NULL && handler->write_byte == NULL
		&& handler->write_word != NULL)
		logger(_L | LOG_FATAL,
			 _("set_area_handler:  cannot have a handler define write_word without write_byte\n"));

	if (!handler->areamemory)
		logger(_L | LOG_FATAL,
				_("set_area_handler:  must have areamemory set\n"));
				
	if (md < 0 || md >= NUMDOMAINS) 
		logger(_L | LOG_FATAL,
			   _("set_area_handler:  domain out of range\n"));

	if (size > PHYSMEMORYSIZE || 
		(u32)addr >= PHYSMEMORYSIZE ||
		addr + size > PHYSMEMORYSIZE)
		logger(_L | LOG_FATAL,
				_("set_area_handler:  illegal address or size\n"));
				
	size = (size + AREASIZE - 1) >> AREASHIFT;
	addr >>= AREASHIFT;
	while (size--) {
		__areahandlers[md][addr++] = tmp;

		/* advance memory pointer(s) */
		if (tmp.areamemory)
			tmp.areamemory += AREASIZE;
		if (tmp.arearead)
			tmp.arearead += AREASIZE;
		if (tmp.areawrite)
			tmp.areawrite += AREASIZE;
	}
}

/****************************************************/

/*************/
/*  CPU RAM  */
/*************/

//  This emulates the standard memory.
static void
wwcram(const mrstruct *mr, u32 addr, u16 val)
{
	WORD(mr->areamemory, (addr & 0x00ff) + 0x300) = val;
}

static      u16
wrcram(const mrstruct *mr, u32 addr)
{
	return WORD(mr->areamemory, (addr & 0x00ff) + 0x300);
}
static void
bwcram(const mrstruct *mr, u32 addr, s8 val)
{
	BYTE(mr->areamemory, (addr & 0x00ff) + 0x300) = val;
}

static      s8
brcram(const mrstruct *mr, u32 addr)
{
	return BYTE(mr->areamemory, (addr & 0x00ff) + 0x300);
}

static u8 std_console_ram[0x400];

mrstruct    std_console_ram_handler = {
	std_console_ram,
	std_console_ram,
	std_console_ram,
	wrcram,
	brcram,						/* for reads, only honor low 8 bits of address */
	wwcram,
	bwcram,						/* for writes, only honor low 8 bits of address */
};

/*
//  Geneve memory
static void
wwcrame(const mrstruct *mr, u32 addr, u16 val)
{
	WORD(mr->areamemory, (addr & 0x03ff)) = val;
}

static      u16
wrcrame(const mrstruct *mr, u32 addr)
{
	return WORD(mr->areamemory, (addr & 0x03ff));
}
static void
bwcrame(const mrstruct *mr, u32 addr, s8 val)
{
	BYTE(mr->areamemory, (addr & 0x03ff)) = val;
}

static      s8
brcrame(const mrstruct *mr, u32 addr)
{
	return BYTE(mr->areamemory, (addr & 0x03ff));
}
*/

mrstruct    enh_console_ram_handler = {
	std_console_ram,
	std_console_ram,
	std_console_ram,
	NULL, NULL, NULL, NULL		/* allow direct access to memory */
};

/******************/

/*  expansion RAM */

/******************/

static void
wweram(const mrstruct *mr, u32 addr, u16 val)
{
	if (isexpram)
		WORD(mr->areamemory, addr & (AREASIZE - 1)) = val;
}

static      u16
wreram(const mrstruct *mr, u32 addr)
{
	return (isexpram ? WORD(mr->areamemory, addr & (AREASIZE - 1)) : 0);
}
static void
bweram(const mrstruct *mr, u32 addr, s8 val)
{
	if (isexpram)
		BYTE(mr->areamemory, addr & (AREASIZE - 1)) = val;
}

static      s8
breram(const mrstruct *mr, u32 addr)
{
	return (isexpram ? BYTE(mr->areamemory, addr & (AREASIZE - 1)) : 0);
}

static u8 low_expansion_memory[0x2000];
mrstruct    low_expansion_memory_handler = {
	low_expansion_memory,
	low_expansion_memory,
	low_expansion_memory,
	wreram,
	breram,						/* only read if expansion memory is on */
	wweram,
	bweram,						/* only write if expansion memory is on */
};

static u8 high_expansion_memory[0x6000];
mrstruct    high_expansion_memory_handler = {
	high_expansion_memory,
	high_expansion_memory,
	high_expansion_memory,
	wreram,
	breram,						/* only read if expansion memory is on */
	wweram,
	bweram,						/* only write if expansion memory is on */
};

/****************/

/*	zero memory */

/****************/

u8 zeroes[PHYSMEMORYSIZE];
mrstruct    zero_memory_handler = {
	zeroes, NULL, NULL,			/* can neither read nor write directly */
	NULL,
	NULL,						/* for reads, return zero */
	NULL,
	NULL						/* for writes, ignore */
};


/***************************************************************************/

static void
bwsound(const mrstruct *mr, u32 addr, s8 val)
{
	if (0 == (addr & 1))
		sound_mmio_write(val);
}

mrstruct    console_sound_handler = {
	zeroes, NULL, NULL,			/* no RAM */
	NULL, NULL,					/* no reads */
	NULL, bwsound
};

static      s8
brrvdp(const mrstruct *mr, u32 addr)
{
	if (0 == (addr & 1))
		return vdp_mmio_read(addr & 2);
	else
		return 0;
}

mrstruct    console_vdp_read_handler = {
	zeroes, NULL, NULL,			/* no RAM */
	NULL, brrvdp,
	NULL, NULL					/* no writes */
};

static void
bwwvdp(const mrstruct *mr, u32 addr, s8 val)
{
	if (!(addr & 1))
		vdp_mmio_write(addr & 2, val);
}

mrstruct    console_vdp_write_handler = {
	zeroes, NULL, NULL,			/* no RAM */
	NULL, NULL,					/* no reads */
	NULL, bwwvdp
};

static      s8
brrspeech(const mrstruct *mr, u32 addr)
{
	if (0 == (addr & 1))
		return speech_mmio_read();
	else
		return 0;
}

mrstruct    console_speech_read_handler = {
	zeroes, NULL, NULL,			/* no RAM */
	NULL, brrspeech,
	NULL, NULL					/* no writes */
};

static void
bwwspeech(const mrstruct *mr, u32 addr, s8 val)
{
	if (0 == (addr & 1))
		speech_mmio_write(val);
}

mrstruct    console_speech_write_handler = {
	zeroes, NULL, NULL,			/* no RAM */
	NULL, NULL,					/* no reads */
	NULL, bwwspeech
};

static      s8
brrgrom(const mrstruct *mr, u32 addr)
{
	if (0 == (addr & 1))
		return grom_mmio_read(addr & 2);
	else
		return 0;
}

mrstruct    console_grom_read_handler = {
	zeroes, NULL, NULL,			/* no RAM */
	NULL, brrgrom,
	NULL, NULL					/* no writes */
};

static void
bwwgrom(const mrstruct *mr, u32 addr, s8 val)
{
	if (!(addr & 1))
		grom_mmio_write(addr & 2, val);
}

mrstruct    console_grom_write_handler = {
	zeroes, NULL, NULL,			/* no RAM */
	NULL, NULL,					/* no reads [should cause a freeze] */
	NULL, bwwgrom
};

/********************************/

u16 domain_read_word_mr(mrstruct *area, mem_domain md, u32 addr)
{
	if (area->read_word)
		return area->read_word(area, addr);
	else if (area->read_byte)
		return (area->read_byte(area, addr) << 8) |
			((area->read_byte(area, addr + 1)) & 0xff);
	else if (area->arearead)
		if (md == md_cpu)
			return WORD(area->arearead, (addr & (AREASIZE - 1)));
		else {
			return (FLAT_BYTE(area->arearead, (addr & (AREASIZE - 1))) << 8) +
				FLAT_BYTE(area->arearead, ((addr+1) & (AREASIZE - 1)));
		}
	else
		return 0;
}

void
domain_write_word_mr(mrstruct *area, mem_domain md, u32 addr, u16 val)
{
	if (area->write_word)
		area->write_word(area, addr, val);
	else if (area->write_byte) {
		area->write_byte(area, addr, val >> 8);
		area->write_byte(area, addr + 1, (s8) val);
	} else if (area->areawrite)
		if (md == md_cpu)
			WORD(area->areawrite, (addr & (AREASIZE - 1))) = val;
		else {
			FLAT_BYTE(area->areawrite, (addr & (AREASIZE - 1))) = (val >> 8) & 0xff;
			FLAT_BYTE(area->areawrite, ((addr+1) & (AREASIZE - 1))) = val & 0xff;
		}
}

s8 domain_read_byte_mr(mrstruct *area, mem_domain md, u32 addr)
{
	if (area->read_byte)
		return area->read_byte(area, addr);
	else if (area->arearead)
		if (md == md_cpu)
			return BYTE(area->arearead, (addr & (AREASIZE - 1)));
		else
			return FLAT_BYTE(area->arearead, (addr & (AREASIZE - 1)));
	else
		return 0;
}

void
domain_write_byte_mr(mrstruct *area, mem_domain md, u32 addr, s8 val)
{
	if (area->write_byte)
		area->write_byte(area, addr, val);
	else if (area->areawrite)
		if (md == md_cpu)
			BYTE(area->areawrite, (addr & (AREASIZE - 1))) = val;
		else
			FLAT_BYTE(area->areawrite, (addr & (AREASIZE - 1))) = val;
}

u16 domain_read_word(mem_domain md, u32 addr)
{
	AREA_SETUP(md, addr);
	return domain_read_word_mr(area, md, addr);
}

void
domain_write_word(mem_domain md, u32 addr, u16 val)
{
	AREA_SETUP(md, addr);
	domain_write_word_mr(area, md, addr, val);
}

s8 domain_read_byte(mem_domain md, u32 addr)
{
	AREA_SETUP(md, addr);
	return domain_read_byte_mr(area, md, addr);
}

void
domain_write_byte(mem_domain md, u32 addr, s8 val)
{
	AREA_SETUP(md, addr);
	domain_write_byte_mr(area, md, addr, val);
}

/***********************************************************************/

MemoryEntry *mementlist;					  /* active memory list */

typedef struct MemoryCallbackList
{
	struct MemoryCallbackList *next;
	MemoryCallback cb;
}	MemoryCallbackList;

MemoryCallbackList *memcallbacks;

static void notify_callbacks(void)
{
	MemoryCallbackList *list = memcallbacks;
	while (list)
	{
		list->cb();
		list = list->next;
	}
}

typedef struct MemoryEntryCallbackList
{
	struct MemoryEntryCallbackList *next;
	MemoryEntryCallback cb;
}	MemoryEntryCallbackList;

MemoryEntryCallbackList *mementcallbacks;

static void notify_entry_callbacks(MemoryEntry *ent)
{
	MemoryEntryCallbackList *list = mementcallbacks;
	while (list)
	{
		list->cb(ent);
		list = list->next;
	}
}

/*
#define PATH_FOR_ENT(ent)	((ent->flags & MEMENT_STORED) == MEMENT_STORED ? ramspath : \
							 (ent->addr == 0) ? romspath : \
							 modulespath)
*/

#define PATH_FOR_ENT(ent)	((ent->flags & MEMENT_STORED) == MEMENT_STORED ? ramspath : \
							 (ent->flags & MEMENT_CART) ? modulespath : \
							 romspath)

#define CPU_MEM_ENT(ent)	(((ent->flags & MEMENT_DOMAIN) == MEMENT_CONSOLE))
							 
u8          modulerom[16384];

static void
memory_dump_list(const char *msg)
{
	MemoryEntry *lst;

	lst = mementlist;
	logger(LOG_USER, _("\nMemory map (%s):\n"), msg);
	while (lst) {
		logger(LOG_USER, _("'%s': Addr: >%04X, Offs: >%04X, Size: >%04X (>%04X)\n"),
			   lst->name,
			   lst->addr, lst->offs,
			   lst->realsize, lst->size);
		if (lst->filename)
			logger(LOG_USER, _("\tFilename: '%s'\n"), lst->filename);
		logger(LOG_USER, _("\tFlags:"));
		if ((lst->flags & MEMENT_STORED) == MEMENT_STORED)	logger( LOG_USER, "STORED, ");
		else if (lst->flags & MEMENT_RAM)	logger( LOG_USER, "RAM, ");
		else logger( LOG_USER, "ROM, ");
		if (lst->flags & MEMENT_BANK_1) logger( LOG_USER, "BANK_1, ");
		else if (lst->flags & MEMENT_BANK_2) logger( LOG_USER, "BANK_2, ");
		if (lst->flags & MEMENT_CART) logger( LOG_USER, "CART, ");
		switch (lst->flags & MEMENT_DOMAIN) {
		case MEMENT_CONSOLE:	logger( LOG_USER, "CONSOLE\n"); break;
		case MEMENT_GRAPHICS:	logger( LOG_USER, "GRAPHICS\n"); break;
		case MEMENT_VIDEO:		logger( LOG_USER, "VIDEO\n"); break;
		case MEMENT_SPEECH:		logger( LOG_USER, "SPEECH\n"); break;
//		case MEMENT_DSR:		logger( LOG_USER, "DSR\n"); break;
		default:	logger(LOG_INTERNAL, _("invalid MEMENT_xxx: %d\n"), (lst->flags & MEMENT_DOMAIN)>>MEMENT_DOMAIN_SHIFT); break;
		}
		lst = lst->next;
	}
}


/*	Free memory list */
void 
memory_free_list(void)
{
	MemoryEntry *lst = mementlist, *prev = 0L;

	while (lst) {
		lst = memory_remove_entry_from_list(prev, lst);
	}

	mementlist = NULL;
}

/*	Initialize memory list */
int
memory_init_list(void)
{
	memory_free_list();

	return 1;
}

/* 	Remove an entry from the memory map */
MemoryEntry *
memory_remove_entry_from_list(MemoryEntry *prev, MemoryEntry *ent)
{
	MemoryEntry *ret;

	if (prev)
		prev->next = ret = ent->next;
	else
		mementlist = ret = ent->next;

	memory_destroy_entry(ent);

	if (log_level(LOG_MEMORY) > 1)
		memory_dump_list(_("memory_remove_entry_from_list"));

	notify_callbacks();
	return ret;
}

/*	Add entry to memory map and destroy entries this overrides */
int
memory_add_entry_to_list(MemoryEntry *ent)
{
	MemoryEntry **lstptr = &mementlist, *lst, *prev = 0L;
	int ret = 1;

	/* if size is unknown, assume it covers everything */
	if (!ent->realsize) {
		my_assert(ent->size > 0);
		ent->realsize = ent->size;
	}

	/* go through the whole list and adjust existing entries 
	   if the new one will overlap by truncating or splitting them */
	while (*lstptr) {
		lst = *lstptr;

		/* This magic tells us that we need to obliterate some memory
		   segment already here.
		   The last two lines say: two different banks can coexist,
		   but banking stuff overrides non-banking stuff, and v.v. */
		if ((lst->flags & MEMENT_DOMAIN) == (ent->flags & MEMENT_DOMAIN) &&
			lst->addr >= ent->addr && 
			lst->addr + lst->realsize <= ent->addr + ent->realsize &&
			((lst->flags & MEMENT_BANKING) == (ent->flags & MEMENT_BANKING) ||
			 ((lst->flags ^ ent->flags) & MEMENT_BANKING) != MEMENT_BANKING)) {

			/* entry will be destroyed or split, save first */
			/*ret &=*/ memory_save_entry(lst);

			/* clear out memory map for new item */
			ent->realsize = ent->realsize;
			memory_unmap_entry(ent);

			/* split entry if necessary */
			if (ent->addr > lst->addr && 
				ent->addr + ent->realsize < lst->addr + lst->realsize) {

				MemoryEntry *nw = (MemoryEntry *)xmalloc(sizeof(MemoryEntry));
				*nw = *lst;

				/* lst is first chunk */
				lst->realsize = ent->addr - lst->addr;

				/* nw is last chunk */
				nw->realsize -= lst->realsize + ent->realsize;
				nw->offs += (ent->addr + ent->realsize) - nw->addr;
				nw->addr += ent->addr + ent->realsize;

				lst->addr = ent->addr + ent->realsize;
				logger(_L | L_2, _("Splitting entry '%s' into >%04X (>%04X), "
					   ">%04X (>%04X), and >%04X (>%04X)\n"),
					   lst->name,
					   lst->addr, lst->realsize,
					   nw->addr, nw->realsize,
					   ent->addr, ent->realsize);
			} else if (lst->addr < ent->addr && lst->realsize > ent->realsize) {
				/* new entry covers tail of old entry */
				lst->realsize = ent->addr - lst->addr;
				logger(_L | L_2, _("Shrinking entry '%s' to >%04X (>%04X)\n"),
					   lst->name,
					   lst->addr, lst->realsize);
			} else if (lst->realsize > ent->realsize) {
				/* new entry covers front of old entry */
				int shrink = ent->realsize;
				lst->offs += (ent->addr + ent->realsize) - lst->addr;
				lst->realsize -= shrink;
				lst->addr = ent->addr + ent->realsize;
				logger(_L | L_2, _("Shrinking entry '%s' to >%04X (>%04X)\n"),
					   lst->name,
					   lst->addr, lst->realsize);
			} else {
				/* old entry completely overriden */
				logger(_L | L_2, _("Overriding old entry '%s' at >%04X (>%04X)\n"),
					   lst->name,
					   lst->addr, lst->realsize);
				lst = memory_remove_entry_from_list(prev, lst);
				lstptr = prev ? &prev->next : &mementlist;
				/* prev is the same */
				continue;
			}
		}
		prev = lst;
		lstptr = &lst->next;
	}

	*lstptr = ent;
	(*lstptr)->next = 0L;

	notify_callbacks();

	return ret;
}

/*	Save an entry in the memory map to disk */
int
memory_save_entry(MemoryEntry *ent)
{
	if ((ent->flags & MEMENT_STORED) == MEMENT_STORED) {
		my_assert(ent->memact.arearead == ent->memact.areawrite &&
				  ent->memact.arearead != NULL &&
				  ent->filename);

		logger(_L | LOG_USER, _("Saving memory image to '%s'\n"), ent->filename);
		return data_save_binary(
				  "RAM",
				  ramspath, 
				  ent->filename, ent->fileoffs + ent->offs,
				  ent->memact.areawrite + ent->offs, 
				  CPU_MEM_ENT(ent),
				  ent->realsize - ent->offs);
	} else {
		/* others are ROM binaries or empty */
		return 1;
	}
}

/*	Load an entry in the memory map from disk */
int
memory_load_entry(MemoryEntry *ent)
{
	if ((ent->flags & MEMENT_STORED) == MEMENT_STORED) {
		my_assert(ent->memact.arearead == ent->memact.areawrite &&
				  ent->memact.arearead != NULL &&
				  ent->filename && 
				  ent->size > 0);

		logger(_L | LOG_USER, _("Restoring memory image from '%s'\n"), ent->filename);

		/* okay for this to fail, the first time at least */
		ent->realsize = data_load_binary(
									"RAM",
									ramspath, 
									ent->filename,
									ent->memact.areawrite + ent->offs, 
									CPU_MEM_ENT(ent),
									ent->offs + ent->fileoffs, 
									ent->size - ent->offs, 
									ent->size);

		if (!ent->realsize) {
			ent->realsize = ent->size;
			logger(_L | LOG_USER, _("Image '%s' ('%s') not found, resetting to zero\n"),
				   ent->filename, ent->name);
			memset(ent->memact.areawrite, 0, ent->realsize);
		}
		else if (ent->realsize < ent->size) {
			/* adjust file size next time we write this */
			ent->realsize = ent->size;
		}
		notify_entry_callbacks(ent);
		
	} else if (!(ent->flags & MEMENT_RAM)) {
		/* ROM */

		if (ent->filename && ent->memact.arearead && ent->realsize) {

			logger(_L | L_1, _("Reading ROM image '%s' at %d to >%04X, size %d\n"),
				   ent->name,
				   ent->fileoffs,
				   ent->addr, ent->size);

			if (!data_load_binary(
						"ROM",
						PATH_FOR_ENT(ent), 
						ent->filename,
						ent->memact.arearead + ent->offs,
						CPU_MEM_ENT(ent),
						ent->fileoffs + ent->offs,
						ent->realsize - ent->offs,
						ent->size < 0 ? -ent->size : 
						ent->size == 0 ? 0x10000 - ent->addr : ent->size))
				return 0;

			notify_entry_callbacks(ent);
		}

	} else {
		/* zero memory (RAM or empty ROM) */
		ent->realsize = ent->size < 0 ? 0x10000 - ent->addr : ent->size;
		logger(_L | L_2, _("Zeroes for '%s' at >%04X, size %d\n"),
			   ent->name,
			   ent->addr, ent->realsize);
	}
	return 1;
}

/*	Unmap an entry from the memory map */
void
memory_unmap_entry(MemoryEntry *ent)
{
	/* reset affected memory */
	logger(_L | L_2, _("Resetting memory area from >%04X to >%04X\n"),
		   ent->addr, ent->addr+ent->realsize-1);

	set_area_handler((ent->flags & MEMENT_DOMAIN) >> MEMENT_DOMAIN_SHIFT,
		ent->addr, ent->realsize, &zero_memory_handler);

	notify_entry_callbacks(ent);
}


mrstruct *memory_module_bank_handlers[2];
int memory_module_bank;
void
memory_set_module_bank(u8 bank)
{
	if (bank > 1 || !memory_module_bank_handlers[0] || !memory_module_bank_handlers[1])
		return; //logger(LOG_INTERNAL, _("memory_set_module_bank: Invalid bank or call to set %d\n"), bank);
	else {
		SET_AREA_HANDLER(0x6000, 0x2000, memory_module_bank_handlers[bank]);
		memory_module_bank = bank;
		notify_callbacks();
	}
}

static void
bwmrom_bank(const mrstruct *mr, u32 addr, s8 val)
{
	memory_set_module_bank((addr & 2) >> 1);
}



mrstruct *memory_console_bank_handlers[2];
int memory_console_bank;
void
memory_set_console_bank(u8 bank)
{
	if (bank > 1 || !memory_console_bank_handlers[0] || !memory_console_bank_handlers[1])
		return; //logger(LOG_INTERNAL, _("memory_set_console_bank: Invalid bank or call to set %d\n"), bank);
	else {
		SET_AREA_HANDLER(0x0000, 0x2000, memory_console_bank_handlers[bank]);
		memory_console_bank = bank;
		notify_callbacks();
	}
}

static void
bwmconsole_bank(const mrstruct *mr, u32 addr, s8 val)
{
	// avoid triggering on NULL writes
	if (addr >= 0x1000)
		memory_set_console_bank((addr & 2) >> 1);
}


/*	Map an entry into the memory map */
int
memory_map_entry(MemoryEntry *ent)
{
	if (!(ent->flags & MEMENT_RAM)) {
		/* fudge stuff for the module banks */
		if (ent->addr == 0x6000 &&
			(ent->flags & MEMENT_DOMAIN) == MEMENT_CONSOLE) {
			if (ent->flags & MEMENT_BANK_1) {
				ent->memact.write_byte = bwmrom_bank;
				memory_module_bank_handlers[0] = &ent->memact;
			} else if (ent->flags & MEMENT_BANK_2) {
				ent->memact.write_byte = bwmrom_bank;
				memory_module_bank_handlers[1] = &ent->memact;
			}
		}
		else if (ent->addr == 0x0000 &&
			(ent->flags & MEMENT_DOMAIN) == MEMENT_CONSOLE) {
			if (ent->flags & MEMENT_BANK_1) {
				ent->memact.write_byte = bwmconsole_bank;
				memory_console_bank_handlers[0] = &ent->memact;
			} else if (ent->flags & MEMENT_BANK_2) {
				ent->memact.write_byte = bwmconsole_bank;
				memory_console_bank_handlers[1] = &ent->memact;
			}
		}
	}

	/* setup memory routines for this entry */
	set_area_handler((ent->flags & MEMENT_DOMAIN) >> MEMENT_DOMAIN_SHIFT,
		ent->addr, ent->realsize, &ent->memact);
		
	return 1;
}

/*	Save volatile memory associated with loaded module */
void
memory_volatile_save(void)
{
	MemoryEntry *lst = mementlist;
	while (lst) {
		if ((lst->flags & MEMENT_STORED) == MEMENT_STORED) {
			memory_save_entry(lst);
		}
		lst = lst->next;
	}
}

/*	Load volatile memory associated with loaded module */
int
memory_volatile_load(void)
{
	MemoryEntry *lst = mementlist;
	while (lst) {
		if ((lst->flags & MEMENT_STORED) == MEMENT_STORED) {
//		if ((lst->flags & MEMENT_RAM) == 0) {
			if (!memory_load_entry(lst))
				return 0;
		}
		lst = lst->next;
	}

	return 1;
}

/*	Load all memory associated with loaded module */
int
memory_complete_load(void)
{
	MemoryEntry *lst = mementlist;
	while (lst) {
		if ((lst->flags & MEMENT_RAM) == 0) {
			if (!memory_load_entry(lst))
				return 0;
		}
		lst = lst->next;
	}

	return 1;
}


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
							  mrstruct *memact)
{
	MemoryEntry *nw = (MemoryEntry *)xmalloc(sizeof(MemoryEntry));
	OSSpec tmpspec;

	memset((void *)nw, 0, sizeof(MemoryEntry));

	my_assert(addr >= 0 && addr < PHYSMEMORYSIZE &&
			  !(addr & (AREASIZE-1)) && !(size & (AREASIZE-1)) &&
			  size >= -PHYSMEMORYSIZE && size <= PHYSMEMORYSIZE &&
			  !((flags & MEMENT_RAM) && (size <= 0)));

	nw->flags = flags;
	nw->fileoffs = fileoffs;
	nw->offs = 0;
	nw->addr = addr;
	nw->size = size;
	nw->realsize = 0;
	nw->filename = filename ? xstrdup(filename) : NULL;
	nw->name = name ? xstrdup(name) : NULL;
	nw->next = 0L;

	if (nw->filename) {
		nw->realsize = data_find_binary(
			PATH_FOR_ENT(nw), 
			nw->filename, 
			&tmpspec);
		if (!nw->realsize && !(nw->flags & MEMENT_RAM)) {
			command_logger(_L | LOG_USER | LOG_ERROR, _("Can't locate '%s'\n"),
				   nw->filename);
			return 0;
		}
//		if (!nw->size && nw->realsize) {
//			nw->size = nw->realsize;
//		}
		/* for large files selected, e.g., by accident */
		if (nw->size > 0 && nw->realsize > nw->size) {
			nw->realsize = nw->size;
		} else if (nw->size < 0 && nw->realsize > -nw->size) {
			nw->realsize = -nw->size;
		} else if (nw->realsize + nw->addr > 0x10000) {
			nw->realsize = 0x10000 - nw->addr;
		}
	}

	if (memact) {
		my_assert(!(nw->flags & MEMENT_USER));
		nw->memact = *memact;

	} else {

		/* create memory for this user entry */
		my_assert(nw->flags & MEMENT_USER);

		nw->memact.areamemory = (u8 *)xmalloc(nw->realsize);
		nw->memact.arearead = nw->memact.areamemory;
		if (nw->flags & MEMENT_RAM)
			nw->memact.areawrite = nw->memact.areamemory;
		else
			nw->memact.areawrite = 0L;

/*
		if ((nw->flags & MEMENT_DOMAIN) == MEMENT_VIDEO)
		{
			nw->memact.read_word = __areahandlers[md_video][0].read_word;
			nw->memact.read_byte = __areahandlers[md_video][0].read_byte;
			nw->memact.write_word = __areahandlers[md_video][0].write_word;
			nw->memact.write_byte = __areahandlers[md_video][0].write_byte;
		}
*/
	}

	return nw;
}

void
memory_destroy_entry(MemoryEntry *ent)
{
	if (ent->name)
		xfree(ent->name);
	if (ent->filename)
		xfree(ent->filename);
	if ((ent->flags & MEMENT_USER) && ent->memact.areamemory)
		xfree(ent->memact.areamemory);

	xfree(ent);
}

/*	Do it all */
int
memory_insert_new_entry(int flags, u32 addr, s32 size,
						char *name, char *filename, u32 fileoffs, 
						mrstruct *memact)
{
	MemoryEntry *ent = memory_new_entry(flags, addr, size,
										name, filename, fileoffs, memact);
	return (ent && 
			memory_add_entry_to_list(ent) && 
			memory_load_entry(ent) &&
			memory_map_entry(ent));
}

MemoryEntry *memory_lookup_entry(u32 addr, mem_domain md, int live)
{
	MemoryEntry *ent;
	for (ent = mementlist; ent; ent = ent->next)
		if (((ent->flags&MEMENT_DOMAIN) >> MEMENT_DOMAIN_SHIFT) == md
			&& ent->addr <= addr
			&& ent->addr + ent->realsize > addr)
		{
			// return correct bank
			if (!live 
				|| !(ent->flags & MEMENT_BANKING)
				|| (memory_module_bank == 0 && (ent->flags & MEMENT_BANK_1))
				|| (memory_module_bank == 1 && (ent->flags & MEMENT_BANK_2)))
				return ent;
		}
	return 0L;
}

DECL_SYMBOL_ACTION(memory_define_entry)
{
	char *flagptr, *fnameptr, *nameptr;
	MemoryEntry *nw;
	long flags;
	int addr, size, fileoffs;
	int state;

	if (task == csa_READ) {
		char buf[32], *bptr = buf;
		static MemoryEntry *ptr;

		if (iter == 0) {
			ptr = mementlist;
		}

		// the non-user memory items are available by
		// default; the cartridge items are defined by
		// ReplaceModule (which also gives the name of the
		// cart for the UI)
		while (ptr 
			   && ((ptr->flags & MEMENT_USER) == 0
				   || (ptr->flags & MEMENT_CART) != 0))
			ptr = ptr->next;

		if (ptr == 0L) {
			return 0;
		}

		if ((ptr->flags & MEMENT_STORED) == MEMENT_STORED)
			*bptr++ = 'S'; 
		else if (ptr->flags & MEMENT_RAM) 	
			*bptr++ = 'W'; 
		else
			*bptr++ = 'R';
		
		if (ptr->flags & MEMENT_CART)		*bptr++ = 'M';
		if (ptr->flags & MEMENT_BANK_1)		*bptr++ = '1';
		else if (ptr->flags & MEMENT_BANK_2) *bptr++ = '2';

		switch (ptr->flags & MEMENT_DOMAIN) {
		case MEMENT_CONSOLE:	*bptr++ = 'C'; break;
		case MEMENT_GRAPHICS:	*bptr++ = 'G'; break;
		case MEMENT_VIDEO:		*bptr++ = 'V'; break;
		case MEMENT_SPEECH:		*bptr++ = 'S'; break;
//		case MEMENT_DSR:		*bptr++ = 'D'; break;
		default:	command_logger(LOG_INTERNAL | LOG_FATAL, _("invalid MEMENT_xxx: %d\n"), (ptr->flags & MEMENT_DOMAIN)>>MEMENT_DOMAIN_SHIFT); break;
		}
		*bptr = 0;

		command_arg_set_string(SYM_ARG_1st, buf);
		command_arg_set_num(SYM_ARG_2nd, ptr->addr);
		command_arg_set_num(SYM_ARG_3rd, ptr->size);
		command_arg_set_string(SYM_ARG_4th, ptr->filename);
		command_arg_set_num(SYM_ARG_5th, ptr->fileoffs);
		command_arg_set_string(SYM_ARG_6th, ptr->name);

		ptr = ptr->next;
		return 1;
	}

	// task == csa_WRITE

	command_arg_get_string(SYM_ARG_1st, &flagptr);
	command_arg_get_num(SYM_ARG_2nd, &addr);
	command_arg_get_num(SYM_ARG_3rd, &size);
	command_arg_get_string(SYM_ARG_4th, &fnameptr);
	command_arg_get_num(SYM_ARG_5th, &fileoffs);
	command_arg_get_string(SYM_ARG_6th, &nameptr);

	flags = MEMENT_USER;
	state = 0;
	while (*flagptr)
	{
		if (state == 0) {
			int type = toupper(*flagptr);
			if (type == 'S')
				flags |= MEMENT_STORED;
			else if (type == 'W')
				flags |= MEMENT_RAM;
			else if (type == 'R')
				flags &= ~MEMENT_RAM;
			else {
				command_logger(_L | LOG_ERROR | LOG_USER, 
					   _("Unknown memory type '%c' (at '%s')\n"),
					   toupper(*flagptr), flagptr);
				return 0;
			}			
			state++;
		} else {

			switch (toupper(*flagptr)) {
			case 'M':
				{
					// this is such a hack.  if we don't clear it
					// here, we end up generating session files that
					// might have invalid "LoadModule"/"ReplaceModule"
					// lines, if v9t9.cnf has a "LoadModule" line,
					// a session file is loaded without a "LoadModule"
					// line, and it is saved again.
					if (addr == 0x6000) {
						flags |= MEMENT_CART; 
						loaded_module = 0L;
					}
					break;
				}

			case '1':	flags = (flags & ~MEMENT_BANK_2) | MEMENT_BANK_1; break;
			case '2':	flags = (flags & ~MEMENT_BANK_1) | MEMENT_BANK_2; break;

			case 'C':	flags = (flags & ~MEMENT_DOMAIN) | MEMENT_CONSOLE; break;
			case 'G':	flags = (flags & ~MEMENT_DOMAIN) | MEMENT_GRAPHICS; break;
			case 'V':	flags = (flags & ~MEMENT_DOMAIN) | MEMENT_VIDEO; break;
			case 'S':	flags = (flags & ~MEMENT_DOMAIN) | MEMENT_SPEECH; break;
//			case 'D':	flags = (flags & ~MEMENT_DOMAIN) | MEMENT_DSR; break;

			default:
				command_logger(_L | LOG_ERROR | LOG_USER, 
					   _("Unknown memory flag '%c' (at '%s')\n"),
					   *flagptr, flagptr);
				return 0;
			}
		}
		flagptr++;
	}

	if (addr < 0 || addr >= 0x10000) {
		command_logger(_L | LOG_ERROR | LOG_USER, _("Illegal address (>%04x) for entry\n"), addr);
		return 0;
	}

	if (addr & 0x1fff) {
		if (addr & (AREASIZE-1)) {
			command_logger(_L | LOG_ERROR | LOG_USER, _("Address is impossible to use, "
				   "minimum granularity is >%04x bytes\n"), AREASIZE);
			return 0;
		} else {
//			command_logger(_L | LOG_WARN | LOG_USER, _("An address should start on a multiple "
//				   "of >2000 bytes (got >%04x)...\n"), addr);
		}
	}

	if (size < -0x10000 || size >= 0x10000) {
		command_logger(_L | LOG_ERROR | LOG_USER, _("Illegal size (%d) for entry, "
			   "should have magnitude between 0 and >FFFF\n"), size);
		return 0;
	}

	if (addr + size >= PHYSMEMORYSIZE) {
		command_logger(_L | LOG_WARN | LOG_USER, _("Size (>%04X) for entry "
			   "plus starting address (>%04X) too large; truncating\n"), 
					   size, addr);
		size = PHYSMEMORYSIZE - addr;
	}

	if ((flags & MEMENT_RAM) && size <= 0) {
		command_logger(_L | LOG_ERROR | LOG_USER, _("Illegal size for RAM entry, must be >= 0 (%d)\n"), size);
		return 0;
	}

	if (size & 0x1fff) {
		if (size & (AREASIZE-1)) {
			command_logger(_L | LOG_ERROR | LOG_USER, _("Entry is impossible to allocate, "
				   "minimum granularity is >%04X bytes\n"), AREASIZE);
			return 0;
		} else {
//			command_logger(_L | LOG_WARN | LOG_USER, _("Entry size should be a multiple "
//				   "of >2000 bytes (got >%04x)...\n"), size);
		}
	}
///

	nw = memory_new_entry(flags, addr, size, nameptr, fnameptr, fileoffs, 0L);
	if (!nw)
		return 0;

	if (memory_add_entry_to_list(nw) && 
		memory_load_entry(nw) && 
		memory_map_entry(nw)) {
		if (log_level(LOG_MEMORY) > 1)
			memory_dump_list("memory_define_entry");
		return 1;
	} else {
		return 0;
	}
}

DECL_SYMBOL_ACTION(memory_dump)
{
	memory_dump_list("memory_dump");
	return 1;
}

static void 
memory_mmio_init(void);

DECL_SYMBOL_ACTION(memory_default_list)
{
	memory_console_bank_handlers[0] = memory_console_bank_handlers[1] = NULL;
	memory_mmio_init();
	memory_ram_init();
	vdp_memory_init();
	gpl_memory_init();	// does nothing currently
	speech_memory_init();
	return 1;
}

/***********************************************************************/

void
memory_ram_init(void)
{
//	if (!low_expansion_memory_handler.areamemory)
//		low_expansion_memory_handler.areamemory = (u8 *)xmalloc(0x2000);

	memory_insert_new_entry(MEMENT_CONSOLE | MEMENT_RAM, 0x2000, 0x2000, 
							_("Low expansion RAM"), 
							0L /*filename*/, 0L /*fileoffs*/, 
							&low_expansion_memory_handler);

//	if (!std_console_ram_handler.areamemory)
//		std_console_ram_handler.areamemory = (u8 *)xmalloc(0x400);

	if (!isenhconsoleram)
		memory_insert_new_entry(MEMENT_CONSOLE | MEMENT_RAM, 0x8000, 0x0400, 
								_("Console RAM (256 bytes)"),
								0L /*filename*/, 0L /*fileoffs*/, 
							   &std_console_ram_handler);
	else
		memory_insert_new_entry(MEMENT_CONSOLE | MEMENT_RAM, 0x8000, 0x0400, 
							   _("Console RAM (1K)"),
								0L /*filename*/, 0L /*fileoffs*/, 
							   &enh_console_ram_handler);

//	if (!high_expansion_memory_handler.areamemory)
//		high_expansion_memory_handler.areamemory = (u8 *)xmalloc(0x6000);

	memory_insert_new_entry(MEMENT_CONSOLE | MEMENT_RAM, 0xA000, 0x6000, 
							_("High expansion RAM"),
							0L /*filename*/, 0L /*fileoffs*/, 
							&high_expansion_memory_handler);
}

static void 
memory_mmio_init(void)
{
	memory_insert_new_entry(MEMENT_CONSOLE, 0x8400, 0x400, 
						   _("Sound MMIO"), 
							0L /*filename*/, 0L /*fileoffs*/, 
							&console_sound_handler);
		
	memory_insert_new_entry(MEMENT_CONSOLE, 0x8800, 0x400, 
						   _("VDP read MMIO"), 
							0L /*filename*/, 0L /*fileoffs*/, 
							&console_vdp_read_handler);

	memory_insert_new_entry(MEMENT_CONSOLE, 0x8C00, 0x400, 
						   _("VDP write MMIO"),
							0L /*filename*/, 0L /*fileoffs*/, 
							&console_vdp_write_handler);
	
	memory_insert_new_entry(MEMENT_CONSOLE, 0x9000, 0x400, 
						   _("Speech read MMIO"), 
							0L /*filename*/, 0L /*fileoffs*/, 
							&console_speech_read_handler);

	memory_insert_new_entry(MEMENT_CONSOLE, 0x9400, 0x400, 
						   _("Speech write MMIO"), 
							0L /*filename*/, 0L /*fileoffs*/, 
							&console_speech_write_handler);

	memory_insert_new_entry(MEMENT_CONSOLE, 0x9800, 0x400, 
						   _("GROM read MMIO"), 
							0L /*filename*/, 0L /*fileoffs*/, 
							&console_grom_read_handler);

	memory_insert_new_entry(MEMENT_CONSOLE, 0x9C00, 0x400, 
						   _("GROM/GRAM write MMIO"), 
							0L /*filename*/, 0L /*fileoffs*/, 
							&console_grom_write_handler);
}

void memory_register_callback(MemoryCallback func)
{
	MemoryCallbackList *list;

	list = memcallbacks;
	while (list)
	{
		if (list->cb == func)
			return;
		list = list->next;
	}

	list = (MemoryCallbackList *)xmalloc(sizeof(MemoryCallbackList));
	list->next = memcallbacks;
	list->cb = func;
	memcallbacks = list;
}

void memory_register_entry_callback(MemoryEntryCallback func)
{
	MemoryEntryCallbackList *list;

	list = mementcallbacks;
	while (list)
	{
		if (list->cb == func)
			return;
		list = list->next;
	}

	list = (MemoryEntryCallbackList *)xmalloc(sizeof(MemoryEntryCallbackList));
	list->next = mementcallbacks;
	list->cb = func;
	mementcallbacks = list;
}

void
memory_init(void)
{
	logger(_L | L_0, _("Initializing memory...\n"));

	memcallbacks = 0;

	memset((void *)__areahandlers, 0, sizeof(__areahandlers));
	memory_init_list();

	memory_ram_init();
   
	memory_mmio_init();

	vdp_memory_init();
	gpl_memory_init();
}

