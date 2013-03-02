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

#ifndef __9901_H__
#define __9901_H__

#include "v9t9_types.h"
#include "sound.h"
#include "command.h"

#include "centry.h"

/*	9901 stuff 	*/

int		setup_9901(void);
void	reset9901(void);

void	trigger9901int(u16 mask);	// set specific interrupt level
void	reset9901int(u16 mask);		// reset specific interrupt level
u16		read9901int(void);			// get level of most important interrupt 

void	handle9901tick(void);		// keyed off 3.0 MHz / 64 clock

void	setclockmode9901(u8 onoff);	// when accessing bit > 15 from CRU

#define M_INT_EXT	2	// peripheral
#define M_INT_VDP 	4	// VDP
#define M_INT_CLOCK 8	// clock

typedef struct hw9901state
{
/*	9901 stuff 	*/
	u16         int9901;		/* interrupt enable mask */
	u16         currentints;	/* current pending interrupts */
	u16         intlevel;		/* current top-level interrupt */

	u8          clockmode;		/* 0=ints enabled, 1=programming clock */

	u16         clockinvl;	/* clock interval, 0 to >3FFF */
	u16         timer;		/* countdown timer value (from clockinvl9901 -> 0) */

	u16         latchedtimer;	/* timer latch -- inverted */
	u16         latchedclockinvl;	/* clock interval latch -- inverted */

}	hw9901state;

extern AudioGate   audiogate;		/* audio gate info */

extern hw9901state hw9901;

DECL_SYMBOL_ACTION(hw9901_machine_state);

#include "cexit.h"

#endif
