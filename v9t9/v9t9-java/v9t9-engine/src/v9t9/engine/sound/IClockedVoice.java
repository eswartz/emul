/**
 * 
 */
package v9t9.engine.sound;

import v9t9.common.sound.IVoice;

/**
 * This represents the parameters controlling a single voice
 * which has a volume and a frequency
 * @author ejs
 *
 */
public interface IClockedVoice extends IVoice {
	/** Set the voice frequency */
	void setPeriod(int hz);
	/** Get the voice frequency */
	int getPeriod();
	
	/** Set the voice volume (0=silent, 255=max) */
	void setAttenuation(int vol);
	/** Get the voice attenuation (0=silent, 255=max) */
	int getAttenuation();

}
