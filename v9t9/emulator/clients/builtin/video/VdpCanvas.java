/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import java.util.Arrays;

import v9t9.engine.memory.ByteMemoryAccess;

/**
 * This class holds the low-level bitmap containing the image
 * of the video screen.  This bitmap is used to update the actual
 * visible canvas on the client.
 * @author ejs
 *
 */
public class VdpCanvas {
    final int UPDATEBLOCK_ROW_STRIDE = (256+64);
	byte[] bitmap = new byte[UPDATEBLOCK_ROW_STRIDE * 256];
	private int width;
	private int height;
	private int clearColor;
    final int UPDPTR(int y,int x) { return ((y)*UPDATEBLOCK_ROW_STRIDE)+(x)+32; }


    // Palette of standard TI colors.
    // Each one is RGB, in that order, from 0 to 255.
    // Color 0, which is clear, and color 17, which is
    // the foreground in text mode, may be ignored,
    // but beware that these will be generated in updarea.

    final int RGB_8_TO_16(int x) { return	(((x)<<8) + ((((x)&1) != 0 ? 0xff : 0))); }
    final int RGB_8_TO_6(int x) { return ((x) >> 2); }

    public VdpCanvas() {
    	setSize(256, 192);
    }


	public void setSize(int x, int y) {
		this.width = x;
		this.height = y;
	}

	/**
	 * Set the real color that the "clear" color has
	 * @param c 1-15 for a real color or 0 for transparent
	 */
	public void setClearColor(int c) {
		this.clearColor = c;
	}

	public int getWidth() {
		return width;
	}


	public int getHeight() {
		return height;
	}


	public void clear() {
		Arrays.fill(bitmap, 0, bitmap.length, (byte) clearColor);
	}


	/**
	 * Blit an 8x8 block defined by a pattern and a foreground/background color to the bitmap
	 * @param r
	 * @param c
	 * @param pattern
	 * @param fg
	 * @param bg
	 */
	public void draw8x8TwoColorBlock(int r, int c, ByteMemoryAccess pattern, byte fg,
			byte bg) {
		
		int offs = UPDPTR(r, c);

		for (int i = 0; i < 8; i++) {
			drawEightPixels(offs, pattern.memory[pattern.offset + i], fg, bg);
			offs += UPDATEBLOCK_ROW_STRIDE;
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
	public void draw8x6TwoColorBlock(int r, int c, ByteMemoryAccess pattern, byte fg,
			byte bg) {
		
		int offs = UPDPTR(r,c);

		for (int i = 0; i < 8; i++) {
			drawSixPixels(offs, pattern.memory[pattern.offset + i], fg, bg);
			offs += UPDATEBLOCK_ROW_STRIDE;
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
		int offs = UPDPTR(r,c);
		
		for (int i = 0; i < 8; i++) {
			byte color = colors.memory[colors.offset + i];
			byte bg = (byte) (color & 0xf);
			byte fg = (byte) ((color >> 4) & 0xf);
			drawEightPixels(offs, pattern.memory[pattern.offset + i], fg, bg);
			offs += UPDATEBLOCK_ROW_STRIDE;
		}
	}


	private void drawEightPixels(int offs, byte pattern, byte fg, byte bg) {
		int mask = 0x80;
		while (mask != 0) {
			bitmap[offs++] = ((pattern & mask) != 0) ? fg : bg;
			mask >>= 1;
		}
	}
	
	private void drawSixPixels(int offs, byte pattern, byte fg, byte bg) {
		int mask = 0x80;
		while (mask != 0x2) {
			bitmap[offs++] = ((pattern & mask) != 0) ? fg : bg;
			mask >>= 1;
		}
	}

	byte vdp_palette[][] = { { 0x00, 0x00, 0x00 }, { 0x00, 0x00, 0x00 },
			{ 0x40, (byte) 0xb0, 0x40 }, { 0x60, (byte) 0xc0, 0x60 },
			{ 0x40, 0x40, (byte) 0xc0 }, { 0x60, 0x60, (byte) 0xf0 },
			{ (byte) 0xc0, 0x40, 0x40 }, { 0x40, (byte) 0xf0, (byte) 0xf0 },
			{ (byte) 0xf0, 0x40, 0x40 }, { (byte) 0xff, (byte) 0x80, 0x60 },
			{ (byte) 0xf0, (byte) 0xc0, 0x40 },
			{ (byte) 0xff, (byte) 0xe0, 0x60 }, { 0x40, (byte) 0x80, 0x40 },
			{ (byte) 0xc0, 0x40, (byte) 0xc0 },
			{ (byte) 0xd0, (byte) 0xd0, (byte) 0xd0 },
			{ (byte) 0xff, (byte) 0xff, (byte) 0xff }, { 0x00, 0x00, 0x00 } };

	/**
	 * Export the bitmap to RGB24 format 
	 * @param r
	 * @param c
	 * @param rgb24 a row of #getWidth() RGB pixels which will be filled
	 * @param width
	 */
	public void readBitmapRGB24(int r, int c, byte[] rgb24, int idx, int width) {
		int offs = UPDPTR(r, c);
		for (int column = 0; column < width; column++) {
			int cl = bitmap[offs++];
			if (cl == 0) 
				cl = clearColor;
			rgb24[idx++] = vdp_palette[cl][0];
			rgb24[idx++] = vdp_palette[cl][1];
			rgb24[idx++] = vdp_palette[cl][2];
		}
	}
	
	/**
	 * Export the bitmap to ARGB32 format 
	 * @param r
	 * @param argb32 a row of #getWidth() ARGB pixels which will be filled
	 */
	public void readBitmapARGB32(int r, int c, byte[] argb32, int idx, int width) {
		int offs = UPDPTR(r, c);
		for (int column = 0; column < width; column++) {
			int cl = bitmap[offs++];
			byte alpha = -1;
			if (cl == 0) {
				cl = clearColor;
				if (cl == 0)
					alpha = 0;
			}
			argb32[idx++] = alpha;
			argb32[idx++] = vdp_palette[cl][0];
			argb32[idx++] = vdp_palette[cl][1];
			argb32[idx++] = vdp_palette[cl][2];
		}
	}


	/** Get the RGB triple for the palette entry. */
	public byte[] getColorRGB(int idx) {
		if (idx == 0)
			idx = clearColor;
		return vdp_palette[idx];
	}


	public void drawUnmagnifiedSpriteChar(int x, int y,
			ByteMemoryAccess byteReadMemoryAccess, byte fg) {
		
	}


	public void drawMagnifiedSpriteChar(int x, int y,
			ByteMemoryAccess byteReadMemoryAccess, byte fg) {
		
	}


	public int getBitmapOffset(int x, int y) {
		return UPDPTR(y, x);
	}


	public void setColorAtOffset(int offset, byte color) {
		bitmap[offset] = color;
	}	
}
