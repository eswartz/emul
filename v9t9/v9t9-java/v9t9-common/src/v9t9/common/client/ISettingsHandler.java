/**
 * 
 */
package v9t9.common.client;

import java.util.Map;

import ejs.base.properties.IProperty;

import v9t9.common.settings.IStoredSettings;
import v9t9.common.settings.SettingSchema;

/**
 * @author ejs 
 *
 */
public interface ISettingsHandler {
	String GLOBAL = "Global";
	String WORKSPACE = "Workspace";
	String INSTANCE = "Instance";
	String TRANSIENT = "Transient";
	
	IProperty get(SettingSchema schema);
	<T extends IProperty> T get(String context, T defaultProperty);
	
	Map<IProperty, SettingSchema> getAllSettings();
	
	IStoredSettings getWorkspaceSettings();
	IStoredSettings getInstanceSettings();
	
	IStoredSettings findSettingStorage(String settingsName);
}
