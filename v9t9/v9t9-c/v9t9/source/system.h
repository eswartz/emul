/*
  system.h						-- prototypes for main loop module

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

#ifndef __MAIN_H__
#define __MAIN_H__

#include "log.h"

//	Intercept commands from user (Interactive=on)
void	system_getcommands(void);

//	Initialize timer to TM_HZ hertz
void	system_timer_init(void);
void	system_timer_install(void);
void	system_timer_uninstall(void);

//	emit text to log (do not add newline)
void	system_log(u32 srcflags, const char *text);

//	Emit a status item for UI.  
void	system_report_status(status_item item, va_list va);

//	signal to inform frontend that debugger is enabled/disabled
//	or paused/running.  Do not call debugger_enable or execution_pause
//	in here!
void	system_debugger_enabled(bool enabled);
void	system_execution_paused(bool paused);

#endif

