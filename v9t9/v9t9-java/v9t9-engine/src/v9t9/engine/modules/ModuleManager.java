/**
 * 
 */
package v9t9.engine.modules;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ejs.base.properties.IProperty;
import ejs.base.settings.ISettingSection;


import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.AbortedException;
import v9t9.common.events.IEventNotifier;
import v9t9.common.events.NotifyException;
import v9t9.common.files.DataFiles;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.modules.IModule;
import v9t9.common.modules.IModuleManager;
import v9t9.common.modules.MemoryEntryInfo;
import v9t9.common.settings.SettingSchema;
import v9t9.common.settings.Settings;
import v9t9.engine.memory.BankedMemoryEntry;
import v9t9.engine.memory.DiskMemoryEntry;

/**
 * @author ejs
 *
 */
public class ModuleManager implements IModuleManager {
	private List<IModule> modules;
	private final IMachine machine;
	
	private List<IModule> loadedModules = new ArrayList<IModule>();
	
	public static SettingSchema settingLastLoadedModule = new SettingSchema(
			ISettingsHandler.WORKSPACE,
			"LastLoadedModule", "");
	
	private Map<IMemoryEntry, IModule> memoryEntryModules = new HashMap<IMemoryEntry, IModule>();
	private IProperty lastLoadedModule;
	private final String stockModuleDatabase;
	
	public ModuleManager(IMachine machine, String stockModuleDatabase) {
		this.machine = machine;
		this.stockModuleDatabase = stockModuleDatabase;
		this.modules = new ArrayList<IModule>();
		
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
			if (!modules.contains(module))
				modules.add(module);
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
		
		for (IMemoryDomain domain : memory.getDomains())
			for (IMemoryEntry entry : domain.getMemoryEntries()) {
				if (memoryEntryModules.get(entry) == loaded) {
					domain.unmapEntry(entry);
					memoryEntryModules.remove(entry);
				}
			}
		
		loadedModules.remove(loaded);
		lastLoadedModule.setString(null);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.modules.IModuleManager#switchModule(java.lang.String)
	 */
	@Override
	public void switchModule(String name) throws NotifyException {
		switchModule(findModuleByName(name, true));
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
		return (IModule[]) loadedModules.toArray(new IModule[loadedModules.size()]);
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


	@SuppressWarnings("unchecked")
	public IMemoryEntry createMemoryEntry(MemoryEntryInfo info, IMemory memory) throws NotifyException {
		try {
			ISettingsHandler settings = Settings.getSettings(machine);
			String base = settings.getInstanceSettings().getConfigDirectory();

			IMemoryEntry entry = null;
			Map<String, Object> properties = info.getProperties();
			if (properties.containsKey(MemoryEntryInfo.FILENAME2)) {
				try {
					entry = DiskMemoryEntry.newBankedWordMemoryFromFile(
							settings,
							(Class<BankedMemoryEntry>) properties.get(MemoryEntryInfo.CLASS),
							info.getInt(MemoryEntryInfo.ADDRESS),
							info.getInt(MemoryEntryInfo.SIZE),
							memory,
							info.getString(MemoryEntryInfo.NAME),
							memory.getDomain(info.getString(MemoryEntryInfo.DOMAIN)),
							info.getFilePath(settings, base, info.getString(MemoryEntryInfo.FILENAME), info.getBool(MemoryEntryInfo.STORED)),
							info.getInt(MemoryEntryInfo.OFFSET),
							info.getFilePath(settings, base, info.getString(MemoryEntryInfo.FILENAME2), info.getBool(MemoryEntryInfo.STORED)),
							info.getInt(MemoryEntryInfo.OFFSET2));
				} catch (IOException e) {
					String filename = info.getString(MemoryEntryInfo.FILENAME); 
					String filename2 = info.getString(MemoryEntryInfo.FILENAME2); 
					if (filename2 == null)
					throw new NotifyException(null, 
							"Failed to load file(s) '" + filename + "' and/or '"+ filename2 + "' for '" + info.getString(MemoryEntryInfo.NAME) + "'",
							e);
				}
			} else if (IMemoryDomain.NAME_CPU.equals(properties.get(MemoryEntryInfo.DOMAIN))) {
				entry = DiskMemoryEntry.newWordMemoryFromFile(
						settings,
						info.getInt(MemoryEntryInfo.ADDRESS),
						info.getInt(MemoryEntryInfo.SIZE),
						info.getString(MemoryEntryInfo.NAME),
						memory.getDomain(info.getString(MemoryEntryInfo.DOMAIN)),
						info.getFilePath(settings, base, info.getString(MemoryEntryInfo.FILENAME), info.getBool(MemoryEntryInfo.STORED)),
						info.getInt(MemoryEntryInfo.OFFSET),
						info.getBool(MemoryEntryInfo.STORED));
			} else {
				entry = DiskMemoryEntry.newByteMemoryFromFile(
						settings,
						info.getInt(MemoryEntryInfo.ADDRESS),
						info.getInt(MemoryEntryInfo.SIZE),
						info.getString(MemoryEntryInfo.NAME),
						memory.getDomain(info.getString(MemoryEntryInfo.DOMAIN)),
						info.getFilePath(settings, base, info.getString(MemoryEntryInfo.FILENAME), info.getBool(MemoryEntryInfo.STORED)),
						info.getInt(MemoryEntryInfo.OFFSET),
						info.getBool(MemoryEntryInfo.STORED));
			}
			return entry;
		} catch (IOException e) {
			String filename = info.getString(MemoryEntryInfo.FILENAME); 
			throw new NotifyException(null, "Failed to load file '" + filename + "' for '" + info.getString(MemoryEntryInfo.NAME) +"'", e);
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
			IMemoryEntry entry = createMemoryEntry(info, memory);
			if (entry != null)
				entries.add(entry);
		}
		return entries;
	}
	

	public void registerModules(URL url) {
		if (url == null)
			return;
		
		//boolean anyErrors = false;
		InputStream is = null;
		try {
			is = url.openStream();
			List<IModule> modList = ModuleLoader.loadModuleList(machine, is);
			addModules(modList);
		} catch (NotifyException e) {
			machine.getClient().getEventNotifier().notifyEvent(e.getEvent());
			//anyErrors = true;
		} catch (IOException e) {
			machine.getClient().getEventNotifier().notifyEvent(this, IEventNotifier.Level.ERROR,
					"Could not load module list: " + e.getMessage());

		} finally {
			if (is != null) {
				try { is.close(); } catch (IOException e) { }
			}
		}
		
		/*
		if (anyErrors) {
			machine.getClient().getEventNotifier().notifyEvent(this, IEventNotifier.Level.ERROR,
					"Be sure your " + DataFiles.settingBootRomsPath.getName() + " setting is established in "
					+ settings.findSettingStorage(DataFiles.settingBootRomsPath.getName()).getConfigFilePath());
		}
		*/
	}

	/* (non-Javadoc)
	 * @see v9t9.common.modules.IModuleManager#reload()
	 */
	@Override
	public void reload() {
		IProperty lastLoadedModule = Settings.get(machine, ModuleManager.settingLastLoadedModule);
		IProperty moduleList = Settings.get(machine, IMachine.settingModuleList);
		
		// first, get stock module database
		machine.getModuleManager().clearModules();
		machine.getModuleManager().registerModules(machine.getModuleManager().getStockDatabaseURL());
		
		// then load any user entries
		String dbNameList = moduleList.getString();
		if (dbNameList.length() > 0) {
			String[] dbNames = dbNameList.split(";");
			for (String dbName : dbNames) {
				File file = DataFiles.resolveFile(Settings.getSettings(machine), dbName);
				if (file != null && file.exists()) {
					try {
						machine.getModuleManager().registerModules(file.toURI().toURL());
					} catch (MalformedURLException e) {
						machine.getClient().getEventNotifier().notifyEvent(this, IEventNotifier.Level.ERROR,
								"Could not resolve module list from " + moduleList.getName() + ": " + e.getMessage());
					}
				}
					
			}
		}
		
		// reset state
		try {
			if (lastLoadedModule.getString().length() > 0)
        		machine.getModuleManager().switchModule(
        				lastLoadedModule.getString());
		} catch (NotifyException e) {
			machine.notifyEvent(e.getEvent());
		}		
	}
}
