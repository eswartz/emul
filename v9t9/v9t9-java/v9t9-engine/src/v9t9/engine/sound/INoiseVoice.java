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
	/** @see TMS9919Constants#REG_OFFS_NOISE_CONTROL */
	void setControl(int value);
	/** @see TMS9919Constants#REG_OFFS_NOISE_CONTROL */
	int getControl();
}
