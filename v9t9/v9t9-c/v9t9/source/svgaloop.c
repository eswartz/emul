/*
  svgaloop.c					-- main loop for SVGAlib frontend

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

#include <signal.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/time.h>

#include "v9t9_common.h"
#include "system.h"
#include "timer.h"
#include "v9t9.h"
#include "log.h"
#include "command_rl.h"
#include "moduleconfig.h"
#include "v9t9_module.h"

#include "unixmain.h"

#define _L	 LOG_INTERNAL | LOG_INFO

#if LINUX_SVGA_VIDEO && LINUX_SVGA_KEYBOARD

extern	int	console_fd;

int
svga_system_init(void)
{
	// reclaim setuid root privileges
	if (setreuid(getuid(), 0) < 0) {
		logger(LOG_USER|LOG_ERROR, 
		   _("Can't get setuid root privileges!\n"
			"As root, execute 'chown root:root v9t9; chmod +s v9t9'\n"
			"to use the SVGAlib interface.\n\n"));
		return 1;
	} else {
		int ret = vga_init();

		// lose root privileges, if any
		setreuid(-1,getuid());

		return ret == 0;
	}
}

int
svga_system_loop(void)
{
	int ret;

	while (1) {
		while (TM_Ticked) {
			TM_TickHandler(0);
//			TM_Ticked--;
			TM_Ticked = 0;
		}
		ret = v9t9_execute();
		if (ret == em_TooFast)
			unix_system_pause();
		else if (ret == em_Quitting || ret == em_Dying)
			break;
	}
	return ret == em_Dying;
		
}

#endif // LINUX_SVGA_VIDEO && LINUX_SVGA_KEYBOARD
