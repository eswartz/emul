/*
  unixmain.c					-- main loop for Un*x

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

#include <signal.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/time.h>
#include <X11/Xlib.h>
#include <stdarg.h>
#include <locale.h>

#include "v9t9_common.h"
#include "system.h"
#include "timer.h"
#include "v9t9.h"
#include "log.h"
#include "command_rl.h"
#include "debugger.h"
#include "moduleconfig.h"
#include "v9t9_module.h"

#if HAVE_GTK
#include <gtk/gtk.h>
#if HAVE_GNOME
#include "gnomeloop.h"
#else
#include "gtkloop.h"
#endif
#include "gtkinterface.h"
#endif


#if HAVE_QTE
#include "qteloop.h"
#endif

#include "unixmain.h"

#define _L	 LOG_INTERNAL | LOG_INFO

static int	unix_v9t9_initialized;
int         console_fd;
int         gdb_debugging = 0;

static void
system_initlog(void);

static void
system_termlog(void);

#if LINUX_SVGA_KEYBOARD && LINUX_SVGA_VIDEO
#define HAVE_SVGA	1
#endif

#if X_WIN_KEYBOARD && X_WIN_VIDEO
#define HAVE_XLIB	1
#endif

enum {
	FE_UNKNOWN,
#if HAVE_SVGA
	FE_SVGA,
#endif
#if HAVE_XLIB
	FE_XLIB,
#endif
#if HAVE_GTK
	FE_GTK,
#endif
#if HAVE_QTE
	FE_QTE,
#endif
}	frontend = FE_UNKNOWN;

int		(*frontend_loop)(void) = 0L;
void	(*frontend_getcommands)(void);
void	(*frontend_log)(u32 srcflags, const char *text) = 0L;
void	(*frontend_report_status)(status_item item, va_list va) = 0L;
void	(*frontend_debugger_enabled)(bool enabled) = 0L;
void	(*frontend_execution_paused)(bool paused) = 0L;

static void
sigsegv(int x)
{
	//v9t9_restop();
#if HAVE_SVGA
	if (linuxKeyboard.runtimeflags & vmRTInUse)
		linuxKeyboard.restop();
#endif
	if (!gdb_debugging) {
		signal(SIGSEGV, SIG_DFL);
		command_logger(_L | LOG_FATAL, _("SIGSEGV caught.\n"));
		abort();
	} else {
		signal(SIGSEGV, SIG_DFL);
		*(char *) 0 = 0;
	}
}

int
main(int argc, char **argv)
{
	Display *display;
	int ret;
	int status;
	int chosen;

	setlocale(LC_ALL, "");
	bindtextdomain(PACKAGE, LOCALEDIR);
	textdomain(PACKAGE);

	// Check for an SVGAlib run -- the program will
	// be SUID root and not under X.
	console_fd = -1;

#if HAVE_GTK || HAVE_XLIB
	/* Check for X connection before assuming GTK
	   will work; it ABORTS if it can't open a connection. */

	if (0 && (display = XOpenDisplay(NULL)) == NULL) {
		console_fd = open("/dev/console", O_RDWR);
	}
#endif

	if (setreuid(-1,getuid()) < 0)
	{
		fprintf(stderr, _("Can't un-set root privileges!\n"));
	}

	v9t9_config(argc, argv);

	unix_v9t9_initialized = 0;

	system_initlog();
	initlog();

	if (argc > 1 && strcmp(argv[1], "--debug") == 0) {
		gdb_debugging = 1;
		argc--;
		argv++;
	}

	if (!gdb_debugging)
	  signal(SIGSEGV, sigsegv);
	else
	  signal(SIGSEGV, SIG_DFL);

#ifdef LINUX_SVGA_VIDEO
	if (argc > 1 && strcmp(argv[1], "--no-video") == 0) {
		svgaVideo.runtimeflags |= vmRTUnselected;
		argc--;
		argv++;
	}
#endif

#ifdef LINUX_SVGA_KEYBOARD
	if (argc > 1 && strcmp(argv[1], "--no-kbd") == 0) {
		linuxKeyboard.runtimeflags |= vmRTUnselected;
		argc--;
		argv++;
	}
#endif

	/* Look for explicit choice of frontend */
	chosen = FE_UNKNOWN;
	if (argc > 2 && strcmp(argv[1], "-fe") == 0) {
#if HAVE_SVGA
		if (strcasecmp(argv[2], "svga") == 0) {
			chosen = FE_SVGA;
			argv += 2;
			argc -= 2;
		} else 
#endif
#if HAVE_XLIB
		if (strcasecmp(argv[2], "xlib") == 0) {
			chosen = FE_XLIB;
			argv += 2;
			argc -= 2;
		} else 
#endif
#if HAVE_GTK
		if (strcasecmp(argv[2], "gtk") == 0) {
			chosen = FE_GTK;
#if HAVE_SVGA
			svgaVideo.runtimeflags |= vmRTUnselected;
			linuxKeyboard.runtimeflags |= vmRTUnselected;
#endif
			argv += 2;
			argc -= 2;
		} else
#endif
#if HAVE_QTE
		if (strcasecmp(argv[2], "qte") == 0) {
			chosen = FE_QTE;
#if HAVE_SVGA
			svgaVideo.runtimeflags |= vmRTUnselected;
			linuxKeyboard.runtimeflags |= vmRTUnselected;
#endif
			argv += 2;
			argc -= 2;
		} else
#endif
		{
			static char *legalstrings = 
				"none"
#if HAVE_SVGA
				   "|svga"
#endif
#if HAVE_XLIB
				   "|xlib"
#endif
#if HAVE_GTK
				   "|gtk"
#endif
#if HAVE_QTE
				"|qte"
#endif
				;
			logger(_L|LOG_USER|LOG_ERROR, _("Unknown option '%s %s';\n"
					"expected one of %s\n"),
					argv[1], argv[2], legalstrings);
			return 1;
		}
	}

	argv[0] = v9t9_argv[0];
	v9t9_argv = argv;
	v9t9_argc = argc;

#if HAVE_GTK || HAVE_XLIB
	/* Check for X connection before assuming GTK
	   will work; it ABORTS if it can't open a connection. */

	if ((display = XOpenDisplay(NULL)) == NULL) {
#if HAVE_SVGA
		if (chosen == FE_UNKNOWN) 
		{
			logger(_L|LOG_USER, _("X11 not detected, using SVGAlib\n"));
			chosen = FE_SVGA;
		}
		else if (chosen != FE_SVGA)
#endif
		{
			logger(_L|LOG_FATAL, _("X11 not detected, cannot use specified frontend\n"));
		}
	} else {
		XrmInitialize();
		XCloseDisplay(display);
		
		/* revoke uid privileges so GTK doesn't abort */
		setresuid(getuid(),getuid(),getuid());
		setresgid(getgid(),getgid(),getgid());
	}
#endif

#if HAVE_GTK
	if ((chosen == FE_UNKNOWN || chosen == FE_GTK) && 
		GTK_system_init()) {
		frontend = FE_GTK;
		frontend_log = GTK_system_log;
		frontend_getcommands = GTK_system_getcommands;
		frontend_report_status = GTK_system_report_status;
		frontend_debugger_enabled = GTK_system_debugger_enabled;
		frontend_execution_paused = GTK_system_execution_paused;
		frontend_loop = GTK_system_loop;

#if HAVE_SVGA
		svgaVideo.runtimeflags |= vmRTUnselected;
		linuxKeyboard.runtimeflags |= vmRTUnselected;
#endif
#if HAVE_XLIB
		X_Video.runtimeflags |= vmRTUnselected;
		X_Keyboard.runtimeflags |= vmRTUnselected;
#endif
#if HAVE_QTE
		QTE_Video.runtimeflags |= vmRTUnselected;
		QTE_Keyboard.runtimeflags |= vmRTUnselected;
#endif
		logger(_L|LOG_USER, _("Using GTK\n"));
		v9t9_window = create_v9t9_window();
#if HAVE_GNOME2
		v9t9_drawing_area = g_object_get_data(G_OBJECT(v9t9_window),
												"v9t9_drawing_area");
#else
		v9t9_drawing_area = gtk_object_get_data(GTK_OBJECT(v9t9_window),
												"v9t9_drawing_area");
#endif
		gtk_widget_show(v9t9_window);
	} else 
#endif

#if HAVE_QTE
	if ((chosen == FE_UNKNOWN || chosen == FE_QTE) && 
		QTE_system_init()) {
		frontend = FE_QTE;
		frontend_log = unix_system_log;
		frontend_getcommands = unix_system_getcommands;
		frontend_report_status = unix_system_report_status;
		frontend_debugger_enabled = unix_system_debugger_enabled;
		frontend_execution_paused = unix_system_execution_paused;
		frontend_loop = QTE_system_loop;

#if HAVE_SVGA
		svgaVideo.runtimeflags |= vmRTUnselected;
		linuxKeyboard.runtimeflags |= vmRTUnselected;
#endif
#if HAVE_XLIB
		X_Video.runtimeflags |= vmRTUnselected;
		X_Keyboard.runtimeflags |= vmRTUnselected;
#endif
#if HAVE_GTK
		gtkVideo.runtimeflags |= vmRTUnselected;
		gtkKeyboard.runtimeflags |= vmRTUnselected;
#endif
	 	logger(_L|LOG_USER, _("Using QT/Embedded\n"));
	} else 
#endif

#if HAVE_XLIB
	if ((chosen == FE_UNKNOWN || chosen == FE_XLIB) && 
			   xlib_system_init()) {
		frontend = FE_XLIB;
		frontend_log = unix_system_log;
		frontend_getcommands = unix_system_getcommands;
		frontend_report_status = unix_system_report_status;
		frontend_loop = xlib_system_loop;
		frontend_debugger_enabled = unix_system_debugger_enabled;
		frontend_execution_paused = unix_system_execution_paused;

#if HAVE_SVGA
		svgaVideo.runtimeflags |= vmRTUnselected;
		linuxKeyboard.runtimeflags |= vmRTUnselected;
#endif
#if HAVE_GTK
		gtkVideo.runtimeflags |= vmRTUnselected;
		gtkKeyboard.runtimeflags |= vmRTUnselected;
#endif
#if HAVE_QTE
		QTE_Video.runtimeflags |= vmRTUnselected;
		QTE_Keyboard.runtimeflags |= vmRTUnselected;
#endif
		logger(_L|LOG_USER, _("Using Xlib\n"));
	} else 
#endif
#if HAVE_SVGA
	if ((chosen == FE_UNKNOWN || chosen == FE_SVGA) && 
			   svga_system_init()) {
		frontend = FE_SVGA;
		frontend_log = unix_system_log;
		frontend_getcommands = unix_system_getcommands;
		frontend_report_status = unix_system_report_status;
		frontend_loop = svga_system_loop;
		frontend_debugger_enabled = unix_system_debugger_enabled;
		frontend_execution_paused = unix_system_execution_paused;

#if HAVE_XLIB
		X_Video.runtimeflags |= vmRTUnselected;
		X_Keyboard.runtimeflags |= vmRTUnselected;
#endif
#if HAVE_GTK
		gtkVideo.runtimeflags |= vmRTUnselected;
		gtkKeyboard.runtimeflags |= vmRTUnselected;
#endif
#if HAVE_QTE
		QTE_Video.runtimeflags |= vmRTUnselected;
		QTE_Keyboard.runtimeflags |= vmRTUnselected;
#endif

		logger(_L|LOG_USER, _("Using SVGAlib\n"));
	} else 
#endif
	{
		logger(_L|LOG_FATAL, _("Could not find a frontend\n"));
		return 2;
	}

	if (chosen != FE_UNKNOWN && chosen != frontend) {
		logger(_L|LOG_FATAL, _("Could not use specified frontend\n"));
		return 2;
	}

	if (!v9t9_init())
		return 3;

	if (!v9t9_restart())
		return 4;

	signal(SIGINT, v9t9_sigint);
	signal(SIGTERM, v9t9_sigterm);

	unix_v9t9_initialized = 1;
	ret = frontend_loop();

	v9t9_restop();

	v9t9_term(ret);

	termlog();
	system_termlog();

	wait(&status);
	exit(ret);
}


void
unix_system_pause(void)
{
	/* Recommended way from glibc info pages
	   for waiting until a certain signal arrives. */
	sigset_t    mask, oldmask;

	sigemptyset(&mask);
	sigaddset(&mask, SIGINT);
	sigprocmask(SIG_BLOCK, &mask, &oldmask);
	sigsuspend(&oldmask);
	sigprocmask(SIG_UNBLOCK, &mask, NULL);
}

void
system_getcommands(void)
{
	if (frontend_getcommands)
		frontend_getcommands();
	else
		unix_system_getcommands();
}

// this is a dicey routine... when GTK is running,
// we can't really stop the emulator properly

void
unix_system_getcommands(void)
{
	FILE       *cmdsin = fopen("/dev/tty", "r");
	FILE       *cmdsout = fopen("/dev/tty", "w");

	logger(_L|LOG_INFO, _("Entering interactive mode\n"));
	if (cmdsin == NULL || cmdsout == NULL || feof(cmdsin)) {
		logger(_L|LOG_ERROR, _("FAILED: could not open terminal\n"));
		return;
	}

	v9t9_restop();
	
	readline_getcommands(cmdsin, cmdsout);
		
	v9t9_restart();
}

static struct itimerval my_timer;
unsigned int TM_Ticked;

static void unix_system_timer_handler(int unused)
{
	TM_Ticked++;
	if (TM_Ticked > 10) TM_Ticked = 10;
	if (!(stateflag & ST_PAUSE)) stateflag |= ST_STOP;
}

void
system_timer_init(void)
{
	TM_Ticked = 0;
}

void
system_timer_install(void)
{
#if HAVE_QTE
	if (frontend == FE_QTE)
	{
		QTE_start_timer();
	}
	else
#endif
	{
	struct sigaction s;

	s.sa_handler = unix_system_timer_handler;
	sigemptyset(&s.sa_mask);
	s.sa_flags = SA_RESTART;
	s.sa_restorer = NULL;

	sigaction(SIGALRM, &s, NULL);

	my_timer.it_value.tv_sec = 0;
	my_timer.it_value.tv_usec = 1000000 / TM_HZ;	/* Hz */
	my_timer.it_interval.tv_sec = 0;
	my_timer.it_interval.tv_usec = 1000000 / TM_HZ;

	setitimer(ITIMER_REAL, &my_timer, NULL);
	}
}

void
system_timer_uninstall(void)
{
#if HAVE_QTE
	if (frontend == FE_QTE)
	{
		QTE_stop_timer();
	}
	else
#endif
	{
	struct itimerval t;

	/* turn off and save current timer */
	memset((void *) &t, 0, sizeof(t));

	setitimer(ITIMER_REAL, &t, &my_timer);
	}
}

//  We emit text to both stdout and a logfile.

static FILE *logfile;			// to disk
static FILE *loguser;			// to tty

static void
system_initlog(void)
{
	if ((logfile = fopen("log.unix.txt", "w")) == NULL) {
		fprintf(stderr, _("Could not create log file\n"));
		exit(1);
	}
	if ((loguser = fdopen(1, "w")) == NULL) {
		fprintf(stderr, _("Could not create user log file\n"));
	}
	setbuf(logfile, NULL);
	if (loguser) setbuf(loguser, NULL);
	atexit(system_termlog);
}

static void
system_termlog(void)
{
	if (logfile) fclose(logfile);
	logfile = 0;
	if (loguser) fclose(loguser);
	loguser = 0;
}

void
system_log(u32 srcflags, const char *text)
{
	fwrite(text, 1, strlen(text), logfile);
	fflush(logfile);

	if (LOG_IS_VISIBLE(srcflags)
		&& !unix_v9t9_initialized)
	{
		fwrite(text, 1, strlen(text), stdout);
		fflush(stdout);
	}

	if (frontend_log)
		frontend_log(srcflags, text);
}

void
unix_system_log(u32 srcflags, const char *text)
{
	if (loguser && LOG_IS_VISIBLE(srcflags)) {
		/*
		const char *cptr = strchr(text, ':');

		if (!cptr || (srcflags & LOG_SRC_MASK) == 0)
			cptr = text;
		else
			cptr += 2;
		if (*text == '\n' && cptr != text)
			fwrite("\n", 1, 1, loguser);
		fwrite(cptr, 1, strlen(cptr), loguser);
		*/
		fwrite(text, 1, strlen(text), loguser);
		fflush(loguser);
	}
}


#define ITEM_ON_CODE_LINE(item) \
		((item) >= STATUS_CPU_PC && \
		(item) <= STATUS_CPU_INSTRUCTION_LAST && \
		 (item) != STATUS_CPU_REGISTER_VIEW)

void
system_report_status(status_item item, va_list va)
{
	char buffer[1024], *bptr = buffer+1;
	static bool last_item_on_code_line;
	bool item_on_code_line;

	report_status_text(item, va, bptr, sizeof(buffer)-1);

	if (*bptr)
	{
		item_on_code_line = ITEM_ON_CODE_LINE(item);
		if (last_item_on_code_line != item_on_code_line) {
			buffer[0] = '\n';
			bptr--;
		}

		system_log(LOG_USER, bptr);

		last_item_on_code_line = item_on_code_line;
	}

	if (frontend_report_status)
		frontend_report_status(item, va);
}

void
unix_system_report_status(status_item item, va_list va)
{
}

void
system_debugger_enabled(bool enabled)
{
	if (frontend_debugger_enabled)
		frontend_debugger_enabled(enabled);
}

void
unix_system_debugger_enabled(bool enabled)
{
	if (enabled)
	{
		int level = log_level(LOG_CPU);
		if (!level)
			log_set_level(LOG_CPU, -1);
	}
}

void
system_execution_paused(bool paused)
{
	if (frontend_execution_paused)
		frontend_execution_paused(paused);
}

void
unix_system_execution_paused(bool paused)
{
	logger(LOG_USER, _("Execution is %s\n"), 
		   paused  ? _("paused") : _("resumed"));
}
