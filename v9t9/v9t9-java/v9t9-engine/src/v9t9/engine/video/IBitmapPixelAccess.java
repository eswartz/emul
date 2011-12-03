/**
 * May 22, 2011
 */
package v9t9.engine.video;

/**
 * @author ejs
 *
 */
public interface IBitmapPixelAccess {
	int getWidth();
	int getHeight();
	/** Get native mode pixel value at the given col/row; 0-15 for most modes; 0-255 for gfx mode 7 */
	byte getPixel(int x, int y);
}
