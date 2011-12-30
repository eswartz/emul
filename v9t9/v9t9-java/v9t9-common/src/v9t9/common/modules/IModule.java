/**
 * 
 */
package v9t9.common.modules;

import java.net.URI;

import v9t9.common.memory.MemoryEntryInfo;

/**
 * @author ejs
 *
 */
public interface IModule {
	URI getDatabaseURI();
	
	String getName();
	
	/** Get filename or path to associated image, or <code>null</code> */
	String getImagePath();
	
	MemoryEntryInfo[] getMemoryEntryInfos();
}
