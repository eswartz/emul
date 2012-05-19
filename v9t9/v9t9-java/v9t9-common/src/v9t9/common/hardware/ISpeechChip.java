/**
 * 
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
	
	SettingSchema settingTalkSpeed = new SettingSchema(
			ISettingsHandler.INSTANCE,
			"TalkSpeed",
			1.0);
	SettingSchema settingLogSpeech = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"LogSpeech",
			new Integer(0));
			
	SettingSchema settingAllowLPCTimeouts = new SettingSchema(
			ISettingsHandler.INSTANCE,
			"LPCTimeouts",
			Boolean.FALSE);

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
