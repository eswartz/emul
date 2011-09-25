package v9t9.emulator.clients.builtin.video.image;

class UserPaletteMapColor extends BasePaletteMapper {
	
	private final boolean limitDither;

	public UserPaletteMapColor(byte[][] thePalette, int firstColor, int numColors,
			boolean isGreyscale, boolean limitDither) {
		super(thePalette, firstColor, numColors, false, isGreyscale);
		this.limitDither = limitDither;
	}
	
	@Override
	protected boolean isFixedPalette() {
		return limitDither;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
	 */
	@Override
	public int mapColor(int[] prgb, int[] distA) {
		int closest = getCloseColor(prgb);
		distA[0] = ColorMapUtils.getRGBDistance(palette, closest, prgb);
		
		return closest;
	}
	
	/**
	 * Get the closest color by sheer brute force 
	 * @param prgb
	 * @return
	 */
	private int getCloseColor(int[] prgb) {
		return ColorMapUtils.getClosestColorByDistance(palette, firstColor, numColors, prgb, -1);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestColor(int[])
	 */
	@Override
	public int getClosestPalettePixel(int[] prgb) {
		int c = getCloseColor(prgb);
		return getPalettePixels()[c];
	}
}