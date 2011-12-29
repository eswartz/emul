/**
 * 
 */
package v9t9.machine.ti99.memory;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class EnhancedTI994AMemoryModel extends TI994AStandardConsoleMemoryModel {
	/**
	 * @param machine
	 */
	public EnhancedTI994AMemoryModel(IMachine machine) {
		super(machine);
	}

	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.memory.TI994AStandardConsoleMemoryModel#initSettings()
	 */
	@Override
	protected void initSettings(ISettingsHandler settings) {
		super.initSettings(settings);
		settings.get(ConsoleRamArea.settingEnhRam).setBoolean(true);
	}
}
