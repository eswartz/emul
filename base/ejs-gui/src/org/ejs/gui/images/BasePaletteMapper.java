/*
  BasePaletteMapper.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package org.ejs.gui.images;

import java.util.TreeMap;
import java.util.Map.Entry;


abstract public class BasePaletteMapper implements IPaletteMapper {
	private final boolean canSetPalette;
	protected byte[][] palette;
	protected int numColors;
	protected int firstColor;
	protected final boolean isColorMappedGreyscale;
	
	protected int[] palettePixels;

	public BasePaletteMapper(byte[][] palette, int firstColor, int numColors, boolean canSetPalette, boolean isGreyscale) {
		this.palette = palette;
		this.isColorMappedGreyscale = isGreyscale;
		
		this.firstColor = firstColor;
		this.numColors = numColors;
		this.canSetPalette = canSetPalette;
	}
	

	@Override
	public byte[][] getPalette() {
		return palette;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getNumColors()
	 */
	@Override
	public int getNumColors() {
		return numColors;
	}
	
	@Override
	public int getMinimalPaletteDistance() {
		return 0x20*0x20 * 3;
	}
	
	protected boolean isFixedPalette() {
		return !canSetPalette;
	}


	/**
	 * Get RGB pixel for each palette entry.
	 * The pixels are calculated lazily in case the
	 * palette changes (this is called only after the
	 * mapping is complete).
	 * @return
	 */
	protected int[] getPalettePixels() {
		if (palettePixels == null) {

			palettePixels = new int[numColors];
			
			for (int x = 0; x < numColors; x++) {
				byte[] nrgb = palette[x];
				if (isColorMappedGreyscale)
					nrgb = getRgbToGreyForGreyscaleMode(nrgb);
				palettePixels[x] = ColorMapUtils.rgb8ToPixel(nrgb);
			}
		}
		return palettePixels;
	}
	
	@Override
	public int getPalettePixel(int c) {
		return getPalettePixels()[c]; //ColorMapUtils.rgb8ToPixel(palette[c]);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.image.IPaletteColorMapper#getClosestPalettePixel(int, int, int)
	 */
	@Override
	public int getClosestPalettePixel(int x, int y, int pixel) {
		int v = getClosestPaletteEntry(x, y, pixel);
		return v >= 0 ? getPalettePixel(v) : pixel;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.gui.images.IPaletteMapper#getPixelForGreyscaleMode(int)
	 */
	@Override
	public int getPixelForGreyscaleMode(int pixel) {
		byte[] rgb = { 0, 0, 0 };
		ColorMapUtils.pixelToRGB(pixel, rgb);
		rgb = getRgbToGreyForGreyscaleMode(rgb);
		return ColorMapUtils.rgb8ToPixel(rgb);
	}
	/* (non-Javadoc)
	 * @see org.ejs.gui.images.IPaletteMapper#getRgbToGreyForGreyscaleMode(byte[])
	 */
	@Override
	public byte[] getRgbToGreyForGreyscaleMode(byte[] rgb) {
		int lum = ColorMapUtils.getRGBLum(rgb);
		
		TreeMap<Integer, byte[]> map = getGreyToRgbMap();
		Entry<Integer, byte[]> entry = map.ceilingEntry(lum);
		if (entry == null) {
			entry = map.floorEntry(lum);
			if (entry == null) {
				throw new AssertionError();
			}
		}
		
		return entry.getValue();
	}
}