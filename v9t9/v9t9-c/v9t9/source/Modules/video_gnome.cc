/*
  video_gnome.c					-- V9t9 module for GNOME video interface

  (c) 1994-2001 Edward Swartz

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

/*
  $Id$
 */

/*
 *	Video module for Gnome
 *	
 *	This writes to the GtkDrawingArea inside the v9t9_window
 */
#include <config.h>
#include "v9t9_common.h"

#if defined(UNDER_UNIX)
#include <gdk/gdkx.h>
#include <math.h>
#elif defined(UNDER_WIN32)
#define WIN32_LEAN_AND_MEAN
#include <gdk/win32/gdkwin32.h>
#endif

#include <gnome.h>

#include "gnomeinterface.h"
#include "gnomecallbacks.h"
#include "gnomeloop.h"

//#include "clstandardtypes.h"
//#include "v9t9_module.h"
//#include "emulate.h"
#include "video.h"
#include "vdp.h"
#include "memory.h"
#include "9900.h"
#include "v9t9.h"
#include "timer.h"

#define _L LOG_VIDEO


////////////////////////////////////
//	Normal mode

extern "C" {

static GdkColor v9t9_gdk_palette[17];

//	GC for each color we'll display...
static GdkGC	*v9t9_gdk_colors[17];

//	true if colors 0 and 17 can be changed without redrawing
static gboolean	v9t9_gdk_paletted;

//	mapping from TI color (0=bg, 17=fg, others the same)
//	to v9t9_gdk_color[] entry
static gint		cmap[17];

static int use_composite_mode;
static int composite_tag;

static int composite_bright, composite_contrast, composite_tint, composite_color;

}

#if 0
#pragma mark -
#endif


static void
video_setpaletteentry(int index, int c)
{
	module_logger(&gtkVideo, _L | L_1, _("Setting index %d to color %d\n"), index, c);

	if (v9t9_gdk_paletted) {
		/* !!! */
	} else {
		cmap[index] = c == 0 ? vdpbg : c == 16 ? vdpfg : c;
	}
}

static void
video_updatepalette(void)
{
	int         x;

	for (x = 1; x < 16; x++)
		video_setpaletteentry(x, x);
	video_setpaletteentry(0, vdpbg);
	video_setpaletteentry(16, vdpfg);
}

static vmResult system_gtkvideo_detect(void);
static vmResult system_gtkvideo_init(void);
static vmResult system_gtkvideo_enable(void);
static vmResult system_gtkvideo_disable(void);
static vmResult system_gtkvideo_restart(void);
static vmResult system_gtkvideo_restop(void);
static vmResult system_gtkvideo_term(void);

static DECL_SYMBOL_ACTION(dump_screen)
{
	int i;
	int colorbase = (vdpregs[3] * 0x40) & 0x3fff;
	for (i = 0; i < 768; i++)
	{
		printf("%x ", 
			  ( domain_read_byte(md_video, colorbase+((domain_read_byte(md_video, i)&0xff)>>3))>>4)&0xf);
		if (i % 32 == 31) printf("\n");
	}

}

static      vmResult
gtkvideo_detect(void)
{
	return system_gtkvideo_detect();
}

static      vmResult
gtkvideo_init(void)
{
	command_symbol_table *gtkcommands =
		command_symbol_table_new(_("GTK Video Options"),
								 _("These commands control the GTK video emulation"),

		 command_symbol_new("DumpScreen",
							_("Dump screen info"),
							c_DONT_SAVE,
							dump_screen /* action */ ,
							NULL /* return */,
							 NULL /* next */
							,
	NULL /*next*/),
	 NULL /*sub*/,
	 NULL/*next*/
	);
			
	int i;

	command_symbol_table_add_subtable(universe, gtkcommands);

	for (i = 1; i < 16; i++)
	{
		v9t9_gdk_palette[i].red = RGB_8_TO_16(vdp_palette[i][0]);
		v9t9_gdk_palette[i].green = RGB_8_TO_16(vdp_palette[i][1]);
		v9t9_gdk_palette[i].blue = RGB_8_TO_16(vdp_palette[i][2]);
	}

	features |= FE_SHOWVIDEO;

	my_assert(v9t9_drawing_area);
	return system_gtkvideo_init();
}

static      vmResult
gtkvideo_enable(void)
{
	if (VALID_WINDOW(v9t9_window)) {
		gtk_widget_show(v9t9_window);
		return system_gtkvideo_enable();
	} else
		return vmInternalError;
}

static      vmResult
gtkvideo_disable(void)
{
	vmResult result;
	if (VALID_WINDOW(v9t9_window)) {
		if ((result = system_gtkvideo_disable()) != vmOk)
			return result;
		gtk_widget_hide(v9t9_window);
		return vmOk;
	} else
		return vmInternalError;
}

static      vmResult
gtkvideo_restart(void)
{
	// allocate our colors
	GdkColormap *map;
	int i;

	if (!VALID_WINDOW(v9t9_window)) {
		return vmInternalError;
	}

	map = gdk_window_get_colormap(v9t9_drawing_area->window);

	// we must get the middle colors
	for (i = 1; i <= 15; i++) {
		if (!gdk_colormap_alloc_color(map, 
									  v9t9_gdk_palette + i, 
									  false	/* writeable */,   
									  true	/* best_match */))
		{
			module_logger(&gtkVideo, _L|LOG_USER|LOG_ERROR, _("Could not allocate 15 colors\n"));
			return vmInternalError;
		}
	}

	// this appears to work, but shouldn't
#if 0
	// try to get bg and fg palettized
	gdk_colormap_alloc_colors(map,
							  v9t9_gdk_palette,
							  1,
							  true /* writeable */,
							  true /* best_match */,
							  &success);

	if (success) {
		gdk_colormap_alloc_colors(map,
								  v9t9_gdk_palette+16,
								  1,
								  true /* writeable */,
								  true /* best_match */,
								  &success);
		if (success) {
			v9t9_gdk_paletted = true;
			module_logger(&gtkVideo, _L|LOG_USER, _("Got writeable colors for bg/fg\n"));
		} else {
			v9t9_gdk_paletted = false;
			gdk_colormap_free_colors(map, v9t9_gdk_palette, 1);
		}
	} else 
#endif
	{
		v9t9_gdk_paletted = false;
		module_logger(&gtkVideo, _L, _("No writeable colors available\n"));
	}

	for (i = 0; i < 17; i++) {
		module_logger(&gtkVideo, _L|L_1, _("Color %d is %08X\n"), i, v9t9_gdk_palette[i].pixel);
	}

	//	Set up the colors in GCs that stay allocated
	for (i = 0; i < 17; i++) {
		v9t9_gdk_colors[i] = gdk_gc_new(v9t9_drawing_area->window);
		gdk_gc_set_foreground(v9t9_gdk_colors[i], v9t9_gdk_palette + i);
	}

	return system_gtkvideo_restart();
}

static      vmResult
gtkvideo_restop(void)
{
	GdkColormap *map;
	int i;
	vmResult result;

	result = system_gtkvideo_restop();
	if (result != vmOk)
		return result;

	if (!VALID_WINDOW(v9t9_window) || !GDK_IS_DRAWABLE(v9t9_window->window)) {
		return vmInternalError;
	}


	// free our colors
	for (i = 0; i < 17; i++) {
		gdk_gc_unref(v9t9_gdk_colors[i]);
		v9t9_gdk_colors[i] = 0L;
	}

	map = gdk_window_get_colormap(v9t9_drawing_area->window);
	if (v9t9_gdk_paletted) {
		gdk_colormap_free_colors(map, v9t9_gdk_palette, 1);
		gdk_colormap_free_colors(map, v9t9_gdk_palette+16, 1);
	}
    gdk_colormap_free_colors(map, v9t9_gdk_palette+1, 15);
		
	return vmOk;
}

static      vmResult
gtkvideo_term(void)
{
	if (!VALID_WINDOW(v9t9_window)) {
		return vmInternalError;
	}

	system_gtkvideo_term();
	gtk_widget_unref(v9t9_window);
	v9t9_drawing_area = 0L;
	v9t9_window = 0L;
	return vmOk;
}

/**************/
#if 0
#pragma mark -
#pragma mark [ X Windows ]
#endif

#if defined(UNDER_UNIX)

static      vmResult
system_gtkvideo_detect(void)
{
	return vmOk;
}

static      vmResult
system_gtkvideo_init(void)
{
	return vmOk;
}

static      vmResult
system_gtkvideo_enable(void)
{
	return vmOk;
}

static      vmResult
system_gtkvideo_disable(void)
{
	return vmOk;
}

static      vmResult
system_gtkvideo_restart(void)
{
	return vmOk;
}

static      vmResult
system_gtkvideo_restop(void)
{
	return vmOk;
}

static      vmResult
system_gtkvideo_term(void)
{
	return vmOk;
}

/*
 *	Use X11 stuff directly, since it's really fast
 */
static  void
gtk_update_area(struct updateblock *ptr, int num)
{
	int			width = 8;
	u8         *blk;
	XRectangle	points[17][64], *pptr[17];
	int			rects = 0, pixels = 0;
	int			xoffs = (256 - GTK_x_size) / 2;
	int         i;
	int         j;

	Display *display;
	Window window;

	int total = num, solid = 0;

	/*
	 * configure/expose callback should have been sent so we can
	 * figure out where it is 
	 */

	display = GDK_WINDOW_XDISPLAY(v9t9_drawing_area->window);
	window = GDK_WINDOW_XWINDOW(v9t9_drawing_area->window);
	my_assert(v9t9_drawing_area);

	if (!num)
		return;

	while (num--) {
		u8			c = 0;
		int			d;

		ptr->r *= GTK_y_mult;
		ptr->c = (ptr->c + xoffs) * GTK_x_mult;

		if (video_block_is_solid(ptr, !v9t9_gdk_paletted, &c) &&
			v9t9_gdk_colors[cmap[c]]) {
			XFillRectangle(display, window,
						   GDK_GC_XGC(v9t9_gdk_colors[cmap[c]]), 
						   ptr->c, 
						   ptr->r,
						   GTK_x_mult * width, 
						   GTK_y_mult * 8);
			solid++;
		}
		else
		{
			/* Reset lists for each color (remember text mode has color==16) */
			for (c = 0; c < 17; c++)
				pptr[c] = points[c];

			/* Generate a list of rectangles for each color
			   in the 8x8 block */
			for (i = 0; i < 8; i++) {
				j = 0;
				while (j < width) {
					/* Find the longest run of pixels of this color */
					c = ptr->data[j];
					d = 1;
					while (j + d < width && ptr->data[j + d] == c)
						d++;

					pptr[c]->x = ptr->c + j * GTK_x_mult;
					pptr[c]->y = ptr->r + i * GTK_y_mult;
					pptr[c]->width = GTK_x_mult * d;
					pptr[c]->height = GTK_y_mult;

					rects++;
					pixels += pptr[c]->width * pptr[c]->height;

					pptr[c]++;
					j += d;
				}
				ptr->data += UPDATEBLOCK_ROW_STRIDE;
			}

			for (c = 0; c <= 16; c++) {
				if (pptr[c] > points[c] && v9t9_gdk_colors[cmap[c]]) {
					XFillRectangles(display, window,
									GDK_GC_XGC(v9t9_gdk_colors[cmap[c]]), 
									points[c], 
									pptr[c] - points[c]);
				}
			}

		}

		ptr++;
	}

//	XFlush(GDK_WINDOW_XDISPLAY(v9t9_drawing_area->window));
	module_logger(&gtkVideo, _L | L_2, _("Drew %d rects, %d pixels\n"), rects, pixels);

	module_logger(&gtkVideo, _L|L_2, _("%d/%d solid\n"), solid, total);
}

#endif

/**************/

#if 0
#pragma mark -
#pragma mark [ Win32 ]
#endif

#if defined(UNDER_WIN32)

static GdkVisual *gdkvisual;
static GdkImage *gdkimage;

static      vmResult
system_gtkvideo_detect(void)
{
		/* get our visual, 8-bit paletted */
		
	gdkvisual = gdk_visual_get_best();
	
	if (!gdkvisual)
	{
		module_logger(&gtkVideo, _L|LOG_USER|LOG_ERROR, _("Could not get visual\n"));
		return vmNotAvailable;
	}
					
	return vmOk;
}

static      vmResult
system_gtkvideo_init(void)
{

	return vmOk;
}

static      vmResult
system_gtkvideo_enable(void)
{
		/* create a GdkImage for the v9t9 screen */
	
	gdkimage = gdk_image_new(GDK_IMAGE_SHARED_PIXMAP, gdkvisual, 256*4, 256*4);
	if (!gdkimage)
	{
		module_logger(&gtkVideo, _L|LOG_USER|LOG_ERROR, _("Could not get image\n"));
		return vmNotAvailable;
	}
	
	return vmOk;
}

static      vmResult
system_gtkvideo_disable(void)
{
	gdk_image_unref(gdkimage);
	
	return vmOk;
}

static      vmResult
system_gtkvideo_restart(void)
{
	return vmOk;
}

static      vmResult
system_gtkvideo_restop(void)
{
	return vmOk;
}

static      vmResult
system_gtkvideo_term(void)
{
	return vmOk;
}

#define MSBFirst 1
#define x11_24_order 0
int vwxsz;

#define vwxm GTK_x_mult
#define vwym GTK_y_mult
#define cmapping(x) v9t9_gdk_palette[cmap[x]].pixel

#include "video_X_draw.h"

static  void
gtk_update_area(struct updateblock *ptr, int num)
{
	int	width = 8;
	static int video_updating;
	RECT updaterect, rect;
	u8 *scrn;
	int i,j;
	int offs;

	drawpixelsfunc func;
	GdkGC *gc;
	
	if (video_updating)
		return;

	video_updating = 1;

	if (GTK_x_mult >= 1 && GTK_x_mult <= 4)
		func = drawpixels_NxN[GTK_x_mult-1][gdkimage->bpp-1];
	else
		func = drawpixels_XxY[gdkimage->bpp-1];
	
	gc = gdk_gc_new(v9t9_drawing_area->window);
	
	if (!gdkimage || !gdkimage->mem)
		return;
		
	SetRectEmpty(&updaterect);
	
	offs=(GTK_x_size/2)-128;
	while (num--)
	{
		scrn = (u8*)gdkimage->mem + 
			(ptr->r * GTK_y_mult * gdkimage->bpl) + 
			(ptr->c + offs) * GTK_x_mult * gdkimage->bpp;

		for (i = 0; i < 8; i++) {
			for (j = 0; j < GTK_y_mult; j++) {
				func(scrn, ptr->data, width);
				scrn += gdkimage->bpl;
			}
			ptr->data += UPDATEBLOCK_ROW_STRIDE;
		}
		
		SetRect(&rect, ptr->c, ptr->r, ptr->c + width, ptr->r + 8);
		UnionRect(&updaterect, &updaterect, &rect);
		ptr++;
	}

	gdk_draw_image(v9t9_drawing_area->window,
		gc,
		gdkimage,
		updaterect.left, updaterect.top,
		(updaterect.left + offs) * GTK_x_mult, updaterect.top * GTK_y_mult,
		(updaterect.right - updaterect.left) * GTK_x_mult,
		(updaterect.bottom - updaterect.top) * GTK_y_mult);
	
	gdk_gc_unref(gc);

	video_updating = 0;
}

#elif 0	// !defined(WIN32)

static  void
gtk_update_area(struct updateblock *ptr, int num)
{
	int width = 8;
	RECT updaterect, physrect, rect;
	u8 *scrn;
	PAINTSTRUCT pstruct;
	HDC hdc;
	int i,j;
	int offs;

	GdkGC *gc = gdk_gc_new(v9t9_drawing_area->window);
	
	if (!gdkimage || !gdkimage->mem)
		return;
		
	SetRectEmpty(&updaterect);
	
	offs=(GTK_x_size/2)-128;
	while (num--)
	{
//		scrn = gdkimage->mem + 
//			((ptr->r * 256) + ptr->c + offs) * gdkimage->bpp;
		for (i=0; i<8; i++)
		{
			for (j=0; j<width; j++)
				gdk_image_put_pixel(gdkimage, ptr->c+j, ptr->r+i,
					v9t9_gdk_palette[ptr->data[j]].pixel);
//			memcpy(scrn,ptr->data,8);
			ptr->data+=UPDATEBLOCK_ROW_STRIDE;
//			scrn += 256;
		}

		SetRect(&rect, ptr->c, ptr->r, ptr->c + width, ptr->r + 8);
		//window_invalidate(&rect);
		UnionRect(&updaterect, &updaterect, &rect);
		ptr++;
	}

	gdk_draw_image(v9t9_drawing_area->window,
		gc,
		gdkimage,
		updaterect.left, updaterect.top,
		updaterect.left + offs * GTK_x_mult, updaterect.top,
		updaterect.right - updaterect.left,
		updaterect.bottom - updaterect.top);
	
	gdk_gc_unref(gc);
}
#elif 0 // !defined(WIN32)

static  void
gtk_update_area(struct updateblock *ptr, int num)
{
	int			width = 8;
	u8         *blk;
	RECT		points[17][64], *pptr[17], *iter;
	int			rects = 0, pixels = 0;
	int			xoffs = (256 - GTK_x_size) / 2;

	HDC hdc = GetDC((HWND)GDK_WINDOW_XWINDOW((GdkWindowPrivate *)(v9t9_drawing_area->window)));
	
	while (num--) {
		int         i;
		int         j;
		u8			c;
		int			d;

		ptr->r *= GTK_y_mult;
		ptr->c = (ptr->c + xoffs) * GTK_x_mult;

		/* Reset lists for each color (remember text mode has color==16) */
		for (c = 0; c < 17; c++)
			pptr[c] = points[c];

		/* Generate a list of rectangles for each color
		   in the 8x8 block */
		for (i = 0; i < 8; i++) {
			j = 0;
			while (j < width) {
				/* Find the longest run of pixels of this color */
				c = ptr->data[j];
				d = 1;
				while (j + d < width && ptr->data[j + d] == c)
					d++;

				pptr[c]->left = ptr->c + j * GTK_x_mult;
				pptr[c]->top = ptr->r + i * GTK_y_mult;
				pptr[c]->right = GTK_x_mult * d;
				pptr[c]->bottom = GTK_y_mult;

				rects++;
				pixels += pptr[c]->right * pptr[c]->bottom;

				pptr[c]++;
				j += d;
			}
			ptr->data += UPDATEBLOCK_ROW_STRIDE;
		}

		for (c = 0; c <= 16; c++) {
			if (pptr[c] > points[c] && v9t9_gdk_colors[cmap[c]]) {
				iter = points[c];
				while (iter < pptr[c]) {
					gdk_draw_rectangle(v9t9_drawing_area->window,
									v9t9_gdk_colors[cmap[c]],
									TRUE /* filled */,
									iter->left,
									iter->top,
									iter->right,
									iter->bottom);
					iter++;
				}
			}
		}

		ptr++;
	}

	ReleaseDC((HWND)GDK_WINDOW_XWINDOW((GdkWindowPrivate *)(v9t9_drawing_area->window)), hdc);

//	XFlush(GDK_WINDOW_XDISPLAY(v9t9_drawing_area->window));
	module_logger(&gtkVideo, _L | L_2, _("Drew %d rects, %d pixels\n"), rects, pixels);
	
//	g_free(gc);
}
#endif	


#if 0
#pragma mark -
#endif

/*****************/

static      vmResult
gtkvideo_updatelist(struct updateblock *ptr, int num)
{
	gtk_update_area(ptr, num);
	return vmOk;
}

extern "C" void GTK_clear_sides(int total, int inside)
{
	if (inside < total && v9t9_gdk_colors[vdpbg])
	{
		int strip = (total - inside) * GTK_x_mult / 2;

		// clear sides
		gdk_draw_rectangle(v9t9_drawing_area->window, 
						   v9t9_gdk_colors[vdpbg], 1,
						   0, 0, 
						   strip, GTK_y_size * GTK_y_mult);
		gdk_draw_rectangle(v9t9_drawing_area->window, 
						   v9t9_gdk_colors[vdpbg], 1,
						   total * GTK_x_mult - strip, 0,
						   strip, GTK_y_size * GTK_y_mult);
	}
}

static      vmResult
gtkvideo_resize(u32 newxsize, u32 newysize)
{
	GTK_clear_sides(GTK_x_size, newxsize);
	GTK_x_size = newxsize;
	GTK_y_size = newysize;
	gtk_widget_queue_resize(v9t9_window);
	return vmOk;
}

static      vmResult
gtkvideo_setfgbg(u8 fg, u8 bg)
{
	video_setpaletteentry(0, bg);
	video_setpaletteentry(16, fg);
	if (!v9t9_gdk_paletted) {
		GTK_clear_sides(256, GTK_x_size);
		vdpcompleteredraw();
	}
	return vmOk;
}

static      vmResult
gtkvideo_setblank(u8 bg)
{
	int         x;

	for (x = 0; x <= 16; x++)
		video_setpaletteentry(x, bg);
	if (!v9t9_gdk_paletted) {
//		GTK_clear_sides(256, GTK_x_size);
		vdpcompleteredraw();
	}
	return vmOk;
}

static      vmResult
gtkvideo_resetfromblank(void)
{
	video_updatepalette();
	if (!v9t9_gdk_paletted) {
//		GTK_clear_sides(256, GTK_x_size);
		vdpcompleteredraw();
	}
	return vmOk;
}

/***********************************************************/

static vmVideoModule gtkvideo_videoModule = {
	3,
	gtkvideo_updatelist,
	gtkvideo_resize,
	gtkvideo_setfgbg,
	gtkvideo_setblank,
	gtkvideo_resetfromblank
};

extern "C" vmModule    gtkVideo = {
	3,
	"GTK+ video",
	"vidGTK",

	vmTypeVideo,
	vmFlagsExclusive,

	gtkvideo_detect,
	gtkvideo_init,
	gtkvideo_term,
	gtkvideo_enable,
	gtkvideo_disable,
	gtkvideo_restart,
	gtkvideo_restop,
	{(vmGenericModule *) & gtkvideo_videoModule}
};
