/*
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

#ifndef __SOUND_H__
#define __SOUND_H__

#include "centry.h"

#define PLAYABLE (FE_SOUND|FE_PLAYSOUND)
#define PLAYSOUND ((features&PLAYABLE)==PLAYABLE)

/* send a sound command to every module */
#define DOSOUND(x,y) do { \
	vmModule *ptr = vmSound; \
	while (ptr) {\
		if ((ptr->runtimeflags & vmRTInUse) && (ptr->m.sound->x)) \
				ptr->m.sound->x y; \
		ptr = ptr->next; \
	} \
	} while (0)

#define ISSOUND(r, x,y) do { \
	vmModule *ptr = vmSound; \
    r = 0; \
	while (ptr) {\
		if ((ptr->runtimeflags & vmRTInUse) && (ptr->m.sound->x)) \
				r |= ptr->m.sound->x y; \
		ptr = ptr->next; \
	} \
	} while (0)

#define DOSOUNDUPDATE(w) DOSOUND(update, (w))

#define DOSOUNDFLUSH() DOSOUND(flush, ())

#define DOSOUNDPLAY(kind,data,size,hz) DOSOUND(play, (kind, data, size, hz))

#define SOUNDUPDATE(w) do { \
	if (features&FE_PLAYSOUND) DOSOUNDUPDATE(w); \
	} while (0)

#define SOUNDFLUSH() DOSOUNDFLUSH()

#define SOUNDPLAY(kind,data,size,hz) do { \
	if (features&FE_PLAYSOUND) DOSOUNDPLAY(kind,data,size,hz); \
	} while (0)

#define SPEECHPLAY(kind,data,size,hz) do { \
	if (features&FE_PLAYSPEECH) DOSOUNDPLAY(kind,data,size,hz); \
	} while (0)

#define SPEECHPLAYING(res, kind) ISSOUND(res, playing, (kind))

/*	This struct is defined in sound.c through writes
	to the sound port.  
	
	'period' and 'hertz' are related; 'period' is a number 0 through 0x3ff
	which was decoded from writes to the port and 'hertz' is the frequency
	of the tone that represents.
	
	'volume' is really attenuation -- 0 is loudest, 0xf is silent or off. 
	
	'stype' describes the characteristics of the noise channel (voices[3]);
	this is directly from the sound port (0-7).  stype >= 4 is white noise,
	else periodic.  The sound module defines 'period' and 'hertz' based on
	whether the stype represents a fixed-frequency sound or one that is
	derived from the frequency of channel 2.
*/

/* These are used as an index into the operation[] field */
enum
{
	OPERATION_FREQUENCY_LO = 0,		/* low 4 bits [1vv0yyyy] */
	OPERATION_CONTROL = 0,			/* for noise  [11100xyy] */
	OPERATION_FREQUENCY_HI = 1,		/* hi 6 bits  [00yyyyyy] */
	OPERATION_ATTENUATION = 2		/* low 4 bits [1vv1yyyy] */
};

typedef struct voiceinfo
{
	u8	operation[3];	// operation bytes
	
	u8	volume;			// volume, 0 == off, 0xf == loudest
	u16	period, hertz;	// calculated from OPERATION_FREQUENCY_xxx
}	voiceinfo;

enum
{
	VOICE_TONE_0, VOICE_TONE_1, VOICE_TONE_2, VOICE_NOISE
};

extern voiceinfo sound_voices[4];

#define OPERATION_TO_VOICE(o) \
		( ((o) & 0x60) >> 5)

/*
 *	Masks for the OPERATION_CONTROL byte for VOICE_NOISE
 */
enum
{
	NOISE_PERIODIC = 0,
	NOISE_WHITE = 0x4
};

enum
{
	NOISE_PERIOD_FIXED_0,
	NOISE_PERIOD_FIXED_1,
	NOISE_PERIOD_FIXED_2,
	NOISE_PERIOD_VARIABLE
};

#define OPERATION_TO_PERIOD(v)	\
		( ((v)->operation[OPERATION_FREQUENCY_LO] & 0xf) | \
		( ((v)->operation[OPERATION_FREQUENCY_HI] & 0x3f) << 4 ) )

#define PERIOD_TO_HERTZ(p)		\
		((p) > 1 ? (111860 / (p)) : (55930))

#define OPERATION_TO_NOISE_TYPE(v)  \
		( (v)->operation[OPERATION_CONTROL] & 0x4 )

#define OPERATION_TO_NOISE_PERIOD(v)  \
		( (v)->operation[OPERATION_CONTROL] & 0x3 )

#define OPERATION_TO_ATTENUATION(v) \
		( (v)->operation[OPERATION_ATTENUATION] & 0xf )

#define OPERATION_TO_VOLUME(v) \
		( 0xf - OPERATION_TO_ATTENUATION(v) )

/*	Audio gate info. */

typedef struct
{
	u32 base_time, last_time;		/* base time for making noise, 
										last time gate accessed */
	u32 length;						/* how long to trigger */
	u32 hertz;						/* reference for length */
	u8 latch;						/* last bit was on or off? */
	u8 play;						/* is gate on or off? */
}	AudioGate;

extern AudioGate audiogate;

/*****************************************************/

/*	Playback options	*/
extern	int		sndplayhz;		/* playback rate */
extern	int		sndplaybits;	/* word size for sound */

extern	void	sound_mmio_write(u8 val);		/* to the I/O port */

extern	void	sound_init(void);

extern	int		sound_restart(void);
extern	void	sound_restop(void);
extern	int		sound_enable(void);
extern	void	sound_disable(void);

/* turn sound on or off according to features & FE_PLAYSOUND */
extern void		sound_switch(void);

/* destructively turn it off */
extern void		sound_silence(void);

/******************/

/*	Cassette routines  */

/*	"Time passed" is measured in 9901 timer ticks, the maximum
	being 46875 Hz, and set with cassette_set_timer() when the clock rate
	changes.  The actual cassette routines will not set the timer this high,
	only about ~1390 Hz.  If a wave file is used for input, the actual input
	will be smoothed to fit the timer rate.  If a data file is used, it is
	assumed to be exactly the cassette's data rate.

	The cassette is constantly running once the motor is started, so it is
	possible to lose data.  This is why cassette_read returns only one bit.
*/

void	cassette_set_timer(u16 hz);
void	cassette_set_motor(int csx, u8 onoff);
void	cassette_write(u8 onoff, u32 timepassed);
u8		cassette_read(void);


#include "cexit.h"

#endif
