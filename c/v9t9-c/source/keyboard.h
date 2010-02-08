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

#include "centry.h"

int keyboard_preconfiginit(void);
int	keyboard_postconfiginit(void);
int keyboard_restart(void);
void keyboard_restop(void);

void keyboard_setkey(int onoff,u8 shift,u8 key);
int keyboard_isset(u8 shift,u8 key);

//	Joy = 1 or 2, x=-1,0,1, y=-1,0,1, fire=0,1

enum
{
	JOY_X = 1,	// set X-axis
	JOY_Y = 2,	// set Y-axis
	JOY_B = 4	// set buttons
};

void	keyboard_setjoyst(int joy, int mask, int x, int y, int fire);

// organized by columns, rows are in 1 << row
extern	u8	crukeyboardmap[8];
extern	u8	caps,crukeyboardcol,AlphaLock;
extern	u8	realshift;	/*	'real' shift keys being held down,
							as opposed to those being faked */

extern int keyboard_soft_keys, keyboard_soft_reset_rate;

void
keyboard_soft_reset(void);

//extern	u16	specialkey;	

/*	Map of ASCII codes and their direct CRU mapping
	(high nybble=row, low nybble=column), except for 0xff,
	which should be faked. */
extern	u8 latinto9901[128];

/*	This macro tells us whether an ASCII code has a direct mapping
	to a 9901 keyboard matrix location (stored in latinto9901[]).
	The '/' character is special, since its 99/4A shifted value ('-') is not
	the same as the standard keyboard's shifted value ('?'). 
	(This is important when we are using a host keyboard module that
    allows us to know the unshifted value of a pressed key.)
*/

#define ASCII_DIRECT_TO_9901(x) (latinto9901[x] != 0xff && (x) != '/')

#define SETKBDCRU(r,c) crukeyboardmap[c]|=(0x80>>(r))
#define RESETKBDCRU(r,c) crukeyboardmap[c]&=~(0x80>>(r))
#define TESTKBDCRU(r,c) (crukeyboardmap[c]&(0x80>>(r)))
#define TOGGLEKBDCRU(r,c) crukeyboardmap[c]^=(0x80>>(r))

#define CHANGEKBDCRU(r,c,v) do { if (v) SETKBDCRU(r,c); else RESETKBDCRU(r,c); } while (0)


#define SHIFT_R 2
#define SHIFT_C 0
#define FCTN_R 3
#define FCTN_C 0
#define CTRL_R 1
#define CTRL_C 0

#define JOY1_C 6
#define JOY2_C 7

#define JOY_FIRE_R 7
#define JOY_LEFT_R 6
#define JOY_RIGHT_R 5
#define JOY_DOWN_R 4
#define JOY_UP_R 3

//	j=1 or 2
#define CHANGEJOYCRU(j,r,v) CHANGEKBDCRU(r, JOY1_C+(j)-1,v)

#if RENAME_SHIFTS
#define SHIFT_M 1
#define FCTN_M 2
#define CTRL_M 4
#else
#define SHIFT 1
#define FCTN 2
#define CTRL 4
#endif

#include "cexit.h"

