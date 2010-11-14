/**
 * 
 */
package v9t9.forthcomp;

import java.util.LinkedHashMap;
import java.util.Map;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.IContext;
import v9t9.forthcomp.IWord;

import v9t9.forthcomp.words.BaseWord;

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
		if (word instanceof BaseWord)
			((BaseWord) word).setName(string);
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
	
	public Map<String, IWord> getDictionary() {
		return dictionary;
	}
}
