/**
 * 
 */
package v9t9.common.keyboard;

/**
 * Reports changes in high-level keyboard changes
 * @author ejs
 *
 */
public interface IKeyboardListener {

	/** Shift state changed 
	 * @param mask logical OR of states representing current state
	 * @see KeyboardConstants#MASK_...
	 */
	void shiftChangeEvent(byte mask);
	
	/** A non-human-readable key (neither ASCII-representable
	 * nor a shift key) was pressed or released.
	 * The character passed is the unshifted, unmodified
	 * key.  Combined with shift changes, represents the full state
	 * used by the emulated machine to interpret
	 * the keypress.
	 * @param key the keyboard character (see KeyboardConstants#KEY_...)
	 * @param ch the keyboard character; SPACE is ' ', ENTER is '\r'
	 * @param pressed true for press, false for release     
	 */
	void keyEvent(int key, char ch, boolean pressed);
	
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
