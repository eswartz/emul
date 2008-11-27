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
	private boolean hasPriority = true;
	private ByteMemoryAccess colorStripe;
	private int colorStride;
	
	public VdpSprite() {
	}
	
	public byte getColor() {
		return color;
	}
	public void setColor(int color) {
		if (color != this.color || colorStripe != null) {
			this.color = (byte) color;
			colorStripe = null;
			colorStride = 0;
			setBitmapDirty(true);
		}
	}
	
	public void setColorStripe(ByteMemoryAccess colorStripe, int colorStride) {
		this.colorStripe = colorStripe;
		this.colorStride = colorStride;
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
	 * Update the screen bitmap to represent a bitmask of sprites 
	 * drawn in each block for the current and previous state of the sprite.
	 * @param bitmap
	 */
	public void markSpriteDeltaCoverage(int[] bitmap, int mask) {
		// set the 8x8 blocks touched by the sprite
		if (!deleted) {
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
				if (y < rowcount.length && (!hasPriority || rowcount[y] < maxPerLine)) {
					if (hasPriority) 
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

	@Override
	public void draw(VdpCanvas canvas) {
		if (deleted || color == 0)
			return;
		
		ByteMemoryAccess colors;
		int colorStride;
		if (colorStripe == null) {
			colors = new ByteMemoryAccess(new byte[] { color }, 0);
			colorStride = 0;
		} else {
			colors = colorStripe;
			colorStride = this.colorStride;
		}
		
		boolean doubleWidth = (canvas.getWidth() == 512);
		
		if (numchars == 1) {
			if (size == 8) {
				canvas.drawUnmagnifiedSpriteChar(y, x, shift, sprrowbitmap, pattern, colors, colorStride, doubleWidth); 
			} else if (size == 16) {
				canvas.drawMagnifiedSpriteChar(y, x, shift, sprrowbitmap, pattern, colors, colorStride, doubleWidth); 
			}
		} else if (numchars == 4) {
			ByteMemoryAccess tmpPattern = new ByteMemoryAccess(pattern);
			if (size == 16) {
				canvas.drawUnmagnifiedSpriteChar(y, x, shift,sprrowbitmap, tmpPattern, colors, colorStride, doubleWidth);
				tmpPattern.offset += 8;
				canvas.drawUnmagnifiedSpriteChar(y + 8, x, shift, sprrowbitmap >> 8, tmpPattern, colors, colorStride, doubleWidth); 
				tmpPattern.offset += 8;
				canvas.drawUnmagnifiedSpriteChar(y, x + 8, shift, sprrowbitmap, tmpPattern, colors, colorStride, doubleWidth); 
				tmpPattern.offset += 8;
				canvas.drawUnmagnifiedSpriteChar(y + 8, x + 8, shift, sprrowbitmap >> 8, tmpPattern, colors, colorStride, doubleWidth); 
			} else if (size == 32) {
				canvas.drawMagnifiedSpriteChar(y, x, shift,sprrowbitmap, tmpPattern, colors, colorStride, doubleWidth);
				tmpPattern.offset += 8;
				canvas.drawMagnifiedSpriteChar(y + 16, x, shift, sprrowbitmap >> 16, tmpPattern, colors, colorStride, doubleWidth); 
				tmpPattern.offset += 8;
				canvas.drawMagnifiedSpriteChar(y, x + 16, shift, sprrowbitmap, tmpPattern, colors, colorStride, doubleWidth); 
				tmpPattern.offset += 8;
				canvas.drawMagnifiedSpriteChar(y + 16, x + 16, shift, sprrowbitmap >> 16, tmpPattern, colors, colorStride, doubleWidth); 
			}
		}
		
	}
	
	/** Update "last" values with current values. */
	public void finishDraw() {
		setBitmapDirty(false);
	}

}