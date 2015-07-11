/*
  ExtBasicAutoLoadFileExecutor.java

  (c) 2013-2014 Ed Swartz

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
public class ExtBasicAutoLoadFileExecutor implements IFileExecutor {

	private IModule module;

	public ExtBasicAutoLoadFileExecutor(IModule module) {
		this.module = module;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#getLabel()
	 */
	@Override
	public String getLabel() {
		return "Auto-run with " + module.getName();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Load module " + module.getName() + " and let it auto-run the program on the disk.\n\n"
				+"This would be done by hand with:\n\n"+
				"RUN \"DSK1.LOAD\""; 
				
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#run(v9t9.common.machine.IMachine)
	 */
	@Override
	public void run(IMachine machine) throws NotifyException {
		machine.getModuleManager().switchModule(module);
		machine.reset();
		machine.getKeyboardHandler().pasteText(
				IKeyboardHandler.WAIT_FOR_FLUSH + IKeyboardHandler.WAIT_FOR_FLUSH + 
				" 2");	// space for title, 2 for extended basic
	}

}
