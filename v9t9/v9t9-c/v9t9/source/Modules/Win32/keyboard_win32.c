/*
  keyboard_win32.c				-- V9t9 module for Win32 keyboard interface

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

#include "winv9t9.h"
#include "v9t9_common.h"
#include "v9t9.h"

#include "timer.h"
#include "video.h"
#include "keyboard.h"
#include "command.h"

///////////

static BYTE win_keymap[256];

#include "memory.h"
static void
win_StoreKey(u8 key, int onoff)
{
	win_keymap[key] = onoff ? 0x100 - onoff : 0x80;
}


LRESULT     CALLBACK
KeyboardMessageHandler(HWND hWnd, UINT messg, WPARAM wParam, LPARAM lParam)
{
	switch (messg) {
	case WM_SYSKEYDOWN:
	{
		int         ch = wParam;	/* virtual key code */
		UINT        key = lParam;	/* info bitmap */

		win_StoreKey(ch, 2);
		return 0;
		break;
	}

	case WM_SYSKEYUP:
	{
		int         ch = wParam;	/* virtual key code */
		UINT        key = lParam;	/* info bitmap */

		win_StoreKey(ch, 0);
		return 0;
		break;
	}

	case WM_KEYDOWN:
	{
		int         ch = wParam;	/* virtual key code */
		UINT        key = lParam;	/* info bitmap */

		win_StoreKey(ch, 1);
		return 0;
		break;
	}

	case WM_KEYUP:
	{
		int         ch = wParam;	/* virtual key code */
		UINT        key = lParam;	/* info bitmap */

		win_StoreKey(ch, 0);
		return 0;
		break;
	}
	}
	return 0;
}

//static int keyboard_event_tag;

static SpecialKey special_key_list[] =
{
	SK_PAUSE,
	SK_F1, SK_F2, SK_F3, SK_F4,
	SK_F5, SK_F6, SK_F7, SK_F8,
	SK_F9, SK_F10, SK_F11, SK_F12,
	SK_ESC, SK_TAB, SK_WIN_LEFT, SK_WIN_RIGHT, SK_MENU
};

static      vmResult
win32keyboard_detect(void)
{
	return vmOk;
}

vmResult    win32keyboard_scan(void);

static int  panic_event_tag;

static      vmResult
win32keyboard_init(void)
{
	features |= FE_KEYBOARD;

	panic_event_tag = TM_UniqueTag();
	return vmOk;
}

static      vmResult
win32keyboard_enable(void)
{
	return vmOk;
}

static      vmResult
win32keyboard_disable(void)
{
	return vmOk;
}

static      vmResult
win32keyboard_restart(void)
{
	realshift = 0;
	memset(crukeyboardmap, 0, 6);
	crukeyboardmap[6] = crukeyboardmap[7] = 0x0;
	return vmOk;
}

static      vmResult
win32keyboard_restop(void)
{
	return vmOk;
}

static      vmResult
win32keyboard_term(void)
{
//  TM_ResetEvent(keyboard_event_tag);
	return vmOk;
}

#if 0
keypad 5 with numlock on: 12 num keypad-- -- ------/: 111 *: 106 -: 109 +:10
7 0 - 9: 96 - 105.: 110 F1 - F12:1 12 - 1 23
#endif
extern HWND hWndStatus;

vmResult win32keyboard_scan(void)
{
	int         x;

	for (x = 0; x < 256; x++)
		if (win_keymap[x]) {
			int         onoff = (win_keymap[x] != 0x80) ? 1 : 0;

			if (onoff)
				module_logger(&win32Keyboard, LOG_KEYBOARD | LOG_INFO,
					   _("win32keyboard_scan: key is %d [%d]\n"), x,
					   win_keymap[x]);
			if (x < 128 && ASCII_DIRECT_TO_9901(x))
				keyboard_setkey(onoff, 0, x);
			else
				switch (x) {
					// special keys
				case VK_ESCAPE:	// ESC
					keyboard_setkey(onoff, 0, SK_ESC);
					break;
				case VK_SCROLL:		// SCROLL LOCK
			        if (onoff)
             			 win_video_switchmodes();
					break;
				case VK_NUMLOCK:	// NUM LOCK
					/* turn off keyboard in Linux */
					break;
				case VK_CAPITAL:	// CAPS LOCK
					if (!onoff)
						caps ^= 1;
					break;
				case VK_BACK:	// BKSP
					keyboard_setkey(onoff, FCTN, 'S');	// left arrow
					break;
				case VK_TAB:	// TAB
					keyboard_setkey(onoff, 0, SK_TAB);
					break;
				case VK_CANCEL:	// CTRL-BREAK
					command_exec_text("Die\n");
					TM_SetEvent(panic_event_tag, TM_HZ * 100, 0,
								TM_FUNC, v9t9_term);
					break;
				case VK_PAUSE:	// PAUSE
					command_exec_text("Interactive=on\n");
					break;

					// shift modifiers
				case VK_SHIFT:	// SHIFT
					keyboard_setkey(onoff, SHIFT, 0);
					break;
				case VK_CONTROL:	// CTRL
					keyboard_setkey(onoff, CTRL, 0);
					break;
				case VK_MENU:	// ALT
					keyboard_setkey(onoff, FCTN, 0);
					break;

					// cursor control keys
				case VK_UP:	// UP
					keyboard_setkey(onoff, FCTN, 'E');
					break;
				case VK_DOWN:	// DOWN
					keyboard_setkey(onoff, FCTN, 'X');
					break;
				case VK_LEFT:	// LEFT
					keyboard_setkey(onoff, FCTN, 'S');
					break;
				case VK_RIGHT:	// RIGHT
					keyboard_setkey(onoff, FCTN, 'D');
					break;
				case VK_INSERT:	// INSERT
					keyboard_setkey(onoff, FCTN, '2');
					break;
				case VK_DELETE:	// DELETE
					keyboard_setkey(onoff, FCTN, '1');
					break;
				case VK_PRIOR:	// PGUP
					keyboard_setkey(onoff, FCTN, '6');
					break;
				case VK_NEXT:	// PGDN
					keyboard_setkey(onoff, FCTN, '4');
					break;
				case VK_HOME:	// HOME
					keyboard_setkey(onoff, FCTN, '5');
					break;
				case VK_END:	// END
					keyboard_setkey(onoff, FCTN, '8');	// ???
					break;

					// windows keys that aren't obvious
				case ':' + 0x80:
					keyboard_setkey(onoff, 0, ';');
					break;
				case 187:
					keyboard_setkey(onoff, 0, '=');
					break;
				case '<' + 0x80:
					keyboard_setkey(onoff, 0, ',');
					break;
				case '>' + 0x80:
					keyboard_setkey(onoff, 0, '.');
					break;

					// keys we must fake with two TI keys
				case 192:
					if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'W'))
						keyboard_setkey(onoff, FCTN, 'C');	/* ` */
					else
						keyboard_setkey(onoff, FCTN, 'W');	/* ~ */
					break;
				case 189:
					if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'U'))
						keyboard_setkey(onoff, SHIFT, '/');	/* - */
					else
						keyboard_setkey(onoff, FCTN, 'U');	/* _ */
					break;
				case '[' + 0x80:
					if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'F'))
						keyboard_setkey(onoff, FCTN, 'R');	/* [ */
					else
						keyboard_setkey(onoff, FCTN, 'F');	/* { */
					break;
				case ']' + 0x80:
					if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'G'))
						keyboard_setkey(onoff, FCTN, 'T');	/* ] */
					else
						keyboard_setkey(onoff, FCTN, 'G');	/* } */
					break;
				case 222:
					if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'P'))
						keyboard_setkey(onoff, FCTN, 'O');	/* ' */
					else
						keyboard_setkey(onoff, FCTN, 'P');	/* " */
					break;
				case '?' + 0x80:
					if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'I'))
						keyboard_setkey(onoff, 0, '/');	/* / */
					else
						keyboard_setkey(onoff, FCTN, 'I');	/* ? */
					break;
				case '\\' + 0x80:
					if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'A'))
						keyboard_setkey(onoff, FCTN, 'Z');	/* \\ */
					else
						keyboard_setkey(onoff, FCTN, 'A');	/* | */
					break;
				case VK_F1:
				case VK_F2:
				case VK_F3:
				case VK_F4:
				case VK_F5:
				case VK_F6:
				case VK_F7:
				case VK_F8:
				case VK_F9:
				case VK_F10:
				case VK_F11:
				case VK_F12:
					keyboard_setkey(onoff, 0, SK_F1 + x - VK_F1);
					break;
				default:
					module_logger(&win32Keyboard, LOG_KEYBOARD | LOG_ERROR,
						   _("unknown keycode %d\n"), x);
					break;
				}
			win_keymap[x] = 0;
		}
	return vmOk;
}


static      vmResult
win32keyboard_getspecialkeys(SpecialKey **list)
{
	*list = special_key_list;
	return vmOk;
}

static vmKeyboardModule win32KbdModule = {
	3,
	win32keyboard_scan,
	win32keyboard_getspecialkeys
};

vmModule    win32Keyboard = {
	3,
	"Win32 keyboard",
	"kbdWin32",

	vmTypeKeyboard,
	vmFlagsNone,

	win32keyboard_detect,
	win32keyboard_init,
	win32keyboard_term,
	win32keyboard_enable,
	win32keyboard_disable,
	win32keyboard_restart,
	win32keyboard_restop,
	{(vmGenericModule *) & win32KbdModule}
};
