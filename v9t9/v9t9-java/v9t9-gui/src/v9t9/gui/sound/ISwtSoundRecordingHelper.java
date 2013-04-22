/**
 * 
 */
package v9t9.gui.sound;

import org.eclipse.swt.widgets.Menu;

import v9t9.common.sound.ISoundRecordingHelper;

/**
 * @author ejs
 *
 */
public interface ISwtSoundRecordingHelper extends ISoundRecordingHelper {

	/**
	 * Add a record/stop recording item to a menu
	 * @param menu
	 * @return the menu
	 */
	Menu populateSoundMenu(Menu menu);

}