/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import java.util.Arrays;

import org.eclipse.swt.graphics.ImageData;

import v9t9.engine.memory.ByteMemoryAccess;

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
    final int UPDPTR(int y,int x) { return ((y)*UPDATEBLOCK_ROW_STRIDE)+(x)+32; }

    public MemoryCanvas() {
    	setSize(256, 192);
    }

	public void doChangeSize() {
	}

	public void clear(byte[] rgb) {
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
	
	public void drawEightSpritePixels(int offs, byte mem, byte fg, byte bitmask, boolean isLogicalOr) {
		if (isLogicalOr) {
			for (int i = 0; i < 8; i++) {
				if ((mem & 0x80) != 0) {
					bitmap[offs + i] |= fg;
				}
				mem <<= 1;
			}
		} else {
			for (int i = 0; i < 8; i++) {
				if ((mem & 0x80) != 0) {
					bitmap[offs + i] = fg;
				}
				mem <<= 1;
			}
		}
	}

	public void drawEightMagnifiedSpritePixels(int offs, byte mem, byte fg, short bitmask, boolean isLogicalOr) {
		for (int i = 0; i < 8; i++) {
			if ((mem & 0x80) != 0) {
				if (isLogicalOr) {
					bitmap[offs + i * 2] |= fg;
					bitmap[offs + i * 2 + 1] |= fg;
				} else {
					bitmap[offs + i * 2] = fg;
					bitmap[offs + i * 2 + 1] = fg;
				}
			}
			mem <<= 1;
		}
	}
	public void drawEightDoubleMagnifiedSpritePixels(int offs, byte mem, byte fg, short bitmask, boolean isLogicalOr) {
		for (int i = 0; i < 8; i++) {
			if ((mem & 0x80) != 0) {
				if (isLogicalOr) {
					bitmap[offs + i * 4] |= fg;
					bitmap[offs + i * 4 + 1] |= fg;
					bitmap[offs + i * 4 + 2] |= fg;
					bitmap[offs + i * 4 + 3] |= fg;
				} else {
					bitmap[offs + i * 4] = fg;
					bitmap[offs + i * 4 + 1] = fg;
					bitmap[offs + i * 4 + 2] = fg;
					bitmap[offs + i * 4 + 3] = fg;
				}
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
			rgb24[idx++] = colorPalette[cl][0];
			rgb24[idx++] = colorPalette[cl][1];
			rgb24[idx++] = colorPalette[cl][2];
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
			argb32[idx++] = colorPalette[cl][0];
			argb32[idx++] = colorPalette[cl][1];
			argb32[idx++] = colorPalette[cl][2];
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
	
	@Override
	public void draw8x8BitmapTwoColorBlock(int offs, ByteMemoryAccess access,
			int rowstride) {
		int lineStride = getLineStride();
		//int offs = getBitmapOffset(offs, r);
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 4; j++) {
				byte mem;
				
				byte pix;

				mem = access.memory[access.offset + j];

				pix = (byte) ((mem >> 4) & 0xf);
				bitmap[offs] = pix;
				
				pix = (byte) (mem & 0xf);
				bitmap[offs + 1] = pix;
				
				offs += 2;
			}
			
			offs += lineStride - 2 * 4;
			access.offset += rowstride;
		}
	}
	
	@Override
	public void draw8x8BitmapFourColorBlock(int offs, ByteMemoryAccess access,
			int rowstride) {
		int lineStride = getLineStride();
		//int offs = getBitmapOffset(offs, r);
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 2; j++) {
				byte mem;
				
				byte pix;

				mem = access.memory[access.offset + j];

				pix = (byte) ((mem >> 6) & 0x3);
				bitmap[offs] = pix;
				
				pix = (byte) ((mem >> 4) & 0x3);
				bitmap[offs + 1] = pix;
				
				pix = (byte) ((mem >> 2) & 0x3);
				bitmap[offs + 2] = pix;
				
				pix = (byte) (mem & 0x3);
				bitmap[offs + 3] = pix;
				
				offs += 4;
			}
			
			offs += lineStride - 2 * 4;
			access.offset += rowstride;
		}
	}
	
	@Override
	public void draw8x8BitmapRGB332ColorBlock(int offs,
			ByteMemoryAccess access, int rowstride) {
		int lineStride = getLineStride();
		//int offs = getBitmapOffset(offset, r) + offset;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				byte mem;
				
				// XXX: no palette
				mem = access.memory[access.offset + j];
				bitmap[offs++] = mem;
			}
			
			offs += lineStride - 8;
			access.offset += rowstride;
		}
	}

	public byte getColorAtOffset(int offset) {
		return bitmap[offset];
	}
	
	@Override
	public void blitSpriteBlock(MemoryCanvas spriteCanvas, int x, int y,
			int blockMag) {
		throw new IllegalArgumentException();
	}
	
	@Override
	public void blitFourColorSpriteBlock(MemoryCanvas spriteCanvas, int x,
			int y, int blockMag) {
		throw new IllegalArgumentException();
	}

	public void clear8x8Block(int offset) {
		for (int i = 0; i < 8; i++) {
			Arrays.fill(bitmap, offset, offset + 8, (byte) 0);
			offset += getLineStride();
		}
	}

}
