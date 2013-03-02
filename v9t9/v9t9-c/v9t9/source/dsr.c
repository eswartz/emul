/*
  dsr.c							-- utility functions for DSR modules

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

#include "v9t9_common.h"
#include "dsr.h"
#include "9900.h"
#include "memory.h"
#include "moduleconfig.h"
#include "configfile.h"
#include "opcode_callbacks.h"

static vmModule *active = NULL;

void
dsr_set_active(vmModule * module)
{
	if (!module)
		active = NULL;
	else {
		if (module->type != vmTypeDSR)
			module_logger(module, LOG_FATAL | LOG_INTERNAL,
				   _("dsr_set_active:  trying to install bogus module"));
		active = module;
	}
}

/*
 *	Your DSR module may use the DSR opcode range (OP_DSR) to make
 *	callbacks into V9t9 to handle subroutine calls.  This routine
 *	handles those opcodes by calling the DSR module 'filehandler'
 *	callback.
 */
void
emulate_dsr(void)
{
	u16         callpc = pc - 2;
	u16			opcode = memory_read_word(callpc);
	u16			crubase = memory_read_word(0x83D0);

	if (callpc >= 0x4000 && callpc < 0x6000) {
		u32         base;

		/*  Only respond if we have an active module whose
		   base matches that which DSRLNK is currently scanning. */
		if (active && active->m.dsr->getcrubase(&base) == vmOk
			&& crubase == base) {
			logger(LOG_CPU | LOG_INFO | L_1, "emulate_dsr:  pc = %d [%4X]\n",
				   callpc, opcode);

			// on success, return to DSR handler, to return an
			// error or otherwise terminate instead of continuing
			// to scan CRU bases
			if (active->m.dsr->filehandler(opcode - OP_DSR) == vmOk) {
				pc = register (11);
			}
		}
	}
}

int dsr_get_disk_count(void)
{
#ifdef EMU_DISK_DSR
	if (emuDiskDSR.runtimeflags & vmRTInUse)
		return 5;
#endif
#ifdef REAL_DISK_DSR
	if (realDiskDSR.runtimeflags & vmRTInUse)
		return 3;
#endif
	return 0;
}

int dsr_is_real_disk(int disk)
{
#ifdef REAL_DISK_DSR
	if (realDiskDSR.runtimeflags & vmRTInUse)
#ifdef EMU_DISK_DSR
		if (emuDiskDSR.runtimeflags & vmRTInUse)
			return disk == 1 || disk == 2;
		else	
#endif
			return disk >= 1 && disk <= 3;
	else
		return 0;
#else
	return 0;
#endif
}

int dsr_is_emu_disk(int disk)
{
#ifdef EMU_DISK_DSR
	if (emuDiskDSR.runtimeflags & vmRTInUse)
#ifdef REAL_DISK_DSR
		if (realDiskDSR.runtimeflags & vmRTInUse)
			return disk >= 3 && disk <= 5;
		else
#endif
			return disk >= 1 && disk <= 5;
	else
		return 0;
#else
	return 0;
#endif
}

const char *dsr_get_disk_info(int disk)
{
#ifdef REAL_DISK_DSR
	if (dsr_is_real_disk(disk))
		//return OS_NameSpecToString1(&diskname[disk-1]);
		return diskname[disk-1];
	else 
#endif
#ifdef EMU_DISK_DSR
	if (dsr_is_emu_disk(disk))
		return OS_PathSpecToString1(&emudiskpath[disk-1]);
	else
#endif
		return 0L;
}

int	dsr_set_disk_info(int disk, const char *path)
{
	char command[1024];
	char *str;
#ifdef REAL_DISK_DSR
	if (dsr_is_real_disk(disk)) {
		snprintf(command, sizeof(command), "DiskImage%d = \"%s\"\n", 
				 disk, str=escape(path,0));
		xfree(str);
		return command_exec_text(command);
	}
#endif
#ifdef EMU_DISK_DSR
	if (dsr_is_emu_disk(disk)) {
		snprintf(command, sizeof(command), "DSK%dPath = \"%s\"\n", 
				 disk, str=escape(path,0));
		xfree(str);
		return command_exec_text(command);
	}
#endif
	return 0;
}
