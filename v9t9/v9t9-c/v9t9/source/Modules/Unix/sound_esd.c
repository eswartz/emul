/*
  sound_esd.c					-- sound driver for Enlightened Sound Daemon

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
 
#include <unistd.h>
#include <fcntl.h>

#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <limits.h>
#include <signal.h>

#include <esd.h>

#include "v9t9_common.h"
#include "timer.h"
#include "sound.h"
#include "9901.h"

#include "mix_server.h"

#define _L	LOG_SOUND|LOG_INFO

static char *hostname = NULL;	/*local*/
static int  server_fd, sound_fd;
static int  soundhz = 44100;

static int  soundblksize;

#include "sound_pthread_mixer.h"

/****************************/

static int
setup_sound(void)
{
	server_fd = esd_open_sound(hostname);
	if (server_fd < 0) {
		module_logger(&esdSound, _L|LOG_ERROR|LOG_USER, _("could not open audio (%s)\n"),
			   strerror(errno));
		soundblksize = 512;
	} else {
		/*
		soundblksize = esd_get_latency(server_fd);
		// 16-bit, mono
		soundblksize *= 2;
*/
		soundblksize = 512;
	}

	sound_fd = esd_play_stream(
					ESD_BITS16|ESD_MONO|ESD_STREAM|ESD_PLAY,
					soundhz,
					hostname,
					"V9t9 sndESD module");
	if (sound_fd < 0) {
		module_logger(&esdSound, _L|LOG_ERROR|LOG_USER, _("could not open stream (%s)\n"),
			   strerror(errno));
		return 0;
	}

	module_logger(&esdSound, _L|LOG_USER, _("\nwarning, this module is not optimal and\n"
		   "sound can be delayed up to a second.\n"
		   "Consider killing esd and using native OSS or ALSA sound.\n\n"));
	return 1;
}


/**************************************/

static void 
sound_module_mix(void *buffer, int bytes)
{
	if (sound_fd > 0) {
		write(sound_fd, buffer, bytes);
	}
}



static void
esd_update(vmsUpdateMask updated)
{
	pthread_mixer_update(updated);
}

static void
esd_flush(void)
{
	pthread_mixer_flush();
}

static void
esd_play(vmsPlayMask kind, s16 * data, int len, int hz)
{
	pthread_mixer_play(kind, data, len, hz);
}

/* read digital data (for cassette) */
static void
esd_read(vmsReadMask kind, u16 * data, int len, int hz)
{
	memset(data, 0, len * sizeof(data[0]));
/*
	int         x;

	read(sound_fd, data, len);
	if (soundformat == AFMT_S8) {
		for (x = 0; x < len; x++)
			data[x] ^= 0x80;
	}
*/
}

static int
esd_playing(vmsPlayMask kind)
{
	return pthread_mixer_playing(kind);
}



/**************************************/

static      vmResult
esd_detect(void)
{
	/* assume it is here if this is compiled in */
	return vmOk;
/*
	server_fd = esd_open_sound(hostname);
	if (server_fd > 0) {
		close(server_fd);
		server_fd = 0;
		module_logger(&esdSound, _L|LOG_USER, _("Detected Enlightened Sound Daemon...\n"));
		return vmOk;
	} else {
		module_logger(&esdSound, _L|LOG_ERROR | LOG_USER, _("Could not open Enlightened Sound Daemon:  %s\n"),
			 strerror(errno));
		return vmNotAvailable;
	}
*/
}

static      vmResult
esd_init(void)
{
#if OSS_SOUND
	/* don't init if OSS is running, since this will spawn
	   a server which will make OSS fail to load */
	if (ossSound.runtimeflags & vmRTUnavailable) 
#endif
    {
		server_fd = esd_open_sound(hostname);
		if (server_fd <= 0) {
			module_logger(&esdSound, _L|LOG_ERROR | LOG_USER, _("Could not open Enlightened Sound Daemon:  %s\n"),
			 strerror(errno));
			return vmNotAvailable;
		}
	}
	return vmOk;
}

static      vmResult
esd_term(void)
{
	if (server_fd > 0) {
		esd_close(server_fd);
		server_fd = 0;
	}
	if (sound_fd > 0) {
		esd_close(sound_fd);
		sound_fd = 0;
	}

/*
	if (server_fd > 0) {
		close(server_fd);
		server_fd = 0;
	}
	if (sound_fd > 0) {
		close(sound_fd);
		sound_fd = 0;
	}
*/
	return vmOk;
}

static      vmResult
esd_enable(void)
{
	if (!setup_sound())
		return vmInternalError;

/*
	if (esd_resume(sound_fd) < 0) {
		module_logger(&esdSound, _L|LOG_USER | LOG_ERROR, _("cannot resume sound (%s)\n"),
			 strerror(errno));
		return vmNotAvailable;
	}
*/

	pthread_mixer_init(soundhz, 
					   soundblksize,
					   true /*signed*/,
					   false /*eight bit*/,
					   false /*big-endian*/);

	return pthread_mixer_enable();
}

static      vmResult
esd_disable(void)
{
	vmResult    res;

	if ((res = pthread_mixer_disable()) != vmOk) {
		module_logger(&esdSound, _L|0, _("disable: %d\n"), res);
		return res;
	}
	pthread_mixer_term();
/*
	if (esd_standby(sound_fd) < 0) {
		module_logger(&esdSound, _L|LOG_USER | LOG_ERROR, _("cannot standby sound (%s)\n"),
			 strerror(errno));
	}
*/

	return vmOk;
}

static      vmResult
esd_restart(void)
{
#warning set volume
	return pthread_mixer_restart();
}

static      vmResult
esd_restop(void)
{
#warning reset volume
	return pthread_mixer_restop();
}


static vmSoundModule esdSoundModule = {
	4,
	esd_update,
	esd_flush,
	esd_play,
	esd_read,
	esd_playing
};

vmModule    esdSound = {
	3,
	"Enlightened Sound Daemon",
	"sndESD",

	vmTypeSound,
	0,

	esd_detect,
	esd_init,
	esd_term,
	esd_enable,
	esd_disable,
	esd_restart,
	esd_restop,

	{(vmGenericModule *) & esdSoundModule}
};
