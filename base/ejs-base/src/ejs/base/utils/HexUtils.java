/*
  HexUtils.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.utils;

/**
 * @author ejs
 *
 */
public class HexUtils {

	/**
	 * @param string
	 * @return
	 */
	public static String padAddress(String string) {
	    final byte[] zeroes = { '0', '0', '0', '0' };
	    int len = 4 - string.length();
	    if (len > 0) {
			return new String(zeroes, 0, len) + string;
		} else {
			return string;
		}
	}

	public static String padByte(String string) {
		final byte[] zeroes = { '0', '0' };
		int len = 2 - string.length();
		if (len > 0) {
			return new String(zeroes, 0, len) + string;
		} else {
			return string;
		}
	}

	public static String toHex4(int value) {
		if (value < 0)
			value &= 0xffff;
	    return padAddress(Integer.toHexString(value & 0x7fffffff).toUpperCase());
	}

	/**
	 * Parse an integer which may be in C-style hex notation (0xF00).
	 * Otherwise it's interpreted as decimal.
	 * @param string
	 * @return
	 */
	public static int parseInt(String string) {
	    string = string.toUpperCase();
	    if (string.length() > 2 && string.charAt(0) == '0'
	        && string.charAt(1) == 'X') {
	        return Integer.parseInt(string.substring(2), 16);
	    }
	    return Integer.parseInt(string);
	}

	public static String padString(String string, int size) {
		StringBuilder builder = new StringBuilder();
		builder.append(string);
		while (builder.length() < size)
			builder.append(' ');
		return builder.toString();
	}

	public static String toHex2(int value) {
		return padByte(Integer.toHexString(value & 0xff).toUpperCase());
	}

}
