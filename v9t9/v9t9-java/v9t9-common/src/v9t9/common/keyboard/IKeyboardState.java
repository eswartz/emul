/**
 * 
 */
package v9t9.common.keyboard;

import v9t9.common.machine.IBaseMachine;

/**
 * @author ejs
 *
 */
public interface IKeyboardState {

	/* Masks, corresponding to column 0 */
	public static final byte SHIFT = 0x20;
	public static final byte FCTN = 0x10;
	public static final byte CTRL = 0x40;

	boolean isAsciiDirectKey(char x);

	void setPasteKeyDelay(int times);

	void resetKeyboard();

	void resetJoystick();

	/**
	 * Set a key in the map.
	 * @param realKey TODO
	 * @param onoff true: pressed, false: released
	 * @param synthetic if true, the shift + key are sent together from a synthetic
	 * event; else, shifts are sent in separate events from keys, so track them
	 * @param shift FCTN, SHIFT, CTRL mask
	 * @param key normalized ASCII key: no lowercase or shifted characters
	 * @param when time in ms when key was detected
	 */
	void setKey(int realKey, boolean onoff, boolean synthetic, int shift,
			int key, long when);

	void pushQueuedKey();

	boolean isSet(byte shift, int key);

	public static final int JOY_B = 4 // set buttons
	;
	public static final int JOY_X = 1;
	public static final int JOY_Y = 2;
	public static final int JOY_FIRE_R = 7;
	public static final int JOY_LEFT_R = 6;
	public static final int JOY_RIGHT_R = 5;
	public static final int JOY_DOWN_R = 4;
	public static final int JOY_UP_R = 3;

	/**
	 * 
	 * @param joy 1 or 2
	 * @param mask JOY_X, JOY_Y, JOY_B
	 * @param x neg or pos or 0
	 * @param y neg or pos or 0
	 * @param fire boolean
	 * @param when TODO
	 */
	void setJoystick(int joy, int mask, int x, int y, boolean fire, long when);

	void setAlpha(boolean on);

	boolean getAlpha();

	int getKeyboardRow(int column);

	boolean wasKeyboardProbed();

	void resetProbe();

	void setProbe();

	void cancelPaste();

	/**
	 * Paste text into the clipboard
	 * @param contents
	 */
	void pasteText(String contents);

	boolean isPasting();

	boolean anyKeyPressed();

	byte getShiftMask();

	/**
	 * @param numLock the numLock to set
	 */
	void setNumLock(boolean numLock);

	boolean getNumLock();
	
    
    /**
     * Post an ASCII character, applying any conversions to make it
     * a legal keystroke on the 99/4A keyboard.
     * @param machine
     * @param pressed
     * @param synthetic if true, the character came from, e.g., pasted text,
     * and there are not distinct shift key events; otherwise, apply logic
     * to detect the patterns of real shift key presses and releases
     * @param shift extra shift keys
     * @param ch
     * @return true if we could represent it as ASCII
     */
	boolean postCharacter(IBaseMachine machine, int realKey, boolean pressed, boolean synthetic, byte shift, char ch, long when);
    	

}