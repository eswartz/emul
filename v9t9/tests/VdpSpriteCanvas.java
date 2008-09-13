/**
 * 
 */
package v9t9.tests;

import java.util.Arrays;

import v9t9.emulator.clients.builtin.video.VdpCanvas;

public class VdpSpriteCanvas {
	/** map, per screen block, of which sprites are here */
	private int[] oldspritebitmap;
	/** map, per screen block, of which sprites are here */
	private int[] spritebitmap;
	private VdpSprite[] sprites;
	private int[] rowcount;
	private final VdpCanvas canvas;

	public VdpSpriteCanvas(VdpCanvas canvas) {
		this.canvas = canvas;
		this.oldspritebitmap = new int[(canvas.getHeight() / 8) * (canvas.getWidth() / 8)];
		this.spritebitmap = new int[(canvas.getHeight() / 8) * (canvas.getWidth() / 8)];
		this.sprites = new VdpSprite[32];
		for (int n = 0; n < 32; n++) {
			sprites[n] = new VdpSprite();
		}
		this.rowcount = new int[canvas.getHeight()];
	}
	
	public VdpSprite[] getSprites() {
		return sprites;
	}
	
	/** After externally updating the sprites to correspond to the current
	 * layout (update position, color, shift, pattern, deleted), call this
	 * to mark where in the bitmap each sprite's change affects the screen
	 * (reflecting the previous position/size/etc. and the new position/size/etc).
	 * @param vdpStatus
	 * @return fifth sprite, if detected
	 */
	public int updateSpriteCoverage(byte[] screenChanges) {
		int fifth = -1;
		
		if (screenChanges.length < spritebitmap.length)
			throw new IllegalArgumentException();

		// see which sprites we already know are dirty
		int knowndirty = 0;
		for (int n = 0; n < 32; n++) {
			if (sprites[n].isBitmapDirty()) {
				knowndirty |= (1 << n);
			}
		}

		// update any sprites made dirty by pending screen changes;
		// this may seem redundant if we dirty a block but also moved
		// the sprite off that block, but this won't dirty more than necessary
		for (int i = 0; i < oldspritebitmap.length; i++) {
			if (screenChanges[i] != 0) {
				int oldsprites = oldspritebitmap[i] & ~knowndirty;
				if (oldsprites != 0) {
					for (int n = 0; n < 32; n++) {
						if ((oldsprites & (1 << n)) != 0) {
							SpriteBase sprite = sprites[n];
							sprite.setBitmapDirty(true);
							knowndirty |= (1 << n);
						}
					}
				}
			}
		}
		
		// now, find where dirty sprites will make changes to the screen
		Arrays.fill(spritebitmap, 0, spritebitmap.length, 0);
		Arrays.fill(rowcount, 0, rowcount.length, 0);
		
		for (int n = 0; n < sprites.length; n++) {
			VdpSprite sprite = sprites[n];
			sprite.markSpriteDeltaCoverage(spritebitmap, 1 << n);
			if (sprite.updateSpriteRowBitmap(rowcount)) {
				if (fifth == -1) {
					fifth = n;
				}
			}
		}
		
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
						for (int n = 0; n < 32; n++) {
							if ((affectedSprites & (1 << n)) != 0) {
								sprites[n].setBitmapDirty(true);
								knowndirty |= 1 << n;
								changed = true;
							}
						}
					}
				}
			}
		} while (changed);
		
		// Now update screen blocks for sprites that will be redrawn,
		for (int i = 0; i < spritebitmap.length; i++) {
			if (((spritebitmap[i] | oldspritebitmap[i]) & knowndirty) != 0) {
				screenChanges[i] = 1;
			}
		}
		
		int[] tmp = oldspritebitmap;
		oldspritebitmap = spritebitmap;
		spritebitmap = tmp;
		
		return fifth;
	}
	
	/**
	 * Draw sprites, after any modified screen blocks have been restored.
	 */
	public void drawSprites() {
		for (int n = sprites.length; --n >= 0; ) {
			VdpSprite sprite = sprites[n];
			if (sprite.isBitmapDirty()) {
				sprite.draw(canvas);
			}
			sprite.finishDraw();
		}
	}
}