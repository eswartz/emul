/*
  sound_pthead_mixer.h			-- V9t9 module backend for threaded sound mixer

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

/*	This is common code for a sound module using the pthreads
	package and the mix_server.c routine.  */
	
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/wait.h>

#include <pthread.h>
#include <semaphore.h>

#include "mix_server.h"

/*	Main module must define this */
static void sound_module_mix(void *buffer, int bytes);

static pthread_attr_t	mixthattr;
static pthread_t		mixthread;
static pthread_cond_t	c_mixstate = PTHREAD_COND_INITIALIZER;
static pthread_mutex_t	m_mixstate = PTHREAD_MUTEX_INITIALIZER;
static pthread_mutex_t	m_datastate = PTHREAD_MUTEX_INITIALIZER;

static bool				mixgoing, mixquitting;

static mix_context		context;

#define TRACING 0

static void	*
MyMixerThread(void *unused)
{
#if TRACING
	fprintf(stderr, "[entered MyMixerThread]\n");
#endif
	while (!mixquitting)
	{
#if TRACING
		fprintf(stderr, "[about to lock m_mixstate]\n");
#endif
		pthread_mutex_lock(&m_mixstate);
#if TRACING
		fprintf(stderr, "[locked m_mixstate]\n");
#endif

		while (!mixgoing && !mixquitting)
		{
#if TRACING
			fprintf(stderr, "[waiting for c_mixstate]\n");
#endif
			pthread_cond_wait(&c_mixstate, &m_mixstate);
		}

#if TRACING
		fprintf(stderr, "[about to work, mixgoing=%d, mixquitting=%d]\n",mixgoing,mixquitting);
#endif
		
		if (!mixquitting && mixgoing)
		{
			if ((features & FE_SOUND) && context.buffer)
			{
				pthread_mutex_lock(&m_datastate);

				mix_mixit(&context, context.bufsize);
			   	//mix_advance(&context, context.bufsize);
			
				sound_module_mix(context.buffer, context.bufsize * (context.eightbit ? 1 : 2));

				pthread_mutex_unlock(&m_datastate);
			}
			
#if TRACING
			fprintf(stderr, "[mixed]\n");
#endif
		}

		pthread_mutex_unlock(&m_mixstate);
	}
#if TRACING
	fprintf(stderr, "[quitting]\n");
#endif
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
	pthread_mutex_lock(&m_datastate);

	switch (kind)
	{
		case vms_Speech:
		{
			mix_handle_data(&context, mix_Speech, hz, 128, len, data);
			break;
		}
		case vms_AGw:
		{
			mix_handle_data(&context, mix_AudioGate, hz,
				audiogate.latch ? audiogate.play ? 0x7f : 0x1f : 0, len, data);
			break;
		}
	}

	pthread_mutex_unlock(&m_datastate);
}


static int
pthread_mixer_playing(vmsPlayMask kind)
{
	return mix_data_pending(&context, kind);
}



static vmResult	
pthread_mixer_enable(void)
{
	int err;
#if TRACING
	printf("Enabling mixer\n");
#endif

	if ((err = pthread_attr_init(&mixthattr)) ||
		(err = pthread_attr_setdetachstate(&mixthattr, PTHREAD_CREATE_JOINABLE)) ||
		(err = pthread_attr_setschedpolicy(&mixthattr, SCHED_OTHER)))
	{
		logger(_L|LOG_ERROR|LOG_USER, "Could not initialize mixer thread parameters (%s)\n", strerror(errno));
		return vmInternalError;
	}

	mixquitting = false;
	mixgoing = false;

	pthread_mutex_init(&m_mixstate, NULL);
	pthread_cond_init(&c_mixstate, NULL);

//////

	
/*	if (!pthread_create(&mixthread, &mixthattr, MyMixerThread, NULL))
	{
		error("Could not create mixer thread (%s)\n", strerror(errno));
		return vmInternalError;
	}*/

	/* Unfortunately, the above code *always* appears to fail
		with EINTR,  though the thread has been properly created. */
	errno = 0;
	pthread_create(&mixthread, &mixthattr, MyMixerThread, NULL);
	if (errno != 0 && errno != EINTR)
	{
		logger(_L|LOG_ERROR|LOG_USER, "Could not create mixer thread (%s)\n", strerror(errno));
		return vmInternalError;
	}
	
	return vmOk;
}

static vmResult	
pthread_mixer_disable(void)
{
	void *ret;
#if TRACING
	printf("Disabling mixer\n");
#endif
	mixquitting = true;
	mixgoing = false;

	pthread_mutex_lock(&m_mixstate);
	pthread_cond_signal(&c_mixstate);
	pthread_mutex_unlock(&m_mixstate);
	
	pthread_join(mixthread, &ret);
	
	return vmOk;
}

static vmResult	
pthread_mixer_restart(void)
{
	#warning set volume 
#if TRACING
	printf("Restarting mixer\n");
#endif
	
	mix_restart(&context);
	
	mixquitting = false;
	mixgoing = true;
	
	pthread_mutex_lock(&m_mixstate);
	pthread_cond_signal(&c_mixstate);
	pthread_mutex_unlock(&m_mixstate);

	return vmOk;
}

static vmResult	
pthread_mixer_restop(void)
{
#if TRACING
	printf("Restopping mixer\n");
#endif
	mixquitting = false;
	mixgoing = false;

	pthread_mutex_lock(&m_mixstate);
	pthread_cond_signal(&c_mixstate);
	pthread_mutex_unlock(&m_mixstate);
	
	#warning set volume to zero
	
	return vmOk;
}

static void 
pthread_mixer_init(int soundhz, int bufsize, 
				   bool issigned, bool eightbit, bool bigendian)
{
//	printf("pthread_mixer_init: issigned=%d, eightbit=%d, bigendian=%d\n",
//		issigned, eightbit, bigendian);
	mix_init(&context, soundhz, bufsize, issigned, eightbit, bigendian);
}

static void 
pthread_mixer_term(void)
{
	mixgoing = false;
	mix_term(&context);
}

