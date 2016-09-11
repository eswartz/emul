/**
 * 
 */
package org.ejs.gui.images;

import java.util.TreeMap;

/**
 * @author ejs
 *
 */
public interface IDirectColorMapper extends IPaletteMapper {

	void mapDirectColor(byte[] rgb, byte c);
	
}
