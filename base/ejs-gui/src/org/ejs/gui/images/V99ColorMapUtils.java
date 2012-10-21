/**
 * 
 */
package org.ejs.gui.images;

import java.util.Map.Entry;
import java.util.TreeMap;


/**
 * @author ejs
 *
 */
public class V99ColorMapUtils {
	static {
		for (int i = 0; i < 8; i++) {
			byte val = (byte) i;
			byte val8 = (byte) (val << 5);
			//if (val > 4)
			//	val8 |= 0x1f;
			val8 |= i * 0x1f / 7; 
			V99ColorMapUtils.rgb3to8[i] = val8;
		}
		for (int i = 0; i < 4; i++) {
			byte val = (byte) i;
			byte val8 = (byte) (val << 6);
			val8 |= i * 0x3f / 3;
			V99ColorMapUtils.rgb2to8[i] = val8;
		}
	}
	public static byte[] rgb3to8 = new byte[8];
	public static byte[] rgb2to8 = new byte[4];
	public static TreeMap<Integer,byte[]> greyToRgbMap;
	/** Get the RGB triple for the 3-bit GRB. */
	public static byte[] getGRB333(int g, int r, int b) {
		return new byte[] { rgb3to8[r&0x7], rgb3to8[g&0x7], rgb3to8[b&0x7] };
	}
	/** Get the RGB triple for the 3-bit GRB. */
	public static byte[] getGRB332(int g, int r, int b) {
		//return new byte[] { rgb3to8[r&0x7], rgb3to8[g&0x7], rgb2to8[b&0x3] };
		return new byte[] { rgb3to8[r&0x7], rgb3to8[g&0x7], rgb3to8[(b&0x3)*2 + ((r|g) & 1)] };
	}
	/** Get the 8-bit RGB values from a packed 3-3-2 GRB byte */
	public static void getGRB332(byte[] rgb, byte grb, boolean isGreyscale) {
		int g = (grb >> 5) & 0x7;
		int r = (grb >> 2) & 0x7;
		rgb[0] = rgb3to8[r];
		rgb[1] = rgb3to8[g];
		//rgb[2] = rgb2to8[grb & 0x3];
		int b = grb & 0x3;
		rgb[2] = rgb3to8[b*2 + ((r|g) & 1)];
		if (isGreyscale) {
			int l = ((rgb[0] & 0xff) * 299 + (rgb[1] & 0xff) * 587 + (rgb[2] & 0xff) * 114) / 1000;
			rgb[0] = (byte) l;
			rgb[1] = (byte) l;
			rgb[2] = (byte) l;
		}
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
		int lum = ColorMapUtils.getRGBLum(rgb);
		
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
		ColorMapUtils.pixelToRGB(pixel, rgb);
		rgb = getRgbToGreyForGreyscaleMode(rgb);
		return ColorMapUtils.rgb8ToPixel(rgb);
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
	public static byte[] getMapForRGB333(byte[] rgb) {
		return new byte[] { (byte) Math.min(255, ((rgb[0] & 0xe0) * 0xff / 0xe0)), 
				(byte) Math.min(255, ((rgb[1] & 0xe0) * 0xff / 0xe0)), 
				(byte) Math.min(255, ((rgb[2] & 0xe0) * 0xff / 0xe0)) };
	}
	public static void mapForRGB333(int[] rgb) {
		rgb[0] = Math.min(255, ((rgb[0] & 0xe0) * 0xff / 0xe0));
		rgb[1] = Math.min(255, ((rgb[1] & 0xe0) * 0xff / 0xe0));
		rgb[2] = Math.min(255, ((rgb[2] & 0xe0) * 0xff / 0xe0));
	}
	public static void mapForRGB555(int[] rgb) {
		rgb[0] = Math.min(255, ((rgb[0] & 0xf8) * 0xff / 0xf8)); 
		rgb[1] = Math.min(255, ((rgb[1] & 0xf8) * 0xff / 0xf8));
		rgb[2] = Math.min(255, ((rgb[2] & 0xf8) * 0xff / 0xf8));
	}
	/**
	 * @param bs
	 * @return
	 */
	public static short rgb8ToRgbRBXG(byte[] rgb) {
		return (short) ((((rgb[0] >> 5) & 0x7) << 12) | 
				(((rgb[2] >> 5) & 0x7) << 8) |
				(((rgb[1] >> 5) & 0x7) << 0));
	}

}
