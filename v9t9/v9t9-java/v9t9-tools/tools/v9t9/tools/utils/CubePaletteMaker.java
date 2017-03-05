/**
 * 
 */
package v9t9.tools.utils;

public class CubePaletteMaker {
	byte[][] pal;
	
	public CubePaletteMaker(int num, int rs, int gs, int bs, boolean pure) {
		pal = new byte[num][];
		int idx = 0;
		
		if (pure) {
			// span 0 to 255 in each channel
			for (int r = 0; r < rs; r++) {
				for (int g = 0; g < gs; g++) {
					for (int b = 0; b < bs; b++) {
						pal[idx] = new byte[] { 
								(byte) (r*255/(rs-1)), 
								(byte) (g*255/(gs-1)),
								(byte) (b*255/(bs-1)) 
						};
						idx++;
					}
				}
			}
		} else {
			// always have non-pure colors, by drawing a line
			// through each color axis that starts 1/2 a step
			// above 0 and ends 1/2 a step below 255,
			// where a step is the distance between successive 
			// increments
			for (int r = 0; r < rs; r++) {
				for (int g = 0; g < gs; g++) {
					for (int b = 0; b < bs; b++) {
						pal[idx] = new byte[] { 
								(byte) ((r*255/rs)+128/rs), 
								(byte) ((g*255/gs)+128/gs),
								(byte) ((b*255/bs)+128/bs) 
						};
						idx++;
					}
				}
			}
		}

		// when pure, black and white are generated
		int greys = num - idx + (pure ? 1 : -1);
		int g = pure ? 1 : 0;
		while (idx < num) {
			byte gr = (byte) (g*255/greys);
			pal[idx] = new byte[] { gr, gr, gr };
			g++;
			idx++;
		}
	}
	
	public byte[][] getPalette() {
		return pal;
	}
}