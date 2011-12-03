/**
 * 
 */
package v9t9.engine;

import v9t9.base.properties.SettingProperty;
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
	public static final SettingProperty settingPlaySound = new SettingProperty("PlaySound", new Boolean(true));
	public static final SettingProperty settingSoundVolume = new SettingProperty("SoundVolume", new Integer(10));
	
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
