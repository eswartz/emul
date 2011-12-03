/**
 * 
 */
package v9t9.engine.video;

public class VdpChanges
{
	// if anything changed in the tables
	public boolean 	changed;
	// if set, just redraw it all
	public boolean	fullRedraw;
	
	final public static int SC_UNTOUCHED = 0;
	final public static int SC_BACKGROUND = 1;
	final public static int SC_SPRITE_DELETED = 2;
	final public static int SC_SPRITE_COVERING = 3;

	/** Tell if the block at the given screen offset was changed 
	 * @see #SC_UNTOUCHED 
	 * @see #SC_BACKGROUND 
	 * @see #SC_SPRITE_DELETED
	 * @see #SC_SPRITE_COVERING
	 */
	public byte	screen[];					// 1: block changed
	/** Tell if the pattern (8x8) at the given table offset was changed */
	public byte	patt[] = new byte[768];			// 1: pattern changed
	/** Tell if the color (8x8) at the given table offset was changed */
	public byte	color[] = new byte[768];			// 1: color changed
	/** Tell if the sprite (bitmask 1<<#) was changed */
	public int 	sprite;				// (1<<x): sprite #x changed
	/** Tell if the sprite pattern (8x8) was changed */
	public byte	sprpat[] = new byte[256];		// 1: sprite pattern changed
	
	public VdpChanges(int maxRedrawblocks) {
		screen = new byte[maxRedrawblocks];
	}
}