/*
  cru.c							-- generic Communications Register Unit routines

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

#define __CRU__

#include <stdlib.h>

#include "v9t9_common.h"
#include "cru.h"
#include "9901.h"

#define _L	 LOG_CRU | LOG_INFO

/************************************************************/

/*	In order to most efficiently handle CRU requests,
	we no longer support multi-bit CRU operations.  */
typedef struct crudevicelist {
	u32         addr;		/*  base address */
	crufunc    *handler;
	struct crudevicelist *next;
} crudevicelist;

static crudevicelist *cruwritedevices, *crureaddevices;

void        
cruinit(void)
{
	cruwritedevices = crureaddevices = NULL;
}


/***************************************************/

static void
crudevicedump(void)
{
	struct crudevicelist *ptr;

	logger(_L|LOG_USER, _("crudevicedump:"));
	logger(_L|LOG_USER, _("\tREAD:\n"));
	ptr = crureaddevices;
	while (ptr) {
		logger(_L |LOG_USER, _("\t\tdevice = >%04X (%p)\n"), ptr->addr, ptr->handler);
		ptr = ptr->next;
	}

	logger(_L | LOG_USER, _("\tWRITE:"));
	ptr = cruwritedevices;
	while (ptr) {
		logger(_L | LOG_USER, _("\t\tdevice = >%04X (%p)\n"), ptr->addr, ptr->handler);
		ptr = ptr->next;
	}
}

DECL_SYMBOL_ACTION(dump_cru_list)
{
	if (task == csa_WRITE) {
		crudevicedump();
	}
	return 1;
}

/*
	Insert an address into the CRU device list.
	
	Sorts entries by address.

	Returns 1 if success, or 0 if failure.
*/
int
cruadddevice(int rw, u32 addr, u32 bits, crufunc * handler)
{
	crudevicelist **head, *ptr, *tmp;
	crudevicelist *new;

	if (addr >= 0x2000) {
		logger(_L | LOG_ERROR | LOG_USER, _("cruadddevice:  address 0x%x is invalid\n"),
			 addr);
		return 0;
	}

	if (bits > 1) {
		logger(_L | LOG_ERROR | LOG_USER,
			 _("cruadddevice:  only single-bit ranges supported (0x%04X, %d)\n"),
			 addr, bits);
		return 0;
	}

	if (rw == CRU_READ)
		head = &crureaddevices;
	else if (rw == CRU_WRITE)
		head = &cruwritedevices;
	else {
		logger(_L | LOG_ERROR | LOG_USER,
			 _("cruadddevice:  invalid 'rw' flag (%d) passed\n"), rw);
		return 0;
	}

	new = (struct crudevicelist *) xmalloc(sizeof *new);

	new->addr = addr;
	new->handler = handler;
	new->next = NULL;

	if (*head) {
		ptr = *head;
		tmp = NULL;
		while (ptr && addr >= ptr->addr) {
			tmp = ptr;
			ptr = ptr->next;
		}
		if (ptr && addr == ptr->addr) {
			logger(_L | LOG_ERROR | LOG_USER,
				 _("cruadddevice:  overlapping I/O (0x%x)\n"), ptr->addr);
			return 0;
		}
		if (tmp) {
			new->next = tmp->next;
			tmp->next = new;
		} else {
			new->next = ptr;
			*head = new;
		}
	} else
		*head = new;

	if (log_level(LOG_CRU) >= 2)
		crudevicedump();

	return 1;
}

int
crudeldevice(int rw, u32 addr, u32 bits, crufunc * handler)
{
	crudevicelist **head, *ptr, *tmp;

	if (addr >= 0x2000) {
		logger(_L | LOG_ERROR | LOG_USER, _("crudeldevice:  address 0x%x is invalid\n"),
			 addr);
		return 0;
	}

	if (bits > 1) {
		logger(_L | LOG_ERROR | LOG_USER,
			 _("crudeldevice:  only single-bit ranges supported (0x%04X, %d)\n"),
			 addr, bits);
		return 0;
	}

	if (rw == CRU_READ)
		head = &crureaddevices;
	else if (rw == CRU_WRITE)
		head = &cruwritedevices;
	else {
		logger(_L | LOG_ERROR | LOG_USER,
			 _("crudeldevice:  invalid 'rw' flag (%d) passed\n"), rw);
		return 0;
	}

	while (*head) {
		tmp = NULL;

		if ((*head)->addr == addr && handler == (*head)->handler) {
			ptr = *head;
			*head = (*head)->next;
			xfree(ptr);
			return 1;
		}
		head = &(*head)->next;
	}

	logger(_L | LOG_ERROR | LOG_USER, _("crudeldevice:  device not found (0x%x)\n"),
		 addr);
	return 0;
}


/***************************************************/

void
cruwrite(u32 addr, u32 val, u32 num)
{
	crudevicelist *ptr = cruwritedevices;

	addr &= 0x1fff;
	logger(_L | L_2, _("CRU write: >%04X[%d], %04X\n"), addr, num, val & 0xffff);

	/* on 99/4A console, 0x0000 through 0x0040 map to 0x40*k through 0x1000 */

	if (addr < 0x1000) {
		addr &= 0x3f;
	}

	if (addr >= 0x30) {
		setclockmode9901(0);
	}

	while (ptr && num) {
		if (ptr->addr > addr) {
			/* if we've already passed a handler for addr,
			   shift out the lost bits */
			int         lost = (ptr->addr - addr) / 2;

			if (lost > num)
				lost = num;


			val >>= lost;
			num -= lost;
			addr = ptr->addr;
			logger(_L | L_2, _("cruwrite:  skipping bits, range is now %04X[%d]\n"), addr,
				 num);
		}

		if (addr == ptr->addr && num) {
			int         used;
			u32         mask;

			logger(_L | L_2, _("cruwrite:  handling %04X[%d] with %04X\n"),
				 addr, num, ptr->addr);

			used = 1;
			mask = ~(~0 << used);
			(ptr->handler) (addr, val & mask, used);

			num -= used;
			addr += used * 2;
			val >>= used;
		}
		ptr = ptr->next;

	}
}

/*
	Routines write 1.  Shift answer left into output.
*/

#include "keyboard.h"

u32 cruread(u32 addr, u32 num)
{
	crudevicelist *ptr = crureaddevices;
	u32         orgaddr = addr;
	u32         val = 0;

	//(num >= 8) ? 0xff00 : 0xffff;

	addr &= 0x1fff;
	orgaddr = addr;
	logger(_L | L_2, _("CRU read: >%04X[%d] = \n"), addr, num);

	if (addr >= 0x30) {
		setclockmode9901(0);
	}

	while (ptr && num) {

		/* if we've already passed a handler for addr,
		   shift out the lost bits */
		if (ptr->addr > addr) {
			int         lost = (ptr->addr - addr) / 2;

			if (lost > num)
				lost = num;

			num -= lost;
			addr = ptr->addr;
			logger(_L | L_2, _("cruread:  skipping bits, range is now %04X[%d]\n"), addr,
				 num);
		}

		if (addr == ptr->addr && num) {
			int         used;
			u32         mask, bits, shift;

			logger(_L | L_2, _("cruread:  handling %04X[%d] with %04X\n"),
				 addr, num, ptr->addr);

			used = 1;
			mask = ~((~0) << used);
			shift = (addr - orgaddr) / 2;
			bits = ((ptr->handler) (addr, val, used) & mask);
			val = (val & ~(mask << shift)) | (bits << shift);
			num -= used;
			addr += used * 2;
		}
		ptr = ptr->next;

	}
	if (orgaddr == 0x6 && 0xff != (val & 0xff))
		logger(_L | L_2, "keyrow:  %2X/%04X\n", crukeyboardmap[crukeyboardcol],
			 val & 0xffff);
	return val & 0xffff;
}
