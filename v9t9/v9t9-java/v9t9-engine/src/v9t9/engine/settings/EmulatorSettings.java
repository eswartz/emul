/**
 * 
 */
package v9t9.engine.settings;

import v9t9.common.settings.BaseStoredSettings;


/**
 * This maintains settings global to the emulator (and saved automagically in
 * a config file) as opposed to state-specific settings.
 * <p>
 * @author ejs
 *
 */
public class EmulatorSettings extends BaseStoredSettings {
	public static final EmulatorSettings INSTANCE = new EmulatorSettings();
	/* (non-Javadoc)
	 * @see v9t9.emulator.common.BaseStoredSettings#getConfigFileName()
	 */
	@Override
	public String getConfigFileName() {
		return "config";
	}
	
	protected EmulatorSettings() {
		super();
	}
}
