
/*
  keyboard_svgalib.c			-- V9t9 module for SVGAlib keyboard

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
#include <signal.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <termios.h>
#include <linux/kd.h>
#include <linux/keyboard.h>
#include <sys/vt.h>
#include <vgakeyboard.h>
#include <vgamouse.h>

#include "v9t9_common.h"
#include "v9t9.h"

#include "timer.h"
#include "command.h"
#include "keyboard.h"

#define _L	LOG_KEYBOARD|LOG_INFO

extern int  console_fd;
int         keyfd;

static struct termios oldkbdtermios, newkbdtermios;
static int  oldkbmode;

//static    int keyboard_event_tag;
static int  panic_event_tag;

static int  kbd_open;

static vmResult   
keyboardupdate(void);

static vmResult    
linux_kbd_detect(void)
{
	module_logger(&linuxKeyboard, _L|LOG_USER, _("testing console keyboard... "));
	if (console_fd < 0) {
		logger(_L|LOG_USER, _("cannot access.\n"));
		return vmNotAvailable;
	}
	logger(_L| LOG_USER, _("ok.\n"));
	return vmOk;
}

static vmResult 
linux_kbd_init(void)
{
	return vmOk;
}

static vmResult 
linux_kbd_term(void)
{
	return vmOk;
}

static vmResult 
linux_kbd_enable(void)
{
	module_logger(&linuxKeyboard, _L|LOG_USER, _("Opening console keyboard... "));
	keyfd = dup(console_fd);
	if (keyfd == -1) {
		logger(_L|LOG_USER, _("could not open console!\n"));
		return vmInternalError;
	}
	logger(_L| LOG_USER, _("ok.\n"));

	mouse_init("/dev/mouse", vga_getmousetype(), MOUSE_DEFAULTSAMPLERATE);


//  keyboard_event_tag = TM_UniqueTag();
//  TM_SetEvent(keyboard_event_tag, TM_HZ*100/50, 0,
//              TM_FUNC|TM_REPEAT, keyboardupdate);

	panic_event_tag = TM_UniqueTag();

	features |= FE_KEYBOARD;
	kbd_open = 0;

	return vmOk;
}

static vmResult 
linux_kbd_restart(void)
{
	module_logger(&linuxKeyboard, _L|L_2, _("setting raw mode\n"));

	realshift = 0;
	memset(crukeyboardmap, 0, 6);
	crukeyboardmap[6] = crukeyboardmap[7] = 0x0;

#warning FE_KEYBOARD
	if ((features & FE_KEYBOARD) && !kbd_open) {
		mouse_setxrange(-65536, 65536);
		mouse_setyrange(-65536, 65536);
		mouse_setwrap(MOUSE_NOWRAP);
		mouse_setposition(0, 0);
		mouse_setscale(16);

		tcgetattr(keyfd, &oldkbdtermios);

		if (ioctl(keyfd, KDGKBMODE, &oldkbmode)) {
			module_logger(&linuxKeyboard, _L|LOG_USER | LOG_ERROR,
				 _("Cannot get keyboard mode.\n"));
			return vmInternalError;
		}

		newkbdtermios = oldkbdtermios;

		newkbdtermios.c_lflag =
			newkbdtermios.c_lflag & ~(ICANON | ECHO | ISIG);
		newkbdtermios.c_iflag &=
			~(ISTRIP | IGNCR | ICRNL | INLCR | IXOFF | IXON);
		memset(newkbdtermios.c_cc, 0, sizeof(newkbdtermios.c_cc));

		tcsetattr(keyfd, TCSAFLUSH, &newkbdtermios);

		ioctl(keyfd, KDSKBMODE, K_MEDIUMRAW);
		kbd_open = 1;
        module_logger(&linuxKeyboard, _L|L_2, _("restart[ed] (%d, %d)...\n"), kbd_open, keyfd);
	}
	return vmOk;
}

static vmResult 
linux_kbd_restop(void)
{
	module_logger(&linuxKeyboard, _L|L_2, _("restop (%d, %d)...\n"), kbd_open, keyfd);
	if (kbd_open) {
		ioctl(keyfd, KDSKBMODE, K_XLATE);
		tcsetattr(keyfd, TCSAFLUSH, &oldkbdtermios);
		kbd_open = 0;
	}
	return vmOk;
}

static vmResult 
linux_kbd_disable(void)
{
	module_logger(&linuxKeyboard, _L|L_2, _("disable... (%d, %d)\n"), kbd_open, keyfd);

//  TM_ResetEvent(keyboard_event_tag);
	module_logger(&linuxKeyboard, _L|LOG_USER, _("Releasing keyboard...\n"));
	close(keyfd);
	return vmOk;
}


/************************************************************/

#define SHIFT_R 2
#define SHIFT_C 0
#define ALT_R 3
#define ALT_C 0
#define CTRL_R 1
#define CTRL_C 0

static SpecialKey special_key_list[] = 
{
//	SK_PAUSE,
	SK_F1, SK_F2, SK_F3, SK_F4,
	SK_F5, SK_F6, SK_F7, SK_F8,
	SK_F9, SK_F10, SK_F11, SK_F12,
	SK_ESC, SK_TAB, SK_WIN_LEFT, SK_WIN_RIGHT, SK_MENU
};


static      vmResult
linux_kbd_getspecialkeys(SpecialKey **list)
{
	*list = special_key_list;
	return vmOk;
}

/**********************************/

#ifdef __linux__
//#define ALLOW_CONSOLE_SWITCH 1
#endif

#if ALLOW_CONSOLE_SWITCH

/*	Console switching.  When we switch away from the
	v9t9 console, turn off video and keyboard, but keep
	running.  Wait for Linux to send us a signal telling
	us we're active again, then turn video and keyboard on
	(or back to their previous state).	*/

static struct vt_mode orig_vt;
static u32  orig_features;
static short orig_console, new_console;
static void (*orig_acqsig) (int);
static void (*orig_relsig) (int);

/*	Called from Linux once switched back. */
static void
switched_back(int sig)
{
	module_logger(&linuxKeyboard, _L|L_3, _("switched_back\n"));

	signal(orig_vt.acqsig, orig_acqsig);
	signal(orig_vt.relsig, orig_relsig);

	ioctl(keyfd, VT_SETMODE, &orig_vt);

//  um, this scheme doesn't work as expected
//  restop();
	features = orig_features;
	v9t9_restart();

	//  clear kb bits
	memset(crukeyboardmap, 0, sizeof(crukeyboardmap));

	// acquire console
	ioctl(keyfd, VT_RELDISP, VT_ACKACQ);

}

/*	Called from Linux once switched away. */
static void
switched_away(int sig)
{
	module_logger(&linuxKeyboard, _L|L_3, _("switched_away\n"));

	// turn off troublesome bits
	orig_features = features;
	v9t9_restop();
//  um, this scheme doesn't work as expected
//  features &= ~(FE_VIDEO | FE_KEYBOARD);
//  restart();


	//  clear kb bits
	memset(crukeyboardmap, 0, sizeof(crukeyboardmap));


	// release console
	ioctl(keyfd, VT_RELDISP, 1);

}

/*	Called from keyboard handler. */

static void
switchconsole(int which)
{
	struct vt_stat stat;
	struct vt_mode vt;


	// find out who we are
	ioctl(keyfd, VT_GETSTATE, &stat);
	orig_console = stat.v_active;
	new_console = which + 1;
	if (orig_console == new_console)
		return;

	// keep running after switch
	ioctl(keyfd, VT_GETMODE, &orig_vt);
	vt = orig_vt;
	vt.mode = VT_PROCESS;
	vt.waitv = 0;
	ioctl(keyfd, VT_SETMODE, &vt);

	// set up handler for acquistion signal
	orig_acqsig = signal(vt.acqsig, switched_back);
	orig_relsig = signal(vt.relsig, switched_away);

	module_logger(&linuxKeyboard, _L|L_3, _("orig_acqsig = %p, vt.acqsig = %d\n"), orig_acqsig, vt.acqsig);

	ioctl(keyfd, VT_ACTIVATE, new_console);

}
#endif	/* ALLOW_CONSOLE_SWITCH */

static void
textmode()
{
	command_exec_text("ShowVideo=!ShowVideo()");
//  restop();
//	features^= FE_VIDEO;
//  restart();
}


static u16 keyboardgetkeycode(u8 scan)
{
	struct kbentry ke;

	module_logger(&linuxKeyboard, _L|L_3, _("retrieving keycode for %d = "), scan);
	ke.kb_table = K_NORMTAB;
	ke.kb_index = scan;
	if (ioctl(keyfd, KDGKBENT, &ke) == 0) {
		logger(_L|L_3, _("%d\n"), ke.kb_value);
		return ke.kb_value;
	} else {
		logger(_L|L_3, _("not mapped\n"));
		return 0xff00 | scan;
	}
}


extern void keyboard_setkey(int onoff, u8 shiftmask, u8 key);

static void
kbfn(u16 keycode, u8 onoff, u8 val)
{
	module_logger(&linuxKeyboard, _L|L_1, _("function key\n"));
	if (val < 12) {
#if ALLOW_CONSOLE_SWITCH
		if (TESTKBDCRU(CTRL_R, CTRL_C) && TESTKBDCRU(FCTN_R, FCTN_C)) {
			switchconsole(val);
			return;
		} else
#endif
		{
// this is a hack 
//          if (val>=11 && onoff)
//              memset(crukeyboardmap, 0, sizeof(crukeyboardmap));
			/* special function key */
			keyboard_setkey(onoff, 0, SK_F1 + val);
		}
	} else {
		switch (keycode) {
		case K_INSERT:
			keyboard_setkey(onoff, FCTN, '2');
			break;
		case K_REMOVE:
			keyboard_setkey(onoff, FCTN, '1');
			break;
		case K_PGUP:
			keyboard_setkey(onoff, FCTN, '6');
			break;
		case K_PGDN:
			keyboard_setkey(onoff, FCTN, '4');
			break;
		case K_FIND: /*K(KT_FN, 20):, HOME*/
			keyboard_setkey(onoff,FCTN,'5');
			break;
		case K_SELECT: /*K(KT_FN, 23):, END*/
			keyboard_setkey(onoff,FCTN,'8');
			break;
		case K_PAUSE:
			if (onoff) {
				command_exec_text("Interactive=on\n");
			}
			break;
		default:
			module_logger(&linuxKeyboard, _L | LOG_USER, _("Unknown function keycode %04X\n"), keycode);
			break;
		}
	}
}


static void
kbspec(u16 keycode, u8 onoff)
{
	module_logger(&linuxKeyboard, _L|L_1, _("special key\n"));
	switch (keycode) {
	case K_ENTER:
		keyboard_setkey(onoff, 0, '\r');
		break;
	case K_BREAK:
		command_exec_text("Die\n");
		break;
	case K_CAPS:
		if (!onoff)
			caps ^= 1;
		break;
	case K_NUM:
		// allow shift+numlk (laptop)
		if (!(realshift & SHIFT)) {
			v9t9_restop();
			features &= ~FE_KEYBOARD;
			v9t9_restart();
		}
		break;
/*
	case K_HOLD:
		if (onoff)
			textmode();
		break;
*/
	default:
		module_logger(&linuxKeyboard, _L | LOG_USER, _("Unknown special keycode %04X\n"), keycode);
		break;
	}
}


static void
kbcur(u16 keycode, u8 onoff)
{
	u8          r, c;

	module_logger(&linuxKeyboard, _L|L_1, _("cursor key\n"));
	switch (keycode) {
	case K_UP:
		keyboard_setkey(onoff, FCTN, 'E');
		break;
	case K_DOWN:
		keyboard_setkey(onoff, FCTN, 'X');
		break;
	case K_LEFT:
		keyboard_setkey(onoff, FCTN, 'S');
		break;
	case K_RIGHT:
		keyboard_setkey(onoff, FCTN, 'D');
		break;
	default:
		module_logger(&linuxKeyboard, _L | LOG_USER, _("Unknown cursor keycode %04X\n"), keycode);
		break;
	}
}


static void
kbshift(u16 keycode, u8 onoff, u8 val)
{
	module_logger(&linuxKeyboard, _L|L_1, _("shift key\n"));
	switch (val) {
	case KG_SHIFT:
	case KG_SHIFTL:
	case KG_SHIFTR:
		keyboard_setkey(onoff, SHIFT, 0);
		break;

	case KG_CTRL:
	case KG_CTRLL:
	case KG_CTRLR:
		keyboard_setkey(onoff, CTRL, 0);
		break;

	case KG_ALT:
	case KG_ALTGR:
		keyboard_setkey(onoff, FCTN, 0);
		break;

	default:
		module_logger(&linuxKeyboard, _L | LOG_USER, _("Unknown shift keycode %04X\n"), keycode);
		break;
	}
}

/*
	All lowercase, unshifted.
*/
static void
kblatin(u16 keycode, u8 onoff, u8 val)
{

/*	Do not optimize away this table!  Although
	it is strikingly similar to the latinto9901[]
	array in keyboard.c, this map tells us what
	keys are not exact equivalents on the TI.
	Notably, the key combo SHIFT+'/' gives '?' on 
	the PC US keyboard, but '-' on the TI keyboard. */

	static u8   isdirectkey[128] = {
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	/* 0-7 */
		0x00, SK_TAB, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	/* 8-15 */
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	/* 16-23 */
		0x00, 0x00, 0x00, SK_ESC, 0x00, 0x00, 0x00, 0x00,	/* 24-31 */

		0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	/* 32-39 */
		0x00, 0x00, 0x00, 0x00, 0x72, 0x00, 0x71, 0x00,	/* 40-47 */
		0x45, 0x35, 0x31, 0x32, 0x33, 0x34, 0x44, 0x43,	/* 48-55 */
		0x42, 0x41, 0x00, 0x65, 0x00, 0x70, 0x00, 0x00,	/* 56-63 */

		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	/* 64-71 */
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	/* 72-79 */
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	/* 80-87 */
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	/* 88-95 */

		0x00, 0x25, 0x04, 0x02, 0x22, 0x12, 0x23, 0x24,	/* 96-103 */
		0x64, 0x52, 0x63, 0x62, 0x61, 0x73, 0x74, 0x51,	/* 104-111 */
		0x55, 0x15, 0x13, 0x21, 0x14, 0x53, 0x03, 0x11,	/* 112-119 */
		0x01, 0x54, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00	/* 120-127 */
	};

	module_logger(&linuxKeyboard, _L|L_1, _("Latin key %c [%02X]\n"), val, val);

	if (val < 128 && isdirectkey[val]) {
		if (isdirectkey[val] >= 128)
			keyboard_setkey(onoff, 0, isdirectkey[val]);
		else if (val >= 'a' && val <= 'z')
			keyboard_setkey(onoff, 0, val - 32);
		else
			keyboard_setkey(onoff, 0, val);
	} else if (val < 32 || val >= 128)
		module_logger(&linuxKeyboard, _L|LOG_ERROR | LOG_USER, 
				_("kblatin:  Got strange latin key %d\n"), val);
	else
		switch (val) {
		case '`':
			if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'W'))
				keyboard_setkey(onoff, FCTN, 'C');	/* ` */
			else
				keyboard_setkey(onoff, FCTN, 'W');	/* ~ */
			break;
		case '-':
			if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'U'))
				keyboard_setkey(onoff, SHIFT, '/');	/* - */
			else
				keyboard_setkey(onoff, FCTN, 'U');	/* _ */
			break;
		case '[':
			if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'F'))
				keyboard_setkey(onoff, FCTN, 'R');	/* [ */
			else
				keyboard_setkey(onoff, FCTN, 'F');	/* { */
			break;
		case ']':
			if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'G'))
				keyboard_setkey(onoff, FCTN, 'T');	/* ] */
			else
				keyboard_setkey(onoff, FCTN, 'G');	/* } */
			break;
		case '\'':
			if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'P'))
				keyboard_setkey(onoff, FCTN, 'O');	/* ' */
			else
				keyboard_setkey(onoff, FCTN, 'P');	/* " */
			break;
		case '/':
			if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'I'))
				keyboard_setkey(onoff, 0, '/');	/* / */
			else
				keyboard_setkey(onoff, FCTN, 'I');	/* ? */
			break;
		case '\\':
			if (!(realshift & SHIFT) && !keyboard_isset(FCTN, 'A'))
				keyboard_setkey(onoff, FCTN, 'Z');	/* \\ */
			else
				keyboard_setkey(onoff, FCTN, 'A');	/* | */
			break;
		case 127:
			keyboard_setkey(onoff, FCTN, 'S');
			break;
		default:
			module_logger(&linuxKeyboard, _L|LOG_ERROR | LOG_USER,
				 _("kblatin:  Unprogrammed fake key '%c' (%d)\n"), val, val);
		}
}

static void
kbpad(u16 keycode, u8 onoff)
{
	module_logger(&linuxKeyboard, _L|L_1, _("keypad key\n"));

	switch (keycode) {
	case K_PPLUS:
		keyboard_setkey(onoff, SHIFT, '=');
		break;
	case K_PMINUS:
		keyboard_setkey(onoff, SHIFT, '/');
		break;
	case K_PSLASH:
		keyboard_setkey(onoff, 0, '/');
		break;
	case K_PSTAR:
		keyboard_setkey(onoff, SHIFT, '8');
		break;
	case K_PDOT:
		keyboard_setkey(onoff, 0, '.');
		break;
	case K_PENTER:
		keyboard_setkey(onoff, 0, '\r');
		break;

		/* joystick */

#define NP_JOYST	1
#define NP_MAG		((int)onoff)

	case K_P8:
		keyboard_setjoyst(NP_JOYST, JOY_Y, 0, -NP_MAG, 0);
		break;
	case K_P2:
		keyboard_setjoyst(NP_JOYST, JOY_Y, 0, NP_MAG, 0);
		break;
	case K_P4:
		keyboard_setjoyst(NP_JOYST, JOY_X, -NP_MAG, 0, 0);
		break;
	case K_P6:
		keyboard_setjoyst(NP_JOYST, JOY_X, NP_MAG, 0, 0);
		break;
	case K_P7:
		keyboard_setjoyst(NP_JOYST, JOY_X | JOY_Y, -NP_MAG, -NP_MAG, 0);
		break;
	case K_P9:
		keyboard_setjoyst(NP_JOYST, JOY_X | JOY_Y, NP_MAG, -NP_MAG, 0);
		break;
	case K_P1:
		keyboard_setjoyst(NP_JOYST, JOY_X | JOY_Y, -NP_MAG, NP_MAG, 0);
		break;
	case K_P3:
		keyboard_setjoyst(NP_JOYST, JOY_X | JOY_Y, NP_MAG, NP_MAG, 0);
		break;
	case K_P5:
//		keyboard_setjoyst(NP_JOYST, JOY_X | JOY_Y, 0, 0, 0);
//		break;
	case K_P0:
		keyboard_setjoyst(NP_JOYST, JOY_B, 0, 0, NP_MAG);
		break;

	default:
		module_logger(&linuxKeyboard, _L | LOG_USER, _("Unknown keycode %04X\n"), keycode);
		break;
	}
}

static void
kbunmapped(unsigned char scancode, int onoff)
{
	switch (scancode)
	{
	case 0x7D:		// windows left
		keyboard_setkey(onoff, 0, SK_WIN_LEFT);
		break;
	case 0x7E:		// windows right
		keyboard_setkey(onoff, 0, SK_WIN_RIGHT);
		break;
	case 0x7F:		// windows menu
		keyboard_setkey(onoff, 0, SK_MENU);
		break;
	default:
		module_logger(&linuxKeyboard, _L | LOG_USER, _("Unknown scancode %d\n"), scancode);
		break;
	}
}

extern int  __svgalib_mouse_fd;

static int  last_x, last_y;

static void
scan_mouse(void)
{
	int         cur_x, cur_y, cur_b;

	if (!mouse_update())
		return;

	cur_x = mouse_getx();
	cur_y = mouse_gety();
	cur_b = mouse_getbutton();
	module_logger(&linuxKeyboard, _L|L_1, _("Mouse: (%d,%d,%d)\n"), cur_x, cur_y, cur_b);

	//  #define CLIP(x) ((x) < 0 ? (((x)+7)>>4) : (((x)-7)>>4) )
#define CLIP(x) ((x) > 32 ? 1 : (x) < -32 ? -1 : 0)

	if (cur_x | cur_y | cur_b) {
		keyboard_setjoyst(1,
						  JOY_X | JOY_Y | JOY_B,
						  CLIP(cur_x - last_x), CLIP(cur_y - last_y), cur_b);
		last_x = cur_x;
		last_y = cur_y;
	}

}

static vmResult keyboardupdate(void)
{
	u8          buff[256], *ptr;
	int         len;
	u16         keycode;
	u8          scan;
	u8          type, val, onoff;

	if (!(features & FE_KEYBOARD))
		return vmOk;

	while ((len = read(keyfd, buff, 256)) > 0) {
		module_logger(&linuxKeyboard, _L|L_2, _("got %d chars to read...\n"), len);
		ptr = buff;
		while (len--) {
			scan = *ptr++;
			onoff = !(scan & 0x80);
			scan &= 0x7f;
			keycode = keyboardgetkeycode(scan);
			type = keycode >> 8;
			val = keycode & 255;

			module_logger(&linuxKeyboard, _L|L_2, _("scan: %02X\n"), scan);
			module_logger(&linuxKeyboard, _L|L_2, _("Key: S:%d O:%d T:%d V:%d\n"), scan, onoff, type, val);


			/* unmapped keys are treated specially */
			if (keycode == K_HOLE) {
				keycode = 0xff00 | scan;
				type = 0xff;
			}

			switch (type) {
			case KT_PAD:
				kbpad(keycode, onoff);
				break;
			case KT_FN:
				kbfn(keycode, onoff, val);
				break;
			case KT_SPEC:
				kbspec(keycode, onoff);
				break;
			case KT_CUR:
				kbcur(keycode, onoff);
				break;
			case KT_SHIFT:
				kbshift(keycode, onoff, val);
				break;
			case KT_LATIN:
			case KT_LETTER:
				kblatin(keycode, onoff, val);
				break;
			case 0xFF:	// unmapped
				kbunmapped(scan, onoff);
				break;
			}
		}
	}
	scan_mouse();
	return vmOk;
}


static vmKeyboardModule myKbd = {
	3,
	keyboardupdate,
	linux_kbd_getspecialkeys
};

vmModule    linuxKeyboard = {
	3,
	"Linux SVGAlib keyboard",
	"kbSVGA",

	vmTypeKeyboard,
	vmFlagsExclusive,

	linux_kbd_detect,
	linux_kbd_init,
	linux_kbd_term,
	linux_kbd_enable,
	linux_kbd_disable,
	linux_kbd_restart,
	linux_kbd_restop,
	{(vmGenericModule *) & myKbd}
};
