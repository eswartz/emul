/*
  FixedPaletteMapColor.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package org.ejs.gui.images;

import java.util.TreeMap;


public class FixedPaletteMapColor extends BasePaletteMapper {
	public FixedPaletteMapColor(byte[][] thePalette, int firstColor, int numColors) {
		super(thePalette, firstColor, numColors, false, false);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
	 */
	@Override
	public int mapColor(int pixel, int[] distA) {
		for (int c = firstColor; c < numColors; c++) {
			int dist = ColorMapUtils.getRGBDistance(palette[c], pixel);
			if (dist < 25*3) {
				distA[0] = dist;
				return c;
			}
		}
		distA[0] = Integer.MAX_VALUE;
		return -1;
	}
	
	@Override
	public int getClosestPaletteEntry(int pixel) {
		int closest = -1;
		for (int c = firstColor; c < numColors; c++) {
			int dist = ColorMapUtils.getRGBDistance(palette[c], pixel);
			if (dist < 25*3) {
				break;
			}
		}
		return closest;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.gui.images.IPaletteMapper#getRGBToGreyMap()
	 */
	@Override
	public TreeMap<Integer, byte[]> getGreyToRgbMap() {
		return V99ColorMapUtils.getGreyToRgbMap332();
	}
}