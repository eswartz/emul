package v9t9.emulator.clients.builtin.video.image;

import java.awt.image.BufferedImage;

interface IPaletteMapper extends IPaletteColorMapper, IColorMapper {
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
	
	/**
	 * Get the maximum pixel distance for absolutely replacing a range
	 * of source pixels with palette pixels. 
	 * @param usedColors the number of unique colors 
	 */
	int getMaximalReplaceDistance(int usedColors);

	int getPalettePixel(int c);
}