/**
 * 
 */
package v9t9.video.imageimport;

import java.awt.image.BufferedImage;

import org.ejs.gui.images.IPaletteMapper;

/**
 * @author ejs
 *
 */
public class DitherNone implements IDither {

	/* (non-Javadoc)
	 * @see v9t9.video.imageimport.IDither#run(java.awt.image.BufferedImage, org.ejs.gui.images.IPaletteMapper, org.ejs.gui.images.Histogram)
	 */
	@Override
	public void run(BufferedImage img, IPaletteMapper mapColor) {
		int w = img.getWidth();
		int h = img.getHeight();
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int pixel = img.getRGB(x, y);
				int newC = mapColor.getClosestPaletteEntry(x, y, pixel);
				int newPixel;
				newPixel = mapColor.getPalettePixel(newC);
				img.setRGB(x, y, newPixel | 0xff000000);
			}
		}
	}

}
