/*
gnomecallbacks-memory.h

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
static GtkWidget *memory_dialog;

void
on_v9t9_window_memory_button_clicked   (GtkButton       *button,
                                        gpointer         user_data)
{
	if (!VALID_WINDOW(memory_dialog)) {
		memory_dialog = create_memory_dialog();
	} else {
		gtk_widget_hide(memory_dialog);
	}
	gtk_widget_show(memory_dialog);

	execution_pause(true);
}


void
on_memory_dialog_close_button_clicked  (GtkButton       *button,
                                        gpointer         user_data)
{
	gtk_widget_hide(memory_dialog);
	execution_pause(false);
	GTK_RESTORE_FOCUS;
}

/*
 *	Lookup and execute the first iteration of a dynamic command
 */
static int 
_v9t9_dynamic_command(const char *name, command_symbol **sym)
{
	if (!command_match_symbol(universe, name, sym))
		logger(_L|LOG_FATAL, _("Unknown command '%s'\n"), name);

	if (!((*sym)->flags & c_DYNAMIC))
		logger(_L|LOG_FATAL, _("'%s' is not c_DYNAMIC\n"), name);

	return (*sym)->action(*sym, csa_READ, 0);
}

/*
 *	Activate or deactivate a button that optionally loads
 *	a module ROM.  These conflict with the "LoadModule"
 *	command and the commands that load them won't be saved
 *	to the config file if they are empty.  We use this
 *	to tell whether they are being used.
 */
void
on_memory_config_module_rom_button_realize
                                        (GtkWidget       *widget,
                                        gpointer         user_data)
{
	GtkToggleButton *tb;
	command_symbol *sym;

	g_return_if_fail(GTK_IS_TOGGLE_BUTTON(widget));
	tb = GTK_TOGGLE_BUTTON(widget);

	/*
	 *	If the module entry is not loaded, activate the button.
	 */
	tb->active = (!_v9t9_dynamic_command("ReplaceModule", &sym));
}

/*
 *	User opted to set a custom module ROM file,
 *	this means we have to unload the module entry so
 *	this will have precedence.
 */
void
on_memory_config_module_rom_button_clicked
                                        (GtkToggleButton *togglebutton,
                                        gpointer         user_data)
{
	gpointer ptr;

	if (gtk_toggle_button_get_active(togglebutton))
	{
		/* Activate all the children */
		ptr = gtk_object_get_data((GtkObject *)memory_dialog, "module_rom_entry");
		if (ptr) gtk_signal_emit_by_name(GTK_OBJECT(ptr), "activate");
		ptr = gtk_object_get_data((GtkObject *)memory_dialog, "module_grom_entry");
		if (ptr) gtk_signal_emit_by_name(GTK_OBJECT(ptr), "activate");
		ptr = gtk_object_get_data((GtkObject *)memory_dialog, "module_rom1_entry");
		if (ptr) gtk_signal_emit_by_name(GTK_OBJECT(ptr), "activate");
		ptr = gtk_object_get_data((GtkObject *)memory_dialog, "module_rom2_entry");
		if (ptr) gtk_signal_emit_by_name(GTK_OBJECT(ptr), "activate");
	}
}

/*
 *	Activated the ROM entry, disable the banked ROM entries
 */
void
on_memory_config_banked_module_deactivate
                                        (GtkEditable     *editable,
                                        gpointer         user_data)
{
	gchar *str;
	g_return_if_fail(GTK_IS_WIDGET(user_data));
	
	str = gtk_entry_get_text(GTK_ENTRY(editable));
	if (str && *str)
		gtk_widget_set_sensitive(GTK_WIDGET(user_data), false);
	else
		gtk_widget_set_sensitive(GTK_WIDGET(user_data), true);

	/* v9t9 handles the memory map stuff */
}

/*
 *	Banked module entry activated
 */
void
on_module_config_banked_module_activate
                                        (GtkEditable     *editable,
                                        gpointer         user_data)
{
	gchar *str;
	g_return_if_fail(GTK_IS_WIDGET(user_data));
	
	str = gtk_entry_get_text(GTK_ENTRY(editable));
	if (str && *str)
		gtk_widget_set_sensitive(GTK_WIDGET(user_data), false);
	else
		gtk_widget_set_sensitive(GTK_WIDGET(user_data), true);

	/* v9t9 handles the memory map stuff */
}


/*
 *	Interface for manipulating the v9t9 memory map
 *
 *	The memory map will contain a GtkNotebook with pages for each memory type.
 *	Inside that we have two panes.  The left side contains a GtkCList with
 *	each MemoryEntry for the memory region listed.  The right side contains
 *	a graphical representation of the areas of memory defined by each
 *	MemoryEntry.  In the middle will be arrows connecting the sides.
 *
 */

static GtkWidget *memory_map_dialog;


void
on_memory_map_dialog_destroy           (GtkObject       *object,
                                        gpointer         user_data)
{
	memory_map_dialog = 0L;
}


static void memory_refresh(void)
{
	if (VALID_WINDOW(memory_map_dialog)) 
		on_memory_map_dialog_realize(memory_map_dialog, 0L);
}

// columns in mmap_mementlist
enum
{
	MME_ADDR,
	MME_SIZE,
	MME_REALSIZE,
	MME_NAME,
	MME_FILENAME,
	MME_FILEOFFS,
	MME_FLAGS,
	MME_LAST
};

static void on_mmap_column_clicked(GtkCList *clist, 
								   gint column, 
								   gpointer user_data)
{
	if (clist->sort_column == column)
		gtk_clist_set_sort_type(clist, clist->sort_type == GTK_SORT_ASCENDING ?
								GTK_SORT_DESCENDING : GTK_SORT_ASCENDING);
	else
	{
		gtk_clist_set_sort_column(clist, column);
		gtk_clist_set_sort_type(clist, GTK_SORT_ASCENDING);
	}
	gtk_clist_sort(clist);
}

static char *mmap_mement_get_flags(MemoryEntry *ent)
{
	static char strbuf[256];

	*strbuf = 0;

	if ((ent->flags & MEMENT_STORED) == MEMENT_STORED)
		strcat(strbuf, _("stored RAM "));
	else if (ent->flags & MEMENT_RAM)
		strcat(strbuf, _("RAM "));
	else
		strcat(strbuf, _("ROM "));

	if (ent->flags & MEMENT_BANK_1)
		strcat(strbuf, _("bank1 "));
	else if (ent->flags & MEMENT_BANK_2)
		strcat(strbuf, _("bank2 "));

	if (ent->flags & MEMENT_CART)
		strcat(strbuf, _("cart "));

	if (ent->flags & MEMENT_USER)
		strcat(strbuf, _("user "));

	return strbuf;
}

static void mmap_update_mement(GtkCList *clist, MemoryEntry *ent, int r)
{
	char mme_addr[8];
	char mme_size[12];
	char mme_realsize[8];
	char mme_fileoffs[8];

	char *items[MME_LAST] = {0};
	int c;

	snprintf(mme_addr, sizeof(mme_addr), ">%04X", ent->addr);
	items[MME_ADDR] = mme_addr;

	if (ent->size >= 0)
		snprintf(mme_size, sizeof(mme_size), ">%04X", ent->size);
	else
		snprintf(mme_size, sizeof(mme_size), ">%04X", -ent->size);
	items[MME_SIZE] = mme_size;

	snprintf(mme_realsize, sizeof(mme_realsize), ">%04X", ent->realsize);
	items[MME_REALSIZE] = mme_realsize;

	snprintf(mme_fileoffs, sizeof(mme_fileoffs), ">%04X", ent->fileoffs);
	items[MME_FILEOFFS] = mme_fileoffs;

	items[MME_FLAGS] = xstrdup(mmap_mement_get_flags(ent));

	items[MME_NAME] = ent->name;
	items[MME_FILENAME] = ent->filename;

	for (c = 0; c < MME_LAST; c++)
	{
		gtk_clist_set_text(clist, r, c, items[c]);
	}

	xfree(items[MME_FLAGS]);
}

static void mmap_update_mementlist(GtkCList *clist, mem_domain md)
{
	MemoryEntry *ent;

	gtk_clist_clear(clist);
	gtk_clist_freeze(clist);

	ent = mementlist;
	while (ent)
	{
		/* for each entry in our domain... */
		if ((ent->flags & MEMENT_DOMAIN) == (md << MEMENT_DOMAIN_SHIFT))
		{
			char *blanks[MME_LAST];
			int c, r;

			for (c = 0; c < MME_LAST; c++)
				blanks[c] = "";

			r = gtk_clist_append(clist, blanks);

			mmap_update_mement(clist, ent, r);
		}

		ent = ent->next;
	}

	gtk_clist_thaw(clist);
}

static void mmap_realize_page(GtkWidget *page, mem_domain md)
{
	GtkWidget *panes;
	GtkWidget *clist;
	GtkWidget *scrolled;

	int c;
	char *mme_titles[MME_LAST];

	/* remove stuff in the page */
	gtk_container_foreach(GTK_CONTAINER(page), (GtkCallback)gtk_widget_destroy, 0);

	/* scrolled window */
	scrolled = gtk_scrolled_window_new(NULL, NULL);
	gtk_widget_ref(scrolled);
	gtk_object_set_data_full (GTK_OBJECT (memory_map_dialog), "scrolled", 
							  scrolled,
							  (GtkDestroyNotify) gtk_widget_unref);

	gtk_widget_show(scrolled);
	gtk_scrolled_window_set_policy(GTK_SCROLLED_WINDOW(scrolled),
								   GTK_POLICY_AUTOMATIC, 
								   GTK_POLICY_AUTOMATIC);

	gtk_container_add(GTK_CONTAINER(page), scrolled);

	/* box for panes holding left/middle/right */
	panes = gtk_hbox_new(FALSE, 4);
	gtk_widget_ref (panes);
	gtk_object_set_data_full (GTK_OBJECT (memory_map_dialog), "sides", 
							  panes,
							  (GtkDestroyNotify) gtk_widget_unref);

	gtk_scrolled_window_add_with_viewport(GTK_SCROLLED_WINDOW(scrolled),
										  panes);

	/* clist in left side holding memory entries */

	clist = gtk_clist_new(MME_LAST);

	gtk_widget_ref(clist);
	gtk_widget_show(clist);

	gtk_object_set_data_full (GTK_OBJECT(memory_map_dialog), "clist",
							  clist,
							  (GtkDestroyNotify) gtk_widget_unref);

	gtk_box_pack_start(GTK_BOX(panes), clist, TRUE/*expand*/, TRUE/*fill*/, 0);

	mme_titles[MME_ADDR] = _("Address");
	mme_titles[MME_SIZE] = _("Max Size");
	mme_titles[MME_REALSIZE] = _("Size");
	mme_titles[MME_NAME] = _("Name");
	mme_titles[MME_FILENAME] = _("Filename");
	mme_titles[MME_FILEOFFS] = _("File offset");
	mme_titles[MME_FLAGS] = _("Flags");

	/* make all the column titles active so they can be sorted */
	for (c = 0; c < MME_LAST; c++)
	{
		gtk_clist_set_column_title(GTK_CLIST(clist), c, mme_titles[c]);
		gtk_clist_set_column_auto_resize(GTK_CLIST(clist), c, true);
		gtk_clist_column_title_active(GTK_CLIST(clist), c);
	}

	gtk_signal_connect(GTK_OBJECT(clist), "click_column", 
					   GTK_SIGNAL_FUNC(on_mmap_column_clicked),
					   (gpointer)0L);

	gtk_clist_column_titles_show(GTK_CLIST(clist));
	mmap_update_mementlist(GTK_CLIST(clist), md);

	gtk_clist_set_sort_column(GTK_CLIST(clist), MME_ADDR);
	gtk_clist_set_sort_type(GTK_CLIST(clist), GTK_SORT_ASCENDING);
	gtk_clist_sort(GTK_CLIST(clist));

	gtk_widget_show(panes);
}

/*
 *	when first realized, we have to fill in the notebook pages.
 */
void
on_memory_map_dialog_realize           (GtkWidget       *widget,
                                        gpointer         user_data)
{
	GtkNotebook *book;
	mem_domain md;
	gpointer *data;

	data = gtk_object_get_data(GTK_OBJECT(widget), "notebook");
	if (!data) return;

	book = GTK_NOTEBOOK(data);
	if (!book) return;

	for (md = md_cpu; md <= md_speech; md++)
	{
		GtkWidget *page = gtk_notebook_get_nth_page(book, md);
		if (!page) continue;

		mmap_realize_page(page, md);
	}
}

void
on_memory_map_dialog_refresh_clicked   (GtkButton       *button,
                                        gpointer         user_data)
{
	memory_refresh();
}

void
on_change_memory_map_button_clicked    (GtkButton       *button,
                                        gpointer         user_data)
{
	if (!VALID_WINDOW(memory_map_dialog)) {
		memory_map_dialog = create_memory_map_dialog();

		memory_register_callback(memory_refresh);
	} else {
		gtk_widget_hide(memory_map_dialog);
	}
	gtk_widget_show(memory_map_dialog);
}

