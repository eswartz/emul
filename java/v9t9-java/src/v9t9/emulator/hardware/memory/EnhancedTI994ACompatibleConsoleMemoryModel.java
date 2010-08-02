/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.emulator.hardware.memory;


/**
 * Enhanced console memory model.
 * <p>
 * This has:
 * @author ejs
 */
public class EnhancedTI994ACompatibleConsoleMemoryModel extends TI994AStandardConsoleMemoryModel {

	public EnhancedTI994ACompatibleConsoleMemoryModel() {
		super();
	}

	@Override
	protected void initSettings() {
		ConsoleRamArea.settingEnhRam.setBoolean(true);

	}
}
