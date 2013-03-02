/*
lpc.h

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

#include "v9t9_types.h"

#include "centry.h"

extern void LPCinit();
extern void LPCstop();

int 
LPCavailable() ;
void
LPCreadEquation(u32 (*LPCfetch)(int), int forceUnvoiced);
void
LPCexec(s16 *data, u32 length);


/**	Do LPCavailable, LPCreadEquation, and LPCfetch.
 * @return 1 to continue, 0 if end of frame */
extern int LPCframe(u32 (*LPCfetch)(int count), s8 *data, u32 length);

extern void* LPCallocState();
extern int LPCstateSize();
extern void LPCgetState(void *data);
extern void LPCsetState(void *data);
extern void LPCfreeState(void *data);

#include "cexit.h"
