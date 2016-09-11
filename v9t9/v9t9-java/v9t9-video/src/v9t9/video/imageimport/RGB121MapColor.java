/*
  RGB121MapColor.java

  (c) 2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.imageimport;

import org.ejs.gui.images.ColorMapUtils;
import org.ejs.gui.images.V99ColorMapUtils;

class RGB121MapColor extends RGBDirectMapColor {

	public RGB121MapColor(boolean isGreyscale) {
		super(createStock121Palette(), 0, 32, isGreyscale);
	}
	
	private static byte[][] createStock121Palette() {
		byte[][] pal = new byte[32][];
		for (int i = 0; i < 32; i++) {
			 byte[] rgb = { 0, 0, 0 };
			 V99ColorMapUtils.getGRB211(rgb, (byte) i, false);
			 pal[i] = rgb;
		}
		return pal;
	}


	protected byte[] getRGB121(int r, int g, int b) {
		byte[] rgbs = V99ColorMapUtils.getGRB211(g, r, b);
		if (isColorMappedGreyscale) {
			// (299 * rgb[0] + 587 * rgb[1] + 114 * rgb[2]) * 256 / 1000;
			rgbs = V99ColorMapUtils.getRgbToGreyForGreyscaleMode(rgbs);
		}
			
		return rgbs;
	}
	
	@Override
	public int mapColor(int pixel, int[] dist) {
		int r = ((pixel & 0xff0000) >>> 16) >>> 7;
		int g = ((pixel & 0x00ff00) >>>  8) >>> 6;
		int b = ((pixel & 0x0000ff) >>>  0) >>> 7;
		
		byte[] rgbs = getRGB121(r, g, b);
		
		if (isColorMappedGreyscale)
			dist[0] = ColorMapUtils.getRGBLumDistance(rgbs, pixel);
		else
			dist[0] = ColorMapUtils.getRGBDistance(rgbs, pixel);

//		if (dist[0] >= getMinimalPaletteDistance()) {
//			return -1;
//		}
		
		int c = (g << 2) | (r << 1) | b;
		return c;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestPaletteColor(int[])
	 */
	@Override
	public int getClosestPalettePixel(int x, int y, int pixel) {
		// we don't need to trawl the palette here
		int r = ((pixel & 0xff0000) >>> 16) >> 7;
		int g = ((pixel & 0x00ff00) >>>  8) >> 6;
		int b = ((pixel & 0x0000ff) >>>  0) >> 7;
		byte[] rgb = getRGB121(r, g, b);
		return ColorMapUtils.rgb8ToPixel(rgb);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestPaletteColor(int[])
	 */
	@Override
	public int getClosestPaletteEntry(int x, int y, int pixel) {
		// we don't need to trawl the palette here
		if (isColorMappedGreyscale) {
			pixel = V99ColorMapUtils.getPixelForGreyscaleMode(pixel);
		}
		
		int r = ((pixel & 0xff0000) >>> 16) >>> 7;
		int g = ((pixel & 0x00ff00) >>>  8) >>> 6;
		int b = ((pixel & 0x0000ff) >>>  0) >>> 7;
		return (g << 3) | (r << 1) | b;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.image.BasePaletteMapper#getMinimalPaletteDistance()
	 */
	@Override
	public int getMinimalPaletteDistance() {
		return 0x40 * 0x40 + 0x80 * 0x80 * 2;
	}
}