/**
 * 
 */
package v9t9.video.imageimport;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import v9t9.common.video.ColorMapUtils;

/**
 * @author ejs
 *
 */
public class AwtImageUtils {
	private AwtImageUtils() { }
	

	/**
	 * Convenience method that returns a scaled instance of the
	 * provided {@code BufferedImage}.
	 *
	 * @param img the original image to be scaled
	 * @param targetWidth the desired width of the scaled instance,
	 *    in pixels
	 * @param targetHeight the desired height of the scaled instance,
	 *    in pixels
	 * @param hint one of the rendering hints that corresponds to
	 *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
	 *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
	 *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
	 *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
	 * @param higherQuality if true, this method will use a multi-step
	 *    scaling technique that provides higher quality than the usual
	 *    one-step technique (only useful in downscaling cases, where
	 *    {@code targetWidth} or {@code targetHeight} is
	 *    smaller than the original dimensions, and generally only when
	 *    the {@code BILINEAR} hint is specified)
	 * @return a scaled version of the original {@code BufferedImage}
	 */
	public static BufferedImage getScaledInstance(Image img,
	                                       int targetWidth,
	                                       int targetHeight,
	                                       Object hint,
	                                       boolean higherQuality)
	{
	    int type = BufferedImage.TYPE_INT_ARGB;
	    BufferedImage ret = (BufferedImage)img;
	    int w, h;
	    if (higherQuality) {
	        // Use multi-step technique: start with original size, then
	        // scale down in multiple passes with drawImage()
	        // until the target size is reached
	        w = img.getWidth(null);
	        h = img.getHeight(null);
	    } else {
	        // Use one-step technique: scale directly from original
	        // size to target size with a single drawImage() call
	        w = targetWidth;
	        h = targetHeight;
	    }
	    
	    do {
	        if (higherQuality && w > targetWidth) {
	            w /= 2;
	            if (w < targetWidth) {
	                w = targetWidth;
	            }
	        }
	
	        if (higherQuality && h > targetHeight) {
	            h /= 2;
	            if (h < targetHeight) {
	                h = targetHeight;
	            }
	        }
	
	        BufferedImage tmp = new BufferedImage(w, h, type);
	        Graphics2D g2 = tmp.createGraphics();
	        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
	        g2.drawImage(ret, 0, 0, w, h, null);
	        g2.dispose();
	
	        ret = tmp;
	    } while (w != targetWidth || h != targetHeight);
	    
	    return ret;
	}

	/**
	 * Reduce colors in image by @mask bits.
	 * @param image
	 * @param mask
	 */
	public static void reduceColors(BufferedImage image, int mask) {
		int rgbByte = (0xff00 & ~(0xff << mask)) >> 8;
		int rgbMask = (rgbByte << 16) | (rgbByte << 8) | rgbByte;
	
		int[] prgb = { 0, 0, 0 };
		
		int[] rgbs = new int[image.getWidth()];
		for (int y = 0; y < image.getHeight(); y++) {
			image.getRGB(0, y, rgbs.length, 1, rgbs, 0, rgbs.length);
			for (int x = 0; x < rgbs.length; x++) {
				int rgb = rgbs[x] & rgbMask;
				ColorMapUtils.pixelToRGB(rgb, prgb);
				prgb[0] = (prgb[0] * 255 / rgbByte);
				prgb[1] = (prgb[1] * 255 / rgbByte);
				prgb[2] = (prgb[2] * 255 / rgbByte);
				rgb = ColorMapUtils.rgb8ToPixel(prgb) | (rgbs[x] & 0xff000000);
				rgbs[x] = rgb;
			}
			image.setRGB(0, y, rgbs.length, 1, rgbs, 0, rgbs.length);
		}
	}

}
