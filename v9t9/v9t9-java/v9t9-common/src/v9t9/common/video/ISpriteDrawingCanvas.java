/**
 * 
 */
package v9t9.common.video;

/**
 * @author ejs
 *
 */
public interface ISpriteDrawingCanvas {

	public void drawEightDoubleMagnifiedSpritePixels(int x, int y, byte mem, byte fg, short bitmask, boolean isLogicalOr);
	public void drawEightMagnifiedSpritePixels(int x, int y, byte mem, byte fg, short bitmask, boolean isLogicalOr);
	public void drawEightSpritePixels(int x, int y, byte mem, byte fg, byte bitmask, boolean isLogicalOr);
	/**
	 * @return
	 */
	public VdpColorManager getColorMgr();
	/**
	 * @return
	 */
	public int getHeight();
	/**
	 * @return
	 */
	public int getWidth();
	/**
	 * @return
	 */
	public int getVisibleWidth();


}
