/**
 * 
 */
package v9t9.video.imageimport;

/**
 * @author ejs
 *
 */
public interface IRawColorMapper {

	int getDistance(int rgb1, int rgb2);
	int getRGBPixel(int rgb);
}
