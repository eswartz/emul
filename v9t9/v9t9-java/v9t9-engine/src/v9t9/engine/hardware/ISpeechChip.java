/**
 * 
 */
package v9t9.engine.hardware;

import v9t9.base.properties.IPersistable;
import v9t9.base.sound.ISoundVoice;

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
