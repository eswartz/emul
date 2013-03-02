/*
  vdp.c							-- video display processor emulation, 
									plus driver for updating graphics

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

#define __VDP_INTERNAL__

#include "v9t9_common.h"
#include "timer.h"
#include "video.h"
#include "memory.h"
#include "vdp.h"
#include "vdpsprites.h"
#include "command.h"
#include "v9t9.h"
#include "demo.h"
#include "9901.h"

static void
vdpwritereg(u16 addr);

static vdp_redrawfunc 	vdp_redraw;
vdp_modify_info 	vdp_modify;

u32  			screenxsize, screenysize;

u8          vdpregs[8];
u8          vdpbg, vdpfg;
static u32         vdpmode;

static u8   		*vdpram;
static u16         	vdpaddr;
static char        	vdpaddrflag;
static u8			vdpreadahead;
static u8          vdpstatus;

#define VDPRAM(addr)	FLAT_MEMORY_PTR(md_video, addr)

// 	We switched into text mode or changed bg color; on next update,
// 	clear sides of screen and tell video module to update.
static bool vdp_redraw_text_sides;

/*
 *	Configuration variables
 */

int         videoupdatespeed = 30;
int         vdp_interrupt_rate = 60;
bool		draw_sprites = true;
bool		five_sprites_on_a_line = true;		// five-sprite limit active?

/*
 *	Shared between vdp.c and vdpsprites.c 
 */

#ifdef __VDP_INTERNAL__

u8       		vdp_updarea[UPDATEBLOCK_ROW_STRIDE * 256];
vdp_mode_info	vdp_mode;

vdp_changes_info vdp_changes;

u32  	sprwidth = 8;		/* in pixels */

#endif


#if 1

u8 vdp_palette[17][3] = 
{
	{ 0x00, 0x00, 0x00 }, // clear
	{ 0x00, 0x00, 0x00 }, // black
	{ 0x40, 0xb0, 0x40 }, // medium green
	{ 0x60, 0xc0, 0x60 }, // light green
	{ 0x40, 0x40, 0xc0 }, // dark blue
	{ 0x60, 0x60, 0xf0 }, // light blue
	{ 0xc0, 0x40, 0x40 }, // dark red
	{ 0x40, 0xf0, 0xf0 }, // cyan
	{ 0xf0, 0x40, 0x40 }, // medium red
	{ 0xff, 0x80, 0x60 }, // light red
	{ 0xf0, 0xc0, 0x40 }, // dark yellow
	{ 0xff, 0xe0, 0x60 }, // light yellow
	{ 0x40, 0x80, 0x40 }, // dark green
	{ 0xc0, 0x40, 0xc0 }, // magenta
	{ 0xd0, 0xd0, 0xd0 }, // grey
	{ 0xff, 0xff, 0xff }, // white
	{ 0x00, 0x00, 0x00 }, // text fg
};

#else

// colors from Sean Young (sean@msxnet.org)
u8 vdp_palette[17][3] = 
{
	{ 0x00, 0x00, 0x00 }, // clear
	{ 0x00, 0x00, 0x00 }, // black
	{ 0x24, 0xDA, 0x24 }, // medium green
	{ 0x6D, 0xFF, 0x48 }, // light green
	{ 0x24, 0x24, 0xFF }, // dark blue
	{ 0x48, 0x6D, 0xFF }, // light blue
	{ 0xB6, 0x24, 0x24 }, // dark red
	{ 0x48, 0xDA, 0xFF }, // cyan
	{ 0xFF, 0x24, 0x24 }, // medium red
	{ 0xFF, 0x6D, 0x6D }, // light red
	{ 0xDA, 0xDA, 0x24 }, // dark yellow
	{ 0xDA, 0xDA, 0x91 }, // light yellow
	{ 0x24, 0x91, 0x24 }, // dark green
	{ 0xDA, 0x48, 0xB6 }, // magenta
	{ 0xB6, 0xB6, 0xB6 }, // grey
	{ 0xFF, 0xFF, 0xFF }, // white
	{ 0x00, 0x00, 0x00 }, // text fg
};

#endif

#define _L	 LOG_VIDEO | LOG_INFO

/*	Interface to v9t9.c.  This installs a timer event that
	periodically refreshes the video display by using the
	currently selected video module. */
static int
video_event_tag, vdp_interrupt_tag;

/*	9918A interrupt */
static void
vdp_interrupt(int tag)
{
	if (vdpregs[1] & R1_INT) {
		trigger9901int(M_INT_VDP);
	}
	demo_record_event(demo_type_tick);
}

/*	Actual display refresh interrupt */
static void
video_update(int tag)
{
	if (vdpregs[1] & R1_INT) {
		vdpstatus |= VDP_INTERRUPT;
	}

	if (features & FE_SHOWVIDEO) {
		vdp_update();
	}
}

static void
video_changing_rates(void)
{
	if (!video_event_tag)
		video_event_tag = TM_UniqueTag();
	else
		TM_ResetEvent(video_event_tag);

	if (videoupdatespeed <= 0)
		videoupdatespeed = 1;
	else if (videoupdatespeed > TM_HZ)
		videoupdatespeed = TM_HZ;

	TM_SetEvent(video_event_tag, TM_HZ * 100 / videoupdatespeed, 0,
				TM_REPEAT | TM_FUNC, video_update);

	if (!vdp_interrupt_tag)
		vdp_interrupt_tag = TM_UniqueTag();
	else
		TM_ResetEvent(vdp_interrupt_tag);

	if (vdp_interrupt_rate <= 0)
		vdp_interrupt_rate = 1;
	else if (vdp_interrupt_rate > TM_HZ)
		vdp_interrupt_rate = TM_HZ;
	TM_SetEvent(vdp_interrupt_tag, TM_HZ * 100 / vdp_interrupt_rate, 0,
				TM_REPEAT | TM_FUNC, vdp_interrupt);

}

int
video_restart(void)
{
	video_changing_rates();

	return 1;
}

void
video_restop(void)
{
	TM_ResetEvent(video_event_tag);
	TM_ResetEvent(vdp_interrupt_tag);
}


/*********************************/

/*	Write to memory-mapped port for VDP */

static s8 vdp_read_byte(const mrstruct *mr, u32 addr)
{
//	return vdpram[addr & 0x3fff];
	return mr->areamemory[addr & (AREASIZE-1)];
}

static void vdp_write_byte(const mrstruct *mr, u32 addr, s8 val)
{
	addr &= 0x3fff;
//	if (vdpram[addr] != val) {
//		vdpram[addr] = val;
	if (mr->areamemory[addr & (AREASIZE-1)] != val) {
		mr->areamemory[addr & (AREASIZE-1)] = val;
		if (vdp_touch(addr))
			demo_record_event(demo_type_video, addr, val);
	}
}

mrstruct vdp_memory_handler =
{
	0L, 0L, 0L,
	NULL, vdp_read_byte,
	NULL, vdp_write_byte
};

void
vdp_memory_init(void)
{
	vdpram = (u8 *)xcalloc(16384);
	vdp_memory_handler.areamemory = vdpram;

	memory_insert_new_entry(MEMENT_VIDEO | MEMENT_RAM, 0x0000, 0x4000, 
						   _("VDP memory"), 
							0L /*filename*/, 0L /*fileoffs*/, 
							&vdp_memory_handler);
}

/*	addr == 0 or addr != 0 based on
	write to >8C00 or >8C02 */
void
vdp_mmio_write(u16 addr, u8 val)
{
	if (addr) {					/* >8C02, address write */
		vdpaddr = (vdpaddr >> 8) | (val << 8);
		if (!(vdpaddrflag ^= 1)) {
			if (vdpaddr & 0x8000) {
				vdpaddr &= 0x3fff;
				vdpwritereg(vdpaddr);
			} else if (vdpaddr & 0x4000) {
				vdpaddr &= 0x3fff;
			} else {
				// read ahead one byte
				vdpreadahead = *VDPRAM(vdpaddr);
				//vdpreadahead = vdpram[vdpaddr];
				vdpaddr = (vdpaddr+1) & 0x3fff;
			}
		}
	} else {					/* >8C00, data write */
		/* this flag is used to verify that the VDP
		   address was written as >4000 + vdpaddr.
		   If not, then writing to it functions as
		   a read-before-write. */

		vdpaddrflag = 0;
		domain_write_byte(md_video, vdpaddr, val);
		demo_record_event(demo_type_video, vdpaddr, val);
		vdpaddr = (vdpaddr + 1) & 0x3fff;
		vdpreadahead = val;
	}
}

/*	Read a byte from the VDP. */

s8 vdp_mmio_read(u16 addr)
{
	s8          ret;

	vdpaddrflag = 0;
	if (addr & 2) {				/* >8802, status read */
		ret = vdpstatus;
		vdpstatus &= ~0xe0;		// thierry:  reset bits when read
		reset9901int(M_INT_VDP);
	} else {					/* >8800, memory read */
		ret = vdpreadahead;
		vdpreadahead = domain_read_byte(md_video, vdpaddr);
		vdpaddr = (vdpaddr + 1) & 0x3fff;
	}
	return ret;
}

extern 	u16	 vdp_mmio_get_addr(void)
{
	return vdpaddr;
}

extern	void vdp_mmio_set_addr(u16 addr)
{
	if (addr & 0x8000) {
		vdpwritereg(addr);
	}
	vdpaddr = addr & 0x3fff;
	vdpaddrflag = 0;
}

extern 	bool vdp_mmio_addr_is_complete(void)
{
	return !vdpaddrflag;
}

extern void vdp_mmio_set_status(u8 status)
{
	vdpstatus = status;
}

extern u8 vdp_mmio_get_status(void)
{
	return vdpstatus;
}

/*********************************************************/

/*	This section of code handles the mapping from a VDP address
	to the various areas in VDP memory that affect the generation
	of the display. */

int         vdpchanged;


void        redraw_graphics(void);
void        redraw_bitmap(void);
void        redraw_text(void);
void        redraw_multi(void);
void        redraw_blank(void);


/************************************************************************/

/*	
 * 	Notify that an address in VDP has been modified externally,
 *	return flag if this change is at all visible.
 */
bool
vdp_touch(u32 addr)
{
	bool visible = false;

	if (vdp_mode.screen.base <= addr && addr < vdp_mode.screen.base + vdp_mode.screen.size) {
		vdp_modify.screen(addr - vdp_mode.screen.base);
		visible = true;
	}

	if (vdp_mode.patt.base <= addr && addr < vdp_mode.patt.base + vdp_mode.patt.size) {
		vdp_modify.patt(addr - vdp_mode.patt.base);
		visible = true;
	}

	if (vdp_mode.color.base <= addr && addr < vdp_mode.color.base + vdp_mode.color.size) {
		vdp_modify.color(addr - vdp_mode.color.base);
		visible = true;
	}

	if (vdp_mode.sprite.base <= addr && addr < vdp_mode.sprite.base + vdp_mode.sprite.size) {
		vdp_modify.sprite(addr - vdp_mode.sprite.base);
		visible = true;
	}

	if (vdp_mode.sprpat.base <= addr && addr < vdp_mode.sprpat.base + vdp_mode.sprpat.size) {
		vdp_modify.sprpat(addr - vdp_mode.sprpat.base);
		visible = true;
	}

	return visible;
}

void
vdp_redraw_screen(s32 x, s32 y, s32 sx, s32 sy)
{
	u8  *ptr;
	int width;
	struct updateblock updatelist[768];
	struct updateblock *ull = updatelist;

	//printf("vdp_dirty: %d,%d,%d,%d\n", x,y,sx,sy);
	if (sx < 0 || sy < 0) return;

	if (vdpregs[1] & R1_TEXT) {
		// left blank column?
		if (x < 8) {
			vdp_redraw_text_sides = true;
			if (sx <= x)
				return;
			sx -= x;
			x = 0;
		// right blank column?
		} else if (x >= 240 + 8) {
			vdp_redraw_text_sides = true;
			return;
		} else {
			x -= 8;
		}
		width = 40;
	} else {
		width = 32;
	}

	if (x < 0) { sx += x; x = 0; }
	if (y < 0) { sy += y; y = 0; }
	if (x >= screenxsize || y >= screenysize) return;

	//printf("2: %d,%d,%d,%d\n", x,y,sx,sy);

	if (width == 40) {
		sx = (sx + (x % 6) + 5) / 6; 
		x /= 6;
	}
	else {
		sx = (sx + (x & 7) + 7) >> 3;
		x >>= 3;
	}
	sy = (sy + (y & 7) + 7) >> 3;
	y >>= 3;

	if (x + sx > width) sx = width - x;
	if (y + sy > 24) sy = 24 - y;

	//printf("3: %d,%d,%d,%d\n", x,y,sx,sy);

#if 1
	ptr = vdp_changes.screen + (y * width) + x;
	while (sy--) {
		memset(ptr, 1, sx);
		ptr += width;
	}
	vdpchanged = 1;
#else
	for (r = y; r < y + sy; r++)
		for (c = x; c < x + sx; c++)
		{
			ull->r = r << 3;
			ull->c = c << 3;
			ull->pattern = 0L;
			ull->colors = 0L;
			ull->data = UPDPTR(ull->r, ull->c);
			ull++;
		}
	if (ull > updatelist)
		VIDEO(updatelist, (updatelist, ull - updatelist));
#endif
}


/*************************************************************/

static void
vdp_dirty_sprites(void)
{
	if (!draw_sprites)
		return;
	vdp_changes.sprite = -1;
	memset(vdp_changes.sprpat, 1, vdp_mode.sprpat.size >> 3);
	vdpchanged = 1;
}


static void
vdp_dirty_all(void)
{
	memset(vdp_changes.screen, SC_BACKGROUND, vdp_mode.screen.size);
	memset(vdp_changes.patt, 1, vdp_mode.patt.size >> 3);
	vdp_dirty_sprites();
	vdpchanged = 1;
	vdp_redraw_text_sides = true;
}

/*	Force a complete redraw of the display
	by making it look like the whole VDP context
	has changed. */
void
vdpcompleteredraw(void)
{
	u8          i;

	vdp_dirty_all();
	for (i = 0; i < 8; i++) {
		vdpwritereg(((i | 0x80) << 8) + vdpregs[i]);
	}
	vdp_update();
}

/*************************************************************/

/*  VDP update routines  

	All of these are type 'updatefunc' and are assigned to
	function pointers when the VDP mode is changed.  
	These routines are called through 'vdp_touch'.
*/

static void
modify_sprite_default(u32 offs)
{
	vdp_changes.sprite |= SPRBIT(offs >> 2);
	vdpchanged = 1;
	/*  debug("modify_sprite_default (%d)\n",offs>>2); */
}


static void
modify_sprpat_default(u32 offs)
{
	u32         patt;

	if (vdpregs[1] & R1_SPR4) {
		patt = (offs >> 3) & 0xfc;
		memset(&vdp_changes.sprpat[patt], 1, 4);
	} else {
		patt = offs >> 3;
		vdp_changes.sprpat[patt] = 1;
	}

	vdpchanged = 1;
}

/*************************************************************/

static void
modify_screen_graphics(u32 offs)
{
	vdp_changes.screen[offs] = vdpchanged = SC_BACKGROUND;
}

static void
modify_patt_graphics(u32 offs)
{
	vdp_changes.patt[offs >> 3] = vdpchanged = 1;
}

static void
modify_color_graphics(u32 offs)
{
	memset(&vdp_changes.patt[offs << 3], 1, 8);
	vdpchanged = 1;
}

/************************************************************/

static void
modify_patt_bitmap(u32 offs)
{
	vdp_changes.patt[offs >> 3] = vdpchanged = 1;
}

static void
modify_color_bitmap(u32 offs)
{
	vdp_changes.color[offs >> 3] = vdpchanged = 1;
}

/************************************************************/

/*	This routine updates all the updatefuncs when some
	register controlling the VDP context has changed. */
static void
vdp_update_params(void)
{
	u16         ramsize = (vdpregs[1] & R1_RAMSIZE) ? 0x3fff : 0xfff;

	/* Is the screen really blank?  
	   If so, respond to nothing but calls to vdp_dirty_screen */
	if (!(vdpregs[1] & R1_NOBLANK)) {
		logger(_L | L_1, _("vdp_update_params:  blank screen\n"));
		vdp_mode.screen.base = 0;
		vdp_mode.screen.size = 0;
		vdp_mode.color.base = 0;
		vdp_mode.color.size = 0;
		vdp_mode.patt.base = 0;
		vdp_mode.patt.size = 0;
		vdp_mode.sprite.base = 0;
		vdp_mode.sprite.size = 0;
		vdp_mode.sprpat.base = 0;
		vdp_mode.sprpat.size = 0;
		screenxsize = 256;
		screenysize = 192;
		vdp_modify.patt = NULL;
		vdp_modify.sprite = vdp_modify.sprpat = NULL;
		vdp_modify.screen = NULL;
		vdp_modify.color = NULL;

		vdp_redraw = redraw_blank;
		return;
	}

	switch (vdpmode) {
	case MODE_GRAPHICS:
		logger(_L | L_1, _("vdp_update_params:  graphics mode\n"));
		vdp_mode.screen.base = (vdpregs[2] * 0x400) & ramsize;
		vdp_mode.screen.size = 768;
		vdp_mode.color.base = (vdpregs[3] * 0x40) & ramsize;
		vdp_mode.color.size = 32;
		vdp_mode.patt.base = (vdpregs[4] * 0x800) & ramsize;
		vdp_mode.patt.size = 2048;
		vdp_mode.sprite.base = (vdpregs[5] * 0x80) & ramsize;
		vdp_mode.sprite.size = 128;
		vdp_mode.sprpat.base = (vdpregs[6] * 0x800) & ramsize;
		vdp_mode.sprpat.size = 2048;
		screenxsize = 256;
		screenysize = 192;
		vdp_modify.screen = modify_screen_graphics;
		vdp_modify.color = modify_color_graphics;
		vdp_modify.patt = modify_patt_graphics;
		vdp_modify.sprite = modify_sprite_default;
		vdp_modify.sprpat = modify_sprpat_default;
		vdp_redraw = redraw_graphics;

		break;

	case MODE_TEXT:
		logger(_L | L_1, _("vdp_update_params:  text mode\n"));
		vdp_mode.screen.base = (vdpregs[2] * 0x400) & ramsize;
		vdp_mode.screen.size = 960;
		vdp_mode.color.base = 0;
		vdp_mode.color.size = 0;
		vdp_mode.patt.base = (vdpregs[4] * 0x800) & ramsize;
		vdp_mode.patt.size = 2048;
		vdp_mode.sprite.base = 0;
		vdp_mode.sprite.size = 0;
		vdp_mode.sprpat.base = 0;
		vdp_mode.sprpat.size = 0;
//		screenxsize = 240;
		screenxsize = 256;
		screenysize = 192;
		vdp_modify.patt = modify_patt_graphics;
		vdp_modify.sprite = vdp_modify.sprpat = NULL;
		vdp_modify.screen = modify_screen_graphics;
		vdp_modify.color = NULL;

		vdp_redraw_text_sides = true;
		vdp_redraw = redraw_text;
		break;

	case MODE_BITMAP:
		logger(_L | L_1, _("vdp_update_params:  bitmap mode\n"));
		vdp_mode.screen.base = (vdpregs[2] * 0x400) & ramsize;
		vdp_mode.screen.size = 768;
		vdp_mode.sprite.base = (vdpregs[5] * 0x80) & ramsize;
		vdp_mode.sprite.size = 128;
		vdp_mode.sprpat.base = (vdpregs[6] * 0x800) & ramsize;
		vdp_mode.sprpat.size = 2048;
		screenxsize = 256;
		screenysize = 192;
		vdp_modify.sprite = modify_sprite_default;
		vdp_modify.sprpat = modify_sprpat_default;
		vdp_modify.screen = modify_screen_graphics;
		vdp_modify.color = modify_color_bitmap;
		vdp_modify.patt = modify_patt_bitmap;
		vdp_redraw = redraw_bitmap;

		vdp_mode.color.base = (vdpregs[3] & 0x80) ? 0x2000 : 0;
		vdp_mode.color.size = 6144;
		vdp_mode.bitcolormask = (((u16) (vdpregs[3] & 0x7f)) << 6) | 0x3f;

		vdp_mode.patt.base = (vdpregs[4] & 0x4) ? 0x2000 : 0;
		vdp_mode.patt.size = 6144;

		// thanks, Thierry!
		if (vdpregs[1] & 0x10)
			vdp_mode.bitpattmask = (((u16) (vdpregs[4] & 0x03) << 11)) | 0x7ff;
		else
			vdp_mode.bitpattmask =
				(((u16) (vdpregs[4] & 0x03) << 11)) | (vdp_mode.bitcolormask & 0x7ff);

		break;

	case MODE_MULTI:
		logger(_L | L_1, _("vdp_update_params:  multi mode\n"));
		vdp_mode.screen.base = (vdpregs[2] * 0x400) & ramsize;
		vdp_mode.screen.size = 768;
		vdp_mode.color.base = 0;
		vdp_mode.color.size = 0;
		vdp_mode.patt.base = (vdpregs[4] * 0x800) & ramsize;
		vdp_mode.patt.size = 1536;
		vdp_mode.sprite.base = (vdpregs[5] * 0x80) & ramsize;
		vdp_mode.sprite.size = 128;
		vdp_mode.sprpat.base = (vdpregs[6] * 0x800) & ramsize;
		vdp_mode.sprpat.size = 2048;
		screenxsize = 256;
		screenysize = 192;
		vdp_modify.sprite = modify_sprite_default;
		vdp_modify.sprpat = modify_sprpat_default;
		vdp_modify.screen = modify_screen_graphics;
		vdp_modify.color = NULL;
		vdp_modify.patt = modify_patt_graphics;
		vdp_redraw = redraw_multi;
		break;

	default:
		logger(_L | LOG_FATAL, _("Unknown graphics mode %d set\n\n"), vdpmode);
		break;
	}
}

/*	Figure out the VDP mode from the VDP mode registers */
static void
vdp_update_mode(void)
{
	if (vdpregs[0] & R0_BITMAP)
		vdpmode = MODE_BITMAP;
	else if (vdpregs[1] & R1_TEXT)
		vdpmode = MODE_TEXT;
	else if (vdpregs[1] & R1_MULTI)
		vdpmode = MODE_MULTI;
	else
		vdpmode = MODE_GRAPHICS;
}

/**************************/

#define REDRAW_NOW 1			/* same-mode change */
#define REDRAW_SPRITES 2		/* sprites change */
#define REDRAW_MODE 4			/* mode change */
#define REDRAW_BLANK 8			/* make blank */
#define REDRAW_PALETTE 16		/* palette update */

#define CHANGED(r,v) ((vdpregs[r]&(v))!=(val&(v)))

static void
vdpwritereg(u16 addr)
{
	u8          reg = (addr >> 8) & 0xf;
	u8          val = addr & 0xff;
	int         redraw = 0;

	u8          old = vdpregs[reg];

	logger(_L | L_1, "vdpwritereg %1X=%2X\n", reg, val);

	if (old != val)
		demo_record_event(demo_type_video, addr | 0x8000);

	switch (reg) {
	case 0:					/* bitmap/video-in */
		if (CHANGED(0, R0_BITMAP+R0_EXTERNAL)) {
			redraw |= REDRAW_MODE;
		}
		vdpregs[0] = val;
		break;

	case 1:					/* various modes, sprite stuff */
		if (CHANGED(1, R1_NOBLANK)) {
			redraw |= REDRAW_BLANK | REDRAW_MODE;
		}

		if (CHANGED(1, R1_SPRMAG + R1_SPR4)) {
			redraw |= REDRAW_SPRITES;
			sprwidth = (val & (R1_SPRMAG + R1_SPR4)) == (R1_SPRMAG | R1_SPR4) ? 32 :
				(val & (R1_SPRMAG + R1_SPR4)) ? 16 : 
				8;
			logger(_L | L_1, "SprWidth=%d\n", sprwidth);
		}

		if (CHANGED(1, R1_TEXT | R1_MULTI)) {
			redraw |= REDRAW_MODE;
		}

		/* if interrupts enabled, and interrupt was pending, trigger it */
		if (val & R1_INT 
		&& 	!(vdpregs[1] & R1_INT) 
		&&	vdpstatus & VDP_INTERRUPT) 
		{
			trigger9901int(M_INT_VDP);
		}

		vdpregs[1] = val;
		break;

	case 2:					/* screen image table */
		if (vdpregs[2] != val) {
			redraw |= REDRAW_MODE;
			vdpregs[2] = val;
		}
		break;

	case 3:					/* color table */
		if (vdpregs[3] != val) {
			redraw |= REDRAW_MODE;
			vdpregs[3] = val;
		}
		break;

	case 4:					/* pattern table */
		if (vdpregs[4] != val) {
			redraw |= REDRAW_MODE;
			vdpregs[4] = val;
		}
		break;

	case 5:					/* sprite table */
		if (vdpregs[5] != val) {
			redraw |= REDRAW_MODE;
			vdpregs[5] = val;
		}
		break;

	case 6:					/* sprite pattern table */
		if (vdpregs[6] != val) {
			redraw |= REDRAW_MODE;
			vdpregs[6] = val;
		}
		break;

	case 7:					/* foreground/background color */
		if (vdpregs[7] != val) {
			vdpfg = val >> 4;
			vdpbg = val & 0xf;
			redraw |= REDRAW_PALETTE;
			vdpregs[7] = val;
		}
		break;

	default:
		logger(_L | L_1, _("Undefined VDP register %d\n"), reg);

	}

	/*  This flag must be checked first because
	   it affects the meaning of the following 
	   calls and checks. */
	if (redraw & REDRAW_MODE) {
		vdp_update_mode();
		vdp_update_params();
		if (features & FE_SHOWVIDEO)
			VIDEO(resize, (screenxsize, screenysize));	/* clear edges? */
		vdp_dirty_all();
	}

	if (redraw & (REDRAW_SPRITES))
		vdp_dirty_sprites();

	if (redraw & REDRAW_PALETTE)
		if (features & FE_SHOWVIDEO) {
			VIDEO(setfgbg, (vdpfg, vdpbg));
			// if screen is blank, force something to change
			if (!(vdpregs[1] & R1_NOBLANK))
				redraw |= REDRAW_BLANK;
			vdp_redraw_text_sides = true;
			vdp_update();
		}

	if (redraw & REDRAW_BLANK)
		if (!(vdpregs[1] & R1_NOBLANK)) {
			if (features & FE_SHOWVIDEO) {
				VIDEO(setblank, (vdpbg));
				vdp_update();
			}
		} else {
			vdp_update_params();
			if (features & FE_SHOWVIDEO) {
				VIDEO(resetfromblank, ());
				vdp_update();
			}
		}
}



/************************************************************/

/*
	Redraw strategy:
	
	Using a 256-color mode, we will present to the video module a list
	of coordinates and 8x8 blocks of update information.  There will
	only be one visible page, upon which all changes will be made.
	
	The point will be that, even with sprites, we won't draw to video
	memory at one place more than once.  Instead, a bitmap is maintained
	in RAM that is only written to video memory after all applicable
	changes have been made.
*/

/*
 *	Tell if an updateblock can be drawn with one color
 *
 *	If collapse is true, treat color 0 as vdpfg and 16 and vdpbg;
 *	this may cause problems on palettized displays when these values
 *	change.
 */
bool video_block_is_solid(updateblock *ptr, bool collapse, u8 *color)
{
	u8 *mem;
	u8 byt;
	int row;
	unsigned long long run;

	// get representative byte (i.e., one color)
	byt = ptr->data[0];

	// duplicate it eight times
	run = byt | (byt<<8) | (byt<<16) | (byt<<24);
	run = (run << 32) | run;

	// compare the 8 rows in memory with the byte
	mem = ptr->data;
	for (row = 0; row < 8; row++) {
		if (*(unsigned long long *)mem != run)
			return false;
		mem += UPDATEBLOCK_ROW_STRIDE;
	}

	// matched!  
	*color = byt;
	return true;

#if 0
	static u8 zeroes[8] = {0,0,0,0,0,0,0,0};
	static u8 ones[8] = {0xff,0xff,0xff,0xff,0xff,0xff,0xff,0xff};
	u8 fg, bg;
	bool colors_same;
	
	// if pattern is not set, we've got sprites
	if (!ptr->pattern)
		return false;

	// check for a run of the same colors...
	colors_same = false;

	// if colors set, we have bitmap mode, and
	// must make sure all of the colors are the same as each other.
	if (ptr->colors) {
		u8 colorrun[8];
		memset(colorrun, ptr->colors[0], 8);
		if (memcmp(colorrun, ptr->colors, 8) == 0) {
			colors_same = true;

			// get those colors
			fg = (ptr->colors[0] & 0xf0) >> 4;
			bg = (ptr->colors[0] & 0xf);

			if (collapse) {
				if (!fg) fg = vdpfg;
				if (!bg) bg = vdpbg;
			}
		}
	} else {
		// all one colors are the same...
		colors_same = true;

		fg = (!collapse || ptr->fg) ? ptr->fg : vdpfg;
		bg = (!collapse || ptr->bg) ? ptr->bg : vdpbg;
	}

	// it could be solid if the colors are the same...
	if (!colors_same)
		return false;

	if (fg == bg) {
		*color = fg;
		return true;
	} 
	// or if the pattern is solid...
	else if (memcmp(ptr->pattern, zeroes, 8) == 0) {
		*color = bg;
		return true;
	} 
	else if (memcmp(ptr->pattern, ones, 8) == 0) {
		*color = fg;
		return true;
	} 

	return false;
#endif
}

/*
	These drawing functions use the highly optimized 256-color
	routines for mapping eight pixels to eight bytes in
	vdpdrawrow.c and vdpdrawrowtext.c.
*/


static void
redraw_graphics_block(u8 * pattern, u8 color, updateblock *ull)
{
	int         i;
	u8          fg, bg;
	u8			*block;

	block = ull->data = UPDPTR(ull->r, ull->c);
	
	bg = color & 0xf;
	fg = color >> 4;
	
	ull->bg = bg;			
	ull->fg = fg;
	ull->colors = NULL;		// simple block
	ull->pattern = pattern;

	for (i = 0; i < 8; i++) {
		vdpdrawrow[*pattern++] (block, fg, bg);
		block += UPDATEBLOCK_ROW_STRIDE;
	}
}

/*	The blank screen must be redrawn, i.e., when a minimized
	window is expanded again.  */
void
redraw_blank(void)
{
	struct updateblock updatelist[768];
	struct updateblock *ull = updatelist;

	u32         i;
	u8         *scptr;

	if (!vdpchanged)
		return;

	/*  Redraw changed chars  */
	for (i = 0, scptr = vdp_changes.screen; i < 768; i++, scptr++) {
		if (*scptr) {			/* this screen pos updated? */
			ull->r = (i >> 5) << 3;
			ull->c = (i & 31) << 3;

			redraw_graphics_block(VDPRAM(0) /*ignored*/,
								  ((vdpregs[7] & 0xf) << 4) | (vdpregs[7] & 0xf),
								  ull);
				
			ull++;				/* next slot */
		}
	}

	memset(vdp_changes.screen, 0, sizeof(vdp_changes.screen));

	if (ull)					/* any changes? (most likely) */
		VIDEO(updatelist, (updatelist, ull - updatelist));
}

void
redraw_graphics(void)
{
	struct updateblock updatelist[768];
	struct updateblock *ull = updatelist;

	u32         i;
	u8         *scptr;
	u32         currchar;

	if (!vdpchanged)
		return;

	/*  Set pattern changes in chars, for sprites  */

	for (i = 0, scptr = vdp_changes.screen; i < 768; i++, scptr++) {
//		currchar = vdpram[vdp_mode.screen.base + i];	/* char # to update */
		currchar = *VDPRAM(vdp_mode.screen.base + i);	/* char # to update */
		if (vdp_changes.patt[currchar])	/* this pattern changed? */
		{
			logger(_L|L_3, _("char %d changed due to patt %d\n"), 
				   i, currchar);
			*scptr = 1;			/* then this char changed */
		}
	}

	/*  vdp_changes.sprite makes changes in vdp_changes.screen */
	vdp_update_sprites();

	/*  Redraw changed chars  */
	for (i = 0, scptr = vdp_changes.screen; i < 768; i++, scptr++) {
		if (*scptr) {			/* this screen pos updated? */
			logger(_L|L_3, _("redrawing char %d\n"), i);
//			currchar = vdpram[vdp_mode.screen.base + i];	/* char # to update */
			currchar = *VDPRAM(vdp_mode.screen.base + i);	/* char # to update */

			ull->r = (i >> 5) << 3;	/* for graphics mode */
			ull->c = (i & 31) << 3;

			redraw_graphics_block(VDPRAM(vdp_mode.patt.base + (currchar << 3)),
								  *VDPRAM(vdp_mode.color.base + (currchar >> 3)), 
								  ull);

				/* can't redraw easily */
			if (*scptr == SC_SPRITE_COVERING)
				ull->pattern = ull->colors = NULL;
				
			ull++;				/* next slot */
		}
	}

	/* draw sprites */
	vdp_redraw_sprites();

	memset(vdp_changes.screen, 0, sizeof(vdp_changes.screen));
	memset(vdp_changes.patt, 0, 256);
	vdp_changes.sprite = 0;
	memset(vdp_changes.sprpat, 0, sizeof(vdp_changes.sprpat));

	if (ull)					/* any changes? (most likely) */
		VIDEO(updatelist, (updatelist, ull - updatelist));
}



static void
redraw_bitmap_block(u8 * pattern, u8 * color, updateblock * ull)
{
	int         i;
	u8          fg, bg;
	u8			*block;

	block = ull->data = UPDPTR(ull->r, ull->c);
	
	ull->fg = vdpfg; ull->bg = vdpbg;
	ull->colors = color;
	ull->pattern = pattern;
	
	for (i = 0; i < 8; i++) {
		bg = *color & 0xf;
		fg = *color >> 4;
		color++;

		vdpdrawrow[*pattern++] (block, fg, bg);
		block += UPDATEBLOCK_ROW_STRIDE;
	}
}


void
redraw_bitmap(void)
{
	struct updateblock updatelist[768];
	struct updateblock *ull = updatelist;

	u32         i;
	u8         *scptr;
	u32         currchar;
	u16         pp, cp;

	if (!vdpchanged)
		return;

	/*  Set pattern or color changes in chars, for sprites  */

	for (i = 0, scptr = vdp_changes.screen; i < 768; i++, scptr++) {
		currchar = *VDPRAM(vdp_mode.screen.base + i);	/* char # to update */
		if (vdp_changes.patt[currchar + (i & 0x300)] ||
			vdp_changes.color[currchar + (i & 0x300)])
			*scptr = 1;
	}


	/*  vdp_changes.sprite makes changes in vdp_changes.screen */
	vdp_update_sprites();

	/*  Redraw changed chars  */
	for (i = 0, scptr = vdp_changes.screen; i < 768; i++, scptr++) {
		if (*scptr) {			/* this screen pos updated? */
			currchar = *VDPRAM(vdp_mode.screen.base + i);	/* char # to update */

			ull->r = (i >> 5) << 3;	/* for graphics mode */
			ull->c = (i & 31) << 3;

			pp = cp = (currchar + (i & 0x300)) << 3;
			pp &= vdp_mode.bitpattmask;
			cp &= vdp_mode.bitcolormask;
			redraw_bitmap_block(VDPRAM(pp + vdp_mode.patt.base), 
								VDPRAM(cp + vdp_mode.color.base),
								ull);

				/* can't redraw easily */
			if (*scptr == SC_SPRITE_COVERING)
				ull->pattern = ull->colors = NULL;

			ull++;				/* next slot */
		}
	}

	/* draw sprites */
	vdp_redraw_sprites();

	memset(vdp_changes.screen, 0, sizeof(vdp_changes.screen));
	memset(vdp_changes.patt, 0, sizeof(vdp_changes.patt));
	memset(vdp_changes.color, 0, sizeof(vdp_changes.color));
	vdp_changes.sprite = 0;
	memset(vdp_changes.sprpat, 0, sizeof(vdp_changes.sprpat));

	if (ull)					/* any changes? (most likely) */
		VIDEO(updatelist, (updatelist, ull - updatelist));
}



static void
redraw_text_block(u8 * pattern, updateblock *ull)
{
	int         i;
	u8			*block;

	block = ull->data = UPDPTR(ull->r, ull->c);
	
	ull->fg = vdpfg;
	ull->bg = vdpbg;
	ull->colors = NULL;
	//ull->pattern = pattern;
	ull->pattern = NULL;		// we redraw 8x8 blocks; this is only 6x8
	
	for (i = 0; i < 8; i++) {
		vdpdrawrowtext[*pattern++] (block);
		block += UPDATEBLOCK_ROW_STRIDE;
	}
}

static void
redraw_text_side_block(updateblock *ull)
{
	int         i;
	u8			*block;
	static u8	ones[8] = { 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff };

	block = ull->data = UPDPTR(ull->r, ull->c);
	ull->fg = vdpbg;
	ull->bg = vdpbg;
	ull->colors = NULL;
	ull->pattern = ones;
	
	for (i = 0; i < 8; i++) {
		vdpdrawrow[0xff] (block, vdpbg, vdpbg);
		block += UPDATEBLOCK_ROW_STRIDE;
	}
}


void
redraw_text(void)
{
	struct updateblock updatelist[960 + 24*2];
	struct updateblock *ull = updatelist;

	u32         i;
	u8         *scptr;
	u32         currchar;

	/*  Set update blocks for text sides */
	if (vdp_redraw_text_sides)
	{
		for (i = 0; i < 24; i++) {
			ull->r = i << 3;
			ull->c = 0;
			
			redraw_text_side_block(ull);
			ull++;

			ull->r = i << 3;
			ull->c = 256 - (256 - 240) / 2;

			redraw_text_side_block(ull);
			ull++;
		}			
		vdp_redraw_text_sides = false;
	}

	if (vdpchanged)
	{
		/*  Set pattern changes in chars */

		for (i = 0, scptr = vdp_changes.screen; i < 960; i++, scptr++) {
			currchar = *VDPRAM(vdp_mode.screen.base + i);	/* char # to update */
			if (vdp_changes.patt[currchar])	/* this pattern changed? */
				*scptr = 1;			/* then this char changed */
		}


		/*  Redraw changed chars  */

		for (i = 0, scptr = vdp_changes.screen; i < 960; i++, scptr++) {
			if (*scptr) {			/* this screen pos updated? */
				currchar = *VDPRAM(vdp_mode.screen.base + i);	/* char # to update */

				ull->r = (i / 40) << 3;	/* for graphics mode */
				ull->c = (i % 40) * 6 + (256 - 240) / 2;

				redraw_text_block(VDPRAM(vdp_mode.patt.base + (currchar << 3)), 
								  ull);
			
				ull++;				/* next slot */
			}
		}

		memset(vdp_changes.screen, 0, sizeof(vdp_changes.screen));
		memset(vdp_changes.patt, 0, sizeof(vdp_changes.patt));

	}

	if (ull)					/* any changes? (most likely) */
		VIDEO(updatelist, (updatelist, ull - updatelist));
}


static void
redraw_multi_block(u8 * color, updateblock *ull)
{
	int         i;
	u8          fg, bg;
	u8			*block;

	static u8	vdp_multi_block_pattern[] = { 
		0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0, 0xf0 };
	
	block = ull->data = UPDPTR(ull->r, ull->c);
	ull->fg = vdpfg; ull->bg = vdpbg;
	ull->colors = color;
	ull->pattern = vdp_multi_block_pattern;

	for (i = 0; i < 2; i++) {
		bg = *color & 0xf;
		fg = *color >> 4;
		color++;

		vdpdrawrow[0xf0] (block, fg, bg);
		vdpdrawrow[0xf0] (block + UPDATEBLOCK_ROW_STRIDE, fg, bg);
		vdpdrawrow[0xf0] (block + UPDATEBLOCK_ROW_STRIDE * 2, fg, bg);
		vdpdrawrow[0xf0] (block + UPDATEBLOCK_ROW_STRIDE * 3, fg, bg);
		block += UPDATEBLOCK_ROW_STRIDE * 4;
	}
}


void
redraw_multi(void)
{
	struct updateblock updatelist[768];
	struct updateblock *ull = updatelist;

	u32         i;
	u8         *scptr;
	u32         currchar;
	u32			patt;

	if (!vdpchanged)
		return;

	/*  Set pattern changes in chars, for sprites  */

	for (i = 0, scptr = vdp_changes.screen; i < 768; i++, scptr++) {
		currchar = *VDPRAM(vdp_mode.screen.base + i);	/* char # to update */
		if (vdp_changes.patt[currchar])	/* this pattern changed? */
			*scptr = 1;			/* then this char changed */
	}

	/*  vdp_changes.sprite makes changes in vdp_changes.screen */
	vdp_update_sprites();

	/*  Redraw changed chars  */
	for (i = 0, scptr = vdp_changes.screen; i < 768; i++, scptr++) {
		if (*scptr) {			/* this screen pos updated? */
			currchar = *VDPRAM(vdp_mode.screen.base + i);	/* char # to update */

			ull->r = (i >> 5) << 3;	/* for graphics mode */
			ull->c = (i & 31) << 3;

			patt = vdp_mode.patt.base + (currchar << 3) + ((i >> 5) & 3) * 2;

			redraw_multi_block(VDPRAM(patt),
							   ull);

				/* can't redraw easily */
			if (*scptr == SC_SPRITE_COVERING)
				ull->pattern = ull->colors = NULL;

			ull++;				/* next slot */
		}
	}

	/* draw sprites */
	vdp_redraw_sprites();

	memset(vdp_changes.screen, 0, sizeof(vdp_changes.screen));
	memset(vdp_changes.patt, 0, 256);
	vdp_changes.sprite = 0;
	memset(vdp_changes.sprpat, 0, sizeof(vdp_changes.sprpat));

	if (ull)					/* any changes? (most likely) */
		VIDEO(updatelist, (updatelist, ull - updatelist));
}

/***************************************/

void
vdp_update(void)
{
	if (features & FE_VIDEO) {
		if (vdp_redraw)
			vdp_redraw();
	}
}

static
DECL_SYMBOL_ACTION(video_showvideo_toggle)
{
	if (task == csa_WRITE) {
		if (MODULE_ITERATE(vmVideo,vmRestopModule) == vmOk)
			MODULE_ITERATE(vmVideo,vmRestartModule);
	}
	return 1;
}

static
DECL_SYMBOL_ACTION(video_draw_sprites_toggle)
{
	if (task == csa_WRITE) {
		vdp_dirty_all();
	}
	return 1;
}

static
DECL_SYMBOL_ACTION(video_change_rates)
{
	video_changing_rates();
	return 1;
}

void
vdpinit(void)
{
	command_symbol_table *videocommands =
		command_symbol_table_new(_("Video Options"),
								 _("These are generic commands for controlling video emulation"),

		 command_symbol_new("ShowVideo",
							_("Control whether the screen is displayed"),
							c_STATIC,
							video_showvideo_toggle,
							RET_FIRST_ARG,
							command_arg_new_toggle
							("off|on",
							 _("toggle video on or off"),
							 NULL /* action */ ,
							 ARG_NUM(features),
							 FE_SHOWVIDEO,
							 NULL /* next */ )
							,

		command_symbol_new("VideoUpdateSpeed",
						   _("Control how often the screen is updated"),
						   c_STATIC,
						   video_change_rates,
						   RET_FIRST_ARG,
						   command_arg_new_num
						   (_("hertz"),
							_("number of times per second"),
							NULL /* action */ ,
							ARG_NUM
							(videoupdatespeed),
							NULL /* next */ )
						   ,

		command_symbol_new("VDPInterruptRate",
						   _("Control how often the VDP interrupts the CPU"),
						   c_STATIC,
						   video_change_rates,
						   RET_FIRST_ARG,
						   command_arg_new_num
						   (_("hertz"),
							_("number of times per second"),
							NULL /* action */ ,
							ARG_NUM
							(vdp_interrupt_rate),
							NULL /* next */ )
						   ,

		 command_symbol_new("DrawSprites",
							_("Control whether sprites are displayed"),
							c_STATIC,
							video_draw_sprites_toggle,
							RET_FIRST_ARG,
							command_arg_new_enum
							("off|on",
							 _("toggle sprites on or off"),
							 NULL /* action */ ,
							 ARG_NUM(draw_sprites),
							 NULL /* next */ )
							,

		 command_symbol_new("FiveSpritesOnLine",
							_("Obey five-sprites-on-a-line limit of TMS9918A"),
							c_STATIC,
							NULL /*action*/,
							RET_FIRST_ARG,
							command_arg_new_enum
							("off|on",
							 _("on: fifth sprite on a line not drawn (default); "
							 "off: all sprites always drawn"),
							 NULL /* action */ ,
							 ARG_NUM(five_sprites_on_a_line),
							 NULL /* next */ )
							,

		  NULL /* next */ ))))),

		 NULL /* sub */ ,

		 NULL	/* next */
		);

	command_symbol_table_add_subtable(universe, videocommands);

	features |= FE_VIDEO;
	memset(vdpregs, 0, 8);
	vdpregs[1] = 0xe0;
	vdp_update_mode();
	vdp_update_params();
}

/*
 *	Callbacks from emulate.c
 */
DECL_SYMBOL_ACTION(vdp_set_register)
{
	int reg, val;
	if (task == csa_READ) {
#if 0
		if (iter > 9)
			return 0;
#else
		if (iter >= 8)
			return 0;
#endif
		command_arg_set_num(sym->args, iter);
		if (iter < 8)
			command_arg_set_num(sym->args->next, vdpregs[iter]);
		else if (iter == 8)
			command_arg_set_num(sym->args->next, vdpreadahead);
		else if (iter == 9)
			command_arg_set_num(sym->args->next, vdpaddrflag);
		return 1;
	}

	command_arg_get_num(sym->args, &reg);
	command_arg_get_num(sym->args->next, &val);

	if (reg < 8) {
		vdpwritereg(0x8000 | ((reg & 0x7) <<8) | (val & 0xff));
	} 
	// deprecated!  Use VDPAddrFlag and VDPReadAhead now
	else if (reg == 8) {
		vdpreadahead = val;
	} else if (reg == 9) {
		vdpaddrflag = val;
	}
	return 1;
}

DECL_SYMBOL_ACTION(vdp_set_addr_flag)
{
	int val;
	if (task == csa_READ) {
		if (iter > 0) return 0;
		command_arg_set_num(sym->args, vdpaddrflag);
		return 1;
	}

	command_arg_get_num(sym->args, &val);
	vdpaddrflag = val;

	return 1;
}

DECL_SYMBOL_ACTION(vdp_set_read_ahead)
{
	int val;

	if (task == csa_READ) {
		if (iter > 0) return 0;
		command_arg_set_num(sym->args, vdpreadahead);
		return 1;
	}

	command_arg_get_num(sym->args, &val);
	vdpreadahead = val;

	return 1;
}

#ifdef WITH_LIB_PNG
#include <sys/stat.h>
#include <png.h>

static int
vdp_save_screen_png(char *filename)
{
	FILE *fp;
	png_structp png_ptr;
	png_infop info_ptr;
	png_color png_palette[17];
	int i;
	png_time ptime;
//	png_color_16 pbkgd;
	png_text ptext;
	png_byte *row_pointers[256];

	fp = fopen(filename, "wb");
	if (!fp)
	{
		command_logger(_L|LOG_USER|LOG_ERROR, _("Could not open '%s' for writing\n"), filename);
	}

	png_ptr = png_create_write_struct(
		PNG_LIBPNG_VER_STRING,
		(png_voidp) 0L /* error_ptr */,
		0L /* error_fn */,
		0L /* warning_fn */);
	if (!png_ptr)
	{
		command_logger(_L|LOG_USER|LOG_ERROR, _("Could not initialize PNG subsystem\n"));
		return 0;
	}

	info_ptr = png_create_info_struct(png_ptr);
	if (!info_ptr)
	{
		command_logger(_L|LOG_USER|LOG_ERROR, _("Could not initialize PNG subsystem\n"));
		png_destroy_write_struct(&png_ptr, (png_infopp)0L);
		return 0;
	}

	if (setjmp(png_ptr->jmpbuf))
	{
		command_logger(_L|LOG_USER|LOG_ERROR, _("Failed to write PNG file '%s'\n"), filename);
		png_destroy_write_struct(&png_ptr, &info_ptr);
		fclose(fp);
		return 0;
	}

	/* set up filters */
	png_set_filter(png_ptr, 0, 
				   PNG_FILTER_NONE | PNG_FILTER_SUB |
				   PNG_FILTER_PAETH);

	/* set up compression */
	png_set_compression_level(png_ptr, Z_BEST_COMPRESSION);

	/* set up header */
	png_set_IHDR(png_ptr, info_ptr, screenxsize, screenysize,
				 8, PNG_COLOR_TYPE_PALETTE, PNG_INTERLACE_NONE,
				 PNG_COMPRESSION_TYPE_DEFAULT,
				 PNG_FILTER_TYPE_DEFAULT);

	/* set up palette */
	for (i = 0; i <= 16; i++)
	{
		int j = (i == 0 ? vdpbg : i == 16 ? vdpfg : i);
		png_palette[i].red = vdp_palette[j][0];
		png_palette[i].green = vdp_palette[j][1];
		png_palette[i].blue = vdp_palette[j][2];
	}
	png_set_PLTE(png_ptr, info_ptr, png_palette, 16);

	/* set up time */
	png_convert_from_time_t(&ptime, time(0L));
	png_set_tIME(png_ptr, info_ptr, &ptime);

	/* set up background and transparency */
//	pbkgd.index = 0; /* paletted */
//	png_set_tRNS(png_ptr, info_ptr, (png_bytep)&pbkgd.index, 1, 0L /*color16*/);
//	png_set_bKGD(png_ptr, info_ptr, &pbkgd);

	/* info text */
	ptext.compression = PNG_TEXT_COMPRESSION_NONE;
	ptext.key = "Software";
	ptext.text = "V9t9";
	ptext.text_length = strlen(ptext.text);
	png_set_text(png_ptr, info_ptr, &ptext, 1);

	/* point to file */
	png_init_io(png_ptr, fp);

	/* write info */
	png_write_info(png_ptr, info_ptr);

	/* write rows */
	for (i = 0; i < screenysize; i++)
	{
		row_pointers[i] = UPDPTR(i, 0);
	}
	png_write_image(png_ptr, row_pointers);

	/* done! */
	png_write_end(png_ptr, info_ptr);

	png_destroy_write_struct(&png_ptr, &info_ptr);

	fclose(fp);
	return 1;
}

#endif	// WITH_LIB_PNG

static char *vdp_auto_name(void)
{
	int count = 0;
	static char *pattern = "scrn-%03d.png";
	static char buffer[OS_NAMESIZE];
	OSSpec spec;
	while (count < 999) {
		sprintf(buffer, pattern, count);
		if (!data_find_file(0L, buffer, &spec)) {
			command_logger(_L|LOG_USER, _("Writing to '%s'\n"), buffer);
			return buffer;
		}
		count++;
	}
	return NULL;
}

DECL_SYMBOL_ACTION(vdp_take_screenshot)
{
	if (task == csa_WRITE) {
		char *filename;
		char path[OS_PATHSIZE];
		OSSpec spec;

		if (!command_arg_get_string(SYM_ARG_1st, &filename))
			return 0;

		if (!filename || !*filename)
			filename = vdp_auto_name();

		if (!filename) {
			command_logger(_L|LOG_ERROR|LOG_USER, _("Could not make a filename for screen shot\n"));
			return 0;
		}

		if (!data_create_file(0L, filename, &spec, &osBinaryType)) {
			command_logger(_L|LOG_ERROR|LOG_USER, _("Could not create '%s' for screen shot\n"),
						   filename);
			return 0;
			
		}

		OS_SpecToString2(&spec, path);
#ifdef WITH_LIB_PNG
		return vdp_save_screen_png(path);
#endif 
		command_logger(_L|LOG_USER|LOG_ERROR, _("No graphics file formats supported!\n"));
		return 0;
	}
	return 1;
}
