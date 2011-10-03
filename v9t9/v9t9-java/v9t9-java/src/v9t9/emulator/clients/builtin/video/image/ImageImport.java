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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.eclipse.swt.graphics.ImageData;
import org.ejs.coffee.core.utils.Pair;

import v9t9.emulator.clients.builtin.video.IBitmapPixelAccess;
import v9t9.emulator.clients.builtin.video.ImageDataCanvas;
import v9t9.emulator.clients.builtin.video.VdpCanvas;
import v9t9.emulator.clients.builtin.video.VdpCanvas.Format;
import v9t9.emulator.clients.builtin.video.image.ColorOctree.LeafNode;
import v9t9.emulator.clients.builtin.video.tms9918a.BitmapModeRedrawHandler;
import v9t9.emulator.clients.builtin.video.v9938.VdpV9938;
import v9t9.engine.VdpHandler;

/**
 * This class handles converting arbitrary external images and 
 * converting them to fit in the current TMS9918A or V9938 mode.
 * @author ejs
 *
 */
public class ImageImport implements IBitmapPixelAccess {
	private boolean DEBUG = true;

	private ImageData imageData;
	private Format format;
	private byte[][] thePalette;
	
	
	/** the RGB-32 pixel for each palette entry */
	protected int[] palettePixels;
	/** mapping from RGB-32 pixel to each palette index */
	protected TreeMap<Integer, Integer> paletteToIndex;
	
	private final ImageDataCanvas canvas;
	private boolean paletteMappingDirty;
	private final VdpHandler vdp;
	private int firstColor;
	private int[] rgbs;
	private boolean canSetPalette;
	private boolean isMono;
	private boolean isStandardPalette;

	private Pair<Integer, Integer>[][] bitmapColors;
	
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
		this.vdp = vdp;
		this.imageData = canvas.getImageData();
		this.format = canvas.getFormat();
		this.thePalette = canvas.getPalette();
		
		isMono = (vdp.getVdpModeRedrawHandler() instanceof BitmapModeRedrawHandler &&
				((BitmapModeRedrawHandler) vdp.getVdpModeRedrawHandler()).isMono());
		
		if (canvas.isGreyscale()) {
			// convert palette
			byte[][] greyPalette = new byte[thePalette.length][];
			for (int c = 0; c < greyPalette.length; c++) {
				greyPalette[c] = VdpCanvas.rgbToGrey(thePalette[c]);
			}
			thePalette = greyPalette;
		}
		this.rgbs = new int[imageData.width];
		if (vdp instanceof VdpV9938) {
			// hack: graphics mode 2 allows setting the palette too, 
			// but for comparison shopping, pretend we can't.
			if (format == Format.COLOR16_8x1 && (vdp.readVdpReg(0) & 0x6) == 0x2) {
				canSetPalette = false;
				
				// measure against this palette, since
				// it has more regular (non-pastel) colors
				//thePalette = stock16ColorPalette;
			} else {
				canSetPalette = format != Format.COLOR256_1x1;
			}
		} else {
			canSetPalette = false;
		}
		firstColor = (canSetPalette && canvas.isClearFromPalette() ? 0 : 1);
		
		for (byte[][] palette : VdpCanvas.allPalettes()) {
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

	private void getRGB(BufferedImage img, int x, int y, int[] rgb) {
		int pixel = img.getRGB(x, y);
		ColorMapUtils.pixelToRGB(pixel, rgb);
	}
	private void ditherRGB(BufferedImage img, int x, int y, int[] rgb, int sixteenths, int r_error, int g_error, int b_error) {
		getRGB(img, x, y, rgb);
		rgb[0] += sixteenths * r_error / 16;
		rgb[1] += sixteenths * g_error / 16;
		rgb[2] += sixteenths * b_error / 16;
		int pixel = ColorMapUtils.rgb8ToPixel(rgb);
		img.setRGB(x, y, pixel | 0xff000000);

	}
	private void ditherize(BufferedImage img, IMapColorBase mapColor,
			int x, int y, int[] prgb, boolean limit8) {
		
		int pixel = img.getRGB(x, y);
		
		ColorMapUtils.pixelToRGB(pixel, prgb);

		int newPixel = mapColor.getClosestPalettePixel(x, y, prgb);
		
		img.setRGB(x, y, newPixel | 0xff000000);
		
		int r_error = prgb[0] - ((newPixel >> 16) & 0xff);
		int g_error = prgb[1] - ((newPixel >> 8) & 0xff);
		int b_error = prgb[2] - ((newPixel >> 0) & 0xff);
		
		if (limit8) {
			r_error /= 3;
			g_error /= 3;
			b_error /= 3;
		}
		else {
			// blue has limited resolution; don't spread error too far
			if (format == Format.COLOR256_1x1)
				b_error /= 2;
		}
		
		if (x + 1 < img.getWidth()) {
			// x+1, y
			ditherRGB(img, x + 1, y, prgb, 
					7, r_error, g_error, b_error);
		}
		if (y + 1 < img.getHeight()) {
			if (x > 0)
				ditherRGB(img, x - 1, y + 1, prgb, 
						3, r_error, g_error, b_error);
			ditherRGB(img, x, y + 1, prgb, 
					5, r_error, g_error, b_error);
			if (x + 1 < img.getWidth())
				ditherRGB(img, x + 1, y + 1, prgb, 
						1, r_error, g_error, b_error);
		}
	}
	

	class BitmapDitherColorMapper implements IMapColorBase {
		
		private final IMapColor paletteMapper;
		private final int rgb[] = { 0, 0, 0 };
		
		public BitmapDitherColorMapper(IMapColor paletteMapper) {
			this.paletteMapper = paletteMapper;
		}
		@Override
		public int getClosestPalettePixel(int x, int y, int[] prgb) {
			if (bitmapColors != null && bitmapColors[y] != null) {
				Pair<Integer, Integer> fgbg = bitmapColors[y][x / 8];
				
				if (fgbg != null) {
					int fgdist = ColorMapUtils.getRGBDistance(fgbg.first, prgb);
					int bgdist = ColorMapUtils.getRGBDistance(fgbg.second, prgb);
					int cand = fgdist <= bgdist ? fgbg.first : fgbg.second;
					System.out.println("y="+y+"; x="+x+" = " + cand);
					ColorMapUtils.pixelToRGB(cand, rgb);
					return paletteMapper.getClosestPalettePixel(x, y, rgb);
				}
			}
			return paletteMapper.getClosestPalettePixel(x, y, prgb);
		}
	}

	private void ditherForBitmapMode(BufferedImage img, IMapColor mapColor) {
		prepareBitmapModeColors(img, mapColor);
		
		BitmapDitherColorMapper mapper = new BitmapDitherColorMapper(mapColor);
		int[] prgb = { 0, 0, 0 };
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x += 8) {
				for (int xo = 0; xo < 8; xo++) {
					ditherize(img, mapper, x + xo, y, prgb, !canSetPalette);
				}
			}
		}
		
	}
	
	private void dither(BufferedImage img, IMapColorBase mapColor, boolean limitDither) {
		int[] prgb = { 0, 0, 0 };
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				ditherize(img, mapColor, x, y, prgb, limitDither);
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
		
		palettePixels = new int[ncols];
		
		if (ncols < 256) {
			for (int c = 0; c < ncols; c++) {
				palettePixels[c] = ColorMapUtils.rgb8ToPixel(thePalette[c]);
				//if (!paletteToIndex.containsKey(palettePixels[c]))
					paletteToIndex.put(palettePixels[c], c);
			}
		} else {
			byte[] rgb = { 0, 0, 0};
			for (int c = 0; c < ncols; c++) {
				canvas.getGRB332(rgb, (byte) c);
				palettePixels[c] = ColorMapUtils.rgb8ToPixel(rgb);
				paletteToIndex.put(palettePixels[c], c);
			}
		}
		
		paletteMappingDirty = false;
	}

	/** Import image from 'img' and set the color indices in 'colorMap' which is #getVisibleWidth() by #getVisibleHeight() */
	protected void setImageData(BufferedImage img) {
		if (format == null || format == Format.TEXT || format == Format.COLOR16_8x8)
			return;
		
		equalize(img);
		
		//denoise(img);
		
		updatePaletteMapping();
	
		flatten(img);
		
		if (!importDirectMappedImage(img)) {
			convertImageToColorMap(img);
		}
	


		try {
			ImageIO.write(img, "png", new File("/tmp/dragged.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		replaceImageData(img);
	}

	/**
	 * Remove color we cannot represent anyway
	 * @param img
	 */
	/*
	private void denoise(BufferedImage img) {
		int[] rgbs = new int[img.getWidth()];
		for (int y = 0; y < img.getHeight(); y++) {
			img.getRGB(0, y, rgbs.length, 1, rgbs, 0, rgbs.length);
			for (int x = 0; x < rgbs.length; x++) {
				int pixel = rgbs[x];
				pixel &= 0xffe0e0e0;
				rgbs[x] = pixel;
			}
			img.setRGB(0, y, rgbs.length, 1, rgbs, 0, rgbs.length);
		}
			
	}
	*/
	
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
				else if (alpha != 0xff) {
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
		if (format == Format.COLOR256_1x1 || isMono)
			return false;
		
		int numColors = format == Format.COLOR4_1x1 ? 4
				: 16;
		
		// effective minimum distance for any mode
		int maxDist = 0xf*0xf * 3;
		int numPixels = img.getWidth() * img.getHeight();
		
		boolean matched = false;
		Histogram hist = new Histogram(img);
		
		List<byte[][]> palettes = new ArrayList<byte[][]>();
		palettes.add(thePalette);
		if (isStandardPalette)
			palettes.addAll(Arrays.asList(VdpCanvas.palettes()));
		
		for (byte[][] palette : palettes) {
			int matchedC = hist.generate(new FixedPaletteMapColor(palette, firstColor, numColors), maxDist); 
			if (matchedC == numPixels) {
				matched = true;
				break;
			}
		}
		
		if (matched) {
			for (int c = 0; c < numColors; c++) {
				replaceColor(img, hist, c, ColorMapUtils.rgb8ToPixel(thePalette[c]), Integer.MAX_VALUE);
			}
			
			return true;
		}
		
		return false;
	}

	private void convertImageToColorMap(BufferedImage img) {
		boolean limitDither = false;
		IMapColor mapColor;
		
		if (format == Format.COLOR16_8x1 || format == Format.COLOR16_4x4) {
			if (canSetPalette) {
				createOptimalPalette(img, 16);
				limitDither = true;
				//mapColor = new RGB333MapColor(thePalette, firstColor, 16, canvas.isGreyscale());
				mapColor = new UserPaletteMapColor(thePalette, firstColor, 16, canvas.isGreyscale(), false);
			} else {
				if (isMono) {
					int reg = vdp.readVdpReg(7);
					mapColor = new MonoMapColor((reg >> 4) & 0xf, reg & 0xf);
					firstColor = 0;
					limitDither = false;  // go nuts!
				} else {
					limitDither = true;
					if (isStandardPalette)
						mapColor = new TI16MapColor(thePalette);
					else
						mapColor = new UserPaletteMapColor(thePalette, firstColor, 16, canvas.isGreyscale(), false);
				}
			}
		}
		else if (format == Format.COLOR16_1x1) {
			createOptimalPalette(img, 16);
			//mapColor = new RGB333MapColor(thePalette, firstColor, 16, canvas.isGreyscale());
			mapColor = new UserPaletteMapColor(thePalette, firstColor, 16, canvas.isGreyscale(), false);
		}
		else if (format == Format.COLOR4_1x1) {
			createOptimalPalette(img, 4);
			//mapColor = new RGB333MapColor(thePalette, firstColor, 4, canvas.isGreyscale());
			mapColor = new UserPaletteMapColor(thePalette, firstColor, 4, canvas.isGreyscale(), false);
		}
		else if (format == Format.COLOR256_1x1) {
			mapColor = new RGB332MapColor(canvas.isGreyscale());
		}
		else {
			return;
		}
		optimizeForNColors(img, mapColor);
		
		updatePaletteMapping();

		if (format == Format.COLOR16_8x1 && limitDither) {
			ditherForBitmapMode(img, mapColor);
		} else {
			dither(img, mapColor, limitDither);
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
			
			canvas.setGRB333(index, Math.min(255, (repr[1] * 0xff / 0xdf)) >> 5, 
				Math.min(255, (repr[0] * 0xff / 0xdf)) >> 5, 
				Math.min(255, (repr[2] * 0xff / 0xdf)) >> 5);
			
			index++;
			
		}
	}


	private void createOptimalPalette(BufferedImage image, int colorCount) {
		int toAllocate = colorCount - firstColor;
			
		ColorOctree octree = new ColorOctree(3, toAllocate, true);
		int[] prgb = { 0, 0, 0 };
		int[] rgbs = new int[image.getWidth()];
		for (int y = 0; y < image.getHeight(); y++) {
			image.getRGB(0, y, rgbs.length, 1, rgbs, 0, rgbs.length);
			for (int x = 0; x < rgbs.length; x++) {
				ColorMapUtils.pixelToRGB(rgbs[x], prgb);
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
			
			if (DEBUG) System.out.println("palette[" + index +"] = " 
					+ Integer.toHexString(repr[0]) + "/" 
					+ Integer.toHexString(repr[1]) + "/" 
					+ Integer.toHexString(repr[2]));
			
			canvas.setGRB333(index, Math.min(255, (repr[1] * 0xff / 0xdf)) >> 5, 
				Math.min(255, (repr[0] * 0xff / 0xdf)) >> 5, 
				Math.min(255, (repr[2] * 0xff / 0xdf)) >> 5);
			
			index++;
			
		}
	}

	private void replaceImageData(BufferedImage img) {
		int xoffs, yoffs;
		
		Arrays.fill(imageData.data, (byte) 0);
	
		if (format == Format.COLOR16_4x4) {
			xoffs = (64 - img.getWidth()) / 2;
			yoffs = (48 - img.getHeight()) / 2;
		} else {
			xoffs = (canvas.getVisibleWidth() - img.getWidth() + canvas.getXOffset() + 0*canvas.getExtraSpace()) / 2;
			yoffs = (canvas.getVisibleHeight() - img.getHeight() + canvas.getYOffset()) / 2;
		}
	
		if (format == Format.COLOR16_8x1 && !isMono) {
			
			reduceBitmapMode(img, xoffs, yoffs);
	
		} else {
	
			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					imageData.setPixel(x + xoffs, y + yoffs, img.getRGB(x, y));
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
		
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int pixel = img.getRGB(x, y);
				ColorMapUtils.pixelToRGB(pixel, prgb);
				ColorMapUtils.rgbToHsv(prgb, hsv);
				
				if (satDiff < 0.5)
					hsv[1] = ((hsv[1] - minSat) / satDiff) * satScale + minSat;
				if (valDiff < 128)
					hsv[2] = ((hsv[2] - minVal) / valDiff) * valScale + minVal;

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
	private int optimizeForNColors(BufferedImage img, IMapColor mapColor) {
		
		int numColors = mapColor.getNumColors();
		
		byte[][] palette = mapColor.getPalette();
		
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
		
		ourDist = mapColor.getMaximalReplaceDistance(usedColors);
		if (format == Format.COLOR16_8x1)
			ourDist /= 2;
		
		if (DEBUG) System.out.println("replacing colors below distance " + ourDist);
		for (int i = 0; i < usedColors; i++) {
			// ensure there will be an exact match so no dithering 
			// occurs on the primary occurrences of this color
			int idx = hist.getColorIndex(i);
			byte[] rgb = palette[idx];
			int newRGB = ColorMapUtils.rgb8ToPixel(rgb);
			replaceColor(img, hist, idx, newRGB, ourDist);
		}
		
		return ourDist;
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
	 * So, rather than taking the most prominent colors wholesale,
	 * which may leave valid but rarely occurring colors stranded, we take
	 * a smaller number (depending on color variety) of the most often occurring
	 * colors and replace them directly in the image so they will not
	 * be dithered.  Then we take an exponentially increasing sample
	 * of the other colors appearing in the image for the remainder
	 * of the palette. 
	 * 
	 * @param img
	 * @param mapColor 
	 */
	int optimizeForNColorsAndRebuildPalette(BufferedImage img, IMapColor mapColor) {
		
		int maxDist = 0;
		maxDist = mapColor.getMinimalPaletteDistance();

		Histogram hist = new Histogram(img);
		int interestingColors = 0;
		int mappedColors = 0;
		
		int mostInterestingColors = 0;
		
		//int total = img.getWidth() * img.getHeight();
		
		//for (int mask = 4; mask < 7; mask++) {
			mappedColors = hist.generate(mapColor, maxDist);
			interestingColors = hist.size();
			if (interestingColors > mostInterestingColors)
				mostInterestingColors = interestingColors;
			if (DEBUG) System.out.println("For mask " + Integer.toHexString(0) 
					+"; # interesting = " + interestingColors
					+"; # mapped = " + mappedColors);
			//if (interestingColors <= 256)
			//	break;
			//if (mappedColors > total / 2)
			//	break;
		//} 
		
		int usedColors = Math.min(mapColor.getNumColors(), interestingColors);
		
		if (DEBUG) System.out.println("interestingColors="+interestingColors
				+"; usedColors="+usedColors
				+"; mappedColors=" + mappedColors);
		
		int replaceDist = mapColor.getMaximalReplaceDistance(mostInterestingColors);

		int[] paletteSelections = new int[usedColors];
		
		if (mapColor.getNumColors() >= 16) {
			selectExponential(interestingColors, usedColors, paletteSelections);
		} else {
			selectNovelColors(mapColor, hist, interestingColors, usedColors, paletteSelections);
		}
		
		for (int c = 0; c < usedColors; c++) {
			int idx = hist.getColorIndex(paletteSelections[c]);
			int r = (idx>>6) & 0x7;
			int g = (idx>>3) & 0x7;
			int b = (idx>>0) & 0x7;
			canvas.setGRB333(c, g, r, b);
			
			replaceColor(img, hist, idx, ColorMapUtils.rgb8ToPixel(thePalette[c]), replaceDist);
		}
		
		
		return maxDist;
	}

	/**
	 * Select the most different colors available.
	 * @param hist
	 * @param interestingColors
	 * @param usedColors
	 * @param paletteSelections
	 */
	private void selectNovelColors(IMapColor mapColor, Histogram hist, int interestingColors,
			int usedColors, int[] paletteSelections) {
		
		Integer[] indices = (Integer[]) hist.getColorIndices().toArray(new Integer[hist.getColorIndices().size()]);
		Set<Integer> visited = new HashSet<Integer>();
		
		byte[] rgb = { 0, 0, 0 };
		float[] hsv = { 0, 0, 0 };
		
		float[] lastHSV = { 0, 0, 0 };
		int[] lastRGB = { 0, 0, 0 };
		int lastPixel = Integer.MIN_VALUE;
		
		int directMap = usedColors / 3;
		int distance = interestingColors / 3;
		
		for (int i = 0; i < usedColors; i++) {
			float maxDiff = -Float.MAX_VALUE;
			int farthestIndex = 0;
			int farthestPixel = 0;
			
			int searchDist = Math.max(1, Math.min(indices.length, (distance * (i + 1) / usedColors)));
			for (int j = 0; j < searchDist && j < indices.length; j++) {
				if (visited.contains(j)) {
					searchDist++;
					continue;
				}
				
				int idx = indices[j];
				int pixel = mapColor.getPalettePixel(idx);
				ColorMapUtils.pixelToRGB(pixel, rgb);
				ColorMapUtils.rgbToHsv(rgb, hsv);
				
				
				float diff = //(hsv[0] - lastHSV[0]) * (hsv[0] - lastHSV[0])
					//+ ((hsv[1] - lastHSV[1]) * (hsv[1] - lastHSV[1])) * 100000
					+ (hsv[2] - lastHSV[2]) * (hsv[2] - lastHSV[2])
				;
				
				//float diff = getRGBDistance(rgb, lastRGB);
				if (diff >= maxDiff) {
					maxDiff = diff;
					farthestPixel = pixel;
					farthestIndex = j;
					if (i < directMap) {
						break;
					} 
				}
			}
			
			// winner!
			paletteSelections[i] = farthestIndex;
			visited.add(farthestIndex);
			
			lastPixel = farthestPixel;
			ColorMapUtils.pixelToRGB(lastPixel, lastRGB);
			ColorMapUtils.rgbToHsv(lastRGB, lastHSV);
		}
	}

	/** select colors, biasing toward the most prominent:
	 
	 index = exp(i * K) - 1
	 
	 interestingColors = exp(usedColors * K) - 1
	 interestingColors + 1 = exp(usedColors * K)
	 ln(interestingColors + 1) = usedColors * K
	 ln(interestingColors + 1) / usedColors = K
	
	*/
	
	void selectExponential(int interestingColors,
			int usedColors, int[] paletteSelections) {
		
		double K = Math.log(interestingColors - usedColors / 2) / usedColors;
		
		int prev = -1;
		
		
		for (int i = 0; i < usedColors; i++) {
			int index;
			if (interestingColors == usedColors || i < usedColors / 2) {
				index = i;
			} else {
				index = (int) Math.expm1(i * K) + usedColors / 2;
				if (index == prev)
					index++;
			}
			//System.out.println("index="+index);
			prev = index;
			
			paletteSelections[i] = index;
		}
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
					int dist = ColorMapUtils.getPixelDistance(rgbs[x], newRGB);
					if (dist < maxDist) {
						rgbs[x] = newRGB;
						replaced++;
						changed = true;
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
					if (xd < width)
						pixel = img.getRGB(xd, y);
					
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
	protected void prepareBitmapModeColors(final BufferedImage img, final IMapColor mapColor) {
		bitmapColors = new Pair[img.getHeight()][];
		
		analyzeBitmap(img, false, new IBitmapColorUser() {

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
						
						int[] low = { (avg[0]+min[0]*3) / 4, (avg[1]+min[1]*3)/4, (avg[2]+min[2]*3)/4 };
						int[] high = { (avg[0]+max[0]*3) / 4, (avg[1]+max[1]*3)/4, (avg[2]+max[2]*3)/4 };
						
						int fpixel = mapColor.getClosestPalettePixel(x, y, low);
						int bpixel = mapColor.getClosestPalettePixel(x, y, high);
						if (ColorMapUtils.getPixelDistance(fpixel, bpixel) > 0x5f*0x5f*3) {

							if (fpixel != bpixel) {
								if (bitmapColors[y] == null)
									bitmapColors[y] = new Pair[(img.getWidth() + 7) / 8];
								
								bitmapColors[y][x / 8] = new Pair<Integer, Integer>(fpixel, bpixel);
							}
						}
					}
				}
				
			}
			
		});
		
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
					int fbDist = ColorMapUtils.getPixelDistance(fpixel, bpixel);
					if (fbDist < 0x1f*0x1f * 3) {
						int left = sorted.get(0).second - sorted.get(1).second;
						for (int sidx = 2; left > 0 && sidx < sorted.size(); sidx++) { 
							int dist = ColorMapUtils.getPixelDistance(fpixel, sorted.get(sidx).first);
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
					if (xd < img.getWidth())
						newPixel = img.getRGB(xd, y);
					
					int fdist = ColorMapUtils.getPixelDistance(newPixel, fpixel);
					int bdist = ColorMapUtils.getPixelDistance(newPixel, bpixel);
					if (fdist < bdist) {
						newPixel = fpixel;
					} else {
						newPixel = bpixel;
					}
						
					imageData.setPixel(xd + xoffs, y + yoffs, newPixel);
				}
			}
		});
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

	/**
	 * @param image
	 * @param isLowColor if the image is known to have a small number of colors -- don't scale
	 */
	public void importImage(BufferedImage image, boolean isLowColor) {
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
		
		if (image.getColorModel().getPixelSize() <= 8) {
			isLowColor = true;
		}
		
		Object hint = !isLowColor ? RenderingHints.VALUE_INTERPOLATION_BILINEAR
					:  RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;

		BufferedImage scaled = ColorMapUtils.getScaledInstance(image, targWidth, targHeight, 
				hint,
				false);
		//System.out.println(scaled.getWidth(null) + " x " +scaled.getHeight(null));
		
		setImageData(scaled);

		synchronized (vdp) {
			vdp.getVdpModeRedrawHandler().importImageData(this);
			vdp.getCanvas().markDirty();
		}

	}
	
}
