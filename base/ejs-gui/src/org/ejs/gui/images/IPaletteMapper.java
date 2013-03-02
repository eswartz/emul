/*
  IPaletteMapper.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package org.ejs.gui.images;

import java.awt.image.BufferedImage;

public interface IPaletteMapper extends IPaletteColorMapper, IColorMapper {
	/**
	 * Get number of colors (range of indices for mapColor and getClosestPalettePixel)
	 */
	int getNumColors();

	/**
	 * Get the palette against which the mapping occurs.
	 * This palette object must match the one that is changed
	 * if the palette is adjusted during mapping via
	 * {@link ImageImport#optimizeForNColorsAndRebuildPalette(BufferedImage, IMapColor)}.
	 * @return palette of size {@link #getNumColors()}
	 */
	byte[][] getPalette();
	
	/**
	 * Get the minimal distance between colors in the palette.
	 */
	int getMinimalPaletteDistance();
}