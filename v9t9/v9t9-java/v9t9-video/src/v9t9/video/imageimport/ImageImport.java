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
import java.util.List;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.ejs.gui.images.AwtImageUtils;
import org.ejs.gui.images.ColorMapUtils;
import org.ejs.gui.images.ColorMedianCut;
import org.ejs.gui.images.ColorOctree;
import org.ejs.gui.images.FixedPaletteMapColor;
import org.ejs.gui.images.Histogram;
import org.ejs.gui.images.IColorQuantizer;
import org.ejs.gui.images.IColorQuantizer.ILeaf;
import org.ejs.gui.images.IDirectColorMapper;
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

	private VdpFormat format;
	private byte[][] thePalette;

	private boolean useColorMappedGreyScale;
	private Dither ditherType;
	private boolean ditherMono;
	private PaletteOption paletteOption;

	private final VdpColorManager colorMgr;
	private int firstColor;


	/** mapping from RGB-32 pixel to each palette index */
	protected TreeMap<Integer, Integer> paletteToIndex;

	private IColorQuantizer quantizer;
	private boolean useOctree = false;
	
	private boolean convertGreyScale;

	private IPaletteMapper mapColor;

	private float gamma = 1.0f;

	private boolean tryDirectMapping = true;

	private boolean flattenGreyScale;
	
	// # of meaningful bits in each palette channel
//	private int channelDepth = 3;
//	private int channelDepthMask;

	private boolean clip;

	private IDither dither;

	private IModeConverter modeConverter;

	public ImageImport(IVdpCanvas canvas) {
		synchronized (canvas) {
			this.colorMgr = canvas.getColorMgr();
		}
	}
	public ImageImport(VdpColorManager colorMgr) {
		this.colorMgr = colorMgr;
	}
	
	/**
	 * @param format2
	 * @return
	 */
	public static boolean isModeSupported(VdpFormat format) {
		return format != null && format.getLayout() != VdpFormat.Layout.TEXT;
	}
	
	protected BufferedImage convertImageData(BufferedImage img, int targWidth, int targHeight) {
		flatten(img);
		
		if (!importDirectMappedImage(img)) {
			if (gamma != 1.0f)
				gammaCorrect(img);
			
			// note: no matter what funky target mode we have, 
			// always dither/etc. on each pixel as if the mode
			// is a straight bitmap
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
		
		// take care of special modes
		BufferedImage converted = convertImageForMode(img, targWidth, targHeight);
		
		if (false) {
			File tempfile = new File(System.getProperty("java.io.tmpdir"), "dragged_cvt.png");
			System.out.println("Temporary buffer to " + tempfile);
			try {
				ImageIO.write(img, "png", tempfile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return converted;

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
		
		//Histogram hist = optimizeForNColors(img);
		
		updatePaletteMapping();

		dither.run(img, mapColor);
	}

	private void addToQuantizer(BufferedImage image) {
		int[] prgb = { 0, 0, 0 };
		int[] rgbs = new int[image.getWidth()];
		for (int y = 0; y < image.getHeight(); y++) {
			image.getRGB(0, y, rgbs.length, 1, rgbs, 0, rgbs.length);
			for (int x = 0; x < rgbs.length; x++) {
				ColorMapUtils.pixelToRGB(rgbs[x], prgb);
				quantizer.addColor(rgbs[x], prgb);
			}
		}
	}
	
	private void createOptimalPalette(int colorCount) {
		int toAllocate = ditherMono ? 2 : colorCount - firstColor;
		
		boolean perfect = quantizer.getLeafCount() <= toAllocate;
		
		if (!perfect)
			quantizer.reduceColors(toAllocate);

		int index = firstColor;
		
		List<ILeaf> leaves = quantizer.gatherLeaves();
		
		for (ILeaf node : leaves) {
			int[] repr = node.reprRGB();
			
			if (useColorMappedGreyScale)
				V99ColorMapUtils.rgbToGreyForGreyscaleMode(
						mapColor.getGreyToRgbMap(),
						repr, repr);
			else if (convertGreyScale) 
				ColorMapUtils.rgbToGrey(repr, repr);
//			else
//				V99ColorMapUtils.mapForRGB333(repr);	// no, don't lose info until needed
			
//			byte[] grb333 = V99ColorMapUtils.getGRB333(repr[0] * 0x7 / 0xff,
//					repr[1] * 0x7 / 0xff,
//					repr[2] * 0x7 / 0xff);
//			thePalette[index][0] = grb333[1];
//			thePalette[index][1] = grb333[0];
//			thePalette[index][2] = grb333[2];

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
	
	/**
	 * Take the image, which has been palette-mapped and/or dithered, and
	 * create a version that follows the rules of the VdpFormat.
	 * @param img
	 * @param targWidth
	 * @param targHeight
	 * @return
	 */
	private BufferedImage convertImageForMode(BufferedImage img, int targWidth, int targHeight) {
		int xoffs, yoffs;

		xoffs = (targWidth - img.getWidth()) / 2;
		yoffs = (targHeight - img.getHeight()) / 2;
		
		BufferedImage convertedImage = new BufferedImage(targWidth, targHeight, 
						BufferedImage.TYPE_3BYTE_BGR);
		
		modeConverter.convert(convertedImage, img, xoffs, yoffs);
		
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

	protected void updatePaletteMapping() {
		int ncols = format.getNumColors();
		
		paletteToIndex = new TreeMap<Integer, Integer>();
		
//		if (ditherMono) {
//			if (false&&format.isPaletted()) {
//				paletteToIndex.put(0x0, colorMgr.getForeground());
//				paletteToIndex.put(0xffffff, colorMgr.getBackground());
//			} else {
//				paletteToIndex.put(0x0, 0);
//				paletteToIndex.put(0xffffff, ncols - 1);
//			}
//			return;
//		}
		
		byte[] rgb = { 0, 0, 0};
		for (int c = firstColor; c < ncols; c++) {
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
		ditherMono = options.isDitherMono(); // || format.getNumColors() == 2;
		if (ditherMono && ditherType == Dither.NONE) 
			ditherType = Dither.FS;
		useOctree = options.isUseOctree();

		this.useColorMappedGreyScale = colorMgr.isGreyscale();
		
		byte[][] curPalette = colorMgr.getColorPalette();
		this.thePalette = new byte[Math.max(curPalette.length, format.getNumColors())][];
		for (int i = 0; i < curPalette.length; i++) {
			thePalette[i] = Arrays.copyOf(curPalette[i], curPalette[i].length);
			if (useColorMappedGreyScale) {
				thePalette[i] = Arrays.copyOf(
						V99ColorMapUtils.getRgbToGreyForGreyscaleMode(V99ColorMapUtils.getGreyToRgbMap332(), thePalette[i]), 3);
			}
		}
		for (int i = curPalette.length; i < thePalette.length; i++) {
			thePalette[i] = new byte[3];
		}
		
		firstColor = (colorMgr.isClearFromPalette() ? 0 : 1);

		paletteToIndex = null;

		quantizer = useOctree ?  new ColorOctree(4, true) : new ColorMedianCut();

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
				mapColor = new MonoMapColor(0, format.getNumColors()-1);
			}
		
			firstColor = 0;
			setChannelDepth(8);
		}
		else if (!format.isPaletted() && paletteOption == PaletteOption.FIXED) {
			switch (format.getNumColors()) {
			case 8:
				// 3 bits, 1+1+1
				mapColor = new RGB111MapColor(useColorMappedGreyScale);
				setChannelDepth(1);
				break;
			case 16:
				// 4 bits, 1+2+1
				mapColor = new RGB121MapColor(useColorMappedGreyScale);
				setChannelDepth(2);
				break;
			case 32:
				// 5 bits, 2+2+1
				mapColor = new RGB221MapColor(useColorMappedGreyScale);
				setChannelDepth(2);
				break;
			case 64:
				// 6 bits, 2+2+2
				mapColor = new RGB222MapColor(useColorMappedGreyScale);
				setChannelDepth(2);
				break;
			case 128:
				// 7 bits, 2+3+2
				mapColor = new RGB232MapColor(useColorMappedGreyScale);
				setChannelDepth(3);
				break;
			case 256:
			default:
				// 8 bits, 3+3+2
				mapColor = new RGB332MapColor(useColorMappedGreyScale);
				setChannelDepth(3);
				break;
			case 512:
				// 8 bits, 3+3+3
				mapColor = new RGB333MapColor(useColorMappedGreyScale);
				setChannelDepth(3);
				break;
			case 4096:
				// 16 bits, 4+4+4
				mapColor = new RGB444MapColor(useColorMappedGreyScale);
				setChannelDepth(4);
				break;
			}
			
			firstColor = 0;
		}
		else  {
			// real or invented mode with palette flexibility
			if (paletteOption == PaletteOption.OPTIMIZED) {
				mapColor = new UserPaletteMapColor(thePalette, 0, format.getNumColors(), useColorMappedGreyScale);
			} else if (paletteOption == PaletteOption.FIXED) {
				if (convertGreyScale)
					mapColor = new TI16MapColor(thePalette, false, true);
				else
					mapColor = new UserPaletteMapColor(thePalette, 0, format.getNumColors(), useColorMappedGreyScale);
			}
			else /* current */ {
				mapColor = new UserPaletteMapColor(thePalette, 0, format.getNumColors(), useColorMappedGreyScale);
			}
			setChannelDepth(3);
		}
		
		// get original mapping
		updatePaletteMapping();
		

		switch (ditherType) {
		case FS:
		case FSR:
			if (ditherMono) {
				dither = new DitherFloydSteinbergMono(thePalette, ditherType);
			} else {
				dither = new DitherFloydSteinberg(useColorMappedGreyScale, ditherType);
			}
			break;
		case ORDERED:
			dither = new DitherOrdered();
			break;
		default:
			dither = new DitherNone();
			break;
		}
	
		if (format.getLayout() == Layout.BITMAP_2_PER_8) {
			modeConverter = new BitmapModeConverter(colorMgr, useColorMappedGreyScale, ditherType, mapColor, curPalette, firstColor);
		} else if (format.getLayout() == Layout.APPLE2_HIRES) {
			modeConverter = new Apple2ModeConverter(colorMgr, useColorMappedGreyScale, mapColor);
		} else {
			modeConverter = new SimpleModeConverter();
		}
	}

	/**
	 * @param i
	 */
	private void setChannelDepth(int i) {
//		channelDepth = i;
//		channelDepthMask = (~(0xff >> channelDepth) & 0xff);
	}
	
	public void addImage(ImageImportOptions options, BufferedImage image) {
		if (image == null)
			return;
		
		if (format == null)
			return;
		
		if (paletteOption == PaletteOption.OPTIMIZED) {
			addToQuantizer(image);
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
		
		updatePaletteMapping();
		
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
		
		Object hint = imageImportOptions.isScaleSmooth() ? RenderingHints.VALUE_INTERPOLATION_BILINEAR
				:  RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
		
		boolean preScale = screenWidth < realWidth && screenHeight < realHeight;
		
		BufferedImage[] converted = new BufferedImage[frames.length];
		
		for (int i = 0; i < frames.length; i++) {
			ImageFrame frame = frames[i];
			if (preScale)
				converted[i] = AwtImageUtils.getScaledInstance(
						frame.image, targWidth, targHeight, 
						hint,
						false);
			else
				converted[i] = frame.image;

			addImage(imageImportOptions, converted[i]);
		}

		finishAddingImages();

		ImageImportData[] datas = new ImageImportData[frames.length];
		for (int i = 0; i < datas.length; i++) {
			if (!preScale)
				converted[i] = AwtImageUtils.getScaledInstance(
					converted[i], targWidth, targHeight, 
					hint,
					false);
			
			ImageImportData data = convertImage(imageImportOptions, 
					converted[i],
					clip ? targWidth : screenWidth, 
					clip ? targHeight : screenHeight);
			data.delayMs = frames[i].delayMs;
			datas[i] = data;
		}

		return datas;		
	}
	
	public void setFlattenGreyscale(boolean b) {
		this.flattenGreyScale = b;
	}
	public void setClip(boolean clip) {
		this.clip = clip;
	}
	
}
