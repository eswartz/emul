/**
 * 
 */
package org.ejs.v9t9.forthcomp;

import java.util.LinkedHashMap;
import java.util.Stack;

/**
 * @author ejs
 *
 */
public class Context implements IContext {
	private LinkedHashMap<String, IWord> dictionary;
	private IWord latest;

	/**
	 * 
	 */
	public Context() {
		dictionary = new LinkedHashMap<String, IWord>();
	}
	/**
	 * @param string
	 * @param base
	 */
	public IWord define(String string, IWord word) {
		dictionary.put(string.toLowerCase(), word);
		latest = word;
		return word;
	}
	
	/**
	 * @return the latest
	 */
	public IWord getLatest() {
		return latest;
	}
	/**
	 * @param token
	 * @return
	 */
	public IWord find(String token) {
		return dictionary.get(token.toLowerCase());
	}
	
}
