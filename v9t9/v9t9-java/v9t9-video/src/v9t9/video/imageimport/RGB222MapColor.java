/*
  RGB222MapColor.java

  (c) 2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.imageimport;

import java.util.TreeMap;

import org.ejs.gui.images.ColorMapUtils;
import org.ejs.gui.images.V99ColorMapUtils;

class RGB222MapColor extends RGBDirectMapColor {

	static private TreeMap<Integer, byte[]> map;

	public RGB222MapColor(boolean isGreyscale) {
		super(createStock222Palette(), 0, 64, isGreyscale);
	}
	
	/** Get the 8-bit RGB values from a packed 2-2-2 GRB byte */
	private static void getGRB222(byte[] rgb, byte grb, boolean isGreyscale) {
		int g = (grb >> 4) & 0x3;
		int r = (grb >> 2) & 0x3;
		int b = grb & 0x3;
		rgb[0] = V99ColorMapUtils.rgb2to8[r];
		rgb[1] = V99ColorMapUtils.rgb2to8[g];
		rgb[2] = V99ColorMapUtils.rgb2to8[b];
		if (isGreyscale) {
			ColorMapUtils.rgbToGrey(rgb, rgb);
		}
	}
	
	private static byte[][] createStock222Palette() {
		byte[][] pal = new byte[64][];
		for (int i = 0; i < 64; i++) {
			 byte[] rgb = { 0, 0, 0 };
			 getGRB222(rgb, (byte) i, false);
			 pal[i] = rgb;
		}
		return pal;
	}
	private byte[] getGRB222(int g, int r, int b) {
		return new byte[] { V99ColorMapUtils.rgb2to8[r & 0x3],
				V99ColorMapUtils.rgb2to8[g & 0x3],
				V99ColorMapUtils.rgb2to8[b & 0x3] };
	}
	protected byte[] getRGB222(int r, int g, int b) {
		byte[] rgbs = getGRB222(g, r, b);
		if (isColorMappedGreyscale) {
			rgbs = getRgbToGreyForGreyscaleMode(rgbs);
		}
			
		return rgbs;
	}
	
	@Override
	public int mapColor(int pixel, int[] dist) {
		int r = ((pixel & 0xff0000) >>> 16) >>> 6;
		int g = ((pixel & 0x00ff00) >>>  8) >>> 6;
		int b = ((pixel & 0x0000ff) >>>  0) >>> 6;
		
		byte[] rgbs = getRGB222(r, g, b);
		
		if (isColorMappedGreyscale)
			dist[0] = ColorMapUtils.getRGBLumDistance(rgbs, pixel);
		else
			dist[0] = ColorMapUtils.getRGBDistance(rgbs, pixel);

		int c = (g << 4) | (r << 2) | b;
		return c;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestPaletteColor(int[])
	 */
	@Override
	public int getClosestPaletteEntry(int pixel) {
		// we don't need to trawl the palette here
		if (isColorMappedGreyscale) {
			pixel = getPixelForGreyscaleMode(pixel);
		}
		
		int r = ((pixel & 0xff0000) >>> 16) >>> 6;
		int g = ((pixel & 0x00ff00) >>>  8) >>> 6;
		int b = ((pixel & 0x0000ff) >>>  0) >>> 6;
		return (g << 4) | (r << 2) | b;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.image.BasePaletteMapper#getMinimalPaletteDistance()
	 */
	@Override
	public int getMinimalPaletteDistance() {
		return 0x40 * 0x40 * 3;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.gui.images.IPaletteMapper#getGreyToRgbMap()
	 */
	@Override
	public TreeMap<Integer, byte[]> getGreyToRgbMap() {
		if (map == null) {
			map = new V99ColorMapUtils.GreyRgbMapper(2, 2, 2).create();
		}
		return map;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.gui.images.IDirectColorMapper#mapDirectColor(byte[], byte)
	 */
	@Override
	public void mapDirectColor(byte[] rgb, byte c) {
		getGRB222(rgb, (byte) c, false);
	}
}