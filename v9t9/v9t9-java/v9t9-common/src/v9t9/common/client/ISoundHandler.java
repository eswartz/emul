/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 29, 2004
 *
 */
package v9t9.common.client;

import v9t9.base.settings.SettingProperty;




/**
 * This interface wraps the sound support for the emulator.
 * @author ejs
 */
public interface ISoundHandler {
	
	public static final SettingProperty settingPlaySound = new SettingProperty("PlaySound", new Boolean(true));
	public static final SettingProperty settingSoundVolume = new SettingProperty("SoundVolume", new Integer(10));


	/**
	 * Dispose sound and turn off audio
	 */
	void dispose();
	
	/**
	 * Generate the sound for the given range of time.
	 */
	void generateSound();
	
	/**
	 * Fill out and flush the sound accumulated for this tick
	 */
	void flushAudio();
	

	/**
	 * Handle one sample (signed 16-bit) of speech data.
	 */
	void speech();
	
}

