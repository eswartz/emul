package v9t9.emulator.clients.builtin.video;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
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

public abstract class ImageDataCanvas extends BitmapVdpCanvas {

	protected ImageData imageData;
	

	public ImageDataCanvas(int extraSpace) {
		super(extraSpace);
	}

	public ImageData getImageData() {
		return imageData;
	}

	@Override
	final public int getLineStride() {
		return bytesPerLine;
	}



	@Override
	public void doChangeSize() {
		imageData = createImageData();
	}

	abstract protected ImageData createImageData();

	public int getDisplayAdjustOffset() {
		int displayAdjust = getYOffset() * getLineStride() + (getXOffset() + this.extraSpace / 2) * getPixelStride();
		return displayAdjust;
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
			rgb = getGRB333(prgb[1] >> 5, prgb[0] >> 5, prgb[2] >> 5);
			newPixel = getPixel(rgb);
			closest = newPixel;
		}
		else {
			closest = getClosestColor(ncols, prgb, format == Format.COLOR16_8x1);
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

	private int getClosestColor(int ncols, int[] prgb, boolean limit8) {
		int closest = -1;
		int mindiff = Integer.MAX_VALUE;
		float[] phsv = rgbToHsv(prgb);
		for (int c = 1; c < ncols; c++) {
			int dist;
			
			int dr = ((thePalette[c][0] & 0xff) - prgb[0]);
			int dg = ((thePalette[c][1] & 0xff) - prgb[1]);
			int db = ((thePalette[c][2] & 0xff) - prgb[2]);
			dist = (dr * dr) + (dg * dg) + (db * db);

			if (limit8 && phsv[1] < 0.25) {
				// bias closer saturation more, to avoid too many unrelated colors
				float[] chsv = rgbToHsv(thePalette[c]);
				int sd = (int) ((1.0 - (phsv[1] - chsv[1])) * 100); 
				dist += sd*sd;
			}
			/*
			float[] chsv = rgbToHsv(thePalette[c]);
			int dh = (int) (phsv[0] - chsv[0]);
			int ds = (int) ((phsv[1] - chsv[1]) * 50);
			int dv = (int) ((phsv[2] - chsv[2]));
			dist = dh*dh + ds*ds + dv*dv; 
			*/
			if (dist < mindiff) {
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

		boolean limitDither = false;

		int ncols;
		if (format == Format.COLOR16_8x1 || format == Format.COLOR16_4x4) {
			ncols = 16;
			
			limitDither = true;
		}
		else if (format == Format.COLOR16_1x1) {
			ncols = 16;
			
			optimizePalette(img);
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

		int xo;
		int yo;
		
		if (format == Format.COLOR16_4x4) {
			xo = (64 - img.getWidth()) / 2;
			yo = (48 - img.getHeight()) / 2;
		} else {
			xo = (getVisibleWidth() - img.getWidth() + getXOffset()) / 2;
			yo = (getVisibleHeight() - img.getHeight() + getYOffset()) / 2;
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
	
	private int rgbIdx(int r, int g, int b) {
		return r*64 + g*8 + b;
	}
	
	private int scaleReduce(int rgb, int shift) {
		int val = (rgb >>> shift) & 0xff;
		
		if (val > 255)
			val = 255;
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
	 * So, rather than taking the 15 most prominent colors, we take
	 * a sampling of the most important colors covering 25% of the
	 * image.
	 * 
	 * The image will still be dithered without any knowledge of this
	 * palette alteration, so we need to update the image to physically
	 * replace the selected colors with their exact RGB values, so 
	 * dithering will not turn a solid color into a mess.
	 * 
	 * @param img
	 */
	private void optimizePalette(BufferedImage img) {
		final float[] hist = new float[512];
		int[] rgbs = new int[img.getWidth()];
		//double[] xImp = new double[img.getWidth()];
		for (int y = 0; y < img.getHeight(); y++) {
			//double yImportance = Math.sin(y * Math.PI / img.getHeight());
			img.getRGB(0, y, img.getWidth(), 1, rgbs, 0, rgbs.length);
			for (int i = 0; i < rgbs.length; i++) {
				/*double xImportance;
				if (y == 0) {
					xImp[i] = Math.sin(i * Math.PI / rgbs.length);
				}
				xImportance = xImp[i];
				
				double importance = xImportance + yImportance;
				//double importance = 1; // + Math.sqrt(xImportance * yImportance);
				*/
				
				int rgb = rgbs[i];
				int r = scaleReduce(rgb, 16);
				int g = scaleReduce(rgb, 8);
				int b = scaleReduce(rgb, 0);
				hist[rgbIdx(r,g,b)] += 0.5f;
				
				/*
				if (false && importance >= 0.25) {
					// due to loss of color in the palette, see
					// if any of the colors are hiding in nearby ones,
					// so dithering will introduce these colors 
					int hr = scaleReduceBias(rgb, 16, true);
					int hg = scaleReduceBias(rgb, 8, true);
					int hb = scaleReduceBias(rgb, 0, true);
					//if (hr != r || hg != g || hb != b)
						hist[rgbIdx(hr,hg,hb)] += 0.125f * importance;
					
					int lr = scaleReduceBias(rgb, 16, false);
					int lg = scaleReduceBias(rgb, 8, false);
					int lb = scaleReduceBias(rgb, 0, false);
					//if (lr != r || lg != g || lb != b)
						hist[rgbIdx(lr,lg,lb)] += 0.375 * importance;
				}
				*/
			}
		}
		List<Integer> indices = new ArrayList<Integer>(512);
		for (int i = 0; i < 512; i++)
			indices.add(i);
		
		Collections.sort(indices, new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				return (int) Math.signum(hist[o2] - hist[o1]);
			}
		});
		
		// take sampling of most prominent colors
		float goal = 0;
		for (float h : hist)
			goal += h;
		
		//goal *= 0.75f;
		
		int interestingColors = 0;
		while (interestingColors < 512) {
			int idx = indices.get(interestingColors);
			if (hist[idx] == 0)
				break;
			goal -= hist[idx];
			if (goal < 0 && interestingColors >= 15)
				break;
			interestingColors++;
		}

		int usedColors = Math.min(15, interestingColors);
		
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
		for (int i = 0; i < usedColors; i++) {
			int c = i + 1;
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
			System.out.println("index="+index);
			prev = index;
			int idx = indices.get(index);
			int r = (idx>>6) & 0x7;
			int g = (idx>>3) & 0x7;
			int b = (idx>>0) & 0x7;
			setGRB333(c, g, r, b);
			
			if ((interestingColors == usedColors || i < (highColors ? 4 : 8))) {
				// ensure there will be an exact match so no dithering occurs on the primary ones
				// for this color
				byte[] rgb = getRGB(c);
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
		for (int y = 0; y < img.getHeight(); y++) {
			img.getRGB(0, y, img.getWidth(), 1, rgbs, 0, rgbs.length);
			for (int i = 0; i < rgbs.length; i++) {
				int rgb = rgbs[i];
				int r = scaleReduce(rgb, 16);
				int g = scaleReduce(rgb, 8);
				int b = scaleReduce(rgb, 0);
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
		if (ncols < 256) {
			for (int c = 0; c < ncols; c++) {
				palettePixels[c] = getPixel(thePalette[c]);
				paletteToIndex.put(palettePixels[c], c);
			}
		} else {
			byte[] rgb = { 0, 0, 0};
			for (int c = 0; c < ncols; c++) {
				getGRB332(rgb, (byte) c);
				palettePixels[c] = getPixel(rgb);
				paletteToIndex.put(palettePixels[c], c);
			}
		}
		
		paletteMappingDirty = false;
	}

	/**
	 * @param buffer
	 * @return 
	 */
	public ByteBuffer copy(ByteBuffer buffer) {
		if (buffer.capacity() < imageData.bytesPerLine * getVisibleHeight())
			buffer = ByteBuffer.allocateDirect(imageData.bytesPerLine * getVisibleHeight());

		buffer.rewind();
		int vw = getVisibleWidth();
		int vh = getVisibleHeight();
		int offs = getBitmapOffset(0, 0);
		int bpp = imageData.bytesPerLine / imageData.width;
		for (int r = 0; r < vh; r++) {
			buffer.put(imageData.data, offs, bpp * vw);
			offs += imageData.bytesPerLine;
		}
		buffer.rewind();
		
		return buffer;
	}
}