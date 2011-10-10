package v9t9.emulator.clients.builtin.video.image;

interface IMapColorBase {

	/**
	 * Get the color in the new palette closest to this one.
	 * @param x 
	 * @param y 
	 * @param pixel
	 * @return color index or -1
	 */
	int getClosestPalettePixel(int x, int y, int pixel);

}