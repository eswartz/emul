/**
 * 
 */
package v9t9.common.video;

public enum VdpFormat {
	/** Text mode */
	TEXT(2),
	/** Graphics mode, one color set per 8x8 block */
	COLOR16_8x8(16),
	/** Bitmap mode, one color set per 8x1 block */
	COLOR16_8x1(16),
	/** Multicolor mode, one color set per 4x4 block */
	COLOR16_4x4(16),
	/** V9938 16-color mode */
	COLOR16_1x1(16),
	/** V9938 4-color mode */
	COLOR4_1x1(4),
	/** V9938 256-color mode */
	COLOR256_1x1(256);
	
	private int ncols;

	private VdpFormat(int ncols) {
		this.ncols = ncols;
	}
	/**
	 * @return the ncols
	 */
	public int getNumColors() {
		return ncols;
	}
}