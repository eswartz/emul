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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.swt.graphics.ImageData;
import org.ejs.coffee.core.utils.Pair;

import v9t9.emulator.clients.builtin.video.VdpCanvas.Format;
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


	private interface IMapColor {
		/**
		 * Get number of colors (range of indices for mapColor and getClosestPalettePixel)
		 */
		int getNumColors();

		/**
		 * Get the palette against which the mapping occurs.
		 * This palette object must match the one that is changed
		 * if the palette is adjusted during mapping via
		 * {@link ImageImport#optimizeForNColorsAndRebuildPalette(BufferedImage, IMapColor)}.
		 * @return palette of size {@link #getNumColors()}
		 */
		byte[][] getPalette();
		
		/**
		 * Get the minimal distance between colors in the palette.
		 */
		int getMinimalPaletteDistance();
		
		/** Return a color index from mapping the RGB pixel 
		 * 
		 * @param prgb pixel in X8R8G8B8 format
		 * @param dist array for receiving distance² from the returned pixel
		 * @return the color index
		 */
		int mapColor(int[] prgb, int[] dist);

		/**
		 * Get the color in the new palette closest to this one.
		 * @param prgb
		 * @return color index or -1
		 */
		int getClosestPalettePixel(int[] prgb);
		
		/**
		 * Get the maximum pixel distance for absolutely replacing a range
		 * of source pixels with palette pixels. 
		 * @param usedColors the number of unique colors 
		 */
		int getMaximalReplaceDistance(int usedColors);

		int getPalettePixel(int c);
	}
	
	/*
	private static class TI16MapColor_ implements IMapColor {
		private final Palette16 palette;

		@Override
		public int mapColor(int[] prgb, int[] distA) {
			float[] phsv = rgbToHsv(prgb);
			
			int closest = -1;
			int mindiff = Integer.MAX_VALUE;
			
			for (int c = 1; c < 16; c++) {
				int dist = palette.getColorDistance(c, phsv, prgb);
				if (dist < mindiff) {
					closest = c;
					mindiff = dist;
				}
			}
			
			// don't bother with greyscale pixels unless they match exactly;
			// dither otherwise
			if (phsv[1] < 0.25 && mindiff != 0) {
				closest = -1;
				mindiff = Integer.MAX_VALUE;
			}
				
			
			distA[0] = mindiff;
			return closest;
		}
		
		@Override
		public int getClosestPalettePixel(int[] prgb) {
			int c = getClosestColor16(16, prgb, Integer.MAX_VALUE);
			if (c != -1)
				return palettePixels[c];
			else
				return ((prgb[0] << 16) | (prgb[1] << 8) | (prgb[2]));
		}
	}
	*/
	
	private static abstract class BasePaletteMapper implements IMapColor {
		private final boolean canSetPalette;
		private int minDist;
		protected byte[][] palette;
		protected int numColors;
		protected int firstColor;
		protected final boolean isGreyscale;
		
		protected int[] palettePixels;

		public BasePaletteMapper(byte[][] palette, int firstColor, int numColors, boolean canSetPalette, boolean isGreyscale) {
			this.palette = palette;
			this.isGreyscale = isGreyscale;
			
			if (isGreyscale) {
				// convert palette
				byte[][] greyPalette = new byte[palette.length][];
				for (int c = 0; c < greyPalette.length; c++) {
					greyPalette[c] = VdpCanvas.rgbToGrey(palette[c]);
				}
				this.palette = greyPalette;
			}
			
			this.firstColor = firstColor;
			this.numColors = numColors;
			this.canSetPalette = canSetPalette;
			
			minDist = Integer.MAX_VALUE;
			for (int c = 0; c < numColors; c++) {
				for (int d = c + 1; d < numColors; d++) {
					int[] prgb = { palette[d][0] & 0xff,
							palette[d][1] & 0xff, 
							palette[d][2] & 0xff 
					};
					int dist = getRGBDistance(palette, c, prgb);
					if (dist != 0 && dist < minDist)
						minDist = dist;
				}
			}
			
		}
		

		@Override
		public byte[][] getPalette() {
			return palette;
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getNumColors()
		 */
		@Override
		public int getNumColors() {
			return numColors;
		}
		
		@Override
		public int getMinimalPaletteDistance() {
			if (canSetPalette)
				// 0xff --> 0xe0 for R, G, B
				return 0x1f*0x1f * 3;

			return minDist;
		}
		@Override
		public int getMaximalReplaceDistance(int usedColors) {
			if (canSetPalette) {
				boolean highColors = usedColors > numColors * 4;
				return highColors ? 0x3*0x3*3 : minDist;
			} else {
				return minDist;
			}
		}
		
		/**
		 * Get RGB pixel for each palette entry.
		 * The pixels are calculated lazily in case the
		 * palette changes (this is called only after the
		 * mapping is complete).
		 * @return
		 */
		protected int[] getPalettePixels() {
			if (palettePixels == null) {

				palettePixels = new int[numColors];
				
				for (int x = 0; x < numColors; x++) {
					palettePixels[x] = rgb8ToPixel(palette[x]);
				}
			}
			return palettePixels;
		}
		
		@Override
		public int getPalettePixel(int c) {
			return rgb8ToPixel(palette[c]);
		}
	}
	
	private static class TI16MapColor extends BasePaletteMapper {
		
		public TI16MapColor(byte[][] thePalette) {
			super(thePalette, 1, 16, false, false);
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
		 */
		@Override
		public int mapColor(int[] prgb, int[] distA) {
			int closest = getCloseColor(prgb);
			distA[0] = getRGBDistance(palette, closest, prgb);
			
			return closest;
		}
		
		/**
		 * Get the closest color by sheer brute force -- we don't
		 * want dark green to emerge as a "close" color for dark or
		 * desaturated colors!
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
		 * 
		 * @param prgb
		 * @return
		 */
		private int getCloseColor(int[] prgb) {
			float[] phsv = { 0, 0, 0 };
			rgbToHsv(prgb[0], prgb[1], prgb[2], phsv);
			
			float hue = phsv[0];
			float val = phsv[2] * 100 / 256;

			int closest = -1;

			final int white = 15;
			final int black = 1;
			final int grey = 14;
			
			if (phsv[1] < (hue >= 30 && hue < 75 ? 0.66f : 0.33f)) {
				if (val >= 70) {
					closest = white;
				} else if (val >= 10) {
					// dithering will take care of the rest
					closest = grey;
				} else {
					closest = black;
				}
			}
			else {
				closest = getClosestColorByDistance(palette, firstColor, 16, prgb, 12);
				
				// see how the color matches
				if (closest == black) {
					if (phsv[1] > 0.9f) {
						if ((hue >= 75 && hue < 140) && (val >= 5 && val <= 33)) {
							closest = 12;
						}
					}
				}
				/*else {
					int rigid = rigidMatch(phsv, hue, val);
					if (phsv[1] < 0.5f && (rigid == 1 || rigid == 14 || rigid == 15)) {
						closest = rigid;
					}
				}*/
			}
			
			//closest = rigidMatch(phsv, hue, val);
			
			return closest;
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestColor(int[])
		 */
		@Override
		public int getClosestPalettePixel(int[] prgb) {
			int c = getCloseColor(prgb);
			return getPalettePixels()[c];
		}
		
		@Override
		public int getMinimalPaletteDistance() {
			return Integer.MAX_VALUE;
		}
	}


	private static class UserPaletteMapColor extends BasePaletteMapper {
		
		public UserPaletteMapColor(byte[][] thePalette, int firstColor, boolean isGreyscale) {
			super(thePalette, firstColor, 16, false, isGreyscale);
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
		 */
		@Override
		public int mapColor(int[] prgb, int[] distA) {
			int closest = getCloseColor(prgb);
			distA[0] = getRGBDistance(palette, closest, prgb);
			
			return closest;
		}
		
		/**
		 * Get the closest color by sheer brute force -- we don't
		 * want dark green to emerge as a "close" color for dark or
		 * desaturated colors!
		
		 * @param prgb
		 * @return
		 */
		private int getCloseColor(int[] prgb) {
			
			return getClosestColorByDistance(palette, firstColor, numColors, prgb, -1);
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestColor(int[])
		 */
		@Override
		public int getClosestPalettePixel(int[] prgb) {
			int c = getCloseColor(prgb);
			return getPalettePixels()[c];
		}
	}


	private static class MonoMapColor extends BasePaletteMapper {
		private final int fg;
		private final int bg;
		
		public MonoMapColor(int fg, int bg) {
			super(createMonoPalette(fg, bg), 0, 16, false, false);
			this.fg = fg;
			this.bg = bg;
		}
		
		private static byte[][] createMonoPalette(int fg, int bg) {
			byte[][] palette = new byte[16][];
			palette[fg] = new byte[] { 0, 0, 0 };
			palette[bg] = new byte[] { -1, -1, -1 };
			for (int c = 0; c < 16; c++) {
				if (c != fg && c != bg) {
					palette[c] = new byte[] { 127, 127, 127 };
				}
			}
			return palette;
		}

		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
		 */
		@Override
		public int mapColor(int[] prgb, int[] distA) {
			int distF = getRGBDistance(palette, fg, prgb);
			int distB = getRGBDistance(palette, bg, prgb);
			if (fg < bg) {
				distA[0] = distF;
				return fg;
			}
			distA[0] = distB;
			return bg;
		}
		
		/**
		 * Get the closest color by sheer brute force -- we don't
		 * want dark green to emerge as a "close" color for dark or
		 * desaturated colors!
		
		 * @param prgb
		 * @return
		 */
		private int getCloseColor(int[] prgb) {
			float[] phsv = { 0, 0, 0 };
			rgbToHsv(prgb[0], prgb[1], prgb[2], phsv);
			return phsv[2] < 64 ? fg : bg;
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestColor(int[])
		 */
		@Override
		public int getClosestPalettePixel(int[] prgb) {
			int c = getCloseColor(prgb);
			return getPalettePixels()[c];
		}
		
		@Override
		public int getMaximalReplaceDistance(int usedColors) {
			return 7*7 * 3;
		}
	}

	private static class RGB333MapColor extends BasePaletteMapper {
		/**
		 * @param isGreyscale 
		 * 
		 */
		public RGB333MapColor(byte[][] thePalette, int firstColor, int numColors, boolean isGreyscale) {
			super(thePalette, firstColor, numColors, true, isGreyscale);
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
		 */
		@Override
		public int mapColor(int[] prgb, int[] dist) {
			int r = prgb[0] >>> 5;
			int g = prgb[1] >>> 5;
			int b = prgb[2] >>> 5;
			
			byte[] rgbs = getRGB333(r, g, b);
			
			dist[0] = getRGBDistance(rgbs, prgb);
			
			// not actual RGB332 index!
			int c = (r << 6) | (g << 3) | b;
			
			return c;
		}

		private byte[] getRGB333(int r, int g, int b) {
			byte[] rgbs;
			if (!isGreyscale)
				rgbs = VdpCanvas.getGRB333(g, r, b);
			else {
				// (299 * rgb[0] + 587 * rgb[1] + 114 * rgb[2]) * 256 / 1000;
				
				int l = (r * 299 + g * 587 + b * 114) / 1000;
				rgbs = VdpCanvas.getGRB333(l, l, l);
			}
				
			return rgbs;
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestPaletteColor(int[])
		 */
		@Override
		public int getClosestPalettePixel(int[] prgb) {
			int closest = -1;
			int mindiff = Integer.MAX_VALUE;
			for (int c = firstColor; c < numColors; c++) {
				int dist = getRGBDistance(palette, c, prgb);
				if (dist < mindiff) {
					closest = c;
					mindiff = dist;
				}
			}
			return getPalettePixels()[closest];
		}
		
		@Override
		public int getPalettePixel(int c) {

			int g = (c >> 6) & 0x7;
			int r = (c >> 3) & 0x7;
			int b = (c >> 0) & 0x7;
			
			byte[] rgbs = getRGB333(r, g, b);
			return rgb8ToPixel(rgbs);
		}
	}


	private static class RGB332MapColor extends RGB333MapColor {

		public RGB332MapColor(boolean isGreyscale) {
			super(createStock332Palette(isGreyscale), 0, 512, false);
		}
		
		private static byte[][] createStock332Palette(boolean isGreyscale) {
			byte[][] pal = new byte[512][];
			for (int i = 0; i < 512; i++) {
				 byte[] rgb = { 0, 0, 0 };
				 VdpCanvas.getGRB332(rgb, (byte) (i >> 1), isGreyscale);
				 pal[i] = rgb;
			}
			return pal;
		}

		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestPaletteColor(int[])
		 */
		@Override
		public int getClosestPalettePixel(int[] prgb) {
			// we don't need to trawl the palette here
			byte[] rgb = VdpCanvas.getGRB333(prgb[1] >> 5, prgb[0] >> 5, (prgb[2] >> 5) & ~1);
			return rgb8ToPixel(rgb);
		}
		
		@Override
		public int getMaximalReplaceDistance(int usedColors) {
			return 0x7*0x7*2 + 0x3*0x3;
		}
		
	}
	

	static class Palette16 {
		private final byte[][] palette;
		private float[][] palhsv;
	
		public Palette16(byte[][] thePalette) {
			this.palette = thePalette;
			this.palhsv = new float[16][];
			for (int c = 1; c < 16; c++) {
				palhsv[c] = rgbToHsv(palette[c]);
			}
		}
		
		public int getClosestColor16(int ncols, int[] prgb, int distLimit) {
			float[] phsv = { 0, 0, 0 };
			int closest = -1;
			int mindiff = Integer.MAX_VALUE;
			for (int c = 1; c < ncols; c++) {
				int dist;
				rgbToHsv(prgb[0], prgb[1], prgb[2], phsv);
				dist = getColorDistance16(c, phsv, prgb);  	
				if (dist < distLimit && dist < mindiff) {
					closest = c;
					mindiff = dist;
				}
			}
			return closest;
		}
	
	
		private int getColorDistance16(int c, float[] phsv, int[] prgb) {
			int dist;
			
			if (phsv[2] < 33 && palhsv[c][2] < 33) {
				return 0;
			}
			else if (phsv[1] < 0.25 && palhsv[c][1] < 0.25) {
				// only select something with low saturation,
				// and match value
				float dh = 16; //(palhsv[c][0] - phsv[0]);	// range: 0-35
				float ds = (palhsv[c][1] - phsv[1]) * 256;
				float dv = (palhsv[c][2] - phsv[2]);
				
				dist = (int) ((dh * dh) + (ds * ds) + (dv * dv));
			} else {
				dist = getRGBDistance(palette, c, prgb);
				
				float dh = Math.abs(palhsv[c][0] - phsv[0]) * 6;
				
				dist = dist * 16 + (int) (dh * dh);
			}
			
			return dist;
		}
		
	}

	private static class FixedPaletteMapColor extends BasePaletteMapper {
		public FixedPaletteMapColor(byte[][] thePalette, int firstColor, int numColors) {
			super(thePalette, firstColor, numColors, false, false);
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
		 */
		@Override
		public int mapColor(int[] prgb, int[] distA) {
			for (int c = firstColor; c < numColors; c++) {
				int dist = getRGBDistance(palette, c, prgb);
				if (dist < 25*3) {
					distA[0] = dist;
					return c;
				}
			}
			distA[0] = Integer.MAX_VALUE;
			return -1;
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestPaletteColor(int[])
		 */
		@Override
		public int getClosestPalettePixel(int[] prgb) {
			int closest = -1;
			for (int c = firstColor; c < numColors; c++) {
				int dist = getRGBDistance(palette, c, prgb);
				if (dist < 25*3) {
					return getPalettePixels()[closest];
				}
			}
			return rgb8ToPixel(prgb);
		}
	}

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
		pixelToRGB(pixel, rgb);
	}
	private void ditherRGB(BufferedImage img, int x, int y, int[] rgb, int sixteenths, int r_error, int g_error, int b_error) {
		getRGB(img, x, y, rgb);
		rgb[0] += sixteenths * r_error / 16;
		rgb[1] += sixteenths * g_error / 16;
		rgb[2] += sixteenths * b_error / 16;
		int pixel = ((Math.max(0, Math.min(rgb[0], 255))) << 16)
			| ((Math.max(0, Math.min(rgb[1], 255))) << 8)
			| ((Math.max(0, Math.min(rgb[2], 255))))
			| 0xff000000;
		img.setRGB(x, y, pixel);

	}
	private void ditherize(BufferedImage img, IMapColor mapColor,
			int x, int y, boolean limit8) {
		
		int pixel = img.getRGB(x, y);
		
		int[] prgb = { 0, 0, 0 };
		pixelToRGB(pixel, prgb);

		int newPixel = mapColor.getClosestPalettePixel(prgb);
		
		img.setRGB(x, y, newPixel);
		
		int r_error = prgb[0] - ((newPixel >> 16) & 0xff);
		int g_error = prgb[1] - ((newPixel >> 8) & 0xff);
		int b_error = prgb[2] - ((newPixel >> 0) & 0xff);
		
		if (limit8) {
	
			r_error /= 3;
			g_error /= 3;
			b_error /= 3;
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
	}
	
	static float[] rgbToHsv(byte[] rgb) {
		float[] hsv = { 0, 0, 0 };
		rgbToHsv(rgb[0]&0xff, rgb[1]&0xff, rgb[2]&0xff, hsv);
		return hsv;
	}
	static void rgbToHsv(int r, int g, int b, float[] hsv) {
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
	static void hsvToRgb(float h, float s, float v, int[] rgb) {
		float c = (v / 256) * s;
		float hprime = (h / 60);
		float x = c * (1 - Math.abs(hprime % 2 - 1));
		
		float fr, fg, fb;
		switch ((int) hprime) {
		case 0:
			fr = c; fg = x; fb = 0; break;
		case 1:
			fr = x; fg = c; fb = 0; break;
		case 2:
			fr = 0; fg = c; fb = x; break;
		case 3:
			fr = 0; fg = x; fb = c; break;
		case 4:
			fr = x; fg = 0; fb = c; break;
		case 5:
			fr = c; fg = 0; fb = x; break;
		default:
			fr = fg = fb = 0;
		}
		
		float m = (v / 256) - c;
		rgb[0] = (int) ((fr + m) * 255);
		rgb[1] = (int) ((fg + m) * 255);
		rgb[2] = (int) ((fb + m) * 255);
	}
	private static int getRGBDistance(byte[] rgb, int[] prgb) {
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
	private static int getPixelDistance(int pixel, int newRGB) {
		int dr = ((pixel & 0xff0000) - (newRGB & 0xff0000)) >> 16; 
		int dg = ((pixel & 0x00ff00) - (newRGB & 0x00ff00)) >> 8; 
		int db = ((pixel & 0x0000ff) - (newRGB & 0x0000ff)) >> 0; 
		return dr*dr + dg*dg + db*db;
	}
	
	private static int getRGBDistance(byte[][] palette, int c, int[] prgb) {
		return getRGBDistance(palette[c], prgb);
	}

	private static int getClosestColorByDistance(byte[][] thePalette, int first, int count, int[] prgb, int exceptFor) {
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

	protected static int rgb8ToPixel(byte[] nrgb) {
		return ((nrgb[0] & 0xff) << 16) | ((nrgb[1] & 0xff) << 8) | (nrgb[2] & 0xff);
	}

	protected static int rgb8ToPixel(int[] prgb) {
		return ((prgb[0]) << 16) | ((prgb[1]) << 8) | (prgb[2]);
	}

	protected static void pixelToRGB(int pixel, int[] prgb) {
		prgb[0] = (pixel & 0xff0000) >> 16;
		prgb[1] = (pixel & 0xff00) >> 8;
		prgb[2] = pixel & 0xff;
	}

	protected static void pixelToRGB(int pixel, byte[] rgb) {
		rgb[0] = (byte) ((pixel & 0xff0000) >> 16);
		rgb[1] = (byte) ((pixel & 0xff00) >> 8);
		rgb[2] = (byte) (pixel & 0xff);
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
				palettePixels[c] = rgb8ToPixel(thePalette[c]);
				paletteToIndex.put(palettePixels[c], c);
			}
		} else {
			byte[] rgb = { 0, 0, 0};
			for (int c = 0; c < ncols; c++) {
				canvas.getGRB332(rgb, (byte) c);
				palettePixels[c] = rgb8ToPixel(rgb);
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

	static class Histogram {
		private final BufferedImage img;
		final Map<Integer, Integer> hist;
		final Map<Integer, Integer> pixelToColor;
		final List<Integer> indices;
		final int[] mappedColors;
		
		public Histogram(BufferedImage img) {
			this.img = img;
			hist = new TreeMap<Integer, Integer>();
			indices = new ArrayList<Integer>();
			mappedColors = new int[img.getWidth() * img.getHeight()];  
			pixelToColor = new TreeMap<Integer, Integer>();
		}
		
		/**
		 * Build a histogram of the colors in the image once the 
		 * colors are reduced to the given palette with the given
		 * error.
		 * @param paletteMapper the color mapper
		 * @param mask TODO
		 * @return the number of colors that map directly (within maxDist)
		 */
		public int generate(IMapColor paletteMapper, int maxDist, int mask) {
			int mapped = gather(paletteMapper, maxDist, mask);
			
			sort();
			
			return mapped;
		}

		private int gather(IMapColor paletteMapper, int maxDist, int mask) {
			hist.clear();
			indices.clear();
			pixelToColor.clear();
			Arrays.fill(mappedColors, 0);
			
			int rgbByte = (0xff00 & ~(0xff << mask)) >> 8;
			int rgbMask = (rgbByte << 16) | (rgbByte << 8) | rgbByte;
			System.out.println("rgbMask = " + Integer.toHexString(rgbMask));
			
			int[] distA = { 0 };
			int[] prgb = { 0, 0, 0 };
			int offs = 0;
			int mapped = 0;
			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					int pixel = img.getRGB(x, y) & rgbMask;
					int c;
					Integer color = pixelToColor.get(pixel);
					if (color == null) {
						pixelToRGB(pixel, prgb);
						
						c = paletteMapper.mapColor(prgb, distA);
						if (distA[0] <= maxDist * (1 << mask)) {
							pixelToColor.put(pixel, c);
							
							Integer count = hist.get(c);
							if (count == null)
								count = 1;
							else
								count++;
							hist.put(c, count);
						} else {
							c = -1;
						}
					} else {
						c = color;
						hist.put(color, hist.get(color) + 1);
					}
					mappedColors[offs++] = c;
					if (c != -1) {
						mapped++;
					}
				}
			}
			return mapped;
		}

		private void sort() {
			List<Pair<Integer, Integer>> sorted = new ArrayList<Pair<Integer,Integer>>();
			for (Map.Entry<Integer, Integer> entry : hist.entrySet()) {
				sorted.add(new Pair<Integer, Integer>(entry.getKey(), entry.getValue()));
			}
			Collections.sort(sorted, new Comparator<Pair<Integer, Integer>>() {

				@Override
				public int compare(Pair<Integer, Integer> o1,
						Pair<Integer, Integer> o2) {
					return o2.second - o1.second;
				}
				
			});
			
			indices.clear();
			for (Pair<Integer, Integer> pair : sorted) {
				indices.add(pair.first);
			}
		}

		/**
		 * Get histogram size, or number of colors mapped.
		 * @return
		 */
		public int size() {
			return hist.size();
		}

		/**
		 * Get the sorted palette indices in prominence order.
		 * @return list of indices into palette from {@link IMapColor#mapColor(int[], int[])}
		 */
		public List<Integer> getColorIndices() {
			return indices;
		}
		/**
		 * Get the palette index of the i'th most prominent color.
		 * @param i
		 * @return index into palette from {@link IMapColor#mapColor(int[], int[])}
		 */
		public int getColorIndex(int i) {
			return indices.get(i);
		}

		/**
		 * Get the number of appearances of the i'th most prominent color.
		 * @param idx
		 * @return count of pixels
		 */
		public int getColorCount(int idx) {
			return hist.get(idx);
		}
		
		/**
		 * @return the mapping of each RGB pixel to its count
		 */
		public Map<Integer, Integer> colorToCountMap() {
			return hist;
		}
		
		/**
		 * @return the mapping of RGB pixel to palette color
		 */
		public Map<Integer, Integer> pixelToColor() {
			return pixelToColor;
		}

		/**
		 * @return
		 */
		public int[] mappedColors() {
			return mappedColors;
		}
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
					pixelToRGB(pixel, prgb);
					prgb[0] = (alpha * prgb[0] + (255 - alpha) * 255) / 256; 
					prgb[1] = (alpha * prgb[1] + (255 - alpha) * 255) / 256; 
					prgb[2] = (alpha * prgb[2] + (255 - alpha) * 255) / 256; 
					pixel = rgb8ToPixel(prgb);
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
			int matchedC = hist.generate(new FixedPaletteMapColor(palette, firstColor, numColors), maxDist, ~0); 
			if (matchedC == numPixels) {
				matched = true;
				break;
			}
		}
		
		if (matched) {
			for (int c = 0; c < numColors; c++) {
				replaceColor(img, hist, c, rgb8ToPixel(thePalette[c]), Integer.MAX_VALUE);
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
				mapColor = new RGB333MapColor(thePalette, firstColor, 16, canvas.isGreyscale());
				limitDither = false;
			} else {
				if (isMono) {
					int reg = vdp.readVdpReg(7);
					mapColor = new MonoMapColor((reg >> 4) & 0xf, reg & 0xf);
					firstColor = 0;
				} else {
					if (isStandardPalette)
						mapColor = new TI16MapColor(thePalette);
					else
						mapColor = new UserPaletteMapColor(thePalette, firstColor, canvas.isGreyscale());
					limitDither = true;
				}
			}
				
			optimizeForNColors(img, mapColor);
			
			//limitDither = true ; // !canSetPalette;
		}
		else if (format == Format.COLOR16_1x1) {
			mapColor = new RGB333MapColor(thePalette, firstColor, 16, canvas.isGreyscale());
	
			optimizeForNColors(img, mapColor);
		}
		else if (format == Format.COLOR4_1x1) {
			mapColor = new RGB333MapColor(thePalette, firstColor, 4, canvas.isGreyscale());
			
			optimizeForNColors(img, mapColor);
		}
		else if (format == Format.COLOR256_1x1) {
			mapColor = new RGB332MapColor(canvas.isGreyscale());
			//optimizeFor256Colors(img, mapColor);
			optimizeForNColors(img, mapColor);
		}
		else {
			return;
		}
		
		updatePaletteMapping();
	
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				ditherize(img, mapColor, x, y, limitDither);
			}
		}
	}

	private void replaceImageData(BufferedImage img) {
		int xoffs, yoffs;
		
		Arrays.fill(imageData.data, (byte) 0);
	
		if (format == Format.COLOR16_4x4) {
			xoffs = (64 - img.getWidth()) / 2;
			yoffs = (48 - img.getHeight()) / 2;
		} else {
			xoffs = (canvas.getVisibleWidth() - img.getWidth() + canvas.getXOffset() + canvas.getExtraSpace()) / 2;
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
				pixelToRGB(pixel, prgb);
				rgbToHsv(prgb[0], prgb[1], prgb[2], hsv);
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
		
		System.out.println("Equalize: sat = "+minSat+" to " +maxSat+"; val = " + minVal + " to " + maxVal+"; value midpoint = " + valueMidpoint);
		float satScale = 1.0f - minSat;
		float valScale = 255 - minVal;
		float satDiff = maxSat - minSat;
		float valDiff = maxVal - minVal;
		
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int pixel = img.getRGB(x, y);
				pixelToRGB(pixel, prgb);
				rgbToHsv(prgb[0], prgb[1], prgb[2], hsv);
				
				if (satDiff < 0.5)
					hsv[1] = ((hsv[1] - minSat) / satDiff) * satScale + minSat;
				if (valDiff < 128)
					hsv[2] = ((hsv[2] - minVal) / valDiff) * valScale + minVal;

				hsvToRgb(hsv[0], hsv[1], hsv[2], prgb);
				
				img.setRGB(x, y, rgb8ToPixel(prgb) | (pixel & 0xff000000));
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
		
		if (canSetPalette) {
			return optimizeForNColorsAndRebuildPalette(img, mapColor);
		}
			
		int numColors = mapColor.getNumColors();
		
		byte[][] palette = mapColor.getPalette();
		
		int ourDist = mapColor.getMinimalPaletteDistance();
		
		System.out.println("Minimum color palette distance: " + ourDist);

		Histogram hist = new Histogram(img);
		int mappedColors = 0;
		int interestingColors = 0;
		int total = img.getWidth() * img.getHeight();
		
		for (int mask = 0; mask < 5; mask++) {
			mappedColors = hist.generate(mapColor, ourDist, mask);
			interestingColors = hist.size();
			System.out.println("For mask " + Integer.toHexString(mask) 
					+"; # interesting = " + interestingColors
					+"; # mapped = " + mappedColors);

			if (mappedColors >= total / 2)
				break;
		}

		
		int usedColors = Math.min(numColors * 3 / 4, 
				Math.min(numColors, interestingColors));
		
		if (!(mapColor instanceof MonoMapColor)) {
			if (interestingColors == 2)
				usedColors = 1;
		}
		
		System.out.println("\nN-color: interestingColors="+interestingColors
				+"; usedColors="+usedColors
				+"; mapped="+mappedColors
				);
		
		ourDist = mapColor.getMaximalReplaceDistance(usedColors);
		for (int i = 0; i < usedColors; i++) {
			// ensure there will be an exact match so no dithering 
			// occurs on the primary occurrences of this color
			int idx = hist.getColorIndex(i);
			byte[] rgb = palette[idx];
			int newRGB = rgb8ToPixel(rgb);
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
	private int optimizeForNColorsAndRebuildPalette(BufferedImage img, IMapColor mapColor) {
		
		int maxDist = 0;
		maxDist = mapColor.getMinimalPaletteDistance();

		Histogram hist = new Histogram(img);
		int interestingColors = 0;
		int mappedColors = 0;
		
		int mostInterestingColors = 0;
		
		//int total = img.getWidth() * img.getHeight();
		
		//for (int mask = 4; mask < 7; mask++) {
			mappedColors = hist.generate(mapColor, maxDist, 0);
			interestingColors = hist.size();
			if (interestingColors > mostInterestingColors)
				mostInterestingColors = interestingColors;
			System.out.println("For mask " + Integer.toHexString(0) 
					+"; # interesting = " + interestingColors
					+"; # mapped = " + mappedColors);
			//if (interestingColors <= 256)
			//	break;
			//if (mappedColors > total / 2)
			//	break;
		//} 
		
		int usedColors = Math.min(mapColor.getNumColors(), interestingColors);
		
		System.out.println("interestingColors="+interestingColors
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
			
			replaceColor(img, hist, idx, rgb8ToPixel(thePalette[c]), replaceDist);
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
				pixelToRGB(pixel, rgb);
				rgbToHsv(rgb[0] & 0xff, rgb[1] & 0xff, rgb[2] & 0xff, hsv);
				
				
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
			pixelToRGB(lastPixel, lastRGB);
			rgbToHsv(lastRGB[0] & 0xff, lastRGB[1] & 0xff, lastRGB[2] & 0xff, lastHSV);
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
		System.out.println("Replacing color #" + c +" with " + Integer.toHexString(newRGB) 
				+ " (#" + hist.getColorCount(c) + ")");
		
		int replaced = 0;
		int offs = 0;
		int[] mappedColors = hist.mappedColors();
		for (int y = 0; y < img.getHeight(); y++) {
			boolean changed = false;
			img.getRGB(0, y, img.getWidth(), 1, rgbs, 0, rgbs.length);
			for (int x = 0; x < img.getWidth(); x++) {
				if (mappedColors[offs] == c) {
					int dist = getPixelDistance(rgbs[x], newRGB);
					if (dist <= maxDist) {
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
		return replaced;
	}

	/**
	 * Only two colors can exist per 8x1 pixels, so find those colors.
	 * If there's a tossup (lots of colors), use information from neighbors
	 * to enhance the odds.
	 * @param img
	 * @param xoffs
	 * @param yoffs
	 */
	protected void reduceBitmapMode(BufferedImage img, int xoffs, int yoffs) {
		@SuppressWarnings("unchecked")
		Map<Integer, Integer>[] histograms = new Map[(img.getWidth() + 7) / 8];
		@SuppressWarnings("unchecked")
		Map<Integer, Integer>[] histogramSides = new Map[(img.getWidth() + 7) / 8];
		
		// be sure we select the 8 pixel groups sensibly
		if ((xoffs & 7) > 3)
			xoffs = (xoffs + 7) & ~7;
		else
			xoffs = xoffs & ~7;
		
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
				scmn = Math.max(0, x - 4);
				scmx = Math.min(width, maxx + 4);
				
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
					int cnt = entry.getValue() * 2;
					Integer scnt = histogramSide.get(c);
					if (scnt != null)
						cnt += scnt;
					sorted.add(new Pair<Integer, Integer>(c, cnt));
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
				
				int newPixel = 0;
				for (int xd = x; xd < maxx; xd++) {
					if (xd < width)
						newPixel = img.getRGB(xd, y);
					
					if (newPixel != fpixel && newPixel != bpixel) {
						if (fpixel < bpixel) {
							newPixel = newPixel <= fpixel ? fpixel : bpixel;
						} else {
							newPixel = newPixel < bpixel ? fpixel : bpixel;
						}
					}
						
					imageData.setPixel(xd + xoffs, y + yoffs, newPixel);
				}
			}
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

	/**
	 * @param image
	 * @param isLowColor if the image is known to have a small number of colors -- don't scale
	 */
	public void importImage(Image image, boolean isLowColor) {

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
			
			// make sure, for bitmap mode, that the size is a multiple of 8,
			// otherwise the import into video memory will destroy the picture
			if (format == Format.COLOR16_8x1) {
				targWidth &= ~7;
				targHeight = (int) (targWidth * realHeight / realWidth / aspect);
			}
		}
		
		Object hint = !isLowColor ? RenderingHints.VALUE_INTERPOLATION_BILINEAR
					:  RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;

		if (image instanceof BufferedImage) {
			BufferedImage bi = (BufferedImage) image;
			if (bi.getColorModel().getPixelSize() <= 8) {
				hint = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
			}
		}
		
		BufferedImage scaled = getScaledInstance(image, targWidth, targHeight, 
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
