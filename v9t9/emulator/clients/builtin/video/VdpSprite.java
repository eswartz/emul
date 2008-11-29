/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import v9t9.engine.memory.ByteMemoryAccess;

public class VdpSprite extends SpriteBase {
	private byte color;
	private ByteMemoryAccess pattern;
	private int numchars;
	
	private int sprrowbitmap;
	private ByteMemoryAccess colorStripe;
	
	public VdpSprite() {
	}
	
	public byte getColor() {
		return color;
	}
	public void setColor(int color) {
		if (color != this.color || colorStripe != null) {
			this.color = (byte) color;
			colorStripe = null;
			setBitmapDirty(true);
		}
	}
	
	public void setColorStripe(ByteMemoryAccess colorStripe) {
		this.colorStripe = colorStripe;
	}
	public ByteMemoryAccess getColorStripe() {
		return colorStripe;
	}

	public int getNumchars() {
		return numchars;
	}
	public void setNumchars(int numchars) {
		if (numchars != this.numchars) {
			this.numchars = numchars;
			setBitmapDirty(true);
		}
	}
	public ByteMemoryAccess getPattern() {
		return pattern;
	}
	
	public void setPattern(ByteMemoryAccess pattern) {
		if (this.pattern == null || !this.pattern.equals(pattern)) {
			this.pattern = pattern;
			setBitmapDirty(true);
		}
	}

	/** 
	 * Update the sprite bitmap to show where this sprite, in
	 * its current state, will be reflected.
	 * @param bitmap
	 */
	public void markSpriteCoverage(int[] bitmap, int mask) {
		// set the 8x8 blocks touched by the sprite
		if (!deleted) {
			
			// for sprite mode 2, shift and sizeX are adjusted 
			int yrows = sizeY + ((y & 7) != 0 ? 1 : 0);
			int xcols = (sizeX + (((x + shift) & 7) != 0 ? 1 : 0));
			
			for (int oy = 0; oy < yrows; oy += 8) {
				int bmrowoffs = (((oy+y) & 0xff)/8) * 32;
				if (bmrowoffs < bitmap.length) {
					for (int ox = 0; ox < xcols; ox += 8) {
						int bmcol = ((ox+x+shift) & 0xff) /8;
						bitmap[bmrowoffs + bmcol] |= mask;
					}
				}
			}
		}
	}
	
	/** 
	 * Update the counts of sprites per row in order to determine
	 * the bitmap of sprite rows which are visible. 
	 * @param rowcount number of sprites per screen row, updated
	 * @param maxPerLine maximum number allowed per line
	 * @return true if the sprite is partially invisible due to being the maximal sprite
	 */
	public boolean updateSpriteRowBitmap(int[] rowcount, int maxPerLine) {
		sprrowbitmap = 0;
		
		boolean isMaximal = false;
		if (!deleted) {
			for (int offs = 0; offs < sizeY; offs++) {
				int y = (this.y + offs) & 0xff;
				if (y < rowcount.length && rowcount[y] < maxPerLine) {
					rowcount[y]++;
					sprrowbitmap |= 1 << offs;
				} else {
					sprrowbitmap &= ~(1 << offs);
					isMaximal = true;
				}
			}
		}
		return isMaximal;
	}

	/** Update "last" values with current values. */
	public void finishDraw() {
		setBitmapDirty(false);
	}
	
	public int getSprrowbitmap() {
		return sprrowbitmap;
	}


}