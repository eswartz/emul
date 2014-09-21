/**
 * 
 */
package v9t9.video;

/**
 * @author ejs
 *
 */
public interface IVdpModeRedrawHandler {

	int getCharsPerRow();
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
	void prepareUpdate();

}