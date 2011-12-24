/**
 * 
 */
package v9t9.common.video;

import java.util.BitSet;


/**
 * @author ejs
 *
 */
public interface ISpriteCanvas {
	/**
	 * @return
	 */
	VdpSprite[] getSprites();

	/**
	 * @param numchars
	 */
	void setNumSpriteChars(int numchars);

	/**
	 * @param isMag
	 */
	void setMagnified(boolean isMag);
	int updateSpriteCoverage(ICanvas canvas, BitSet screen,
			boolean forceRedraw);
	/**
	 * @param canvas
	 */
	void drawSprites(ISpriteDrawingCanvas canvas, boolean force);
}
