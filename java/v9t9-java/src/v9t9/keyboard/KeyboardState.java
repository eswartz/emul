/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Aug 27, 2005
 *
 */
package v9t9.keyboard;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.common.Machine;

@SuppressWarnings("unused")
public class KeyboardState {
    /* Masks, corresponding to column 0 */
    public static final byte SHIFT = 0x20;
    public static final byte FCTN = 0x10;
    public static final byte CTRL = 0x40;
    
    /* CRU rows and columns */
    private static final byte SHIFT_R = 2;
    private static final byte SHIFT_C = 0;
    private static final byte FCTN_R = 3;
    private static final byte FCTN_C = 0;
    private static final byte CTRL_R = 1;
    private static final byte CTRL_C = 0;
    private static final byte JOY1_C = 6;

    /* Map of keys whose shifted/ctrled/fctned versions are being tracked */
    private byte fakemap[] = new byte[256];
    private byte shiftmap[] = new byte[256];
    private byte ctrlmap[] = new byte[256];
    private byte fctnmap[] = new byte[256];
    private int cctrl, cfctn, cshift;

    /** 'real' shift keys being held down, as opposed to those being synthesized */
    private byte realshift;
    
	private byte[] crukeyboardmap = new byte[8];
	private boolean lastAlphaLock;
	private byte[] lastcrukeyboardmap = new byte[8];
	/** actual state of alpha */
	private boolean alphaLock;
	private int probedColumns;
	private Machine machine;

	
	//protected Timer pasteTimer;
	protected Runnable pasteTask;
	private boolean pasteNext;
	private int pasteKeyDelay = 20;

	private Queue<KeyDelta> queuedKeys = new LinkedList<KeyDelta>();
	
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
	//private final Cpu cpu;
	//private long lastAbortTime;

    /*	This macro tells us whether an ASCII code has a direct mapping
	to a 9901 keyboard matrix location (stored in latinto9901[]).
	The '/' character is special, since its 99/4A shifted value ('-') is not
	the same as the standard keyboard's shifted value ('?'). 
	(This is important when we are using a host keyboard module that
    allows us to know the unshifted value of a pressed key.)
     */

    public boolean isAsciiDirectKey(char x) { 
    	return (latinto9901[x] != -1 && (x) != '/');
    }

    public KeyboardState(Machine machine) {
		this.machine = machine;
        
    }
    
    public void setPasteKeyDelay(int times) {
    	this.pasteKeyDelay = times;
    }
    
    public synchronized void resetKeyboard() {
        Arrays.fill(crukeyboardmap, 0, 6, (byte)0);
        Arrays.fill(lastcrukeyboardmap, 0, 6, (byte)0);
        Arrays.fill(fakemap, 0, fakemap.length, (byte)0);
        realshift = 0;
        probedColumns = 0;
        //fireListeners();
    }
    

    public synchronized void resetJoystick() {
        Arrays.fill(crukeyboardmap, 6, 8, (byte)0);
        Arrays.fill(lastcrukeyboardmap, 6, 8, (byte)0);
    }
    
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
    public synchronized boolean postCharacter(Machine machine, boolean pressed, boolean synthetic, byte shift, char ch, long when) {
    	//System.out.println("post: ch=" + ch + "; shift="+ HexUtils.toHex2(shift)+"; pressed="+pressed);
    	if (isAsciiDirectKey(ch)) {
    		setKey(pressed, synthetic, shift, ch, when);
    		return true;
    	}
    	
		int fctnShifted = shift | FCTN;
    	int ctrlShifted = shift | CTRL;
    	
		switch (ch) {
		
		case 8:
			setKey(pressed, synthetic, ctrlShifted, 'H', when);	/* BKSP */
			break;
		case 9:
			setKey(pressed, synthetic, ctrlShifted, 'I', when);	/* TAB */
			break;
			
		case 13:
			setKey(pressed, synthetic, (byte)0, '\r', when);
			break;
			
			// shifted keys
		case '!':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '1', when);
			break;
		case '@':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '2', when);
			break;
		case '#':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '3', when);
			break;
		case '$':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '4', when);
			break;
		case '%':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '5', when);
			break;
		case '^':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '6', when);
			break;
		case '&':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '7', when);
			break;
		case '*':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '8', when);
			break;
		case '(':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '9', when);
			break;
		case ')':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '0', when);
			break;
		case '+':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '=', when);
			break;
		case '<':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, ',', when);
			break;
		case '>':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '.', when);
			break;
		case ':':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, ';', when);
			break;
			
			// faked keys
		case '`':
			if (0 == (realshift & KeyboardState.SHIFT) && !isSet(KeyboardState.FCTN, 'W'))
				setKey(pressed, synthetic, fctnShifted, 'C', when);	/* ` */
			else
				setKey(pressed, synthetic, fctnShifted, 'W', when);	/* ~ */
			break;
		case '~':
			setKey(pressed, synthetic, fctnShifted, 'W', when);
			break;
		case '-':
			if (0 == (realshift & KeyboardState.SHIFT) && !isSet(KeyboardState.FCTN, 'U'))
				setKey(pressed, synthetic, KeyboardState.SHIFT, '/', when);	/* - */
			else
				setKey(pressed, synthetic, fctnShifted, 'U', when);	/* _ */
			break;
		case '_':
			setKey(pressed, synthetic, fctnShifted, 'U', when);
			break;
		case '[':
			if (0 == (realshift & KeyboardState.SHIFT) && !isSet(KeyboardState.FCTN, 'F'))
				setKey(pressed, synthetic, fctnShifted, 'R', when);	/* [ */
			else
				setKey(pressed, synthetic, fctnShifted, 'F', when);	/* { */
			break;
		case '{':
			setKey(pressed, synthetic, fctnShifted, 'F', when);
			break;
		case ']':
			if (0 == (realshift & KeyboardState.SHIFT) && !isSet(KeyboardState.FCTN, 'G'))
				setKey(pressed, synthetic, fctnShifted, 'T', when);	/* ] */
			else
				setKey(pressed, synthetic, fctnShifted, 'G', when);	/* } */
			break;
		case '}':
			setKey(pressed, synthetic, fctnShifted, 'G', when);
			break;
			
		case '\'':
			if (0 == (realshift & KeyboardState.SHIFT) && !isSet(KeyboardState.FCTN, 'P'))
				setKey(pressed, synthetic, fctnShifted, 'O', when);	/* ' */
			else
				setKey(pressed, synthetic, fctnShifted, 'P', when);	/* " */
			break;
		case '"':
			setKey(pressed, synthetic, fctnShifted, 'P', when);
			break;
		case '/':
			if (0 == (realshift & KeyboardState.SHIFT) && !isSet(KeyboardState.FCTN, 'I'))
				setKey(pressed, synthetic, (byte)0, '/', when);	/* / */
			else
				setKey(pressed, synthetic, fctnShifted, 'I', when);	/* ? */
			break;
		case '?':
			setKey(pressed, synthetic, fctnShifted, 'I', when);
			break;
		case '\\':
			if (0 == (realshift & KeyboardState.SHIFT) && !isSet(KeyboardState.FCTN, 'A'))
				setKey(pressed, synthetic, fctnShifted, 'Z', when);	/* \\ */
			else
				setKey(pressed, synthetic, fctnShifted, 'A', when);	/* | */
			break;
		case '|':
			setKey(pressed, synthetic, fctnShifted, 'A', when);
			break;
			
    	case 127:
    		setKey(pressed, synthetic, fctnShifted, '1', when);	
			break;
		default:
			return false;
    	}
		
		// force the CPU to notice
		if (machine.getCpu().isThrottled()) {
			machine.getCpu().addAllowedCycles(3000);
			//cpu.getCruAccess().triggerInterrupt(InternalCru9901.INT_VDP);
		}
    	return true;
    }
    
    /**
	 * 
	 */
	private void fireListeners() {
		machine.keyStateChanged();
	}

	public static class KeyDelta {
		@Override
		public String toString() {
			return "KeyDelta [key=" + key + ", onoff=" + onoff + ", shift="
					+ shift + ", synthetic=" + synthetic + ", time=" + time
					+ "]";
		}
		int key;
		int shift;
		boolean synthetic;
		boolean onoff;
		public long time;
		public KeyDelta(long time, int key, int shift, boolean synthetic, boolean onoff) {
			this.time = time;
			this.key = key;
			this.shift = shift;
			this.synthetic = synthetic;
			this.onoff = onoff;
		}
		
	}
	/**
     * Set a key in the map.
     * @param onoff true: pressed, false: released
	 * @param synthetic if true, the shift + key are sent together from a synthetic
     * event; else, shifts are sent in separate events from keys, so track them
	 * @param shift FCTN, SHIFT, CTRL mask
	 * @param key normalized ASCII key: no lowercase or shifted characters
	 * @param when TODO
     */
    public synchronized void setKey(boolean onoff, boolean synthetic, int shift, int key, long when) {
        key &= 0xff;
        shift &= 0xff;

        long time = when;
        //if (!onoff)
        //	time += MAX_KEY_DELAY_MS;
        queuedKeys.add(new KeyDelta(time, key, shift, synthetic, onoff));

        fireListeners();
    }
    
    private synchronized void applyKeyDelta(KeyDelta delta) {
    	//System.out.println(delta);
    	
    	boolean onoff = delta.onoff;
    	boolean synthetic = delta.synthetic;
    	int shift = delta.shift;
    	int key = delta.key;
    	
        /* macros bound to high keys */
        /*
        if (key >= 128) {
            keyboard_macro(onoff, shift, key - 128);
            return;
        }*/

        if (!synthetic) {
        	shift = trackRealShiftKeys(onoff, shift, key);
       
	
	        if (key != 0) {
	            changeKeyboardMatrix(onoff, key);
	        } else {
	            if ((shift & SHIFT) != 0)
	                realshift = (byte) ((realshift & ~SHIFT) | (onoff ? SHIFT : 0));
	            if ((shift & CTRL) != 0)
	                realshift = (byte) ((realshift & ~CTRL) | (onoff ? CTRL : 0));
	            if ((shift & FCTN) != 0)
	                realshift = (byte) ((realshift & ~FCTN) | (onoff ? FCTN : 0));
	        }
        } else {
        	// a shift key is sent at the same time as a key
        	changeKeyboardShifts(onoff, shift);
        	changeKeyboardMatrix(onoff, key);
        }
        //if (shift && !onoff)
//            logger(_L | L_1, "turned off [%d]: cshift=%d, cctrl=%d, cfctn=%d\n\n",
                 //shift, cshift, cctrl, cfctn);
    }

	private void changeKeyboardMatrix(boolean onoff, int key) {
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
	
	private void changeKeyboardShifts(boolean onoff, int shift) {
		if (onoff)
			crukeyboardmap[0] |= shift;
		else
			crukeyboardmap[0] &= ~shift;
	}

    /**
	 * This complicated code maintains a map of shifts that we've explicitly
	 * turned on with other keys. The reason we need to know all this is that
	 * there are multiple "on" events (repeats) but only one "off" event. If we
	 * do "left arrow on" (FCTN+S), "right arrow on" (FCTN+D), and
	 * "left arrow off" (FCTN+S) we cannot reset FCTN since FCTN+D is still
	 * pressed. Etc.
	 */

	private int trackRealShiftKeys(boolean onoff, int shift, int key) {
        if (!onoff && shift == 0 && fakemap[key] != 0) {
            //logger(_L | L_1, _("Resetting %d for key %d\n"), fakemap[key], key);
            shift |= fakemap[key];
        }
        fakemap[key] = (byte) (onoff ? shift : 0);

        if ((shift & SHIFT) != 0) {
            if (onoff) {
                if (shiftmap[key] == 0) {
                    shiftmap[key] = 1;
                    cshift++;
                }
                changeKbdMatrix(SHIFT_R, SHIFT_C, true);
            } else {
                if (shiftmap[key] != 0) {
                    shiftmap[key] = 0;
                    cshift--;
                }
                if (cshift == 0)
                    changeKbdMatrix(SHIFT_R, SHIFT_C, false);
            }
        }
        if ((shift & FCTN) != 0) {
            if (onoff) {
                if (fctnmap[key] == 0) {
                    fctnmap[key] = 1;
                    cfctn++;
                }
                changeKbdMatrix(FCTN_R, FCTN_C, true);
            } else {
                if (fctnmap[key] != 0) {
                    fctnmap[key] = 0;
                    cfctn--;
                }
                if (cfctn == 0)
                    changeKbdMatrix(FCTN_R, FCTN_C, false);
            }
        }
        if ((shift & CTRL) != 0) {
            if (onoff) {
                if (ctrlmap[key] == 0) {
                    ctrlmap[key] = 1;
                    cctrl++;
                }
                changeKbdMatrix(CTRL_R, CTRL_C, true);
            } else {
                if (ctrlmap[key] != 0) {
                    ctrlmap[key] = 0;
                    cctrl--;
                }
                if (cctrl == 0)
                    changeKbdMatrix(CTRL_R, CTRL_C, false);
            }
        }
		return shift;
	}
    
    private void changeKbdMatrix(byte r, byte c, boolean v) {
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


    public static final int JOY_X = 1,	// set X-axis
		JOY_Y = 2,	// set Y-axis
		JOY_B = 4	// set buttons
	;
    
	public static final int JOY_FIRE_R = 7;
	public static final int JOY_LEFT_R = 6;
	public static final int JOY_RIGHT_R = 5;
	public static final int JOY_DOWN_R = 4;
	public static final int JOY_UP_R = 3;
	private static final long MAX_KEY_DELAY_MS = 100;


	//	j=1 or 2
	final private void changeJoyMatrix(int j,int r, boolean v) {
		changeKbdMatrix((byte) r, (byte)(JOY1_C+(j)-1), v);
	}
	
	/**
	 * 
	 * @param joy 1 or 2
	 * @param mask JOY_X, JOY_Y, JOY_B
	 * @param x neg or pos or 0
	 * @param y neg or pos or 0
	 * @param fire boolean
	 * @param when TODO
	 */
    public synchronized void setJoystick(int joy, int mask, int x, int y, boolean fire, long when) {
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
    }
    
	public synchronized void setAlpha(boolean on) {
		this.alphaLock = on;
	}

	public synchronized boolean getAlpha() {
		return lastAlphaLock;
	}

	public synchronized byte[] getKeyboardMap() {
		return crukeyboardmap;
	}

	public synchronized int getKeyboardRow(int column) {
		probedColumns |= (1 << column);
		return lastcrukeyboardmap[column];
	}
	
	public synchronized boolean wasKeyboardProbed() {
		return probedColumns == 0x3f;
	}

	public synchronized void resetProbe() {
		probedColumns = 0;
		if (isPasting() && pasteNext)
			pasteTask.run();
		if (!isPasting())
			pushQueuedKey();
		pasteNext = true;
	}
	
	public synchronized void setProbe() {
		pasteNext = true;
	}

	public synchronized void pushQueuedKey() {
		System.arraycopy(crukeyboardmap, 0, lastcrukeyboardmap, 0, 8);
		//for (int i=0;i<8;i++) System.out.print(Utils.toHex2(crukeyboardmap[i])+" "); System.out.println();
		lastAlphaLock = alphaLock;
	}

	public void cancelPaste() {
		resetKeyboard();
		pasteTask = null;
		
	}
	
	/**
	 * Paste text into the clipboard
	 * @param contents
	 */
	public void pasteText(String contents) {

		contents = contents.replaceAll("(\r\n|\r|\n)", "\r");
		contents = contents.replaceAll("\t", "    ");
		final char[] chs = contents.toCharArray();
		
		// this runnable is manually executed, not scheduled
		pasteTask = new Runnable() {
			int index = 0;
			byte prevShift = 0;
			char prevCh = 0;
			int runDelay;
			public void run() {
				if (!machine.isAlive())
					cancelPaste();
				
				if (Machine.settingPauseMachine.getBoolean())
					return;
				
				if (runDelay > 0) {
					runDelay--;
					return;
				} else {
					runDelay = pasteKeyDelay;
				}
				if (index <= chs.length) {
					// only send chars as fast as the machine is reading
					//if (!wasKeyboardProbed())
					//	return;
					
					long now = System.currentTimeMillis();
					if (prevCh != 0) {
						postCharacter(machine, false, true, prevShift, prevCh, now);
					}
					
					if (index < chs.length) {
						char ch = chs[index];
						byte shift = 0;

						if (Character.isLowerCase(ch)) {
				    		ch = Character.toUpperCase(ch);
				    		shift &= ~ KeyboardState.SHIFT;
				    	} else if (Character.isUpperCase(ch)) {
				    		shift |= KeyboardState.SHIFT;
				    	}
				    	
						//System.out.println("ch="+ch+"; prevCh="+prevCh);
						if (ch == prevCh) {
							postCharacter(machine, false, true, shift, ch, now);
							prevCh = 0;
							return;
							/*
							if (successiveCharTimeout == 0) {
								// need to inject a spacer to distinguish 
								// successive repeated characters
								resetKeyboard();
								prevCh = 0;
								successiveCharTimeout = 2;
								return;
							} else if (--successiveCharTimeout > 0) {
								return;
							}
							*/
						}
						
						index++;
						
						postCharacter(machine, true, true, shift, ch, now);
						
						
						prevCh = ch;
						prevShift = shift;
					} else {
						cancelPaste();
					}
				}
			}
			
		};
	}

	public boolean isPasting() {
		return pasteTask != null;
	}

	/**
	 * 
	 */
	public synchronized void nextKey() {
		if (isPasting()) {
			pushQueuedKey();
		} else  {
			long now = System.currentTimeMillis();
			pushQueuedKey();
			while (!queuedKeys.isEmpty()) {
				KeyDelta delta = queuedKeys.peek();
				if (now >= delta.time) {
					queuedKeys.remove();
					applyKeyDelta(delta);
					break;
				} else {
					break;
				}
				//if (delta.time < now + MAX_KEY_DELAY_MS
				//		|| (delta.onoff && !queuedKeys.isEmpty() && !queuedKeys.peek().onoff))
				//	break;
			}
			
			/*
			for (int i=0;i<8;i++) System.out.print(
					HexUtils.toHex2(crukeyboardmap[i])+" "); 
			System.out.println();
			*/
		}
	}

	/**
	 * 
	 */
	public synchronized void checkForPendingKeys() {
		if (!queuedKeys.isEmpty()) {
			nextKey();
			fireListeners();
		}
	}

	/**
	 * 
	 */
	public synchronized void notifyActive() {
		boolean any = anyKeyPressed();
		if (queuedKeys.isEmpty() && any)
			fireListeners();
	}

	public synchronized boolean keyPending() {
		return !queuedKeys.isEmpty();
	}
	public boolean anyKeyPressed() {
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
}
