/**
 * 
 */
package v9t9.engine.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import v9t9.base.properties.IPersistable;
import v9t9.base.properties.SettingProperty;
import v9t9.base.settings.ISettingSection;
import v9t9.common.events.IEventNotifier;
import v9t9.common.events.NotifyException;
import v9t9.common.memory.Memory;
import v9t9.common.memory.MemoryDomain;
import v9t9.common.memory.MemoryEntry;
import v9t9.engine.files.DataFiles;
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
	
	private Map<MemoryEntry, IModule> memoryEntryModules = new HashMap<MemoryEntry, IModule>();
	
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
			unloadModule(loaded);
		}
		loadedModules.clear();
		settingLastLoadedModule.setString(null);
	}
	
	public void loadModule(IModule module) throws NotifyException {
		if (module != null) {
			if (loadedModules.contains(module))
				return;
			
			List<MemoryEntry> entries = new ArrayList<MemoryEntry>();
			Memory memory = machine.getMemory();
			for (MemoryEntryInfo info : module.getMemoryEntryInfos()) {
				MemoryEntry entry = info.createMemoryEntry(memory);
				entries.add(entry);
			}
			for (MemoryEntry entry : entries) {
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
		
		Memory memory = machine.getMemory();
		
		for (MemoryDomain domain : memory.getDomains())
			for (MemoryEntry entry : domain.getMemoryEntries()) {
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
}
