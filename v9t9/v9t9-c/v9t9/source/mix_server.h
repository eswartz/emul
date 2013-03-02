/*
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

#ifndef __MIX_SERVER_H__
#define __MIX_SERVER_H__

#include "clstandardtypes.h"
#include "OSLib.h"

#include "centry.h"

/*	values for the channel argument. */
enum
{
	mix_CHN0, mix_CHN1, mix_CHN2, mix_Noise, 	/* tone channels */
	mix_Speech,									/* data channels */
	mix_AudioGate								/* toggle channels */
};

typedef struct sampleblock sampleblock;

struct sampleblock
{
	s16		*data;					/* digitized data */
	u32		start;					/* start offset (for bias of sample.st) */
	u32		size;					/* number of samples */
	sampleblock *next;				/* next sample or NULL */
};

typedef struct 
{
//	s16		*data;					/* circular buffer (may be NULL) */
	sampleblock *head, *tail;		/* digitized samples for data channels, or NULL */
//	u32		len, used;				/* total alloc'd len */
	u32		st, en;					/* start and end of buffer */
	u32		div, delta, clock, vol;	/* timing info */
	u32		iswhite, ns1, ns2;		/* noise info */
}	sample;

// v0-v2 are tones
// v3 is noise
// v4 is speech
// v5 is audio gate
typedef struct
{
	OSMutex	mutex;
	sample	voices[6];
	u32		soundhz;
	s32		*buffer;
	u32		bufsize;
	bool	issigned, eightbit, swapendian;
}	mix_context;

void 	mix_init(mix_context *m, u32 hertz, u32 bufsize,
				bool issigned, bool eightbit, bool bigendian);
void 	mix_term(mix_context *m);
void 	mix_restart(mix_context *m);

void	mix_mixit(mix_context *m, u32 samples);
//void	mix_advance(mix_context *m, int samples);

void 	mix_handle_voice(mix_context *m, u8 channel, u32 hertz, u8 volume);
void 	mix_handle_noise(mix_context *m, u8 channel, u32 hertz, u8 volume, 
						 int iswhite);
void 	mix_handle_data(mix_context *m, u8 channel, u32 hertz, u8 volume, 
						u32 len, s16 *data);
int		mix_data_pending(mix_context *m, u8 channel);

void	mixer_init_commands(void);

#include "cexit.h"

#endif
