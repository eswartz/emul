/*
  EditAssmLoadAndRunFileExecutor.java

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
public class EditAssmLoadAndRunFileExecutor implements IFileExecutor {

	private IModule module;
	private String devicePath;
	private String entry;

	public EditAssmLoadAndRunFileExecutor(IModule module, String devicePath, String entry) {
		this.module = module;
		this.devicePath = devicePath;
		this.entry = entry;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#getLabel()
	 */
	@Override
	public String getLabel() {
		if (entry == null)
			return "Load file " + devicePath + " with " + module.getName();
		else
			return "Load file " + devicePath + " and run " + entry + " with " + module.getName();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Load module " + module.getName() + ", load the object file '" 
				+ devicePath + "' using 'Option 3'"
				+ (entry != null ? ", then run at the entry '" + entry + "'." : "");
				
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.files.IFileExecutor#run(v9t9.common.machine.IMachine)
	 */
	@Override
	public void run(IMachine machine) throws NotifyException {
		machine.getModuleManager().switchModule(module);
		machine.reset();
		machine.getKeyboardHandler().pasteText(" 2"+	// space for title, 2 for extended basic
				IKeyboardHandler.WAIT_FOR_FLUSH + IKeyboardHandler.WAIT_VIDEO + 
				"3" + devicePath + "\n" + IKeyboardHandler.WAIT_FOR_FLUSH + IKeyboardHandler.WAIT_VIDEO +
				"\n"+
				entry + "\n");
	}

}
