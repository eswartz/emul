/* GTK - The GIMP Toolkit
 * Copyright (C) 1995-1997 Peter Mattis, Spencer Kimball and Josh MacDonald
 *
 * modified (c) 1999-2001 by Edward Swartz for v9t9
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.  
 */

#ifndef __V99_FILESEL_H__
#define __V99_FILESEL_H__


#include <gdk/gdk.h>
#include <gtk/gtkwindow.h>
#include <gtk/gtkctree.h>
#include <gtk/gtkclist.h>

#ifdef __cplusplus
//extern "C" {
#endif /* __cplusplus */


#define V99_TYPE_FILE_SELECTION            (v99_file_selection_get_type ())
#define V99_FILE_SELECTION(obj)            (GTK_CHECK_CAST ((obj), V99_TYPE_FILE_SELECTION, V99FileSelection))
#define V99_FILE_SELECTION_CLASS(klass)    (GTK_CHECK_CLASS_CAST ((klass), V99_TYPE_FILE_SELECTION, V99FileSelectionClass))
#define V99_IS_FILE_SELECTION(obj)         (GTK_CHECK_TYPE ((obj), V99_TYPE_FILE_SELECTION))
#define V99_IS_FILE_SELECTION_CLASS(klass) (GTK_CHECK_CLASS_TYPE ((klass), V99_TYPE_FILE_SELECTION))


typedef struct _V99FileSelection       V99FileSelection;
typedef struct _V99FileSelectionClass  V99FileSelectionClass;

//	Callback to translate a filename entry into an item to add to the file_list
//typedef gchar *  (*v99_file_selection_file_set)(V99FileSelection *fs, gchar *text);

//	Callback to translate an item in the clist into a filename for caller
typedef gchar *  (*v99_file_selection_file_get)(V99FileSelection *fs, 
												GtkCList *clist,
												int row,
												gchar **text);

//	Callback to translate a filename entry into an item to add to the file_list
typedef int (*v99_file_selection_file_append)(V99FileSelection *fs, 
											  GtkCList *clist,
											  const gchar *path,
											  const gchar *text);

struct _V99FileSelection
{
	GtkWindow window;

	GtkWidget *dir_list;
	GtkWidget *file_list;

	GtkWidget *list_hbox;
	GtkWidget *file_list_scrolled_win;
	GtkWidget *dir_list_scrolled_win;

	guint 	file_list_select_row_handler_id;

	GtkWidget *selection_entry;
	GtkWidget *selection_text;
	GtkWidget *main_vbox;
	GtkWidget *ok_button;
	GtkWidget *cancel_button;
	GtkWidget *help_button;
	GtkWidget *history_pulldown;
	GtkWidget *history_menu;
	GList     *history_list;
	GtkWidget *fileop_dialog;
	GtkWidget *fileop_entry;
	gchar     *fileop_file;
	gpointer   cmpl_state;
  
	GtkWidget *fileop_c_dir;
	GtkWidget *fileop_del_file;
	GtkWidget *fileop_ren_file;
  
	GtkWidget *button_area;
	GtkWidget *action_area;

 	GtkWidget *path_list_vbox;

	gboolean  active_file_list;

	gpointer user_data;
	v99_file_selection_file_append user_file_append;
	v99_file_selection_file_get user_file_get;
};

struct _V99FileSelectionClass
{
  GtkWindowClass parent_class;
};


GtkType    v99_file_selection_get_type            (void);
GtkWidget* v99_file_selection_new                 (const gchar      *title);
void       v99_file_selection_set_filename        (V99FileSelection *filesel,
						   const gchar      *filename);
gchar*     v99_file_selection_get_filename        (V99FileSelection *filesel);
void	   v99_file_selection_complete		  (V99FileSelection *filesel,
						   const gchar	    *pattern);
void       v99_file_selection_show_fileop_buttons (V99FileSelection *filesel);
void       v99_file_selection_hide_fileop_buttons (V99FileSelection *filesel);

void		v99_file_selection_set_file_list_columns(V99FileSelection *filesel,
													 int columns,
													 gchar *titles[],
													 v99_file_selection_file_append ahandler,
													 v99_file_selection_file_get ghandler);
void		v99_file_selection_set_file_list_active(V99FileSelection *filesel, 
													gboolean active);

GtkCList	*v99_file_selection_add_path_list(V99FileSelection *filesel,
										 gchar *path_list_name,
										 gchar *path_list);

#ifdef __cplusplus
}
#endif /* __cplusplus */


#endif /* __V99_FILESEL_H__ */










