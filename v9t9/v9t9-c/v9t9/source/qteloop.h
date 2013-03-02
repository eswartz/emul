/*
qteloop.h

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
#ifndef __QTELOOP_H__
#define __QTELOOP_H__

#include "qtehandler.h"

#ifdef __cplusplus

extern QteHandler *mainHandler;

class QKeyEvent;
class QMouseEvent;

extern int QTE_handle_key(QKeyEvent *e, int down);
extern int QTE_handle_mouse(QMouseEvent *e, int pressrelease, int move);

#endif	// __cplusplus

#include "centry.h"

int QTE_system_init(void);
int QTE_system_loop(void);

void QTE_start_timer(void);
void QTE_stop_timer(void);

#include "cexit.h"

#endif
