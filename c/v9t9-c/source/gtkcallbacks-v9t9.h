/*
 *	V9t9 emulator window and Command Central window callbacks.
 *
 */

/*
 *	Initial window size configured?
 */
int GTK_size_configured;

/*
 *	Size of TI screen
 */
int GTK_x_size, GTK_y_size;

/*
 *	Size of drawing area (as of last configure_event)
 *	and multiple of x_size and y_size this is 
 */
int GTK_x_pixels, GTK_y_pixels, GTK_x_mult, GTK_y_mult;
int GTK_user_size_configured;

/*
 *	"Pause" button
 */
GtkButton	*v9t9_window_pause_button;

/*
 *	Make a tag for a widget used in setting unique names
 *	for repeated members of an object
 */
static char *
widget_tag(const char *base, int number, int mag)
{
	static char widget_tag_buf[32];
	int len = strlen(base);
	unsigned int nyb = mag;
	memcpy(widget_tag_buf, base, len);
	while (nyb) {
		widget_tag_buf[len++] = "0123456789ABCDEF"[number & nyb];
		nyb >>= 4;
	}
	widget_tag_buf[len] = 0;
	return widget_tag_buf;
}

void
on_v9t9_window_destroy                 (GtkObject       *object,
                                        gpointer         user_data)
{
	gtk_main_quit();
	v9t9_sigterm(1);
}


gboolean
on_v9t9_draw_area_configure_event      (GtkWidget       *widget,
                                        GdkEventConfigure *event,
                                        gpointer         user_data)
{
/*	g_print("configure_event, ev =%d,%d req=%d,%d alloc=%d,%d\n",
			event->width, event->height,
			widget->requisition.width, widget->requisition.height,
			widget->allocation.width, widget->allocation.height);*/

#if defined(UNDER_WIN32)
	if (gtkVideo.runtimeflags & vmRTUnselected)
	{
		if (v9t9_drawing_area)
		{
			gtk_widget_destroy(v9t9_drawing_area);
			v9t9_drawing_area = NULL;
		}
	}
#endif

#if 0 && defined(UNDER_WIN32)	
	// an attempt to make an actual window be reparented
	// inside the v9t9_drawing_area... doesn't seem to work
	{
		GdkWindow    *window;
		GtkWidget *parent;
		GtkWidget *area;
		extern HWND hWndWindow;
		
		parent = v9t9_drawing_area->parent;
		window = gdk_window_foreign_new ((guint32) hWndWindow);
		gtk_widget_destroy(v9t9_drawing_area);
		v9t9_drawing_area = gtk_drawing_area_new();
		v9t9_drawing_area->window = window;
		gtk_widget_show(v9t9_drawing_area);
//		area = gtk_widget_new();
//		area->window = window;
		
		gtk_container_add(GTK_CONTAINER(parent), v9t9_drawing_area);
	}

#endif

	if (!GTK_x_size)	GTK_x_size = 256;
	if (!GTK_y_size)	GTK_y_size = 192;
	GTK_size_configured = 1;

	GTK_x_mult = (widget->allocation.width) / GTK_x_size;
	GTK_y_mult = (widget->allocation.height) / GTK_y_size;

	if (!GTK_x_mult)	GTK_x_mult = 1;
	if (!GTK_y_mult)	GTK_y_mult = 1;

	if (GTK_x_size * GTK_x_mult != GTK_x_pixels ||
		GTK_y_size * GTK_y_mult != GTK_y_pixels) 
	{
		GTK_x_pixels = GTK_x_size * GTK_x_mult;
		GTK_y_pixels = GTK_y_size * GTK_y_mult;
		vdpcompleteredraw();
	}

	return TRUE;
}

/*
 *	Make sure the size of the widget is a multiple of 256 and 192
 */
void
on_v9t9_draw_area_size_request         (GtkWidget       *widget,
                                        GtkRequisition  *requisition,
                                        gpointer         user_data)
{
	int psx, psy;
	GtkWidget *main;

/*	g_print("size_request, Req=%d,%d, req=%d,%d alloc=%d,%d\n",
			requisition->width, requisition->height,
			widget->requisition.width, widget->requisition.height,
			widget->allocation.width, widget->allocation.height);
	
	g_print("\tparent's parent's size is %d,%d\n", 
			widget->parent->parent->allocation.width,
			widget->parent->parent->allocation.height);*/

	// base our size on main window size
	main = widget->parent;
	while (main && !GTK_IS_WINDOW(main)) {
		main = main->parent;
	}

	g_assert(main);

	if (!GTK_x_size)	GTK_x_size = 256;
	if (!GTK_y_size)	GTK_y_size = 192;

	if (GTK_size_configured || GTK_user_size_configured) {

		// did user specify the size?
		if (GTK_user_size_configured) {
			psx = GTK_x_mult * 256;
			psy = GTK_y_mult * 192;
			GTK_user_size_configured = 0;
		} else {
			// else, use parent's size
			psx = widget->allocation.width;
			psy = widget->allocation.height;

			// ignore smaller window size and keep existing size
			if (psx < 256 || psy < 192) {
				psx = main->allocation.width;
				psy = main->allocation.height;
			}

			if (psx < 256 || psy < 192) {
				psx = 256;
				psy = 192;
			}
		} 

	} else {

		// if sizes not set up yet (say, by v9t9 reading -geometry),
		// take up 1/2 of the screen

		psx = gdk_screen_width() / 2 / GTK_x_size;
		psy = gdk_screen_height() / 2 / GTK_y_size;

		// fix to an aspected multiple of 256x192
		if (psx < 1) psx = 1;
		if (psy < 1) psy = 1;
		if (psx < psy) psx = psy;
		psx *= GTK_x_size;
		psy *= GTK_y_size;
	}

	GTK_size_configured = 1;

	if (psx > 16384) {
		psx = 16384;
	}
	if (psy > 16384) {
		psy = 16384;
	}

	// don't resize to 240 for text mode, since when the mode switches
	// back to graphics, the window will shrink to 1/2 size
	requisition->width = (psx / 256 /*GTK_x_size*/) * 256 /*GTK_x_size*/;
	if (requisition->width < 256 /*GTK_x_size*/) requisition->width = 256 /*GTK_x_size*/;
	requisition->height = (psy / GTK_y_size) * GTK_y_size;
	if (requisition->height < GTK_y_size) requisition->height = GTK_y_size;

}

/*
 *	set up window hints so user can only resize to a multiple of the
 *	VDP screen
 */
void
on_v9t9_draw_area_realize              (GtkWidget       *widget,
                                        gpointer         user_data)
{
	GdkGeometry hints;
	GtkWidget *app = GTK_WIDGET(user_data);


	hints.base_width = 0;
	hints.base_height = 0;

	hints.width_inc = 256;
	hints.height_inc = 192;
	hints.min_width = 256;
	hints.min_height = 192;
	
	gtk_window_set_geometry_hints(GTK_WINDOW(app),
								  GTK_WIDGET(widget),
								  &hints,
								  GDK_HINT_RESIZE_INC|GDK_HINT_MIN_SIZE|GDK_HINT_BASE_SIZE);

}


#if 0 && defined(UNDER_WIN32)
extern HWND hWndWindow;
extern LRESULT CALLBACK WindowWndProc( HWND hWnd, UINT messg, WPARAM wParam, LPARAM lParam );
void gtk_window_paint(RECT *updaterect);
#endif

gboolean
on_v9t9_draw_area_expose_event         (GtkWidget       *widget,
                                        GdkEventExpose  *event,
                                        gpointer         user_data)
{
	gint x,y,sx,sy;
	int xoffs;

	xoffs = (256 - GTK_x_size) * GTK_x_mult / 2;
	x = (event->area.x - xoffs);
	y = (event->area.y);

	sx = (event->area.width);
	sy = (event->area.height);

	// we can get expose events for stuff outside window
	if (x < 0)	{ sx += x;  x = 0; }
	if (x >= GTK_x_pixels) return FALSE;
	if (y < 0)	{ sy += y;  y = 0; }
	if (y >= GTK_y_pixels) return FALSE;

	if (sx + x > GTK_x_pixels)	sx = GTK_x_pixels - x;
	if (sy + y > GTK_y_pixels)	sy = GTK_y_pixels - y;

	sx = (sx + (x % GTK_x_mult) + GTK_x_mult - 1) / GTK_x_mult;
	sy = (sy + (y % GTK_y_mult) + GTK_y_mult - 1) / GTK_y_mult;
	x /= GTK_x_mult;
	y /= GTK_y_mult;

	logger(_L|L_2, _("for expose %d,%d, dirtying (%d,%d) to (%d,%d)\n"),
		   GTK_x_mult, GTK_y_mult, x,y,x+sx,y+sy);

	GTK_clear_sides(256, GTK_x_size);
	vdp_redraw_screen(x, y, sx, sy);

	return TRUE;
}


gboolean
on_v9t9_draw_area_draw                 (GtkWidget       *widget,
                                        GdkRectangle  *area,
                                        gpointer         user_data)
{
	return FALSE;
}

/*
 *	bring up command dialog on right click
 */
gboolean
on_v9t9_drawing_area_button_press_event
                                        (GtkWidget       *widget,
                                        GdkEventButton  *event,
                                        gpointer         user_data)
{
	// bring up v9t9 window
	if (event->button == 1) {
		gdk_window_raise(v9t9_window->window);
	}
	// raise command dialog to center on cursor
	else if (event->button > 1) {
		gdk_window_raise(command_center->window);
		gtk_widget_activate(command_center);
		if (event->button == 2) 
			gtk_widget_set_uposition(command_center, 
									 event->x_root - command_center->allocation.width / 2, 
									 event->y_root - command_center->allocation.height / 2);
	}
	return TRUE;
}


gboolean
on_v9t9_key_press_event                (GtkWidget       *widget,
                                        GdkEventKey     *event,
                                        gpointer         user_data)
{	
	GTK_keyboard_set_key(event->keyval, 1);
	return TRUE;
}


gboolean
on_v9t9_key_release_event              (GtkWidget       *widget,
                                        GdkEventKey     *event,
                                        gpointer         user_data)
{
	GTK_keyboard_set_key(event->keyval, 0);
	return TRUE;
}

#define GTK_RESTORE_FOCUS \
	gtk_widget_grab_focus(v9t9_window);\
	gtk_window_set_focus(GTK_WINDOW(v9t9_window), v9t9_drawing_area)

gboolean
on_v9t9_window_enter_notify_event      (GtkWidget       *widget,
                                        GdkEventCrossing *event,
                                        gpointer         user_data)
{
//	g_print("v9t9_window_enter\n");
	GTK_RESTORE_FOCUS;
	return FALSE;
}


gboolean
on_v9t9_draw_area_enter_notify_event   (GtkWidget       *widget,
                                        GdkEventCrossing *event,
                                        gpointer         user_data)
{
//	g_print("v9t9_drawing_area_enter\n");
	GTK_RESTORE_FOCUS;

	return FALSE;
}


