/*
  PrefUtils.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.utils;

import org.eclipse.jface.dialogs.IDialogSettings;





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
}
