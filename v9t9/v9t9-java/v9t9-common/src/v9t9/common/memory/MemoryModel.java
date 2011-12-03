/**
 * 
 */
package v9t9.common.memory;

import v9t9.common.events.IEventNotifier;
import v9t9.common.settings.IBaseMachine;

/**
 * This defines the model for memory in the emulator.
 * @author ejs
 *
 */
public interface MemoryModel {
	/**
	 * Get the memory defined by the model.
	 */
	Memory getMemory();
	
	/**
	 * Initialize the memory for this machine
	 */
	void initMemory(IBaseMachine machine);
	
	/**
	 * Get the console memory.
	 */
	MemoryDomain getConsole();

	/**
	 * Load memory
	 * @param eventNotifier 
	 */
	void loadMemory(IEventNotifier eventNotifier);

	/**
	 * Reset memory to load-time state
	 */
	void resetMemory();
	
}
