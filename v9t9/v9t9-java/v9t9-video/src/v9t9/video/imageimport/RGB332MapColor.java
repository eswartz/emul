/*
  RGB332MapColor.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.imageimport;

import java.util.TreeMap;

import org.ejs.gui.images.ColorMapUtils;
import org.ejs.gui.images.V99ColorMapUtils;

class RGB332MapColor extends RGBDirectMapColor {

	public RGB332MapColor(boolean isGreyscale) {
		super(createStock332Palette(), 0, 256, isGreyscale);
	}
	
	private static byte[][] createStock332Palette() {
		byte[][] pal = new byte[256][];
		for (int i = 0; i < 256; i++) {
			 byte[] rgb = { 0, 0, 0 };
			 V99ColorMapUtils.getGRB332(rgb, (byte) i, false);
			 pal[i] = rgb;
		}
		return pal;
	}


	protected byte[] getRGB332(int r, int g, int b) {
		byte[] rgbs = V99ColorMapUtils.getGRB332(g, r, b);
		if (isColorMappedGreyscale) {
			rgbs = getRgbToGreyForGreyscaleMode(rgbs);
		}
			
		return rgbs;
	}
	
	@Override
	public int mapColor(int pixel, int[] dist) {
		int r = ((pixel & 0xff0000) >>> 16) >>> 5;
		int g = ((pixel & 0x00ff00) >>>  8) >>> 5;
		int b = ((pixel & 0x0000ff) >>>  0) >>> 6;
		
		byte[] rgbs = getRGB332(r, g, b);
		int c = (g << 5) | (r << 2) | b;
		
		if (isColorMappedGreyscale)
			dist[0] = ColorMapUtils.getRGBLumDistance(rgbs, pixel);
		else
			dist[0] = ColorMapUtils.getRGBDistance(rgbs, pixel);

		return c;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestPaletteColor(int[])
	 */
	@Override
	public int getClosestPaletteEntry(int pixel) {
		// we don't need to trawl the palette here
		int r = ((pixel & 0xff0000) >>> 16) >>> 5;
		int g = ((pixel & 0x00ff00) >>>  8) >>> 5;
		int b = ((pixel & 0x0000ff) >>>  0) >>> 6;
		return (g << 5) | (r << 2) | b;
	}
	@Override
	public int getClosestPalettePixel(int x, int y, int pixel) {
		// we don't need to trawl the palette here
		int r = ((pixel & 0xff0000) >>> 16) >> 5;
		int g = ((pixel & 0x00ff00) >>>  8) >> 5;
		int b = ((pixel & 0x0000ff) >>>  0) >> 6;
		byte[] rgb = getRGB332(r, g, b);
		return ColorMapUtils.rgb8ToPixel(rgb);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.image.BasePaletteMapper#getMinimalPaletteDistance()
	 */
	@Override
	public int getMinimalPaletteDistance() {
		return 0x20 * 0x20 * 2 + 0x10 * 0x10;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.gui.images.IPaletteMapper#getGreyToRgbMap()
	 */
	@Override
	public TreeMap<Integer, byte[]> getGreyToRgbMap() {
		return V99ColorMapUtils.getGreyToRgbMap332();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.gui.images.IDirectColorMapper#mapDirectColor(byte[], byte)
	 */
	@Override
	public void mapDirectColor(byte[] rgb, byte c) {
		V99ColorMapUtils.getGRB332(rgb, (byte) c, false);
	}
}