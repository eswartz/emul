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
	mc_setup_commands,
	mc_module_entry	// not displayed
};


static int get_selected_row(GtkTreeSelection *selection) 
{
	GtkTreeIter iter;
	GtkTreeModel* model;

	int row = 0;
	if (gtk_tree_selection_get_selected(selection, &model, &iter)) 
	{
		GtkTreePath* path = gtk_tree_model_get_path(model, &iter);
		row = gtk_tree_path_get_indices(path)[0];
		gtk_tree_path_free(path);
	}
	return row;
}

static void set_selected_row(GtkTreeView *view, GtkTreeSelection *selection, int row)
{
	GtkTreePath *path = gtk_tree_path_new_from_indices(row, -1);
	gtk_tree_selection_select_path(selection, path);
	gtk_tree_view_scroll_to_cell(view, path, NULL, TRUE, 0.5, 0.5);
	gtk_tree_path_free(path);
}

static void
module_clist_prefix_clear(GtkWidget *widget);

/* Create treeview from module database */
void
on_module_clist_realize                (GtkWidget       *widget,
                                        gpointer         user_data)
{
	ModuleEntry *ent;
	GtkTreeView *treeview = GTK_TREE_VIEW(widget);
	GtkListStore *store = GTK_LIST_STORE(gtk_tree_view_get_model(treeview));
	GtkTreeSelection *selection = gtk_tree_view_get_selection(treeview);
	int old_selection = get_selected_row(selection);
	GtkCellRenderer *renderer;
	GtkTreeViewColumn *column;

	if (store) 
		g_object_unref(store);

	// Make a new model
	store = gtk_list_store_new(4, 
							   G_TYPE_STRING, 
							   G_TYPE_STRING, 
							   G_TYPE_STRING,
							   G_TYPE_POINTER);

	// Add an entry for each item
	ent = moddb;
	while (ent)
	{
		GtkTreeIter iter;
		
		gtk_list_store_append(store, &iter);
		gtk_list_store_set(store, &iter, 
						   mc_text_name, ent->name,
						   mc_tag_name, ent->tag,
						   mc_setup_commands, ent->commands,
						   mc_module_entry, ent,
							-1);

		ent = ent->next;
	}

	// Set new model
	gtk_tree_view_set_model(treeview, store);

	// Reselect old selection
	selection = gtk_tree_view_get_selection(treeview);
	set_selected_row(treeview, selection, old_selection);

	renderer = gtk_cell_renderer_text_new();
	column = gtk_tree_view_column_new_with_attributes(
		_("Name"), renderer, "text", mc_text_name, NULL);
	//gtk_tree_view_column_set_max_width(column, 50);
	gtk_tree_view_append_column(treeview, column);

	renderer = gtk_cell_renderer_text_new();
	column = gtk_tree_view_column_new_with_attributes(
		_("Tag"), renderer, "text", mc_tag_name, NULL);
	gtk_tree_view_append_column(treeview, column);

	renderer = gtk_cell_renderer_text_new();
	column = gtk_tree_view_column_new_with_attributes(
		_("Setup commands"), renderer, "text", mc_setup_commands, NULL);
	gtk_tree_view_append_column(treeview, column);

	gtk_tree_view_set_headers_visible(treeview, TRUE);
	gtk_tree_view_set_headers_clickable(treeview, TRUE);

	gtk_tree_view_columns_autosize(treeview);
}


void
on_v9t9_window_module_button_clicked   (GtkButton       *button,
                                        gpointer         user_data)
{
	GtkWidget *treeview;

	if (!VALID_WINDOW(module_dialog)) {
		module_dialog = create_modules_dialog();
		module_reset_toggle = 0L;
	} else {
		gtk_widget_hide(module_dialog);
	}

	treeview = gtk_object_get_data((GtkObject *)module_dialog, "module_clist");
	if (treeview) module_clist_prefix_clear(treeview);
	gtk_widget_show(module_dialog);
}

void
on_module_clist_load_button_clicked    (GtkButton       *button,
                                        gpointer         user_data)
{
	GtkTreeView *treeview;
	GtkTreeSelection *selection;
	GtkTreeIter iter;
	GtkListStore *store;

	//g_return_if_fail(GTK_IS_TREE_VIEW(treeview = user_data));
	g_return_if_fail(GTK_IS_TREE_VIEW(treeview = lookup_widget(module_dialog, "module_clist")));
	store = GTK_LIST_STORE(gtk_tree_view_get_model(treeview));
	selection = gtk_tree_view_get_selection(treeview);

	if (gtk_tree_selection_get_selected(selection, NULL, &iter)) 
	{
		ModuleEntry *ent;
		gtk_tree_model_get(store, &iter, mc_module_entry, &ent, -1);
		module_load(ent);

		if (!module_reset_toggle ||
			gtk_toggle_button_get_active(module_reset_toggle))
		{
			GTK_send_command("ResetComputer\n");
		}
	}

	// Unselect items
	gtk_tree_selection_unselect_all(selection);

	GTK_RESTORE_FOCUS;
}


void
on_module_clist_close_button_clicked  (GtkButton       *button,
									   gpointer         user_data)
{
	GtkTreeView *treeview;
	GtkTreeSelection *selection;
	g_return_if_fail(GTK_IS_TREE_VIEW(treeview = user_data));
	selection = gtk_tree_view_get_selection(treeview);

	// Unselect items
	gtk_tree_selection_unselect_all(selection);

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
 *	Key was pressed in (module) treeview.
 *
 *	Create a prefix string from the keys pressed and find
 *	a suitable match in the given column in (int)user_data.
 */
gboolean
on_clist_key_press_event               (GtkWidget       *widget,
                                        GdkEventKey     *event,
                                        gpointer         user_data)
{
	return FALSE;
#if 0
	GtkTreeView *treeview;
	gchar *old;
	char *prefix_name;
	gchar *prefix;
	int row;
	int col = (int)user_data;
	GList *list;
	int current;
	gboolean matched;

	g_return_val_if_fail(GTK_IS_TREE_VIEW(widget), false);

	treeview = GTK_TREEVIEW(widget);

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
		gtk_treeview_unselect_all(treeview);
		prefix = g_strdup("");
	}

	/* find current selection */
	list = treeview->selection;
	
	/* find item with this prefix */
	current = (list ? (gint)list->data : -1);
	matched = false;

//	g_print("prefix='%s', current=%d\n", prefix, current);

	if (*prefix)
	for (row = 0; row < treeview->rows; row++) {
		gchar *text;
		if (gtk_treeview_get_text(treeview, row, col, &text)
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
			if (current < 0 || current >= treeview->rows)
				current = 0;
		}
		else
			current = 0;

//		g_print("current=%d, list->data=%d\n", current, list->data);
		g_free(prefix);
		prefix = g_strdup(old);

		for (row = current + 1; row != current; 
			 row = (row + 1 >= treeview->rows ? 0 : row + 1)) {
			gchar *text;
			if (gtk_treeview_get_text(treeview, row, col, &text)
				&& strncasecmp(text, prefix, strlen(prefix)) == 0) {
				current = row;
				matched = true;
				break;
			}
		}
	}

	gtk_object_set_data(GTK_OBJECT(widget), prefix_name, prefix);

	if (matched && current != -1) {
		gtk_treeview_undo_selection(treeview);
		treeview->focus_row = current;
		gtk_treeview_select_row(treeview, current, 0 /*col*/);
		gtk_treeview_moveto(treeview, current, 0 /*col*/, 0.5, 0.5);
	} else if (!matched) {
		gtk_treeview_undo_selection(treeview);
	}

	g_free(old);

	return TRUE;
#endif
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


void
on_module_clist_click_column           (GtkTreeView        *treeview,
                                        gint             column,
                                        gpointer         user_data)
{
#if 0
	gboolean swap = (treeview->sort_column == column);
	GtkSortType sort = swap 
		? (treeview->sort_type == GTK_SORT_ASCENDING ? 
		   GTK_SORT_DESCENDING : 
		   GTK_SORT_ASCENDING)
		: GTK_SORT_ASCENDING;

	gtk_treeview_set_sort_column(treeview, column);
	gtk_treeview_set_sort_type(treeview, sort);
	gtk_treeview_sort(treeview);
	module_clist_prefix_clear(GTK_WIDGET(treeview));
#endif
}

/*
 *	Toggled "show setup commands" button
 */
void
on_show_commands_cb_toggled            (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
	GtkTreeView *treeview;
	GtkTreeViewColumn *column;
	g_return_if_fail(GTK_IS_TREE_VIEW(treeview = user_data));

	g_return_if_fail((column = gtk_tree_view_get_column(treeview, mc_setup_commands)) != NULL);
	
	gtk_tree_view_column_set_visible(column,
			 gtk_toggle_button_get_active(togglebutton));


	g_return_if_fail((column = gtk_tree_view_get_column(treeview, mc_tag_name)) != NULL);
	
	//gtk_tree_view_column_set_max_width(column, 50);
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
	g_return_if_fail(GTK_IS_TREE_VIEW(user_data));
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



