/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.swt.graphics.ImageData;
import org.ejs.coffee.core.utils.Pair;

import v9t9.emulator.clients.builtin.video.VdpCanvas.Format;
import v9t9.engine.VdpHandler;

/**
 * This class handles converting arbitrary external images and 
 * converting them to fit in the current TMS9918A or V9938 mode.
 * @author ejs
 *
 */
public class ImageImport implements IBitmapPixelAccess {

	private ImageData imageData;
	private Format format;
	private byte[][] thePalette;
	
	
	/** the RGB-32 pixel for each palette entry */
	protected int[] palettePixels;
	/** mapping from RGB-32 pixel to each palette index */
	protected TreeMap<Integer, Integer> paletteToIndex;
	
	/** HSV values of each palette entry */
	protected float[][] palhsv;
	private final ImageDataCanvas canvas;
	private boolean paletteMappingDirty;
	private final VdpHandler vdp;

	public ImageImport(ImageDataCanvas canvas, VdpHandler vdp) {
		this.canvas = canvas;
		this.vdp = vdp;
		this.imageData = canvas.getImageData();
		this.format = canvas.getFormat();
		this.thePalette = canvas.getPalette();
	}

	private void getRGB(BufferedImage img, int x, int y, int[] rgb) {
		int pixel = img.getRGB(x, y);
		rgb[0] = (pixel & 0xff0000) >> 16;
		rgb[1] = (pixel & 0xff00) >> 8;
		rgb[2] = pixel & 0xff;
	}
	private void setRGB(BufferedImage img, int x, int y, int[] rgb) {
		int pixel = ((Math.max(0, Math.min(rgb[0], 255))) << 16)
			| ((Math.max(0, Math.min(rgb[1], 255))) << 8)
			| ((Math.max(0, Math.min(rgb[2], 255))));
		img.setRGB(x, y, pixel);
	}
	private void ditherRGB(BufferedImage img, int x, int y, int[] rgb, int sixteenths, int r_error, int g_error, int b_error) {
		getRGB(img, x, y, rgb);
		rgb[0] += sixteenths * r_error / 16;
		rgb[1] += sixteenths * g_error / 16;
		rgb[2] += sixteenths * b_error / 16;
		setRGB(img, x, y, rgb);

	}
	private int ditherize(BufferedImage img, int x, int y, int ncols, boolean limit8) {
		int[] prgb = { 0, 0, 0 };
		getRGB(img, x, y, prgb);
		int newPixel;
		
		byte[] rgb;
		int closest;
		if (format == Format.COLOR256_1x1) {
			rgb = VdpCanvas.getGRB333(prgb[1] >> 5, prgb[0] >> 5, prgb[2] >> 5);
			newPixel = getPixel(rgb);
			closest = newPixel;
		}
		else {
			closest = getClosestColor(ncols, prgb, limit8, Integer.MAX_VALUE);
			rgb = thePalette[closest];
			newPixel = palettePixels[closest];
		}
		img.setRGB(x, y, newPixel);
		
		int r_error = prgb[0] - (rgb[0] & 0xff);
		int g_error = prgb[1] - (rgb[1] & 0xff);
		int b_error = prgb[2] - (rgb[2] & 0xff);
		
		if (limit8) {
			r_error /= 4;
			g_error /= 4;
			b_error /= 4;
		}
		if (x + 1 < img.getWidth()) {
			// x+1, y
			ditherRGB(img, x + 1, y, prgb, 7, r_error, g_error, b_error);
		}
		if (y + 1 < img.getHeight()) {
			if (x > 0)
				ditherRGB(img, x - 1, y + 1, prgb, 3, r_error, g_error, b_error);
			ditherRGB(img, x, y + 1, prgb, 5, r_error, g_error, b_error);
			if (x + 1 < img.getWidth())
				ditherRGB(img, x + 1, y + 1, prgb, 1, r_error, g_error, b_error);
		}
		
		return closest;
	}
	

	private int getColorDistance(int c, float[] phsv, int[] prgb) {
		int dist;
		
		if (phsv[1] < 0.25 && palhsv[c][1] < 0.25) {
			// only select something with low saturation,
			// and match value
			float dh = 16; //(palhsv[c][0] - phsv[0]);	// range: 0-35
			float ds = (palhsv[c][1] - phsv[1]) * 256;
			float dv = (palhsv[c][2] - phsv[2]);
			
			dist = (int) ((dh * dh) + (ds * ds) + (dv * dv));
		} else {
			int dr = ((thePalette[c][0] & 0xff) - prgb[0]);
			int dg = ((thePalette[c][1] & 0xff) - prgb[1]);
			int db = ((thePalette[c][2] & 0xff) - prgb[2]);
			
			//float dv = Math.abs(palhsv[c][2] - phsv[2]);
			
			dist = (int)((dr * dr) + (dg * dg) + (db * db) ) ;
		}
		
		return dist;
	}
	
	private int getColorDistance(int c, int[] prgb) {
		int dist;
		
		int dr = ((thePalette[c][0] & 0xff) - prgb[0]);
		int dg = ((thePalette[c][1] & 0xff) - prgb[1]);
		int db = ((thePalette[c][2] & 0xff) - prgb[2]);
		dist = (dr * dr) + (dg * dg) + (db * db);
		return dist;
	}


	private int getClosestColor(int ncols, int[] prgb, boolean limit8, int distLimit) {
		int closest = -1;
		int mindiff = Integer.MAX_VALUE;
		for (int c = (canvas.isClearFromPalette() ? 0 : 1); c < ncols; c++) {
			int dist;
			if (limit8) {
				float[] phsv = rgbToHsv(prgb);
				dist = getColorDistance(c, phsv, prgb);  	
			} else {
				dist = getColorDistance(c, prgb);
			}
			if (dist < distLimit && dist < mindiff) {
				closest = c;
				mindiff = dist;
			}
		}
		return closest;
	}

	float[] rgbToHsv(byte[] rgb) {
		return rgbToHsv(new int[] { rgb[0]&0xff, rgb[1]&0xff, rgb[2]&0xff } );
	}
	float[] rgbToHsv(int[] rgb) {
		float[] hsv = { 0, 0, 0 };
		int theMin = Math.min(Math.min(rgb[0], rgb[1]), rgb[2]);
		int theMax = Math.max(Math.max(rgb[0], rgb[1]), rgb[2]);
		hsv[2] = theMax;
        float delta = (theMax - theMin) + 0.0f;
        if (delta != 0)
        	hsv[1] = delta / theMax;
        else {
        	return hsv;
        }
        if (rgb[0] == theMax)
        	hsv[0] = (rgb[1] - rgb[2]) / delta;
        else if (rgb[1] == theMax)
        	hsv[0] = 2 + (rgb[2] - rgb[0]) / delta;
        else
        	hsv[0] = 4 + (rgb[0] - rgb[1]) / delta;
        hsv[0] *= 60.0;
        if (hsv[0] < 0)
        	hsv[0] += 360.0;
        return hsv;
	}

	/** Import image from 'img' and set the color indices in 'colorMap' which is #getVisibleWidth() by #getVisibleHeight() */
	public void setImageData(BufferedImage img) {
		if (format == null || format == Format.TEXT || format == Format.COLOR16_8x8)
			return;

		updatePaletteMapping();
		
		boolean limitDither = false;

		int ncols;
		if (format == Format.COLOR16_8x1 || format == Format.COLOR16_4x4) {
			ncols = 16;
			
			optimizeFor16Colors(img);
			//limitDither = true;
		}
		else if (format == Format.COLOR16_1x1) {
			ncols = 16;
			
			
			
			//optimizeFor16Colors(img);
			optimizeFor16ColorsAndRebuildPalette(img);
		}
		else if (format == Format.COLOR4_1x1) {
			ncols = 4;
		}
		else if (format == Format.COLOR256_1x1) {
			ncols = 256;
			
			optimizeFor256Colors(img);
		}
		else {
			return;
		}

		int xo;
		int yo;
		
		if (format == Format.COLOR16_4x4) {
			xo = (64 - img.getWidth()) / 2;
			yo = (48 - img.getHeight()) / 2;
		} else {
			xo = (canvas.getVisibleWidth() - img.getWidth() + canvas.getXOffset()) / 2;
			yo = (canvas.getVisibleHeight() - img.getHeight() + canvas.getYOffset()) / 2;
		}

		Arrays.fill(imageData.data, (byte) 0); 
		
		updatePaletteMapping();

		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				ditherize(img, x, y, ncols, limitDither);
			}
		}
		
		if (format == Format.COLOR16_8x1) {
			
			reduceBitmapMode(img, xo, yo);

		} else {

			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					imageData.setPixel(x + xo, y + yo, img.getRGB(x, y));
				}
			}
		}
	}

	/**
	 * @param img
	 * @param xo
	 * @param yo
	 */
	protected void reduceBitmapMode(BufferedImage img, int xo, int yo) {
		Map<Integer, Integer> histogram = new HashMap<Integer, Integer>();
		
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x += 8) {

				histogram.clear();
				
				int maxx = Math.min(x + 8, img.getWidth());
				
				for (int xd = x; xd < maxx; xd++) {
					int pixel = img.getRGB(xd, y);
					
					Integer cnt = histogram.get(pixel);
					if (cnt == null)
						cnt = 1;
					else
						cnt++;
					histogram.put(pixel, cnt);
				}
				
				// get prominent colors
				List<Pair<Integer, Integer>> sorted = new ArrayList<Pair<Integer,Integer>>();
				for (Map.Entry<Integer, Integer> entry : histogram.entrySet()) {
					sorted.add(new Pair<Integer, Integer>(entry.getKey(), entry.getValue()));
				}
				Collections.sort(sorted, new Comparator<Pair<Integer, Integer>>() {

					@Override
					public int compare(Pair<Integer, Integer> o1,
							Pair<Integer, Integer> o2) {
						return o2.second - o1.second;
					}
					
				});

				int fpixel, bpixel;
				if (sorted.size() >= 2) {
					fpixel = sorted.get(0).first;
					bpixel = sorted.get(1).first;
				} else {
					fpixel = bpixel = sorted.get(0).first;
				}
				
				for (int xd = x; xd < maxx; xd++) {
					int newPixel = img.getRGB(xd, y);
					
					if (newPixel != fpixel && newPixel != bpixel) {
						if (fpixel < bpixel) {
							newPixel = newPixel <= fpixel ? fpixel : bpixel;
						} else {
							newPixel = newPixel < bpixel ? fpixel : bpixel;
						}
					}
						
					imageData.setPixel(xd + xo, y + yo, newPixel);
				}
			}
		}
	}
	
	private static int scaleReduce(int rgb, int shift) {
		int val = (rgb >>> shift) & 0xff;
		val = ((val >>> 5)) & 0x7;
		return val;
		
	}
	
	/*
	private int scaleReduceBias(int rgb, int shift, boolean high) {
		int val = (rgb >>> shift) & 0xff;
		
		if (high) {
			val = val * 255 / 0xdf;
			if (val > 255)
				val = 255;
		}
		else
			val = val * 0xdf / 0xff;
		val = ((val >>> 5)) & 0x7;
		return val;
		
	}*/
	

	/**
	 * @param rgbs
	 * @param prgb
	 * @param i
	 * @return
	 */
	private int findPaletteColor(int rgb) {
		
		int prgb[] = { (rgb >> 16) & 0xff,
			(rgb >> 8) & 0xff,
			(rgb >> 0) & 0xff };
		int c = getClosestColor(16, prgb, true, Integer.MAX_VALUE);
		return c;
	}

	private interface IMapColor {
		/** Return a color index from mapping the RGB pixel 
		 * 
		 * @param rgb pixel in X8R8G8B8 format
		 * @param dist array for receiving distance² from the returned pixel
		 * @return the color index
		 */
		int mapColor(int rgb, int[] dist);
	}
	
	private class TI16MapColor implements IMapColor {

		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
		 */
		@Override
		public int mapColor(int rgb, int[] distA) {
			int prgb[] = { (rgb >> 16) & 0xff,
				(rgb >> 8) & 0xff,
				(rgb >> 0) & 0xff };
			
			float[] phsv = rgbToHsv(prgb);
			
			int closest = -1;
			int mindiff = Integer.MAX_VALUE;
			for (int c = (canvas.isClearFromPalette() ? 0 : 1); c < 16; c++) {
				int dist = getColorDistance(c, phsv, prgb);
				if (dist < mindiff) {
					closest = c;
					mindiff = dist;
				}
			}

			distA[0] = mindiff;
			return closest;
		}
		
	}


	private class RGB333MapColor implements IMapColor {

		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
		 */
		@Override
		public int mapColor(int rgb, int[] dist) {
			int r = scaleReduce(rgb, 16);
			int g = scaleReduce(rgb, 8);
			int b = scaleReduce(rgb, 0);
			
			// not actual RGB332 index!
			int c = (r << 6) | (g << 3) | b;
			
			byte[] rgbs = VdpCanvas.getGRB333(g, r, b);
			
			int dr = ((rgbs[0] & 0xff) - ((rgb >> 16) & 0xff));
			int dg = ((rgbs[1] & 0xff) - ((rgb >>  8) & 0xff));
			int db = ((rgbs[2] & 0xff) - ((rgb >>  0) & 0xff));
			
			dist[0] = (dr*dr) + (dg*dg) + (db*db);
			
			return c;
		}
		
	}


	private class RGB332MapColor implements IMapColor {

		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
		 */
		@Override
		public int mapColor(int rgb, int[] dist) {
			int r = scaleReduce(rgb, 16);
			int g = scaleReduce(rgb, 8);
			int b = scaleReduce(rgb, 0) & ~0x1;
			
			// not actual RGB332 index!
			int c = (r << 6) | (g << 3) | b;
			
			byte[] rgbs = VdpCanvas.getGRB333(g, r, b);
			
			int dr = ((rgbs[0] & 0xff) - ((rgb >> 16) & 0xff));
			int dg = ((rgbs[1] & 0xff) - ((rgb >>  8) & 0xff));
			int db = ((rgbs[2] & 0xff) - ((rgb >>  0) & 0xff));
			
			dist[0] = (dr*dr) + (dg*dg) + (db*db);
			
			return c;
		}
		
	}
	/**
	 * Build a histogram of the colors in the image once the 
	 * colors are reduced to the given palette with the given
	 * error.
	 * 
	 * @param img
	 * @param paletteMapper the means of mapping a palette
	 * @param hist histogram; count of pixels per each mapped color
	 * @param maxDist maximum distance² for a pixel beyond which
	 * it will not be counted in the histogram
	 * @return the number of colors detected (# of non-zero entries in hist[])
	 */
	private int buildHistogram(BufferedImage img, 
			IMapColor paletteMapper,
			int maxDist,
			final int[] hist,
			final Integer[] indices
			) 
	{
		int[] rgbs = new int[img.getWidth()];
		int[] distA = { 0 };
		for (int y = 0; y < img.getHeight(); y++) {
			img.getRGB(0, y, img.getWidth(), 1, rgbs, 0, rgbs.length);
			for (int i = 0; i < rgbs.length; i++) {
				int rgb = rgbs[i];
				int c = paletteMapper.mapColor(rgb, distA);
				if (distA[0] <= maxDist) {
					hist[c]++;
				}
			}
		}
		for (int i = 0; i < indices.length; i++)
			indices[i] = i;
		
		Arrays.sort(indices, new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				return hist[o2] - hist[o1];
			}
		});
		
		// count actual colors
		int interestingColors = 0;
		while (interestingColors < 512) {
			int idx = indices[interestingColors];
			if (hist[idx] == 0)
				break;
			interestingColors++;
		}

		return interestingColors;
	}


	/**
	 * Update the images for the optimal distribution of colors in the fixed
	 * 16 color palette, presumably bitmap mode or multicolor mode,
	 * where dithering will not do a lot of good.
	 * 
	 * We cannot control the palette in this mode, but we can avoid unwanted
	 * dithering, e.g. in a cartoon or line art, by replacing the most important
	 * colors with their closest entries in the palette.
	 * 
	 * @param img
	 */
	private void optimizeFor16Colors(BufferedImage img) {
		TI16MapColor mapColor = new TI16MapColor();
		
		int minDist = Integer.MAX_VALUE;
		for (int c = 1; c < 16; c++) {
			for (int d = c + 1; d < 16; d++) {
				int[] prgb = { thePalette[d][0] & 0xff,
						thePalette[d][1] & 0xff, 
						thePalette[d][2] & 0xff 
				};
				int dist = getColorDistance(c, palhsv[d], prgb);
				if (dist < minDist)
					minDist = dist;
			}
		}
		System.out.println("Minimum 16-color palette distance: " + minDist);
		

		final int[] hist = new int[16];
		final Integer[] indices = new Integer[16];
		
		int interestingColors = buildHistogram(img, mapColor, minDist, hist, indices);

		int usedColors = Math.min(16, interestingColors);
		
		boolean highColors = interestingColors >= 12;
		
		System.out.println("16: interestingColors="+interestingColors+"; usedColors="+usedColors+"; highColor="+highColors);
		
		int[] rgbs = new int[img.getWidth()];
		
		int replaceLimit = usedColors;
		
		for (int i = 0; i < replaceLimit; i++) {
			// ensure there will be an exact match so no dithering 
			// occurs on the primary occurrences of this color
			int idx = indices[i];
			System.out.println("Replacing " + idx);
			byte[] rgb = canvas.getRGB(idx);
			int newRGB = ((rgb[0] & 0xff) << 16) | ((rgb[1] & 0xff) << 8) | ((rgb[2] & 0xff));
			replaceColor16(img, rgbs, idx, newRGB);
		}
	}

	/**
	 * @param img
	 * @param rgbs 
	 * @param r
	 * @param g
	 * @param b
	 * @param rgb
	 */
	private void replaceColor16(BufferedImage img, int[] rgbs, int theC, int newRGB) {
		for (int y = 0; y < img.getHeight(); y++) {
			img.getRGB(0, y, img.getWidth(), 1, rgbs, 0, rgbs.length);
			for (int i = 0; i < rgbs.length; i++) {
				int rgb = rgbs[i];
				int c = findPaletteColor(rgb);
				if (c == theC) {
					rgbs[i] = newRGB;
				}
			}
			img.setRGB(0, y, img.getWidth(), 1, rgbs, 0, rgbs.length);
		}

	}

	
	/**
	 * Update the palette for the optimal distribution of colors.
	 * 
	 * The V9938 palette only has 3 bits of precision for each of R,G,B
	 * so we just reduce each color to 3-3-3 and make a histogram.
	 * 
	 * The histogram is sorted by frequency of colors.
	 * 
	 * The histogram may show a small number of "important" colors,
	 * e.g. in a cartoon.  Or it may have a medium number of important
	 * colors, for art.  Or it may have a large number of
	 * colors all relatively of the same importance, in a photograph.
	 * 
	 * So, rather than taking the 15 most prominent colors wholesale,
	 * which may leave valid but rarely occurring colors stranded, we take
	 * 4 or 8 (depending on color variety) of the most often occurring
	 * colors and replace them directly in the image so they will not
	 * be dithered.  Then we take an exponentially increasing sample
	 * of the other colors appearing in the image for the remainder
	 * of the palette. 
	 * 
	 * @param img
	 */
	private void optimizeFor16ColorsAndRebuildPalette(BufferedImage img) {
		
		final int[] hist = new int[512];
		final Integer[] indices = new Integer[512];
		
		// 0xff --> 0xe0 for R, G, B
		int maxDist = 0x1f*0x1f * 3;
		
		int interestingColors = buildHistogram(img, new RGB333MapColor(), maxDist, hist, indices);

		int usedColors = Math.min(16, interestingColors);
		
		boolean highColors = interestingColors >= 128;
		
		System.out.println("interestingColors="+interestingColors+"; usedColors="+usedColors+"; highColor="+highColors);
		
		/* select colors, biasing toward the most prominent:
		 
		 index = exp(i * K) - 1
		 
		 interestingColors = exp(usedColors * K) - 1
		 interestingColors + 1 = exp(usedColors * K)
		 ln(interestingColors + 1) = usedColors * K
		 ln(interestingColors + 1) / usedColors = K
		
		*/
		
		double K = Math.log(interestingColors - usedColors / 2) / usedColors;
		
		int prev = -1;
		int[] rgbs = new int[img.getWidth()];
		int replaceLimit = (highColors ? 4 : 8);
		for (int i = 0; i < usedColors; i++) {
			int c = i;
			int index;
			if (interestingColors == usedColors || i < usedColors / 2) {
				index = i;
			} else {
				//int idx = indices.get(i * interestingColors / usedColors);
				//int index = interestingColors - (int) (interestingColors * Math.log10(1 + i * K)) - 1;
				index = (int) Math.expm1(i * K) + usedColors / 2;
				if (index == prev)
					index++;
			}
			//System.out.println("index="+index);
			prev = index;
			int idx = indices[index];
			int r = (idx>>6) & 0x7;
			int g = (idx>>3) & 0x7;
			int b = (idx>>0) & 0x7;
			canvas.setGRB333(c, g, r, b);
			
			if ((interestingColors == usedColors || i < replaceLimit)) {
				// ensure there will be an exact match so no dithering 
				// occurs on the primary occurrences of this color
				byte[] rgb = canvas.getRGB(c);
				int newRGB = ((rgb[0] & 0xff) << 16) | ((rgb[1] & 0xff) << 8) | ((rgb[2] & 0xff));
				replaceColor(img, rgbs, r, g, b, newRGB);
			}
		}
	}

	/**
	 * Update the images for the optimal distribution of colors in the fixed
	 * 3-3-2 RGB palette.
	 * 
	 * We cannot control the palette in this mode, but we can avoid unwanted
	 * dithering, e.g. in a cartoon or line art, by replacing the most important
	 * colors with their closest entries in the palette.
	 * 
	 * We create a histogram over the 3-3-2 distribution of colors.
	 * 
	 * @param img
	 */
	private void optimizeFor256Colors(BufferedImage img) {

		final int[] hist = new int[512];
		final Integer[] indices = new Integer[512];
		
		// 0xff --> 0xe0 for R, G and 0xff -> 0xc0 for B
		int maxDist = 0x1f*0x1f * 2 + 0x3f*0x3f;
		
		int interestingColors = buildHistogram(img, new RGB332MapColor(), maxDist, hist, indices);
		
		int usedColors = Math.min(256, interestingColors);
		
		boolean highColors = interestingColors >= 64;
		
		System.out.println("256: interestingColors="+interestingColors+"; usedColors="+usedColors+"; highColor="+highColors);
		
		byte[] rgb = { 0, 0, 0};
		int[] rgbs = new int[img.getWidth()];
		
		int replaceLimit = (highColors ? 64 : 128);
		for (int i = 0; i < usedColors; i++) {
			if (interestingColors == usedColors || i < replaceLimit) {
				// ensure there will be an exact match so no dithering 
				// occurs on the primary occurrences of this color
				
				int idx = indices[i];
				int r = (idx>>6) & 0x7;
				int g = (idx>>3) & 0x7;
				int b = (idx>>0) & 0x6;
				
				canvas.getGRB332(rgb, (byte)(idx >> 1));
				byte y = rgb[1]; rgb[1] = rgb[0]; rgb[0] = y;
				int newRGB = ((rgb[0] & 0xff) << 16) | ((rgb[1] & 0xff) << 8) | ((rgb[2] & 0xff));
				replaceColor(img, rgbs, r, g, b, newRGB);
			}
		}
	}

	/**
	 * @param img
	 * @param rgbs 
	 * @param r
	 * @param g
	 * @param b
	 * @param rgb
	 */
	private void replaceColor(BufferedImage img, int[] rgbs, int theR, int theG, int theB, int newRGB) {
		int bMask = format == Format.COLOR256_1x1 ? ~0x1 : ~0;
		
		for (int y = 0; y < img.getHeight(); y++) {
			img.getRGB(0, y, img.getWidth(), 1, rgbs, 0, rgbs.length);
			for (int i = 0; i < rgbs.length; i++) {
				int rgb = rgbs[i];
				int r = scaleReduce(rgb, 16);
				int g = scaleReduce(rgb, 8);
				int b = scaleReduce(rgb, 0) & bMask;
				if (r == theR && g == theG && b == theB) {
					rgbs[i] = newRGB;
				}
			}
			img.setRGB(0, y, img.getWidth(), 1, rgbs, 0, rgbs.length);
		}

	}

	public byte getPixel(int x, int y) {
		if (paletteMappingDirty) {
			updatePaletteMapping();
			if (paletteMappingDirty)
				return 0;
		}
		int p = imageData.getPixel(x, y) & 0xffffff;
		Integer c = paletteToIndex.get(p);
		if (c == null && format == Format.COLOR256_1x1) {
			// whhyyyy is someone losing precision?!
			c = paletteToIndex.get(paletteToIndex.ceilingKey(p));
		}
		if (c == null) {
			return 0;
		}
		return (byte) (int) c;
	}

	protected int getPixel(byte[] nrgb) {
		return ((nrgb[0] & 0xff) << 16) | ((nrgb[1] & 0xff) << 8) | (nrgb[2] & 0xff);
	}


	protected void updatePaletteMapping() {
		if (format == null || format == Format.TEXT || format == Format.COLOR16_8x8) 
			return;
			
		int ncols;
		if (format == Format.COLOR16_1x1 
				|| format == Format.COLOR16_8x1 
				|| format == Format.COLOR16_4x4) {
			ncols = 16;
		}
		else if (format == Format.COLOR4_1x1) {
			ncols = 4;
		}
		else if (format == Format.COLOR256_1x1) {
			ncols = 256;
		}
		else {
			return;
		}

		paletteToIndex = new TreeMap<Integer, Integer>();
		
		palettePixels = new int[ncols];
		palhsv = new float[16][];
		
		if (ncols < 256) {
			for (int c = 0; c < ncols; c++) {
				palettePixels[c] = getPixel(thePalette[c]);
				paletteToIndex.put(palettePixels[c], c);
				palhsv[c] = rgbToHsv(thePalette[c]);
			}
		} else {
			byte[] rgb = { 0, 0, 0};
			for (int c = 0; c < ncols; c++) {
				canvas.getGRB332(rgb, (byte) c);
				palettePixels[c] = getPixel(rgb);
				paletteToIndex.put(palettePixels[c], c);
			}
		}
		
		paletteMappingDirty = false;
	}

	/**
	 * @param format2
	 * @return
	 */
	public static boolean isModeSupported(Format format) {
		if (format == null || format == Format.TEXT || format == Format.COLOR16_8x8) 
			return false;
			
		return true;
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
    private BufferedImage getScaledInstance(Image img,
                                           int targetWidth,
                                           int targetHeight,
                                           Object hint,
                                           boolean higherQuality)
    {
        int type = BufferedImage.TYPE_INT_RGB;
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
	 * @param image
	 */
	public void importImage(Image image) {

		int targWidth = canvas.getVisibleWidth();
		int targHeight = canvas.getVisibleHeight();
		float aspect = targWidth * targHeight / 256.f  / 192.f;
		if (canvas.getFormat() == Format.COLOR16_4x4) {
			targWidth = 64;
			targHeight = 48;
			aspect = 1.0f;
		}
		int realWidth = image.getWidth(null);
		int realHeight = image.getHeight(null);
		if (realWidth < 0 || realHeight < 0) {
			throw new IllegalArgumentException("image has zero or negative size");
		}
		
		if (realWidth * targHeight * aspect > realHeight * targWidth) {
			targHeight = (int) (targWidth * realHeight / realWidth / aspect);
		} else {
			targWidth = (int) (targHeight * realWidth * aspect / realHeight);
		}
		
		BufferedImage scaled = getScaledInstance(image, targWidth, targHeight, 
				//RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, 
				RenderingHints.VALUE_INTERPOLATION_BILINEAR, 
				false);
		//System.out.println(scaled.getWidth(null) + " x " +scaled.getHeight(null));
		
		setImageData(scaled);

		synchronized (vdp) {
			vdp.getVdpModeRedrawHandler().importImageData(this);
		}

	}

}
