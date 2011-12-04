/**
 * 
 */
package v9t9.gui.video;

import java.util.Arrays;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.video.ColorMapUtils;
import v9t9.engine.video.Sprite2Canvas;

/**
 * Render video content into an ImageData
 * @author ejs
 *
 */
public class ImageDataCanvas24Bit extends ImageDataCanvas {
	private static PaletteData stockPaletteData = new PaletteData(0xFF0000, 0xFF00, 0xFF);

	protected byte[][] colorRGBMap;
	protected byte[][] spriteColorRGBMap;

	private byte[][][] fourColorRGBMap;

	public ImageDataCanvas24Bit() {
		super();
		setSize(256, 192);
	}
	
	protected PaletteData getPaletteData() {
		return stockPaletteData;
	}

	@Override
	protected ImageData createImageData() {
		int allocHeight = height;
		if ((height & 7) != 0)
			allocHeight += 8;
		
		ImageData data = new ImageData(width, allocHeight * (isInterlacedEvenOdd() ? 2 : 1), 24, getPaletteData());
		if (isInterlacedEvenOdd())
			bytesPerLine = data.bytesPerLine * 2;
		else
			bytesPerLine = data.bytesPerLine;
		
		return data;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#clear()
	 */
	@Override
	public void clear() {
		byte[] rgb;
		if (getFormat() == Format.COLOR256_1x1) {
			rgb = new byte[] { 0, 0, 0};
			ColorMapUtils.getGRB332(rgb, (byte) getColorMgr().getClearColor(), getColorMgr().isGreyscale());
		} else {
			rgb = getColorMgr().getRGB(getColorMgr().getClearColor());
		}
		
		int bpp = imageData.depth >> 3;
		for (int i = 0; i < imageData.data.length; i += bpp) {
			imageData.data[i] = rgb[0];
			imageData.data[i + 1] = rgb[1];
			imageData.data[i + 2] = rgb[2];
		}
		if (imageData.alphaData != null) {
			Arrays.fill(imageData.alphaData, 0, imageData.alphaData.length, (byte)-1);
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#getBitmapOffset(int, int)
	 */
	@Override
	final public int getBitmapOffset(int x, int y) {
		return getLineStride() * (y) + x * (imageData.depth >> 3);
	}

	@Override
	final public int getPixelStride() {
		return imageData.depth >> 3;
	}

	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#syncColors()
	 */
	@Override
	public void syncColors() {
		super.syncColors();

		if (colorRGBMap == null) 
			colorRGBMap = new byte[16][];
		if (spriteColorRGBMap == null) 
			spriteColorRGBMap = new byte[16][];
		if (fourColorRGBMap == null) { 
			fourColorRGBMap = new byte[2][][];
			fourColorRGBMap[0] = new byte[16][];
			fourColorRGBMap[1] = new byte[16][];
		}
		
		for (int i = 0; i < 16; i++) {
			colorRGBMap[i] = getColorMgr().getRGB(colorMap[i]);
			spriteColorRGBMap[i] = getColorMgr().getRGB(spriteColorMap[i]);
			fourColorRGBMap[0][i] = getColorMgr().getRGB(fourColorMap[0][i]);
			fourColorRGBMap[1][i] = getColorMgr().getRGB(fourColorMap[1][i]);
		}

	}

	protected void drawEightPixels(int offs, byte mem, byte fg, byte bg) {
		byte[] fgRGB = colorRGBMap[fg];
		byte[] bgRGB = colorRGBMap[bg];
		for (int i = 0; i < 8; i++) {
			byte[] rgb = (mem & 0x80) != 0 ? fgRGB : bgRGB;
			imageData.data[offs] = rgb[0];
			imageData.data[offs + 1] = rgb[1];
			imageData.data[offs + 2] = rgb[2];
			mem <<= 1;
			offs += 3;
		}
	}

	protected void drawSixPixels(int offs, byte mem, byte fg, byte bg) {
		byte[] fgRGB = colorRGBMap[fg];
		byte[] bgRGB = colorRGBMap[bg];
		for (int i = 0; i < 6; i++) {
			byte[] rgb = (mem & 0x80) != 0 ? fgRGB : bgRGB;
			imageData.data[offs++] = rgb[0];
			imageData.data[offs++] = rgb[1];
			imageData.data[offs++] = rgb[2];
			mem <<= 1;
		}
	}

	public void drawEightSpritePixels(int x, int y, byte mem, byte fg, byte bitmask, boolean isLogicalOr) {
		int offs = getBitmapOffset(x, y);
		byte[] fgRGB = colorRGBMap[fg];
		for (int i = 0; i < 8; i++) {
			if ((mem & bitmask & 0x80) != 0) {
				imageData.data[offs] = fgRGB[0];
				imageData.data[offs + 1] = fgRGB[1];
				imageData.data[offs + 2] = fgRGB[2];
			}
			bitmask <<= 1;
			mem <<= 1;
			offs += 3;
		}
	}

	public void drawEightMagnifiedSpritePixels(int x, int y, byte mem_, byte fg, short bitmask, boolean isLogicalOr) {
		int offs = getBitmapOffset(x, y);
		byte[] fgRGB = colorRGBMap[fg];
		short mem = (short) (mem_ << 8);
		for (int i = 0; i < 8; i++) {
			if ((mem & bitmask & 0x8000) != 0) {
				imageData.data[offs] = fgRGB[0];
				imageData.data[offs + 1] = fgRGB[1];
				imageData.data[offs + 2] = fgRGB[2];
			}
			bitmask <<= 1;
			offs += 3;
			if ((mem & bitmask & 0x8000) != 0) {
				imageData.data[offs] = fgRGB[0];
				imageData.data[offs + 1] = fgRGB[1];
				imageData.data[offs + 2] = fgRGB[2];
			}
			bitmask <<= 1;
			offs += 3;
			mem <<= 1;
		}
	}

	public void drawEightDoubleMagnifiedSpritePixels(int x, int y, byte mem_, byte fg, short bitmask, boolean isLogicalOr) {
		int offs = getBitmapOffset(x, y);
		byte[] fgRGB = colorRGBMap[fg];
		short mem = (short) (mem_ << 8);
		for (int i = 0; i < 8; i++) {
			if ((mem & bitmask & 0x8000) != 0) {
				imageData.data[offs] = fgRGB[0];
				imageData.data[offs + 1] = fgRGB[1];
				imageData.data[offs + 2] = fgRGB[2];
				imageData.data[offs + 3] = fgRGB[0];
				imageData.data[offs + 4] = fgRGB[1];
				imageData.data[offs + 5] = fgRGB[2];
			}
			bitmask <<= 1;
			offs += 6;
			if ((mem & bitmask & 0x8000) != 0) {
				imageData.data[offs] = fgRGB[0];
				imageData.data[offs + 1] = fgRGB[1];
				imageData.data[offs + 2] = fgRGB[2];
				imageData.data[offs + 3] = fgRGB[0];
				imageData.data[offs + 4] = fgRGB[1];
				imageData.data[offs + 5] = fgRGB[2];
			}
			bitmask <<= 1;
			offs += 6;
			mem <<= 1;
		}
	}
	@Override
	public void draw8x8BitmapTwoColorBlock(int x, int y,
			ByteMemoryAccess access, int rowstride) {
		int lineStride = getLineStride();
		int offs = getBitmapOffset(x, y);
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 4; j++) {
				byte mem;
				
				byte pix;
				byte[] rgb;

				mem = access.memory[access.offset + j];

				pix = (byte) ((mem >> 4) & 0xf);
				rgb = colorRGBMap[pix];
				imageData.data[offs] = rgb[0];
				imageData.data[offs + 1] = rgb[1];
				imageData.data[offs + 2] = rgb[2];
				
				offs += 3;
				
				pix = (byte) (mem & 0xf);
				rgb = colorRGBMap[pix];
				imageData.data[offs] = rgb[0];
				imageData.data[offs + 1] = rgb[1];
				imageData.data[offs + 2] = rgb[2];
				
				offs += 3;
			}
			
			offs += lineStride - 6 * 4;
			access.offset += rowstride;
		}
	}
	
	@Override
	public void draw8x8BitmapFourColorBlock(int x, int y,
			ByteMemoryAccess access, int rowstride) {
		int lineStride = getLineStride();
		int offs = getBitmapOffset(x, y);
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 2; j++) {
				byte mem;
				
				byte pix;
				byte[] rgb;

				mem = access.memory[access.offset + j];

				pix = (byte) ((mem >> 6) & 0x3);
				rgb = fourColorRGBMap[0][pix];
				imageData.data[offs] = rgb[0];
				imageData.data[offs + 1] = rgb[1];
				imageData.data[offs + 2] = rgb[2];
				
				offs += 3;
				
				pix = (byte) ((mem >> 4) & 0x3);
				rgb = fourColorRGBMap[1][pix];
				imageData.data[offs] = rgb[0];
				imageData.data[offs + 1] = rgb[1];
				imageData.data[offs + 2] = rgb[2];
				
				offs += 3;
				
				pix = (byte) ((mem >> 2) & 0x3);
				rgb = fourColorRGBMap[0][pix];
				imageData.data[offs] = rgb[0];
				imageData.data[offs + 1] = rgb[1];
				imageData.data[offs + 2] = rgb[2];
				
				offs += 3;
				
				pix = (byte) (mem & 0x3);
				rgb = fourColorRGBMap[1][pix];
				imageData.data[offs] = rgb[0];
				imageData.data[offs + 1] = rgb[1];
				imageData.data[offs + 2] = rgb[2];
				
				offs += 3;
			}
			
			offs += lineStride - 6 * 4;
			access.offset += rowstride;
		}
	}
	
	@Override
	public void draw8x8BitmapRGB332ColorBlock(int x, int y,
			ByteMemoryAccess access, int rowstride) {
		int lineStride = getLineStride();
		int offs = getBitmapOffset(x, y);
		byte[] rgb = { 0, 0, 0 };
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				byte mem;
				
				mem = access.memory[access.offset + j];

				getColorMgr().getGRB332(rgb, mem);
				imageData.data[offs] = rgb[0];
				imageData.data[offs + 1] = rgb[1];
				imageData.data[offs + 2] = rgb[2];
				
				offs += 3;
			}
			
			offs += lineStride - 3 * 8;
			access.offset += rowstride;
		}
	}
	
	@Override
	public void blitSpriteBlock(Sprite2Canvas spriteCanvas, int x, int y,
			int blockMag) {
		int sprOffset = spriteCanvas.getBitmapOffset(x, y);
		int bitmapOffset = getBitmapOffset(x * blockMag, y);
		try {
			for (int i = 0; i < 8; i++) {
				for (int j = 0; j < 8; j++) {
					byte cl = spriteCanvas.getColorAtOffset(sprOffset + j);
					if (cl != 0) {
						byte[] rgb = spriteColorRGBMap[cl];
						imageData.data[bitmapOffset] = rgb[0];
						imageData.data[bitmapOffset + 1] = rgb[1];
						imageData.data[bitmapOffset + 2] = rgb[2];
						if (blockMag > 1 && bitmapOffset < imageData.data.length) {
							imageData.data[bitmapOffset + 3] = rgb[0];
							imageData.data[bitmapOffset + 4] = rgb[1];
							imageData.data[bitmapOffset + 5] = rgb[2];
						}
					}
					bitmapOffset += 3 * blockMag;
				}
				sprOffset += spriteCanvas.getLineStride();
				bitmapOffset += getLineStride() - 3 * 8 * blockMag;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// ignore
		}
	}
	
	@Override
	public void blitFourColorSpriteBlock(Sprite2Canvas spriteCanvas, int x, int y,
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
						byte[] rgb = spriteColorRGBMap[cl];
						imageData.data[bitmapOffset] = rgb[0];
						imageData.data[bitmapOffset + 1] = rgb[1];
						imageData.data[bitmapOffset + 2] = rgb[2];
					}
					bitmapOffset += 3;
					colorColumn ^= 1;
					//System.out.println(j+","+(j * blockMag + x + 1));
					if (blockMag > 1) {
						if (colorColumn == 0)
							cl = (byte) ((col & 0xc) >> 2);
						else
							cl = (byte) (col & 0x3);
						if (cl != 0) {
							byte[] rgb = getColorMgr().getSpriteRGB(cl);
							imageData.data[bitmapOffset] = rgb[0];
							imageData.data[bitmapOffset + 1] = rgb[1];
							imageData.data[bitmapOffset + 2] = rgb[2];
						}
						bitmapOffset += 3;
						colorColumn ^= 1;
					}
				}
				sprOffset += spriteCanvas.getLineStride();
				bitmapOffset += getLineStride() - 3 * 8 * blockMag;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// ignore
		}			
	}
}
