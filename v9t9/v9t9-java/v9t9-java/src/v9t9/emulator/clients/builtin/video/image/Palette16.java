package v9t9.emulator.clients.builtin.video.image;

class Palette16 {
	private final byte[][] palette;
	private float[][] palhsv;

	public Palette16(byte[][] thePalette) {
		this.palette = thePalette;
		this.palhsv = new float[16][];
		for (int c = 1; c < 16; c++) {
			palhsv[c] = ColorMapUtils.rgbToHsv(palette[c]);
		}
	}
	
	public int getClosestColor16(int ncols, int[] prgb, int distLimit) {
		float[] phsv = { 0, 0, 0 };
		int closest = -1;
		int mindiff = Integer.MAX_VALUE;
		for (int c = 1; c < ncols; c++) {
			int dist;
			ColorMapUtils.rgbToHsv(prgb, phsv);
			dist = getColorDistance16(c, phsv, prgb);  	
			if (dist < distLimit && dist < mindiff) {
				closest = c;
				mindiff = dist;
			}
		}
		return closest;
	}


	private int getColorDistance16(int c, float[] phsv, int[] prgb) {
		int dist;
		
		if (phsv[2] < 33 && palhsv[c][2] < 33) {
			return 0;
		}
		else if (phsv[1] < 0.25 && palhsv[c][1] < 0.25) {
			// only select something with low saturation,
			// and match value
			float dh = 16; //(palhsv[c][0] - phsv[0]);	// range: 0-35
			float ds = (palhsv[c][1] - phsv[1]) * 256;
			float dv = (palhsv[c][2] - phsv[2]);
			
			dist = (int) ((dh * dh) + (ds * ds) + (dv * dv));
		} else {
			dist = ColorMapUtils.getRGBDistance(palette, c, prgb);
			
			float dh = Math.abs(palhsv[c][0] - phsv[0]) * 6;
			
			dist = dist * 16 + (int) (dh * dh);
		}
		
		return dist;
	}
	
}