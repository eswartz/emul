package v9t9.emulator.clients.builtin.video.image;

class FixedPaletteMapColor extends BasePaletteMapper {
	public FixedPaletteMapColor(byte[][] thePalette, int firstColor, int numColors) {
		super(thePalette, firstColor, numColors, false, false);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
	 */
	@Override
	public int mapColor(int[] prgb, int[] distA) {
		for (int c = firstColor; c < numColors; c++) {
			int dist = ColorMapUtils.getRGBDistance(palette, c, prgb);
			if (dist < 25*3) {
				distA[0] = dist;
				return c;
			}
		}
		distA[0] = Integer.MAX_VALUE;
		return -1;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestPaletteColor(int[])
	 */
	@Override
	public int getClosestPalettePixel(int[] prgb) {
		int closest = -1;
		for (int c = firstColor; c < numColors; c++) {
			int dist = ColorMapUtils.getRGBDistance(palette, c, prgb);
			if (dist < 25*3) {
				return getPalettePixels()[closest];
			}
		}
		return ColorMapUtils.rgb8ToPixel(prgb);
	}
}