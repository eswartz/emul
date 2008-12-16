/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Aug 27, 2005
 *
 */
package v9t9.keyboard;

import java.util.Arrays;

import v9t9.emulator.clients.builtin.InternalCru;
import v9t9.emulator.runtime.Cpu;

public class KeyboardState {
    /* Masks */
    public static final byte SHIFT = 1;
    public static final byte FCTN = 2;
    public static final byte CTRL = 4;
    
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
	private final InternalCru cru;
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

    public KeyboardState(Cpu cpu, InternalCru cru) {
		//this.cpu = cpu;
		this.cru = cru;
        
    }
    
    public void resetKeyboard() {
        Arrays.fill(cru.getKeyboardMap(), 0, 8, (byte)0);
    }
    
    /**
     * Set a key in the map.
     * @param onoff true: pressed, false: released
     * @param shift FCTN, SHIFT, CTRL mask
     * @param key normalized ASCII key: no lowercase or shifted characters
     */
    public void setKey(boolean onoff, byte shift, int key) {
        byte b, r, c;
        key &= 0xff;

        //if (shift && onoff)
            //logger(_L | L_1, "turned on [%d]:  cshift=%d, cctrl=%d, cfctn=%d\n",
                 //shift, cshift, cctrl, cfctn);

        /* macros bound to high keys */
        /*
        if (key >= 128) {
            keyboard_macro(onoff, shift, key - 128);
            return;
        }*/

        /*  This complicated code maintains a map of shifts
           that we've explicitly turned on with other keys.  The
           reason we need to know all this is that there are
           multiple "on" events (repeats) but only one "off"
           event.  If we do "left arrow on" (FCTN+S), 
           "right arrow on" (FCTN+D), and "left arrow off" (FCTN+S)
           we cannot reset FCTN since FCTN+D is still pressed.  Etc. */

        if (!onoff && shift == 0 && fakemap[key] != 0) {
            //logger(_L | L_1, _("Resetting %d for key %d\n"), fakemap[key], key);
            shift |= fakemap[key];
        }
        fakemap[key] = onoff ? shift : 0;

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

        if (key != 0) {
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
        } else {
            if ((shift & SHIFT) != 0)
                realshift = (byte) ((realshift & ~SHIFT) | (onoff ? SHIFT : 0));
            if ((shift & CTRL) != 0)
                realshift = (byte) ((realshift & ~CTRL) | (onoff ? CTRL : 0));
            if ((shift & FCTN) != 0)
                realshift = (byte) ((realshift & ~FCTN) | (onoff ? FCTN : 0));
        }

        //if (shift && !onoff)
//            logger(_L | L_1, "turned off [%d]: cshift=%d, cctrl=%d, cfctn=%d\n\n",
                 //shift, cshift, cctrl, cfctn);
    }
    
    private void CHANGEKBDCRU(byte r, byte c, int v) {
        if (v != 0)
            SETKBDCRU(r, c);
        else
            RESETKBDCRU(r, c);
    }

    private boolean TESTKBDCRU(byte r, byte c) {
        return (cru.getKeyboardMap()[c] & (0x80 >> r)) != 0;
    }

    private void RESETKBDCRU(byte r, byte c) {
        cru.getKeyboardMap()[c] &= ~(0x80 >> r);
    }

    private void SETKBDCRU(byte r, byte c) {
        cru.getKeyboardMap()[c] |= (0x80 >> r);
    }

    public boolean isSet(byte shift, int key) {
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
    
    public byte getRealShift() {
        return realshift;
    }

	public void setAlpha(boolean on) {
		this.cru.setAlphaLock(on);
	}

	public boolean getAlpha() {
		return cru.getAlphaLock();
	}
}
