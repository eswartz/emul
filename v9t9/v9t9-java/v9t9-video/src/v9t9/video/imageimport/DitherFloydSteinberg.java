/**
 * 
 */
package v9t9.video.imageimport;

import java.awt.image.BufferedImage;

import org.ejs.gui.images.IPaletteColorMapper;
import org.ejs.gui.images.IPaletteMapper;

import v9t9.video.imageimport.ImageImportOptions.Dither;

/**
 * @author ejs
 *
 */
public class DitherFloydSteinberg implements IDither {

	private Dither ditherType;
	private boolean useColorMappedGreyScale;


	/**
	 * @param thePalette
	 * @param ditherType
	 */
	public DitherFloydSteinberg(boolean useColorMappedGreyScale, Dither ditherType) {
		this.useColorMappedGreyScale = useColorMappedGreyScale;
		this.ditherType = ditherType;
	}

	private final int clamp(int i) {
		return i < 0 ? 0 : i > 255 ? 255 : i;
	}

	// https://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering
	private void ditherFSPixel(BufferedImage img, IPaletteColorMapper mapColor,
			int x, int y) {
		
		int pixel = img.getRGB(x, y);

		int newC = mapColor.getClosestPaletteEntry(pixel);
		
		int newPixel = mapColor.getPalettePixel(newC);
		
		img.setRGB(x, y, newPixel | 0xff000000);
		
		int r_error;
		int g_error;
		int b_error;
		
		r_error = ((pixel >> 16) & 0xff) - ((newPixel >> 16) & 0xff);
		g_error = ((pixel >> 8) & 0xff) - ((newPixel >> 8) & 0xff);
		b_error = ((pixel >> 0) & 0xff) - ((newPixel >> 0) & 0xff);
		
		if (ditherType == Dither.FSR) {
			// reduce bleed by ignoring some error
			r_error = reduceBleed(r_error);
			g_error = reduceBleed(g_error);
			b_error = reduceBleed(b_error);
		}

		if (useColorMappedGreyScale) {
			int lum = (299 * r_error + 587 * g_error + 114 * b_error) / 1000;
			r_error = g_error = b_error = lum;
		}
		
		if ((r_error | g_error | b_error) != 0) {
			if (x + 1 < img.getWidth()) {
				// x+1, y
				ditherFSApplyError(img, x + 1, y,  
						7, r_error, g_error, b_error);
			}
			if (y + 1 < img.getHeight()) {
				if (x > 0) {
					ditherFSApplyError(img, x - 1, y + 1, 
							3, r_error, g_error, b_error);
				}
				ditherFSApplyError(img, x, y + 1, 
						5, r_error, g_error, b_error);
				if (x + 1 < img.getWidth()) {
					ditherFSApplyError(img, x + 1, y + 1, 
							1, r_error, g_error, b_error);
				}
			}
		}
	}
	
	/**
	 * @param b_error
	 * @return
	 */
	private int reduceBleed(int v) {
		if (v < 0)
			return -(-v / 3);
		else
			return v / 3;
	}
	
	private void ditherFSApplyError(BufferedImage img, int x, int y, int sixteenths, int r_error, int g_error, int b_error) {
		int pixel = img.getRGB(x, y);
		int r = clamp(((pixel >> 16) & 0xff) + (sixteenths * r_error / 16));
		int g = clamp(((pixel >> 8) & 0xff) + (sixteenths * g_error / 16));
		int b = clamp(((pixel >> 0) & 0xff) + (sixteenths * b_error / 16));
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
				ditherFSPixel(img, mapColor, x, y);
			}
		}
		
	}

}
