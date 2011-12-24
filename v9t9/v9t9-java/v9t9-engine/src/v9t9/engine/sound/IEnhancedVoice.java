/**
 * 
 */
package v9t9.engine.sound;

/**
 * @author ejs
 *
 */
public interface IEnhancedVoice extends IVoice {

	void setEffect(int effect, byte value);
	byte getEffectValue(int effect);
}
