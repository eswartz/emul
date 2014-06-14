/**
 * 
 */
package v9t9.tools.forthcomp;

import v9t9.engine.memory.MemoryDomain;

/**
 * @author ejs
 *
 */
public interface IGromTargetContext extends ITargetContext {

	/**
	 * @return
	 */
	int getGP();

	/**
	 * @param gp
	 */
	void setGP(int gp);

	/**
	 * @param grom
	 */
	void setGrom(MemoryDomain grom);

	/**
	 * @return
	 */
	MemoryDomain getGrom();

	/**
	 * @param b
	 */
	void setUseGromDictionary(boolean b);

	/**
	 * @return
	 */
	boolean useGromDictionary();

}
