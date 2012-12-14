/**
 * 
 */
package v9t9.common.sound;

import v9t9.common.machine.IRegisterAccess;
import ejs.base.sound.ISoundVoice;

/**
 * @author ejs
 *
 */
public interface ISoundGenerator extends IRegisterAccess.IRegisterWriteListener {

	ISoundVoice[] getSoundVoices();
	//void tick();
}
