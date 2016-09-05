/*
  VdpFormat.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.video;

public enum VdpFormat {
	/** Text mode */
	TEXT(2, false),
	/** Graphics mode, one color set per 8x8 block */
	COLOR16_8x8(16, false),
	/** Bitmap mode, one color set per 8x1 block */
	COLOR16_8x1(16, false),
	/** Multicolor mode, one color set per 4x4 block */
	COLOR16_4x4(16, false),
	/** V9938 bitmap mode, one color set per 8x1 block, sprite mode 2, palette set */
	COLOR16_8x1_9938(16, true),
	/** V9938 16-color mode */
	COLOR16_1x1(16, true),
	/** V9938 4-color mode */
	COLOR4_1x1(4, true),
	/** V9938 256-color mode */
	COLOR256_1x1(256, true);
	
	private int ncols;
	private boolean isMsx2;

	private VdpFormat(int ncols, boolean isMsx2) {
		this.ncols = ncols;
		this.isMsx2 = isMsx2;
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
}