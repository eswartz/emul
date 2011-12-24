/**
 * 
 */
package v9t9.audio.speech;

import v9t9.common.machine.IMachine;
import v9t9.common.speech.ISpeechGenerator;

/**
 * @author ejs
 *
 */
public class SpeechGeneratorFactory {

	/**
	 * @param machine
	 * @return
	 */
	public static ISpeechGenerator createSpeechGenerator(IMachine machine) {
		return new SpeechTMS5220Generator(machine.getSpeech());
	}

}
