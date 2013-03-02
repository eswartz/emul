/*
  ISoundHandler.java

  (c) 2005-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.client;

import ejs.base.sound.ISoundOutput;
import v9t9.common.settings.SettingSchema;




/**
 * This interface wraps the sound support for the emulator.
 * @author ejs
 */
public interface ISoundHandler {
	
	SettingSchema settingPlaySound = new SettingSchema(
			ISettingsHandler.MACHINE,
			"PlaySound", Boolean.TRUE);
	SettingSchema settingSoundVolume = new SettingSchema(
			ISettingsHandler.MACHINE,
			"SoundVolume", new Integer(5));
	SettingSchema settingRecordSoundOutputFile = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"RecordSoundOutputFile", String.class, null);
	SettingSchema settingRecordSpeechOutputFile = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"RecordSpeechOutputFile", String.class, null);

	SettingSchema settingPauseSoundRecording = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"PauseSoundRecording", Boolean.FALSE);

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
	
	ISoundOutput getSoundOutput();
	ISoundOutput getSpeechOutput();
	
}

