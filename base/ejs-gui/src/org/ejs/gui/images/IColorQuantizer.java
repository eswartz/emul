/**
 * 
 */
package org.ejs.gui.images;

import java.util.List;

/**
 * @author ejs
 *
 */
public interface IColorQuantizer {

	public interface ILeaf {
		int[] reprRGB();
		int getPixelCount();
	}
	
	public void addColor(int pixel, int[] prgb);
	public int getLeafCount();
	public void reduceColors(int maxLeafCount);
	public List<ILeaf> gatherLeaves();
}
