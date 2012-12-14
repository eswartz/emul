/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Dec 29, 2004
 *
 */
package v9t9.common.client;

import v9t9.common.settings.SettingSchema;




/**
 * This interface wraps the sound support for the emulator.
 * @author ejs
 */
public interface ISoundHandler {
	
	SettingSchema settingPlaySound = new SettingSchema(
			ISettingsHandler.WORKSPACE,
			"PlaySound", new Boolean(true));
	SettingSchema settingSoundVolume = new SettingSchema(
			ISettingsHandler.WORKSPACE,
			"SoundVolume", new Integer(5));


	/**
	 * Dispose sound and turn off audio
	 */
	void dispose();
	
	/**
	 * Generate the sound for the given range of time.
	 * @param pos TODO
	 * @param total TODO
	 */
	void generateSound(int pos, int total);
	
	/**
	 * Fill out and flush the sound accumulated for this tick
	 * @param pos TODO
	 * @param total TODO
	 */
	void flushAudio(int pos, int total);
	

	/**
	 * Handle one sample (signed 16-bit) of speech data.
	 */
	void speech();
	
}

