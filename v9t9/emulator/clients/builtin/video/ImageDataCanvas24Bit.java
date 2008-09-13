/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import java.util.Arrays;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

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
		return new ImageData(width, height, 24, palette);
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#clear()
	 */
	@Override
	public void clear() {
		byte[] rgb = getColorRGB(clearColor);
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

	protected void drawEightPixels(int offs, byte mem, byte fg, byte bg) {
		byte[] fgRGB = getColorRGB(fg);
		byte[] bgRGB = getColorRGB(bg);
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
		byte[] fgRGB = getColorRGB(fg);
		byte[] bgRGB = getColorRGB(bg);
		for (int i = 0; i < 6; i++) {
			byte[] rgb = (mem & 0x80) != 0 ? fgRGB : bgRGB;
			imageData.data[offs++] = rgb[0];
			imageData.data[offs++] = rgb[1];
			imageData.data[offs++] = rgb[2];
			mem <<= 1;
		}
	}

	protected void drawEightSpritePixels(int offs, byte mem, byte fg) {
		byte[] fgRGB = getColorRGB(fg);
		for (int i = 0; i < 8; i++) {
			if ((mem & 0x80) != 0) {
				imageData.data[offs] = fgRGB[0];
				imageData.data[offs + 1] = fgRGB[1];
				imageData.data[offs + 2] = fgRGB[2];
			}
			mem <<= 1;
			offs += 3;
		}
	}

	protected void drawEightMagnifiedSpritePixels(int offs, byte mem, byte fg) {
		byte[] fgRGB = getColorRGB(fg);
		int rowOffset = getLineStride();
		for (int i = 0; i < 8; i++) {
			if ((mem & 0x80) != 0) {
				imageData.data[offs] = fgRGB[0];
				imageData.data[offs + 1] = fgRGB[1];
				imageData.data[offs + 2] = fgRGB[2];
				imageData.data[offs + 3] = fgRGB[0];
				imageData.data[offs + 4] = fgRGB[1];
				imageData.data[offs + 5] = fgRGB[2];
				imageData.data[offs + rowOffset] = fgRGB[0];
				imageData.data[offs + rowOffset + 1] = fgRGB[1];
				imageData.data[offs + rowOffset + 2] = fgRGB[2];
				imageData.data[offs + rowOffset + 3] = fgRGB[0];
				imageData.data[offs + rowOffset + 4] = fgRGB[1];
				imageData.data[offs + rowOffset + 5] = fgRGB[2];
			}
			mem <<= 1;
			offs += 3;
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#getBitmapOffset(int, int)
	 */
	@Override
	public int getBitmapOffset(int x, int y) {
		return imageData.bytesPerLine * y + (x) * (imageData.depth >> 3);
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
		byte[] rgb = getColorRGB(color);
		imageData.data[offset] = rgb[0];
		imageData.data[offset + 1] = rgb[1];
		imageData.data[offset + 2] = rgb[2];
	}

}
