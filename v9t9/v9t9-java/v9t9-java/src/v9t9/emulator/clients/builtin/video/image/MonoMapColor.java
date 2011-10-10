package v9t9.emulator.clients.builtin.video.image;

import v9t9.emulator.clients.builtin.video.ColorMapUtils;

class MonoMapColor extends BasePaletteMapper {
	private final int fg;
	private final int bg;
	
	public MonoMapColor(int fg, int bg) {
		super(createMonoPalette(fg, bg), 0, 16, false, false);
		this.fg = fg;
		this.bg = bg;
	}
	
	private static byte[][] createMonoPalette(int fg, int bg) {
		byte[][] palette = new byte[16][];
		palette[fg] = new byte[] { 0, 0, 0 };
		palette[bg] = new byte[] { -1, -1, -1 };
		for (int c = 0; c < 16; c++) {
			if (c != fg && c != bg) {
				palette[c] = new byte[] { 127, 127, 127 };
			}
		}
		return palette;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
	 */
	@Override
	public int mapColor(int pixel, int[] distA) {
		int distF = ColorMapUtils.getRGBDistance(palette[fg], pixel);
		int distB = ColorMapUtils.getRGBDistance(palette[bg], pixel);
		if (fg < bg) {
			distA[0] = distF;
			return fg;
		}
		distA[0] = distB;
		return bg;
	}
	
	/**
	 * @param prgb
	 * @return
	 */
	private int getCloseColor(int pixel) {
		int lum = ColorMapUtils.getPixelLum(pixel);
		return lum < 50 ? fg : bg;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestColor(int[])
	 */
	@Override
	public int getClosestPalettePixel(int x, int y, int pixel) {
		int c = getCloseColor(pixel);
		return getPalettePixels()[c];
	}
	
	@Override
	public int getMaximalReplaceDistance(int usedColors) {
		return 7*7 * 3;
	}
}