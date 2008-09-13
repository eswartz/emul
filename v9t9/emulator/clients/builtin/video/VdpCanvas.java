/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import v9t9.engine.memory.ByteMemoryAccess;

/**
 * This class implements rendering of video contents.
 * @author ejs
 *
 */
public abstract class VdpCanvas {
	protected static final int X_PADDING = 32;


    protected int clearColor;

	public VdpCanvas() {
    	setSize(256, 192);
    }


	public abstract void setSize(int x, int y);

	/**
	 * Set the real color that the "clear" color has
	 * @param c 1-15 for a real color or 0 for transparent
	 */
	public void setClearColor(int c) {
		this.clearColor = c;
	}

	public abstract int getWidth() ;


	public abstract int getHeight();

	public abstract void clear();



	protected byte vdp_palette[][] = { { 0x00, 0x00, 0x00 }, { 0x00, 0x00, 0x00 },
			{ 0x40, (byte) 0xb0, 0x40 }, { 0x60, (byte) 0xc0, 0x60 },
			{ 0x40, 0x40, (byte) 0xc0 }, { 0x60, 0x60, (byte) 0xf0 },
			{ (byte) 0xc0, 0x40, 0x40 }, { 0x40, (byte) 0xf0, (byte) 0xf0 },
			{ (byte) 0xf0, 0x40, 0x40 }, { (byte) 0xff, (byte) 0x80, 0x60 },
			{ (byte) 0xf0, (byte) 0xc0, 0x40 },
			{ (byte) 0xff, (byte) 0xe0, 0x60 }, { 0x40, (byte) 0x80, 0x40 },
			{ (byte) 0xc0, 0x40, (byte) 0xc0 },
			{ (byte) 0xd0, (byte) 0xd0, (byte) 0xd0 },
			{ (byte) 0xff, (byte) 0xff, (byte) 0xff }, { 0x00, 0x00, 0x00 } };

	/** Get the RGB triple for the palette entry. */
	public byte[] getColorRGB(int idx) {
		if (idx == 0)
			idx = clearColor;
		return vdp_palette[idx];
	}
	
	public abstract int getBitmapOffset(int x, int y);

	public abstract void setColorAtOffset(int offset, byte color);
	
	public abstract int getPixelStride();
	
	public abstract int getLineStride();

	/**
	 * Blit an 8x8 block defined by a pattern and a foreground/background color to the bitmap
	 * @param r
	 * @param c
	 * @param pattern
	 * @param fg
	 * @param bg
	 */
	public void draw8x8TwoColorBlock(int r, int c, ByteMemoryAccess pattern,
			byte fg, byte bg) {
		int offs = getBitmapOffset(c, r);
		int bytesPerLine = getLineStride();
		for (int i = 0; i < 8; i++) {
			byte mem = pattern.memory[pattern.offset + i];
			drawEightPixels(offs, mem, fg, bg);
			offs += bytesPerLine;
		}

	}

	/**
	 * Blit an 8x6 block defined by a pattern and a foreground/background color to the bitmap
	 * @param r
	 * @param c
	 * @param pattern
	 * @param fg
	 * @param bg
	 */
	public void draw8x6TwoColorBlock(int r, int c, ByteMemoryAccess pattern,
			byte fg, byte bg) {
		int offs = getBitmapOffset(c, r);
		int bytesPerLine = getLineStride();
		for (int i = 0; i < 8; i++) {
			byte mem = pattern.memory[pattern.offset + i];
			drawSixPixels(offs, mem, fg, bg);
			offs += bytesPerLine;
		}
	}

	/**
	 * Blit an 8x8 block defined by a pattern and colors to the bitmap
	 * @param r
	 * @param c
	 * @param pattern
	 * @param colors
	 */
	public void draw8x8MultiColorBlock(int r, int c,
			ByteMemoryAccess pattern, ByteMemoryAccess colors) {
		int offs = getBitmapOffset(c, r);
		int bytesPerLine = getLineStride();
		for (int i = 0; i < 8; i++) {
			byte mem = pattern.memory[pattern.offset + i];
			byte color = colors.memory[colors.offset + i];
			byte fg = (byte) ((color >> 4) & 0xf);
			byte bg = (byte) (color & 0xf);
			drawEightPixels(offs, mem, fg, bg);
			offs += bytesPerLine;
		}
	}

	abstract protected void drawEightPixels(int offs, byte mem, byte fg, byte bg); 
	abstract protected void drawSixPixels(int offs, byte mem, byte fg, byte bg); 

	public void drawUnmagnifiedSpriteChar(int y, int x, int shift, int rowbitmap, ByteMemoryAccess pattern,
			byte color) {
		int mask;
		int xx, yy;
		
		if (x + shift + 8 <= 0)
			return;
		
		int pixelStride = getPixelStride();
		for (yy = 0; yy < 8; yy++) {
			if (y >= getHeight())
				continue;
			if ((rowbitmap & (1 << yy)) != 0) {
				byte patt = pattern.memory[pattern.offset + yy];
				if (patt != 0) {
					int block = getBitmapOffset(x + shift, y);
					mask = 0x80;
					for (xx = 0; xx < 8; xx++) {
						int xp = x + shift + xx;
						if (xp >= 0 && xp < 256) {
							if ((patt & mask) != 0) {
								setColorAtOffset(block, color);
							}
						}
						mask >>= 1;
						block += pixelStride;
					}
				}
			}
			y = (y + 1) & 0xff;
		}
	}

	public void drawMagnifiedSpriteChar(int y, int x, int shift, int rowbitmap, ByteMemoryAccess pattern,
			byte color) {
		int mask;
		int xx, yy;
		
		if (x + shift + 16 <= 0)
			return;

		int pixelStride = getPixelStride();
		for (yy = 0; yy < 16; yy++) {
			if (y >= getHeight())
				continue;
			if ((rowbitmap & (1 << yy)) != 0) {
				byte patt = pattern.memory[pattern.offset + yy / 2];
				if (patt != 0) {
					int block = getBitmapOffset(x + shift, y);
					mask = 0x80;
					for (xx = 0; xx < 16; xx++) {
						int xp = x + shift + xx;
						if (xp >= 0 && xp < 256) {
							if ((patt & mask) != 0) {
								setColorAtOffset(block, color);
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
	
	abstract protected void drawEightSpritePixels(int offs, byte mem, byte fg); 
	abstract protected void drawEightMagnifiedSpritePixels(int offs, byte mem, byte fg);
}
