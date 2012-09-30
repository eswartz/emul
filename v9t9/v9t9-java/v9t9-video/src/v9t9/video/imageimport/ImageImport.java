/**
 * 
 */
package v9t9.video.imageimport;

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

import v9t9.common.video.ColorMapUtils;
import v9t9.common.video.VdpColorManager;
import v9t9.common.video.VdpFormat;
import v9t9.video.ImageDataCanvas;
import v9t9.video.imageimport.ColorOctree.LeafNode;
import v9t9.video.imageimport.ImageImportOptions.Dither;
import v9t9.video.imageimport.ImageImportOptions.Palette;
import ejs.base.utils.Pair;

/**
 * This class handles converting arbitrary external images and 
 * converting them to fit in the current TMS9918A or V9938 mode.
 * @author ejs
 *
 */
public class ImageImport {
	private boolean DEBUG = true;

	//private BufferedImage convertedImage;
	private VdpFormat format;
	private byte[][] thePalette;

	private boolean useColorMappedGreyScale;
	private Dither ditherType;
	private boolean ditherMono;
	private Palette paletteOption;

//	private final ImageDataCanvas canvas;
	private final VdpColorManager colorMgr;
	private boolean paletteMappingDirty;
	//private final IVdpChip vdp;
	private int firstColor;
	private int[] rgbs;


	/** mapping from RGB-32 pixel to each palette index */
	protected TreeMap<Integer, Integer> paletteToIndex;
	private Pair<Integer,Integer>[][] bitmapColors;

	private ColorOctree octree;
	

	/* 
	 * This palette adheres to the strict naming of the given colors
	 * and is quite tacky visually, but helps in color selection, since
	 * the colors cover a wider gamut.
	 *  
	 * 0 clear
	 * 
	 * 1 black
	 * 
	 * 2 medium green
	 * 
	 * 3 light green
	 * 
	 * 4 dark blue
	 * 
	 * 5 light blue
	 * 
	 * 6 dark red
	 * 
	 * 7 cyan
	 * 
	 * 8 medium red
	 * 
	 * 9 orange/skin
	 * 
	 * 10 yellow
	 * 
	 * 11 light yellow
	 * 
	 * 12 dark green
	 * 
	 * 13 purple
	 * 
	 * 14 grey
	 * 
	 * 15 white
	 */
	protected static final byte[][] stock16ColorPalette = {
		/* 0 */ { 0x00, 0x00, 0x00 }, 
		/* 1 */ { 0x00, 0x00, 0x00 },
		/* 2 */ { 0x00, (byte) 0xc0, 0x00 }, 
		/* 3 */ { 0x00, (byte) 0xff, 0x00 },
		/* 4 */ { 0x00, 0x00, (byte) 0x80 }, 
		/* 5 */ { 0x00, 0x00, (byte) 0xff },
		/* 6 */ { (byte) 0xc0, 0x00, 0x00 }, 
		/* 7 */ { 0x00, (byte) 0xff, (byte) 0xff },
		/* 8 */ { (byte) 0xff, 0x00, 0x00 }, 
		/* 9 */ { (byte) 0xff, (byte) 0xc0, 0x40 },
		/* 10 */ { (byte) 0xc0, (byte) 0xc0, 0 },
		/* 11 */ { (byte) 0xff, (byte) 0xff, 0x00 }, 
		/* 12 */ { 0x00, (byte) 0x80, 0x00 },
		/* 13 */ { (byte) 0xc0, 0x00, (byte) 0xc0 },
		/* 14 */ { (byte) 0xc0, (byte) 0xc0, (byte) 0xc0 },
		/* 15 */ { (byte) 0xff, (byte) 0xff, (byte) 0xff }, 
	};
	protected static final byte[][] niceColorPalette = {
		/* 0 */ { 0x00, 0x00, 0x00 }, 
		/* 1 */ { 0x00, 0x00, 0x00 },
		/* 2 */ { 0x40, (byte) 0xb0, 0x40 }, 
		/* 3 */ { 0x60, (byte) 0xc0, 0x60 },
		/* 4 */ { 0x40, 0x40, (byte) 0xc0 }, 
		/* 5 */ { 0x60, 0x60, (byte) 0xf0 },
		/* 6 */ { (byte) 0xc0, 0x40, 0x40 }, 
		/* 7 */ { 0x40, (byte) 0xf0, (byte) 0xf0 },
		/* 8 */ { (byte) 0xf0, 0x40, 0x40 }, 
		/* 9 */ { (byte) 0xff, (byte) 0x80, 0x60 },
		/* 10 */ { (byte) 0xf0, (byte) 0xc0, 0x40 },
		/* 11 */ { (byte) 0xff, (byte) 0xe0, 0x60 }, 
		/* 12 */ { 0x40, (byte) 0x80, 0x40 },
		/* 13 */ { (byte) 0xc0, 0x40, (byte) 0xc0 },
		/* 14 */ { (byte) 0xd0, (byte) 0xd0, (byte) 0xd0 },
		/* 15 */ { (byte) 0xff, (byte) 0xff, (byte) 0xff }, 
	};

//	private boolean supportsSetPalette;

	private boolean convertGreyScale;

private boolean isBitmap;

private IPaletteMapper mapColor;

	public ImageImport(ImageDataCanvas canvas, boolean supportsSetPalette) {
//		this.canvas = canvas;
//		this.supportsSetPalette = supportsSetPalette;
		synchronized (canvas) {
			this.colorMgr = canvas.getColorMgr();
			this.format = canvas.getFormat();
	
			isBitmap = format == VdpFormat.COLOR16_8x1 || format == VdpFormat.COLOR16_8x1_9938; 
			byte[][] curPalette = colorMgr.getColorPalette();
			this.thePalette = new byte[curPalette.length][];
			for (int i = 0; i < thePalette.length; i++) {
				thePalette[i] = Arrays.copyOf(curPalette[i], curPalette[i].length);
			}
			
			this.useColorMappedGreyScale = colorMgr.isGreyscale();
		}
		
		this.rgbs = new int[canvas.getImageData().width];
		
	}
	
	private final int clamp(int i) {
		return i < 0 ? 0 : i > 255 ? 255 : i;
	}

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

		if ((r_error | g_error | b_error) != 0) {
			int tot_err = Math.abs(r_error) | Math.abs(g_error) | Math.abs(b_error); 
			if (x + 1 < img.getWidth()) {
				// x+1, y
				ditherFSApplyError(img, x + 1, y,  
						7, r_error, g_error, b_error, tot_err);
			}
			if (y + 1 < img.getHeight()) {
				if (x > 0) {
					ditherFSApplyError(img, x - 1, y + 1, 
							3, r_error, g_error, b_error, tot_err);
				}
				ditherFSApplyError(img, x, y + 1, 
						5, r_error, g_error, b_error, tot_err);
				if (x + 1 < img.getWidth()) {
					ditherFSApplyError(img, x + 1, y + 1, 
							1, r_error, g_error, b_error, tot_err);
				}
			}
		}
	}
	
	private void ditherFSApplyError(BufferedImage img, int x, int y, int sixteenths, int r_error, int g_error, int b_error, int tot_err) {
		if (sixteenths * tot_err / 16 == 0)
			return;
		int pixel = img.getRGB(x, y);
		int r = clamp(((pixel >> 16) & 0xff) + (sixteenths * r_error / 16));
		int g = clamp(((pixel >> 8) & 0xff) + (sixteenths * g_error / 16));
		int b = clamp(((pixel >> 0) & 0xff) + (sixteenths * b_error / 16));
		img.setRGB(x, y, (r << 16) | (g << 8) | b | 0xff000000);
	
	}

	private void ditherFloydSteinberg(BufferedImage img, IPaletteColorMapper mapColor, Histogram hist) {
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				ditherFSPixel(img, mapColor, hist, x, y);
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

		int threshold = thresholdMap8x8[x & 7][y & 7];
		prgb[0] = (prgb[0] + threshold - 32);
		prgb[1] = (prgb[1] + threshold - 32);
		prgb[2] = (prgb[2] + threshold - 32);
		
		int newC = mapColor.getClosestPaletteEntry(x, y, ColorMapUtils.rgb8ToPixel(prgb));
		
		int newPixel = mapColor.getPalettePixel(newC);
		
		if (pixel != newPixel)
			img.setRGB(x, y, newPixel | 0xff000000);
	}
	
	private void ditherOrdered(BufferedImage img, IPaletteColorMapper mapColor) {
		int[] prgb = { 0, 0, 0 };
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
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
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
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
		if (format == null || format == VdpFormat.TEXT) 
			return false;
			
		return true;
	}
	
	protected void reduceNoise(BufferedImage img) {
		int width = img.getWidth();
		int[] rgbs = new int[width];
		int[] prgb = { 0, 0, 0 };

		ColorOctree octree = new ColorOctree(3, false, false);

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
				ColorMapUtils.mapForRGB333(prgb);
				rgbs[x] = ColorMapUtils.rgb8ToPixel(prgb);
			}
			img.setRGB(0, y, width, 1, rgbs, 0, width);
		}
	}
	protected BufferedImage convertImageData(BufferedImage img, int targWidth, int targHeight) {
		flatten(img);
		
		if (!importDirectMappedImage(img)) {
			 equalize(img);
			
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
		if (format == VdpFormat.COLOR256_1x1 || ditherMono || format == VdpFormat.COLOR16_8x8)
			return false;
		
		int numColors = format.getNumColors();
		
		// effective minimum distance for any mode
		int maxDist = 0x8*0x8 * 3;
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
			if (matchedC == numPixels) {
				matched = true;
				break;
			}
		}
		
		if (matched) {
			for (int c = 0; c < numColors; c++) {
				replaceColor(img, hist, c, ColorMapUtils.rgb8ToPixel(thePalette[c]), Integer.MAX_VALUE);
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
			ditherFloydSteinberg(img, mapColor, hist);
		} else if (ditherType == Dither.ORDERED) {
			ditherOrdered(img, mapColor);
//		} else if (ditherType == Dither.ORDERED2) {
//			ditherOrderedBitmap(img, colorMapper);
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
//					if (useColorMappedGreyScale)
//						ColorMapUtils.rgbToGreyForGreyscaleMode(prgb, prgb);
//					else if (convertGreyScale) 
//						ColorMapUtils.rgbToGrey(prgb, prgb);
				
				octree.addColor(prgb);
			}
		}

	}
	private void createOptimalPalette(int colorCount) {
		int toAllocate = colorCount - firstColor;
		
		octree.reduceTree(toAllocate);

		int index = firstColor;
		
		List<LeafNode> leaves = octree.gatherLeaves();
		
		for (ColorOctree.LeafNode node :  leaves) {
			int[] repr = node.reprRGB();
			
			if (useColorMappedGreyScale)
				ColorMapUtils.rgbToGreyForGreyscaleMode(repr, repr);
			else if (convertGreyScale) 
				ColorMapUtils.rgbToGrey(repr, repr);
			else
				ColorMapUtils.mapForRGB333(repr);
			
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

//	/**
//	 * Pick areas where histogram finds the most luminance.
//	 * @param image
//	 * @param colorCount
//	 */
//	private void addToOctreeInGreyscale(BufferedImage image) {
//		Histogram prevHist = null;
//		
//		for (int mask = 0; mask < 3; mask++) {
//			final int maskVal = (~0) << mask;
//			final int max = 8 - (1 << mask);
//			IColorMapper greyMapper = new IColorMapper() {
//				
//				@Override
//				public int mapColor(int pixel, int[] dist) {
//					int lum = ColorMapUtils.getPixelLum(pixel);
//					return ((lum >> 5) & maskVal) * 7 / max;
//				}
//			};
//			
//			Histogram hist = new Histogram(greyMapper, image.getWidth(), image.getHeight(), Integer.MAX_VALUE);
//			hist.generate(image);
//	
//			Map<Integer, Integer> colorToCountMap = hist.colorToCountMap();
//			if (colorToCountMap.size() > toAlloc && mask + 1 < 3) {
//				prevHist = hist;
//				continue;
//			}
//		
//			if (colorToCountMap.size() < colorCount && prevHist != null)
//				hist = prevHist;
//			
//			for (int cidx = firstColor; cidx < colorCount; cidx++) {
//				int c = hist.getColorIndex(cidx);
//				//colorMgr.setGRB333(cidx, c, c, c);
//				thePalette[cidx][0] = ColorMapUtils.rgb3to8[c];
//				thePalette[cidx][1] = ColorMapUtils.rgb3to8[c];
//				thePalette[cidx][2] = ColorMapUtils.rgb3to8[c];
//			}
//			
//			break;
//		}
//		
//	}
//
//	/**
//	 * Pick areas where histogram finds the most luminance.
//	 * @param image
//	 * @param colorCount
//	 */
//	private void createOptimalGreyscalePalette(int colorCount) {
//		int toAlloc = colorCount - firstColor;
//		
//		Histogram prevHist = null;
//		
//		for (int mask = 0; mask < 3; mask++) {
//			final int maskVal = (~0) << mask;
//			final int max = 8 - (1 << mask);
//			IColorMapper greyMapper = new IColorMapper() {
//				
//				@Override
//				public int mapColor(int pixel, int[] dist) {
//					int lum = ColorMapUtils.getPixelLum(pixel);
//					return ((lum >> 5) & maskVal) * 7 / max;
//				}
//			};
//			
//			Histogram hist = new Histogram(greyMapper, image.getWidth(), image.getHeight(), Integer.MAX_VALUE);
//			hist.generate(image);
//			
//			// take darkest and brighest first
//			Map<Integer, Integer> colorToCountMap = hist.colorToCountMap();
//			if (colorToCountMap.size() > toAlloc && mask + 1 < 3) {
//				prevHist = hist;
//				continue;
//			}
//			
//			if (colorToCountMap.size() < colorCount && prevHist != null)
//				hist = prevHist;
//			
//			for (int cidx = firstColor; cidx < colorCount; cidx++) {
//				int c = hist.getColorIndex(cidx);
//				//colorMgr.setGRB333(cidx, c, c, c);
//				thePalette[cidx][0] = ColorMapUtils.rgb3to8[c];
//				thePalette[cidx][1] = ColorMapUtils.rgb3to8[c];
//				thePalette[cidx][2] = ColorMapUtils.rgb3to8[c];
//			}
//			
//			break;
//		}
//		
//	}
	
	private BufferedImage createConvertedImage(BufferedImage img, int targWidth, int targHeight) {
		int xoffs, yoffs;
		
		//Arrays.fill(canvasImageData.data, (byte) 0);
		BufferedImage convertedImage = new BufferedImage(targWidth, targHeight, 
				BufferedImage.TYPE_3BYTE_BGR);
	
//		if (format == VdpFormat.COLOR16_4x4) {
//			xoffs = (64 - img.getWidth()) / 2;
//			yoffs = (48 - img.getHeight()) / 2;
//		} else {
//			xoffs = (canvas.getVisibleWidth() - img.getWidth() + canvas.getXOffset()) / 2;
//			yoffs = (canvas.getVisibleHeight() - img.getHeight() + canvas.getYOffset()) / 2;
//		}
		xoffs = (targWidth - img.getWidth()) / 2;
		yoffs = (targHeight - img.getHeight()) / 2;
	
		if (isBitmap) {
			// be sure we select the 8 pixel groups sensibly
			if ((xoffs & 7) > 3)
				xoffs = (xoffs + 7) & ~7;
			else
				xoffs = xoffs & ~7;
		}
		
		if (isBitmap && !ditherMono) {
			
			reduceBitmapMode(convertedImage, img, xoffs, yoffs);
	
		} else {
	
			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					convertedImage.setRGB(x + xoffs, y + yoffs, img.getRGB(x, y));
				}
			}
		}
		
		return convertedImage;
	}

	/**
	 * Equalize an image so it has a full range of 
	 * saturation and value
	 * @return middle luminance
	 */
	private int equalize(BufferedImage img) {
		float minSat = Float.MAX_VALUE, maxSat = Float.MIN_VALUE;
		float minVal = Float.MAX_VALUE, maxVal = Float.MIN_VALUE;
		
		int[] prgb = { 0, 0, 0 };
		float[] hsv = { 0, 0, 0 };
		
		int[] valCount = new int[16];
		
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int pixel = img.getRGB(x, y);
				ColorMapUtils.pixelToRGB(pixel, prgb);
				ColorMapUtils.rgbToHsv(prgb, hsv);
				minSat = Math.min(hsv[1], minSat);
				maxSat = Math.max(hsv[1], maxSat);
				minVal = Math.min(hsv[2], minVal);
				maxVal = Math.max(hsv[2], maxVal);
				valCount[(int) (hsv[2] / 16)]++;
			}
		}
		
		int maxPt = -1;
		for (int i = 1; i < 15; i++) {
			if (maxPt == -1 || valCount[i] >= valCount[maxPt]) {
				maxPt = i;
			}
		}
		
		int valueMidpoint = maxPt * 16;
		
		if (DEBUG) System.out.println("Equalize: sat = "+minSat+" to " +maxSat+"; val = " + minVal + " to " + maxVal+"; value midpoint = " + valueMidpoint);
		float satScale = 1.0f - minSat;
		float valScale = 255 - minVal;
		float satDiff = maxSat - minSat;
		float valDiff = maxVal - minVal;
		
		if ((satDiff > 0.1 && satDiff < 0.5) && (valDiff > 64 && valDiff < 128)) {
			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					int pixel = img.getRGB(x, y);
					ColorMapUtils.pixelToRGB(pixel, prgb);
					ColorMapUtils.rgbToHsv(prgb, hsv);
					
					if (satDiff > 0.1 && satDiff < 0.5)
						hsv[1] = ((hsv[1] - minSat) / satDiff) * satScale + minSat;
					if (valDiff < 128)
						hsv[2] = ((hsv[2] - minVal) / valDiff) * valScale + minVal;
	
					ColorMapUtils.hsvToRgb(hsv[0], hsv[1], hsv[2], prgb);
					
					img.setRGB(x, y, ColorMapUtils.rgb8ToPixel(prgb) | (pixel & 0xff000000));
				}
			}
		}
		
		return (int) ((minVal + maxVal) / 2);
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
		interestingColors = hist.size();
		if (DEBUG) System.out.println("# interesting = " + interestingColors
				+"; # mapped = " + mappedColors);
		
		int usedColors = Math.min(numColors, interestingColors);
		
		if (DEBUG) System.out.println("\nN-color: interestingColors="+interestingColors
				+"; usedColors="+usedColors
				+"; mapped="+mappedColors
				);
		
		return hist;
	}

	/**
	 * Replace a close match color (or often-appearing color)
	 * to ensure there will be an exact match so no dithering
	 * occurs on the primary occurrences of this color.
	 * @param img
	 * @param mappedColors
	 * @param maxDist 
	 * @param idx
	 */
	private int replaceColor(BufferedImage img, Histogram hist, int c, int newRGB, int maxDist) {
		int replaced = 0;
		int offs = 0;
		int[] mappedColors = hist.mappedColors();
		for (int y = 0; y < img.getHeight(); y++) {
			boolean changed = false;
			img.getRGB(0, y, img.getWidth(), 1, rgbs, 0, rgbs.length);
			for (int x = 0; x < img.getWidth(); x++) {
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
				img.setRGB(0, y, img.getWidth(), 1, rgbs, 0, rgbs.length);
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
							pixel = ColorMapUtils.getPixelForGreyscaleMode(pixel);
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
							pixel = ColorMapUtils.getPixelForGreyscaleMode(pixel);
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
								origPixel = ColorMapUtils.getPixelForGreyscaleMode(origPixel);
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
								origPixel = ColorMapUtils.getPixelForGreyscaleMode(origPixel);
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
		int ncols = format.getNumColors();
	
		paletteToIndex = new TreeMap<Integer, Integer>();
		
		if (ncols < 256) {
			if (ditherMono) {
				paletteToIndex.put(0x0, colorMgr.getForeground());
				paletteToIndex.put(0xffffff, colorMgr.getBackground());
			} else {
				for (int c = 0; c < ncols; c++) {
					byte[] nrgb = thePalette[c];
					if (useColorMappedGreyScale) 
						nrgb = ColorMapUtils.getRgbToGreyForGreyscaleMode(nrgb);
					int p = ColorMapUtils.rgb8ToPixel(nrgb);
					paletteToIndex.put(p, c);
				}
			}
		} else {
			byte[] rgb = { 0, 0, 0};
			for (int c = 0; c < ncols; c++) {
				ColorMapUtils.getGRB332(rgb, (byte) c, false);
				int p;
				if (useColorMappedGreyScale) {
					byte[] nrgb = ColorMapUtils.getRgbToGreyForGreyscaleMode(rgb);
					p = ColorMapUtils.rgb8ToPixel(nrgb);
				} else {
					p = ColorMapUtils.rgb8ToPixel(rgb);
				}
				paletteToIndex.put(p, c);
			}
		}
		
		paletteMappingDirty = false;
	}

	/**
	 * @param options
	 * @param image
	 */
	public void prepareConversion(ImageImportOptions options) {
		paletteOption = options.getPalette();
		ditherType = options.getDitherType();
		ditherMono = options.isDitherMono();

		firstColor = (colorMgr.isClearFromPalette() ? 0 : 1);


		if (format == VdpFormat.COLOR16_8x8) {
			paletteOption = Palette.STANDARD;
			ditherMono = true;
		}
		
		//new ColorOctree(3, toAllocate, true, false);
		//octree = options.getOctree();
		
		paletteMappingDirty = true;
		paletteToIndex = null;
		octree = new ColorOctree(3, true, false);
		
		if (paletteOption == Palette.STANDARD || options.isDitherMono()) {
			byte[][] orig;
			if (format.isMsx2()) {
				orig = VdpColorManager.stockPaletteV9938;
			} else {
				orig = VdpColorManager.stockPalette;
			}
			for (int i = 0; i < thePalette.length; i++) {
				System.arraycopy(orig[i], 0, thePalette[i], 0, 3);
			}
		}

		convertGreyScale = options.isAsGreyScale();

		// get original mapping
		updatePaletteMapping();
		
		if (ditherMono) {
			paletteOption = Palette.CURRENT;
			Pair<Integer, Integer> darkInfo = ColorMapUtils.getClosestColorByLumDistance(thePalette, firstColor, format.getNumColors(), 0);
			Pair<Integer, Integer> brightInfo = ColorMapUtils.getClosestColorByLumDistance(thePalette, firstColor, format.getNumColors(), 0xffffff);
			mapColor = new MonoMapColor(darkInfo.first, brightInfo.first);
			firstColor = 0;
		} else if (isBitmap || format == VdpFormat.COLOR16_4x4) {
			if (paletteOption == Palette.OPTIMIZED) {
				mapColor = new UserPaletteMapColor(thePalette, firstColor, 16, useColorMappedGreyScale);
			} else if (paletteOption == Palette.STANDARD) {
				if (convertGreyScale)
					mapColor = new TI16MapColor(thePalette, false, true);
				else
					mapColor = new UserPaletteMapColor(thePalette, firstColor, 16, useColorMappedGreyScale);
			}
			else /* current */ {
				mapColor = new UserPaletteMapColor(thePalette, firstColor, 16, useColorMappedGreyScale);
			}
		}
		else if (format == VdpFormat.COLOR16_1x1) {
			mapColor = new UserPaletteMapColor(thePalette, firstColor, 16, useColorMappedGreyScale);
		}
		else if (format == VdpFormat.COLOR4_1x1) {
			mapColor = new UserPaletteMapColor(thePalette, firstColor, 4, useColorMappedGreyScale);
		}
		else if (format == VdpFormat.COLOR256_1x1) {
			paletteOption = Palette.STANDARD;
			mapColor = new RGB332MapColor(useColorMappedGreyScale);
		}
		else if (format == VdpFormat.COLOR16_8x8) {
			paletteOption = Palette.STANDARD;
			mapColor = new TI16MapColor(thePalette, useColorMappedGreyScale, convertGreyScale);
		}
		else {
			throw new UnsupportedOperationException(format.toString());
		}
	}

	public void addImage(ImageImportOptions options, BufferedImage image) {
		if (image == null)
			return;
		
		if (format == null)
			return;
		
		if (paletteOption == Palette.OPTIMIZED) {
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
				ColorMapUtils.pixelToRGB(pixel, rgb);
				int lum = (299 * ((pixel >> 16) & 0xff) + 587 * ((pixel >> 8) & 0xff) + 114 * (pixel & 0xff)) / 1000;
				rgb[0] = rgb[1] = rgb[2] = lum;
				int newPixel = ColorMapUtils.rgb8ToPixel(rgb);
				img.setRGB(x, y, newPixel | (pixel & 0xff000000));
			}
		}
		
		
	}

//	public byte getPixel(int x, int y) {
//			
//			int p;
//			if (x < convertedImage.getWidth() && y < convertedImage.getHeight())
//				p = convertedImage.getRGB(x, y) & 0xffffff;
//			else
//				p = 0;
//			
//			Integer c = paletteToIndex.get(p);
//	//		if (c == null && format == VdpFormat.COLOR256_1x1) {
//	//			c = paletteToIndex.get(paletteToIndex.ceilingKey(p));	// should be fixed now
//	//		}
//			if (c == null) {
//				return 0;
//			}
//			return (byte) (int) c;
//		}

	/**
	 * @return
	 */
	public VdpFormat getFormat() {
		return format;
	}
	
}
