/**
 * 
 */
package v9t9.engine.memory;

import v9t9.emulator.common.IEventNotifier;
import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.memory.mmio.GplMmio;
import v9t9.emulator.hardware.memory.mmio.SoundMmio;
import v9t9.emulator.hardware.memory.mmio.SpeechMmio;
import v9t9.emulator.hardware.memory.mmio.VdpMmio;

/**
 * This defines the model for memory in the emulator.
 * @author ejs
 *
 */
public interface MemoryModel {
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
	
	/**
	 * Get the VDP MMIO. 
	 */
	VdpMmio getVdpMmio();
	
	/**
	 * Get the GPL MMIO. 
	 */
	GplMmio getGplMmio();
	
	/**
	 * Get the sound MMIO. 
	 */
	SoundMmio getSoundMmio();
	
	/**
	 * Get the speech MMIO. 
	 */
	SpeechMmio getSpeechMmio();

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
