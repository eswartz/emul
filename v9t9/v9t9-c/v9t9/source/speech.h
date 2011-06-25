
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

#ifndef __SPEECH_H__
#define __SPEECH_H__

#ifndef STANDALONE
#include "command.h"
#endif

#include "centry.h"

struct	tms5200
{
	u8	status;		/* as returned via read-status */
#define	SS_TS	0x80	/* talk status */
#define SS_BL	0x40	/* buffer low */
#define	SS_BE	0x20	/* buffer empty */
#define SS_SPEAKING 1	/* internal */

	u8	gate;		/* how do we route Writes and Reads? */
#define GT_WCMD	0x1		/* write -> command */
#define GT_WDAT	0x2		/* write -> speech external data */
#define	GT_RSTAT 0x4	/* read -> status */
#define GT_RDAT	0x8		/* read -> data */	

	u32	addr;		/* address -- 20 bits */

	u8	fifo[16];	/* fifo buffer */
	u8	out,in;		/* ptrs.  out==in --> empty */
	u8	len;		/* # bytes in buffer */
	
	u8	bit;		/* bit offset of whatever we're reading */
	u8	bits;		/* # bits read */
	
	u8	command;	/* last command */
	u8	data;		/* data register for reading */	
	
	u8	timeout;	

	int addr_pos;	/* for debugger: position of address (0=complete) */

};


#ifndef STANDALONE
extern	char	speechromfilename[64];

extern 	void	speech_memory_init(void);

extern 	u16		speech_mmio_get_addr(void);
extern 	void	speech_mmio_set_addr(u16 addr);
extern	bool 	speech_mmio_addr_is_complete(void);

extern	void	speech_mmio_write(u8 val);
extern	s8		speech_mmio_read(void);
#endif


#ifndef STANDALONE

#define SPEECHABLE (FE_SPEECH|FE_PLAYSPEECH)
#define PLAYSPEECH ((features&SPEECHABLE)==SPEECHABLE)

int	speech_preconfiginit(void);
int	speech_postconfiginit(void);
int speech_restart(void);
void speech_restop(void);
void speech_shutdown(void);

void	speech_intr(void);

/* demo callbacks */
void	speech_demo_init(void);
void	speech_demo_stop(void);
void	speech_demo_term(void);
void	speech_demo_push(u8 val);

DECL_SYMBOL_ACTION(speech_machine_state);

#endif

#include "cexit.h"

#endif
