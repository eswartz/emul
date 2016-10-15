/*
  IPaletteColorMapper.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package org.ejs.gui.images;

public interface IPaletteColorMapper {
	/**
	 * Get the color in the new palette closest to this one.
	 * @param pixel
	 * @return color index or -1
	 */
	int getClosestPaletteEntry(int pixel);


	/**
	 * Get the color in the new palette closest to this one.
	 * @param x 
	 * @param y 
	 * @param pixel
	 * @return RGB color 
	 */
	int getClosestPalettePixel(int x, int y, int pixel);

	/** 
	 * Get the RGB pixel for the given palette index
	 * @param c
	 * @return
	 */
	int getPalettePixel(int c);

}