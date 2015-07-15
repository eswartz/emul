/*
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of the License "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description: 
*
*/
package ejs.base.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text handling utilities
 *
 */
public class TextUtils {

	/** Pattern describing a run of illegal identifier characters */
    private static final Pattern illegalIdentifierCharsPattern = Pattern.compile("[^A-Za-z0-9_]+"); //$NON-NLS-1$
    
	/**
	 * Return true if text is null or zero-length
	 */
	public static boolean isEmpty(String text) {
		return text == null || text.length() == 0;
	}
	
	/**
	 * Return the given text or an empty string, never null
	 */
	public static String safeStr(String text) {
		return text != null? text : "";
	}
	
	/**
	 * Return string length or zero for null string
	 * @param text
	 * @return length, or 0 
	 */
	public static int strlen(String text) {
		return text != null? text.length() : 0;
	}
	
	/**
	 * Strip the rightmost extension from a file name,
	 * e.g. foo.a.b -> foo.a.
	 * If the string is an extension only, e.g. .foo then
	 * nothing is stripped.
	 * @param text
	 * @return new string, without trailing period
	 */
	public static String stripExtension(String text) {
		String result = text;
		if (text != null) {
			int pos = text.lastIndexOf('.');
			if (pos >= 1) {
				result = text.substring(0, pos);
			}
		}
		return result;
	}
	
	/**
	 * Get the rightmost extension from a file name.
	 * If the string is an extension only, e.g. .foo then
	 * the whole string is returned. If there's no '.' then
	 * null is returned
	 * @param text
	 * @return the extension, without a period
	 */
	public static String getExtension(String text) {
		String result = null;
		if (text != null) {
			int pos = text.lastIndexOf('.');
			if (pos >= 0) {
				result = text.substring(pos+1);
			}
		}
		return result;
	}
	
    /**
     * Clean up text from an XML node.  Removes leading and trailing
     * text and converts all runs of whitespace to single spaces.  
     * 
     * @param text input string
     * @return cleaned-up string
     */
    public static String cleanUpXMLText(String text) {
    	if (text == null)
    		return null;
        // map embedded whitespace to space characters
        Pattern patt = Pattern.compile("\\s+", Pattern.MULTILINE); //$NON-NLS-1$
        Matcher matcher = patt.matcher(text);
        text = matcher.replaceAll(" "); //$NON-NLS-1$
        // remove leading & trailing whitespace
        return text.trim();
    }
    
    /**
     * Parse a string into an integer, returning a default
     * value instead of throwing an exception for invalid strings.
     */
    public static int parseInt(String s, int defaultValue) {
    	int result = defaultValue;
    	// avoid spurious IllegalArgumentException
    	if (s != null) { 
    		try {
	    		result = Integer.parseInt(s);
	    	}
	    	catch (NumberFormatException x) {
	    	}
    	}
    	return result;
    }
    
    /**
     * Parse a string into a float, returning a default
     * value instead of throwing an exception for invalid strings.
     */
  public static float parseFloat(String s, float defaultValue) {
    	float result = defaultValue;
    	try {
    		result = Float.parseFloat(s);
    	}
    	catch (NumberFormatException x) {
    	}
    	return result;
    }

    /**
     * Escape all the characters in the string which might be
     * used in regular expressions
     * @param text
     * @return escaped text
     */
    public static String regexEscape(String text) {
        StringBuffer buffer = new StringBuffer();
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if ("[{()}]\\+*^$|".indexOf(chars[i]) >= 0) { //$NON-NLS-1$
                buffer.append('\\');
            }
            buffer.append(chars[i]);
        }
        return buffer.toString();
    }

    /**
     * Change all the newlines to "\n"
     * @param text
     * @return text with canonical newlines
     */
    public static String canonicalizeNewlines(String text) {
    	return canonicalizeNewlines(text, "\n"); //$NON-NLS-1$
    }

    /**
     * Change all the newlines to the given eol sequence
     * @param text
     * @param eol
     * @return text with canonical newlines
     */
    public static String canonicalizeNewlines(String text, String eol) {
        Matcher matcher = ANY_NEWLINE_MATCHING_PATTERN.matcher(text);
        return matcher.replaceAll(eol); //$NON-NLS-1$
    }

    static final Pattern patternUnixNewlineMatch = Pattern.compile("\n"); //$NON-NLS-1$
    static final Pattern patternUnixNewlinesMatch = Pattern.compile("\n+"); //$NON-NLS-1$
	public static final String LINE_ENDING_PATTERN_STRING = "(\r\n|\r|\n)"; //$NON-NLS-1$
	/** Pattern which matches a single instance of any newline pattern. */
	public static final Pattern ANY_NEWLINE_MATCHING_PATTERN = Pattern.compile(LINE_ENDING_PATTERN_STRING); //$NON-NLS-1$
    

    /**
     * Return a pattern which matches the given literal text,
     * except that the particular newline in use (represented by '\n' in the
     * literal) can match any newline style in the target.
     * @param lit
     * @param newlineRuns if true, match >=1 newlines for each \n, else match
     * only the exact number provided 
     * @return pattern
     */
    public static Pattern getNewlineIndependentPattern(String lit, boolean newlineRuns) {
        String rxLit = regexEscape(lit);
        Matcher matcher = patternUnixNewlineMatch.matcher(rxLit);
        String repl = LINE_ENDING_PATTERN_STRING;
        rxLit = matcher.replaceAll(newlineRuns ? repl + "+" : repl); //$NON-NLS-1$
        return Pattern.compile(rxLit);
    }

    /**
     * Return the string in Titlecase, i.e. the first character is capitalized.
     * If the first character is not a letter, there is no change.
     * @param name
     * @return titlecased string
     */
    public static String titleCase(String name) {
        if (name.length() == 0)
            return name;
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Return the string in inverse titlecase, i.e. the first character is lowercased.
     * If the first character is not a letter, there is no change.
     * @param name
     * @return inverse titlecased string
     */
    public static String inverseTitleCase(String name) {
    	if (name.length() == 0)
    		return name;
    	return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
    
    /**
     * Tell if the given name is a legal identifier, according to the
     * common rules that it must start with an letter or underscore
     * and be followed by alphanumeric characters or underscores.
     * @param name
     * @return true: legal
     */
    public static boolean isLegalIdentifier(String name) {
    	return !illegalIdentifierCharsPattern.matcher(name).find();
    }


    /**
     * Return a version of the name with all the runs of illegal
     * characters changed to underscores.
     * @param name
     * @return identifier-legal string
     */
    public static String legalizeIdentifier(String name) {
        // replace runs of non-identifier characters with "_";
        Matcher matcher = illegalIdentifierCharsPattern.matcher(name);
        name = matcher.replaceAll("_"); //$NON-NLS-1$
        if (name.length() > 0 && Character.isDigit(name.charAt(0)))
            return "_" + name; //$NON-NLS-1$
        else
            return name;
    }

    /**
     * Escape a string 'val' into C/C++ style, doubling
     * backslashes and escaping the quote character.
     * @param val incoming "pure" string
     * @param quote the quote character
     * @return the string with interesting characters escaped
     */
    static public String escape(String val, char quote) {
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < val.length(); i++) {
            char ch = val.charAt(i);
            if (ch == quote || ch == '\\') {
                buff.append('\\');
                buff.append(ch);
            }
            else if (ch < 32) {
                switch (ch) {
                case '\t':
                    buff.append("\\t"); //$NON-NLS-1$
                    break;
                case '\n':
                    buff.append("\\n"); //$NON-NLS-1$
                    break;
                case '\r':
                    buff.append("\\r"); //$NON-NLS-1$
                    break;
                case '\f':
                    buff.append("\\f"); //$NON-NLS-1$
                    break;
                case '\b':
                    buff.append("\\b"); //$NON-NLS-1$
                    break;
                default:
                    buff.append("\\");
                	buff.append(String.format("%03o", Integer.valueOf(ch)));
                    break;
                }
            } else if (isLineOrParaSeparator(ch)) {
            	buff.append("\\u"); //$NON-NLS-1$
            	buff.append(Integer.toHexString(ch));
            } else
                buff.append(ch);
        }
        return buff.toString();
    }

    private static boolean isLineOrParaSeparator(char ch) {
		int type = Character.getType(ch);
		return ch > 127 && 
			(type == Character.PARAGRAPH_SEPARATOR || type == Character.LINE_SEPARATOR);
	}

	/**
     * Quote a string, escaping it into C/C++ style
     * @param val incoming "pure" string
     * @param quote the quote character
     * @return an escaped string surrounded with quotes. 
     */
    static public String quote(String val, char quote) {
        StringBuffer buff = new StringBuffer();
        buff.append(quote);
        buff.append(escape(val, quote));
        buff.append(quote);
        return buff.toString();
    }

     /**
     * Unescape escaped chars in a string 'val'.
     * 
     */
    static public String unescape(String val, char quote) {
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < val.length(); i++) {
            char ch = val.charAt(i);
            if (ch == '\\' && i + 1 < val.length()) {
                ch = val.charAt(++i);
                if (Character.digit(ch, 8) >= 0 && i + 2 < val.length()) {
                	String code = val.substring(i, i+3);
                	i += 2;
                	try {
                		int octal = Integer.parseInt(code, 8);
                		buff.append((char) octal);
                	} catch (NumberFormatException x) {
//                		UtilsPlugin.log(x);
                	}
                } else {
	                switch (ch) {
	                case 't':
	                    buff.append('\t');
	                    break;
	                case 'n':
	                    buff.append('\n');
	                    break;
	                case 'r':
	                    buff.append('\r');
	                    break;
	                case 'f':
	                    buff.append('\f');
	                    break;
	                case 'b':
	                    buff.append('\b');
	                    break;
	                case 'u':
	                	try {
	                		String code = val.substring(i+1, i+5);
	                		buff.append((char) Integer.parseInt(code, 16));
	                		i+=4;
	                	} catch (NumberFormatException e) {
	                		buff.append('\\');
	                		buff.append('u');
                		}
	                	break;
	                case '\\':
	                    buff.append('\\');
	                    break;
	                case '"':
	                	buff.append('"');
	                	break;
	                case '\'':
	                	buff.append('\'');
	                	break;
	                default:
	                    buff.append('\\');
	                    buff.append(ch);
	                    break;
	                }
                }
            } else {
                buff.append(ch);
            }
        }
        return buff.toString();
    }

    /**
     * Remove any surrounding quotes from a string
     * @param string
     * @param quote quoting character
     * @return updated string
     */
    public static String unquote(String string, char quote) {
        if (string.length() >= 2 
                && string.charAt(0) == quote
                && string.charAt(string.length() - 1) == quote)
        	//return unescape(string.substring(1, string.length() - 1), quote);
        	return string.substring(1, string.length() - 1);
        else
            return string;
    }
    
    /**
     * Escape quoted strings in text 'val', doubling backslashes.
     * @param val incoming string
     * @param quote the quote character
     * @return the string with escapes inside quoted strings double-escaped
     */
    static public String escapeStrings(String val, char quote) {
        StringBuffer buff = new StringBuffer();
        boolean inQuote = false;
        for (int i = 0; i < val.length(); i++) {
            char ch = val.charAt(i);
            if (ch == quote) {
            	// changing quote state
            	inQuote = true;
            	buff.append(ch);
            } else if (inQuote) {
            	// handle escape chars inside string
            	if (ch == '\\') {
            		buff.append('\\');
            		buff.append(ch);
            		if (i + 1 < val.length() && val.charAt(i+1) == quote) {
            			// if quote is already escaped, pass it through (don't toggle quote state)
            			buff.append(quote);
            			i++;
            		}
            	} else {
            		buff.append(ch);
            	}
            } else {
            	// not quoted
            	buff.append(ch);
            }
        }
        return buff.toString();
    }

	/**
	 * Format a string for dumping to console, e.g.
	 * by adding a newline whereever a newline escape appears.
	 * @param string
	 * @return formatted string
	 */
	public static String formatForDump(String string) {
		String[] lines = string.split("\\n"); //$NON-NLS-1$
		StringBuilder builder = new StringBuilder();
		boolean hadLine = false;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].length() > 0) {
				if (hadLine)
					builder.append('\n');
				builder.append(lines[i]);
				builder.append("\\n"); //$NON-NLS-1$
				hadLine = true;
			}
		}
		return builder.toString();
	}
	
	/**
	 * Return a string equivalent to the input, but with
	 * all illegal XML characters escaped.
	 */
	public static String escapeXML(String s) {
		StringBuffer result = new StringBuffer();
		int len = s != null? s.length() : 0;
		for (int i = 0; i < len; i++) {
			char ch = s.charAt(i);
			switch (ch) {
			case '&':
				result.append("&amp;"); //$NON-NLS-1$
				break;
			case '"':
				result.append("&quot;"); //$NON-NLS-1$
				break;
			case '\'':
				result.append("&apos;"); //$NON-NLS-1$
				break;
			case '<':
				result.append("&lt;"); //$NON-NLS-1$
				break;
			case '>':
				result.append("&gt;"); //$NON-NLS-1$
				break;
			default:
				result.append(ch);
			}
		}
		return result.toString();
	}

	/**
	 * Format a list of items into a text string that can be displayed 
	 * in a dialog.
	 * @param messages
	 * @return string with embedded tabs and newlines
	 */
	public static String formatTabbedList(Collection<?> items) {
		StringBuilder builder = new StringBuilder();
		for (Iterator<?> iter = items.iterator(); iter.hasNext();) {
			String item = iter.next().toString();
			builder.append('\t');
			builder.append(item);
			builder.append('\n');
		}
		return builder.toString();
	}
	
	/**
	 * Title case every word in a sentence, except for conjunctions
	 * @param string
	 * @return String Titlecased Like This
	 */
	public static String titleCaseSentence(String string) {
		Pattern conjunction = Pattern.compile("(in|to|of|and|or|but|if|as)");
		Pattern pattern = Pattern.compile("\\b");
		String[] pieces = pattern.split(string);
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < pieces.length; i++) {
			if (!conjunction.matcher(pieces[i]).matches())
				pieces[i] = titleCase(pieces[i]);
			builder.append(pieces[i]);
		}
		return builder.toString();
	}
	
	/**
	 * @param string input string with '\' + return breaking lines
	 * @return string with '\' + return breaks removed
	 */
	public static String catenateBrokenLines(String string) {
		if (string == null)
			return null;
		return string.replaceAll("\\\\" + LINE_ENDING_PATTERN_STRING, "");
	}
	
	/**
	 * Catenate all the Object.toStrings() together, with an optional string in between.
	 */
	public static String catenateStrings(Object[] objects, String separator) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (Object o : objects) {
			if (first)
				first = false;
			else if (separator != null)
				builder.append(separator);
			builder.append(o);
		}
		return builder.toString();
	}
	/**
	 * Catenate all the Object.toStrings() together, with an optional string in between.
	 */
	public static String catenateStrings(Collection<?> list, String separator) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (Object o : list) {
			if (first)
				first = false;
			else if (separator != null)
				builder.append(separator);
			builder.append(o);
		}
		return builder.toString();
	}

	/**
	 * Create a reasonable human-readable label from an identifier.
	 * @param name
	 * @return
	 */
	public static String createLabelFromIdentifier(String name) {
		if (name == null)
			return "";
		
		// strip qualifier and dotted prefix
		int idx;
		idx = name.indexOf(':');
		if (idx >= 0)
			name = name.substring(0, idx);
		
		idx = name.lastIndexOf('.');
		if (idx >= 0)
			name = name.substring(idx + 1);
		
		if (name.isEmpty())
			return "";
		
		StringBuilder label = new StringBuilder();
		StringBuilder word = new StringBuilder();
		char lastCh = 0;
		for (char ch : name.toCharArray()) {
			if (ch == '_') {
				label.append(fixupWordCapitalization(word.toString(), label.length() == 0));
				word.setLength(0);
				label.append(' ');
				lastCh = ch;
				continue;
			} else if (lastCh != 0 && Character.isUpperCase(ch) && !Character.isUpperCase(lastCh)) {
				label.append(fixupWordCapitalization(word.toString(), label.length() == 0));
				word.setLength(0);
				label.append(' ');
			}
			word.append(Character.toLowerCase(ch));
			
			lastCh = ch;
		}
		label.append(fixupWordCapitalization(word.toString(), label.length() == 0));

		return label.toString();
	}

	/**
	 * @param string
	 * @return
	 */
	private static Object fixupWordCapitalization(String string, boolean first) {
		if (string.matches("if|of|the|a|an"))
			return string;
		
		// acronyms
		boolean plural = false;
		if (string.endsWith("s")) {
			string = string.substring(0, string.length() - 1);
			plural = true;
		}
		if (string.matches("(?i)uri|url|xml")) {
			string = string.toUpperCase();
		} else {
			string = titleCase(string);
		}
		if (plural) {
			return string + "s";
		}
		return string;
	}

	/**
	 * @param string
	 * @param count
	 * @return
	 */
	public static String repeatString(String string, int count) {
		if (count == 0)
			return "";
		else if (count == 1)
			return string;
		StringBuilder sb = new StringBuilder();
		while (count-- > 0)
			sb.append(string);
		return sb.toString();
	}
	
	public static String binaryToString(byte[] content) {
		return binaryToString(content, 0, content.length);
	}
	public static String binaryToString(byte[] content, int offset, int length) {
		StringBuilder sb = new StringBuilder();
		for (int o = 0; o < length; o++) {
			sb.append(HexUtils.toHex2(content[offset + o]));
		}
		return sb.toString();
	}
}
