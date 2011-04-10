package v9t9.emulator.clients.builtin.video;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.ImageData;
import org.ejs.coffee.core.utils.Pair;

public abstract class ImageDataCanvas extends VdpCanvas {

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

		int ncols;
		if (format == Format.COLOR16_1x1 || format == Format.COLOR16_8x1 || format == Format.COLOR16_4x4) {
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

		int xo;
		int yo;
		
		if (format == Format.COLOR16_4x4) {
			xo = (64 - img.getWidth()) / 2;
			yo = (48 - img.getHeight()) / 2;
		} else {
			xo = (imageData.width - img.getWidth()) / 2;
			yo = (imageData.height - img.getHeight()) / 2;
		}

		Arrays.fill(imageData.data, (byte) 0); 
		
		updatePaletteMapping();

		boolean limitDither = format == Format.COLOR16_8x1 || format == Format.COLOR16_4x4;
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				ditherize(img, x, y, ncols, limitDither);
			}
		}
		
		if (format == Format.COLOR16_8x1) {
			
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

		} else {

			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					imageData.setPixel(x + xo, y + yo, img.getRGB(x, y));
				}
			}
		}
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.VdpCanvas#getPixel(int, int)
	 */
	@Override
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

}