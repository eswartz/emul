/*
  VdpFormat.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.video;

public class VdpFormat {

	/** Tell how data is laid out */
	public enum Layout {
		TEXT,
		/** An 8x8 block on the screen maps to one of 256 patterns */
		PATTERN,
		/** Classic 99/4A bitmap mode: 2 colors per 8 pixels */ 
		BITMAP_2_PER_8,
		/** Multicolor mode -- like PATTERN or BITMAP but reduced in resolution */ 
		MULTICOLOR,
		/** Free bitmap mode: any color in any pixel */ 
		BITMAP,
		/** Apple ][ hi-res mode */
		APPLE2_HIRES,
	};
	
	/** Text mode */
	public static VdpFormat TEXT = new VdpFormat(Layout.TEXT, 2, false);
	/** Graphics mode, one color set per 8x8 block */
	public static VdpFormat COLOR16_8x8 = new VdpFormat(Layout.PATTERN, 16, false);
	/** Bitmap mode, one color set per 8x1 block */
	public static VdpFormat COLOR16_8x1 = new VdpFormat(Layout.BITMAP_2_PER_8, 16, false);
	/** Bitmap mode, two-color */
	public static VdpFormat COLOR2_8x1 = new VdpFormat(Layout.BITMAP, 2, false);
	/** Multicolor mode, one color set per 4x4 block */
	public static VdpFormat COLOR16_4x4 = new VdpFormat(Layout.MULTICOLOR, 16, false);
	/** V9938 bitmap mode, one color set per 8x1 block, sprite mode 2, palette set */
	public static VdpFormat COLOR16_8x1_9938 = new VdpFormat(Layout.BITMAP_2_PER_8, 16, true);
	/** V9938 16-color mode */
	public static VdpFormat COLOR16_1x1 = new VdpFormat(Layout.BITMAP, 16, true);
	/** V9938 4-color mode */
	public static VdpFormat COLOR4_1x1 = new VdpFormat(Layout.BITMAP, 4, true);
	/** V9938 256-color mode */
	public static VdpFormat COLOR256_1x1 = new VdpFormat(Layout.BITMAP, 256, true, true);
	
	private Layout layout;
	private int ncols;
	private boolean isMsx2;
	private boolean pixelIsColor;
	private boolean canSetPalette;

	public VdpFormat(Layout layout, int ncols, boolean isMsx2) {
		this.layout = layout;
		this.ncols = ncols;
		this.isMsx2 = isMsx2;
		canSetPalette = isMsx2;
	}
	public VdpFormat(Layout layout, int ncols, boolean isMsx2, boolean pixelIsColor) {
		this.layout = layout;
		this.ncols = ncols;
		this.isMsx2 = isMsx2;
		this.pixelIsColor = pixelIsColor;
		canSetPalette = !pixelIsColor;
	}

	
	@Override
	public String toString() {
		return "VdpFormat [layout=" + layout + ", ncols=" + ncols + ", isMsx2="
				+ isMsx2 + ", pixelIsColor=" + pixelIsColor
				+ ", canSetPalette=" + canSetPalette + "]";
	}
	/**
	 * @return the layout
	 */
	public Layout getLayout() {
		return layout;
	}
	
	/**
	 * @return the number of colors in the mode
	 */
	public int getNumColors() {
		return ncols;
	}
	/**
	 * @return true if the mode comes from the V9938 
	 */
	public boolean isMsx2() {
		return isMsx2;
	}
	/**
	 * @return true if a pixel maps to a palette entry and 
	 * false if the pixel encodes its own color
	 */
	public boolean isPaletted() {
		return !pixelIsColor;
	}
	/**
	 * @return
	 */
	public boolean canSetPalette() {
		return canSetPalette;
	}
}