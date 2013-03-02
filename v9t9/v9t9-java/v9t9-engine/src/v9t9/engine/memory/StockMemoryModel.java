/*
  StockMemoryModel.java

  (c) 2008-2012 Edward Swartz

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
package v9t9.engine.memory;


import v9t9.common.events.IEventNotifier;
import v9t9.common.machine.IBaseMachine;
import v9t9.common.memory.IMemory;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryModel;
import v9t9.common.memory.MemoryEntryInfo;

/**
 * @author ejs
 *
 */
public class StockMemoryModel implements IMemoryModel {

	private Memory memory;
	private MemoryDomain CPU;

	public StockMemoryModel() {
		memory = new Memory();
		CPU = new MemoryDomain(IMemoryDomain.NAME_CPU);
		memory.addDomain(IMemoryDomain.NAME_CPU, CPU);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryModel#resetMemory()
	 */
	@Override
	public void resetMemory() {
		
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryModel#getConsole()
	 */
	public IMemoryDomain getConsole() {
		return CPU;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryModel#getLatency(int)
	 */
	/**
	 * @param addr  
	 */
	public int getLatency(int addr) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryModel#getMemory()
	 */
	public IMemory getMemory() {
		return memory;
	}

	public void initMemory(IBaseMachine machine) {
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.memory.MemoryModel#loadMemory(v9t9.emulator.clients.builtin.IEventNotifier)
	 */
	@Override
	public void loadMemory(IEventNotifier eventNotifier) {
		
	}

	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryModel#getOptionalRomProperties()
	 */
	@Override
	public MemoryEntryInfo[] getOptionalRomMemoryEntries() {
		return new MemoryEntryInfo[0];
	}
	/* (non-Javadoc)
	 * @see v9t9.common.memory.IMemoryModel#getRequiredRomProperties()
	 */
	@Override
	public MemoryEntryInfo[] getRequiredRomMemoryEntries() {
		return new MemoryEntryInfo[0];
	}
}
