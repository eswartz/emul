/**
 * 
 */
package v9t9.video.imageimport;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ejs.gui.images.ColorMapUtils;
import org.ejs.gui.images.FixedPaletteMapColor;
import org.ejs.gui.images.IPaletteColorMapper;
import org.ejs.gui.images.IPaletteMapper;

import ejs.base.utils.Pair;
import v9t9.common.video.VdpColorManager;
import v9t9.video.imageimport.ImageImportOptions.Dither;

/**
 * @author ejs
 *
 */
public class BitmapModeConverter implements IModeConverter {

	private boolean useColorMappedGreyScale;
	private Dither ditherType;
	private VdpColorManager colorMgr;
	private IPaletteMapper mapColor;
	private byte[][] thePalette;
	private int firstColor;

	private Pair<Integer,Integer>[][] bitmapColors;

	public BitmapModeConverter(VdpColorManager colorMgr, boolean useColorMappedGreyScale, Dither ditherType,
			IPaletteMapper mapColor, byte[][] thePalette, int firstColor) {
		this.colorMgr = colorMgr;
		this.useColorMappedGreyScale = useColorMappedGreyScale;
		this.ditherType = ditherType;
		this.mapColor = mapColor;
		this.thePalette = thePalette;
		this.firstColor = firstColor;
		
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
					
//					System.out.println("at "+y+"/"+x+": " + Integer.toHexString(fpixel) + "/"+Integer.toHexString(bpixel));
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
							
							int threshold = DitherOrdered.thresholdMap8x8[xd & 7][y & 7];
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


	/* (non-Javadoc)
	 * @see v9t9.video.imageimport.IModeConverter#convert(java.awt.image.BufferedImage, java.awt.image.BufferedImage, int, int)
	 */
	@Override
	public void convert(BufferedImage convertedImage, BufferedImage img,
			int xoffs, int yoffs) {
		// be sure we select the 8 pixel groups sensibly
		if ((xoffs & 7) > 3)
			xoffs = (xoffs + 7) & ~7;
		else
			xoffs = xoffs & ~7;
		reduceBitmapMode(convertedImage, img, xoffs, yoffs);
		
	}

}
