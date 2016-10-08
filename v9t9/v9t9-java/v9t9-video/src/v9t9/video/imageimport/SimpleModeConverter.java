/**
 * 
 */
package v9t9.video.imageimport;

import java.awt.image.BufferedImage;

/**
 * @author ejs
 *
 */
public class SimpleModeConverter implements IModeConverter {

	/* (non-Javadoc)
	 * @see v9t9.video.imageimport.IModeConverter#convert(java.awt.image.BufferedImage, java.awt.image.BufferedImage, int, int)
	 */
	@Override
	public void convert(BufferedImage convertedImage, BufferedImage img,
			int xoffs, int yoffs) {
		int h = img.getHeight();
		int w = img.getWidth();
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				convertedImage.setRGB(x + xoffs, y + yoffs, img.getRGB(x, y));
			}
		}
	}

}
