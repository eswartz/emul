/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.machine.ti99.memory;

import v9t9.engine.files.DataFiles;


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
		DataFiles.addSearchPath("../../build/forth");
		super.initSettings();
		ConsoleRamArea.settingEnhRam.setBoolean(true);


	}
}
