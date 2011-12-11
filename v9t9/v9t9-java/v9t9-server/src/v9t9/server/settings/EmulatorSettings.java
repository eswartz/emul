/**
 * 
 */
package v9t9.server.settings;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.StaticStoredSettings;


/**
 * This maintains settings global to the emulator (and saved automagically in
 * a config file) as opposed to state-specific settings.
 * <p>
 * @author ejs
 *
 */
public class EmulatorSettings extends StaticStoredSettings {
	//public static final EmulatorSettings INSTANCE = new EmulatorSettings();
	/* (non-Javadoc)
	 * @see v9t9.emulator.common.BaseStoredSettings#getConfigFileName()
	 */
	
	protected EmulatorSettings() {
		super(ISettingsHandler.INSTANCE, "config");
	}
}
