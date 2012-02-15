/**
 * 
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

import org.eclipse.swt.graphics.ImageData;

import ejs.base.utils.Pair;

import v9t9.common.hardware.IVdpChip;
import v9t9.common.hardware.IVdpTMS9918A;
import v9t9.common.hardware.VdpV9938Consts;
import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.video.ColorMapUtils;
import v9t9.common.video.IVdpCanvasRenderer;
import v9t9.common.video.VdpColorManager;
import v9t9.common.video.VdpFormat;
import v9t9.video.ImageDataCanvas;
import v9t9.video.imageimport.ColorOctree.LeafNode;
import v9t9.video.imageimport.ImageImportOptions.Dither;

import static v9t9.common.hardware.VdpV9938Consts.*;

/**
 * This class handles converting arbitrary external images and 
 * converting them to fit in the current TMS9918A or V9938 mode.
 * @author ejs
 *
 */
public class ImageImport {
	private boolean DEBUG = false;

	private final ImageImportOptions options;
	private ImageData canvasImageData;
	private VdpFormat format;
	private byte[][] thePalette;

	private boolean useColorMappedGreyScale;
	private Dither ditherType;
	private boolean ditherMono;
	private boolean canSetPalette;

	private final ImageDataCanvas canvas;
	private final VdpColorManager colorMgr;
	private boolean paletteMappingDirty;
	private final IVdpChip vdp;
	private int firstColor;
	private int[] rgbs;


	/** mapping from RGB-32 pixel to each palette index */
	protected TreeMap<Integer, Integer> paletteToIndex;

	private Pair<Integer,Integer>[][] bitmapColors;

	private BufferedImage theImage;
	

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

	private final IVdpCanvasRenderer renderer;

	public ImageImport(IVdpCanvasRenderer renderer, ImageDataCanvas canvas, IVdpChip vdp, ImageImportOptions imageImportOptions) {
		this.renderer = renderer;
		this.canvas = canvas;
		this.options = imageImportOptions;
		this.colorMgr = canvas.getColorMgr();
		this.vdp = vdp;
		this.canvasImageData = canvas.getImageData();
		this.format = canvas.getFormat();
		this.thePalette = colorMgr.getColorPalette();
		
		this.useColorMappedGreyScale = colorMgr.isGreyscale();
		
		this.rgbs = new int[canvasImageData.width];
		
	}
	
	private void ditherFSApplyError(BufferedImage img, int x, int y, int sixteenths, int r_error, int g_error, int b_error) {
		int pixel = img.getRGB(x, y);
		int r = clamp(((pixel >> 16) & 0xff) + (sixteenths * r_error / 16));
		int g = clamp(((pixel >> 8) & 0xff) + (sixteenths * g_error / 16));
		int b = clamp(((pixel >> 0) & 0xff) + (sixteenths * b_error / 16));
		img.setRGB(x, y, (r << 16) | (g << 8) | b | 0xff000000);

	}
	private final int clamp(int i) {
		return i < 0 ? 0 : i > 255 ? 255 : i;
	}

	private void ditherFSPixel(BufferedImage img, IPaletteColorMapper mapColor,
			Histogram hist, int x, int y, boolean limit8) {

		
		int pixel = img.getRGB(x, y);

		int newC = mapColor.getClosestPaletteEntry(x, y, pixel);
		
		int newPixel;
		
		int offs = y * img.getWidth() + x;
		int prevC = hist.mappedColors()[offs];
		
		boolean skipped = false;
		
		if (!useColorMappedGreyScale && !ditherMono && prevC != -1) {
			if (newC == prevC) {
				// was not dithered away, but palette may have changed,
				// so update color but ignore dithering
				pixel = 
					newPixel = mapColor.getPalettePixel(newC);
				skipped = true;
			}
			else {
				// don't change existing colors, but propagate error
				newPixel = mapColor.getPalettePixel(newC);
			}
		} else  {
			newPixel = mapColor.getPalettePixel(newC);
		}
		
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
		else if (!ditherMono) {
			if (limit8) {
				if (!skipped) {
					if (Math.abs(r_error) < 0x20 && Math.abs(g_error) < 0x20 && Math.abs(b_error) < 0x20) {
						r_error /= 4;
						g_error /= 4;
						b_error /= 4;
					}
					else {
						//if (Math.abs(r_error) < 0x20)
						//	r_error /= 4;
						//else
							r_error /= 2;
						//if (Math.abs(g_error) < 0x20)
						//	g_error /= 4;
						//else
							g_error /= 2;
						//if (Math.abs(b_error) < 0x20)
						//	b_error /= 4;
						//else
							b_error /= 2;
					}
				}
			} else {
				if (format == VdpFormat.COLOR256_1x1) {
					// will never find a match if the color 
					// didn't already match...
					if (Math.abs(r_error) < 0x8 && Math.abs(g_error) < 0x8 && Math.abs(b_error) < 0x8) {
						r_error /= 2;
						g_error /= 2;
						b_error /= 2;
					}
				}
			}
		}
		
		if ((r_error | g_error | b_error) != 0) {
			if (x + 1 < img.getWidth()) {
				// x+1, y
				ditherFSApplyError(img, x + 1, y,  
						7, r_error, g_error, b_error);
			}
			if (y + 1 < img.getHeight()) {
				if (x > 0)
					ditherFSApplyError(img, x - 1, y + 1, 
							3, r_error, g_error, b_error);
				ditherFSApplyError(img, x, y + 1, 
						5, r_error, g_error, b_error);
				if (x + 1 < img.getWidth())
					ditherFSApplyError(img, x + 1, y + 1, 
							1, r_error, g_error, b_error);
			}
		}
	}
	
	private void ditherFloydSteinberg(BufferedImage img, IPaletteColorMapper mapColor, Histogram hist, boolean limitDither) {
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				ditherFSPixel(img, mapColor, hist, x, y, limitDither);
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

		ColorOctree octree = new ColorOctree(3, 8*8*8, false, false);

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
		if (numColors > (format == VdpFormat.COLOR256_1x1 ? 256 : 16))
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
	/** Import image from 'img' and update the canvas' image data
	 * with colors from the current palette and in a configuration
	 * legal for the current mode. */
	protected void setImageData(BufferedImage img) {
		if (format == null || format == VdpFormat.TEXT)
			return;
		
		flatten(img);
		
		if (!importDirectMappedImage(img)) {
			int midLum = equalize(img);
			
			//reduceNoise(img);
			
			convertImageToColorMap(img, midLum);
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
		
		replaceImageData(img);
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
		
		int numColors = format == VdpFormat.COLOR4_1x1 ? 4
				: 16;
		
		// effective minimum distance for any mode
		int maxDist = 0x8*0x8 * 3;
		int numPixels = img.getWidth() * img.getHeight();
		
		boolean matched = false;
		Histogram hist = new Histogram(img);
		
		List<byte[][]> palettes = new ArrayList<byte[][]>();
		palettes.add(thePalette);
		palettes.addAll(Arrays.asList(VdpColorManager.palettes()));
		
		for (byte[][] palette : palettes) {
			FixedPaletteMapColor paletteMapper = new FixedPaletteMapColor(
					palette, firstColor, numColors);
			int matchedC = hist.generate(paletteMapper, maxDist); 
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

	private void convertImageToColorMap(BufferedImage img, int midLum) {
		IPaletteMapper mapColor;

		if (ditherMono) {
			int reg = vdp.getRegister(7);
			mapColor = new MonoMapColor((reg >> 4) & 0xf, reg & 0xf, midLum);
			firstColor = 0;
		} else if (format == VdpFormat.COLOR16_8x1 || format == VdpFormat.COLOR16_4x4) {
			if (canSetPalette) {
				if (false)
					createOptimalPaletteWithHSV(img, 16);
				else
					createOptimalPalette(img, 16);
				mapColor = new UserPaletteMapColor(thePalette, firstColor, 16, useColorMappedGreyScale);
			} else {
				boolean isStandardPalette = false;
				if (!useColorMappedGreyScale) {
					isStandardPalette = canvas.getColorMgr().isStandardPalette();
				}
				
				if (isStandardPalette) {
					mapColor = new TI16MapColor(thePalette);
					//mapColor = new UserPaletteMapColor(thePalette, firstColor, 16, isGreyScale);
				}
				else
					mapColor = new UserPaletteMapColor(thePalette, firstColor, 16, useColorMappedGreyScale);
			}
		}
		else if (format == VdpFormat.COLOR16_1x1) {
			if (canSetPalette) 
				createOptimalPalette(img, 16);
			mapColor = new UserPaletteMapColor(thePalette, firstColor, 16, useColorMappedGreyScale);
		}
		else if (format == VdpFormat.COLOR4_1x1) {
			if (canSetPalette) {
				if (useColorMappedGreyScale)
					createOptimalGreyscalePalette(img, 4);
				else
					createOptimalPalette(img, 4);
			}
			mapColor = new UserPaletteMapColor(thePalette, firstColor, 4, useColorMappedGreyScale);
		}
		else if (format == VdpFormat.COLOR256_1x1) {
			mapColor = new RGB332MapColor(useColorMappedGreyScale);
		}
		else if (format == VdpFormat.COLOR16_8x8) {
			mapColor = new TI16MapColor(thePalette);
		}
		else {
			return;
		}
		
		Histogram hist = optimizeForNColors(img, mapColor);
		
		updatePaletteMapping();

		IPaletteColorMapper colorMapper = mapColor;
		
		if (false && ditherType != Dither.NONE && format == VdpFormat.COLOR16_8x1) {
			prepareBitmapModeColors(img, mapColor);
			colorMapper = new BitmapDitherColorMapper(mapColor);
		}
		
		if (ditherType == Dither.FS) {
			ditherFloydSteinberg(img, colorMapper, hist, false);
		} else if (ditherType == Dither.ORDERED) {
			ditherOrdered(img, colorMapper);
		} else {
			ditherNone(img, colorMapper);
		}
	}


	void createOptimalPaletteWithHSV(BufferedImage image, int colorCount) {
		int toAllocate = colorCount - firstColor;
			
		ColorOctree octree = new ColorOctree(3, toAllocate, true, false);
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
		
		octree.reduceTree();

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


	private void createOptimalPalette(BufferedImage image, int colorCount) {
		int toAllocate = colorCount - firstColor;
		
		if (false && format == VdpFormat.COLOR16_8x1) {
			// we will not be able to allocate more than two colors
			// per 8 pixels anyway, so fit the palette as if each
			// 8 pixel block were only two pixels.
			image = AwtImageUtils.getScaledInstance(
					image, image.getWidth() / 4, image.getHeight(), 
					RenderingHints.VALUE_INTERPOLATION_BICUBIC,
					false);

		}
		
		ColorOctree octree = new ColorOctree(4, toAllocate, true, false);
		int[] prgb = { 0, 0, 0 };
		int[] rgbs = new int[image.getWidth()];
		for (int y = 0; y < image.getHeight(); y++) {
			image.getRGB(0, y, rgbs.length, 1, rgbs, 0, rgbs.length);
			for (int x = 0; x < rgbs.length; x++) {
				ColorMapUtils.pixelToRGB(rgbs[x], prgb);
				if (useColorMappedGreyScale)
					ColorMapUtils.rgbToGreyForGreyscaleMode(prgb, prgb);
				octree.addColor(prgb);
			}
		}
		
		octree.reduceTree();

		int index = firstColor;
		
		List<LeafNode> leaves = octree.gatherLeaves();
		
		for (ColorOctree.LeafNode node :  leaves) {
			int[] repr = node.reprRGB();
			
			if (DEBUG) System.out.println("palette[" + index +"] = " 
					+ Integer.toHexString(repr[0]) + "/" 
					+ Integer.toHexString(repr[1]) + "/" 
					+ Integer.toHexString(repr[2]));

			if (useColorMappedGreyScale)
				ColorMapUtils.rgbToGreyForGreyscaleMode(prgb, prgb);
			else
				ColorMapUtils.mapForRGB555(repr);
			
			thePalette[index][0] = (byte) repr[0];
			thePalette[index][1] = (byte) repr[1];
			thePalette[index][2] = (byte) repr[2];
			
			index++;
			
		}
	}

	/**
	 * Pick areas where histogram finds the most luminance.
	 * @param image
	 * @param colorCount
	 */
	private void createOptimalGreyscalePalette(BufferedImage image, int colorCount) {
		int toAlloc = colorCount - firstColor;
		
		Histogram prevHist = null;
		
		for (int mask = 0; mask < 3; mask++) {
			final int maskVal = (~0) << mask;
			final int max = 8 - (1 << mask);
			IColorMapper greyMapper = new IColorMapper() {
				
				@Override
				public int mapColor(int pixel, int[] dist) {
					int lum = ColorMapUtils.getPixelLum(pixel);
					return ((lum >> 5) & maskVal) * 7 / max;
				}
			};
			
			Histogram hist = new Histogram(image);
			hist.generate(greyMapper, Integer.MAX_VALUE);
	
			// take darkest and brighest first
			Map<Integer, Integer> colorToCountMap = hist.colorToCountMap();
			if (colorToCountMap.size() > toAlloc && mask + 1 < 3) {
				prevHist = hist;
				continue;
			}
		
			if (colorToCountMap.size() < colorCount && prevHist != null)
				hist = prevHist;
			
			for (int cidx = firstColor; cidx < colorCount; cidx++) {
				int c = hist.getColorIndex(cidx);
				colorMgr.setGRB333(cidx, c, c, c);
			}
			
			break;
		}
		
	}

	private void replaceImageData(BufferedImage img) {
		int xoffs, yoffs;
		
		Arrays.fill(canvasImageData.data, (byte) 0);
	
		if (format == VdpFormat.COLOR16_4x4) {
			xoffs = (64 - img.getWidth()) / 2;
			yoffs = (48 - img.getHeight()) / 2;
		} else {
			xoffs = (canvas.getVisibleWidth() - img.getWidth() + canvas.getXOffset()) / 2;
			yoffs = (canvas.getVisibleHeight() - img.getHeight() + canvas.getYOffset()) / 2;
		}
	
		if (format == VdpFormat.COLOR16_8x1) {
			// be sure we select the 8 pixel groups sensibly
			if ((xoffs & 7) > 3)
				xoffs = (xoffs + 7) & ~7;
			else
				xoffs = xoffs & ~7;
		}
		
		if (format == VdpFormat.COLOR16_8x1 && !ditherMono) {
			
			reduceBitmapMode(img, xoffs, yoffs);
	
		} else {
	
			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					canvasImageData.setPixel(x + xoffs, y + yoffs, img.getRGB(x, y));
				}
			}
		}
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
	private Histogram optimizeForNColors(BufferedImage img, IPaletteMapper mapColor) {
		
		int numColors = mapColor.getNumColors();
		
		int ourDist = mapColor.getMinimalPaletteDistance();
		
		if (DEBUG) System.out.println("Minimum color palette distance: " + ourDist);

		Histogram hist = new Histogram(img);
		int mappedColors = 0;
		int interestingColors = 0;
		
		mappedColors = hist.generate(mapColor, ourDist);
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
					int min = 255;
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
				int total = 0;
				for (int i = 0; i < sorted.size(); i++) {
					Pair<Integer, Integer> ent = sorted.get(i);
					total += ent.second;
				}
				
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
	protected void prepareBitmapModeColors(final BufferedImage img, final IPaletteMapper mapColor) {
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
	protected void reduceBitmapMode(final BufferedImage img, final int xoffs, final int yoffs) {
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
						
						canvasImageData.setPixel(xd + xoffs, y + yoffs, newPixel);
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
						
						canvasImageData.setPixel(xd + xoffs, y + yoffs, newPixel); 
					}
				}
				
			}
		});
	}

	protected void updatePaletteMapping() {
		int ncols;
		if (format == VdpFormat.COLOR16_1x1 
				|| format == VdpFormat.COLOR16_8x1 
				|| format == VdpFormat.COLOR16_4x4
				|| format == VdpFormat.COLOR16_8x8) {
			ncols = 16;
		}
		else if (format == VdpFormat.COLOR4_1x1) {
			ncols = 4;
		}
		else if (format == VdpFormat.COLOR256_1x1) {
			ncols = 256;
		}
		else {
			return;
		}
	
		paletteToIndex = new TreeMap<Integer, Integer>();
		
		if (ncols < 256) {
			// TODO: why is greyscale acting funny?
			if (useColorMappedGreyScale) {
				// ensure palette is valid: higher bit depth may have
				// been guessed during palette optimization
				for (int c = 0; c < ncols; c++) {
					vdp.setRegister(VdpV9938Consts.REG_PAL0 + c, ColorMapUtils.rgb8ToRgbRBXG(thePalette[c]));
					//colorMgr.setRGB333(c, thePalette[c]);
				}
			} else {
				// ensure palette is valid: higher bit depth may have
				// been guessed during palette optimization
				for (int c = 0; c < ncols; c++) {
					vdp.setRegister(VdpV9938Consts.REG_PAL0 + c, ColorMapUtils.rgb8ToRgbRBXG(thePalette[c]));
					//colorMgr.setRGB(c, thePalette[c]);
				}
				
			}
			
			if (ditherMono) {
				byte reg = (byte) vdp.getRegister(7);
				paletteToIndex.put(0x0, (reg >> 4) & 0xf);
				paletteToIndex.put(0xffffff, (reg >> 0) & 0xf);
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

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.IBitmapPixelAccess#getHeight()
	 */
	/*
	@Override
	public int getHeight() {
		return theImage.getHeight();
	}
	*/
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.IBitmapPixelAccess#getWidth()
	 */
	/*
	@Override
	public int getWidth() {
		return theImage.getWidth();
	}
	*/
	public byte getPixel(int x, int y) {
		if (paletteMappingDirty) {
			updatePaletteMapping();
			if (paletteMappingDirty)
				return 0;
		}
		int p = canvasImageData.getPixel(x, y) & 0xffffff;
		
		Integer c = paletteToIndex.get(p);
		if (c == null && format == VdpFormat.COLOR256_1x1) {
			// whhyyyy is someone losing precision?!
			c = paletteToIndex.get(paletteToIndex.ceilingKey(p));
		}
		if (c == null) {
			return 0;
		}
		return (byte) (int) c;
	}


	/**
	 * @param image
	 * @param isLowColor if the image is known to have a small number of colors -- don't scale
	 */
	public void importImage() {
		BufferedImage image = options.getImage();
		if (image == null)
			return;

		int targWidth = canvas.getVisibleWidth();
		int targHeight = canvas.getVisibleHeight();

		if (canvas.getFormat() == VdpFormat.COLOR16_4x4) {
			targWidth = 64;
			targHeight = 48;
		}
		
		float aspect = (float) targWidth / targHeight / 256.f * 192.f;
		int realWidth = image.getWidth(null);
		int realHeight = image.getHeight(null);
		
		if (options.isKeepAspect()) {
			if (realWidth < 0 || realHeight < 0) {
				throw new IllegalArgumentException("image has zero or negative size");
			}
			
			if (realWidth != targWidth || realHeight != targHeight) {
				if (realWidth * targHeight * aspect > realHeight * targWidth) {
					targHeight = (int) (targWidth * realHeight / realWidth / aspect);
				} else {
					targWidth = (int) (targHeight * realWidth * aspect / realHeight);
					
					// make sure, for bitmap mode, that the size is a multiple of 8,
					// otherwise the import into video memory will destroy the picture
					if (format == VdpFormat.COLOR16_8x1) {
						targWidth &= ~7;
						targHeight = (int) (targWidth * realHeight / realWidth / aspect);
					}
				}
			}
		}
		
		if (canvas.getFormat() == VdpFormat.COLOR16_8x8) {
			// make a maximum of 256 blocks  (256*64 = 16384)
			// Reduces total screen real estate down by sqr(3)
			//targWidth = (int) (targWidth / 1.732) & ~7;
			//targHeight = (int) (targHeight / 1.732) & ~7;
			while ((targWidth & ~0x7) * 
					 (((int)(targWidth * realHeight / realWidth / aspect) + 7) & ~0x7) > 16384) {
				targWidth *= 0.99;
				targHeight *= 0.99;
			}
			targWidth &= ~0x7;
			targHeight = (int) (targWidth * realHeight / realWidth / aspect);
			if (DEBUG) System.out.println("Graphics mode: " + targWidth*((targHeight+7)&~0x7));
		}
		
		Object hint = options.isScaleSmooth() ? RenderingHints.VALUE_INTERPOLATION_BILINEAR
					:  RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;

		BufferedImage scaled = AwtImageUtils.getScaledInstance(
				image, targWidth, targHeight, 
				hint,
				false);
		//System.out.println(scaled.getWidth(null) + " x " +scaled.getHeight(null));

		if (options.isAsGreyScale()) {
			convertGreyscale(scaled);
		}
		
		canSetPalette = options.isOptimizePalette();
		ditherType = options.getDitherType();
		ditherMono = options.isDitherMono();

		firstColor = (canSetPalette && colorMgr.isClearFromPalette() ? 0 : 1);


		if (canvas.getFormat() == VdpFormat.COLOR16_8x8) {
			canSetPalette = false;
			ditherMono = true;
		}
		
		byte[][] orig = options.getOrigPalette();
		if (orig != null) {
			for (int i = 0; i < thePalette.length; i++) {
				System.arraycopy(orig[i], 0, thePalette[i], 0, 3);
			}
		}
		updatePaletteMapping();
		
		this.theImage = scaled;

		synchronized (renderer) {
			synchronized (canvas) {
				setImageData(scaled);

				setVideoMemory();
				canvas.markDirty();
			}
		}
	}

	/**
	 * 
	 */
	private void setVideoMemory() {
		if (vdp instanceof IVdpTMS9918A) {
			IVdpTMS9918A vdp99 = (IVdpTMS9918A) vdp;
			if (format == VdpFormat.COLOR16_8x1) {
				setVideoMemoryBitmapMode(vdp99);
			} 
			else if (format == VdpFormat.COLOR16_8x8) {
				setVideoMemoryGraphicsMode(vdp99);
			}
			else if (format == VdpFormat.COLOR16_1x1) {
				setVideoMemoryV9938BitmapMode(vdp99);
			}
			else if (format == VdpFormat.COLOR256_1x1) {
				setVideoMemoryV9938BitmapMode(vdp99);
			}
			else if (format== VdpFormat.COLOR16_4x4) {
				setVideoMemoryMulticolorMode(vdp99);
			}
		}
	}

	/**
	 * @param vdp99
	 */
	private void setVideoMemoryMulticolorMode(IVdpTMS9918A vdp99) {
		ByteMemoryAccess patt = vdp.getByteReadMemoryAccess(vdp99.getPatternTableBase());
		
		for (int y = 0; y < 48; y++) {
			for (int x = 0; x < 64; x += 2) {
				
				byte f = getPixel(x, y);
				byte b = getPixel(x + 1, y);
				
				int poffs = ((y >> 3) << 8) + (y & 7) + ((x >> 1) << 3);  
				//System.out.println("("+y+","+x+") = "+ poffs);
				vdp.writeAbsoluteVdpMemory(patt.offset + poffs, (byte) ((f << 4) | b));
			}
		}
	}

	protected interface IBitmapModeImportHandler {
		byte createImageDataByte(int x, int row);

		/**
		 * @return
		 */
		int getRowStride();

		/**
		 * @return
		 */
		int getColumnStride();
	}
	
	class GraphicsMode4ImportHandler implements IBitmapModeImportHandler {
		@Override
		public byte createImageDataByte(int x, int row) {
			
			byte f = getPixel(x, row);
			byte b = getPixel(x + 1, row);
			return (byte) ((f << 4) | b);
		}

		@Override
		public int getRowStride() {
			return 128;
		}

		@Override
		public int getColumnStride() {
			return 2;
		}
	}

	class GraphicsMode5ImportHandler implements IBitmapModeImportHandler {
		@Override
		public byte createImageDataByte(int x, int y) {
			byte p = 0;
			for (int xo = 0; xo < 4; xo++) {
				byte c = getPixel(x + xo, y);
				p |= c << ((3 - xo) * 2);
			}
			return p;
		}

		@Override
		public int getRowStride() {
			return 128;
		}

		@Override
		public int getColumnStride() {
			return 4;
		}
	}

	class GraphicsMode6ImportHandler implements IBitmapModeImportHandler {
		@Override
		public byte createImageDataByte(int x, int y) {
			byte f = getPixel(x, y);
			byte b = getPixel(x + 1, y);
			
			return (byte) ((f << 4) | b);
		}

		@Override
		public int getRowStride() {
			return 256;
		}

		@Override
		public int getColumnStride() {
			return 2;
		}
	}

	class GraphicsMode7ImportHandler implements IBitmapModeImportHandler {
		@Override
		public byte createImageDataByte(int x, int y) {
			
			return getPixel(x, y);
		}

		@Override
		public int getRowStride() {
			return 256;
		}

		@Override
		public int getColumnStride() {
			return 1;
		}
		
	}
	/**
	 * @param vdp99
	 */
	private void setVideoMemoryV9938BitmapMode(IVdpTMS9918A vdp99) {
		IBitmapModeImportHandler handler;
		int mx;
		switch (vdp.getModeNumber()) {
		case MODE_GRAPHICS4:
			handler = new GraphicsMode4ImportHandler();
			mx = 256;
			break;
		case MODE_GRAPHICS5:
			handler = new GraphicsMode5ImportHandler();
			mx = 512;
			break;
		case MODE_GRAPHICS6:
			handler = new GraphicsMode6ImportHandler();
			mx = 512;
			break;
		case MODE_GRAPHICS7:
			handler = new GraphicsMode7ImportHandler();
			mx = 256;
			break;
		default:
			throw new IllegalStateException();	
		}
		
		int ystep = vdp.isInterlacedEvenOdd() ? 2 : 1;
		int my =  (vdp.getRegister(9) & 0x80) != 0 ? 212 : 192;
		int graphicsPageSize = vdp.getGraphicsPageSize();
		
		int colstride = handler.getColumnStride();
		int rowstride = handler.getRowStride();
		
		for (int eo = 0; eo < ystep; eo++) {
			ByteMemoryAccess patt = vdp.getByteReadMemoryAccess(vdp99.getPatternTableBase()
					^ (eo != 0 ? graphicsPageSize : 0));
			for (int y = 0; y < my; y++) {
				int row = y * ystep + eo;
				for (int x = 0; x < mx; x += colstride) {
					
					byte byt = handler.createImageDataByte(x, row);
					
					int poffs = y * rowstride + (x / colstride); 
					vdp.writeAbsoluteVdpMemory(patt.offset + poffs, byt);
				}
			}
		}
		
	}
	/**
	 * @param vdp99
	 */
	private void setVideoMemoryGraphicsMode(IVdpTMS9918A vdp99) {
		ByteMemoryAccess screen = vdp.getByteReadMemoryAccess(vdp99.getScreenTableBase());
		ByteMemoryAccess patt = vdp.getByteReadMemoryAccess(vdp99.getPatternTableBase());
		ByteMemoryAccess color = vdp.getByteReadMemoryAccess(vdp99.getColorTableBase());
		
		// assume char 255 is not used
		for (int i = 0; i < 768; i++)
			vdp.writeAbsoluteVdpMemory(screen.offset + i, (byte) 0xff);

		for (int i = 0; i < 8; i++)
			vdp.writeAbsoluteVdpMemory(patt.offset + 255*8 + i, (byte) 0x0);

		byte b = 0;
		
		byte cb = (byte) vdp.getRegister(7);
		cb = (byte) ((cb & 0xf) | 0x10);
		
		b = (byte) ((cb >> 0) & 0xf);

		for (int i = 0; i < 32; i++)
			vdp.writeAbsoluteVdpMemory(color.offset + i, cb);

		int width = theImage.getWidth();
		int height = theImage.getHeight();
		
		int yoffs = ((192 - height) / 2) & ~0x7;
		int xoffs = ((256 - width) / 2) & ~0x7;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x+= 8) {
				int ch = ((y >> 3) * ((width + 7) >> 3)) + (x >> 3);
				if (ch > 0xff)
					throw new IllegalStateException();
				int choffs = (((y + yoffs) >> 3) << 5) + ((x + xoffs) >> 3);
				
				vdp.writeAbsoluteVdpMemory(screen.offset + choffs, (byte) ch);
				
				int poffs = (ch << 3) + (y & 7);
				
				byte p = 0;
				
				for (int xo = 0; xo < 8; xo++) {
					byte c = getPixel(x + xo + xoffs, y + yoffs);
					if (c != b) {
						p |= 0x80 >> xo;
					}
				}

				vdp.writeAbsoluteVdpMemory(patt.offset + poffs, p);
			}
		}
		
	}

	/**
	 * @param vdp99
	 */
	private void setVideoMemoryBitmapMode(IVdpTMS9918A vdp99) {
		boolean isMono = vdp99.isBitmapMonoMode();
		
		ByteMemoryAccess screen = vdp.getByteReadMemoryAccess(vdp99.getScreenTableBase());
		ByteMemoryAccess patt = vdp.getByteReadMemoryAccess(vdp99.getPatternTableBase());
		ByteMemoryAccess color = vdp.getByteReadMemoryAccess(vdp99.getColorTableBase());
		
		byte f = 0, b = 0;
		
		if (isMono) {
			f = (byte) ((vdp.getRegister(7) >> 4) & 0xf);
			b = (byte) ((vdp.getRegister(7) >> 0) & 0xf);
		}

		for (int y = 0; y < 192; y++) {
			for (int x = 0; x < 256; x += 8) {
				
				int choffs = ((y >> 6) << 8) + ((y & 0x3f) >> 3) * 32 + (x >> 3);
				int ch = choffs & 0xff;
				
				if ((y & 7) == 0) {
					vdp.writeAbsoluteVdpMemory(screen.offset + choffs, (byte) ch);
				}

				int poffs = (y >> 6) * 0x800 + (ch << 3) + (y & 7);
				
				byte p = 0;
				
				if (!isMono) {
					// in color mode, by convention keep the foreground color
					// as the lesser color.
					f = getPixel(x, y);
					p = (byte) 0x80;
				
					boolean gotBG = false;
					for (int xo = 1; xo < 8; xo++) {
						byte c = getPixel(x + xo, y);
						if (c == f) {
							p |= 0x80 >> xo;
						} else {
							if (!gotBG) {
								if (c < f) {
									b = f;
									f = c;
									p ^= (0xff << (8 - xo));
									p |= 0x80 >> xo;
								} else {
									b = c;
								}
								gotBG = true;
							}
						}
					}
					
					vdp.writeAbsoluteVdpMemory(color.offset + poffs, (byte) ((f << 4) | (b)));
				} else {
					// in mono mode, mapper has matched with fg and bg from vr7
					for (int xo = 0; xo < 8; xo++) {
						byte c = getPixel(x + xo, y);
						if (c == f) {
							p |= 0x80 >> xo;
						}
					}
				}

				vdp.writeAbsoluteVdpMemory(patt.offset + poffs, p);
			}
		}
	}

	/**
	 * @param img
	 */
	private void convertGreyscale(BufferedImage img) {
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int pixel = img.getRGB(x, y);
				int lum = ColorMapUtils.getPixelLum(pixel);
				int newPixel = ColorMapUtils.rgb8ToPixel(new int[] { lum, lum, lum });
				img.setRGB(x, y, newPixel | (pixel & 0xff000000));
			}
		}
		
		
	}

	/**
	 * @return
	 */
	public VdpFormat getFormat() {
		return format;
	}
	
}
