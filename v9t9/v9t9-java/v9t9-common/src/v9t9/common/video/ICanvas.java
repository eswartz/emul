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
	int getLineStride();
	
	VdpColorManager getColorMgr();
}
