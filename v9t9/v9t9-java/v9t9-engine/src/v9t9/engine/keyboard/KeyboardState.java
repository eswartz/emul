/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Aug 27, 2005
 *
 */
package v9t9.engine.keyboard;

import static v9t9.common.keyboard.KeyboardConstants.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import v9t9.common.keyboard.IKeyboardListener;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.common.keyboard.KeyboardConstants;
import v9t9.common.machine.IMachine;
import ejs.base.utils.HexUtils;
import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;

/**
 * This class tracks the state of the keyboard as exposed to the 99/4A CRU, in a
 * matrix of bits addressed by column and row.
 * 
 * @author ejs
 * 
 */
public class KeyboardState implements IKeyboardState {
	public static boolean DEBUG = false;
	
	 /* CRU rows and columns */
    static final byte SHIFT_R = 2;
    static final byte SHIFT_C = 0;
    static final byte FCTN_R = 3;
    static final byte FCTN_C = 0;
    static final byte CTRL_R = 1;
    static final byte CTRL_C = 0;
    static final byte JOY1_C = 6;

    static final int JOY_FIRE_R = 7;
	static final int JOY_LEFT_R = 6;
	static final int JOY_RIGHT_R = 5;
	static final int JOY_DOWN_R = 4;
	static final int JOY_UP_R = 3;

    /** 'real' shift keys being held down, as opposed to those being synthesized */
    private byte realshift;
    
	private byte[] crukeyboardmap = new byte[8];
	private byte[] lastcrukeyboardmap = new byte[8];
	private IMachine machine;

	
	//protected Timer pasteTimer;
	private boolean prevWasBlank;

	private ListenerList<IKeyboardListener> listeners = new ListenerList<IKeyboardListener>();

	private byte locks;

    /*  Map of ASCII codes and their direct CRU mapping
        (high nybble=row, low nybble=column), except for 0xff,
        which should be faked. */

    /*  NOTE: 47 = '/' in Latin-1 corresponds to the US keyboard key '/'
        and '?', but on the TI keyboard, 0x75 this is the key for '/' and
        '-'.  The target-specific code must trap '-', '/', '?', '_'
        and should use FCTN+I for '?'.*/
    static final byte latinto9901[] = new byte[] {
        0x20, 0x10, 0x30,   -1,   -1,   -1,   -1,   -1, /* 0-7 */
          -1,   -1,   -1,   -1,   -1, 0x50,   -1,   -1, /* 8-15 */
          -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, /* 16-23 */
          -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, /* 24-31 */
    
        0x60,   -1,   -1,   -1,   -1,   -1,   -1,   -1, /* 32-39 */
          -1,   -1,   -1,   -1, 0x72,   -1, 0x71, 0x75, /* 40-47 */
        0x45, 0x35, 0x31, 0x32, 0x33, 0x34, 0x44, 0x43, /* 48-55 */
        0x42, 0x41,   -1, 0x65,   -1, 0x70,   -1,   -1, /* 56-63 */
    
          -1, 0x25, 0x04, 0x02, 0x22, 0x12, 0x23, 0x24, /* 64-71 */
        0x64, 0x52, 0x63, 0x62, 0x61, 0x73, 0x74, 0x51, /* 72-79 */
        0x55, 0x15, 0x13, 0x21, 0x14, 0x53, 0x03, 0x11, /* 80-87 */
        0x01, 0x54, 0x05,   -1,   -1,   -1,   -1,   -1, /* 88-95 */
    
          -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, /* 96-103 */
          -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, /* 104-111 */
          -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, /* 112-119 */
          -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1  /* 120-127 */
    };
    
    /** entries as 0x[row:4][column:4] */
    static final char x9901ToLatin[] = {
    	/* 0x00 */   0, 'X', 'C', 'V', 'B', 'Z', 0, 0,
    	/* 0x10 */   0, 'W', 'E', 'R', 'T', 'Q', 0, 0,
    	/* 0x20 */   0, 'S', 'D', 'F', 'G', 'A', 0, 0,
    	/* 0x30 */   0, '2', '3', '4', '5', '1', 0, 0,
    	/* 0x40 */   0, '9', '8', '7', '6', '0', 0, 0,
    	/* 0x50 */  13, 'O', 'I', 'U', 'Y', 'P', 0, 0,
    	/* 0x60 */ ' ', 'L', 'K', 'J', 'H', ';', 0, 0,
    	/* 0x70 */ '=', '.', ',', 'M', 'N', '/', 0, 0,
    	
    };
	//private final Cpu cpu;
	//private long lastAbortTime;

    /*	This macro tells us whether an ASCII code has a direct mapping
	to a 9901 keyboard matrix location (stored in latinto9901[]).
	The '/' character is special, since its 99/4A shifted value ('-') is not
	the same as the standard keyboard's shifted value ('?'). 
	(This is important when we are using a host keyboard module that
    allows us to know the unshifted value of a pressed key.)
     */

    /* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#isAsciiDirectKey(char)
	 */
    @Override
	public boolean isAsciiDirectKey(char x) { 
    	return (latinto9901[x] != -1 && (x) != '/');
    }

    public KeyboardState(IMachine machine) {
		this.machine = machine;
        
    }
    
    /* (non-Javadoc)
     * @see v9t9.common.keyboard.IKeyboardState#clearKeyboard()
     */
    @Override
    public void incrClearKeyboard() {
    	Arrays.fill(crukeyboardmap, 0, 6, (byte)0);
    	realshift = 0;
		if (DEBUG) System.out.println("===========");

    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#resetKeyboard()
	 */
    @Override
	public synchronized void resetKeyboard() {
        Arrays.fill(crukeyboardmap, 0, 6, (byte)0);
        Arrays.fill(lastcrukeyboardmap, 0, 6, (byte)0);
        realshift = 0;
        //lastLocks = 0;	// keep these
    }
    

    /* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#resetJoystick()
	 */
    @Override
	public synchronized void resetJoystick() {
        Arrays.fill(crukeyboardmap, 6, 8, (byte)0);
        Arrays.fill(lastcrukeyboardmap, 6, 8, (byte)0);
    }
    
   
    /**
	 * 
	 */
	private void fireListeners() {
		machine.keyStateChanged();
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#pushQueuedKey()
	 */
	@Override
	public synchronized void applyIncrKeyState() {
		System.arraycopy(crukeyboardmap, 0, lastcrukeyboardmap, 0, 8);
		boolean noKey = !anyKeyPressed();
		if (!noKey || !prevWasBlank) {
			if (DEBUG) { for (int i=0;i<8;i++) System.out.print(HexUtils.toHex2(crukeyboardmap[i])+" "); System.out.println(); }
			prevWasBlank = noKey;
		}
		//lastLocks = locks;
	}

	final Map<Integer, int[]> vkeyToKeyMappings = new HashMap<Integer, int[]>();

	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardState#registerMapping(int, int[])
	 */
	@Override
	public void registerMapping(int vkey, int... keys) {
		for (int k : keys) {
			if (latinto9901[k] == -1)
				throw new IllegalStateException();
		}
		vkeyToKeyMappings.put(vkey, keys);
	}
		
	{
		registerMapping(KEY_TAB, KEY_CONTROL, 'I');
		registerMapping(KEY_EXCLAMATION, KEY_SHIFT, '1');
		registerMapping(KEY_AT, KEY_SHIFT, '2');
		registerMapping(KEY_POUND, KEY_SHIFT, '3');
		registerMapping(KEY_DOLLAR, KEY_SHIFT, '4');
		registerMapping(KEY_PERCENT, KEY_SHIFT, '5');
		registerMapping(KEY_CIRCUMFLEX, KEY_SHIFT, '6');
		registerMapping(KEY_AMPERSAND, KEY_SHIFT, '7');
		registerMapping(KEY_ASTERISK, KEY_SHIFT, '8');
		registerMapping(KEY_OPEN_PARENTHESIS, KEY_SHIFT, '9');
		registerMapping(KEY_CLOSE_PARENTHESIS, KEY_SHIFT, '0');
		registerMapping(KEY_PLUS, KEY_SHIFT, '=');
		registerMapping(KEY_LESS, KEY_SHIFT, ',');
		registerMapping(KEY_GREATER, KEY_SHIFT, '.');
		registerMapping(KEY_COLON, KEY_SHIFT, ';');
		registerMapping(KEY_BACK_QUOTE, KEY_ALT, 'C');
		registerMapping(KEY_TILDE, KEY_ALT, 'W');
		registerMapping(KEY_MINUS, KEY_SHIFT, '/');
		registerMapping(KEY_UNDERSCORE, KEY_ALT, 'U');
		registerMapping(KEY_OPEN_BRACKET, KEY_ALT, 'R');
		registerMapping(KEY_OPEN_BRACE, KEY_ALT, 'F');
		registerMapping(KEY_CLOSE_BRACKET, KEY_ALT, 'T');
		registerMapping(KEY_CLOSE_BRACE, KEY_ALT, 'G');
		registerMapping(KEY_QUOTE, KEY_ALT, 'P');
		registerMapping(KEY_SINGLE_QUOTE, KEY_ALT, 'O');
		registerMapping(KEY_QUESTION, KEY_ALT, 'I');
		registerMapping(KEY_BACK_SLASH, KEY_ALT, 'Z');
		registerMapping(KEY_BAR, KEY_ALT, 'A');
		registerMapping(KEY_DELETE, KEY_ALT, '1');
		registerMapping(KEY_F1, KEY_ALT, '1');
		registerMapping(KEY_F2, KEY_ALT, '2');
		registerMapping(KEY_F3, KEY_ALT, '3');
		registerMapping(KEY_F4, KEY_ALT, '4');
		registerMapping(KEY_F5, KEY_ALT, '5');
		registerMapping(KEY_F6, KEY_ALT, '6');
		registerMapping(KEY_F7, KEY_ALT, '7');
		registerMapping(KEY_F8, KEY_ALT, '8');
		registerMapping(KEY_F9, KEY_ALT, '9');
		
		registerMapping(KEY_ARROW_DOWN, KEY_ALT, 'X');
		registerMapping(KEY_ARROW_UP, KEY_ALT, 'E');
		registerMapping(KEY_ARROW_LEFT, KEY_ALT, 'S');
		registerMapping(KEY_ARROW_RIGHT, KEY_ALT, 'D');

		registerMapping(KEY_PAGE_UP, KEY_ALT, '6');	// CLEAR  // (as per E/A and TI Writer)
		registerMapping(KEY_PAGE_DOWN, KEY_ALT, '4');	// PROC'D  // (as per E/A and TI Writer)
		registerMapping(KEY_HOME, KEY_ALT, '5');	// BEGIN
		registerMapping(KEY_END, KEY_ALT, '0');	
		registerMapping(KEY_INSERT, KEY_ALT, '2');	// INS	
		registerMapping(KEY_DELETE, KEY_ALT, '1');	// DEL
		
		registerMapping(KEY_KP_ARROW_DOWN, KEY_ALT, 'X');
		registerMapping(KEY_KP_ARROW_UP, KEY_ALT, 'E');
		registerMapping(KEY_KP_ARROW_LEFT, KEY_ALT, 'S');
		registerMapping(KEY_KP_ARROW_RIGHT, KEY_ALT, 'D');

		registerMapping(KEY_KP_PAGE_UP, KEY_ALT, '6');	// CLEAR  // (as per E/A and TI Writer)
		registerMapping(KEY_KP_PAGE_DOWN, KEY_ALT, '4');	// PROC'D  // (as per E/A and TI Writer)
		registerMapping(KEY_KP_HOME, KEY_ALT, '5');	// BEGIN
		registerMapping(KEY_KP_END, KEY_ALT, '0');	
		registerMapping(KEY_KP_INSERT, KEY_ALT, '2');	// INS	
		registerMapping(KEY_KP_DELETE, KEY_ALT, '1');	// DEL
		
		
		registerMapping(KEY_KP_SLASH, '/');
		registerMapping(KEY_KP_ASTERISK, KEY_SHIFT, '8');
		registerMapping(KEY_KP_MINUS,  KEY_SHIFT, '/');
		registerMapping(KEY_KP_PLUS, KEY_SHIFT, '=');
		registerMapping(KEY_KP_ENTER, '\r');
		registerMapping(KEY_KP_0, '0');
		registerMapping(KEY_KP_1, '1');
		registerMapping(KEY_KP_2, '2');
		registerMapping(KEY_KP_3, '3');
		registerMapping(KEY_KP_4, '4');
		registerMapping(KEY_KP_5, '5');
		registerMapping(KEY_KP_6, '6');
		registerMapping(KEY_KP_7, '7');
		registerMapping(KEY_KP_8, '8');
		registerMapping(KEY_KP_9, '9');
	}
		
	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardState#setKeysFrom(java.util.Set)
	 */
	@Override
	public void setKeysFrom(Set<Integer> keys) {
		// remove virtual keys first
		for (int k : keys.toArray(new Integer[keys.size()])) {
			int[] map = vkeyToKeyMappings.get(k);
			if (map != null) {
				keys.remove(k);
				for (int mk : map) {
					keys.remove(mk);
					incrSetKey(true, mk);
				}
			}
			// else, skip for now
		}
		
		for (int k : keys.toArray(new Integer[keys.size()])) {
			if (k >= 0 && k < 128 && latinto9901[k] != -1) {
				keys.remove(k);
				incrSetKey(true, k);
			}
			else if (k >= KEY_JOYST_UP && k <= KEY_JOYST_IDLE) {
				keys.remove(k);
				int joy = 1 + (k & 1);
				k &= ~1;
				switch (k) {
				case KEY_JOYST_DOWN:
					setJoystick(joy, JOY_Y, 0, 1, false);
					break;
				case KEY_JOYST_DOWN_LEFT:
					setJoystick(joy, JOY_X|JOY_Y, -1, 1, false);
					break;
				case KEY_JOYST_DOWN_RIGHT:
					setJoystick(joy, JOY_X|JOY_Y, 1, 1, false);
					break;
				case KEY_JOYST_UP:
					setJoystick(joy, JOY_Y, 0, -1, false);
					break;
				case KEY_JOYST_UP_LEFT:
					setJoystick(joy, JOY_X|JOY_Y, -1, -1, false);
					break;
				case KEY_JOYST_UP_RIGHT:
					setJoystick(joy, JOY_X|JOY_Y, 1, -1, false);
					break;
				case KEY_JOYST_LEFT:
					setJoystick(joy, JOY_X, -1, 0, false);
					break;
				case KEY_JOYST_RIGHT:
					setJoystick(joy, JOY_X, 1, 0, false);
					break;
				case KEY_JOYST_FIRE:
					changeJoyMatrix(joy, JOY_FIRE_R, true);
					break;
				case KEY_JOYST_IDLE:
					setJoystick(joy, JOY_X|JOY_Y|JOY_B, 0, 0, false);
					break;
				}
			}
		}

		// any left are not functional
		for (int k : keys) {
			System.err.println("*** could not map virtual key " + k + " to hardware");
		}
	}
	
	public void incrSetKey(boolean onoff, int key) {
		byte b;
		byte r;
		byte c;
		
		switch (key) {
		case KeyboardConstants.KEY_SHIFT:
			realshift = (byte) ((realshift & ~MASK_SHIFT) | (onoff ? MASK_SHIFT : 0));
			break;
		case KeyboardConstants.KEY_CONTROL:
			realshift = (byte) ((realshift & ~MASK_CONTROL) | (onoff ? MASK_CONTROL : 0));
			break;
		case KeyboardConstants.KEY_ALT:
			realshift = (byte) ((realshift & ~MASK_ALT) | (onoff ? MASK_ALT : 0));
			break;
		case KeyboardConstants.KEY_CONTEXT:
			realshift = (byte) ((realshift & ~MASK_CONTEXT) | (onoff ? MASK_CONTEXT : 0));
			break;
		case KeyboardConstants.KEY_LOGO:
			realshift = (byte) ((realshift & ~MASK_LOGO) | (onoff ? MASK_LOGO : 0));
			break;
		}

		
		b = latinto9901[key];
		if (b != -1) {
		    r = (byte) (b >> 4);
		    c = (byte) (b & 15);
		    //if (DEBUG) System.out.println("changeKbdMatrix: " + r+ "/" +c +" = " + v);
			if (onoff)
				crukeyboardmap[c] |= (0x80 >> r);
			else
				crukeyboardmap[c] &= ~(0x80 >> r);
		} else {
			System.err.println("*** should have faked key " + key);
		}
	}
	
	public void changeShifts(boolean onoff, byte shift) {
		byte cruShift = 0;
		if ((shift & KeyboardConstants.MASK_SHIFT) != 0)
			cruShift |= 0x80 >> SHIFT_R; 
		if ((shift & KeyboardConstants.MASK_CONTROL) != 0)
			cruShift |= 0x80 >> CTRL_R; 
		if ((shift & KeyboardConstants.MASK_ALT) != 0)
			cruShift |= 0x80 >> FCTN_R; 
			
		if (onoff)
			crukeyboardmap[0] |= cruShift;
		else
			crukeyboardmap[0] &= ~cruShift;
	}

	public void changeLocks(boolean onoff, byte lock) {
		if (onoff)
			locks |= lock;
		else
			locks &= ~lock;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardState#toggleKeyboardLocks(byte)
	 */
	@Override
	public void toggleKeyboardLocks(byte lock) {
		locks ^= lock;
	}
	
 
    private boolean testKbdMatrix(byte r, byte c) {
        return (crukeyboardmap[c] & (0x80 >> r)) != 0;
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#isSet(byte, int)
	 */
    @Override
	public synchronized boolean isSet(byte shift, int key) {
        byte b, r, c;
        boolean res = false;

        if ((shift & KeyboardConstants.MASK_SHIFT) != 0 && testKbdMatrix(SHIFT_R, SHIFT_C))
            res = true;
        if ((shift & KeyboardConstants.MASK_CONTROL) != 0 && testKbdMatrix(CTRL_R, CTRL_C))
            res = true;
        if ((shift & KeyboardConstants.MASK_ALT) != 0 && testKbdMatrix(FCTN_R, FCTN_C))
            res = true;

        if (key != 0) {
            b = (byte) latinto9901[key];
            /*if (b == 0xff)
                logger(_L | L_0,
                     _("keyboard_isset:  got a key that should be faked '%c' (%d)\n\n"),
                     key, key);*/
            r = (byte) (b >> 4);
            c = (byte) (b & 15);
            return res && testKbdMatrix(r, c);
        } else
            return res;
    }


	//	j=1 or 2
	final private void changeJoyMatrix(int j,int row, boolean v) {
		//changeKbdMatrix((byte) r, (byte)(JOY1_C+(j)-1), v);
		
		// do joystick immediately
		byte mask = (byte) (0x80 >> row);
		byte c = (byte) (JOY1_C+(j)-1);
		if (v) {
			crukeyboardmap[c] |= mask;
			lastcrukeyboardmap[c] |= mask;
		} else {
			crukeyboardmap[c] &= ~mask;
			lastcrukeyboardmap[c] &= ~mask;

		}
			
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#setJoystick(int, int, int, int, boolean, long)
	 */
    @Override
	public synchronized void setJoystick(final int joy, int mask, int x, int y, boolean fire) {
    	int joyRow = JOY1_C+joy-1;
		byte oldValue = crukeyboardmap[joyRow];
    	
    	if ((mask & JOY_X) != 0) {
    		//logger(_L | L_1, _("changing JOY_X (%d)\n\n"), x);
    		changeJoyMatrix(joy, JOY_LEFT_R, x < 0);
    		changeJoyMatrix(joy, JOY_RIGHT_R, x > 0);
    	}
    	if ((mask & JOY_Y) != 0) {
    		//logger(_L | L_1, _("changing JOY_Y (%d)\n\n"), y);
    		changeJoyMatrix(joy, JOY_UP_R, y < 0);
    		changeJoyMatrix(joy, JOY_DOWN_R, y > 0);
    	}
    	if ((mask & JOY_B) != 0) {
    		//logger(_L | L_1, _("changing JOY_B (%d)\n\n"), fire);
    		changeJoyMatrix(joy, JOY_FIRE_R, fire);
    	}

    	/*  clear unused bits  */
    	changeJoyMatrix(joy, 0, false);
    	changeJoyMatrix(joy, 1, false);
    	changeJoyMatrix(joy, 2, false);
    	
    	final byte newValue = crukeyboardmap[joyRow];
    	
    	fireListeners();
    	
    	if (oldValue != newValue && !listeners.isEmpty()) {
			listeners.fire(new IFire<IKeyboardListener>() {

				@Override
				public void fire(IKeyboardListener listener) {
					listener.joystickChangeEvent(joy, newValue); 
				}
			});
		}
    }
    
	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#getKeyboardRow(int)
	 */
	@Override
	public synchronized int getKeyboardRow(int column) {
		return lastcrukeyboardmap[column];
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#anyKeyPressed()
	 */
	@Override
	public boolean anyKeyPressed() {
//		if (!queuedKeys.isEmpty())
//			return true;
		
		boolean any = false;
		any = (realshift != 0);
		if (!any) {
			for (byte b : crukeyboardmap)
				if (b != 0) {
					any = true;
					break;
				}
		}
		return any;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#getShiftMask()
	 */
	@Override
	public byte getShiftMask() {
		return realshift;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardState#getLockMask()
	 */
	@Override
	public byte getLockMask() {
		return locks;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardState#setLockMask(byte)
	 */
	@Override
	public void setLockMask(byte locks) {
		this.locks = locks; 
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardState#isLock(byte)
	 */
	@Override
	public boolean isLock(byte lockMask) {
		return (locks & lockMask) == lockMask;
	}
	

	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardState#addKeyboardListener(v9t9.common.keyboard.IKeyboardListener)
	 */
	@Override
	public synchronized void addKeyboardListener(IKeyboardListener listener) {
		listeners.add(listener);
	}
	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardState#removeKeyboardListener(v9t9.common.keyboard.IKeyboardListener)
	 */
	@Override
	public synchronized void removeKeyboardListener(IKeyboardListener listener) {
		listeners.remove(listener);
	}
	
}
