/*
  sound_speaker.c				-- V9t9 module for PC speaker sound

  This uses the KIOCSOUND ioctl().

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

#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>

#include <stdlib.h>
#include <sys/kd.h>
#include <sys/ioctl.h>
#include <errno.h>

#include "v9t9_common.h"
#include "timer.h"
#include "sound.h"
#include "command.h"

/*	forward */
extern vmModule linuxSpeakerSound;

extern int  console_fd;

/****************************/

static int  togglerate = 45;

static u16  stlasthertz;
static u8   stvoice;

/*	Set the speaker to a given frequency  */
static void
speaker_hertz(u16 hertz)
{
	u32         chip;

	if (!(linuxSpeakerSound.runtimeflags & vmRTInUse))
		return;

	if (hertz) {
		if (hertz == stlasthertz)
			return;

		chip = (hertz <= 0x12 ? 0 : 0x1234dd / hertz);
		ioctl(console_fd, KIOCSOUND, chip);
		stlasthertz = hertz;
	} else {
		ioctl(console_fd, KIOCSOUND, 0);
		stlasthertz = 0;
	}
}

/*	Update several times a second in an arpeggio  */
static void
speaker_poll(void)
{
	u8          next = stvoice;
	struct voiceinfo *v;
	int         tries;

	if (features & FE_PLAYSOUND)
	{
		module_logger(&linuxSpeakerSound, LOG_SOUND|L_4, _("speaker_poll\n"));
		tries = 4;
		while (tries) {
			v = &sound_voices[next];
			module_logger(&linuxSpeakerSound, LOG_SOUND|L_4, _("speaker_poll:  next=%d, v->volume=%d, v->hertz=%d\n"),
				  next,  v->volume, v->hertz);
			if (v->volume) {
				if (next < VOICE_NOISE) {
					speaker_hertz(v->hertz);
					stvoice = (next + 1) % 4;
					break;
				} else if (OPERATION_TO_NOISE_TYPE(v) != NOISE_WHITE) {
					speaker_hertz(v->hertz / 16);
					stvoice = (next + 1) % 4;
					break;
				}
			}
			tries--;
			next = (next + 1) % 4;
		}
		if (!tries) {
			speaker_hertz(0);
		}
	}
	else {
		speaker_hertz(0);
	}
}

/*****************************/

/*	Update voice parameters  */
static void
speaker_update(vmsUpdateMask updated)
{
	if (features & FE_PLAYSOUND) {
		if (updated & (vms_Tv0 | vms_Tv1 | vms_Tv2 |
					   vms_Vv0 | vms_Vv1 | vms_Vv2))
		{
			speaker_poll();
		}
	}
}

static void
speaker_flush(void)
{
	speaker_hertz(0);
}

/**************************************/

static int  speaker_event_tag;

static      vmResult
speaker_detect(void)
{
	if (console_fd > 0) {
		module_logger(&linuxSpeakerSound, LOG_SOUND | LOG_USER, _("Detected PC speaker..\n"));
		return vmOk;
	} else {
		return vmNotAvailable;
	}
}

static
DECL_SYMBOL_ACTION(speaker_change_event)
{
	if (togglerate < 0)
		togglerate = 0;
	else if (togglerate > TM_HZ)
		togglerate = TM_HZ;
	TM_SetEvent(speaker_event_tag, TM_HZ * 100 / togglerate, 0,
				TM_FUNC | TM_REPEAT, speaker_poll);
	return 1;
}

static      vmResult
speaker_init(void)
{
	command_symbol_table *speakercommands =
		command_symbol_table_new(_("PC Speaker Options"),
								 _("These commands control the PC speaker sound module"),

		 command_symbol_new("SpeakerArpeggioRate",
							_("Set base rate for arpeggiation"),
							c_STATIC,
							speaker_change_event,
							NULL /* ret */ ,
							command_arg_new_num(_("Hz"),
												_("changes per second"),
												NULL	/* action */,
												NEW_ARG_NUM
												(togglerate),
												NULL 	/* next */),

		 NULL 	/* next */ ),
		 NULL 	/* sub */ ,
		 NULL	/* next */
	);

	command_symbol_table_add_subtable(universe, speakercommands);

	if (console_fd <= 0) {
		module_logger(&linuxSpeakerSound, LOG_SOUND | LOG_USER | LOG_ERROR,
			   _("Could not open console for sound effects (%s)\n"),
			   strerror(errno));
		return vmNotAvailable;
	}

	speaker_event_tag = TM_UniqueTag();

	return vmOk;
}

static      vmResult
speaker_term(void)
{
	speaker_hertz(0);
	return vmOk;
}

static      vmResult
speaker_enable(void)
{
	return vmOk;
}

static      vmResult
speaker_disable(void)
{
	return vmOk;
}


static      vmResult
speaker_restart(void)
{
	stvoice = 0;
	stlasthertz = 0;
	/*  This event does all the work  */
	TM_SetEvent(speaker_event_tag, TM_HZ * 100 / togglerate, 0,
				TM_FUNC | TM_REPEAT, speaker_poll);
	return vmOk;
}

static      vmResult
speaker_restop(void)
{
	TM_ResetEvent(speaker_event_tag);
	speaker_hertz(0);
	return vmOk;
}

static vmSoundModule linuxSpeakerSoundModule = {
	4,
	speaker_update,
	speaker_flush,
	NULL,
	NULL,
	NULL
};

vmModule    linuxSpeakerSound = {
	3,
	"Linux PC speaker",
	"sndSpeaker",

	vmTypeSound,
	0,

	speaker_detect,
	speaker_init,
	speaker_term,
	speaker_enable,
	speaker_disable,
	speaker_restart,
	speaker_restop,

	{(vmGenericModule *) & linuxSpeakerSoundModule}
};
