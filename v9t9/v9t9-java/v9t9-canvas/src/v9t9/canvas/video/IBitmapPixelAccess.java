/**
 * May 22, 2011
 */
package v9t9.canvas.video;

/**
 * @author ejs
 *
 */
public interface IBitmapPixelAccess {
	/** Get image width */
	int getWidth();
	/** Get image height */
	int getHeight();
	/** Get native mode pixel value at the given col/row; 0-15 for most modes; 0-255 for gfx mode 7 */
	byte getPixel(int x, int y);
}
