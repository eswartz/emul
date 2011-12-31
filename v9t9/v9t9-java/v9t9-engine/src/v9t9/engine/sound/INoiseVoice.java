/**
 * 
 */
package v9t9.engine.sound;

/**
 * This represents the extra parameters influencing a noise voice
 * @author ejs
 *
 */
public interface INoiseVoice extends IClockedVoice {
	/** The control nybble for the noise generator (xyy where x is 0=periodic, 1=noise and yy is freq selector) */
	void setControl(int value);
	/** The control nybble for the noise generator (xyy where x is 0=periodic, 1=noise and yy is freq selector)  */
	int getControl();
}
