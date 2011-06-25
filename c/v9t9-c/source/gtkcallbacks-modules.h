/*
 *	V9t9 Module selection dialog callbacks
 */

static GtkWidget *module_dialog;
static GtkToggleButton *module_reset_toggle;

/*	These must correspond with columns in dialog */
enum
{
	mc_text_name,
	mc_tag_name,
	mc_setup_commands
};

static void
module_clist_prefix_clear(GtkWidget *widget);

void
on_v9t9_window_module_button_clicked   (GtkButton       *button,
                                        gpointer         user_data)
{
	GtkWidget *clist;

	if (!VALID_WINDOW(module_dialog)) {
		module_dialog = create_modules_dialog();
		module_reset_toggle = 0L;
	} else {
		gtk_widget_hide(module_dialog);
	}

	clist = gtk_object_get_data((GtkObject *)module_dialog, "module_clist");
	if (clist) module_clist_prefix_clear(clist);
	gtk_widget_show(module_dialog);
}

void
on_module_clist_load_button_clicked    (GtkButton       *button,
                                        gpointer         user_data)
{
	GtkCList *clist;
	GList *list;

	g_return_if_fail(GTK_IS_CLIST(clist = user_data));

	// Step through list of selected modules 
	// and load them up
	list = clist->selection;

	while (list) 
	{
		gint row = (gint)(list->data);
		ModuleEntry *ent = (ModuleEntry *)gtk_clist_get_row_data(clist, row);

		if (ent) module_load(ent);
		list = list->next;
	}

	if (clist->selection
		&& (!module_reset_toggle ||
			gtk_toggle_button_get_active(module_reset_toggle)))
	{
		GTK_send_command("ResetComputer\n");
	}

	// Unselect items
	gtk_clist_unselect_all(clist);

	GTK_RESTORE_FOCUS;
}


void
on_module_clist_close_button_clicked  (GtkButton       *button,
									   gpointer         user_data)
{
	GtkCList *clist;
	g_return_if_fail(GTK_IS_CLIST(clist = user_data));

	// Unselect items
	gtk_clist_unselect_all(clist);

//	gtk_widget_hide(module_dialog);

	// destroy it so we will re-read module list next time
	gtk_widget_destroy(module_dialog);
	module_reset_toggle = 0L;
	module_dialog = 0L;

	GTK_RESTORE_FOCUS;
}

#define ALT_KEY(x)		\
	 ((x) == GDK_Meta_L ||\
	  (x) == GDK_Meta_R ||\
	  (x) == GDK_Alt_L ||\
	  (x) == GDK_Alt_R)

/*
 *	Don't intercept ALT-key shortcuts
 */
gboolean
on_clist_key_release_event             (GtkWidget       *widget,
                                        GdkEventKey     *event,
                                        gpointer         user_data)
{
	if (ALT_KEY(event->keyval)) {
		gtk_object_set_data(GTK_OBJECT(widget), "alt_down", (gpointer)0);
	}
	return FALSE;
}

/*
 *	Key was pressed in (module) clist.
 *
 *	Create a prefix string from the keys pressed and find
 *	a suitable match in the given column in (int)user_data.
 */
gboolean
on_clist_key_press_event               (GtkWidget       *widget,
                                        GdkEventKey     *event,
                                        gpointer         user_data)
{
	GtkCList *clist;
	gchar *old;
	char *prefix_name;
	gchar *prefix;
	int row;
	int col = (int)user_data;
	GList *list;
	int current;
	gboolean matched;

	g_return_val_if_fail(GTK_IS_CLIST(widget), false);

	clist = GTK_CLIST(widget);

	prefix_name = widget_tag("prefix_string_", col, 1);
	old = gtk_object_get_data(GTK_OBJECT(widget), prefix_name);

	/*
	 *	Don't intercept ALT keys
	 */
	if (ALT_KEY(event->keyval))
	{
		gtk_object_set_data(GTK_OBJECT(widget), "alt_down", (gpointer)1);
		return false;
	}

	if ((int)gtk_object_get_data(GTK_OBJECT(widget), "alt_down"))
	{
		return false;
	}

	/*
	 *	Ignore accelerator keys
	 */
	if (event->keyval == GDK_Escape)
	{
		return false;
	}

	/*
	 *	Start off with some old prefix value to compare against
	 */
	if (!old)
		old = g_strdup("");

	/*
	 *	Don't append non-printable characters, which we assume
	 *	are control characters intended to clear the prefix.
	 */
	if (event->string && *event->string && isprint(*event->string))
	{
		prefix = g_strconcat(old, event->string, 0L);
	}
	else
	{
		gtk_clist_unselect_all(clist);
		prefix = g_strdup("");
	}

	/* find current selection */
	list = clist->selection;
	
	/* find item with this prefix */
	current = (list ? (gint)list->data : -1);
	matched = false;

//	g_print("prefix='%s', current=%d\n", prefix, current);

	if (*prefix)
	for (row = 0; row < clist->rows; row++) {
		gchar *text;
		if (gtk_clist_get_text(clist, row, col, &text)
			&& strncasecmp(text, prefix, strlen(prefix)) == 0) {
			current = row;
			matched = true;
			break;
		}
	}

//	g_print("old='%s', event='%s'\n", old, event->string);

	/* if we couldn't find one with new prefix,
	   and this new key is a suffix of the last
	   prefix, look for another one matching... */

	if (!matched
		&& event->string && *event->string 
		&& strcasecmp(old + strlen(old) - strlen(event->string),
					  event->string) == 0)
	{
		if (list) {
			current = (gint)list->data;
			if (current < 0 || current >= clist->rows)
				current = 0;
		}
		else
			current = 0;

//		g_print("current=%d, list->data=%d\n", current, list->data);
		g_free(prefix);
		prefix = g_strdup(old);

		for (row = current + 1; row != current; 
			 row = (row + 1 >= clist->rows ? 0 : row + 1)) {
			gchar *text;
			if (gtk_clist_get_text(clist, row, col, &text)
				&& strncasecmp(text, prefix, strlen(prefix)) == 0) {
				current = row;
				matched = true;
				break;
			}
		}
	}

	gtk_object_set_data(GTK_OBJECT(widget), prefix_name, prefix);

	if (matched && current != -1) {
		gtk_clist_undo_selection(clist);
		clist->focus_row = current;
		gtk_clist_select_row(clist, current, 0 /*col*/);
		gtk_clist_moveto(clist, current, 0 /*col*/, 0.5, 0.5);
	} else if (!matched) {
		gtk_clist_undo_selection(clist);
	}

	g_free(old);
	return TRUE;
}

static void
module_clist_prefix_clear(GtkWidget *widget)
{
	gchar *prefix;
	int i;
	for (i=0; i < 2; i++) {
		char *name = widget_tag("prefix_string_", i, 1);
		prefix = (gchar *)gtk_object_get_data(GTK_OBJECT(widget), name);
		if (prefix) g_free(prefix);
		gtk_object_set_data(GTK_OBJECT(widget), name, 0L);
	}
}

gboolean
on_module_clist_event                  (GtkWidget       *widget,
                                        GdkEvent        *event,
                                        gpointer         user_data)
{
	if ((event->type == GDK_KEY_PRESS 
		 && event->key.keyval == GDK_Return) ||
		event->type == GDK_2BUTTON_PRESS)
	{
		on_module_clist_load_button_clicked(NULL, user_data);

		gtk_widget_hide(module_dialog);

		GTK_RESTORE_FOCUS;
	}
	return FALSE;
}


/* Create clist from module database */
void
on_module_clist_realize                (GtkWidget       *widget,
                                        gpointer         user_data)
{
	ModuleEntry *ent;
	GtkCList *clist = GTK_CLIST(widget);
	int old_selection;

	old_selection = (clist->selection ? (gint)clist->selection->data : 0);

	// Clear clist
	gtk_clist_clear(clist);

	// Freeze display
	gtk_clist_freeze(clist);

	// Add an entry for each item
	ent = moddb;
	while (ent)
	{
		gchar *items[3];
		gint row;

		items[0] = ent->name;
		items[1] = ent->tag;
		items[2] = ent->commands;

		// add row
		row = gtk_clist_append(clist, items);

		// associate row with ModuleEntry
		gtk_clist_set_row_data(clist, row, ent);

		ent = ent->next;
	}

	// Unfreeze 
	gtk_clist_thaw(clist);

	// Reselect old selection
	gtk_clist_select_row(clist, old_selection, 0 /*col*/);
	gtk_clist_moveto(clist, old_selection, 0 /*col*/, 0.5, 0.5);

	gtk_clist_set_column_max_width(clist, mc_tag_name, 50);
}

void
on_module_clist_click_column           (GtkCList        *clist,
                                        gint             column,
                                        gpointer         user_data)
{
	gboolean swap = (clist->sort_column == column);
	GtkSortType sort = swap 
		? (clist->sort_type == GTK_SORT_ASCENDING ? 
		   GTK_SORT_DESCENDING : 
		   GTK_SORT_ASCENDING)
		: GTK_SORT_ASCENDING;

	gtk_clist_set_sort_column(clist, column);
	gtk_clist_set_sort_type(clist, sort);
	gtk_clist_sort(clist);
	module_clist_prefix_clear(GTK_WIDGET(clist));
}

/*
 *	Toggled "show setup commands" button
 */
void
on_show_commands_cb_toggled            (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
	GtkCList *clist;
	g_return_if_fail(GTK_IS_CLIST(clist = user_data));

	gtk_clist_set_column_visibility(clist, mc_setup_commands, 
									gtk_toggle_button_get_active(togglebutton));
	gtk_clist_set_column_max_width(clist, mc_tag_name, 50);
}

/*
 *	Toggled "reset computer after load" button
 */
void
on_reset_computer_cb_toggled           (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
	module_reset_toggle = togglebutton;
}


void
on_modules_refresh_button_clicked      (GtkButton       *button,
                                        gpointer         user_data)
{
	g_return_if_fail(GTK_IS_CLIST(user_data));
	GTK_send_command("InitModuleDatabase\n"
					 "LoadConfigFile \"modules.inf\"\n");
	module_clist_prefix_clear(GTK_WIDGET(user_data));
	on_module_clist_realize(GTK_WIDGET(user_data), 0L);
}

void
on_unload_current_button_clicked       (GtkButton       *button,
                                        gpointer         user_data)
{
	GTK_send_command("UnloadModuleOnly\n");
}



