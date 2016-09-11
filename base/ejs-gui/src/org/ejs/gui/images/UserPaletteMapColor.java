/*
  UserPaletteMapColor.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package org.ejs.gui.images;


import java.util.TreeMap;

import ejs.base.utils.Pair;

public class UserPaletteMapColor extends BasePaletteMapper {
	
	public UserPaletteMapColor(byte[][] thePalette, int firstColor, int numColors,
			boolean isGreyscale) {
		super(thePalette, firstColor, numColors, false, isGreyscale);
	}
	
	@Override
	protected boolean isFixedPalette() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
	 */
	@Override
	public int mapColor(int pixel, int[] distA) {
		Pair<Integer, Integer> info = getCloseColor(pixel);
		distA[0] = info.second;
		return info.first;
	}
	
	/**
	 * Get the closest color by sheer brute force 
	 * @param pixel
	 * @return pair of color index and distance
	 */
	private Pair<Integer, Integer> getCloseColor(int pixel) {
		if (isColorMappedGreyscale) {
			return ColorMapUtils.getClosestColorByLumDistance(palette, firstColor, numColors, pixel);
		}
		return ColorMapUtils.getClosestColorByDistance(palette, firstColor, numColors, pixel, -1);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestColor(int[])
	 */
	@Override
	public int getClosestPaletteEntry(int x, int y, int pixel) {
		Pair<Integer, Integer> info = getCloseColor(pixel);
		return info.first;
	}
	

	/* (non-Javadoc)
	 * @see org.ejs.gui.images.IPaletteMapper#getRGBToGreyMap()
	 */
	@Override
	public TreeMap<Integer, byte[]> getGreyToRgbMap() {
		return V99ColorMapUtils.getGreyToRgbMap332();
	}
}