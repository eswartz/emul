/*
  render-gtk.c

  (c) 2010-2011 Edward Swartz

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
#include <gdk/gdk.h>
#include <gdk-pixbuf/gdk-pixbuf.h>
#include <stdlib.h>
#include <math.h>
#include "render.h"
#include "analogtv.h"

#include "noisy.h"

static void no_free(guchar *pixels, gpointer data) {
	//printf("ignoring free %p\n",pixels);
}

void 		renderGdkPixbufFromImageData(char *byteArray, int width, int height, int rowstride,
		int destWidth, int destHeight,
		int upx, int upy, int upwidth, int upheight,
		long long gdkWindow_) {
	GdkWindow* window = GDK_WINDOW((void*) gdkWindow_);
	GdkGC* gc = gdk_gc_new(window);

	GdkPixbuf* pixbuf = gdk_pixbuf_new_from_data(byteArray, GDK_COLORSPACE_RGB, 0, 8, width, height, rowstride,
			no_free, 0);
	GdkPixbuf* pixbuf_scaled;
	gdk_drawable_get_size(window, &destWidth, &destHeight);
	if (width != destWidth || height != destHeight) {
		pixbuf_scaled = gdk_pixbuf_new(GDK_COLORSPACE_RGB, 0, 8, destWidth, destHeight);
		gdk_pixbuf_scale(pixbuf, pixbuf_scaled, 0, 0, destWidth, destHeight, 0, 0,
				(double)destWidth /width, (double)destHeight / height, GDK_INTERP_NEAREST);
	} else {
		pixbuf_scaled = pixbuf;
		g_object_ref(pixbuf_scaled);
	}
	gdk_pixbuf_render_to_drawable(pixbuf_scaled, window, gc,
			upx, upy, upx, upy, upwidth, upheight,
			//0, 0, 0, 0, destWidth, destHeight,
			GDK_RGB_DITHER_NONE, 0, 0);
	g_object_unref(gc);
	g_object_unref(pixbuf_scaled);
	g_object_unref(pixbuf);
}

struct AnalogTV* allocateAnalogTv(int width, int height) {
	AnalogTV* tv = (AnalogTV*) calloc(sizeof(AnalogTV), 1);
	tv->tv = analogtv_allocate(width, height);
	analogtv_set_defaults(tv->tv);
	tv->input= analogtv_input_allocate();

	tv->tv->tint_control = 0;// 180;
	tv->tv->color_control = .85;
	tv->tv->width_control = 1.0;

	analogtv_setup_sync(tv->input, 1, 0);
	tv->rec = analogtv_reception_new();
	tv->rec->level = 1.0;
	tv->rec->input = tv->input;
	return tv;
}
void freeAnalogTv(struct AnalogTV* tv) {
	analogtv_release(tv->tv);
	free(tv->rec);
	free(tv->input);
}

void 		renderAnalogGdkPixbufFromImageData(
		AnalogTV* analog,
		char* byteArray, int width, int height, int rowstride,
		int destWidth, int destHeight,
		long long gdkWindow_) {

	analogtv_setup_sync(analog->input, 1, 0);
	analogtv_setup_frame(analog->tv);
	analogtv_load_rgb24(analog->tv, analog->rec->input, byteArray, width, height, rowstride);
	analogtv_init_signal(analog->tv, 0.0);
	analogtv_reception_update(analog->rec);
	analogtv_add_signal(analog->tv, analog->rec);
	analogtv_draw(analog->tv);

	GdkWindow* window = GDK_WINDOW((void*) gdkWindow_);
	GdkGC* gc = gdk_gc_new(window);

	/*printf("tv info: tvwidth=%d, tvheight=%d, width=%d, height=%d, destWidth=%d, destHeight=%d\n",
			analog->tv->usewidth, analog->tv->useheight,
			width, height, destWidth, destHeight);
*/

	width = analog->tv->usewidth;
	height = analog->tv->useheight;
	GdkPixbuf* pixbuf = gdk_pixbuf_new_from_data(analog->tv->image, GDK_COLORSPACE_RGB, 0, 8,
			width, height, analog->tv->bytes_per_line, no_free, 0);


	GdkPixbuf* pixbuf_scaled;
	gdk_drawable_get_size(window, &destWidth, &destHeight);
	if ((width != destWidth || height != destHeight)) {
		pixbuf_scaled = gdk_pixbuf_new(GDK_COLORSPACE_RGB, 0, 8, destWidth, destHeight);
		gdk_pixbuf_scale(pixbuf, pixbuf_scaled, 0, 0, destWidth, destHeight, 0, 0,
				(double)destWidth /width, (double)destHeight / height, GDK_INTERP_BILINEAR);
	} else {
		destWidth = gdk_pixbuf_get_width(pixbuf);
		destHeight = gdk_pixbuf_get_height(pixbuf);
		pixbuf_scaled = pixbuf;
		g_object_ref(pixbuf_scaled);
	}
	gdk_pixbuf_render_to_drawable(pixbuf_scaled, window, gc, 0, 0, 0, 0,
			destWidth, destHeight, GDK_RGB_DITHER_NONE, 0, 0);
	g_object_unref(gc);
	g_object_unref(pixbuf_scaled);
	g_object_unref(pixbuf);
}


void 		renderNoisyGdkPixbufFromImageData(
		char* byteArray, int width, int height, int rowstride,
		int destWidth, int destHeight,
		int upx, int upy, int upwidth, int upheight,
		long long gdkWindow_) {
	GdkWindow* window = GDK_WINDOW((void*) gdkWindow_);
	GdkGC* gc = gdk_gc_new(window);

	GdkPixbuf* pixbuf = gdk_pixbuf_new_from_data(byteArray, GDK_COLORSPACE_RGB, 0, 8, width, height, rowstride,
			no_free, 0);
	GdkPixbuf* pixbuf_scaled;
	//gdk_drawable_get_size(window, &destWidth, &destHeight);
	int addEffects = 1;

	// we're modifying the data, so always copy
	int scaleType = GDK_INTERP_NEAREST;
	if (1 || width != destWidth || height != destHeight) {
		// normally, zoom will be an integral multiple

		// if not, fuzzify it
		if (destHeight / height * height != destHeight)
			scaleType = GDK_INTERP_BILINEAR;
		else if (destWidth / width * width != destWidth)
			scaleType = GDK_INTERP_BILINEAR;

		pixbuf_scaled = gdk_pixbuf_scale_simple(pixbuf, destWidth, destHeight,
				scaleType);
	} else {
		pixbuf_scaled = pixbuf;
		g_object_ref(pixbuf_scaled);
	}

	if (addEffects) {
	 addNoise(gdk_pixbuf_get_pixels(pixbuf_scaled), 0,
			gdk_pixbuf_get_width(pixbuf_scaled),
			gdk_pixbuf_get_height(pixbuf_scaled), gdk_pixbuf_get_rowstride(pixbuf_scaled),
			width,
			height);
	}

	if (upheight + upy > destHeight)
		upheight = destHeight - upy;
	if (upwidth + upx > destWidth)
		upwidth = destWidth - upx;
	//printf("%d %d % d %d\n", upx, upy, upwidth, upheight);

	// adjust for blurring that touches adjacent pixels
	int adjFac = (scaleType == GDK_INTERP_NEAREST) ? 1 : 3;
	upx -= adjFac;
	if (upx < 0) {
		upwidth -= upx;
		upx = 0;
	}
	upwidth += adjFac*2;
	if (upx + upwidth > destWidth) upwidth = destWidth - upx;

	upy -= adjFac;
	if (upy < 0) {
		upheight -= upy;
		upy = 0;
	}
	upheight += adjFac*2;
	if (upy + upheight > destHeight) upheight = destHeight - upy;

	// since we're tweaking the drawable region,
	// opposed to what the expose event originally sent,
	// we need to tell GDK we're touching more.
	GdkRectangle rect = { upx, upy, upwidth, upheight };
	GdkRegion* region = gdk_region_rectangle(&rect);
	gdk_window_begin_paint_region(window, region);
	gdk_draw_pixbuf(window, gc, pixbuf_scaled,
			upx, upy, upx, upy, upwidth, upheight,
			GDK_RGB_DITHER_NONE, 0, 0);
	gdk_window_end_paint(window);
	gdk_region_destroy(region);

	g_object_unref(gc);
	g_object_unref(pixbuf_scaled);
	g_object_unref(pixbuf);

}

