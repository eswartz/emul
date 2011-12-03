/**
 * 
 */
package v9t9.engine.memory;

import v9t9.emulator.common.IBaseMachine;
import v9t9.emulator.common.IEventNotifier;

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
