/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

/**
 * @author ejs
 *
 */
public interface ISpriteCanvas extends ICanvas {

	/** Draw eight pixels of an 8x1 row. 
	 * @param bitmask mask of rows visible from top-down 
	 * @param isLogicalOr */
	void drawEightSpritePixels(int x, int y, byte mem, byte fg, byte bitmask, boolean isLogicalOr); 
	/** Draw 16 (8 magnified) pixels of an 8x1 row. 
	 * @param bitmask mask of rows visible from top-down 
	 * @param isLogicalOr */
	void drawEightMagnifiedSpritePixels(int x, int y, byte mem, byte fg, short bitmask, boolean isLogicalOr);
	/** Draw 32 (8 magnified) pixels of an 8x1 row. 
	 * @param bitmask mask of rows visible from top-down 
	 * @param isLogicalOr */
	void drawEightDoubleMagnifiedSpritePixels(int x, int y, byte mem, byte fg, short bitmask, boolean isLogicalOr);
}
