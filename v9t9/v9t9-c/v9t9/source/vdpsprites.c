
/*
  vdpsprites.c					--	handle sprite-change functions and drawing.

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
#include "video.h"
#include "vdp.h"
#include "vdpsprites.h"
#include "memory.h"

#define _L	 LOG_SPRITES | LOG_INFO

#define SPRITE_PTR(x)	(struct sprite *)(FLAT_MEMORY_PTR(md_video, vdp_mode.sprite.base) + (x)*4)
#define SPRPAT_PTR(x)	(FLAT_MEMORY_PTR(md_video, vdp_mode.sprpat.base) + ((x) << 3))

/*	
	Structure representing a sprite table entry in VDP RAM.

	y is one pixel less than the real screen row (255 = top of screen)	
	y==0xd0 	==> all sprites here and beyond are 'deleted'
	color&0x80  ==> early clock on, move sprite 32 pixels to the left
*/
struct sprite {
	 u8          y, x;
	 u8          ch, color;
};

#define GETX(sp) (((sp)->color&0x80) ? ((sp)->x)-32 : (sp)->x)
#define GETY(sp) (((sp)->y+1)&255)

struct sprite oldsprites[32];
int         olddelptr;
struct sprite *newsprites;
int         newdelptr;

u32         toredraw;

u32         sprbitmap[32 * 32];	/* where are sprites? */

static u32  sprlines[256];	/* sprites on each line */
static u8   sprlinecnt[256];	/* counts of # bits set */

/*
	This function does the mammoth tasks of:
	
	0)  Change sprites under whom chars changed
	1)  See if sprite pattern change forces a sprite update.
	2)  See if position change forces a sprite update.
	3)  See if deleted-ptr changes, forcing sprite updates.
	4)  Update vdp_changes.screen where sprites dance.
	5)  Force updates to sprites under changed ones.
	6)  Weed out blank sprites.
	7)  Set VDP status flags for coincidence and five sprites on a line (hackish)
*/
void        
vdp_update_sprites(void)
{
	int         i;
	struct sprite *sp, *osp;
	int         deleted;
	u32         moved;
	u32         olddelbitmap, newdelbitmap;
	u32         nearchanges;

	int			five_sprites = -1;

	if (!draw_sprites) 
		return;

	logger(_L | L_2, "---------");

	toredraw = 0;
	newsprites = SPRITE_PTR(0);
	sp = newsprites;
	osp = oldsprites;
	deleted = 0;
	newdelptr = 32;

	logger(_L | L_2, _("spritechanges: %08X\n\n"), vdp_changes.sprite);
	if (log_level(LOG_SPRITES) >= 2)
		for (i = 0; i < 32; i++) {
			if (vdp_changes.sprite & SPRBIT(i)) {
				logger(_L | L_3, _("Sprite %02d changed: %s%s%s\n"), i,
					 (sp->x != osp->x || sp->y != osp->y) ? _("pos, ") : "",
					 (sp->color != osp->color) ? _("color, ") : "",
					 (sp->ch != osp->ch) ? _("patt") : "");
			}
			sp++;
			osp++;
		}


	/*  Check deleted ptr and force redraws  */
	sp = newsprites;
	osp = oldsprites;

	newdelbitmap = olddelbitmap = 0;
	for (i = 0; i < 32; i++) {
		/*  Check deleted state  */
		if (!deleted) {
			deleted = (sp->y == 0xd0);
			if (deleted) {
				logger(_L | L_2, _("Deleted sprites past %d\n"), i);
				newdelptr = i;
			}
		}

		if (i >= olddelptr && i < newdelptr) {	/* need to draw undeleted sprite */
			vdp_changes.sprite |= SPRBIT(i);
			logger(_L | L_1, _("Updating sprite %d because undeleted\n"), i);
		} else if (i >= newdelptr && i < olddelptr) {	/* need to delete sprite */
			newdelbitmap |= SPRBIT(i);	/* newly deleted */
			logger(_L | L_1, _("Updating sprite %d because deleted\n"), i);
		} else
			/*  Check for pattern changes  */
		if (i < newdelptr) {	/* definitely not deleted */
			if (vdp_changes.sprpat[sp->ch]) {
				logger(_L | L_1, _("Sprite %d changed pattern\n"), i);
				vdp_changes.sprite |= SPRBIT(i);
			}
		} else
			/*  still deleted  */
		{
			/*vdp_changes.sprite&=~SPRBIT(i); */
			olddelbitmap |= SPRBIT(i);	/* still deleted */
			/* deleted in either case, can't redraw */
		}

		sp++;
	}

//  vdp_changes.sprite&=~(olddelbitmap|newdelbitmap); /* deleted, don't bother */

	logger(_L | L_1, _("VDP_CHANGES.SPRITE: %04X  DELBITMAP: %04X/%04X\n"), vdp_changes.sprite,
		 olddelbitmap, newdelbitmap);

	if (newdelptr == 32)
		logger(_L | L_2, _("No deleted sprites\n"));


	/*  Now, check sprite moves, and redraw.  

	   This looks at the old bitmap.
	 */

	/*  1)  Which sprites moved?  
	   We consider newly deleted sprites to have moved.
	 */
	osp = oldsprites;
	sp = newsprites;
	moved = newdelbitmap;
	for (i = 0; i < 32; i++) {
//      if (i>=newdelptr && i<olddelptr)
//          moved|=SPRBIT(i);
//      else
		if ((osp->x != sp->x || osp->y != sp->y))
			moved |= SPRBIT(i);
		osp++;
		sp++;
	}

	/*  2)  Where WERE they?  */
	for (i = 0; i < 768; i++) {
		if (sprbitmap[i] & (vdp_changes.sprite | newdelbitmap))	/* old bitmap */
		{
			logger(_L|L_3, _("redrawing char %d due to deleted sprite\n"), i);
			vdp_changes.screen[i] = SC_SPRITE_DELETED;
		}
	}


	/*  Now, create new sprite bitmap.  */

	memset((void *) sprbitmap, 0, sizeof(sprbitmap));
	memset((void *) sprlines, five_sprites_on_a_line ? 0 : -1, sizeof(sprlines));
	memset((void *) sprlinecnt, 0, sizeof(sprlinecnt));
	five_sprites = -1;
	sp = newsprites;

	for (i = 0; i < 32; i++) {
		u8 px, py;		// pixel coordinate
		u8 bx, by;		// block coordinate
		int dx, dy;		// delta for sprite
		int pyl;

		py = GETY(sp);
		by = (py >> 3);
		dy = (py & 7) + sprwidth;


		/*  count sprites on each line and keep track
			of which of the first four we can draw */

		pyl = (py + sprwidth) & 255;
		while (py != pyl) {
			sprlinecnt[py]++;
			if (sprlinecnt[py] > 4) {
				/* record fifth sprite */
				if (five_sprites == -1) five_sprites = i;
			} else {
				/* set bit so this sprite will be drawn */
				sprlines[py] |= SPRBIT(i);
			}
			py = (py+1) & 255;
		}
	
		while (dy > 0) {

			px = GETX(sp);
			bx = (px >> 3);
			dx = (px & 7) + sprwidth;

			while (dx > 0) {
				/* very rough check for sprite coincidence:  FIXME !!! */
				if (sprbitmap[(by << 5) + bx])
					vdp_mmio_set_status(vdp_mmio_get_status() | VDP_COINC);

				sprbitmap[(by << 5) + bx] |= SPRBIT(i);

				bx = (bx + 1) & 31;
				dx -= 8;
			}

			by = (by + 1) & 31;
			dy -= 8;
		}
		sp++;
	}

	if (five_sprites != -1) {
		vdp_mmio_set_status( (vdp_mmio_get_status() 
							  & ~(VDP_FIVE_SPRITES | VDP_FIFTH_SPRITE))
							 | VDP_FIVE_SPRITES | i );

		//vdp_changes.sprite |= -1;
		if (log_level(LOG_SPRITES) >= 3) {
			logger(_L|L_3, _("Five sprites on lines:\n"));
			for (i=0; i<256; i++) {
				if (sprlinecnt[i] > 4) {
					u32 b, n;
					logger(_L|L_3, "%d: ", i);
					b = 1;
					n = 0;
					while (b) {
						if (sprlines[i] & b) {
							logger(_L|L_3, "%d,", n);
						}
						n++;
						b <<= 1;
					}
					logger(_L|L_3, "\n");
				}
			}
		}
	} else {
		vdp_mmio_set_status( (vdp_mmio_get_status() 
							  & ~(VDP_FIVE_SPRITES | VDP_FIFTH_SPRITE)) );
	}

	/*  Where chars change, force sprite update  */
	for (i = 0; i < 768; i++)
		if (vdp_changes.screen[i])
			vdp_changes.sprite |= sprbitmap[i];


	/*
	   Now, set all the sprites below/above a changed one 

	   Do this by walking sprbitmap.  If sprbitmap&vdp_changes.sprite,
	   this means this char is affected by a change, so we need to
	   redraw all other sprites on that char, so vdp_changes.sprite|sprbitmap.
	 */

/*	for (i=0; i<768; i++)
 *	{
 *	
 *		if (sprbitmap[i]&vdp_changes.sprite)
 *			vdp_changes.sprite|=sprbitmap[i];
 *  }
 *
 *	This is not the correct solution. Imagine a diagonal line
 *	of sprites from the top left to the screen to the bottom right,
 *  in increasing sprite number.  When the bottommost one changes,
 *	we'll get the next one above it with this algorithm, but not
 *	the one on top of *that* one, etc.  The "solution" to recursively
 *	redraw every sprite on up the line would lead to terrible 
 *	performance problems in these cases.
 *
 *	What we need to do is keep track of sprites that are redrawn
 *	in the vicinity of a real changed sprite, and avoid setting
 *	their bits in vdp_changes.screen[].  That's what nearchanges is.
 */

	nearchanges = 0;
	if (!five_sprites_on_a_line) {
		for (i = 0; i < 768; i++) {
			if (sprbitmap[i] & vdp_changes.sprite) 
				nearchanges |= sprbitmap[i];
		}
	} else {
		for (i = 0; i < 32; i++) {
			if (sprlines[i] & vdp_changes.sprite) {
				nearchanges |= sprlines[i];
			}
		}
	}

	/*  Don't *draw* deleted sprites */
	vdp_changes.sprite &= ~(newdelbitmap);

	/*  Now, figure which sprites should be drawn  */
	sp = newsprites;
	toredraw = 0;
	for (i = 0; i < 32; i++) {
		if ((vdp_changes.sprite | nearchanges) & SPRBIT(i)) {
			/*  CHECK BLANK SPRITE PATTERNS  */

			if (i < newdelptr && (sp->color & 15)) {
				toredraw |= SPRBIT(i);
				logger(_L | L_3, _("going to redraw sprite %d at %d,%d\n"), i, GETX(sp),
					 GETY(sp));
			}
		}
		sp++;
	}


	/*
	   Mark all the screen positions that need to be updated  

	   If sprbitmap&vdp_changes.sprite, then that space needs update.

	   NOT toredraw here -- clear sprites aren't drawn but the spaces
	   they occupy may have changed (if they turn clear -- as in carwars)
	 */

	logger(_L | L_1, _("Toredraw: %08X  spritechanges: %08X\n"), toredraw, vdp_changes.sprite);

	for (i = 0; i < 768; i++) {
		if (sprbitmap[i] & (vdp_changes.sprite | newdelbitmap))
		{
			logger(_L|L_3, _("redrawing char %d due to moved/deleted sprite\n"), i);
			vdp_changes.screen[i] = SC_SPRITE_COVERING;
		}
	}

	if (log_level(LOG_VIDEO) > 3)
	{
		logger(_L, _("screenchanges:\n\t"));
		for (i = 0; i < 768; i++) {
			logger(_L, "%1d ", vdp_changes.screen[i]);
			if ((i & 31) == 31)
				logger(_L, "\n\t");
		}
	}

	vdp_changes.sprite = 0;
	memset(vdp_changes.sprpat, 0, sizeof(vdp_changes.sprpat));
	memcpy((void *) oldsprites, (void *) newsprites, 128);
	olddelptr = newdelptr;
}

/*
	This sprite coincidence routine is probably not the same
	one used in the TMS9918A, which I fear actually intersects
	the pixels of the sprite patterns as they appear on the screen. 
*/
static int
spritecoinc(struct sprite *a, struct sprite *b)
{
	s32         ax0, ay0;
	s32         bx0, by0;

	ax0 = GETX(a);
	ay0 = GETY(a);

	bx0 = GETX(b);
	by0 = GETY(b);

	/*  See if 'b' intersects 'a' */
	if (((bx0 >= ax0 && bx0 < ax0 + sprwidth)
		 || (bx0 + sprwidth > ax0 && bx0 <= ax0)) 
		&& 
		((by0 >= ay0 && by0 < ay0 + sprwidth)
		 || (by0 + sprwidth > ay0 && by0 <= ay0))) 
	{
		logger(_L | L_2, _("Sprite at (%d,%d) intersects (%d,%d)\n"), ax0, ay0, bx0, by0);
		return 1;
	} else
		return 0;

}

/****************************************************/

/*
	Draw the sprites we have set to update.
	This is called after graphics chars have been changed into
		updatelist.
	updblock is a copy of the screen.  just draw on it.
*/

static int vdp_sprite_current;

/*	'x' is an int, which is necessary for proper positioning
	of the sprite once the early clock has been calculated.
	'y' is a u8 so we can avoid clipping it to range. */
INLINE void
drawspritechar(int x, u8 y, u8 * patt, u8 color)
{
	u8         *block;
	u8          mask;
	int         xx, yy;
	s8          shift;

	shift = ((color & 0x80) ? -32 : 0);
	block = UPDPTR(y, x + shift);
	for (yy = 0; yy < 8; yy++) {
		if (sprlines[y & 255] & (1 << vdp_sprite_current)) {
			mask = 0x80;
			for (xx = 0; xx < 8; xx++) {
				if (*patt & mask)
					if (x + xx < 256)
						*(block + xx) = color & 0xf;
				mask >>= 1;
			}
		}
		patt++;
		block += UPDATEBLOCK_ROW_STRIDE;
		y++;
		if (y == 0)
			block = UPDPTR(0, x + shift);
	}
}


/*	'x' is an int, which is necessary for proper positioning
	of the sprite once the early clock has been calculated.
	'y' is a u8 so we can avoid clipping it to range. */
INLINE void
drawspritecharmag(int x, u8 y, u8 * patt, u8 color)
{
	u8         *block;
	u8          mask;
	int         xx, yy;
	s8          shift;

	shift = ((color & 0x80) ? -32 : 0);
	block = UPDPTR(y, x + shift);
	for (yy = 0; yy < 16; yy++) {
		mask = 0x80;
		if (sprlines[y & 255] & (1 << vdp_sprite_current)) {
			for (xx = 0; xx < 16; xx++) {
				if (*patt & mask)
					if (x + xx < 256)
						*(block + xx) = color & 0xf;
				if (xx & 1)
					mask >>= 1;
			}
		}
		if (yy & 1)
			patt++;
		block += UPDATEBLOCK_ROW_STRIDE;
		y++;
		if (y == 0)
			block = UPDPTR(0, x + shift);
	}
}

static void
drawsprite8x8(struct sprite *sp)
{
	int         x, y;

	x = sp->x;
	y = GETY(sp);
	drawspritechar(x, y, SPRPAT_PTR(sp->ch), sp->color);
}

static void
drawsprite16x16(struct sprite *sp)
{
	int         x, y;
	u8			*ptr = SPRPAT_PTR(sp->ch);

	x = sp->x;
	y = GETY(sp);
	drawspritechar(x, y, ptr, sp->color);
	drawspritechar(x + 8, y, ptr + 16, sp->color);
	drawspritechar(x, y + 8, ptr + 8,  sp->color);
	drawspritechar(x + 8, y + 8, ptr + 24, sp->color);
}

static void
drawspritemag8x8(struct sprite *sp)
{
	int         x, y;

	x = sp->x;
	y = GETY(sp);
	drawspritecharmag(x, y, SPRPAT_PTR(sp->ch), sp->color);
}

static void
drawspritemag16x16(struct sprite *sp)
{
	int         x, y;
	u8			*ptr = SPRPAT_PTR(sp->ch);

	x = sp->x;
	y = GETY(sp);
	drawspritecharmag(x, y, ptr, sp->color);
	drawspritecharmag(x + 16, y, ptr + 16, sp->color);
	drawspritecharmag(x, y + 16, ptr + 8,  sp->color);
	drawspritecharmag(x + 16, y + 16, ptr + 24, sp->color);
}


void
vdp_redraw_sprites(void)
{
	int         i;
	void        (*func) (struct sprite *) = 0L;

	if (!draw_sprites)
		return;

	switch (vdpregs[1] & (R1_SPRMAG + R1_SPR4)) {
	case 0:
		func = drawsprite8x8;
		break;
	case R1_SPRMAG:
		func = drawspritemag8x8;
		break;
	case R1_SPR4:
		func = drawsprite16x16;
		break;
	case R1_SPR4 + R1_SPRMAG:
		func = drawspritemag16x16;
	}

	for (i = 31; i >= 0; i--) {
		vdp_sprite_current = i;
		if (toredraw & SPRBIT(i)) {
			logger(_L | L_2, _("Redrawing sprite #%d [%d,%d,%02x,%02x]\n"), i,
				   newsprites[i].x,newsprites[i].y,newsprites[i].ch,
				   newsprites[i].color);
			func(&newsprites[i]);
		}
	}
}
