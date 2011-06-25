/**
 * 
 */
package v9t9.emulator.common;

import java.io.IOException;

import org.ejs.coffee.core.properties.SettingProperty;
import org.ejs.coffee.core.settings.ISettingSection;

/**
 * @author ejs
 *
 */
public interface IStoredSettings {

	void load() throws IOException;
	void load(ISettingSection settings);

	void save() throws IOException;
	void save(ISettingSection settings);

	void register(SettingProperty setting);

	void clearConfigVar(String configVar);

	ISettingSection getSettings();

	/** Get directory for saving the file */
	String getConfigDirectory();
	
	/** Get full path for saving the file */
	String getConfigFilePath();

	/** Get base filename for saving the file */
	String getConfigFileName();

	/**
	 * @return
	 */
	ISettingSection getHistorySettings();

}