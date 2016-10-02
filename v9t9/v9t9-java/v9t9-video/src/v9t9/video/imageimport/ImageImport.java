/*
  ImageImport.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.imageimport;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.ejs.gui.images.AwtImageUtils;
import org.ejs.gui.images.ColorMapUtils;
import org.ejs.gui.images.ColorOctree;
import org.ejs.gui.images.ColorOctree.LeafNode;
import org.ejs.gui.images.FixedPaletteMapColor;
import org.ejs.gui.images.Histogram;
import org.ejs.gui.images.IDirectColorMapper;
import org.ejs.gui.images.IPaletteColorMapper;
import org.ejs.gui.images.IPaletteMapper;
import org.ejs.gui.images.MonoMapColor;
import org.ejs.gui.images.UserPaletteMapColor;
import org.ejs.gui.images.V99ColorMapUtils;

import v9t9.common.video.IVdpCanvas;
import v9t9.common.video.VdpColorManager;
import v9t9.common.video.VdpFormat;
import v9t9.common.video.VdpFormat.Layout;
import v9t9.video.imageimport.ImageImportOptions.Dither;
import v9t9.video.imageimport.ImageImportOptions.PaletteOption;
import ejs.base.utils.Pair;

/**
 * This class handles converting arbitrary external images and 
 * converting them to fit in the current TMS9918A or V9938 mode.
 * @author ejs
 *
 */
public class ImageImport {
	private boolean DEBUG = false;

	//private BufferedImage convertedImage;
	private VdpFormat format;
	private byte[][] thePalette;

	private boolean useColorMappedGreyScale;
	private Dither ditherType;
	private boolean ditherMono;
	private PaletteOption paletteOption;

	private final VdpColorManager colorMgr;
	private boolean paletteMappingDirty;
	//private final IVdpChip vdp;
	private int firstColor;


	/** mapping from RGB-32 pixel to each palette index */
	protected TreeMap<Integer, Integer> paletteToIndex;
	private Pair<Integer,Integer>[][] bitmapColors;

	private ColorOctree octree;
	
	private boolean convertGreyScale;

	private IPaletteMapper mapColor;

	private float gamma = 1.0f;

	private boolean tryDirectMapping = true;

	private boolean flattenGreyScale;

	public ImageImport(IVdpCanvas canvas) {
		synchronized (canvas) {
			this.colorMgr = canvas.getColorMgr();
		}
	}
	public ImageImport(VdpColorManager colorMgr) {
		this.colorMgr = colorMgr;
	}
	
	private final int clamp(int i) {
		return i < 0 ? 0 : i > 255 ? 255 : i;
	}

	// https://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering
	private void ditherFSPixel(BufferedImage img, IPaletteColorMapper mapColor,
			Histogram hist, int x, int y) {
		
		int pixel = img.getRGB(x, y);

		int newC = mapColor.getClosestPaletteEntry(x, y, pixel);
		
		int newPixel = mapColor.getPalettePixel(newC);
		
		img.setRGB(x, y, newPixel | 0xff000000);
		
		int r_error;
		int g_error;
		int b_error;
		
		r_error = ((pixel >> 16) & 0xff) - ((newPixel >> 16) & 0xff);
		g_error = ((pixel >> 8) & 0xff) - ((newPixel >> 8) & 0xff);
		b_error = ((pixel >> 0) & 0xff) - ((newPixel >> 0) & 0xff);

		if (useColorMappedGreyScale) {
			int lum = (299 * r_error + 587 * g_error + 114 * b_error) / 1000;
			r_error = g_error = b_error = lum;
		}
		
		if ((r_error | g_error | b_error) != 0) {
			if (x + 1 < img.getWidth()) {
				// x+1, y
				ditherFSApplyError(img, x + 1, y,  
						7, r_error, g_error, b_error);
			}
			if (y + 1 < img.getHeight()) {
				if (x > 0) {
					ditherFSApplyError(img, x - 1, y + 1, 
							3, r_error, g_error, b_error);
				}
				ditherFSApplyError(img, x, y + 1, 
						5, r_error, g_error, b_error);
				if (x + 1 < img.getWidth()) {
					ditherFSApplyError(img, x + 1, y + 1, 
							1, r_error, g_error, b_error);
				}
			}
		}
	}
	
	private void ditherFSApplyError(BufferedImage img, int x, int y, int sixteenths, int r_error, int g_error, int b_error) {
		int pixel = img.getRGB(x, y);
		int r = clamp(((pixel >> 16) & 0xff) + (sixteenths * r_error / 16));
		int g = clamp(((pixel >> 8) & 0xff) + (sixteenths * g_error / 16));
		int b = clamp(((pixel >> 0) & 0xff) + (sixteenths * b_error / 16));
		img.setRGB(x, y, (r << 16) | (g << 8) | b | 0xff000000);
	
	}

	private void ditherFS(BufferedImage img, IPaletteColorMapper mapColor, Histogram hist) {
		int h = img.getHeight();
		int w = img.getWidth();

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				ditherFSPixel(img, mapColor, hist, x, y);
			}
		}
		
	}

	// https://en.wikipedia.org/wiki/Floyd%E2%80%93Steinberg_dithering
	private void ditherFSPixelMono(BufferedImage img, IPaletteColorMapper mapColor,
			Histogram hist, int x, int y) {
		
		int pixel = img.getRGB(x, y);
		int lum = ColorMapUtils.getPixelLum(pixel);

//		int newC = lum < 128 ? 0 : 1; //mapColor.getClosestPaletteEntry(x, y, pixel);
		
		int newPixel = lum < 128 ? 0 : 0xffffff; //mapColor.getPalettePixel(newC);
		int newLum = ColorMapUtils.getPixelLum(newPixel);

		img.setRGB(x, y, newPixel | 0xff000000);
		
		int error = lum - newLum;
		
		if (error != 0) {
			if (x + 1 < img.getWidth()) {
				// x+1, y
				ditherFSMonoApplyError(img, x + 1, y, 7, error);
			}
			if (y + 1 < img.getHeight()) {
				if (x > 0) {
					ditherFSMonoApplyError(img, x - 1, y + 1, 3, error);
				}
				ditherFSMonoApplyError(img, x, y + 1, 5, error);
				if (x + 1 < img.getWidth()) {
					ditherFSMonoApplyError(img, x + 1, y + 1, 1, error); 
				}
			}
		}
	}

	private void ditherFSMonoApplyError(BufferedImage img, int x, int y, int sixteenths, int error) {
		int offs = sixteenths * error / 16;
		if (offs == 0)
			return;

		int pixel = img.getRGB(x, y);

		int r = clamp(((pixel >> 16) & 0xff) + offs);
		int g = clamp(((pixel >> 8) & 0xff) + offs);
		int b = clamp(((pixel >> 0) & 0xff) + offs);
		img.setRGB(x, y, (r << 16) | (g << 8) | b | 0xff000000);
	
	}

	private void ditherFSMono(BufferedImage img, IPaletteColorMapper mapColor, Histogram hist) {
		int h = img.getHeight();
		int w = img.getWidth();
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				ditherFSPixelMono(img, mapColor, hist, x, y);
			}
		}
		
	}
	
	// http://en.wikipedia.org/wiki/Ordered_dithering
	// http://upload.wikimedia.org/wikipedia/en/math/5/3/1/531fd7f88bac5f6482c465d1de15e16f.png
	
	final static byte[][] thresholdMap8x8 = {
		{  1, 49, 13, 61,  4, 52, 16, 61 },
		{ 33, 17, 45, 29, 36, 20, 48, 32 },
		{  9, 57,  5, 53, 12, 60,  8, 56 },
		{ 41, 25, 37, 21, 44, 28, 40, 24 },
		{  3, 51, 15, 63,  2, 50, 14, 62 },
		{ 35, 19, 47, 31, 34, 18, 46, 30 },
		{ 11, 59,  7, 55, 10, 58,  6, 54 },
		{ 43, 27, 39, 23, 42, 26, 38, 22 }
	};

	private void ditherOrderedPixel(BufferedImage img, IPaletteColorMapper mapColor,
			int x, int y, int[] prgb) {

		int pixel = img.getRGB(x, y);
		ColorMapUtils.pixelToRGB(pixel, prgb);

		if (true) {
			int threshold = ((byte) (thresholdMap8x8[x & 7][y & 7] << 2)) >> 2;
			prgb[0] = (prgb[0] + threshold);
			prgb[1] = (prgb[1] + threshold);
			prgb[2] = (prgb[2] + threshold);
		} else {
			int threshold = thresholdMap8x8[x & 7][y & 7];
			prgb[0] = (prgb[0] + threshold - 32);
			prgb[1] = (prgb[1] + threshold - 32);
			prgb[2] = (prgb[2] + threshold - 32);
		}
		
		int newC = mapColor.getClosestPaletteEntry(x, y, ColorMapUtils.rgb8ToPixel(prgb));
		
		int newPixel = mapColor.getPalettePixel(newC);
		
		if (pixel != newPixel)
			img.setRGB(x, y, newPixel | 0xff000000);
	}
	
	private void ditherOrdered(BufferedImage img, IPaletteColorMapper mapColor) {
		int h = img.getHeight();
		int w = img.getWidth();

		int[] prgb = { 0, 0, 0 };
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				ditherOrderedPixel(img, mapColor, x, y, prgb);
			}
		}
		
	}
	

//	private void ditherOrderedPixelBitmap(BufferedImage img, IPaletteColorMapper mapColor,
//			int x, int y, int[] prgb) {
//
//		
//		int pixel = img.getRGB(x, y);
//		ColorMapUtils.pixelToRGB(pixel, prgb);
//
//		int threshold = thresholdMap8x8[x & 7][y & 7];
//		int threshold2 = thresholdMap8x8[(x + 1) & 7][y & 7];
//		int threshold3 = thresholdMap8x8[x & 7][(y + 1) & 7];
//		prgb[0] = (prgb[0] + threshold - 32);
//		prgb[1] = (prgb[1] + threshold2 - 32);
//		prgb[2] = (prgb[2] + threshold3 - 32);
//		
//		int newC = mapColor.getClosestPaletteEntry(x, y, ColorMapUtils.rgb8ToPixel(prgb));
//		
//		int newPixel = mapColor.getPalettePixel(newC);
//		
//		img.setRGB(x, y, newPixel | 0xff000000);
//	}
//	
//	private void ditherOrderedBitmap(BufferedImage img, IPaletteColorMapper mapColor) {
//		int[] prgb = { 0, 0, 0 };
//		for (int y = 0; y < img.getHeight(); y++) {
//			for (int x = 0; x < img.getWidth(); x++) {
//				ditherOrderedPixelBitmap(img, mapColor, x, y, prgb);
//			}
//		}
//		
//	}
	
	private void ditherNone(BufferedImage img, IPaletteColorMapper mapColor) {
		int w = img.getWidth();
		int h = img.getHeight();
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int pixel = img.getRGB(x, y);
				int newC = mapColor.getClosestPaletteEntry(x, y, pixel);
				int newPixel;
				newPixel = mapColor.getPalettePixel(newC);
				img.setRGB(x, y, newPixel | 0xff000000);
			}
		}
		
	}

	
	/**
	 * @param format2
	 * @return
	 */
	public static boolean isModeSupported(VdpFormat format) {
		return format != null && format.getLayout() != VdpFormat.Layout.TEXT;
	}
	
	protected void reduceNoise(BufferedImage img) {
		int width = img.getWidth();
		int[] rgbs = new int[width];
		int[] prgb = { 0, 0, 0 };

		ColorOctree octree = new ColorOctree(3, false);

		int total = 0;
		for (int y = 0; y < img.getHeight(); y++) {
			img.getRGB(0, y, width, 1, rgbs, 0, width);
			for (int x = 0; x < width; x++) {
				ColorMapUtils.pixelToRGB(rgbs[x], prgb);
				octree.addColor(prgb);
				total++;
			}
		}

		List<LeafNode> leaves = octree.gatherLeaves();
		int numColors = 0;
		int numPixels = total / 2;
		for (LeafNode leaf : leaves) {
			numColors++;
			numPixels -= leaf.getPixelCount();
			if (numPixels <= 0)
				break;
		}
		if (DEBUG) System.out.println("*** finding " + numColors + " apparent colors");
		if (numColors > format.getNumColors()) //(format == VdpFormat.COLOR256_1x1 ? 256 : 16))
			return;
		
		for (int y = 0; y < img.getHeight(); y++) {
			img.getRGB(0, y, width, 1, rgbs, 0, width);
			for (int x = 0; x < width; x++) {
				ColorMapUtils.pixelToRGB(rgbs[x], prgb);
				V99ColorMapUtils.mapForRGB333(prgb);
				rgbs[x] = ColorMapUtils.rgb8ToPixel(prgb);
			}
			img.setRGB(0, y, width, 1, rgbs, 0, width);
		}
	}
	protected BufferedImage convertImageData(BufferedImage img, int targWidth, int targHeight) {
		flatten(img);
		
		if (!importDirectMappedImage(img)) {
			if (gamma != 1.0f)
				gammaCorrect(img);
			
//			reduceNoise(img);
			
			convertImageToColorMap(img);
		}
	
		if (false) {
			File tempfile = new File(System.getProperty("java.io.tmpdir"), "dragged.png");
			System.out.println("Temporary buffer to " + tempfile);
			try {
				ImageIO.write(img, "png", tempfile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return createConvertedImage(img, targWidth, targHeight);
	}

	/**
	 * Remove alpha
	 * @param img
	 */
	private void flatten(BufferedImage img) {
		int[] prgb = { 0, 0, 0 };
		int[] rgbs = new int[img.getWidth()];
		for (int y = 0; y < img.getHeight(); y++) {
			img.getRGB(0, y, rgbs.length, 1, rgbs, 0, rgbs.length);
			for (int x = 0; x < rgbs.length; x++) {
				int pixel = rgbs[x];
				int alpha = pixel >>> 24;
				if (alpha == 0) {
					pixel = 0xffffff;
				}
				else if (alpha == 0xff) {
					// itself
				}
				else {
					// blend with white
					ColorMapUtils.pixelToRGB(pixel, prgb);
					prgb[0] = (alpha * prgb[0] + (255 - alpha) * 255) / 256; 
					prgb[1] = (alpha * prgb[1] + (255 - alpha) * 255) / 256; 
					prgb[2] = (alpha * prgb[2] + (255 - alpha) * 255) / 256; 
					pixel = ColorMapUtils.rgb8ToPixel(prgb);
				}
				rgbs[x] = pixel | 0xff000000;
			}
			img.setRGB(0, y, rgbs.length, 1, rgbs, 0, rgbs.length);
		}
				
	}

	/**
	 * An image may be an exact match and not need dithering.
	 * This would happen either from a screenshot or from a
	 * picture with a very small number of colors.
	 * @param img
	 * @return
	 */
	private boolean importDirectMappedImage(BufferedImage img) {
		if (!tryDirectMapping || ditherMono || format.getLayout() == VdpFormat.Layout.PATTERN || format.getNumColors() > 16)
			return false;
		
		int[] rgbs = new int[img.getWidth()];

		int numColors = format.getNumColors();
		
		// effective minimum distance for any mode
		int maxDist = mapColor.getMinimalPaletteDistance();
		int numPixels = img.getWidth() * img.getHeight();
		
		boolean matched = false;
		Histogram hist = null;
		
		List<byte[][]> palettes = new ArrayList<byte[][]>();
		palettes.add(thePalette);
		palettes.addAll(Arrays.asList(VdpColorManager.palettes()));
		
		for (byte[][] palette : palettes) {
			FixedPaletteMapColor paletteMapper = new FixedPaletteMapColor(
					palette, firstColor, numColors);
			hist = new Histogram(paletteMapper, img.getWidth(), img.getHeight(), maxDist);
			int matchedC = hist.generate(img);
			if (mapColor instanceof MonoMapColor)
				((MonoMapColor) mapColor).setMidLum(hist.getAverageLuminance());

			if (matchedC == numPixels) {
				matched = true;
				break;
			}
		}
		
		if (matched) {
			for (int c = 0; c < numColors; c++) {
				replaceColor(img, rgbs, hist, c, ColorMapUtils.rgb8ToPixel(thePalette[c]), Integer.MAX_VALUE);
			}
			
			updatePaletteMapping();
			
			return true;
		}
		
		return false;
	}

	private void convertImageToColorMap(BufferedImage img) {
		
		Histogram hist = optimizeForNColors(img);
		
		updatePaletteMapping();

		if (ditherType == Dither.FS) {
			if (ditherMono)
				ditherFSMono(img, mapColor, hist);
			else
				ditherFS(img, mapColor, hist);
		} else if (ditherType == Dither.ORDERED) {
			ditherOrdered(img, mapColor);
		} else {
			ditherNone(img, mapColor);
		}
	}


	void createOptimalPaletteWithHSV(BufferedImage image, int colorCount) {
		int toAllocate = colorCount - firstColor;
			
		int[] prgb = { 0, 0, 0 };
		float[] hsv = { 0, 0, 0 };
		int[] rgbs = new int[image.getWidth()];
		for (int y = 0; y < image.getHeight(); y++) {
			image.getRGB(0, y, rgbs.length, 1, rgbs, 0, rgbs.length);
			for (int x = 0; x < rgbs.length; x++) {
				ColorMapUtils.pixelToRGB(rgbs[x], prgb);
				ColorMapUtils.rgbToHsv(prgb, hsv);
				prgb[0] = (int) (hsv[0] * 256 / 360);
				prgb[1] = (int) (hsv[1] * 255);
				prgb[2] = (int) hsv[2];
				octree.addColor(prgb);
			}
		}
		
		octree.reduceTree(colorCount);

		int index = firstColor;
		
		List<LeafNode> leaves = octree.gatherLeaves();
		if (leaves.size() > toAllocate)
			throw new IllegalStateException();
		
		for (ColorOctree.LeafNode node : leaves) {
			int[] repr = node.reprRGB();
			
			hsv[0] = repr[0] * 360 / 256.f;
			hsv[1] = repr[1] / 255.f;
			hsv[2] = repr[2];
			ColorMapUtils.hsvToRgb(hsv[0], hsv[1], hsv[2], repr);
			
			if (DEBUG) System.out.println("palette[" + index +"] = " 
					+ Integer.toHexString(repr[0]) + "/" 
					+ Integer.toHexString(repr[1]) + "/" 
					+ Integer.toHexString(repr[2]));
			
			thePalette[index][0] = (byte) repr[0];
			thePalette[index][1] = (byte) repr[1];
			thePalette[index][2] = (byte) repr[2];
			           
			index++;
			
		}
	}

	private void addToOctree(BufferedImage image) {
		//ColorOctree octree = new ColorOctree(4, toAllocate, true, false);
		int[] prgb = { 0, 0, 0 };
		int[] rgbs = new int[image.getWidth()];
		for (int y = 0; y < image.getHeight(); y++) {
			image.getRGB(0, y, rgbs.length, 1, rgbs, 0, rgbs.length);
			for (int x = 0; x < rgbs.length; x++) {
				ColorMapUtils.pixelToRGB(rgbs[x], prgb);
				octree.addColor(prgb);
			}
		}
	}
	
	private void createOptimalPalette(int colorCount) {
		int toAllocate = colorCount - firstColor;
		
		boolean perfect = octree.getLeafCount() <= toAllocate;
		
		if (!perfect)
			octree.reduceTree(toAllocate);

		int index = firstColor;
		
		List<LeafNode> leaves = octree.gatherLeaves();
		
		for (ColorOctree.LeafNode node : leaves) {
			int[] repr = node.reprRGB();
			
			if (useColorMappedGreyScale)
				V99ColorMapUtils.rgbToGreyForGreyscaleMode(
						mapColor.getGreyToRgbMap(),
						repr, repr);
			else if (convertGreyScale) 
				ColorMapUtils.rgbToGrey(repr, repr);
//			else
//				V99ColorMapUtils.mapForRGB333(repr);	// no, don't lose info until needed
			
			thePalette[index][0] = (byte) repr[0];
			thePalette[index][1] = (byte) repr[1];
			thePalette[index][2] = (byte) repr[2];

			if (DEBUG) System.out.println("palette[" + index +"] = " 
					+ Integer.toHexString(thePalette[index][0]&0xff) + "/" 
					+ Integer.toHexString(thePalette[index][1]&0xff) + "/" 
					+ Integer.toHexString(thePalette[index][2]&0xff));


			index++;
			
		}
	}

	
	private BufferedImage createConvertedImage(BufferedImage img, int targWidth, int targHeight) {
		int xoffs, yoffs;
		
		BufferedImage convertedImage = new BufferedImage(targWidth, targHeight, 
				BufferedImage.TYPE_3BYTE_BGR);

		int h = img.getHeight();
		int w = img.getWidth();
		
		xoffs = (targWidth - w) / 2;
		yoffs = (targHeight - h) / 2;
	
		if (format.getLayout() == VdpFormat.Layout.BITMAP_2_PER_8) {
			// be sure we select the 8 pixel groups sensibly
			if ((xoffs & 7) > 3)
				xoffs = (xoffs + 7) & ~7;
			else
				xoffs = xoffs & ~7;
		}
		
		if (format.getLayout() == VdpFormat.Layout.BITMAP_2_PER_8 && !ditherMono) {
			
			reduceBitmapMode(convertedImage, img, xoffs, yoffs);
	
		} else {
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					convertedImage.setRGB(x + xoffs, y + yoffs, img.getRGB(x, y));
				}
			}
		}
		
		return convertedImage;
	}

	/**
	 * Gamma correct an image 
	 * @return middle luminance
	 */
	private void gammaCorrect(BufferedImage img) {
		int[] prgb = { 0, 0, 0 };
		float[] hsv = { 0, 0, 0 };

		int h = img.getHeight();
		int w = img.getWidth();

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int pixel = img.getRGB(x, y);
				ColorMapUtils.pixelToRGB(pixel, prgb);
				ColorMapUtils.rgbToHsv(prgb, hsv);
				hsv[2] = (float) Math.pow(hsv[2], gamma);
				ColorMapUtils.hsvToRgb(hsv[0], hsv[1], hsv[2], prgb);
				
				img.setRGB(x, y, ColorMapUtils.rgb8ToPixel(prgb) | (pixel & 0xff000000));
			}
		}
	}


	/**
	 * Update the images for the optimal distribution of colors in a fixed
	 * color palette, presumably bitmap mode or multicolor mode,
	 * where dithering will not do a lot of good.
	 * 
	 * We cannot control the palette in this mode, but we can avoid unwanted
	 * dithering, e.g. in a cartoon or line art, by replacing the most important
	 * colors with their closest entries in the palette.
	 * 
	 * @param img
	 * @return minimum color distance
	 */
	private Histogram optimizeForNColors(BufferedImage img) {
		
		int numColors = mapColor.getNumColors();
		
		int ourDist = mapColor.getMinimalPaletteDistance();
		
		if (DEBUG) System.out.println("Minimum color palette distance: " + ourDist);

		Histogram hist = new Histogram(mapColor, img.getWidth(), img.getHeight(), ourDist);
		int mappedColors = 0;
		int interestingColors = 0;
		
		mappedColors = hist.generate(img);

		if (mapColor instanceof MonoMapColor)
			((MonoMapColor) mapColor).setMidLum(hist.getAverageLuminance());
		
		interestingColors = hist.size();
		if (DEBUG) System.out.println("# interesting = " + interestingColors
				+"; # mapped = " + mappedColors);
		
		if (format.canSetPalette()
				&& mappedColors == img.getWidth() * img.getHeight() 
				&& hist.pixelToColor().size() <= numColors - firstColor) {
			// perfect mapping!
			if (DEBUG) System.out.println("Perfect mapping!");
			int c = firstColor;
			byte[] rgb = { 0, 0, 0 };
			for (Map.Entry<Integer, Integer> ent : hist.pixelToColor().entrySet()) {
				ColorMapUtils.pixelToRGB(ent.getKey(), rgb);
				thePalette[c][0] = rgb[0];
				thePalette[c][1] = rgb[1];
				thePalette[c][2] = rgb[2];
				c++;
			}
			interestingColors = numColors - firstColor;
		}
		
		int usedColors = Math.min(numColors, interestingColors);
		
		if (DEBUG) System.out.println("\nN-color: interestingColors="+interestingColors
				+"; usedColors="+usedColors
				+"; mapped="+mappedColors
				);
		
		if (paletteOption == PaletteOption.OPTIMIZED) {
			for (int i = interestingColors + firstColor; i < thePalette.length; i++) {
				thePalette[i][0] = 0;
				thePalette[i][1] = 0;
				thePalette[i][2] = 0;
			}
		}
		return hist;
	}

	/**
	 * Replace a close match color (or often-appearing color)
	 * to ensure there will be an exact match so no dithering
	 * occurs on the primary occurrences of this color.
	 * @param img
	 * @param rgbs2 
	 * @param mappedColors
	 * @param maxDist 
	 * @param idx
	 */
	private int replaceColor(BufferedImage img, int[] rgbs, Histogram hist, int c, int newRGB, int maxDist) {
		int replaced = 0;
		int offs = 0;
		int[] mappedColors = hist.mappedColors();
		
		int h = img.getHeight();
		int w = img.getWidth();

		for (int y = 0; y < h; y++) {
			boolean changed = false;
			img.getRGB(0, y, w, 1, rgbs, 0, rgbs.length);
			for (int x = 0; x < w; x++) {
				if (mappedColors[offs] == c) {
					int dist = useColorMappedGreyScale ?  ColorMapUtils.getPixelLumDistance(rgbs[x], newRGB)
							: ColorMapUtils.getPixelDistance(rgbs[x], newRGB);
					if (dist < maxDist) {
						rgbs[x] = newRGB;
						replaced++;
						changed = true;
					} else {
						mappedColors[offs] = -1;
					}
				}
				offs++;
			}
			if (changed) {
				img.setRGB(0, y, w, 1, rgbs, 0, rgbs.length);
			}
		}
		
		if (replaced > 0) {
			if (DEBUG) System.out.println("Replaced color #" + c +" with " + Integer.toHexString(newRGB) 
					+ " (#" + hist.getColorCount(c) + " --> " + replaced +")");
		}
		return replaced;
	}

	interface IBitmapColorUser {
		/** Called once per each 8x1 block */
		void useColor(int x, int maxx, int y, List<Pair<Integer,Integer>> sorted);
	}

	/**
	 * Only two colors can exist per 8x1 pixels, so find those colors.
	 * If there's a tossup (lots of colors), use information from neighbors
	 * to enhance the odds.
	 * @param img
	 */
	protected void analyzeBitmap__(BufferedImage img, boolean includeSides, IBitmapColorUser colorUser) {
		@SuppressWarnings("unchecked")
		Map<Integer, Integer>[] histograms = new Map[(img.getWidth() + 7) / 8];
		@SuppressWarnings("unchecked")
		Map<Integer, Integer>[] histogramSides = new Map[(img.getWidth() + 7) / 8];
		
		int width = img.getWidth();
		for (int y = 0; y < img.getHeight(); y++) {
			// first scan: get histogram for each range
			
			for (int x = 0; x < width; x += 8) {
				Map<Integer, Integer> histogram = new HashMap<Integer, Integer>();
				Map<Integer, Integer> histogramSide = new HashMap<Integer, Integer>();
				
				histograms[x / 8] = histogram;
				histogramSides[x / 8] = histogramSide;
				
				int maxx = x + 8 < width ? x + 8 : width;
				
				int scmx;
				int scmn;
				
				// scan outside the 8 pixels to get a broader
				// idea of what colors are important
				if (includeSides) {
					scmn = Math.max(0, x - 2);
					scmx = Math.min(width, maxx + 2);
				} else {
					scmn = x;
					scmx = maxx;
				}
				
				int pixel = 0;
				for (int xd = scmn; xd < scmx; xd++) {
					if (xd < width) {
						pixel = img.getRGB(xd, y);
						if (useColorMappedGreyScale) {
							pixel = mapColor.getPixelForGreyscaleMode(pixel);
						}
					}
					
					Map<Integer, Integer> hist = (xd >= x && xd < maxx) ? histogram : histogramSide;
					
					Integer cnt = hist.get(pixel);
					if (cnt == null)
						cnt = 1;
					else
						cnt++;
					
					hist.put(pixel, cnt);
				}
			}
			
	
			for (int x = 0; x < width; x += 8) {
				Map<Integer, Integer> histogram = histograms[x / 8];
				Map<Integer, Integer> histogramSide = histogramSides[x / 8];
				
				int maxx = x + 8 < width ? x + 8 : width;
				
				// get prominent colors, weighing colors that also
				// appear in surrounding pixels higher  
				List<Pair<Integer, Integer>> sorted = new ArrayList<Pair<Integer,Integer>>();
				for (Map.Entry<Integer, Integer> entry : histogram.entrySet()) {
					Integer c = entry.getKey();
					int cnt = entry.getValue();
					if (includeSides) {
						cnt *= 2;
						Integer scnt = histogramSide.get(c);
						if (scnt != null)
							cnt += scnt;
					}
					sorted.add(new Pair<Integer, Integer>(c, cnt));
				}
				Collections.sort(sorted, new Comparator<Pair<Integer, Integer>>() {
	
					@Override
					public int compare(Pair<Integer, Integer> o1,
							Pair<Integer, Integer> o2) {
						return o2.second - o1.second;
					}
					
				});
	
				colorUser.useColor(x, maxx, y, sorted);
			}
		}
	}
	
	/**
	 * Only two colors can exist per 8x1 pixels, so find those colors.
	 * If there's a tossup (lots of colors), use information from neighbors
	 * to enhance the odds.
	 * @param img
	 */
	protected void analyzeBitmap(BufferedImage img, int includeSides, IBitmapColorUser colorUser) {
		@SuppressWarnings("unchecked")
		Map<Integer, Integer>[] histograms = new Map[(img.getWidth() + 7) / 8];
		@SuppressWarnings("unchecked")
		Map<Integer, Integer>[] histogramSides = new Map[(img.getWidth() + 7) / 8];
		Map<Integer, Integer>[] histogramsAbove = null;
		
		int width = img.getWidth();
		for (int y = 0; y < img.getHeight(); y++) {
			// first scan: get histogram for each range
			
			for (int x = 0; x < width; x += 8) {
				Map<Integer, Integer> histogram = new HashMap<Integer, Integer>();
				Map<Integer, Integer> histogramSide = new HashMap<Integer, Integer>();
				
				histograms[x / 8] = histogram;
				histogramSides[x / 8] = histogramSide;
				
				int maxx = x + 8 < width ? x + 8 : width;
				
				int scmx;
				int scmn;
				
				// scan outside the 8 pixels to get a broader
				// idea of what colors are important
				if (includeSides > 0) {
					scmn = Math.max(0, x - includeSides);
					scmx = Math.min(width, maxx + includeSides);
				} else {
					scmn = x;
					scmx = maxx;
				}
				
				int pixel = 0;
				for (int xd = scmn; xd < scmx; xd++) {
					if (xd < width) {
						pixel = img.getRGB(xd, y) & 0xffffff;
						if (useColorMappedGreyScale) {
							pixel = mapColor.getPixelForGreyscaleMode(pixel);
						}
					}
					
					Map<Integer, Integer> hist = (xd >= x && xd < maxx) ? histogram : histogramSide;
					
					Integer cnt = hist.get(pixel);
					if (cnt == null)
						cnt = 1;
					else
						cnt++;
					
					hist.put(pixel, cnt);
				}
			}
			
			
			for (int x = 0; x < width; x += 8) {
				Map<Integer, Integer> histogram = histograms[x / 8];
				Map<Integer, Integer> histogramSide = histogramSides[x / 8];
				Map<Integer, Integer> histogramAbove = histogramsAbove != null ? histogramsAbove[x / 8] : null;
				
				int maxx = x + 8 < width ? x + 8 : width;
				
				// get prominent colors, weighing colors that also
				// appear in surrounding pixels higher  
				List<Pair<Integer, Integer>> sorted = new ArrayList<Pair<Integer,Integer>>();
				for (Map.Entry<Integer, Integer> entry : histogram.entrySet()) {
					Integer c = entry.getKey();
					int cnt = entry.getValue();
					if (includeSides > 0) {
						cnt *= 4;
						
						int minDist = Integer.MAX_VALUE;
						int minCnt = 0;
						for(Map.Entry<Integer, Integer> sideEntry : histogramSide.entrySet()) {
							int dist = ColorMapUtils.getPixelDistance(c, sideEntry.getKey());
							if (dist < minDist && dist < 0x1f*0x1f*3 && sideEntry.getValue() > minCnt) {
								minDist = dist;
								minCnt = sideEntry.getValue();
							}
						}
						if (histogramAbove != null) {
							for(Map.Entry<Integer, Integer> aboveEntry : histogramAbove.entrySet()) {
								int dist = ColorMapUtils.getPixelDistance(c, aboveEntry.getKey());
								if (dist < minDist && dist < 0x1f*0x1f*3 && aboveEntry.getValue() > minCnt) {
									minDist = dist;
									minCnt = aboveEntry.getValue();
								}
							}
						}
						cnt += minCnt;
					}
					sorted.add(new Pair<Integer, Integer>(c, cnt));
				}
				Collections.sort(sorted, new Comparator<Pair<Integer, Integer>>() {
					
					@Override
					public int compare(Pair<Integer, Integer> o1,
							Pair<Integer, Integer> o2) {
						return o2.second - o1.second;
					}
					
				});
				
				colorUser.useColor(x, maxx, y, sorted);
			}
			histogramsAbove = histograms;
		}
	}
	

	class BitmapDitherColorMapper implements IPaletteColorMapper {
		
		private final IPaletteMapper paletteMapper;
		private final boolean isGreyScale = colorMgr.isGreyscale();
		
		public BitmapDitherColorMapper(IPaletteMapper paletteMapper) {
			this.paletteMapper = paletteMapper;
		}
		@Override
		public int getClosestPaletteEntry(int x, int y, int pixel) {
			if (bitmapColors != null && bitmapColors[y] != null) {
				Pair<Integer, Integer> fgbg = bitmapColors[y][x / 8];
				
				if (fgbg != null) {
					int fgdist = isGreyScale ? ColorMapUtils.getPixelLumDistance(fgbg.first, pixel) 
							: ColorMapUtils.getPixelDistance(fgbg.first, pixel);
					int bgdist = isGreyScale ? ColorMapUtils.getPixelLumDistance(fgbg.second, pixel) 
							: ColorMapUtils.getPixelDistance(fgbg.second, pixel);
					int cand = fgdist <= bgdist ? fgbg.first : fgbg.second;
					//if (DEBUG) System.out.println("y="+y+"; x="+x+" = " + Integer.toHexString(cand));
					return paletteMapper.getClosestPaletteEntry(x, y, cand);
				}
			}
			return paletteMapper.getClosestPaletteEntry(x, y, pixel);
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.image.IPaletteColorMapper#getClosestPalettePixel(int, int, int)
		 */
		@Override
		public int getClosestPalettePixel(int x, int y, int pixel) {
			return getPalettePixel(getClosestPaletteEntry(x, y, pixel));
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.image.IPaletteColorMapper#getPalettePixel(int)
		 */
		@Override
		public int getPalettePixel(int c) {
			return paletteMapper.getPalettePixel(c);
		}
	}


	/**
	 * @author ejs
	 *
	 */
	final class PrepareClosestLuminances implements IBitmapColorUser {
		/**
		 * 
		 */
		private final BufferedImage img;
		/**
		 * 
		 */
		private final IPaletteMapper mapColor;
	
		/**
		 * @param img
		 * @param mapColor
		 */
		private PrepareClosestLuminances(BufferedImage img, IPaletteMapper mapColor) {
			this.img = img;
			this.mapColor = mapColor;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public void useColor(int x, int maxx, int y, List<Pair<Integer,Integer>> sorted) {
	
			if (sorted.size() >= 2) {
				if (sorted.get(0).second + sorted.get(1).second < 4) {
					// eek, need to pick dominant colors
					int min = Integer.MAX_VALUE;
					int max = 0;
					int avg = 0;
					
					for (int i = 0; i < sorted.size(); i++) {
						Pair<Integer, Integer> ent = sorted.get(i);
						int lum = ColorMapUtils.getPixelLum(ent.first);
						avg += ent.second * lum;
						if (lum < min) min = lum;
						if (lum > max) max = lum;
					}
					
					avg /= 8;
					
					int lowa = (avg+min*3) / 4;
					int higha = (avg+max*3) / 4;
					
					int low = ColorMapUtils.rgb8ToPixel(new int[] { lowa, lowa, lowa });
					int high = ColorMapUtils.rgb8ToPixel(new int[] { higha, higha, higha });
					
					int fpixel = mapColor.getClosestPalettePixel(x, y, low);
					int bpixel = mapColor.getClosestPalettePixel(x, y, high);
					
					int dist = ColorMapUtils.getPixelLumDistance(fpixel, bpixel);
					if (dist > 0x5f*0x5f) {
	
						if (fpixel != bpixel) {
							if (bitmapColors[y] == null)
								bitmapColors[y] = new Pair[(img.getWidth() + 7) / 8];
							
							bitmapColors[y][x / 8] = new Pair<Integer, Integer>(fpixel, bpixel);
						}
					}
				}
			}
			
		}
	}

	/**
	 * @author ejs
	 *
	 */
	private final class PrepareClosestColors implements IBitmapColorUser {
		/**
		 * 
		 */
		private final BufferedImage img;
		/**
		 * 
		 */
		private final IPaletteMapper mapColor;
	
		/**
		 * @param img
		 * @param mapColor
		 */
		private PrepareClosestColors(BufferedImage img, IPaletteMapper mapColor) {
			this.img = img;
			this.mapColor = mapColor;
		}
	
		@SuppressWarnings("unchecked")
		@Override
		public void useColor(int x, int maxx, int y, List<Pair<Integer,Integer>> sorted) {
	
			if (sorted.size() > 2) {
//				int total = 0;
//				for (int i = 0; i < sorted.size(); i++) {
//					Pair<Integer, Integer> ent = sorted.get(i);
//					total += ent.second;
//				}
				
				/*
				if (sorted.get(0).second + sorted.get(1).second < total / 2) {
					// eek, need to pick dominant colors
					int[] min = { 255, 255, 255 };
					int[] max = { 0, 0, 0 };
					int[] avg = { 0, 0, 0 };
					
					int[] prgb = { 0, 0, 0 };
					for (int i = 0; i < sorted.size(); i++) {
						Pair<Integer, Integer> ent = sorted.get(i);
						ColorMapUtils.pixelToRGB(ent.first, prgb);
						avg[0] += ent.second * prgb[0];
						avg[1] += ent.second * prgb[1];
						avg[2] += ent.second * prgb[2];
						if (prgb[0] < min[0]) min[0] = prgb[0];
						if (prgb[1] < min[1]) min[1] = prgb[1];
						if (prgb[2] < min[2]) min[2] = prgb[2];
						if (prgb[0] > max[0]) max[0] = prgb[0];
						if (prgb[1] > max[1]) max[1] = prgb[1];
						if (prgb[2] > max[2]) max[2] = prgb[2];
					}
					
					avg[0] /= total;
					avg[1] /= total;
					avg[2] /= total;
					
					int[] lowa = { (avg[0]+min[0]*3) / 4, (avg[1]+min[1]*3)/4, (avg[2]+min[2]*3)/4 };
					int[] higha = { (avg[0]+max[0]*3) / 4, (avg[1]+max[1]*3)/4, (avg[2]+max[2]*3)/4 };
					
					int low = ColorMapUtils.rgb8ToPixel(lowa);
					int high = ColorMapUtils.rgb8ToPixel(higha);
					
					int fpixel = mapColor.getClosestPalettePixel(x, y, low);
					int bpixel = mapColor.getClosestPalettePixel(x, y, high);
					
					int dist = ColorMapUtils.getPixelDistance(fpixel, bpixel);
					if (dist > 0x1f*0x1f*3) {
	
						if (bitmapColors[y] == null)
							bitmapColors[y] = new Pair[(img.getWidth() + 7) / 8];
						
						System.out.println("at "+y+"/"+x+": " + Integer.toHexString(fpixel) + "/"+Integer.toHexString(bpixel));
						bitmapColors[y][x / 8] = new Pair<Integer, Integer>(fpixel, bpixel);
					}
				}*/
				
				int[] prgb = { 0, 0, 0 };
				int[] prgb0 = { 0, 0, 0 };
				int[] prgb1 = { 0, 0, 0 };
				Map<Integer, Integer> matches = new HashMap<Integer, Integer>();
				int loss;
				for (loss = 0; loss < 8; loss++) {
					matches.clear();
					for (int i = 0; i < sorted.size(); i++) {
						Pair<Integer, Integer> ent = sorted.get(i);
						ColorMapUtils.pixelToRGB(ent.first, prgb);
						prgb[0] &= 0xff << loss;
						prgb[1] &= 0xff << loss;
						prgb[2] &= 0xff << loss;
						
						int pixel = ColorMapUtils.rgb8ToPixel(prgb);
						int color = mapColor.getClosestPalettePixel(x, y, pixel);
						
						Integer cur = matches.get(color);
						if (cur == null)
							cur = ent.first;
						else {
							ColorMapUtils.pixelToRGB(ent.first, prgb0);
							ColorMapUtils.pixelToRGB(cur, prgb1);
							prgb0[0] = (prgb0[0] + prgb1[0]) / 2;
							prgb0[1] = (prgb0[1] + prgb1[1]) / 2;
							prgb0[2] = (prgb0[2] + prgb1[2]) / 2;
							cur = ColorMapUtils.rgb8ToPixel(prgb0);
						}
						matches.put(color, cur);
					}
					if (matches.size() <= 2)
						break;
				}
				
				if (matches.size() == 2) {
					if (bitmapColors[y] == null)
						bitmapColors[y] = new Pair[(img.getWidth() + 7) / 8];
					
					Iterator<Map.Entry<Integer, Integer>> iterator = matches.entrySet().iterator();
					int fpixel = iterator.next().getKey();
					int bpixel = iterator.hasNext() ? iterator.next().getKey() : fpixel;
					
					if (DEBUG)
						System.out.println("at "+y+"/"+x+": " + Integer.toHexString(fpixel) + "/"+Integer.toHexString(bpixel));
					bitmapColors[y][x / 8] = new Pair<Integer, Integer>(fpixel, bpixel);
				}
			}
			
		}
	}

	/**
	 * Find the two dominant colors per each 8x1 block, so dithering
	 * will only use those colors.
	 * Use information from neighbors
	 * to enhance the odds.
	 * 
	 * @param img
	 * @param xoffs
	 * @param yoffs
	 */
	@SuppressWarnings("unchecked")
	protected void prepareBitmapModeColors(final BufferedImage img) {
		bitmapColors = new Pair[img.getHeight()][];
		
		if (!colorMgr.isGreyscale()) {
			analyzeBitmap(img, 4, new PrepareClosestColors(img, mapColor));
		} else {
			// doesn't help any in greyscale since we pick totally
			// random colors
			//analyzeBitmap(img, false, new PrepareClosestLuminances(img, mapColor));
		}
	}


	/**
	 * Only two colors can exist per 8x1 pixels, so find those colors.
	 * If there's a tossup (lots of colors), use information from neighbors
	 * to enhance the odds.
	 * @param img
	 * @param xoffs
	 * @param yoffs
	 */
	protected void reduceBitmapMode(final BufferedImage convertedImage, final BufferedImage img, final int xoffs, final int yoffs) {
		analyzeBitmap(img, 4, new IBitmapColorUser() {
			
			@Override
			public void useColor(int x, int maxx, int y, 
					List<Pair<Integer,Integer>> sorted) {

				int fpixel, bpixel;
				if (sorted.size() <= 2) {
					// ok, only two different colors to care about -- fast path
					fpixel = sorted.get(0).first;
					bpixel = sorted.size() > 1 ? sorted.get(1).first : fpixel;
					
					int origPixel = 0;
					for (int xd = x; xd < maxx; xd++) {
						if (xd < img.getWidth()) {
							origPixel = img.getRGB(xd, y);
							if (useColorMappedGreyScale)
								origPixel = mapColor.getPixelForGreyscaleMode(origPixel);
						}
						
						int fdist = useColorMappedGreyScale ? ColorMapUtils.getPixelLumDistance(origPixel, fpixel) 
								: ColorMapUtils.getPixelDistance(origPixel, fpixel);
						int bdist = useColorMappedGreyScale ? ColorMapUtils.getPixelLumDistance(origPixel, bpixel) :
								ColorMapUtils.getPixelDistance(origPixel, bpixel);
						
						int newPixel;
						if (fdist < bdist) {
							newPixel = fpixel;
						} else {
							newPixel = bpixel;
						}
						
						convertedImage.setRGB(xd + xoffs, y + yoffs, newPixel);
					}
				} else {
					// there are more than 2 significant colors:  
					// since only two will appear in this 8 pixels,
					// find the most representative
					int total = 0;
					for (Pair<Integer, Integer> ent : sorted) {
						total += ent.second;
					}
					
					boolean dither = false;
					if (true) {
						fpixel = sorted.get(0).first;
						int fcnt = sorted.get(0).second;
						bpixel = sorted.get(1).first;
						int bcnt = sorted.get(1).second;
						
						int fbDist = useColorMappedGreyScale ? ColorMapUtils.getPixelLumDistance(fpixel, bpixel) 
								: ColorMapUtils.getPixelDistance(fpixel, bpixel);
						int wantDist = useColorMappedGreyScale ? 0x3f*0x3f : 0x3f*0x3f * 3;
						
						if (fcnt + bcnt < total * 8 / 10 && fbDist < wantDist) {
							for (int sidx = 2; sidx < sorted.size(); sidx++) { 
								Pair<Integer, Integer> ent = sorted.get(sidx);
								if (ent.second < 2)
									break;
								int dist = useColorMappedGreyScale ? ColorMapUtils.getPixelLumDistance(fpixel, ent.first) 
										: ColorMapUtils.getPixelDistance(fpixel, ent.first);
								if (dist > fbDist && dist > wantDist) {
									bpixel = ent.first;
									fbDist = dist;
								}
							}
						}
						if (ditherType != Dither.NONE && fbDist > wantDist && !useColorMappedGreyScale)
							dither = true; 
					} else {

						IPaletteColorMapper mapColor = new FixedPaletteMapColor(thePalette, firstColor, 16); 
						int[] prgb = { 0, 0, 0 };
						Map<Integer, Integer> matches = new LinkedHashMap<Integer, Integer>();
						int loss;
						for (loss = 0; loss < 8; loss++) {
							matches.clear();
							for (int i = 0; i < sorted.size(); i++) {
								Pair<Integer, Integer> ent = sorted.get(i);
								ColorMapUtils.pixelToRGB(ent.first, prgb);
								prgb[0] &= 0xff << loss;
								prgb[1] &= 0xff << loss;
								prgb[2] &= 0xff << loss;
								
								int pixel = ColorMapUtils.rgb8ToPixel(prgb);
								int color = mapColor.getClosestPalettePixel(x, y, pixel);
								
								Integer cur = matches.get(pixel);
								if (cur == null) {
									cur = ent.first;
									matches.put(color, cur);
								}
							}
							if (matches.size() <= 2)
								break;
						}
						
						Iterator<Map.Entry<Integer, Integer>> iterator = matches.entrySet().iterator();
						fpixel = iterator.next().getValue();
						bpixel = iterator.hasNext() ? iterator.next().getValue() : fpixel;
						
						dither = loss >= 4 && !useColorMappedGreyScale;
					}
					
					int[] prgb = { 0, 0, 0 };
					int origPixel = 0;
					for (int xd = x; xd < maxx; xd++) {
						if (xd < img.getWidth()) {
							origPixel = img.getRGB(xd, y);
							if (useColorMappedGreyScale)
								origPixel = mapColor.getPixelForGreyscaleMode(origPixel);
						}
						
						int newPixel = origPixel;
						
						if (dither) {
							ColorMapUtils.pixelToRGB(origPixel, prgb);
							
							int threshold = thresholdMap8x8[xd & 7][y & 7];
							prgb[0] = (prgb[0] + threshold - 32);
							prgb[1] = (prgb[1] + threshold - 32);
							prgb[2] = (prgb[2] + threshold - 32);
							
							newPixel = ColorMapUtils.rgb8ToPixel(prgb);
						}
							
						int fdist = useColorMappedGreyScale ? ColorMapUtils.getPixelLumDistance(newPixel, fpixel) 
								: ColorMapUtils.getPixelDistance(newPixel, fpixel);
						int bdist = useColorMappedGreyScale ? ColorMapUtils.getPixelLumDistance(newPixel, bpixel) :
								ColorMapUtils.getPixelDistance(newPixel, bpixel);
						
						if (fdist < bdist) {
							newPixel = fpixel;
						} else {
							newPixel = bpixel;
						}
						
						convertedImage.setRGB(xd + xoffs, y + yoffs, newPixel); 
					}
				}
				
			}
		});
	}

	protected void updatePaletteMapping() {
		paletteMappingDirty = false;
		
		int ncols = format.getNumColors();
		
		paletteToIndex = new TreeMap<Integer, Integer>();
		
		if (ditherMono) {
			if (format.isPaletted()) {
				paletteToIndex.put(0x0, colorMgr.getForeground());
				paletteToIndex.put(0xffffff, colorMgr.getBackground());
			} else {
				paletteToIndex.put(0x0, 0);
				paletteToIndex.put(0xffffff, ncols - 1);
			}
			return;
		}
		
		byte[] rgb = { 0, 0, 0};
		for (int c = 0; c < ncols; c++) {
			if (format.isPaletted()) {
				rgb = thePalette[c];
			} else if (mapColor instanceof IDirectColorMapper) {
				((IDirectColorMapper) mapColor).mapDirectColor(rgb, (byte) c);
			}
			int p = ColorMapUtils.rgb8ToPixel(rgb);
			paletteToIndex.put(p, c);
		}
	}

	public void setTryDirectMapping(boolean tryDirectMapping) {
		this.tryDirectMapping = tryDirectMapping;
	}
	public boolean isTryDirectMapping() {
		return tryDirectMapping;
	}
	/**
	 * @param options
	 * @param image
	 */
	public void prepareConversion(ImageImportOptions options) {
		format = options.getFormat();
		paletteOption = options.getPaletteUsage();
		ditherType = options.getDitherType();
		ditherMono = options.isDitherMono();

		byte[][] curPalette = colorMgr.getColorPalette();
		this.thePalette = new byte[curPalette.length][];
		for (int i = 0; i < thePalette.length; i++) {
			thePalette[i] = Arrays.copyOf(curPalette[i], curPalette[i].length);
		}
		
		this.useColorMappedGreyScale = colorMgr.isGreyscale();
		firstColor = (colorMgr.isClearFromPalette() ? 0 : 1);

		paletteMappingDirty = true;
		paletteToIndex = null;
		octree = new ColorOctree(3, true);

		convertGreyScale = options.isAsGreyScale();
		gamma = 1.0f + (options.getGamma() / 100f);
		
		if (ditherMono) {
			if (format.isPaletted()) {
				Pair<Integer, Integer> darkInfo = ColorMapUtils.getClosestColorByLumDistance(thePalette, firstColor, format.getNumColors(), 0);
				Pair<Integer, Integer> brightInfo = ColorMapUtils.getClosestColorByLumDistance(thePalette, firstColor, format.getNumColors(), 0xffffff);
				
				// don't pick the same color for both!
				if (darkInfo.first == brightInfo.first)
					darkInfo.first = brightInfo.first ^ 1;
				
				mapColor = new MonoMapColor(darkInfo.first, brightInfo.first);
			} else {
				mapColor = new MonoMapColor(0, 15);
			}
		
			firstColor = 0;
		}
		else if (!format.isPaletted() && paletteOption == PaletteOption.FIXED) {
			switch (format.getNumColors()) {
			case 8:
				// 3 bits, 1+1+1
				mapColor = new RGB111MapColor(useColorMappedGreyScale);
				break;
			case 16:
				// 4 bits, 1+2+1
				mapColor = new RGB121MapColor(useColorMappedGreyScale);
				break;
			case 32:
				// 5 bits, 2+2+1
				mapColor = new RGB221MapColor(useColorMappedGreyScale);
				break;
			case 64:
				// 6 bits, 2+2+2
				mapColor = new RGB222MapColor(useColorMappedGreyScale);
				break;
			case 128:
				// 7 bits, 2+3+2
				mapColor = new RGB232MapColor(useColorMappedGreyScale);
				break;
			case 256:
			default:
				// 8 bits, 3+3+2
				mapColor = new RGB332MapColor(useColorMappedGreyScale);
				break;
			}
		}
		else  {
			// real or invented mode with palette flexibility
			if (paletteOption == PaletteOption.OPTIMIZED) {
				mapColor = new UserPaletteMapColor(thePalette, firstColor, format.getNumColors(), useColorMappedGreyScale);
			} else if (paletteOption == PaletteOption.FIXED) {
				if (convertGreyScale)
					mapColor = new TI16MapColor(thePalette, false, true);
				else
					mapColor = new UserPaletteMapColor(thePalette, firstColor, format.getNumColors(), useColorMappedGreyScale);
			}
			else /* current */ {
				mapColor = new UserPaletteMapColor(thePalette, firstColor, format.getNumColors(), useColorMappedGreyScale);
			}
		}
		
		// get original mapping
		updatePaletteMapping();
	}

	public void addImage(ImageImportOptions options, BufferedImage image) {
		if (image == null)
			return;
		
		if (format == null)
			return;
		
		if (paletteOption == PaletteOption.OPTIMIZED) {
			addToOctree(image);
//			else if (format.getNumColors() > 4)
//				// note: in this mode, there is no point trying to use colors to
//				// map to greyscale (to find "more depth") -- any real colors
//				// discovered are essentially random, and when viewed in greyscale,
//				// may all have similar luminance.  Just make a grey palette to
//				// begin with.
//				if (useColorMappedGreyScale)
//					createOptimalGreyscalePalette(image, format.getNumColors());
//				else
//					createOptimalPalette(image, format.getNumColors());
//			}
		}
	}
	
	public void finishAddingImages() {
		createOptimalPalette(format.getNumColors());

	}
	public ImageImportData convertImage(ImageImportOptions options, BufferedImage image,
			int targWidth, int targHeight) {
		if (image == null)
			return null;

		if (convertGreyScale) {
			convertGreyscale(image);
		}

		BufferedImage converted = convertImageData(image, targWidth, targHeight);
		
		if (paletteMappingDirty) {
			// update mapping if palette was altered
			updatePaletteMapping();
		}

		if (flattenGreyScale && colorMgr.isGreyscale()) {
			int w = converted.getWidth();
			int h = converted.getHeight();
			byte[] rgb = { 0, 0, 0 };
			int[] rgbs = new int[w];
			for (int y = 0; y < h; y++) {
				converted.getRGB(0, y, w, 1, rgbs, 0, w);
				for (int x = 0; x < w; x++) {
					ColorMapUtils.pixelToRGB(rgbs[x], rgb);
					rgbs[x] = ColorMapUtils.rgb8ToPixel(ColorMapUtils.rgbToGrey(rgb));
				}
				converted.setRGB(0, y, w, 1, rgbs, 0, w);
			}
		}
		
		ImageImportData data = new ImageImportData(converted, thePalette, paletteToIndex);
		
		return data;
	}

	/**
	 * @param img
	 */
	private void convertGreyscale(BufferedImage img) {
		int[] rgb = new int[3];
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int pixel = img.getRGB(x, y);
				int lum = ColorMapUtils.getPixelLum(pixel);
				rgb[0] = rgb[1] = rgb[2] = lum;
				int newPixel = ColorMapUtils.rgb8ToPixel(rgb);
				img.setRGB(x, y, newPixel | (pixel & 0xff000000));
			}
		}
		
		
	}

	/**
	 * @param imageImportOptions
	 * @param targWidth
	 * @param targHeight
	 * @param format2
	 * @return
	 */
	public ImageImportData[] importImage(
			ImageImportDialogOptions imageImportOptions, int targWidth,
			int targHeight) {
		format = imageImportOptions.getFormat();
		int screenWidth = targWidth;
		int screenHeight = targHeight;
		
		int realWidth = imageImportOptions.getWidth();
		int realHeight = imageImportOptions.getHeight();
		float aspect = (float) targWidth / targHeight / imageImportOptions.getAspect();
		
		if (imageImportOptions.isKeepAspect()) {
			if (realWidth <= 0 || realHeight <= 0) {
				throw new IllegalArgumentException("image has zero or negative size");
			}
			if (realWidth != targWidth || realHeight != targHeight) {
				if (realWidth * targHeight * aspect > realHeight * targWidth) {
					targHeight = (int) Math.round(targWidth * realHeight / realWidth / aspect);
				} else {
					targWidth = (int) Math.round(targHeight * realWidth * aspect / realHeight);
					
					// make sure, for bitmap mode, that the size is a multiple of 8,
					// otherwise the import into video memory will destroy the picture
					if (format.getLayout() == Layout.BITMAP_2_PER_8) {
						targWidth &= ~7;
						targHeight = (int) Math.round(targWidth * realHeight / realWidth / aspect);
					}
				}
			}
		}
		
		if (format.getLayout() == VdpFormat.Layout.PATTERN) {
			// make a maximum of 256 blocks  (256*64 = 16384)
			// Reduces total screen real estate down by sqrt(3)
			//targWidth = (int) (targWidth / 1.732) & ~7;
			//targHeight = (int) (targHeight / 1.732) & ~7;
			int testWidth, testHeight;
			while (true) {
				testWidth = targWidth &~ 0x7;
				testHeight = ((int)(testWidth * realHeight / realWidth / aspect) + 7) & ~0x7;
				if (testWidth * testHeight <= 16384)
					break;
				
				targWidth *= 0.99;
				targHeight *= 0.99;
			}
			targWidth = testWidth;
			targHeight = testHeight;
			
			screenWidth = targWidth;
			screenHeight = targHeight;
			//if (DEBUG) System.out.println("Graphics mode: " + targWidth*((targHeight+7)&~0x7));
		}

		ImageFrame[] frames = imageImportOptions.getImages();
		if (frames == null)
			return null;
		
		if (frames.length > 1)
			setTryDirectMapping(false);
		
		prepareConversion(imageImportOptions);
		
		for (ImageFrame frame : frames) {
			addImage(imageImportOptions, frame.image);
		}

		finishAddingImages();

		Object hint = imageImportOptions.isScaleSmooth() ? RenderingHints.VALUE_INTERPOLATION_BILINEAR
				:  RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;

		ImageImportData[] datas = new ImageImportData[frames.length];
		for (int i = 0; i < datas.length; i++) {
			// always scale even if same size since the option destroys the image
			BufferedImage scaled = AwtImageUtils.getScaledInstance(
					frames[i].image, targWidth, targHeight, 
					hint,
					false);
			
			ImageImportData data = convertImage(imageImportOptions, scaled,
					screenWidth, screenHeight);
			data.delayMs = frames[i].delayMs;
			datas[i] = data;
		}

		return datas;		
	}
	
	public void setFlattenGreyscale(boolean b) {
		this.flattenGreyScale = b;
	}
	
}
