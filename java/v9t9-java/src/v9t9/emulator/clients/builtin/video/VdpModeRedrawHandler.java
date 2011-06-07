/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

/**
 * The redraw handler manages the efficient update of the
 * VDP canvas ("background") given changes made to the VDP
 * memory.  
 * 
 * Any modifying write to VDP memory will be either
 * to one or more mapped areas (screen image table,
 * pattern table, sprite table, etc) or will be invisible
 * (i.e. not mapped to any memory that participates in drawing,
 * or e.g. in simpler graphics modes, affecting a pattern for 
 * which no character is in the screen image table).
 * 
 * The implementor of this interface then is responsible
 * for tracking what has changed on screen.  Effectively
 * this comes down to what screen blocks must be redrawn.
 * These may be directly obvious or may be propagated through
 * other changes (e.g. to the pattern table).
 * @author Ed
 *
 */
public interface VdpModeRedrawHandler {
	/**
	 * Record that the VDP memory at addr was changed.
	 * @param addr
	 * @return true if change will be visible on-screen
	 */
	boolean touch(int addr);
	
	/**
	 * Update the changed blocks (on the screen) according to relationships
	 * between the various update areas.
	 */
	void propagateTouches();

	/** 
	 * Given the touched blocks, redraw the bitmap,
	 * generate the redraw blocks representing the changed bits of the bitmap,
	 * and return the number of blocks updated.
	 * @param blocks array of at most 1024 blocks
	 * @param force force redraw of everything 
	 * @return number of blocks changed
	 */
	int updateCanvas(RedrawBlock[] blocks, boolean force);

	/**
	 * Clear the canvas
	 */
	void clear();

	/**
	 * Import data from the image data into video memory
	 * @param access 
	 */
	void importImageData(IBitmapPixelAccess access);

}