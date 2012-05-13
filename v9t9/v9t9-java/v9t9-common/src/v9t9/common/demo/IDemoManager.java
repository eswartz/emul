/**
 * 
 */
package v9t9.common.demo;

import v9t9.common.files.IPathFileLocator;

/**
 * @author ejs
 *
 */
public interface IDemoManager {

	/** locate the demos */
	IPathFileLocator getDemoLocator();
	
	/** get an array of all known demos along paths */
	IDemo[] getDemos();
	
	/** refresh demo list */
	void reload();
	
	/** register this demo (e.g., just recorded) */
	void addDemo(IDemo demo);

	/**
	 * remove a demo
	 */
	void removeDemo(IDemo demo);
}
