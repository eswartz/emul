/**
 * 
 */
package v9t9.video.imageimport;

import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * This is the data resulting from an image import operation.
 * @author ejs
 *
 */
public class ImageImportData {

	private BufferedImage converted;

	protected byte[][] thePalette;
	
	/** mapping from RGB-32 pixel to each palette index */
	protected Map<Integer, Integer> paletteToIndex;

	private BufferedImage scaledImage;

	/**
	 * 
	 */
	public ImageImportData(BufferedImage scaled, BufferedImage converted, byte[][] thePalette, Map<Integer, Integer> paletteToIndex) {
		this.scaledImage = scaled;
		this.converted = converted;
		this.thePalette = thePalette;
		this.paletteToIndex = paletteToIndex;
	}
	
	/**
	 * @return the thePalette
	 */
	public byte[][] getThePalette() {
		return thePalette;
	}
	
	public BufferedImage getConvertedImage() {
		return converted;
	}
	
	public byte getPixel(int x, int y) {
		
		int p;
		if (x < converted.getWidth() && y < converted.getHeight())
			p = converted.getRGB(x, y) & 0xffffff;
		else
			p = 0;
		
		Integer c = paletteToIndex.get(p);
//		if (c == null && format == VdpFormat.COLOR256_1x1) {
//			c = paletteToIndex.get(paletteToIndex.ceilingKey(p));	// should be fixed now
//		}
		if (c == null) {
			return 0;
		}
		return (byte) (int) c;
	}

	/**
	 * @return
	 */
	public Map<Integer, Integer> getPaletteToIndex() {
		return paletteToIndex;
	}

	/**
	 * @return
	 */
	public BufferedImage getScaledImage() {
		return scaledImage;
	}
}
