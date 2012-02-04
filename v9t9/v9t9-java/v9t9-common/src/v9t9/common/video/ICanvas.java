/**
 * 
 */
package v9t9.common.video;

/**
 * @author ejs
 *
 */
public interface ICanvas {

	void setSize(int x, int y);

	/** Get the full screen width (this includes any overscan and possibly extra pixels
	 * not intended to be seen). */
	int getWidth();

	/** Get the full screen width (this includes any overscan and possibly extra pixels
	 * not intended to be seen). */
	int getVisibleWidth();

	/** Get the nominal screen height. This does not count interlacing. */
	int getHeight();

	int getVisibleHeight();
	

	/** Get the delta for one pixel, in terms of the offset. 
	 * @see #getBitmapOffset(int, int) 
	 */ 
	int getPixelStride();
	/** Get the delta for one row, in terms of the offset. 
	 * @see #getBitmapOffset(int, int) 
	 */ 
	int getLineStride();
	
}
