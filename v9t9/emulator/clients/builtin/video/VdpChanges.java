/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

public class VdpChanges
{
	final static int SC_UNTOUCHED = 0;
	final static int SC_BACKGROUND = 1;
	final static int SC_SPRITE_DELETED = 2;
	final static int SC_SPRITE_COVERING = 3;

	/** Tell if the block at the given screen offset was changed 
	 * @see #SC_UNTOUCHED 
	 * @see #SC_BACKGROUND 
	 * @see #SC_SPRITE_DELETED
	 * @see #SC_SPRITE_COVERING
	 */
	byte	screen[] = new byte[1024];		// 1: block changed
	/** Tell if the pattern (8x8) at the given table offset was changed */
	byte	patt[] = new byte[768];			// 1: pattern changed
	/** Tell if the color (8x8) at the given table offset was changed */
	byte	color[] = new byte[768];			// 1: color changed
	/** Tell if the sprite (bitmask 1<<#) was changed */
	int sprite;				// (1<<x): sprite #x changed
	/** Tell if the sprite pattern (8x8) was changed */
	byte	sprpat[] = new byte[256];		// 1: sprite pattern changed
}