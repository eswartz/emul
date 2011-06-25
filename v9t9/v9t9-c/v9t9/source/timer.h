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

#ifndef __TIMER_H__
#define __TIMER_H__

#include "centry.h"

extern	int	TM_Installed;			/* 1 if timer going */

int		TM_Init(void);
void	TM_Kill(void);
int		TM_Start(void);
void	TM_TickHandler(int unused);
int		TM_Stop(void);
int		TM_GetTicks(void);

int		TM_UniqueTag(void);

//	ticks = hz * TM_HZ
int		TM_SetEvent(int tag,int ticks,int start,int flags,volatile void *flag);
int		TM_ResetEvent(int tag);

/*	flags for TM_SetEvent:	*/

#define	TM_REPEAT	1		/* repeat event */
#define	TM_FUNC		2		/* 'void *flag' is actually void (*flag)() */

typedef void (*tm_timer_func)(void);
#define TM_EVENT_FUNC(x)	(volatile void *)(x)

#if defined(UNDER_WIN32)
#define TM_HZ	100
#else
#define TM_HZ 	100			/* base clock freq */
#endif


#include "cexit.h"

#endif
