/**
 * 
 */
package v9t9.emulator.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;
import org.ejs.coffee.core.properties.SettingProperty;
import org.ejs.coffee.core.settings.ISettingSection;
import org.ejs.coffee.core.settings.ISettingStorage;
import org.ejs.coffee.core.settings.SettingsSection;
import org.ejs.coffee.core.settings.XMLSettingStorage;

/**
 * @author ejs
 *
 */
public abstract class BaseStoredSettings implements IStoredSettings {

	private static final String ROOT = "root";
	protected ISettingSection section;
	protected List<SettingProperty> trackedSettings;
	protected boolean isLoading;
	protected IPropertyListener trackedSettingListener;
	protected boolean isLoaded;
	protected boolean needsSave;

	public BaseStoredSettings() {
		section = new SettingsSection();
		trackedSettings = new ArrayList<SettingProperty>();
		trackedSettingListener = new IPropertyListener() {
			
			@Override
			public void propertyChanged(IProperty setting) {
				if (!isLoading)
					needsSave = true;
			}
		};
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IStoredSettings#load(org.ejs.coffee.core.settings.ISettingSection)
	 */
	@Override
	public void load(ISettingSection settings) {
		for (SettingProperty setting : trackedSettings) {
			setting.loadState(settings);
		}		
	}
	
	public synchronized void load() throws IOException {
		try {
			ISettingStorage storage = new XMLSettingStorage(ROOT);
			File settingsConfigurationFile = new File(getConfigFilePath());
			section = storage.load(settingsConfigurationFile);
		} catch (IOException e) {
			needsSave = true;
			section = new SettingsSection();
			throw e;
		}
			
		isLoaded = true;
		needsSave = false;
		isLoading = true;
		try {
			load(section);
		} finally {
			isLoading = false;
		}
	}

	public synchronized void save() throws IOException {
		String path = getConfigFilePath();
		File file = new File(path);
		file.getParentFile().mkdirs();
		//if (file.exists() && !needsSave)
		//	return;
		
		File backup = new File(path + "~");
		file.renameTo(backup);
		
		ISettingSection saveSection = section;
		
		// toss unrecognized keys
		//if (true)
		//	saveSection = new SettingsSection();
		
		save(saveSection);
		
		ISettingStorage storage = new XMLSettingStorage(ROOT);
		storage.save(file, saveSection);
		needsSave = false;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.common.IStoredSettings#save(org.ejs.coffee.core.settings.ISettingSection)
	 */
	@Override
	public void save(ISettingSection settings) {
		for (SettingProperty setting : trackedSettings) {
			setting.saveState(settings);
		}
	}

	public void register(SettingProperty setting) {
		if (!trackedSettings.contains(setting)) {
			trackedSettings.add(setting);
			setting.addListener(trackedSettingListener);
			if (isLoaded) {
				setting.loadState(section);
			}
		}
	}

	public void clearConfigVar(String configVar) {
		section.put(configVar, (String) null);
	}

	public ISettingSection getSettings() {
		if (section == null) {
			try {
				load();
			} catch (IOException e) {
				assert section != null;
			}
		}
		return section;
	}

	public String getConfigDirectory() {
		return System.getProperty("user.home") + File.separatorChar + ".v9t9j" + File.separatorChar;
	}
	
	public String getConfigFilePath() {
		return getConfigDirectory() + getConfigFileName();
	}

	public ISettingSection getHistorySettings() {
		ISettingSection historySection = section.findOrAddSection("History");
		return historySection;
	}

}