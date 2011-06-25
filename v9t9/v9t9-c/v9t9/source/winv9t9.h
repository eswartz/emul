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

#ifndef __WINv9t9_h__
#define __WINv9t9_h__

/*
#if __MWERKS__
#  if __cplusplus
#	 include "winheaders.mch++"
#  else
#	 include "winheaders.mch"
#  endif
#else
#  if __cplusplus
#    include "winheaders.pch++"
#  else
#    include "winheaders.pch"
#   endif
#endif
*/
//#define WIN32_LEAN_AND_MEAN
#include <windows.h>
//#include <windowsx.h>
//#include <mmsystem.h>
//#define HMONITOR_DECLARED
//#include <ddraw.h>
//#include <dsound.h>
#define NOCOMPMAN
#define NOVIDEO
#define NOAVIFMT
#define NOMMREG
#define NOAVIFILE
#define NOMCIWND
#define NOAVICAP
#define NOMSACM
#include <vfw.h>
#include <stdarg.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <ctype.h>

#include "log.h"
#include "centry.h"

extern	int		TM_Ticked;
extern	UINT	timer;			/* my timer */
extern	HACCEL	hAccelerators;
#define FAIL(x) 	do { MessageBox((HWND)NULL,x,"D'oh!",MB_OK); v9t9_term(1); }  while (0)

extern	HINSTANCE	myHInst, myHPreInst;	// startup params for video modules
extern	int			mynCmdShow;			

LRESULT CALLBACK KeyboardMessageHandler(HWND hWnd, UINT messg, WPARAM wParam, LPARAM lParam );

void	win_StatusLine(int which, char *text);

void	initlog(void);
void	termlog(void);

void 	win_video_switchmodes(void);

int		win_command(HWND hWnd, WORD cmd);

/////

void
win32_system_pause(void);

void
win32_system_log(u32 srcflags, const char *text);

//void
//win32_system_statusline(int line, const char *text);
void
win32_system_getcommands(void);

void
win32_system_report_status(status_item item, va_list va);

void
win32_system_execution_paused(bool paused);

void
win32_system_debugger_enabled(bool enabled);

int 
win32_system_init(void);

int 
win32_system_loop(void);

/*	handlers for GTK frontend */

int
GTK_system_init(void);

int
GTK_system_loop(void);

void
GTK_system_log(u32 srcflags, const char *text);

int
win32_gtk_system_loop(void);


#define HAS_DRAWDIB	1
#define HAS_DDRAW	1

#define	HAS_GTK		1

enum {
		FE_UNKNOWN,
#if HAS_DRAWDIB
		FE_DRAWDIB,
#endif
#if HAS_DDRAW
		FE_DDRAW,
#endif
#if HAS_GTK
		FE_GTK,
#endif
};

extern int	win_frontend;

#include "cexit.h"

#endif

