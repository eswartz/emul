/**
 * 
 */
package v9t9.common.client;

import v9t9.base.settings.SettingProperty;
import v9t9.common.settings.IStoredSettings;
import v9t9.common.settings.SettingDefinition;

/**
 * @author ejs 
 *
 */
public interface ISettingsHandler {
	String WORKSPACE = "Workspace";
	String INSTANCE = "Instance";
	String TRANSIENT = "Transient";
	
	SettingProperty get(SettingDefinition def);
	<T extends SettingProperty> T get(String context, T defaultProperty);
	
	IStoredSettings getWorkspaceSettings();
	IStoredSettings getInstanceSettings();
}
