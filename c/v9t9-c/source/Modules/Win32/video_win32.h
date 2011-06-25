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

/*
 *	Common Win32 video routines and macros
 *
 */

#ifndef __VIDEO_WIN32_H__
#define __VIDEO_WIN32_H__

#include "centry.h" 

//  If set, the background is drawn as color 0,
//  and the palette entry is changed as the BG changes.
//  Also, text is drawn with colors 0 and 16.
#define USEPALETTESWITCHING

#ifdef USEPALETTESWITCHING
#define BG 0
#else
#define BG vdpbg
#endif

// used by directsound
extern HWND hWndApp;

/*	Size of video screen in logical pixels (is 240x192 for text) */
extern int  win_logxsize, win_logysize;

extern int  video_event_tag;

/*****************/

extern BOOL win_wasfullscreen;	// if we restop()'ed, what to reset?

//  shadow palette for DirectDraw
extern PALETTEENTRY pals[256];

// our 256x256 windows bitmap
extern BYTE *win_bitmap;

//  mapping from logical pixels to palette entries
extern u8   win_rgbmap[17];

// 	event tag for update requests
extern int win_video_event_tag;

#define	DB_PALBASE 0
#define DB_RGBTOPAL(x,y) do { pals[(x)+DB_PALBASE].peFlags = PC_NOCOLLAPSE | PC_RESERVED; \
						pals[(x)+DB_PALBASE].peRed = vdp_palette[y][0]; \
						pals[(x)+DB_PALBASE].peGreen = vdp_palette[y][1]; \
						pals[(x)+DB_PALBASE].peBlue = vdp_palette[y][2]; } while (0)

#define	DD_PALBASE 0
#define DD_RGBTOPAL(x,y) do { pals[(x)+DD_PALBASE].peFlags = PC_NOCOLLAPSE | PC_RESERVED; \
						pals[(x)+DD_PALBASE].peRed = vdp_palette[y][0]; \
						pals[(x)+DD_PALBASE].peGreen = vdp_palette[y][1]; \
						pals[(x)+DD_PALBASE].peBlue = vdp_palette[y][2]; } while (0)

//extern void
//win_video_update(void);

extern BITMAPINFO *win_bm;		/* bitmap for window */
extern BOOL win_createBitmap(void);

extern void win_video_switchmodes(void);

#include "cexit.h"

#endif	// __VIDEO_WIN32_H__
