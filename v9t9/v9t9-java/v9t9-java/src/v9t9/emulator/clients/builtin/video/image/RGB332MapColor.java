package v9t9.emulator.clients.builtin.video.image;

import v9t9.emulator.clients.builtin.video.VdpCanvas;

class RGB332MapColor extends RGB333MapColor {

	public RGB332MapColor(boolean isGreyscale) {
		super(createStock332Palette(isGreyscale), 0, 512, false);
	}
	
	private static byte[][] createStock332Palette(boolean isGreyscale) {
		byte[][] pal = new byte[512][];
		for (int i = 0; i < 512; i++) {
			 byte[] rgb = { 0, 0, 0 };
			 VdpCanvas.getGRB332(rgb, (byte) (i >> 1), isGreyscale);
			 pal[i] = rgb;
		}
		return pal;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestPaletteColor(int[])
	 */
	@Override
	public int getClosestPalettePixel(int[] prgb) {
		// we don't need to trawl the palette here
		int g = prgb[1] >> 5;
		int r = prgb[0] >> 5;
		int b = prgb[2] >> 6;
		byte[] rgb = VdpCanvas.getGRB332(g, r, b);
		return ColorMapUtils.rgb8ToPixel(rgb);
	}
	
	@Override
	public int getMaximalReplaceDistance(int usedColors) {
		return 0xf*0xf*2 + 0x1f*0x1f;
	}
	
}