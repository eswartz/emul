/*
  winmain.c						-- system loop for Win32

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

/*
#error in disk dialog, need to quote string c:\tidisk\disk
#error move win32 code into winloop.c again
#error figure out why the startup problems sometimes
#error why do we need to reparent?
#error upon parse error, force reset of parser (escapes and such appear to cause this)
#error propagate closes (v9t9_window)
#error be sure to use correct log
*/

#include "winv9t9.h"
#include "v9t9_common.h"

#include <gtk/gtk.h>
#include "gtkloop.h"
#include "gtkinterface.h"

#include "log.h"
#include "9900.h"
#include "memory.h"
#include "vdp.h"
#include "timer.h"
#include "v9t9.h"
#include "video.h"
#include "system.h"
#include "command.h"
#include "resource.h"
#include "moduleconfig.h"
#include "command_rl.h"

#include "video_win32.h"

#define _L	 LOG_INFO | LOG_HOSTOS

static int  _argc_;
static char **_argv_;

extern vmModule win32DirectDrawVideo, win32DrawDibVideo;
extern vmModule win32KbdModule;

// used by directsound
HWND        hWndApp;
HWND        hWndWindow, hWndStatus;	// our windows
HWND		hWndScreen;				// DD window
//HACCEL		hAccelerators;			// key accelerators

HINSTANCE	myHInst, myHPreInst;	// startup params for video modules
int			mynCmdShow;			

static FILE *logfile;
static HANDLE conout;

static void
system_initlog(void);

static void
system_termlog(void);

int		(*frontend_loop)(void) = 0L;
void	(*frontend_getcommands)(void);
void	(*frontend_log)(u32 srcflags, const char *text) = 0L;
//void	(*frontend_statusline)(int line, const char *text) = 0L;
void	(*frontend_report_status)(status_item item, va_list va) = 0L;
void	(*frontend_debugger_enabled)(bool enabled) = 0L;
void	(*frontend_execution_paused)(bool paused) = 0L;


static void 
MakeARGV(char *cmd)
{
	int         quoting = 0;
	char       *ptr = cmd;
	char       *arg = NULL;

	while (*ptr && isspace(*ptr))
		ptr++;

	_argc_ = 0;
	_argv_ = 0;

	_argv_ = (char **) xmalloc(2 * sizeof(char *));

	_argv_[_argc_++] = "v9t9";

	while (*ptr) {
		if (arg == NULL) {
			_argv_ =

				(char **) xrealloc(_argv_, (_argc_ + 2) * sizeof(char *));
			arg = _argv_[_argc_] = ptr;
			_argc_++;
		}

		if (quoting && *ptr == '\"') {
			ptr++;
			quoting = 0;
		} else if (quoting)
			*arg++ = *ptr++;
		else if (*ptr == '\"') {
			ptr++;
			quoting = 1;
		} else if (*ptr == ' ') {
			*arg = 0;
			arg = NULL;
			ptr++;
		} else
			*arg++ = *ptr++;
	}
	if (arg)
		*arg = 0;
	_argv_[_argc_] = 0;
}


int win_frontend;

int WINAPI
WinMain(HINSTANCE hInst,		/*Win32 entry-point routine */
		HINSTANCE hPreInst, LPSTR lpszCmdLine, int nCmdShow)
{
	int ret;

	win_frontend = FE_UNKNOWN;
		
	system_initlog();
	initlog();

	if (!win_createBitmap())
		return vmInternalError;

	MakeARGV(lpszCmdLine);

#define argc v9t9_argc
#define argv v9t9_argv

	v9t9_config(_argc_, _argv_);

	/* Look for explicit choice of win_frontend */
	if (argc > 2 && strcmp(argv[1], "-fe") == 0) {
#if HAS_DDRAW
		if (strcasecmp(argv[2], "ddraw") == 0) {
			win_frontend = FE_DDRAW;
			argv += 2;
			argc -= 2;
		} else 
#endif
#if HAS_DRAWDIB
		if (strcasecmp(argv[2], "drawdib") == 0 ||
			strcasecmp(argv[2], "win32") == 0) {
			win_frontend = FE_DRAWDIB;
			argv += 2;
			argc -= 2;
		} else 
#endif
#if HAS_GTK
		if (strcasecmp(argv[2], "gtk") == 0) {
			win_frontend = FE_GTK;
			argv += 2;
			argc -= 2;
		} else
#endif
		{
			static char *legalstrings = 
#if HAS_DDRAW
				   "ddraw|"
#endif
#if HAS_DRAWDIB
				   "win32|"
#endif
#if HAS_GTK
				   "gtk"
#endif
				;
			logger(_L|LOG_USER|LOG_ERROR, _("Unknown option '%s %s';\n"
					"expected one of %s \n"),
					argv[1], argv[2], legalstrings);
			return 1;
		}
	}

#if HAS_GTK && HAS_DRAWDIB
	if ((win_frontend == FE_UNKNOWN || win_frontend == FE_GTK) && 
		GTK_system_init()) {
		char *var;
		
		win_frontend = FE_GTK;
		frontend_log = GTK_system_log;
		frontend_getcommands = win32_system_getcommands;
		frontend_report_status = GTK_system_report_status;
		frontend_debugger_enabled = GTK_system_debugger_enabled;
		frontend_execution_paused = GTK_system_execution_paused;
		frontend_loop = win32_gtk_system_loop;

#if HAS_DDRAW
		win32DirectDrawVideo.runtimeflags |= vmRTUnselected;
#endif
#if HAS_DRAWDIB
//		win32DrawDibVideo.runtimeflags |= vmRTUnselected;
#endif

		// why why why does the GTK win32 interface suck so bad?
		// I got over the video speed problems but finally
		// hit a dead end when finding that the keyboard events
		// are always on/off right in succession, even with the
		// key held down... sigh...
		gtkVideo.runtimeflags |= vmRTUnselected;
		gtkKeyboard.runtimeflags |= vmRTUnselected;
		
		// don't ask me why, but the presence of this
		// variable causes a MASSIVE startup delay
		var = getenv("HOME");
		if (var) *var = 0;
		SetEnvironmentVariable("HOME", NULL);
		
		logger(_L|LOG_USER, _("Using GTK\n"));

#if 1
		v9t9_window = create_v9t9_window();
		v9t9_drawing_area = gtk_object_get_data(GTK_OBJECT(v9t9_window),
												"v9t9_drawing_area");
// don't show this since it's empty!  (drawdib puts up separate window)
//		gtk_widget_show(v9t9_window);
#endif		
	} else 
#endif
#if HAS_DRAWDIB
	if ((win_frontend == FE_UNKNOWN || win_frontend == FE_DRAWDIB) && 
			   win32_system_init()) {
		win_frontend = FE_DRAWDIB;
		frontend_log = win32_system_log;
		frontend_getcommands = win32_system_getcommands;
		frontend_report_status = win32_system_report_status;
		frontend_debugger_enabled = win32_system_debugger_enabled;
		frontend_execution_paused = win32_system_execution_paused;
		frontend_loop = win32_system_loop;

#if HAS_DDRAW
		win32DirectDrawVideo.runtimeflags |= vmRTUnselected;
		//win32Keyboard.runtimeflags |= vmRTUnselected;
#endif
#if HAS_GTK
		gtkVideo.runtimeflags |= vmRTUnselected;
		gtkKeyboard.runtimeflags |= vmRTUnselected;
#endif
		logger(_L|LOG_USER, _("Using DrawDib\n"));
	} else 
#endif
#if HAS_DDRAW
	if ((win_frontend == FE_UNKNOWN || win_frontend == FE_DDRAW) && 
			   win32_system_init()) {
		win_frontend = FE_DDRAW;
		frontend_log = win32_system_log;
		frontend_getcommands = win32_system_getcommands;
		frontend_report_status = win32_system_report_status;
		frontend_debugger_enabled = win32_system_debugger_enabled;
		frontend_execution_paused = win32_system_execution_paused;
		frontend_loop = win32_system_loop;

#if HAS_DRAWDIB
		win32DrawDibVideo.runtimeflags |= vmRTUnselected;
		//win32Keyboard.runtimeflags |= vmRTUnselected;
#endif
#if HAS_GTK
		gtkVideo.runtimeflags |= vmRTUnselected;
		gtkKeyboard.runtimeflags |= vmRTUnselected;
#endif

		logger(_L|LOG_USER, _("Using DirectDraw\n"));
	} else 
#endif
	{
		logger(_L|LOG_FATAL, _("Could not find a frontend\n"));
		return 2;
	}

#if HAS_GTK
	if (win_frontend == FE_GTK)
	{
		gtk_text_freeze(GTK_TEXT(v9t9_command_log));
	}
#endif

	if (!v9t9_init())
		return 3;

#if HAS_GTK
	if (win_frontend == FE_GTK)
	{
		gtk_text_thaw(GTK_TEXT(v9t9_command_log));
	}
#endif

	if (!v9t9_restart())
		return 4;


#undef argc
#undef argv

	ret = frontend_loop();

	v9t9_restop();

	v9t9_term(ret);

	termlog();
	system_termlog();

	PostQuitMessage(0);
	exit(ret);
	return TRUE;
}

////////////////
int 
win32_system_init(void)
{
	return 1;
}

int  TM_Ticked;			/* we can't do jack inside a callback */

int v9t9_return;

int
win32_system_loop(void)
{
	int ret;
	MSG         lpMsg;

	while (1) {
	#if USE_MM_TIMER || USE_WAITABLE_TIMER
		while (TM_Ticked) {
			TM_TickHandler(0);
//			TM_Ticked--;
			TM_Ticked = 0;
		}
	#endif
	
		lpMsg.message = WM_NULL;
		while (PeekMessage(&lpMsg, NULL, 0, 0, PM_REMOVE)) {
			if (lpMsg.message == WM_DESTROY || lpMsg.message == WM_CLOSE) 
				return 1;
				
			TranslateMessage(&lpMsg);
			DispatchMessage(&lpMsg);
		}
			
		ret = v9t9_execute();
		if (ret == em_TooFast)
			win32_system_pause();
		else if (ret == em_Quitting || ret == em_Dying)
			break;
	}
	return ret == em_Dying;
}

extern gint gtk_idle_handler(gpointer *data);


static int 
win32_system_idle(gpointer *data)
{
	MSG         lpMsg;
	int ret;

#if USE_MM_TIMER || USE_WAITABLE_TIMER
	while (TM_Ticked /*1||(++TM_Ticked & 63)==0*/) {
	//	logger(LOG_USER, ".");
		TM_TickHandler(0);
		TM_Ticked--;
	}
#endif

		/* window paints, keyboard messages */
	while (PeekMessage(&lpMsg, NULL, 0, 0, PM_REMOVE)) {
	//	logger(LOG_USER, "-");
		TranslateMessage(&lpMsg);
		DispatchMessage(&lpMsg);
	}
		
	ret = v9t9_return = v9t9_execute();
	if (ret == em_TooFast)
		win32_system_pause();
	else if (ret == em_Quitting || ret == em_Dying) {
		gtk_main_quit();
		return FALSE;
	}
	
	return TRUE;
}

int
win32_gtk_system_loop(void)
{
	guint idle_id;

	idle_id = gtk_idle_add((GtkFunction) win32_system_idle, 0L);
	
	gtk_main();

	gtk_idle_remove(idle_id);

	gtk_widget_destroy(v9t9_window);	
	return v9t9_return == em_Dying;
}


/////////

char        statuslines[8][160];
void
win_StatusLine(int which, char *text)
{
#if 0
	strcpy(statuslines[which], text);
	REDRAWSTATUS;
#endif
}

////////////////

// in Timer.c
extern HANDLE hTimerTickEvent;

/*	The value '1' is almost perfect.  
	Don't try zero -- that makes v9t9 hog the processor and the OS
	responds slowly and sluggishly. */
void
win32_system_pause(void)
{
	WaitForSingleObject(hTimerTickEvent, 1);
	//TM_Ticked = 1;
}

/************************************************/

#if USE_MESSAGE_TIMER

#define TIMERCODE 0x1

static UINT timer;
static DWORD tm;

static void CALLBACK
myHandleTimer(HWND hWnd, UINT uMsg, UINT idEvent, DWORD dwTime)
{
	if (idEvent == TIMERCODE) {
		static DWORD lastcalled, lasttm;

		if (!lastcalled)
			lastcalled = dwTime;

		/*  this loop is necessary since the timer event is
		   not at all prioritizable, and we can miss tons of
		   messages (on an unburdened Win98 box, it never goes
		   faster than 10Hz!)
		 */
		while (lastcalled + 10 < dwTime) {
			tm++;
			lastcalled += 10;
			TM_TickHandler(0);
		}
	}
}

/*
static void
TakeABreak(void)
{
	stateflag |= ST_STOP;
}
*/
void
system_timer_init(void)
{
	//TM_SetEvent(TM_UniqueTag(), TM_HZ*100/10, 0, TM_FUNC|TM_REPEAT, TakeABreak);
}

void
system_timer_install(void)
{
	timer = SetTimer(win_GetWindow(), TIMERCODE, 1000 / TM_HZ, myHandleTimer);	/* 100 Hz */
}


void
system_timer_uninstall(void)
{
	KillTimer(win_GetWindow(), timer);
}

#elif USE_MM_TIMER

#define TARGET_RESOLUTION (1000/TM_HZ)	/* 100 Hz */
#define TIMERCODE 0x1

static UINT timer;
static UINT timerRes;
static DWORD tm;

HANDLE      hTimerTickEvent;

static void CALLBACK
myHandleTimer(UINT uID, UINT uMsg, DWORD dwUser, DWORD dw1, DWORD dw2)
{
	if (uID == timer) {
		TM_Ticked++;
		if (TM_Ticked > 10) TM_Ticked = 10;

		if (!(stateflag & ST_PAUSE)) stateflag |= ST_STOP;
		tm++;

		SetEvent(hTimerTickEvent);
	}
}

void
system_timer_init(void)
{
	TIMECAPS    tc;

	logger(_L|L_2, _("timer init\n"));
	if (timeGetDevCaps(&tc, sizeof(TIMECAPS)) != TIMERR_NOERROR) {
		logger(_L | LOG_FATAL, _("Could not get timer caps\n"));
		ExitThread(-1);
	}

	timerRes = min(max(tc.wPeriodMin, TARGET_RESOLUTION), tc.wPeriodMax);
	logger(_L|L_2,_("timerRes = %d\n"), timerRes);
}

void
system_timer_install(void)
{
	timeBeginPeriod(timerRes);

	logger(_L|L_2, _("timer install\n"));
	timer = timeSetEvent(timerRes, 1, myHandleTimer, 0, TIME_PERIODIC);
	if (timer == 0) {
		logger(_L | LOG_ERROR, _("Could not set timer to %d ms\n\n"), timerRes);
		ExitThread(-1);
	}
	hTimerTickEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
	if (hTimerTickEvent == 0) {
		logger(_L | LOG_ERROR, _("Could not create timer event\n"));
		ExitThread(-1);
	}

}


void
system_timer_uninstall(void)
{
	logger(_L|L_2, _("timer uninstall\n"));
	timeKillEvent(timer);
	timeEndPeriod(timerRes);
}

#elif USE_WAITABLE_TIMER

#define TIMERCODE 0x1

static HANDLE hTimer;
static DWORD tm;

static VOID APIENTRY
myHandleTimer(LPVOID lpArgToCompletionRoutine,
			  DWORD dwTimerLowValue, DWORD dwTimerHighValue)
{
//  log(_("ticked\n"));
	TM_Ticked++;
	if (TM_Ticked > 10) TM_Ticked = 10;
	if (!(stateflag & ST_PAUSE)) stateflag |= ST_STOP;
	tm++;
}

void
system_timer_init(void)
{
	hTimer = CreateWaitableTimer(NULL, TRUE, NULL);
	if (hTimer == 0) {
		fatal(_("Could not create timer\n"));
	}
}

void
system_timer_install(void)
{
	LARGE_INTEGER pDue;

	pDue.QuadPart = -100;
	if (!SetWaitableTimer
		(hTimer, (LARGE_INTEGER *) & pDue, 10, myHandleTimer, 0, 0))
		fatal(_("Could not set timer to 10 ms"));
}


void
system_timer_uninstall(void)
{
	CancelWaitableTimer(hTimer);
}

#endif

void
system_getcommands(void)
{
	if (frontend_getcommands)
		frontend_getcommands();
	else
		win32_system_getcommands();
}

void
win32_system_getcommands(void)
{
//  SetForegroundWindow(hWndStatus);

	FILE       *in = fopen("CONIN$", "r");
	FILE		*out = fopen("CONOUT$", "w");
	
	logger(_L | L_0, _("Entering interactive mode\n\n"));

	if (!in || feof(in) || !out) {
		logger(_L | LOG_USER | LOG_ERROR, _("FAILED: could not open stdin/stdout\n\n"));
		return;
	}

	v9t9_restop();

	readline_getcommands(in, out);

	v9t9_restart();
}


static void
system_initlog(void)
{
	if (logfile == NULL) {
		COORD       buf;
		SMALL_RECT  rect;

		logfile = fopen("log.win.txt", "w");
		if (logfile == NULL)
			FAIL(_("could not open log.win.txt\n"));
			
		// this may fail, or not, depending on what runtime we use
		AllocConsole();
		
		if ((conout = GetStdHandle(STD_OUTPUT_HANDLE)) ==
			INVALID_HANDLE_VALUE) FAIL(_("Could not get stdout for console\n"));
		SetConsoleTitle(_("V9t9 Log"));
		SetConsoleMode(conout, ENABLE_LINE_INPUT | ENABLE_ECHO_INPUT |
					   ENABLE_PROCESSED_INPUT | ENABLE_PROCESSED_OUTPUT |
					   ENABLE_WRAP_AT_EOL_OUTPUT);
		rect.Left = 0;
		rect.Right = 80;
		rect.Top = 0;
		rect.Bottom = 25;
		SetConsoleWindowInfo(conout, true, &rect);
		buf.X = 80;
		buf.Y = 25;
		SetConsoleScreenBufferSize(conout, buf);
	}

}

static void
system_termlog(void)
{
	if (logfile) {
		fclose(logfile);
		FreeConsole();
	}
}

//  emit text to log (do not add newline)
void
win32_system_log(u32 srcflags, const char *text)
{
	int         len = strlen(text);
	DWORD       writ;

	if (LOG_IS_VISIBLE(srcflags)) {
		WriteFile(conout, text, len, &writ, NULL);
	}
}

void
system_log(u32 srcflags, const char *text)
{
	if (logfile) {
		fwrite(text, 1, strlen(text), logfile);
		fflush(logfile);
	}

	if (frontend_log)
		frontend_log(srcflags, text);
}

/*
//  show debugger line
static void
system_statusline(int line, const char *text)
{
	if (logfile) {
		if (!line)
			fputs("\n", logfile);
		fputs(text, logfile);
		fputs("\n", logfile);
	}

	if (frontend_statusline)
		frontend_statusline(line, text);
}

void
win32_system_statusline(int line, const char *text)
{
	DWORD writ;
	if (!line)
		WriteFile(conout, "\r\n", 2, &writ, NULL);
	WriteFile(conout, text, strlen(text), &writ, NULL);
	WriteFile(conout, "\r\n", 2, &writ, NULL);
}
*/

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
win32_system_report_status(status_item item, va_list va)
{
}

void
system_execution_paused(bool paused)
{
	if (frontend_execution_paused)
		frontend_execution_paused(paused);
}

void
win32_system_execution_paused(bool paused)
{
	logger(LOG_USER, _("Execution is %s\n"), 
		   paused  ? _("paused") : _("resumed"));
}

void
system_debugger_enabled(bool enabled)
{
	if (frontend_debugger_enabled)
		frontend_debugger_enabled(enabled);
}

void
win32_system_debugger_enabled(bool enabled)
{
}

/*	this is used when not using GTK... a callback for the menu bar. */

int
win_command(HWND hWnd, WORD cmd)
{
	logger(_L | L_1, _("win_command: %d\n\n"), cmd);
	switch (cmd) {
	case ID_FILE_EXIT:
		v9t9_term(0);
		break;

	case ID_MODULES_LOADMODULE:
		return 0;

	case ID_VIDEO_FULLSCREEN:
	    win_video_switchmodes();
		return 0;

	case ID_EMULATOR_RESETCOMPUTER:
		command_exec_text("Reset\n");
		return 0;

	case ID_COMMANDS_ENTERINTERACTIVEMODE:
		command_exec_text("Interactive=On\n");
		return 0;

	case ID_COMMANDS_RELOADV9T9CNF:
		command_exec_text("#include \"v9t9.cnf\"\n");
		return 0;
		
	case ID_OPEN_CONFIG:
		command_exec_text("#include \"savesession.cnf\"\n");
		return 0;
		
	case ID_SAVE_CONFIG:
		command_exec_text("SaveConfig \"savesession.cnf\"\n");
		return 0;
				
	}
	return 1;
}

