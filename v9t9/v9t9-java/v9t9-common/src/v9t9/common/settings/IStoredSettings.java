/**
 * 
 */
package v9t9.common.settings;

import java.io.IOException;

import v9t9.base.settings.ISettingSection;
import v9t9.base.settings.SettingProperty;

/**
 * @author ejs
 *
 */
public interface IStoredSettings {

	void load() throws IOException;
	void load(ISettingSection settings);

	void save() throws IOException;
	void save(ISettingSection settings);

	//void register(SettingProperty setting);
	SettingProperty findOrCreate(SettingDefinition def);
	<T extends SettingProperty> T findOrCreate(T defaultProperty);

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