/*
  RGB333MapColor.java

  (c) 2011-2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.imageimport;

import java.util.TreeMap;

import org.ejs.gui.images.BasePaletteMapper;
import org.ejs.gui.images.ColorMapUtils;
import org.ejs.gui.images.V99ColorMapUtils;

class RGB333MapColor extends BasePaletteMapper {
	private static TreeMap<Integer, byte[]> map;


	/**
	 * @param isGreyscale 
	 * 
	 */

	public RGB333MapColor(boolean isGreyscale) {
		super(createStock333Palette(), 0, 512, false, isGreyscale);
	}
	

	private static byte[][] createStock333Palette() {
		byte[][] pal = new byte[512][];
		for (int i = 0; i < 512; i++) {
			 byte[] rgb = { 0, 0, 0 };
			 getGRB333(rgb, i, false);
			 pal[i] = rgb;
		}
		return pal;
	}

	/** Get the 8-bit RGB values from a packed 3-3-3 GRB byte */
	private static void getGRB333(byte[] rgb, int grb, boolean isGreyscale) {
		int g = (grb >> 6) & 0x7;
		int r = (grb >> 3) & 0x7;
		int b = grb & 0x7;
		rgb[0] = V99ColorMapUtils.rgb3to8[r];
		rgb[1] = V99ColorMapUtils.rgb3to8[g];
		rgb[2] = V99ColorMapUtils.rgb3to8[b];
		if (isGreyscale) {
			ColorMapUtils.rgbToGrey(rgb, rgb);
		}
	}


	/** Get the RGB triple for the 333 GRB. */
	private byte[] getGRB333(int g, int r, int b) {
		return new byte[] { V99ColorMapUtils.rgb3to8[r & 0x7],
				V99ColorMapUtils.rgb3to8[g & 0x7],
				V99ColorMapUtils.rgb3to8[b & 0x7] };
	}
	protected byte[] getRGB333(int r, int g, int b) {
		byte[] rgbs = getGRB333(g, r, b);
		if (isColorMappedGreyscale) {
			rgbs = getRgbToGreyForGreyscaleMode(rgbs);
		}
			
		return rgbs;
	}

	public RGB333MapColor(byte[][] thePalette, int firstColor, int numColors, boolean isGreyscale) {
		super(thePalette, firstColor, numColors, true, isGreyscale);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
	 */
	@Override
	public int mapColor(int pixel, int[] dist) {
		int r = ((pixel & 0xff0000) >>> 16) >>> 5;
		int g = ((pixel & 0x00ff00) >>>  8) >>> 5;
		int b = ((pixel & 0x0000ff) >>>  0) >>> 5;
		
		byte[] rgbs = getRGB333(r, g, b);
		
		if (isColorMappedGreyscale)
			dist[0] = ColorMapUtils.getRGBLumDistance(rgbs, pixel);
		else
			dist[0] = ColorMapUtils.getRGBDistance(rgbs, pixel);
		
		int c = (r << 6) | (g << 3) | b;
		
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
		
		int r = ((pixel & 0xff0000) >>> 16) >>> 5;
		int g = ((pixel & 0x00ff00) >>>  8) >>> 5;
		int b = ((pixel & 0x0000ff) >>>  0) >>> 5;
		return (r << 6) | (g << 3) | b;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.image.BasePaletteMapper#getMinimalPaletteDistance()
	 */
	@Override
	public int getMinimalPaletteDistance() {
		return 3 * 0x20 * 0x20;
	}

	@Override
	public int getPalettePixel(int c) {

		int r = (c >> 6) & 0x7;
		int g = (c >> 3) & 0x7;
		int b = (c >> 0) & 0x7;
		
		byte[] rgbs = getRGB333(r, g, b);
		return ColorMapUtils.rgb8ToPixel(rgbs);
	}
	

	/* (non-Javadoc)
	 * @see org.ejs.gui.images.IPaletteMapper#getGreyToRgbMap()
	 */
	@Override
	public TreeMap<Integer, byte[]> getGreyToRgbMap() {
		if (map == null) {
			map = new V99ColorMapUtils.GreyRgbMapper(3, 3, 3).create();
		}
		return map;

	}
}