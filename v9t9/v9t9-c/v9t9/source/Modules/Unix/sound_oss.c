/*
  sound_oss.c					-- V9t9 module for Open Sound System driver

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
#include <fcntl.h>

#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <limits.h>
#include <signal.h>

#include <sys/ioctl.h>
#include <sys/signal.h>
#include <sys/soundcard.h>
#include <sys/time.h>

#include "v9t9_common.h"
#include "timer.h"
#include "sound.h"
#include "9901.h"

#include "mix_server.h"

#define _L	LOG_SOUND|LOG_INFO

extern vmModule ossSound;

#define DEVICE "/dev/dsp"

static int  sound_fd;
static int  sound_dir = O_RDWR;
static int  soundhz = 44100;
static int  soundformat = AFMT_S16_LE;
static int 	soundformatlist[] = 
{
	AFMT_U8,
	AFMT_S8,
	AFMT_U16_LE,
	AFMT_S16_LE,
	AFMT_U16_BE,
	AFMT_S16_BE,
	0
};
static int  soundblksize;

//static int    soundformat;

#define	SAMPLESIZE 512
//static    s8  samplebuf[SAMPLESIZE];

#include "sound_thread_mixer.h"

static void 
sound_module_mix(void *buffer, int bytes)
{
	//printf("Writing %p / %d to %d\n", buffer, bytes, sound_fd);
	(void) write(sound_fd, buffer, bytes);
}


/****************************/

#define TRY(x,y)  if (ioctl(sound_fd, x, y) < 0) \
					{ module_logger(&ossSound, _L|LOG_ERROR,_("ioctl failed (" #x "," #y "): %s\n"), \
					strerror(errno)); close(sound_fd); return 0; }

// not fatal -- might not be supported
#define TRY0(x,y)  if (ioctl(sound_fd, x, y) < 0) \
					{ module_logger(&ossSound, _L|LOG_WARN,_("ioctl failed (" #x "," #y "): %s\n"), \
					strerror(errno)); }

static int
setup_sound(void)
{
	int         soundstereo = 0;
	int         soundformats;
	int         soundfragment = 11 | (2 << 16);
	int			i;

	TRY0(SNDCTL_DSP_SETFRAGMENT, &soundfragment);
	TRY(SNDCTL_DSP_RESET, 0);
	soundblksize = 512;
	TRY0(SNDCTL_DSP_GETBLKSIZE, &soundblksize);
	TRY(SNDCTL_DSP_SPEED, &soundhz);
	TRY(SNDCTL_DSP_STEREO, &soundstereo);

	TRY(SNDCTL_DSP_GETFMTS, &soundformats);
	if (!(soundformats & soundformat)) {
		for (i = 0; soundformatlist[i]; i++) {
			if (soundformats & (soundformat = soundformatlist[i])) break;
		}
		if (!soundformat) {
			module_logger(&ossSound, _L|LOG_USER,
						  _("desired sound quality not supported by hardware (%08X/%08X)\n"), soundformats, soundformat);
			return 0;
		}
	}

	TRY(SNDCTL_DSP_SETFMT, &soundformat);

	return 1;
}


/**************************************/

static void
oss_update(vmsUpdateMask updated)
{
	pthread_mixer_update(updated);
}

static void
oss_flush(void)
{
	pthread_mixer_flush();
}

static void
oss_play(vmsPlayMask kind, s16 * data, int len, int hz)
{
	pthread_mixer_play(kind, data, len, hz);
}

/* read digital data (for cassette) */
static void
oss_read(vmsReadMask kind, u16 * data, int len, int hz)
{
	int         x;

	(void) read(sound_fd, data, len * sizeof(data[0]));
	if (soundformat == AFMT_S16_LE) {
		for (x = 0; x < len; x++)
			data[x] ^= 0x8000;
	}
//  for (x=0; x<len; x++)
//      log("%d ", data[x]);
//  log("");
}

static int
oss_playing(vmsPlayMask kind)
{
	return pthread_mixer_playing(kind);
}

/**************************************/

static      vmResult
oss_detect(void)
{
	sound_fd = open(DEVICE, O_RDWR);
	if (sound_fd > 0) {
		module_logger(&ossSound, _L|LOG_USER, _("Detected Open Sound System...\n"));
		close(sound_fd);
		return vmOk;
	} else {
		module_logger(&ossSound, _L|LOG_ERROR | LOG_USER, _("Could not open /dev/dsp:  %s\n"),
			 strerror(errno));
		return vmNotAvailable;
	}
}

static      vmResult
oss_init(void)
{

	return vmOk;
}

static      vmResult
oss_term(void)
{
	return vmOk;
}

static      vmResult
oss_enable(void)
{
	if ((sound_fd = open(DEVICE, sound_dir = O_RDWR)) < 0 &&
		(sound_fd = open(DEVICE, sound_dir = O_RDONLY)) < 0) {
		module_logger(&ossSound, _L|LOG_USER | LOG_ERROR, _("cannot open sound (%s)\n"),
			 strerror(errno));
		return vmNotAvailable;
	}
	if (!setup_sound())
		return vmInternalError;

	pthread_mixer_init(soundhz, SAMPLESIZE,
					   (soundformat == AFMT_S8 ||
						soundformat == AFMT_S16_BE ||
						soundformat == AFMT_S16_LE),
					   (soundformat == AFMT_S8 ||
						soundformat == AFMT_U8),
					   (soundformat == AFMT_S16_BE ||
						soundformat == AFMT_U16_BE));

	return pthread_mixer_enable();
}

static      vmResult
oss_disable(void)
{
	vmResult    res;

	if ((res = pthread_mixer_disable()) != vmOk) {
		module_logger(&ossSound, _L|0, _("disable: %d\n"), res);
		return res;
	}
	pthread_mixer_term();
	close(sound_fd);
	return vmOk;
}

static      vmResult
oss_restart(void)
{
#warning set volume
	return pthread_mixer_restart();
}

static      vmResult
oss_restop(void)
{
#warning reset volume
	return pthread_mixer_restop();
}


static vmSoundModule ossSoundModule = {
	4,
	oss_update,
	oss_flush,
	oss_play,
	oss_read,
	oss_playing
};

vmModule    ossSound = {
	3,
	"Open Sound System",
	"sndOSS",

	vmTypeSound,
	0,

	oss_detect,
	oss_init,
	oss_term,
	oss_enable,
	oss_disable,
	oss_restart,
	oss_restop,

	{(vmGenericModule *) & ossSoundModule}
};
