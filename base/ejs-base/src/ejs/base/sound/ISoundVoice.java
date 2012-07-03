/**
 * 
 */
package ejs.base.sound;

import javax.sound.sampled.AudioFormat;

/**
 * @author ejs
 *
 */
public interface ISoundVoice {

	void setFormat(AudioFormat format);
	
	/** Generate samples from 'from' to 'to' in 'soundGeneratorWorkBuffer' 
	 * @return true if sound generated
	 */
	boolean generate(float[] soundGeneratorWorkBuffer,
			int from, int to);

	/** Tell if the voice is active (i.e. producing sound, not muted) */
	boolean isActive();

	/**
	 * Set the rate in Hz at which the voice is generated.
	 * @param soundClock
	 */
	void setSoundClock(int soundClock);

	/**
	 * Reset the voice
	 */
	void reset();

}