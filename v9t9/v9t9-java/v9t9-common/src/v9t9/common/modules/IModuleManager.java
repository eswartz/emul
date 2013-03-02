/*
  IModuleManager.java

  (c) 2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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

	/**
	 * @param module
	 */
	void removeModule(IModule module);

}