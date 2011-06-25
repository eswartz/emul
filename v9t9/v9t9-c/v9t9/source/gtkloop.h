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
#include "log.h"
#include "command.h"

/*
 *	Helper for sanity checks
 */
#define VALID_WINDOW(w)	((w) && GTK_IS_OBJECT(w) && !GTK_OBJECT_DESTROYED(w))

/*	Global data  */

/*	Window holding V9t9 and command buttons */
extern GtkWidget	*v9t9_window;

/*	Drawing area for v9t9 screen */
extern GtkWidget	*v9t9_drawing_area;

/*	Command center window */
extern GtkWidget	*command_center;

/*	Command log text entry widget */
extern GtkWidget	*v9t9_command_log;


/*	
 *	Send a command to V9t9
 */
void
GTK_send_command(const gchar *text);

/*
 *	Append text to command log
 */
void
GTK_append_log(const gchar *text, GdkColor *color, GdkFont *font);

/*
 *	Get log font
 */
GdkFont *
GTK_get_log_font(void);

/*
 *	Change font used to display log text
 */
void
GTK_change_log_font(char *fontname /*GdkFont *font*/);

/*
 *	Delete all text in the log
 */
void
GTK_flush_log(void);

/*
 *	Call to keyboard_gtk.c module to set a key from a
 *	key press/release event
 */
void   	
GTK_keyboard_set_key(guint key, int onoff);

/*
 *	Passed from system_report_status to debugger frontend
 */
void
gtk_debugger_report_status(status_item item, va_list va);

void
GTK_system_report_status(status_item item, va_list va);

/*
 *	Debugger was enabled or disabled... react
 */
void
GTK_system_debugger_enabled(bool enabled);

/*
 *	Execution was paused or unpaused... react 
 */
void
GTK_system_execution_paused(bool paused);

extern int gtk_run_event_rate;
DECL_SYMBOL_ACTION(gtk_debugger_set_update_rate);

extern gchar *gtk_command_central_font;
DECL_SYMBOL_ACTION(gtk_command_central_set_font);

#include "cexit.h"

