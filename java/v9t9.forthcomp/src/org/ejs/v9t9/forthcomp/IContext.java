/**
 * 
 */
package org.ejs.v9t9.forthcomp;

/**
 * This is context for a FORTH environment: dictionary, user variables, etc.
 * @author ejs
 *
 */
public interface IContext {
	IWord define(String string, IWord word);
	IWord find(String token);
	
}
