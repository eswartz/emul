/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Aug 27, 2005
 *
 */
package v9t9.engine.keyboard;

import java.util.Arrays;

import v9t9.common.keyboard.IKeyboardListener;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.common.machine.IMachine;
import ejs.base.utils.HexUtils;
import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;

/**
 * This class tracks the state of the keyboard as exposed to the 99/4A CRU, in a
 * matrix of bits addressed by column and row.
 * 
 * There are two challenges:
 * 
 * 1) A modern PC keyboard has many more keys than the 99/4A did. While someone
 * can press Alt+R to imitate Fctn-R to enter '[', he can also directly press
 * '[' in a single keypress. Thus, in some cases, one keypress must set multiple
 * bits, one "real" key and one "fake" shift key.
 * 
 * One particular aspect is arrow keys.  When, for example, Left Arrow is
 * held down (Fctn-S) and then Right-Arrow is pressed (Fctn-D), then 
 * a release of Left-Arrow should not reset the Fctn key until Right-Arrow
 * is also released.
 * 
 * At the same time, real shift keys may be pressed. So we cannot get confused
 * if the user presses, e.g., '{' via Shift+R. This should not, if possible, be
 * exposed as "Fctn+Shift+R" but just as "Fctn-R". And of course, once '[' or
 * '{' is released on the PC, the "fake" shift keys should be released unless
 * the "real" shift or Alt key is still held down.
 * 
 * 
 * 
 * 2) Modern OSes expose keypresses via interrupts with "on" and "off" states.
 * And the host is much faster than the older computers, so it's highly likely
 * that the PC user can quickly press and release a key, and the emulated
 * computer may not even see it!
 * 
 * This is mainly due to the issues of emulating the system down to the lowest
 * levels. In the "real" 99/4A computer, the keyboard is scanned only at known
 * times, and when a keypress is detected, the keyboard scanning routine will
 * enter a delay loop to avoid "keyboard debounce". But this loop, interleaved
 * with the real host computer, can conspire to make a lot of keypresses so
 * unnoticed.
 * 
 * Also, again, since the host publishes discrete events for each key press,
 * "rollover" doesn't work quite the same way. When typing quickly, several
 * key-press events can happen at once and "pile up" before the corresponding
 * key-release events arrive. On the emulated keyboard, though, such pile- up
 * will set the logical OR of the bits for those keys, and will either be
 * detected as "no key" or detected as only one or the other key.
 * 
 * Finally, the host (esp. under AWT) may send key-press AND key-release events
 * when repeating a key! When this sequence is interleaved with the emulated
 * keyscan routine, the frequencies may alias each other, and the keyscan
 * routine may see the key during the short time it is "released", even when
 * held down continuously on the host, leading to dropped keys, where the
 * keyboard appears to "choke" between a long series of repeated keys.
 * 
 * @author ejs
 * 
 */
public class KeyboardState implements IKeyboardState {
	public static boolean DEBUG = false;
	
   
 

    /** 'real' shift keys being held down, as opposed to those being synthesized */
    private byte realshift;
    
	private byte[] crukeyboardmap = new byte[8];
	private boolean lastAlphaLock;
	private byte[] lastcrukeyboardmap = new byte[8];
	/** actual state of alpha */
	private boolean alphaLock;
	private IMachine machine;

	
	//protected Timer pasteTimer;
	private boolean prevWasBlank;

	private boolean numLock;
	
	private ListenerList<IKeyboardListener> listeners = new ListenerList<IKeyboardListener>();

    /*  Map of ASCII codes and their direct CRU mapping
        (high nybble=row, low nybble=column), except for 0xff,
        which should be faked. */

    /*  NOTE: 47 = '/' in Latin-1 corresponds to the US keyboard key '/'
        and '?', but on the TI keyboard, 0x75 this is the key for '/' and
        '-'.  The target-specific code must trap '-', '/', '?', '_'
        and should use FCTN+I for '?'.*/
    static final byte latinto9901[] = new byte[] {
          -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, /* 0-7 */
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
    public void clearKeyboard() {
    	Arrays.fill(crukeyboardmap, 0, 6, (byte)0);
		if (DEBUG) System.out.println("===========");

    }
    
    /* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#resetKeyboard()
	 */
    @Override
	public synchronized void resetKeyboard() {
        Arrays.fill(crukeyboardmap, 0, 6, (byte)0);
        Arrays.fill(lastcrukeyboardmap, 0, 6, (byte)0);
        //Arrays.fill(fakemap, 0, fakemap.length, (byte)0);
        realshift = 0;
        //fireListeners();
    }
    

    /* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#resetJoystick()
	 */
    @Override
	public synchronized void resetJoystick() {
        Arrays.fill(crukeyboardmap, 6, 8, (byte)0);
        Arrays.fill(lastcrukeyboardmap, 6, 8, (byte)0);
    }
    
//    /**
//     * Post an ASCII character, applying any conversions to make it
//     * a legal keystroke on the 99/4A keyboard.
//     * @param machine
//     * @param pressed
//     * @param synthetic if true, the character came from, e.g., pasted text,
//     * and there are not distinct shift key events; otherwise, apply logic
//     * to detect the patterns of real shift key presses and releases
//     * @param shift extra shift keys
//     * @param ch
//     * @return true if we could represent it as ASCII
//     */
//    public synchronized boolean postCharacter(IBaseMachine machine, int realKey, boolean pressed, boolean synthetic, byte shift, char ch, long when) {
//    	if (DEBUG) System.out.println(when + "==> post: ch=" + ch + "; shift="+ HexUtils.toHex2(shift)+"; pressed="+pressed);
//    	if (isAsciiDirectKey(ch)) {
//    		setKey(realKey, pressed, synthetic, shift, ch, when);
//    		return true;
//    	}
//    	
//		int fctnShifted = shift | FCTN;
//    	int ctrlShifted = shift | CTRL;
//    	
//		switch (ch) {
//		
//		case 8:
//			if (Settings.get(machine, IKeyboardState.settingBackspaceIsCtrlH).getBoolean())
//				setKey(realKey, pressed, synthetic, ctrlShifted, 'H', when);	/* BKSP */
//			else
//				setKey(realKey, pressed, synthetic, fctnShifted, 'S', when);	/* FCTN-S */
//			break;
//		case 9:
//			setKey(realKey, pressed, synthetic, ctrlShifted, 'I', when);	/* TAB */
//			break;
//			
//		case 13:
//			setKey(realKey, pressed, synthetic, (byte)0, '\r', when);
//			break;
//			
//			// shifted keys
//		case '!':
//			setKey(realKey, pressed, synthetic, shift | IKeyboardState.SHIFT, '1', when);
//			break;
//		case '@':
//			setKey(realKey, pressed, synthetic, shift | IKeyboardState.SHIFT, '2', when);
//			break;
//		case '#':
//			setKey(realKey, pressed, synthetic, shift | IKeyboardState.SHIFT, '3', when);
//			break;
//		case '$':
//			setKey(realKey, pressed, synthetic, shift | IKeyboardState.SHIFT, '4', when);
//			break;
//		case '%':
//			setKey(realKey, pressed, synthetic, shift | IKeyboardState.SHIFT, '5', when);
//			break;
//		case '^':
//			setKey(realKey, pressed, synthetic, shift | IKeyboardState.SHIFT, '6', when);
//			break;
//		case '&':
//			setKey(realKey, pressed, synthetic, shift | IKeyboardState.SHIFT, '7', when);
//			break;
//		case '*':
//			setKey(realKey, pressed, synthetic, shift | IKeyboardState.SHIFT, '8', when);
//			break;
//		case '(':
//			setKey(realKey, pressed, synthetic, shift | IKeyboardState.SHIFT, '9', when);
//			break;
//		case ')':
//			setKey(realKey, pressed, synthetic, shift | IKeyboardState.SHIFT, '0', when);
//			break;
//		case '+':
//			setKey(realKey, pressed, synthetic, shift | IKeyboardState.SHIFT, '=', when);
//			break;
//		case '<':
//			setKey(realKey, pressed, synthetic, shift | IKeyboardState.SHIFT, ',', when);
//			break;
//		case '>':
//			setKey(realKey, pressed, synthetic, shift | IKeyboardState.SHIFT, '.', when);
//			break;
//		case ':':
//			setKey(realKey, pressed, synthetic, shift | IKeyboardState.SHIFT, ';', when);
//			break;
//			
//			// faked keys
//		case '`':
//			if (0 == (realshift & IKeyboardState.SHIFT) && !isSet(IKeyboardState.FCTN, 'W'))
//				setKey(realKey, pressed, synthetic, fctnShifted, 'C', when);	/* ` */
//			else
//				setKey(realKey, pressed, synthetic, fctnShifted, 'W', when);	/* ~ */
//			break;
//		case '~':
//			setKey(realKey, pressed, synthetic, fctnShifted, 'W', when);
//			break;
//		case '-':
//			if (0 == (realshift & IKeyboardState.SHIFT) && !isSet(IKeyboardState.FCTN, 'U'))
//				setKey(realKey, pressed, synthetic, IKeyboardState.SHIFT, '/', when);	/* - */
//			else
//				setKey(realKey, pressed, synthetic, fctnShifted, 'U', when);	/* _ */
//			break;
//		case '_':
//			setKey(realKey, pressed, synthetic, fctnShifted, 'U', when);
//			break;
//		case '[':
//			if (0 == (realshift & IKeyboardState.SHIFT) && !isSet(IKeyboardState.FCTN, 'F'))
//				setKey(realKey, pressed, synthetic, fctnShifted, 'R', when);	/* [ */
//			else
//				setKey(realKey, pressed, synthetic, fctnShifted, 'F', when);	/* { */
//			break;
//		case '{':
//			setKey(realKey, pressed, synthetic, fctnShifted, 'F', when);
//			break;
//		case ']':
//			if (0 == (realshift & IKeyboardState.SHIFT) && !isSet(IKeyboardState.FCTN, 'G'))
//				setKey(realKey, pressed, synthetic, fctnShifted, 'T', when);	/* ] */
//			else
//				setKey(realKey, pressed, synthetic, fctnShifted, 'G', when);	/* } */
//			break;
//		case '}':
//			setKey(realKey, pressed, synthetic, fctnShifted, 'G', when);
//			break;
//			
//		case '\'':
//			if (0 == (realshift & IKeyboardState.SHIFT) && !isSet(IKeyboardState.FCTN, 'P'))
//				setKey(realKey, pressed, synthetic, fctnShifted, 'O', when);	/* ' */
//			else
//				setKey(realKey, pressed, synthetic, fctnShifted, 'P', when);	/* " */
//			break;
//		case '"':
//			setKey(realKey, pressed, synthetic, fctnShifted, 'P', when);
//			break;
//		case '/':
//			if (0 == (realshift & IKeyboardState.SHIFT) && !isSet(IKeyboardState.FCTN, 'I'))
//				setKey(realKey, pressed, synthetic, (byte)0, '/', when);	/* / */
//			else
//				setKey(realKey, pressed, synthetic, fctnShifted, 'I', when);	/* ? */
//			break;
//		case '?':
//			setKey(realKey, pressed, synthetic, fctnShifted, 'I', when);
//			break;
//		case '\\':
//			if (0 == (realshift & IKeyboardState.SHIFT) && !isSet(IKeyboardState.FCTN, 'A'))
//				setKey(realKey, pressed, synthetic, fctnShifted, 'Z', when);	/* \\ */
//			else
//				setKey(realKey, pressed, synthetic, fctnShifted, 'A', when);	/* | */
//			break;
//		case '|':
//			setKey(realKey, pressed, synthetic, fctnShifted, 'A', when);
//			break;
//			
//    	case 127:
//    		setKey(realKey, pressed, synthetic, fctnShifted, '1', when);	
//			break;
//		default:
//			return false;
//    	}
//		
//		// force the CPU to notice
//		if (machine.getCpu().isThrottled()) {
//			//machine.getCpu().addAllowedCycles(3000);
//			//cpu.getCruAccess().triggerInterrupt(InternalCru9901.INT_VDP);
//		}
//    	return true;
//    }
    
    /**
	 * 
	 */
	private void fireListeners() {
		machine.keyStateChanged();
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#setKey(int, boolean, boolean, int, int, long)
	 */
//    @Override
//	public synchronized void setKey(int realKey, boolean onoff, boolean synthetic, int shift, int key, long when) {
//        key &= 0xff;
//        shift &= 0xff;
//
//        long time = when;
//
//        KeyDelta delta = new KeyDelta(time, realKey, key, shift, synthetic, onoff);
//        if (currentGroup == null) {
//        	currentGroup = new ArrayList<KeyDelta>();
//        } else if (!delta.groupsWith(currentGroup)) {
//        	queuedKeys.add(currentGroup);
//        	currentGroup = new ArrayList<KeyDelta>();
//        }
//        currentGroup.add(delta);
//      
//        fireListeners();
//    }
//    
	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#pushQueuedKey()
	 */
	@Override
	public synchronized void applyKeyState() {
		byte[] prevmap = null;
		if (!listeners.isEmpty())
			prevmap = Arrays.copyOf(crukeyboardmap, crukeyboardmap.length);
		
		System.arraycopy(crukeyboardmap, 0, lastcrukeyboardmap, 0, 8);
		boolean noKey = !anyKeyPressed();
		if (!noKey || !prevWasBlank) {
			if (DEBUG) { for (int i=0;i<8;i++) System.out.print(HexUtils.toHex2(crukeyboardmap[i])+" "); System.out.println(); }
			prevWasBlank = noKey;
		}
		lastAlphaLock = alphaLock;
		
		if (prevmap != null) {
			sendKeyboardChanges(prevmap);
		}

	}


    /**
	 * @param prevmap
	 */
	private void sendKeyboardChanges(final byte[] prevmap) {
		listeners.fire(new IFire<IKeyboardListener>() {

			@Override
			public void fire(IKeyboardListener listener) {

				// examine shift changes
				if (((crukeyboardmap[0] ^ prevmap[0]) & FCTN + SHIFT + CTRL) != 0) {
					listener.shiftChangeEvent((byte) (crukeyboardmap[0] & FCTN + SHIFT + CTRL));
				}
				
				// examine ASCII changes
				for (int c = 0; c < 6; c++) {
					byte diff = (byte) (crukeyboardmap[c] ^ prevmap[c]);
					if (diff != 0) {
						for (int r = 0; r < 8; r++) {
							int mask = crukeyboardmap[c] & (0x80 >> r);
							if ((diff & mask) != 0) {
								char ch = x9901ToLatin[(r << 3) | c];
								if (ch != 0) {
									listener.asciiKeyEvent(ch, 
											mask != 0);
								}
							}
						}
					}
				}
				
				
				// joystick changes were sent immediately
			}
		});
		
	}


	public void changeKeyboardMatrix(boolean onoff, int key) {
		byte b;
		byte r;
		byte c;
		b = latinto9901[key];
		/*if (b == 0xff)
		    logger(_L | LOG_ERROR,
		         _("keyboard_setkey:  got a key that should be faked '%c' (%d)\n\n"),
		         key, key);*/
		//System.out.println("b = "+b + "; onoff="+onoff +"; shift="+Utils.toHex4(shift));
		if (b != -1) {
		    r = (byte) (b >> 4);
		    c = (byte) (b & 15);
		    
		    /*
		    // NMI on FCTN+SHIFT+CTRL
		    if (shift == CTRL + FCTN + SHIFT && key == ' '
		    		&& TESTKBDCRU(r, c) && !onoff) {
		    	cpu.holdpin(Cpu.INTPIN_LOAD);
		    }
		    */
		    
		    changeKbdMatrix(r, c, onoff);
		    
		}
	}
	
	public void changeKeyboardShifts(boolean onoff, byte shift) {
		if (onoff)
			crukeyboardmap[0] |= shift;
		else
			crukeyboardmap[0] &= ~shift;
	}

 
    public void changeKbdMatrix(byte r, byte c, boolean v) {
    	//if (DEBUG) System.out.println("changeKbdMatrix: " + r+ "/" +c +" = " + v);
        if (v)
            setKbdMatrix(r, c);
        else
            resetKbdMatrix(r, c);
    }

    private boolean testKbdMatrix(byte r, byte c) {
        return (crukeyboardmap[c] & (0x80 >> r)) != 0;
    }

    private void resetKbdMatrix(byte r, byte c) {
    	crukeyboardmap[c] &= ~(0x80 >> r);
    }

    private void setKbdMatrix(byte r, byte c) {
    	crukeyboardmap[c] |= (0x80 >> r);
    }

    /* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#isSet(byte, int)
	 */
    @Override
	public synchronized boolean isSet(byte shift, int key) {
        byte b, r, c;
        boolean res = false;

        if ((shift & SHIFT) != 0 && testKbdMatrix(SHIFT_R, SHIFT_C))
            res = true;
        if ((shift & CTRL) != 0 && testKbdMatrix(CTRL_R, CTRL_C))
            res = true;
        if ((shift & FCTN) != 0 && testKbdMatrix(FCTN_R, FCTN_C))
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
	public synchronized void setJoystick(final int joy, int mask, int x, int y, boolean fire, long when) {
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
	 * @see v9t9.engine.keyboard.IKeyboardState#setAlpha(boolean)
	 */
	@Override
	public synchronized void setAlpha(boolean on) {
		System.out.println("Alpha lock is " + (on ? "ON" : "OFF"));
		this.alphaLock = on;
		
		if (!listeners.isEmpty()) {
			listeners.fire(new IFire<IKeyboardListener>() {

				@Override
				public void fire(IKeyboardListener listener) {
					listener.otherKeyEvent(OTHER_KEY_ALPHA_LOCK, alphaLock);
				}
			});
		}

	}

	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#getAlpha()
	 */
	@Override
	public synchronized boolean getAlpha() {
		return lastAlphaLock;
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#getKeyboardRow(int)
	 */
	@Override
	public synchronized int getKeyboardRow(int column) {
		return lastcrukeyboardmap[column];
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#wasKeyboardProbed()
	 */
//	@Override
//	public synchronized boolean wasKeyboardProbed() {
//		return probedColumns == 0x3f;
//	}

	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#resetProbe()
	 */
	@Override
	public synchronized void resetProbe() {
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#setProbe()
	 */
	@Override
	public synchronized void setProbe() {
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
	 * @see v9t9.common.keyboard.IKeyboardState#setShiftMask(byte)
	 */
	@Override
	public void setShiftMask(byte shift) {
		this.realshift = shift;
	}

	
	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#setNumLock(boolean)
	 */
	@Override
	public void setNumLock(boolean numLock) {
		this.numLock = numLock;
	}
	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#getNumLock()
	 */
	@Override
	public boolean getNumLock() {
		return numLock;
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
