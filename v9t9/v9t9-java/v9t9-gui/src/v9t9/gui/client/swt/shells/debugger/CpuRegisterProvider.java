/*
  CpuRegisterProvider.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.debugger;

import v9t9.common.machine.IMachine;

/**
 * @author ejs
 *
 */
public class CpuRegisterProvider extends BaseRegisterProvider {
	/**
	 * @param machine
	 */
	public CpuRegisterProvider(IMachine machine) {
		super(machine, machine.getCpu().getState());
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.debugger.IRegisterProvider#getNumDigits()
	 */
	@Override
	public int getNumDigits() {
		return 4;
	}

}
