/*
  Basic9900MachineModelTest.java

  (c) 2014 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.common.tests;

import v9t9.common.cpu.ICpu;
import v9t9.common.machine.IMachine;
import v9t9.machine.ti99.cpu.Cpu9900;

/**
 * @author ejs
 *
 */
public class Basic9900MachineModelTest extends BasicMachineModelTest {

	/**
	 * 
	 */
	public Basic9900MachineModelTest() {
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return "Basic9900";
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#getName()
	 */
	@Override
	public String getName() {
		return "Basic 9900";
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createCPU(v9t9.common.machine.IMachine)
	 */
	@Override
	public ICpu createCPU(IMachine machine) {
		return new Cpu9900(machine, null);
	}

}
