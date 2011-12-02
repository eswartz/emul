/**
 * 
 */
package v9t9.emulator.clients.builtin.video.image;

/**
 * @author ejs
 *
 */
public interface IRawColorMapper {

	int getDistance(int rgb1, int rgb2);
	int getRGBPixel(int rgb);
}
