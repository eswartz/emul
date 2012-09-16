/**
 * 
 */
package v9t9.common.keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import ejs.base.utils.HexUtils;

import v9t9.common.client.IKeyboardHandler;
import v9t9.common.client.KeyDelta;
import static v9t9.common.keyboard.IKeyboardState.*;
import v9t9.common.machine.IBaseMachine;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;

/**
 * @author ejs
 * 
 */
public abstract class BaseKeyboardHandler implements IKeyboardHandler {
	public static boolean DEBUG = false;
	private static final long TIMEOUT = 500;


	/**
	 * @author ejs
	 *
	 */
	private final class PasteTask implements Runnable {
		/**
		 * 
		 */
		private final char[] chs;
		int index = 0;
		byte prevShift = 0;
		char prevCh = 0;
		int runDelay;

		/**
		 * @param chs
		 */
		private PasteTask(char[] chs) {
			this.chs = chs;
		}

		public void run() {
			if (!machine.isAlive())
				cancelPaste();
			
			if (machine.isPaused())
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
					postCharacter(machine, prevCh, false, true, prevShift, prevCh, now);
				}
				
				if (index < chs.length) {
					char ch = chs[index];
					byte shift = 0;

					if (Character.isLowerCase(ch)) {
			    		ch = Character.toUpperCase(ch);
			    		shift &= ~ IKeyboardState.SHIFT;
			    	} else if (Character.isUpperCase(ch)) {
			    		shift |= IKeyboardState.SHIFT;
			    	}
			    	
					//System.out.println("ch="+ch+"; prevCh="+prevCh);
					if (ch == prevCh) {
						postCharacter(machine, prevCh, false, true, shift, ch, now);
						prevCh = 0;
						return;
					}
					
					index++;
					
					postCharacter(machine, ch, true, true, shift, ch, now);
					
					
					prevCh = ch;
					prevShift = shift;
				} else {
					cancelPaste();
				}
			}
		}
	}


	
    private Queue<List<KeyDelta>> queuedKeys = new LinkedList<List<KeyDelta>>();
	private long lastChangeTime;
	private ArrayList<KeyDelta> currentGroup = null;

	protected final IMachine machine;

	protected IKeyboardState keyboardState;
	
	   /* Map of keys whose shifted/ctrled/fctned versions are being tracked */
    private byte fakemap[] = new byte[256];
    private byte shiftmap[] = new byte[256];
    private byte ctrlmap[] = new byte[256];
    private byte fctnmap[] = new byte[256];
    private int cctrl, cfctn, cshift;
    
	protected PasteTask pasteTask;
	private int pasteKeyDelay = 20;

	
	public BaseKeyboardHandler(IKeyboardState keyboardState, IMachine machine) {
		this.keyboardState = keyboardState;
		this.machine = machine;
		machine.setKeyboardHandler(this);
	}

	protected void pushQueuedKey() {
		long now = System.currentTimeMillis();
		if (queuedKeys.isEmpty() && currentGroup == null) {
			if (lastChangeTime + TIMEOUT < now) {
				keyboardState.clearKeyboard();
				
			} else
				return;
		} else {
			if (currentGroup != null) {
				queuedKeys.add(currentGroup);
				currentGroup = null;
			}
			
			keyboardState.clearKeyboard();
			List<KeyDelta> group = queuedKeys.remove();
			byte shift = 0;
			for (KeyDelta delta : group) {
				applyKeyDelta(delta);
				if (delta.onoff) shift |= delta.shift;
			}
			keyboardState.changeKeyboardShifts(true, shift);
		}
		lastChangeTime = now;
		
		keyboardState.applyKeyState();

	}
	
	private void applyKeyDelta(KeyDelta delta) {
    	if (DEBUG) System.out.println(delta);
    	
    	boolean onoff = delta.onoff;
    	boolean synthetic = delta.synthetic;
    	byte shift = delta.shift;
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
	            keyboardState.changeKeyboardMatrix(onoff, key);
	        } else {
	        	byte realshift = keyboardState.getShiftMask();
	            if ((shift & SHIFT) != 0)
	                realshift = (byte) ((realshift & ~SHIFT) | (onoff ? SHIFT : 0));
	            if ((shift & CTRL) != 0)
	                realshift = (byte) ((realshift & ~CTRL) | (onoff ? CTRL : 0));
	            if ((shift & FCTN) != 0)
	                realshift = (byte) ((realshift & ~FCTN) | (onoff ? FCTN : 0));
	            keyboardState.setShiftMask(realshift);
	        }
        } else {
        	// a shift key is sent at the same time as a key
        	keyboardState.changeKeyboardShifts(onoff, shift);
        	keyboardState.changeKeyboardMatrix(onoff, key);
        }
        //if (shift && !onoff)
//            logger(_L | L_1, "turned off [%d]: cshift=%d, cctrl=%d, cfctn=%d\n\n",
                 //shift, cshift, cctrl, cfctn);
    }

	   /**
		 * This complicated code maintains a map of shifts that we've explicitly
		 * turned on with other keys. The reason we need to know all this is that
		 * there are multiple "on" events (repeats) but only one "off" event. If we
		 * do "left arrow on" (FCTN+S), "right arrow on" (FCTN+D), and
		 * "left arrow off" (FCTN+S) we cannot reset FCTN since FCTN+D is still
		 * pressed. Etc.
		 */

		private byte trackRealShiftKeys(boolean onoff, byte shift, int key) {
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
	                keyboardState.changeKbdMatrix(SHIFT_R, SHIFT_C, true);
	            } else {
	                if (shiftmap[key] != 0) {
	                    shiftmap[key] = 0;
	                    cshift--;
	                }
	                if (cshift == 0)
	                	keyboardState.changeKbdMatrix(SHIFT_R, SHIFT_C, false);
	            }
	        }
	        if ((shift & FCTN) != 0) {
	            if (onoff) {
	                if (fctnmap[key] == 0) {
	                    fctnmap[key] = 1;
	                    cfctn++;
	                }
	                keyboardState.changeKbdMatrix(FCTN_R, FCTN_C, true);
	            } else {
	                if (fctnmap[key] != 0) {
	                    fctnmap[key] = 0;
	                    cfctn--;
	                }
	                if (cfctn == 0)
	                	keyboardState.changeKbdMatrix(FCTN_R, FCTN_C, false);
	            }
	        }
	        if ((shift & CTRL) != 0) {
	            if (onoff) {
	                if (ctrlmap[key] == 0) {
	                    ctrlmap[key] = 1;
	                    cctrl++;
	                }
	                keyboardState.changeKbdMatrix(CTRL_R, CTRL_C, true);
	            } else {
	                if (ctrlmap[key] != 0) {
	                    ctrlmap[key] = 0;
	                    cctrl--;
	                }
	                if (cctrl == 0)
	                	keyboardState.changeKbdMatrix(CTRL_R, CTRL_C, false);
	            }
	        }
			return shift;
		}
	    

	/**
	 * 
	 */
	public void resetKeyboard() {
		queuedKeys.clear();
		keyboardState.resetKeyboard();
		Arrays.fill(fakemap, 0, fakemap.length, (byte)0);
	}
	

    /* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#setPasteKeyDelay(int)
	 */
    @Override
	public void setPasteKeyDelay(int times) {
    	this.pasteKeyDelay = times;
    }


	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#resetProbe()
	 */
	@Override
	public synchronized void resetProbe() {
		keyboardState.resetProbe();
		if (isPasting())
			pasteTask.run();
		pushQueuedKey();
//		pasteNext = true;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#setProbe()
	 */
	@Override
	public synchronized void setProbe() {
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#cancelPaste()
	 */
	@Override
	public void cancelPaste() {
		resetKeyboard();
		pasteTask = null;
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#pasteText(java.lang.String)
	 */
	@Override
	public void pasteText(String contents) {

		contents = contents.replaceAll("(\r\n|\r|\n)", "\r");
		contents = contents.replaceAll("\t", "    ");
		final char[] chs = contents.toCharArray();
		
		// this runnable is manually executed, not scheduled
		pasteTask = new PasteTask(chs);
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#isPasting()
	 */
	@Override
	public boolean isPasting() {
		return pasteTask != null;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IKeyboardHandler#anyKeyPressed()
	 */
	@Override
	public boolean anyKeyPressed() {
		return !queuedKeys.isEmpty() || keyboardState.anyKeyPressed();
	}

	
  @Override
	public synchronized void setKey(int realKey, boolean onoff, boolean synthetic, byte shift, int key, long when) {
      key &= 0xff;
      shift &= 0xff;

      long time = when;

      KeyDelta delta = new KeyDelta(time, realKey, key, shift, synthetic, onoff);
      if (currentGroup == null) {
      	currentGroup = new ArrayList<KeyDelta>();
      } else if (!delta.groupsWith(currentGroup)) {
      	queuedKeys.add(currentGroup);
      	currentGroup = new ArrayList<KeyDelta>();
      }
      currentGroup.add(delta);
    
		machine.keyStateChanged();
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
	public synchronized boolean postCharacter(IBaseMachine machine, int realKey, boolean pressed, boolean synthetic, byte shift, char ch, long when) {
		if (DEBUG) System.out.println(when + "==> post: ch=" + ch + "; shift="+ HexUtils.toHex2(shift)+"; pressed="+pressed);
		if (keyboardState.isAsciiDirectKey(ch)) {
			setKey(realKey, pressed, synthetic, shift, ch, when);
			return true;
		}
		
		byte fctnShifted = (byte) (shift | FCTN);
		byte ctrlShifted = (byte) (shift | CTRL);
		byte shiftShifted = (byte) (shift | SHIFT);
		
		byte realshift = keyboardState.getShiftMask();
		
		switch (ch) {
		
		case 8:
			if (Settings.get(machine, IKeyboardState.settingBackspaceIsCtrlH).getBoolean())
				setKey(realKey, pressed, synthetic, ctrlShifted, 'H', when);	/* BKSP */
			else
				setKey(realKey, pressed, synthetic, fctnShifted, 'S', when);	/* FCTN-S */
			break;
		case 9:
			setKey(realKey, pressed, synthetic, ctrlShifted, 'I', when);	/* TAB */
			break;
			
		case 13:
			setKey(realKey, pressed, synthetic, (byte)0, '\r', when);
			break;
			
			// shifted keys
		case '!':
			setKey(realKey, pressed, synthetic, shiftShifted, '1', when);
			break;
		case '@':
			setKey(realKey, pressed, synthetic, shiftShifted, '2', when);
			break;
		case '#':
			setKey(realKey, pressed, synthetic, shiftShifted, '3', when);
			break;
		case '$':
			setKey(realKey, pressed, synthetic, shiftShifted, '4', when);
			break;
		case '%':
			setKey(realKey, pressed, synthetic, shiftShifted, '5', when);
			break;
		case '^':
			setKey(realKey, pressed, synthetic, shiftShifted, '6', when);
			break;
		case '&':
			setKey(realKey, pressed, synthetic, shiftShifted, '7', when);
			break;
		case '*':
			setKey(realKey, pressed, synthetic, shiftShifted, '8', when);
			break;
		case '(':
			setKey(realKey, pressed, synthetic, shiftShifted, '9', when);
			break;
		case ')':
			setKey(realKey, pressed, synthetic, shiftShifted, '0', when);
			break;
		case '+':
			setKey(realKey, pressed, synthetic, shiftShifted, '=', when);
			break;
		case '<':
			setKey(realKey, pressed, synthetic, shiftShifted, ',', when);
			break;
		case '>':
			setKey(realKey, pressed, synthetic, shiftShifted, '.', when);
			break;
		case ':':
			setKey(realKey, pressed, synthetic, shiftShifted, ';', when);
			break;
			
			// faked keys
		case '`':
			if (0 == (realshift & IKeyboardState.SHIFT) && !keyboardState.isSet(IKeyboardState.FCTN, 'W'))
				setKey(realKey, pressed, synthetic, fctnShifted, 'C', when);	/* ` */
			else
				setKey(realKey, pressed, synthetic, fctnShifted, 'W', when);	/* ~ */
			break;
		case '~':
			setKey(realKey, pressed, synthetic, fctnShifted, 'W', when);
			break;
		case '-':
			if (0 == (realshift & IKeyboardState.SHIFT) && !keyboardState.isSet(IKeyboardState.FCTN, 'U'))
				setKey(realKey, pressed, synthetic, IKeyboardState.SHIFT, '/', when);	/* - */
			else
				setKey(realKey, pressed, synthetic, fctnShifted, 'U', when);	/* _ */
			break;
		case '_':
			setKey(realKey, pressed, synthetic, fctnShifted, 'U', when);
			break;
		case '[':
			if (0 == (realshift & IKeyboardState.SHIFT) && !keyboardState.isSet(IKeyboardState.FCTN, 'F'))
				setKey(realKey, pressed, synthetic, fctnShifted, 'R', when);	/* [ */
			else
				setKey(realKey, pressed, synthetic, fctnShifted, 'F', when);	/* { */
			break;
		case '{':
			setKey(realKey, pressed, synthetic, fctnShifted, 'F', when);
			break;
		case ']':
			if (0 == (realshift & IKeyboardState.SHIFT) && !keyboardState.isSet(IKeyboardState.FCTN, 'G'))
				setKey(realKey, pressed, synthetic, fctnShifted, 'T', when);	/* ] */
			else
				setKey(realKey, pressed, synthetic, fctnShifted, 'G', when);	/* } */
			break;
		case '}':
			setKey(realKey, pressed, synthetic, fctnShifted, 'G', when);
			break;
			
		case '\'':
			if (0 == (realshift & IKeyboardState.SHIFT) && !keyboardState.isSet(IKeyboardState.FCTN, 'P'))
				setKey(realKey, pressed, synthetic, fctnShifted, 'O', when);	/* ' */
			else
				setKey(realKey, pressed, synthetic, fctnShifted, 'P', when);	/* " */
			break;
		case '"':
			setKey(realKey, pressed, synthetic, fctnShifted, 'P', when);
			break;
		case '/':
			if (0 == (realshift & IKeyboardState.SHIFT) && !keyboardState.isSet(IKeyboardState.FCTN, 'I'))
				setKey(realKey, pressed, synthetic, (byte)0, '/', when);	/* / */
			else
				setKey(realKey, pressed, synthetic, fctnShifted, 'I', when);	/* ? */
			break;
		case '?':
			setKey(realKey, pressed, synthetic, fctnShifted, 'I', when);
			break;
		case '\\':
			if (0 == (realshift & IKeyboardState.SHIFT) && !keyboardState.isSet(IKeyboardState.FCTN, 'A'))
				setKey(realKey, pressed, synthetic, fctnShifted, 'Z', when);	/* \\ */
			else
				setKey(realKey, pressed, synthetic, fctnShifted, 'A', when);	/* | */
			break;
		case '|':
			setKey(realKey, pressed, synthetic, fctnShifted, 'A', when);
			break;
			
		case 127:
			setKey(realKey, pressed, synthetic, fctnShifted, '1', when);	
			break;
		default:
			return false;
		}
		
		return true;
	}
}
