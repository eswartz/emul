/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import java.util.Arrays;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import v9t9.engine.memory.ByteMemoryAccess;

/**
 * Render video content into an ImageData using a palette
 * @author ejs
 *
 */
public class ImageDataCanvasPaletted extends ImageDataCanvas {
	
	public ImageDataCanvasPaletted() {
		super();
	}

	@Override
	protected ImageData createImageData() {
		RGB[] colors = new RGB[16];
		for (int i = 0; i < 16; i++) {
			byte[] rgb = getRGB(i);
			colors[i] = new RGB(rgb[0] & 255, rgb[1] & 255, rgb[2] & 255);
		}
		PaletteData palette = new PaletteData(colors);
		int allocHeight = height;
		if ((height & 7) != 0)
			allocHeight += 8;
		return new ImageData(width * (isInterlacedEvenOdd() ? 2 : 1), allocHeight, 8, palette);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#clear()
	 */
	@Override
	public void clear(byte[] rgb) {
		Arrays.fill(imageData.data, 0, imageData.data.length, (byte) getColorMgr().getClearColor());
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#getBitmapOffset(int, int)
	 */
	@Override
	public int getBitmapOffset(int x, int y) {
		return imageData.bytesPerLine * (y) + (x);
	}

	public int getPixelStride() {
		return 1;
	}

	protected void drawEightPixels(int offs, byte mem, byte fg, byte bg) {
		for (int i = 0; i < 8; i++) {
			byte color = (mem & 0x80) != 0 ? fg : bg;
			imageData.data[offs + i] = color;
			mem <<= 1;
		}
	}
	protected void drawSixPixels(int offs, byte mem, byte fg, byte bg) {
		for (int i = 0; i < 6; i++) {
			byte color = (mem & 0x80) != 0 ? fg : bg;
			imageData.data[offs + i] = color;
			mem <<= 1;
		}
	}
	
	public void drawEightSpritePixels(int x, int y, byte mem, byte fg, byte bitmask, boolean isLogicalOr) {
		int offs = getBitmapOffset(x, y);
		for (int i = 0; i < 8; i++) {
			if ((mem & bitmask & 0x80) != 0) {
				imageData.data[offs] = fg;
			}
			offs++;
			bitmask <<= 1;
			mem <<= 1;
		}
	}

	public void drawEightMagnifiedSpritePixels(int x, int y, byte mem_, byte fg, short bitmask, boolean isLogicalOr) {
		int offs = getBitmapOffset(x, y);
		short mem = (short) (mem_ << 8);
		for (int i = 0; i < 8; i++) {
			if ((mem & bitmask & 0x8000) != 0) {
				imageData.data[offs] = fg;
			}
			offs++;
			bitmask <<= 1;
			if ((mem & bitmask & 0x8000) != 0) {
				imageData.data[offs] = fg;
			}
			offs++;
			bitmask <<= 1;
			mem <<= 1;
		}
	}
	public void drawEightDoubleMagnifiedSpritePixels(int x, int y, byte mem_, byte fg, short bitmask, boolean isLogicalOr) {
		int offs = getBitmapOffset(x, y);
		short mem = (short) (mem_ << 8);
		for (int i = 0; i < 8; i++) {
			if ((mem & bitmask & 0x8000) != 0) {
				imageData.data[offs] = fg;
				imageData.data[offs + 1] = fg;
			}
			offs+=2;
			bitmask <<= 1;
			if ((mem & bitmask & 0x8000) != 0) {
				imageData.data[offs] = fg;
				imageData.data[offs + 1] = fg;
			}
			offs+=2;
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
				imageData.data[offs] = pix;
				
				pix = (byte) (mem & 0xf);
				imageData.data[offs + 1] = pix;
				
				offs += 2;
			}
			
			offs += lineStride - 2 * 4;
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
				imageData.data[offs] = fourColorMap[0][pix];
				
				pix = (byte) ((mem >> 4) & 0x3);
				imageData.data[offs + 1] = fourColorMap[1][pix];
				
				pix = (byte) ((mem >> 2) & 0x3);
				imageData.data[offs + 2] = fourColorMap[0][pix];
				
				pix = (byte) (mem & 0x3);
				imageData.data[offs + 3] = fourColorMap[1][pix];
				
				offs += 4;
			}
			
			offs += lineStride - 2 * 4;
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
				
				int r = ColorMapUtils.rgb3to8[(mem >> 5) & 0x7] & 0xff;
				int g = ColorMapUtils.rgb3to8[(mem >> 2) & 0x7] & 0xff;
				int b = ColorMapUtils.rgb2to8[mem & 0x3] & 0xff;
				
				// XXX: no palette
				byte pix = (byte) imageData.palette.getPixel(new RGB(r, g, b));
				imageData.data[offs++] = pix;
			}
			
			offs += lineStride - 8;
			access.offset += rowstride;
		}
	}
	
	@Override
	public void blitSpriteBlock(Sprite2Canvas spriteCanvas, int x, int y,
			int blockMag) {
		throw new IllegalArgumentException();
	}
	
	@Override
	public void blitFourColorSpriteBlock(Sprite2Canvas spriteCanvas, int x,
			int y, int blockMag) {
		throw new IllegalArgumentException();
	}
}
