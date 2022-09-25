/*
  RGB687MapColor.java

  (c) 2019 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.imageimport;

import java.util.TreeMap;

import org.ejs.gui.images.ColorMapUtils;

class RGB865MapColor extends RGBDirectMapColor {

	static private TreeMap<Integer, byte[]> map;

	public RGB865MapColor(boolean isGreyscale) {
		super(createStock865Palette(), 0, 256, isGreyscale);
	}
	
	/** Get the 8-bit RGB values GRB 8-6-5 byte 
	 * 
	 */
	private static void getGRB865(byte[] rgb, byte grb, boolean isGreyscale) {
		int g,r,b;
		int grbi = grb & 0xff;
		if (grbi >= 240) {
			g = r = b = (grbi - 240) * 255 / 15;
		} else {
			b = (grbi % 5) * 255 / 4;
			grbi /= 5;
			r = (grbi % 6) * 255 / 5;
			g = (grbi / 6) * 255 / 7;
		}
		rgb[0] = (byte) r;
		rgb[1] = (byte) g;
		rgb[2] = (byte) b;
		if (isGreyscale) {
			ColorMapUtils.rgbToGrey(rgb, rgb);
		}
	}
	
	private static byte[][] createStock865Palette() {
		byte[][] pal = new byte[256][];
		for (int i = 0; i < 256; i++) {
			 byte[] rgb = { 0, 0, 0 };
			 getGRB865(rgb, (byte) i, false);
			 pal[i] = rgb;
		}
		return pal;
	}

	protected byte[] getRGB865(int r, int g, int b) {
		if (r == g && g == b) {
			byte val15 = (byte) (r * 15 / 255);
			byte val255 = (byte) (val15 * 255 / 15);
			return new byte[] { val255, val255, val255 };
		}
		int r8 = r * 5 / 255; 
		int g8 = g * 7 / 255; 
		int b8 = b * 4 / 255; 
		byte[] rgbs = new byte[] { (byte) (r8 * 255 / 5),
				(byte) (g8 * 255 / 7),
				(byte) (b8 * 255 / 4) };
		if (isColorMappedGreyscale) {
			rgbs = getRgbToGreyForGreyscaleMode(rgbs);
		}
			
		return rgbs;
	}
	
	@Override
	public int mapColor(int pixel, int[] dist) {

		int r = ((pixel & 0xff0000) >>> 16);
		int g = ((pixel & 0x00ff00) >>>  8);
		int b = ((pixel & 0x0000ff) >>>  0);
		
		byte[] rgbs = getRGB865(r, g, b);
		
		if (isColorMappedGreyscale)
			dist[0] = ColorMapUtils.getRGBLumDistance(rgbs, pixel);
		else
			dist[0] = ColorMapUtils.getRGBDistance(rgbs, pixel);

		if (r == g && g == b) {
			int val15 = (r * 15 / 255);
			return 240 + val15;
		}
		
		int c = ((rgbs[0] & 0xff) * 4 / 255) +
				((rgbs[1] & 0xff) * 5 / 255) * 5 +
				((rgbs[2] & 0xff) * 7 / 255) * 30;
		return c;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestPaletteColor(int[])
	 */
	@Override
	public int getClosestPaletteEntry(int pixel) {
		// we don't need to trawl the palette here
		if (isColorMappedGreyscale) {
			pixel = getPixelForGreyscaleMode(pixel);
		}
		
		int r = ((pixel & 0xff0000) >>> 16) >>> 6;
		int g = ((pixel & 0x00ff00) >>>  8) >>> 5;
		int b = ((pixel & 0x0000ff) >>>  0) >>> 6;
		return (g << 4) | (r << 2) | b;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.image.BasePaletteMapper#getMinimalPaletteDistance()
	 */
	@Override
	public int getMinimalPaletteDistance() {
		return (255 / 5) * (255 / 5) + (255 / 6) * (255 / 6) + (255 / 7) * (255 / 7);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.gui.images.IPaletteMapper#getGreyToRgbMap()
	 */
	@Override
	public TreeMap<Integer, byte[]> getGreyToRgbMap() {
		if (map == null) {
			map = new TreeMap<Integer, byte[]>();
		}
		return map;

	}
	
	/* (non-Javadoc)
	 * @see org.ejs.gui.images.IDirectColorMapper#mapDirectColor(byte[], byte)
	 */
	@Override
	public void mapDirectColor(byte[] rgb, byte c) {
		getGRB865(rgb, (byte) c, false);
	}
}