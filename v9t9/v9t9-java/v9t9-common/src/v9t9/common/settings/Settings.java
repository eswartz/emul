/**
 * 
 */
package v9t9.common.settings;

import v9t9.base.properties.IProperty;
import v9t9.common.client.IClient;
import v9t9.common.client.ISettingsHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.machine.IBaseMachine;

/**
 * @author ejs
 *
 */
public class Settings {

	public static ISettingsHandler getSettings(ICpu cpu) {
		return getSettings(cpu.getMachine());
	}
	public static ISettingsHandler getSettings(IBaseMachine machine) {
		return machine.getSettings();
	}
	public static ISettingsHandler getSettings(IClient client) {
		return client.getSettingsHandler();
	}
	public static IProperty get(ICpu cpu, SettingSchema schema) {
		return getSettings(cpu).get(schema);
	}
	public static IProperty get(IBaseMachine machine,
			SettingSchema schema) {
		return getSettings(machine).get(schema);
	}
	public static IProperty get(IClient client, SettingSchema schema) {
		return getSettings(client).get(schema);
	}
}
