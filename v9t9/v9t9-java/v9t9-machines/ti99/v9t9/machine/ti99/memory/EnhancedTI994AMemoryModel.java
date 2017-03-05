/*
  EnhancedTI994AMemoryModel.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
