/*
  RGB232MapColor.java

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

class RGB232MapColor extends RGBDirectMapColor {

	static private TreeMap<Integer, byte[]> map;

	public RGB232MapColor(boolean isGreyscale) {
		super(createStock232Palette(), 0, 128, isGreyscale);
	}
	
	/** Get the 8-bit RGB values from a packed 3-2-2 GRB byte */
	private static void getGRB322(byte[] rgb, byte grb, boolean isGreyscale) {
		int g = (grb >> 4) & 0x7;
		int r = (grb >> 2) & 0x3;
		int b = grb & 0x3;
		rgb[0] = V99ColorMapUtils.rgb2to8[r];
		rgb[1] = V99ColorMapUtils.rgb3to8[g];
		rgb[2] = V99ColorMapUtils.rgb2to8[b];
		if (isGreyscale) {
			ColorMapUtils.rgbToGrey(rgb, rgb);
		}
	}
	
	private static byte[][] createStock232Palette() {
		byte[][] pal = new byte[128][];
		for (int i = 0; i < 128; i++) {
			 byte[] rgb = { 0, 0, 0 };
			 getGRB322(rgb, (byte) i, false);
			 pal[i] = rgb;
		}
		return pal;
	}

	/** Get the RGB triple for the 232 GRB. */
	private byte[] getGRB232(int g, int r, int b) {
		return new byte[] { V99ColorMapUtils.rgb2to8[r & 0x3],
				V99ColorMapUtils.rgb3to8[g & 0x7],
				V99ColorMapUtils.rgb2to8[b & 0x3] };
	}
	
	protected byte[] getRGB232(int r, int g, int b) {
		byte[] rgbs = getGRB232(g, r, b);
		if (isColorMappedGreyscale) {
			rgbs = getRgbToGreyForGreyscaleMode(rgbs);
		}
			
		return rgbs;
	}
	
	@Override
	public int mapColor(int pixel, int[] dist) {
		int r = ((pixel & 0xff0000) >>> 16) >>> 6;
		int g = ((pixel & 0x00ff00) >>>  8) >>> 5;
		int b = ((pixel & 0x0000ff) >>>  0) >>> 6;
		
		byte[] rgbs = getRGB232(r, g, b);
		
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
		int g = ((pixel & 0x00ff00) >>>  8) >>> 5;
		int b = ((pixel & 0x0000ff) >>>  0) >>> 6;
		return (g << 4) | (r << 2) | b;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.image.BasePaletteMapper#getMinimalPaletteDistance()
	 */
	@Override
	public int getMinimalPaletteDistance() {
		return 0x40 * 0x40 * 2 + 0x20 * 0x20;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.gui.images.IPaletteMapper#getGreyToRgbMap()
	 */
	@Override
	public TreeMap<Integer, byte[]> getGreyToRgbMap() {
		if (map == null) {
			map = new V99ColorMapUtils.GreyRgbMapper(2, 3, 2).create();
		}
		return map;

	}
	
	/* (non-Javadoc)
	 * @see org.ejs.gui.images.IDirectColorMapper#mapDirectColor(byte[], byte)
	 */
	@Override
	public void mapDirectColor(byte[] rgb, byte c) {
		getGRB322(rgb, (byte) c, false);
	}
}