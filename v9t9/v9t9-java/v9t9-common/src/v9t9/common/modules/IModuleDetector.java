/**
 * 
 */
package v9t9.common.modules;

import java.io.File;
import java.util.Collection;

/**
 * @author ejs
 *
 */
public interface IModuleDetector {

	/** Scan the given directory and return modules found */
	Collection<IModule> scan(File base);
	
	/** Get the cumulative detected modules after or more invocations of #scan() */
	Collection<IModule> getModules();

	/**
	 * If set, read the ROM headers even if there is another name
	 * available (e.g. from RPK)
	 * @param readHeaders
	 */
	void setReadHeaders(boolean readHeaders);

	/**
	 * If set, ignore stock module database, to see what the 
	 * module itself thinks the name is (e.g. auto-start or from RPK)
	 * @param ignoreStock
	 */
	void setIgnoreStock(boolean ignoreStock);

}
