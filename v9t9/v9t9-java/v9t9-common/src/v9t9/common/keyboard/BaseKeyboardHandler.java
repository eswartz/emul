/**
 * 
 */
package v9t9.common.keyboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import ejs.base.properties.IProperty;
import ejs.base.utils.HexUtils;

import v9t9.common.client.IKeyboardHandler;
import v9t9.common.client.KeyDelta;
import v9t9.common.events.IEventNotifier;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import static v9t9.common.keyboard.KeyboardConstants.*;

/**
 * High-level conversion of host OS keyboard events into intermediate {@link KeyboardConstants}.
 * <p/>
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
 * @author ejs
 * 
 */
public abstract class BaseKeyboardHandler implements IKeyboardHandler {
	public static boolean DEBUG = true;
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
				
				if (prevCh != 0) {
					postCharacter(false, prevShift, prevCh);
				}
				
				if (index < chs.length) {
					char ch = chs[index];
					byte shift = 0;

					if (Character.isLowerCase(ch)) {
			    		ch = Character.toUpperCase(ch);
			    		shift &= ~ MASK_SHIFT;
			    	} else if (Character.isUpperCase(ch)) {
			    		shift |= MASK_SHIFT;
			    	}
			    	
					//System.out.println("ch="+ch+"; prevCh="+prevCh);
					if (ch == prevCh) {
						postCharacter(false, shift, ch);
						prevCh = 0;
						return;
					}
					
					index++;
					
					postCharacter(true, shift, ch);
					
					
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
//    private byte fakemap[] = new byte[256];
//    private byte shiftmap[] = new byte[256];
//    private byte ctrlmap[] = new byte[256];
//    private byte fctnmap[] = new byte[256];
//    private int cctrl, cfctn, cshift;
    
	protected PasteTask pasteTask;
	private int pasteKeyDelay = 20;
	private IEventNotifier eventNotifier;

	
	public BaseKeyboardHandler(IKeyboardState keyboardState, IMachine machine) {
		this.keyboardState = keyboardState;
		this.machine = machine;
		machine.setKeyboardHandler(this);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IKeyboardHandler#setEventNotifier(v9t9.common.events.IEventNotifier)
	 */
	@Override
	public void setEventNotifier(IEventNotifier notifier) {
		this.eventNotifier = notifier;
	}

	protected synchronized void applyKeyGroup() {
		long now = System.currentTimeMillis();
		if (queuedKeys.isEmpty() && currentGroup == null) {
			if (lastChangeTime + TIMEOUT < now) {
				keyboardState.incrClearKeyboard();
				
			} else
				return;
		} else {
			if (currentGroup != null) {
				queuedKeys.add(currentGroup);
				currentGroup = null;
			}
			
			keyboardState.incrClearKeyboard();
			List<KeyDelta> group = queuedKeys.remove();
			
			Set<Integer> keys = new HashSet<Integer>();
			for (KeyDelta delta : group) {
				if (delta.onoff) {
					keys.add(delta.key);
				}
			}
			keyboardState.setKeysFrom(keys);
			
//			for (KeyDelta delta : group) {
//				keyboardState.incrSetKey(delta.onoff, delta.key);
//			}
			
		}
		lastChangeTime = now;
		
		keyboardState.applyIncrKeyState();

	}

//   /**
//	 * This complicated code maintains a map of shifts that we've explicitly
//	 * turned on with other keys. The reason we need to know all this is that
//	 * there are multiple "on" events (repeats) but only one "off" event. If we
//	 * do "left arrow on" (FCTN+S), "right arrow on" (FCTN+D), and
//	 * "left arrow off" (FCTN+S) we cannot reset FCTN since FCTN+D is still
//	 * pressed. Etc.
//	 */
//
//	private byte trackRealShiftKeys(boolean onoff, byte shift, int key) {
//        if (!onoff && shift == 0 && fakemap[key] != 0) {
//            System.err.println("Resetting "+fakemap[key]+" for key "+key);
//            shift |= fakemap[key];
//        }
//        fakemap[key] = (byte) (onoff ? shift : 0);
//
//        byte effShift = 0;
//        
//        if ((shift & MASK_SHIFT) != 0) {
//            if (onoff) {
//                if (shiftmap[key] == 0) {
//                    shiftmap[key] = 1;
//                    cshift++;
//                }
//                effShift |= MASK_SHIFT; 
//            } else {
//                if (shiftmap[key] != 0) {
//                    shiftmap[key] = 0;
//                    cshift--;
//                }
//                if (cshift == 0)
//                	effShift |= MASK_SHIFT;
//            }
//        }
//        if ((shift & MASK_ALT) != 0) {
//            if (onoff) {
//                if (fctnmap[key] == 0) {
//                    fctnmap[key] = 1;
//                    cfctn++;
//                }
//                effShift |= MASK_ALT; 
//            } else {
//                if (fctnmap[key] != 0) {
//                    fctnmap[key] = 0;
//                    cfctn--;
//                }
//                if (cfctn == 0)
//                	effShift |= MASK_ALT;
//            }
//        }
//        if ((shift & MASK_CONTROL) != 0) {
//            if (onoff) {
//                if (ctrlmap[key] == 0) {
//                    ctrlmap[key] = 1;
//                    cctrl++;
//                }
//                effShift |= MASK_CONTROL; 
//            } else {
//                if (ctrlmap[key] != 0) {
//                    ctrlmap[key] = 0;
//                    cctrl--;
//                }
//                if (cctrl == 0)
//                	effShift |= MASK_CONTROL;
//            }
//        }
//        
//        keyboardState.changeShifts(onoff, effShift);
//		return shift;
//	}
//    

	/**
	 * 
	 */
	public void resetKeyboard() {
		queuedKeys.clear();
		keyboardState.resetKeyboard();
//		Arrays.fill(fakemap, 0, fakemap.length, (byte)0);
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
		if (isPasting())
			pasteTask.run();
		applyKeyGroup();
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
	public synchronized boolean anyKeyAvailable() {
		return !queuedKeys.isEmpty() || currentGroup != null || isPasting() || keyboardState.anyKeyPressed();
	}


	/**
	 * @param onoff
	 * @param shiftBits
	 */
	protected void pushShifts(boolean onoff, byte shiftBits) {
		int shiftVal = KEY_SHIFT;
		while (shiftBits != 0) {
			int shiftMask = 1 << shiftVal;
			if ((shiftBits & shiftMask) != 0)
				pushKey(onoff, shiftVal);
			shiftBits &= ~shiftMask;
			shiftVal++;
		}
	}

	/**
	 * @param onoff
	 * @param shift
	 * @param key
	 */
	protected synchronized void pushKey(boolean onoff, int key) {
		KeyDelta delta = new KeyDelta(onoff, key);
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
	 * @param pressed
	* and there are not distinct shift key events; otherwise, apply logic
	* to detect the patterns of real shift key presses and releases
	 * @param shift extra shift keys
	 * @param ch
	* @return true if we could represent it as ASCII
	*/
	public synchronized boolean postCharacter(boolean pressed, byte shift, char ch) {
		if (DEBUG) System.out.println("==> post: ch=" + ch + "; shift="+ HexUtils.toHex2(shift)+"; pressed="+pressed);

		// check for recognized non-hardware keys
		switch (ch) {
		case KEY_MINUS:
		case KEY_EQUALS:
		case KEY_OPEN_BRACKET:
		case KEY_CLOSE_BRACKET:
		case KEY_BACK_SLASH:
		case KEY_SLASH:
		case KEY_COMMA:
		case KEY_PERIOD:
		case KEY_SINGLE_QUOTE:
		case KEY_SEMICOLON:
			pushShifts(pressed, shift);
			pushKey(pressed, ch);
			return true;

			
		case KEY_BACK_QUOTE:	
		case KEY_TILDE:	
		case KEY_EXCLAMATION:
		case KEY_AT:
		case KEY_POUND:
		case KEY_DOLLAR:
		case KEY_PERCENT:
		case KEY_CIRCUMFLEX:
		case KEY_AMPERSAND:
		case KEY_ASTERISK:
		case KEY_OPEN_PARENTHESIS:
		case KEY_CLOSE_PARENTHESIS:
		case KEY_UNDERSCORE:
		case KEY_PLUS:
		case KEY_OPEN_BRACE:
		case KEY_CLOSE_BRACE:
		case KEY_BAR:
		case KEY_QUESTION:
		case KEY_LESS:
		case KEY_GREATER:
		case KEY_QUOTE:
		case KEY_COLON:
			pushShifts(pressed, (byte) (shift & ~MASK_SHIFT));
			pushKey(pressed, ch);
			return true;
		}
		
		if (ch > 0 && keyboardState.isAsciiDirectKey(ch)) {
			pushShifts(pressed, shift);
			pushKey(pressed, ch);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Act on a keypress which is not represented in the keyboard map.
	 * @param pressed
	 * @param key
	 * @return true if key should be ignored
	 */
	protected boolean handleActionKey(boolean pressed, int key) {
		
		switch (key) { 
			case KEY_BREAK:
				if (pressed) {
					machine.asyncExec(new Runnable() {
						public void run() {
							machine.getClient().close();
						}
					});
				}
				return true;
			case KEY_PAUSE:
				if (pressed) {
					IProperty paused = Settings.get(machine, IMachine.settingPauseMachine);
					paused.setBoolean(!paused.getBoolean());
				}
				return true;
			case KEY_NUM_LOCK:
				if (pressed) {
					keyboardState.toggleKeyboardLocks(MASK_NUM_LOCK);
				}
				return true;
			case KEY_CAPS_LOCK:
				if (pressed) {
					keyboardState.toggleKeyboardLocks(MASK_CAPS_LOCK);
				}
				return true;
			case KEY_SCROLL_LOCK:
				if (pressed) {
					keyboardState.toggleKeyboardLocks(MASK_SCROLL_LOCK);
					
					boolean speedy = machine.getCpu().settingRealTime().getBoolean();
					machine.getCpu().settingRealTime().setBoolean(!speedy);
					if (eventNotifier != null)
						eventNotifier.notifyEvent(null, Level.INFO, 
								speedy ? "Scroll Lock: Executing at maximum speed" : 
									"Scroll Lock: Executing at fixed rate");
					//VdpTMS9918A.settingCpuSynchedVdpInterrupt.setBoolean(speedy);
				}
				return true;
		}
		return false;
	}



}
