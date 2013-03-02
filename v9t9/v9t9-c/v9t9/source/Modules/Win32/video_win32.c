//  browse this to find error messages DDERR_INVALIDPARAMS

/*
  video_win32.c 				-- V9t9 video modules for Win32

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

#define ALLOW_DIRECTDRAW
#define ALLOW_DRAWDIB
#pragma auto_inline off
#include "winv9t9.h"
//#include <gtk/gtk.h>
//#include <gdk/win32/gdkwin32.h>
#include <malloc.h>
//#include "gtkloop.h"

#include "v9t9_common.h"
#include "9900.h"
#include "memory.h"
#include "vdp.h"
#include "timer.h"
#include "v9t9.h"
#include "video.h"

#include "log.h"
#define _L LOG_VIDEO | LOG_INFO
#include "resource.h"

#include "video_win32.h"
#include "video_win32_drawdib.h"

/*****************/

/*	Size of video screen in logical pixels (is 240x192 for text) */
extern int  win_logxsize = 256, win_logysize = 192;

extern int  win_video_event_tag;

/*****************/

extern BOOL win_wasfullscreen;	// if we restop()'ed, what to reset?

// the bitmap is needed for cases where we "lose" the memory
BYTE *win_bitmap;		/* 256x192 256-color bitmap */

//  palette we map to windows
PALETTEENTRY pals[256];

//  mapping from logical pixels to palette entries
u8   win_rgbmap[17];

#if 0
int win_video_event_tag;

extern void
win_video_update(void)
{
	if (features & FE_VIDEO) {
		if (redrawscreen)
			redrawscreen();		/* eventually gets to videoupdatelist and videoupdatetextlist in module */
	}
}
#endif

BITMAPINFO *win_bm;		/* bitmap for window */

BOOL win_createBitmap(void)
{
	win_bm = (BITMAPINFO *) xmalloc(sizeof(BITMAPINFO) + sizeof(RGBQUAD) * 16);
	win_bm->bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
	win_bm->bmiHeader.biWidth = 256;
	win_bm->bmiHeader.biHeight = 256;
	win_bm->bmiHeader.biPlanes = 1;	/* must be one */
	win_bm->bmiHeader.biBitCount = 8;	/* call it eight bits for faster drawing */
	win_bm->bmiHeader.biCompression = BI_RGB;
	win_bm->bmiHeader.biSizeImage = 0;	/* 0 for BI_RGB bitmaps */
	win_bm->bmiHeader.biClrUsed = 17;
	win_bm->bmiHeader.biClrImportant = 17;

	win_bitmap = (u8 *) xmalloc(256 * 256);

	return TRUE;
}

void win_video_switchmodes(void)
{
	if (!(win32DrawDibVideo.runtimeflags & vmRTUnselected) &&
		(win32DrawDibVideo.runtimeflags & vmRTInUse))
	{
		command_exec_text("ToggleV9t9Module \"vidWin32DrawDib\" off\n" 
							"ToggleV9t9Module \"vidWin32DirectDraw\" on\n");
	}
	else
	{
		command_exec_text("ToggleV9t9Module \"vidWin32DirectDraw\" off\n" 
							"ToggleV9t9Module \"vidWin32DrawDib\" on\n");
	}
}
