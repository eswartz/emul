package v9t9.video.imageimport;

import v9t9.common.video.ColorMapUtils;

class FixedPaletteMapColor extends BasePaletteMapper {
	public FixedPaletteMapColor(byte[][] thePalette, int firstColor, int numColors) {
		super(thePalette, firstColor, numColors, false, false);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
	 */
	@Override
	public int mapColor(int pixel, int[] distA) {
		for (int c = firstColor; c < numColors; c++) {
			int dist = ColorMapUtils.getRGBDistance(palette[c], pixel);
			if (dist < 25*3) {
				distA[0] = dist;
				return c;
			}
		}
		distA[0] = Integer.MAX_VALUE;
		return -1;
	}
	
	@Override
	public int getClosestPaletteEntry(int x, int y, int pixel) {
		int closest = -1;
		for (int c = firstColor; c < numColors; c++) {
			int dist = ColorMapUtils.getRGBDistance(palette[c], pixel);
			if (dist < 25*3) {
				break;
			}
		}
		return closest;
	}
}