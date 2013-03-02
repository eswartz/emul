/*
  IMemoryModel.java

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
package v9t9.common.memory;

import v9t9.common.events.IEventNotifier;
import v9t9.common.machine.IBaseMachine;

/**
 * This defines the model for memory in the emulator.
 * @author ejs
 *
 */
public interface IMemoryModel {
	/**
	 * Get the memory defined by the model.
	 */
	IMemory getMemory();
	
	/**
	 * Initialize the memory for this machine
	 */
	void initMemory(IBaseMachine machine);
	
	/**
	 * Get the console memory.
	 */
	IMemoryDomain getConsole();

	/**
	 * Load memory
	 * @param eventNotifier 
	 */
	void loadMemory(IEventNotifier eventNotifier);

	/**
	 * Reset memory to load-time state
	 */
	void resetMemory();

	/**
	 * Return an array of entries specifying the characteristics
	 * of required ROMs
	 * @return
	 */
	MemoryEntryInfo[] getRequiredRomMemoryEntries();
	

	/**
	 * Return an array of entries specifying the characteristics
	 * of optional ROMs
	 * @return
	 */
	MemoryEntryInfo[] getOptionalRomMemoryEntries();
	
}
