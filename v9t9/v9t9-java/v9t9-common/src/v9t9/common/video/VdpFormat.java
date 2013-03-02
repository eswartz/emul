/*
  VdpFormat.java

  (c) 2011-2013 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
	 * @return the ncols
	 */
	public int getNumColors() {
		return ncols;
	}
	/**
	 * @return the isMsx2
	 */
	public boolean isMsx2() {
		return isMsx2;
	}
}