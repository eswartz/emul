/*
  gtkloop.c						-- main loop for GTK frontend

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

#include <config.h>
#if __MWERKS__ && _WIN32
#include <sys/signal.h>
// buggy cygwin sys/time.h 
#define _WINSOCK_H
#else
#include <signal.h>
#endif
#if UNDER_UNIX
#include "unixmain.h"
#else
#include "winv9t9.h"
#endif
#include "v9t9_common.h"

#include <fcntl.h>
#include <unistd.h>
#include <sys/time.h>
#include <sys/stat.h>

#include <gtk/gtk.h>

#include "gtkinterface.h"

#include "gtkcallbacks.h"
#include "gtkloop.h"
#include "system.h"
#include "timer.h"
#include "v9t9.h"
#include "command.h"
#include "log.h"

#include "command_rl.h"
#include "moduleconfig.h"
#include "v9t9_module.h"

#define _L	 LOG_INTERNAL | LOG_INFO

/*	globals */

/*	window containing v9t9 and command buttons */
GtkWidget		*v9t9_window;

/*	Drawing area for v9t9 screen */
GtkWidget		*v9t9_drawing_area;

/*	Command log text entry widget */
GtkWidget		*v9t9_command_log;

/* Use this function to set the directory containing installed pixmaps. */
extern void
add_pixmap_directory                   (const gchar     *directory);


#if GTK_VIDEO && GTK_KEYBOARD

static int v9t9_return;

static int v9t9_execute_tag, v9t9_execute_flag;

GtkWidget *command_center;

/*
 *	This is where we actually do the stuff in GTK_system_loop.
 */
static gint gtk_idle_handler(gpointer *data)
{
	int ret;

//	g_print("Timer = %d\n", TM_GetTicks());

	OS_LockMutex(&command_mutex);

	while (TM_Ticked) {
		TM_TickHandler(0);
//			TM_Ticked--;
		TM_Ticked = 0;
	}

	ret = v9t9_return = v9t9_execute();
		
	if (ret == em_TooFast) {
#if UNDER_UNIX		
		unix_system_pause();	
#else
		win32_system_pause();
#endif						
	}
	else if (ret == em_Quitting || ret == em_Dying) {
		gtk_main_quit();
		return FALSE;
	}
	OS_UnlockMutex(&command_mutex);

	// continue to call me...  :)
	return TRUE;
}

#if UNDER_UNIX
#include "Xv9t9.h"
#include <gdk/gdkx.h>
#include "xlibresource.h"

extern Display *x11_dpy;
extern int x11_screen;

static int
GTK_get_initial_size(void)
{
	int vwxoff, vwyoff, vwxsz, vwysz;

	int dpy_xsize, dpy_ysize;
	XSizeHints *vwin_size_hints; 
	XrmDatabase cmdlineDB = 0;
	char       *str_type;
	XrmValue    value;
	int         gravity;
	bool		user_geometry;
	int			flags;
	int			i;

	/*  Read command-line args */
	XrmParseCommand(&cmdlineDB, xlib_opTable, SIZEOF_OPS, 
					OS_GetFileNamePtr(v9t9_argv[0]), 
					&v9t9_argc, v9t9_argv);

	/*  Get hints for -display parsing */
	x11_dpy = GDK_DISPLAY();
	x11_screen = DefaultScreen(x11_dpy);

	dpy_xsize = DisplayWidth(x11_dpy, x11_screen);
	dpy_ysize = DisplayHeight(x11_dpy, x11_screen);

	if ((vwin_size_hints = XAllocSizeHints()) == NULL) {
		logger(_L|LOG_ERROR | LOG_USER, _("cannot allocate size hints\n"));
		return 0;
	}

	vwin_size_hints->flags = PMinSize | PMaxSize | PResizeInc | PAspect | PBaseSize;
	vwin_size_hints->base_width = 0;
	vwin_size_hints->base_height = 0;
	vwin_size_hints->min_width = 256;
	vwin_size_hints->min_height = 192;
	vwin_size_hints->max_width = dpy_xsize;
	vwin_size_hints->max_height = dpy_ysize;
	vwin_size_hints->width_inc = 256;
	vwin_size_hints->height_inc = 192;
	vwin_size_hints->min_aspect.x = vwin_size_hints->max_aspect.x = 4;
	vwin_size_hints->min_aspect.y = vwin_size_hints->max_aspect.y = 3;

	/* Read sizes from resource */

	xlib_get_resources(OS_GetFileNamePtr(v9t9_argv[0]), cmdlineDB);

	if (!XrmGetResource(xlib_rDB, "v9t9.geometry", "V9t9.Geometry",
						&str_type, &value)) {
		user_geometry = false;
		value.addr = 0L;
	} else {
		user_geometry = true;
	}

	/*	Parse geometry specification  */

	if ((flags = XWMGeometry(x11_dpy, x11_screen,
					(char *) value.addr,
					"1x1",
					1 /* border width */ , vwin_size_hints,
					&vwxoff, &vwyoff, &vwxsz, &vwysz, &gravity))) {

		if (vwxsz >= 256*256 && vwysz >= 192*192) {
			// assume they misunderstood the geometry and scale down
			vwxsz /= 256;
			vwysz /= 192;
		}
	}

	GTK_x_mult = vwxsz / 256;
	GTK_y_mult = vwysz / 192;
	GTK_user_size_configured = 1;
	
	// on_v9t9_draw_area_size_request uses this info
	return 1;
}

#elif UNDER_WIN32

static void
GTK_get_initial_size(void)
{

}

#endif

int
GTK_system_init(void)
{
	struct stat st;
	command_symbol_table *gtkcommands =
		command_symbol_table_new(_("GTK Options"),
								 _("These commands control the GTK interface"),

    	 command_symbol_new("GTKDebuggerUpdateRate",
    						_("Set the number of times per second "
							  "that the debugger refreshes in 'Run' mode"),
							c_STATIC,
    						gtk_debugger_set_update_rate /* action */ ,
    						RET_FIRST_ARG,
    						command_arg_new_num
    						(_("rate"),
    						 _("update rate in hertz"),
    						 NULL /* action */ ,
							 ARG_NUM(gtk_run_event_rate),
    						 NULL /* next */ )
    						,

    	 command_symbol_new("GTKCommandCentralFont",
    						_("Set the font used for the 'Command Central' window"),
							c_STATIC,
    						gtk_command_central_set_font /* action */ ,
    						RET_FIRST_ARG,
    						command_arg_new_string
    						(_("font"),
    						 _("font string in X11 format, i.e., "
								 "'-adobe-utopia-regular-r-normal-*-*-120-*-*-p-*-iso8859-1'"),
    						 NULL /* action */ ,
							 NEW_ARG_STRBUF(&gtk_command_central_font),
    						 NULL /* next */ )
    						,

				 

			NULL /* next */ )),

    	 NULL /* sub */ ,

    	 NULL	/* next */
		);


	gtk_set_locale();
	gtk_init(&v9t9_argc, &v9t9_argv);

	if (stat(SHAREDIR "/pixmaps", &st) == 0)
		add_pixmap_directory(SHAREDIR "/pixmaps");
	else
		add_pixmap_directory(TOPSRCDIR "/pixmaps");

#if UNDER_WIN32
	if (stat(SHAREDIR "/v9t9.win32.gtkrc", &st) == 0)
		gtk_rc_parse(SHAREDIR "/v9t9.win32.gtkrc");
	else
		gtk_rc_parse(TOPSRCDIR "/v9t9.win32.gtkrc");
#else
	if (stat(SHAREDIR "/v9t9.gtkrc", &st) == 0)
		gtk_rc_parse(SHAREDIR "/v9t9.gtkrc");
	else
		gtk_rc_parse(TOPSRCDIR "/v9t9.gtkrc");
#endif

	command_center = create_command_dialog();
	gtk_widget_show(command_center);

	GTK_get_initial_size();

	command_symbol_table_add_subtable(universe, gtkcommands);

	return 1;
}

int
GTK_system_loop(void)
{
	guint idle_id;
	
	idle_id = gtk_idle_add((GtkFunction) gtk_idle_handler, 0L);

	gtk_main();

	gtk_idle_remove(idle_id);
	return v9t9_return == em_Dying;
}


void
GTK_system_getcommands(void)
{
	if (!execution_paused())
	{
		execution_pause(true);

		logger(LOG_USER, _("\nEmulator is paused; press 'Resume' button to resume\n"));
		stateflag &= ~ST_INTERACTIVE;
	}
}


static int fatal_dialog_button_clicked = 0;
void
on_fatal_dialog_ok_button_clicked      (GtkButton       *button,
                                        gpointer         user_data)
{
	fatal_dialog_button_clicked = 1;
}


void
GTK_system_log(u32 srcflags, const gchar *text)
{
	if (LOG_IS_VISIBLE(srcflags)) {
		GTK_append_log(text, NULL, NULL);
	}
	if (LOG_IS_FATAL(srcflags)) {
		GtkWidget *dialog = create_fatal_dialog();
		GtkLabel *label;

		fatal_dialog_button_clicked = 0;

		// set message
		label = gtk_object_get_data(GTK_OBJECT(dialog), "message_label");
		if (label) {
			gtk_label_set_text(label, text);
		}
	   
		// display modal dialog
		gtk_grab_add(dialog);
		gtk_widget_show(dialog);
		
		OS_UnlockMutex(&command_mutex);

		while (!fatal_dialog_button_clicked && VALID_WINDOW(dialog)) 
			gtk_main_iteration();
		if (VALID_WINDOW(dialog))
			gtk_grab_remove(dialog);
	}
}

void
GTK_system_report_status(status_item item, va_list va)
{
	char buffer[256];
	GtkLabel *label;
	
	label = GTK_LABEL(gtk_object_get_data(GTK_OBJECT(command_center),
										"progress_label"));
	switch (item)
	{
	case STATUS_CYCLES_SECOND:
	{
		int cyc_avg = va_arg(va, int);
		int inst_avg = va_arg(va, int);
		sprintf(buffer, _("Executing %d cycles per second, "
				"%d instructions per second"),
				cyc_avg, inst_avg);
		gtk_label_set_text(label, buffer);
		break;
	}

	case STATUS_FRAMES_SECOND:
	{
		int frames_avg = va_arg(va, int);
		sprintf(buffer, _("Displaying %d frames per second"),
				frames_avg);
		gtk_label_set_text(label, buffer);
		break;
	}

	case STATUS_DISK_ACCESS:
	case STATUS_RS232_ACCESS:
	case STATUS_PIO_ACCESS:
	{
		GtkWidget *w;
		int num = va_arg(va, int);
		int onoff = va_arg(va, int);
		
		w = (item == STATUS_DISK_ACCESS) ?
			gtk_object_get_data(GTK_OBJECT(v9t9_window), "disk_access_drawing_area") :
			gtk_object_get_data(GTK_OBJECT(v9t9_window), "rs232_access_drawing_area") ;
		if (w)
		{
			GdkColor yellow = { 0xe0, 0xe0, 0x40 };
			GdkColor black = { 0x00, 0x00, 0x00 };
			GtkStyle *style = gtk_widget_get_style(v9t9_window);
			GdkGC *gc = gdk_gc_new(v9t9_window->window);
			gdk_gc_set_foreground(gc, onoff ? &style->fg[4] : &style->bg[0]);
			gdk_draw_rectangle(w->window, 
							   gc,
							   true /*filled*/,
							   0, 0,
							   w->allocation.width, w->allocation.height);
//			gtk_style_unref(style);
//			g_print("%d\n", onoff);
			gdk_gc_unref(gc);
		}
		break;
	}

	case STATUS_DEBUG_REFRESH:
	case STATUS_CPU_PC:
	case STATUS_CPU_STATUS:
	case STATUS_CPU_WP:
	case STATUS_CPU_REGISTER_VIEW:
	case STATUS_CPU_REGISTER_READ:
	case STATUS_CPU_REGISTER_WRITE:
	case STATUS_CPU_INSTRUCTION:
	case STATUS_CPU_INSTRUCTION_LAST:
	case STATUS_MEMORY_VIEW:
	case STATUS_MEMORY_READ:
	case STATUS_MEMORY_WRITE:
		gtk_debugger_report_status(item, va);
		break;

	default:
		logger(LOG_USER|LOG_ERROR, _("Unhandled event %d!"), item);
		break;
	}
}

/***********************/

/*
 *	Get log font
 */
GdkFont *
GTK_get_log_font(void)
{
	GtkStyle *style;
	if (GTK_OBJECT(v9t9_command_log)->flags & GTK_DESTROYED)
		return 0L;
	if (!v9t9_command_log) return 0L;
	style = gtk_widget_get_style(v9t9_command_log);
	return style->font;
}

/*
 *	Change font used to display log text, NULL means reset to default
 */
void
GTK_change_log_font(gchar *fontname)
{
#if 0 // old GTK
	GtkRcStyle *rcstyle = gtk_rc_style_new();
#else
	GtkStyle *rcstyle = gtk_style_new(); 
#endif	

	if (GTK_OBJECT(v9t9_command_log)->flags & GTK_DESTROYED) {
		g_free(rcstyle);
		return;
	}

	if (fontname && *fontname) {
		printf("FONT: %s\n", fontname);
#if 0 // old GTK
		rcstyle->font_name = fontname;
		gtk_widget_modify_style(v9t9_command_log, rcstyle);
#else
		rcstyle->font = gdk_font_load(fontname);
		gtk_widget_set_style(v9t9_command_log, rcstyle);
#endif		
	} else {
		// this is probably too harsh for a cancel
		gtk_widget_restore_default_style(v9t9_command_log);
	}
//	g_free(rcstyle);
}

/*
 *	Delete all text in the log
 */
void
GTK_flush_log(void)
{
	if (!(GTK_OBJECT(v9t9_command_log)->flags & GTK_DESTROYED))
		gtk_editable_delete_text(GTK_EDITABLE(v9t9_command_log), 
								 0, -1);
}

/*
 *	Append text to command log
 */
void
GTK_append_log(const gchar *text, GdkColor *color, GdkFont *font)
{
	if (!(GTK_OBJECT(v9t9_command_log)->flags & GTK_DESTROYED))
		gtk_text_insert(GTK_TEXT(v9t9_command_log), 
						font, 
						color, 
						NULL, 
						text, 
						strlen(text));
}

/*	
 *	Send a command to V9t9
 */
void
GTK_send_command(const gchar *text)
{
	GdkColor red = { 0, 0x8000, 0x0000, 0x0000 };

	GTK_append_log(text, &red, NULL);
	GTK_append_log("\n", &red, NULL);

	OS_LockMutex(&command_mutex);
	command_exec_text((char *)text);
	OS_UnlockMutex(&command_mutex);
}


#endif	// GTK_VIDEO && GTK_KEYBOARD

