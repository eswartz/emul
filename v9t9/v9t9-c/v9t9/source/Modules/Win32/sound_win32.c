/*
  sound_win32.c					-- V9t9 module for Win32 sound interface
  
  Currently using the wave functions.

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

#include "winv9t9.h"

#include "v9t9_common.h"
#include "v9t9.h"
#include "log.h"
#include "sound.h"
#include "timer.h"

#include "mix_server.h"

#define _L LOG_SOUND|LOG_INFO

//static UINT wDevID, wDevMask;
//static WAVEOUTCAPS wCaps;
static HWAVEOUT wHandle;
static HANDLE wSem;
static HANDLE hMixerThread;
static WAVEFORMATEX wfx;

#define MIXFREQ 5
#define MIXFRAGS 20

static int  numhdrs;
static WAVEHDR *whdrs;
static int  curhdr;

static mix_context context;

static HANDLE hMixEvent;

static void CALLBACK 
MyWaveCallBack(HWAVEOUT whandle, UINT uMsg,
			   DWORD dwUser, DWORD dwParam1,
			   DWORD dwParam2)
{
	switch (uMsg) {
	case WOM_OPEN:
		/* inspire mixer to continue */
		SetEvent(hMixEvent);
		break;
	case WOM_DONE:
	{
		//log("releasing wSem... %d\n", GetTickCount());
		ReleaseSemaphore((HANDLE) dwUser, 1, NULL);
		SetEvent(hMixEvent);
		break;
	}
	case WOM_CLOSE:
		break;
	}
}

////////////////

static DWORD dwMixThreadID;

/*************************/

/*	Mixer thread */

static BOOL bMixingDone;


static DWORD WINAPI
MyMixerThread(LPVOID arg)
{
	WAVEHDR    *hdr;
	MMRESULT    res;

//  if (!SetThreadPriority(GetCurrentThread(), THREAD_PRIORITY_TIME_CRITICAL))
//      FAIL("Could not set thread priority");
	curhdr = 0;
	if (!wHandle)
		ExitThread(0);
	
	while (WaitForSingleObject(hMixEvent, 100) != WAIT_OBJECT_0)
			;

	bMixingDone = FALSE;
	while (!bMixingDone) {

		while (!bMixingDone && 
			WaitForSingleObject(wSem, 100) != WAIT_OBJECT_0)
			   ;

		if (features & FE_SOUND) 
		{

			if (!wHandle)
				ExitThread(0);

			hdr = whdrs + curhdr;
			mix_mixit(&context, context.bufsize);
			//mix_advance(&context, context.bufsize);

			memcpy(hdr->lpData, context.buffer, hdr->dwBufferLength);

			res = waveOutWrite(wHandle, hdr, sizeof(WAVEHDR));
			if (res != MMSYSERR_NOERROR)
				module_logger(&win32Sound, _L | LOG_ERROR, "failed in waveOutWrite (%d)\n", res);

			curhdr = (curhdr + 1) % numhdrs;
		}
	}

	ExitThread(0);
	return 0;
}

/*****************************/

/*	Update voice parameters  */
static void
win32sound_update(vmsUpdateMask updated)
{
	int         v;

	if (!(features & FE_PLAYSOUND)) {
		return;
	}

	for (v = VOICE_TONE_0; v <= VOICE_TONE_2; v++)
	{
		voiceinfo *vinfo = &sound_voices[v];
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
		voiceinfo *vinfo = &sound_voices[VOICE_NOISE];
		mix_handle_noise(&context, 
						 mix_Noise, 
						 vinfo->hertz,
						 OPERATION_TO_VOLUME(vinfo),
						 OPERATION_TO_NOISE_TYPE(vinfo) == NOISE_WHITE);
	}
}

/*	Flush voices */
static void	
win32sound_flush(void)
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
static void
win32sound_play(vmsPlayMask kind, s16 * data, int len, int hz)
{
//  if (!(features & FE_SOUND)) return;
	switch (kind) {
	case vms_Speech:
	{
		mix_handle_data(&context, mix_Speech, hz, 255, len, data);
		break;
	}
	case vms_AGw:
	{
		mix_handle_data(&context, mix_AudioGate, hz,
						audiogate.latch ? audiogate.play ? 0x7f : 0x1f : 0,
						len, data);
		break;
	}
	}
}

static int
win32sound_playing(vmsPlayMask kind)
{
	// stub
	return 0;
}

/**************************************/

static      vmResult
win32sound_detect(void)
{
	if (waveOutGetNumDevs() == 0) {
		module_logger(&win32Sound, _L|LOG_USER | LOG_ERROR, _("No Win32 wave sound devices installed\n"));
		return vmNotAvailable;
	} else
		return vmOk;
}

static      vmResult
win32sound_init(void)
{
	MMRESULT    res;

	wfx.wFormatTag = WAVE_FORMAT_PCM;
	wfx.nChannels = 1;
	wfx.wBitsPerSample = sndplaybits;
	if (sndplayhz <= 11025)
		wfx.nSamplesPerSec = 11025;
	else if (sndplayhz <= 22050)
		wfx.nSamplesPerSec = 22050;
	else						/*if (sndplayhz <= 44100) */
		wfx.nSamplesPerSec = 44100;
	wfx.nBlockAlign = wfx.wBitsPerSample * wfx.nChannels / 8;
	wfx.nAvgBytesPerSec = wfx.nSamplesPerSec * wfx.nBlockAlign;
	wfx.cbSize = 0;

	numhdrs = MIXFRAGS;

	whdrs = (WAVEHDR *) calloc(sizeof(WAVEHDR), numhdrs);
	if (whdrs == NULL)
		module_logger(&win32Sound, _L|LOG_FATAL, _("out of memory for wave headers\n"));

	wSem = CreateSemaphore(NULL, numhdrs, numhdrs, NULL);
	if (wSem == 0)
		module_logger(&win32Sound, _L|LOG_FATAL, _("could not create wave semaphore\n"));

	hMixEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
	if (hMixEvent == 0) {
		module_logger(&win32Sound, _L|LOG_ERROR | LOG_USER, _("Could not create mixer event\n"));
		ExitThread(-1);
	}

	hMixerThread = CreateThread(NULL, 16384, MyMixerThread, NULL,
								CREATE_SUSPENDED, &dwMixThreadID);
	if (hMixerThread == 0) {
		module_logger(&win32Sound, _L|LOG_ERROR | LOG_USER, _("could not create mixer thread, '%d'\n"),
			 GetLastError());
		return vmInternalError;
	}
//#warning FE_SOUND
//  if (features & FE_SOUND)
	{
		wHandle = 0;
		res =
			waveOutOpen(&wHandle, WAVE_MAPPER, &wfx, (DWORD) MyWaveCallBack,
						(DWORD) wSem, CALLBACK_FUNCTION);
		if (res != MMSYSERR_NOERROR) {
			wHandle = 0;
			module_logger(&win32Sound, _L|LOG_ERROR | LOG_USER, _("Could not open a wave device.\n"));
			//FAIL("waveOutOpen failed");
//          features &= ~FE_SOUND;
			return vmNotAvailable;
		}

		/*  Initialize our headers */
		for (curhdr = 0; curhdr < numhdrs; curhdr++) {
			WAVEHDR    *hdr = whdrs + curhdr;
			int         length =
				wfx.nSamplesPerSec / MIXFRAGS / MIXFREQ * sizeof(s16);

			hdr->lpData = (void *) malloc(length);
			if (hdr->lpData == NULL) {
				module_logger(&win32Sound, _L|LOG_ERROR | LOG_USER,
					 _("out of memory for wave headers\n"));
//              features &= ~FE_SOUND;
				return vmInternalError;
			}

			hdr->dwBufferLength = length;
			hdr->dwFlags = 0;
			hdr->dwLoops = 0;

			res = waveOutPrepareHeader(wHandle, hdr, sizeof(WAVEHDR));
			if (res != MMSYSERR_NOERROR) {
				module_logger(&win32Sound, _L|LOG_ERROR | LOG_USER,
					 _("failed in waveOutPrepareHeader (%d)\n"), res);
//              features &= ~FE_SOUND;
				return vmInternalError;
			}
		}
	}

	return vmOk;
}

static      vmResult
win32sound_term(void)
{
	MMRESULT    res;

//  #warning FE_SOUND
//  if (features & FE_SOUND)
	{
		/* tells thread to quit */
		bMixingDone = TRUE;
		if (hMixerThread)
			CloseHandle(hMixerThread);
//      ResumeThread(hMixerThread);
//      WaitForSingleObject(hMixerThread, INFINITE);

		if (wHandle)
		{
			/*  after this call, we will get a lot of callbacks */
			res = waveOutReset(wHandle);
			if (res != MMSYSERR_NOERROR) {
				FAIL("waveOutReset failed");
				return vmInternalError;
			}

			/*  Release our headers */
			for (curhdr = 0; curhdr < numhdrs; curhdr++) {
				WAVEHDR    *hdr = whdrs + curhdr;

				waveOutUnprepareHeader(wHandle, hdr, sizeof(WAVEHDR));
				if (hdr->lpData) {
					free(hdr->lpData);
					hdr->lpData = NULL;
				}
			}

			waveOutClose(wHandle);
			if (res != MMSYSERR_NOERROR) {
				FAIL("waveOutClose failed");
				return vmInternalError;
			}
		}
		CloseHandle(wSem);
	}
	return vmOk;
}

static      vmResult
win32sound_enable(void)
{
	mix_init(&context, wfx.nSamplesPerSec,
			 wfx.nSamplesPerSec / MIXFRAGS / MIXFREQ * sizeof(s16),
			 false, true, false);

	return vmOk;
}

static      vmResult
win32sound_disable(void)
{
	mix_term(&context);

	return vmOk;
}


static      vmResult
win32sound_restart(void)
{
	MMRESULT    res;

	mix_restart(&context);

//#warning FE_SOUND 
//  if (features & FE_SOUND)
	{
		if (wHandle)
		{
			if (hMixerThread)
				ResumeThread(hMixerThread);
			res = waveOutRestart(wHandle);
			if (res != MMSYSERR_NOERROR) {
				FAIL("waveOutRestart failed");
	//          features &= ~FE_SOUND;
				return vmInternalError;
			}
		}
	}
	return vmOk;
}

static      vmResult
win32sound_restop(void)
{
	MMRESULT    res;

//#warning FE_SOUND 
//  if (features & FE_SOUND)
	{
		if (wHandle)
		{
			res = waveOutPause(wHandle);
			if (res != MMSYSERR_NOERROR) {
				FAIL("waveOutReset failed");
	//          features &= ~FE_SOUND;
				return vmInternalError;
			}
			if (hMixerThread)
				SuspendThread(hMixerThread);
		}
	}

	return vmOk;
}

static vmSoundModule win32SoundModule = {
	4,
	win32sound_update,
	win32sound_flush,
	win32sound_play,
	NULL,
	win32sound_playing
};

vmModule    win32Sound = {
	3,
	"Win32 Sound",
	"sndWin32",

	vmTypeSound,
	vmFlagsNone,

	win32sound_detect,
	win32sound_init,
	win32sound_term,
	win32sound_enable,
	win32sound_disable,
	win32sound_restart,
	win32sound_restop,

	{(vmGenericModule *) & win32SoundModule}
};
