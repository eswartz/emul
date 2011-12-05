/**
 * 
 */
package v9t9.common.client;

import v9t9.common.settings.IStoredSettings;

/**
 * @author ejs
 *
 */
public interface ISettingsHandler {

	IStoredSettings getWorkspaceSettings();
	IStoredSettings getInstanceSettings();
}
