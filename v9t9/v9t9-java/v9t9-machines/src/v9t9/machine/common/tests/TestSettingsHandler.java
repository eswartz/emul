/**
 * 
 */
package v9t9.machine.common.tests;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.BaseSettingsHandler;
import v9t9.common.settings.StaticStoredSettings;

/**
 * @author ejs
 * 
 */
public class TestSettingsHandler extends BaseSettingsHandler {

	public TestSettingsHandler() {
		super(new StaticStoredSettings(
				ISettingsHandler.WORKSPACE,
					"workspace"), 
			new StaticStoredSettings(
					ISettingsHandler.INSTANCE, 
					"instance"));
	}

}
