/*
  mix_server.c					-- routines for mixing digitized sound

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

//#define DUMP_DATS 1

#include "v9t9_common.h"

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <malloc.h>

#include <config.h>
#include "clstandardtypes.h"
#include "sysdeps.h"
#include "log.h"
#include "mix_server.h"
#include "xmalloc.h"

#define _L	 LOG_SOUND | LOG_INFO

/*		MIXER SERVER		*/

#if UNDER_UNIX && __i386__
//#define WRITE_TO_FILE 1
#endif

#if WRITE_TO_FILE
#include <fcntl.h>
int snd_file, mix_file;
#endif


/*	This is a OS-generic module for mixing digitized samples together. */
void        
mix_init(mix_context * m, u32 hertz, u32 bufsize,
		 bool issigned, bool eightbit, bool bigendian)
{
	u16         x;

#if DUMP_DATS
	unlink("dats.txt");
#endif

#if WRITE_TO_FILE
	snd_file = open("digital.raw", O_CREAT|O_TRUNC|O_WRONLY, 0666);
	if (snd_file < 0) snd_file = 0;
	mix_file = open("mixed.raw", O_CREAT|O_TRUNC|O_WRONLY, 0666);
	if (mix_file < 0) mix_file = 0;
#endif

	x = ('A' << 8) + 'B';
	m->soundhz = hertz < 4000 ? 4000 : hertz;
	m->issigned = issigned;
	m->eightbit = eightbit;
	m->swapendian = (*(u8 *) & x == 'B') == bigendian;

	logger(_L | L_1, _("mix_init: swapendian=%d\n\n"), m->swapendian);
	m->buffer = (s32 *) xmalloc(sizeof(s32) * bufsize);
	m->bufsize = bufsize;

	OS_CreateMutex(&m->mutex);
}

void
mix_term(mix_context * m)
{
	if (m->buffer) {
		xfree(m->buffer);
		m->buffer = NULL;
	}
#if WRITE_TO_FILE
	if (snd_file) close(snd_file);
	if (mix_file) close(mix_file);
#endif

	OS_KillMutex(&m->mutex);
}

void
mix_restart(mix_context * m)
{
	memset(m->voices, 0, sizeof(m->voices));
	m->voices[0].clock = m->voices[1].clock = m->voices[2].clock =
		m->voices[3].clock = m->voices[4].clock = m->voices[5].clock =
		m->soundhz;
	m->voices[3].ns1 = 0xaaaaaaaa;
	m->voices[3].ns2 = 1;
}

//  Step a tone voice by one sample and return contrib.
INLINE void
step_tone(sample * v, s32 * chn, int * active)
{
	if (!(v->vol & 0x80000000)) {
		*chn += v->vol;
		(*active)++;
	}
	v->div += v->delta;
	while (v->div >= v->clock) {
		v->vol ^= 0x80000000;
		//while (v->div >= v->clock)
		v->div -= v->clock;
	}	
}

//  Step white noise by one sample and update dat.

#define NOISE_GATE(x,y) \
	do {						\
		x = (x<<1) | (x>>31);	\
		x ^= y;					\
		if ((y += x)==0)	y++; \
	} while (0)

INLINE void
step_white(sample * v, s32 * chn, int * active)
{
	v->div += v->delta;
	while (v->div >= v->clock) {
		NOISE_GATE(v->ns1, v->ns2);
		v->div -= v->clock;
	}
	if (v->ns1 & 1) {
		*chn += v->vol;
		(*active)++;
	}
}

//  Step periodic noise by one sample and update dat.
#define PERIODMULT 16
INLINE void
step_periodic(sample * v, s32 * chn, int * active)
{
	v->div += v->delta;
	if (v->div >= v->clock) {
		*chn += v->vol;
		(*active)++;
		while (v->div >= v->clock)
			v->div -= v->clock;
	} else {
		*chn -= v->vol;
		(*active)++;
	}
}

static sampleblock *unlink_sample_block(sample *v, sampleblock *blk)
{
	sampleblock *next = blk->next;
	xfree(blk->data);
	blk->data = 0;
	xfree(blk);
	if (v->head == blk)
		v->head = next;
	if (v->tail == blk) {
		v->tail = 0L;
		v->st = 0;
	}
	return next;
}

//  Step speech by one sample and update dat.
//  Sample is finished when s->used==0.  Caller should free memory.
INLINE void
step_digital(sample * v, s32 * chn, int * active)
{
	sampleblock *blk = v->head;

	// find block with the current sample,
	// throwing away unused blocks
	while (blk && blk->start + blk->size <= v->st) {
		blk = unlink_sample_block(v, blk);
	}

	// play sample from block
	if (blk) {
		int offs = v->st - blk->start;
		if (offs < 0 || offs >= blk->size)
			logger(_L|LOG_FATAL, _("accessing outside sampleblock (%d, %d)\n"),
				   offs, blk->size);
		*chn += blk->data[offs] * (long long) v->vol / (signed) 65536;
		(*active)++;

		v->div += v->delta;
		while (v->div >= v->clock) {
			v->div -= v->clock;
			v->st++;
		}
	}
}


#if 0
//  Step audio gate 
INLINE void
step_audiogate(sample * v, s32 * chn, int * active)
{
	// since this can change so fast, we set v->clock
	// to indicate something should happen
	if (v->clock & 1) {
		*chn += v->vol;
	} else if (v->clock) {
		*chn -= 0x7fffff;
	}
	(*active)++;
}

//  Advance audio gate
INLINE void
advance_audiogate(sample * v, u32 samples)
{
	if (v->clock > samples)
		v->clock -= samples;
	else
		v->clock = 0;
}
#endif

static int  had_silent_frame = 0;
static int
mix_silence(mix_context * m)
{
	// check that something is on
	return (
			(m->voices[0].vol | m->voices[1].vol | m->voices[2].
			 vol | m->voices[3].vol | m->voices[4].vol | m->voices[5].vol) ==
			0 ||
			// and that nothing is illegal
			(m->voices[0].clock && m->voices[1].clock && m->voices[2].clock
			 && m->voices[3].clock && m->voices[4].clock
			 && m->voices[5].clock) == 0);
}

/*
	Mix the channels together and generate a segment of sound.
*/
void
mix_mixit(mix_context * m, u32 samples)
{
	s32        *out = m->buffer, *end = out + samples;
	s32         dat = 0;
	int         div = 0;
	int         silent;
#if DUMP_DATS
	FILE		*f = 0L;
#endif

	OS_LockMutex(&m->mutex);

#if DUMP_DATS
	if (log_level(_L) >= 3)
	{
		f = fopen("dats.txt", "a");
	}
#endif

	silent = mix_silence(m);
	if (!silent || !had_silent_frame) {
		had_silent_frame = silent;

		while (out < end) {
			dat = 0;
			div = 0;

			// tones
			if (m->voices[0].vol)
				step_tone(&m->voices[0], &dat, &div);
			if (m->voices[1].vol)
				step_tone(&m->voices[1], &dat, &div);
			if (m->voices[2].vol)
				step_tone(&m->voices[2], &dat, &div);

			// noise
			if (m->voices[3].vol) {
				sample     *n = &m->voices[3];

				if (n->iswhite) {
					step_white(n, &dat, &div);
				} else {
					step_periodic(n, &dat, &div);
				}

			}

			// speech
			if (m->voices[4].head) {
				step_digital(&m->voices[4], &dat, &div);
			}

			// audio gate
			if (m->voices[5].head) {
				step_digital(&m->voices[5], &dat, &div);
			}

			if (div) {
				//dat /= div;
				dat >>= 2;
			}

			*out = dat <= -0x00800000 ? -0x007fffff :
				dat >= 0x00800000 ? 0x007fffff : dat;

#if DUMP_DATS
			if (f)
			{
				static int was_zero = 0;
				if (!was_zero || *out)
					fprintf(f, "%d\n", *out);
				was_zero = !*out;
			}
#endif

			out++;
		}

	} else {
		memset(m->buffer, 0, samples * sizeof(s32));
		had_silent_frame = true;
	}

	/*  Convert sample  */

	if (m->eightbit) {
		int         idx;
		s8         *ptr = (s8 *) m->buffer;

		for (idx = 0; idx < samples; idx++)
			*ptr++ = m->buffer[idx] >> 16;	/* 24 -> 8 */
	} else {
		int         idx;
		s16        *ptr = (s16 *) m->buffer;

		for (idx = 0; idx < samples; idx++)
			*ptr++ = m->buffer[idx] >> 8;	/* 24 -> 16 */
	}

	if (!m->issigned) {
		int         idx;
		int         step = (m->eightbit ? 1 : 2);
		s8         *ptr = (s8 *) m->buffer;

		for (idx = (m->swapendian ? 0 : step - 1); idx < samples; idx += step)
			ptr[idx] ^= 0x80;
	}

	if (m->swapendian && !m->eightbit) {
		swab((const void *) m->buffer, (void *) m->buffer,
			 samples * sizeof(u16));
	}

#if WRITE_TO_FILE
	if (mix_file) write(mix_file, m->buffer, samples * (m->eightbit ? 1 : 2));
#endif

#if DUMP_DATS
	if (f)
	{
		if (!silent)
			fprintf(f,"0\n0\n0\n0\n0\n0\n0\n0\n");

		fclose(f);
	}
#endif

	OS_UnlockMutex(&m->mutex);
}

static void
stackdata(mix_context *m, sample * s, s16 * bytes, int size)
{
	sampleblock *ptr;

	if (!bytes)
		return;

//	OS_LockMutex(&m->mutex);

	// link sample onto list
	ptr = (sampleblock *)xmalloc(sizeof(sampleblock));
	ptr->data = xmalloc(size * sizeof(ptr->data[0]));
	ptr->size = size;
	ptr->start = s->tail ? s->tail->start + s->tail->size : 0;
	ptr->next = 0L;
	memcpy(ptr->data, bytes, size * sizeof(ptr->data[0]));

	if (s->tail)
		s->tail->next = ptr;
	else
		s->head = ptr;

	s->tail = ptr;

	ptr = s->head;
	logger(_L|L_1, _("samples:\t"));
	while (ptr) {
		logger(_L|L_1, _("[start=%d, size=%d] "), ptr->start, ptr->size);
		ptr = ptr->next;
	}

//	OS_UnlockMutex(&m->mutex);
}

///////////////////////////////////////////////////////

/*
#include <math.h>
#include <stdio.h>

int main(void)
{
        int x;
        for (x=0; x<16; x++)
        {
                double f = exp((x/15.0) * log(17)) ;
                printf("\t%08X,\n", (int)(f * 0x080000));

        }
}
*/

static u32  atten[16] = {
	0x00000000,
//  0x00080000,
	0x0009A9C5,
	0x000BAC10,
	0x000E1945,
	0x001107A1,
	0x001491FC,
	0x0018D8C4,
	0x001E0327,
	0x00244075,
	0x002BC9D6,
	0x0034E454,
	0x003FE353,
	0x004D2B8C,
	0x005D36AB,
	0x007097A5,
	0x007FFFFF
};

void
mix_handle_voice(mix_context * m, u8 channel, u32 hertz, u8 volume)
{
	sample     *s;

	OS_LockMutex(&m->mutex);

	logger(_L | L_2, _("mix_handle_voice: channel %d, hertz = 0x%x, volume = %d\n"),
		 channel, hertz, volume);

	if (channel >= mix_CHN0 && channel <= mix_CHN2) 
	{
		s = &m->voices[channel];

		// sounds this high-pitched won't
		// work at all.
		if (hertz * 2 >= m->soundhz) {
			logger(_L | L_2, _("ignoring tone at %d Hz\n"), hertz);
			s->delta = m->soundhz/2-1;
			//s->vol = atten[volume];
			s->vol = 0;
			s->clock = m->soundhz;	// assure no zero divides
		} else {
			s->clock = m->soundhz;
			s->delta = hertz * 2;
			//s->div = volume ? s->div : 0;
			s->div = 0;
			s->vol = atten[volume];
		}
	}

	OS_UnlockMutex(&m->mutex);
}

void
mix_handle_noise(mix_context * m, u8 channel, u32 hertz, u8 volume, 
				 int iswhite)
{
	sample     *s;

	OS_LockMutex(&m->mutex);

	logger(_L | L_2, _("mix_handle_noise: channel %d, hertz = 0x%x, volume = %d, iswhite = %d\n"),
		 channel, hertz, volume, iswhite);

	if (channel == mix_Noise)
	{
		s = &m->voices[channel];

		if (iswhite) {
			s->clock = m->soundhz;
			s->delta = hertz;
			s->div = volume ? s->div : 0;
			s->vol = atten[volume];
			s->iswhite = 1;
		} else {
			s->clock = m->soundhz * PERIODMULT;
			s->delta = hertz;
			s->div = volume ? s->div : 0;
			s->vol = atten[volume];
			s->iswhite = 0;
		}
	}

	OS_UnlockMutex(&m->mutex);
}

void
mix_handle_data(mix_context * m, u8 channel, u32 hertz, u8 volume, u32 length,
				s16 * data)
{
	OS_LockMutex(&m->mutex);

	logger(_L | L_1, _("mix_handle_data: using %d bytes of %d Hz data on channel %d\n"),
		 length, hertz, channel);

#if WRITE_TO_FILE
	if (snd_file) write(snd_file, data, length * sizeof(data[0]));
#endif

	switch (channel) {
	case mix_Speech:
	{
		sample     *s = &m->voices[4];
		if (data && length) {
			s->clock = m->soundhz;
			s->delta = hertz;
			s->div = 0;
			s->vol = volume << 15;
			stackdata(m, s, data, length);
		} else {
			// forcibly flush
			sampleblock *blk = s->head;
			while (blk)
				blk = unlink_sample_block(s, blk);
		}

		break;
	}

	case mix_AudioGate:
	{
		/*  for the audio gate, we only use the volume;
		   no data need be passed.  This is because we interpret
		   'length' as a repeat count for 'vol'.  */

		if (length) {
			s16         *tmp = (s16 *) alloca(length * sizeof(s16));
			sample     *s = &m->voices[5];

//              int x;
			logger(_L | L_2, _("writing %d bytes of %d as audio gate\n"), length, volume);
//              for (x=0; x<length; x++)
//                  tmp[x] = ((volume ^ !!(x&1)) ? 0x7f : 0) ;
			memset(tmp, volume, length);
			s->clock = m->soundhz;
			s->delta = hertz;
			s->div = 0;
			s->vol = volume << 15;
			stackdata(m, s, tmp, length);
		} else {
			sample     *s = &m->voices[5];

/*
			if (s->used > s->delta) {
//                  if (s->data) xfree(s->data); s->data = NULL;
				s->st = s->en = s->len = s->used = 0;
			}
*/
		}
		break;
	}
	}
	OS_UnlockMutex(&m->mutex);
}


int		mix_data_pending(mix_context *m, u8 channel)
{
	sample *s = 0L;
	int ret;

	OS_LockMutex(&m->mutex);

	s = &m->voices[channel];

	ret = s && s->head;

	OS_UnlockMutex(&m->mutex);
	return ret;
}

/*********************************/
