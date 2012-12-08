/**
 * 
 */
package v9t9.video;

import java.util.Arrays;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.ejs.gui.images.V99ColorMapUtils;

import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.video.ISpriteVdpCanvas;
import v9t9.common.video.VdpFormat;

/**
 * Render video content into an ImageData
 * @author ejs
 *
 */
public class ImageDataCanvasR3G3B2 extends ImageDataCanvas {
	private static PaletteData stockPaletteData = new PaletteData(0xFF0000, 0xFF00, 0xFF);


	protected byte[] colorRGBMap;
	protected byte[] spriteColorRGBMap;

	private byte[][] fourColorRGBMap;
	
	public ImageDataCanvasR3G3B2() {
		super();
		setSize(256, 192);
	}
	
	protected PaletteData getPaletteData() {
		return stockPaletteData;
	}

	@Override
	protected void createImageData() {
		int allocHeight = height;
		if ((height & 7) != 0)
			allocHeight += 8;
		
		imageData = new ImageData(width, allocHeight * (isInterlacedEvenOdd() ? 2 : 1), 8, getPaletteData());
		if (isInterlacedEvenOdd())
			bytesPerLine = imageData.bytesPerLine * 2;
		else
			bytesPerLine = imageData.bytesPerLine;
		
		pixSize = 1; //(imageData.depth >> 3);
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
		
		Arrays.fill(imageData.data, col);
		if (imageData.alphaData != null) {
			Arrays.fill(imageData.alphaData, 0, imageData.alphaData.length, (byte)-1);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#clear()
	 */
	@Override
	public void clearToEvenOddClearColors() {
		byte cc = colorRGBMap[getColorMgr().getClearColor()];
		byte cc1 = colorRGBMap[getColorMgr().getClearColor1()];
		
		if (colorRGBMap == null)
			return;
		
		byte fgRGB = colorRGBMap[cc];
		byte bgRGB = colorRGBMap[cc1];

		for (int i = 0; i < imageData.data.length; i += 2) {
			imageData.data[i ] = fgRGB;
			imageData.data[i + 1] = bgRGB;
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
		return getLineStride() * (y) + x * pixSize;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#syncColors()
	 */
	@Override
	public void syncColors() {
		super.syncColors();
		
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
	}

	protected void drawEightPixels(int offs, byte mem, byte fg, byte bg) {
		byte fgRGB = colorRGBMap[fg];
		byte bgRGB = colorRGBMap[bg];
		for (int i = 0; i < 8; i++) {
			byte rgb = (mem & 0x80) != 0 ? fgRGB : bgRGB;
			imageData.data[offs + i] = rgb;
			mem <<= 1;
		}
	}

	protected void drawSixPixels(int offs, byte mem, byte fg, byte bg) {
		byte fgRGB = colorRGBMap[fg];
		byte bgRGB = colorRGBMap[bg];
		for (int i = 0; i < 6; i++) {
			byte rgb = (mem & 0x80) != 0 ? fgRGB : bgRGB;
			imageData.data[offs + i] = rgb;
			mem <<= 1;
		}
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
				imageData.data[ioffs] = fgRGB;
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
			int ioffs = offs + i;
			if (ioffs >= endOffs)
				break;
			if ((mem & bitmask & 0x8000) != 0) {
				imageData.data[ioffs] = fgRGB;
			}
			bitmask <<= 1;
			if (ioffs + 1 >= endOffs)
				break;
			if ((mem & bitmask & 0x8000) != 0) {
				imageData.data[ioffs + 1] = fgRGB;
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
				imageData.data[ioffs] = fgRGB;
				if (ioffs + 1 >= endOffs)
					return;
				imageData.data[ioffs + 1] = fgRGB;
			}
			bitmask <<= 1;
			if (ioffs + 2 >= endOffs)
				break;
			if ((mem & bitmask & 0x8000) != 0) {
				imageData.data[ioffs + 2] = fgRGB;
				if (ioffs + 3 >= endOffs)
					return;
				imageData.data[ioffs + 3] = fgRGB;
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
			for (int j = 0; j < 4; j++) {
				byte mem;
				
				byte pix;

				mem = access.memory[access.offset + j];

				pix = (byte) ((mem >> 4) & 0xf);
				imageData.data[offs + j] = colorRGBMap[pix];
				
				pix = (byte) (mem & 0xf);
				imageData.data[offs + j] = colorRGBMap[pix];
			}
			
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
			for (int j = 0; j < 2; j++) {
				byte mem;
				
				byte pix;

				mem = access.memory[access.offset + j];

				pix = (byte) ((mem >> 6) & 0x3);
				imageData.data[offs] = colorRGBMap[pix];
				
				pix = (byte) ((mem >> 4) & 0x3);
				imageData.data[offs + 1] = colorRGBMap[pix];
				
				pix = (byte) ((mem >> 2) & 0x3);
				imageData.data[offs + 2] = colorRGBMap[pix];
				
				pix = (byte) (mem & 0x3);
				imageData.data[offs + 3] = colorRGBMap[pix];
			}
			
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
			for (int j = 0; j < 8; j++) {
				byte mem;
				
				mem = access.memory[access.offset + j];
				imageData.data[offs + j] = mem;
			}
			
			offs += lineStride - 8;
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
						imageData.data[bitmapOffset] = rgb;
						if (blockMag > 1 && bitmapOffset + 1 < imageData.data.length) {
							imageData.data[bitmapOffset + 1] = rgb;
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
						imageData.data[bitmapOffset] = rgb;
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
							imageData.data[bitmapOffset] = rgb;
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
}
