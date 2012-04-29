/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package org.ejs.gui.common;


import org.eclipse.swt.graphics.Rectangle;




/** 
 * @author ejs
 */
public class SwtPrefUtils  {
	public static Rectangle readBoundsString(String boundsStr) {
		Rectangle savedBounds = null;
		if (boundsStr == null)
			return null;
		String[] parts = boundsStr.split("\\|");
		if (parts.length == 4) {
			try {
				savedBounds = new Rectangle(
						Integer.parseInt(parts[0]),
						Integer.parseInt(parts[1]),
						Integer.parseInt(parts[2]),
						Integer.parseInt(parts[3]));
				
			} catch (ArrayIndexOutOfBoundsException e) {
				
			} catch (NumberFormatException e) {
				
			}
		}
		return savedBounds;
	}

	public static String writeBoundsString(Rectangle bounds) {
		if (bounds == null)
			return "";
		String boundsStr = bounds.x + "|" + bounds.y + "|" + bounds.width + "|" + bounds.height;
		return boundsStr;
	}

}
