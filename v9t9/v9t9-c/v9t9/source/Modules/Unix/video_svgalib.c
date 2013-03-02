
/*
  video_svgalib.c				-- V9t9 module for SVGAlib video

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

/*
  $Id$
 */

#include <vga.h>

#include "v9t9_common.h"
#include "emulate.h"
#include "error.h"
#include "video.h"
#include "vdp.h"
#include "timer.h"

#define _L	LOG_VIDEO|LOG_INFO

int         vga_gfx_mode;
static int	good_vga_gfx_mode;

static int	tixsize, tiysize;
static int  xsize, ysize;
static int 	xmult, ymult;

static void
video_setpaletteentry(int index, int c)
{
	vga_setpalette(index, 
				   RGB_8_TO_6(vdp_palette[c][0]), 
				   RGB_8_TO_6(vdp_palette[c][1]),
				   RGB_8_TO_6(vdp_palette[c][2]));
}

static void
video_updatepalette(void)
{
	int         x;

	for (x = 1; x < 16; x++)
		video_setpaletteentry(x, x);
	video_setpaletteentry(0, vdpbg ? vdpbg : 1);
	video_setpaletteentry(16, vdpfg ? vdpfg : 15);
}

static int
can_use_mode(int num)
{
	vga_modeinfo *vi = vga_getmodeinfo(num);

	return (vi != NULL && vi->width >= 256 && vi->height >= 192 &&
			vi->colors >= 16 && 
			(vi->width * vi->height * vi->bytesperpixel < 65536 ||
			 (vi->flags & (CAPABLE_LINEAR | IS_LINEAR))));
}


static int
video_setmode(void)
{
	int         i;

	module_logger(&svgaVideo, _L|0, _("Setting graphics mode\n"));
	if (vga_setmode(vga_gfx_mode))
		return 0;

	vga_setlinearaddressing();
	xsize = vga_getxdim();
	ysize = vga_getydim();
	xmult = xsize / 256;
	ymult = ysize / 192;

	for (i = 0; i < 16; i++)
		video_setpaletteentry(i, i);

	return 1;
}

static DECL_SYMBOL_ACTION(svgavideo_set_video_mode)
{
	if (stateflag & ST_INTERACTIVE)
		return 1;

	if (!can_use_mode(vga_gfx_mode)) {
		module_logger(&svgaVideo, _L|LOG_USER, "Cannot use mode %d\n", 
					  vga_gfx_mode);
		vga_gfx_mode = good_vga_gfx_mode;
	}

	if (!video_setmode()) {
		module_logger(&svgaVideo, _L|LOG_USER, "Cannot set mode %d\n", 
					  vga_gfx_mode);
		vga_gfx_mode = good_vga_gfx_mode;
	}	

	return video_setmode();
}

static void
video_settext(void)
{
	module_logger(&svgaVideo, _L|0, _("Setting text mode\n"));
	vga_setmode(TEXT);
}


//static    int video_event_tag;

extern int  console_fd;

extern int  __svgalib_runinbackground;

static      vmResult
svgavideo_detect(void)
{
//	__svgalib_runinbackground = 1;
	module_logger(&svgaVideo, _L|LOG_USER, _("testing console video... "));
	if (console_fd < 0) {
		logger(_L|LOG_USER, _("cannot access.\n"));
		return vmNotAvailable;
	}
	logger(_L| LOG_USER, _("ok.\n"));
	return vmOk;
}

static      vmResult
svgavideo_init(void)
{
	static int  goodmodes[] = { G320x200x256, G320x240x256, 0 };
	static char svga_modes[256];
	int         x;
	command_symbol_table *svgacommands =
		command_symbol_table_new(_("SVGAlib Options"),
								 _("These commands control the SVGAlib interface"),

    	 command_symbol_new("SVGAMode",
    						_("Set the video mode used in SVGAlib"),
							c_STATIC,
    						svgavideo_set_video_mode /* action */ ,
    						RET_FIRST_ARG,
    						command_arg_new_enum
    						(svga_modes,
    						 _("video mode"),
    						 NULL /* action */ ,
							 ARG_NUM(vga_gfx_mode),
    						 NULL /* next */ )
    						,

			NULL /* next */ ),

    	 NULL /* sub */ ,

    	 NULL	/* next */
		);

	snprintf(svga_modes, sizeof(svga_modes),
			 "G320x200x256=%d|G320x200x16=%d|G320x240x256=%d"
			 "|G320x400x256=%d|G320x480x256=%d"
			 "|G360x480x256=%d|G640x480x16=%d"
			 "|G800x600x16=%d|G640x480x256=%d|G800x600x256=%d",
			 G320x200x256, G320x200x16, G320x240x256,
			 G320x400x256, G320x480x256, 
			 G360x480x256, G640x480x16,
			 G800x600x16, G640x480x256, G800x600x256);

	features |= FE_SHOWVIDEO;

	for (x = 0; goodmodes[x]; x++) {
		module_logger(&svgaVideo, _L|LOG_USER, _("detecting mode %d... "), goodmodes[x]);
		if (can_use_mode(goodmodes[x])) 
			break;
		logger(_L|LOG_USER, _("\n"));
	}
	vga_setmode(TEXT);

	if (!goodmodes[x])
	{
		logger(_L|LOG_USER, _("no suitable VGA modes found.\n"));
		return vmConfigError;
	}
	else
	{
		logger(_L|LOG_USER, _("succeeded.\n"));
		good_vga_gfx_mode = vga_gfx_mode = goodmodes[x];
		command_symbol_table_add_subtable(universe, svgacommands);
		return vmOk;
	}
}

static      vmResult
svgavideo_enable(void)
{
	module_logger(&svgaVideo, _L|LOG_USER, _("testing mode %d... "), vga_gfx_mode);
	if (can_use_mode(vga_gfx_mode)) {
		logger(_L|LOG_USER, _("success.\n"));
		return vmOk;
	}
	logger(_L|LOG_USER, _("cannot use this mode.\n"));
	return vmConfigError;
}

static      vmResult
svgavideo_disable(void)
{
	return vmOk;
}

static      vmResult
svgavideo_restart(void)
{
	if (features & FE_SHOWVIDEO) {
		if (!video_setmode())
		{
			module_logger(&svgaVideo, _L|LOG_USER|LOG_ERROR, _("Could not restore video mode for SVGAlib\n"));
			return vmNotAvailable;
		}
		video_updatepalette();
	}
	return vmOk;
}

static      vmResult
svgavideo_restop(void)
{
	if (features & FE_SHOWVIDEO)
	{
		video_settext();
	}
	return vmOk;
}

static      vmResult
svgavideo_term(void)
{
	module_logger(&svgaVideo, _L|LOG_USER, _("Shutting down video... "));
	if (vga_getcurrentmode() != TEXT) {
		vga_setdisplaystart(0);	// seems to work
		vga_setmode(TEXT);
	}
	logger(_L|LOG_USER, _("done.\n"));
	return vmOk;
}


static      vmResult
svgavideo_resize(u32 newxsize, u32 newysize)
{
	u8 		   	*a;
	int         y, b;
	int 		yoffs;
	u8       	*n;

	b = xsize - (newxsize * xmult / 2);
	yoffs = ysize / 2 - (newysize * ymult / 2);
	if (b > 0) {
		n = (u8 *)alloca(b);
		memset(n, 0, b);
		a = vga_getgraphmem() + yoffs * UPDATEBLOCK_ROW_STRIDE;
		for (y = 0; y < newysize * ymult; y++) {
			if (vga_gfx_mode != G320x200x256) {
				vga_drawscansegment(n, 0, y + yoffs, b);
				vga_drawscansegment(n, xsize - b, y + yoffs, b);
			} else {
				memset(a, 0, b);
				memset(a + xsize - b, 0, b);
				a += UPDATEBLOCK_ROW_STRIDE;
			}
		}
	}
	tixsize = newxsize;
	tiysize = newysize;
	return vmOk;
}

static      vmResult
svgavideo_setfgbg(u8 fg, u8 bg)
{
	video_setpaletteentry(0, bg ? bg : 1);
	video_setpaletteentry(16, fg ? fg : 15);
//  video_updatepalette();
	return vmOk;
}

static      vmResult
svgavideo_setblank(u8 bg)
{
	int         x;

	for (x = 1; x <= 15; x++)
		video_setpaletteentry(x, bg ? bg : 1);
	return vmOk;
}


static      vmResult
svgavideo_resetfromblank(void)
{
	video_updatepalette();
	return vmOk;
}

/***********************************************************/

static      void
update_list(struct updateblock *ptr, int num)
{
	int         i;
	int			width=8;
	int         xoffs, yoffs;
	u8         *scrn, *video;

	video = vga_getgraphmem();
	if (!video) return;

	xoffs = (xsize / 2) - (width == 6 ? 120 : 128);
	yoffs = (ysize / 2) - 96;

	while (num--) {
		if (vga_gfx_mode != G320x200x256) {
			for (i = 0; i < 8; i++) {
				vga_drawscansegment(ptr->data, ptr->c + xoffs, yoffs + ptr->r++, width);
				ptr->data += UPDATEBLOCK_ROW_STRIDE;
			}
		} else {
			scrn = video + ((yoffs + ptr->r) * xsize) + 
				ptr->c + xoffs;
			for (i = 0; i < 8; i++) {
				memcpy(scrn, ptr->data, width);
				ptr->data += UPDATEBLOCK_ROW_STRIDE;
				scrn += xsize;
			}
		}
		ptr++;
	}
}

static      vmResult
svgavideo_updatelist(struct updateblock *ptr, int num)
{
	update_list(ptr, num);
	return vmOk;
}

static vmVideoModule svgavideo_videoModule = {
	3,
	svgavideo_updatelist,
	svgavideo_resize,
	svgavideo_setfgbg,
	svgavideo_setblank,
	svgavideo_resetfromblank
};

vmModule    svgaVideo = {
	3,
	"SVGALIB video",
	"vidSVGA",

	vmTypeVideo,
	vmFlagsExclusive,

	svgavideo_detect,
	svgavideo_init,
	svgavideo_term,
	svgavideo_enable,
	svgavideo_disable,
	svgavideo_restart,
	svgavideo_restop,
	{(vmGenericModule *) & svgavideo_videoModule}
};

