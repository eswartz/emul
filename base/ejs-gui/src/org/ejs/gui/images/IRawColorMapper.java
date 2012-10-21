/**
 * 
 */
package org.ejs.gui.images;

/**
 * @author ejs
 *
 */
public interface IRawColorMapper {

	int getDistance(int rgb1, int rgb2);
	int getRGBPixel(int rgb);
}
