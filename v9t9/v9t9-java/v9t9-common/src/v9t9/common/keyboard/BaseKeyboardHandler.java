/*
  BaseKeyboardHandler.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.keyboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;

import ejs.base.properties.IProperty;
import ejs.base.utils.HexUtils;
import ejs.base.utils.ListenerList;

import v9t9.common.client.IKeyboardHandler;
import v9t9.common.client.KeyDelta;
import v9t9.common.client.IVideoRenderer.IVideoRenderListener;
import v9t9.common.events.IEventNotifier;
import v9t9.common.events.NotifyEvent.Level;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.common.video.IVdpCanvas;
import static v9t9.common.keyboard.KeyboardConstants.*;

/**
 * High-level conversion of host OS keyboard events into intermediate {@link KeyboardConstants}.
 * @author ejs
 * 
 */
public abstract class BaseKeyboardHandler implements IKeyboardHandler {

	private static final Logger log = Logger.getLogger(BaseKeyboardHandler.class);
	
	
	public static boolean DEBUG = false;
	private static final long TIMEOUT = 500;

	private IProperty useNumPadForJoystick;

	protected PasteTask pasteTask;
	private IEventNotifier eventNotifier;

	private ListenerList<IPasteListener> pasteListeners = new ListenerList<IPasteListener>();


	/**
	 * @author ejs
	 *
	 */
	private static final class PasteTask implements Runnable, IVideoRenderListener {
		/**
		 * 
		 */
		private final char[] chs;
		int index = 0;
		byte prevShift = 0;
		char prevCh = 0;
		long nextTime;
		private IKeyboardHandler handler;
		private boolean redrew;
		private boolean awaitingRedraw;
		private boolean holdingDown;

		/**
		 * @param chs
		 */
		private PasteTask(IKeyboardHandler handler, char[] chs) {
			this.handler = handler;
			this.chs = chs;
			
			log.debug("Pasting text: " + new String(chs));
			
			handler.getMachine().getClient().getVideoRenderer().addListener(this);
		}
		
		/* (non-Javadoc)
		 * @see v9t9.common.client.IVideoRenderer.IVideoRenderListener#finishedRedraw(v9t9.common.video.IVdpCanvas)
		 */
		@Override
		public synchronized void finishedRedraw(IVdpCanvas canvas) {
			redrew = true;
		}

		public void run() {
			if (!handler.getMachine().isAlive())
				handler.cancelPaste();
			
			if (handler.getMachine().isPaused())
				return;
			
			long now = System.currentTimeMillis();
			if (now < nextTime) {
				return;
			} 
			
			int pasteDelay = handler.getMachine().getSettings().get(IKeyboardHandler.settingPasteKeyDelay).getInt();
			
			if (index <= chs.length) {
				// only send chars as fast as the machine is reading
//				if (!keyboardState.wasKeyboardProbed())
//					return;

				//System.out.println("ch="+ch+"; prevCh="+prevCh);
				handler.flushCurrentGroup();
				
				if (prevCh != 0 && !holdingDown) {
					handler.postCharacter(false, prevShift, prevCh);
					prevCh = 0;
				}
				
				if (index < chs.length) {
					char ch = chs[index];
					byte shift = 0;


					if (ch == WAIT_FOR_FLUSH) {
						// wait for current keys to flush
						if (handler.isAnyKeyPending()) {
							handler.applyKeyGroup();
							nextTime = now + pasteDelay;
							return;
						}
						
						//nextTime += 1 * 1000;
						index++;
						return;
					}
					else if (ch == WAIT_VIDEO) {
						// wait for some change in video
						synchronized (this) {
							// flush keys first
							if (handler.isAnyKeyPending()) {
								handler.applyKeyGroup();
								nextTime = now + pasteDelay;
								return;
							}
							
							if (!awaitingRedraw) {
								redrew = false;
								awaitingRedraw = true;
								nextTime = now + 500;
							} else {
								if (redrew) {
									nextTime = now + 500;
									index++;
								}
							}
							return;
						}
					}

					synchronized (this) {
						redrew = false;
					}
					
					if (ch == FCTN) {
						index++;
						shift |= MASK_ALT;
						ch = chs[index];
					}
					else if (ch == HOLD_DOWN) {
						index++;
						holdingDown = true;
						return;
					}
					else if (ch == RELEASE) {
						index++;
						holdingDown = false;
						return;
					}
					else {
						if (Character.isLowerCase(ch)) {
				    		ch = Character.toUpperCase(ch);
				    		shift &= ~ MASK_SHIFT;
				    	} else if (Character.isUpperCase(ch)) {
				    		shift |= MASK_SHIFT;
				    	}
					}
					
					if (!holdingDown) {
						handler.postCharacter(false, shift, ch);
					}
					
					if (ch == prevCh) {
						prevCh = 0;
						nextTime += pasteDelay;
						return;
					}

					handler.flushCurrentGroup();

					handler.postCharacter(true, shift, ch);
					
					nextTime = now + pasteDelay;
					index++;
					
					prevCh = ch;
					prevShift = shift;
					
					handler.applyKeyGroup();
					
				} else {
					if (handler.isAnyKeyPending()) {
						handler.applyKeyGroup();
						nextTime = now + pasteDelay;
					} else {
						handler.finishPaste();
					}
				}
			}
		}

		/**
		 * 
		 */
		public void dispose() {
			handler.getMachine().getClient().getVideoRenderer().removeListener(this);			
		}
	}


	
    private Queue<List<KeyDelta>> queuedKeys = new LinkedList<List<KeyDelta>>();
	private long lastChangeTime;
	private ArrayList<KeyDelta> currentGroup = null;

	protected final IMachine machine;

	protected IKeyboardState keyboardState;
	private long nextResetTimeout;
	
	   /* Map of keys whose shifted/ctrled/fctned versions are being tracked */
//    private byte fakemap[] = new byte[256];
//    private byte shiftmap[] = new byte[256];
//    private byte ctrlmap[] = new byte[256];
//    private byte fctnmap[] = new byte[256];
//    private int cctrl, cfctn, cshift;
    
	public BaseKeyboardHandler(IKeyboardState keyboardState, IMachine machine) {
		this.keyboardState = keyboardState;
		this.machine = machine;
		
		machine.setKeyboardHandler(this);
		
		useNumPadForJoystick = Settings.get(machine, IKeyboardState.settingUseNumPadForJoystick);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IKeyboardHandler#setEventNotifier(v9t9.common.events.IEventNotifier)
	 */
	@Override
	public void setEventNotifier(IEventNotifier notifier) {
		this.eventNotifier = notifier;
	}

	public synchronized void applyKeyGroup() {
		long now = System.currentTimeMillis();
		if (queuedKeys.isEmpty() && currentGroup == null) {
			if (lastChangeTime + TIMEOUT < now) {
				keyboardState.incrClearKeyboard();
				
			} else
				return;
		} else {
			flushCurrentGroup();
				
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

	/**
	 * 
	 */
	public void flushCurrentGroup() {
		if (currentGroup != null) {
			queuedKeys.add(currentGroup);
			currentGroup = null;
		}
	}

	/**
	 * 
	 */
	public void resetKeyboard() {
		queuedKeys.clear();
		keyboardState.resetKeyboard();
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#resetProbe()
	 */
	@Override
	public synchronized void resetProbe() {
		long now = System.currentTimeMillis();
		if (now < nextResetTimeout)
			return;
		
		if (isPasting())
			pasteTask.run();
		else
			applyKeyGroup();
		
		nextResetTimeout = now + 5;
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
		donePaste();
		
		pasteListeners.fire(new ListenerList.IFire<IPasteListener>() {

			@Override
			public void fire(IPasteListener listener) {
				listener.pasteCanceled();
			}
		});

	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IKeyboardHandler#finishPaste()
	 */
	@Override
	public void finishPaste() {
		donePaste();
		
		pasteListeners.fire(new ListenerList.IFire<IPasteListener>() {

			@Override
			public void fire(IPasteListener listener) {
				listener.pasteCompleted();
			}
		});

		
	}
	
	protected void donePaste() {
		resetKeyboard();	// clear queued keys
		if (pasteTask != null)
			pasteTask.dispose();
		pasteTask = null;
		
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.engine.keyboard.IKeyboardState#pasteText(java.lang.String)
	 */
	@Override
	public void pasteText(String contents) {
		if (isPasting())
			cancelPaste();
		
		contents = contents.replaceAll("(\r\n|\r|\n)", "\r");
		contents = contents.replaceAll("\t", "    ");
		final char[] chs = contents.toCharArray();
		
		// this runnable is manually executed, not scheduled
		pasteTask = new PasteTask(this, chs);
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
	public boolean anyKeyAvailable() {
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

		if (Character.isLowerCase(ch)) {
			ch = Character.toUpperCase(ch);
		}
		
		if ((shift & MASK_SHIFT) != 0) {
			boolean unshiftIt = true;
			switch (ch) {
				case KEY_BACK_QUOTE:
					ch = KEY_TILDE; break;
				case KEY_MINUS:
					ch = KEY_UNDERSCORE; break;
				case KEY_EQUALS:
					ch = KEY_PLUS; break;
				case KEY_OPEN_BRACKET:
					ch = KEY_OPEN_BRACE; break;
				case KEY_CLOSE_BRACKET:
					ch = KEY_CLOSE_BRACE; break;
				case KEY_BACK_SLASH:
					ch = KEY_BAR; break;
				case KEY_SLASH:
					ch = KEY_QUESTION; break;
				case KEY_COMMA:
					ch = KEY_LESS; break;
				case KEY_PERIOD:
					ch = KEY_GREATER; break;
				case KEY_SINGLE_QUOTE:
					ch = KEY_QUOTE; break;
				case KEY_SEMICOLON:
					ch = KEY_COLON; break;
				default:
					unshiftIt = false;
			}
			if (unshiftIt) {
				pushShifts(pressed, (byte) (shift & ~MASK_SHIFT));
				pushKey(pressed, ch);
				return true;
			}
		}

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
			
		case KEY_QUIT:
			pushShifts(pressed, (byte) (shift | MASK_ALT));
			pushKey(pressed, KEY_EQUALS);
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
					boolean on;
//					try {
//						on = !Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_NUM_LOCK);
//					} catch (UnsupportedOperationException e) {
						on = (keyboardState.getLockMask() & MASK_NUM_LOCK) == 0;
//					}
					
					keyboardState.changeLocks(on, MASK_NUM_LOCK);
					if (keyboardState.isLock(MASK_SCROLL_LOCK))
						notifyNumpadInfo();
				}
				return true;
			case KEY_CAPS_LOCK:
				if (pressed) {
					boolean on;
//					try {
//						on = !Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
//					} catch (UnsupportedOperationException e) {
						on = (keyboardState.getLockMask() & MASK_CAPS_LOCK) == 0;
//					}
					keyboardState.changeLocks(on, MASK_CAPS_LOCK);
				}
				return true;
			case KEY_SCROLL_LOCK:
				if (pressed) {
					boolean on;
//					try {
//						on = !Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_NUM_LOCK);
//					} catch (UnsupportedOperationException e) {
						on = (keyboardState.getLockMask() & MASK_SCROLL_LOCK) == 0;
//					}
					keyboardState.changeLocks(on, MASK_SCROLL_LOCK);
					notifyNumpadInfo();
				}
				return true;
		}
		return false;
	}

	/**
	 * 
	 */
	private void notifyNumpadInfo() {
		if (eventNotifier != null && useNumPadForJoystick.getBoolean())
			eventNotifier.notifyEvent(null, Level.INFO, 
					(keyboardState.isLock(MASK_SCROLL_LOCK) ? 
							"Using numpad for joystick #1 (shift for #2)" : 
								(keyboardState.isLock(MASK_NUM_LOCK) ?
										"Using numpad for numbers" : "Using numpad for arrows")));
		
	}

	protected int convertKeypadToKey(int kpKey, byte shiftMask) {
		boolean isShifted = (shiftMask & MASK_SHIFT) != 0;
		boolean isNumLock = (keyboardState.getLockMask() & MASK_NUM_LOCK) != 0;
		boolean isScrollLock = (keyboardState.getLockMask() & MASK_SCROLL_LOCK) != 0;

		switch (kpKey) {
		case KEY_ARROW_UP:
			kpKey = KEY_KP_ARROW_UP; break;
		case KEY_ARROW_DOWN:
			kpKey = KEY_KP_ARROW_DOWN; break;
		case KEY_ARROW_LEFT:
			kpKey = KEY_KP_ARROW_LEFT; break;
		case KEY_ARROW_RIGHT:
			kpKey = KEY_KP_ARROW_RIGHT; break;
		case KEY_HOME:
			kpKey = KEY_KP_HOME; break;
		case KEY_END:
			kpKey = KEY_KP_END; break;
		case KEY_PAGE_UP:
			kpKey = KEY_KP_PAGE_UP; break;
		case KEY_PAGE_DOWN:
			kpKey = KEY_KP_PAGE_DOWN; break;
		case KEY_INSERT:
			kpKey = KEY_KP_INSERT; break;
		case KEY_DELETE:
			kpKey = KEY_KP_DELETE; break;
		}
		
		int key = kpKey;
		if (!isNumLock && isScrollLock && useNumPadForJoystick.getBoolean()) {
			int joy = isShifted ? 1 : 0;
			switch (kpKey) {
			case KEY_KP_ENTER:
			case KEY_KP_INSERT:
			case KEY_KP_0:
				key = KEY_JOYST_FIRE + joy; break;
			case KEY_KP_END:
			case KEY_KP_1:
				key = KEY_JOYST_DOWN_LEFT + joy; break;
			case KEY_KP_ARROW_DOWN:
			case KEY_KP_2:
				key = KEY_JOYST_DOWN + joy; break;
			case KEY_KP_PAGE_DOWN:
			case KEY_KP_3:
				key = KEY_JOYST_DOWN_RIGHT + joy; break;
			case KEY_KP_ARROW_LEFT:
			case KEY_KP_4:
				key = KEY_JOYST_LEFT + joy; break;
			case KEY_KP_SHIFT_5:
			case KEY_KP_5:
				key = KEY_JOYST_IDLE + joy; break;
			case KEY_KP_ARROW_RIGHT:
			case KEY_KP_6:
				key = KEY_JOYST_RIGHT + joy; break;
			case KEY_KP_HOME:
			case KEY_KP_7:
				key = KEY_JOYST_UP_LEFT + joy; break;
			case KEY_KP_ARROW_UP:
			case KEY_KP_8:
				key = KEY_JOYST_UP + joy; break;
			case KEY_KP_PAGE_UP:
			case KEY_KP_9:
				key = KEY_JOYST_UP_RIGHT + joy; break;
			}
		}
		else if (isNumLock == isShifted) {
			switch (kpKey) {
			case KEY_KP_0:
				key = KEY_KP_INSERT; break; 
			case KEY_KP_1:
				key = KEY_KP_END; break; 
			case KEY_KP_2:
				key = KEY_KP_ARROW_DOWN; break; 
			case KEY_KP_3:
				key = KEY_KP_PAGE_DOWN; break; 
			case KEY_KP_4:
				key = KEY_KP_ARROW_LEFT; break; 
			case KEY_KP_5:
				key = KEY_KP_SHIFT_5; break; 
			case KEY_KP_6:
				key = KEY_KP_ARROW_RIGHT; break; 
			case KEY_KP_7:
				key = KEY_KP_HOME; break; 
			case KEY_KP_8:
				key = KEY_KP_ARROW_UP; break; 
			case KEY_KP_9:
				key = KEY_KP_PAGE_UP; break;
			}
		} 
		else {
			switch (kpKey) {
			case KEY_KP_0:
			case KEY_KP_1:
			case KEY_KP_2:
			case KEY_KP_3:
			case KEY_KP_4:
			case KEY_KP_5:
			case KEY_KP_6:
			case KEY_KP_7:
			case KEY_KP_8:
			case KEY_KP_9:
				key = key + '0' - KEY_KP_0; break;
			}
		}
		return key;
	}

	protected void handleSpecialKey(boolean pressed, byte shiftMask, int ikey, boolean keyPad) {
		if (handleActionKey(pressed, ikey)) {
			return;
		}

		// convert keypad variants
		if (keyPad) {
			int prev = ikey;
			ikey = convertKeypadToKey(ikey, shiftMask);
			if (ikey != prev)
				shiftMask &= ~MASK_SHIFT;
		}

		if (shiftMask != 0)
			pushShifts(pressed, shiftMask);
		pushKey(pressed, ikey);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IKeyboardHandler#getMachine()
	 */
	@Override
	public IMachine getMachine() {
		return machine;
	}
	/* (non-Javadoc)
	 * @see v9t9.common.client.IKeyboardHandler#isAnyKeyPending()
	 */
	@Override
	public boolean isAnyKeyPending() {
		return !keyboardState.isBufferEmpty() || !queuedKeys.isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IKeyboardHandler#addPasteListener(v9t9.common.keyboard.IPasteListener)
	 */
	@Override
	public void addPasteListener(IPasteListener listener) {
		pasteListeners.add(listener);
	}
	/* (non-Javadoc)
	 * @see v9t9.common.client.IKeyboardHandler#removePasteListener(v9t9.common.keyboard.IPasteListener)
	 */
	@Override
	public void removePasteListener(IPasteListener listener) {
		pasteListeners.remove(listener);
	}

}
