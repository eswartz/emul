/*
  timer.c						-- generic timer list functions

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

#include <stdlib.h>
#include <stdio.h>

#include "v9t9_common.h"
#include "timer.h"
#include "system.h"

#define _L	 LOG_TIMER | LOG_INFO

static int  timer_installed;

/*************************************************************************/

static int  ticks;			/* number of ticks that have passed */
static int  lasttag;		/* unique tag for timer events */

#define	TM_MAXEVENTS	64

#define	NOTAG		0

typedef void (*FUNC) (void);

struct timerrec {
	 u32         	tag;			/* user-defined ID, 0 means unused */
	 u32         	flags;
	 s32         	max, counter;	/* /100 = # times per second */
	 volatile void *flag;			/* function or flag addr */
} TM_Events[TM_MAXEVENTS];

static struct timerrec *
TM_FindFreeEvent(void)
{
	struct timerrec *follow = TM_Events;
	int         c = TM_MAXEVENTS;

	while (follow->tag != NOTAG && c) {
		follow++;
		c--;
	}

	if (!c)
		return NULL;
	else
		return follow;
}


static void
TM_FreeEvent(struct timerrec *which)
{
	which->tag = NOTAG;
}


/*-----------------------------------------------------------------------*/

/*
	Handle a tick event (called TM_HZ times a second), possibly
	by TM_TickHandler, or by the main program.
*/

void
TM_TickHandler(int unused)
{
	struct timerrec *nw;

	if (log_level(LOG_TIMER) >= 3) {
		logger(_L | L_3, _("tick... %d\n"), ticks);
		nw = TM_Events;
		while (nw < TM_Events + TM_MAXEVENTS) {
			if (nw->tag)
				logger(_L | L_3, "[ %d.%d ] -> ", nw->tag, nw->counter);
			nw++;
		}
		logger(_L | L_3, " [=]\n");
	}

	ticks++;					/* update ticks */

	nw = TM_Events;

	while (nw < TM_Events + TM_MAXEVENTS) {
		if (nw->tag != NOTAG) {
			nw->counter += TM_HZ;	/* >tick< */
			if (nw->counter >= nw->max) {	/* went off? */
				logger(_L | L_1, _("executing event with id=%d\n"), nw->tag);
				if (nw->flags & TM_FUNC)
					((FUNC) nw->flag) ();	/* call rout */
				else
					*(u8 *) (nw->flag) = 1;	/* whoo hoo! */

				/*  repeat?     */
				if (nw->flags & TM_REPEAT) {
					nw->counter -= nw->max;
				} else {
					logger(_L | L_1, _("freeing event with id=%d\n\n"), nw->tag);
					TM_FreeEvent(nw);
				}
			}
		}
		nw++;
	}

}

/*
	Initialize the timer functions.
*/

int
TM_Init(void)
{
	memset(TM_Events, 0, sizeof(TM_Events));
	ticks = 0;

	system_timer_init();

	atexit(TM_Kill);

	return 1;
}

/*	
	Turn on the timer. 
*/
int
TM_Start(void)
{
	if (timer_installed)
		return 0;

	system_timer_install();
	timer_installed = 1;
	return 1;
}


/*
	Turn off the timer.
*/

int
TM_Stop(void)
{
	if (!timer_installed)
		return 0;

	system_timer_uninstall();
	timer_installed = 0;

	return 1;
}


void
TM_Kill(void)
{
	TM_Stop();
}



int
TM_GetTicks(void)
{
	return ticks;
}

int
TM_UniqueTag(void)
{
	return ++lasttag;
}


/*
	Insert a event into the event chain.

	flags & TM_REPEAT --> automatically reschedule timer event
	flags & TM_FUNC	 --> call a function instead of setting a flag
*/

int
TM_SetEvent(int tag, int ticks, int start, int flags, volatile void *flag)
{
	struct timerrec *nw;
	int         restart;

	if (tag == NOTAG)
		return 0;

	nw = TM_FindFreeEvent();
	if (nw == NULL) {
		logger(_L | LOG_FATAL, _("\nTimer event list full\n"));
		return 0;
	}

	restart = timer_installed;
	TM_Stop();

	nw->flags = flags;
	nw->tag = tag;
	nw->flag = flag;
	nw->max = ticks;
	nw->counter = start;

	if (log_level(LOG_TIMER) >= 2) {
		struct timerrec *step;

		logger(_L | L_2, _("TM_SetEvent:\n"));
		step = TM_Events;
		while (step < TM_Events + TM_MAXEVENTS) {
			if (step->tag != NOTAG)
				logger(_L | L_2 |  0, "[ %d.%d ] -> ", step->tag,
					 step->counter);
			step++;
		}
		logger(_L | L_2, " [=]\n");
	}

	if (restart)
		TM_Start();

	return 1;
}



int
TM_ResetEvent(int tag)
{
	struct timerrec *step;
	int         restart;

	restart = timer_installed;
	TM_Stop();

	logger(_L | L_1, "TM_ResetEvent %d\n\n\n", tag);

	step = TM_Events;

	while (step < TM_Events + TM_MAXEVENTS && step->tag != tag) {
		step++;
	}

	if (step >= TM_Events + TM_MAXEVENTS) {
		if (restart)
			TM_Start();
		return 0;
	} else {
		TM_FreeEvent(step);
	}

	if (restart)
		TM_Start();
	return 1;
}
