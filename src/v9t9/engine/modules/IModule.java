/**
 * 
 */
package v9t9.engine.modules;

import v9t9.engine.memory.MemoryEntry;

/**
 * @author ejs
 *
 */
public interface IModule {
	String getName();
	
	MemoryEntryInfo[] getMemoryEntryInfos();
	
}
