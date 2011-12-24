/**
 * 
 */
package v9t9.engine.sound;

/**
 * This represents the parameters controlling a single voice
 * which has a volume and a frequency
 * @author ejs
 *
 */
public interface IClockedVoice extends IVoice {
	/** Set the voice period */
	void setPeriod(int period);
	/** Get the voice period */
	int getPeriod();
	
	/** Set the voice attenuation (0-15) */
	void setAttenuation(int vol);
	/** Get the voice attenuation (0-15) */
	int getAttenuation();

}
