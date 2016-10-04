/*
  V99ColorMapUtils.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package org.ejs.gui.images;

import java.util.Map.Entry;
import java.util.TreeMap;


/**
 * @author ejs
 *
 */
public class V99ColorMapUtils {
	public static byte[] rgb4to8 = new byte[16];
	public static byte[] rgb3to8 = new byte[8];
	public static byte[] rgb2to8 = new byte[4];
	public static byte[] rgb1to8 = new byte[2];
	static {
		for (int i = 0; i < 16; i++) {
			byte val = (byte) i;
			byte val8 = (byte) (val << 4);
			val8 |= i * 0xf / 15; 
			V99ColorMapUtils.rgb4to8[i] = val8;
		}
		for (int i = 0; i < 8; i++) {
			byte val = (byte) i;
			byte val8 = (byte) (val << 5);
			val8 |= i * 0x1f / 7; 
			V99ColorMapUtils.rgb3to8[i] = val8;
		}
		for (int i = 0; i < 4; i++) {
			byte val = (byte) i;
			byte val8 = (byte) (val << 6);
			val8 |= i * 0x3f / 3;
			V99ColorMapUtils.rgb2to8[i] = val8;
		}
		
		V99ColorMapUtils.rgb1to8[0] = 0;
		V99ColorMapUtils.rgb1to8[1] = (byte) 0xff;
	}
	
	private static TreeMap<Integer,byte[]> greyToRgbMap332;
	
	/** Get the RGB triple for the 333 GRB. */
	public static byte[] getGRB333(int g, int r, int b) {
		return new byte[] { rgb3to8[r&0x7], rgb3to8[g&0x7], rgb3to8[b&0x7] };
	}
	/** Get the RGB triple for the 332 GRB. */
	public static byte[] getGRB332(int g, int r, int b) {
		return new byte[] { rgb3to8[r&0x7], rgb3to8[g&0x7], rgb2to8[b&0x3] };
//		return new byte[] { rgb3to8[r&0x7], rgb3to8[g&0x7], rgb3to8[(b&0x3)*2 + ((r|g) & 1)] };
	}
	
	/** Get the 8-bit RGB values from a packed 3-3-2 GRB byte */
	public static void getGRB332(byte[] rgb, byte grb, boolean isGreyscale) {
		int g = (grb >> 5) & 0x7;
		int r = (grb >> 2) & 0x7;
		int b = grb & 0x3;
		rgb[0] = rgb3to8[r];
		rgb[1] = rgb3to8[g];
		rgb[2] = rgb2to8[b];
//		rgb[2] = rgb3to8[b*2 + ((r|g) & 1)];
		if (isGreyscale) {
			ColorMapUtils.rgbToGrey(rgb, rgb);
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
	public static void rgbToGreyForGreyscaleMode(TreeMap<Integer, byte[]> map, int[] rgb, int[] out) {
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
	public static byte[] getRgbToGreyForGreyscaleMode(TreeMap<Integer, byte[]> map, byte[] rgb) {
		int lum = ColorMapUtils.getRGBLum(rgb);
		
		Entry<Integer, byte[]> entry = map.ceilingEntry(lum);
		if (entry == null) {
			entry = map.floorEntry(lum);
			if (entry == null) {
				throw new AssertionError();
			}
		}
		
		return entry.getValue();
	}

	public static class GreyRgbMapper {
		private int rdelta;
		private int gdelta;
		private int bdelta;
		private int rscale;
		private int gscale;
		private int bscale;

		public GreyRgbMapper(int rbits, int gbits, int bbits) {
			rdelta = 0x100 >> rbits;
			gdelta = 0x100 >> gbits;
			bdelta = 0x100 >> bbits;
			
			rscale = ~(0xff >> rbits) & 0xff;
			gscale = ~(0xff >> gbits) & 0xff;
			bscale = ~(0xff >> bbits) & 0xff;
		}
		
		public TreeMap<Integer, byte[]> create() {
			TreeMap<Integer, byte[]> map = new TreeMap<Integer, byte[]>();
			for (int g = 0; g < 256; g += gdelta) {
				for (int r = 0; r < 256; r += rdelta) {
					for (int b = 0; b < 256; b += bdelta) {
						byte[] rgb = new byte[] { (byte) (r * 0xff / rscale), 
								(byte) (g * 0xff / gscale), 
								(byte) (b * 0xff / bscale) };
						byte[] greys = ColorMapUtils.rgbToGrey(rgb);
						int lum = greys[0] & 0xff;
						if (!map.containsKey(lum)) {
							map.put(lum, rgb);
						}
					}
				}
			}
			
			return map;
		}
	}
	
	public static TreeMap<Integer, byte[]> getGreyToRgbMap332() {
		if (greyToRgbMap332 == null) {
			greyToRgbMap332 = new GreyRgbMapper(3, 3, 2).create();
		}
		return greyToRgbMap332;
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
	/**
	 * @param rgb
	 * @return
	 */
	public static byte getRGBToGRB332(byte[] rgb) {
		int r = (rgb[0] >> 5) & 0x7;
		int g = (rgb[1] >> 5) & 0x7;
		int b = (rgb[2] >> 6) & 0x3;
		return (byte) ((r << 5) | (g << 2) | b);
	}

}
