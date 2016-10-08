/**
 * 
 */
package v9t9.video.imageimport;

import java.awt.image.BufferedImage;

/**
 * Convert an image to the restrictions of the video mode
 * (beyond color mapping, which has already been done) 
 * @author ejs
 *
 */
public interface IModeConverter {

	/**
	 * @param convertedImage
	 * @param img
	 * @param xoffs
	 * @param yoffs
	 */
	void convert(BufferedImage convertedImage, BufferedImage img, int xoffs,
			int yoffs);

}
