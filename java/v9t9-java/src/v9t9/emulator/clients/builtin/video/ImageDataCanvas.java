package v9t9.emulator.clients.builtin.video;

import java.awt.image.BufferedImage;

import org.eclipse.swt.graphics.ImageData;

public abstract class ImageDataCanvas extends VdpCanvas {

	protected ImageData imageData;

	public ImageDataCanvas(int extraSpace) {
		super(extraSpace);
	}

	public ImageData getImageData() {
		return imageData;
	}

	@Override
	final public int getLineStride() {
		return bytesPerLine;
	}



	@Override
	public void doChangeSize() {
		imageData = createImageData();
	}

	abstract protected ImageData createImageData();

	public int getDisplayAdjustOffset() {
		int displayAdjust = getYOffset() * getLineStride() + (getXOffset() + this.extraSpace / 2) * getPixelStride();
		return displayAdjust;
	}

	/**
	 * @param scaled
	 */
	public void setImageData(BufferedImage img) {
		if (format == null || format == Format.TEXT || format == Format.COLOR16_8x8)
			return;

		int ncols;
		if (format == Format.COLOR16_1x1) {
			ncols = 16;
		}
		else if (format == Format.COLOR4_1x1) {
			ncols = 4;
		}
		else if (format == Format.COLOR256_1x1) {
			ncols = 256;
		}
		else {
			return;
		}
			
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int pixel = img.getRGB(x, y);
				int newPixel;
				int g = (pixel & 0xff00) >> 8;
				int r = (pixel & 0xff0000) >> 16;
				int b = pixel & 0xff;
				byte[] rgb = { 0, 0, 0};
				if (format == Format.COLOR256_1x1) {
					rgb = getGRB333(Math.min((g + 16) >> 5, 7), Math.min((r + 16) >> 5, 7),  Math.min((b + 32) >> 6, 3));
					newPixel = ((rgb[0] & 0xff) << 16) | ((rgb[1] & 0xff) << 8) | (rgb[2] & 0xff);
				}
				else {
					int closest = -1;
					int mindiff = Integer.MAX_VALUE;
					for (int c = 0; c < ncols; c++) {
						int dr = ((thePalette[c][0] & 0xff) - r);
						int dg = ((thePalette[c][1] & 0xff) - g);
						int db = ((thePalette[c][2] & 0xff) - b);
						int dist = (dr * dr) + (dg * dg) + (db * db);
						if (dist < mindiff) {
							closest = c;
							mindiff = dist;
						}
					}
					rgb = thePalette[closest];
					newPixel = ((rgb[0] & 0xff) << 16) | ((rgb[1] & 0xff) << 8) | (rgb[2] & 0xff);
				}
				imageData.setPixel(x, y, newPixel);
			}
		}
	}

}