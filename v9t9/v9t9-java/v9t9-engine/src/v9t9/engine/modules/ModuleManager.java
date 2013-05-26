/*
  ModuleManager.java

  (c) 2010-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.modules;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.AbortedException;
import v9t9.common.events.NotifyEvent.Level;
import v9t9.common.events.NotifyException;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.memory.MemoryEntryInfo;
import v9t9.common.modules.IModule;
import v9t9.common.modules.IModuleManager;
import v9t9.common.modules.ModuleDatabase;
import v9t9.common.modules.ModuleInfoDatabase;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.Settings;
import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingSection;

/**
 * @author ejs
 *
 */
public class ModuleManager implements IModuleManager {
	private static Logger log = Logger.getLogger(ModuleManager.class);
	
	private List<IModule> modules;
	private final IMachine machine;
	
	private List<IModule> loadedModules = new ArrayList<IModule>();
	
	public static SettingSchema settingLastLoadedModule = new SettingSchema(
			ISettingsHandler.MACHINE,
			"LastLoadedModule", "");
	
	private Map<IMemoryEntry, IModule> memoryEntryModules = new HashMap<IMemoryEntry, IModule>();
	private IProperty lastLoadedModule;
	private final String stockModuleDatabase;

	private ModuleInfoDatabase moduleInfoDb;

	public ModuleManager(IMachine machine, String stockModuleDatabase) {
		this.machine = machine;
		this.stockModuleDatabase = stockModuleDatabase;
		this.modules = new ArrayList<IModule>();
		
		this.moduleInfoDb = ModuleInfoDatabase.loadModuleInfo(machine);
		
		lastLoadedModule = Settings.get(machine, settingLastLoadedModule);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModuleManager#getStockDatabaseURL()
	 */
	@Override
	public URL getStockDatabaseURL() {
		try {
			return new URL(machine.getModel().getDataURL(), stockModuleDatabase);
		} catch (MalformedURLException e) {
			log.error("could not find " + stockModuleDatabase, e);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModuleManager#clearModules()
	 */
	@Override
	public void clearModules() {
		modules.clear();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModuleManager#addModules(java.util.Collection)
	 */
	@Override
	public void addModules(Collection<IModule> modList) {
		for (IModule module : modList) {
			if (!modules.contains(module)) {
				modules.add(module);
			}
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModuleManager#getModules()
	 */
	@Override
	public IModule[] getModules() {
		return (IModule[]) modules.toArray(new IModule[modules.size()]);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModuleManager#switchModule(v9t9.common.modules.IModule)
	 */
	@Override
	public void switchModule(IModule module) throws NotifyException {
		unloadAllModules();
		
		loadModule(module);
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModuleManager#unloadAllModules()
	 */
	@Override
	public void unloadAllModules() {
		for (IModule loaded : (IModule[]) loadedModules.toArray(new IModule[loadedModules.size()])) {
			try {
				unloadModule(loaded);
			} catch (AbortedException e) {
				// ignore
			}
		}
		loadedModules.clear();
		lastLoadedModule.setString(null);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModuleManager#loadModule(v9t9.common.modules.IModule)
	 */
	@Override
	public void loadModule(IModule module) throws NotifyException {
		if (module != null) {
			if (loadedModules.contains(module))
				return;
			
			Collection<IMemoryEntry> entries = getModuleMemoryEntries(module);
			for (IMemoryEntry entry : entries) {
				try {
					machine.getMemory().addAndMap(entry);
				} catch (AbortedException e) {
					// ignore
				}
				memoryEntryModules.put(entry, module);
			}
			loadedModules.add(module);
			
			lastLoadedModule.setString(module.getName());
		} else {
			lastLoadedModule.setString(null);
		}
		
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModuleManager#unloadModule(v9t9.common.modules.IModule)
	 */
	@Override
	public void unloadModule(IModule loaded) {
		if (loaded == null)
			return;
		
		IMemory memory = machine.getMemory();
		
		for (IMemoryDomain domain : memory.getDomains()) {
			for (IMemoryEntry entry : domain.getMemoryEntries()) {
				if (loaded.equals(memoryEntryModules.get(entry))) {
					domain.unmapEntry(entry);
					memoryEntryModules.remove(entry);
				}
			}
		}
		
		loadedModules.remove(loaded);
		lastLoadedModule.setString(null);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModuleManager#findModuleByName(java.lang.String, boolean)
	 */
	@Override
	public IModule findModuleByName(String string, boolean exact) {
		for (IModule module : modules) {
			if (exact) {
				if (module.getName().equals(string))
					return module;
			} else {
				if (module.getName().toLowerCase().contains(string.toLowerCase()))
					return module;
			}
		}
		return null;
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModuleManager#getLoadedModules()
	 */
	@Override
	public IModule[] getLoadedModules() {
		return loadedModules.toArray(new IModule[loadedModules.size()]);
	}

	public void saveState(ISettingSection section) {
		String[] moduleNames = new String[loadedModules.size()];
		for (int i = 0; i < moduleNames.length; i++)
			moduleNames[i] = loadedModules.get(i).getName();
		section.put("LoadedModules", moduleNames);
	}
	
	public void loadState(ISettingSection section) {
		unloadAllModules();
		if (section == null)
			return;
		String[] loaded = section.getArray("LoadedModules");
		if (loaded == null)
			return;
		for (String name : loaded) {
			try {
				loadModule(findModuleByName(name, true));
			} catch (NotifyException e) {
				machine.notifyEvent(e.getEvent());
			}
		}
		
	}


	public IMemoryEntry createMemoryEntry(IModule module, MemoryEntryInfo info, IMemory memory) throws NotifyException {
		try {

			IMemoryEntry entry = null;
			entry = memory.getMemoryEntryFactory().newMemoryEntry(info);
			return entry;
		} catch (FileNotFoundException e) {
			String filename = info.getString(MemoryEntryInfo.FILENAME); 
			throw new NotifyException(null, "Failed to load file '" + filename 
					+ "' for '" + info.getString(MemoryEntryInfo.NAME) +"' "
					+ "in " + module.getDatabaseURI(),
					e);
		} catch (IOException e) {
			String filename = info.getString(MemoryEntryInfo.FILENAME); 
			throw new NotifyException(null, "Error with file '" + filename 
					+ "' for '" + info.getString(MemoryEntryInfo.NAME) +"' "
					+ "in " + module.getDatabaseURI() +":\n\n" + e.getMessage(),
					e);
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModuleManager#getModuleMemoryEntries(v9t9.common.modules.IModule)
	 */
	@Override
	public Collection<IMemoryEntry> getModuleMemoryEntries(IModule module)
			throws NotifyException {
		List<IMemoryEntry> entries = new ArrayList<IMemoryEntry>();
		IMemory memory = machine.getMemory();
		for (MemoryEntryInfo info : module.getMemoryEntryInfos()) {
			IMemoryEntry entry = createMemoryEntry(module, info, memory);
			if (entry != null)
				entries.add(entry);
		}
		return entries;
	}
	

	public void registerModules(URI uri) {
		if (uri == null)
			return;

		try {
			List<IModule> modList = readModules(uri);
			addModules(modList);
		} catch (IOException e) {
			machine.getClient().getEventNotifier().notifyEvent(this, Level.ERROR,
					"Could not load module list " + uri + ": " + e.getMessage());
		}

	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModuleManager#readModules(java.net.URI)
	 */
	@Override
	public List<IModule> readModules(URI databaseURI) throws IOException {
		InputStream is = null;
		is = machine.getRomPathFileLocator().createInputStream(databaseURI);
		List<IModule> modList = ModuleDatabase.loadModuleListAndClose(machine.getMemory(), 
				moduleInfoDb,
				is, databaseURI);
		return modList;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModuleManager#reload()
	 */
	@Override
	public void reload() {
		URI databaseURI;
		
		// first, get stock module database
		machine.getModuleManager().clearModules();
		databaseURI = machine.getRomPathFileLocator().findFile("stock_modules.xml");
		if (databaseURI != null) {
			registerModules(databaseURI);
		} else {
			//throw new AssertionError("missing stock_modules.xml");
			log.error("failed to find stock_modules.xml");
			return;
		}
		
		// then load any user entries
		IProperty moduleList = Settings.get(machine, IModuleManager.settingUserModuleLists);
		List<String> dbNames = moduleList.getList();
		for (String dbName : dbNames) {
			databaseURI = machine.getRomPathFileLocator().findFile(dbName);
			if (databaseURI != null) {
				registerModules(databaseURI);
			} else {
				machine.getClient().getEventNotifier().notifyEvent(this, Level.ERROR,
						"Could not find module list " + dbName);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModuleManager#removeModule(v9t9.common.modules.IModule)
	 */
	@Override
	public void removeModule(IModule module) {
		modules.remove(module);
	}
	
	/**
	 * @return the moduleInfoDb
	 */
	public ModuleInfoDatabase getModuleInfoDatabase() {
		return moduleInfoDb;
	}
}
