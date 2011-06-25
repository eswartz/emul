/*
  keyboard_qte.c				-- V9t9 module for Qt/Embedded

  (c) 1994-2002 Edward Swartz

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
#include "v9t9.h"

#include "timer.h"
#include "command.h"

#define RENAME_SHIFTS 1
#include "keyboard.h"
#define _L LOG_KEYBOARD

#include "qteloop.h"
#include <qevent.h>
#include <qnamespace.h>
#include "video_qte.h"

static int last_x,last_y;

int QTE_handle_key(QKeyEvent *e, int onoff)
{
	Qt::ButtonState state = e->state();
	int key = e->key();
	int myshift = 0;

	module_logger(&QTE_Keyboard, _L|L_1, _("QTE key = %02X/%02X (%d)\n"), key, state, onoff);

	// keyboard/etc register keys without keycodes?
	if (!key)
	{
		const QString &text = e->text();
		int c = (int)text[0];

		module_logger(&QTE_Keyboard, _L|L_1, _("QTE ASCII key = '%c' (%d)\n"), c, onoff);
		
		if (!isprint(c))
			return 0;

		key = (c >= 'a' && c <= 'z') ? c - 'a' + 'A' : c;
	}

	// a few troublesome keys that won't work because
	// they're shifted, and we already detect the shift key.
	switch (key)
	{
	case Qt::Key_Colon:		key = Qt::Key_Semicolon; break;
	case Qt::Key_Plus:		key = Qt::Key_Equal; break;
	case Qt::Key_Exclam:	key = Qt::Key_1; break;
	case Qt::Key_At:		key = Qt::Key_2; break;
	case Qt::Key_NumberSign:key = Qt::Key_3; break;
	case Qt::Key_Dollar:	key = Qt::Key_4; break;
	case Qt::Key_Percent:	key = Qt::Key_5; break;
	case Qt::Key_AsciiCircum:key = Qt::Key_6; break;
	case Qt::Key_Ampersand:	key = Qt::Key_7; break;
	case Qt::Key_Asterisk:	key = Qt::Key_8; break;
	case Qt::Key_ParenLeft:	key = Qt::Key_9; break;
	case Qt::Key_ParenRight:key = Qt::Key_0; break;
	case Qt::Key_Less:		key = Qt::Key_Comma; break;
	case Qt::Key_Greater:	key = Qt::Key_Period; break;
	}

	// in soft key mode, we use the ButtonState with the key event,
	// and don't track the modifier keys
	if (keyboard_soft_keys)
	{
		keyboard_soft_reset();

		if (state & Qt::ShiftButton) myshift |= SHIFT_M;
		if (state & Qt::ControlButton) myshift |= CTRL_M;
		if (state & Qt::AltButton) myshift |= FCTN_M;
		keyboard_setkey(onoff, myshift, 0);
	}

	if (key < 128 && ASCII_DIRECT_TO_9901(key)) {
		module_logger(&QTE_Keyboard, _L|L_1, _("direct key\n"));
		keyboard_setkey(onoff, 0, key);
	} else 
	{
		switch (key) {
			//case Qt::Key_Escape:
//			keyboard_setkey(onoff, 0, SK_ESC);
//			break;
		case Qt::Key_Tab:
			keyboard_setkey(onoff, 0, SK_TAB);
			break;

		case Qt::Key_F1: case Qt::Key_F2: case Qt::Key_F3: case Qt::Key_F4: case Qt::Key_F5:
		case Qt::Key_F6: case Qt::Key_F7: case Qt::Key_F8: case Qt::Key_F9: case Qt::Key_F10:
		case Qt::Key_F11: case Qt::Key_F12:
			keyboard_setkey(onoff, 0, SK_F1 + (key - Qt::Key_F1));
			break;

		case Qt::Key_ScrollLock:
		case Qt::Key_NumLock:
			if (!onoff)
				command_exec_text("DumpKeyMap\n");
			break;

		case Qt::Key_Return:
		case Qt::Key_Enter:
			keyboard_setkey(onoff, 0, '\r');
			break;

		case Qt::Key_CapsLock:
			if (!onoff)
				caps ^= 1;
			break;
		//case Qt::Key_Break:
		//if (onoff)
		//	command_exec_text("Die\n");
		//	break;
		case Qt::Key_Escape:
		case 0x009A:	// ???
			if (onoff)
				command_exec_text("Interactive on\n");
			break;

		case Qt::Key_Pause:
			if (onoff)
				command_exec_text("PauseComputer (!PauseComputer())\n");
			break;

			// shifts
		case Qt::Key_Shift:
			if (!keyboard_soft_keys) keyboard_setkey(onoff, SHIFT_M, 0);
			break;
		case Qt::Key_Control:
			if (!keyboard_soft_keys) keyboard_setkey(onoff, CTRL_M, 0);
			break;
		case Qt::Key_Alt:
		case Qt::Key_Meta:
			if (!keyboard_soft_keys) keyboard_setkey(onoff, FCTN_M, 0);
			break;

					// cursor
		case Qt::Key_Up:
			keyboard_setkey(onoff, FCTN_M, 'E');
			break;
		case Qt::Key_Down:
			keyboard_setkey(onoff, FCTN_M, 'X');
			break;
		case Qt::Key_Left:
		case Qt::Key_BackSpace:
			keyboard_setkey(onoff, FCTN_M, 'S');
			break;
		case Qt::Key_Right:
			keyboard_setkey(onoff, FCTN_M, 'D');
			break;
		case Qt::Key_Insert:
			keyboard_setkey(onoff, FCTN_M, '2');	// INSERT
			break;
		case Qt::Key_Delete:
			keyboard_setkey(onoff, FCTN_M, '1');	// DELETE
			break;
		case Qt::Key_PageUp:
			keyboard_setkey(onoff, FCTN_M, '6');	// PGUP
			break;
		case Qt::Key_PageDown:
			keyboard_setkey(onoff, FCTN_M, '4');	// PGDN
			break;
		case Qt::Key_Home:
			keyboard_setkey(onoff, FCTN_M, '5');	// BEGIN
			break;
		case Qt::Key_End:	//???
			keyboard_setkey(onoff, FCTN_M, '8');	// REDO
			break;
		case Qt::Key_Help:
			keyboard_setkey(onoff, FCTN_M, '7');	// AID
			break;

			// faked keys
		case Qt::Key_AsciiTilde:
			if (!(realshift & SHIFT_M) && !keyboard_isset(FCTN_M, 'W'))
				keyboard_setkey(onoff, FCTN_M, 'C');	/* ` */
			else
				keyboard_setkey(onoff, FCTN_M, 'W');	/* ~ */
			break;
		case Qt::Key_Minus:
		case Qt::Key_Underscore:
			if (!(realshift & SHIFT_M) && !keyboard_isset(FCTN_M, 'U'))
				keyboard_setkey(onoff, SHIFT_M, '/');	/* - */
			else
				keyboard_setkey(onoff, FCTN_M, 'U');	/* _ */
			break;
		case Qt::Key_BracketLeft:
		case Qt::Key_BraceLeft:
			if (!(realshift & SHIFT_M) && !keyboard_isset(FCTN_M, 'F'))
				keyboard_setkey(onoff, FCTN_M, 'R');	/* [ */
			else
				keyboard_setkey(onoff, FCTN_M, 'F');	/* { */
			break;
		case Qt::Key_BracketRight:
		case Qt::Key_BraceRight:
			if (!(realshift & SHIFT_M) && !keyboard_isset(FCTN_M, 'G'))
				keyboard_setkey(onoff, FCTN_M, 'T');	/* ] */
			else
				keyboard_setkey(onoff, FCTN_M, 'G');	/* } */
			break;
		case Qt::Key_Apostrophe:
		case Qt::Key_QuoteDbl:
			if (!(realshift & SHIFT_M) && !keyboard_isset(FCTN_M, 'P'))
				keyboard_setkey(onoff, FCTN_M, 'O');	/* ' */
			else
				keyboard_setkey(onoff, FCTN_M, 'P');	/* " */
			break;
		case Qt::Key_Slash:
		case Qt::Key_Question:
			if (!(realshift & SHIFT_M) && !keyboard_isset(FCTN_M, 'I'))
				keyboard_setkey(onoff, 0, '/');	/* / */
			else
				keyboard_setkey(onoff, FCTN_M, 'I');	/* ? */
			break;
		case Qt::Key_Backslash:
		case Qt::Key_Bar:
			if (!(realshift & SHIFT_M) && !keyboard_isset(FCTN_M, 'A'))
				keyboard_setkey(onoff, FCTN_M, 'Z');	/* \\ */
			else
				keyboard_setkey(onoff, FCTN_M, 'A');	/* | */
			break;

#if 0
			// mouse movement
#define NP_JOYST	1
#define NP_MAG		((int)onoff)

		case Qt::Key_KP_8:
			keyboard_setjoyst(NP_JOYST, JOY_Y, 0, -NP_MAG, 0);
			break;
		case Qt::Key_KP_2:
			keyboard_setjoyst(NP_JOYST, JOY_Y, 0, NP_MAG, 0);
			break;
		case Qt::Key_KP_4:
			keyboard_setjoyst(NP_JOYST, JOY_X, -NP_MAG, 0, 0);
			break;
		case Qt::Key_KP_6:
			keyboard_setjoyst(NP_JOYST, JOY_X, NP_MAG, 0, 0);
			break;
		case Qt::Key_KP_7:
			keyboard_setjoyst(NP_JOYST, JOY_X | JOY_Y, -NP_MAG, -NP_MAG, 0);
			break;
		case Qt::Key_KP_9:
			keyboard_setjoyst(NP_JOYST, JOY_X | JOY_Y, NP_MAG, -NP_MAG, 0);
			break;
		case Qt::Key_KP_1:
			keyboard_setjoyst(NP_JOYST, JOY_X | JOY_Y, -NP_MAG, NP_MAG, 0);
			break;
		case Qt::Key_KP_3:
			keyboard_setjoyst(NP_JOYST, JOY_X | JOY_Y, NP_MAG, NP_MAG, 0);
			break;
		case Qt::Key_KP_5:
//			keyboard_setjoyst(NP_JOYST, JOY_X | JOY_Y, 0, 0, 0);
//			break;
		case Qt::Key_KP_0:
			keyboard_setjoyst(NP_JOYST, JOY_B, 0, 0, NP_MAG);
			break;
#endif
		}
	}
	return 1;
}

int
QTE_handle_mouse(QMouseEvent *e, int pressrelease, int move)
{
	if (pressrelease)
	{
		module_logger(&QTE_Keyboard, _L|L_2, _("mouse press=%d\n"),
					  pressrelease);
		keyboard_setjoyst(1, JOY_B, 0, 0, pressrelease>0);

		last_x = e->x();
		last_y = e->y();
	}
	if (move)
	{
		int dx, dy;
		int mask_x = 0, mask_y = 0;

		if (pressrelease)
		{
			dx = last_x - e->x();
			dy = last_y - e->y();
		}
		else
		{
			dx = qteVideo->width() / 2 - e->x();
			dy = qteVideo->height() / 2 - e->y();
		}

		module_logger(&QTE_Keyboard, _L|L_2, _("mouse move=%d,%d -> %d,%d\n"),
					  last_x, last_y, e->x(), e->y());

		//last_x = e->x();
		//last_y = e->y();
		const int delta = 16;
		keyboard_setjoyst(1, JOY_X | JOY_Y, 
						  dx<-delta?1:dx>delta?-1:0, 
						  dy<-delta?1:dy>delta?-1:0, 0);
	}
}

void QteVideo::keyPressEvent(QKeyEvent *e)
{
	if (!QTE_handle_key(e, 1))
		e->ignore();
}

void QteVideo::keyReleaseEvent(QKeyEvent *e)
{
	if (!QTE_handle_key(e, 0))
		e->ignore();
}

void QteVideo::mousePressEvent(QMouseEvent *e)
{
	QTE_handle_mouse(e, 1, 0);
}

void QteVideo::mouseReleaseEvent(QMouseEvent *e)
{
	QTE_handle_mouse(e, -1, 0);
}

void QteVideo::mouseMoveEvent(QMouseEvent *e)
{
	QTE_handle_mouse(e, 0, 1);
}




static      vmResult
QTE_keyboard_detect(void)
{
	return vmOk;
}

static      vmResult
QTE_keyboard_init(void)
{
	features |= FE_KEYBOARD;

	// Zaurus software keyboards are unreliable
	command_exec_text("SoftKeyScanning on\n");
	return vmOk;
}

static      vmResult
QTE_keyboard_enable(void)
{
	return vmOk;
}

static      vmResult
QTE_keyboard_disable(void)
{
	return vmOk;
}

static      vmResult
QTE_keyboard_restart(void)
{
	realshift = 0;
	memset(crukeyboardmap, 0, 6);
	crukeyboardmap[6] = crukeyboardmap[7] = 0x0;
	return vmOk;
}

static      vmResult
QTE_keyboard_restop(void)
{
	return vmOk;
}

static      vmResult
QTE_keyboard_term(void)
{
	return vmOk;
}

static      vmResult
QTE_keyboard_scan(void)
{
	return vmOk;
}

static      vmResult
QTE_keyboard_getspecialkeys(SpecialKey ** list)
{
	static SpecialKey none[] = { (SpecialKey)0 };

	*list = none;
	return vmOk;
}

static vmKeyboardModule QTE_KbdModule = {
	3,
	QTE_keyboard_scan,
	QTE_keyboard_getspecialkeys
};

vmModule    QTE_Keyboard = {
	3,
	"Qt/Embedded keyboard",
	"kbdQTE",

	vmTypeKeyboard,
	vmFlagsExclusive,

	QTE_keyboard_detect,
	QTE_keyboard_init,
	QTE_keyboard_term,
	QTE_keyboard_enable,
	QTE_keyboard_disable,
	QTE_keyboard_restart,
	QTE_keyboard_restop,
	{(vmGenericModule *) & QTE_KbdModule}
};
