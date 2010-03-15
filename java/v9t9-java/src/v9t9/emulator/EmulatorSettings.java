/**
 * 
 */
package v9t9.emulator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.DialogSettings;
import org.ejs.coffee.core.properties.DialogSettingsPropertyStorage;
import org.ejs.coffee.core.properties.IProperty;
import org.ejs.coffee.core.properties.IPropertyListener;
import org.ejs.coffee.core.properties.IPropertyStorage;
import org.ejs.coffee.core.properties.SettingProperty;

/**
 * This maintains settings global to the emulator (and saved automagically in
 * a config file) as opposed to state-specific settings.
 * <p>
 * @author ejs
 *
 */
public class EmulatorSettings {
	public static final EmulatorSettings INSTANCE = new EmulatorSettings();
	protected IPropertyStorage storage;

	private List<SettingProperty> trackedSettings;
	private boolean isLoading;
	private IPropertyListener trackedSettingListener;
	protected boolean isLoaded;
	protected boolean needsSave;
	
	protected EmulatorSettings() {
		storage = new DialogSettingsPropertyStorage(new DialogSettings("root"));
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
			storage.load(getSettingsConfigurationPath());
			
			isLoaded = true;
			needsSave = false;
			isLoading = true;
			try {
				for (SettingProperty setting : trackedSettings) {
					setting.loadState(storage);
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
			setting.saveState(storage);
		}
		
		try {
			storage.save(path);
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
				setting.loadState(storage);
			}
		}
	}
	public void clearConfigVar(String configVar) {
		// TODO
		storage.remove(new SettingProperty(configVar, null));
	}

	public String getBaseConfigurationPath() {
		return System.getProperty("user.home") + File.separatorChar + ".v9t9j" + File.separatorChar;
	}

	public IPropertyStorage getApplicationSettings() {
		if (storage == null) {
			load();
		}
		return storage;
	}

	public String getSettingsConfigurationPath() {
		return getBaseConfigurationPath() + "config";
	}

	/**
	 * @return
	 */
	public IPropertyStorage getHistorySettings() {
		IPropertyStorage section = storage.getSection("History");
		if (section == null)
			section = storage.addNewSection("History");
		return section;
	}
}
