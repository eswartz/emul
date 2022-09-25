/*
  KeyboardState.java

  (c) 2005-2017 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.keyboard;

import static v9t9.common.keyboard.KeyboardConstants.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
    protected byte SHIFT_R;
    protected byte SHIFT_C;
    protected byte FCTN_R;
    protected byte FCTN_C;
    protected byte CTRL_R;
    protected byte CTRL_C;
    protected byte JOY1_C;
    protected byte JOY2_C;

    protected int JOY_FIRE_R;
    protected int JOY_LEFT_R;
    protected int JOY_RIGHT_R;
    protected int JOY_DOWN_R;
    protected int JOY_UP_R;

    /** 'real' shift keys being held down, as opposed to those being synthesized */
    private byte realshift;
    
	private byte[] crukeyboardmap = new byte[8];
	private byte[] lastcrukeyboardmap = new byte[8];
	private IMachine machine;

	private Set<Integer> pressedKeyCodes = new HashSet<Integer>();
	private Set<Integer> pressedKeyIds = new HashSet<Integer>();
	
	//protected Timer pasteTimer;
	private boolean prevWasBlank;

	private ListenerList<IKeyboardListener> listeners = new ListenerList<IKeyboardListener>();

	private byte locks;

    /*  Map of ASCII codes and their direct CRU mapping
        (high nybble=row, low nybble=column), except for 0xff,
        which should be faked. */
    protected byte latinto9901[] = new byte[128];
    
    
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
    	return (int) x < latinto9901.length && (latinto9901[x] != -1 && (x) != '/');
    }

    public KeyboardState(IMachine machine) {
		this.machine = machine;
        
    }
    
    /* (non-Javadoc)
     * @see v9t9.common.keyboard.IKeyboardState#clearKeyboard()
     */
    @Override
    public synchronized void incrClearKeyboard() {
    	fireKeyboardListeners(false);
    	
    	pressedKeyCodes.clear();
    	pressedKeyIds.clear();
    	
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
        pressedKeyCodes.clear();
        pressedKeyIds.clear();
        stickyKeys.clear();
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
		if (!stickyKeys.isEmpty()) {
			setKeysFrom(new HashSet<Integer>(stickyKeys));
		}
		
		System.arraycopy(crukeyboardmap, 0, lastcrukeyboardmap, 0, 8);
		boolean noKey = !anyKeyPressed();
		if (!noKey || !prevWasBlank) {
			if (DEBUG) { for (int i=0;i<8;i++) System.out.print(HexUtils.toHex2(crukeyboardmap[i])+" "); System.out.println(); }
			prevWasBlank = noKey;
		}
		
		fireKeyboardListeners(true);

		//lastLocks = locks;
	}

	/**
	 * 
	 */
	private void fireKeyboardListeners(final boolean onoff) {
		if (!listeners.isEmpty() && !pressedKeyCodes.isEmpty()) {
			listeners.fire(new IFire<IKeyboardListener>() {

				@Override
				public void fire(IKeyboardListener listener) {
					listener.keyEvent(pressedKeyCodes, onoff); 
				}
			});
			
			listeners.fire(new IFire<IKeyboardListener>() {

				@Override
				public void fire(IKeyboardListener listener) {
					listener.physKeyEvent(pressedKeyIds, onoff); 
				}
			});
		}
		
	}

	final Map<Integer, int[]> vkeyToKeyMappings = new HashMap<Integer, int[]>();

	private Set<Integer> stickyKeys = new HashSet<Integer>();

	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardState#registerMapping(int, int[])
	 */
	@Override
	public void registerMapping(int vkey, int... keys) {
		for (int k : keys) {
			if (latinto9901[k] == -1)
				//throw new IllegalStateException();
				return;
		}
		vkeyToKeyMappings.put(vkey, keys);
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
			if (k >= 0 && k < 128) {
				keys.remove(k);
				incrSetKey(true, k);
			}
			else if (k >= KEY_JOYST_UP && k <= KEY_JOYST_IDLE) {
				keys.remove(k);
				
				//System.out.println("k:" + k);
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
				case KEY_JOYST_FIRE_UP:
					changeJoyMatrix(joy, JOY_FIRE_R, false);
					break;
				case KEY_JOYST_IDLE:
					setJoystick(joy, JOY_X|JOY_Y, 0, 0, false);
					break;
				}
			}
		}

		// any left are not functional
		for (int k : keys) {
			if (k != KEY_UNKNOWN)
				System.err.println("*** could not map virtual key " + k + " to hardware");
		}
	}
	
	public void incrSetKey(final boolean onoff, final int key) {
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

			if (onoff) {
				pressedKeyCodes.add(key);
				pressedKeyIds.add((r<<4)|c);
			} else {
				pressedKeyCodes.remove(key);
				pressedKeyIds.remove((r<<4)|c);
			}
		} else {
			System.err.println("*** should have faked key " + key);
		}
	}
	
	public void changeLocks(boolean onoff, byte lock) {
		if (onoff) {
			locks |= lock;
			if ((locks & MASK_CAPS_LOCK) != 0) {
				pressedKeyCodes.add(KEY_CAPS_LOCK);
				pressedKeyIds.add(0x100);
			}
				
		} else {
			locks &= ~lock;
			
			if ((locks & MASK_CAPS_LOCK) == 0) {
				pressedKeyCodes.remove(KEY_CAPS_LOCK);
				pressedKeyIds.remove(0x100);
			}
		}
		
		fireKeyboardListeners(true);

	}
	
 
    private boolean testKbdMatrix(byte r, byte c) {
    	if ((r | c) < 0) {
    		return false;
    	}
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
	final public void changeJoyMatrix(int joy,int row, boolean v) {
		//changeKbdMatrix((byte) r, (byte)(JOY1_C+(j)-1), v);
		
		// do joystick immediately
		//System.out.println("joy:"+j+","+row+"="+v);
		byte mask = (byte) (0x80 >> row);
		byte c = joy == 1 ? JOY1_C : JOY2_C;
		if (v) {
			crukeyboardmap[c] |= mask;
			lastcrukeyboardmap[c] |= mask;
		} else {
			crukeyboardmap[c] &= ~mask;
			lastcrukeyboardmap[c] &= ~mask;

		}
			
	}

	public boolean testJoyMatrix(int joy,int row) {
		byte mask = (byte) (0x80 >> row);
		byte c = joy == 0 ? JOY1_C : JOY2_C;
		return (crukeyboardmap[c] & mask) != 0;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#setJoystick(int, int, int, int, boolean, long)
	 */
    @Override
	public synchronized void setJoystick(final int joy, int mask, int x, int y, boolean fire) {
    	byte c = joy == 0 ? JOY1_C : JOY2_C;
    	if (c < 0) {
    		return;
    	}
		byte oldValue = crukeyboardmap[c];
    	
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
    	
    	final byte newValue = crukeyboardmap[c];
    	
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
    
    @Override
    public synchronized int getJoystick(final int joy, int mask) {
    	if ((mask & JOY_X) != 0) {
    		return testJoyMatrix(joy, JOY_LEFT_R) ? -1 : testJoyMatrix(joy, JOY_RIGHT_R) ? 1 : 0;
    	}
    	if ((mask & JOY_Y) != 0) {
    		return testJoyMatrix(joy, JOY_UP_R) ? -1 : testJoyMatrix(joy, JOY_DOWN_R) ? 1 : 0;
    	}
    	if ((mask & JOY_B) != 0) {
    		return testJoyMatrix(joy, JOY_FIRE_R) ? 1 : 0;
    	}
    	return 0;
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

	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardState#stickyApplyKey(int, boolean)
	 */
	@Override
	public void stickyApplyKey(int keycode, boolean onoff) {
		if (onoff)
			stickyKeys .add(keycode);
		else
			stickyKeys.remove(keycode);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.IKeyboardState#isBufferEmpty()
	 */
	@Override
	public boolean isBufferEmpty() {
		
		return this.stickyKeys.isEmpty() && pressedKeyCodes.isEmpty();
	}
}
