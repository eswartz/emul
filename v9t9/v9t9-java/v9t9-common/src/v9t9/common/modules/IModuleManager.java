/*
  IModuleManager.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.modules;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.events.IEventNotifier;
import v9t9.common.events.NotifyException;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.settings.SettingSchema;
import ejs.base.properties.IPersistable;

/**
 * @author ejs
 *
 */
public interface IModuleManager extends IPersistable {

	SettingSchema settingUserModuleLists = new SettingSchema(
			ISettingsHandler.MACHINE,
			"UserModuleLists", String.class, new ArrayList<String>());

	void clearModules();
	void addModules(Collection<IModule> modList);

	//void loadModuleDatabases(String[] files, IEventNotifier notifier);

	IModule[] getModules();
	List<IModule> readModules(URI databaseURI) throws IOException;

	/**
	 * Unload the current module and load this module into memory
	 * @param module
	 * @throws NotifyException
	 */
	void switchModule(IModule module) throws NotifyException;

	/**
	 * Unload any loaded modules from memory
	 */
	void unloadAllModules();

	/**
	 * Load the given module into memory
	 * @param module
	 * @throws NotifyException
	 */
	void loadModule(IModule module) throws NotifyException;

	/**
	 * Unload the given module from memory
	 * @param module
	 * @throws NotifyException
	 */
	void unloadModule(IModule loaded);

	/**
	 * Find the first module with this name
	 * @param string
	 * @param exact
	 * @return
	 */
	IModule findModuleByName(String string, boolean exact);
	
	/**
	 * Find the first module with this MD5 
	 * which also matches the name, or, fall back to the first
	 * with this name
	 * @param name human-readable name
	 * @param md5 from IModule#getMD5()
	 * @return first match or <code>null</code>
	 */
	IModule findModuleByNameAndHash(String name, String md5);

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
	 * Reload module entries from databases 
	 */
	void reloadDatabase();

	/**
	 * Reload current loaded modules
	 */
	void reloadModules(IEventNotifier notifier);

	/**
	 * @param module
	 */
	void removeModule(IModule module);

	/**
	 * @return
	 */
	ModuleInfoDatabase getModuleInfoDatabase();

	/**
	 * Find a stock module with the same module MD5
	 * @param moduleMd5 md5 to match
	 * @return stock module or <code>null</code>
	 */
	IModule findStockModuleByMd5(String moduleMd5);

	/**
	 * Find the stock module that replaces the one with the given MD5.
	 * The distinction is, replacement stock modules have extra memory entries.
	 * @param moduleMd5 md5 to match
	 * @return stock module or <code>null</code>
	 */
	IModule findReplacedStockModuleByMd5(String moduleMd5);
	
	/**
	 * Get all the stock modules
	 * @return
	 */
	IModule[] getStockModules();
	/**
	 * Load the last loaded module (after startup)
	 */
	void restoreLastModule() throws NotifyException;
}