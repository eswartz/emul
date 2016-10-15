/**
 * 
 */
package v9t9.video.imageimport;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import org.ejs.gui.images.AwtImageUtils;
import org.ejs.gui.images.ColorMapUtils;
import org.ejs.gui.images.IPaletteMapper;

import v9t9.common.video.VdpColorManager;
import ejs.base.utils.Pair;

/**
 * Emulate the Apple ][ hi-res mode. 
 * 
 * Nominal resolution is 280x192. Each byte represents 7 pixels. 'Off' bits are
 * black. 'On' bits are colored (violet/blue in even columns, green/red in odd columns) 
 * or white if two 'on' bits are next to each other.
 * 
 * @author ejs
 * 
 */
public class Apple2ModeConverter extends BaseBitmapModeConverter {

	private int black, white, violet, blue, green, red;
	private int blackPixel, whitePixel, violetPixel, bluePixel, greenPixel, redPixel;

	public Apple2ModeConverter(VdpColorManager colorMgr, boolean useColorMappedGreyScale, 
			IPaletteMapper mapColor) {
		
		// we handle four pixels at a time
		super(4, colorMgr, useColorMappedGreyScale, mapColor);
		
		black = 0;
		white = 15;
		violet = 3;
		red = 9;
		blue = 6;
		green = 12;

		blackPixel = mapColor.getPalettePixel(black);
		whitePixel = mapColor.getPalettePixel(white);
		violetPixel = mapColor.getPalettePixel(violet);
		redPixel = mapColor.getPalettePixel(red);
		bluePixel = mapColor.getPalettePixel(blue);
		greenPixel = mapColor.getPalettePixel(green);

	}
	
	/* (non-Javadoc)
	 * @see v9t9.video.imageimport.IModeConverter#prepareImage(java.awt.image.BufferedImage)
	 */
	@Override
	public BufferedImage prepareImage(BufferedImage img) {
		// in the Apple ][ mode, which is effectively
		// half the horizontal resolution due to color restrictions,
		// no point analyzing colors dithering beyond that
		return AwtImageUtils.getScaledInstance(
					img, img.getWidth() / 2, img.getHeight(), 
					RenderingHints.VALUE_INTERPOLATION_BILINEAR,
					false);
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
		analyzeBitmap(img, 0, new IBitmapColorUser() {
			
			@Override
			public void useColor(int x, int maxx, int y, 
					List<Pair<Integer,Integer>> sorted) {

				// see if violet/green are more prevalent than blue/red
				int violets = 0;
				int greens = 0;
				int blues = 0;
				int reds = 0;
				for (Pair<Integer,Integer> ent : sorted) {
					int color = mapColor.getClosestPaletteEntry(ent.first);
					if (color == violet) {
						violets++;
					} else if (color == green) {
						greens++;
					} else if (color == blue) {
						blues++;
					} else if (color == red) {
						reds++;
					}
				}
				
				int even, odd;
				
				if (violets + greens > blues + reds) {
					even = violetPixel;
					odd = greenPixel;
				}
				else {
					even = bluePixel;
					odd = redPixel;
				}
				
				int w = img.getWidth();
				
				int origPixel = 0;
				for (int xd = x; xd < maxx; xd++) {
					if (xd < w) {
						origPixel = img.getRGB(xd, y);
						if (useColorMappedGreyScale)
							origPixel = mapColor.getPixelForGreyscaleMode(origPixel);
					}
					
					
					int nx = xd * 2;

					int evenOdd = 0;

					int newPixel;
					int color = mapColor.getClosestPaletteEntry(origPixel);
					if (color == black) {
						newPixel = blackPixel;
					}
					else if (color == white) {
						newPixel = whitePixel;
					}
					else {
						// or, pick the best color
						int evenDist = useColorMappedGreyScale 
								? ColorMapUtils.getPixelLumDistance(origPixel, even) 
								: ColorMapUtils.getPixelDistance(origPixel, even);
						int oddDist = useColorMappedGreyScale 
								? ColorMapUtils.getPixelLumDistance(origPixel, odd) 
								: ColorMapUtils.getPixelDistance(origPixel, odd);
						int blackDist = useColorMappedGreyScale 
								? ColorMapUtils.getPixelLumDistance(origPixel, blackPixel)
								: ColorMapUtils.getPixelDistance(origPixel, blackPixel);
						int whiteDist = useColorMappedGreyScale 
								? ColorMapUtils.getPixelLumDistance(origPixel, whitePixel)
								: ColorMapUtils.getPixelDistance(origPixel, whitePixel);
						
						if (blackDist < oddDist && blackDist < evenDist) {
							newPixel = blackPixel;
						}
						else if (whiteDist < oddDist && whiteDist < evenDist) {
							newPixel = whitePixel;
						}
						else {
							if (oddDist < evenDist) {
								newPixel = odd;
								evenOdd = 1;
							} else {
								newPixel = even;
							}
						}
					}
					
					convertedImage.setRGB(nx ^ evenOdd, y + yoffs, newPixel);
					
					convertedImage.setRGB((nx + 1) ^ evenOdd, y + yoffs, 
							newPixel == whitePixel ? newPixel : blackPixel);
				}
				
			}
		});
	}


	/* (non-Javadoc)
	 * @see v9t9.video.imageimport.IModeConverter#convert(java.awt.image.BufferedImage, java.awt.image.BufferedImage, int, int)
	 */
	@Override
	public BufferedImage convert(BufferedImage img,
			int targWidth, int targHeight) {
		
		int xoffs, yoffs;

		xoffs = (targWidth - img.getWidth()) / 2;
		yoffs = (targHeight - img.getHeight()) / 2;
		
		// be sure we select the 8 pixel groups sensibly
		if ((xoffs & 7) > 3)
			xoffs = (xoffs + 7) & ~7;
		else
			xoffs = xoffs & ~7;
		
		BufferedImage convertedImage = new BufferedImage(targWidth, targHeight, 
				BufferedImage.TYPE_3BYTE_BGR);

		reduceBitmapMode(convertedImage, img, xoffs, yoffs);
		
		return convertedImage;
	}

}
