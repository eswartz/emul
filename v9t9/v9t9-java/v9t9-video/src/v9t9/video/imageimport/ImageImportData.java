/**
 * 
 */
package v9t9.video.imageimport;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
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

	public int delayMs;

	/**
	 * 
	 */
	public ImageImportData(BufferedImage converted, byte[][] thePalette, Map<Integer, Integer> paletteToIndex) {
		this.converted = converted;
		this.thePalette = new byte[thePalette.length][];
		for (int  i = 0; i < thePalette.length; i++)
			this.thePalette[i] = Arrays.copyOf(thePalette[i], thePalette[i].length);
		this.paletteToIndex = new HashMap<Integer, Integer>(paletteToIndex);
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

	/**
	 * @return
	 */
	public Map<Integer, Integer> getPaletteToIndex() {
		return paletteToIndex;
	}
}
