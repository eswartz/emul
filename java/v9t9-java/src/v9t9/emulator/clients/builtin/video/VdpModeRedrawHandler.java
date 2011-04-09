/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

public interface VdpModeRedrawHandler {
	/**
	 * Record that the VDP memory at addr was changed.
	 * @param addr
	 * @return true if change will be visible on-screen
	 */
	boolean touch(int addr);
	
	/**
	 * Update the changed blocks according to relationships
	 * between the various update areas.
	 * @param modeInfo
	 * @param changes incoming and outgoing information 
	 */
	void propagateTouches();

	/** 
	 * Given the touched blocks, redraw the bitmap,
	 * generate the redraw blocks representing the changed bits of the bitmap,
	 * and return the number of blocks updated.
	 * @param blocks array of at most 1024 blocks
	 * @param force force redraw of everything 
	 * @param modeInfo
	 * @param changes
	 * @return number of blocks changed
	 */
	int updateCanvas(RedrawBlock[] blocks, boolean force);

	/**
	 * Clear the canvas
	 */
	void clear();

	/**
	 * Import data from the image data into video memory
	 */
	void importImageData();

}