/**
 * 
 */
package v9t9.video.imageimport;

import java.awt.image.BufferedImage;

import org.ejs.gui.images.ColorMapUtils;
import org.ejs.gui.images.IPaletteColorMapper;
import org.ejs.gui.images.IPaletteMapper;

import v9t9.video.imageimport.ImageImportOptions.Dither;

/**
 * @author ejs
 *
 */
public class DitherFloydSteinbergMono implements IDither {

	private byte[][] thePalette;
	private Dither ditherType;

	public DitherFloydSteinbergMono(byte[][] thePalette, Dither ditherType) {
		this.thePalette = thePalette;
		this.ditherType = ditherType;
		
	}
	private void ditherFSPixelMono(BufferedImage img, IPaletteColorMapper mapColor,
			int x, int y) {
		
		int pixel = img.getRGB(x, y);
		int lum = ColorMapUtils.getPixelLum(pixel);

		int newC = mapColor.getClosestPaletteEntry(pixel);
		int newPixel = ColorMapUtils.rgb8ToPixel(thePalette[newC]); 
		int newLum = ColorMapUtils.getPixelLum(newPixel);

		img.setRGB(x, y, newPixel | 0xff000000);
		
		int error = lum - newLum;

		if (ditherType == Dither.FSR) {
			// reduce bleed by ignoring some error
			error = reduceBleed(error);
		}

		if (error != 0) {
			if (x + 1 < img.getWidth()) {
				// x+1, y
				ditherFSMonoApplyError(img, x + 1, y, 7, error);
			}
			if (y + 1 < img.getHeight()) {
				if (x > 0) {
					ditherFSMonoApplyError(img, x - 1, y + 1, 3, error);
				}
				ditherFSMonoApplyError(img, x, y + 1, 5, error);
				if (x + 1 < img.getWidth()) {
					ditherFSMonoApplyError(img, x + 1, y + 1, 1, error); 
				}
			}
		}
	}
	
	private int reduceBleed(int v) {
		if (v < 0)
			return -(-v / 3);
		else
			return v / 3;
	}
	
	private final int clamp(int i) {
		return i < 0 ? 0 : i > 255 ? 255 : i;
	}

	
	private void ditherFSMonoApplyError(BufferedImage img, int x, int y, int sixteenths, int error) {
		int offs = sixteenths * error / 16;
		if (offs == 0)
			return;

		int pixel = img.getRGB(x, y);

		int r = clamp(((pixel >> 16) & 0xff) + offs);
		int g = clamp(((pixel >> 8) & 0xff) + offs);
		int b = clamp(((pixel >> 0) & 0xff) + offs);
		img.setRGB(x, y, (r << 16) | (g << 8) | b | 0xff000000);
	
	}

	/* (non-Javadoc)
	 * @see v9t9.video.imageimport.IDither#run(java.awt.image.BufferedImage, org.ejs.gui.images.IPaletteMapper, org.ejs.gui.images.Histogram)
	 */
	@Override
	public void run(BufferedImage img, IPaletteMapper mapColor) {
		int h = img.getHeight();
		int w = img.getWidth();
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				ditherFSPixelMono(img, mapColor, x, y);
			}
		}
		
	}

}
