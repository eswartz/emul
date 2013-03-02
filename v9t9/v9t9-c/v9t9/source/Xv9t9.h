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

#ifndef __Xv9t9_h__
#define __Xv9t9_h__

#include "centry.h"

#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <X11/Xos.h>
#include <X11/Xatom.h>
#include <X11/Xcms.h>
#include <X11/Xresource.h>

#include <stdio.h>

#define FAIL(x) 	fprintf(stderr, x)

//extern		X_keymap[];		/* map of keys */

void	x_handle_video_event(XEvent *e);
void	x_handle_kbd_event(XEvent *e);

#include "cexit.h"

#endif
