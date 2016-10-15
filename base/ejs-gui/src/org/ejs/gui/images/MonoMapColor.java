/*
  MonoMapColor.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package org.ejs.gui.images;

import java.util.TreeMap;


public class MonoMapColor implements IPaletteMapper {
	private final int fg;
	private final int bg;
	private byte[][] palette;
	private int midLum;
	
	public MonoMapColor(int fg, int bg) {
		this.fg = fg;
		this.bg = bg;
		this.palette = createMonoPalette(fg, bg);
	}
	
	/**
	 * @param midLum the midLum to set
	 */
	public void setMidLum(int midLum) {
		this.midLum = midLum;
	}
	
	
	private static byte[][] createMonoPalette(int fg, int bg) {
		byte[][] palette = new byte[Math.max(16, bg+1)][];
		palette[fg] = new byte[] { 0, 0, 0 };
		palette[bg] = new byte[] { -1, -1, -1 };
		for (int c = 0; c < 16; c++) {
			if (c != fg && c != bg) {
				palette[c] = new byte[] { 127, 127, 127 };
			}
		}
		return palette;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
	 */
	@Override
	public int mapColor(int pixel, int[] distA) {
		int lum = ColorMapUtils.getPixelLum(pixel);
		distA[0] = lum;
		return lum <= midLum ? 0 : 1;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestColor(int[])
	 */
	@Override
	public int getClosestPaletteEntry(int pixel) {
		int lum = ColorMapUtils.getPixelLum(pixel);
		return lum <= midLum ? fg : bg;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.image.IPaletteColorMapper#getClosestPalettePixel(int, int, int)
	 */
	@Override
	public int getClosestPalettePixel(int x, int y, int pixel) {
		int lum = ColorMapUtils.getPixelLum(pixel);
		return lum <= midLum ?  0 : -1;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.image.IPaletteMapper#getMinimalPaletteDistance()
	 */
	@Override
	public int getMinimalPaletteDistance() {
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.image.IPaletteMapper#getNumColors()
	 */
	@Override
	public int getNumColors() {
		return 2;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.image.IPaletteMapper#getPalette()
	 */
	@Override
	public byte[][] getPalette() {
		return palette;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.image.IPaletteColorMapper#getPalettePixel(int)
	 */
	@Override
	public int getPalettePixel(int c) {
		return c == fg ? 0 : -1;
	}

	/* (non-Javadoc)
	 * @see org.ejs.gui.images.IPaletteMapper#getPixelForGreyscaleMode(int)
	 */
	@Override
	public int getPixelForGreyscaleMode(int pixel) {
		return pixel;
	}
	/* (non-Javadoc)
	 * @see org.ejs.gui.images.IPaletteMapper#getRgbToGreyForGreyscaleMode(byte[])
	 */
	@Override
	public byte[] getRgbToGreyForGreyscaleMode(byte[] nrgb) {
		return nrgb;
	}

	/* (non-Javadoc)
	 * @see org.ejs.gui.images.IPaletteMapper#getRGBToGreyMap()
	 */
	@Override
	public TreeMap<Integer, byte[]> getGreyToRgbMap() {
		return V99ColorMapUtils.getGreyToRgbMap332();
	}

	/**
	 * @return
	 */
	public int getForeground() {
		return fg;
	}
	/**
	 * @return the bg
	 */
	public int getBackground() {
		return bg;
	}

	/**
	 * @return
	 */
	public int getMidLum() {
		return midLum;
	}
}