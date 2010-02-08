/*
  grom.c						-- GROM/GRAM routines

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

#include "v9t9_common.h"
#include "memory.h"
#include "grom.h"

//u8          gplrom[65536];
static u16	  gromaddr;
static bool		gromaddrflag;	// not for real use, only for debugger

/*	GROM has a strange banking scheme where the upper portion
	of the address does not change when incremented;
	this acts like an 8K bank. */
static u16
grom_mmio_get_next_addr(u16 addr)
{
	return (((addr+1) & 0x1fff) | (gromaddr & 0xe000));
}

u16
grom_mmio_get_addr(void)
{
	return gromaddr;
}

u8
grom_mmio_get_addr_byte(void)
{
	return grom_mmio_get_next_addr(gromaddr) >> 8;
}

void
grom_mmio_set_addr(u16 addr)
{
	gromaddr = addr;
}

bool 
grom_mmio_addr_is_complete(void)
{
	return !gromaddrflag;
}

void
grom_mmio_write(u32 addr, u8 val)
{
	if (addr) {
		gromaddr = (gromaddr << 8) | val;
		gromaddrflag ^= 1;
		logger(LOG_CPU | LOG_INFO | L_2, "GROMSETADDR: %04X\n", gromaddr);
	} else {
		gromaddrflag = 0;
		domain_write_byte(md_graphics, gromaddr, val);
		gromaddr = grom_mmio_get_next_addr(gromaddr);
	}
}

s8 grom_mmio_read(u32 addr)
{
	register u8 ret;
	register u32 temp;

	if (addr) {
		temp = grom_mmio_get_next_addr(gromaddr);
		ret = grom_mmio_get_addr_byte();
		gromaddr = temp << 8;
		gromaddrflag ^= 1;
		logger(LOG_CPU | LOG_INFO | L_2, "GROMREADADDR: %02X / %04X\n", ret,
			   gromaddr);
		return ret;
	} else {
		gromaddrflag = 0;
		ret = domain_read_byte(md_graphics, gromaddr);
		logger(LOG_CPU | LOG_INFO | L_2, "GROMADDR: %04X\n", gromaddr);
		gromaddr = grom_mmio_get_next_addr(gromaddr);
		return ret;
	}
}

/*	Initially set up this memory as empty.  We expect ROMs to be
	loaded by the config file.  */

void
gpl_memory_init(void)
{
//	memory_insert_new_entry(MEMENT_GRAPHICS, 0x0000, 0x10000, 
//						   "Graphics ROM", 0L, &zero_memory_handler);
}
