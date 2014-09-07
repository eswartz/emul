/*
  F99bMachineModel.java

  (c) 2008-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.machine;

import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryModel;

/**
 * This is a machine model for F9900 where no client is needed (chars emitted to stdio)
 * @author ejs
 *
 */
public class Forth9900StandaloneMachineModel extends Forth9900MachineModel {

	public static final String ID = "Forth9900Standalone";
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return ID;
	}
	

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#getName()
	 */
	@Override
	public String getName() {
		return "FORTH9900 Machine (Standalone)";
	}
	
	public IMemoryModel createMemoryModel(IMachine machine) {
		return new Forth9900StandaloneMemoryModel(machine);
	}
}
