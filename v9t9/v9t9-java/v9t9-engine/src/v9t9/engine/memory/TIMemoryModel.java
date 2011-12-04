/**
 * 
 */
package v9t9.engine.memory;

import v9t9.common.memory.IMemoryModel;

/**
 * @author ejs
 *
 */
public interface TIMemoryModel extends IMemoryModel {

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
