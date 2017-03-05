/*
  ColorMapUtils.java

  (c) 2011-2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package org.ejs.gui.images;

import ejs.base.utils.Pair;


public class ColorMapUtils {

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
		float c = (Math.max(0f, v / 256)) * Math.max(0f, Math.min(1f, s));
		float hprime = (Math.max(0f, Math.min(360f, h)) / 60);
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

		float m = (Math.max(0f, v) / 256) - c;
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
	 * Get pixel luminance (0-255)
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

		count = Math.min(thePalette.length, count);
		
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
		
		count = Math.min(thePalette.length, count);
		
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

		count = Math.min(thePalette.length, count);
		
		// first, only pick greys for low hue cases
		float[] phsv = { 0, 0, 0 };
		rgbToHsv((pixel & 0xff0000) >> 16, (pixel & 0xff00) >> 8, (pixel & 0xff), phsv);

		if (phsv[1] < (phsv[0] >= 30 && phsv[0] < 75 ? 0.66f : 0.33f)) {
			float[] hsv = { 0, 0, 0 };
			int whiteColor = -1, blackColor = -1;
			float whiteVal = -1;
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
	public static void rgbToGrey(int[] rgb, int[] g) {
		int lum = getRGBLum(rgb);
		g[0] = g[1] = g[2] = lum;
	}
	public static void rgbToGrey(byte[] rgb, byte[] g) {
		int lum = getRGBLum(rgb);
		g[0] = g[1] = g[2] = (byte) lum;
	}

	public static short getRGBToRGB565(byte[] rgb) {
		int r = (rgb[0] >> 3) & 0x1f;
		int g = ((rgb[1] >> 3) & 0x1f) << 1;
		int b = (rgb[2] >> 3) & 0x1f;
		return (short) ((r << 11) | (g << 5) | b);
	}
	public static void rgb565ToRGB(short pixel, byte[] rgb) {
		int r = (pixel >> 11) & 0x1f;
		int g = (pixel >> 5) & 0x3f;
		int b = (pixel) & 0x1f;
		rgb[0] = (byte) (r * 0xff / 0x1f);
		rgb[1] = (byte) (g * 0xff / 0x3f);
		rgb[2] = (byte) (b * 0xff / 0x1f);
	}
	public static short getRGBToRGBX555(byte[] rgb) {
		int r = (rgb[0] >> 3) & 0x1f;
		int g = (rgb[1] >> 3) & 0x1f;
		int b = (rgb[2] >> 3) & 0x1f;
		return (short) ((r << 11) | (g << 6) | (b << 1) );
	}
	public static void rgbX555ToRGB(short pixel, byte[] rgb) {
		int r = (pixel >> 11) & 0x1f;
		int g = (pixel >> 6) & 0x1f;
		int b = (pixel >> 1) & 0x1f;
		rgb[0] = (byte) (r * 0xff / 0x1f);
		rgb[1] = (byte) (g * 0xff / 0x1f);
		rgb[2] = (byte) (b * 0xff / 0x1f);
	}
	
	public static short getRGBToRGBX444(byte[] rgb) {
		int r = (rgb[0] >> 4) & 0xf;
		int g = (rgb[1] >> 4) & 0xf;
		int b = (rgb[2] >> 4) & 0xf;
		return (short) ((r << 12) | (g << 8) | (b << 4));
	}
}
