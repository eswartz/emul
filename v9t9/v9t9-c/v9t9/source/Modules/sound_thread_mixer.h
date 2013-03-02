/*
  sound_thead_mixer.h			-- V9t9 module backend for threaded sound mixer

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

/*	This is common code for a sound module using the mix_server.c routine.  */

#include "OSLib.h"	
#include "mix_server.h"

/*	Main module must define this */
static void sound_module_mix(void *buffer, int bytes);

static OSThread			mixthread;
static bool				mixquitting;
static mix_context		context;

//#define TRACING 1

static void	*
MyMixerThread(void *unused)
{
	while (!mixquitting)
	{
		if ((features & FE_SOUND) && context.buffer)
		{
			mix_mixit(&context, context.bufsize);
			//mix_advance(&context, context.bufsize);
			
			sound_module_mix(context.buffer, context.bufsize * (context.eightbit ? 1 : 2));
		}
	}

	return 0;
}


/*	Update voice parameters  */
static 	void	
pthread_mixer_update(vmsUpdateMask updated)
{
	int v;
	voiceinfo *vinfo;

	if (!(features & FE_PLAYSOUND)) {
		return;
	}

	for (v = VOICE_TONE_0; v <= VOICE_TONE_2; v++)
	{
		vinfo = &sound_voices[v];
		if (updated & ((vms_Tv0 << v) | (vms_Vv0 << v)))
		{
			mix_handle_voice(&context, 
							 v + mix_CHN0, 
							 vinfo->hertz,
							 OPERATION_TO_VOLUME(vinfo));
		}
	}

	if (updated & (vms_Tn | vms_Vn))
	{
		vinfo = &sound_voices[VOICE_NOISE];
		mix_handle_noise(&context, 
						 mix_Noise, 
						 vinfo->hertz,
						 OPERATION_TO_VOLUME(vinfo),
						 OPERATION_TO_NOISE_TYPE(vinfo) == NOISE_WHITE);
	}

}

/*	Flush voices */
static 	void	
pthread_mixer_flush(void)
{
	int v;

	for (v = VOICE_TONE_0; v <= VOICE_TONE_2; v++) {
		mix_handle_voice(&context,
						 v + mix_CHN0,
						 0 /*hertz*/,
						 0 /*volume*/);
	}

	mix_handle_noise(&context,
					 mix_Noise,
					 0 /*hertz*/,
					 0 /*volume*/,
					 0 /*iswhite*/);
}

 	/* schedule digital data for playing (immediately if possible,
  		else after all other sound of this type);
  		kind is *one* element of vmsPlayMask;
  		data/size/hz describe the sample;
  		if data==NULL, the channel is flushed */
static 	void	
pthread_mixer_play(vmsPlayMask kind, 
				   s16 *data, int len, int hz)
{
	switch (kind)
	{
		case vms_Speech:
		{
			mix_handle_data(&context, mix_Speech, hz, 255, len, data);
			break;
		}
		case vms_AGw:
		{
			mix_handle_data(&context, mix_AudioGate, hz,
				audiogate.latch ? audiogate.play ? 0x7f : 0x1f : 0, len, data);
			break;
		}
	}
}


static int
pthread_mixer_playing(vmsPlayMask kind)
{
	switch (kind)
	{
	case vms_Speech:
		return mix_data_pending(&context, mix_Speech);
	case vms_AGw:
		return mix_data_pending(&context, mix_AudioGate);
	}
	return 0;
}



static vmResult	
pthread_mixer_enable(void)
{
	OSError err;

	mixquitting = false;

	if ((err = OS_CreateThread(&mixthread, MyMixerThread, NULL)) != OS_NOERR)
	{
		logger(LOG_USER|LOG_ERROR, "Could not create mixer (%s)\n", 
			   OS_GetErrText(err));
		return vmInternalError;
	}

	return vmOk;
}

static vmResult	
pthread_mixer_disable(void)
{
	void *ret;

	mixquitting = true;
	OS_ResumeThread(mixthread);
	OS_JoinThread(mixthread, &ret);

	return vmOk;
}

static vmResult	
pthread_mixer_restart(void)
{
	#warning set volume 
	
	mix_restart(&context);
	
	OS_ResumeThread(mixthread);

	return vmOk;
}

static vmResult	
pthread_mixer_restop(void)
{
	#warning set volume to zero
	
	OS_SuspendThread(mixthread);

	return vmOk;
}

static void 
pthread_mixer_init(int soundhz, int bufsize, 
				   bool issigned, bool eightbit, bool bigendian)
{
//	printf("thread_mixer_init: issigned=%d, eightbit=%d, bigendian=%d\n",
//		issigned, eightbit, bigendian);
	mix_init(&context, soundhz, bufsize, issigned, eightbit, bigendian);
}

static void 
pthread_mixer_term(void)
{
	OS_KillThread(mixthread, true);	
	mix_term(&context);
}

