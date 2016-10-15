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
	 * @param img
	 * @return
	 */
	BufferedImage prepareImage(BufferedImage img);

	/**
	 * /**
	 * Take the image, which has been palette-mapped and/or dithered, and
	 * create a version that follows the rules of the VdpFormat.
	 * @param img
	 * @param targWidth
	 * @param targHeight
	 */
	BufferedImage convert(BufferedImage img, int targWidth, int targHeight);


}
