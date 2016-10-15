/**
 * 
 */
package v9t9.video.imageimport;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ejs.gui.images.ColorMapUtils;
import org.ejs.gui.images.FixedPaletteMapColor;
import org.ejs.gui.images.IPaletteColorMapper;
import org.ejs.gui.images.IPaletteMapper;

import v9t9.common.video.VdpColorManager;
import v9t9.video.imageimport.ImageImportOptions.Dither;
import ejs.base.utils.Pair;

/**
 * @author ejs
 *
 */
public class BitmapModeConverter extends BaseBitmapModeConverter {

	private Dither ditherType;
	private byte[][] thePalette;
	private int firstColor;

	public BitmapModeConverter(VdpColorManager colorMgr, boolean useColorMappedGreyScale, Dither ditherType,
			IPaletteMapper mapColor, byte[][] thePalette, int firstColor) {
		super(8, colorMgr, useColorMappedGreyScale, mapColor);
		this.ditherType = ditherType;
		this.thePalette = thePalette;
		this.firstColor = firstColor;
		
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
