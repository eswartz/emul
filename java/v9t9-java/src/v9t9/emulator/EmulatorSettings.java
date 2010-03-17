/**
 * 
 */
package v9t9.emulator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.DialogSettings;
import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;
import org.ejs.coffee.core.properties.SettingProperty;
import org.ejs.coffee.core.settings.ISettingSection;
import org.ejs.coffee.core.settings.ISettingStorage;
import org.ejs.coffee.core.settings.SettingsSection;
import org.ejs.coffee.core.settings.XMLSettingStorage;
import org.json.JSONObject;

/**
 * This maintains settings global to the emulator (and saved automagically in
 * a config file) as opposed to state-specific settings.
 * <p>
 * @author ejs
 *
 */
public class EmulatorSettings {
	/**
	 * 
	 */
	private static final String ROOT = "root";
	public static final EmulatorSettings INSTANCE = new EmulatorSettings();
	protected ISettingSection section;

	private List<SettingProperty> trackedSettings;
	private boolean isLoading;
	private IPropertyListener trackedSettingListener;
	protected boolean isLoaded;
	protected boolean needsSave;
	
	protected EmulatorSettings() {
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
	
	public synchronized void load() {
		try {
			ISettingStorage storage = new XMLSettingStorage(ROOT);
			File settingsConfigurationFile = new File(getSettingsConfigurationPath());
			section = storage.load(settingsConfigurationFile);
			
			isLoaded = true;
			needsSave = false;
			isLoading = true;
			try {
				for (SettingProperty setting : trackedSettings) {
					setting.loadState(section);
				}
			} finally {
				isLoading = false;
			}
		} catch (IOException e) {
			needsSave = true;
		}
	}
	public synchronized void save() {
		String path = EmulatorSettings.INSTANCE.getSettingsConfigurationPath();
		File file = new File(path);
		file.getParentFile().mkdirs();
		//if (file.exists() && !needsSave)
		//	return;
		
		File backup = new File(path + "~");
		file.renameTo(backup);
		
		for (SettingProperty setting : trackedSettings) {
			setting.saveState(section);
		}
		
		try {
			ISettingStorage storage = new XMLSettingStorage(ROOT);
			storage.save(file, section);
			needsSave = false;
		} catch (IOException e) {
			e.printStackTrace();
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

	public String getBaseConfigurationPath() {
		return System.getProperty("user.home") + File.separatorChar + ".v9t9j" + File.separatorChar;
	}

	public ISettingSection getApplicationSettings() {
		if (section == null) {
			load();
		}
		return section;
	}

	public String getSettingsConfigurationPath() {
		return getBaseConfigurationPath() + "config";
	}

	/**
	 * @return
	 */
	public ISettingSection getHistorySettings() {
		ISettingSection historySection = section.findOrAddSection("History");
		return historySection;
	}
}
