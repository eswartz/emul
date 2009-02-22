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
public class EnhancedCompatibleConsoleMemoryModel extends StandardConsoleMemoryModel {

	public EnhancedCompatibleConsoleMemoryModel() {
		super();
	}

	@Override
	protected void initSettings() {
		ConsoleRamArea.settingEnhRam.setBoolean(true);

	}
}
