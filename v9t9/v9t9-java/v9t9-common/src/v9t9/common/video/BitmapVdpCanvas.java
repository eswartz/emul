/*
  BitmapVdpCanvas.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.video;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import v9t9.common.memory.ByteMemoryAccess;

/**
 * @author ejs
 *
 */
public abstract class BitmapVdpCanvas extends VdpCanvas {

	public BitmapVdpCanvas() {
		super();
	}

	/**
	 * Draw eight pixels in a row, where pixels corresponding to an "on"
	 * bit in "mem" are painted with the "fg" color, otherwise with the "bg" color.
	 * @param offs
	 * @param mem
	 * @param fg foreground; use 16 for the vdpreg[7] fg 
	 * @param bg background; use 0 for the vdpreg[7] bg
	 * @see #getBitmapOffset(int, int)
	 */
	abstract protected void drawEightPixels(int offs, byte mem, byte fg, byte bg); 
	/**
	 * Draw six pixels in a row, where pixels corresponding to an "on"
	 * bit in "mem" are painted with the "fg" color, otherwise with the "bg" color.
	 * @param offs
	 * @param mem
	 * @param fg foreground; use 16 for the vdpreg[7] fg 
	 * @param bg background; use 0 for the vdpreg[7] bg
	 * @see #getBitmapOffset(int, int)
	 */
	abstract protected void drawSixPixels(int offs, byte mem, byte fg, byte bg); 

	/** Get an implementation-defined offset into the bitmap */ 
	public abstract int getBitmapOffset(int x, int y);

	
	/**
	 * Blit an 8x8 block defined by a pattern and a foreground/background color to the bitmap
	 * @param r
	 * @param c
	 * @param pattern
	 * @param fg foreground; use 16 for the vdpreg[7] fg 
	 * @param bg background; use 0 for the vdpreg[7] bg
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
	 * @param fg foreground; use 16 for the vdpreg[7] fg 
	 * @param bg background; use 0 for the vdpreg[7] bg
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
	 * @param colors array of 0x&lt;fg&gt;&lt;bg&gt; pixels; bg may be 0 for vdpreg[7] bg
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

	protected Buffer copyBytes(Buffer buffer, byte[] data, int bytesPerLine, int bpp) {
		int vw = getVisibleWidth();
		int vh = getVisibleHeight();
		if (buffer == null || buffer.capacity() < bytesPerLine * vh)
			buffer = ByteBuffer.allocateDirect(bytesPerLine * vh);

		buffer.rewind();
		int offs = getBitmapOffset(0, 0);
		//int bpp = bytesPerLine / width;
		if (bytesPerLine == bpp * vw) {
			((ByteBuffer) buffer).put(data, offs, bpp * vw * vh);
		} else {
			for (int r = 0; r < vh; r++) {
				((ByteBuffer) buffer).put(data, offs, bpp * vw);
				offs += bpp * vw;	// skip other half
			}
		}
		buffer.rewind();
		
		return buffer;
	}
	
	protected Buffer copyShorts(Buffer buffer, short[] data, int pixelsPerLine) {
		int vw = getVisibleWidth();
		int vh = getVisibleHeight();
		if (buffer == null || buffer.capacity() < pixelsPerLine * vh) {
			buffer = ByteBuffer.allocateDirect(pixelsPerLine * vh * 2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
		}
		
		buffer.rewind();
		int offs = getBitmapOffset(0, 0);
		//int bpp = bytesPerLine / width;
		if (pixelsPerLine == vw) {
			((ShortBuffer) buffer).put(data, offs, vw * vh);
		} else {
			for (int r = 0; r < vh; r++) {
				((ShortBuffer) buffer).put(data, offs, vw);
				offs += vw;	// skip other half
			}
		}
		buffer.rewind();
		
		return buffer;
	}
	
	protected Buffer copyInts(Buffer buffer, int[] data, int pixelsPerLine) {
		int vw = getVisibleWidth();
		int vh = getVisibleHeight();
		if (buffer == null || buffer.capacity() < pixelsPerLine * vh) {
			buffer = ByteBuffer.allocateDirect(pixelsPerLine * vh * 4).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
		}
		
		buffer.rewind();
		int offs = getBitmapOffset(0, 0);
		//int bpp = bytesPerLine / width;
		if (pixelsPerLine == vw) {
			((IntBuffer) buffer).put(data, offs, vw * vh);
		} else {
			for (int r = 0; r < vh; r++) {
				((IntBuffer) buffer).put(data, offs, vw);
				offs += vw;	// skip other half
			}
		}
		buffer.rewind();
		
		return buffer;
	}


	public int getDisplayAdjustOffset() {
		int displayAdjust = getYOffset() * getLineStride() + getXOffset() * getPixelStride();
		return displayAdjust;
	}

	public abstract Buffer getBuffer();

	public abstract void getNextRGB(Buffer buffer, byte[] rgb);
}