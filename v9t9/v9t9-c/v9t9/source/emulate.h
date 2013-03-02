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

#ifndef __EMULATE_H__
#define __EMULATE_H__

#include "v9t9_module.h"

#include "centry.h"

extern	vmModule emulate9900CPU;

extern 	u32	stateflag;

#define	ST_DEBUG		0x1
#define	ST_TERMINATE 	0x2
#define ST_INTERRUPT	0x8
#define	ST_REBOOT		0x10
#define ST_PAUSE		0x20
#define ST_STOP			0x100		// interrupt execution
#define ST_INTERACTIVE	0x200		// ask for user commands
#define ST_SINGLESTEP	0x400	   	// step once
#define ST_DEMOING		0x800		// recording a demo

#define FE_SOUND		0x1
#define FE_PLAYSOUND	0x2
#define FE_SPEECH		0x4
#define FE_PLAYSPEECH	0x8
#define FE_VIDEO		0x10
#define FE_SHOWVIDEO	0x20
#define FE_KEYBOARD		0x40
#define FE_CSXREAD		0x80
#define FE_CSXWRITE		0x100
#define FE_CASSETTE		(FE_CSXREAD|FE_CSXWRITE)

#ifdef REAL_DISK_DSR
#define FE_realdisk		0x1000
#endif
#ifdef EMU_DISK_DSR
#define FE_emudisk		0x2000
#endif

#define FE_CLIENT  		0x4000

extern u32	features;

void	execution_pause(bool enable);
INLINE bool execution_paused(void) { return !!(stateflag & ST_PAUSE); }

//	basic divisor for handling time functions
#define BASE_EMULATOR_HZ 	100
//	clock speed of CPU -- 3.0 MHz 
extern u32 baseclockhz;

extern u32 currenttime;		/* time, [0..baseclockhz / BASE_EMULATOR_HZ) */
extern u64 totalticks;		/* total number of BASE_EMULATOR_HZ events passed */

extern u64 currentcycles;	// current cycles per 1/BASE_EMULATOR_HZ second
extern u64 totalcurrentcycles;	// total # current cycles executed


//	Utility function to make a hex string for machine_state routines
//	If '*hex' is NULL, we xmalloc() space and return a pointer to it,
//	else the buffer at 'hex' should have size*2+1 bytes available.
char	*emulate_bin2hex(u8 *mem, char *hex, int size);

//	Utility for converting hex string to binary.
//	size is the upper limit on the expected size of the
//	string.  If 'hex' is smaller, we zero-fill the memory.
void emulate_hex2bin(char *hex, u8 *mem, int size);

/*
 *	Attempt to RLE compress 'len' bytes of 'data' 
 *	into 'compress' but not using more than 'maxlen' bytes.
 *
 *	Returns length of 'compress' or 0 if compression failed.
 */
int rle_compress(u8 *data, int len, u8 *compress, int maxlen);

/*
 *	Attempt to RLE uncompress 'len' bytes of 'data' 
 *	into 'uncompress' using at most 'maxlen' bytes.
 *
 *	Returns zero if uncompressed data cannot fit, else 
 *	the length of that data.
 */
int rle_uncompress(u8 *data, int len, u8 *uncompress, int maxlen);

void	emulate_setup_for_restore(void);

//	trigger a breakpoint inside v9t9
void	emulate_break(void);

#include "memory.h"

mem_domain emulate_parse_mem_type(const char *typ, bool *rom);
char emulate_stringize_mem_type(mem_domain md);

#include "cexit.h"
#endif

	
