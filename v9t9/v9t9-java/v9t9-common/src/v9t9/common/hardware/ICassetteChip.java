/**
 * 
 */
package v9t9.common.hardware;

import ejs.base.properties.IPersistable;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.sound.ICassetteVoice;

/**
 * @author ejs
 *
 */
public interface ICassetteChip extends IPersistable, IRegisterAccess {
	ICassetteVoice getCassetteVoice();
	void reset();
}
