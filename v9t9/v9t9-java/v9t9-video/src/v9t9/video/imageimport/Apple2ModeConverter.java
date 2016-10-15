/**
 * 
 */
package v9t9.video.imageimport;

import java.awt.image.BufferedImage;
import java.util.List;

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
		super(7, colorMgr, useColorMappedGreyScale, mapColor);
		
		black = mapColor.getClosestPaletteEntry(0x000000); // 0
		white = mapColor.getClosestPaletteEntry(0xffffff); // 15
		violet = mapColor.getClosestPaletteEntry(0xd93cf0); // 3
		red = mapColor.getClosestPaletteEntry(0xd9680f); // 9
		blue = mapColor.getClosestPaletteEntry(0x2697f0); // 6
		green = mapColor.getClosestPaletteEntry(0x26c30f); // 12

		blackPixel = mapColor.getPalettePixel(black);
		whitePixel = mapColor.getPalettePixel(white);
		violetPixel = mapColor.getPalettePixel(violet);
		redPixel = mapColor.getPalettePixel(red);
		bluePixel = mapColor.getPalettePixel(blue);
		greenPixel = mapColor.getPalettePixel(green);

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
					
					int newPixel;
					
					// see if we want black or white
					int color = mapColor.getClosestPaletteEntry(origPixel);
					if (color == black) {
						newPixel = blackPixel;
					}
					else if (color == white) {
						newPixel = whitePixel;
					}
					else {
						int colPixel = (xd&1)== 0 ? even : odd;
						int colDist = useColorMappedGreyScale 
								? ColorMapUtils.getPixelLumDistance(origPixel, colPixel) 
								: ColorMapUtils.getPixelDistance(origPixel, colPixel);
						int blackDist = useColorMappedGreyScale 
								? ColorMapUtils.getPixelLumDistance(origPixel, blackPixel)
								: ColorMapUtils.getPixelDistance(origPixel, blackPixel);
						int whiteDist = useColorMappedGreyScale 
								? ColorMapUtils.getPixelLumDistance(origPixel, whitePixel)
								: ColorMapUtils.getPixelDistance(origPixel, whitePixel);
						
						if (blackDist < colDist) {
							newPixel = blackPixel;
						}
						else if (whiteDist < colDist) {
							newPixel = whitePixel;
						}
						else {
							newPixel = colPixel;
						}
					}
					
					convertedImage.setRGB(xd + xoffs, y + yoffs, newPixel);
					
					// If we are setting a white pixel, then any non-black and non-white 
					// pixel to the left will be rendered white as well.  Force that one to
					// black or white.
					if (newPixel != blackPixel && xd + xoffs > 0) {
						int leftPixel = convertedImage.getRGB(xd + xoffs - 1, y + yoffs);
						if (leftPixel != blackPixel) {
							// oops, white or color will force the new pixel white,
							// unless the old or new one become black
							if (leftPixel == whitePixel) {
								int blackDist = useColorMappedGreyScale 
									? ColorMapUtils.getPixelLumDistance(newPixel, blackPixel)
									: ColorMapUtils.getPixelDistance(newPixel, blackPixel);
								int whiteDist = useColorMappedGreyScale 
									? ColorMapUtils.getPixelLumDistance(newPixel, whitePixel)
									: ColorMapUtils.getPixelDistance(newPixel, whitePixel);

								if (whiteDist < blackDist) {
									// ok, current pixel is more white too 
									convertedImage.setRGB(xd + xoffs, y + yoffs, whitePixel);
								} else {
									// change the other to black to preserve color 
									convertedImage.setRGB(xd + xoffs, y + yoffs, blackPixel);
								}
							} else if (newPixel != whitePixel) {
								convertedImage.setRGB(xd + xoffs - 1, y + yoffs, blackPixel);
							}
						}
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
