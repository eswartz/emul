/*
  RGBDirectMapColor.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.imageimport;

import org.ejs.gui.images.BasePaletteMapper;
import org.ejs.gui.images.ColorMapUtils;
import org.ejs.gui.images.IDirectColorMapper;

abstract class RGBDirectMapColor extends BasePaletteMapper implements IDirectColorMapper {
	/**
	 * @param isGreyscale 
	 * 
	 */
	public RGBDirectMapColor(byte[][] thePalette, int firstColor, int numColors, boolean isGreyscale) {
		super(thePalette, firstColor, numColors, true, isGreyscale);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestPaletteColor(int[])
	 */
	@Override
	public int getClosestPaletteEntry(int x, int y, int pixel) {
		int closest = -1;
		int mindiff = Integer.MAX_VALUE;
		if (isColorMappedGreyscale) {
			for (int c = firstColor; c < numColors; c++) {
				int dist = ColorMapUtils.getRGBLumDistance(palette[c], pixel);
				if (dist < mindiff) {
					closest = c;
					mindiff = dist;
				}
			}
		} else {
			for (int c = firstColor; c < numColors; c++) {
				int dist = ColorMapUtils.getRGBDistance(palette[c], pixel);
				if (dist < mindiff) {
					closest = c;
					mindiff = dist;
				}
			}
		}
		return getPalettePixels()[closest];
	}
	
}