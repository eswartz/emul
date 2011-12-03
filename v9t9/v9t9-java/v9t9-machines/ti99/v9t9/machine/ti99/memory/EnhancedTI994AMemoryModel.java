/**
 * 
 */
package v9t9.machine.ti99.memory;

/**
 * @author ejs
 *
 */
public class EnhancedTI994AMemoryModel extends TI994AStandardConsoleMemoryModel {
	/* (non-Javadoc)
	 * @see v9t9.machine.ti99.memory.TI994AStandardConsoleMemoryModel#initSettings()
	 */
	@Override
	protected void initSettings() {
		super.initSettings();
		ConsoleRamArea.settingEnhRam.setBoolean(true);
	}
}
