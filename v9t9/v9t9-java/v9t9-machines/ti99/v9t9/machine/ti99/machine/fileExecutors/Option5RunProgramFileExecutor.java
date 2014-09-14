/*
  EditAssmRunProgramFileExecutor.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.machine.fileExecutors;

import v9t9.common.client.IKeyboardHandler;
import v9t9.common.events.NotifyException;
import v9t9.common.files.IFileExecutor;
import v9t9.common.machine.IMachine;
import v9t9.common.modules.IModule;

/**
 * @author ejs
 *
 */
public class Option5RunProgramFileExecutor implements IFileExecutor {

	private IModule module;
	private String devicePath;

	public Option5RunProgramFileExecutor(IModule module, String devicePath) {
		this.module = module;
		this.devicePath = devicePath;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#getLabel()
	 */
	@Override
	public String getLabel() {
		return "Load program file " + devicePath + " with " + module.getName();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Load module " + module.getName() + ", and run the program file " + devicePath + " using 'Option 5'.";
				
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#run(v9t9.common.machine.IMachine)
	 */
	@Override
	public void run(IMachine machine) throws NotifyException {
		machine.getModuleManager().switchModule(module);
		machine.reset();
		machine.getKeyboardHandler().pasteText(IKeyboardHandler.WAIT_VIDEO + " "+
				IKeyboardHandler.WAIT_VIDEO + "2"+	// space for title, 2 for E/A
				IKeyboardHandler.WAIT_FOR_FLUSH + IKeyboardHandler.WAIT_VIDEO + 
				"5" + devicePath + "\n");
	}

}
