/*
  video_gtk.c					-- V9t9 module for GTK+ video interface

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
 *	Video module for GTK+
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
#include <gdk/gdk.h>

#define PIXBUF_BG 1

#if PIXBUF_BG
#include <gdk-pixbuf/gdk-pixbuf.h>
#include <gdk-pixbuf/gdk-pixbuf-xlibrgb.h>
#endif
#include <gtk/gtk.h>

#include "gtkinterface.h"
#include "gtkcallbacks.h"
#include "gtkloop.h"

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

#if PIXBUF_BG
static GdkPixbuf *bg_pixbuf;
#endif

static bool is_external_video()
{
#if PIXBUF_BG
  	return VDP_IS_EXTERNAL_VIDEO() && bg_pixbuf;
#else
	return 0;
#endif
}

#if defined(UNDER_UNIX)

////////////////////////////////////
//	Apple-2 composite filter mode (from bsod.c apple2 
//	part by Trevor Blackwell <tlb@tlb.org>

#define A2_CMAP_HISTBITS	5
#define A2_CMAP_LEVELS		2
#define A2_CMAP_OFFSETS		4

#define A2_CMAP_INDEX(COLORMODE, LEVEL, HIST, OFFSET) \
	((((COLORMODE)*A2_CMAP_LEVELS+(LEVEL))<<A2_CMAP_HISTBITS)+(HIST))* \
	A2_CMAP_OFFSETS+(OFFSET)

#define NTSC_MODE 2
#define DIRECT_RGB 0

#if NTSC_MODE == 0
#define NTSC_CLOCKS (300)
#define NTSC_BIT_BASE (1024)
#elif NTSC_MODE == 1
// B&W
#define NTSC_CLOCKS (600)	
#define NTSC_BIT_BASE (128)
#elif NTSC_MODE == 2
#define NTSC_CLOCKS (1024)
#define NTSC_BIT_BASE (1024)
#elif NTSC_MODE == 3
#define NTSC_CLOCKS (600)
#define NTSC_BIT_BASE (1024)
#elif NTSC_MODE == 4
#define NTSC_CLOCKS (912)
#define NTSC_BIT_BASE (912)
#endif

struct ntsc_dec {
  char pattern[NTSC_CLOCKS];
  int ntscy[NTSC_CLOCKS];
  int ntsci[NTSC_CLOCKS];
  int ntscq[NTSC_CLOCKS];
  int multi[NTSC_CLOCKS];
  int multq[NTSC_CLOCKS];
  int brightness_control;
};


struct com_state
{
	Display *dpy;
	Window window;
	XWindowAttributes xgwa;
	GC gc;
	XImage *image;
	int use_shm;
#ifdef HAVE_XSHM_EXTENSION
	XShmSegmentInfo shm_info;
#endif
	unsigned long colors[A2_CMAP_INDEX(1, A2_CMAP_LEVELS-1,
									   (1<<A2_CMAP_HISTBITS)-1,
									   A2_CMAP_OFFSETS-3)+1];
	int n_colors;
	int red_invprec,red_shift,green_invprec,green_shift,blue_invprec,blue_shift;
	int use_cmap, use_color;
	int w, h;
	int screen_xo, screen_yo;
	int win_width, win_height;
	double contrast_control, tint_control, brightness_control, color_control;
	struct ntsc_dec *dec;
	short *raw_rgb;

	int rowimage[24];
};

static struct com_state com;

enum {
  A2_SP_ROWMASK=1023,
  A2_SP_PUT=1024,
  A2_SP_COPY=2048,
};

static      vmResult
gtkvideo_restop(void);
static      vmResult
gtkvideo_restart(void);


static DECL_SYMBOL_ACTION(switch_composite_mode)
{
	if (task == csa_READ) {
		if (iter)
			return 0;
		command_arg_set_num(SYM_ARG_1st, use_composite_mode);
	} else {
		int val;
		command_arg_get_num(SYM_ARG_1st, &val);
		if (val != use_composite_mode)
		{
			gtkvideo_restop();
			use_composite_mode = val;
			gtkvideo_restart();
		}
	}
	return 1;
}

/*
  First generate the I and Q reference signals, which we'll multiply by the
  input signal to accomplish the demodulation. Normally they are shifted 33
  degrees from the colorburst. I think this was convenient for
  inductor-capacitor-vacuum tube implementation.
               
  The tint control, FWIW, just adds a phase shift to the chroma signal, and 
  the color control controls the amplitude.
               
  In text modes (colormode==0) the system disabled the color burst, and no
  color was detected by the monitor.

  freq_error gives a mismatch between the built-in oscillator and the TV's
  colorbust. Older II Plus machines seemed to occasionally get instability
  problems -- the crystal oscillator was a single transistor if I remember
  correctly -- and the frequency would vary enough that the tint would change
  across the width of the screen.  The left side would be in correct tint
  because it had just gotten resynchronized with the color burst.
*/
static void
ntsc_set_demod(struct ntsc_dec *it, double tint_control, 
               double color_control, double brightness_control,
               double freq_error, 
               int colormode)
{
  int i;

  it->brightness_control=(int)(1024.0*brightness_control);

  for (i=0; i<NTSC_CLOCKS; i++) {
    double phase=90.0-90.0*i + freq_error*i/NTSC_CLOCKS + tint_control;
    it->multi[i]=(int)(-cos(3.1415926/180.0*(phase-303)) * 65536.0 * 
                       color_control * colormode * 4);
    it->multq[i]=(int)(cos(3.1415926/180.0*(phase-33)) * 65536.0 * 
                       color_control * colormode * 4);
  }
}

/* Here we model the analog circuitry of an NTSC television. Basically, it
   splits the signal into 3 signals: Y, I and Q. Y corresponds to luminance,
   and you get it by low-pass filtering the input signal to below 3.57 MHz.

   I and Q are the in-phase and quadrature components of the 3.57 MHz
   subcarrier. We get them by multiplying by cos(3.57 MHz*t) and sin(3.57
   MHz*t), and low-pass filtering. Because the eye has less resolution in some
   colors than others, the I component gets low-pass filtered at 1.5 MHz and
   the Q at 0.5 MHz. The I component is approximately orange-blue, and Q is
   roughly purple-green. See http://www.ntsc-tv.com for details.
 */
static void
ntsc_to_yiq(struct ntsc_dec *it) 
{
  int i;
  int fyx[10],fyy[10];
  int fix[10],fiy[10];
  int fqx[10],fqy[10];
  int pixghost;
  int iny,ini,inq,pix,blank;
  
  for (i=0; i<10; i++) fyx[i]=fyy[i]=fix[i]=fiy[i]=fqx[i]=fqy[i]=0.0;
  pixghost=0;
  for (i=0; i<NTSC_CLOCKS; i++) {
    /* Get the video out signal, and add a teeny bit of ghosting, typical of RF
       monitor cables. This corresponds to a pretty long cable, but looks right
       to me. */
    pix=it->pattern[i]*NTSC_BIT_BASE;
    if (i>=20) pixghost += it->pattern[i-20]*15*NTSC_BIT_BASE/1024;
    if (i>=30) pixghost -= it->pattern[i-30]*15*NTSC_BIT_BASE/1024;
    pix += pixghost;

    /* Get Y, I, Q before filtering */
    iny=pix;
    ini=(pix*it->multi[i])>>16;
    inq=(pix*it->multq[i])>>16;
            
    blank = (i>=7 && i<NTSC_CLOCKS-4 ? it->brightness_control : -200);

    /* Now filter them. These are infinite impulse response filters calculated
       by the script at http://www-users.cs.york.ac.uk/~fisher/mkfilter. This
       is fixed-point integer DSP, son. No place for wimps. We do it in integer
       because you can count on integer being faster on most CPUs. We care
       about speed because we need to recalculate every time we blink text, and
       when we spew random bytes into screen memory. This is roughly 16.16
       fixed point arithmetic, but we scale some filter values up by a few bits
       to avoid some nasty precision errors. */
            
    /* Filter y at with a 4-pole low-pass Butterworth filter at 3.5 MHz 
       with an extra zero at 3.5 MHz, from
       mkfilter -Bu -Lp -o 4 -a 2.1428571429e-01 0 -Z 2.5e-01 -l
       Delay about 2 */

    fyx[0] = fyx[1]; fyx[1] = fyx[2]; fyx[2] = fyx[3]; 
    fyx[3] = fyx[4]; fyx[4] = fyx[5]; fyx[5] = fyx[6]; 
    fyx[6] = (iny * 1897) >> 13;
    fyy[0] = fyy[1]; fyy[1] = fyy[2]; fyy[2] = fyy[3]; 
    fyy[3] = fyy[4]; fyy[4] = fyy[5]; fyy[5] = fyy[6]; 
    fyy[6] = (fyx[0]+fyx[6]) + 4*(fyx[1]+fyx[5]) + 7*(fyx[2]+fyx[4]) + 8*fyx[3]
      + ((-151*fyy[2] + 8115*fyy[3] - 38312*fyy[4] + 36586*fyy[5]) >> 16);
    if (i>=2) it->ntscy[i-2] = blank + (fyy[6]>>3);

    /* Filter I and Q at 1.5 MHz. 3 pole Butterworth from
       mkfilter -Bu -Lp -o 3 -a 1.0714285714e-01 0
       Delay about 3.

       The NTSC spec says the Q value should be filtered at 0.5 MHz at the
       transmit end, But the Apple's video circuitry doesn't any such thing.
       AFAIK, oldish televisions (before comb filters) simply applied a 1.5 MHz
       filter to both after the demodulator.
    */

    fix[0] = fix[1]; fix[1] = fix[2]; fix[2] = fix[3];
    fix[3] = (ini * 1413) >> 14;
    fiy[0] = fiy[1]; fiy[1] = fiy[2]; fiy[2] = fiy[3];
    fiy[3] = (fix[0]+fix[3]) + 3*(fix[1]+fix[2])
      + ((16559*fiy[0] - 72008*fiy[1] + 109682*fiy[2]) >> 16);
    if (i>=3) it->ntsci[i-3] = fiy[3]>>2;

    fqx[0] = fqx[1]; fqx[1] = fqx[2]; fqx[2] = fqx[3];
    fqx[3] = (inq * 1413) >> 14;
    fqy[0] = fqy[1]; fqy[1] = fqy[2]; fqy[2] = fqy[3];
    fqy[3] = (fqx[0]+fqx[3]) + 3*(fqx[1]+fqx[2])
      + ((16559*fqy[0] - 72008*fqy[1] + 109682*fqy[2]) >> 16);
    if (i>=3) it->ntscq[i-3] = fqy[3]>>2;

  }
  for (; i<NTSC_CLOCKS+10; i++) {
    if (i-2<NTSC_CLOCKS) it->ntscy[i-2]=0;
    if (i-3<NTSC_CLOCKS) it->ntsci[i-3]=0;
    if (i-9<NTSC_CLOCKS) it->ntscq[i-9]=0;
  }
}

// !@#
static void composite_term(struct com_state *com);

static int composite_init(struct com_state *com, Display *dpy, Window window)
{
	int visclass;
	struct ntsc_dec *dec;
	XWindowAttributes xgwa;
	XGCValues gcv;
	int i;

	XGetWindowAttributes (dpy, window, &xgwa);
	visclass=xgwa.visual->class;
	com->red_shift=com->red_invprec=
		com->green_shift=com->green_invprec=
		com->blue_shift=com->blue_invprec=-1;
	if (visclass == TrueColor || xgwa.visual->class == DirectColor) {
		com->use_cmap=0;
		com->use_color=1;	//!mono_p;
	}
	else if (visclass == PseudoColor || visclass == StaticColor) {
		com->use_cmap=1;
		com->use_color=1; //!mono_p;
	}
	else {
		com->use_cmap=1;
		com->use_color=0;
	}

	/* Model the video controls on a standard television */
/*
	tint_control = get_float_resource("apple2TVTint","Apple2TVTint");
	color_control = get_float_resource("apple2TVColor","Apple2TVColor")/100.0;
	brightness_control = get_float_resource("apple2TVBrightness",
											"Apple2TVBrightness") / 100.0;
	contrast_control = get_float_resource("apple2TVContrast",
										  "Apple2TVContrast") / 100.0;
*/
	
	com->tint_control = composite_tint;
	com->color_control = (double)composite_color/100;
	com->brightness_control = (double)composite_bright/100;
	com->contrast_control = (double)composite_contrast/100;




	/* The Apple II screen was 280x192, sort of. We expand the width to 300
	   pixels to allow for overscan. We then pick a size within the window
	   that's an integer multiple of 300x192. The small case happens when
	   we're displaying in a subwindow. Then it ends up showing the center
	   of the screen, which is OK. */
	com->w = xgwa.width;
	com->h = (xgwa.height/192)*192;
	if (com->w<300) com->w=300;
	if (com->h==0) com->h=192;
	com->win_width = xgwa.width;
	com->win_height = xgwa.height;

	com->screen_xo=(xgwa.width-com->w)/2;
	com->screen_yo=(xgwa.height-com->h)/2;

	com->dec=dec=(struct ntsc_dec *)xmalloc(sizeof(struct ntsc_dec));

	com->dpy = dpy;
	com->window = window;
	gcv.background = 0;
	com->gc = XCreateGC(dpy, window, GCBackground, &gcv);
	com->xgwa = xgwa;

	com->use_shm = 0;
	if (com->use_shm) {
#ifdef HAVE_XSHM_EXTENSION
		com->image = create_xshm_image (dpy, xgwa.visual, xgwa.depth, ZPixmap, 0, 
									  &shm_info, w, h);
#endif
		if (!com->image) {
			module_logger(&gtkVideo, _L|LOG_USER|LOG_ERROR, _("create_xshm_image failed\n"));
			com->use_shm=0;
		}
	}
	if (!com->image) {
		com->image = XCreateImage(dpy, xgwa.visual, xgwa.depth, ZPixmap, 0, 0,
								com->w, com->h, 8, 0);
		com->image->data = (char *)xcalloc(com->image->height*com->image->bytes_per_line);
	}

  	if (com->use_cmap) {
		int hist,offset,level;
		int colorprec=8;
		int colormode;

	cmap_again:
		com->n_colors=0;
		/* Typically allocates 214 distinct colors, but will scale back its
		   ambitions pretty far if it can't get them */
		for (colormode=0; colormode<=com->use_color; colormode++) {
			ntsc_set_demod(dec, com->tint_control, com->color_control, com->brightness_control,
						   0.0, colormode);
			for (level=0; level<2; level++) {
				for (hist=0; hist<(1<<A2_CMAP_HISTBITS); hist++) {
					for (offset=0; offset<4; offset++) {
						int interpy,interpi,interpq,r,g,b;
						int levelmult=level ? 64 : 32;
						int prec=colormode ? colorprec : (colorprec*2+2)/3;
						int precmask=(0xffff<<(16-prec))&0xffff;
						XColor col;

						if (A2_CMAP_INDEX(colormode,level,hist,offset) != com->n_colors) {
							module_logger(&gtkVideo, _L|LOG_USER|LOG_ERROR, _("apple2: internal colormap allocation error\n"));
							goto bailout;
						}

						for (i=0; i<NTSC_CLOCKS; i++) dec->pattern[i]=0;
						for (i=0; i<A2_CMAP_HISTBITS; i++) {
							dec->pattern[64+offset-i]=(hist>>i)&1;
						}
        
						ntsc_to_yiq(dec);
						interpy=dec->ntscy[63+offset];
						interpi=dec->ntsci[63+offset];
						interpq=dec->ntscq[63+offset];

						r=(interpy + ((+68128*interpi+40894*interpq)>>16))*levelmult;
						g=(interpy + ((-18087*interpi-41877*interpq)>>16))*levelmult;
						b=(interpy + ((-72417*interpi+113312*interpq)>>16))*levelmult;
						if (r<0) r=0;
						if (r>65535) r=65535;
						if (g<0) g=0;
						if (g>65535) g=65535;
						if (b<0) b=0;
						if (b>65535) b=65535;
          
						col.red=r & precmask;
						col.green=g & precmask;
						col.blue=b & precmask;
						col.pixel=0;
						if (!XAllocColor(dpy, xgwa.colormap, &col)) {
							XFreeColors(dpy, xgwa.colormap, com->colors, com->n_colors, 0L);
							com->n_colors=0;
							colorprec--;
							if (colorprec<3) {
								goto bailout;
							}
							goto cmap_again;
						}
						com->colors[com->n_colors++]=col.pixel;
					}
				}
			}
		}
	} else {
		/* Is there a standard way to do this? Does this handle all cases? */
		int shift, prec;
		for (shift=0; shift<32; shift++) {
			for (prec=1; prec<16 && prec<32-shift; prec++) {
				unsigned long mask=(0xffffUL>>(16-prec)) << shift;
				if (com->red_shift<0 && mask==xgwa.visual->red_mask)
					com->red_shift=shift, com->red_invprec=16-prec;
				if (com->green_shift<0 && mask==xgwa.visual->green_mask)
					com->green_shift=shift, com->green_invprec=16-prec;
				if (com->blue_shift<0 && mask==xgwa.visual->blue_mask)
					com->blue_shift=shift, com->blue_invprec=16-prec;
			}
		}
		if (com->red_shift<0 || com->green_shift<0 || com->blue_shift<0) {
			module_logger(&gtkVideo, _L|LOG_USER|LOG_ERROR, _("Can't figure out color space\n"));
			return 0;
		}
		com->raw_rgb=(short *)xcalloc(com->w*3*sizeof(short));
	}
	for (i=0; i<24; i++) com->rowimage[i]=-1;

	module_logger(&gtkVideo, _L|LOG_USER, _("Composite mode initialized\n"));
	return 1;

bailout:
	composite_term(com);
	return 0;
}

static void composite_term(struct com_state *com)
{
	if (com->image) {
		if (com->use_shm) {
#ifdef HAVE_XSHM_EXTENSION
			destroy_xshm_image(com->dpy, com->image, &com->shm_info);
#endif
		} else {
			XDestroyImage(com->image);
		}
		com->image=NULL;
	}
	if (com->raw_rgb) xfree(com->raw_rgb);
	if (com->dec) xfree(com->dec);
	if (com->n_colors) XFreeColors(com->dpy, com->xgwa.colormap, com->colors, com->n_colors, 0L);
}

/*
  You'll need these to generate standard NTSC TV signals
 */
enum {
  /* We don't handle interlace here */
  ANALOGTV_V=262,
  ANALOGTV_TOP=30,
  ANALOGTV_VISLINES=200,
  ANALOGTV_BOT=ANALOGTV_TOP + ANALOGTV_VISLINES,

  /* This really defines our sampling rate, 4x the colorburst
     frequency. Handily equal to the Apple II's dot clock.
     You could also make a case for using 3x the colorburst freq,
     but 4x isn't hard to deal with. */
  ANALOGTV_H=NTSC_CLOCKS,

  /* Each line is 63500 nS long. The sync pulse is 4700 nS long, etc.
     Define sync, back porch, colorburst, picture, and front porch
     positions */
  ANALOGTV_SYNC_START=0,
  ANALOGTV_BP_START=4700*ANALOGTV_H/63500,
  ANALOGTV_CB_START=5800*ANALOGTV_H/63500,
  /* signal[row][ANALOGTV_PIC_START] is the first displayed pixel */
  ANALOGTV_PIC_START=9400*ANALOGTV_H/63500,
  ANALOGTV_PIC_LEN=52600*ANALOGTV_H/63500,
  ANALOGTV_FP_START=62000*ANALOGTV_H/63500,
  ANALOGTV_PIC_END=ANALOGTV_FP_START,

  /* TVs scan past the edges of the picture tube, so normally you only
     want to use about the middle 3/4 of the nominal scan line.
  */
  ANALOGTV_VIS_START=ANALOGTV_PIC_START + (ANALOGTV_PIC_LEN*1/8),
  ANALOGTV_VIS_END=ANALOGTV_PIC_START + (ANALOGTV_PIC_LEN*7/8),
  ANALOGTV_VIS_LEN=ANALOGTV_VIS_END-ANALOGTV_VIS_START,

  ANALOGTV_HASHNOISE_LEN=6,

  ANALOGTV_GHOSTFIR_LEN=4,

  /* analogtv.signal is in IRE units, as defined below: */
  ANALOGTV_WHITE_LEVEL=100,
  ANALOGTV_GRAY50_LEVEL=55,
  ANALOGTV_GRAY30_LEVEL=35,
  ANALOGTV_BLACK_LEVEL=10,
  ANALOGTV_BLANK_LEVEL=0,
  ANALOGTV_SYNC_LEVEL=-40,
  ANALOGTV_CB_LEVEL=20,

  ANALOGTV_SIGNAL_LEN=ANALOGTV_V*ANALOGTV_H,

  /* The number of intensity levels we deal with for gamma correction &c */
  ANALOGTV_CV_MAX=1024
};

//	Take the 'width' x 192 'bitmap' -- each unit, a VDP color --
//	and using the changes for each 24 rows 'rowchanged',
//	make composite version in 'com->image'
static void
composite_render(struct com_state *com, u8 *bitmap, u8 *rowchanged, int width)
{
	int i,j,x,y,textrow,row,col,stepno,imgrow;
	int colormode;
//	int w = com->w, h = com->h;
	char c,*s;
//	struct timeval basetime_tv;
//	double next_actiontime;
//	XWindowAttributes xgwa;
//	int visclass;
//	int screen_xo,screen_yo;
//  XImage *comimage=NULL;
//	XImage *text_im=NULL;
	int screen_plan[24];
	//short *raw_rgb=NULL;
	short *rrp;
//	struct apple2_state *st=NULL;
	double freq_error=0.0,freq_error_inc=0.0;
	double horiz_desync=5.0;
	int flutter_horiz_desync=0;
	int flutter_tint=0;
	double crtload[192];
//	int red_invprec,red_shift,green_invprec,green_shift,blue_invprec,blue_shift;
	int fillptr, fillbyte;
//	int use_shm,use_cmap,use_color;
	/* localbyteorder is 1 if MSB first, 0 otherwise */
	unsigned int localbyteorder_loc = MSBFirst<<24;
	int localbyteorder=*(char *)&localbyteorder_loc;

	com->tint_control = composite_tint;
	com->color_control = (double)composite_color/100.0;
	com->brightness_control = (double)composite_bright/100.0;
	com->contrast_control = (double)composite_contrast/100.0;

	if (rand()%4==0 &&
		!com->use_cmap && com->use_color 
		&& com->xgwa.visual->bits_per_rgb>=8) {
		flutter_tint=1;
	}
	else if (rand()%3==0) {
		flutter_horiz_desync=1;
	}

	crtload[0]=0.0;
	stepno=0;
	fillptr=fillbyte=0;
	if (1) {
		int startdisplayrow=0;
		int cheapdisplay=0;

		if (flutter_tint) {
			/* Oscillator instability. Look for freq_error below. We should only do
			   this with color depth>=8, since otherwise you see pixels changing. */
			freq_error_inc += (-0.10*freq_error_inc
							   + ((int)(rand()&0xff)-0x80) * 0.01);
			freq_error += freq_error_inc;
			for (i=0; i<24; i++) com->rowimage[i]=-1;
		}
		else if (flutter_horiz_desync) {
			/* Horizontal sync during vertical sync instability. */
			horiz_desync += (-0.10*(horiz_desync-3.0) +
							 ((int)(rand()&0xff)-0x80) * 
							 ((int)(rand()&0xff)-0x80) *
							 ((int)(rand()&0xff)-0x80) * 0.0000003);
			for (i=0; i<3; i++) com->rowimage[i]=-1;
		} 

		/* update rows that changed (extend four down for shadowing) */
		{
			int downcounter=0;
			for (row=0; row<24; row++) {
				if (rowchanged[row])
					downcounter=4;
				if (downcounter>0) {
					com->rowimage[row]=-1;
					downcounter--;
				}
			}
			startdisplayrow=rand()%24;
		} 

		/* Now, we turn the data in the video into a screen display. This
		   is interesting because of the interaction with the NTSC color decoding
		   in a color television. */

		colormode=com->use_color;
		if (!com->use_cmap) {
			ntsc_set_demod(com->dec, com->tint_control, com->color_control, com->brightness_control,
						   freq_error, colormode);
		}
		imgrow=0;
		for (textrow=0; textrow<24; textrow++) {
			if (com->rowimage[textrow] == textrow) {
				screen_plan[textrow]=0;
			}
			else if (cheapdisplay && com->rowimage[textrow]>=0 &&
					 textrow<21 && com->rowimage[textrow]<21 && 
					 com->rowimage[textrow]>=2 && textrow>=2 &&
					 (com->rowimage[textrow]+1)*com->h/24 + com->screen_xo <= com->win_height) {
				screen_plan[textrow]= A2_SP_COPY | com->rowimage[textrow];
				for (i=0; i<8; i++) {
					crtload[textrow*8+i]=crtload[com->rowimage[textrow]*8+i];
				}
				startdisplayrow=0;
			}
			else {
				com->rowimage[textrow]=imgrow;
				screen_plan[textrow]=imgrow | A2_SP_PUT;
							srand(1);
				for (row=textrow*8; row<textrow*8+8; row++) {
					char *pp;
					int pixmultinc,pixbright;
					int scanstart_i, scanend_i;
					int squishright_i, squishdiv;
					int pixrate;
					double bloomthisrow,shiftthisrow;
					int ytop=(imgrow*com->h/24) + ((row-textrow*8) * com->h/192);
					int ybot=ytop+com->h/192;

#if !DIRECT_RGB
					/* First we generate the pattern that the video circuitry shifts out
					   of memory. It has a 14.something MHz dot clock, equal to 4 times
					   the color burst frequency. So each group of 4 bits defines a
					   color.  Each character position, or byte in hires, defines 14
					   dots, so odd and even bytes have different color spaces. So,
					   pattern[0..NTSC_CLOCKS] gets the dots for one scan line. */

					memset(com->dec->pattern,0,sizeof(com->dec->pattern));
					pp=com->dec->pattern+20;
        
					for (col=0; col<256; col++) {
						int color  = bitmap[row*width+col];
						if (color == 0) color = vdpbg; else if (color == 16) color = vdpfg;
#if NTSC_MODE == 4
						{
							int ntsc[4];
							unsigned int r = vdp_palette[color][0], 
								g = vdp_palette[color][1],
								b = vdp_palette[color][2];

							int tvx=col*4 /*+left*/;
							//if (tvx<ANALOGTV_PIC_START || tvx+4>ANALOGTV_PIC_END) continue;

							int rawy=( 5*r + 11*g + 2*b) / 64;
							int rawi=(10*r -  4*g - 5*b) / 64;
							int rawq=( 3*r -  8*g + 5*b) / 64;
							int i;

							ntsc[0]=rawy+rawq;
							ntsc[1]=rawy-rawi;
							ntsc[2]=rawy-rawq;
							ntsc[3]=rawy+rawi;

							for (i=0; i<4; i++) {
								if (ntsc[i]>ANALOGTV_WHITE_LEVEL) ntsc[i]=ANALOGTV_WHITE_LEVEL;
								if (ntsc[i]<ANALOGTV_BLACK_LEVEL) ntsc[i]=ANALOGTV_BLACK_LEVEL;
							}

							pp[tvx+0]= ntsc[(tvx+0)&3];
							pp[tvx+1]= ntsc[(tvx+1)&3];
							pp[tvx+2]= ntsc[(tvx+2)&3];
							pp[tvx+3]= ntsc[(tvx+3)&3];

							//pp++;
							//*pp++ = (bits>>(~col&3))&1;
						}
						
#elif NTSC_MODE == 0
						{	
							static int ntsc_cmap[16] = 
								{
									0	/*black*/,
									0,	/*black*/
									14,	/*medium green*/
									3,	/*light green */
									4,	/*dark blue*/
									5,	/*light blue*/
									6,	/*dark red*/
									7,	/*cyan*/
									8,	/*medium red*/
									9,	/*light red*/
									10,	/*dark yellow*/
									11,	/*light yellow*/
									12,	/*dark green*/
									14,	/*magenta*/
									5, /*grey*/
									15	/*white*/
								};
							int bits = ntsc_cmap[color];
							*pp++ = (bits>>(~col&3))&1;
						}
#elif NTSC_MODE == 2
						//color = cmap[color];
						{
							/*
							  6= light blue
							  3= magenta
							  1= black
							  b= light red (pink)
							  c= light green
							  d= dark yellow
							  f= white
							  4= med green
							  2= dark blue
							  8= dark green
							  e= cyan
							  5= grey
							  9= orange
							  a= grey?
							*/
							static int ntsc_map[16] =
								{
									0	/*0 black*/,
									0,	/*1 black*/
									4,	/*2 medium green*/
									12,	/*3 light green */
									2,	/*4 dark blue*/
									6,	/*5 light blue*/
									7,	/*6 dark red*/
									14,	/*7 cyan*/
									1,	/*8 medium red*/
									9,	/*9 light red*/
									13,	/*a dark yellow*/
									11,	/*b light yellow*/ //?
									8,	/*c dark green*/
									3,	/*d magenta*/
									10, /*e grey*/	/*5=dark*/
									15	/*f white*/
								};
							int bits = ntsc_map[color];
							//bits = (row>>3)^(col&1);
							
							*pp++ = (bits>>0)&1;
							*pp++ = (bits>>1)&1;
							*pp++ = (bits>>2)&1;
							*pp++ = (bits>>3)&1;
							/*
							int myrow = (row >> 3);
							pp[0] = pp[2] = rand()%2;
							pp[1] = pp[3] = rand()%2;
							pp+=4;
							//*pp++ = rand()%2;
							//*pp++ = rand()%2;
							*/
/*							*pp++ = myrow ? ((col)%myrow)&1 : 0;
							*pp++ = myrow ? 0*((col+1)%myrow)&1 : 0;
							*pp++ = myrow ? ((col+2)%myrow)&1 : 0;
							*pp++ = myrow ? 0*((col+3)%myrow)&1 : 0;*/
						}
#elif NTSC_MODE == 3
						// one entry per pixel, but eight pixels share the same
						// info
						{
							*pp++ = (color>>(3&(1+(col&1))))&1;
							*pp++ = (color>>(3&((col&1))))&1;
						}
#elif NTSC_MODE == 1						
						*pp++ = (vdp_palette[color][0]+vdp_palette[color][1]+vdp_palette[color][2])/128;
 						*pp++ = (vdp_palette[color][0]+vdp_palette[color][1]+vdp_palette[color][2])/128;
#endif
					}

#endif

					/*
					  Interpolate the NTSC_CLOCKS-dotclock line into however many horizontal
					  screen pixels we're using, and convert to RGB. 

					  We add some 'bloom', variations in the horizontal scan width with
					  the amount of brightness, extremely common on period TV sets. They
					  had a single oscillator which generated both the horizontal scan
					  and (during the horizontal retrace interval) the high voltage for
					  the electron beam. More brightness meant more load on the
					  oscillator, which caused an decrease in horizontal deflection. Look
					  for (bloomthisrow).

					  Also, the A2 did a bad job of generating horizontal sync pulses
					  during the vertical blanking interval. This, and the fact that the
					  horizontal frequency was a bit off meant that TVs usually went a
					  bit out of sync during the vertical retrace, and the top of the
					  screen would be bent a bit to the left or right. Look for
					  (shiftthisrow).

					  We also add a teeny bit of left overscan, just enough to be
					  annoying, but you can still read the left column of text.
            
					  We also simulate compression & brightening on the right side of the
					  screen. Most TVs do this, but you don't notice because they
					  overscan so it's off the right edge of the CRT. But the A2 video
					  system used so much of the horizontal scan line that you had to
					  crank the horizontal width down in order to not lose the right few
					  characters, and you'd see the compression on the right
					  edge. Associated with compression is brightening; since the
					  electron beam was scanning slower, the same drive signal hit the
					  phosphor harder. Look for (squishright_i) and (squishdiv).
					*/

#if DIRECT_RGB
					for (i=j=0; i<256; i++) {
						int color =bitmap[row*width+i];
						if (color == 0) color = vdpbg; else if (color == 16) color = vdpfg;
						int grey = (vdp_palette[color][0]*30+vdp_palette[color][1]*59+vdp_palette[color][2]*11)/100;
						j += grey>=75;
					}

#else
					for (i=j=0; i<NTSC_CLOCKS; i++) {
						j += com->dec->pattern[i];
					}
#endif
					crtload[row] = (crtload[row>1 ? row-1 : 0]) * 0.98 + 0.02*(j*NTSC_BIT_BASE/1024/(double)NTSC_CLOCKS) +
						(row>180 ? (row-180)*(row-180)*0.0005 : 0.0);
					bloomthisrow = -10.0 * crtload[row];
					shiftthisrow=((row<18) ? ((18-row)*(18-row)* 0.002 + (18-row)*0.05)
								  * horiz_desync : 0.0);

					scanstart_i=(int)((bloomthisrow+shiftthisrow+18.0)*65536.0);
					if (scanstart_i<0) scanstart_i=0;
					if (scanstart_i>30*65536) scanstart_i=30*65536;
					scanend_i=(NTSC_CLOCKS-1)*65536;
					squishright_i=scanstart_i + (NTSC_CLOCKS*90/100)*65536;
					squishdiv=com->w/15;

#if DIRECT_RGB
					pixrate=(int)(((double)width*95/100-2.0*bloomthisrow)*65536.0/com->w);
					if (0)
#else
					pixrate=(int)(((double)NTSC_CLOCKS*95/100-2.0*bloomthisrow)*65536.0/com->w);
					if (com->use_cmap)
#endif
					{
						for (y=ytop; y<ybot; y++) {
							int level=(!(y==ytop && ybot-ytop>=3) &&
									   !(y==ybot-1 && ybot-ytop>=5));
							int hist=0;
							int histi=0;

							pixmultinc=pixrate;
							for (x=0, i=scanstart_i;
								 x<com->w && i<scanend_i;
								 x++, i+=pixmultinc) {
								int pati=(i>>16);
								int offset=pati&3;
								while (pati>=histi) {
									hist=(((hist<<1) & ((1<<A2_CMAP_HISTBITS)-1)) |
										  (com->dec->pattern[histi]&1));
									histi++;
								}
								XPutPixel(com->image, x, y, 
										  com->colors[A2_CMAP_INDEX(colormode,level,hist,offset)]);
								if (i >= squishright_i) {
									pixmultinc += pixmultinc/squishdiv;
								}
							}
							for ( ; x<com->w; x++) {
								XPutPixel(com->image, x, y, com->colors[0]);
							}
						}
					} else {

#if !DIRECT_RGB
						ntsc_to_yiq(com->dec);
#endif

						pixbright=(int)(com->contrast_control*65536.0);
						pixmultinc=pixrate;
						for (x=0, i=scanstart_i, rrp=com->raw_rgb;
							 x<com->w && i<scanend_i;
							 x++, i+=pixmultinc, rrp+=3) {
							int pixfrac=i&0xffff;
							int invpixfrac=65536-pixfrac;
							int pati=i>>16;
							int r,g,b;

#if DIRECT_RGB
							int color = bitmap[row*width+pati];
							if (color==0) color=vdpbg; else if (color==16) color=vdpfg;
							r = vdp_palette[color][0]*2*pixbright/65536;
							g = vdp_palette[color][1]*2*pixbright/65536;
							b = vdp_palette[color][2]*2*pixbright/65536;
#else
							int interpy=((com->dec->ntscy[pati]*invpixfrac + 
										  com->dec->ntscy[pati+1]*pixfrac)>>16);
							int interpi=((com->dec->ntsci[pati]*invpixfrac + 
										  com->dec->ntsci[pati+1]*pixfrac)>>16);
							int interpq=((com->dec->ntscq[pati]*invpixfrac + 
										  com->dec->ntscq[pati+1]*pixfrac)>>16);

							/*
							  According to the NTSC spec, Y,I,Q are generated as:
                
							  y=0.30 r + 0.59 g + 0.11 b
							  i=0.60 r - 0.28 g - 0.32 b
							  q=0.21 r - 0.52 g + 0.31 b
                
							  So if you invert the implied 3x3 matrix you get what standard
							  televisions implement with a bunch of resistors (or directly in
							  the CRT -- don't ask):
                
							  r = y + 0.948 i + 0.624 q
							  g = y - 0.276 i - 0.639 q
							  b = y - 1.105 i + 1.729 q
                
							  These coefficients are below in 16.16 format.
							*/

							r=((interpy + ((+68128*interpi+40894*interpq)>>16))*pixbright)
																		 >>16;
							g=((interpy + ((-18087*interpi-41877*interpq)>>16))*pixbright)
																		 >>16;
							b=((interpy + ((-72417*interpi+113312*interpq)>>16))*pixbright)
																		  >>16;
#endif
							if (r<0) r=0;
							if (g<0) g=0;
							if (b<0) b=0;
							rrp[0]=r;
							rrp[1]=g;
							rrp[2]=b;

							if (i>=squishright_i) {
								pixmultinc += pixmultinc/squishdiv;
								pixbright += pixbright/squishdiv;
							}
						}
						for ( ; x<com->w; x++, rrp+=3) {
							rrp[0]=rrp[1]=rrp[2]=0;
						}

						for (y=ytop; y<ybot; y++) {
							/* levelmult represents the vertical size of scan lines. Each
							   line is brightest in the middle, and there's a dark band
							   between them. */
							int levelmult;
							double levelmult_fp=(y + 0.5 - (ytop+ybot)*0.5) / (ybot-ytop);
							levelmult_fp = 1.0-(levelmult_fp*levelmult_fp*levelmult_fp
												*levelmult_fp)*16.0;
							if (levelmult_fp<0.0) levelmult_fp=0.0;
							levelmult = (int)(64.9*levelmult_fp);

							/* Fast special cases to avoid the slow XPutPixel. Ugh. It goes
							   to show why standard graphics sw has to be fast, or else
							   people will have to work around it and risk incompatibility.
							   The quickdraw folks understood this. The other answer would
							   be for X11 to have fewer formats for bitm.. oh, never
							   mind. If neither of these cases work (they probably cover 99%
							   of setups) it falls back on the Xlib routines. */
							if (com->image->format==ZPixmap && com->image->bits_per_pixel==32 && 
								sizeof(unsigned long)==4 &&
								com->image->byte_order==localbyteorder) {
								unsigned long *pixelptr =
									(unsigned long *) (com->image->data + y * com->image->bytes_per_line);
								for (x=0, rrp=com->raw_rgb; x<com->w; x++, rrp+=3) {
									unsigned long ntscri, ntscgi, ntscbi;
									ntscri=((unsigned long)rrp[0])*levelmult;
									ntscgi=((unsigned long)rrp[1])*levelmult;
									ntscbi=((unsigned long)rrp[2])*levelmult;
									if (ntscri>65535) ntscri=65535;
									if (ntscgi>65535) ntscgi=65535;
									if (ntscbi>65535) ntscbi=65535;
									*pixelptr++ = ((ntscri>>com->red_invprec)<<com->red_shift) |
										((ntscgi>>com->green_invprec)<<com->green_shift) |
										((ntscbi>>com->blue_invprec)<<com->blue_shift);
								}
							}
							else if (com->image->format==ZPixmap && com->image->bits_per_pixel==16 && 
									 sizeof(unsigned short)==2 &&
									 com->image->byte_order==localbyteorder) {
								unsigned short *pixelptr =
									(unsigned short *)(com->image->data + y*com->image->bytes_per_line);
								for (x=0, rrp=com->raw_rgb; x<com->w; x++, rrp+=3) {
									unsigned long ntscri, ntscgi, ntscbi;
									ntscri=((unsigned long)rrp[0])*levelmult;
									ntscgi=((unsigned long)rrp[1])*levelmult;
									ntscbi=((unsigned long)rrp[2])*levelmult;
									if (ntscri>65535) ntscri=65535;
									if (ntscgi>65535) ntscgi=65535;
									if (ntscbi>65535) ntscbi=65535;
									*pixelptr++ = ((ntscri>>com->red_invprec)<<com->red_shift) |
										((ntscgi>>com->green_invprec)<<com->green_shift) |
										((ntscbi>>com->blue_invprec)<<com->blue_shift);
								}
                
							}
							else {
								for (x=0, rrp=com->raw_rgb; x<com->w; x++, rrp+=3) {
									unsigned long pixel, ntscri, ntscgi, ntscbi;
									/* Convert to 16-bit color values, with saturation. The ntscr
									   values are 22.10 fixed point, and levelmult is 24.6, so we
									   get 16 bits out*/
									ntscri=((unsigned long)rrp[0])*levelmult;
									ntscgi=((unsigned long)rrp[1])*levelmult;
									ntscbi=((unsigned long)rrp[2])*levelmult;
									if (ntscri>65535) ntscri=65535;
									if (ntscgi>65535) ntscgi=65535;
									if (ntscbi>65535) ntscbi=65535;
									pixel = ((ntscri>>com->red_invprec)<<com->red_shift) |
										((ntscgi>>com->green_invprec)<<com->green_shift) |
										((ntscbi>>com->blue_invprec)<<com->blue_shift);
									XPutPixel(com->image, x, y, pixel);
								}
							}
						}
					}
				}
				imgrow++;
			}
		}

		/* For just the the rows which changed, blit the image to the screen. */
		for (textrow=0; textrow<24; ) {
			int top,bot,srcrow,srctop,nrows;
      
			nrows=1;
			while (textrow+nrows < 24 &&
				   screen_plan[textrow+nrows] == screen_plan[textrow]+nrows)
				nrows++;

			top=com->h*textrow/24;
			bot=com->h*(textrow+nrows)/24;
			srcrow=screen_plan[textrow]&A2_SP_ROWMASK;
			srctop=srcrow*com->h/24;

			if (screen_plan[textrow] & A2_SP_COPY) {
				if (0) printf("Copy %d screenrows %d to %d\n", nrows, srcrow, textrow);
				XCopyArea(com->dpy, com->window, com->window, com->gc,
						  com->screen_xo, com->screen_yo + srctop,
						  com->w, bot-top,
						  com->screen_xo, com->screen_yo + top);
			}
			else if (screen_plan[textrow] & A2_SP_PUT) {
				if (0) printf("Draw %d imgrows %d to %d\n", nrows, srcrow, textrow);
				if (com->use_shm) {
#ifdef HAVE_XSHM_EXTENSION
					XShmPutImage(com->dpy, com->window, com->gc, com->image, 
								 0, srctop, com->screen_xo, com->screen_yo + top,
								 com->w, bot-top, False);
#endif
				} else {
					XPutImage(com->dpy, com->window, com->gc, com->image, 
							  0, srctop,
							  com->screen_xo, com->screen_yo + top,
							  com->w, bot-top);
				}
			}
			textrow += nrows;
		}
		//XSync(com->dpy,0);

		for (textrow=0; textrow<24; textrow++) {
			com->rowimage[textrow]=textrow;
		}
	}
}

static DECL_SYMBOL_ACTION(update_composite)
{
	int i;
	for (i = 0; i < 24; i++)
		com.rowimage[i] = -1;
	return 1;
}

//	timer-triggered event
static void 
redraw_composite(int tag)
{
	u8 rowchanged[24] = {0};
	composite_render(&com, UPDPTR(0,0), rowchanged, UPDATEBLOCK_ROW_STRIDE);
}

//	ordinary redraw event
static void
draw_composite(struct updateblock *ptr, int num)
{
	u8 rowchanged[32];
	while (num--)
	{
		rowchanged[ptr->r/8] = 1;
		ptr++;
	}
	composite_render(&com, UPDPTR(0,0), rowchanged, UPDATEBLOCK_ROW_STRIDE);
}

#else	//#if defined(UNDER_UNIX)
static DECL_SYMBOL_ACTION(switch_composite_mode)
{
	if (task == csa_READ) {
		if (iter)
			return 0;
		command_arg_set_num(SYM_ARG_1st, use_composite_mode);
	} else {
		int val;
		command_arg_get_num(SYM_ARG_1st, &val);
		if (val)
			module_logger(&gtkVideo, _L|LOG_USER|LOG_ERROR, _("Composite video not supported on this OS\n"));
		use_composite_mode = 0;
	}
	return 1;
}

static DECL_SYMBOL_ACTION(update_composite)
{
	return 1;
}

#endif	//#if defined(UNDER_UNIX)


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

		 command_symbol_new("CompositeMode",
							_("Use composite video filter (borrowed from Apple2 BSOD hack)"),
							c_STATIC,
							switch_composite_mode /* action */ ,
							RET_FIRST_ARG,
							command_arg_new_enum
							("off|on",
							 _("on:  use composite mode; "
							   "off:  use pixel-for-pixel display"),
							 NULL /* action */ ,
							 NEW_ARG_NUM(int),
							 NULL /* next */ )
							,
		 command_symbol_new("CMTint",
							_("Set tint level for composite video"),
							c_STATIC,
							update_composite /* action */ ,
							RET_FIRST_ARG,
							command_arg_new_num
							(_("level"),
							 _("tint level (0..359)"),
							 NULL /* action */ ,
							 ARG_NUM(composite_tint),
							 NULL /* next */ )
							,
		 command_symbol_new("CMBrightness",
							_("Set brightness level for composite video"),
							c_STATIC,
							update_composite /* action */ ,
							RET_FIRST_ARG,
							command_arg_new_num
							(_("level"),
							 _("brightness level (0..100)"),
							 NULL /* action */ ,
							 ARG_NUM(composite_bright),
							 NULL /* next */ )
							,
		 command_symbol_new("CMColor",
							_("Set color level for composite video"),
							c_STATIC,
							update_composite /* action */ ,
							RET_FIRST_ARG,
							command_arg_new_num
							(_("level"),
							 _("color level (0..100)"),
							 NULL /* action */ ,
							 ARG_NUM(composite_color),
							 NULL /* next */ )
							,
		 command_symbol_new("CMContrast",
							_("Set contrast level for composite video"),
							c_STATIC,
							update_composite /* action */ ,
							RET_FIRST_ARG,
							command_arg_new_num
							(_("level"),
							 _("contrast level (0..100)"),
							 NULL /* action */ ,
							 ARG_NUM(composite_contrast),
							 NULL /* next */ )
							,
		 command_symbol_new("DumpScreen",
							_("Dump screen info"),
							c_DONT_SAVE,
							dump_screen /* action */ ,
							NULL /* return */,
							 NULL /* next */
							,
	NULL /*next*/)))))),
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

	use_composite_mode = 0;
	composite_tint = 50;
	composite_bright = 10;
	composite_contrast = 90;
	composite_color = 50;

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

#if PIXBUF_BG
	{
		GdkPixbuf *orig = gdk_pixbuf_new_from_file("bg.png");
		if (orig) {
			bg_pixbuf = gdk_pixbuf_new(GDK_COLORSPACE_RGB, false, 8, GTK_x_mult*256, GTK_y_mult*192);
			if (bg_pixbuf) {
				gdk_pixbuf_scale(orig, bg_pixbuf, 0, 0, GTK_x_mult*256, GTK_y_mult*192,
								 0, 0, 
								 (GTK_x_mult*256./gdk_pixbuf_get_width(orig)),
								 (GTK_y_mult*192./gdk_pixbuf_get_height(orig)),
								 GDK_INTERP_BILINEAR);
			}
			gdk_pixbuf_unref(orig);
		}
	}
#endif

	return system_gtkvideo_restart();
}

static      vmResult
gtkvideo_restop(void)
{
	GdkColormap *map;
	int i;
	vmResult result;

	if (!VALID_WINDOW(v9t9_window)) {
		return vmInternalError;
	}

#if PIXBUF_BG
	if (bg_pixbuf) {
		gdk_pixbuf_unref(bg_pixbuf);
		bg_pixbuf = 0;
	}
#endif

	result = system_gtkvideo_restop();
	if (result != vmOk)
		return result;

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
	if (use_composite_mode)
	{
		Display *display;
		Window window;

		display = GDK_WINDOW_XDISPLAY(v9t9_drawing_area->window);
		window = GDK_WINDOW_XWINDOW(v9t9_drawing_area->window);
		
		if (composite_init(&com, display, window))
		{
			if (!composite_tag) composite_tag = TM_UniqueTag();
			TM_ResetEvent(composite_tag);
			TM_SetEvent(composite_tag, TM_HZ*100/60, 0,
						TM_FUNC|TM_REPEAT, redraw_composite);
		}
		else
			use_composite_mode = 0;
	}
	return vmOk;
}

static      vmResult
system_gtkvideo_restop(void)
{
	if (use_composite_mode)
	{
		composite_term(&com);
		TM_ResetEvent(composite_tag);
	}
	return vmOk;
}

static      vmResult
system_gtkvideo_term(void)
{
	return vmOk;
}

static void draw_pixbuf_portion(int c, int r, int w, int h) 
{
#if PIXBUF_BG
	int iw, ih;
	int mw, mh;
	int ox, oy;
	iw = gdk_pixbuf_get_width(bg_pixbuf);
	ih = gdk_pixbuf_get_height(bg_pixbuf);
	ox = c % iw;
	if (ox + w > iw)
		w = iw - ox;
	oy = r % ih;
	if (oy + h > ih)
		h = ih - oy;
	gdk_pixbuf_render_to_drawable(bg_pixbuf, 
								  v9t9_drawing_area->window,
								  v9t9_gdk_colors[0],
								  ox, oy,
								  c, r,
								  w, h,
								  XLIB_RGB_DITHER_NONE,
								  0, 0);	
#endif
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

	if (use_composite_mode)
	{
		draw_composite(ptr, num);
		return;
	}

	while (num--) {
		u8			c = 0;
		int			d;
		int			w = GTK_x_mult * width;
		int			h = GTK_y_mult * 8;

		ptr->r *= GTK_y_mult;
		ptr->c = (ptr->c + xoffs) * GTK_x_mult;

		if (video_block_is_solid(ptr, !v9t9_gdk_paletted, &c) &&
			v9t9_gdk_colors[cmap[c]]) {
/*
			XFillRectangle(display, window,
						   GDK_GC_XGC(v9t9_gdk_colors[cmap[c]]), 
						   ptr->c, 
						   ptr->r,
						   w, h);
*/
			if (c || !is_external_video()) {
				gdk_draw_rectangle(v9t9_drawing_area->window, 
							   v9t9_gdk_colors[cmap[c]],
							   true,
							   ptr->c, ptr->r, 
								   w, h);
				solid++;
			} else {
				draw_pixbuf_portion(ptr->c, ptr->r, w, h);
			}
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
					if (c || !is_external_video()) {
						XFillRectangles(display, window,
									GDK_GC_XGC(v9t9_gdk_colors[cmap[c]]), 
									points[c], 
									pptr[c] - points[c]);
					} else {
						XRectangle *rp;
						for (rp = points[c]; rp < pptr[c]; rp++) {
							draw_pixbuf_portion(rp->x, rp->y,
												rp->width, rp->height);
						}
					}
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

void GTK_clear_sides(int total, int inside)
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
	if (!v9t9_gdk_paletted || (bg == 0 && is_external_video())) {
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
	if (!v9t9_gdk_paletted || (bg == 0 && is_external_video())) {
//		GTK_clear_sides(256, GTK_x_size);
		vdpcompleteredraw();
	}
	return vmOk;
}

static      vmResult
gtkvideo_resetfromblank(void)
{
	video_updatepalette();
	if (!v9t9_gdk_paletted || is_external_video()) {
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

vmModule    gtkVideo = {
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
