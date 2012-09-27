/**
 * 
 */
package v9t9.video.imageimport;

import java.awt.image.BufferedImage;

/**
 * @author ejs
 * 
 */
public class ImageFrame {
	public BufferedImage image;
	public boolean isLowColor;
	public int delayMs;

	public ImageFrame(BufferedImage image, boolean isLowColor, int delayMs) {
		this.image = image;
		this.isLowColor = isLowColor;
		this.delayMs = delayMs;
	}

	public ImageFrame(BufferedImage image, boolean isLowColor) {
		this.image = image;
		this.isLowColor = isLowColor;

		this.delayMs = 0;
	}

}
