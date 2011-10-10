package v9t9.emulator.clients.builtin.video;

import java.util.Map.Entry;
import java.util.TreeMap;

import org.ejs.coffee.core.utils.Pair;


public class ColorMapUtils {
	protected static byte[] rgb3to8 = new byte[8];
	protected static byte[] rgb2to8 = new byte[4];
	private static TreeMap<Integer,byte[]> greyToRgbMap;

	static {
		for (int i = 0; i < 8; i++) {
			byte val = (byte) i;
			byte val8 = (byte) (val << 5);
			//if (val > 4)
			//	val8 |= 0x1f;
			val8 |= i * 0x1f / 7; 
			rgb3to8[i] = val8;
		}
		for (int i = 0; i < 4; i++) {
			byte val = (byte) i;
			byte val8 = (byte) (val << 6);
			val8 |= i * 0x3f / 3;
			rgb2to8[i] = val8;
		}
	}
	private ColorMapUtils() {
	}

	public static void rgbToHsv(byte[] rgb, float[] hsv) {
		rgbToHsv(rgb[0] & 0xff, rgb[1] & 0xff, rgb[2] & 0xff, hsv);
	}

	/**
	 * Get hue/value/satuation.
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @param hsv
	 *            in 0-360, 0-1, 0-255
	 */
	public static void rgbToHsv(int r, int g, int b, float[] hsv) {
		int theMin = Math.min(Math.min(r, g), b);
		int theMax = Math.max(Math.max(r, g), b);
		hsv[2] = theMax;
		float delta = (theMax - theMin);
		if (delta != 0)
			hsv[1] = delta / theMax;
		else {
			hsv[1] = 0;
			return;
		}
		if (r == theMax)
			hsv[0] = (g - b) / delta;
		else if (g == theMax)
			hsv[0] = 2 + (b - r) / delta;
		else
			hsv[0] = 4 + (r - g) / delta;
		hsv[0] *= 60.0;
		if (hsv[0] < 0)
			hsv[0] += 360.0;
	}

	/**
	 * Get hue/value/satuation.
	 * 
	 * @param rgb
	 * @param hsv
	 *            in 0-360, 0-1, 0-255
	 */
	public static void rgbToHsv(int[] rgb, float[] hsv) {
		rgbToHsv(rgb[0], rgb[1], rgb[2], hsv);
	}

	public static void hsvToRgb(float h, float s, float v, int[] rgb) {
		float c = (v / 256) * s;
		float hprime = (h / 60);
		float x = c * (1 - Math.abs(hprime % 2 - 1));

		float fr, fg, fb;
		switch ((int) hprime) {
		case 0:
			fr = c;
			fg = x;
			fb = 0;
			break;
		case 1:
			fr = x;
			fg = c;
			fb = 0;
			break;
		case 2:
			fr = 0;
			fg = c;
			fb = x;
			break;
		case 3:
			fr = 0;
			fg = x;
			fb = c;
			break;
		case 4:
			fr = x;
			fg = 0;
			fb = c;
			break;
		case 5:
			fr = c;
			fg = 0;
			fb = x;
			break;
		default:
			fr = fg = fb = 0;
		}

		float m = (v / 256) - c;
		rgb[0] = (int) ((fr + m) * 255);
		rgb[1] = (int) ((fg + m) * 255);
		rgb[2] = (int) ((fb + m) * 255);
	}

	public static int getRGBDistance(byte[] rgb, int pixel) {
		int dr = ((pixel & 0xff0000) >> 16) - (rgb[0] & 0xff);
		int dg = ((pixel & 0x00ff00) >> 8) - (rgb[1] & 0xff);
		int db = ((pixel & 0x0000ff) >> 0) - (rgb[2] & 0xff);
		return dr * dr + dg * dg + db * db;
	}
	public static int getPixelDistance(int pixel, int newRGB) {
		int dr = ((pixel & 0xff0000) - (newRGB & 0xff0000)) >> 16;
		int dg = ((pixel & 0x00ff00) - (newRGB & 0x00ff00)) >> 8;
		int db = ((pixel & 0x0000ff) - (newRGB & 0x0000ff)) >> 0;
		return dr * dr + dg * dg + db * db;
	}
	public static int getRGBLumDistance(byte[] rgb, int pixel) {
		int p = getPixelLum(pixel);
		int r = getRGBLum(rgb);
		return (p-r) * (p-r);
	}
	
	public static int getPixelLumDistance(int pixel, int newRGB) {
		int d = getPixelLum(pixel) - getPixelLum(newRGB);
		return d * d;
	}
	
	
	/**
	 * @param rgb
	 * @return
	 */
	public static int getRGBLum(byte[] rgb) {
		int plum = (299 * (rgb[0] & 0xff) + 
				587 * (rgb[1] & 0xff) + 
				114 * (rgb[2] & 0xff)) / 1000;
		return plum;
	}

	/**
	 * @param prgb
	 * @return
	 */
	public static int getRGBLum(int[] prgb) {
		int lum = (299 * (prgb[0] & 0xff) + 587 * (prgb[1] & 0xff) + 114 * (prgb[2] & 0xff)) / 1000;
		return lum;
	}


	/**
	 * @param prgb
	 * @return
	 */
	public static int getPixelLum(int pixel) {
		int lum = (299 * ((pixel >> 16) & 0xff) + 587 * ((pixel >> 8) & 0xff) + 114 * (pixel & 0xff)) / 1000;
		return lum;
	}
	/**
	 * Return palette index and distance
	 * @param thePalette
	 * @param first
	 * @param count
	 * @param prgb
	 * @param exceptFor
	 * @return
	 */
	public static Pair<Integer, Integer> getClosestColorByDistance(byte[][] thePalette, int first,
			int count, int pixel, int exceptFor) {
		int mindiff = Integer.MAX_VALUE;
		int closest = -1;

		for (int c = first; c < count; c++) {
			if (c != exceptFor) {
				int dist = getRGBDistance(thePalette[c], pixel);
				if (dist < mindiff) {
					mindiff = dist;
					closest = c;
				}
			}
		}

		if (closest == -1) {
			closest = exceptFor;
			mindiff = getRGBDistance(thePalette[closest], pixel);
		}
		return new Pair<Integer, Integer>(closest, mindiff);
	}


	public static Pair<Integer, Integer> getClosestColorByLumDistance(byte[][] thePalette, int first,
			int count, int pixel) {
		
		int lum = getPixelLum(pixel);
		
		int mindiff = Integer.MAX_VALUE;
		int closest = -1;

		for (int c = first; c < count; c++) {
			int plum = getRGBLum(thePalette[c]);
			int dist = (plum - lum) * (plum - lum);
			if (dist < mindiff) {
				mindiff = dist;
				closest = c;
			}
		}
		return new Pair<Integer, Integer>(closest, mindiff);
	}


	public static Pair<Integer, Integer> getClosestColorByDistanceAndHSV(byte[][] thePalette, int first,
			int count, int pixel, int exceptFor) {
		int mindiff = Integer.MAX_VALUE;
		int closest = -1;

		// first, only pick greys for low hue cases
		float[] phsv = { 0, 0, 0 };
		rgbToHsv((pixel & 0xff0000) >> 16, (pixel & 0xff00) >> 8, (pixel & 0xff), phsv);

		if (phsv[1] < (phsv[0] >= 30 && phsv[0] < 75 ? 0.66f : 0.33f)) {
			float[] hsv = { 0, 0, 0 };
			int whiteColor = -1, blackColor = -1;
			float whiteVal = Float.MAX_VALUE;
			float blackVal = Float.MAX_VALUE;
			for (int c = first; c < count; c++) {
				if (c != exceptFor) {
					rgbToHsv(thePalette[c], hsv);
					if (Math.abs(phsv[1] - hsv[1]) < 0.1f) {
						int dist = Math.abs(Math.round(phsv[2] - hsv[2]));
						if (dist < mindiff) {
							mindiff = dist;
							closest = c;
						}
					}
					if (hsv[2] > whiteVal) {
						whiteColor = c;
						whiteVal = hsv[2];
					}
					if (hsv[2] < blackVal) {
						blackColor = c;
						blackVal = hsv[2];
					}
				}
			}
			if (closest == -1) {
				// must be white or black
				if (phsv[2] < 0.5f)
					closest = blackColor;
				else
					closest = whiteColor;
				mindiff = getRGBDistance(thePalette[closest], pixel);
			}
		}
		if (closest == -1) {
			return getClosestColorByDistance(thePalette, first, count, pixel, exceptFor);
		}
		return new Pair<Integer, Integer>(closest, mindiff);
	}

	public static int rgb8ToPixel(byte[] nrgb) {
		return ((nrgb[0] & 0xff) << 16) | ((nrgb[1] & 0xff) << 8)
				| (nrgb[2] & 0xff);
	}

	public static int rgb8ToPixel(int[] prgb) {
		return (Math.max(0, Math.min(prgb[0], 255)) << 16) 
				| (Math.max(0, Math.min(prgb[1], 255)) << 8) 
				| Math.max(0, Math.min(prgb[2], 255));
	}

	public static void pixelToRGB(int pixel, int[] prgb) {
		prgb[0] = (pixel & 0xff0000) >> 16;
		prgb[1] = (pixel & 0xff00) >> 8;
		prgb[2] = pixel & 0xff;
	}

	public static void pixelToRGB(int pixel, byte[] rgb) {
		rgb[0] = (byte) ((pixel & 0xff0000) >> 16);
		rgb[1] = (byte) ((pixel & 0xff00) >> 8);
		rgb[2] = (byte) (pixel & 0xff);
	}

	/** Get the RGB triple for the 3-bit GRB. */
	public static byte[] getGRB333(int g, int r, int b) {
		return new byte[] { rgb3to8[r&0x7], rgb3to8[g&0x7], rgb3to8[b&0x7] };
	}

	/** Get the RGB triple for the 3-bit GRB. */
	public static byte[] getGRB332(int g, int r, int b) {
		return new byte[] { rgb3to8[r&0x7], rgb3to8[g&0x7], rgb2to8[b&0x3] };
	}

	/** Get the 8-bit RGB values from a packed 3-3-2 GRB byte */
	public static void getGRB332(byte[] rgb, byte grb, boolean isGreyscale) {
		rgb[0] = rgb3to8[(grb >> 2) & 0x7];
		rgb[1] = rgb3to8[(grb >> 5) & 0x7];
		rgb[2] = rgb2to8[grb & 0x3];
		if (isGreyscale) {
			int l = ((rgb[0] & 0xff) * 299 + (rgb[1] & 0xff) * 587 + (rgb[2] & 0xff) * 114) / 1000;
			rgb[0] = (byte) l;
			rgb[1] = (byte) l;
			rgb[2] = (byte) l;
		}
	}

	/**
	 * Return a grey RGB triplet corresponding to the luminance
	 * of the incoming color RGB triplet.
	 * @param rgb
	 * @return
	 */
	public static byte[] rgbToGrey(byte[] rgb) {
		byte[] g = new byte[3];
		int lum = getRGBLum(rgb);
		g[0] = g[1] = g[2] = (byte) lum;
		return g;
	}
	
	/**
	 * Return an RGB triplet corresponding to the luminance
	 * of the incoming color RGB triplet, in a mode where
	 * all colors are rendered as greyscale.
	 * 
	 * Obviously, the incoming color trivially fulfills this requirement.
	 * But the intent here is to return a canonical RGB triplet
	 * which will allow reducing a full-color gamut into a
	 * set of 199 RGB values to allow for better palette matching.
	 * 
	 * @param rgb
	 * @return
	 */
	public static void rgbToGreyForGreyscaleMode(int[] rgb, int[] out) {
		TreeMap<Integer, byte[]> map = getGreyToRgbMap();
		int lum = getRGBLum(rgb);
		
		Entry<Integer, byte[]> entry = map.floorEntry(lum);
		if (entry == null) {
			entry = map.ceilingEntry(lum);
			if (entry == null) {
				throw new AssertionError();
			}
		}
		
		byte[] value = entry.getValue();
		out[0] = value[0] & 0xff;
		out[1] = value[1] & 0xff;
		out[2] = value[2] & 0xff;
	}


	/**
	 * Return an RGB triplet corresponding to the luminance
	 * of the incoming color RGB triplet, in a mode where
	 * all colors are rendered as greyscale.
	 * 
	 * Obviously, the incoming color trivially fulfills this requirement.
	 * But the intent here is to return a canonical RGB triplet
	 * which will allow reducing a full-color gamut into a
	 * set of 199 RGB values to allow for better palette matching.
	 * 
	 * @param rgb
	 * @return
	 */
	public static byte[] getRgbToGreyForGreyscaleMode(byte[] rgb) {
		TreeMap<Integer, byte[]> map = getGreyToRgbMap();
		byte[] greys = ColorMapUtils.rgbToGrey(rgb);
		int lum = greys[0] & 0xff;
		
		Entry<Integer, byte[]> entry = map.ceilingEntry(lum);
		if (entry == null) {
			entry = map.floorEntry(lum);
			if (entry == null) {
				throw new AssertionError();
			}
		}
		
		return entry.getValue();
	}

	/**
	 * Return an RGB triplet corresponding to the luminance
	 * of the incoming color RGB triplet, in a mode where
	 * all colors are rendered as greyscale.
	 * 
	 * Obviously, the incoming color trivially fulfills this requirement.
	 * But the intent here is to return a canonical RGB triplet
	 * which will allow reducing a full-color gamut into a
	 * set of 199 RGB values to allow for better palette matching.
	 * 
	 * @param rgb
	 * @return
	 */
	public static int getPixelForGreyscaleMode(int pixel) {
		byte[] rgb = { 0, 0, 0 };
		pixelToRGB(pixel, rgb);
		rgb = getRgbToGreyForGreyscaleMode(rgb);
		return rgb8ToPixel(rgb);
	}

	/**
	 * @return
	 */
	private static TreeMap<Integer, byte[]> getGreyToRgbMap() {
		if (greyToRgbMap == null) {
			greyToRgbMap = new TreeMap<Integer, byte[]>();

			for (int g = 0; g < 256; g += 0x20) {
				for (int r = 0; r < 256; r += 0x20) {
					for (int b = 0; b < 256; b += 0x20) {
						byte[] rgb = new byte[] { (byte) (r * 0xff / 0xe0), 
								(byte) (g * 0xff / 0xe0), 
								(byte) (b * 0xff / 0xe0) };
						/*
						byte[] rgb = new byte[] { (byte) (r), 
								(byte) (g), 
								(byte) (b) };
								*/
						byte[] greys = ColorMapUtils.rgbToGrey(rgb);
						int lum = greys[0] & 0xff;
						if (!greyToRgbMap.containsKey(lum)) {
							greyToRgbMap.put(lum, rgb);
						}
					}
				}
			}
			/*
			for (Map.Entry<Integer, byte[]> ent : greyToRgbMap.entrySet()) {
				System.out.printf("%d:\t%02x %02x %02x\n",
						ent.getKey(), 
						(ent.getValue()[0]) & 0xff,
						(ent.getValue()[1]) & 0xff,
						(ent.getValue()[2]) & 0xff);
			}
			*/
		}
		return greyToRgbMap;
	}
}
