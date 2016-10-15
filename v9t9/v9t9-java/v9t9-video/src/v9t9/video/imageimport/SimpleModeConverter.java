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
	 * @see v9t9.video.imageimport.IModeConverter#prepareImage(java.awt.image.BufferedImage)
	 */
	@Override
	public BufferedImage prepareImage(BufferedImage img) {
		return img;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.video.imageimport.IModeConverter#convert(java.awt.image.BufferedImage, java.awt.image.BufferedImage, int, int)
	 */
	@Override
	public BufferedImage convert(BufferedImage img,
			int targWidth, int targHeight) {
		
		int xoffs, yoffs;

		xoffs = (targWidth - img.getWidth()) / 2;
		yoffs = (targHeight - img.getHeight()) / 2;
		
		BufferedImage convertedImage = new BufferedImage(targWidth, targHeight, 
						BufferedImage.TYPE_3BYTE_BGR);

		
		int h = img.getHeight();
		int w = img.getWidth();
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				convertedImage.setRGB(x + xoffs, y + yoffs, img.getRGB(x, y));
			}
		}
		
		return convertedImage;
	}

}
