/*
  ImageDataCanvasR3G3B2.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import org.ejs.gui.images.ColorMapUtils;
import org.ejs.gui.images.V99ColorMapUtils;

import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.video.BitmapVdpCanvas;
import v9t9.common.video.ISpriteVdpCanvas;
import v9t9.common.video.VdpFormat;

/**
 * Render video content into a direct ByteBuffer
 * @author ejs
 *
 */
public class BufferCanvasByteR3G3B2 extends BitmapVdpCanvas implements IGLDataCanvas {
	protected byte[] colorRGB332Map;
	protected byte[] colorRGBMap;
	protected byte[] spriteColorRGBMap;

	private byte[][] fourColorRGBMap;

	private ByteBuffer buffer;
	private int bytesPerLine;
	
	public BufferCanvasByteR3G3B2() {
		super();
		setSize(256, 192);
	}

	@Override
	final public int getLineStride() {
		return bytesPerLine;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.video.ICanvas#getPixelStride()
	 */
	@Override
	final public int getPixelStride() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.video.VdpCanvas#doChangeSize()
	 */
	@Override
	public void doChangeSize() {
		int allocHeight = height;
		if ((height & 7) != 0)
			allocHeight += 8;
		
		int sz = width * allocHeight * (isInterlacedEvenOdd() ? 2 : 1);
		if (buffer == null || buffer.capacity() < sz) {
			buffer = ByteBuffer.allocateDirect(sz);
		}
		
		if (isInterlacedEvenOdd())
			bytesPerLine = width * 2;
		else
			bytesPerLine = width;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.video.IVdpCanvas#writeRow(byte[])
	 */
	@Override
	public void writeRow(int y, byte[] rowData) {
		if (colorRGBMap == null)
			return;
		
		int offs = getBitmapOffset(0, y);
		for (int i = 0; i < rowData.length; i++) {
			byte fgRGB = colorRGBMap[rowData[i]];
			buffer.put(offs+i, fgRGB);
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#clear()
	 */
	@Override
	public void clear() {
		byte[] rgb;
		if (getFormat() == VdpFormat.COLOR256_1x1) {
			rgb = new byte[] { 0, 0, 0};
			V99ColorMapUtils.getGRB332(rgb, (byte) getColorMgr().getClearColor(), getColorMgr().isGreyscale());
		} else {
			rgb = getColorMgr().getRGB(getColorMgr().getClearColor());
		}

		byte col;
		col = V99ColorMapUtils.getRGBToGRB332(rgb);

		for (int i = 0; i < buffer.capacity(); i++)
			buffer.put(i, col);
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#clear()
	 */
	@Override
	public void clearToEvenOddClearColors() {
		if (colorRGBMap == null)
			return;
		
		byte fgRGB = colorRGBMap[getColorMgr().getClearColor()];
		byte bgRGB = colorRGBMap[getColorMgr().getClearColor1()];

		for (int i = 0; i < buffer.capacity(); i += 2) {
			buffer.put(i, fgRGB)
				.put(i + 1, bgRGB);
		}
	}
	


	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#getBitmapOffset(int, int)
	 */
	@Override
	final public int getBitmapOffset(int x, int y) {
		return getLineStride() * (y) + x;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#syncColors()
	 */
	@Override
	public void syncColors() {
		super.syncColors();

		if (colorRGB332Map == null)
			colorRGB332Map = new byte[256];

		if (colorRGBMap == null) 
			colorRGBMap = new byte[16];
		if (spriteColorRGBMap == null) 
			spriteColorRGBMap = new byte[16];
		if (fourColorRGBMap == null) { 
			fourColorRGBMap = new byte[2][];
			fourColorRGBMap[0] = new byte[16];
			fourColorRGBMap[1] = new byte[16];
		}
		
		for (int i = 0; i < 16; i++) {
			colorRGBMap[i] = V99ColorMapUtils.getRGBToGRB332(getColorMgr().getRGB(colorMap[i]));
			spriteColorRGBMap[i] = V99ColorMapUtils.getRGBToGRB332(getColorMgr().getRGB(spriteColorMap[i]));
			fourColorRGBMap[0][i] = V99ColorMapUtils.getRGBToGRB332(getColorMgr().getRGB(fourColorMap[0][i]));
			fourColorRGBMap[1][i] = V99ColorMapUtils.getRGBToGRB332(getColorMgr().getRGB(fourColorMap[1][i]));
		}

		// TODO: clean up between color manager, SwtLwjglVideoRenderer, etc in terms of how
		// to map greyscale -- we get it for free from OpenGL!
		if (getColorMgr().isGreyscale()) {
			byte[] rgb = { 0, 0, 0 };
			for (int i = 0; i < 256; i++) {
				V99ColorMapUtils.getGRB332(rgb, (byte) i, true);
				colorRGB332Map[i] = (byte) ColorMapUtils.getRGBLum(rgb);
			}
		} else {
			for (int i = 0; i < 256; i++) {
				int j = ((i & 0xe0) >> 3) | ((i & 0x1c) << 3) | (i & 0x3);
				colorRGB332Map[i] = (byte) j;
			}
		}
	}

	protected void drawEightPixels(int offs, byte mem, byte fg, byte bg) {
		byte fgRGB = colorRGBMap[fg];
		byte bgRGB = colorRGBMap[bg];
		buffer.put(offs + 0, (mem & 0x80) != 0 ? fgRGB : bgRGB)
			.put(offs + 1, (mem & 0x40) != 0 ? fgRGB : bgRGB)
			.put(offs + 2, (mem & 0x20) != 0 ? fgRGB : bgRGB)
			.put(offs + 3, (mem & 0x10) != 0 ? fgRGB : bgRGB)
			.put(offs + 4, (mem & 0x08) != 0 ? fgRGB : bgRGB)
			.put(offs + 5, (mem & 0x04) != 0 ? fgRGB : bgRGB)
			.put(offs + 6, (mem & 0x02) != 0 ? fgRGB : bgRGB)
			.put(offs + 7, (mem & 0x01) != 0 ? fgRGB : bgRGB);
	}

	protected void drawSixPixels(int offs, byte mem, byte fg, byte bg) {
		byte fgRGB = colorRGBMap[fg];
		byte bgRGB = colorRGBMap[bg];
		buffer.put(offs + 0, (mem & 0x80) != 0 ? fgRGB : bgRGB)
			.put(offs + 1, (mem & 0x40) != 0 ? fgRGB : bgRGB)
			.put(offs + 2, (mem & 0x20) != 0 ? fgRGB : bgRGB)
			.put(offs + 3, (mem & 0x10) != 0 ? fgRGB : bgRGB)
			.put(offs + 4, (mem & 0x08) != 0 ? fgRGB : bgRGB)
			.put(offs + 5, (mem & 0x04) != 0 ? fgRGB : bgRGB);
	}

	public void drawEightSpritePixels(int x, int y, byte mem, byte fg, byte bitmask, boolean isLogicalOr) {
		int offs = getBitmapOffset(x, y);
		int endOffs = getBitmapOffset(256, y);
		byte fgRGB = colorRGBMap[fg];
		for (int i = 0; i < 8; i++) {
			int ioffs = offs + i;
			if (ioffs >= endOffs)
				break;
			if ((mem & bitmask & 0x80) != 0) {
				buffer.put(ioffs, fgRGB);
			}
			bitmask <<= 1;
			mem <<= 1;
		}
	}

	public void drawEightMagnifiedSpritePixels(int x, int y, byte mem_, byte fg, short bitmask, boolean isLogicalOr) {
		int offs = getBitmapOffset(x, y);
		int endOffs = getBitmapOffset(256, y);
		byte fgRGB = colorRGBMap[fg];
		short mem = (short) (mem_ << 8);
		for (int i = 0; i < 8; i++) {
			int ioffs = offs + i * 2;
			if (ioffs >= endOffs)
				break;
			if ((mem & bitmask & 0x8000) != 0) {
				buffer.put(ioffs, fgRGB);
			}
			bitmask <<= 1;
			if (ioffs + 1 >= endOffs)
				break;
			if ((mem & bitmask & 0x8000) != 0) {
				buffer.put(ioffs + 1, fgRGB);
			}
			bitmask <<= 1;
			mem <<= 1;
		}
	}

	public void drawEightDoubleMagnifiedSpritePixels(int x, int y, byte mem_, byte fg, short bitmask, boolean isLogicalOr) {
		int offs = getBitmapOffset(x, y);
		int endOffs = getBitmapOffset(256, y);
		byte fgRGB = colorRGBMap[fg];
		short mem = (short) (mem_ << 8);
		for (int i = 0; i < 8; i++) {
			int ioffs = offs + i;
			if (ioffs >= endOffs)
				break;
			if ((mem & bitmask & 0x8000) != 0) {
				buffer.put(ioffs, fgRGB);
				if (ioffs + 1 >= endOffs)
					return;
				buffer.put(ioffs + 1, fgRGB);
			}
			bitmask <<= 1;
			if (ioffs + 2 >= endOffs)
				break;
			if ((mem & bitmask & 0x8000) != 0) {
				buffer.put(ioffs + 2, fgRGB);
				if (ioffs + 3 >= endOffs)
					return;
				buffer.put(ioffs + 3, fgRGB);
			}
			bitmask <<= 1;
			mem <<= 1;
		}
	}
	@Override
	public void draw8x8BitmapTwoColorBlock(int x, int y,
			ByteMemoryAccess access, int rowstride) {
		int lineStride = getLineStride();
		int offs = getBitmapOffset(x, y);
		for (int i = 0; i < 8; i++) {
			byte mem;

			mem = access.memory[access.offset + 0];
			buffer.put(offs + 0, colorRGBMap[((mem >> 4) & 0xf)]);
			buffer.put(offs + 1, colorRGBMap[(mem & 0xf)]);
			
			mem = access.memory[access.offset + 1];
			buffer.put(offs + 2, colorRGBMap[((mem >> 4) & 0xf)]);
			buffer.put(offs + 3, colorRGBMap[(mem & 0xf)]);
			
			mem = access.memory[access.offset + 2];
			buffer.put(offs + 4, colorRGBMap[((mem >> 4) & 0xf)]);
			buffer.put(offs + 5, colorRGBMap[(mem & 0xf)]);
			
			mem = access.memory[access.offset + 3];
			buffer.put(offs + 6, colorRGBMap[((mem >> 4) & 0xf)]);
			buffer.put(offs + 7, colorRGBMap[(mem & 0xf)]);
			
			offs += lineStride;
			access.offset += rowstride;
		}
	}
	
	@Override
	public void draw8x8BitmapFourColorBlock(int x, int y,
			ByteMemoryAccess access, int rowstride) {
		int lineStride = getLineStride();
		int offs = getBitmapOffset(x, y);
		for (int i = 0; i < 8; i++) {
			byte mem;
			
			mem = access.memory[access.offset + 0];

			buffer.put(offs + 0, colorRGBMap[((mem >> 6) & 0x3)]);
			buffer.put(offs + 1, colorRGBMap[((mem >> 4) & 0x3)]);
			buffer.put(offs + 2, colorRGBMap[((mem >> 2) & 0x3)]);
			buffer.put(offs + 3, colorRGBMap[(mem & 0x3)]);
			
			mem = access.memory[access.offset + 1];
			
			buffer.put(offs + 4, colorRGBMap[((mem >> 6) & 0x3)]);
			buffer.put(offs + 5, colorRGBMap[((mem >> 4) & 0x3)]);
			buffer.put(offs + 6, colorRGBMap[((mem >> 2) & 0x3)]);
			buffer.put(offs + 7, colorRGBMap[(mem & 0x3)]);
			
			offs += lineStride;
			access.offset += rowstride;
		}
	}
	
	@Override
	public void draw8x8BitmapRGB332ColorBlock(int x, int y,
			ByteMemoryAccess access, int rowstride) {
		int lineStride = getLineStride();
		int offs = getBitmapOffset(x, y);
		for (int i = 0; i < 8; i++) {
			buffer.put(offs + 0, colorRGB332Map[access.memory[access.offset + 0] & 0xff]);
			buffer.put(offs + 1, colorRGB332Map[access.memory[access.offset + 1] & 0xff]);
			buffer.put(offs + 2, colorRGB332Map[access.memory[access.offset + 2] & 0xff]);
			buffer.put(offs + 3, colorRGB332Map[access.memory[access.offset + 3] & 0xff]);
			buffer.put(offs + 4, colorRGB332Map[access.memory[access.offset + 4] & 0xff]);
			buffer.put(offs + 5, colorRGB332Map[access.memory[access.offset + 5] & 0xff]);
			buffer.put(offs + 6, colorRGB332Map[access.memory[access.offset + 6] & 0xff]);
			buffer.put(offs + 7, colorRGB332Map[access.memory[access.offset + 7] & 0xff]);
			
			offs += lineStride;
			access.offset += rowstride;
		}
	}
	
	@Override
	public void blitSpriteBlock(ISpriteVdpCanvas spriteCanvas, int x, int y,
			int blockMag) {
		int sprOffset = spriteCanvas.getBitmapOffset(x, y);
		int bitmapOffset = getBitmapOffset(x * blockMag, y);
		try {
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					byte cl = spriteCanvas.getColorAtOffset(sprOffset + j);
					if (cl != 0) {
						byte rgb = spriteColorRGBMap[cl];
						buffer.put(bitmapOffset, rgb);
						if (blockMag > 1 && bitmapOffset + 1 < buffer.capacity()) {
							buffer.put(bitmapOffset + 1, rgb);
						}
					}
					bitmapOffset += blockMag;
				}
				sprOffset += spriteCanvas.getLineStride();
				bitmapOffset += getLineStride() - 8 * blockMag;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// ignore
		}
	}
	
	@Override
	public void blitFourColorSpriteBlock(ISpriteVdpCanvas spriteCanvas, int x, int y,
			int blockMag) {
		int sprOffset = spriteCanvas.getBitmapOffset(x, y);
		int bitmapOffset = getBitmapOffset(x * blockMag, y);
		try {
			for (int i = 0; i < 8; i++) {
				int colorColumn = x % 2;
				for (int j = 0; j < 8; j++) {
					byte col = spriteCanvas.getColorAtOffset(sprOffset + j);
					byte cl;
					if (colorColumn == 0)
						cl = (byte) ((col & 0xc) >> 2);
					else
						cl = (byte) (col & 0x3);
					if (cl != 0) {
						byte rgb = spriteColorRGBMap[cl];
						buffer.put(bitmapOffset, rgb);
					}
					bitmapOffset ++;
					colorColumn ^= 1;
					//System.out.println(j+","+(j * blockMag + x + 1));
					if (blockMag > 1) {
						if (colorColumn == 0)
							cl = (byte) ((col & 0xc) >> 2);
						else
							cl = (byte) (col & 0x3);
						if (cl != 0) {
							byte rgb = spriteColorRGBMap[cl];
							buffer.put(bitmapOffset, rgb);
						}
						bitmapOffset ++;
						colorColumn ^= 1;
					}
				}
				sprOffset += spriteCanvas.getLineStride();
				bitmapOffset += getLineStride() - 8 * blockMag;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// ignore
		}			
	}

	/* (non-Javadoc)
	 * @see v9t9.video.IGLDataCanvas#getBuffer()
	 */
	@Override
	public Buffer getBuffer() {
		return buffer;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.video.BitmapVdpCanvas#getNextRGB(java.nio.Buffer, byte[])
	 */
	public void getNextRGB(Buffer buffer, byte[] rgb) {
		byte b = ((ByteBuffer) buffer).get();
		V99ColorMapUtils.getGRB332(rgb, b, getColorMgr().isGreyscale());
		byte t = rgb[0];
		rgb[0] = rgb[1];
		rgb[1] = t;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.video.IGLDataCanvas#getImageType()
	 */
	@Override
	public int getImageType() {
		return getColorMgr().isGreyscale() ? GL_UNSIGNED_BYTE : GL_UNSIGNED_BYTE_3_3_2;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.video.IGLDataCanvas#getImageFormat()
	 */
	@Override
	public int getImageFormat() {
		return getColorMgr().isGreyscale() ? GL_LUMINANCE : GL_RGB;
	}
	/* (non-Javadoc)
	 * @see v9t9.video.IGLDataCanvas#getInternalFormat()
	 */
	@Override
	public int getInternalFormat() {
		return GL_RGB4;
	}

}
