/**
 * 
 */
package v9t9.engine.machine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import v9t9.base.properties.IPersistable;
import v9t9.base.properties.SettingProperty;
import v9t9.base.settings.ISettingSection;
import v9t9.common.cpu.AbortedException;
import v9t9.common.events.IEventNotifier;
import v9t9.common.events.NotifyException;
import v9t9.common.memory.BankedMemoryEntry;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.engine.files.DataFiles;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.modules.IModule;
import v9t9.engine.modules.MemoryEntryInfo;
import v9t9.engine.modules.ModuleLoader;
import v9t9.engine.settings.WorkspaceSettings;

/**
 * @author ejs
 *
 */
public class ModuleManager implements IPersistable {
	private List<IModule> modules;
	private final IMachine machine;
	
	private List<IModule> loadedModules = new ArrayList<IModule>();
	
	public static SettingProperty settingLastLoadedModule = new SettingProperty("LastLoadedModule", "");
	
	private Map<IMemoryEntry, IModule> memoryEntryModules = new HashMap<IMemoryEntry, IModule>();
	
	public ModuleManager(IMachine machine) {
		this.machine = machine;
		this.modules = Collections.emptyList();
	}
	

    public void loadModules(String[] files, IEventNotifier notifier) {
    	if (modules.isEmpty()) {

			for (String dbName : files) {
				boolean anyErrors = false;
	    		try {
					List<IModule> modList = ModuleLoader.loadModuleList(dbName);
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
							+ WorkspaceSettings.CURRENT.getConfigFilePath());
	    			if (modules == null) {
	    				modules = Collections.emptyList();
	    			}
	    		}
    		}
    	}
    }

	public IModule[] getModules() {
		return (IModule[]) modules.toArray(new IModule[modules.size()]);
	}

	public void switchModule(IModule module) throws NotifyException {
		unloadAllModules();
		
		loadModule(module);
	}
	public void unloadAllModules() {
		for (IModule loaded : (IModule[]) loadedModules.toArray(new IModule[loadedModules.size()])) {
			try {
				unloadModule(loaded);
			} catch (AbortedException e) {
				// ignore
			}
		}
		loadedModules.clear();
		settingLastLoadedModule.setString(null);
	}
	
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
				memory.addAndMap(entry);
				memoryEntryModules.put(entry, module);
			}
			loadedModules.add(module);
			
			settingLastLoadedModule.setString(module.getName());
		} else {
			settingLastLoadedModule.setString(null);
		}
		
	}

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
		settingLastLoadedModule.setString(null);
	}
	
	public void switchModule(String name) throws NotifyException {
		switchModule(findModuleByName(name, true));
	}
	
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
	/**
	 * @return
	 */
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
			IMemoryEntry entry = null;
			Map<String, Object> properties = info.getProperties();
			if (properties.containsKey(MemoryEntryInfo.FILENAME2)) {
				try {
					entry = DiskMemoryEntry.newBankedWordMemoryFromFile(
							(Class<BankedMemoryEntry>) properties.get(MemoryEntryInfo.CLASS),
							info.getInt(MemoryEntryInfo.ADDRESS),
							info.getInt(MemoryEntryInfo.SIZE),
							memory,
							info.getString(MemoryEntryInfo.NAME),
							memory.getDomain(info.getString(MemoryEntryInfo.DOMAIN)),
							info.getFilePath(info.getString(MemoryEntryInfo.FILENAME), info.getBool(MemoryEntryInfo.STORED)),
							info.getInt(MemoryEntryInfo.OFFSET),
							info.getFilePath(info.getString(MemoryEntryInfo.FILENAME2), info.getBool(MemoryEntryInfo.STORED)),
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
						info.getInt(MemoryEntryInfo.ADDRESS),
						info.getInt(MemoryEntryInfo.SIZE),
						info.getString(MemoryEntryInfo.NAME),
						memory.getDomain(info.getString(MemoryEntryInfo.DOMAIN)),
						info.getFilePath(info.getString(MemoryEntryInfo.FILENAME), info.getBool(MemoryEntryInfo.STORED)),
						info.getInt(MemoryEntryInfo.OFFSET),
						info.getBool(MemoryEntryInfo.STORED));
			} else {
				entry = DiskMemoryEntry.newByteMemoryFromFile(
						info.getInt(MemoryEntryInfo.ADDRESS),
						info.getInt(MemoryEntryInfo.SIZE),
						info.getString(MemoryEntryInfo.NAME),
						memory.getDomain(info.getString(MemoryEntryInfo.DOMAIN)),
						info.getFilePath(info.getString(MemoryEntryInfo.FILENAME), info.getBool(MemoryEntryInfo.STORED)),
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
