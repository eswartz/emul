package v9t9.emulator.clients.builtin.video.image;

interface IMapColorBase {

	/**
	 * Get the color in the new palette closest to this one.
	 * @param x TODO
	 * @param y TODO
	 * @param prgb
	 * @return color index or -1
	 */
	int getClosestPalettePixel(int x, int y, int[] prgb);

}