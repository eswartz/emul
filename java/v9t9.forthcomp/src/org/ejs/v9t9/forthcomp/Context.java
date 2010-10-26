/**
 * 
 */
package org.ejs.v9t9.forthcomp;

import java.util.LinkedHashMap;

/**
 * @author ejs
 *
 */
public class Context implements IContext {
	protected LinkedHashMap<String, IWord> dictionary;
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
	
	public IWord require(String token) throws AbortException {
		IWord word = find(token);
		if (word == null)
			throw new AbortException("no word " + token);
		return word;
	}

	/**
	 * 
	 */
	public void clearDict() {
		dictionary.clear();
		latest = null;
	}
	
}
