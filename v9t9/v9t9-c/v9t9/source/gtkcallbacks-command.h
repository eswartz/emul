/*
 *	V9t9 Command Central dialog callbacks
 *
 */

/***********************************/
#if 0
#pragma mark -
#endif

/*
 *	This is a callback for a generic button that has a fixed 
 *	command string in user_data.
 */
void
on_v9t9_button_clicked                 (GtkButton       *button,
                                        gpointer         user_data)
{
	GTK_send_command((const gchar *)user_data);
}

/*
 *	This is for a generic cancel button which will unpause the
 *	computer.
 */
void
on_v9t9_button_cancel                  (GtkButton       *button,
                                        gpointer         user_data)
{
	if (!debugger_enabled())
		execution_pause(false);

	gtk_widget_destroy(GTK_WIDGET(user_data));
}


void
on_v9t9_pause_button_clicked           (GtkButton       *button,
                                        gpointer         user_data)
{
//	v9t9_window_pause_button = button;
	execution_pause(!execution_paused());
}


void
on_quit_button_clicked                 (GtkButton       *button,
                                        gpointer         user_data)
{
	GtkWidget *dialog = create_quit_dialog();
	gtk_widget_show(dialog);

	if (!debugger_enabled())
		execution_pause(true);
}

gboolean
on_v9t9_window_configure_event         (GtkWidget       *widget,
                                        GdkEventConfigure *event,
                                        gpointer         user_data)
{
//	gtk_window_set_focus(GTK_WINDOW(widget), v9t9_drawing_area);
	return FALSE;
}

#if 0
#pragma mark -
#endif

typedef struct {
	GList *lines;
	int index;
}	History;

static GtkWidget *command_text_entry;
static History *command_text_history;
static GtkWidget *completions_popup;

static History *history_get(History **where)
{
	if (!*where) {
		*where = (History *)g_malloc(sizeof(History));

		(*where)->lines = g_list_alloc();
		(*where)->index = 0;
	}
	return *where;
}

static void	history_append(History *history, gpointer data)
{
	history->lines = g_list_append(history->lines, data);
	history->index = g_list_length(history->lines);
}

static void	history_update(History *history, gpointer data)
{
	if (history->index == g_list_length(history->lines)) {
		
	}
}

static void	history_remove(History *history)
{
	int length = g_list_length(history->lines);
	if (length) {
		g_list_free(g_list_last(history->lines));
		history->index = MIN(history->index, length);
	}
}

static gpointer *history_prev(History *history)
{
	if (history->index > 0) {
		history->index--;
	}
	return g_list_nth_data(history->lines, history->index);
}

static gpointer *history_next(History *history)
{
	if (history->index + 1 < g_list_length(history->lines)) {
		history->index++;
	}
	return g_list_nth_data(history->lines, history->index);
}

/*
 *	Someone has entered text in the window.  
 *	Need to turn on interactive mode temporarily, or 
 *	make the entry insensitive.
 *
 *	user_data is the text box
 */
void
on_command_text_entry_activate         (GtkEditable     *editable,
                                        gpointer         user_data)
{
	History *history = history_get(&command_text_history);

	// get line of text
	gchar *text = gtk_editable_get_chars(editable, 0, -1);

	// execute text
	GTK_send_command(text);

	// append it
	history_append(history, text);

//	g_free(text);

	// select text so it can be cleared
	gtk_editable_select_region(editable, 0, -1);
}

#define IS_WORD_CHAR(c)	(isalnum(c) || (c) == '_')


static gchar *editable_get_word(GtkEditable *editable, gint *start, gint *end)
{
	gchar *text = gtk_editable_get_chars(editable, 0, -1);
	int	pos = gtk_editable_get_position(editable);
	gchar *wordst, *worden;
	
	worden = text + pos;
	if (worden > text && !IS_WORD_CHAR(*(worden-1))) {
		g_free(text);
		return 0L;
	}

	wordst = worden - 1;
	while (wordst > text && IS_WORD_CHAR(*(wordst-1)))
		wordst--;

	*start = wordst - text;
	*end = worden - text;
	return text;
}

static void 
editable_replace_word(GtkEditable *editable, const char *selection)
{
	gint pos;
	gchar *text;
	gint start, end;

	pos = gtk_editable_get_position(editable);

	text = editable_get_word(editable, &start, &end);
	if (text) {
		/* delete word */
		gtk_editable_delete_text(editable, start, end);
		g_free(text);
	}

	/* insert selected word, and a space */
	gtk_editable_insert_text(editable, selection, strlen(selection), &pos);
	gtk_editable_insert_text(editable, " ", 1, &pos);

	/* move to the end */
	gtk_editable_set_position(editable, pos);
}

static void editable_popup_completions(GtkEditable *editable, char **completions)
{
	GtkCList *list;
	GList *glist = 0L;
	gint pos;

	/* make a popup window to get the selection */
	if (VALID_WINDOW(completions_popup)) {
		gtk_widget_destroy(completions_popup);
	}

	completions_popup = create_completion_popup();
	list = GTK_CLIST(gtk_object_get_data(GTK_OBJECT(completions_popup), 
										 "completions"));
	if (!list) {
		logger(_L|LOG_ABORT, "no 'completions' in popup\n");
	}

	gtk_clist_set_column_auto_resize(list, 0, true);

	/* copy possible matches into the list.... */
	for (pos = 0; completions[pos]; pos++) {
		gchar *items[2];
		items[0] = completions[pos];
		items[1] = 0L;
		gtk_clist_append(list, items);
	}

	gtk_object_set_data(GTK_OBJECT(completions_popup), 
						"editable", editable);

	/* put window at text cursor */	
	gtk_widget_show(completions_popup);
}


gboolean
on_command_text_entry_key_press_event  (GtkWidget       *widget,
                                        GdkEventKey     *event,
                                        gpointer         user_data)
{
	gchar *text;
	History *history = history_get(&command_text_history);
	gint pos;
	GtkEditable *editable = GTK_EDITABLE(widget);

	// handle up and down arrow for history
	switch (event->keyval) 
	{
	case GDK_Up:
	case GDK_Page_Up:
		text = gtk_editable_get_chars(editable, 0, -1);
		history_update(history, text);
		text = (gchar *)history_prev(history);
		if (text) {
			gtk_editable_delete_text(editable, 0, -1);
			pos = 0;
			gtk_editable_insert_text(editable, 
									 text, strlen(text),
									 &pos);
		} else {
			//	history_remove(history);
		}
		break;

	case GDK_Down:
	case GDK_Page_Down:
		text = gtk_editable_get_chars(editable, 0, -1);
		history_update(history, text);
		text = (gchar *)history_next(history);
		if (text) {
			gtk_editable_delete_text(editable, 0, -1);
			pos = 0;
			gtk_editable_insert_text(editable, 
									 text, strlen(text),
									 &pos);
		} else {
			//		history_remove(history);
		}
		break;

		// notice these keys
	case GDK_End:
	case GDK_Left:
		if (editable->has_selection) {
			text = gtk_editable_get_chars(editable, 0, -1);
			gtk_editable_select_region(editable, 0, 0);
			gtk_editable_set_position(editable, strlen(text));
			g_free(text);
		}
		break;

	case GDK_Home:
	case GDK_Right:
		if (editable->has_selection) {
			gtk_editable_select_region(editable, 0, 0);
			gtk_editable_set_position(editable, 0);
		}
		break;
		
#if HAVE_READLINE
	case GDK_Tab:
	{
		char **completions;
		int start, end;

		text = editable_get_word(editable, &start, &end);
		if (!text) {
			text = g_strdup("");
			start = end = 0;
		}

		completions = readline_completion(text + start, start, end);

		if (completions) {
			if (completions[1]) {
				/* pop up window so user can select */
				editable_popup_completions(editable, completions);
			} else {
				/* just replace only match */
				editable_replace_word(editable, completions[0]);
			}

			/* get rid of original matches */
			for (pos = 0; completions[pos]; pos++) {
				free(completions[pos]);
			}
			free(completions);
		}

		free(text);
		break;
	}
#endif

	default:
		return TRUE;
	}
	return TRUE;
}

/*
 *	entry was selected; replace current word with that
 */
static void
on_completions_selection_clicked       (GtkCList         *_list,
                                        gpointer         user_data)
{
	GtkCList *clist;
	GtkEditable *editable;
	gchar *selection;
	gint row;

	/* get the list of items */
	clist = GTK_CLIST(gtk_object_get_data(GTK_OBJECT(completions_popup),
										  "completions"));
	g_return_if_fail(clist);

	/* get editable into which we'll replace the word */
	editable = GTK_EDITABLE(gtk_object_get_data(GTK_OBJECT(completions_popup), 
												"editable"));
	g_return_if_fail(editable);

	/* what word did the user pick? */
	row = (gint)clist->selection->data;
	gtk_clist_get_text(clist, row, 0, &selection);
	g_return_if_fail(selection);

	editable_replace_word(editable, selection);

}


gboolean
on_completions_event                   (GtkWidget       *widget,
                                        GdkEvent        *event,
                                        gpointer         user_data)
{
	if ((event->type == GDK_KEY_PRESS 
		 && event->key.keyval == GDK_Return) ||
		event->type == GDK_BUTTON_RELEASE)
	{
		//!!! which one was clicked?
		on_completions_selection_clicked(NULL, user_data);

		gtk_widget_destroy(completions_popup);
		completions_popup = 0L;

		GTK_RESTORE_FOCUS;
	}
	else if (event->type == GDK_KEY_PRESS
			 && !(event->key.keyval == GDK_Up 
				 || event->key.keyval == GDK_Down
				 ||	event->key.keyval == ' '))
	{
		gtk_widget_destroy(completions_popup);
		completions_popup = 0L;

		GTK_RESTORE_FOCUS;

	}
	return FALSE;
}



void
on_completions_realize                 (GtkWidget       *widget,
                                        gpointer         user_data)
{
}


void
on_completions_destroy                 (GtkObject       *object,
                                        gpointer         user_data)
{
	/* free all the junk in the list */
/*
	GList *glist = GTK_LIST(object)->children, *ptr = glist;
	while (ptr) {
		g_free(ptr->data);
	    ptr = ptr->next;
	}
	g_list_free(glist);
//	g_list_free_1(GTK_LIST(object)->children);
*/
	completions_popup = 0L;
}


void
on_log_text_box_realize_event          (GtkWidget       *widget,
                                        gpointer         user_data)
{
	v9t9_command_log = widget;
}

/*
 *	Flush text in command log
 */
void
on_flush_item_activate                 (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
	GTK_flush_log();
}

#if 0
#pragma mark -
#endif

/*
 *	Select text font
 */

static GtkWidget *font_selector;
gchar 	*gtk_command_central_font;

void
on_font_item_activate                  (GtkMenuItem     *menuitem,
                                        gpointer         user_data)
{
	if (!VALID_WINDOW(font_selector)) {
		font_selector = create_command_log_font_selector();
	} else {
		gtk_widget_hide(font_selector);
	}

	if (gtk_command_central_font) {
		gtk_font_selection_dialog_set_font_name(
			GTK_FONT_SELECTION_DIALOG(font_selector),
			gtk_command_central_font);
	}

	gtk_widget_show(font_selector);
}


void
on_command_log_font_selector_ok_button1_clicked
                                        (GtkButton       *button,
                                        gpointer         user_data)
{
	char *fontname;

	fontname = gtk_font_selection_dialog_get_font_name(
		GTK_FONT_SELECTION_DIALOG(font_selector));
	GTK_change_log_font(fontname);

	if (gtk_command_central_font)
		xfree(gtk_command_central_font);

	gtk_command_central_font = xstrdup(fontname);
	g_free(fontname);
	gtk_widget_hide(font_selector);
}


void
on_command_log_font_selector_apply_button1_clicked
                                        (GtkButton       *button,
                                        gpointer         user_data)
{
	gchar *fontname;

	fontname = gtk_font_selection_dialog_get_font_name(
		GTK_FONT_SELECTION_DIALOG(font_selector));
	GTK_change_log_font(fontname);

	g_free(fontname);
}


void
on_command_log_font_cancel1_button_clicked
                                        (GtkButton       *button,
                                        gpointer         user_data)
{
	GTK_change_log_font(gtk_command_central_font);
	gtk_widget_hide(font_selector);
}




gboolean
on_command_text_entry_event            (GtkWidget       *widget,
                                        GdkEvent        *event,
                                        gpointer         user_data)
{
  return FALSE;
}


gboolean
on_command_dialog_key_press_event      (GtkWidget       *widget,
                                        GdkEventKey     *event,
                                        gpointer         user_data)
{
/*	if (event->keyval == GDK_Up || event->keyval == GDK_Down) {
		gtk_signal_emit_stop_by_name(GTK_OBJECT(widget), "key_press_event");
		gtk_signal_emit_by_name(GTK_OBJECT(user_data), "key_press_event", event);
	}
*/
	return FALSE;
}


void
on_command_dialog_destroy        (GtkObject				*object,
                                  gpointer         		user_data)
{
	gtk_main_quit();
	v9t9_sigterm(1);
}

gboolean
on_command_dialog_button_press_event   (GtkWidget       *widget,
                                        GdkEventButton  *event,
                                        gpointer         user_data)
{
	// bring up command center window
	if (event->button == 1) {
		gdk_window_raise(command_center->window);
	}
	// bring up v9t9 window
	else if (event->button > 1) {
		gdk_window_raise(v9t9_window->window);
		gtk_widget_activate(v9t9_window);
		if (event->button == 2)
			gtk_widget_set_uposition(v9t9_window,
									 event->x_root - v9t9_window->allocation.width / 2, 
									 event->y_root - v9t9_window->allocation.height / 2);
	}
	return TRUE;
}

DECL_SYMBOL_ACTION(gtk_command_central_set_font)
{
	if (task == csa_WRITE) {
		char *str;
		command_arg_get_string(SYM_ARG_1st, &str);
		GTK_change_log_font(str);
	}
	return 1;
}

/***********************************************************************/
