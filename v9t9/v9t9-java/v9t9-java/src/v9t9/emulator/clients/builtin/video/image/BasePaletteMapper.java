package v9t9.emulator.clients.builtin.video.image;

import v9t9.emulator.clients.builtin.video.VdpCanvas;

abstract class BasePaletteMapper implements IMapColor {
	private final boolean canSetPalette;
	private int minDist;
	protected byte[][] palette;
	protected int numColors;
	protected int firstColor;
	protected final boolean isGreyscale;
	
	protected int[] palettePixels;

	public BasePaletteMapper(byte[][] palette, int firstColor, int numColors, boolean canSetPalette, boolean isGreyscale) {
		this.palette = palette;
		this.isGreyscale = isGreyscale;
		
		if (isGreyscale) {
			// convert palette
			byte[][] greyPalette = new byte[palette.length][];
			for (int c = 0; c < greyPalette.length; c++) {
				greyPalette[c] = VdpCanvas.rgbToGrey(palette[c]);
			}
			this.palette = greyPalette;
		}
		
		this.firstColor = firstColor;
		this.numColors = numColors;
		this.canSetPalette = canSetPalette;
		
		minDist = Integer.MAX_VALUE;
		for (int c = 0; c < numColors; c++) {
			for (int d = c + 1; d < numColors; d++) {
				int[] prgb = { palette[d][0] & 0xff,
						palette[d][1] & 0xff, 
						palette[d][2] & 0xff 
				};
				int dist = ColorMapUtils.getRGBDistance(palette, c, prgb);
				if (dist != 0 && dist < minDist)
					minDist = dist;
			}
		}
		
	}
	

	@Override
	public byte[][] getPalette() {
		return palette;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getNumColors()
	 */
	@Override
	public int getNumColors() {
		return numColors;
	}
	
	@Override
	public int getMinimalPaletteDistance() {
		if (canSetPalette)
			// 0xff --> 0xe0 for R, G, B
			return 0x1f*0x1f * 3;

		return minDist;
	}
	@Override
	public int getMaximalReplaceDistance(int usedColors) {
		if (!isFixedPalette()) {
			//boolean highColors = numColors < 16 || usedColors > numColors * 4;
			//return highColors ? 0x7*0x7 * 3 : minDist;
			return ((numColors == 4 ? 0x3*0x3*3 : 0x7*0x7 * 3) * 3 + minDist) / 4;
		} else {
			return minDist;
		}
	}
	
	protected boolean isFixedPalette() {
		return !canSetPalette;
	}


	/**
	 * Get RGB pixel for each palette entry.
	 * The pixels are calculated lazily in case the
	 * palette changes (this is called only after the
	 * mapping is complete).
	 * @return
	 */
	protected int[] getPalettePixels() {
		if (palettePixels == null) {

			palettePixels = new int[numColors];
			
			for (int x = 0; x < numColors; x++) {
				palettePixels[x] = ColorMapUtils.rgb8ToPixel(palette[x]);
			}
		}
		return palettePixels;
	}
	
	@Override
	public int getPalettePixel(int c) {
		return ColorMapUtils.rgb8ToPixel(palette[c]);
	}
}