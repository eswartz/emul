/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import java.util.Arrays;

/**
 * This class holds the low-level bitmap containing the image
 * of the video screen.  This bitmap is used to update the actual
 * visible canvas on the client.
 * @author ejs
 *
 */
public class MemoryCanvas extends VdpCanvas {
    final int UPDATEBLOCK_ROW_STRIDE = (256+64);
	byte[] bitmap = new byte[UPDATEBLOCK_ROW_STRIDE * 256];
	private int width;
	private int height;
	private int clearColor;
    final int UPDPTR(int y,int x) { return ((y)*UPDATEBLOCK_ROW_STRIDE)+(x)+32; }

    public MemoryCanvas() {
    	setSize(256, 192);
    }


	public void setSize(int x, int y) {
		this.width = x;
		this.height = y;
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

	@Override
	public int getLineStride() {
		return UPDATEBLOCK_ROW_STRIDE;
	}

	protected void drawEightPixels(int offs, byte pattern, byte fg, byte bg) {
		int mask = 0x80;
		while (mask != 0) {
			bitmap[offs++] = ((pattern & mask) != 0) ? fg : bg;
			mask >>= 1;
		}
	}
	
	protected void drawSixPixels(int offs, byte pattern, byte fg, byte bg) {
		int mask = 0x80;
		while (mask != 0x2) {
			bitmap[offs++] = ((pattern & mask) != 0) ? fg : bg;
			mask >>= 1;
		}
	}
	
	protected void drawEightSpritePixels(int offs, byte mem, byte fg) {
		for (int i = 0; i < 8; i++) {
			if ((mem & 0x80) != 0) {
				bitmap[offs++] = fg;
			}
			mem <<= 1;
		}
	}

	protected void drawEightMagnifiedSpritePixels(int offs, byte mem, byte fg) {
		int rowOffset = getLineStride();
		for (int i = 0; i < 8; i++) {
			if ((mem & 0x80) != 0) {
				bitmap[offs + i * 2] = fg;
				bitmap[offs + i * 2 + 1] = fg;
				bitmap[offs + rowOffset + i * 2] = fg;
				bitmap[offs + rowOffset + i * 2 + 1] = fg;
			}
			mem <<= 1;
		}
	}


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


	public int getBitmapOffset(int x, int y) {
		return UPDPTR(y, x);
	}

	public int getPixelStride() {
		return 1;
	}

	public void setColorAtOffset(int offset, byte color) {
		bitmap[offset] = color;
	}	
}
