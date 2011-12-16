/**
 * 
 */
package v9t9.common.hardware;

import ejs.base.properties.IPersistable;
import ejs.base.sound.ISoundVoice;

/**
 * @author ejs
 *
 */
public interface ISpeechChip extends IPersistable {
	ISoundVoice[] getSpeechVoices();

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
}
