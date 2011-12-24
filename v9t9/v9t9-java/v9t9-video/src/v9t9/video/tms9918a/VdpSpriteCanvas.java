/**
 * 
 */
package v9t9.video.tms9918a;

import java.util.Arrays;
import java.util.BitSet;

import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.video.ICanvas;
import v9t9.common.video.ISpriteCanvas;
import v9t9.common.video.ISpriteDrawingCanvas;
import v9t9.common.video.IVdpCanvas;
import v9t9.common.video.SpriteBase;
import v9t9.common.video.VdpSprite;


public class VdpSpriteCanvas implements ISpriteCanvas {
	protected static final int NUMSPRITES = 32;
	/** map, per screen block, of which sprites are here */
	protected int[] oldspritebitmap;
	/** map, per screen block, of which sprites are here */
	protected int[] spritebitmap;
	protected VdpSprite[] sprites;
	/** map of sprites visible on the row */
	protected int[] rowcount;
	protected final int maxPerLine;
	protected int knowndirty;
	/** map of which rows in a sprite are to be drawn */
	protected int[] sprrowbitmaps;
	private int numSpriteChars;
	private boolean isMagnified;


	public VdpSpriteCanvas(IVdpCanvas vdpCanvas, int maxPerLine) {
		this.maxPerLine = maxPerLine;
		this.oldspritebitmap = new int[vdpCanvas.getBlockCount()];
		this.spritebitmap = new int[vdpCanvas.getBlockCount()];
		this.sprites = new VdpSprite[NUMSPRITES];
		for (int n = 0; n < NUMSPRITES; n++) {
			sprites[n] = new VdpSprite(n);
		}
		this.rowcount = new int[vdpCanvas.getHeight()];
		this.knowndirty = 0;
		this.sprrowbitmaps = new int[NUMSPRITES];
	}
	
	public VdpSprite[] getSprites() {
		return sprites;
	}
	
	/** 
	 * After externally updating the sprites to correspond to the current
	 * layout (update position, color, shift, pattern, deleted), call this
	 * to mark where in the bitmap each sprite's change affects the screen
	 * (reflecting the previous position/size/etc. and the new position/size/etc).
	 * @param screenCanvas the screen's canvas, used for resolution matching
	 * @param screenChanges the real screen's block bitmap
	 * @param forceRedraw if true, force full redraw
	 * @return maximal sprite, if detected
	 */
	public int updateSpriteCoverage(ICanvas screenCanvas,  BitSet screenChanges, boolean forceRedraw) {
		int maximal = -1;
		
		if (forceRedraw) {
			for (VdpSprite sprite : getSprites()) {
				sprite.setBitmapDirty(true);
			}
		}
		
		// see which sprites we already know are dirty, since their attributes just changed
		knowndirty = 0;
		for (int n = 0; n < NUMSPRITES; n++) {
			if (sprites[n].isBitmapDirty()) {
				knowndirty |= (1 << n);
			}
		}
		
		// see where the screen changed and forces new sprite redraws
		updateSpriteBitmapForScreenChanges(screenCanvas, screenChanges, oldspritebitmap);
		
		// see where the current sprites mark the screen
		maximal = getSpriteCoverage();
		
		// see where sprite drawings trigger other sprite redraws
		getCascadingSpriteChanges();
		
		// mark changed positions in screen so it will be redrawn before sprites
		updateScreenBitmapForSpriteChanges(screenCanvas, screenChanges);

		updateSpriteBitmapForScreenChanges(screenCanvas, screenChanges, spritebitmap);
		
		int[] tmp = oldspritebitmap;
		oldspritebitmap = spritebitmap;
		spritebitmap = tmp;
		
		return maximal;
	}

	/**
	 * Update any sprites made dirty by pending screen changes; this may
	 * seem redundant if we dirty a block but also moved the sprite off that
	 * block, but this won't dirty more than necessary
	 * @param screenCanvas the screen's canvas, used for resolution matching
	 */
	protected void updateSpriteBitmapForScreenChanges(ICanvas screenCanvas,
			BitSet screenChanges,
			int[] bitmap) {
		int blockStride = screenCanvas.getVisibleWidth() / 8;
		// 512-wide modes draw double-width sprites
		int blockMag = blockStride / 32;
		int blockCount = 32 * screenCanvas.getHeight() / 8;
		int screenOffs = 0;
		
		for (int i = 0; i < blockCount; i += 32) {
			for (int j = 0; j < 32; j++) {
				boolean screenChanged = screenChanges.get(screenOffs + j * blockMag);
				if (blockMag > 1)
					screenChanged |= screenChanges.get(screenOffs + j * blockMag + 1);
				if (screenChanged) {
					int oldsprites = bitmap[i + j] & ~knowndirty;
					if (oldsprites != 0) {
						for (int n = 0; n < NUMSPRITES; n++) {
							if ((oldsprites & (1 << n)) != 0) {
								SpriteBase sprite = sprites[n];
								sprite.setBitmapDirty(true);
								knowndirty |= (1 << n);
							}
						}
					}
				}
			}
			screenOffs += blockStride;
		}
	}

	/**
	 * Get current coverage of sprites
	 * @return max # sprite on a line, or -1
	 */
	protected int getSpriteCoverage() {
		int maximal = -1;
		
		Arrays.fill(spritebitmap, 0);
		Arrays.fill(rowcount, 0);
		Arrays.fill(sprrowbitmaps, 0);
		
		for (int n = 0; n < sprites.length; n++) {
			VdpSprite sprite = sprites[n];
			
			// set the 8x8 blocks touched by the sprite
			if (!sprite.isDeleted()) {
				
				// for sprite mode 2, shift and sizeX are adjusted 
				int yrows = sprite.getSizeY() + ((sprite.getY() & 7) != 0 ? 1 : 0);
				//int xcols = (sizeX + (((x + shift) & 7) != 0 ? 8 : 0));
				int xcols = sprite.getSizeX() + ((sprite.getX() & 7) != 0 ? 1 : 0);
				
				for (int oy = 0; oy < yrows; oy += 8) {
					int bmrowoffs = (((oy+sprite.getY()) & 0xff)/8) * 32;
					if (bmrowoffs < spritebitmap.length) {
						for (int ox = 0; ox < xcols; ox += 8) {
							int bmcol = (ox+sprite.getX()+sprite.getShift()) / 8;
							if (bmcol >= 0 && bmcol < 32)
								spritebitmap[bmrowoffs + bmcol] |= 1 << n;
						}
					}
				}
				
				/*
				 * Update the counts of sprites per row in order to determine
				 * the bitmap of sprite rows which are visible. 
				 * @return true if the sprite is partially invisible due to being the maximal sprite
				 */
				int sprrowbitmap = 0;
				
				boolean isMaximal = false;
				for (int offs = 0; offs < sprite.getSizeY(); offs++) {
					int y = (sprite.getY() + offs) & 0xff;
					if (y < rowcount.length && rowcount[y] < maxPerLine) {
						rowcount[y]++;
						sprrowbitmap |= 1 << offs;
					} else {
						sprrowbitmap &= ~(1 << offs);
						isMaximal = true;
					}
				}
				sprrowbitmaps[n] = sprrowbitmap;
				
				if (isMaximal) {
					if (maximal == -1) {
						maximal = n;
					}
				}
			}
		}
		
		return maximal;
	}

	/**
	 * Find where dirty sprites (or redrawings of those sprites) will make changes 
	 * to the sprite bitmap
	 */
	protected void getCascadingSpriteChanges() {
		
		// Now update otherwise unchanged sprites intersecting changed ones,
		// because redrawing a sprite will require that other sprites be redrawn.
		// This could cascade and redraw all the sprites if they each have pixels in common.
		boolean changed;
		do {
			changed = false;
			for (int i = 0; i < spritebitmap.length; i++) {
				if (spritebitmap[i] != (oldspritebitmap[i] & ~knowndirty)) {
					int affectedSprites = spritebitmap[i] & ~knowndirty;
					if (affectedSprites != 0) {
						spritebitmap[i] |= affectedSprites;
						for (int n = 0; n < NUMSPRITES; n++) {
							if ((affectedSprites & (1 << n)) != 0) {
								sprites[n].setBitmapDirty(true);
								knowndirty |= 1 << n;	// don't update knowndirty all at once since we'll miss the chance to mark them dirty
								changed = true;
							}
						}
					}
				}
			}
		} while (changed);
		/*
		boolean changed;
		changed = false;
		for (int i = 0; i < spritebitmap.length; i++) {
			if (spritebitmap[i] != (oldspritebitmap[i] & ~knowndirty)) {
				int affectedSprites = spritebitmap[i] & ~knowndirty;
				if (affectedSprites != 0) {
					knowndirty |= affectedSprites;
					spritebitmap[i] |= affectedSprites;
					changed = true;
				}
			}
		}
		*/
	}

	/**
	 * Update screen blocks for sprites that will be redrawn
	 * @param screenCanvas the screen's canvas, used for resolution matching
	 * @param screenChanges
	 */
	protected void updateScreenBitmapForSpriteChanges(ICanvas screenCanvas,
			BitSet screenChanges) {
		int blockStride = screenCanvas.getVisibleWidth() / 8;
		// 512-wide modes draw double-width sprites
		int blockMag = blockStride / 32;
		int blockCount = 32 * screenCanvas.getHeight() / 8;
		int screenOffs = 0;
		
		int touched = 0;
		for (int i = 0; i < blockCount; i += 32) {
			for (int j = 0; j < 32; j++) {
				if (((spritebitmap[i + j] | oldspritebitmap[i + j]) & knowndirty) != 0) {
					screenChanges.set(screenOffs + j * blockMag);
					if (blockMag > 1) {
						screenChanges.set(screenOffs + j * blockMag + 1);
					} 
					touched++;
				}
			}
			screenOffs += blockStride;
		}
		//System.out.println(touched + " dirty from sprites");
	}

	/**
	 * Draw sprites, after any modified screen blocks have been restored.
	 * @param canvas the canvas to modify
	 */
	public void drawSprites(ISpriteDrawingCanvas canvas, boolean force) {
		for (int n = sprites.length; --n >= 0; ) {
			VdpSprite sprite = sprites[n];
			if ((force || sprite.isBitmapDirty()) && !sprite.isDeleted() && sprrowbitmaps[n] != 0) {
				drawSprite(canvas, sprite, sprrowbitmaps[n]);
			}
			sprite.finishDraw();
		}
	}

	/**
	 * Draws an 8x8 sprite character
	 * @param y
	 * @param x
	 * @param shift the early clock shift (usu. 0 or -32)
	 * @param rowbitmap a map of the rows which should be drawn, based on sprite priority
	 * and N-sprites-per-line calculations.  The LSB corresponds to the top row.
	 * @param pattern the sprite's pattern
	 * @param color the color for "on" bits on the sprite; will not be 0
	 */
	protected void drawUnmagnifiedSpriteChar(ISpriteDrawingCanvas canvas, int y, int x, int shift, byte color, 
			int rowbitmap, ByteMemoryAccess pattern) {
		x &= 0xff;
		if (x + shift + 8 <= 0 || x + shift >= 256)
			return;

		byte bitmask = -1;
		if (x + shift < 0) {
			bitmask &= 0xff >> -(x + shift);
		} else if (x + shift + 8 > 256) {
			//bitmask &= 0xffff << ((x + shift + 8) - 256);
		}
		
		x += shift;
		for (int yy = 0; yy < 8; yy++) {
			if (y >= canvas.getHeight())
				continue;
			if ((rowbitmap & (1 << yy)) != 0) {
				byte patt = pattern.memory[pattern.offset + yy];
				if (patt != 0) {
					canvas.drawEightSpritePixels(x, y, patt, color, bitmask, false);
				}
			}
			y = (y + 1) & 0xff;
		}
	}

	/**
	 * Draws an 16x16 sprite character from an 8x8 pattern
	 * @param y
	 * @param x
	 * @param shift the early clock shift (usu. 0 or -32)
	 * @param rowbitmap a map of the rows which should be drawn, based on sprite priority
	 * and N-sprites-per-line calculations.  The LSB corresponds to the top row.
	 * @param pattern the sprite's pattern
	 * @param color the color for "on" bits on the sprite; will not be 0
	 */
	protected void drawMagnifiedSpriteChar(ISpriteDrawingCanvas canvas, int y, int x, int shift, byte color, 
			int rowbitmap, ByteMemoryAccess pattern) {
		if (x + shift + 16 <= 0 || x + shift >= 256)
			return;

		x &= 0xff;
		short bitmask = -1;
		if (x + shift < 0) {
			bitmask &= 0xffff >> -(x + shift);
		} else if (x + shift + 16 > 256) {
			//bitmask &= 0xffff << ((x + shift + 16) - 256);
		}
		
		x += shift;
		for (int yy = 0; yy < 16; yy++) {
			if (y >= canvas.getHeight())
				continue;
			if ((rowbitmap & (1 << yy)) != 0) {
				byte patt = pattern.memory[pattern.offset + yy / 2];
				if (patt != 0) {
					canvas.drawEightMagnifiedSpritePixels(x, y, patt, color, bitmask, false);
				}
			}
			y = (y + 1) & 0xff;
		}
	}
	
	/** y,x */
	protected static final int[] charshifts = { 0, 0, 8, 0, 0, 8, 8, 8 };
	
	protected void drawSprite(ISpriteDrawingCanvas canvas, VdpSprite sprite, int sprrowbitmap) {
		// color 0 is transparent and always invisible
		if (sprite.getColor() == 0)
			return;
		
		//System.out.println("Drawing " + sprite);
		
		int x = sprite.getX();
		int y = sprite.getY();
		int shift = sprite.getShift();
		byte color = sprite.getColor();
		ByteMemoryAccess tmpPattern = new ByteMemoryAccess(sprite.getPattern());
		
		for (int c = 0; c < numSpriteChars; c++) {
			int rowshift = charshifts[c*2];
			int colshift = charshifts[c*2+1];
			if (x + colshift + shift >= 256)
				continue;
			if (!isMagnified)
				drawUnmagnifiedSpriteChar(canvas, y + rowshift, x + colshift, 
						shift, color, sprrowbitmap >> rowshift, tmpPattern);
			else
				drawMagnifiedSpriteChar(canvas, y + rowshift * 2, x + colshift * 2, 
						shift, color, sprrowbitmap >> (rowshift*2), tmpPattern);
			tmpPattern.offset += 8;
		}
	}

	public int getNumSpriteChars() {
		return numSpriteChars;
	}

	public void setNumSpriteChars(int numSpriteChars) {
		this.numSpriteChars = numSpriteChars;
	}

	public boolean isMagnified() {
		return isMagnified;
	}

	public void setMagnified(boolean isMagnified) {
		this.isMagnified = isMagnified;
	}	
}