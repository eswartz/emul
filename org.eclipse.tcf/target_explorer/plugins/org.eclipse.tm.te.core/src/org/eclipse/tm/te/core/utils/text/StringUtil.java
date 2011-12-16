/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.utils.text;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;

/**
 * A class providing useful static method to manipulate strings.
 */
public final class StringUtil {

	/**
	 * Tokenize a list of whitespace-separated arguments into a list.
	 * <p>
	 * <b>Note:</b> <i>This method has been designed for the specific needs of tokenizing
	 * command line arguments to launch external application from Java!</i>
	 * <p>
	 * Arguments may be quoted by double quotes. Quotes must not appear
	 * inside words, or they will lead to new tokens.
	 * <p>
	 * Example:<pre><code>    a"bc"d   -->  tokenized into a,bc,d</code></pre>
	 * <p>
	 * If <code>maxArgs</code> is greater than 0, then a maximum of <code>maxArgs</code>
	 * tokens is returned and the resulting array is filled to the given number of arguments
	 * with empty strings.
	 * <p>
	 * If <code>maxArgs</code> is less than or equal 0, the original number of arguments is tokenized.
	 *
	 * @param arguments The space separated arguments string. Must not be <code>null</code>.
	 * @param maxArgs The maximum number of returned tokens or <code>0</code>.
	 * @param keepQuotes If <code>true</code>, the original arguments quotes are retained, <code>false</code> otherwise.
	 *
	 * @return The tokenized string or an empty list.
	 */
	public static String[] tokenize(String arguments, int maxArgs, boolean keepQuotes) {
		Assert.isNotNull(arguments);

		// Create the result list
		List<String> result = maxArgs > 0 ? new ArrayList<String>(maxArgs) : new ArrayList<String>();

		// Arguments sent separately
		StreamTokenizer tok = new StreamTokenizer(new StringReader(arguments));
		tok.resetSyntax();
		// whitespace is everything from 0 to 32 (space)
		tok.whitespaceChars(0, 32);
		// everything from 33 to 255 is treated as word character
		tok.wordChars(33, 255);
		// except the 0xa0, is an whitespace too.
		tok.whitespaceChars(0xa0, 0xa0);
		// the quoting character is the double-quote
		tok.quoteChar('"');

		// extract only the number of arguments request or unlimited if maxArgs == 0
		int nArgs = 0;
		while (maxArgs <= 0 || nArgs < maxArgs) {
			try {
				// get the next token from the stream
				int ttype = tok.nextToken();
				// if reached end of file, leave the loop
				if (ttype == StreamTokenizer.TT_EOF) {
					break;
				}

				if (keepQuotes && ttype == 34) { //quoted word
					String quoted = enQuote(tok.sval);
					if (quoted.length() < 2 || quoted.charAt(0) != '"' ||
						quoted.charAt(quoted.length() - 1) != '"') {
						quoted = '"' + quoted + '"';
					}
					result.add(quoted);
				}
				else {
					result.add(tok.sval);
				}
				nArgs++;
			}
			catch (IOException e) {
				// on any IO exception, break the loop
				break;
			}
		}
		return result.toArray(new String[result.size()]);
	}

	private final static Pattern ALLOWED_STRING_PATTERN = Pattern.compile("[a-zA-Z0-9_@.-]*"); //$NON-NLS-1$

	/**
	 * Enquote the given string if necessary by putting double quotes around it.
	 * <p>
	 * <b>Note:</b> <i>This method has been designed for the specific needs of enquoting
	 * command line arguments to launch external application from Java!</i>
	 * <p>
	 * Characters that require quoting are:
	 * <dl>
	 *   <dt>/\:;</dt><dd>Because they are file or path separators respectively</dd>
	 *   <dt>\s</dt><dd>All whitespace characters, naturally</dd>
	 *   <dt>|<>{}()$^~</dt><dd>Because they have shell or TCL special meaning</dd>
	 *   <dt>*%?&</dt><dd>Because they are commonly used as wildcard's</dd>
	 *   <dt>#</dt><dd>Because it is commonly used to start comments</dd>
	 *   <dt>'"`</dt><dd>Because they are quoting characters</dd>
	 * </dl>
	 * To make a long story short, we only allow ASCII characters, numbers,
	 * the underscore and <code>@.-</code> to go unquoted.
	 *
	 * @param unqouted The string to quote or <code>null</code>.
	 *
	 * @return The quoted string or "\"\"".
	 */
	public static String enQuote(String unqouted) {
		if (unqouted == null) {
			return "\"\""; //$NON-NLS-1$
		}
		else if (unqouted.length() == 0) {
			return "\"\""; //$NON-NLS-1$
		}
		else if (ALLOWED_STRING_PATTERN.matcher(unqouted).matches()) {
			return unqouted;
		}
		else {
			StringReader r = new StringReader(unqouted);
			StringBuffer buf = new StringBuffer(unqouted.length() + 16);
			boolean containsWhitespaces =
				Pattern.compile("\\s").matcher(unqouted).find(); //$NON-NLS-1$
			if (containsWhitespaces) {
				buf.append('\"');
			}
			try {
				int c = r.read();
				while (c >= 0) {
					switch (c) {
						// case '\\':
						case '\'':
						case '\"':
							buf.append('\\');
							buf.append((char)c);
							break;
						case '\b':
							buf.append("\\b");break; //$NON-NLS-1$
						case '\f':
							buf.append("\\f");break; //$NON-NLS-1$
						case '\n':
							buf.append("\\n");break; //$NON-NLS-1$
						case '\r':
							buf.append("\\r");break; //$NON-NLS-1$
						case '\t':
							buf.append("\\t");break; //$NON-NLS-1$
						default:
							if (c > 0xff) { // Unicode
								buf.append('\\');
								buf.append('u');
								String hexString = Integer.toHexString(c);
								if (hexString.length() < 4) {
									buf.append('0');
								}
								buf.append(hexString);
							}
							else if (c < 0x20 || c > '~') {
								// octal escape sequence
								buf.append('\\');
								buf.append(Integer.toOctalString(c));
							}
							else {
								buf.append((char)c);
							}
					}
					c = r.read();
				}
			}
			catch (IOException e) { /* ignore */
			}
			r.close();
			if (containsWhitespaces) {
				buf.append('\"');
			}
			return buf.toString();
		}
	}

}
