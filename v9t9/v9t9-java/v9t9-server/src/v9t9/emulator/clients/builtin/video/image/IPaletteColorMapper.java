package v9t9.emulator.clients.builtin.video.image;

interface IPaletteColorMapper {
	/**
	 * Get the color in the new palette closest to this one.
	 * @param x 
	 * @param y 
	 * @param pixel
	 * @return color index or -1
	 */
	int getClosestPaletteEntry(int x, int y, int pixel);


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