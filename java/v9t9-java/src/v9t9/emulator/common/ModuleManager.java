/**
 * 
 */
package v9t9.emulator.common;

import java.util.ArrayList;
import java.util.List;

import org.ejs.coffee.core.properties.IPersistable;
import org.ejs.coffee.core.properties.SettingProperty;
import org.ejs.coffee.core.settings.ISettingSection;

import v9t9.emulator.clients.builtin.NotifyException;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;
import v9t9.engine.modules.IModule;
import v9t9.engine.modules.MemoryEntryInfo;

/**
 * @author ejs
 *
 */
public class ModuleManager implements IPersistable {
	private List<IModule> modules;
	private final Machine machine;
	
	private List<IModule> loadedModules = new ArrayList<IModule>();
	
	public static SettingProperty settingLastLoadedModule = new SettingProperty("LastLoadedModule", "");
	
	public ModuleManager(Machine machine, List<IModule> modules) {
		this.machine = machine;
		this.modules = modules;
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
				entry.moduleLoaded = module;
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
			for (MemoryEntry entry : domain.getMemoryEntries())
				if (loaded.equals(entry.moduleLoaded))
					domain.unmapEntry(entry);
		
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
