/*
  xlibloop.c					-- main loop for Xlib frontend

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

#include <signal.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/time.h>

#include "Xv9t9.h"

#include "v9t9_common.h"
#include "system.h"
#include "timer.h"
#include "v9t9.h"
#include "log.h"

#include "command_rl.h"
#include "moduleconfig.h"
#include "v9t9_module.h"

#include "unixmain.h"

#if X_WIN_VIDEO && X_WIN_KEYBOARD

#define _L	 LOG_INTERNAL | LOG_INFO

static OSThread command_thread;

#include "xlibresource.h"

#define SIZEOF_OPS	4
XrmOptionDescRec xlib_opTable[SIZEOF_OPS] = {
	 {"-geometry", 	".geometry", 	XrmoptionSepArg, (XPointer) NULL},
	 {"-xrm", 		NULL, 			XrmoptionResArg, (XPointer) NULL},
	 {"-display", 	".display", 	XrmoptionSepArg, (XPointer) NULL},
	 {"-iconic", 	"*iconStartup", XrmoptionNoArg,  (XPointer) NULL},
};

XrmDatabase xlib_rDB;

extern Display *x11_dpy;
extern Window vwin;
extern int  x11_screen;

void xlib_get_resources(const char *appname, XrmDatabase cmdlineDB)
{
	XrmDatabase homeDB, serverDB, appDB;
	char filenamebuf[1024];
	char *str;

	/*	Get merged resource database  */

	/*	Read app-defaults... */
	strcpy(filenamebuf, "/usr/lib/X11/app-defaults/");
	strcat(filenamebuf, appname);
	appDB = XrmGetFileDatabase(filenamebuf);
	if (appDB) XrmMergeDatabases(appDB, &xlib_rDB);

	/*	Read server defaults... */
	if ((str = XResourceManagerString(x11_dpy)) != NULL) {
		serverDB = XrmGetStringDatabase(str);
	} else {
		/* Open .Xdefaults file */
		strcpy(filenamebuf, (str = getenv("HOME")) ? str : ".");
		strcat(filenamebuf, "/.Xdefaults");
		serverDB = XrmGetFileDatabase(filenamebuf);
	}
	if (serverDB) XrmMergeDatabases(serverDB, &xlib_rDB);

	/*	Get local defaults for server */
	if ((str = getenv("XENVIRONMENT")) == NULL) {
		int len;
		strcpy(filenamebuf, (str = getenv("HOME")) ? str : ".");
		strcat(filenamebuf, "/.Xdefaults-");
		len = strlen(filenamebuf);
		gethostname(filenamebuf+len, sizeof(filenamebuf)-len);
		str = filenamebuf;
	}
	homeDB = XrmGetFileDatabase(str);
	if (homeDB) XrmMergeDatabases(homeDB, &xlib_rDB);

	/*	Command line is priority */
	if (cmdlineDB) XrmMergeDatabases(cmdlineDB, &xlib_rDB);
}

static void *xlib_command_thread(void *unused)
{
	while (!feof(stdin)) {
		readline_getcommand(stdin, stdout);
	}
}

/*
 *	Initialize what we can and return 1 if we are to use
 *	the xlib loop.
 */	
int
xlib_system_init(void)
{
	XrmDatabase cmdlineDB = 0;
	XrmValue    value;
	char 		*display;
	char        *str_type[20];

	/*  Read command-line args */
	//!!! XLib sucks up shorter arguments if they match a prefix!  Yuck!
	XrmParseCommand(&cmdlineDB, xlib_opTable, SIZEOF_OPS, 
					OS_GetFileNamePtr(v9t9_argv[0]), 
					&v9t9_argc, v9t9_argv);

	/* Initialize X context */
	if (XrmGetResource(cmdlineDB, "v9t9.display", "V9t9.Display",
						str_type, &value)) {
		display = (char *)value.addr;
	} else {
		display = NULL;
	}

	if ((x11_dpy = XOpenDisplay(display)) == NULL) {
		logger(_L | LOG_USER, _("Cannot connect to X server '%s'\n"),
			XDisplayName(display));
		return 0;
	}

	xlib_get_resources(OS_GetFileNamePtr(v9t9_argv[0]), cmdlineDB);

	x11_screen = DefaultScreen(x11_dpy);

	// create thread for commands
	if (OS_CreateThread(&command_thread, xlib_command_thread, 0L) != OS_NOERR)
	{
		logger(LOG_ERROR|LOG_USER, _("Cannot create command thread\n"));
		return 0;
	}

	return 1;
}

int
xlib_system_loop(void)
{
	XEvent      Event;
	int			ret;

	OS_ResumeThread(command_thread);

	while (1) {
		while (TM_Ticked) {
			TM_TickHandler(0);
//			TM_Ticked--;
			TM_Ticked = 0;
		}
		while (XCheckWindowEvent(x11_dpy, vwin, ~0, &Event)) {
			logger(_L | L_2, _("Event = %d\n\n\n"), Event.type);
			x_handle_kbd_event(&Event);
			x_handle_video_event(&Event);
		}
//		XSync(x11_dpy, False);

		ret = v9t9_execute();
		if (ret == em_TooFast)
			unix_system_pause();
		else if (ret == em_Quitting || ret == em_Dying)
			break;
	}

	OS_KillThread(command_thread, true);

	return ret == em_Dying;
}

#endif	// X_WIN_VIDEO && X_WIN_KEYBOARD
