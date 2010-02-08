
/*
  qteloop.c						-- main loop for Qt/Embedded frontend

  (c) 1994-2002 Edward Swartz

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

#include <config.h>

#if defined(UNDER_UNIX)
#include "unixmain.h"
#else
#include "winv9t9.h"
#endif
#include "v9t9_common.h"

#include "system.h"
#include "timer.h"
#include "v9t9.h"
#include "command.h"
#include "log.h"
#include "qteloop.h"

#include "command_rl.h"
#include "moduleconfig.h"
#include "v9t9_module.h"

#include <qpe/qpeapplication.h>
#include <qpe/resource.h>
#include <qtimer.h>

#define _L	 LOG_INTERNAL | LOG_INFO

QPEApplication *mainApp;
QteHandler *mainHandler;
int v9t9_return;

QteHandler::QteHandler(QWidget *parent, const char *name, WFlags fl)
	: QMainWindow(parent, name, fl), timerid(0), idleid(0)
{
	if (!name) setName("V9t9");
	setCaption("V9t9");
	setIcon(Resource::loadPixmap("v9t9"));
	resize(256, 192);
	lay = new QVBoxLayout(this);
}

void QteHandler::installTimer()
{
	timerid = startTimer(1000/TM_HZ);
	idleid = startTimer(0);
}

void QteHandler::uninstallTimer()
{
	killTimer(timerid);
	killTimer(idleid);
}

void QteHandler::timerEvent(QTimerEvent *e)
{
	int ret;

	if (e->timerId() == timerid)
	{
		//printf("timerid %d\n",TM_GetTicks());
		TM_Ticked++;
		if (!(stateflag & ST_PAUSE)) stateflag |= ST_STOP;
		return;
	}
	
	if (e->timerId() == idleid)
	{
		//printf("idleid %d\n",TM_GetTicks());

		OS_LockMutex(&command_mutex);
		if (TM_Ticked>10) TM_Ticked=10;

		while (TM_Ticked) {
			TM_TickHandler(0);
			TM_Ticked=0;
		}

		ret = v9t9_return = v9t9_execute();
		//printf("ret=%d (%d)\n",ret, TM_GetTicks());

		if (ret == em_TooFast) {
			// just exit
		}
		else if (ret == em_Quitting || ret == em_Dying) {
			mainApp->exit(0);
		}
		OS_UnlockMutex(&command_mutex);
		return;
	}
}

void QTE_start_timer(void)
{
	if (mainHandler)
		mainHandler->installTimer();
}

void QTE_stop_timer(void)
{
	if (mainHandler)
		mainHandler->uninstallTimer();
}


int
QTE_system_init(void)
{
	mainApp = new QPEApplication(v9t9_argc, v9t9_argv);
	if (!mainApp)
		return 0;
	mainHandler = new QteHandler();
	return 1;
}

int
QTE_system_loop(void)
{
	// this freezes the window in place, which is bad due to 
	// conflicting size of keyboard and display

	//mainApp->showMainDocumentWidget(mainHandler);
	mainApp->showMainWidget(mainHandler);

	//mainApp->setMainWidget(mainHandler);
	//mainHandler->show();

	mainApp->setGlobalMouseTracking(true);
	mainApp->exec();
	return v9t9_return == em_Dying;
}
