/**
 * 
 */
package v9t9.common.speech;

import ejs.base.sound.ISoundVoice;

/**
 * @author ejs
 *
 */
public interface ISpeechSoundVoice extends ISoundVoice {

	/** Remember one sample; this should be updated e.g. 8000 times a second */
	void addSample(short sample);

	/**
	 * @return
	 */
	int getSampleCount();

}