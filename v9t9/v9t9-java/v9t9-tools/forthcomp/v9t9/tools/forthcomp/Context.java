/*
  Context.java

  (c) 2010-2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp;

import java.util.LinkedHashMap;
import java.util.Map;

import v9t9.tools.forthcomp.words.BaseWord;

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
	

	public void redefine(IWord word, IWord newWord) {
		getDictionary().put(word.getName().toLowerCase(), newWord);
	}

	
	public void undefine(String string) throws AbortException {
		IWord word = dictionary.get(string.toLowerCase());
		if (word != null) {
			if (word instanceof ITargetWord)
				throw new AbortException("cannot undefine target words");
			dictionary.remove(string.toLowerCase());
		} else {
			System.out.println("cannot undef unknown word: " + string);
		}
	}
	/**
	 * @return the latest
	 */
	public IWord getLatest() {
		return latest;
	}

	/**
	 * @param word
	 */
	public void setLatest(IWord word) {
		latest = word;
		
	}

	/**
	 * @param token
	 * @return
	 */
	public IWord find(String token) {
		IWord word = dictionary.get(token.toLowerCase());
		if (word != null && word instanceof ITargetWord && ((ITargetWord) word).getEntry().isHidden()) {
			IWord[] words = (IWord[]) dictionary.values().toArray(new IWord[dictionary.values().size()]);
			boolean saw = false;
			for (int idx = words.length; idx-- > 0; ) {
				if (words[idx] == word) {
					saw = true;
				}
				else if (words[idx].getName().equalsIgnoreCase(token.toLowerCase()) && !saw) {
					return words[idx];
				}
			}
			return null;
		}
		return word;
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
