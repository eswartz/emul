/**
 * 
 */
package org.ejs.eulang;

/**
 * The compilation target
 * @author ejs
 *
 */
public interface ITarget {
	TypeEngine getTypeEngine();
	
	/** e.g. "ccc", "fastcc", "cc &lt;n&gt;" */
	String getLLCallingConvention();

	/**
	 * Get the GNU-style target triple
	 * @return e.g. "foo-bar-baz"
	 */
	String getTriple();
	
	boolean moveLocalsToTemps();
}
