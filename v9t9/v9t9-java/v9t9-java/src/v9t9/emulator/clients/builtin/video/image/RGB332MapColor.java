package v9t9.emulator.clients.builtin.video.image;

import v9t9.emulator.clients.builtin.video.ColorMapUtils;

class RGB332MapColor extends RGB333MapColor {

	public RGB332MapColor(boolean isGreyscale) {
		super(createStock332Palette(isGreyscale), 0, 512, isGreyscale);
	}
	
	private static byte[][] createStock332Palette(boolean isGreyscale) {
		byte[][] pal = new byte[512][];
		for (int i = 0; i < 512; i++) {
			 byte[] rgb = { 0, 0, 0 };
			 ColorMapUtils.getGRB332(rgb, (byte) (i >> 1),false && isGreyscale);
			 pal[i] = rgb;
		}
		return pal;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestPaletteColor(int[])
	 */
	@Override
	public int getClosestPalettePixel(int x, int y, int pixel) {
		// we don't need to trawl the palette here
		int r = ((pixel & 0xff0000) >>> 16) >> 5;
		int g = ((pixel & 0x00ff00) >>>  8) >> 5;
		int b = ((pixel & 0x0000ff) >>>  0) >> 6;
		byte[] rgb = ColorMapUtils.getGRB332(g, r, b);
		if (isGreyscale)
			rgb = ColorMapUtils.getRgbToGreyForGreyscaleMode(rgb);
		return ColorMapUtils.rgb8ToPixel(rgb);
	}
	
	@Override
	public int getMaximalReplaceDistance(int usedColors) {
		return 0x10*0x10*3;
	}
	
}