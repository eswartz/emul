package v9t9.emulator.clients.builtin.video.image;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.TreeMap;

public class ColorMapUtils {
	private ColorMapUtils() {
	}

	public static float[] rgbToHsv(byte[] rgb) {
		float[] hsv = { 0, 0, 0 };
		rgbToHsv(rgb[0] & 0xff, rgb[1] & 0xff, rgb[2] & 0xff, hsv);
		return hsv;
	}

	public static void rgbToHsv(byte[] rgb, float[] hsv) {
		rgbToHsv(rgb[0] & 0xff, rgb[1] & 0xff, rgb[2] & 0xff, hsv);
	}

	/**
	 * Get hue/value/satuation.
	 * 
	 * @param r
	 * @param g
	 * @param b
	 * @param hsv
	 *            in 0-360, 0-1, 0-255
	 */
	public static void rgbToHsv(int r, int g, int b, float[] hsv) {
		int theMin = Math.min(Math.min(r, g), b);
		int theMax = Math.max(Math.max(r, g), b);
		hsv[2] = theMax;
		float delta = (theMax - theMin);
		if (delta != 0)
			hsv[1] = delta / theMax;
		else {
			hsv[1] = 0;
			return;
		}
		if (r == theMax)
			hsv[0] = (g - b) / delta;
		else if (g == theMax)
			hsv[0] = 2 + (b - r) / delta;
		else
			hsv[0] = 4 + (r - g) / delta;
		hsv[0] *= 60.0;
		if (hsv[0] < 0)
			hsv[0] += 360.0;
	}

	/**
	 * Get hue/value/satuation.
	 * 
	 * @param rgb
	 * @param hsv
	 *            in 0-360, 0-1, 0-255
	 */
	public static void rgbToHsv(int[] rgb, float[] hsv) {
		rgbToHsv(rgb[0], rgb[1], rgb[2], hsv);
	}

	public static void hsvToRgb(float h, float s, float v, int[] rgb) {
		float c = (v / 256) * s;
		float hprime = (h / 60);
		float x = c * (1 - Math.abs(hprime % 2 - 1));

		float fr, fg, fb;
		switch ((int) hprime) {
		case 0:
			fr = c;
			fg = x;
			fb = 0;
			break;
		case 1:
			fr = x;
			fg = c;
			fb = 0;
			break;
		case 2:
			fr = 0;
			fg = c;
			fb = x;
			break;
		case 3:
			fr = 0;
			fg = x;
			fb = c;
			break;
		case 4:
			fr = x;
			fg = 0;
			fb = c;
			break;
		case 5:
			fr = c;
			fg = 0;
			fb = x;
			break;
		default:
			fr = fg = fb = 0;
		}

		float m = (v / 256) - c;
		rgb[0] = (int) ((fr + m) * 255);
		rgb[1] = (int) ((fg + m) * 255);
		rgb[2] = (int) ((fb + m) * 255);
	}

	public static int getRGBDistance(byte[] rgb, int[] prgb) {
		int dist;

		int dr = ((rgb[0] & 0xff) - prgb[0]);
		int dg = ((rgb[1] & 0xff) - prgb[1]);
		int db = ((rgb[2] & 0xff) - prgb[2]);

		dist = (dr * dr) + (dg * dg) + (db * db);
		return dist;
	}

	/**
	 * @param i
	 * @param newRGB
	 * @return
	 */
	public static int getPixelDistance(int pixel, int newRGB) {
		int dr = ((pixel & 0xff0000) - (newRGB & 0xff0000)) >> 16;
		int dg = ((pixel & 0x00ff00) - (newRGB & 0x00ff00)) >> 8;
		int db = ((pixel & 0x0000ff) - (newRGB & 0x0000ff)) >> 0;
		return dr * dr + dg * dg + db * db;
	}

	public static int getRGBDistance(byte[][] palette, int c, int[] prgb) {
		return getRGBDistance(palette[c], prgb);
	}

	public static int getClosestColorByDistance(byte[][] thePalette, int first,
			int count, int[] prgb, int exceptFor) {
		int mindiff = Integer.MAX_VALUE;
		int closest = -1;

		for (int c = first; c < count; c++) {
			if (c != exceptFor) {
				int dist = getRGBDistance(thePalette, c, prgb);
				if (dist < mindiff) {
					mindiff = dist;
					closest = c;
				}
			}
		}

		if (closest == -1)
			closest = exceptFor;
		return closest;
	}


	public static int getClosestColorByDistanceAndHSV(byte[][] thePalette, int first,
			int count, int[] prgb, int exceptFor) {
		int mindiff = Integer.MAX_VALUE;
		int closest = -1;

		// first, only pick greys for low hue cases
		float[] phsv = { 0, 0, 0 };
		rgbToHsv(prgb, phsv);

		if (phsv[1] < (phsv[0] >= 30 && phsv[0] < 75 ? 0.66f : 0.33f)) {
			float[] hsv = { 0, 0, 0 };
			int whiteColor = -1, blackColor = -1;
			float whiteVal = Float.MAX_VALUE;
			float blackVal = Float.MAX_VALUE;
			for (int c = first; c < count; c++) {
				if (c != exceptFor) {
					rgbToHsv(thePalette[c], hsv);
					if (Math.abs(phsv[1] - hsv[1]) < 0.1f) {
						int dist = Math.abs(Math.round(phsv[2] - hsv[2]));
						if (dist < mindiff) {
							mindiff = dist;
							closest = c;
						}
					}
					if (hsv[2] > whiteVal) {
						whiteColor = c;
						whiteVal = hsv[2];
					}
					if (hsv[2] < blackVal) {
						blackColor = c;
						blackVal = hsv[2];
					}
				}
			}
			if (closest == -1) {
				// must be white or black
				if (phsv[2] < 0.5f)
					closest = blackColor;
				else
					closest = whiteColor;
			}
		}
		if (closest == -1) {
			closest = getClosestColorByDistance(thePalette, first, count, prgb, exceptFor);
		}

		if (closest == -1)
			closest = exceptFor;
		return closest;
	}

	public static int rgb8ToPixel(byte[] nrgb) {
		return ((nrgb[0] & 0xff) << 16) | ((nrgb[1] & 0xff) << 8)
				| (nrgb[2] & 0xff);
	}

	public static int rgb8ToPixel(int[] prgb) {
		return ((prgb[0]) << 16) | ((prgb[1]) << 8) | (prgb[2]);
	}

	public static void pixelToRGB(int pixel, int[] prgb) {
		prgb[0] = (pixel & 0xff0000) >> 16;
		prgb[1] = (pixel & 0xff00) >> 8;
		prgb[2] = pixel & 0xff;
	}

	public static void pixelToRGB(int pixel, byte[] rgb) {
		rgb[0] = (byte) ((pixel & 0xff0000) >> 16);
		rgb[1] = (byte) ((pixel & 0xff00) >> 8);
		rgb[2] = (byte) (pixel & 0xff);
	}

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
	 * Quick histogram: return number of unique colors, when masking
	 * out @mask bits.
	 * @param image
	 * @param mask
	 * @return count
	 */
	public static int quickHist(BufferedImage image, int mask) {
		int rgbByte = (0xff00 & ~(0xff << mask)) >> 8;
		int rgbMask = (rgbByte << 16) | (rgbByte << 8) | rgbByte;
	
		TreeMap<Integer, Integer> hist = new TreeMap<Integer, Integer>();
		int[] rgbs = new int[image.getWidth()];
		for (int y = 0; y < image.getHeight(); y++) {
			image.getRGB(0, y, rgbs.length, 1, rgbs, 0, rgbs.length);
			for (int x = 0; x < rgbs.length; x++) {
				int key = rgbs[x] & rgbMask;
				Integer count = hist.get(key);
				if (count == null)
					hist.put(key, 1);
				else
					hist.put(key, count + 1);
			}
		}
		System.out.println("For mask size " + mask +"; " + hist.size() + " colors");
		return hist.size();
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
