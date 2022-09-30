/*
  ISpeechChip.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.hardware;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.SettingSchema;
import v9t9.common.speech.ISpeechDataSender;
import v9t9.common.speech.ILPCParametersListener;
import v9t9.common.speech.ISpeechPhraseListener;
import ejs.base.properties.IPersistable;

/**
 * @author ejs
 *
 */
public interface ISpeechChip extends IPersistable {
	SettingSchema settingGenerateSpeech = new SettingSchema(
			ISettingsHandler.USER,
			"GenerateSpeech",
			Boolean.TRUE);

	SettingSchema settingTalkSpeed = new SettingSchema(
			ISettingsHandler.USER,
			"TalkSpeed",
			1.0);
	SettingSchema settingPitchAdjust = new SettingSchema(
			ISettingsHandler.USER,
			"PitchAdjust",
			1.0);
	SettingSchema settingPitchRangeAdjust = new SettingSchema(
			ISettingsHandler.USER,
			"PitchRangeAdjust",
			1.0);
	SettingSchema settingPitchMidRangeAdjustRate = new SettingSchema(
			ISettingsHandler.USER,
			"PitchMidRangeAdjustRate",
			4);
	SettingSchema settingForceUnvoiced = new SettingSchema(
			ISettingsHandler.USER,
			"ForceUnvoiced",
			Boolean.FALSE);
	SettingSchema settingLogSpeech = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"LogSpeech",
			Integer.valueOf(0));
			
	/**
	 * Read the next byte from memory 
	 * @return
	 */
	byte read();

	/**
	 * Write a byte to the command buffer 
	 * @param val
	 */
	void write(byte val);
	
	void addSpeechListener(ISpeechDataSender sender);
	void removeSpeechListener(ISpeechDataSender sender);

	/**
	 * Reset synthesizer
	 */
	void reset();

	void addPhraseListener(ISpeechPhraseListener phraseListener);
	void removePhraseListener(ISpeechPhraseListener phraseListener);
	
	void addParametersListener(ILPCParametersListener listener);
	void removeParametersListener(ILPCParametersListener listener);
}
