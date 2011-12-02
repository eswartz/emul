package v9t9.emulator.clients.builtin.video.image;

import org.ejs.coffee.core.utils.Pair;

import v9t9.emulator.clients.builtin.video.ColorMapUtils;

class TI16MapColor extends BasePaletteMapper {
	
	public TI16MapColor(byte[][] thePalette) {
		super(thePalette, 1, 16, false, false);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
	 */
	@Override
	public int mapColor(int pixel, int[] distA) {
		Pair<Integer, Integer> info = getCloseColor(pixel);
		distA[0] = info.second;
		return info.first;
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
	 * @return pair of index and distance
	 */
	private Pair<Integer, Integer> getCloseColor(int pixel) {
		if (isGreyscale) {
			return ColorMapUtils.getClosestColorByLumDistance(palette, firstColor, numColors, pixel);
		}
		
		final int white = 15;
		final int black = 1;
		final int grey = 14;
		final int darkGreen = 12;
		
		float[] phsv = { 0, 0, 0 };
		ColorMapUtils.rgbToHsv((pixel & 0xff0000) >> 16, (pixel & 0x00ff00) >> 8, (pixel & 0xff), phsv);
		
		float hue = phsv[0];
		float val = phsv[2] * 100 / 256;

		int closest = -1;
		int mindiff;

		
		if (phsv[1] < (hue >= 30 && hue < 75 ? 0.66f : 0.33f)) {
			if (val >= 70) {
				closest = white;
			} else if (val >= 25) {
				// dithering will take care of the rest
				closest = grey;
			} else {
				closest = black;
			}
			mindiff = ColorMapUtils.getRGBDistance(palette[closest], pixel);
		}
		else {
			Pair<Integer, Integer> info = ColorMapUtils.getClosestColorByDistanceAndHSV(
					palette, firstColor, 16, pixel, -1);
			
			closest = info.first; mindiff = info.second;
			if (!isGreyscale) {
				if (closest == darkGreen) {
					if (phsv[1] < 0.5f) {
						closest = black;
						mindiff = ColorMapUtils.getRGBDistance(palette[closest], pixel);
					}
				}
				// see how the color matches
				else if (false && closest == black) {
					if (phsv[1] > 0.9f && val >= 25) {
						if ((hue >= 90 && hue < 140) && (val >= 5 && val <= 33)) {
							closest = 12;
							mindiff = ColorMapUtils.getRGBDistance(palette[closest], pixel);
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
		}
		
		//closest = rigidMatch(phsv, hue, val);
		
		return new Pair<Integer, Integer>(closest, mindiff);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestColor(int[])
	 */
	@Override
	public int getClosestPaletteEntry(int x, int y, int pixel) {
		Pair<Integer, Integer> info = getCloseColor(pixel);
		return info.first;
	}
	
	@Override
	public int getMinimalPaletteDistance() {
		//return super.getMinimalPaletteDistance();
		return 0x10 * 0x20 * 3;
	}
}