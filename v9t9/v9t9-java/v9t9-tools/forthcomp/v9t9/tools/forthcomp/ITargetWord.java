/**
 * 
 */
package v9t9.tools.forthcomp;


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
