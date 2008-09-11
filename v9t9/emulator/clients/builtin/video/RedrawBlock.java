package v9t9.emulator.clients.builtin.video;

/** 
 * This class represents a single block which needs to be
 * redrawn.
 * @author ejs
 *
 */
class RedrawBlock {
	/** pixel offset, 0=top */
	int r;
	/** pixel offset, 0=left */
	int c;
	/** width of block */
	int w;
	/** height of block */
	int h;
}