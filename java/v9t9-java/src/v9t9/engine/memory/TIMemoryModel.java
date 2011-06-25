/**
 * 
 */
package v9t9.engine.memory;

import v9t9.emulator.hardware.memory.mmio.*;

/**
 * @author ejs
 *
 */
public interface TIMemoryModel extends MemoryModel {

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
}
