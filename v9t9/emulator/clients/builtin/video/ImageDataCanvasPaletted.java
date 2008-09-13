/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import java.util.Arrays;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

/**
 * Render video content into an ImageData using a palette
 * @author ejs
 *
 */
public class ImageDataCanvasPaletted extends ImageDataCanvas {
	
	@Override
	protected ImageData createImageData() {
		RGB[] colors = new RGB[16];
		for (int i = 0; i < 16; i++) {
			byte[] rgb = getColorRGB(i);
			colors[i] = new RGB(rgb[0] & 255, rgb[1] & 255, rgb[2] & 255);
		}
		PaletteData palette = new PaletteData(colors);
		return new ImageData(width, height, 8, palette);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#clear()
	 */
	@Override
	public void clear() {
		Arrays.fill(imageData.data, 0, imageData.data.length, (byte) clearColor);
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
	
	protected void drawEightSpritePixels(int offs, byte mem, byte fg) {
		for (int i = 0; i < 8; i++) {
			if ((mem & 0x80) != 0) {
				imageData.data[offs + i] = fg;
			}
			mem <<= 1;
		}
	}

	protected void drawEightMagnifiedSpritePixels(int offs, byte mem, byte fg) {
		int rowOffset = getLineStride();
		for (int i = 0; i < 8; i++) {
			if ((mem & 0x80) != 0) {
				imageData.data[offs + i * 2] = fg;
				imageData.data[offs + i * 2 + 1] = fg;
				imageData.data[offs + rowOffset + i * 2] = fg;
				imageData.data[offs + rowOffset + i * 2 + 1] = fg;
			}
			mem <<= 1;
		}
	}


	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#getBitmapOffset(int, int)
	 */
	@Override
	public int getBitmapOffset(int x, int y) {
		return imageData.bytesPerLine * y + (x);
	}

	public int getPixelStride() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#setColorAtOffset(int, byte)
	 */
	@Override
	public void setColorAtOffset(int offset, byte color) {
		imageData.data[offset] = color;
	}

}
