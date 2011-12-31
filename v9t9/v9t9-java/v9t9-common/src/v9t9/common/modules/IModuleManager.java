/**
 * 
 */
package v9t9.common.modules;

import java.net.URI;
import java.net.URL;
import java.util.Collection;

import ejs.base.properties.IPersistable;

import v9t9.common.events.NotifyException;
import v9t9.common.memory.IMemoryEntry;

/**
 * @author ejs
 *
 */
public interface IModuleManager extends IPersistable {

	URL getStockDatabaseURL();
	
	void clearModules();
	void addModules(Collection<IModule> modList);

	//void loadModuleDatabases(String[] files, IEventNotifier notifier);

	IModule[] getModules();

	void switchModule(IModule module) throws NotifyException;

	void unloadAllModules();

	void loadModule(IModule module) throws NotifyException;

	void unloadModule(IModule loaded);

	void switchModule(String name) throws NotifyException;

	IModule findModuleByName(String string, boolean exact);

	/**
	 * @return
	 */
	IModule[] getLoadedModules();

	/**
	 * Get the memory entries for the module segments
	 * @param module
	 * @return collection of entries
	 * @throws NotifyException if any segment cannot be loaded
	 */
	Collection<IMemoryEntry> getModuleMemoryEntries(IModule module) throws NotifyException;

	/**
	 * @param stockDatabaseURL
	 */
	void registerModules(URI databaseURI);

	/**
	 * 
	 */
	void reload();

}