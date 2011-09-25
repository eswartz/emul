package v9t9.emulator.clients.builtin.video.image;

class TI16MapColor extends BasePaletteMapper {
	
	public TI16MapColor(byte[][] thePalette) {
		super(thePalette, 1, 16, false, false);
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
	 * Get the closest color by sheer brute force -- we don't
	 * want dark green to emerge as a "close" color for dark or
	 * desaturated colors!
	 * 
	 * 0 clear
	 * 
	 * 1 black
	 * 
	 * 2 medium green
	 * 
	 * 3 light green
	 * 
	 * 4 dark blue
	 * 
	 * 5 light blue
	 * 
	 * 6 dark red
	 * 
	 * 7 cyan
	 * 
	 * 8 medium red
	 * 
	 * 9 orange/skin
	 * 
	 * 10 yellow
	 * 
	 * 11 light yellow
	 * 
	 * 12 dark green
	 * 
	 * 13 purple
	 * 
	 * 14 grey
	 * 
	 * 15 white
	 * 
	 * @param prgb
	 * @return
	 */
	private int getCloseColor(int[] prgb) {
		float[] phsv = { 0, 0, 0 };
		ColorMapUtils.rgbToHsv(prgb, phsv);
		
		float hue = phsv[0];
		float val = phsv[2] * 100 / 256;

		int closest = -1;

		final int white = 15;
		final int black = 1;
		final int grey = 14;
		
		if (phsv[1] < (hue >= 30 && hue < 75 ? 0.66f : 0.33f)) {
			if (val >= 70) {
				closest = white;
			} else if (val >= 10) {
				// dithering will take care of the rest
				closest = grey;
			} else {
				closest = black;
			}
		}
		else {
			closest = ColorMapUtils.getClosestColorByDistanceAndHSV(palette, firstColor, 16, prgb, 12);
			
			// see how the color matches
			if (closest == black) {
				if (phsv[1] > 0.9f) {
					if ((hue >= 75 && hue < 140) && (val >= 5 && val <= 33)) {
						closest = 12;
					}
				}
			}
			/*else {
				int rigid = rigidMatch(phsv, hue, val);
				if (phsv[1] < 0.5f && (rigid == 1 || rigid == 14 || rigid == 15)) {
					closest = rigid;
				}
			}*/
		}
		
		//closest = rigidMatch(phsv, hue, val);
		
		return closest;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestColor(int[])
	 */
	@Override
	public int getClosestPalettePixel(int[] prgb) {
		int c = getCloseColor(prgb);
		return getPalettePixels()[c];
	}
	
	@Override
	public int getMinimalPaletteDistance() {
		return Integer.MAX_VALUE;
	}
}