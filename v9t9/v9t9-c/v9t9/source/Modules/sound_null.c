/*
  sound_null.c					-- V9t9 module for null sound

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

#include "v9t9_common.h"
#include "sound.h"

static      vmResult
nullspeaker_detect(void)
{
	return vmOk;
}

static      vmResult
nullspeaker_init(void)
{
	return vmOk;
}

static      vmResult
nullspeaker_term(void)
{
	return vmOk;
}

static      vmResult
nullspeaker_enable(void)
{
	module_logger(&nullSound, LOG_WARN|LOG_USER, _("No sound driver loaded\n"));
	return vmOk;
}

static      vmResult
nullspeaker_disable(void)
{
	return vmOk;
}

static      vmResult
nullspeaker_restart(void)
{
	return vmOk;
}

static      vmResult
nullspeaker_restop(void)
{
	return vmOk;
}

static vmSoundModule nullSoundModule = {
	4,
	NULL,
	NULL,
	NULL,
	NULL
};

vmModule    nullSound = {
	3,
	"Null sound",
	"sndNull",

	vmTypeSound,
	vmFlagsExclusive,

	nullspeaker_detect,
	nullspeaker_init,
	nullspeaker_term,
	nullspeaker_enable,
	nullspeaker_disable,
	nullspeaker_restart,
	nullspeaker_restop,

	{(vmGenericModule *) & nullSoundModule}
};
