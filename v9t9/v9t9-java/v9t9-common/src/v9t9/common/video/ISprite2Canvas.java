/**
 * 
 */
package v9t9.common.video;


/**
 * @author ejs
 *
 */
public interface ISprite2Canvas extends ICanvas {

	void clear8x8Block(int offset);

	byte getColorAtOffset(int offset);

	/**
	 * @param x
	 * @param y
	 * @return
	 */
	int getBitmapOffset(int x, int y);

	/**
	 * @param i
	 */
	void setClearColor(int i);

	/**
	 * @param spriteCanvas
	 * @param i
	 * @param j
	 * @param blockMag
	 */
	//void blitSpriteBlock(ISprite2Canvas spriteCanvas, int i, int j, int blockMag);

	/**
	 * @param spriteCanvas
	 * @param i
	 * @param j
	 * @param blockMag
	 */
	//void blitFourColorSpriteBlock(ISprite2Canvas spriteCanvas, int i, int j,
	//		int blockMag);

	public void drawEightDoubleMagnifiedSpritePixels(int x, int y, byte mem, byte fg, short bitmask, boolean isLogicalOr);
	public void drawEightMagnifiedSpritePixels(int x, int y, byte mem, byte fg, short bitmask, boolean isLogicalOr);
	public void drawEightSpritePixels(int x, int y, byte mem, byte fg, byte bitmask, boolean isLogicalOr);



}