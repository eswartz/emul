/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Aug 27, 2005
 *
 */
package v9t9.keyboard;

import java.util.Arrays;

import v9t9.emulator.runtime.Cpu;

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
    static final byte JOY1_C = 6;
    static final byte JOY1_R = 7;

    /* Map of keys whose shifted/ctrled/fctned versions are being tracked */
    private byte fakemap[] = new byte[256];
    private byte shiftmap[] = new byte[256];
    private byte ctrlmap[] = new byte[256];
    private byte fctnmap[] = new byte[256];
    private int cctrl, cfctn, cshift;

    /** 'real' shift keys being held down, as opposed to those being synthesized */
    private byte realshift;
    
	private byte[] crukeyboardmap = new byte[8];
	/** actual state of alpha */
	private boolean alphaLock;
	private int probedColumns;

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

    public KeyboardState(Cpu cpu) {
		//this.cpu = cpu;
        
    }
    
    public synchronized void resetKeyboard() {
        Arrays.fill(getKeyboardMap(), 0, 8, (byte)0);
        Arrays.fill(fakemap, 0, fakemap.length, (byte)0);
        realshift = 0;
    }
    
    /**
     * Post an ASCII character, applying any conversions to make it
     * a legal keystroke on the 99/4A keyboard.
     * @param pressed
     * @param synthetic if true, the character came from, e.g., pasted text,
     * and there are not distinct shift key events; otherwise, apply logic
     * to detect the patterns of real shift key presses and releases
     * @param shift extra shift keys
     * @param ch
     * @return true if we could represent it as ASCII
     */
    public synchronized boolean postCharacter(boolean pressed, boolean synthetic, byte shift, char ch) {
    	//System.out.println("post: ch=" + ch + "; shift="+ Utils.toHex2(shift)+"; pressed="+pressed);
    	if (isAsciiDirectKey(ch)) {
    		setKey(pressed, synthetic, shift, ch);
    		return true;
    	}
    	
		int fctnShifted = shift | FCTN;
    	int ctrlShifted = shift | CTRL;
    	
		switch (ch) {
		
		case 8:
			setKey(pressed, synthetic, ctrlShifted, 'H');	/* BKSP */
			break;
		case 9:
			setKey(pressed, synthetic, ctrlShifted, 'I');	/* TAB */
			break;
			
		case 13:
			setKey(pressed, synthetic, (byte)0, '\r');
			break;
			
			// shifted keys
		case '!':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '1');
			break;
		case '@':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '2');
			break;
		case '#':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '3');
			break;
		case '$':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '4');
			break;
		case '%':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '5');
			break;
		case '^':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '6');
			break;
		case '&':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '7');
			break;
		case '*':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '8');
			break;
		case '(':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '9');
			break;
		case ')':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '0');
			break;
		case '+':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '=');
			break;
		case '<':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, ',');
			break;
		case '>':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, '.');
			break;
		case ':':
			setKey(pressed, synthetic, shift | KeyboardState.SHIFT, ';');
			break;
			
			// faked keys
		case '`':
			if (0 == (realshift & KeyboardState.SHIFT) && !isSet(KeyboardState.FCTN, 'W'))
				setKey(pressed, synthetic, fctnShifted, 'C');	/* ` */
			else
				setKey(pressed, synthetic, fctnShifted, 'W');	/* ~ */
			break;
		case '~':
			setKey(pressed, synthetic, fctnShifted, 'W');
			break;
		case '-':
			if (0 == (realshift & KeyboardState.SHIFT) && !isSet(KeyboardState.FCTN, 'U'))
				setKey(pressed, synthetic, KeyboardState.SHIFT, '/');	/* - */
			else
				setKey(pressed, synthetic, fctnShifted, 'U');	/* _ */
			break;
		case '_':
			setKey(pressed, synthetic, fctnShifted, 'U');
			break;
		case '[':
			if (0 == (realshift & KeyboardState.SHIFT) && !isSet(KeyboardState.FCTN, 'F'))
				setKey(pressed, synthetic, fctnShifted, 'R');	/* [ */
			else
				setKey(pressed, synthetic, fctnShifted, 'F');	/* { */
			break;
		case '{':
			setKey(pressed, synthetic, fctnShifted, 'F');
			break;
		case ']':
			if (0 == (realshift & KeyboardState.SHIFT) && !isSet(KeyboardState.FCTN, 'G'))
				setKey(pressed, synthetic, fctnShifted, 'T');	/* ] */
			else
				setKey(pressed, synthetic, fctnShifted, 'G');	/* } */
			break;
		case '}':
			setKey(pressed, synthetic, fctnShifted, 'G');
			break;
			
		case '\'':
			if (0 == (realshift & KeyboardState.SHIFT) && !isSet(KeyboardState.FCTN, 'P'))
				setKey(pressed, synthetic, fctnShifted, 'O');	/* ' */
			else
				setKey(pressed, synthetic, fctnShifted, 'P');	/* " */
			break;
		case '"':
			setKey(pressed, synthetic, fctnShifted, 'P');
			break;
		case '/':
			if (0 == (realshift & KeyboardState.SHIFT) && !isSet(KeyboardState.FCTN, 'I'))
				setKey(pressed, synthetic, (byte)0, '/');	/* / */
			else
				setKey(pressed, synthetic, fctnShifted, 'I');	/* ? */
			break;
		case '?':
			setKey(pressed, synthetic, fctnShifted, 'I');
			break;
		case '\\':
			if (0 == (realshift & KeyboardState.SHIFT) && !isSet(KeyboardState.FCTN, 'A'))
				setKey(pressed, synthetic, fctnShifted, 'Z');	/* \\ */
			else
				setKey(pressed, synthetic, fctnShifted, 'A');	/* | */
			break;
		case '|':
			setKey(pressed, synthetic, fctnShifted, 'A');
			break;
			
    	case 127:
    		setKey(pressed, synthetic, fctnShifted, '1');	
			break;
		default:
			return false;
    	}
    	return true;
    }
    
    /**
     * Set a key in the map.
     * @param onoff true: pressed, false: released
     * @param synthetic if true, the shift + key are sent together from a synthetic
     * event; else, shifts are sent in separate events from keys, so track them
     * @param shift FCTN, SHIFT, CTRL mask
     * @param key normalized ASCII key: no lowercase or shifted characters
     */
    public synchronized void setKey(boolean onoff, boolean synthetic, int shift, int key) {
        key &= 0xff;
        shift &= 0xff;

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
		    
		    CHANGEKBDCRU(r, c, onoff ? 1 : 0);
		    
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
                CHANGEKBDCRU(SHIFT_R, SHIFT_C, 1);
            } else {
                if (shiftmap[key] != 0) {
                    shiftmap[key] = 0;
                    cshift--;
                }
                if (cshift == 0)
                    CHANGEKBDCRU(SHIFT_R, SHIFT_C, 0);
            }
        }
        if ((shift & FCTN) != 0) {
            if (onoff) {
                if (fctnmap[key] == 0) {
                    fctnmap[key] = 1;
                    cfctn++;
                }
                CHANGEKBDCRU(FCTN_R, FCTN_C, 1);
            } else {
                if (fctnmap[key] != 0) {
                    fctnmap[key] = 0;
                    cfctn--;
                }
                if (cfctn == 0)
                    CHANGEKBDCRU(FCTN_R, FCTN_C, 0);
            }
        }
        if ((shift & CTRL) != 0) {
            if (onoff) {
                if (ctrlmap[key] == 0) {
                    ctrlmap[key] = 1;
                    cctrl++;
                }
                CHANGEKBDCRU(CTRL_R, CTRL_C, 1);
            } else {
                if (ctrlmap[key] != 0) {
                    ctrlmap[key] = 0;
                    cctrl--;
                }
                if (cctrl == 0)
                    CHANGEKBDCRU(CTRL_R, CTRL_C, 0);
            }
        }
		return shift;
	}
    
    private void CHANGEKBDCRU(byte r, byte c, int v) {
        if (v != 0)
            SETKBDCRU(r, c);
        else
            RESETKBDCRU(r, c);
    }

    private boolean TESTKBDCRU(byte r, byte c) {
        return (crukeyboardmap[c] & (0x80 >> r)) != 0;
    }

    private void RESETKBDCRU(byte r, byte c) {
    	crukeyboardmap[c] &= ~(0x80 >> r);
    }

    private void SETKBDCRU(byte r, byte c) {
    	crukeyboardmap[c] |= (0x80 >> r);
    }

    public synchronized boolean isSet(byte shift, int key) {
        byte b, r, c;
        boolean res = false;

        if ((shift & SHIFT) != 0 && TESTKBDCRU(SHIFT_R, SHIFT_C))
            res = true;
        if ((shift & CTRL) != 0 && TESTKBDCRU(CTRL_R, CTRL_C))
            res = true;
        if ((shift & FCTN) != 0 && TESTKBDCRU(FCTN_R, FCTN_C))
            res = true;

        if (key != 0) {
            b = (byte) latinto9901[key];
            /*if (b == 0xff)
                logger(_L | L_0,
                     _("keyboard_isset:  got a key that should be faked '%c' (%d)\n\n"),
                     key, key);*/
            r = (byte) (b >> 4);
            c = (byte) (b & 15);
            return res && TESTKBDCRU(r, c);
        } else
            return res;
    }
    
	public synchronized void setAlpha(boolean on) {
		this.alphaLock = on;
	}

	public synchronized boolean getAlpha() {
		return alphaLock;
	}

	public synchronized byte[] getKeyboardMap() {
		return crukeyboardmap;
	}

	public synchronized int getKeyboardRow(int column) {
		probedColumns |= (1 << column);
		return crukeyboardmap[column];
	}
	
	public synchronized boolean wasKeyboardProbed() {
		return probedColumns == 0x3f;
	}

	public void resetProbe() {
		probedColumns = 0;
	}
}
