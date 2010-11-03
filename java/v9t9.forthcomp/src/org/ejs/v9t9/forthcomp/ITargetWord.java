/**
 * 
 */
package org.ejs.v9t9.forthcomp;

/**
 * @author ejs
 *
 */
public interface ITargetWord extends IWord {
	
	DictEntry getEntry();

	/**
	 * @param localDP
	 */
	void setHostDp(int localDP);

}
