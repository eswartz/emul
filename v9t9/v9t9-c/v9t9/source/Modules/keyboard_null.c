/*
  keyboard_null.c				-- V9t9 module for null keyboard (ouch!)

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
#include "v9t9.h"

#include "timer.h"

#include "keyboard.h"
#define _L LOG_USER|LOG_INFO

static      vmResult
nullkeyboard_detect(void)
{
	return vmOk;
}

static      vmResult
nullkeyboard_init(void)
{
	features |= FE_KEYBOARD;
	return vmOk;
}

static      vmResult
nullkeyboard_enable(void)
{
	return vmOk;
}

static      vmResult
nullkeyboard_disable(void)
{
	return vmOk;
}

static      vmResult
nullkeyboard_restart(void)
{
	realshift = 0;
	memset(crukeyboardmap, 0, 6);
	crukeyboardmap[6] = crukeyboardmap[7] = 0x0;
	module_logger(&nullKeyboard, LOG_WARN|LOG_USER, _("No keyboard driver loaded\n"));
	return vmOk;
}

static      vmResult
nullkeyboard_restop(void)
{
	return vmOk;
}

static      vmResult
nullkeyboard_term(void)
{
	return vmOk;
}

static      vmResult
nullkeyboard_scan(void)
{
	return vmOk;
}

static      vmResult
nullkeyboard_getspecialkeys(SpecialKey ** list)
{
	static SpecialKey none[] = { 0 };

	*list = none;
	return vmOk;
}

static vmKeyboardModule nullKbdModule = {
	3,
	nullkeyboard_scan,
	nullkeyboard_getspecialkeys
};

vmModule    nullKeyboard = {
	3,
	"Null keyboard",
	"kbdNull",

	vmTypeKeyboard,
	vmFlagsExclusive,

	nullkeyboard_detect,
	nullkeyboard_init,
	nullkeyboard_term,
	nullkeyboard_enable,
	nullkeyboard_disable,
	nullkeyboard_restart,
	nullkeyboard_restop,
	{(vmGenericModule *) & nullKbdModule}
};
