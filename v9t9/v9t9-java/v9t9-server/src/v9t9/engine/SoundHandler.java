/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 29, 2004
 *
 */
package v9t9.engine;

import org.ejs.coffee.core.properties.SettingProperty;



/**
 * This interface wraps the sound support for the emulator.
 * @author ejs
 */
public interface SoundHandler {
	
	public static SettingProperty settingPlaySound = new SettingProperty("PlaySound", new Boolean(true));
	public static SettingProperty settingSoundVolume = new SettingProperty("SoundVolume", new Integer(10));


	/**
	 * Dispose sound and turn off audio
	 */
	void dispose();
	
	/**
	 * Update the sound for the given position in
	 * the machine tick.  
	 * @param pos current cycle
	 * @param total total cycles per tick
	 */
	void updateVoice(int pos, int total);
	
	/**
	 * Fill out and flush the sound accumulated for this tick
	 * @param pos current cycle
	 * @param total total cycles per tick
	 */
	void flushAudio(int pos, int total);
	

	/**
	 * Handle one sample (signed 16-bit) of speech data.
	 * @param sample
	 */
	void speech(short sample);
	
}

