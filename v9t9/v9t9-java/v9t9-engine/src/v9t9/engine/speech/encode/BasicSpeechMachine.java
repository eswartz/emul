/*
  BasicSpeechMachine.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.speech.encode;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.files.IFileExecutionHandler;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IMachineModel;
import v9t9.engine.machine.MachineBase;
import v9t9.engine.machine.NullFileExecutionHandler;

/**
 * @author ejs
 *
 */
public class BasicSpeechMachine extends MachineBase implements IMachine {

	/**
	 * @param settings
	 * @param machineModel
	 */
	public BasicSpeechMachine(ISettingsHandler settings,
			IMachineModel machineModel) {
		super(settings, machineModel);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.machine.MachineBase#createFileExecutionHandler()
	 */
	@Override
	protected IFileExecutionHandler createFileExecutionHandler() {
		return new NullFileExecutionHandler();
	}
	
}
