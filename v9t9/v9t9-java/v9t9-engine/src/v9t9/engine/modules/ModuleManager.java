/**
 * 
 */
package v9t9.engine.modules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ejs.base.properties.IPersistable;
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
public class ModuleManager implements IPersistable, IModuleManager {
	private List<IModule> modules;
	private final IMachine machine;
	
	private List<IModule> loadedModules = new ArrayList<IModule>();
	
	public static SettingSchema settingLastLoadedModule = new SettingSchema(
			ISettingsHandler.WORKSPACE,
			"LastLoadedModule", "");
	
	private Map<IMemoryEntry, IModule> memoryEntryModules = new HashMap<IMemoryEntry, IModule>();
	private IProperty lastLoadedModule;
	
	public ModuleManager(IMachine machine) {
		this.machine = machine;
		this.modules = Collections.emptyList();
		
		lastLoadedModule = Settings.get(machine, settingLastLoadedModule);
	}
	

    /* (non-Javadoc)
	 * @see v9t9.engine.modules.IModuleManager#loadModules(java.lang.String[], v9t9.common.events.IEventNotifier)
	 */
    @Override
	public void loadModules(String[] files, IEventNotifier notifier) {
    	ISettingsHandler settings = Settings.getSettings(machine);
    	if (modules.isEmpty()) {

			for (String dbName : files) {
				boolean anyErrors = false;
	    		try {
					List<IModule> modList = ModuleLoader.loadModuleList(settings, dbName);
					if (modules == null || modules.isEmpty())
						modules = modList;
					else
						modules.addAll(modList);
	    		} catch (NotifyException e) {
	    			notifier.notifyEvent(e.getEvent());
				}
	    		if (anyErrors) {
	    			notifier.notifyEvent(this, IEventNotifier.Level.ERROR,
	    					"Be sure your " + DataFiles.settingBootRomsPath.getName() + " setting is established in "
							+ settings.getWorkspaceSettings().getConfigFilePath());
	    			if (modules == null) {
	    				modules = Collections.emptyList();
	    			}
	    		}
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
			
			List<IMemoryEntry> entries = new ArrayList<IMemoryEntry>();
			IMemory memory = machine.getMemory();
			for (MemoryEntryInfo info : module.getMemoryEntryInfos()) {
				IMemoryEntry entry = createMemoryEntry(info, memory);
				if (entry != null)
					entries.add(entry);
			}
			for (IMemoryEntry entry : entries) {
				try {
					memory.addAndMap(entry);
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
}
