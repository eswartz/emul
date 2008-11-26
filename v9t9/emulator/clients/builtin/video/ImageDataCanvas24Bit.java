/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import java.util.Arrays;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

import v9t9.engine.memory.ByteMemoryAccess;

/**
 * Render video content into an ImageData
 * @author ejs
 *
 */
public class ImageDataCanvas24Bit extends ImageDataCanvas {
	public ImageDataCanvas24Bit() {
		setSize(256, 192);
	}
	
	@Override
	protected ImageData createImageData() {
		PaletteData palette = new PaletteData(0xFF0000, 0xFF00, 0xFF);
		int allocHeight = height;
		if ((height & 7) != 0)
			allocHeight += 8;
		return new ImageData(width, allocHeight, 24, palette);
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#clear()
	 */
	@Override
	public void clear() {
		byte[] rgb = getRGB(clearColor);
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
	public int getBitmapOffset(int x, int y) {
		return imageData.bytesPerLine * (y) + (x) * (imageData.depth >> 3);
	}

	@Override
	public int getPixelStride() {
		return imageData.depth >> 3;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#setColorAtOffset(int, byte)
	 */
	@Override
	public void setColorAtOffset(int offset, byte color) {
		/*
		byte alpha = -1;
		if (color == 0 && clearColor == 0)
			alpha = 0;
		*/
		byte[] rgb = getRGB(color);
		imageData.data[offset] = rgb[0];
		imageData.data[offset + 1] = rgb[1];
		imageData.data[offset + 2] = rgb[2];
	}

	protected void drawEightPixels(int offs, byte mem, byte fg, byte bg) {
		byte[] fgRGB = getRGB(fg);
		byte[] bgRGB = getRGB(bg);
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
		byte[] fgRGB = getRGB(fg);
		byte[] bgRGB = getRGB(bg);
		for (int i = 0; i < 6; i++) {
			byte[] rgb = (mem & 0x80) != 0 ? fgRGB : bgRGB;
			imageData.data[offs++] = rgb[0];
			imageData.data[offs++] = rgb[1];
			imageData.data[offs++] = rgb[2];
			mem <<= 1;
		}
	}

	protected void drawEightSpritePixels(int offs, byte mem, byte fg, byte bitmask) {
		byte[] fgRGB = getRGB(fg);
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

	protected void drawEightMagnifiedSpritePixels(int offs, byte mem_, byte fg, short bitmask) {
		byte[] fgRGB = getRGB(fg);
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

	@Override
	public void draw8x8BitmapTwoColorBlock(int offs,
			ByteMemoryAccess access, int rowstride) {
		int lineStride = getLineStride();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 4; j++) {
				byte mem;
				
				byte pix;
				byte[] rgb;

				mem = access.memory[access.offset + j];

				pix = (byte) ((mem >> 4) & 0xf);
				rgb = getRGB(pix);
				imageData.data[offs] = rgb[0];
				imageData.data[offs + 1] = rgb[1];
				imageData.data[offs + 2] = rgb[2];
				
				offs += 3;
				
				pix = (byte) (mem & 0xf);
				rgb = getRGB(pix);
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
	public void draw8x8BitmapFourColorBlock(int offs,
			ByteMemoryAccess access, int rowstride) {
		int lineStride = getLineStride();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 2; j++) {
				byte mem;
				
				byte pix;
				byte[] rgb;

				mem = access.memory[access.offset + j];

				pix = (byte) ((mem >> 6) & 0x3);
				rgb = getRGB(pix);
				imageData.data[offs] = rgb[0];
				imageData.data[offs + 1] = rgb[1];
				imageData.data[offs + 2] = rgb[2];
				
				offs += 3;
				
				pix = (byte) ((mem >> 4) & 0x3);
				rgb = getRGB(pix);
				imageData.data[offs] = rgb[0];
				imageData.data[offs + 1] = rgb[1];
				imageData.data[offs + 2] = rgb[2];
				
				offs += 3;
				
				pix = (byte) ((mem >> 2) & 0x3);
				rgb = getRGB(pix);
				imageData.data[offs] = rgb[0];
				imageData.data[offs + 1] = rgb[1];
				imageData.data[offs + 2] = rgb[2];
				
				offs += 3;
				
				pix = (byte) (mem & 0x3);
				rgb = getRGB(pix);
				imageData.data[offs] = rgb[0];
				imageData.data[offs + 1] = rgb[1];
				imageData.data[offs + 2] = rgb[2];
				
				offs += 3;
			}
			
			offs += lineStride - 6 * 4;
			access.offset += rowstride;
		}
	}
}
