/*
  IDsrHandler9900.java

  (c) 2008-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
