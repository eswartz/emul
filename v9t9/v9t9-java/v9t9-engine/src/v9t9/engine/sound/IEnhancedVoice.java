/**
 * 
 */
package v9t9.engine.sound;

import v9t9.common.sound.IVoice;

/**
 * @author ejs
 *
 */
public interface IEnhancedVoice extends IVoice {

	void setEffect(int effect, byte value);
	byte getEffectValue(int effect);
}
