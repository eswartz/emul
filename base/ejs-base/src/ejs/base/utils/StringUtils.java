/*
  StringUtils.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.utils;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ejs
 *
 */
public class StringUtils {

	public static String catenate(Object[] items, String sep) {
		StringBuilder sb = new StringBuilder();
		for (Object it : items) {
			if (sb.length() > 0)
				sb.append(sep);
			sb.append(it);
		}
		return sb.toString();
	}
	public static String catenate(byte[] items, String sep) {
		StringBuilder sb = new StringBuilder();
		for (byte it : items) {
			if (sb.length() > 0)
				sb.append(sep);
			sb.append(HexUtils.toHex2(it));
		}
		return sb.toString();
	}
	

	public static String catenate(Collection<?> items, String sep) {
		StringBuilder sb = new StringBuilder();
		for (Object it : items) {
			if (sb.length() > 0)
				sb.append(sep);
			sb.append(it);
		}
		return sb.toString();
	}
	
	/**
	 * Convert a camelcased string to a label
	 * @param name
	 * @return
	 */
	public static String getTitleCasedPartsString(String name) {
		String[] parts = name.split("\\s+");
		for (int i = 0; i < parts.length; i++) 
			if (parts[i].length() > 0)
				parts[i] = getTitleCasedString(parts[i]);
		return StringUtils.catenate(parts, " ");
	}


	public static String getTitleCasedString(String part) {
		return Character.toUpperCase(part.charAt(0)) + part.substring(1);
	}


	/**
	 * Get a string where the initial run of capital letters in each space-separated
	 * segment is lowercased
	 * @param replace
	 * @return
	 */
	public static String getLowerCasedPartsString(String name) {
		String[] parts = name.split("\\s+");
		for (int i = 0; i < parts.length; i++) 
			if (parts[i].length() > 0)
				parts[i] = getLowerCasedString(parts[i]);
		return StringUtils.catenate(parts, " ");
	}

	private static Pattern initialsToLower = Pattern.compile(
			"([A-Z]+)(.*)");
	/**
	 * Get a string where the initial run of capital letters is lowercased
	 * @param part
	 * @return
	 */
	public static String getLowerCasedString(String part) {
		Matcher m = initialsToLower.matcher(part);
		if (m.matches()) {
			return m.group(1).toLowerCase() + m.group(2);
		}
		return part;
	}


}
