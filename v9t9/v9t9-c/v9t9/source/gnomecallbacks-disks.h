/*
gnomecallbacks-disks.h

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
static GtkWidget *disk_dialog;
static GtkTable *disk_dialog_table;

enum
{
	ddt_disk_name,
	ddt_combo_history,
	ddt_choose_button
};

void
on_v9t9_window_disks_button_clicked    (GtkButton       *button,
                                        gpointer         user_data)
{
	if (!VALID_WINDOW(disk_dialog)) {
		disk_dialog = create_disks_dialog();
	} else {
		gtk_widget_hide(disk_dialog);
	}
	gtk_widget_show(disk_dialog);
}

/*
 *	Clicked 'apply' button on dialog
 */
void
on_disk_dialog_apply_button_clicked   (GtkButton       *button,
                                        gpointer         user_data)
{
	GTK_RESTORE_FOCUS;
}

/*
 *	Clicked 'close' button on dialog
 */
void
on_disk_dialog_close_button_clicked    (GtkButton       *button,
                                        gpointer         user_data)
{
	gtk_widget_hide(disk_dialog);
//	disk_dialog = 0L;
//	disk_dialog_table = 0L;

	GTK_RESTORE_FOCUS;
}

/*
 *	Setup disk table.  Format is:
 *	column 0:  	label DSKx
 *	column 1:  	combo box, history of names used [not implemented]
 *	column 2:	'choose' button, which triggers on_disk_choose_button_clicked(row)
 */

/*
 *	Icky!  No accessor functions.
 */
static GtkWidget *table_get_widget(GtkTable *table, gint row, gint column)
{
	GList *list;

	for (list = table->children; list; list = list->next) {
		GtkTableChild *child;

		child = (GtkTableChild *)list->data;
		if (child->top_attach == row && child->left_attach == column)
			return child->widget;
	}
	return 0L;
}

void
on_disk_info_table_realize             (GtkWidget       *widget,
                                        gpointer         user_data)
{
	gint row;
	GtkTable *table;
	GtkWidget *kid;

	g_return_if_fail(GTK_IS_TABLE(table = GTK_TABLE(widget)));

	disk_dialog_table = table;

	for (row = 0; row < dsr_get_disk_count(); row++) {
		GtkWidget *label, *combo, *choose;
		const char *path;

		label = table_get_widget(table, row, ddt_disk_name);
		combo = table_get_widget(table, row, ddt_combo_history);
		choose = table_get_widget(table, row, ddt_choose_button);

		// enable widgets for disks we can use
		gtk_widget_set_sensitive(label, TRUE);
		gtk_widget_set_sensitive(combo, TRUE);
		gtk_widget_set_sensitive(choose, TRUE);

		path = dsr_get_disk_info(row + 1);
//		gtk_entry_set_text(GTK_ENTRY(GTK_COMBO(combo)->entry), path ? path : "");
		gtk_entry_set_text(GTK_ENTRY(combo), path ? path : "");
	}

	// Disable inaccessible kids
	for (; row < 5; row++) {
		int col;
		for (col = 0; col < 3; col++) {
			kid = table_get_widget(table, row, col);
			gtk_widget_set_sensitive(kid, FALSE);
		}
	}
}

/*
 *	Canonicalize a path to either a directory or a filename
 */
static gchar *disk_file_canonicalize(gchar **searchpath,
									 gboolean dir, const char *path, 
									 OSSpec *spec, gboolean add_dir)
{
	char *fptr;
	gchar *copy;

	fptr = (char *)OS_GetFileNamePtr(path);
	if (dir) {
		// don't accept a file as a directory
		if (OS_MakeFileSpec(path, spec) == OS_NOERR &&
			OS_Status(spec) == OS_NOERR) {
			return 0L;
		}
		if (OS_MakeSpec(path, spec, NULL) == OS_NOERR) {
			copy = g_strdup(OS_PathSpecToString1(&spec->path));
		} else {
			copy = g_strdup(path);
		}
	} else {
		// find file in the path, or add it.
		if (!*fptr)
			return 0L;

		if (!data_find_binary(*searchpath, fptr, spec)) {
			if (add_dir && *fptr) {
				/* try to add the containing directory to the search path */
				char *list = (char *)xmalloc((*searchpath ? strlen(*searchpath) : 0) 
											 + (fptr - path) + 1 + 1);
				sprintf(list, "%s%c%.*s", *searchpath ? *searchpath : "", 
						OS_ENVSEP, (int)(fptr - path), path);

				/* can we find it in the revised path? */
				if (data_find_binary(list, fptr, spec)) {
					xfree(*searchpath);
					*searchpath = list;
				} else {
					xfree(list);
					return 0L;
				}
			}
			copy = g_strdup(fptr);
		} else {
			copy = g_strdup(OS_NameSpecToString1(&spec->name));
		}
	}

	return copy;
}


#if 0
// um, this combo crap really doesn't make sense...

/*
 *	Text in combo box changed for disk in user_data (1..x)
 */
void
on_disk_combo_entry_activate           (GtkEditable     *editable,
                                        gpointer         user_data)
{
	char msg[256];
	GtkCombo *combo;
	gint disk;
	gchar *path;
	gchar *canon;

	g_return_if_fail(GTK_IS_COMBO(combo = GTK_COMBO(GTK_WIDGET(editable)->parent)));

	disk = (gint)user_data;
	path = gtk_editable_get_chars(editable, 0, -1);

	snprintf(msg, sizeof(msg), _("Changing DSK%d to '%s'\n"), disk, path);
	GTK_append_log(msg, NULL, NULL);

	// if not an error, add to history
	if (dsr_set_disk_info(disk, path) && (canon = dsr_get_disk_info(disk))) {
		GtkList *strings = GTK_LIST(combo->list);
		GList *items;
		GtkWidget *item;

		gint pos;

		gtk_editable_delete_text(editable, 0, -1);
		pos = 0;
		gtk_editable_insert_text(editable, canon, strlen(canon), &pos);

		items = g_list_alloc();
		g_list_append(items, (gpointer)editable);
		gtk_list_prepend_items(strings, items);

// ???
//		gtk_combo_set_popdown_strings(combo, strings);
//		combo->list = strings;
	}

	g_free(path);
}
#endif

/*
 *	Text in combo box changed for disk in user_data (1..x)
 */
void
on_disk_combo_entry_activate           (GtkEditable     *editable,
                                        gpointer         user_data)
{
	char msg[256];
	gint disk;
	char *path;
	gchar *copy;
	OSSpec spec;

	disk = (ptrdiff_t)user_data;
	path = gtk_editable_get_chars(editable, 0, -1);

	copy = disk_file_canonicalize(&diskimagepath,
								  dsr_is_emu_disk(disk), path, &spec, true /*add_dir*/);

	if (copy) {
		snprintf(msg, sizeof(msg), _("Changing DSK%d to '%s'\n"), disk, copy);
		GTK_append_log(msg, NULL, NULL);

		dsr_set_disk_info(disk, copy);
	}

	g_free(path);
	g_free(copy);
}


static GtkWidget*
create_disk_file_selection (gchar *title)
{
  GtkWidget *disk_file_selection;
  GtkWidget *ok_button2;
  GtkWidget *cancel_button2;

  disk_file_selection = v99_file_selection_new (title);
//  gtk_object_set_data (GTK_OBJECT (disk_file_selection), "disk_file_selection", disk_file_selection);
  gtk_container_set_border_width (GTK_CONTAINER (disk_file_selection), 10);

  ok_button2 = V99_FILE_SELECTION (disk_file_selection)->ok_button;
  gtk_object_set_data (GTK_OBJECT (disk_file_selection), "ok_button2", ok_button2);
  gtk_widget_show (ok_button2);
  GTK_WIDGET_SET_FLAGS (ok_button2, GTK_CAN_DEFAULT);

  cancel_button2 = V99_FILE_SELECTION (disk_file_selection)->cancel_button;
  gtk_object_set_data (GTK_OBJECT (disk_file_selection), "cancel_button2", cancel_button2);
  gtk_widget_show (cancel_button2);
  GTK_WIDGET_SET_FLAGS (cancel_button2, GTK_CAN_DEFAULT);

  return disk_file_selection;
}

#if 0
#pragma mark -
#endif

/*
 *	Choose a disk or directory for the disk in user_data (1..x)
 */

V99FileSelection *disk_file_dialog;		// V99FileDialog

static void 
GTK_info_logger(u32 srcflags, const char *format, ...)
{
	va_list va;
	if (srcflags & (LOG_ERROR|LOG_WARN))
		return;
	if (srcflags & LOG_VERBOSE_MASK)
		return;
	
	va_start(va, format);
	vlogger(srcflags, format, va);
	va_end(va);
}


/*
 *	Given a filename, append a row to the clist with info
 *	about the V9t9 file (if it is one)
 */
static const char *
emu_disk_clist_titles[] =
{ "Name", "Size", "Type", "P", "Host filename" };

#if __MWERKS__
// Support for the runtime initialization of local arrays
#pragma gcc_extensions on
#endif

static int
on_v99_file_selection_file_append(V99FileSelection *filesel, 
								  GtkCList *clist, 
								  const gchar *path, 
								  const gchar *filename)
{
	char tiname[11];
	char size[8];
	char type[12];
	char protect[2];
	gchar *cols[6] = { tiname, size, type, protect, (gchar *)filename, NULL };
	int charwidth = gdk_string_width(gtk_style_get_font(filesel->file_list->style), "M");
	int widths[5]= { charwidth*10, charwidth*3, charwidth*10, charwidth*1, charwidth*16 };
	int col;
	fiad_tifile tf;
	OSSpec spec;
	fiad_logger_func old;

	/* Try to make a tifile from the entry */
	if (OS_MakeSpec2(path, filename, &spec) != OS_NOERR) {
		/* oops, not even a good file (maybe broken softlink) */
		return 0;
	}

	/* Don't log errors found in likely-non-V9t9 files,
		but log renames */
	old = fiad_set_logger(GTK_info_logger);

	if (fiad_tifile_setup_spec_with_spec(&tf, &spec) == OS_NOERR &&
		fiad_tifile_get_info(&tf))
	{
		/* it might have just been renamed */
		cols[4] = OS_NameSpecToString1(&tf.spec.name);

		memcpy(tiname, tf.fdr.filenam, 10);
		tiname[10] = 0;
		sprintf(size, "%d", tf.fdr.secsused + 1);
		strcpy(type, fiad_catalog_get_file_type_string(&tf.fdr));
		protect[0] = (tf.fdr.flags & ff_protected) ? 'Y' : ' ';
		protect[1] = 0;

	} 
	else 	/* not a V9t9 file */
	{
		*tiname = 0;
		*size = 0;
		*protect = 0;
		*type = 0;
	}

	/* figure widths for each column */
	for (col = 0; col < 5; col++) {
		int width = gdk_string_width(gtk_style_get_font(filesel->file_list->style), cols[col]);
		if (width > widths[col]) {
			widths[col] = width;
		}
		gtk_clist_set_column_width(clist, col, widths[col]);
	}

	return gtk_clist_append(clist, cols);
}

#if __MWERKS__
#pragma gcc_extensions reset
#endif

static void
on_v99_file_selection_file_click_column(GtkCList *clist,
										gint column,
										gpointer user_data)
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
}

/*
 *	user_data is the disk number
 */
void
on_disk_file_ok_button_clicked         (GtkButton       *button,
                                        gpointer         user_data)
{
	gint disk = (ptrdiff_t)user_data;
	gboolean dir = dsr_is_emu_disk(disk);
	const char *path;
	gchar *copy;
	OSSpec spec;

	g_return_if_fail(disk >= 1 && disk <= 5);

	path = v99_file_selection_get_filename(disk_file_dialog);
	copy = disk_file_canonicalize(&diskimagepath, 
								  dir, path, &spec, true /*add_dir*/);

	if (copy) {
		if (dsr_set_disk_info(disk, copy)) {
			GtkWidget *entry = table_get_widget(disk_dialog_table, disk-1, 
												ddt_combo_history);

			gtk_entry_set_text(GTK_ENTRY(entry), copy);
			gtk_widget_destroy((GtkWidget *)disk_file_dialog);
			disk_file_dialog = 0L;

			GTK_RESTORE_FOCUS;
		}
	} else {
		v99_file_selection_complete(disk_file_dialog,
									path);
	}

	g_free(copy);
	//g_free(path);

}


void
on_disk_file_cancel_button_clicked     (GtkButton       *button,
                                        gpointer         user_data)
{
	gtk_widget_destroy((GtkWidget *)disk_file_dialog);
	disk_file_dialog = 0;
	GTK_RESTORE_FOCUS;
}

/*
 *	Main disk/file chooser dialog.
 *	
 */
void
on_disk_choose_button_clicked          (GtkButton       *button,
                                        gpointer         user_data)
{
	gint disk = (ptrdiff_t)user_data;

	g_return_if_fail(disk >= 1 && disk <= 5);

	if (!VALID_WINDOW(disk_file_dialog)) {
		disk_file_dialog = V99_FILE_SELECTION(create_disk_file_selection(_("Select Path")));
		if (dsr_is_real_disk(disk)) {
			// normal file selection
			v99_file_selection_set_file_list_active(disk_file_dialog, 
													TRUE);
		} else {
			// v9t9 directory selection
			v99_file_selection_set_file_list_columns(disk_file_dialog, 
													 5,
													 (gchar **)emu_disk_clist_titles,
													 on_v99_file_selection_file_append,
													 NULL);
			gtk_signal_connect(GTK_OBJECT(disk_file_dialog->file_list),
							   "click_column",
							   GTK_SIGNAL_FUNC(on_v99_file_selection_file_click_column),
							   NULL);

			// no, we can't select files as directories
			v99_file_selection_set_file_list_active(disk_file_dialog, 
													FALSE);
			disk_file_dialog->user_data = (gpointer)(ptrdiff_t)(disk - 1);
		}
	}

	gtk_widget_show(GTK_WIDGET(disk_file_dialog));

	if (dsr_is_real_disk(disk)) {
//		OSError err;
		OSSpec spec;
		const char *filename;
		gchar *copy;

		// Set the full path if we can
		filename = dsr_get_disk_info(disk);
		copy = disk_file_canonicalize(&diskimagepath,
									  false /*directory*/, filename, &spec,
									  false /*add_dir*/);

		v99_file_selection_set_filename(disk_file_dialog, 
										OS_SpecToString1(&spec));
		v99_file_selection_complete(disk_file_dialog,
									"*.dsk");

		g_free(copy);
	} else {
		// it's already a full path
		char path[OS_PATHSIZE];
		strcpy(path, dsr_get_disk_info(disk));
#if !defined(UNDER_MACOS)
		// force a directory selection so ppl don't think they can
		// type a filename
		strcat(path, ".");	
#endif
		v99_file_selection_set_filename(disk_file_dialog, 
										path);

	}

	// wire up buttons here (so we can pass known disk number)
	gtk_signal_connect(GTK_OBJECT(disk_file_dialog->ok_button), 
					   "clicked", 
					   GTK_SIGNAL_FUNC(on_disk_file_ok_button_clicked),
					   (gpointer)(ptrdiff_t)disk);
	gtk_signal_connect(GTK_OBJECT(disk_file_dialog->cancel_button), 
					   "clicked", 
					   GTK_SIGNAL_FUNC(on_disk_file_cancel_button_clicked),
					   (gpointer)(ptrdiff_t)disk);
}

/*
 *	Toggle use of realdisk DSR
 */

void
on_real_disk_cb_toggled                (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
	char cmd[64];
	sprintf(cmd, "ToggleV9t9 dsrRealDisk %s", 
			gtk_toggle_button_get_active(togglebutton) ? "on" : "off");
	GTK_send_command(cmd);
	gtk_signal_emit_by_name(GTK_OBJECT(user_data), "realize");
}


void
on_real_disk_cb_realize                (GtkWidget       *widget,
                                        gpointer         user_data)
{
	gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(widget),
						   !!(realDiskDSR.runtimeflags & vmRTInUse));
	gtk_signal_emit_by_name(GTK_OBJECT(user_data), "realize");
}

/*
 *	Toggle use of emulated disk DSR
 */

void
on_emu_disk_cb_toggled                 (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
	char cmd[64];
	sprintf(cmd, "ToggleV9t9 dsrEmuDisk %s",
			gtk_toggle_button_get_active(togglebutton) ? "on" : "off");
	GTK_send_command(cmd);
	gtk_signal_emit_by_name(GTK_OBJECT(user_data), "realize");
}


void
on_emu_disk_cb_realize                 (GtkWidget       *widget,
                                        gpointer         user_data)
{
	gtk_toggle_button_set_active(GTK_TOGGLE_BUTTON(widget),
						   !!(emuDiskDSR.runtimeflags & vmRTInUse));
	gtk_signal_emit_by_name(GTK_OBJECT(user_data), "realize");
}

