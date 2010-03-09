/**
 * 
 */
package v9t9.emulator;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;

/**
 * @author ejs
 *
 */
public class EmulatorSettings {
	private static final EmulatorSettings INSTANCE = new EmulatorSettings();
	protected DialogSettings settings;

	public static EmulatorSettings getInstance() {
		return INSTANCE;
	}
	
	protected EmulatorSettings() {
		
	}

	public void load() {
		if (settings == null) {
			settings = new DialogSettings("root");
			try {
				settings.load(getSettingsConfigurationPath());
			} catch (IOException e) {
			}
		}
	}
	public void save() {
		try {
			String path = EmulatorSettings.getInstance().getSettingsConfigurationPath();
			File file = new File(path);
			file.getParentFile().mkdirs();
			settings.save(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void clearConfigVar(String configVar) {
		DialogSettings settings = getApplicationSettings();
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
