/*
gtkcallbacks-debugger.h

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
static GtkWidget *debugger_window;
GtkWidget *debugger_registers_table,
	*debugger_instruction_box,
	*debugger_status_bar,
	*debugger_pc_entry,
	*debugger_wp_entry,
	*debugger_st_entry;

// flag indicating we want to display all the time-wasting
// updates (turned on/off during intermittent mode, etc)
static bool debugger_verbose_updates;

#define DBG_FG_NORMAL(s)	(&(s)->fg[GTK_STATE_NORMAL])
#define DBG_FG_AUTO(s)		(&(s)->fg[GTK_STATE_PRELIGHT])
#define DBG_FG_BREAK(s)		(&(s)->fg[GTK_STATE_SELECTED])
#define DBG_FG_VIEW(s)		(&(s)->fg[GTK_STATE_NORMAL])
#define DBG_FG_READ(s)		(&(s)->fg[GTK_STATE_NORMAL])
#define DBG_FG_WRITTEN(s)	(&(s)->fg[GTK_STATE_NORMAL])

#define DBG_BG_NORMAL(s)	(&(s)->bg[GTK_STATE_NORMAL])
#define DBG_BG_AUTO(s)		(&(s)->bg[GTK_STATE_NORMAL])
#define DBG_BG_BREAK(s)		(&(s)->bg[GTK_STATE_NORMAL])
#define DBG_BG_VIEW(s)		(&(s)->bg[GTK_STATE_NORMAL])
#define DBG_BG_READ(s)		(&(s)->bg[GTK_STATE_PRELIGHT])
#define DBG_BG_WRITTEN(s)	(&(s)->bg[GTK_STATE_PRELIGHT])

static GdkColor *
debugger_status_color_fg(status_item item, GtkStyle *style)
{
	switch (item)
	{
	case STATUS_CPU_PC:				return DBG_FG_NORMAL(style);
	case STATUS_CPU_STATUS:			return DBG_FG_NORMAL(style);
	case STATUS_CPU_WP:				return DBG_FG_NORMAL(style);
	case STATUS_CPU_REGISTER_VIEW:	return DBG_FG_VIEW(style);
	case STATUS_CPU_REGISTER_READ:	return DBG_FG_READ(style);
	case STATUS_CPU_REGISTER_WRITE:	return DBG_FG_WRITTEN(style);
	case STATUS_CPU_INSTRUCTION:
		if (debugger_check_breakpoint(pc))
			return DBG_FG_BREAK(style);
		else if ((stateflag & ST_PAUSE) || debugger_verbose_updates)
			return DBG_FG_NORMAL(style);
		else
			return DBG_FG_AUTO(style);
	case STATUS_CPU_INSTRUCTION_LAST: return DBG_FG_WRITTEN(style);
	case STATUS_MEMORY_VIEW:		return DBG_FG_VIEW(style);
	case STATUS_MEMORY_READ:		return DBG_FG_READ(style);
	case STATUS_MEMORY_WRITE:		return DBG_FG_WRITTEN(style);
	}
	return DBG_FG_NORMAL(style);
}

static GdkColor*
debugger_status_color_bg(status_item item, GtkStyle *style)
{
	switch (item)
	{
	case STATUS_CPU_PC:				return DBG_BG_NORMAL(style);
	case STATUS_CPU_STATUS:			return DBG_BG_NORMAL(style);
	case STATUS_CPU_WP:				return DBG_BG_NORMAL(style);
	case STATUS_CPU_REGISTER_VIEW:	return DBG_BG_VIEW(style);
	case STATUS_CPU_REGISTER_READ:	return DBG_BG_READ(style);
	case STATUS_CPU_REGISTER_WRITE:	return DBG_BG_WRITTEN(style);
	case STATUS_CPU_INSTRUCTION:
		if (debugger_check_breakpoint(pc))
			return DBG_BG_BREAK(style);
		else if ((stateflag & ST_PAUSE) || debugger_verbose_updates)
			return DBG_BG_NORMAL(style);
		else
			return DBG_BG_AUTO(style);
	case STATUS_CPU_INSTRUCTION_LAST: return DBG_BG_WRITTEN(style);
	case STATUS_MEMORY_VIEW:		return DBG_BG_VIEW(style);
	case STATUS_MEMORY_READ:		return DBG_BG_READ(style);
	case STATUS_MEMORY_WRITE:		return DBG_BG_WRITTEN(style);
	}
	return DBG_BG_NORMAL(style);
//	return &style->bg[0];
//	return 0L;
}

//??? can anyone explain why the string height is wrong under X?
#if UNDER_WIN32
#define ADJUST_HEIGHT(h)	(h)
#else
#define ADJUST_HEIGHT(h)	((h)*3/2)
#endif

/*
 *	Set up registers table for the first time
 */
static void
setup_debugger_registers_table(void)
{
	int reg;
	GtkWidget *w;
	GtkTable *t;
	GtkStyle *s;
	int width, height;

	g_return_if_fail(debugger_registers_table);

	t = GTK_TABLE(debugger_registers_table);

	s = gtk_widget_get_style(debugger_registers_table);

	// setup base size of text box
	width = gdk_string_width(s->font, "_>FFFF_");
	height = ADJUST_HEIGHT(gdk_string_height(s->font, "j~`")) + 4;

//	gtk_style_unref(s);

	// fix the items below the register table here...
	gtk_widget_set_usize(debugger_pc_entry, width, height);
	gtk_widget_set_usize(debugger_wp_entry, width, height);
	gtk_widget_set_usize(debugger_st_entry, width, height);

	// resize
	gtk_table_resize(t, 16 /*rows*/, 2 /*columns*/);

	// set up each register row
	for (reg = 0; reg < 16; reg++) {
		// assign label...
		char tmp[32];
		char *com;
		sprintf(tmp, "R%d", reg);
		
		w = gtk_label_new(tmp);
		gtk_widget_ref(w);
		gtk_object_set_data_full(GTK_OBJECT(debugger_window), 
								 widget_tag("register_label_", reg, 1),
								 w, (GtkDestroyNotify) gtk_widget_unref);
		gtk_widget_show(w);
		gtk_table_attach(t, w, 0, 1, reg, reg+1,
						 (GtkAttachOptions) (0),
						 (GtkAttachOptions) (0), 2, 0);

		// assign text entry to register

		w = gtk_text_new(NULL, NULL);
		gtk_text_set_editable(GTK_TEXT(w), true);
		gtk_text_set_word_wrap(GTK_TEXT(w), false);
		gtk_text_set_line_wrap(GTK_TEXT(w), false);
		gtk_widget_ref(w);

		// force size
		gtk_widget_set_usize(w, width, height);

		gtk_object_set_data_full(GTK_OBJECT(debugger_window),
								 widget_tag("reg_value_", reg, 1),
								 w, (GtkDestroyNotify) gtk_widget_unref);

		// callback
		sprintf(tmp, "SetRAM C WP()+%d", reg*2);
		com = g_strdup(tmp);
		gtk_signal_connect (GTK_OBJECT (w), "activate",
							GTK_SIGNAL_FUNC (on_debugger_editable_activate),
							com);
		gtk_signal_connect (GTK_OBJECT (w), "event",
							GTK_SIGNAL_FUNC (on_debugger_editable_event),
							com);
		

		gtk_table_attach(t, w, 1, 2, reg, reg+1,
						 (GtkAttachOptions) (0),
						 (GtkAttachOptions) (0), 2, 0);

		gtk_widget_show(w);
	}
}

static void
update_debugger_register(status_item item, int reg, int val)
{
	GtkTable *t = GTK_TABLE(debugger_registers_table);
	GtkText *tb;
	GtkWidget *w;
	GtkStyle *style;
	char buffer[8];

	// get value widget
	w = table_get_widget(t, reg, 1);

	if (!VALID_WINDOW(w))
		return;

	// get style
	style = gtk_widget_get_style(w);

	// freeze entry
	tb = GTK_TEXT(w);
	gtk_text_freeze(tb);
	
	// remove old text
	gtk_editable_delete_text(GTK_EDITABLE(tb), 0, -1);

	// insert new text
	sprintf(buffer, ">%04X", val);
	gtk_text_insert(tb, 
					style->font,
					debugger_status_color_fg(item, style),
					debugger_status_color_bg(item, style),
					buffer,
					5);
					
	// update
	gtk_text_thaw(tb);
}

/*
 *	Setup memory windows for the first time.
 *
 *	Each entry in the vbox contains a frame with a scrolled window inside.
 */

#define MEMORY_BYTES_PER_ROW	16

static char *memory_frame_names[MEMORY_VIEW_COUNT] =
{
	"cpu_1_memory_frame",
	"cpu_2_memory_frame",
	"video_memory_frame",
	"graphics_memory_frame",
	"speech_memory_frame"
};

static char *memory_view_names[MEMORY_VIEW_COUNT] =
{
	"cpu_view_1",
	"cpu_view_2",
	"video_view",
	"graphics_view",
	"speech_view"
};

/*	This event is passed to the GtkViewport around a GtkText */
static void
on_debugger_memory_window_size_request_event (GtkWidget       *widget,
                                        GtkRequisition  *requisition,
                                        gpointer         user_data)
{
	GtkWidget *v;
	GtkWidget *tb;
	GtkStyle *s;
	int width, height;

	v = widget;
	g_return_if_fail(GTK_IS_TEXT(user_data));

	tb = GTK_WIDGET(user_data);
	s = gtk_widget_get_style(tb);

	height = ADJUST_HEIGHT(gdk_string_height(s->font, "!j~`")) * 5;
	width = gdk_string_width(s->font, "F");

	if (requisition->width < width) {
		requisition->width = width;
		//gtk_widget_set_usize(widget, width, -2);		// ??? -2 ???
	}
	if (requisition->height < height) {
		requisition->height = height;
		//gtk_widget_set_usize(widget, -2, height);		// ??? -2 ???
	}


//	requisition->width = 80;
//	requisition->height = 80;

//	g_print("requesting %d x %d\n", requisition->width, requisition->height);

}

static gboolean
on_debugger_memory_window_size_allocate_event  (GtkWidget       *widget,
                                        GtkAllocation 	*allocation,
                                        gpointer         user_data)
{
	GtkWidget *v, *tb;
	GtkStyle *s;
	int width, height, which;
	GtkScrolledWindow *sw;

	v = widget;
	g_return_val_if_fail(GTK_IS_TEXT(user_data), 0);

//	g_print("allocated %d x %d\n", allocation->width, allocation->height);

//	g_return_if_fail(tb = GTK_TEXT(GTK_BIN(v)->child));
	tb = GTK_WIDGET(user_data);

	s = gtk_widget_get_style(tb);

//	height = allocation->height / ADJUST_HEIGHT(gdk_string_height(s->font, "!"));
//	width = debugger_hex_dump_chars_to_bytes(allocation->width / (gdk_string_width(s->font, "F")) - 1);

	width = debugger_hex_dump_chars_to_bytes((allocation->width) / (gdk_string_width(s->font, "F")));
	height = (allocation->height - 4) / gdk_string_height(s->font, "!j~`");

//	g_print("width=%d, height=%d\n", width, height);
//	height = 256 / width;

	//width &= ~1;	// display whole words

	// in case the window was sized really small...
	if (height <= 0) height = 1;
	if (width <= 0) width = 1;

//	gtk_style_unref(s);

	gtk_object_set_data(GTK_OBJECT(tb), "view_width", (gpointer)width);
	gtk_object_set_data(GTK_OBJECT(tb), "view_height", (gpointer)height);

	which = (int)gtk_object_get_data(GTK_OBJECT(tb), "which");
	debugger_memory_view_size[which] = width * height;

	sw = GTK_SCROLLED_WINDOW(v->parent);
	if (!sw) return FALSE;

//	adj = (GtkAdjustment*)gtk_adjustment_new(0, 0, 65536, 1, 64, 32);

	//gtk_scrolled_window_set_hadjustment(sw, 0L);
	//gtk_scrolled_window_set_vadjustment(sw, adj);

	//gtk_scrolled_window_set_vadjustment(sw, 0L);
	//gtk_text_set_adjustments(tb, 0L, 0L);

	return FALSE;
}


static void
on_scrolled_window_changed_event(GtkAdjustment *adjustment,
								 gpointer user_data)
{
}

static void
on_scrolled_window_value_changed_event(GtkAdjustment *adjustment,
									   gpointer user_data)
{
}


static void
setup_debugger_memory_views(void)
{
//	GtkWidget *win;
	GtkFrame *f;
	GtkWidget *w;
	GtkScrolledWindow *sw;
	GtkViewport *v;
	GtkText *tb;
	GtkStyle *s;

	int width, height;
	int idx;

	int default_tab_width;

	// get a feel for width of window
	// by trying a test line
	default_tab_width = 1;

	// setup base size of text box
	s = gtk_widget_get_style(debugger_window);
	width = gdk_string_width(s->font, "M") * (debugger_hex_dump_bytes_to_chars(6));
	height = ADJUST_HEIGHT(gdk_string_height(s->font, "!j`~"));
//	gtk_style_unref(s);

	idx = MEMORY_VIEW_CPU_1;
	while (idx < MEMORY_VIEW_COUNT) {
		w = gtk_object_get_data(GTK_OBJECT(debugger_window), 
								  memory_frame_names[idx]);

		// 'w' is a frame containing a GtkBin.
		// The GtkBin has a GtkScrolledWindow inside, whose scrollbar
		// is used to page over the entire range of memory.
		// Inside the GtkScrolledWindow is a GtkViewport which
		// contains a GtkText that displays only as much text as
		// is visible in the window.

		g_return_if_fail(f = GTK_FRAME(w));
	
		w = GTK_BIN(f)->child;
		g_return_if_fail(sw = GTK_SCROLLED_WINDOW(w));
		
		w = GTK_BIN(sw)->child;
		g_return_if_fail(v = GTK_VIEWPORT(w));

		w = GTK_BIN(v)->child;
		g_return_if_fail(tb = GTK_TEXT(w));

		gtk_text_set_editable (tb, false);

		// force size
		gtk_widget_set_usize(w, width, height);

		// don't wrap lines!
		gtk_text_set_line_wrap(tb, false);

		//adj = (GtkAdjustment*)gtk_adjustment_new(0, 0, 65536, 1, 64, 32);
		//gtk_scrolled_window_set_hadjustment(sw, 0L);
		//gtk_scrolled_window_set_vadjustment(sw, 0L);
		//gtk_scrolled_window_set_vadjustment(sw, adj);
		gtk_text_set_adjustments(tb, 0L, 0L);

		// set tab width
		tb->default_tab_width = default_tab_width;

		// rename the memory view to point to the scrolled window
		gtk_object_set_data_full(GTK_OBJECT(debugger_window), 
								 memory_view_names[idx],
								 sw,	
								 (GtkDestroyNotify) gtk_widget_unref);

		gtk_object_set_data(GTK_OBJECT(tb), 
							"which",
							(gpointer)idx);

		debugger_memory_view_size[idx] = MEMORY_BYTES_PER_ROW * 4;

		// watch for resizes so we can fill the memory view
		gtk_signal_connect (GTK_OBJECT (v), "size_allocate",
                      GTK_SIGNAL_FUNC (on_debugger_memory_window_size_allocate_event),
                      (gpointer)tb);

		gtk_signal_connect (GTK_OBJECT (v), "size_request",
                      GTK_SIGNAL_FUNC (on_debugger_memory_window_size_request_event),
                      (gpointer)tb);

		//gtk_widget_show(w);
		//gtk_container_add(GTK_CONTAINER(win), w);

		idx++;
	}
}

static void
update_memory_window(status_item item, Memory *mem)
{
	GtkText *tb;
	GtkViewport *v;
	GtkScrolledWindow *sw;
	GtkWidget *w;
	GtkStyle *style;

	char buffer[256];
	char *start, *end, *astart, *aend;
	int len;
	int offs;

	int width, height, which;

	// get our view
	w = gtk_object_get_data(GTK_OBJECT(debugger_window),
							memory_view_names[mem->which]);

//	if (!VALID_WINDOW(w))
//		return;
	g_return_if_fail(sw = GTK_SCROLLED_WINDOW(w));

	w = GTK_BIN(sw)->child;
	g_return_if_fail(v = GTK_VIEWPORT(w));

	w = GTK_BIN(v)->child;
	g_return_if_fail(tb = GTK_TEXT(w));

//	adj = gtk_scrolled_window_get_vadjustment(sw);
//	if (adj)
//		g_print("adj: %f-%f, %f\n", adj->lower, adj->upper, adj->value);

	width = (int)gtk_object_get_data(GTK_OBJECT(tb), "view_width");
	height = (int)gtk_object_get_data(GTK_OBJECT(tb), "view_height");
	which = (int)gtk_object_get_data(GTK_OBJECT(tb), "which");

	// get style
	style = gtk_widget_get_style((GtkWidget*)tb);

	// freeze entry
	gtk_text_freeze(tb);
	
	// remove old text
	gtk_editable_delete_text(GTK_EDITABLE(tb), 0, -1);

	offs = 0;

	while (offs < debugger_memory_view_size[which]) {
		// create new text
		debugger_hex_dump_line(mem, offs, width,
							   ' ', ' ', ' ', 
							   offs + width < debugger_memory_view_size[which]
							   ? '\n' : 0, 
							   buffer, sizeof(buffer),
							   &start, &end, &astart, &aend);
		len = strlen(buffer);

		if (!start) {
			start = end = buffer + len;
			astart = aend = buffer + len;
		}

		// insert normal text
		gtk_text_insert(tb, 
						style->font,
						debugger_status_color_fg(STATUS_MEMORY_VIEW, style),
						debugger_status_color_bg(STATUS_MEMORY_VIEW, style),
						buffer,
						start - buffer);
	
		// insert hex byte update text
		if (start < end) {
			gtk_text_insert(tb, 
							style->font,
							debugger_status_color_fg(item, style),
							debugger_status_color_bg(item, style),
							start,
							end - start);
		}

		// insert normal text
		if (end < astart) {
			gtk_text_insert(tb, 
							style->font,
							debugger_status_color_fg(STATUS_MEMORY_VIEW, style),
							debugger_status_color_bg(STATUS_MEMORY_VIEW, style),
							end,
							astart - end);
		}

		// insert ascii changed text
		if (astart < aend) {
			gtk_text_insert(tb, 
							style->font,
							debugger_status_color_fg(item, style),
							debugger_status_color_bg(item, style),
							astart,
							aend - astart);
		}

		// insert normal ascii text
		if (aend < buffer + len) {
			gtk_text_insert(tb, 
							style->font,
							debugger_status_color_fg(STATUS_MEMORY_VIEW, style),
							debugger_status_color_bg(STATUS_MEMORY_VIEW, style),
							aend,
							buffer + len - aend);
		}

		offs += width;
	}

	// update
	gtk_text_thaw(tb);
}

#define INSTRUCTION_BOX_MAX_LENGTH (256*1024)

static void
setup_debugger_instruction_box(void)
{
	GtkText *tb;
	GtkStyle *s;
	int width, height;

	g_return_if_fail(debugger_instruction_box);

	tb = GTK_TEXT(debugger_instruction_box);

	// set tab width
	tb->default_tab_width = 4;

	// setup base size of text box
	s = gtk_widget_get_style(debugger_instruction_box);
	width = gdk_string_width(s->font, "^") * 32;
	height = gdk_string_height(s->font, "!`j~") * 16;
//	gtk_style_unref(s);

//	width = 64;
//	height = 16;
	// force size
	gtk_widget_set_usize(debugger_instruction_box, width, height);
//	memset((void *)&req, 0, sizeof(req));
//	req.width = width;
//	req.height = height;
//	gtk_widget_size_request(debugger_instruction_box, &req);

	// don't wrap lines!
	gtk_text_set_line_wrap(tb, false);
}

static void
update_debugger_instruction(status_item item, bool show_verbose,
							Instruction *inst,
							char *hex, char *disasm,	// may be NULL
							char *op1, char *op2)
{
	char buffer[256];
	GtkStyle *style;
	GtkText *tb;
	int len, point;

	if (!VALID_WINDOW(debugger_instruction_box))
		return;

	// only deal with single instructions, ignore their effects
	if (item == STATUS_CPU_INSTRUCTION)
	{
		tb = GTK_TEXT(debugger_instruction_box);

		// delete old text
		len = gtk_text_get_length(tb);
		if (len > INSTRUCTION_BOX_MAX_LENGTH * 2) {
			gtk_text_freeze(tb);
			point = gtk_text_get_point(tb);
			gtk_text_set_point(tb, 0);
			gtk_text_forward_delete(tb, len - INSTRUCTION_BOX_MAX_LENGTH);
			gtk_text_set_point(tb, INSTRUCTION_BOX_MAX_LENGTH);
			gtk_text_thaw(tb);
		}

		len = sprintf(buffer, "%s %s %s\n",
					  hex, inst->name, disasm);

		style = gtk_widget_get_style(debugger_instruction_box);
		gtk_text_insert(tb,
						style->font,
						debugger_status_color_fg(item, style),
						debugger_status_color_bg(item, style),
						buffer,
						len);
		gtk_text_set_point(tb, gtk_text_get_length(tb));
	}
}

static void
ping_debugger_instruction_box(void)
{
/*
	GtkText *tb;
	g_return_if_fail(GTK_IS_TEXT(debugger_instruction_box));
	tb = GTK_TEXT(debugger_instruction_box);
	gtk_text_set_point(tb, gtk_text_get_length(tb));
	if (debugger_verbose_updates) {
		if (tb->freeze_count) gtk_text_thaw(tb);
	} else {
		gtk_text_insert(tb, NULL, NULL, NULL, "\n", 1);
		if (!tb->freeze_count) gtk_text_freeze(tb);
	}
*/
}

static void
update_debugger_entry(GtkWidget *entry, status_item item, u16 val)
{
	char buffer[32];

	if (!VALID_WINDOW(entry))
		return;

	sprintf(buffer, ">%04X", val);
	gtk_entry_set_text(GTK_ENTRY(entry), buffer);
}

static void v9t9_set_numeric_entry(const char *name, const char *val)
{
	if (val)
	{
		char command[256];
		if (*val == '>')
			snprintf(command, sizeof(command), "%s 0x%s", name, val+1);
		else
			snprintf(command, sizeof(command), "%s %s", name, val);
		GTK_send_command(command);
		//command_exec_text(command);
	}
}

static void v9t9_set_register_entry(const char *name, u16 val)
{
	if (val)
	{
		char command[256];
		snprintf(command, sizeof(command), "%s \"%04X\"", name, val);
		GTK_send_command(command);
		//command_exec_text(command);
	}
}

gboolean
on_debugger_editable_event             (GtkWidget       *widget,
                                        GdkEvent        *event,
                                        gpointer         user_data)
{
	if (event->type == GDK_KEY_PRESS 
	&& 	event->key.keyval == GDK_Return)
	{
		gtk_signal_emit_by_name(GTK_OBJECT(widget), "activate", user_data);
		return TRUE;
	}
	return FALSE;
}

void
on_debugger_editable_activate          (GtkEditable     *editable,
                                        gpointer         user_data)
{
	gchar *ptr, *chr;

	ptr = gtk_editable_get_chars(editable, 0, -1);
	if (!ptr) return;

	if (user_data && strcmp(user_data, "StatusRegister") == 0)
	{
		// interpret coded entry:
		// ((+|-|^)*(LAECOPX)*)*

		if (*ptr != '>')
		{
			int set = 1;		// 0=reset, -1=toggle
			int mask;

			u16 stat = status;
			chr = ptr;
			while (*chr)
			{
				mask = 0;
				if (strchr("+-^", *chr))
				{
					if (*chr == '+') set = 1;
					else if (*chr == '-') set = 0;
					else if (*chr == '^') set = -1;
				}
				else if (strchr("LAECOPXlaecopx", *chr))
				{
					if (toupper(*chr) == 'L')		mask = ST_L;
					else if (toupper(*chr) == 'A')	mask = ST_A;
					else if (toupper(*chr) == 'E')	mask = ST_E;
					else if (toupper(*chr) == 'C')	mask = ST_C;
					else if (toupper(*chr) == 'O')	mask = ST_O;
					else if (toupper(*chr) == 'P')	mask = ST_P;
					else if (toupper(*chr) == 'X')	mask = ST_X;

					if (set > 0)
						if (chr == ptr)
							stat = mask;	// if no punct, reset status
						else
							stat |= mask;
					else if (set < 0)
						stat ^= mask;
					else
						stat &= ~mask;
				}
				else 
				{
					logger(_L|LOG_WARN, _("Character '%c' not understood\n"),
						   *chr);
				}
				chr++;
			}

			g_free(ptr);
			ptr = g_strdup_printf("0x%04x", stat);
		}
		v9t9_set_numeric_entry("StatusRegister", ptr);
		report_status(STATUS_CPU_STATUS, status);
	}
	else if (user_data)
	{
		if (strstr(user_data, "WP()"))
		{
			u16 val;
			if (*ptr == '>')
				val = strtoul(ptr+1, &chr, 16);
			else
				val = strtoul(ptr, &chr, 0);
			v9t9_set_register_entry(user_data, val);
			debugger_register_clear_view();
		}
		else
		{
			v9t9_set_numeric_entry(user_data, ptr);

			// refresh UI
			report_status(STATUS_CPU_WP, wp);
			report_status(STATUS_CPU_PC, pc);
		}
	}
	else
		logger(_L|LOG_INTERNAL, "no user_data set for editable\n");

	g_free(ptr);
}


/***************/

static int debugger_verbose_update_count;

void
gtk_debugger_report_status(status_item item, va_list va)
{
	bool show_verbose = execution_paused() || debugger_verbose_updates ||
		debugger_verbose_update_count != 0;

	if (!VALID_WINDOW(debugger_window))
		return;

	switch (item)
	{
	case STATUS_DEBUG_REFRESH:
		if (debugger_verbose_update_count) {
			debugger_register_clear_view();
			debugger_memory_clear_views();
			debugger_instruction_clear_view();
			debugger_verbose_update_count--;
		}

		break;
	case STATUS_CPU_PC:
		if (show_verbose) {
			update_debugger_entry(debugger_pc_entry, item, va_arg(va, int));
		}
		break;

	case STATUS_CPU_STATUS:
		if (show_verbose) {
			update_debugger_entry(debugger_st_entry, item, va_arg(va, int));
		}
		break;

	case STATUS_CPU_WP:
		if (show_verbose) {
			update_debugger_entry(debugger_wp_entry, item, va_arg(va, int));
		}
		break;

	case STATUS_CPU_REGISTER_READ:
	case STATUS_CPU_REGISTER_WRITE:
	{
		if (show_verbose) {
			int reg, val;
			reg = va_arg(va, int);
			val = va_arg(va, int);
			update_debugger_register(item, reg, val);
		}
		break;
	}

	case STATUS_CPU_REGISTER_VIEW:
	{
		if (show_verbose) {
			int wp;
			u16 *regs;
			int reg;
			wp = va_arg(va, int);
			regs = va_arg(va, u16 *);
			for (reg = 0; reg < 16; reg++) {
				update_debugger_register(item, reg, regs[reg]);
			}
		}
		break;
	}

	case STATUS_CPU_INSTRUCTION:
	{
		if (show_verbose) {
		Instruction *inst;
		char *hex, *disasm, *op1, *op2;
		inst = va_arg(va, Instruction *);
		hex = va_arg(va, char *);
		disasm = va_arg(va, char *);
		op1 = va_arg(va, char *);
		op2 = va_arg(va, char *);

		update_debugger_instruction(item, show_verbose,
									inst, hex, disasm, op1, op2);
		ping_debugger_instruction_box();
		}
		break;
	}

	case STATUS_CPU_INSTRUCTION_LAST:
	{
		if (show_verbose) {
		Instruction *inst;
		char *op1, *op2;
		inst = va_arg(va, Instruction *);
		op1 = va_arg(va, char *);
		op2 = va_arg(va, char *);

		update_debugger_instruction(item, show_verbose,
									inst, 0L, 0L, op1, op2);
		}
		break;
	}

	case STATUS_MEMORY_READ:
	case STATUS_MEMORY_WRITE:
	case STATUS_MEMORY_VIEW:
		if (show_verbose) {
			Memory *mem = va_arg(va, Memory *);
			update_memory_window(item, mem);
		}
		break;
	}
}

/***************/

static int debugger_run_tag;
int gtk_run_event_rate = 10;

static void
debugger_run_event(void)
{
	debugger_verbose_update_count = 2;
//1	debugger_refresh();
	debugger();
}

static void  set_update_rate(void)
{
	if (gtk_run_event_rate <= 0)
		gtk_run_event_rate = 1;
	else if (gtk_run_event_rate >= TM_HZ)
		gtk_run_event_rate = TM_HZ;

	if (debugger_run_tag)
	{
		TM_ResetEvent(debugger_run_tag);
		TM_SetEvent(debugger_run_tag, TM_HZ*100/gtk_run_event_rate, 0, 
					TM_FUNC|TM_REPEAT, TM_EVENT_FUNC(debugger_run_event));
	}
}

DECL_SYMBOL_ACTION(gtk_debugger_set_update_rate)
{
	if (task == csa_WRITE)
		set_update_rate();
	return 1;
}

void
debugger_change_verbosity(bool verbose)
{
	if (debugger_verbose_updates != verbose) {
		debugger_verbose_updates = verbose;
		ping_debugger_instruction_box();
		if (!verbose) {
			if (!debugger_run_tag) {
				debugger_run_tag = TM_UniqueTag();
			}
			set_update_rate();
		} else {
			if (debugger_run_tag) {
				TM_ResetEvent(debugger_run_tag);
				debugger_run_tag = 0;
			}
		}
	}
}

void
on_v9t9_debug_button_clicked           (GtkButton       *button,
                                        gpointer         user_data)
{
/*
	GtkWidget *label = GTK_BIN(button)->child;

	debugger_enable(!debugger_enabled());

	if (debugger_enabled()) {
		gtk_label_set_text(GTK_LABEL(label), "Stop Tracing");
	} else {
		gtk_label_set_text(GTK_LABEL(label), "Trace");
	}
*/

	execution_pause(true);
	debugger_enable(true);
	debugger_change_verbosity(true);
}

void
on_debugger_close_button_clicked       (GtkButton       *button,
                                        gpointer         user_data)
{
	/* turn off background debugging so we can close the window */
//	debugger_change_verbosity(true);
	debugger_enable(false);
}

void
on_debugger_run_button_clicked         (GtkButton       *button,
                                        gpointer         user_data)
{
	debugger_change_verbosity(false);
	ping_debugger_instruction_box();
	execution_pause(false);
	stateflag &= ~ST_DEBUG;
//	debugger_enable(false);
}


void
on_debugger_walk_button_clicked        (GtkButton       *button,
                                        gpointer         user_data)
{
	debugger_change_verbosity(true);
	ping_debugger_instruction_box();
	execution_pause(false);
	stateflag |= ST_DEBUG;
//	debugger_enable(true);
}


void
on_debugger_break_button_clicked        (GtkButton       *button,
                                        gpointer         user_data)
{
//	debugger_enable(true);
	stateflag |= ST_DEBUG;
	debugger_change_verbosity(true);
	execution_pause(true);
//1	debugger_refresh();
//	if (!(stateflag & ST_PAUSE))
//		stateflag |= ST_SINGLESTEP;
	ping_debugger_instruction_box();
}

/*	Look at the routine at addr and see how it changes 'reg'.
 *	Usually the routine does:
 *
 *		MOV *reg+,...
 *		...
 *
 *	Just look at modifies of 'reg'; if it is killed, give up.
 */
static u16 debugger_guess_argument_count(u16 addr, u8 reg)
{
	Instruction inst;
	u16 delta = 0;
	int max = 1024;
	while (max--)
	{
		addr = dis9900_decode(MEMORY_READ_WORD(addr), addr, wp, 0,
							 flat_read_word, &inst);
		/*if (inst.jump == OP_JUMP_COND)
		{
			addr = inst.op1.ea;
			continue;
			}*/
		if (inst.op1.type == OP_INC && inst.op1.val == reg)
			delta++;
		if (inst.op2.type == OP_INC && inst.op2.val == reg)
			delta++;

		/*
		if (inst.op1.type == OP_REG && inst.op1.val == reg 
			&& inst.op1.dest == OP_DEST_KILLED)
			break;
		if (inst.op2.type == OP_REG && inst.op2.val == reg 
			&& inst.op2.dest == OP_DEST_KILLED)
			break;
		*/
		if (inst.inst == Ib && inst.op1.type == OP_IND && inst.op1.val == reg)
			break;
		if (inst.inst == Irtwp)
			break;
	}

	logger(LOG_USER, "routine appears to use %d words\n", delta);
	return delta;
}

/*	Guess the next instruction at the same level (i.e.,
	skipping calls).  If this is a conditional jump,
	return 0 because we don't know. 
*/
static int
debugger_get_next_linear_instruction(u16 pc, u16 *next)
{
	u16 bpc;
	Instruction inst;

	bpc = dis9900_decode(MEMORY_READ_WORD(pc), pc, wp, 0,
						 flat_read_word, &inst);

	if (inst.inst == Ibl)
		bpc += debugger_guess_argument_count(inst.op1.ea, 11)*2;
	else if (inst.inst == Iblwp || inst.inst == Ixop)
		bpc += debugger_guess_argument_count(inst.op1.ea, 14)*2;
	else if (inst.inst == Irtwp)
		bpc = MEMORY_READ_WORD(wp+14*2);
	else if (inst.jump == OP_JUMP_COND)
	{
		*next = bpc;
		// who knows?
		return 0;
	}
	else if (inst.jump)
		bpc = inst.op1.ea;

	*next = bpc;
	return 1;
}

void
on_debugger_next_button_clicked       (GtkButton       *button,
                                        gpointer         user_data)
{
	u16 bpc;

	if (!debugger_get_next_linear_instruction(pc, &bpc))
	{
		logger(LOG_USER, "conditional jump: single stepping\n");
		stateflag |= ST_DEBUG;
		debugger_change_verbosity(true);
		execution_pause(true);
		stateflag |= ST_SINGLESTEP;
		ping_debugger_instruction_box();
	}
	else
	{
		logger(LOG_USER, _("setting breakpoint at >%04X\n"), bpc);

		debugger_set_pc_breakpoint(bpc, true /*temporary*/);
		debugger_change_verbosity(false);
		ping_debugger_instruction_box();
		execution_pause(false);
	}
}


void
on_debugger_after_button_clicked       (GtkButton       *button,
                                        gpointer         user_data)
{
	u16 bpc;

	debugger_get_next_linear_instruction(pc, &bpc);

	logger(LOG_USER, _("setting breakpoint at >%04X\n"), bpc);

	stateflag |= ST_DEBUG;
	debugger_set_pc_breakpoint(bpc, true /*temporary*/);
	debugger_change_verbosity(false);
	ping_debugger_instruction_box();
	execution_pause(false);
}


void
on_debugger_step_button_clicked        (GtkButton       *button,
                                        gpointer         user_data)
{
//	debugger_enable(true);
	stateflag |= ST_DEBUG;
	debugger_change_verbosity(true);
	execution_pause(true);
	stateflag |= ST_SINGLESTEP;
	ping_debugger_instruction_box();
}



