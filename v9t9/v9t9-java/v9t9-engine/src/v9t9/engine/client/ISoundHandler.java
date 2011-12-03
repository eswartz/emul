/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 29, 2004
 *
 */
package v9t9.engine.client;

import v9t9.base.properties.SettingProperty;



/**
 * This interface wraps the sound support for the emulator.
 * @author ejs
 */
public interface ISoundHandler {
	
	public static SettingProperty settingPlaySound = new SettingProperty("PlaySound", new Boolean(true));
	public static SettingProperty settingSoundVolume = new SettingProperty("SoundVolume", new Integer(10));


	/**
	 * Dispose sound and turn off audio
	 */
	void dispose();
	
	/**
	 * Generate the sound for the given range of time.
	 * @param pos current cycle
	 * @param total total cycles per tick
	 */
	void generateSound(int pos, int total);
	
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

