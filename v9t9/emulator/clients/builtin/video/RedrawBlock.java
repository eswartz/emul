package v9t9.emulator.clients.builtin.video;

/** 
 * This class represents a single block which needs to be
 * redrawn.
 * @author ejs
 *
 */
public class RedrawBlock {
	/** pixel offset, 0=top */
	public int r;
	/** pixel offset, 0=left */
	public int c;
	/** width of block */
	public int w;
	/** height of block */
	public int h;
}