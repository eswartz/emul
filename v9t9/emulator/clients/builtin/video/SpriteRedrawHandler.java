/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import java.util.Arrays;

import v9t9.engine.memory.ByteMemoryAccess;
import v9t9.engine.memory.MemoryDomain;

/**
 * @author ejs
 * 
 */
public class SpriteRedrawHandler extends BaseRedrawHandler {

	protected VdpTouchHandler modify_sprite_default = new VdpTouchHandler() {

		public void modify(int offs) {
			vdpChanges.sprite |= InternalVdp.SPRBIT(offs >> 2);
			vdpchanged = 1;
		}

	};
	protected VdpTouchHandler modify_sprpat_default = new VdpTouchHandler() {

		public void modify(int offs) {
			int patt;

			if ((vdpregs[1] & InternalVdp.R1_SPR4) != 0) {
				patt = (offs >> 3) & 0xfc;
				Arrays.fill(vdpChanges.sprpat, patt, patt + 4, (byte) 1);
			} else {
				patt = offs >> 3;
				vdpChanges.sprpat[patt] = 1;
			}

			vdpchanged = 1;
		}

	};

	public SpriteRedrawHandler(byte[] vdpregs, MemoryDomain vdpMemory,
			VdpChanges vdpChanges, VdpCanvas vdpCanvas) {
		super(vdpregs, vdpMemory, vdpChanges, vdpCanvas);

		int ramsize = (vdpregs[1] & InternalVdp.R1_RAMSIZE) != 0 ? 0x3fff
				: 0xfff;

		vdpModeInfo.sprite.base = (vdpregs[5] * 0x80) & ramsize;
		vdpModeInfo.sprite.size = 128;
		vdpModeInfo.sprpat.base = (vdpregs[6] * 0x800) & ramsize;
		vdpModeInfo.sprpat.size = 2048;

		vdpTouchBlock.sprite = modify_sprite_default;
		vdpTouchBlock.sprpat = modify_sprpat_default;
	}

	@Override
	public boolean touch(int addr) {
		boolean visible = false;

		if (vdpModeInfo.sprite.base <= addr
				&& addr < vdpModeInfo.sprite.base + vdpModeInfo.sprite.size) {
			vdpTouchBlock.sprite.modify(addr - vdpModeInfo.sprite.base);
			visible = true;
		}

		if (vdpModeInfo.sprpat.base <= addr
				&& addr < vdpModeInfo.sprpat.base + vdpModeInfo.sprpat.size) {
			vdpTouchBlock.sprpat.modify(addr - vdpModeInfo.sprpat.base);
			visible = true;
		}

		return visible;
	}

	/*
	 * Structure representing a sprite table entry in VDP RAM.
	 * 
	 * y is one pixel less than the real screen row (255 = top of screen)
	 * y==0xd0 ==> all sprites here and beyond are 'deleted' color&0x80 ==>
	 * early clock on, move sprite 32 pixels to the left
	 */
	class Sprite {
		int y, x;
		int ch, color;

		int getX() {
			return ((color & 0x80) != 0) ? x - 32 : x;
		}

		int getY() {
			return (y + 1) & 0xff;
		}
	}

	private static boolean five_sprites_on_a_line = true;

	private Sprite[] oldsprites;
	private int olddelptr;
	private Sprite[] newsprites;
	private int newdelptr;

	private int toredraw;

	private int[] sprbitmap = new int[32 * 32]; /* where are sprites? */

	private int[] sprlines = new int[256]; /* bitmap of sprites on each line */
	private byte[] sprlinecnt = new byte[256]; /* counts of # bits set */

	private int SPRBIT(int idx) {
		return 1 << idx;
	}

	/**
	 * This function does the mammoth tasks of:
	 * 
	 * 0) Change sprites under whom chars changed 1) See if sprite pattern
	 * change forces a sprite update. 2) See if position change forces a sprite
	 * update. 3) See if deleted-ptr changes, forcing sprite updates. 4) Update
	 * vdp_changes.screen where sprites dance. 5) Force updates to sprites under
	 * changed ones. 6) Weed out blank sprites. 7) Set VDP status flags for
	 * coincidence and five sprites on a line (hackish)
	 * 
	 * @return the updated VDP status bits
	 */
	public byte updateSpriteCoverage(byte vdpStatus) {
		int i;
		boolean deleted;
		int moved;
		int olddelbitmap, newdelbitmap;
		int nearchanges;

		int five_sprites = -1;

		toredraw = 0;
		newsprites = readSprites();
		deleted = false;
		newdelptr = 32;

		/* Check deleted ptr and force redraws */

		newdelbitmap = olddelbitmap = 0;
		for (i = 0; i < 32; i++) {
			Sprite sp = newsprites[i];

			/* Check deleted state */
			if (!deleted) {
				deleted = (sp.y == 0xd0);
				if (deleted) {
					newdelptr = i;
				}
			}

			if (i >= olddelptr && i < newdelptr) { /*
													 * need to draw undeleted
													 * sprite
													 */
				vdpChanges.sprite |= SPRBIT(i);
				// logger(_L | L_1, _("Updating sprite %d because undeleted\n"),
				// i);
			} else if (i >= newdelptr && i < olddelptr) { /* need to delete sprite */
				newdelbitmap |= SPRBIT(i); /* newly deleted */
				// logger(_L | L_1, _("Updating sprite %d because deleted\n"),
				// i);
			} else
			/* Check for pattern changes */
			if (i < newdelptr) { /* definitely not deleted */
				if (vdpChanges.sprpat[sp.ch] != 0) {
					// logger(_L | L_1, _("Sprite %d changed pattern\n"), i);
					vdpChanges.sprite |= SPRBIT(i);
				}
			} else {
				/* still deleted */
				olddelbitmap |= SPRBIT(i); /* still deleted */
				/* deleted in either case, can't redraw */
			}
		}

		// logger(_L | L_1,
		// _("VDP_CHANGES.SPRITE: %04X  DELBITMAP: %04X/%04X\n"),
		// vdp_changes.sprite,
		// olddelbitmap, newdelbitmap);

		// if (newdelptr == 32)
		// logger(_L | L_2, _("No deleted sprites\n"));

		/*
		 * Now, check sprite moves, and redraw.
		 * 
		 * This looks at the old bitmap.
		 */

		/*
		 * 1) Which sprites moved? We consider newly deleted sprites to have
		 * moved.
		 */
		moved = newdelbitmap;
		for (i = 0; i < 32; i++) {
			if (oldsprites != null) {
				Sprite osp = oldsprites[i];
				Sprite sp = newsprites[i];
				if (osp.x != sp.x || osp.y != sp.y) {
					moved |= SPRBIT(i);
				}
			} else {
				moved |= SPRBIT(i);
			}
		}

		/* 2) Where WERE they? */
		int changeBitmap = vdpChanges.sprite | newdelbitmap;
		if (changeBitmap != 0) {
			for (i = 0; i < 768; i++) {
				if ((sprbitmap[i] & changeBitmap) != 0) /*
																			 * old
																			 * bitmap
																			 */
				{
					// logger(_L|L_3,
					// _("redrawing char %d due to deleted sprite\n"), i);
					vdpChanges.screen[i] = VdpChanges.SC_SPRITE_DELETED;
				}
			}
		}
		
		/* Now, create new sprite bitmap. */
		Arrays.fill(sprbitmap, 0, sprbitmap.length, 0);
		Arrays.fill(sprlines, 0, sprlines.length, five_sprites_on_a_line ? 0
				: -1);
		Arrays.fill(sprlinecnt, 0, sprlinecnt.length, (byte) 0);

		five_sprites = -1;

		int val = vdpregs[1] & (InternalVdp.R1_SPRMAG + InternalVdp.R1_SPR4);
		int sprwidth = val == (InternalVdp.R1_SPRMAG | InternalVdp.R1_SPR4) ? 32
				: val != 0 ? 16 : 8;

		for (i = 0; i < 32; i++) {
			Sprite sp = newsprites[i];

			int px, py; // pixel coordinate
			int bx, by; // block coordinate
			int dx, dy; // delta for sprite
			int pyl;

			py = sp.getY();
			by = (py >> 3);
			dy = (py & 7) + sprwidth;

			/*
			 * count sprites on each line and keep track of which of the first
			 * four we can draw
			 */

			pyl = (py + sprwidth) & 255;
			while (py != pyl) {
				sprlinecnt[py]++;
				if (sprlinecnt[py] > 4) {
					/* record fifth sprite */
					if (five_sprites == -1)
						five_sprites = i;
				} else {
					/* set bit so this sprite will be drawn */
					sprlines[py] |= SPRBIT(i);
				}
				py = (py + 1) & 255;
			}

			while (dy > 0) {

				px = sp.getX() & 0xff;
				bx = (px >> 3);
				dx = (px & 7) + sprwidth;

				while (dx > 0) {
					/* very rough check for sprite coincidence: FIXME !!! */
					if (sprbitmap[(by << 5) + bx] != 0) {
						vdpStatus |= InternalVdp.VDP_COINC;
					}

					sprbitmap[(by << 5) + bx] |= SPRBIT(i);

					bx = (bx + 1) & 31;
					dx -= 8;
				}

				by = (by + 1) & 31;
				dy -= 8;
			}
		}

		if (five_sprites != -1) {
			vdpStatus = (byte) (vdpStatus
					& ~(InternalVdp.VDP_FIVE_SPRITES | InternalVdp.VDP_FIFTH_SPRITE) | (InternalVdp.VDP_FIVE_SPRITES | i));

			// vdp_changes.sprite |= -1;
			/*
			 * if (log_level(LOG_SPRITES) >= 3) { logger(_L|L_3,
			 * _("Five sprites on lines:\n")); for (i=0; i<256; i++) { if
			 * (sprlinecnt[i] > 4) { u32 b, n; logger(_L|L_3, "%d: ", i); b = 1;
			 * n = 0; while (b) { if (sprlines[i] & b) { logger(_L|L_3, "%d,",
			 * n); } n++; b <<= 1; } logger(_L|L_3, "\n"); } } }
			 */
		} else {
			vdpStatus &= ~(InternalVdp.VDP_FIVE_SPRITES | InternalVdp.VDP_FIFTH_SPRITE);
		}

		/* Where chars change, force sprite update */
		for (i = 0; i < 768; i++) {
			if (vdpChanges.screen[i] != 0) {
				vdpChanges.sprite |= sprbitmap[i];
			}
		}

		/*
		 * Now, set all the sprites below/above a changed one
		 * 
		 * Do this by walking sprbitmap. If sprbitmap&vdp_changes.sprite, this
		 * means this char is affected by a change, so we need to redraw all
		 * other sprites on that char, so vdp_changes.sprite|sprbitmap.
		 */

		/*
		 * for (i=0; i<768; i++) {
		 * 
		 * if (sprbitmap[i]&vdp_changes.sprite)
		 * vdp_changes.sprite|=sprbitmap[i]; }
		 * 
		 * This is not the correct solution. Imagine a diagonal line of sprites
		 * from the top left to the screen to the bottom right, in increasing
		 * sprite number. When the bottommost one changes, we'll get the next
		 * one above it with this algorithm, but not the one on top ofthat one,
		 * etc. The "solution" to recursively redraw every sprite on up the line
		 * would lead to terrible performance problems in these cases.
		 * 
		 * What we need to do is keep track of sprites that are redrawn in the
		 * vicinity of a real changed sprite, and avoid setting their bits in
		 * vdp_changes.screen[]. That's what nearchanges is.
		 */

		nearchanges = 0;
		if (!five_sprites_on_a_line) {
			for (i = 0; i < 768; i++) {
				if ((sprbitmap[i] & vdpChanges.sprite) != 0)
					nearchanges |= sprbitmap[i];
			}
		} else {
			for (i = 0; i < 32; i++) {
				if ((sprlines[i] & vdpChanges.sprite) != 0) {
					nearchanges |= sprlines[i];
				}
			}
		}

		/* Don't draw deleted sprites */
		vdpChanges.sprite &= ~newdelbitmap;

		/* Now, figure which sprites should be drawn */
		toredraw = 0;
		changeBitmap = (vdpChanges.sprite | nearchanges);
		if (changeBitmap != 0) {
			for (i = 0; i < newdelptr; i++) {
				Sprite sp = newsprites[i];
				if ((sp.color & 15) != 0 && (changeBitmap & SPRBIT(i)) != 0) {
					/* CHECK BLANK SPRITE PATTERNS */
					toredraw |= SPRBIT(i);
				}
			}
		}
		
		/*
		 * Mark all the screen positions that need to be updated
		 * 
		 * If sprbitmap&vdp_changes.sprite, then that space needs update.
		 * 
		 * NOT toredraw here -- clear sprites aren't drawn but the spaces they
		 * occupy may have changed (if they turn clear -- as in carwars)
		 */

		// logger(_L | L_1, _("Toredraw: %08X  spritechanges: %08X\n"),
		// toredraw, vdpChanges.sprite);
		changeBitmap = vdpChanges.sprite | newdelbitmap;
		//System.out.print("sprite changes: ");
		if (changeBitmap != 0) {
			for (i = 0; i < 768; i++) {
				if ((sprbitmap[i] & changeBitmap) != 0) {
					// logger(_L|L_3,
					// _("redrawing char %d due to moved/deleted sprite\n"), i);
					//System.out.print(i+",");
					vdpChanges.screen[i] = VdpChanges.SC_SPRITE_COVERING;
				}
			}
		}
		//System.out.println();
		
		/*
		 * if (log_level(LOG_VIDEO) > 3) { //logger(_L,
		 * _("screenchanges:\n\t")); for (i = 0; i < 768; i++) { logger(_L,
		 * "%1d ", vdpChanges.screen[i]); if ((i & 31) == 31) logger(_L,
		 * "\n\t"); } }
		 */

		vdpChanges.sprite = 0;
		Arrays.fill(vdpChanges.sprpat, 0, vdpChanges.sprpat.length, (byte) 0);
		oldsprites = newsprites;
		olddelptr = newdelptr;

		return vdpStatus;
	}

	/**
	 * Extract sprites from VDP memory
	 * 
	 * @return
	 */
	private Sprite[] readSprites() {
		Sprite[] sprites = new Sprite[32];
		int sprbase = vdpModeInfo.sprite.base;
		ByteMemoryAccess access = vdpMemory.getByteReadMemoryAccess(sprbase);
		for (int i = 0; i < 32; i++) {
			Sprite sprite = sprites[i] = new Sprite();
			sprite.y = access.memory[access.offset++] & 0xff;
			sprite.x = access.memory[access.offset++] & 0xff;
			sprite.ch = access.memory[access.offset++] & 0xff;
			sprite.color = access.memory[access.offset++] & 0xff;
		}
		return sprites;
	}

	/**
	 * Render a sprite to the VDP canvas.
	 * 
	 * @author ejs
	 * 
	 */
	public interface ISpriteRenderer {
		void draw(Sprite sprite, int number);
	}

	private final int SPRPAT_PTR(int x) {
		return vdpModeInfo.sprpat.base + ((x) << 3);
	}

	private final ISpriteRenderer drawsprite8x8 = new ISpriteRenderer() {

		public void draw(Sprite sprite, int number) {
			int x, y;

			x = sprite.x;
			y = sprite.getY();
			int sprpat = SPRPAT_PTR(sprite.ch & 0xff);
			byte color = (byte) (sprite.color & 0xf);
			int shift = ((sprite.color & 0x80) != 0 ? -32 : 0);

			drawUnmagnifiedSpriteChar(number, x, y, shift, 
					vdpMemory.getByteReadMemoryAccess(sprpat), color);
		}

	};

	private final ISpriteRenderer drawsprite16x16 = new ISpriteRenderer() {

		public void draw(Sprite sprite, int number) {
			int x, y;

			x = sprite.x;
			y = sprite.getY();
			int sprpat = SPRPAT_PTR(sprite.ch & 0xff);
			byte color = (byte) (sprite.color & 0xf);
			int shift = ((sprite.color & 0x80) != 0 ? -32 : 0);
			
			drawUnmagnifiedSpriteChar(number, x, y, shift, vdpMemory
					.getByteReadMemoryAccess(sprpat), color);
			drawUnmagnifiedSpriteChar(number, x, y + 8, shift, vdpMemory
					.getByteReadMemoryAccess(sprpat + 8), color);
			drawUnmagnifiedSpriteChar(number, x + 8, y, shift, vdpMemory
					.getByteReadMemoryAccess(sprpat + 16), color);
			drawUnmagnifiedSpriteChar(number, x + 8, y + 8, shift, vdpMemory
					.getByteReadMemoryAccess(sprpat + 24), color);
		}

	};

	private final ISpriteRenderer drawspritemag8x8 = new ISpriteRenderer() {

		public void draw(Sprite sprite, int number) {
			int x, y;

			x = sprite.x;
			y = sprite.getY();
			int sprpat = SPRPAT_PTR(sprite.ch & 0xff);
			byte color = (byte) (sprite.color & 0xf);
			int shift = ((sprite.color & 0x80) != 0 ? -32 : 0);

			drawMagnifiedSpriteChar(number, x, y, shift, vdpMemory
					.getByteReadMemoryAccess(sprpat), color);
		}

	};

	private final ISpriteRenderer drawspritemag16x16 = new ISpriteRenderer() {

		public void draw(Sprite sprite, int number) {
			int x, y;

			x = sprite.x;
			y = sprite.getY();
			int sprpat = SPRPAT_PTR(sprite.ch & 0xff);
			byte color = (byte) (sprite.color & 0xf);
			int shift = ((sprite.color & 0x80) != 0 ? -32 : 0);
			drawMagnifiedSpriteChar(number, x, y, shift, vdpMemory
					.getByteReadMemoryAccess(sprpat), color);
			drawMagnifiedSpriteChar(number, x, y + 16, shift, vdpMemory
					.getByteReadMemoryAccess(sprpat + 8), color);
			drawMagnifiedSpriteChar(number, x + 16, y, shift, vdpMemory
					.getByteReadMemoryAccess(sprpat + 16), color);
			drawMagnifiedSpriteChar(number, x + 16, y + 16, shift, vdpMemory
					.getByteReadMemoryAccess(sprpat + 24), color);
		}

	};

	public void updateCanvas() {
		int i;
		ISpriteRenderer renderer = null;

		switch (vdpregs[1] & (InternalVdp.R1_SPRMAG + InternalVdp.R1_SPR4)) {
		case 0:
			renderer = drawsprite8x8;
			break;
		case InternalVdp.R1_SPRMAG:
			renderer = drawspritemag8x8;
			break;
		case InternalVdp.R1_SPR4:
			renderer = drawsprite16x16;
			break;
		case InternalVdp.R1_SPR4 + InternalVdp.R1_SPRMAG:
			renderer = drawspritemag16x16;
		}

		for (i = 31; i >= 0; i--) {
			if ((toredraw & SPRBIT(i)) != 0) {
				Sprite sp = newsprites[i];
				/*
				 * System.out.println("Drawing sprite " + i + " to " + sp.x +
				 * "," + sp.y + " ch=" + Integer.toHexString(sp.ch) + " color="
				 * + Integer.toHexString(sp.color));
				 */
				renderer.draw(sp, i);
			}
		}
	}

	protected void drawUnmagnifiedSpriteChar(int num, int x, int y, int shift,
			ByteMemoryAccess pattern, byte color) {
		int mask;
		int xx, yy;
		
		int pixelStride = vdpCanvas.getPixelStride();
		for (yy = 0; yy < 8; yy++) {
			if (y >= vdpCanvas.getHeight())
				continue;
			int block = vdpCanvas.getBitmapOffset(x + shift, y);
			if ((sprlines[y & 255] & (1 << num)) != 0) {
				byte patt = pattern.memory[pattern.offset + yy];
				if (patt != 0) {
					mask = 0x80;
					for (xx = 0; xx < 8; xx++) {
						if ((patt & mask) != 0) {
							if (x + xx < 256) {
								vdpCanvas.setColorAtOffset(block, color);
							}
						}
						mask >>= 1;
						block += pixelStride;
					}
				}
			}
			y = (y + 1) & 0xff;
			block = vdpCanvas.getBitmapOffset(x + shift, y);
		}
	}

	protected void drawMagnifiedSpriteChar(int num, int x, int y, int shift,
			ByteMemoryAccess pattern, byte color) {
		int mask;
		int xx, yy;
		
		int pixelStride = vdpCanvas.getPixelStride();
		for (yy = 0; yy < 16; yy++) {
			if (y >= vdpCanvas.getHeight())
				continue;
			int block = vdpCanvas.getBitmapOffset(x + shift, y);
			if ((sprlines[y & 255] & (1 << num)) != 0) {
				byte patt = pattern.memory[pattern.offset + yy / 2];
				if (patt != 0) {
					mask = 0x80;
					for (xx = 0; xx < 16; xx++) {
						if ((patt & mask) != 0) {
							if (x + xx < 256) {
								vdpCanvas.setColorAtOffset(block, color);
							}
						}
						block += pixelStride;
						if ((xx & 1) != 0)
							mask >>= 1;
					}
				}
			}
			y = (y + 1) & 0xff;
		}
	}

}
