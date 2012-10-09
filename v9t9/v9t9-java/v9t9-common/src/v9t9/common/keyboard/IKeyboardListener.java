/**
 * 
 */
package v9t9.common.keyboard;

import java.util.Collection;

/**
 * Reports changes in high-level keyboard changes
 * @author ejs
 *
 */
public interface IKeyboardListener {

	/** A key was pressed or released.
	 * @param key the keyboard character (see KeyboardConstants#KEY_...)
	 * @param pressed true for press, false for release     
	 */
	void keyEvent(Collection<Integer> keys, boolean pressed);
	
	/**
	 * Joystick state changed.
	 * @param num
	 * @param mask
	 * @see IKeyboardState#JOY_UP_R
	 * @see IKeyboardState#JOY_DOWN_R
	 * @see IKeyboardState#JOY_LEFT_R
	 * @see IKeyboardState#JOY_RIGHT_R
	 * @see IKeyboardState#JOY_FIRE_R
	 */
	void joystickChangeEvent(int num, byte mask);
}
