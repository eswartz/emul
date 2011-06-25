/*
  sound_alsa.c					-- V9t9 module for ALSA sound driver

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

/*	Sound module for ALSA project.  */

#include <unistd.h>
#include <fcntl.h>

#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <limits.h>
#include <signal.h>

#include <sys/ioctl.h>
#include <sys/signal.h>
#include <sys/time.h>

#include <alsa/asoundlib.h>

#include "v9t9_common.h"
#include "timer.h"
#include "sound.h"
#include "command.h"
#include "mix_server.h"
#include "9901.h"

//#define WRITE_TO_DISK

extern vmModule alsaSound;
static      vmResult
alsa_disable(void);
static      vmResult
alsa_enable(void);

#define _L	LOG_SOUND|LOG_INFO

/****************************/
static int  alsa_sndf;
static int  alsa_paused;
static int  alsa_ticks;

static snd_pcm_t *pcm_handle;
static char pcm_name[64];

/*	defaults */
static int  pcm_hz = 44100;
static int  pcm_format = SND_PCM_FORMAT_S16_LE;
static int	pcm_framesize = 2;
static int	pcm_periods = 2;
static int	pcm_periodsize = 512;
static snd_pcm_uframes_t  pcm_samplesize;

static s16  *samplebuf;

#include "sound_pthread_mixer.h"

static void 
alsa_record_fragment(void *, int);

static void 
sound_module_mix(void *buffer, int bytes)
{
	if (snd_pcm_writei(pcm_handle, buffer, bytes / pcm_framesize) < 0) {
		snd_pcm_prepare(pcm_handle);
	}

#ifdef WRITE_TO_DISK
	alsa_record_fragment(buffer, bytes);
#endif
}

/**************************/

static int
setup_playback(void)
{
	int         err;
	snd_pcm_hw_params_t *pcm_params;

	if ((err = snd_pcm_hw_params_malloc(&pcm_params)) < 0 ||
		(err = snd_pcm_hw_params_any(pcm_handle, pcm_params)) < 0 ||
		(err = snd_pcm_hw_params_set_access(pcm_handle, pcm_params, SND_PCM_ACCESS_RW_INTERLEAVED)) < 0 ||
		(err = snd_pcm_hw_params_set_format(pcm_handle, pcm_params, pcm_format)) < 0 ||
		(err = snd_pcm_hw_params_set_rate_near(pcm_handle, pcm_params, &pcm_hz, 0)) < 0 ||
		(err = snd_pcm_hw_params_set_channels(pcm_handle, pcm_params, 1)) < 0)
	{
		module_logger(&alsaSound, _L|LOG_USER | LOG_ERROR, _("params setup error: %s\n"),
			 snd_strerror(err));
		return 0;
	}

#if 0
	// ???
	pcm_samplesize = pcm_periodsize * pcm_periods / pcm_framesize;

	if ((err = snd_pcm_hw_params_set_periods(pcm_handle, pcm_params, pcm_periods, 0)) < 0 ||
		(err = snd_pcm_hw_params_set_buffer_size_near(pcm_handle, pcm_params, &pcm_samplesize)) < 0) {
		module_logger(&alsaSound, _L|LOG_USER | LOG_ERROR, _("cannot set up period size: %s\n"),
			 snd_strerror(err));
		// continue
	}

	samplebuf = (s8 *) malloc(pcm_samplesize * pcm_framesize);
#else

	pcm_samplesize = pcm_periodsize;
	samplebuf = (s16 *) malloc(pcm_samplesize * sizeof(samplebuf[0]));
#endif

	if ((err = snd_pcm_hw_params(pcm_handle, pcm_params)) < 0) {
		module_logger(&alsaSound, _L|LOG_USER | LOG_ERROR, _("params setting error: %s\n"),
			 snd_strerror(err));
		return 0;
	}

	snd_pcm_hw_params_free(pcm_params);

	if ((err = snd_pcm_prepare(pcm_handle)) < 0) {
		module_logger(&alsaSound, _L|LOG_USER | LOG_ERROR, _("error preparing PCM handle\n"),
			 snd_strerror(err));
		return 0;
	}


	return 1;
}

#if 0
static int
setup_recording(void)
{
	int         err;


	if ((err = snd_pcm_capture_info(pcm_handle, &rec_info)) < 0) {
		module_logger(&alsaSound, _L|LOG_USER | LOG_ERROR, _("record info error: %s\n"),
			 snd_strerror(err));
		return 0;
	}

	/*  Verify that our format is legal. */

	if (rec_info.min_rate > rec_format.rate ||
		rec_info.max_rate < rec_format.rate) {
		module_logger(&alsaSound, _L|LOG_USER, _("device supports recording range %d to %d Hz"
			 "clipping default rate.\n"), rec_info.min_rate, rec_info.max_rate);
		if (rec_info.min_rate > rec_format.rate)
			rec_format.rate = rec_info.min_rate;
		if (rec_format.rate > rec_info.max_rate)
			rec_format.rate = rec_info.max_rate;
	}

	memset((void *) &rec_format, 0, sizeof(rec_format));
	rec_format.rate = pcm_hz;
	rec_format.channels = 1;
	if (!(rec_info.formats & (1 << (rec_format.format = SND_PCM_SFMT_S8))) &&
		!(rec_info.formats & (1 << (rec_format.format = SND_PCM_SFMT_U8)))) {
		module_logger(&alsaSound, _L|LOG_ERROR | LOG_USER,
			 _("device does not support desired eight-bit recording format\n"));
		//!!! until mix_server.c is more flexible
		return 0;
	}

	if ((err = snd_pcm_capture_format(pcm_handle, &rec_format)) < 0) {
		module_logger(&alsaSound, _L|LOG_ERROR | LOG_USER, _("device cannot set recording format\n"),
			 snd_strerror(err));
		return 0;
	}


	/*  Optimize fragments  */

	pcm_samplesize = 512;
	samplebuf = (s16 *) malloc(pcm_samplesize * sizeof(samplebuf[0]));
//  log(_("ALSA sample size = %d"), pcm_samplesize);

	memset((void *) &rec_params, 0, sizeof(rec_params));
	rec_params.fragment_size = pcm_samplesize * sizeof(s16);
	if ((err = snd_pcm_capture_params(pcm_handle, &rec_params)) < 0) {
		module_logger(&alsaSound, _L|LOG_ERROR | LOG_USER,
			 _("device cannot set requested PCM recording parameters:\n"
			 "%s\n"), snd_strerror(err));
		return 0;
	}

	return 1;
}
#endif

/****************************/

/*	Update voice parameters  */
static void
alsa_update(vmsUpdateMask updated)
{
	pthread_mixer_update(updated);
}

static void
alsa_flush(void)
{
	pthread_mixer_flush();
}

/* schedule digital data for playing */
static void
alsa_play(vmsPlayMask kind, s16 * data, int len, int hz)
{
	pthread_mixer_play(kind, data, len, hz);
}

/* read digital data (for cassette) */
static void
alsa_read(vmsReadMask kind, u16 * data, int len, int hz)
{
	int         x;

//	snd_pcm_readi(pcm_handle, data, len / pcm_framesize);
//	if (rec_format.format == SND_PCM_SFMT_S8) 
	{
		for (x = 0; x < len; x++)
			data[x] ^= 0x8000;
	}
//  for (x=0; x<len; x++)
//      log("%d ", data[x]);
//  log("");
}

static int
alsa_playing(vmsPlayMask kind)
{
	return pthread_mixer_playing(kind);
}



/**************************************/

static      vmResult
alsa_detect(void)
{
	return vmOk;
/*
	int         err;

	if ((err = snd_pcm_open(&pcm_handle, pcm_name, SND_PCM_STREAM_PLAYBACK, 0)) == 0) {
		snd_pcm_close(pcm_handle);
		pcm_handle = 0;
		module_logger(&alsaSound, _L|LOG_USER, _("Detected Advanced Linux Sound Architecture\n"));
		return vmOk;
	}
	else {
		module_logger(&alsaSound, _L|LOG_USER, _("No 'default' PCM devices found\n"));
		return vmNotAvailable;
	}
*/
}


/*	Interface to command system */

static void
do_alsa_stop_recording(void)
{
	if (alsa_sndf) {
		module_logger(&alsaSound, _L|LOG_USER,
			 _("StopSoundRecording:  Closing previously recording soundtrack\n"));
		close(alsa_sndf);
		alsa_sndf = 0;
	}
}

static
DECL_SYMBOL_ACTION(alsa_stop_recording)
{
	do_alsa_stop_recording();
	return 1;
}

static
DECL_SYMBOL_ACTION(alsa_record_sound)
{
	do_alsa_stop_recording();
	alsa_sndf =
		open(sym->args->u.string.m.mem, O_WRONLY | O_APPEND | O_CREAT, 0666);
	if (alsa_sndf < 0) {
		module_logger(&alsaSound, LOG_ERROR,
					_("RecordSoundToFile:  could not open '%s' for appending"),
					sym->args->u.string.m.mem);
		return 0;
	}
	return 1;
}

static
DECL_SYMBOL_ACTION(alsa_pause_sound)
{
	module_logger(&alsaSound, LOG_INFO, _("PausingSoundRecording:  %s\n"),
		 alsa_paused ? _("paused") : _("resuming"));
	return 1;
}

static void
alsa_record_fragment(void *buffer, int bytes)
{
	if (alsa_sndf && !alsa_paused) {
		write(alsa_sndf, buffer, bytes);
	}
}

static
DECL_ARG_ACTION(alsa_add_file_extension)
{
	char       *nm;

	if (!command_arg_get_string(arg, &nm))
		return 0;
	if (strchr(OS_GetFileNamePtr(nm), '.') == NULL) {
/*
		sprintf(nm + strlen(nm), ".%d.%s",
				play_format.rate,
				play_format.format == SND_PCM_FORMAT_S8 ? "s8" :
				play_format.format == SND_PCM_FORMAT_U8 ? "u8" :
				play_format.format == SND_PCM_FORMAT_S16_LE ? "s16.le" :
				play_format.format == SND_PCM_FORMAT_S16_BE ? "s16.be" :
				play_format.format == SND_PCM_FORMAT_U16_LE ? "u16.le" :
				"u16.be");*/
		sprintf(nm + strlen(nm), "s16.le");
		module_logger(&alsaSound, _L|LOG_USER, _("RecordSoundToFile:  using '%s'\n"), nm);
	}
	return 1;
}

static
DECL_SYMBOL_ACTION(alsa_change_device)
{
/*
	if (pcm_handle) {
		alsa_disable();
		alsa_enable();
	}
*/
	return 1;
}

static      vmResult
alsa_init(void)
{
	command_symbol_table *alsacommands =
		command_symbol_table_new(_("ALSA Sound Mixer Options"),
								 _("These commands control the ALSA 99/4A sound module"),

    	 command_symbol_new("ALSADevice",
    						_("Specify ALSA PCM drivere"),
    						c_STATIC,
    						alsa_change_device,
    						NULL /* ret */ ,
    						command_arg_new_string
    						(_("device"),
    						 _("device to use for PCM sound; e.g. 'plughw:0,0' "),
    						 NULL /* action */ ,
    						 ARG_STR(pcm_name),
    						 NULL /* next */ )
    						,
    	 command_symbol_new("RecordSoundToFile",
    						_("Record soundtrack to a file"),
    						c_DONT_SAVE,
    						alsa_record_sound,
    						NULL /* ret */ ,
    						command_arg_new_string
    						(_("file"),
    						 _("file to receive sound; "
    						 "if file exists, new data will be appended to it; "
    						 "if no extension is given, one will be added that details "
    						 "the RAW sound format used"),
    						 alsa_add_file_extension
    						 /* action */ ,
    						 NEW_ARG_STR(OS_PATHSIZE),
							NULL /* next */ )
    						,
    						command_symbol_new
    						("PausingSoundRecording",
    						 _("Pause or resume soundtrack recording (does not close sound file)"),
    						 c_DONT_SAVE,
    						 alsa_pause_sound
    						 /* action */ ,
    						 RET_FIRST_ARG,
    						 command_arg_new_num
    						 (_("state"),
    						  _("whether recording is paused"),
    						  NULL /* action */ ,
    						  ARG_NUM(alsa_paused),
    						  NULL /* next */ )
    						 ,
    						 command_symbol_new
    						 ("StopSoundRecording",
    						  _("Stop soundtrack recording and close sound file"),
    						  c_DONT_SAVE,
    						  alsa_stop_recording,
    						  NULL /* ret */ ,
    						  NULL	/* args */
    						  ,

    						  NULL /* next */ )))),

    	 NULL /* sub */ ,

    	 NULL	/* next */
	);

	command_symbol_table_add_subtable(universe, alsacommands);

	strcpy(pcm_name, "default");
	alsa_sndf = 0;
	alsa_paused = 0;

	return vmOk;
}

static      vmResult
alsa_term(void)
{
	return vmOk;
}

static      vmResult
alsa_enable(void)
{
	int         err;

	if ((err = snd_pcm_open(&pcm_handle, pcm_name,
							SND_PCM_STREAM_PLAYBACK, 0)) < 0) {
		module_logger(&alsaSound, _L|LOG_ERROR | LOG_USER,
					  _("Could not open ALSA PCM device (%s): %s\n"), pcm_name,
					  snd_strerror(err));
		return vmNotAvailable;
	}

	if (!setup_playback())
		return vmInternalError;

	// allow this to fail
//	if (direction == SND_PCM_OPEN_DUPLEX && !setup_recording())
//	direction = SND_PCM_OPEN_PLAYBACK;

	pthread_mixer_init(pcm_hz, pcm_samplesize,
					   (pcm_format == SND_PCM_FORMAT_S8 ||
						pcm_format == SND_PCM_FORMAT_S16_BE ||
						pcm_format == SND_PCM_FORMAT_S16_LE),
					   (pcm_format == SND_PCM_FORMAT_S8 ||
						pcm_format == SND_PCM_FORMAT_U8),
					   (pcm_format == SND_PCM_FORMAT_S16_BE ||
						pcm_format == SND_PCM_FORMAT_U16_BE));

	return pthread_mixer_enable();
}

static      vmResult
alsa_disable(void)
{
	vmResult    res;

#ifdef WRITE_TO_DISK
	if (alsa_sndf)
		close(alsa_sndf);
#endif
	if ((res = pthread_mixer_disable()) != vmOk)
		return res;
	pthread_mixer_term();
	snd_pcm_close(pcm_handle);
	return vmOk;
}

static      vmResult
alsa_restart(void)
{
#warning set volume?
	return pthread_mixer_restart();
}

static      vmResult
alsa_restop(void)
{
#warning set volume?
	return pthread_mixer_restop();
}

static vmSoundModule alsaSoundModule = {
	4,
	alsa_update,
	alsa_flush,
	alsa_play,
	alsa_read,
	alsa_playing
};

vmModule    alsaSound = {
	3,
	"Advanced Linux Sound Architecture",
	"sndALSA",

	vmTypeSound,
	0,

	alsa_detect,
	alsa_init,
	alsa_term,
	alsa_enable,
	alsa_disable,
	alsa_restart,
	alsa_restop,

	{(vmGenericModule *) & alsaSoundModule}
};
