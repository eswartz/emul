/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

/**
 * @author ejs
 *
 */
public interface ICanvas {

	public abstract void setSize(int x, int y);

	/** Get the full screen width (this includes any overscan and possibly extra pixels
	 * not intended to be seen). */
	public abstract int getWidth();

	/** Get the full screen width (this includes any overscan and possibly extra pixels
	 * not intended to be seen). */
	public abstract int getVisibleWidth();

	/** Get the nominal screen height. This does not count interlacing. */
	public abstract int getHeight();

	public abstract int getVisibleHeight();
	public int getLineStride();
	
	/**
	 * Tell whether the color 0 is transparent or a color in the
	 * palette.
	 * @return true: clear (color 0) is a palette color, false: transparent
	 */
	boolean isClearFromPalette();

}