#ifdef HAVE_CONFIG_H
#  include <config.h>
#endif
#include "v9t9_common.h"

#define GTK_ENABLE_BROKEN	// for GtkText...?

#include <gnome.h>
#include <gtk/gtk.h>
#include <gdk/gdkkeysyms.h>

#include "gnomecallbacks.h"
#include "gnomeinterface.h"
#include "gnomesupport.h"

#include "gnome_v99filesel.h"

#include "gnomeloop.h"

#include "v9t9.h"
#include "moduleconfig.h"
#include "moduledb.h"
#include "debugger.h"
#include "dis9900.h"
#include "dsr.h"
#include "vdp.h"
#include "fiad.h"
#include "timer.h"
#include "configfile.h"
#include "9900.h"
#include "command_rl.h"

#define _L LOG_VIDEO
#include "log.h"

#include "gnomecallbacks-v9t9.h"
#include "gnomecallbacks-command.h"
#include "gnomecallbacks-modules.h"
#include "gnomecallbacks-disks.h"
#include "gnomecallbacks-debugger.h"
#include "gnomecallbacks-memory.h"


void
GTK_system_debugger_enabled(bool enabled)
{
	if (enabled) {
		if (!VALID_WINDOW(debugger_window)) {
			debugger_window = create_debugger_window();
			gtk_widget_set_name(debugger_window, "v9t9.debugger");

			debugger_registers_table = gtk_object_get_data(GTK_OBJECT(debugger_window), 
														   "debugger_registers_table");
			debugger_instruction_box = gtk_object_get_data(GTK_OBJECT(debugger_window),
														   "debugger_instruction_box");
			debugger_status_bar = gtk_object_get_data(GTK_OBJECT(debugger_window),
														   "debugger_status_bar");
			debugger_pc_entry = gtk_object_get_data(GTK_OBJECT(debugger_window),
													"debugger_pc_entry");
			debugger_wp_entry = gtk_object_get_data(GTK_OBJECT(debugger_window),
													"debugger_wp_entry");
			debugger_st_entry = gtk_object_get_data(GTK_OBJECT(debugger_window),
													"debugger_st_entry");

			setup_debugger_registers_table();
			setup_debugger_memory_views();
			setup_debugger_instruction_box();
		}
//		debugger_verbose_updates = true;
		gtk_widget_show(debugger_window);
//1		debugger_refresh();
//		debugger();
//		ping_debugger_instruction_box();
	} else {
		// turn off background debugging
		debugger_change_verbosity(true);
		if (debugger_window) {
			execution_pause(false);
			gtk_widget_hide(debugger_window);
			GTK_RESTORE_FOCUS;
		}
	}
}

void
GTK_system_execution_paused(bool paused)
{
	v9t9_window_pause_button = gtk_object_get_data(
		GTK_OBJECT(command_center), "pause_button");

	// remove timer event, if necessary
	if (paused)
		debugger_change_verbosity(true);

	if (v9t9_window_pause_button) {
		GtkWidget *label = GTK_BIN(v9t9_window_pause_button)->child;
		if (!GTK_IS_LABEL(label))
			return;

		if (debugger_enabled()) {
			debugger_register_clear_view();
			debugger_memory_clear_views();
			debugger_instruction_clear_view();
		}

		if (paused) {
			gtk_label_set_text(GTK_LABEL(label), "Resume");
		} else {
			gtk_label_set_text(GTK_LABEL(label), "Pause");
		}
	}
}

/*
 *	Generic routine that enables or disables a widget in user_data
 *	based on the state of the toggle button.
 */
void
on_v9t9_togglebutton_realize_widget_enable (GtkWidget 		  *widget,
										 gpointer         user_data)
{
	g_return_if_fail(GTK_IS_TOGGLE_BUTTON(widget));
	g_return_if_fail(GTK_IS_WIDGET(user_data));
	gtk_widget_set_sensitive(GTK_WIDGET(user_data), 
		 gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(widget)));
}

void
on_v9t9_togglebutton_toggled_widget_enable (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
	g_return_if_fail(GTK_IS_WIDGET(user_data));
	gtk_widget_set_sensitive(GTK_WIDGET(user_data), 
							 gtk_toggle_button_get_active(togglebutton));
}

void
on_v9t9_togglebutton_realize_widget_enable_not (GtkWidget 		  *widget,
										 gpointer         user_data)
{
	g_return_if_fail(GTK_IS_TOGGLE_BUTTON(widget));
	g_return_if_fail(GTK_IS_WIDGET(user_data));
	gtk_widget_set_sensitive(GTK_WIDGET(user_data), 
		 !gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(widget)));
}

void
on_v9t9_togglebutton_toggled_widget_enable_not (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
	g_return_if_fail(GTK_IS_WIDGET(user_data));
	gtk_widget_set_sensitive(GTK_WIDGET(user_data), 
							 !gtk_toggle_button_get_active(togglebutton));
}

/*
 *	Generic routine that toggles the value of the command variable
 *	name in user_data depending on the value of togglebutton.
 */
static void
togglebutton_toggled_command_toggle
                                        (GtkToggleButton *togglebutton,
										 gpointer         user_data,
										 gboolean 		if_active)
{
	gchar *var = (gchar *)user_data;
	gboolean enabled = gtk_toggle_button_get_active(togglebutton);
	char command[256];

	snprintf(command, sizeof(command), "%s %s\n", var, 
			 if_active == enabled ? "on" : "off");
	GTK_send_command(command);
}

/*
 *	Generic routine that toggles the value of the command variable
 *	name in user_data depending on the value of togglebutton.
 */
void
on_v9t9_togglebutton_toggled_command_toggle
                                        (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
	togglebutton_toggled_command_toggle(togglebutton, user_data, true);
}

/*
 *	Generic routine that toggles the value of the command variable
 *	name in user_data depending on the inverted value of togglebutton.
 */
void
on_v9t9_togglebutton_toggled_command_toggle_not
                                        (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
	togglebutton_toggled_command_toggle(togglebutton, user_data, false);
}


/*
 *	Generic routine that sets the value of the togglebutton
 *	based on the value of the command variable name in user_data.
 */
static void
togglebutton_realize_active (GtkWidget       *widget,
								   gpointer         user_data,
								   gboolean			if_active)
{
	GtkToggleButton *tb;
	char *var = (char *)user_data;
	command_symbol *sym;
	int toggle;

	g_return_if_fail(var);
	g_return_if_fail(GTK_IS_TOGGLE_BUTTON(widget));
	tb = GTK_TOGGLE_BUTTON(widget);

	/* Look up the symbol and set its value */
	if (command_match_symbol(universe, var, &sym) &&
		command_arg_get_num(sym->args, &toggle)) 
	{
		// don't call this, or else it triggers the other
		// callback and executes a command...
		//gtk_toggle_button_set_active(tb, if_active == toggle);
		tb->active = (if_active == !!toggle);
	}
	else
	{
		logger(LOG_USER|LOG_FATAL, _("Button mapped to missing option '%s'\n"),
			   var);
	}
}

/*
 *	Generic routine that sets the value of the togglebutton
 *	based on the true value of the command variable name in user_data.
 */
void
on_v9t9_togglebutton_realize_active  (GtkWidget       *widget,
									 gpointer         user_data)
{
	togglebutton_realize_active(widget, user_data, true);
}

/*
 *	Generic routine that sets the value of the togglebutton
 *	based on the false value of the command variable name in user_data.
 */
void
on_v9t9_togglebutton_realize_inactive (GtkWidget       *widget,
									 gpointer         user_data)
{
	togglebutton_realize_active(widget, user_data, false);
}

/*
 *	Generic callback to execute a command if a toggle button
 *	has been clicked and thusly enabled.
 */
void
on_v9t9_togglebutton_clicked            (GtkButton       *button,
                                        gpointer         user_data)
{
	g_return_if_fail(GTK_IS_TOGGLE_BUTTON(button));
	if (gtk_toggle_button_get_active(GTK_TOGGLE_BUTTON(button)))
	{
		GTK_send_command((const gchar *)user_data);
	}
}

static char *
_v9t9_rom_entry_get_filename(gpointer user_data)
{
	char *var, *filename;
	command_symbol *sym;

	var = (char *)user_data;

	/* Look up the symbol and set its value */
	if (command_match_symbol(universe, var, &sym) &&
		command_arg_get_string(sym->args, &filename)) 
	{
		return filename;
	}
	else
	{
		logger(LOG_USER|LOG_FATAL, _("Text entry mapped to missing option '%s'\n"),
			   var);
		return 0L;
	}
}

static void
_v9t9_rom_entry_set_filename(gpointer user_data, char *filename)
{
	char msg[256];

	snprintf(msg, sizeof(msg), "%s \"%s\"\n",
			 (gchar *)user_data, filename);
	GTK_send_command(msg);
}

/*
 *	Generic DSR entry activation callback
 */
void
on_v9t9_rom_entry_activate             (GtkEditable     *editable,
                                        gpointer         user_data)
{
	char *path;
	gchar *copy;
	OSSpec spec;

	if (GTK_WIDGET(editable)->state != GTK_STATE_INSENSITIVE)
	{
		path = gtk_editable_get_chars(editable, 0, -1);

		copy = disk_file_canonicalize(&romspath, false /*directory*/, 
									  path, &spec,
									  true /*add_dir*/);
		if (copy) 
			_v9t9_rom_entry_set_filename(user_data, copy);
		else
			v99_file_selection_complete(disk_file_dialog,
										path);


		g_free(path);
		g_free(copy);
	}
}

/*
 *	Setup the text entry
 */
void
on_v9t9_rom_entry_realize              (GtkWidget       *widget,
                                        gpointer         user_data)
{
	char *filename;

	g_return_if_fail(GTK_IS_ENTRY(widget));

	filename = _v9t9_rom_entry_get_filename(user_data);
	gtk_entry_set_text(GTK_ENTRY(widget), filename ? filename : "");
}

static GtkFileSelection *rom_file_dialog;

static GtkFileSelection *
create_rom_file_selection (void)
{
  GtkWidget *rom_file_selection;
  GtkWidget *ok_button2;
  GtkWidget *cancel_button2;

  rom_file_selection = gtk_file_selection_new (_("Select ROM Filename"));
  gtk_object_set_data( GTK_OBJECT(rom_file_selection), "rom_file_selection", GTK_OBJECT(rom_file_selection));
  gtk_container_set_border_width (GTK_CONTAINER (rom_file_selection), 10);

  ok_button2 = GTK_FILE_SELECTION (rom_file_selection)->ok_button;
  gtk_object_set_data (GTK_OBJECT(rom_file_selection), "ok_button2", GTK_OBJECT(ok_button2));
  gtk_widget_show (ok_button2);
  GTK_WIDGET_SET_FLAGS (ok_button2, GTK_CAN_DEFAULT);

  cancel_button2 = GTK_FILE_SELECTION (rom_file_selection)->cancel_button;
  gtk_object_set_data (GTK_OBJECT(rom_file_selection), "cancel_button2", GTK_OBJECT(cancel_button2));
  gtk_widget_show (cancel_button2);
  GTK_WIDGET_SET_FLAGS (cancel_button2, GTK_CAN_DEFAULT);

  return GTK_FILE_SELECTION(rom_file_selection);
}

/*
 *	user_data is the text entry widget
 */
static void
on_rom_file_ok_button_clicked         (GtkButton       *button,
                                        gpointer         user_data)
{
	const gchar *path;
	gchar *copy;
	GtkEntry *entry;
	OSSpec spec;

	g_return_if_fail(GTK_IS_ENTRY(user_data));

	entry = GTK_ENTRY(user_data);

	path = gtk_file_selection_get_filename(rom_file_dialog);
	copy = disk_file_canonicalize(&romspath, false /*directory*/, 
								  path, &spec,
								  true /*add_dir*/);

	if (copy) gtk_entry_set_text(entry, copy);
	gtk_signal_emit_by_name(GTK_OBJECT(entry), "activate");

	gtk_widget_unref((GtkWidget *)rom_file_dialog);
	rom_file_dialog = 0L;

	g_free(copy);
}

/*
 *	user_data is the DSR filename variable
 */
static void
on_rom_file_cancel_button_clicked     (GtkButton       *button,
                                        gpointer         user_data)
{
	gtk_widget_unref((GtkWidget *)rom_file_dialog);
	rom_file_dialog = 0L;
}

/*
 *	Choose a new entry for the filename.
 *
 *	user_data is the text entry widget
 */
void
on_v9t9_rom_button_clicked             (GtkButton       *button,
                                        gpointer         user_data)
{
	OSSpec spec;
	const char *filename;
	GtkEntry *entry;
	gchar *copy;

	g_return_if_fail(GTK_IS_ENTRY(user_data));

	entry = GTK_ENTRY(user_data);

	if (VALID_WINDOW(rom_file_dialog)) {
		return;
	}

	rom_file_dialog = create_rom_file_selection();

	gtk_widget_show(GTK_WIDGET(rom_file_dialog));
	filename = gtk_entry_get_text(entry);

	copy = disk_file_canonicalize(&romspath, false /*directory*/, 
								  filename, &spec,
								  false /*add_dir*/);

	gtk_file_selection_set_filename(rom_file_dialog, OS_SpecToString1(&spec));
	gtk_file_selection_complete(rom_file_dialog, "*.bin");

	g_free(copy);

	// wire up buttons here (so we can pass known disk number)
	gtk_signal_connect(GTK_OBJECT(rom_file_dialog->ok_button), 
					   "clicked", 
					   GTK_SIGNAL_FUNC(on_rom_file_ok_button_clicked),
					   (gpointer)entry);
	gtk_signal_connect(GTK_OBJECT(rom_file_dialog->cancel_button), 
					   "clicked", 
					   GTK_SIGNAL_FUNC(on_rom_file_cancel_button_clicked),
					   (gpointer)0L);
}


#if 0
#pragma mark -
#endif

/*
 *	Set the value of a spin button from a command.
 */
void
on_v9t9_spin_button_realize_value      (GtkWidget       *widget,
                                        gpointer         user_data)
{
	GtkSpinButton *s;
	command_symbol *sym;
	int val;

	g_return_if_fail(user_data != 0L);
	g_return_if_fail(GTK_IS_SPIN_BUTTON(widget));
	s = GTK_SPIN_BUTTON(widget);

	/* Look up the symbol and set its value */
	if (command_match_symbol(universe, (char *)user_data, &sym) &&
		command_arg_get_num(sym->args, &val)) 
	{
		gtk_spin_button_set_value(s, (gfloat)val);
	}
	else
	{
		logger(LOG_USER|LOG_FATAL, _("Button mapped to missing option '%s'\n"),
			   user_data);
	}
}

/*
 *	User changed value of a spin button.
 *	user_data is the name of the command to set.
 */
void
on_v9t9_spin_button_changed_value      (GtkEditable     *editable,
                                        gpointer         user_data)
{
	char command[256];
	GtkSpinButton *s;

	g_return_if_fail(user_data != 0L);
	g_return_if_fail(GTK_IS_SPIN_BUTTON(editable));
	s = GTK_SPIN_BUTTON(editable);

	snprintf(command, sizeof(command), "%s %d\n", (char *)user_data, 
			 gtk_spin_button_get_value_as_int(s));

//	GTK_send_command(command);
	// don't do that; it may deadlock
	command_exec_text(command);
}

/*
 *	Clicked a button that affects the value of another widget.
 */
void
on_v9t9_button_clicked_realize_widget  (GtkButton       *button,
                                        gpointer         user_data)
{
	GtkWidget *w;
	g_return_if_fail(GTK_IS_WIDGET(user_data));

	w = GTK_WIDGET(user_data);

	// why can't we realize the widget again?
	gtk_widget_hide(w);
	gtk_widget_show(w);
}

#if 0
#pragma mark -
#endif

V99FileSelection *config_file_dialog;

/*
 *	user_data is 0 for saving, != 0 for loading
 */
static void
on_config_file_ok_button_clicked         (GtkButton       *button,
                                        gpointer         user_data)
{
	const char *path;
	OSSpec spec;
	OSError err;
	bool loading = (ptrdiff_t)user_data == GTK_QUICK_LOAD;

	path = v99_file_selection_get_filename(config_file_dialog);
	err = OS_MakeFileSpec(path, &spec);
	if (err != OS_NOERR 
		|| (loading && (err = OS_Status(&spec)) != OS_NOERR)) {
		logger(_L|LOG_ERROR|LOG_USER, _("Could not resolve filename '%s' (%s)\n"), 
			   path, OS_GetErrText(err));
		return;
	}
	
	if ((ptrdiff_t)user_data == GTK_QUICK_LOAD ?
		config_load_spec(&spec, true /*session*/) :
		config_save_spec(&spec, true /*session*/)) 
	{	
		gtk_widget_hide((GtkWidget *)config_file_dialog);
		GTK_RESTORE_FOCUS;
	}
}


static void
on_config_file_cancel_button_clicked     (GtkButton       *button,
                                        gpointer         user_data)
{
	gtk_widget_hide((GtkWidget *)config_file_dialog);
	GTK_RESTORE_FOCUS;
}

/*
 *	Change directory by selecting an entry in the pathlist
 */
static void 
on_config_file_path_list_select_row	(GtkWidget      *clist,
									 gint            row,
									 gint            column,
									 GdkEventButton *event,
									 gpointer        data )
{
	gchar *text;
	gchar *wild;
	gchar sep[] = { G_DIR_SEPARATOR, 0 };

	/* Get the directory selected */
	gtk_clist_get_text(GTK_CLIST(clist), row, column, &text);

	#warning ???
/*
	if (strcmp(text, ".") == 0) {
		text = OS_PathSpecToString1(&v9t9_datadir);
	}
*/
	wild = g_strconcat(text, sep, "*.cnf", 0L);
	v99_file_selection_complete(V99_FILE_SELECTION(config_file_dialog), wild);
	g_free(wild);
}

/*
 *	user_data is 0 for saving, != 0 for loading
 */
void
on_v9t9_quick_load_save_button_clicked      (GtkButton       *button,
											 gpointer         user_data)
{
	GtkCList *clist;
	int paused = execution_paused();
	if (!user_data) execution_pause(1);

	if (VALID_WINDOW(config_file_dialog)) {
		gtk_widget_destroy((GtkWidget *)config_file_dialog);
	}

	config_file_dialog = V99_FILE_SELECTION(create_disk_file_selection(
		(ptrdiff_t)user_data == GTK_QUICK_SAVE
		? _("Save Session File") 
		: _("Load Session File")));

	v99_file_selection_set_file_list_active(config_file_dialog, TRUE);

	// wire up buttons here
	gtk_signal_connect(GTK_OBJECT(config_file_dialog->ok_button), 
					   "clicked", 
					   GTK_SIGNAL_FUNC(on_config_file_ok_button_clicked),
					   user_data);
	gtk_signal_connect(GTK_OBJECT(config_file_dialog->cancel_button), 
					   "clicked", 
					   GTK_SIGNAL_FUNC(on_config_file_cancel_button_clicked),
					   (gpointer)0L);
	
	gtk_widget_show(GTK_WIDGET(config_file_dialog));

	// FIXME:  add tooltips
	clist = v99_file_selection_add_path_list(config_file_dialog, 
											 _("Sessions"),
											 sessionspath);

	/* wire up callback to change directory */
	gtk_signal_connect(GTK_OBJECT(clist), 
					   "select_row", 
					   GTK_SIGNAL_FUNC(on_config_file_path_list_select_row),
					   (gpointer)0L);

	clist = v99_file_selection_add_path_list(config_file_dialog, 
											 _("Configurations"),
											 configspath);

	/* wire up callback to change directory */
	gtk_signal_connect(GTK_OBJECT(clist), 
					   "select_row", 
					   GTK_SIGNAL_FUNC(on_config_file_path_list_select_row),
					   (gpointer)0L);

	if ((ptrdiff_t)user_data == GTK_QUICK_LOAD)
		v99_file_selection_complete(config_file_dialog, "*.cnf");
	else
		v99_file_selection_complete(config_file_dialog, "quicksave.cnf");

	v99_file_selection_set_filename(config_file_dialog, "quicksave.cnf");

	execution_pause(paused);
}

/*********************************************/

/*
 *	Logging Configuration dialog.
 *
 *	The meat of the dialog is automatically generated.
 */
static GtkWidget *log_dialog;
static GtkTable *log_table;

void
on_v9t9_window_logging_button_clicked  (GtkButton       *button,
                                        gpointer         user_data)
{
	if (!VALID_WINDOW(log_dialog)) {
		log_dialog = create_logging_dialog();
	} else {
		gtk_widget_hide(log_dialog);
	}
	gtk_widget_show(log_dialog);
}

void
on_logging_reset_all_clicked           (GtkButton       *button,
                                        gpointer         user_data)
{
	GTK_send_command("Log All 0\n");

	/* force a realize */
	gtk_signal_emit_by_name(GTK_OBJECT(log_table), "realize");
}

void
on_logging_dialog_close_button_clicked (GtkButton       *button,
                                        gpointer         user_data)
{
	gtk_widget_hide(log_dialog);
}

/*
 *	Logging spin button needs to be realized.
 *	user_data is the logging subsystem.
 */
static void
on_log_spin_button_realize_value      (GtkWidget       *widget,
                                        gpointer         user_data)
{
	GtkSpinButton *s;
	int val;

	g_return_if_fail(GTK_IS_SPIN_BUTTON(widget));
	s = GTK_SPIN_BUTTON(widget);

	/* Look up the symbol and set its value */
	val = log_level((ptrdiff_t)user_data);
	gtk_spin_button_set_value(s, (gfloat)val);
}

/*
 *	Logging spin button changes.
 *	user_data is the log subsystem.
 */
static void
on_log_spin_button_changed_value      (GtkEditable     *editable,
                                       gpointer         user_data)
{
	char command[256];
	GtkSpinButton *s;

	g_return_if_fail(GTK_IS_SPIN_BUTTON(editable));
	s = GTK_SPIN_BUTTON(editable);

	snprintf(command, sizeof(command), "Log %s %d\n", 
			 log_name((ptrdiff_t)user_data),
			 gtk_spin_button_get_value_as_int(s));

	GTK_send_command(command);
}


/*
 *	Clicked a button that affects the value
 */
static void
on_log_button_clicked_realize_widget  (GtkButton       *button,
                                        gpointer         user_data)
{
	GtkWidget *w;
	g_return_if_fail(GTK_IS_WIDGET(user_data));

	w = GTK_WIDGET(user_data);

	// why can't we realize the widget again?
	gtk_widget_hide(w);
	gtk_widget_show(w);
}

/*
 *	This action sets up the log_dialog's log_table item 
 *	to include a dial and label for each log subsystem.
 */
void
on_logging_log_table_realize           (GtkWidget       *widget,
                                        gpointer         user_data)
{
	int rows = LOG_NUM_SRC / 3;
	int cols = 3;
	int row, col, sys;

	/* Get the table */
	log_table = GTK_TABLE(gtk_object_get_data((GtkObject *)log_dialog, "log_table"));
	if (!log_table)
		logger(_L|LOG_FATAL, _("Cannot get log_table from dialog\n"));

	/* Resize from 1x1 to an interesting size */
	gtk_table_resize(log_table, rows, cols);

	/* Add an entry for each subsystem */
	row = col = 0;
	for (sys = 0; sys < LOG_NUM_SRC; sys++) {
		/* make the widgets */
		GtkObject *spin_adj = gtk_adjustment_new(
			log_level(sys), 
			L_0,
			L_4,
			1,
			1,
			1);
		GtkWidget *spin = gtk_spin_button_new(GTK_ADJUSTMENT(spin_adj), 1, 1);
		GtkWidget *label = gtk_label_new(log_name(sys));
		GtkWidget *hbox = gtk_hbox_new(false /*homogenous*/, 4 /*spacing*/);

		/* standard stuff */
		gtk_widget_ref(spin);
		gtk_widget_ref(label);
		gtk_widget_ref(hbox);

		gtk_object_set_data_full (GTK_OBJECT(log_table), "log_spin_button",
								  spin,(GtkDestroyNotify) gtk_widget_unref);
		gtk_object_set_data_full (GTK_OBJECT(log_table), "log_label",
								  label,(GtkDestroyNotify) gtk_widget_unref);
		gtk_object_set_data_full (GTK_OBJECT(log_table), "log_hbox",
								  hbox,(GtkDestroyNotify) gtk_widget_unref);

		/* associate subsystem with spin button */
		gtk_signal_connect_after (GTK_OBJECT(spin), "activate",
								  GTK_SIGNAL_FUNC (on_log_spin_button_changed_value),
								  (gpointer)(ptrdiff_t)sys);
		gtk_signal_connect (GTK_OBJECT(spin), "changed",
							GTK_SIGNAL_FUNC (on_log_spin_button_changed_value),
							  (gpointer)(ptrdiff_t)sys);
		gtk_signal_connect (GTK_OBJECT(spin), "realize",
							GTK_SIGNAL_FUNC (on_log_spin_button_realize_value),
							(gpointer)(ptrdiff_t)sys);
		gtk_signal_connect (GTK_OBJECT(spin), "show",
							GTK_SIGNAL_FUNC (on_log_spin_button_realize_value),
							(gpointer)(ptrdiff_t)sys);

		gtk_box_pack_start(GTK_BOX(hbox), label, 
						   TRUE /*expand*/, FALSE /*fill*/, 0 /*padding*/);
		gtk_box_pack_start(GTK_BOX(hbox), spin, 
						   FALSE /*expand*/, TRUE /*fill*/, 0 /*padding*/);

		gtk_widget_show(spin);
		gtk_widget_show(label);
		gtk_widget_show(hbox);

		/* add hbox to table */
		gtk_table_attach_defaults (GTK_TABLE (log_table), 
								   hbox, col, col+1, row, row+1);

		if (++col >= cols) {
			col = 0;
			++row;
		}
	}
}


/*
 *	OPTIONS WINDOW
 */

static GtkWidget *options_dialog;

void
on_v9t9_window_options_button_clicked  (GtkButton       *button,
                                        gpointer         user_data)
{
	if (!VALID_WINDOW(options_dialog)) {
		options_dialog = create_options_dialog();
	} else {
		gtk_widget_hide(options_dialog);
	}
	gtk_widget_show(options_dialog);
}

void
on_option_dialog_close_button_clicked  (GtkButton       *button,
                                        gpointer         user_data)
{
	gtk_widget_hide(options_dialog);

	GTK_RESTORE_FOCUS;
}


/***************************************************************/

void
on_newsession1_activate               (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
	command_exec_text(N_("ResetComputer\n"));
}

void
on_open1_activate               (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
	on_v9t9_quick_load_save_button_clicked(0L, (gpointer)GTK_QUICK_LOAD);
}

void
on_opensession1_activate               (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
	on_v9t9_quick_load_save_button_clicked(0L, (gpointer)GTK_QUICK_LOAD);
}

void
on_savesession1_activate               (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
	on_v9t9_quick_load_save_button_clicked(0L, (gpointer)GTK_QUICK_SAVE);
}

void
on_save_session_as1_activate           (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
	on_v9t9_quick_load_save_button_clicked(0L, (gpointer)GTK_QUICK_SAVE);
}

void
on_load_configuration1_activate        (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
}

void
on_save1_activate        (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
}

void
on_save_as1_activate     (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
}

void
on_save_configuration1_activate        (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
}

void
on_save_configuration_as1_activate     (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
}

void
on_quit1_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
	
}

void
on_cut1_activate                       (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
}

void
on_copy1_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
}

void
on_paste1_activate                     (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
}

void
on_clear1_activate                     (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
}

void
on_properties1_activate                (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
}


void
on_preferences1_activate               (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
}

void
on_about1_activate                     (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
}

