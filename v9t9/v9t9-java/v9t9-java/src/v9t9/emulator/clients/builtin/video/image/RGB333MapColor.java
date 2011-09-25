package v9t9.emulator.clients.builtin.video.image;

import v9t9.emulator.clients.builtin.video.VdpCanvas;

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
	public int mapColor(int[] prgb, int[] dist) {
		int r = prgb[0] >>> 5;
		int g = prgb[1] >>> 5;
		int b = prgb[2] >>> 5;
		
		byte[] rgbs = getRGB333(r, g, b);
		
		dist[0] = ColorMapUtils.getRGBDistance(rgbs, prgb);
		
		// not actual RGB332 index!
		int c = (r << 6) | (g << 3) | b;
		
		return c;
	}

	private byte[] getRGB333(int r, int g, int b) {
		byte[] rgbs;
		if (!isGreyscale)
			rgbs = VdpCanvas.getGRB333(g, r, b);
		else {
			// (299 * rgb[0] + 587 * rgb[1] + 114 * rgb[2]) * 256 / 1000;
			
			int l = (r * 299 + g * 587 + b * 114) / 1000;
			rgbs = VdpCanvas.getGRB333(l, l, l);
		}
			
		return rgbs;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestPaletteColor(int[])
	 */
	@Override
	public int getClosestPalettePixel(int[] prgb) {
		int closest = -1;
		int mindiff = Integer.MAX_VALUE;
		for (int c = firstColor; c < numColors; c++) {
			int dist = ColorMapUtils.getRGBDistance(palette, c, prgb);
			if (dist < mindiff) {
				closest = c;
				mindiff = dist;
			}
		}
		return getPalettePixels()[closest];
	}
	
	@Override
	public int getPalettePixel(int c) {

		int g = (c >> 6) & 0x7;
		int r = (c >> 3) & 0x7;
		int b = (c >> 0) & 0x7;
		
		byte[] rgbs = getRGB333(r, g, b);
		return ColorMapUtils.rgb8ToPixel(rgbs);
	}
}