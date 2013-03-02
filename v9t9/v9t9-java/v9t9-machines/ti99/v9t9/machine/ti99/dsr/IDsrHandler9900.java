/*
  IDsrHandler9900.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.dsr;

import java.io.IOException;

import v9t9.common.dsr.IDsrHandler;
import v9t9.common.dsr.IMemoryTransfer;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntryFactory;

public interface IDsrHandler9900 extends IDsrHandler {

	/** Get the CRU base */
	short getCruBase();


	/** Handle the DSR call (DSR opcode in mapped ROM)
	 * @param xfer method of moving memory around
	 * @param code the operand of the Idsr instruction
	 * @return true if handled the operand, false if the device doesn't match
	 */
	boolean handleDSR(IMemoryTransfer xfer, short code);

	/** Activate the DSR (should be called when the ROM memory entry is mapped) 
	 * @param console
	 * @param memoryEntryFactory TODO
	 * @throws IOException */
	void activate(IMemoryDomain console, IMemoryEntryFactory memoryEntryFactory) throws IOException;
	/** Dectivate the DSR (should be called when the ROM memory entry is unmapped) 
	 * @param console */
	void deactivate(IMemoryDomain console);
}
