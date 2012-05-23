/**
 * 
 */
package v9t9.common.keyboard;

/**
 * @author ejs
 *
 */
public interface IKeyboardListener {

	/** Shift state changed 
	 * @param mask
	 * @see IKeyboardState#CTRL
	 * @see IKeyboardState#FCTN
	 * @see IKeyboardState#SHIFT
	 */
	void shiftChangeEvent(byte mask);
	
	/** A human-readable key was pressed or released.
	 * The character passed is the unshifted, unmodified
	 * key shown on the emulated keyboard.  Combined
	 * with shift changes, represents the full state
	 * used by the emulated machine to interpret
	 * the keypress.
	 * @param ch the keyboard character; SPACE is ' ', ENTER is '\r'
	 * @param pressed true for press, false for release     
	 */
	void asciiKeyEvent(char ch, boolean pressed);
	
	/** A non-human-readable key (neither ASCII-representable
	 * nor a shift key) was pressed or released.
	 * The character passed is the unshifted, unmodified
	 * key.  Combined with shift changes, represents the full state
	 * used by the emulated machine to interpret
	 * the keypress.
	 * @param ch the keyboard character     
	 * @param pressed true for press, false for release     
	 */
	void otherKeyEvent(int ch, boolean pressed);
	
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
