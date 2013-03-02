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

#ifndef __DEMO_H__
#define __DEMO_H__

#define DEMO_MAGIC_HEADER	"V910"
#define DEMO_MAGIC_HEADER_LENGTH 4

typedef enum
{
	demo_type_tick = 0,	/* wait for emulator tick */
	demo_type_video = 1,	/* video addresses and data */
	demo_type_sound = 2,	/* sound bytes */
	demo_type_speech = 3,	/* speech commands */
	demo_type_cru_write = 4,		/* CRU access [live client only] */
	demo_type_cru_read = 5		/* CRU access [live client only] */
}	demo_type;

typedef enum
{
	demo_speech_starting = 0,		/* new phrase */
	demo_speech_adding_byte = 1,	/* an LPC encoded byte (following) */
	demo_speech_terminating = 2,	/* terminating speech */
	demo_speech_stopping = 3,		/* finished */
	demo_speech_interrupt = 4		/* interrupt to perform work */
}	demo_speech_event;

/*
 *	The demo file format is very rudimentary.
 *
 *	Header:		'V910' bytes
 *
 *	Followed by a list of sections for various demo_types.
 *	Each section starts with one byte (demo_type) and is
 *	followed by nothing (for the timer) or by a buffer length
 *	(little-endian, 16 bits) which is passed to the event handler.
 *
 *	Video has 16-bit little-endian addresses followed (if the
 *	address does not have the 0x8000 bit set, which is a register
 *	write) by a 16-bit little-endian length and data bytes.
 *
 *	Sound has a series of data bytes.
 *
 *	Speech has a series of demo_speech_event bytes, and the
 *	demo_speech_adding_byte event is followed by that byte.
 */

extern bool demo_recording, demo_playing;

void demo_init(void);
void demo_term(void);
int demo_record_event(demo_type type, ...);
int demo_start_playback(OSSpec *spec);
int demo_playback_loop(void);

#endif
