/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import v9t9.engine.memory.ByteMemoryAccess;

public class VdpSprite extends SpriteBase {
	private byte color;
	private ByteMemoryAccess pattern;
	private int numchars;
	
	protected int lasty;
	protected int lastx;
	protected int lastshift;
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
			if (colorStripe == null) {
				int yrows = size + ((y & 7) != 0 ? 1 : 0);
				int xcols = (size + (((x + shift) & 7) != 0 ? 1 : 0));
				
				for (int oy = 0; oy < yrows; oy += 8) {
					int bmrowoffs = (((oy+y) & 0xff)/8) * 32;
					if (bmrowoffs < bitmap.length) {
						for (int ox = 0; ox < xcols; ox += 8) {
							int bmcol = ((ox+x+shift) & 0xff) /8;
							bitmap[bmrowoffs + bmcol] |= mask;
						}
					}
				}
			} else {
				// shift can be set per line!
				int yrows = size + ((y & 7) != 0 ? 1 : 0);
				ByteMemoryAccess access = new ByteMemoryAccess(colorStripe);
				boolean ismag = !((numchars == 1 && size == 8) || (numchars == 4 && size == 16));
				for (int oy = 0; oy < yrows; oy ++) {
					// TODO: optimize to check only whether shift changes
					int shift = (access.memory[access.offset] & 0x80) != 0 ? -32 : 0;
					if (!ismag)
						access.offset++;
					else if (oy % 2 == 1)
						access.offset++;
						
					int xcols = (size + (((x + shift) & 7) != 0 ? 1 : 0));
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
			for (int offs = 0; offs < size; offs++) {
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