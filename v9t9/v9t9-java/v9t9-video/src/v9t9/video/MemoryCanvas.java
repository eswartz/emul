/*
  MemoryCanvas.java

  (c) 2011-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;

import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.video.BitmapVdpCanvas;
import v9t9.common.video.ISpriteVdpCanvas;

/**
 * This class holds the low-level bitmap containing the image
 * of the video screen.  This bitmap is used to update the actual
 * visible canvas on the client.
 * @author ejs
 *
 */
public class MemoryCanvas extends BitmapVdpCanvas {
    final int UPDATEBLOCK_ROW_STRIDE = (256+64);
	final byte[] bitmap = new byte[UPDATEBLOCK_ROW_STRIDE * 256];
    final int UPDPTR(int y,int x) { return ((y)*UPDATEBLOCK_ROW_STRIDE)+(x)+32; }

    public MemoryCanvas() {
    	setSize(256, 192);
    }

	public void doChangeSize() {
	}
	
	public void clear() {
		Arrays.fill(bitmap, (byte) getColorMgr().getClearColor());
	}

	public void clearToEvenOddClearColors() {
		byte cc = (byte) getColorMgr().getClearColor();
		byte cc1 = (byte) getColorMgr().getClearColor1();
		for (int x = 0; x < bitmap.length; x += 2) {
			bitmap[x] = cc;
			bitmap[x+1] = cc1;
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.video.IVdpCanvas#writeRow(byte[])
	 */
	@Override
	public void writeRow(int y, byte[] rowData) {
		System.arraycopy(rowData, 0, bitmap, getBitmapOffset(0, y), rowData.length);
	}
	
	
	@Override
	public int getLineStride() {
		return UPDATEBLOCK_ROW_STRIDE;
	}

	public void drawEightPixels(int offs, byte pattern, byte fg, byte bg) {
		int mask = 0x80;
		while (mask != 0) {
			bitmap[offs++] = ((pattern & mask) != 0) ? fg : bg;
			mask >>= 1;
		}
	}
	
	public void drawSixPixels(int offs, byte pattern, byte fg, byte bg) {
		int mask = 0x80;
		while (mask != 0x2) {
			bitmap[offs++] = ((pattern & mask) != 0) ? fg : bg;
			mask >>= 1;
		}
	}
	
	public void drawEightSpritePixels(int x, int y, byte mem, byte fg, byte bitmask, boolean isLogicalOr) {
		int offs = getBitmapOffset(x, y);
		if (isLogicalOr) {
			for (int i = 0; i < 8; i++) {
				if (x + i >= 256)
					break;
				if ((mem & 0x80) != 0) {
					bitmap[offs + i] |= fg;
				}
				mem <<= 1;
			}
		} else {
			for (int i = 0; i < 8; i++) {
				if (x + i >= 256)
					break;
				if ((mem & 0x80) != 0) {
					bitmap[offs + i] = fg;
				}
				mem <<= 1;
			}
		}
	}

	public void drawEightMagnifiedSpritePixels(int x, int y, byte mem, byte fg, short bitmask, boolean isLogicalOr) {
		int offs = getBitmapOffset(x, y);
		for (int i = 0; i < 8; i++) {
			if ((mem & 0x80) != 0) {
				if (x + i*2 >= 256)
					break;
				if (isLogicalOr) {
					bitmap[offs + i * 2] |= fg;
					if (x + i*2 >= 256)
						break;
					bitmap[offs + i * 2 + 1] |= fg;
				} else {
					bitmap[offs + i * 2] = fg;
					if (x + i*2 + 1 >= 256)
						break;
					bitmap[offs + i * 2 + 1] = fg;
				}
			}
			mem <<= 1;
		}
	}
	public void drawEightDoubleMagnifiedSpritePixels(int x, int y, byte mem, byte fg, short bitmask, boolean isLogicalOr) {
		int offs = getBitmapOffset(x, y);
		for (int i = 0; i < 8; i++) {
			if (x + i*4 >= 256)
				break;

			if ((mem & 0x80) != 0) {
				if (isLogicalOr) {
					bitmap[offs + i * 4] |= fg;
					if (x + i*4 >= 256)
						break;
					bitmap[offs + i * 4 + 1] |= fg;
					if (x + i*4 + 1 >= 256)
						break;
					bitmap[offs + i * 4 + 2] |= fg;
					if (x + i*4 + 2 >= 256)
						break;
					if (x + i*4 + 3 >= 256)
						break;
					bitmap[offs + i * 4 + 3] |= fg;
				} else {
					bitmap[offs + i * 4] = fg;
					if (x + i*4 + 1 >= 256)
						break;
					bitmap[offs + i * 4 + 1] = fg;
					if (x + i*4 + 2 >= 256)
						break;
					bitmap[offs + i * 4 + 2] = fg;
					if (x + i*4 + 3 >= 256)
						break;
					bitmap[offs + i * 4 + 3] = fg;
				}
			}
			mem <<= 1;
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
	
	/* (non-Javadoc)
	 * @see v9t9.common.video.IVdpCanvas#draw8x8BitmapTwoColorByte(int, int, v9t9.common.memory.ByteMemoryAccess)
	 */
	@Override
	public void draw8x8BitmapTwoColorByte(int x, int y, ByteMemoryAccess access) {
		int offs = getBitmapOffset(x, y);
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
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.video.IVdpCanvas#draw8x8BitmapFourColorByte(int, int, v9t9.common.memory.ByteMemoryAccess)
	 */
	@Override
	public void draw8x8BitmapFourColorByte(int x, int y, ByteMemoryAccess access) {
		int offs = getBitmapOffset(x, y);
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
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.video.IVdpCanvas#draw8x8BitmapRGB332ColorByte(int, int, v9t9.common.memory.ByteMemoryAccess)
	 */
	@Override
	public void draw8x8BitmapRGB332ColorByte(int x, int y,
			ByteMemoryAccess access) {
		int offs = getBitmapOffset(x, y);
		for (int j = 0; j < 8; j++) {
			byte mem;
			
			// XXX: no palette
			mem = access.memory[access.offset + j];
			bitmap[offs++] = mem;
		}
	}

	public byte getColorAtOffset(int offset) {
		return bitmap[offset];
	}
	
	public void clear8x8Block(int offset) {
		for (int i = 0; i < 8; i++) {
			Arrays.fill(bitmap, offset, offset + 8, (byte) 0);
			offset += getLineStride();
		}
	}
	

	@Override
	public void blitSpriteBlock(ISpriteVdpCanvas spriteCanvas, int x, int y,
			int blockMag) {
		throw new IllegalArgumentException();
	}
	
	@Override
	public void blitFourColorSpriteBlock(ISpriteVdpCanvas spriteCanvas, int x,
			int y, int blockMag) {
		throw new IllegalArgumentException();
	}
	
	public Buffer copy(Buffer buffer) {
		return copyBytes(buffer, bitmap, getLineStride(), 1);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.video.BitmapVdpCanvas#getBuffer()
	 */
	@Override
	public Buffer getBuffer() {
		return ByteBuffer.wrap(bitmap);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.video.BitmapVdpCanvas#getNextRGB(java.nio.Buffer, byte[])
	 */
	@Override
	public void getNextRGB(Buffer buffer, byte[] rgb) {
		byte col = ((ByteBuffer) buffer).get();
		byte[] crgb = getColorMgr().getRGB(col & 0xff);
		rgb[0] = crgb[0];
		rgb[1] = crgb[1];
		rgb[2] = crgb[2];
	}
}
