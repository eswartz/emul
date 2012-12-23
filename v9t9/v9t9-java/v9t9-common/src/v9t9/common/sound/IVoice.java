/**
 * 
 */
package v9t9.common.sound;

import ejs.base.properties.IPersistable;

/**
 * This represents the parameters controlling a single voice
 * @author ejs
 *
 */
public interface IVoice extends IPersistable {
	/** Get the identifier for the voice, in register naming */
	String getId();
	/**
	 * Get the name or description of the voice
	 */
	String getName();
	
	/** Get base register number for voice */
	int getBaseRegister();
	/** Get register count for voice */
	int getRegisterCount();

	int getRegister(int reg);
	void setRegister(int reg, int newValue);
}
