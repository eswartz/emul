/*
  keyboard.c					-- 9901 hardware-level keyboard routines

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

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <ctype.h>
#include <string.h>
#include <signal.h>

#define __KEYBOARD__

#include "v9t9_common.h"
#include "v9t9.h"
#include "command.h"
#include "command_parser.h"
#include "keyboard.h"
#include "timer.h"

#define _L	 LOG_KEYBOARD | LOG_INFO

u8          crukeyboardmap[8];
u8          caps, crukeyboardcol, AlphaLock;
u8          realshift;
u16         specialkey;

u8          ctrlmap[256], fctnmap[256], shiftmap[256];
u32         cctrl, cfctn, cshift;
u8			fakemap[256];

int			keyboard_soft_keys;			// reset keymap at regular intervals?
static int	keyboard_soft_reset_tick;	// last tick we reset
int			keyboard_soft_reset_rate = 2;	// if above is set, the interval (Hz)
static int	keyboard_soft_reset_tick_delta;	// # ticks to wait

static void validate_shifts(void);

// these names map to SpecialKey in v9t9_module.h
static const char *special_key_names[] = 
{
	"Pause",
	"F1", "F2", "F3", "F4", "F5", "F6", 
	"F7", "F8", "F9", "F10", "F11", "F12",
	"Esc", "Tab",
	"WinLeft", "WinRight", "WinMenu"
};

#define NUMBINDINGS (SK_LAST-SK_FIRST)

/*
    Bindings for special keys.
	If the 'key' field is set, it must be set to
	special_key_names[x] for key_binds[x];
	otherwise, the keyboard module cannot generate the key.
*/
struct {
	const char *key;
	char       *up, *down;
} key_binds[NUMBINDINGS];

u8          bindmap[NUMBINDINGS];	/* keys currently held down */

static int  keyboard_event_tag;

static      
DECL_SYMBOL_ACTION(keyboard_bind_key)
{
	char       *key;
	char       *bind;
	int         updown;			// -1 = up, 0 = none, 1 = down
	int         x;

	if (task == csa_READ) {
		static int bindidx;		// >0 --> find next, <0 --> show up
		char tmp[32];

		if (!iter)
			bindidx = 0;

		// printed up before?
		if (bindidx < 0) {
			sprintf(tmp, "-%s", key_binds[-bindidx].key);
			command_arg_set_string(sym->args, tmp);
			command_arg_set_string(sym->args->next, key_binds[-bindidx].up);
			bindidx = (-bindidx)+1;
			return 1;
		}

		/* emit bindings for all special keys, even if current 
		   module doesn't support them */
		while (bindidx < NUMBINDINGS && 
			   (/*!key_binds[bindidx].key ||*/
				   !key_binds[bindidx].up ||
				   !key_binds[bindidx].down)) 
			bindidx++;
		if (bindidx >= NUMBINDINGS)
			return 0;
		
		if (key_binds[bindidx].up && key_binds[bindidx].down) {
			sprintf(tmp, "+%s", special_key_names[bindidx]);
			command_arg_set_string(sym->args, tmp);
			command_arg_set_string(sym->args->next, key_binds[bindidx].down);
			bindidx = -bindidx;
		} else {
			command_arg_set_string(sym->args, special_key_names[bindidx]);
			command_arg_set_string(sym->args->next, 
								   key_binds[bindidx].up ? 
								   key_binds[bindidx].up :
								   key_binds[bindidx].down);
			bindidx++;
		}
		return 1;
	}

	if (!command_arg_get_string(sym->args, &key) ||
		!command_arg_get_string(sym->args->next, &bind))
		return 0;

	if (*key == '+') {
		updown = 1;
		key++;
	} else if (*key == '-') {
		updown = -1;
		key++;
	} else
		updown = 0;

	logger(_L | L_0, _("Binding %scommand to '%s':\n\t%s\n"),
		 updown < 0 ? _("up ") : updown > 0 ? _("down ") : "", key, bind);

	for (x = 0; x < NUMBINDINGS; x++) {
		if (!strcasecmp(special_key_names[x], key)) {
			// if not a [+-]key, then it is a down key only,
			// so remove up action
			if (updown <= 0) {
				if (key_binds[x].up) {
					logger(_L | 0, _("Unbinding old 'up' command:\n\t%s\n\n"),
						 key_binds[x].up);
					xfree(key_binds[x].up);
				}
				if (updown)
					key_binds[x].up = xstrdup(bind);
				else
					key_binds[x].down = xstrdup(bind);
			} else if (updown > 0) {
				if (key_binds[x].down) {
					logger(_L | 0, _("Unbinding old 'down' command:\n\t%s\n\n"),
						 key_binds[x].down);
					xfree(key_binds[x].down);
				}
				key_binds[x].down = xstrdup(bind);
			}
			break;
		}
	}
	if (x >= NUMBINDINGS) {
		command_logger(LOG_ERROR, _("BindKey: key '%s' not defined\n"), key);
		return 0;
	}
	return 1;
}

static
DECL_SYMBOL_ACTION(keyboard_list_keys)
{
	int         x;
	int         y = 0;
	bool		any = false;

	if (task != csa_WRITE)
		return 1;
	for (x = 0; x < NUMBINDINGS; x++) {
		if (key_binds[x].key) {
			any = true;
			if ((y += strlen(key_binds[x].key)) >= 80) {
				logger(_L | LOG_USER, "\n");
				y = 0;
			}
			logger(_L | LOG_USER, "%s ", key_binds[x].key);
		}
	}
	logger(_L | LOG_USER, "\n");
	if (!any)
		logger(_L | LOG_USER, _("<no keys to bind>\n"));
	return 1;
}

static
DECL_SYMBOL_ACTION(keyboard_list_bindings)
{
	int         x;
	bool		any = false;

	if (task != csa_WRITE)
		return 1;
	for (x = 0; x < NUMBINDINGS; x++) {
		if (!key_binds[x].key) {
			logger(_L | LOG_USER, _("<%s unavailable>\n"), special_key_names[x]);
		} else 	if (key_binds[x].up && key_binds[x].down) {
			logger(_L | LOG_USER, "+%s: %s\n" "-%s: %s\n",
				 key_binds[x].key, key_binds[x].down,
				 key_binds[x].key, key_binds[x].up);
		} else if (key_binds[x].up || key_binds[x].down) {
			logger(_L | LOG_USER, "%s: %s\n", key_binds[x].key,
				 key_binds[x].down ? key_binds[x].down :
				 key_binds[x].up ? key_binds[x].up : _("<empty>"));
		} else 
			continue;
		any = true;
	}
	if (!any)
		logger(_L | LOG_USER, _("<no keys bound>\n"));
	return 1;
}

static
DECL_SYMBOL_ACTION(keyboard_dump_crumap)
{
	int i,j,b;

	logger(_L | LOG_USER, _("Map of keyboard CRU:\n"));
	for (i = 0; i < 8; i++) {
		for (b = 1, j = 0; j < 8; b<<=1,j++) {
			logger(_L | LOG_USER, "%d ", !!(crukeyboardmap[i]&b));
		}
		logger(_L | LOG_USER, "\n");
	}
	return 1;
}

static
DECL_SYMBOL_ACTION(keyboard_set_softkeys)
{
	int num;
	if (!command_arg_get_num(SYM_ARG_1st, &num))
		return 0;

	if (keyboard_soft_keys != num)
	{
		keyboard_soft_keys = num;
		keyboard_soft_reset_tick_delta = TM_HZ*100/keyboard_soft_reset_rate;
		keyboard_soft_reset();
	}
	return 1;
}

static
DECL_SYMBOL_ACTION(keyboard_set_keyresetrate)
{
	int num;
	if (!command_arg_get_num(SYM_ARG_1st, &num))
		return 0;

	if (num < 0) num = 1; else if (num > TM_HZ) num = TM_HZ;
	keyboard_soft_reset_tick_delta = TM_HZ*100/num;
	if (keyboard_soft_keys)
	{
		keyboard_soft_reset();
	}
	return 1;
}

static DECL_SYMBOL_ACTION(keyboard_reset)
{
	keyboard_soft_reset();
	return 1;
}


/*	Install commands */
int
keyboard_preconfiginit(void)
{
	command_symbol_table *keyboardcommands =
	  command_symbol_table_new(_("Keyboard / Joystick Options"),
								 _("These are generic commands for controlling the keyboard and joystick emulation"),

		command_symbol_new("AlphaLock",
							_("Enable or disable ALPHA LOCK state (i.e., upon startup; CAPS performs this function at runtime)"),
							c_STATIC,
							NULL /* action */ ,
							RET_FIRST_ARG,
							command_arg_new_enum
							("off|on", 
							_("state"),
							 NULL,
							 ARG_NUM(caps),
							 NULL /* next */ )
							,

		command_symbol_new
							("BindKey",
							 _("Bind a non-TI key to a command"),
							 c_DYNAMIC,
							 keyboard_bind_key,
							 NULL /* ret */ ,
							 command_arg_new_string
							 (_("key"),
							  _("symbolic name of key (see ListKeys); "
							  "bare key name means 'perform command when key is pressed'; "
							  "+key means 'perform command when key is pressed' and "
							  "'perform command bound to -key when key is released'"),
							  NULL /* action */ ,
							  NEW_ARG_STR(16),
							  command_arg_new_string
							  (_("command"),
							   _("text of command to execute"),
							   NULL /* action */ ,
							   NEW_ARG_NEW_STRBUF,
							   NULL /* next */ ))
							 ,
		command_symbol_new
							 ("ListKeys",
							  _("List symbolic names of bindable keys"),
							  c_DONT_SAVE,
							  keyboard_list_keys,
							  NULL /* ret */ ,
							  NULL	/* args */
							  ,
		command_symbol_new
							  ("ListBindings",
							   _("List current key bindings"),
							   c_DONT_SAVE,
							   keyboard_list_bindings,
							   NULL /* ret */ ,
							   NULL	/* args */
							   ,
		command_symbol_new
							  ("DumpKeyMap",
							   _("Display map of current TI keys held down"),
							   c_DONT_SAVE,
							   keyboard_dump_crumap,
							   NULL /* ret */ ,
							   NULL	/* args */
							   ,

		command_symbol_new
							 ("ResetKeyboard",
							  _("Reset keyboard (i.e. when a key seems stuck)"),
							  c_DONT_SAVE,
							  keyboard_reset,
							  NULL /* ret */ ,
							  NULL	/* args */
							  ,
    	 command_symbol_new("SoftKeyScanning",
    						_("Enable software keyboard mode; "
							  "this mode remembers only the last key pressed "
							  "and flushes the keyboard map at regular intervals; "
							  "used with clients that do not reliably send key release "
							  "events.  Default set by client."),
							c_STATIC,
    						keyboard_set_softkeys /* action */ ,
    						RET_FIRST_ARG,
    						command_arg_new_enum
    						(_("off|on"),
    						 _("off: assume key press and release events are reliable\n"
								 "on: handle one key at a time, flush keymap KeyboardFlushRate times per second\n"),
    						 NULL /* action */ ,
							 NEW_ARG_NUM(bool),
    						 NULL /* next */ )
    						,

    	 command_symbol_new("KeyResetRate",
    						_("Control rate keyboard map is cleared when SoftKeyScanning mode is enabled "
								"and keyboard is idle"),
							c_STATIC,
    						keyboard_set_keyresetrate /* action */ ,
    						RET_FIRST_ARG,
    						command_arg_new_num
    						(_("hertz"),
    						 _("times per second to flush keyboard map\n"),
    						 NULL /* action */ ,
							 ARG_NUM(keyboard_soft_reset_rate),
    						 NULL /* next */ )
    						,

							   NULL /* next */ )))))))),

		 NULL /* sub */ ,

		 NULL	/* next */
		);

	command_symbol_table_add_subtable(universe, keyboardcommands);
	return 1;
}

/*	Ask keyboard module for its list of key names */
int
keyboard_postconfiginit(void)
{
	SpecialKey	*array;

	logger(_L | L_1, "keyboard_postconfiginit\n");
	if (features & FE_KEYBOARD) {
		int         x;

		vmKeyboard->m.kbd->getspecialkeys(&array);
		for (x = 0; array[x]; x++) {
			if (!(array[x] >= SK_FIRST && array[x] < SK_LAST)) {
				logger(_L | LOG_INTERNAL, _("Illegal special key value %d in %s, ignoring\n"),
					   array[x], vmKeyboard->name);
			} else {
				key_binds[array[x] - SK_FIRST].key = special_key_names[array[x] - SK_FIRST];
			}
		}
	}
	return 1;
}

/*	Activate command macro

	key: 0<=key<KEYBINDINGS 
	'shift' is ignored currently 

*/
static void
keyboard_macro(int onoff, u8 shift, u8 key)
{
	if (onoff) {
		if (bindmap[key] && key_binds[key].up)
			return;				/* assume it's bad to repeat this */
		else if (key_binds[key].down) {
			bindmap[key] = 1;
			logger(_L | L_0, _("Activating binding for +'%s'\n\n\n"),
				 key_binds[key].key);
			command_exec_text(key_binds[key].down);
		} else {
			if (key_binds[key].key)
				logger(_L | LOG_USER, _("Nothing bound to '+%s'\n\n"), key_binds[key].key);
			else
				logger(_L | LOG_USER, _("Special key %d not supported\n\n"), key);

		}
	} else {
		bindmap[key] = 0;		/* don't care if it wasn't down */
		if (key_binds[key].up) {
			logger(_L | L_0, _("Activating binding for -'%s'\n\n"),
				 key_binds[key].key);
			command_exec_text(key_binds[key].up);
		}
	}
}

/*
 *	This routine is the timer event interface to the 
 *	v9t9 keyboard module, which calls us  back to set specific keys
 *	in the CRU map.
 */
static void
keyboard_scan(int tag)
{
	// time to clear keyboard?
	if (keyboard_soft_keys)
	{
		if (TM_GetTicks() >= keyboard_soft_reset_tick + keyboard_soft_reset_tick_delta)
			keyboard_soft_reset();
	}

	validate_shifts();
	if (features & FE_KEYBOARD)
		vmKeyboardMain->m.kbd->scan();
}

// Client calls this before setting new keys, and scanner calls it periodically
void
keyboard_soft_reset(void)
{
	int i;

	// partial reset; keep joystick
	for (i=0; i<6; i++)
		crukeyboardmap[i] = 0;

	memset(ctrlmap, 0, sizeof(ctrlmap));
	memset(fctnmap, 0, sizeof(fctnmap));
	memset(shiftmap, 0, sizeof(shiftmap));
	memset(fakemap, 0, sizeof(fakemap));
	cctrl = cfctn = cshift = 0;

	keyboard_soft_reset_tick = TM_GetTicks();
}

int
keyboard_restart(void)
{
	memset(crukeyboardmap, 0, sizeof(crukeyboardmap));
	memset(ctrlmap, 0, sizeof(ctrlmap));
	memset(fctnmap, 0, sizeof(fctnmap));
	memset(shiftmap, 0, sizeof(shiftmap));
	memset(fakemap, 0, sizeof(fakemap));
	cctrl = cfctn = cshift = 0;

	if (!keyboard_event_tag)
		keyboard_event_tag = TM_UniqueTag();
	TM_SetEvent(keyboard_event_tag, TM_HZ * 100 / 50, 0,
				TM_FUNC | TM_REPEAT, keyboard_scan);
	return 1;
}

void
keyboard_restop(void)
{
	TM_ResetEvent(keyboard_event_tag);
}


/************************************************************/


/*	Map of ASCII codes and their direct CRU mapping
	(high nybble=row, low nybble=column), except for 0xff,
	which should be faked. */

/*	NOTE: 47 = '/' in Latin-1 corresponds to the US keyboard key '/'
	and '?', but on the TI keyboard, 0x75 this is the key for '/' and
	'-'.  The target-specific code must trap '-', '/', '?', '_'
	and should use FCTN+I for '?'.*/
u8          latinto9901[128] = {
	0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,	/* 0-7 */
	0xff, 0xff, 0xff, 0xff, 0xff, 0x50, 0xff, 0xff,	/* 8-15 */
	0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,	/* 16-23 */
	0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,	/* 24-31 */

	0x60, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,	/* 32-39 */
	0xff, 0xff, 0xff, 0xff, 0x72, 0xff, 0x71, 0x75,	/* 40-47 */
	0x45, 0x35, 0x31, 0x32, 0x33, 0x34, 0x44, 0x43,	/* 48-55 */
	0x42, 0x41, 0xff, 0x65, 0xff, 0x70, 0xff, 0xff,	/* 56-63 */

	0xff, 0x25, 0x04, 0x02, 0x22, 0x12, 0x23, 0x24,	/* 64-71 */
	0x64, 0x52, 0x63, 0x62, 0x61, 0x73, 0x74, 0x51,	/* 72-79 */
	0x55, 0x15, 0x13, 0x21, 0x14, 0x53, 0x03, 0x11,	/* 80-87 */
	0x01, 0x54, 0x05, 0xff, 0xff, 0xff, 0xff, 0xff,	/* 88-95 */

	0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,	/* 96-103 */
	0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,	/* 104-111 */
	0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff,	/* 112-119 */
	0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff	/* 120-127 */
//      0xff,0x25,0x04,0x02,0x22,0x12,0x23,0x24,        /* 96-103 */
//      0x64,0x52,0x63,0x62,0x61,0x73,0x74,0x51,        /* 104-111 */
//      0x55,0x15,0x13,0x21,0x14,0x53,0x03,0x11,        /* 112-119 */
//      0x01,0x54,0x05,0xff,0xff,0xff,0xff,0xff         /* 120-127 */
};

void
keyboard_setkey(int onoff, u8 shift, u8 key)
{
	u8          b, r, c;

	if (shift && onoff)
		logger(_L | L_1, "turned on [%d]:  cshift=%d, cctrl=%d, cfctn=%d\n",
			 shift, cshift, cctrl, cfctn);

	/* macros bound to high keys */
	if (key >= 128) {
		keyboard_macro(onoff, shift, key - 128);
		return;
	}

	/*  This complicated code maintains a map of shifts
	   that we've explicitly turned on with other keys.  The
	   reason we need to know all this is that there are
	   multiple "on" events (repeats) but only one "off"
	   event.  If we do "left arrow on" (FCTN+S), 
	   "right arrow on" (FCTN+D), and "left arrow off" (FCTN+S)
	   we cannot reset FCTN since FCTN+D is still pressed.  Etc. */

	if (!onoff && !shift && fakemap[key]) {
		logger(_L | L_1, _("Resetting %d for key %d\n"), fakemap[key], key);
		shift |= fakemap[key];
	}
	fakemap[key] = onoff ? shift : 0;

	if (shift & SHIFT) {
		if (onoff) {
			if (!shiftmap[key]) {
				shiftmap[key] = 1;
				cshift++;
			}
			CHANGEKBDCRU(SHIFT_R, SHIFT_C, 1);
		} else {
			if (shiftmap[key]) {
				shiftmap[key] = 0;
				cshift--;
			}
			if (cshift == 0)
				CHANGEKBDCRU(SHIFT_R, SHIFT_C, 0);
		}
	}
	if (shift & FCTN) {
		if (onoff) {
			if (!fctnmap[key]) {
				fctnmap[key] = 1;
				cfctn++;
			}
			CHANGEKBDCRU(FCTN_R, FCTN_C, 1);
		} else {
			if (fctnmap[key]) {
				fctnmap[key] = 0;
				cfctn--;
			}
			if (cfctn == 0)
				CHANGEKBDCRU(FCTN_R, FCTN_C, 0);
		}
	}
	if (shift & CTRL) {
		if (onoff) {
			if (!ctrlmap[key]) {
				ctrlmap[key] = 1;
				cctrl++;
			}
			CHANGEKBDCRU(CTRL_R, CTRL_C, 1);
		} else {
			if (ctrlmap[key]) {
				ctrlmap[key] = 0;
				cctrl--;
			}
			if (cctrl == 0)
				CHANGEKBDCRU(CTRL_R, CTRL_C, 0);
		}
	}

	if (key) {
		b = latinto9901[key];
		if (b == 0xff)
			logger(_L | LOG_ERROR,
				 _("keyboard_setkey:  got a key that should be faked '%c' (%d)\n\n"),
				 key, key);
		r = b >> 4;
		c = b & 15;
		CHANGEKBDCRU(r, c, onoff);
	} else {
		if (shift & SHIFT)
			realshift = (realshift & !SHIFT) | (onoff ? SHIFT : 0);
		if (shift & CTRL)
			realshift = (realshift & !CTRL) | (onoff ? CTRL : 0);
		if (shift & FCTN)
			realshift = (realshift & !FCTN) | (onoff ? FCTN : 0);
	}

	if (shift && !onoff)
		logger(_L | L_1, "turned off [%d]: cshift=%d, cctrl=%d, cfctn=%d\n\n",
			 shift, cshift, cctrl, cfctn);

}

/*
 *	even with the complex checking above, fake shifts can still
 *	get stuck -- check every cycle to verify that something is
 *	held down besides a shift.
 */
#define ALL_SHIFTS	((0x80 >> CTRL_R) | (0x80 >> FCTN_R) | (0x80 >> SHIFT_R))
static void
validate_shifts(void)
{
	// all shifts are on column 0
	if (crukeyboardmap[0]
	&& 	!realshift
	&&	!(crukeyboardmap[0] & ~ALL_SHIFTS))
	{
		int col;
		for (col = 1; col < 6; col++)
			if (crukeyboardmap[col])
				return;
		
		logger(_L|L_1, _("Clearing fake shifts\n"));

		memset(shiftmap, 0, sizeof(shiftmap));
		memset(ctrlmap, 0, sizeof(ctrlmap));
		memset(fctnmap, 0, sizeof(fctnmap));
		crukeyboardmap[0] &= ~ALL_SHIFTS;
		cshift = cfctn = cctrl = 0;
	}
}

int
keyboard_isset(u8 shift, u8 key)
{
	u8          b, r, c;
	int         res = 0;

	if (shift & SHIFT && TESTKBDCRU(SHIFT_R, SHIFT_C))
		res = 1;
	if (shift & CTRL && TESTKBDCRU(CTRL_R, CTRL_C))
		res = 1;
	if (shift & FCTN && TESTKBDCRU(FCTN_R, FCTN_C))
		res = 1;

	if (key) {
		b = latinto9901[key];
		if (b == 0xff)
			logger(_L | L_0,
				 _("keyboard_isset:  got a key that should be faked '%c' (%d)\n\n"),
				 key, key);
		r = b >> 4;
		c = b & 15;
		return res && TESTKBDCRU(r, c);
	} else
		return res;
}

/*	Set joystick bits.  
	'mask' tells whether to set axes or buttons. */

//  Joy = 1 or 2, x=-1,0,1, y=-1,0,1, fire=0,1
void
keyboard_setjoyst(int joy, int mask, int x, int y, int fire)
{
	if (mask & JOY_X) {
		logger(_L | L_1, _("changing JOY_X (%d)\n\n"), x);
		CHANGEJOYCRU(joy, JOY_LEFT_R, x < 0);
		CHANGEJOYCRU(joy, JOY_RIGHT_R, x > 0);
	}
	if (mask & JOY_Y) {
		logger(_L | L_1, _("changing JOY_Y (%d)\n\n"), y);
		CHANGEJOYCRU(joy, JOY_UP_R, y < 0);
		CHANGEJOYCRU(joy, JOY_DOWN_R, y > 0);
	}
	if (mask & JOY_B) {
		logger(_L | L_1, _("changing JOY_B (%d)\n\n"), fire);
		CHANGEJOYCRU(joy, JOY_FIRE_R, fire);
	}

	/*  clear unused bits  */
	CHANGEJOYCRU(joy, 0, 0);
	CHANGEJOYCRU(joy, 1, 0);
	CHANGEJOYCRU(joy, 2, 0);
}
