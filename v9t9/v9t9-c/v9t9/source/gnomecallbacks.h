
#include <gnome.h>

#include <centry.h>

/*
 *      Size of TI screen, set by VIDEO(resize)
 */
extern int GTK_x_size, GTK_y_size;

/*
 *      Size of drawing area (as of last configure_event)
 *      and multiple of x_size and y_size this is
 */
extern int GTK_x_pixels, GTK_y_pixels, GTK_x_mult, GTK_y_mult;
extern int GTK_user_size_configured;
 
/*
 *      Dirty v9t9 refresh buffer to force updates in
 *      response to Expose events
 */
void
GTK_dirty_screen(int x, int y, int sx, int sy);
 
/*
 *      Clear sides of screen in 9918 text mode
 */
void
GTK_clear_sides(int total, int inside);
                                                                                
/*
 *      Set or reset bit in CRU bitmap for keypress event
 */
void
GTK_keyboard_set_key(guint code, int state);
                                                                                
#define GTK_QUICK_SAVE 0
#define GTK_QUICK_LOAD 1
 

gboolean
on_v9t9_window_configure_event         (GtkWidget       *widget,
                                        GdkEventConfigure *event,
                                        gpointer         user_data);

gboolean
on_v9t9_window_enter_notify_event      (GtkWidget       *widget,
                                        GdkEventCrossing *event,
                                        gpointer         user_data);

void
on_v9t9_window_destroy                 (GtkObject       *object,
                                        gpointer         user_data);

gboolean
on_v9t9_draw_area_configure_event      (GtkWidget       *widget,
                                        GdkEventConfigure *event,
                                        gpointer         user_data);

gboolean
on_v9t9_draw_area_expose_event         (GtkWidget       *widget,
                                        GdkEventExpose  *event,
                                        gpointer         user_data);

gboolean
on_v9t9_key_press_event                (GtkWidget       *widget,
                                        GdkEventKey     *event,
                                        gpointer         user_data);

gboolean
on_v9t9_key_release_event              (GtkWidget       *widget,
                                        GdkEventKey     *event,
                                        gpointer         user_data);

void
on_v9t9_draw_area_size_request         (GtkWidget       *widget,
                                        GtkRequisition  *requisition,
                                        gpointer         user_data);

gboolean
on_v9t9_draw_area_enter_notify_event   (GtkWidget       *widget,
                                        GdkEventCrossing *event,
                                        gpointer         user_data);

gboolean
on_v9t9_drawing_area_button_press_event
                                        (GtkWidget       *widget,
                                        GdkEventButton  *event,
                                        gpointer         user_data);

void
on_v9t9_draw_area_realize              (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_v9t9_button_clicked                 (GtkButton       *button,
                                        gpointer         user_data);

void
on_v9t9_button_cancel                  (GtkButton       *button,
                                        gpointer         user_data);

void
on_command_log_font_selector_ok_button1_clicked
                                        (GtkButton       *button,
                                        gpointer         user_data);

void
on_command_log_font_cancel1_button_clicked
                                        (GtkButton       *button,
                                        gpointer         user_data);

void
on_command_log_font_selector_apply_button1_clicked
                                        (GtkButton       *button,
                                        gpointer         user_data);

void
on_disk_info_table_realize             (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_disk_choose_button_clicked          (GtkButton       *button,
                                        gpointer         user_data);

void
on_disk_combo_entry_activate           (GtkEditable     *editable,
                                        gpointer         user_data);

void
on_v9t9_togglebutton_toggled_command_toggle_not
                                        (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

void
on_v9t9_togglebutton_realize_inactive  (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_v9t9_togglebutton_toggled_widget_enable
                                        (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

void
on_v9t9_togglebutton_realize_widget_enable
                                        (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_v9t9_togglebutton_clicked           (GtkButton       *button,
                                        gpointer         user_data);

void
on_v9t9_togglebutton_realize_active    (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_v9t9_togglebutton_toggled_command_toggle
                                        (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

void
on_v9t9_rom_entry_activate             (GtkEditable     *editable,
                                        gpointer         user_data);

void
on_v9t9_rom_entry_realize              (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_v9t9_rom_button_clicked             (GtkButton       *button,
                                        gpointer         user_data);

void
on_emu_disk_cb_toggled                 (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

void
on_emu_disk_cb_realize                 (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_real_disk_cb_toggled                (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

void
on_real_disk_cb_realize                (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_disk_dialog_close_button_clicked    (GtkButton       *button,
                                        gpointer         user_data);

void
on_module_clist_click_column           (GtkTreeView        *clist,
                                        gint             column,
                                        gpointer         user_data);

void
on_module_clist_realize                (GtkWidget       *widget,
                                        gpointer         user_data);

gboolean
on_module_clist_event                  (GtkWidget       *widget,
                                        GdkEvent        *event,
                                        gpointer         user_data);

gboolean
on_clist_key_press_event               (GtkWidget       *widget,
                                        GdkEventKey     *event,
                                        gpointer         user_data);

gboolean
on_clist_key_release_event             (GtkWidget       *widget,
                                        GdkEventKey     *event,
                                        gpointer         user_data);

void
on_show_commands_cb_toggled            (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

void
on_reset_computer_cb_toggled           (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

void
on_unload_current_button_clicked       (GtkButton       *button,
                                        gpointer         user_data);

void
on_module_clist_close_button_clicked   (GtkButton       *button,
                                        gpointer         user_data);

void
on_module_clist_load_button_clicked    (GtkButton       *button,
                                        gpointer         user_data);

void
on_modules_refresh_button_clicked      (GtkButton       *button,
                                        gpointer         user_data);

void
on_debugger_editable_activate          (GtkEditable     *editable,
                                        gpointer         user_data);

void
on_debugger_close_button_clicked       (GtkButton       *button,
                                        gpointer         user_data);

void
on_debugger_run_button_clicked         (GtkButton       *button,
                                        gpointer         user_data);

void
on_debugger_step_button_clicked        (GtkButton       *button,
                                        gpointer         user_data);

void
on_debugger_after_button_clicked       (GtkButton       *button,
                                        gpointer         user_data);

void
on_debugger_break_button_clicked       (GtkButton       *button,
                                        gpointer         user_data);

void
on_debugger_walk_button_clicked        (GtkButton       *button,
                                        gpointer         user_data);

void
on_debugger_next_button_clicked        (GtkButton       *button,
                                        gpointer         user_data);

void
on_v9t9_spin_button_realize_value      (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_v9t9_spin_button_changed_value      (GtkEditable     *editable,
                                        gpointer         user_data);

void
on_memory_config_module_rom_button_realize
                                        (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_memory_config_module_rom_button_clicked
                                        (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

void
on_memory_config_banked_module_deactivate
                                        (GtkEditable     *editable,
                                        gpointer         user_data);

void
on_module_config_banked_module_activate
                                        (GtkEditable     *editable,
                                        gpointer         user_data);

void
on_memory_dialog_close_button_clicked  (GtkButton       *button,
                                        gpointer         user_data);

void
on_change_memory_map_button_clicked    (GtkButton       *button,
                                        gpointer         user_data);

void
on_v9t9_togglebutton_toggled_widget_enable_not
                                        (GtkToggleButton *togglebutton,
                                        gpointer         user_data);

void
on_v9t9_togglebutton_realize_widget_enable_not
                                        (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_v9t9_button_clicked_realize_widget  (GtkButton       *button,
                                        gpointer         user_data);

void
on_option_dialog_close_button_clicked  (GtkButton       *button,
                                        gpointer         user_data);

void
on_logging_log_table_realize           (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_logging_reset_all_clicked           (GtkButton       *button,
                                        gpointer         user_data);

void
on_logging_dialog_close_button_clicked (GtkButton       *button,
                                        gpointer         user_data);

void
on_fatal_dialog_ok_button_clicked      (GtkButton       *button,
                                        gpointer         user_data);

gboolean
on_completions_event                   (GtkWidget       *widget,
                                        GdkEvent        *event,
                                        gpointer         user_data);

void
on_memory_map_dialog_realize           (GtkWidget       *widget,
                                        gpointer         user_data);

void
on_memory_map_dialog_destroy           (GtkObject       *object,
                                        gpointer         user_data);

void
on_memory_map_dialog_refresh_clicked   (GtkButton       *button,
                                        gpointer         user_data);

gboolean
on_command_dialog_key_press_event      (GtkWidget       *widget,
                                        GdkEventKey     *event,
                                        gpointer         user_data);

void
on_command_dialog_destroy              (GtkObject       *object,
                                        gpointer         user_data);

void
on_flush_item_activate                 (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_font_item_activate                  (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_log_text_box_realize_event          (GtkWidget       *widget,
                                        gpointer         user_data);

gboolean
on_command_dialog_button_press_event   (GtkWidget       *widget,
                                        GdkEventButton  *event,
                                        gpointer         user_data);

void
on_command_text_entry_activate         (GtkEditable     *editable,
                                        gpointer         user_data);

gboolean
on_command_text_entry_key_press_event  (GtkWidget       *widget,
                                        GdkEventKey     *event,
                                        gpointer         user_data);

void
on_v9t9_pause_button_clicked           (GtkButton       *button,
                                        gpointer         user_data);

void
on_v9t9_quick_load_save_button_clicked (GtkButton       *button,
                                        gpointer         user_data);

void
on_v9t9_window_options_button_clicked  (GtkButton       *button,
                                        gpointer         user_data);

void
on_v9t9_debug_button_clicked           (GtkButton       *button,
                                        gpointer         user_data);

void
on_v9t9_window_module_button_clicked   (GtkButton       *button,
                                        gpointer         user_data);

void
on_v9t9_window_disks_button_clicked    (GtkButton       *button,
                                        gpointer         user_data);

void
on_v9t9_window_memory_button_clicked   (GtkButton       *button,
                                        gpointer         user_data);

void
on_quit_button_clicked                 (GtkButton       *button,
                                        gpointer         user_data);

void
on_v9t9_window_logging_button_clicked  (GtkButton       *button,
                                        gpointer         user_data);

void
on_new_session1_activate               (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_opensession1_activate               (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_savesession1_activate               (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_save_session_as1_activate           (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_load_configuration1_activate        (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_save_configuration1_activate        (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_save_configuration_as1_activate     (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_exit1_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_cut1_activate                       (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_copy1_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_paste1_activate                     (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_clear1_activate                     (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_properties1_activate                (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_preferences1_activate               (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_about1_activate                     (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_newsession1_activate                (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_open1_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_save1_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_save_as1_activate                   (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_load_configuration1_activate        (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_save_configuration1_activate        (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_save_configuration_as1_activate     (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_quit1_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_cut1_activate                       (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_copy1_activate                      (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_paste1_activate                     (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_clear1_activate                     (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_properties1_activate                (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_preferences1_activate               (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

void
on_about1_activate                     (GtkMenuItem     *menuitem,
                                        gpointer         user_data);

#include <cexit.h>
