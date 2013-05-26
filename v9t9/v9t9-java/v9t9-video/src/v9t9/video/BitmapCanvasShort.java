/*
  BitmapCanvasShort.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video;

import java.nio.Buffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import org.ejs.gui.images.ColorMapUtils;
import org.ejs.gui.images.V99ColorMapUtils;

import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.video.BitmapVdpCanvas;
import v9t9.common.video.ISpriteVdpCanvas;
import v9t9.common.video.VdpFormat;

/**
 * Render video content into a short array
 * @author ejs
 *
 */
public class BitmapCanvasShort extends BitmapVdpCanvas implements IGLDataCanvas {
	protected short[] sdata;
	protected int pixelsPerLine;

	

	protected short[] colorRGB332Map;
	protected short[] colorRGBMap;
	protected short[] spriteColorRGBMap;

	private short[][] fourColorRGBMap;
	private Buffer vdpCanvasBuffer;
	
	public BitmapCanvasShort() {
		super();
		setSize(256, 192);
	}
	
	
	@Override
	final public int getLineStride() {
		return pixelsPerLine;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.video.ICanvas#getPixelStride()
	 */
	@Override
	final public int getPixelStride() {
		return 1;
	}

	@Override
	public void doChangeSize() {
		int allocHeight = height;
		if ((height & 7) != 0)
			allocHeight += 8;
		
		sdata = new short[width * allocHeight * (isInterlacedEvenOdd() ? 2 : 1)];
		if (isInterlacedEvenOdd())
			pixelsPerLine = width * 2;
		else
			pixelsPerLine = width;
		
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
			short fgRGB = colorRGBMap[rowData[i]];
			sdata[offs+i] = fgRGB;
		}
	}

	/**
	 * @param buffer
	 * @return 
	 */
	public Buffer copy(Buffer buffer) {
		return copyShorts(buffer, sdata, pixelsPerLine);
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

		short col;
		col = ColorMapUtils.getRGBToRGB565(rgb);
		
		Arrays.fill(sdata, col);
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#clear()
	 */
	@Override
	public void clearToEvenOddClearColors() {
		if (colorRGBMap == null)
			return;
		
		short fgRGB = colorRGBMap[getColorMgr().getClearColor()];
		short bgRGB = colorRGBMap[getColorMgr().getClearColor1()];

		for (int i = 0; i < sdata.length; i += 2) {
			sdata[i ] = fgRGB;
			sdata[i + 1] = bgRGB;
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
			colorRGB332Map = new short[256];

		if (colorRGBMap == null) 
			colorRGBMap = new short[16];
		if (spriteColorRGBMap == null) 
			spriteColorRGBMap = new short[16];
		if (fourColorRGBMap == null) { 
			fourColorRGBMap = new short[2][];
			fourColorRGBMap[0] = new short[16];
			fourColorRGBMap[1] = new short[16];
		}
		
		for (int i = 0; i < 16; i++) {
			colorRGBMap[i] = ColorMapUtils.getRGBToRGB565(getColorMgr().getRGB(colorMap[i]));
			spriteColorRGBMap[i] = ColorMapUtils.getRGBToRGB565(getColorMgr().getRGB(spriteColorMap[i]));
			fourColorRGBMap[0][i] = ColorMapUtils.getRGBToRGB565(getColorMgr().getRGB(fourColorMap[0][i]));
			fourColorRGBMap[1][i] = ColorMapUtils.getRGBToRGB565(getColorMgr().getRGB(fourColorMap[1][i]));
		}

		// TODO: clean up between color manager, SwtLwjglVideoRenderer, etc in terms of how
		// to map greyscale -- we get it for free from OpenGL!
		boolean greyscale = getColorMgr().isGreyscale();
//		if (greyscale) {
//			byte[] rgb = { 0, 0, 0 };
//			for (int i = 0; i < 256; i++) {
//				V99ColorMapUtils.getGRB332(rgb, (byte) i, true);
//				colorRGB332Map[i] = (byte) ColorMapUtils.getRGBLum(rgb);
//			}
//		} else 
		{
			byte[] rgb = { 0, 0, 0 };
			for (int i = 0; i < 256; i++) {
				V99ColorMapUtils.getGRB332(rgb, (byte) i, greyscale);
				colorRGB332Map[i] = ColorMapUtils.getRGBToRGB565(rgb);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.video.BitmapVdpCanvas#getNextRGB(java.nio.Buffer, byte[])
	 */
	@Override
	public void getNextRGB(Buffer buffer, byte[] rgb) {
		short pixel = ((ShortBuffer) buffer).get();
		ColorMapUtils.rgb565ToRGB(pixel, rgb);
	}

	protected void drawEightPixels(int offs, byte mem, byte fg, byte bg) {
		short fgRGB = colorRGBMap[fg];
		short bgRGB = colorRGBMap[bg];
		sdata[offs + 0] = (mem & 0x80) != 0 ? fgRGB : bgRGB;
		sdata[offs + 1] = (mem & 0x40) != 0 ? fgRGB : bgRGB;
		sdata[offs + 2] = (mem & 0x20) != 0 ? fgRGB : bgRGB;
		sdata[offs + 3] = (mem & 0x10) != 0 ? fgRGB : bgRGB;
		sdata[offs + 4] = (mem & 0x08) != 0 ? fgRGB : bgRGB;
		sdata[offs + 5] = (mem & 0x04) != 0 ? fgRGB : bgRGB;
		sdata[offs + 6] = (mem & 0x02) != 0 ? fgRGB : bgRGB;
		sdata[offs + 7] = (mem & 0x01) != 0 ? fgRGB : bgRGB;
	}

	protected void drawSixPixels(int offs, byte mem, byte fg, byte bg) {
		short fgRGB = colorRGBMap[fg];
		short bgRGB = colorRGBMap[bg];
		sdata[offs + 0] = (mem & 0x80) != 0 ? fgRGB : bgRGB;
		sdata[offs + 1] = (mem & 0x40) != 0 ? fgRGB : bgRGB;
		sdata[offs + 2] = (mem & 0x20) != 0 ? fgRGB : bgRGB;
		sdata[offs + 3] = (mem & 0x10) != 0 ? fgRGB : bgRGB;
		sdata[offs + 4] = (mem & 0x08) != 0 ? fgRGB : bgRGB;
		sdata[offs + 5] = (mem & 0x04) != 0 ? fgRGB : bgRGB;
	}

	public void drawEightSpritePixels(int x, int y, byte mem, byte fg, byte bitmask, boolean isLogicalOr) {
		int offs = getBitmapOffset(x, y);
		int endOffs = getBitmapOffset(256, y);
		short fgRGB = colorRGBMap[fg];
		for (int i = 0; i < 8; i++) {
			int ioffs = offs + i;
			if (ioffs >= endOffs)
				break;
			if ((mem & bitmask & 0x80) != 0) {
				sdata[ioffs] = fgRGB;
			}
			bitmask <<= 1;
			mem <<= 1;
		}
	}

	public void drawEightMagnifiedSpritePixels(int x, int y, byte mem_, byte fg, short bitmask, boolean isLogicalOr) {
		int offs = getBitmapOffset(x, y);
		int endOffs = getBitmapOffset(256, y);
		short fgRGB = colorRGBMap[fg];
		short mem = (short) (mem_ << 8);
		for (int i = 0; i < 8; i++) {
			int ioffs = offs + i * 2;
			if (ioffs >= endOffs)
				break;
			if ((mem & bitmask & 0x8000) != 0) {
				sdata[ioffs] = fgRGB;
			}
			bitmask <<= 1;
			if (ioffs + 1 >= endOffs)
				break;
			if ((mem & bitmask & 0x8000) != 0) {
				sdata[ioffs + 1] = fgRGB;
			}
			bitmask <<= 1;
			mem <<= 1;
		}
	}

	public void drawEightDoubleMagnifiedSpritePixels(int x, int y, byte mem_, byte fg, short bitmask, boolean isLogicalOr) {
		int offs = getBitmapOffset(x, y);
		int endOffs = getBitmapOffset(256, y);
		short fgRGB = colorRGBMap[fg];
		short mem = (short) (mem_ << 8);
		for (int i = 0; i < 8; i++) {
			int ioffs = offs + i;
			if (ioffs >= endOffs)
				break;
			if ((mem & bitmask & 0x8000) != 0) {
				sdata[ioffs] = fgRGB;
				if (ioffs + 1 >= endOffs)
					return;
				sdata[ioffs + 1] = fgRGB;
			}
			bitmask <<= 1;
			if (ioffs + 2 >= endOffs)
				break;
			if ((mem & bitmask & 0x8000) != 0) {
				sdata[ioffs + 2] = fgRGB;
				if (ioffs + 3 >= endOffs)
					return;
				sdata[ioffs + 3] = fgRGB;
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
			sdata[offs + 0] = colorRGBMap[((mem >> 4) & 0xf)];
			sdata[offs + 1] = colorRGBMap[(mem & 0xf)];
			
			mem = access.memory[access.offset + 1];
			sdata[offs + 2] = colorRGBMap[((mem >> 4) & 0xf)];
			sdata[offs + 3] = colorRGBMap[(mem & 0xf)];
			
			mem = access.memory[access.offset + 2];
			sdata[offs + 4] = colorRGBMap[((mem >> 4) & 0xf)];
			sdata[offs + 5] = colorRGBMap[(mem & 0xf)];
			
			mem = access.memory[access.offset + 3];
			sdata[offs + 6] = colorRGBMap[((mem >> 4) & 0xf)];
			sdata[offs + 7] = colorRGBMap[(mem & 0xf)];
			
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

			sdata[offs + 0] = colorRGBMap[((mem >> 6) & 0x3)];
			sdata[offs + 1] = colorRGBMap[((mem >> 4) & 0x3)];
			sdata[offs + 2] = colorRGBMap[((mem >> 2) & 0x3)];
			sdata[offs + 3] = colorRGBMap[(mem & 0x3)];
			
			mem = access.memory[access.offset + 1];
			
			sdata[offs + 4] = colorRGBMap[((mem >> 6) & 0x3)];
			sdata[offs + 5] = colorRGBMap[((mem >> 4) & 0x3)];
			sdata[offs + 6] = colorRGBMap[((mem >> 2) & 0x3)];
			sdata[offs + 7] = colorRGBMap[(mem & 0x3)];
			
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
			sdata[offs + 0] = colorRGB332Map[access.memory[access.offset + 0] & 0xff];
			sdata[offs + 1] = colorRGB332Map[access.memory[access.offset + 1] & 0xff];
			sdata[offs + 2] = colorRGB332Map[access.memory[access.offset + 2] & 0xff];
			sdata[offs + 3] = colorRGB332Map[access.memory[access.offset + 3] & 0xff];
			sdata[offs + 4] = colorRGB332Map[access.memory[access.offset + 4] & 0xff];
			sdata[offs + 5] = colorRGB332Map[access.memory[access.offset + 5] & 0xff];
			sdata[offs + 6] = colorRGB332Map[access.memory[access.offset + 6] & 0xff];
			sdata[offs + 7] = colorRGB332Map[access.memory[access.offset + 7] & 0xff];
			
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
						short rgb = spriteColorRGBMap[cl];
						sdata[bitmapOffset] = rgb;
						if (blockMag > 1 && bitmapOffset + 1 < sdata.length) {
							sdata[bitmapOffset + 1] = rgb;
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
						short rgb = spriteColorRGBMap[cl];
						sdata[bitmapOffset] = rgb;
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
							short rgb = spriteColorRGBMap[cl];
							sdata[bitmapOffset] = rgb;
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
		vdpCanvasBuffer = copyShorts(vdpCanvasBuffer, sdata, pixelsPerLine);
		return vdpCanvasBuffer;
	}


	/* (non-Javadoc)
	 * @see v9t9.video.IGLDataCanvas#getImageType()
	 */
	@Override
	public int getImageType() {
		//return getColorMgr().isGreyscale() ? GL_LUMINANCE12_ALPHA4 : GL_UNSIGNED_SHORT_4_4_4_4;
		return GL_UNSIGNED_SHORT_5_6_5;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.video.IGLDataCanvas#getImageFormat()
	 */
	@Override
	public int getImageFormat() {
//		return getColorMgr().isGreyscale() ? GL_LUMINANCE12_ALPHA4 : GL_RGBA;
		return GL_RGB;
	}

	/* (non-Javadoc)
	 * @see v9t9.video.IGLDataCanvas#getInternalFormat()
	 */
	@Override
	public int getInternalFormat() {
		return getColorMgr().isGreyscale() ? GL_LUMINANCE12_ALPHA4 : GL_RGB4;
//		return GL_RGB4;
	}

}
