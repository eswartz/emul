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

/*
	DSR (device service routine) emulation.  All devices that
	conform to the TI standard file API have hardware
	emulation (through CRU traps) and possibly software
	emulation (through the DSR routine).
	
	The way to add a new DSR is:  (1) make a new CRU module
	for the I/O address space (like >1200...>12FF).  The handler
	for SBO >0 should turn on the ROM for the DSR at >4000 and
	update currentdsr.  See examples.  (2) possibly add a handler
	
*/

#ifndef __DSR_H__
#define __DSR_H__

#include "centry.h"

void	dsr_set_active(vmModule *dsrmodule);

int		dsr_get_disk_count(void);		

// disks 1..x are valid for these calls

int		dsr_is_real_disk(int disk);		// disk==1..5

int		dsr_is_emu_disk(int disk);		// disk==1..5

const char *dsr_get_disk_info(int disk);
int   	dsr_set_disk_info(int disk, const char *path);

extern OSPathSpec emudiskpath[5];

extern char *diskimagepath;
extern char diskname[3][OS_NAMESIZE];

#include "cexit.h"

#endif
