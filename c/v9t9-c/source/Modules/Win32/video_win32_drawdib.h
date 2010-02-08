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
	Video functions for the DrawDIB extension under Win32
	
	This module implements video by drawing to a window using the
	fast drawdib extension, which automatically scales and colormaps
	a virtual bitmap to the screen.
*/

// attempt to use drawdib under GTK... not likely!
#define MIXED_GTK 0

extern HWND hWndWindow, hWndStatus;		// our windows

extern vmModule win32DrawDibVideo;

#if MIXED_GTK
extern void gtk_window_paint(RECT *rect);
#endif
