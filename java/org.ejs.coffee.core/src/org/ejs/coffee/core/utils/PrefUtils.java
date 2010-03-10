/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 17, 2004
 *
 */
package org.ejs.coffee.core.utils;


import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.graphics.Rectangle;



/** Global settings for the emulator as a whole.
 * @author ejs
 */
public class PrefUtils  {
    /**
	 * Read an integer without a superfluous NumberFormatException from
	 * a missing key.
	 * @param section
	 * @param key
	 * @return
	 */
	public static int readSavedInt(IDialogSettings section, String key) {
		if (section == null)
			return 0;
		String value = section.get(key);
		if (value == null) {
			return 0;
		} else {
			try {
				return HexUtils.parseInt(value);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return 0;
			}
		}
	}
	
	/**
	 * Read a boolean without a superfluous NumberFormatException from
	 * a missing key.

	 * @param section
	 * @param key
	 * @param defaultIfUndefined
	 * @return setting
	 */
	public static boolean readSavedBoolean(IDialogSettings section, String key, boolean defaultIfUndefined) {
		if (section == null)
			return false;
		String value = section.get(key);
		if (value == null) {
			return defaultIfUndefined;
		} else {
			try {
				return Boolean.parseBoolean(value);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return defaultIfUndefined;
			}
		}
	}
	public static boolean readSavedBoolean(IDialogSettings section, String key) {
		return readSavedBoolean(section, key, false);
	}
	
	public static Rectangle readBoundsString(String boundsStr) {
		Rectangle savedBounds = null;
		if (boundsStr == null)
			return null;
		String[] parts = boundsStr.split("\\|");
		try {
			savedBounds = new Rectangle(
					Integer.parseInt(parts[0]),
					Integer.parseInt(parts[1]),
					Integer.parseInt(parts[2]),
					Integer.parseInt(parts[3]));
			
		} catch (ArrayIndexOutOfBoundsException e) {
			
		} catch (NumberFormatException e) {
			
		}
		return savedBounds;
	}

	public static String writeBoundsString(Rectangle bounds) {
		String boundsStr = bounds.x + "|" + bounds.y + "|" + bounds.width + "|" + bounds.height;
		return boundsStr;
	}

}
