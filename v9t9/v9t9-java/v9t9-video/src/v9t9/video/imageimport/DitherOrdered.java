/*
  DitherOrdered.java

  (c) 2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.imageimport;

import java.awt.image.BufferedImage;

import org.ejs.gui.images.ColorMapUtils;
import org.ejs.gui.images.IPaletteColorMapper;
import org.ejs.gui.images.IPaletteMapper;

/**
 * @author ejs
 *
 */
public class DitherOrdered implements IDither {

	/**
	 * @param thePalette
	 */
	public DitherOrdered() {
	}


	// http://en.wikipedia.org/wiki/Ordered_dithering
	// http://upload.wikimedia.org/wikipedia/en/math/5/3/1/531fd7f88bac5f6482c465d1de15e16f.png
	
	final static byte[][] thresholdMap8x8 = {
		{  1, 49, 13, 61,  4, 52, 16, 61 },
		{ 33, 17, 45, 29, 36, 20, 48, 32 },
		{  9, 57,  5, 53, 12, 60,  8, 56 },
		{ 41, 25, 37, 21, 44, 28, 40, 24 },
		{  3, 51, 15, 63,  2, 50, 14, 62 },
		{ 35, 19, 47, 31, 34, 18, 46, 30 },
		{ 11, 59,  7, 55, 10, 58,  6, 54 },
		{ 43, 27, 39, 23, 42, 26, 38, 22 }
	};

	private void ditherOrderedPixel(BufferedImage img, IPaletteColorMapper mapColor,
			int x, int y, int[] prgb) {

		int pixel = img.getRGB(x, y);
		ColorMapUtils.pixelToRGB(pixel, prgb);

		if (true) {
			int threshold = ((byte) (thresholdMap8x8[x & 7][y & 7] << 2)) >> 2;
			prgb[0] = (prgb[0] + threshold);
			prgb[1] = (prgb[1] + threshold);
			prgb[2] = (prgb[2] + threshold);
		} else {
			int threshold = thresholdMap8x8[x & 7][y & 7];
			prgb[0] = (prgb[0] + threshold - 32);
			prgb[1] = (prgb[1] + threshold - 32);
			prgb[2] = (prgb[2] + threshold - 32);
		}
		
		int newC = mapColor.getClosestPaletteEntry(ColorMapUtils.rgb8ToPixel(prgb));
		
		int newPixel = mapColor.getPalettePixel(newC);
		
		if (pixel != newPixel)
			img.setRGB(x, y, newPixel | 0xff000000);
	}
	
	
	/* (non-Javadoc)
	 * @see v9t9.video.imageimport.IDither#run(java.awt.image.BufferedImage, org.ejs.gui.images.IPaletteMapper, org.ejs.gui.images.Histogram)
	 */
	@Override
	public void run(BufferedImage img, IPaletteMapper mapColor) {
		int h = img.getHeight();
		int w = img.getWidth();

		int[] prgb = { 0, 0, 0 };
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				ditherOrderedPixel(img, mapColor, x, y, prgb);
			}
		}

	}

}
