/*
  keyboard_gtk.c				-- V9t9 module for GTK keyboard interface

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

#include "v9t9_common.h"

#include <gtk/gtk.h>
#include <gdk/gdkkeysyms.h>
#include "gtkinterface.h"
#include "gtkloop.h"

#include "v9t9.h"
#include "timer.h"
#include "keyboard.h"
#include "command.h"

#define _L LOG_KEYBOARD|LOG_INFO

void   	GTK_keyboard_set_key(guint key, int onoff)
{
	module_logger(&gtkKeyboard, _L|L_1, _("GTK key = %02X (%d)\n"), key, onoff);
	if (key >= GDK_a && key <= GDK_z)
		key += (GDK_A - GDK_a);

	// a few troublesome keys that won't work because
	// they're shifted, and we already detect the shift key.
	switch (key)
	{
	case GDK_colon:		key = GDK_semicolon; break;
	case GDK_plus:		key = GDK_equal; break;
	case GDK_exclam:	key = GDK_1; break;
	case GDK_at:		key = GDK_2; break;
	case GDK_numbersign:key = GDK_3; break;
	case GDK_dollar:	key = GDK_4; break;
	case GDK_percent:	key = GDK_5; break;
	case GDK_asciicircum:key = GDK_6; break;
	case GDK_ampersand:	key = GDK_7; break;
	case GDK_asterisk:	key = GDK_8; break;
	case GDK_parenleft:	key = GDK_9; break;
	case GDK_parenright:key = GDK_0; break;
	case GDK_less:		key = GDK_comma; break;
	case GDK_greater:	key = GDK_period; break;
	}

	if (key < 128 && ASCII_DIRECT_TO_9901(key)) {
		module_logger(&gtkKeyboard, _L, _("direct key\n"));
		keyboard_setkey(onoff, 0, key);
	} else 
		switch (key) {
		case GDK_Escape:
			keyboard_setkey(onoff, 0, SK_ESC);
			break;
		case GDK_Tab: case GDK_KP_Tab:
			keyboard_setkey(onoff, 0, SK_TAB);
			break;

		case GDK_F1: case GDK_F2: case GDK_F3: case GDK_F4: case GDK_F5:
		case GDK_F6: case GDK_F7: case GDK_F8: case GDK_F9: case GDK_F10:
		case GDK_F11: case GDK_F12:
			keyboard_setkey(onoff, 0, SK_F1 + (key - GDK_F1));
			break;

		case GDK_Scroll_Lock:
		case GDK_Num_Lock:
			if (!onoff)
				command_exec_text("DumpKeyMap\n");
			break;

		case GDK_Return:
			keyboard_setkey(onoff, 0, '\r');
			break;

		case GDK_Caps_Lock:
			if (!onoff)
				caps ^= 1;
			break;
		case GDK_Break:
			if (onoff)
				command_exec_text("Die\n");
			break;
		case GDK_Pause:
			if (onoff)
			{
				if (keyboard_isset(CTRL, 0))
					command_exec_text("Die\n");	// ctrl-break not working?
				else
					command_exec_text("PauseComputer (!PauseComputer())\n");
			}
			break;

			// shifts
		case GDK_Shift_L:
		case GDK_Shift_R:
			keyboard_setkey(onoff, SHIFT, 0);
			break;
		case GDK_Control_L:
		case GDK_Control_R:
			keyboard_setkey(onoff, CTRL, 0);
			break;
		case GDK_Alt_L:
		case GDK_Alt_R:
		case GDK_Meta_L:
		case GDK_Meta_R:
			keyboard_setkey(onoff, FCTN, 0);
			break;

					// cursor
		case GDK_Up:
			keyboard_setkey(onoff, FCTN, 'E');
			break;
		case GDK_Down:
			keyboard_setkey(onoff, FCTN, 'X');
			break;
		case GDK_Left:
		case GDK_BackSpace:
			keyboard_setkey(onoff, FCTN, 'S');
			break;
		case GDK_Right:
			keyboard_setkey(onoff, FCTN, 'D');
			break;
		case GDK_Insert:
			keyboard_setkey(onoff, FCTN, '2');	// INSERT
			break;
		case GDK_Delete:
			keyboard_setkey(onoff, FCTN, '1');	// DELETE
			break;
		case GDK_Page_Up:
			keyboard_setkey(onoff, FCTN, '6');	// PGUP
			break;
		case GDK_Page_Down:
			keyboard_setkey(onoff, FCTN, '4');	// PGDN
			break;
		case GDK_Begin:
			keyboard_setkey(onoff, FCTN, '5');	// BEGIN
			break;
		case GDK_End:	// ???
		case GDK_Redo:
			keyboard_setkey(onoff, FCTN, '8');	// REDO
			break;
		case GDK_Help:
			keyboard_setkey(onoff, FCTN, '7');	// AID
			break;

			// faked keys
		case GDK_grave:
		case GDK_asciitilde:
			if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'W'))
				keyboard_setkey(onoff, FCTN, 'C');	/* ` */
			else
				keyboard_setkey(onoff, FCTN, 'W');	/* ~ */
			break;
		case GDK_minus:
		case GDK_underscore:
			if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'U'))
				keyboard_setkey(onoff, SHIFT, '/');	/* - */
			else
				keyboard_setkey(onoff, FCTN, 'U');	/* _ */
			break;
		case GDK_bracketleft:
		case GDK_braceleft:
			if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'F'))
				keyboard_setkey(onoff, FCTN, 'R');	/* [ */
			else
				keyboard_setkey(onoff, FCTN, 'F');	/* { */
			break;
		case GDK_bracketright:
		case GDK_braceright:
			if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'G'))
				keyboard_setkey(onoff, FCTN, 'T');	/* ] */
			else
				keyboard_setkey(onoff, FCTN, 'G');	/* } */
			break;
		case GDK_apostrophe:
		case GDK_quotedbl:
			if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'P'))
				keyboard_setkey(onoff, FCTN, 'O');	/* ' */
			else
				keyboard_setkey(onoff, FCTN, 'P');	/* " */
			break;
		case GDK_slash:
		case GDK_question:
			if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'I'))
				keyboard_setkey(onoff, 0, '/');	/* / */
			else
				keyboard_setkey(onoff, FCTN, 'I');	/* ? */
			break;
		case GDK_backslash:
		case GDK_bar:
			if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'A'))
				keyboard_setkey(onoff, FCTN, 'Z');	/* \\ */
			else
				keyboard_setkey(onoff, FCTN, 'A');	/* | */
			break;

			// mouse movement
#define NP_JOYST	1
#define NP_MAG		((int)onoff)

		case GDK_KP_8:
			keyboard_setjoyst(NP_JOYST, JOY_Y, 0, -NP_MAG, 0);
			break;
		case GDK_KP_2:
			keyboard_setjoyst(NP_JOYST, JOY_Y, 0, NP_MAG, 0);
			break;
		case GDK_KP_4:
			keyboard_setjoyst(NP_JOYST, JOY_X, -NP_MAG, 0, 0);
			break;
		case GDK_KP_6:
			keyboard_setjoyst(NP_JOYST, JOY_X, NP_MAG, 0, 0);
			break;
		case GDK_KP_7:
			keyboard_setjoyst(NP_JOYST, JOY_X | JOY_Y, -NP_MAG, -NP_MAG, 0);
			break;
		case GDK_KP_9:
			keyboard_setjoyst(NP_JOYST, JOY_X | JOY_Y, NP_MAG, -NP_MAG, 0);
			break;
		case GDK_KP_1:
			keyboard_setjoyst(NP_JOYST, JOY_X | JOY_Y, -NP_MAG, NP_MAG, 0);
			break;
		case GDK_KP_3:
			keyboard_setjoyst(NP_JOYST, JOY_X | JOY_Y, NP_MAG, NP_MAG, 0);
			break;
		case GDK_KP_5:
//			keyboard_setjoyst(NP_JOYST, JOY_X | JOY_Y, 0, 0, 0);
//			break;
		case GDK_KP_0:
			keyboard_setjoyst(NP_JOYST, JOY_B, 0, 0, NP_MAG);
			break;
		}
}

static      vmResult
gtkkeyboard_detect(void)
{
	return vmOk;
}

static      vmResult
gtkkeyboard_init(void)
{
	features |= FE_KEYBOARD;
	return vmOk;
}

static      vmResult
gtkkeyboard_enable(void)
{
	return vmOk;
}

static      vmResult
gtkkeyboard_disable(void)
{
	return vmOk;
}

static      vmResult
gtkkeyboard_restart(void)
{
	realshift = 0;
	memset(crukeyboardmap, 0, 6);
	crukeyboardmap[6] = crukeyboardmap[7] = 0x0;

	// select up and down keypresses
	my_assert(v9t9_drawing_area);

	gdk_window_set_events(v9t9_drawing_area->window,
						  gdk_window_get_events(v9t9_drawing_area->window) |
						  GDK_KEY_RELEASE_MASK | GDK_KEY_PRESS_MASK);

	return vmOk;
}

static      vmResult
gtkkeyboard_restop(void)
{
	return vmOk;
}

static      vmResult
gtkkeyboard_term(void)
{
	return vmOk;
}

static      vmResult
gtkkeyboard_scan(void)
{
	return vmOk;
}

static      vmResult
gtkkeyboard_getspecialkeys(SpecialKey ** list)
{
	static SpecialKey none[] = { 0 };

	*list = none;
	return vmOk;
}

static vmKeyboardModule gtkKbdModule = {
	3,
	gtkkeyboard_scan,
	gtkkeyboard_getspecialkeys
};

vmModule    gtkKeyboard = {
	3,
	"GTK+ keyboard",
	"kbdGTK",

	vmTypeKeyboard,
	vmFlagsExclusive,

	gtkkeyboard_detect,
	gtkkeyboard_init,
	gtkkeyboard_term,
	gtkkeyboard_enable,
	gtkkeyboard_disable,
	gtkkeyboard_restart,
	gtkkeyboard_restop,
	{(vmGenericModule *) & gtkKbdModule}
};
