/**
 * 
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
