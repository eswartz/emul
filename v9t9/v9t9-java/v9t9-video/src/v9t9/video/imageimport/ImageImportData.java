/*
  ImageImportData.java

  (c) 2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
