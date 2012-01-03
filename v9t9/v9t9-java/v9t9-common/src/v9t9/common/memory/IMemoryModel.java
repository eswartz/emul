/**
 * 
 */
package v9t9.common.memory;

import ejs.base.properties.IProperty;
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
	 * Return an array of properties specifying the names of
	 * required ROMs (which, perhaps, the user might edit or configure)
	 * @return
	 */
	IProperty[] getRequiredRomProperties();
	

	/**
	 * Return an array of properties specifying the names of
	 * optional ROMs (which, perhaps, the user might edit or configure)
	 * @return
	 */
	IProperty[] getOptionalRomProperties();
	
}
