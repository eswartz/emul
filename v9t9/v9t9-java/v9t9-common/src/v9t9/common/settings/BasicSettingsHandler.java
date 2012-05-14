/**
 * 
 */
package v9t9.common.settings;

import v9t9.common.client.ISettingsHandler;

/**
 * @author ejs
 * 
 */
public class BasicSettingsHandler extends BaseSettingsHandler {

	public BasicSettingsHandler() {
		super(new StaticStoredSettings(
				ISettingsHandler.WORKSPACE,
					"workspace"), 
			new StaticStoredSettings(
					ISettingsHandler.INSTANCE, 
					"instance"));
	}

}
