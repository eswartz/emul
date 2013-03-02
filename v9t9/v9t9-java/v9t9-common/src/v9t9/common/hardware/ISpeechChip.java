/*
  ISpeechChip.java

  (c) 2011-2013 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
			new Integer(0));
			
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
