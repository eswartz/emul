/**
 * 
 */
package v9t9.emulator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.ejs.coffee.core.utils.ISettingListener;
import org.ejs.coffee.core.utils.Setting;

/**
 * This maintains settings global to the emulator (and saved automagically in
 * a config file) as opposed to state-specific settings.
 * <p>
 * @author ejs
 *
 */
public class EmulatorSettings {
	public static final EmulatorSettings INSTANCE = new EmulatorSettings();
	protected DialogSettings settings;

	private List<Setting> trackedSettings;
	private boolean isLoading;
	private ISettingListener trackedSettingListener;
	protected boolean isLoaded;
	protected boolean needsSave;
	
	protected EmulatorSettings() {
		settings = new DialogSettings("root");
		trackedSettings = new ArrayList<Setting>();
		trackedSettingListener = new ISettingListener() {
			
			@Override
			public void changed(Setting setting, Object oldValue) {
				if (!isLoading)
					needsSave = true;
			}
		};
	}
	
	public synchronized void load() {
		try {
			settings.load(getSettingsConfigurationPath());
			
			isLoaded = true;
			needsSave = false;
			isLoading = true;
			try {
				for (Setting setting : trackedSettings) {
					setting.loadState(settings);
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
		
		for (Setting setting : trackedSettings) {
			setting.saveState(settings);
		}
		
		try {
			settings.save(path);
			needsSave = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void register(Setting setting) {
		if (!trackedSettings.contains(setting)) {
			trackedSettings.add(setting);
			setting.addListener(trackedSettingListener);
			if (isLoaded) {
				setting.loadState(settings);
			}
		}
	}
	public void clearConfigVar(String configVar) {
		settings.put(configVar, (String)null);
	}

	public String getBaseConfigurationPath() {
		return System.getProperty("user.home") + File.separatorChar + ".v9t9j" + File.separatorChar;
	}

	public DialogSettings getApplicationSettings() {
		if (settings == null) {
			load();
		}
		return settings;
	}

	public String getSettingsConfigurationPath() {
		return getBaseConfigurationPath() + "config";
	}

	/**
	 * @return
	 */
	public IDialogSettings getHistorySettings() {
		IDialogSettings section = settings.getSection("History");
		if (section == null)
			section = settings.addNewSection("History");
		return section;
	}
}
