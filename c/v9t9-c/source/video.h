/*
  video.h						-- V9t9 video module interface

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

#ifndef __VIDEO_H__
#define __VIDEO_H__

#include "centry.h"

/*	A list of these structs is created by the vdp.c "redrawXXXX" routines. 
	vdp.c knows exactly what needs to be updated on the screen.  All
	a module must do is draw it.  */

/*	Each updateblock represents an 8x8 block that needs to be redrawn.
	'data' points into a global bitmap (updarea) which contains the
	fully rendered screen in bytes, with values 0..15 corresponding
	to typical TI colors, and 16 representing the foreground color
	for a text screen.  
	
	The row size if UPDROWSIZE bytes.  'r' and 'c' are the coordinates 
	of the block (0,0 top-left).

	For advanced use, 'pattern' points to the monochrome pattern to
	apply to the block, and 'fg' and 'bg' give the colors to use
	for a simple block (only two colors); or, if non-NULL, 'colors' 
	gives the color list for complex blocks, where each byte is
	(foreground<<16)|background.
	
	If 'pattern' is NULL, the block is obscured by a sprite and
	cannot be drawn directly.
*/

// distance between rows in updateblock->data
#define UPDATEBLOCK_ROW_STRIDE (256+64)

typedef struct updateblock
{
	u8	*data;			// byte pointer into preformatted bitmap
	u32 r,c;			// row and column, 0..191 and 0..255
	
	u8  *pattern;		// pointer to 8 bytes for 8x8 pattern of block
	u8 	*colors, fg,bg;	// colors to apply
}	updateblock;

/*
 *	Tell if an updateblock can be drawn with one color
 *
 *	If collapse is true, treat color 0 as vdpfg and 16 and vdpbg;
 *	this may cause problems on palettized displays when these values
 *	change.
 */
bool video_block_is_solid(updateblock *ptr, bool collapse, u8 *color);

#define VIDEO(x,y) do { \
	vmModule *ptr = vmVideo; \
	while (ptr && ((ptr->runtimeflags & vmRTUnselected) || !(ptr->runtimeflags & vmRTEnabledOnce))) \
		ptr = ptr->next; \
	if (ptr) ptr->m.video->x y; \
	} while (0)

extern int video_restart(void);
extern void video_restop(void);

#include "cexit.h"

#endif
