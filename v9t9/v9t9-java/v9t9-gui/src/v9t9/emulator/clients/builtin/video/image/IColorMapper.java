/**
 * 
 */
package v9t9.emulator.clients.builtin.video.image;

/**
 * @author ejs
 *
 */
interface IColorMapper {

	/** Return a color index from mapping the RGB pixel 
	 * 
	 * @param pixel pixel in X8R8G8B8 format
	 * @param dist array for receiving distance² from the returned pixel
	 * @return the color index
	 */
	public abstract int mapColor(int pixel, int[] dist);

}