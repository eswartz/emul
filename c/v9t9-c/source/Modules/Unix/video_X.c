/*
  video_x.c						-- V9t9 module for X11 video interface

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

#include "Xv9t9.h"

#include "v9t9_common.h"
#include "emulate.h"
#include "video.h"
#include "vdp.h"
#include "timer.h"
#include "memory.h"
#include "v9t9.h"

#define _L	LOG_VIDEO|LOG_INFO

#define USING_XIMAGE	0

extern XrmDatabase xlib_rDB;

Display    *x11_dpy;		// our display
int         x11_screen;		// our screen #

static char *v9t9_windowname = "V9t9 -- TI Emulator";
static char *v9t9_iconname = "V9t9";
static char *v9t9_classname = "v9t9";

bool        exposed;		// are we exposed and able to draw?

Visual     *x11_visual;		// the visual we're using
Colormap    x11_cmap;		// color map
int         x11_depth;		// visual depth (8,16,24)
int         x11_bytes;		// bytes per pixel (1,2,3,4)
int         x11_24_order;	// LSBFirst or MSBFirst for 24-bit
int         x11_class;		// color class
unsigned long x11_planes[1];// plane masks
bool        using_palette;	// using a read/write colormap?

unsigned long colormap[17];	// mapping from TI colors to X colors
unsigned long cmap[17];		// palette mapping

XSizeHints *vwin_size_hints; 
bool	vwin_iconified;		// in icon state?

static const char *Xcolornames[16] =
{
	"black", 
	"black", 
	"SeaGreen", 
	"MediumSeaGreen", 
	"SlateBlue", 
	"LightSlateBlue", 
	"IndianRed3", 
	"turquoise", 
	"brown1", 
	"salmon1", 
	"yellow2", 
	"goldenrod1", 
	"DarkOliveGreen", 
	"DarkOrchid", 
	"Gray88",
	"white"
};

Window      vwin;				// v9t9 screen
GC          vgc;				// video window GC

#if USING_XIMAGE
XImage     *vim;				// video window image
#endif

static int  dpy_xsize, dpy_ysize;	// size of display

static int  vwxoff, vwyoff;		// offset of window
static int  vwxsz, vwysz;		// (absolute) size of window
static int  vwxm, vwym;			// multipliers

static int  xsize, ysize;		// size of video bitmap

/**************************************/

#if USING_XIMAGE
typedef void (*drawpixelsfunc) (u8 * dst, const u8 * src, bool text);

#include "video_X_draw.h"

static drawpixelsfunc drawpixels;

static void
x_alloc_image(void)
{
	if (vim) {
		XDestroyImage(vim);
		vim = NULL;
	}
	//  Create an image that matches the visual and covers the whole
	//  visible TI screen.
	vim = XCreateImage(x11_dpy, x11_visual,
					   x11_depth, ZPixmap,
					   0, xmalloc(1), vwxsz, vwysz, 8, x11_bytes * vwxsz);

	if (vim == NULL)
		module_logger(&X_Video, _L|LOG_FATAL _("Could not create image for window\n"));

	x11_bytes = vim->bits_per_pixel / 8;
	if (x11_bytes == 3)
		x11_24_order = vim->byte_order;

	if (x11_bytes < 1 || x11_bytes > 4) {
		module_logger(&X_Video, _L|LOG_FATAL, _("cannot handle bits_per_pixel of %d\n")
			 vim->bits_per_pixel);
	}

	vim->data = (char *) xrealloc(vim->data, x11_bytes * vwxsz * vwysz);

	if (vwxm == vwym && vwxm >= 1 && vwxm <= 4)
		drawpixels = drawpixels_NxN[vwxm - 1][x11_bytes - 1];
	else
		drawpixels = drawpixels_XxY[x11_bytes - 1];
}

#endif

/**************************************/

static void
x_draw_sides(void)
{
	Region      region;
	XRectangle  rect;
	int width;

	if (xsize == 256)
		return;

	width = ((256 - xsize) / 2) * vwxm;
	XSetForeground(x11_dpy, vgc, cmap[0]);
	XFillRectangle(x11_dpy, vwin, vgc, 0, 0, width, vwysz);
	XFillRectangle(x11_dpy, vwin, vgc, vwxsz-width, 0, width, vwysz);
}


void
x_handle_video_event(XEvent * e)
{
	Region      region;
	XRectangle  rect;

	switch (e->type) {
	case Expose:
		// keep on selecting expose events.
		//region = XCreateRegion();
		exposed = true;
		rect.x = (short) e->xexpose.x;
		rect.y = (short) e->xexpose.y;
		rect.width = (unsigned short) e->xexpose.width;
		rect.height = (unsigned short) e->xexpose.height;
		module_logger(&X_Video, _L | L_2, _("dirtying rectangle (%d,%d+%d,%d)\n"),
			   rect.x, rect.y, rect.width, rect.height);
		vdp_redraw_screen(rect.x / vwxm, rect.y / vwym,
					   rect.width / vwxm, rect.height / vwym);
			//XUnionRectWithRegion(&rect, region, region);
		if (e->xexpose.count == 0) {
			//XSetRegion(x11_dpy, vgc, region);
			// vdpcompleteredraw();
			x_draw_sides();
//			VIDEO(update, ());
			vdp_update();
			rect.x = 0;
			rect.y = 0;
			rect.width = vwxsz;
			rect.height = vwysz;
			//XUnionRectWithRegion(&rect, region, region);
			//XSetRegion(x11_dpy, vgc, region);
			//XDestroyRegion(region);
		}
		break;

	case ColormapNotify:
		break;

	case ConfigureNotify:
		// window size might have changed.
		vwxoff = e->xconfigure.x;
		vwyoff = e->xconfigure.y;
		vwxsz = e->xconfigure.width;
		vwysz = e->xconfigure.height;
		module_logger(&X_Video, _L|0, _("Resize: vwxsz = %d, vwysz = %d\n"), vwxsz, vwysz);
		vwxm = vwxsz / 256;
		vwym = vwysz / 192;
//		XSetClipRectangles(x11_dpy, vgc, 0, 0, &e->xconfigure, 1, Unsorted);
		XSetClipMask(x11_dpy, vgc, None);
#if USING_XIMAGE
		x_alloc_image();
#endif
		vdpcompleteredraw();
		break;

	case EnterNotify:
	case FocusIn:
		XInstallColormap(x11_dpy, x11_cmap);
		break;
	case LeaveNotify:
	case FocusOut:
		XInstallColormap(x11_dpy, DefaultColormap(x11_dpy, x11_screen));
		break;

	case MapNotify:
		vwin_iconified = false;
		break;

	case UnmapNotify:
		vwin_iconified = true;
		break;
	}
}

/*****************************************************/

static void
video_setpaletteentry(int index, int c)
{
//  if (index != 0 && index != 16)
//      return;

	module_logger(&X_Video, _L | L_1, _("Setting index %d to color %d\n"), index, c);

	if (using_palette
		/*&& x11_cmap != DefaultColormap(x11_dpy, x11_screen) */ ) {
		XColor      color;

		if (index != 0 && index != 16) {
			module_logger(&X_Video, _L | L_1, _("Index is normal color\n"));
			cmap[index] = colormap[c];
			return;
		}

		module_logger(&X_Video, _L | L_1, _("Index is fg/bg color changing\n"));
		color.pixel = colormap[index];
		color.red = RGB_8_TO_16(vdp_palette[c][0]);
		color.green = RGB_8_TO_16(vdp_palette[c][1]);
		color.blue = RGB_8_TO_16(vdp_palette[c][2]);
		color.flags = DoRed | DoGreen | DoBlue;
		XStoreColor(x11_dpy, x11_cmap, &color);
		cmap[index] = colormap[index];
	} else {
		cmap[index] = colormap[c == 0 ? vdpbg : c == 16 ? vdpfg : c];
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

static      vmResult
X_video_detect(void)
{
	XVisualInfo *vinfo;
	int         nvinfo;
	XVisualInfo vtmp;
	static int  desired_classes[] = {
		PseudoColor, DirectColor, GrayScale, TrueColor, StaticColor,
		StaticGray
	};
	int         cl;

	static char *classes[] = {
		"StaticGray",
		"GrayScale",
		"StaticColor",
		"PseudoColor",
		"TrueColor",
		"DirectColor"
	};

	if (x11_dpy == NULL)
		return vmNotAvailable;

	vtmp.screen = x11_screen;

	x11_depth = DefaultDepth(x11_dpy, x11_screen);

	// most optimistic cases first
	for (cl = 0; cl < 6; cl++) {
		x11_class = desired_classes[cl];
		if (XMatchVisualInfo(x11_dpy, x11_screen, 8,
							 x11_class, &vtmp) ||
			XMatchVisualInfo(x11_dpy, x11_screen, x11_depth,
							 x11_class, &vtmp)) break;
	}

	if (cl >= 6) {
		module_logger(&X_Video, _L|LOG_ERROR | LOG_USER,
			 _("no compatible color classes found, cannot display\n"));
		return vmNotAvailable;
	}

	x11_depth = vtmp.depth;

	if (x11_depth <= 4) {
		module_logger(&X_Video, _L|LOG_ERROR | LOG_USER,
			 _("video_X:  Cannot use visual with only %d colors.\n"),
			 1 << (x11_depth - 1));
//		return vmNotAvailable;
	}

	x11_visual = vtmp.visual;

	module_logger(&X_Video, _L|L_0, _("color class = %s\n"), classes[x11_class]);

	module_logger(&X_Video, _L|LOG_USER, _("Detected X Window System...\n"));
	return vmOk;
}

static      vmResult
X_video_init(void)
{
	XWMHints   *wm_hints;
	XClassHint *class_hints;
	XTextProperty windowName, iconName;
	XSetWindowAttributes attributes;
	XGCValues   gcv;
	char       *str_type;
	XrmValue    value;
	int         gravity;
	bool		user_geometry;
	int			flags;
	int			i;

	exposed = false;

	dpy_xsize = DisplayWidth(x11_dpy, x11_screen);
	dpy_ysize = DisplayHeight(x11_dpy, x11_screen);

	/* Set up size hints for resizing and -geometry parsing */

	if ((vwin_size_hints = XAllocSizeHints()) == NULL) {
		module_logger(&X_Video, _L|LOG_ERROR | LOG_USER, _("cannot allocate size hints\n"));
		return vmInternalError;
	}

	vwin_size_hints->flags = PMinSize | PMaxSize | PResizeInc | PAspect | PBaseSize;
	vwin_size_hints->base_width = 0;
	vwin_size_hints->base_height = 0;
	vwin_size_hints->min_width = 256;
	vwin_size_hints->min_height = 192;
	vwin_size_hints->max_width = dpy_xsize;
	vwin_size_hints->max_height = dpy_ysize;
	vwin_size_hints->width_inc = 256;
	vwin_size_hints->height_inc = 192;
	vwin_size_hints->min_aspect.x = vwin_size_hints->max_aspect.x = 4;
	vwin_size_hints->min_aspect.y = vwin_size_hints->max_aspect.y = 3;

	/* Read sizes from resource */

	if (!XrmGetResource(xlib_rDB, "v9t9.geometry", "V9t9.Geometry",
						&str_type, &value)) {
		user_geometry = false;
		value.addr = 0L;
	} else {
		user_geometry = true;
	}

	/*	Parse geometry specification  */

	if ((flags = XWMGeometry(x11_dpy, x11_screen,
					(char *) value.addr,
					"1x1",
					1 /* border width */ , vwin_size_hints,
					&vwxoff, &vwyoff, &vwxsz, &vwysz, &gravity))) {

		if (vwxsz >= 256*256 && vwysz >= 192*192) {
			// assume they misunderstood the geometry and scale down
			vwxsz /= 256;
			vwysz /= 192;
		}

		if (user_geometry) {
			/*	Since user-specified, change position  */
	
			if (flags & (XValue | YValue)) {
				vwin_size_hints->flags |= USPosition;
				vwin_size_hints->x = vwxoff;
				vwin_size_hints->y = vwyoff;
			}
//			XSetWMNormalHints(x11_dpy, vwin, vwin_size_hints);
//			XMoveResizeWindow(x11_dpy, vwin, vwxoff, vwyoff, vwxsz, vwysz);
		}
	}

	vwxm = vwxsz / 256;
	vwym = vwysz / 192;

	vwin = XCreateWindow(x11_dpy, DefaultRootWindow(x11_dpy), 
						 vwxoff, vwyoff, 
						 vwxsz, vwysz, 1	/* border width */,
						 DefaultDepth(x11_dpy, x11_screen),
						 InputOutput, CopyFromParent,
						 CWBorderPixel, &attributes);

	if (!vwin) {
		error(_("failed in XCreateWindow\n"));
		return vmNotAvailable;
	}


	/*	Set window and class properties  */

	if ((wm_hints = XAllocWMHints()) == NULL ||
		(class_hints = XAllocClassHint()) == NULL) {
		return vmNotAvailable;
	}

	/* Set up window hints */
	wm_hints->flags |= StateHint | InputHint;
	wm_hints->initial_state = NormalState;
	wm_hints->input = True;		// yes, want kbd input

	/* Set up resource hints */
	class_hints->res_name = "v9t9";
	class_hints->res_class = "V9t9";

	if (XStringListToTextProperty(&v9t9_windowname, 1, &windowName) == 0 ||
		XStringListToTextProperty(&v9t9_iconname, 1, &iconName) == 0) {
		FAIL("XStringListToTextProperty\n");
		return vmNotAvailable;
	}

	XSetWMProperties(x11_dpy, vwin, &windowName, &iconName,
					 v9t9_argv, v9t9_argc, NULL, wm_hints, class_hints);


	/* Select desired input types */

	XSelectInput(x11_dpy, vwin, ExposureMask | KeyPressMask |
				 KeyReleaseMask | ButtonPressMask | ButtonReleaseMask |
				 StructureNotifyMask | ColormapChangeMask | EnterWindowMask |
				 LeaveWindowMask);


	vgc = XCreateGC(x11_dpy, vwin, 0, &gcv);
	if (vgc == NULL)
		module_logger(&X_Video, _L|LOG_FATAL, _("Could not create GC for window\n"));

#if USING_XIMAGE
	x_alloc_image();
#endif

	features |= FE_SHOWVIDEO;
	return vmOk;
}

static      vmResult
X_video_enable(void)
{
	return vmOk;
}

static      vmResult
X_video_disable(void)
{
	return vmOk;
}

static      vmResult
X_video_restart(void)
{
	/*  Allocate colors every time we enable, 
	   in case some color-hogging apps have left. */

	int         tries = 0;
	int         x, alloced;

	//  Allocate our colors.  For using_palette, allocate read/write
	//  colorcells for 17 colors (bg and text fg change).  Else,
	//  allocate 15 colors.
	//  Either way, cmap[] maps each color to the pixel used to
	//  display it.

	x11_cmap = DefaultColormap(x11_dpy, x11_screen);

	//  Go through twice:  first time, try to allocate colors
	//  from default colormap.  If this fails sufficiently,
	//  allocate a virtual colormap.
	do {

		//  This tells us if colors 0 and 16 (for video reg 7)
		//  can change at will, or if we need to redraw the screen for them
		using_palette = x11_class != TrueColor
			&& x11_class != StaticColor && x11_class != StaticGray;

		//  Get 15 immutable cells
		alloced = 0;
		for (x = 1; x < 16; x++) {
			XColor      color;
			XcmsColor   cmClrScrn, cmClrExact;
			XcmsColorFormat cmFmt;

			color.red = RGB_8_TO_16(vdp_palette[x][0]);
			color.green = RGB_8_TO_16(vdp_palette[x][1]);
			color.blue = RGB_8_TO_16(vdp_palette[x][2]);
			color.flags = DoRed | DoGreen | DoBlue;

			if (!XAllocColor(x11_dpy, x11_cmap, &color)) {
				if (!XAllocNamedColor(x11_dpy, x11_cmap, Xcolornames[x],
									  &color, &color)) {
					module_logger(&X_Video, _L|L_1, _("could not allocate color %d\n"), x);
					//return vmInternalError;
				} else {
					module_logger(&X_Video, _L|L_1, _("color #%d:  got named color '%s'\n"), x,
						 Xcolornames[x]);
					alloced++;
				}
			} else {
				module_logger(&X_Video, _L|L_1, _("color #%d:  got RGB color\n"), x);
				alloced += 2;
			}

			colormap[x] = color.pixel;

		}

		//  Get fg/bg colors
		if (using_palette) {
			if (!XAllocColorCells(x11_dpy, x11_cmap, False /* contiguous */ ,
								  x11_planes, 0, colormap, 1)) {
				using_palette = false;
			} else
				if (!XAllocColorCells
					(x11_dpy, x11_cmap, False /* contiguous */ ,
					 x11_planes, 0, colormap + 16, 1)) {
				using_palette = false;
				// free the one color we got
				XFreeColors(x11_dpy, x11_cmap, colormap, 1, x11_planes[0]);
			}
			if (!using_palette)
				module_logger(&X_Video, _L|LOG_ERROR |L_1,
					 _("Could not allocate 2 read/write colorcells\n"));
		}
//		if (!using_palette) {
//			cmap[0] = cmap[1];
//			cmap[16] = cmap[15];
//		}

		//  If we got an insufficient number of colors,
		//  try for virtual colormap.
		module_logger(&X_Video, _L|L_1, _("alloced = %d\n"), alloced);
		if (!tries && alloced < 16 &&
			(x11_class == GrayScale || x11_class == PseudoColor)) {
			x11_cmap = XCreateColormap(x11_dpy, vwin, x11_visual, AllocNone);
			XSetWindowColormap(x11_dpy, vwin, x11_cmap);
			module_logger(&X_Video, _L|LOG_USER, _("allocated custom colormap for window\n"));
		} else {
			tries = 1;
		}

	} while (++tries < 2);

	XSetWindowColormap(x11_dpy, vwin, x11_cmap);

	XSetWMNormalHints(x11_dpy, vwin, vwin_size_hints);
	XMapWindow(x11_dpy, vwin);

//	x_dirty_screen(0, 0, 32, 24);
	return vmOk;
}

static      vmResult
X_video_restop(void)
{
	XWindowAttributes attrs;

	//	Save position, since mapping and unmapping 
	//	will let window manager re-position window
	// 	(we could just iconify it instead of unmapping it!)
	XGetWindowAttributes(x11_dpy, vwin, &attrs);

	vwin_size_hints->flags |= USPosition;
	vwin_size_hints->x = attrs.x;
	vwin_size_hints->y = attrs.y;

	XUnmapWindow(x11_dpy, vwin);

	//  Free allocated colors
	if (using_palette && x11_cmap) {
		XFreeColors(x11_dpy, x11_cmap, cmap, 1, x11_planes[0]);
		XFreeColors(x11_dpy, x11_cmap, cmap + 16, 1, x11_planes[0]);
	}
	//  Free custom colormap, if any
	if (x11_cmap && x11_cmap != DefaultColormap(x11_dpy, x11_screen))
		XFreeColormap(x11_dpy, x11_cmap);

	x11_cmap = 0L;

	return vmOk;
}

static      vmResult
X_video_term(void)
{
#if USING_XIMAGE
	if (vim)
		XDestroyImage(vim);
#endif
	return vmOk;
}

static      vmResult
X_video_resize(u32 newxsize, u32 newysize)
{
	xsize = newxsize;
	ysize = newysize;
	x_draw_sides();
	return vmOk;
}

static      vmResult
X_video_setfgbg(u8 fg, u8 bg)
{
	video_setpaletteentry(0, bg);
	video_setpaletteentry(16, fg);
	if (!using_palette) {
		vdpcompleteredraw();
		x_draw_sides();
	}
	return vmOk;
}

static      vmResult
X_video_setblank(u8 bg)
{
	int         x;

	for (x = 0; x <= 16; x++)
		video_setpaletteentry(x, bg);
	if (!using_palette)
		vdpcompleteredraw();
	return vmOk;
}


static      vmResult
X_video_resetfromblank(void)
{
	int         x;

	video_updatepalette();
	if (!using_palette)
		vdpcompleteredraw();
	return vmOk;
}

/***********************************************************/

#if USING_XIMAGE

/*	Don't rewrite this to use Regions, since XFree86 is remarkably
	broken when a region has more than, say, 25 rectangles.  I think
	this is related to the maximum X packet size, but it's incomprehensible
	that the startup screen (32x24 blocks, in order) wouldn't generate
	one 256x192 XRectangle.  */
static void
x_video_updatelist(struct updateblock *ptr, int num)
{
	int			width = 8;
	int         offs = (128 - xsize / 2) * vwxm;
	u8         *blk;
	u32         lx = vwxsz, ly = vwysz, mx = 0, my = 0;

	if (!exposed || vwin_iconified)
		return;

	while (num--) {
		int         i;
		int         j;

		ptr->r *= vwym;
		ptr->c *= vwxm;
		blk = vim->data + ptr->r * vwxsz * x11_bytes + (ptr->c) * x11_bytes;
		if (ptr->r < ly)
			ly = ptr->r;
		if (ptr->c < lx)
			lx = ptr->c;
		if (ptr->r > my)
			my = ptr->r;
		if (ptr->c > mx)
			mx = ptr->c;
		for (i = 0; i < 8; i++) {
			drawpixels(blk, ptr->data, width==6);
			blk += vim->bytes_per_line * vwym;
			ptr->data += UPDATEBLOCK_ROW_STRIDE;
		}

		ptr++;
	}

	if ((mx >= lx && my >= ly)) {
		module_logger(&X_Video, _L|L_1, _("X_video_updatelist (%d,%d+%d,%d)\n"),
			   lx, ly, (mx-lx+8), (my-ly+8));
		XPutImage(x11_dpy, vwin, vgc, vim,
				  lx, ly,
				  lx + offs, ly,
				  mx - lx + width * vwxm, my - ly + 8 * vwym);
//		XFlush(x11_dpy);
	}
}

#else

static void
x_video_updatelist(struct updateblock *ptr, int num)
{
	int			width = 8;
	static int video_updating;
	u8         *blk;
	u32         lx = vwxsz, ly = vwysz, mx = 0, my = 0;
	XRectangle	points[17][64], *pptr[17];
	int			rects = 0, pixels = 0;
	int         offs = (128 - xsize / 2) * vwxm;

	int total = num, solid = 0;

	if (!exposed || !num || vwin_iconified || video_updating)
		return;

	video_updating = 1;

	while (num--) {
		int         i;
		int         j;
		u8			c;
		int			d;

		ptr->r *= vwym;
		ptr->c = ptr->c * vwxm;

		if (video_block_is_solid(ptr, !using_palette, &c)) {
			XSetForeground(x11_dpy, vgc, cmap[c]);
			XFillRectangle(x11_dpy, vwin, vgc,
						   ptr->c + offs, 
						   ptr->r,
						   vwxm * width, 
						   vwym * 8);
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

				pptr[c]->x = ptr->c + j * vwxm + offs;
				pptr[c]->y = ptr->r + i * vwym;
				pptr[c]->width = vwxm * d;
				pptr[c]->height = vwym;

				rects++;
				pixels += pptr[c]->width * pptr[c]->height;

				pptr[c]++;
				j += d;
			}
			ptr->data += UPDATEBLOCK_ROW_STRIDE;
		}

		for (c = 0; c <= 16; c++) {
			if (pptr[c] > points[c]) {
				module_logger(&X_Video, _L | L_3, _("Got color %d (%x)\n"), c, cmap[c]);
				XSetForeground(x11_dpy, vgc, cmap[c]);
				XFillRectangles(x11_dpy, vwin, vgc, points[c], pptr[c] - points[c]);
			}
		}
		}

		ptr++;
	}

	module_logger(&X_Video, _L | L_2, _("Drew %d rects, %d pixels\n"), rects, pixels);
	module_logger(&X_Video, _L|L_2, _("%d/%d solid\n"), solid, total);
	video_updating = 0;
}

#endif

static      vmResult
X_video_updatelist(struct updateblock *ptr, int num)
{
	x_video_updatelist(ptr, num);
	return vmOk;
}

static vmVideoModule X_video_videoModule = {
	3,
	X_video_updatelist,
	X_video_resize,
	X_video_setfgbg,
	X_video_setblank,
	X_video_resetfromblank
};

vmModule    X_Video = {
	3,
	"X-Window video",
	"vidX",

	vmTypeVideo,
	vmFlagsExclusive,

	X_video_detect,
	X_video_init,
	X_video_term,
	X_video_enable,
	X_video_disable,
	X_video_restart,
	X_video_restop,
	{(vmGenericModule *) & X_video_videoModule}
};
