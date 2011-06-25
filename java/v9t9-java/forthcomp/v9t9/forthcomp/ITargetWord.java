/**
 * 
 */
package v9t9.forthcomp;

import v9t9.forthcomp.DictEntry;
import v9t9.forthcomp.IWord;

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
