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
	
	private class TI16MapColor implements IMapColor {
		
		public TI16MapColor() {
		}
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getNumColors()
		 */
		@Override
		public int getNumColors() {
			return 16;
		}
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
		 */
		@Override
		public int mapColor(int[] prgb, int[] distA) {
			int closest = getCloseColor(prgb);
			distA[0] = getColorDistance(thePalette, closest, prgb);
			
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

			int white = getClosestColorByDistance(thePalette, new int[] { 255, 255, 255 }, -1);
			int black = getClosestColorByDistance(thePalette, new int[] { 0, 0, 0 }, -1);
			int[] idealGrey = { 192, 192, 192 };
			int grey = getClosestColorByDistance(thePalette, idealGrey, white);
			if (grey == white)
				grey = 8;
			
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
				closest = getClosestColorByDistance(thePalette, prgb, 12);
				
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
		
		/*
		int rigidMatch(float[] phsv, float hue, float val) {
			int closest;
			if (val <= 25) {
				// any color
				closest = 1;
			}
			else if (phsv[1] < 0.333f) {
				if (val >= 66) {
					closest = 15;
				} else if (val >= 33) {
					// dithering will take care of the rest
					closest = 14;
				} else {
					closest = 1;
				}
			} else if (hue >= 330 || hue < 30) {
				// red
				if (hue < 30 && phsv[1] < 0.75) {
					// skin
					if (val >= 75)
						closest = 9;
					else if (val >= 66)
						closest = 8;
					else
						closest = 1;
				}
				else if (val >= 100 - 25/2) {
					closest = 8;
				} else if (val >= 50 - 25/2) {
					closest = 6;
				} else {
					closest = 1;
				}
			} else if (hue >= 30 && hue < 75) {
				// yellow/orange
				if (val >= 95) {
					if (phsv[1] < 0.8)
						closest = 11;
					else
						closest = 10;
				} else if (val >= 75 && phsv[1] >= 0.8) {
					closest = 10;
				} else if (val >= 66 && phsv[1] < 0.8) {
					closest = 9;
				} else {
					if (hue >= 75 && val >= 50 - 25/2) {
						// green-yellow
						closest = 12;
					//} else if (val < 50 && phsv[1] >= 0.75f) {
					//	// could be considered green
					//	closest = 12;
					} else if (val >= 33) {
						closest = 14;
					} else {
						closest = 1;
					}
				}
			} else if (hue >= 75 && hue < 140) {
				// green
				if (val >= 75) {
					closest = 3;
				} else if (val >= 64 - 25/2) {
					closest = 2;
				} else if (val >= 25 - 25/2) {
					closest = 12;
				} else {
					closest = 1;
				}
			} else if (hue >= 140 && hue < 200) {
				// cyan
				if (val >= 75) {
					closest = 7;
				} else {
					closest = 1;
				}
			} else if (hue >= 200 && hue < 270) {
				// blue
				if (val >= 100 - 25/2 && hue < 210) {
					closest = 7;
				} else if (val >= 75) {
					closest = 5;
				} else if (val >= 50 - 25/2) {
					closest = 4;
				} else {
					closest = 1;
				}
			} else {
				// purple
				if (val >= 50 - 25/2) {
					closest = 13;
				} else {
					closest = 1;
				}
			}
			return closest;
		}
		 */
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestColor(int[])
		 */
		@Override
		public int getClosestPalettePixel(int[] prgb) {
			int c = getCloseColor(prgb);
			return palettePixels[c];
		}
	}


	private class User16MapColor implements IMapColor {
		public User16MapColor() {
			
		}
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getNumColors()
		 */
		@Override
		public int getNumColors() {
			return 16;
		}
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
		 */
		@Override
		public int mapColor(int[] prgb, int[] distA) {
			int closest = getCloseColor(prgb);
			distA[0] = getColorDistance(thePalette, closest, prgb);
			
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
			
			return getClosestColorByDistance(thePalette, prgb, -1);
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestColor(int[])
		 */
		@Override
		public int getClosestPalettePixel(int[] prgb) {
			int c = getCloseColor(prgb);
			return palettePixels[c];
		}
	}


/*
	private class TI16ColorPaletteMapColor implements IMapColor {

		Palette16[] palettes;
		private TreeMap<Integer, Integer> pixelMap;
		public TI16ColorPaletteMapColor() {
			palettes = new Palette16[VdpCanvas.palettes.length + 1];
			int idx = 0;
			for (byte[][] palette : VdpCanvas.palettes) {
				palettes[idx++] = new Palette16(palette);
			}
			palettes[idx++] = new Palette16(thePalette);
			
			pixelMap = new TreeMap<Integer, Integer>();
			
		}
		
		@Override
		public int getNumColors() {
			return 16;
		}
		
		@Override
		public int mapColor(int[] prgb, int[] distA) {
			int pixel = getPixel(prgb);
			for (Palette16 palette : palettes) {
				for (int c = 1; c < 16; c++) {
					int dist = getColorDistance(palette.thePalette, c, prgb);
					if (dist < 25*3) {
						pixelMap.put(pixel, c);
						distA[0] = dist;
						return c;
					}
				}
			}
			
			distA[0] = Integer.MAX_VALUE;
			return -1;
		}
		
		@Override
		public int getClosestPalettePixel(int[] prgb) {
			int pixel = getPixel(prgb);
			Integer c = pixelMap.get(pixel);
			if (c == null)
				return -1;
			else
				return c;
		}
	}
	*/


	private class RGB333MapColor implements IMapColor {

		private final int numColors;

		/**
		 * 
		 */
		public RGB333MapColor(int numColors) {
			this.numColors = numColors;
		}
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getNumColors()
		 */
		@Override
		public int getNumColors() {
			return numColors;
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
		 */
		@Override
		public int mapColor(int[] prgb, int[] dist) {
			int r = prgb[0] >>> 5;
			int g = prgb[1] >>> 5;
			int b = prgb[2] >>> 5;
			
			// not actual RGB332 index!
			int c = (r << 6) | (g << 3) | b;
			
			byte[] rgbs = VdpCanvas.getGRB333(g, r, b);
			
			int dr = ((rgbs[0] & 0xff) - (prgb[0] & 0xff));
			int dg = ((rgbs[1] & 0xff) - (prgb[1] & 0xff));
			int db = ((rgbs[2] & 0xff) - (prgb[2] & 0xff));
			
			dist[0] = (dr*dr) + (dg*dg) + (db*db);
			
			return c;
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestPaletteColor(int[])
		 */
		@Override
		public int getClosestPalettePixel(int[] prgb) {
			int closest = -1;
			int mindiff = Integer.MAX_VALUE;
			for (int c = firstColor; c < numColors; c++) {
				int dist = getColorDistance(c, prgb);
				if (dist < mindiff) {
					closest = c;
					mindiff = dist;
				}
			}
			return rgb8ToPixel(thePalette[closest]);
		}
	}


	private class RGB332MapColor implements IMapColor {

		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getNumColors()
		 */
		@Override
		public int getNumColors() {
			return 16;
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
		 */
		@Override
		public int mapColor(int[] prgb, int[] dist) {
			int r = prgb[0] >>> 5;
			int g = prgb[1] >>> 5;
			int b = (prgb[2] >>> 5) & ~0x1;
			
			// not actual RGB332 index!
			int c = (r << 6) | (g << 3) | b;
			
			byte[] rgbs = VdpCanvas.getGRB333(g, r, b);
			
			int dr = ((rgbs[0] & 0xff) - (prgb[0] & 0xff));
			int dg = ((rgbs[1] & 0xff) - (prgb[1] & 0xff));
			int db = ((rgbs[2] & 0xff) - (prgb[2] & 0xff));
			
			dist[0] = (dr*dr) + (dg*dg) + (db*db);
			
			return c;
		}
		
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getClosestPaletteColor(int[])
		 */
		@Override
		public int getClosestPalettePixel(int[] prgb) {
			byte[] rgb = VdpCanvas.getGRB333(prgb[1] >> 5, prgb[0] >> 5, prgb[2] >> 5);
			return rgb8ToPixel(rgb);
		}
		
	}
	

	private static class PaletteMapColor implements IMapColor {
		private final int numColors;
		private final byte[][] palette;
		private int firstColor;

		public PaletteMapColor(byte[][] thePalette, int firstColor, int numColors) {
			this.palette = thePalette;
			this.numColors = numColors;
			this.firstColor = firstColor;
		}
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageImport.IMapColor#getNumColors()
		 */
		@Override
		public int getNumColors() {
			return numColors;
		}
		/* (non-Javadoc)
		 * @see v9t9.emulator.clients.builtin.video.ImageDataCanvas.IMapColor#mapColor(int, int[])
		 */
		@Override
		public int mapColor(int[] prgb, int[] distA) {
			for (int c = firstColor; c < numColors; c++) {
				int dist = getColorDistance(palette, c, prgb);
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
				int dist = getColorDistance(palette, c, prgb);
				if (dist < 25*3) {
					return rgb8ToPixel(palette[closest]);
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
		
		if (canvas.isGreyscale()) {
			// convert palette
			byte[][] greyPalette = new byte[thePalette.length][];
			for (int c = 0; c < greyPalette.length; c++) {
				greyPalette[c] = canvas.rgbToGrey(thePalette[c]);
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
	}

	private void getRGB(int pixel, int[] rgb) {
		rgb[0] = (pixel & 0xff0000) >> 16;
		rgb[1] = (pixel & 0xff00) >> 8;
		rgb[2] = pixel & 0xff;
	}
	private void getRGB(BufferedImage img, int x, int y, int[] rgb) {
		int pixel = img.getRGB(x, y);
		getRGB(pixel, rgb);
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
		getRGB(pixel, prgb);

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
				dist = getColorDistance(palette, c, prgb);
				
				float dh = Math.abs(palhsv[c][0] - phsv[0]) * 6;
				
				dist = dist * 16 + (int) (dh * dh);
			}
			
			return dist;
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

	private static int getColorDistance(byte[][] palette, int c, int[] prgb) {
		int dist;
		
		int dr = ((palette[c][0] & 0xff) - prgb[0]);
		int dg = ((palette[c][1] & 0xff) - prgb[1]);
		int db = ((palette[c][2] & 0xff) - prgb[2]);
		
		dist = (dr * dr) + (dg * dg) + (db * db);
		return dist;
	}

	private int getColorDistance(int c, int[] prgb) {
		return getColorDistance(thePalette, c, prgb);
	}

	private static int getClosestColorByDistance(byte[][] thePalette, int[] prgb, int exceptFor) {
		int mindiff = Integer.MAX_VALUE;
		int closest = -1;
		
		for (int c = 0; c < 16; c++) {
			if (c != exceptFor) {
				int dist = getColorDistance(thePalette, c, prgb);
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

	/** Import image from 'img' and set the color indices in 'colorMap' which is #getVisibleWidth() by #getVisibleHeight() */
	protected void setImageData(BufferedImage img) {
		if (format == null || format == Format.TEXT || format == Format.COLOR16_8x8)
			return;

		equalize(img);
		
		updatePaletteMapping();

		flatten(img);
		
		if (!importDirectMappedImage(img)) {
			convertImageToColorMap(img);
		}

		replaceImageData(img);
	}

	/**
	 * Remove alpha
	 * @param img
	 */
	private void flatten(BufferedImage img) {
		int[] prgb = { 0, 0, 0 };
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int pixel = img.getRGB(x, y);
				int alpha = pixel >>> 24;
				if (alpha == 0) {
					pixel = 0xffffff;
				}
				else if (alpha != 0xff) {
					// blend with white
					prgb[0] = (pixel & 0xff0000) >> 16;
					prgb[1] = (pixel & 0xff00) >> 8;
					prgb[2] = pixel & 0xff;
					prgb[0] = (alpha * prgb[0] + (255 - alpha) * 255) / 256; 
					prgb[1] = (alpha * prgb[1] + (255 - alpha) * 255) / 256; 
					prgb[2] = (alpha * prgb[2] + (255 - alpha) * 255) / 256; 
					pixel = rgb8ToPixel(prgb);
				}
				else
					continue;
				img.setRGB(x, y, pixel | 0xff000000);
			}
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
		if (format == Format.COLOR256_1x1)
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
		palettes.addAll(Arrays.asList(VdpCanvas.palettes()));
		
		for (byte[][] palette : palettes) {
			int matchedC = hist.generate(new PaletteMapColor(palette, firstColor, numColors), maxDist); 
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
			mapColor = canSetPalette ? new RGB333MapColor(16) 
				: isStandardPalette() ? new TI16MapColor()
				: new User16MapColor();
				
			optimizeForNColors(img, mapColor);
			limitDither = !canSetPalette;
			//limitDither = true ; // !canSetPalette;
		}
		else if (format == Format.COLOR16_1x1) {
			mapColor = new RGB333MapColor(16);

			optimizeForNColors(img, mapColor);
		}
		else if (format == Format.COLOR4_1x1) {
			mapColor = new RGB333MapColor(4);
			
			optimizeForNColors(img, mapColor);
		}
		else if (format == Format.COLOR256_1x1) {
			mapColor = new RGB332MapColor();
			optimizeFor256Colors(img, mapColor);
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

	private boolean isStandardPalette() {
		for (byte[][] palette : VdpCanvas.allPalettes()) {
			if (Arrays.equals(palette, thePalette))
				return true;
		}
		return false;
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

		if (format == Format.COLOR16_8x1) {
			
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
		 * @return the number of colors that map directly (within maxDist)
		 */
		public int generate(IMapColor paletteMapper, int maxDist) {
			int mapped = gather(paletteMapper, maxDist);
			
			sort();
			
			return mapped;
		}

		private int gather(IMapColor paletteMapper, int maxDist) {
			hist.clear();
			indices.clear();
			pixelToColor.clear();
			Arrays.fill(mappedColors, 0);
			
			int[] distA = { 0 };
			int[] prgb = { 0, 0, 0 };
			int offs = 0;
			int mapped = 0;
			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					int pixel = img.getRGB(x, y);
					pixel &= 0xffffff;
					int c;
					Integer color = pixelToColor.get(pixel);
					if (color == null) {
						prgb[0] = (pixel & 0xff0000) >> 16;
						prgb[1] = (pixel & 0xff00) >> 8;
						prgb[2] = pixel & 0xff;
						
						c = paletteMapper.mapColor(prgb, distA);
						if (distA[0] <= maxDist) {
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
		 * @param i
		 * @return
		 */
		public int getColorIndex(int i) {
			return indices.get(i);
		}

		/**
		 * @return
		 */
		public int[] colorMap() {
			return mappedColors;
		}
		
		/**
		 * @return the pixelToColor
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
	
	/**
	 * Equalize an image so it has a full range of 
	 * saturation and value
	 */
	private void equalize(BufferedImage img) {
		float minSat = Float.MAX_VALUE, maxSat = Float.MIN_VALUE;
		float minVal = Float.MAX_VALUE, maxVal = Float.MIN_VALUE;
		
		int[] prgb = { 0, 0, 0 };
		float[] hsv = { 0, 0, 0 };
		
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int pixel = img.getRGB(x, y);
				getRGB(pixel, prgb);
				rgbToHsv(prgb[0], prgb[1], prgb[2], hsv);
				minSat = Math.min(hsv[1], minSat);
				maxSat = Math.max(hsv[1], maxSat);
				minVal = Math.min(hsv[2], minVal);
				maxVal = Math.max(hsv[2], maxVal);
			}
		}
		
		System.out.println("Equalize: sat = "+minSat+" to " +maxSat+"; val = " + minVal + " to " + maxVal);
		float satScale = 1.0f - minSat;
		float valScale = 255 - minVal;
		float satDiff = maxSat - minSat;
		float valDiff = maxVal - minVal;
		
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int pixel = img.getRGB(x, y);
				getRGB(pixel, prgb);
				rgbToHsv(prgb[0], prgb[1], prgb[2], hsv);
				
				if (satDiff > 0.5)
					hsv[1] = ((hsv[1] - minSat) / satDiff) * satScale + minSat;
				if (valDiff > 50)
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
		
		int minDist = Integer.MAX_VALUE;
		for (int c = firstColor; c < numColors; c++) {
			for (int d = c + 1; d < numColors; d++) {
				int[] prgb = { thePalette[d][0] & 0xff,
						thePalette[d][1] & 0xff, 
						thePalette[d][2] & 0xff 
				};
				int dist = getColorDistance(c, prgb);
				if (dist < minDist)
					minDist = dist;
			}
		}
		
		int ourDist = numColors == 16 ? Integer.MAX_VALUE : minDist;
		System.out.println("Minimum color palette distance: " + minDist+ "; using " + ourDist);

		Histogram hist = new Histogram(img);
		hist.generate(mapColor, ourDist);

		int interestingColors = hist.size();
		
		boolean highColors = interestingColors >= numColors * 3 / 4;
		
		int usedColors = Math.min(numColors * 3 / 4, 
				Math.min(numColors, interestingColors));
		
		if (interestingColors == 2)
			usedColors = 1;
		
		System.out.println("N-color: interestingColors="+interestingColors
				+"; usedColors="+usedColors
				+"; highColor="+highColors
				);
		
		//int replaceLimit = usedColors;
		
		ourDist = minDist;
		for (int i = 0; i < usedColors; i++) {
			// ensure there will be an exact match so no dithering 
			// occurs on the primary occurrences of this color
			int idx = hist.getColorIndex(i);
			byte[] rgb = canvas.getRGB(idx);
			int newRGB = ((rgb[0] & 0xff) << 16) | ((rgb[1] & 0xff) << 8) | ((rgb[2] & 0xff));
			//replaceColor16(img, rgbs, idx, newRGB, ourDist);
			replaceColor(img, hist, idx, newRGB, ourDist);
		}
		
		return ourDist;
	}

	/**
	 * @param img
	 * @param rgbs 
	 * @param r
	 * @param g
	 * @param b
	 * @param rgb
	 */
	/*
	private void replaceColor16(BufferedImage img, int[] rgbs, int theC, int newRGB, int minDist) {
		for (int y = 0; y < img.getHeight(); y++) {
			img.getRGB(0, y, img.getWidth(), 1, rgbs, 0, rgbs.length);
			for (int i = 0; i < rgbs.length; i++) {
				int rgb = rgbs[i];
				
				int prgb[] = { (rgb >> 16) & 0xff,
					(rgb >> 8) & 0xff,
					(rgb >> 0) & 0xff };
				int c = getClosestColor16(16, prgb, minDist);
				
				if (c == theC) {
					rgbs[i] = newRGB;
				}
			}
			img.setRGB(0, y, img.getWidth(), 1, rgbs, 0, rgbs.length);
		}

	}
	*/
	
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
		
		// 0xff --> 0xe0 for R, G, B
		int maxDist = 0x1f*0x1f * 3;

		Histogram hist = new Histogram(img);
		hist.generate(mapColor, maxDist);
		int interestingColors = hist.size();
		
		int usedColors = Math.min(mapColor.getNumColors(), interestingColors);
		
		boolean highColors = interestingColors > mapColor.getNumColors() * 4;
		
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
		//int replaceLimit = (highColors ? 4 : 8);
		
		//int replaceLimit = highColors ? mappedColors.length / 16 : mappedColors.length;
		
		int replaceDist = highColors ? 0x3*0x3*3 : maxDist;
		
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
			int idx = hist.getColorIndex(index);
			int r = (idx>>6) & 0x7;
			int g = (idx>>3) & 0x7;
			int b = (idx>>0) & 0x7;
			canvas.setGRB333(c, g, r, b);
			
			//if ((interestingColors == usedColors || i < replaceLimit)) {
			replaceColor(img, hist, idx, rgb8ToPixel(thePalette[c]), replaceDist);
		}
		
		return maxDist;
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
	 * @param mapColor 
	 */
	private void optimizeFor256Colors(BufferedImage img, IMapColor mapColor) {

		// 0xff --> 0xe0 for R, G and 0xff -> 0xc0 for B
		int maxDist = 0x1f*0x1f * 2 + 0x3f*0x3f;
		
		Histogram hist = new Histogram(img);
		hist.generate(mapColor, maxDist);
		
		int interestingColors = hist.size();
		
		int usedColors = Math.min(256, interestingColors);
		
		boolean highColors = interestingColors > 64;
		System.out.println("256: interestingColors="+interestingColors+"; usedColors="+usedColors+"; highColor="+highColors);
		
		int[] mappedColors = hist.colorMap();
		int replaceLimit = highColors ? mappedColors.length / 4 : mappedColors.length;
		
		int replaceDist = 0x7*0x7*2 + 0x3*0x3;
		
		byte[] rgb = { 0, 0, 0};
		for (int i = 0; i < usedColors && replaceLimit > 0; i++) {
			int idx = hist.getColorIndex(i);
			
			canvas.getGRB332(rgb, (byte)(idx >> 1));
			byte y = rgb[1]; rgb[1] = rgb[0]; rgb[0] = y;
			int newRGB = ((rgb[0] & 0xff) << 16) | ((rgb[1] & 0xff) << 8) | ((rgb[2] & 0xff));
			
			replaceColor(img, hist, idx, newRGB, replaceDist);
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
		System.out.println("Replacing color '" + c +"' with " + Integer.toHexString(newRGB));
		
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
	 * @param i
	 * @param newRGB
	 * @return
	 */
	private int getPixelDistance(int pixel, int newRGB) {
		int dr = ((pixel & 0xff0000) - (newRGB & 0xff0000)) >> 16; 
		int dg = ((pixel & 0x00ff00) - (newRGB & 0x00ff00)) >> 8; 
		int db = ((pixel & 0x0000ff) - (newRGB & 0x0000ff)) >> 0; 
		return dr*dr + dg*dg + db*db;
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

	protected static int rgb8ToPixel(byte[] nrgb) {
		return ((nrgb[0] & 0xff) << 16) | ((nrgb[1] & 0xff) << 8) | (nrgb[2] & 0xff);
	}
	protected static int rgb8ToPixel(int[] prgb) {
		return ((prgb[0]) << 16) | ((prgb[1]) << 8) | (prgb[2]);
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
