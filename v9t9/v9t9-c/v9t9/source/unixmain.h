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

#include <stdarg.h>

#include "log.h"

#include "centry.h"


//	Set when the timer ticks and we should call TM_TickHandler()
extern unsigned int TM_Ticked;

void
unix_system_pause(void);

void
unix_system_log(u32 srcflags, const char *text);

void
unix_system_getcommands(void);

void
unix_system_report_status(status_item item, va_list va);

void
unix_system_debugger_enabled(bool enabled);

void
unix_system_execution_paused(bool paused);

/*	handlers for GTK frontend */

int
GTK_system_init(void);

int
GTK_system_loop(void);

void
GTK_system_getcommands(void);

void
GTK_system_log(u32 srcflags, const char *text);

void
GTK_system_report_status(status_item item, va_list va);

void
GTK_system_debugger_enabled(bool enabled);

void
GTK_system_execution_paused(bool paused);

/*	handlers for xlib frontend */

int
xlib_system_init(void);

int
xlib_system_loop(void);

/*	handlers for svga frontend */

int
svga_system_init(void);

int
svga_system_loop(void);

#include "cexit.h"
