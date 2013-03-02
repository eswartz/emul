/*
  vdp.h							-- delcarations for VDP processor 

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

#ifndef __VDP_H__
#define __VDP_H__

#include "command.h"

#include "centry.h"

#include "video.h"

/*  vdpaddr == register value  */
extern void vdpupdate(u32 addr);
extern void vdpcompleteredraw(void);
extern void	vdpinit(void);

/*
 *	Force current changes to be sent to video module
 */
extern void	vdp_update(void);

/*	
 * 	Notify that an address in VDP has been modified externally,
 *	and return true if the change was at all visible.
 */
extern bool	vdp_touch(u32 addr);

extern 	u16	 vdp_mmio_get_addr(void);
extern	void vdp_mmio_set_addr(u16 addr);
extern	bool vdp_mmio_addr_is_complete(void);

extern 	void vdp_memory_init(void);
extern	void vdp_mmio_write(u16 addr,u8 val);
extern	s8 	 vdp_mmio_read(u16 addr);

extern	u8 	vdp_mmio_get_status(void);
extern	void vdp_mmio_set_status(u8 status);
/*
 *	Force an update of a given region of the screen.
 *	Coordinates and offsets given in pixels.
 */
extern void vdp_redraw_screen(s32 x, s32 y, s32 dx, s32 dy);

extern	u32 screenxsize,screenysize;
extern	u8	vdpregs[8];	
extern	u8 vdpbg,vdpfg;

extern 	bool draw_sprites;
extern 	bool five_sprites_on_a_line;
extern	int	videoupdatespeed;

//extern	u8	vdpstatus;

#define VDP_INTERRUPT 	0x80
#define VDP_COINC		0x40
#define VDP_FIVE_SPRITES 0x20
#define VDP_FIFTH_SPRITE 0x1f

// Palette of standard TI colors.
// Each one is RGB, in that order, from 0 to 255.
// Color 0, which is clear, and color 17, which is
// the foreground in text mode, may be ignored,
// but beware that these will be generated in updarea.
extern u8 vdp_palette[17][3];

#define RGB_8_TO_16(x)	(((x)<<8) + (((x)&1) ? 0xff : 0))
#define RGB_8_TO_6(x)	((x) >> 2)

#define SPRBIT(x) (1<<(x))

#define R0_BITMAP 2
#define R0_EXTERNAL 1

#define R1_RAMSIZE 128
#define R1_NOBLANK 64
#define R1_INT 32
#define R1_TEXT 16
#define R1_MULTI 8
#define R1_SPR4 2
#define R1_SPRMAG 1

#define MODE_BITMAP 1
#define MODE_GRAPHICS 0
#define MODE_TEXT 2
#define MODE_MULTI 3

#define VDP_IS_EXTERNAL_VIDEO() ((vdpregs[0] & R0_EXTERNAL)!=0)

/*
 *	Command interface callbacks
 */
DECL_SYMBOL_ACTION(vdp_set_register);
DECL_SYMBOL_ACTION(vdp_set_read_ahead);
DECL_SYMBOL_ACTION(vdp_set_addr_flag);
DECL_SYMBOL_ACTION(vdp_take_screenshot);


/*
 *	This information should only be visible to vdp.c and vdpsprites.c
 */

/*	Special exception for composite mode */

extern	u8 vdp_updarea[UPDATEBLOCK_ROW_STRIDE*256];
#define UPDPTR(y,x) (&vdp_updarea[((y)*UPDATEBLOCK_ROW_STRIDE)+(x)+32])


#ifdef __VDP_INTERNAL__

/*	Values in screenchanges[] after handlespritechanges() */
enum
{
	SC_UNTOUCHED 	= 0,	// block not changed
	SC_BACKGROUND	= 1,	// background changed
	SC_SPRITE_DELETED = 2,	// sprite deleted, revealing block
	SC_SPRITE_COVERING = 3	// sprite is covering the block
};

extern	u8 screenchanges[960];
extern	u8 pattchanges[768];
extern	u8 colorchanges[768];

extern	u16 bitpattmask,bitcolormask;

extern	u8 sprpatchanges[256];
extern	u8 sprpatvisible[256];
extern	u32 spritechanges;

extern	u32	sprwidth;	/* in pixels */

extern void (*vdpdrawrow[])(u8 *,u8,u8);
extern void (*vdpdrawrowtext[])(u8 *);

typedef void (*vdp_redrawfunc)(void);

//extern	redrawfunc redrawscreen;

typedef void (*vdp_modifyfunc)(u32 addr);

typedef struct vdp_area
{
	u32	base, size;
}	vdp_area;

typedef struct vdp_mode_info
{
	vdp_area screen,	 	// screen image table
		patt, 				// pattern definition table
		color, 				// color definition table
		sprite, 			// sprite definition table
		sprpat;				// sprite pattern definition table
	u16 bitpattmask,bitcolormask;	// address masks for bitmap tables
}	vdp_mode_info;

extern	vdp_mode_info vdp_mode;

typedef struct vdp_modify_info
{
	vdp_modifyfunc screen,	// modified screen image table
		patt,				// modified pattern definition table
		color,				// modified color definition table
		sprite,				// modified sprite definition table
		sprpat;				// modified sprite pattern table
}	vdp_modify_info;

extern 	vdp_modify_info vdp_modify;

typedef struct vdp_changes_info
{
	u8	screen[1024];		// 1: block changed
	u8	patt[768];			// 1: pattern changed
	u8	color[768];			// 1: color changed
	u32 sprite;				// (1<<x): sprite #x changed
	u8	sprpat[256];		// 1: sprite pattern changed
}	vdp_changes_info;		

extern vdp_changes_info vdp_changes;

#endif // __VDP_INTERNAL__

#include "cexit.h"

#endif
