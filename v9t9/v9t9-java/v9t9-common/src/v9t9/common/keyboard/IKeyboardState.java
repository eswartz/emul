/**
 * 
 */
package v9t9.common.keyboard;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.settings.SettingSchema;

/**
 * @author ejs
 *
 */
public interface IKeyboardState {
	public static SettingSchema settingBackspaceIsCtrlH = new SettingSchema(
			ISettingsHandler.WORKSPACE, 
			"BackspaceIsControlH", Boolean.FALSE);

    
	boolean isAsciiDirectKey(char x);

	/**
	 * Fully reset keyboard state
	 */
	void resetKeyboard();
	/**
	 * Fully reset joystick
	 */
	void resetJoystick();
	

	/**
	 * Clear the state of all keys represented, before updating incrementally
	 */
	void incrClearKeyboard();

	/**
	 * Incrementally modify the state of the given key
	 * @param onoff
	 * @param key one of {@link KeyboardConstants#KEY_xxx}, including shifts
	 */
	void incrSetKey(boolean onoff, int key);

	/**
	 * Apply recent changes via {@link #incrSetKey(boolean, int)} 
	 * etc. to expose a unified keyboard state
	 */
	void applyIncrKeyState();

	public static final int JOY_B = 4; // set buttons
	public static final int JOY_X = 1;
	public static final int JOY_Y = 2;
	
	/**
	 * 
	 * @param joy 1 or 2
	 * @param mask JOY_X, JOY_Y, JOY_B
	 * @param x neg or pos or 0
	 * @param y neg or pos or 0
	 * @param fire boolean
	 */
	void setJoystick(int joy, int mask, int x, int y, boolean fire);

	/** Get the low-level hardware bits for the keyboard column (8) */
	int getKeyboardRow(int column);

	boolean anyKeyPressed();

	boolean isSet(byte shift, int key);

	byte getShiftMask();

	void setLockMask(byte locks);
	byte getLockMask();

	void addKeyboardListener(IKeyboardListener listener);
	void removeKeyboardListener(IKeyboardListener listener);

	/**
	 * Modify the shift state of the given bits in shift
	 * @param onoff true to enable, false to disable
	 * @param shift mask of {@link KeyboardConstants#MASK_xxx} bits to set or reset
	 */
	void changeShifts(boolean onoff, byte shift);


	/**
	 * Modify the lock state of the given bits in lock
	 * @param onoff true to enable, false to disable
	 * @param lock mask of {@link KeyboardConstants#MASK_xxx} bits to set or reset
	 */
	void changeLocks(boolean onoff, byte lock);
	/**
	 * Toggle the lock state of the given bits in lock
	 * @param lock mask of {@link KeyboardConstants#MASK_xxx} bits to set or reset
	 */
	void toggleKeyboardLocks(byte lock);

}