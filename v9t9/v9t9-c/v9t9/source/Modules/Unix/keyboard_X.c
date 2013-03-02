/*
  keyboard_X.c					-- V9t9 module for X11 keyboard interface

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

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>

#include "Xv9t9.h"

//  these symbols select which XK_xxx defs we get
#define XK_MISCELLANY
#define XK_LATIN1
#include <X11/keysymdef.h>

#include "v9t9_common.h"
#include "v9t9.h"

#include "timer.h"

#include "keyboard.h"

#define _L	LOG_KEYBOARD|LOG_INFO

extern Display *x11_dpy;
extern Window vwin;

/***********************************/

void        
x_handle_kbd_event(XEvent * e)
{
	KeySym      key;
	int         onoff;

	switch (e->type) {
	case EnterNotify:
		XGrabKeyboard(x11_dpy, vwin, True,
					  GrabModeAsync, GrabModeAsync, CurrentTime);
		break;
	case LeaveNotify:
		XUngrabKeyboard(x11_dpy, CurrentTime);
		break;
	case KeyPress:
	case KeyRelease:
		onoff = e->type == KeyPress;

		if ((key = XLookupKeysym(&e->xkey, 0)) != NoSymbol) {
			module_logger(&X_Keyboard, _L|L_1, _("key = 0x%X\n"), key);
			if (key >= XK_a && key <= XK_z)
				key += (XK_A - XK_a);
			if (key < 128 && ASCII_DIRECT_TO_9901(key)) {
				module_logger(&X_Keyboard, _L|L_1, _("direct key\n"));
				keyboard_setkey(onoff, 0, key);
			} else
				switch (key) {
				case XK_Escape:
					keyboard_setkey(onoff, 0, SK_ESC);
					break;
				case XK_Tab:
					keyboard_setkey(onoff, 0, SK_TAB);
					break;

				case XK_F1: case XK_F2: case XK_F3: case XK_F4: case XK_F5:
				case XK_F6: case XK_F7: case XK_F8: case XK_F9: case XK_F10:
				case XK_F11: case XK_F12:
					keyboard_setkey(onoff, 0, SK_F1 + (key - XK_F1));
					break;

				case XK_Scroll_Lock:
				case XK_Num_Lock:
					break;

				case XK_Return:
					keyboard_setkey(onoff, 0, '\r');
					break;

				case XK_Caps_Lock:
					if (!onoff)
						caps ^= 1;
					break;

					/* in XFree86 4.0, it looks like
					   ctrl+break only registers as pause,
					   so treat pause as break if ctrl is held down */
				case XK_Pause:
					if (realshift & CTRL)
					{
				case XK_Break:
					if (onoff)
						command_exec_text("Die\n");
					break;
					}
					else
					if (onoff)
						command_exec_text("PauseComputer !(PauseComputer())\n");
					break;

					// shifts
				case XK_Shift_L:
				case XK_Shift_R:
					keyboard_setkey(onoff, SHIFT, 0);
					break;
				case XK_Control_L:
				case XK_Control_R:
					keyboard_setkey(onoff, CTRL, 0);
					break;
				case XK_Alt_L:
				case XK_Alt_R:
				case XK_Meta_L:
				case XK_Meta_R:
					keyboard_setkey(onoff, FCTN, 0);
					break;

					// cursor
				case XK_Up:
					keyboard_setkey(onoff, FCTN, 'E');
					break;
				case XK_Down:
					keyboard_setkey(onoff, FCTN, 'X');
					break;
				case XK_Left:
				case XK_BackSpace:
					keyboard_setkey(onoff, FCTN, 'S');
					break;
				case XK_Right:
					keyboard_setkey(onoff, FCTN, 'D');
					break;
				case XK_Insert:
					keyboard_setkey(onoff, FCTN, '2');	// INSERT
					break;
				case XK_Delete:
					keyboard_setkey(onoff, FCTN, '1');	// DELETE
					break;
				case XK_Page_Up:
					keyboard_setkey(onoff, FCTN, '6');	// PGUP
					break;
				case XK_Page_Down:
					keyboard_setkey(onoff, FCTN, '4');	// PGDN
					break;
				case XK_Begin:
					keyboard_setkey(onoff, FCTN, '5');	// BEGIN
					break;
				case XK_End:	// ???
				case XK_Redo:
					keyboard_setkey(onoff, FCTN, '8');	// REDO
					break;
				case XK_Help:
					keyboard_setkey(onoff, FCTN, '7');	// AID
					break;

					// faked keys
				case XK_grave:
				case XK_asciitilde:
					if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'W'))
						keyboard_setkey(onoff, FCTN, 'C');	/* ` */
					else
						keyboard_setkey(onoff, FCTN, 'W');	/* ~ */
					break;
				case XK_minus:
				case XK_underscore:
					if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'U'))
						keyboard_setkey(onoff, SHIFT, '/');	/* - */
					else
						keyboard_setkey(onoff, FCTN, 'U');	/* _ */
					break;
				case XK_bracketleft:
				case XK_braceleft:
					if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'F'))
						keyboard_setkey(onoff, FCTN, 'R');	/* [ */
					else
						keyboard_setkey(onoff, FCTN, 'F');	/* { */
					break;
				case XK_bracketright:
				case XK_braceright:
					if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'G'))
						keyboard_setkey(onoff, FCTN, 'T');	/* ] */
					else
						keyboard_setkey(onoff, FCTN, 'G');	/* } */
					break;
				case XK_apostrophe:
				case XK_quotedbl:
					if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'P'))
						keyboard_setkey(onoff, FCTN, 'O');	/* ' */
					else
						keyboard_setkey(onoff, FCTN, 'P');	/* " */
					break;
				case XK_slash:
				case XK_question:
					if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'I'))
						keyboard_setkey(onoff, 0, '/');	/* / */
					else
						keyboard_setkey(onoff, FCTN, 'I');	/* ? */
					break;
				case XK_backslash:
				case XK_bar:
					if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'A'))
						keyboard_setkey(onoff, FCTN, 'Z');	/* \\ */
					else
						keyboard_setkey(onoff, FCTN, 'A');	/* | */
					break;

					// mouse movement
#define NP_JOYST	1
#define NP_MAG		((int)onoff)

				case XK_KP_8:
					keyboard_setjoyst(NP_JOYST, JOY_Y, 0, -NP_MAG, 0);
					break;
				case XK_KP_2:
					keyboard_setjoyst(NP_JOYST, JOY_Y, 0, NP_MAG, 0);
					break;
				case XK_KP_4:
					keyboard_setjoyst(NP_JOYST, JOY_X, -NP_MAG, 0, 0);
					break;
				case XK_KP_6:
					keyboard_setjoyst(NP_JOYST, JOY_X, NP_MAG, 0, 0);
					break;
				case XK_KP_7:
					keyboard_setjoyst(NP_JOYST, JOY_X | JOY_Y, -NP_MAG, -NP_MAG, 0);
					break;
				case XK_KP_9:
					keyboard_setjoyst(NP_JOYST, JOY_X | JOY_Y, NP_MAG, -NP_MAG, 0);
					break;
				case XK_KP_1:
					keyboard_setjoyst(NP_JOYST, JOY_X | JOY_Y, -NP_MAG, NP_MAG, 0);
					break;
				case XK_KP_3:
					keyboard_setjoyst(NP_JOYST, JOY_X | JOY_Y, NP_MAG, NP_MAG, 0);
					break;
				case XK_KP_5:
//					keyboard_setjoyst(NP_JOYST, JOY_X | JOY_Y, 0, 0, 0);
//					break;
				case XK_KP_0:
					keyboard_setjoyst(NP_JOYST, JOY_B, 0, 0, NP_MAG);
					break;

#undef NP_JOYST
#undef NP_MAG		
				}
		} else {
			// no keysym...
			switch (e->xkey.keycode)
			{
#if __i386__
			case 111:
				// printscreen
				break;
			case 92:
				// sysrq
				break;
			case 115:
				keyboard_setkey(onoff, 0, SK_WIN_LEFT);
				break;
			case 116:
				keyboard_setkey(onoff, 0, SK_WIN_RIGHT);
				break;
			case 117:
				keyboard_setkey(onoff, 0, SK_MENU);
				break;
			case 114:
				if (onoff)
					command_exec_text("Die\n");
				break;
#endif
			default:
				module_logger(&X_Keyboard, _L|LOG_USER, _("no symbol for keycode (%d)\n"),
					   e->xkey.keycode);
			}
		}
	}
}

/****************************/

static vmResult 
X_Kbd_detect(void)
{
	if (x11_dpy == NULL)
		return vmNotAvailable;

	return vmOk;
}

static vmResult 
X_Kbd_init(void)
{
#warning FE_KEYBOARD
	features |= FE_KEYBOARD;
	return vmOk;
}

static vmResult 
X_Kbd_term(void)
{
	return vmOk;
}

static vmResult 
X_Kbd_enable(void)
{
	return vmOk;
}

static vmResult 
X_Kbd_disable(void)
{
	return vmOk;
}

static vmResult 
X_Kbd_restart(void)
{
	realshift = 0;
	memset(crukeyboardmap, 0, 6);
	crukeyboardmap[6] = crukeyboardmap[7] = 0x0;
	return vmOk;
}

static vmResult 
X_Kbd_restop(void)
{
	XUngrabKeyboard(x11_dpy, CurrentTime);
	return vmOk;
}


/************************************************************/


static vmResult 
X_Kbd_Update(void)
{
	return vmOk;
}

static SpecialKey special_key_list[] = 
{
//	SK_PAUSE,
	SK_F1, SK_F2, SK_F3, SK_F4,
	SK_F5, SK_F6, SK_F7, SK_F8,
	SK_F9, SK_F10, SK_F11, SK_F12,
	SK_ESC, SK_TAB, SK_WIN_LEFT, SK_WIN_RIGHT, SK_MENU
};

static      vmResult
X_Kbd_getspecialkeys(SpecialKey ** list)
{
	*list = special_key_list;
	return vmOk;
}


static vmKeyboardModule XKbdModule = {
	3,
	X_Kbd_Update,
	X_Kbd_getspecialkeys
};

vmModule    X_Keyboard = {
	3,
	"X-Window keyboard",
	"kbX",

	vmTypeKeyboard,
	vmFlagsExclusive,

	X_Kbd_detect,
	X_Kbd_init,
	X_Kbd_term,
	X_Kbd_enable,
	X_Kbd_disable,
	X_Kbd_restart,
	X_Kbd_restop,
	{(vmGenericModule *) & XKbdModule}
};
