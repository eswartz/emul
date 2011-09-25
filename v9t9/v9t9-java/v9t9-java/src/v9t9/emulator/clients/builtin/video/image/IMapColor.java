package v9t9.emulator.clients.builtin.video.image;

import java.awt.image.BufferedImage;

interface IMapColor {
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
	
	/** Return a color index from mapping the RGB pixel 
	 * 
	 * @param prgb pixel in X8R8G8B8 format
	 * @param dist array for receiving distance² from the returned pixel
	 * @return the color index
	 */
	int mapColor(int[] prgb, int[] dist);

	/**
	 * Get the color in the new palette closest to this one.
	 * @param prgb
	 * @return color index or -1
	 */
	int getClosestPalettePixel(int[] prgb);
	
	/**
	 * Get the maximum pixel distance for absolutely replacing a range
	 * of source pixels with palette pixels. 
	 * @param usedColors the number of unique colors 
	 */
	int getMaximalReplaceDistance(int usedColors);

	int getPalettePixel(int c);
}