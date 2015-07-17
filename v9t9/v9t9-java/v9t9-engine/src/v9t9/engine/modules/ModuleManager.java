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
import v9t9.common.events.IEventNotifier;
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
import ejs.base.utils.Pair;

/**
 * @author ejs
 *
 */
public class ModuleManager implements IModuleManager {
	private static Logger log = Logger.getLogger(ModuleManager.class);
	
	private List<IModule> modules;
	private final IMachine machine;
	
	private List<IModule> loadedModules = new ArrayList<IModule>();
	
	/** The name of the module (may not be unique) */
	public static SettingSchema settingLastLoadedModule = new SettingSchema(
			ISettingsHandler.MACHINE,
			"LastLoadedModule", "");

	/** The hash of the module (may should be unique) */
	public static SettingSchema settingLastLoadedModuleHash = new SettingSchema(
			ISettingsHandler.MACHINE,
			"LastLoadedModuleHash", "");

	private Map<IMemoryEntry, IModule> memoryEntryModules = new HashMap<IMemoryEntry, IModule>();
	private final String[] stockModuleDatabases;

	private ModuleInfoDatabase moduleInfoDb;
	
	private IProperty lastLoadedModule;
	private IProperty lastLoadedModuleHash;

	private List<IModule> stockModuleList;

	public ModuleManager(IMachine machine, String[] stockModuleDatabases) {
		this.machine = machine;
		this.stockModuleDatabases = stockModuleDatabases;
		this.modules = new ArrayList<IModule>();
		
		this.moduleInfoDb = ModuleInfoDatabase.loadModuleInfo(machine);
		
		lastLoadedModule = Settings.get(machine, settingLastLoadedModule);
		lastLoadedModuleHash = Settings.get(machine, settingLastLoadedModuleHash);
		
		stockModuleList = null;
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
			lastLoadedModuleHash.setString(module.getMD5());
		} else {
			lastLoadedModule.setString(null);
			lastLoadedModuleHash.setString(null);
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
		lastLoadedModuleHash.setString(null);
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
	 * @see v9t9.common.modules.IModuleManager#findModuleByNameAndHash(java.lang.String, java.lang.String)
	 */
	@Override
	public IModule findModuleByNameAndHash(String name, String md5) {
		IModule nameCand = null;
		IModule md5Cand = null;
		for (IModule module : modules) {
			if (module.getMD5().equals(md5)) {
				md5Cand = module;
				if (module.getName().equals(name))
					break;
			}
			else if (module.getName().equals(name)) {
				nameCand = module;
			}
		}
		return md5Cand != null ? md5Cand : nameCand;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModuleManager#getLoadedModules()
	 */
	@Override
	public IModule[] getLoadedModules() {
		return loadedModules.toArray(new IModule[loadedModules.size()]);
	}


	protected Pair<String, String> getModuleNameHash(String saved) {
		int idx = saved.indexOf(':');
		if (idx >= 0) {
			return new Pair<String, String>(saved.substring(0, idx), saved.substring(idx+1));
		} else {
			return new Pair<String, String>(saved, "");
		}
	}

	protected String getModuleNameHash(IModule module) {
		if (module == null)
			return null;
		
		return module.getName() + ":" + module.getMD5();
	}
	
	
	public void saveState(ISettingSection section) {
		String[] moduleNames = new String[loadedModules.size()];
		for (int i = 0; i < moduleNames.length; i++)
			moduleNames[i] = getModuleNameHash(loadedModules.get(i));
		section.put("LoadedModules", moduleNames);
	}
	
	public void loadState(ISettingSection section) {
		unloadAllModules();
		if (section == null)
			return;
		String[] loaded = section.getArray("LoadedModules");
		if (loaded == null)
			return;
		for (String hashOrName : loaded) {
			try {
				IModule mod;
				
				Pair<String, String> nameAndMd5 = getModuleNameHash(hashOrName);
				if (nameAndMd5 != null) {
					mod = findModuleByNameAndHash(nameAndMd5.first, nameAndMd5.second);
					if (mod != null) {
						loadModule(mod);
					} else {
						throw new NotifyException(this, "No registered module matches '" + hashOrName +"'");
					}
				}
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
	public void reloadDatabase() {
		URI databaseURI;
		
		machine.getModuleManager().clearModules();
		
		// first, get stock module database
		try {
			ensureStockModules();
		} catch (NotifyException e) {
			log.error("failed to load stock modules", e);
			machine.getEventNotifier().notifyEvent(e.getEvent());
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
	
	/**
	 * 
	 */
	private void ensureStockModules() throws NotifyException {
		if (stockModuleList != null)
			return;
		
		for (String stockDB : stockModuleDatabases) {
			URL url;
			try {
				url = new URL(machine.getModel().getDataURL(), stockDB);
				if (url == null) {
					throw new NotifyException(this, "failed to find " + stockDB);
				}
			} catch (MalformedURLException e) {
				log.error("could not find " + stockDB, e);
				continue;
			}
			try {
				stockModuleList = readModules(url.toURI());
			} catch (Exception e) {
				throw new NotifyException(this, "failed to load stock_modules.xml", e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModuleManager#reloadState()
	 */
	@Override
	public void reloadModules(IEventNotifier notifier) {
		IModule[] loadedModules = getLoadedModules();
		
		unloadAllModules();
		
		for (IModule loaded : loadedModules) {
			try {
				loadModule(loaded);
			} catch (NotifyException e) {
				notifier.notifyEvent(e.getEvent());
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
	@Override
	public ModuleInfoDatabase getModuleInfoDatabase() {
		return moduleInfoDb;
	}

	@Override
	public IModule findStockModuleByMd5(String modMd5) {
		try {
			ensureStockModules();
		} catch (NotifyException e) {
			return null;
		}
		
		for (IModule stock : stockModuleList) {
			if (modMd5.equals(stock.getMD5())) {
				return stock;
			}
		}
		return null;
	}
	
	@Override
	public IModule findReplacedStockModuleByMd5(String modMd5) {
		try {
			ensureStockModules();
		} catch (NotifyException e) {
			return null;
		}
		
		for (IModule stock : stockModuleList) {
			if (stock.getReplaceMD5() != null && stock.getReplaceMD5().contains(modMd5)) {
				return stock;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModuleManager#getStockModules()
	 */
	@Override
	public IModule[] getStockModules() {
		try {
			ensureStockModules();
		} catch (NotifyException e) {
			return new IModule[0];
		}
		
		return stockModuleList.toArray(new IModule[stockModuleList.size()]);
	}
}
