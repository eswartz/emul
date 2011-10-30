/**
 * 
 */
package v9t9.emulator.clients.builtin.video.image;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.eclipse.swt.graphics.ImageData;
import org.ejs.coffee.core.utils.Pair;

import v9t9.emulator.clients.builtin.video.ColorMapUtils;
import v9t9.emulator.clients.builtin.video.IBitmapPixelAccess;
import v9t9.emulator.clients.builtin.video.ImageDataCanvas;
import v9t9.emulator.clients.builtin.video.VdpColorManager;
import v9t9.emulator.clients.builtin.video.VdpCanvas.Format;
import v9t9.emulator.clients.builtin.video.image.ColorOctree.LeafNode;
import v9t9.emulator.clients.builtin.video.image.ImportOptions.Dither;
import v9t9.engine.VdpHandler;

/**
 * This class handles converting arbitrary external images and 
 * converting them to fit in the current TMS9918A or V9938 mode.
 * @author ejs
 *
 */
public class ImageImport implements IBitmapPixelAccess {
	private boolean DEBUG = false;

	private ImageData canvasImageData;
	private Format format;
	private byte[][] thePalette;

	private boolean useColorMappedGreyScale;
	private Dither ditherType;
	private boolean canSetPalette;

	private final ImageDataCanvas canvas;
	private final VdpColorManager colorMgr;
	private boolean paletteMappingDirty;
	private final VdpHandler vdp;
	private int firstColor;
	private int[] rgbs;

	private Pair<Integer, Integer>[][] bitmapColors;

	/** mapping from RGB-32 pixel to each palette index */
	protected TreeMap<Integer, Integer> paletteToIndex;
	

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

	public ImageImport(ImageDataCanvas canvas, VdpHandler vdp) {
		this.canvas = canvas;
		this.colorMgr = canvas.getColorMgr();
		this.vdp = vdp;
		this.canvasImageData = canvas.getImageData();
		this.format = canvas.getFormat();
		this.thePalette = colorMgr.getColorPalette();
		
		this.useColorMappedGreyScale = colorMgr.isGreyscale();
		
		this.rgbs = new int[canvasImageData.width];
		
	}
	
	private void ditherRGB(BufferedImage img, int x, int y, int sixteenths, int r_error, int g_error, int b_error) {
		int pixel = img.getRGB(x, y);
		int r = clamp(((pixel >> 16) & 0xff) + (sixteenths * r_error / 16));
		int g = clamp(((pixel >> 8) & 0xff) + (sixteenths * g_error / 16));
		int b = clamp(((pixel >> 0) & 0xff) + (sixteenths * b_error / 16));
		img.setRGB(x, y, (r << 16) | (g << 8) | b | 0xff000000);

	}
	private final int clamp(int i) {
		return i < 0 ? 0 : i > 255 ? 255 : i;
	}

	private void ditherize(BufferedImage img, IPaletteColorMapper mapColor,
			Histogram hist, int x, int y, boolean limit8) {

		
		int pixel = img.getRGB(x, y);

		int newC = mapColor.getClosestPaletteEntry(x, y, pixel);
		
		int newPixel;
		
		int offs = y * img.getWidth() + x;
		int prevC = hist.mappedColors()[offs];
		
		boolean skipped = false;
		
		if (!useColorMappedGreyScale && ditherType != Dither.MONO && prevC != -1) {
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
		else if (ditherType != Dither.MONO) {
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
				if (format == Format.COLOR256_1x1) {
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
				ditherRGB(img, x + 1, y,  
						7, r_error, g_error, b_error);
			}
			if (y + 1 < img.getHeight()) {
				if (x > 0)
					ditherRGB(img, x - 1, y + 1, 
							3, r_error, g_error, b_error);
				ditherRGB(img, x, y + 1, 
						5, r_error, g_error, b_error);
				if (x + 1 < img.getWidth())
					ditherRGB(img, x + 1, y + 1, 
							1, r_error, g_error, b_error);
			}
		}
	}
	

	class BitmapDitherColorMapper implements IPaletteColorMapper {
		
		private final IPaletteMapper paletteMapper;
		
		public BitmapDitherColorMapper(IPaletteMapper paletteMapper) {
			this.paletteMapper = paletteMapper;
		}
		@Override
		public int getClosestPaletteEntry(int x, int y, int pixel) {
			if (bitmapColors != null && bitmapColors[y] != null) {
				Pair<Integer, Integer> fgbg = bitmapColors[y][x / 8];
				
				if (fgbg != null) {
					int fgdist = useColorMappedGreyScale ? ColorMapUtils.getPixelLumDistance(fgbg.first, pixel) 
							: ColorMapUtils.getPixelDistance(fgbg.first, pixel);
					int bgdist = useColorMappedGreyScale ? ColorMapUtils.getPixelLumDistance(fgbg.second, pixel) 
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

	private void ditherForBitmapMode(BufferedImage img, IPaletteMapper mapColor, Histogram hist) {
		prepareBitmapModeColors(img, mapColor);
		
		BitmapDitherColorMapper mapper = new BitmapDitherColorMapper(mapColor);
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x += 8) {
				for (int xo = 0; xo < 8; xo++) {
					ditherize(img, mapper, hist, x + xo, y, false);
				}
			}
		}
		
	}
	
	private void dither(BufferedImage img, IPaletteMapper mapColor, Histogram hist, boolean limitDither) {
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				ditherize(img, mapColor, hist, x, y, limitDither);
			}
		}
		
	}
	
	private void ditherNone(BufferedImage img, IPaletteMapper mapColor) {
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
	public static boolean isModeSupported(Format format) {
		if (format == null || format == Format.TEXT || format == Format.COLOR16_8x8) 
			return false;
			
		return true;
	}
	
	protected void reduceNoise(BufferedImage img) {
		int width = img.getWidth();
		int[] rgbs = new int[width];
		int[] prgb = { 0, 0, 0 };

		ColorOctree octree = new ColorOctree(3, 8*8*8, false);

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
		if (numColors > (format == Format.COLOR256_1x1 ? 256 : 16))
			return;
		
		for (int y = 0; y < img.getHeight(); y++) {
			img.getRGB(0, y, width, 1, rgbs, 0, width);
			for (int x = 0; x < width; x++) {
				ColorMapUtils.pixelToRGB(rgbs[x], prgb);
				prgb = ColorMapUtils.mapForRGB333(prgb);
				rgbs[x] = ColorMapUtils.rgb8ToPixel(prgb);
			}
			img.setRGB(0, y, width, 1, rgbs, 0, width);
		}
	}
	/** Import image from 'img' and update the canvas' image data
	 * with colors from the current palette and in a configuration
	 * legal for the current mode. */
	protected void setImageData(BufferedImage img) {
		if (format == null || format == Format.TEXT || format == Format.COLOR16_8x8)
			return;
		
		flatten(img);
		
		if (!importDirectMappedImage(img)) {
			equalize(img);
			
			//reduceNoise(img);
			
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
		if (format == Format.COLOR256_1x1 || ditherType == Dither.MONO)
			return false;
		
		int numColors = format == Format.COLOR4_1x1 ? 4
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

	private void convertImageToColorMap(BufferedImage img) {
		IPaletteMapper mapColor;

		if (ditherType == Dither.MONO) {
			int reg = vdp.readVdpReg(7);
			mapColor = new MonoMapColor((reg >> 4) & 0xf, reg & 0xf);
			firstColor = 0;
		} else if (format == Format.COLOR16_8x1 || format == Format.COLOR16_4x4) {
			if (canSetPalette) {
				createOptimalPalette(img, 16);
				mapColor = new UserPaletteMapColor(thePalette, firstColor, 16, useColorMappedGreyScale);
			} else {
				boolean isStandardPalette = false;
				if (!useColorMappedGreyScale) {
					for (byte[][] palette : VdpColorManager.allPalettes()) {
						if (palette.length == thePalette.length) {
							boolean match = true;
							for (int i = 0; i < palette.length; i++) {
								if (!Arrays.equals(palette[i], thePalette[i])) {
									match = false;
									break;
								}
							}
							if (match) {
								isStandardPalette = true;
								break;
							}
						}
					}
				}
				
				if (isStandardPalette) {
					mapColor = new TI16MapColor(thePalette);
					//mapColor = new UserPaletteMapColor(thePalette, firstColor, 16, isGreyScale);
				}
				else
					mapColor = new UserPaletteMapColor(thePalette, firstColor, 16, useColorMappedGreyScale);
			}
		}
		else if (format == Format.COLOR16_1x1) {
			if (canSetPalette) 
				createOptimalPalette(img, 16);
			mapColor = new UserPaletteMapColor(thePalette, firstColor, 16, useColorMappedGreyScale);
		}
		else if (format == Format.COLOR4_1x1) {
			if (canSetPalette) {
				if (useColorMappedGreyScale)
					createOptimalGreyscalePalette(img, 4);
				else
					createOptimalPalette(img, 4);
			}
			mapColor = new UserPaletteMapColor(thePalette, firstColor, 4, useColorMappedGreyScale);
		}
		else if (format == Format.COLOR256_1x1) {
			mapColor = new RGB332MapColor(useColorMappedGreyScale);
		}
		else {
			return;
		}
		Histogram hist = optimizeForNColors(img, mapColor);
		
		updatePaletteMapping();

		if (ditherType != Dither.NONE) {
			if (format == Format.COLOR16_8x1 && ditherType == Dither.FS_LOW) {
				ditherForBitmapMode(img, mapColor, hist);
			} else {
				dither(img, mapColor, hist, ditherType == Dither.FS_LOW);
			}
		} else {
			ditherNone(img, mapColor);
		}
	}

	void createOptimalPaletteWithHSV(BufferedImage image, int colorCount) {
		int toAllocate = colorCount - firstColor;
			
		ColorOctree octree = new ColorOctree(3, toAllocate, true);
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
			
			/*
			colorMgr.setGRB333(index, Math.min(255, (repr[1] * 0xff / 0xdf)) >> 5, 
				Math.min(255, (repr[0] * 0xff / 0xdf)) >> 5, 
				Math.min(255, (repr[2] * 0xff / 0xdf)) >> 5);
			*/
			thePalette[index][0] = (byte) repr[0];
			thePalette[index][1] = (byte) repr[1];
			thePalette[index][2] = (byte) repr[2];
			           
			index++;
			
		}
	}


	private void createOptimalPalette(BufferedImage image, int colorCount) {
		int toAllocate = colorCount - firstColor;
		
		ColorOctree octree = new ColorOctree(4, toAllocate, true);
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
		
		for (ColorOctree.LeafNode node : leaves) {
			int[] repr = node.reprRGB();
			
			if (DEBUG) System.out.println("palette[" + index +"] = " 
					+ Integer.toHexString(repr[0]) + "/" 
					+ Integer.toHexString(repr[1]) + "/" 
					+ Integer.toHexString(repr[2]));
			
			/*
			colorMgr.setGRB333(index, Math.min(255, (repr[1] * 0xff / 0xe0)) >> 5, 
				Math.min(255, (repr[0] * 0xff / 0xe0)) >> 5, 
				Math.min(255, (repr[2] * 0xff / 0xe0)) >> 5);
			*/
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
	
		if (format == Format.COLOR16_4x4) {
			xoffs = (64 - img.getWidth()) / 2;
			yoffs = (48 - img.getHeight()) / 2;
		} else {
			xoffs = (canvas.getVisibleWidth() - img.getWidth() + canvas.getXOffset()) / 2;
			yoffs = (canvas.getVisibleHeight() - img.getHeight() + canvas.getYOffset()) / 2;
		}
	
		if (format == Format.COLOR16_8x1 && ditherType != Dither.MONO) {
			
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
	 */
	private void equalize(BufferedImage img) {
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
	protected void analyzeBitmap(BufferedImage img, boolean includeSides, IBitmapColorUser colorUser) {
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
					scmn = Math.max(0, x - 4);
					scmx = Math.min(width, maxx + 4);
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
	
			if (sorted.size() >= 2) {
				if (sorted.get(0).second + sorted.get(1).second < 4) {
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
					
					avg[0] /= 8;
					avg[1] /= 8;
					avg[2] /= 8;
					
					int[] lowa = { (avg[0]+min[0]*3) / 4, (avg[1]+min[1]*3)/4, (avg[2]+min[2]*3)/4 };
					int[] higha = { (avg[0]+max[0]*3) / 4, (avg[1]+max[1]*3)/4, (avg[2]+max[2]*3)/4 };
					
					int low = ColorMapUtils.rgb8ToPixel(lowa);
					int high = ColorMapUtils.rgb8ToPixel(higha);
					
					int fpixel = mapColor.getClosestPalettePixel(x, y, low);
					int bpixel = mapColor.getClosestPalettePixel(x, y, high);
					
					int dist = ColorMapUtils.getPixelDistance(fpixel, bpixel);
					if (dist > 0x5f*0x5f*3) {
	
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
		
		if (!useColorMappedGreyScale) {
			analyzeBitmap(img, false, new PrepareClosestColors(img, mapColor));
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
	protected void reduceBitmapMode(final BufferedImage img, int xoffs_, final int yoffs) {

		// be sure we select the 8 pixel groups sensibly
		final int xoffs;
		if ((xoffs_ & 7) > 3)
			xoffs = (xoffs_ + 7) & ~7;
		else
			xoffs = xoffs_ & ~7;
		
		analyzeBitmap(img, true, new IBitmapColorUser() {
			
			@Override
			public void useColor(int x, int maxx, int y, List<Pair<Integer,Integer>> sorted) {

				int fpixel, bpixel;
				if (sorted.size() >= 2) {
					fpixel = sorted.get(0).first;
					bpixel = sorted.get(1).first;
					int fbDist = useColorMappedGreyScale ? ColorMapUtils.getPixelLumDistance(fpixel, bpixel) 
							: ColorMapUtils.getPixelDistance(fpixel, bpixel);
					if (fbDist < (useColorMappedGreyScale ? 0x1f*0x1f : 0x1f*0x1f * 3)) {
						int left = sorted.get(0).second - sorted.get(1).second;
						for (int sidx = 2; left > 0 && sidx < sorted.size(); sidx++) { 
							int dist = useColorMappedGreyScale ? ColorMapUtils.getPixelLumDistance(fpixel, sorted.get(sidx).first) 
									: ColorMapUtils.getPixelDistance(fpixel, sorted.get(sidx).first);
							if (dist > fbDist) {
								bpixel = sorted.get(sidx).first;
								fbDist = dist;
							}
							left -= sorted.get(sidx).second;
						}
					}
				} else {
					fpixel = bpixel = sorted.get(0).first;
				}
				
				int newPixel = 0;
				for (int xd = x; xd < maxx; xd++) {
					if (xd < img.getWidth()) {
						newPixel = img.getRGB(xd, y);
						if (useColorMappedGreyScale)
							newPixel = ColorMapUtils.getPixelForGreyscaleMode(newPixel);
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
		});
	}


	protected void updatePaletteMapping() {
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
		
		if (ncols < 256) {
			// TODO: why is greyscale acting funny?
			if (useColorMappedGreyScale) {
				// ensure palette is valid: higher bit depth may have
				// been guessed during palette optimization
				for (int c = 0; c < ncols; c++) {
					colorMgr.setRGB333(c, thePalette[c]);
				}
			} else {
				// ensure palette is valid: higher bit depth may have
				// been guessed during palette optimization
				for (int c = 0; c < ncols; c++) {
					colorMgr.setRGB(c, thePalette[c]);
				}
				
			}
			
			for (int c = 0; c < ncols; c++) {
				byte[] nrgb = thePalette[c];
				if (useColorMappedGreyScale) 
					nrgb = ColorMapUtils.getRgbToGreyForGreyscaleMode(nrgb);
				int p = ColorMapUtils.rgb8ToPixel(nrgb);
				paletteToIndex.put(p, c);
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

	public byte getPixel(int x, int y) {
		if (paletteMappingDirty) {
			updatePaletteMapping();
			if (paletteMappingDirty)
				return 0;
		}
		int p = canvasImageData.getPixel(x, y) & 0xffffff;
		
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


	/**
	 * @param image
	 * @param isLowColor if the image is known to have a small number of colors -- don't scale
	 */
	public void importImage(ImportOptions options) {
		BufferedImage image = options.getImage();
		if (image == null)
			return;

		int targWidth = canvas.getVisibleWidth();
		int targHeight = canvas.getVisibleHeight();

		if (options.isKeepAspect()) {
			float aspect = (float) targWidth / targHeight / 256.f * 192.f;
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
			
			if (realWidth != targWidth || realHeight != targHeight) {
				if (realWidth * targHeight * aspect > realHeight * targWidth) {
					targHeight = (int) (targWidth * realHeight / realWidth / aspect);
				} else {
					targWidth = (int) (targHeight * realWidth * aspect / realHeight);
					
					// make sure, for bitmap mode, that the size is a multiple of 8,
					// otherwise the import into video memory will destroy the picture
					if (format == Format.COLOR16_8x1) {
						targWidth &= ~7;
						targHeight = (int) (targWidth * realHeight / realWidth / aspect);
					}
				}
			}
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

		firstColor = (canSetPalette && colorMgr.isClearFromPalette() ? 0 : 1);

		byte[][] orig = options.getOrigPalette();
		if (orig != null) {
			for (int i = 0; i < thePalette.length; i++) {
				System.arraycopy(orig[i], 0, thePalette[i], 0, 3);
			}
		}
		updatePaletteMapping();
		
		setImageData(scaled);

		synchronized (vdp) {
			vdp.getVdpModeRedrawHandler().importImageData(this);
			vdp.getCanvas().markDirty();
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
	public Format getFormat() {
		return format;
	}
	
}