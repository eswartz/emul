/*
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

#ifndef __GROM_H__
#define __GROM_H__

extern void grom_mmio_write(u32 addr, u8 val);
extern s8 	grom_mmio_read(u32 addr);

extern u16	grom_mmio_get_addr(void);
extern void	grom_mmio_set_addr(u16 addr);
extern bool grom_mmio_addr_is_complete(void);

extern u8	grom_mmio_get_addr_byte(void);

extern void	gpl_memory_init(void);

#endif
