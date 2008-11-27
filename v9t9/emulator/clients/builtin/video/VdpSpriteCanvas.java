/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import java.util.Arrays;


public class VdpSpriteCanvas {
	protected static final int NUMSPRITES = 32;
	/** map, per screen block, of which sprites are here */
	protected int[] oldspritebitmap;
	/** map, per screen block, of which sprites are here */
	protected int[] spritebitmap;
	protected VdpSprite[] sprites;
	protected int[] rowcount;
	protected final int maxPerLine;
	protected int knowndirty;

	public VdpSpriteCanvas(VdpCanvas vdpCanvas, int maxPerLine) {
		this.maxPerLine = maxPerLine;
		this.oldspritebitmap = new int[vdpCanvas.getBlockCount()];
		this.spritebitmap = new int[vdpCanvas.getBlockCount()];
		this.sprites = new VdpSprite[NUMSPRITES];
		for (int n = 0; n < NUMSPRITES; n++) {
			sprites[n] = new VdpSprite();
		}
		this.rowcount = new int[vdpCanvas.getHeight()];
		this.knowndirty = 0;
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
	 * @return maximal sprite, if detected
	 */
	public int updateSpriteCoverage(VdpCanvas screenCanvas,  byte[] screenChanges) {
		int maximal = -1;
		
		if (screenChanges.length < spritebitmap.length)
			throw new IllegalArgumentException();

		// see which sprites we already know are dirty
		knowndirty = 0;
		for (int n = 0; n < NUMSPRITES; n++) {
			if (sprites[n].isBitmapDirty()) {
				knowndirty |= (1 << n);
			}
		}
		
		updateSpriteBitmapForScreenChanges(screenCanvas, screenChanges);
		
		maximal = getSpriteBitmapChanges(maximal);
		
		updateScreenBitmapForSpriteChanges(screenCanvas, screenChanges);
		
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
	protected void updateSpriteBitmapForScreenChanges(VdpCanvas screenCanvas,
			byte[] screenChanges) {
		int blockStride = screenCanvas.getWidth() / 8;
		int blockMag = blockStride / 32;
		int blockCount = 32 * screenCanvas.getHeight() / 8;
		int screenOffs = 0;
		
		for (int i = 0; i < blockCount; i += 32) {
			for (int j = 0; j < 32; j++) {
				boolean screenChanged = screenChanges[screenOffs + j * blockMag] != 0;
				if (blockMag > 1)
					screenChanged |= screenChanges[screenOffs + j * blockMag + 1] != 0;
				if (screenChanged) {
					int oldsprites = oldspritebitmap[i + j] & ~knowndirty;
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
	 * Find where dirty sprites (or redrawings of those sprites) will make changes 
	 * to the sprite bitmap
	 * 
	 * @param maximal
	 * @return maximal sprite, if detected
	 */
	protected int getSpriteBitmapChanges(int maximal) {
		
		Arrays.fill(spritebitmap, 0, spritebitmap.length, 0);
		Arrays.fill(rowcount, 0, rowcount.length, 0);
		
		for (int n = 0; n < sprites.length; n++) {
			VdpSprite sprite = sprites[n];
			sprite.markSpriteDeltaCoverage(spritebitmap, 1 << n);
			if (sprite.updateSpriteRowBitmap(rowcount, maxPerLine)) {
				if (maximal == -1) {
					maximal = n;
				}
			}
		}
		
		// Now update otherwise unchanged sprites intersecting changed ones,
		// because redrawing a sprite will require that other sprites be redrawn.
		// This could cascade and redraw all the sprites if they each have pixels in common.
		/*
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
								knowndirty |= 1 << n;	// don't set affectedSprites since we'll miss the chance to mark them dirty
								changed = true;
							}
						}
					}
				}
			}
		} while (changed);
		*/
		
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
		if (changed) {
			for (int n = 0; n < NUMSPRITES; n++) {
				if ((knowndirty & (1 << n)) != 0) {
					sprites[n].setBitmapDirty(true);
				}
			}
		}
		
		return maximal;
	}

	/**
	 * Update screen blocks for sprites that will be redrawn
	 * @param screenCanvas the screen's canvas, used for resolution matching
	 * @param screenChanges
	 */
	protected void updateScreenBitmapForSpriteChanges(VdpCanvas screenCanvas,
			byte[] screenChanges) {
		int blockStride = screenCanvas.getWidth() / 8;
		int blockMag = blockStride / 32;
		int blockCount = 32 * screenCanvas.getHeight() / 8;
		int screenOffs = 0;
		for (int i = 0; i < blockCount; i += 32) {
			for (int j = 0; j < 32; j++) {
				if (((spritebitmap[i + j] | oldspritebitmap[i + j]) & knowndirty) != 0) {
					screenChanges[screenOffs + j * blockMag] = 1;
					if (blockMag != 1)
						screenChanges[screenOffs + j * blockMag + 1] = 1;
				}
			}
			screenOffs += blockStride;
		}
	}
	
	/**
	 * Draw sprites, after any modified screen blocks have been restored.
	 * @param canvas the canvas to modify
	 */
	public void drawSprites(VdpCanvas canvas) {
		for (int n = sprites.length; --n >= 0; ) {
			VdpSprite sprite = sprites[n];
			if (sprite.isBitmapDirty()) {
				sprite.draw(canvas);
			}
			sprite.finishDraw();
		}
	}
}