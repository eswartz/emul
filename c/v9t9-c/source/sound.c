/*
  sound.c						-- TMS9918 sound chip and cassette routines

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

#include <malloc.h>
#include "v9t9_common.h"
#include "timer.h"
#include "sound.h"
#include "demo.h"
#include "command.h"

#define _L	 LOG_SOUND | LOG_INFO

/* I've read from ti99sim that this is really logarithmic */
static u8 attenuation_to_volume[16] =
{
	0xff, 0xee, 0xdd, 0xcc,
	0xbb, 0xaa, 0x99, 0x88,
	0x77, 0x66, 0x55, 0x44,
	0x33, 0x22, 0x11, 0x00
};

voiceinfo sound_voices[4];

/*	
	The current voice (selected by writing OPERATION_FREQUENCY_LO byte).  
	May be useful for arpeggiation. 
*/
static u8   cvoice;

int         sndplayhz = 44100;	/* playback rate */
int         sndplaybits = 8;	/* word size for sound */

/*************************************/

static u16  noise_period[4] = 
{
	16,
	32, 
	64, 
	0 						/* determined by VOICE_TONE_2 */
};


/*
	Update cached fields
*/
static void 
voice_cache_values(voiceinfo *v)
{
	if (v != &sound_voices[VOICE_NOISE]) {
		v->volume = OPERATION_TO_VOLUME(v);
		v->period = OPERATION_TO_PERIOD(v);
		v->hertz = PERIOD_TO_HERTZ(v->period);
	} else {
		int period = OPERATION_TO_NOISE_PERIOD(v);
		int type = OPERATION_TO_NOISE_TYPE(v);

		if (period != NOISE_PERIOD_VARIABLE) {
			v->volume = OPERATION_TO_VOLUME(v);
			v->period = noise_period[period];
			v->hertz = PERIOD_TO_HERTZ(v->period);
		} else {
			v->volume = OPERATION_TO_VOLUME(v);
			v->period = sound_voices[VOICE_TONE_2].period;
			v->hertz = sound_voices[VOICE_TONE_2].hertz;
		}
	}

	logger(_L|L_3, _("voice_cache_values: lo=>%02x, hi=>%02x, period=>%04x, hertz=%d, volume=%d\n"),
		   v->operation[OPERATION_FREQUENCY_LO], 
		   v->operation[OPERATION_FREQUENCY_HI],
		   v->period,
		   v->hertz,
		   v->volume);
}

/*
 *	Update the noise channel when a noise operation occurs
 *	or VOICE_TONE_2 changes and the noise control is NOISE_PERIOD_VARIABLE.
 */
static void
sound_update_noise(void)
{
	voiceinfo *v = &sound_voices[VOICE_NOISE];
	int period = OPERATION_TO_NOISE_PERIOD(v);

	if ((cvoice == VOICE_TONE_2 && period == NOISE_PERIOD_VARIABLE)
		 || cvoice == VOICE_NOISE)
	{
		SOUNDUPDATE(vms_Tn);
	}
}

void
sound_mmio_write(u8 val)
{
	voiceinfo *v;
	int vn;

	demo_record_event(demo_type_sound, val);

	/*  handle command byte */
	if (val & 0x80) {
		vn = OPERATION_TO_VOICE(val);
		cvoice = vn;
		v = &sound_voices[vn];
		switch ((val & 0x70) >> 4) 
		{
		case 0:				/* T1 FRQ */
		case 2:				/* T2 FRQ */
		case 4:				/* T3 FRQ */
			v->operation[OPERATION_FREQUENCY_LO] = val;
		   	voice_cache_values(v);
			sound_update_noise();
			SOUNDUPDATE(vms_Tv0 << vn);
			break;
		case 1:				/* T1 ATT */
		case 3:				/* T2 ATT */
		case 5:				/* T3 ATT */
			v->operation[OPERATION_ATTENUATION] = val;
			voice_cache_values(v);
			SOUNDUPDATE(vms_Vv0 << vn);
			break;
		case 6:				/* noise ctl */
			v->operation[OPERATION_CONTROL] = val;
			sound_update_noise();
			break;
		case 7:				/* noise vol */
			v->operation[OPERATION_ATTENUATION] = val;
			voice_cache_values(v);
			SOUNDUPDATE(vms_Vn);
			break;
		}
	}
	/*  second frequency byte */
	else if (!(val & 0xc0)) {
		v = &sound_voices[cvoice];
		v->operation[OPERATION_FREQUENCY_HI] = val;
		voice_cache_values(v);
		sound_update_noise();
		SOUNDUPDATE(vms_Tv0 << cvoice);
	}
}

void sound_switch(void)
{
	if (features & FE_PLAYSOUND) {
		DOSOUNDUPDATE(vms_Tv0 | vms_Tv1 | vms_Tv2 | vms_Tn | 
					  vms_Vv0 | vms_Vv1 | vms_Vv2 | vms_Vn);
	} else {
		DOSOUNDFLUSH();
	}
}

void sound_silence(void)
{
	sound_mmio_write(0x9f);
	sound_mmio_write(0xbf);
	sound_mmio_write(0xdf);
	sound_mmio_write(0xff);
}

/********************************************/

/*	Cassette routines  */

static vmModule *cs[2];
static u8   cs_open[2];
static u16  cs_clock;
static u8   cs_motor[2];
static u32  cs_lasttime;		// last clock when data was read (3.0/64 mhz)
static u8   cs_latch,			// last data read
            cs_min, cs_max;		// midrange of incoming data

static void
cassette_open(int csx)
{
	vmModule   *ptr;

	if (csx < 0 || csx >= 2)
		return;

	if (cs_open[csx])
		return;

	/*  Select where to filter cassette info;
	   for now, just use sound module */

	for (ptr = vmSound; ptr; ptr = ptr->next) {
		// cs2 [csx=1] can only write, methinks (try "OLD CS2")
		if (ptr->m.sound->play && (csx != 1 || ptr->m.sound->read)
			&& !(ptr->runtimeflags & vmRTUnselected)) {
			break;
		}
	}
	if (ptr)
		logger(_L | 0, _("Cassette %d is %p (%s)\n\n"), csx, ptr, ptr->name);
	cs[csx] = ptr;
	cs_open[csx] = (ptr != NULL);
}

void
cassette_set_timer(u16 hz)
{
	cs_clock = hz;
}

void
cassette_set_motor(int csx, u8 onoff)
{
	if (csx < 0 || csx >= 2)
		return;

	cs_motor[csx] = onoff;
	cs_lasttime = 0;

	cs_min = 0xff;
	cs_max = 0;
	cs_latch = 0x80;
	if (onoff)
		cassette_open(csx);
}

void
cassette_write(u8 onoff, u32 timepassed)
{
	// we should not write to the sound module because
	// the mix server is usually owning it.

/*
	u8 *data = (u8 *)alloca(4096);
	memset(data, onoff ? 0x7f : 0x00, 4096);
	while (timepassed) {
		u32 quant = (timepassed >= 4096 ? 4096 : timepassed);
		#warning can we write to both at once?
		if (cs_motor[0])
			cs[0]->m.sound->play(vms_CS, data, quant, 
				cs_clock ? cs_clock : baseclockhz / 64);
		if (cs_motor[1])
			cs[1]->m.sound->play(vms_CS, data, quant, 
				cs_clock ? cs_clock : baseclockhz / 64);
		timepassed -= quant;
	}
*/
}

u8
cassette_read(void)
{
	u32         timepassed;
	s8         *data;
	u32         average;

	if (currenttime >= cs_lasttime)
		timepassed = currenttime - cs_lasttime;
	else
		timepassed = 64;

	if (cs_clock)
		timepassed = (timepassed + cs_clock - 1) *
			(cs_clock) / (baseclockhz / 64);
	else
		timepassed = timepassed / 64;

	data = (s8 *) alloca(timepassed + 1);
	average = 0;

	if (timepassed && cs_open[0]) {
		cs_lasttime = currenttime;

		//  read at most as much data as represented by cs_clock
		if (cs[0]->m.sound->read)
			cs[0]->m.sound->read(vms_AGr, data, timepassed,
								 cs_clock ? cs_clock : baseclockhz / 64);

		while (timepassed--)
			average = (average + data[timepassed]) / 2;

		cs_latch = average;

		if (cs_latch < cs_min)
			cs_min = cs_latch;
		else if (cs_min < 0x60 / 2)
			cs_min++;
		if (cs_latch > cs_max)
			cs_max = cs_latch;
		else if (cs_max > 0xa0 / 2)
			cs_max--;

	}							// else, use last cs_latch



	logger(_L | L_2, "cs = %d/%d/%d = \n", cs_min, cs_latch, cs_max);
	if (cs_latch > cs_min + (cs_max - cs_min) / 2) {
		logger(_L | L_2, "1\n");
		return 1;
	} else {
		logger(_L | L_2, "0\n\n");
		return 0;
	}
}


/********************************************/

static
DECL_SYMBOL_ACTION(sound_playsound_toggle)
{
	sound_switch();
	return 1;
}

static
DECL_SYMBOL_ACTION(sound_enablesound_toggle)
{
	if (features & FE_SOUND) {
		sound_enable();
		sound_restart();
	} else {
		sound_restop();
		sound_disable();
	}
	return 1;
}

static
DECL_SYMBOL_ACTION(sound_module_restart)
{
	if (sndplaybits <= 8)
		sndplaybits = 8;
	else
		sndplaybits = 16;
	sound_restop();
	sound_restart();
	return 1;
}

static
DECL_SYMBOL_ACTION(sound_config_state)
{
	struct voiceinfo *v;
	int val;

	if (task == csa_READ) {
		if (iter >= 4)
			return 0;
		v = &sound_voices[iter];
		command_arg_set_num(SYM_ARG_1st, iter);
		command_arg_set_num(SYM_ARG_2nd, v->operation[OPERATION_FREQUENCY_LO]);
		command_arg_set_num(SYM_ARG_3rd, v->operation[OPERATION_FREQUENCY_HI]);
		command_arg_set_num(SYM_ARG_4th, v->operation[OPERATION_ATTENUATION]);
		return 1;
	}

	command_arg_get_num(SYM_ARG_1st, &val);
	if (val < 0 || val >= 4) {
		command_logger(_L | LOG_USER | LOG_ERROR, _("Invalid sound channel specified (%d)\n"), val);
		return 0;
	}

	iter = val;
	v = &sound_voices[iter];

	command_arg_get_num(SYM_ARG_2nd, &val);	
	v->operation[OPERATION_FREQUENCY_LO] = val;

	command_arg_get_num(SYM_ARG_3rd, &val);
	v->operation[OPERATION_FREQUENCY_HI] = val;

	command_arg_get_num(SYM_ARG_4th, &val);
	v->operation[OPERATION_ATTENUATION] = val;

	voice_cache_values(v);
	sound_update_noise();
	SOUNDUPDATE((vms_Tv0|vms_Vv0) << iter);

	return 1;
}


void
sound_init(void)
{
	command_symbol_table *soundcommands =
		command_symbol_table_new(_("Sound Options"),
								 _("These are generic commands for controlling sound emulation"),

	command_symbol_new("PlaySound",
					   _("Control whether music/noise sound is played.\n"
						 "(Note: to turn off all sound, "
						 "disable the sound module or use \"EnableSound off\".)"),
							c_STATIC,
							sound_playsound_toggle,
							RET_FIRST_ARG,
							command_arg_new_toggle
							("off|on",
							 _("toggle sound on or off"),
							 NULL /* action */ ,
							 ARG_NUM(features),
							 FE_PLAYSOUND,
							 NULL /* next */ )
							,

	command_symbol_new("EnableSound",
					   _("Control whether any sound is emitted."),
							c_STATIC,
							sound_enablesound_toggle,
							RET_FIRST_ARG,
							command_arg_new_toggle
							("off|on",
							 _("toggle sound on or off"),
							 NULL /* action */ ,
							 ARG_NUM(features),
							 FE_SOUND,
							 NULL /* next */ )
							,

	command_symbol_new("DigitalSoundHertz",
					   _("Set playback rate for digitized sound; "
					   "interpretation is dependent on sound module in effect"),
					   c_STATIC,
					   sound_module_restart,
					   RET_FIRST_ARG,
					   command_arg_new_num(_("Hz"),
										   _("set rate for playback"),
										   NULL  /* action */,
										   ARG_NUM(sndplayhz),
										   NULL
										   /* next */),
    command_symbol_new("DigitalSoundBits",
					   _("Set word size for digitized sound; "
					   "interpretation is dependent on sound module in effect"),
					   c_STATIC,
					   sound_module_restart,
					   RET_FIRST_ARG,
					   command_arg_new_num("8|16",
										   _("set word size for playback"),
										   NULL /* action */ ,
										   ARG_NUM(sndplaybits),
										   NULL /* next */ )
					   ,

    command_symbol_new("SoundChannelState",
					   NULL /* help */,
					   c_DYNAMIC|c_SESSION_ONLY,
					   sound_config_state,
					   NULL /* ret */,
					   command_arg_new_num(_("channel"),
										   _("channel number, 0-3"),
										   NULL /* action */,
										   NEW_ARG_NUM(u8),
					   command_arg_new_num(_("period"),
										   _("period of channel"),
										   NULL /* action */,
										   NEW_ARG_NUM(u16),
					   command_arg_new_num(_("volume"),
										   _("volume 0-15, 15=off"),
										   NULL /* action */,
										   NEW_ARG_NUM(u8),
					   command_arg_new_num(_("type"),
										   _("type of noise, or 0 for tone"),
										   NULL /* action */,
										   NEW_ARG_NUM(u8),
					   NULL)))),

	NULL /* next */ ))))),

    NULL /* sub */ ,

	NULL	/* next */
	);
	int v;

	command_symbol_table_add_subtable(universe, soundcommands);

	features |= FE_PLAYSOUND | FE_PLAYSPEECH;
	for (v=0; v<4; v++) {
		sound_voices[v].operation[OPERATION_ATTENUATION] = 0xf;
		voice_cache_values(&sound_voices[v]);
	}
	cvoice = 0;
}

int
sound_restart(void)
{
	if (features & FE_PLAYSOUND) {
		DOSOUNDUPDATE(vms_Tv0 | vms_Tv1 | vms_Tv2 | vms_Tn | vms_Vv0 | vms_Vv1 |
					  vms_Vv2 | vms_Vn);
	}
	if (features & FE_PLAYSPEECH) {
		DOSOUNDPLAY(vms_Speech, NULL, 0, 0);
	}
	return 1;
}

void
sound_restop(void)
{
	SOUNDFLUSH();
}

int
sound_enable(void)
{
	vmModule *ptr = vmSound;
	while (ptr) {
		if (ptr->runtimeflags & vmRTInUse)
		{
			if (vmEnableModule(ptr) != vmOk)
				return 0;
		}
		ptr = ptr->next;
	}

	return 1;
}

void
sound_disable(void)
{
	vmModule *ptr = vmSound;
	while (ptr) {
		if (ptr->runtimeflags & vmRTInUse)
		{
			vmDisableModule(ptr);
		}
		ptr = ptr->next;
	}
}
