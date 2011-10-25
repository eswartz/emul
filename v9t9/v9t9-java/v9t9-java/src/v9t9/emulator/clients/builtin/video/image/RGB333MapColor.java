package v9t9.emulator.clients.builtin.video.image;

import v9t9.emulator.clients.builtin.video.ColorMapUtils;

class RGB333MapColor extends BasePaletteMapper {
	/**
	 * @param isGreyscale 
	 * 
	 */
	public RGB333MapColor(byte[][] thePalette, int firstColor, int numColors, boolean isGreyscale) {
		super(thePalette, firstColor, numColors, true, isGreyscale);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
	 */
	@Override
	public int mapColor(int pixel, int[] dist) {
		int r = ((pixel & 0xff0000) >>> 16) >>> 5;
		int g = ((pixel & 0x00ff00) >>>  8) >>> 5;
		int b = ((pixel & 0x0000ff) >>>  0) >>> 5;
		
		byte[] rgbs = getRGB333(r, g, b);
		
		if (isGreyscale)
			dist[0] = ColorMapUtils.getRGBLumDistance(rgbs, pixel);
		else
			dist[0] = ColorMapUtils.getRGBDistance(rgbs, pixel);
		
		// not actual RGB332 index!
		int c = (r << 6) | (g << 3) | b;
		
		return c;
	}

	private byte[] getRGB333(int r, int g, int b) {
		byte[] rgbs;
		if (!isGreyscale) {
			rgbs = ColorMapUtils.getGRB333(g, r, b);
		} else {
			// (299 * rgb[0] + 587 * rgb[1] + 114 * rgb[2]) * 256 / 1000;
			
			//int l = (r * 299 + g * 587 + b * 114) / 1000;
			//rgbs = ColorMapUtils.getGRB333(l, l, l);
			rgbs = ColorMapUtils.getRgbToGreyForGreyscaleMode(new byte[] { 
					(byte) (r * 255 / 7), (byte) (g * 255 / 7), (byte) (b * 255 / 7) });
		}
			
		return rgbs;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestPaletteColor(int[])
	 */
	@Override
	public int getClosestPaletteEntry(int x, int y, int pixel) {
		int closest = -1;
		int mindiff = Integer.MAX_VALUE;
		if (isGreyscale) {
			for (int c = firstColor; c < numColors; c++) {
				int dist = ColorMapUtils.getRGBLumDistance(palette[c], pixel);
				if (dist < mindiff) {
					closest = c;
					mindiff = dist;
				}
			}
		} else {
			for (int c = firstColor; c < numColors; c++) {
				int dist = ColorMapUtils.getRGBDistance(palette[c], pixel);
				if (dist < mindiff) {
					closest = c;
					mindiff = dist;
				}
			}
		}
		return getPalettePixels()[closest];
	}
	
	@Override
	public int getPalettePixel(int c) {

		int r = (c >> 6) & 0x7;
		int g = (c >> 3) & 0x7;
		int b = (c >> 0) & 0x7;
		
		byte[] rgbs = getRGB333(r, g, b);
		return ColorMapUtils.rgb8ToPixel(rgbs);
	}
}