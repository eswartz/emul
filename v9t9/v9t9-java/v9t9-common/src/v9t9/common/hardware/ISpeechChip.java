/**
 * 
 */
package v9t9.common.hardware;

import v9t9.common.speech.ISpeechDataSender;
import ejs.base.properties.IPersistable;

/**
 * @author ejs
 *
 */
public interface ISpeechChip extends IPersistable {
	//ISoundVoice[] getSpeechVoices();

	void generateSpeech();
	
	/**
	 * @return
	 */
	byte read();

	/**
	 * @param val
	 */
	void write(byte val);

	/**
	 * @return
	 */
	int getGenerateRate();
	
	void addSpeechListener(ISpeechDataSender sender);

	/**
	 * 
	 */
	void reset();
}
