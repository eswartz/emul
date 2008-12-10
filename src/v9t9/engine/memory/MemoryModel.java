/**
 * 
 */
package v9t9.engine.memory;

import v9t9.emulator.Machine;

/**
 * This defines the model for memory in the emulator.
 * @author ejs
 *
 */
public interface MemoryModel {
	/** Get the latency for accessing memory at this address.
	 * This is typically used only for initializing MemoryDomain and MemoryAreas,
	 * which take over the task of tracking memory cycle counts.
	 * @param addr
	 * @return number of cycles to access a byte
	 */
	int getLatency(int addr);
	
	/**
	 * Get the memory defined by the model.
	 */
	Memory createMemory();
	
	/**
	 * Initialize the memory for this machine
	 */
	void initMemory(Machine machine);
	
	/**
	 * Get the console memory.
	 */
	MemoryDomain getConsole();
}
