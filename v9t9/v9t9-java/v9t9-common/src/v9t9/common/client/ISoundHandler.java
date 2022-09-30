/*
  ISoundHandler.java

  (c) 2005-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.client;

import java.util.Map;

import ejs.base.sound.ISoundOutput;

import v9t9.common.settings.SettingSchema;
import v9t9.common.sound.ISoundGenerator;




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
			"SoundVolume", Integer.valueOf(5));
	SettingSchema settingRecordSoundOutputFile = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"RecordSoundOutputFile", String.class, null);
	SettingSchema settingRecordSpeechOutputFile = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"RecordSpeechOutputFile", String.class, null);

	SettingSchema settingPauseSoundRecording = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"PauseSoundRecording", Boolean.FALSE);

	void register(ISoundGenerator generator);
	Map<ISoundGenerator, ISoundOutput> getGeneratorToOutputMap();
	
	/**
	 * Dispose sound and turn off audio
	 */
	void dispose();
	
	/**
	 * Generate the sound for the given range of time.
	 * @param pos cycles
	 * @param total total cycles
	 */
	void generateSound(int pos, int total);
	
	/**
	 * Fill out and flush the sound accumulated for this tick
	 * @param pos cycles
	 * @param total total cycles
	 */
	void flushAudio(int pos, int total);
	

	/**
	 * Handle one sample (signed 16-bit) of speech data.
	 */
	void speech();
}

