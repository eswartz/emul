package v9t9.emulator.clients.builtin.video.image;

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
	public int mapColor(int[] prgb, int[] distA) {
		int distF = ColorMapUtils.getRGBDistance(palette, fg, prgb);
		int distB = ColorMapUtils.getRGBDistance(palette, bg, prgb);
		if (fg < bg) {
			distA[0] = distF;
			return fg;
		}
		distA[0] = distB;
		return bg;
	}
	
	/**
	 * Get the closest color by sheer brute force -- we don't
	 * want dark green to emerge as a "close" color for dark or
	 * desaturated colors!
	
	 * @param prgb
	 * @return
	 */
	private int getCloseColor(int[] prgb) {
		float[] phsv = { 0, 0, 0 };
		ColorMapUtils.rgbToHsv(prgb, phsv);
		return phsv[2] < 64 ? fg : bg;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestColor(int[])
	 */
	@Override
	public int getClosestPalettePixel(int x, int y, int[] prgb) {
		int c = getCloseColor(prgb);
		return getPalettePixels()[c];
	}
	
	@Override
	public int getMaximalReplaceDistance(int usedColors) {
		return 7*7 * 3;
	}
}