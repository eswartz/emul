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

}
