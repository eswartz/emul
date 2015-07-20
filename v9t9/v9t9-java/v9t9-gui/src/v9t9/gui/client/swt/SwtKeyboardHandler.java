/*
  SwtKeyboardHandler.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt;

import static v9t9.common.keyboard.KeyboardConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.client.IVideoRenderer;
import v9t9.common.cpu.ICpu;
import v9t9.common.keyboard.BaseKeyboardHandler;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.common.keyboard.KeyboardConstants;
import v9t9.common.machine.IMachine;
import v9t9.gui.common.BaseEmulatorWindow;
import ejs.base.utils.HexUtils;

/**
 * SWT keyboard control. 
 * 
 * We establish a display-level filter because SWT doesn't route keyboard events
 * predictably to the widgets you'd expect.
 * <p>
 * Sadly, due to criminally bad support for keyup events in Win32, we need to
 * generically assume they won't come in, using a linked list of "known pressed"
 * keys along with timeouts for each so we know when to assume a key is dead.
 * (Luckily, key repeats come in for most keys, except shift type keys.)
 * <p>
 * This can happen when, for instance, you hold down E, then press X, then
 * release E -- you'll only see keydowns for E and X. 
 * <p>
 * 
 * @author ejs
 * 
 */
public class SwtKeyboardHandler extends BaseKeyboardHandler {
	
	private Timer pasteTimer;
	private long baseTime;

	public SwtKeyboardHandler(IKeyboardState keyboardState, IMachine machine) {
		super(keyboardState, machine);
		baseTime = System.currentTimeMillis() & 0xFFFFFFFF00000000L;
	}

	/**
	 * Update the information about pressed keys
	 * @param pressed
	 * @param stateMask
	 * @param keyCode
	 */
	private void recordKey(Event keyEvent) {
		//long now = System.currentTimeMillis();
		boolean pressed = keyEvent.type == SWT.KeyDown;
		int stateMask = keyEvent.stateMask;
		int keyCode = keyEvent.keyCode;
		boolean keyPad = keyEvent.keyLocation == SWT.KEYPAD;
		
		if (keyCode == SWT.ESC) {
			if (pasteTimer != null && pressed) {
				cancelPaste();
				return;
			}
			else {
				keyboardState.resetKeyboard();
				keyboardState.resetJoystick();
			}
		}
		
		// immediately record it
		updateKey(baseTime + (keyEvent.time & 0xFFFFFFFFL), pressed, stateMask, keyCode, keyPad);
	}
	
	private static final Map<Integer, Integer> swtKeycodeToKey = new HashMap<Integer, Integer>(); 
	private static final int[] keycodesAndKeys = {
		SWT.SHIFT, KEY_SHIFT,
		SWT.CONTROL, KEY_CONTROL,
		SWT.ALT, KEY_ALT,
		SWT.CAPS_LOCK, KEY_CAPS_LOCK,
		SWT.NUM_LOCK, KEY_NUM_LOCK,
		SWT.SCROLL_LOCK, KEY_SCROLL_LOCK,
		SWT.BREAK, KEY_BREAK,
		SWT.PAUSE, KEY_PAUSE,
		SWT.ESC, KEY_ESCAPE,
		SWT.F1, KEY_F1,
		SWT.F2, KEY_F2,
		SWT.F3, KEY_F3,
		SWT.F4, KEY_F4,
		SWT.F5, KEY_F5,
		SWT.F6, KEY_F6,
		SWT.F7, KEY_F7,
		SWT.F8, KEY_F8,
		SWT.F9, KEY_F9,
		SWT.F10, KEY_F10,
		SWT.F11, KEY_F11,
		SWT.F12, KEY_F12,
		SWT.ARROW_DOWN, KEY_ARROW_DOWN,
		SWT.ARROW_UP, KEY_ARROW_UP,
		SWT.ARROW_LEFT, KEY_ARROW_LEFT,
		SWT.ARROW_RIGHT, KEY_ARROW_RIGHT,
		SWT.PAGE_DOWN, KEY_PAGE_DOWN,
		SWT.PAGE_UP, KEY_PAGE_UP,
		SWT.HOME, KEY_HOME,
		SWT.END, KEY_END,
		SWT.INSERT, KEY_INSERT,
		SWT.DEL, KEY_DELETE,
		SWT.PRINT_SCREEN, KEY_PRINT_SCREEN,
		SWT.KEYPAD_0, KEY_KP_0,
		SWT.KEYPAD_1, KEY_KP_1,
		SWT.KEYPAD_2, KEY_KP_2,
		SWT.KEYPAD_3, KEY_KP_3,
		SWT.KEYPAD_4, KEY_KP_4,
		SWT.KEYPAD_5, KEY_KP_5,
		SWT.KEYPAD_6, KEY_KP_6,
		SWT.KEYPAD_7, KEY_KP_7,
		SWT.KEYPAD_8, KEY_KP_8,
		SWT.KEYPAD_9, KEY_KP_9,
		SWT.KEYPAD_ADD, KEY_KP_PLUS,
		SWT.KEYPAD_MULTIPLY, KEY_KP_ASTERISK,
		SWT.KEYPAD_CR, KEY_KP_ENTER,
		SWT.KEYPAD_DECIMAL, KEY_KP_POINT,
		SWT.KEYPAD_SUBTRACT, KEY_KP_MINUS,
		SWT.KEYPAD_DIVIDE, KEY_KP_SLASH,
		SWT.TAB, KEY_TAB,
		SWT.BS, KEY_BACKSPACE,
	};
	static {
		for (int i = 0; i < keycodesAndKeys.length; i += 2) {
			swtKeycodeToKey.put(keycodesAndKeys[i], keycodesAndKeys[i+1]);
		}
	}
	private void updateKey(long time, boolean pressed, int stateMask, int keyCode, boolean keyPad) {
		
		//System.out.println("keyCode="+keyCode+"; stateMask="+stateMask+"; pressed="+pressed);
		byte shiftMask = 0;
		
		// separately pressed keys show up in keycode sometimes
		
		if (((stateMask | keyCode) & SWT.CTRL) != 0)
			shiftMask |= MASK_CONTROL;
		if (((stateMask | keyCode) & SWT.SHIFT) != 0)
			shiftMask |= MASK_SHIFT;
		if (((stateMask | keyCode) & SWT.ALT) != 0)
			shiftMask |= MASK_ALT;
		
		if ((keyCode & SWT.KEYCODE_BIT) == 0) {
			keyCode &= 0xff;
		
			if (postCharacter(time, pressed, shiftMask, (char) keyCode)) {
				return;
			}
		}

		
		
		if (keyCode == 0) {
			pushShifts(time, pressed, shiftMask);
			return;
		}
		
		Integer ikey = swtKeycodeToKey.get(keyCode);
		if (ikey != null) {
			handleSpecialKey(time, pressed, shiftMask, ikey, keyPad);
		}
		else {
			System.err.println("*** unhandled SWT keyCode: " + keyCode);
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.keyboard.BaseKeyboardHandler#handleActionKey(boolean, int)
	 */
	@Override
	protected boolean handleActionKey(long time, boolean pressed, int key) {
		if (key == KEY_PRINT_SCREEN) {
			if (pressed) {
				byte shiftMask = machine.getKeyboardState().getShiftMask();
				if (shiftMask == 0) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							window.screenshot();
						}
					});
				} else if ((shiftMask & KeyboardConstants.MASK_ALT + KeyboardConstants.MASK_CONTROL) != 0) {
					machine.getSettings().get(ICpu.settingDebugging).setBoolean(true);
				}
			}
			return true;
		}
		return super.handleActionKey(time, pressed, key);
	}
	
	private int lastKeyPressedCode = -1;
	protected BaseEmulatorWindow window;
	
	public void init(IVideoRenderer renderer) {
		final Control control = ((ISwtVideoRenderer) renderer).getControl();
		Shell shell = control.getShell();
		
	 	shell.getDisplay().addFilter(SWT.KeyDown, new Listener() {


			public void handleEvent(Event event) {
				if (!control.isFocusControl())
					return;
				
				if (event.keyCode == SWT.ESC) {
					cancelPaste();
					event.doit = false;
					return;
				}

		        // System.out.println("keyPressed(" + SwtKey.findByCode(event.keyCode) + ")");
		        if (event.keyCode == lastKeyPressedCode) {
		            // ignore if this is a repeat event
		        	//return;
		        }

		        if (lastKeyPressedCode != -1 && event.keyCode != lastKeyPressedCode) {
		            // if this is a different key to the last key that was pressed, then
		            // add an 'up' even for the previous one - SWT doesn't send an 'up' event for the
		            // first key in the below scenario:
		            // 1. key 1 down
		            // 2. key 2 down
		            // 3. key 1 up
		        	//recordKey(false, event.stateMask, lastKeyPressedCode);
		        }

		        lastKeyPressedCode = event.keyCode;

				recordKey(event);
				event.doit = false;
				
			}
			
		});
		shell.getDisplay().addFilter(SWT.KeyUp, new Listener() {

			public void handleEvent(Event event) {
				recordKey(event);
				event.doit = false;
				lastKeyPressedCode = -1;
			}
			
		});

		window = ((ISwtVideoRenderer) renderer).getWindow();
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				System.out.println("KEY PRESSED " + HexUtils.toHex4(e.keyCode) + " / " + HexUtils.toHex4(e.stateMask));
			}

			public void keyReleased(KeyEvent e) {
				System.out.println("KEY RELEASE " + HexUtils.toHex4(e.keyCode) + " / " + HexUtils.toHex4(e.stateMask));
			}
			
		});
		display.addFilter(SWT.KeyUp, new Listener() {

			public void handleEvent(Event e) {
				System.out.println("RELEASE " + HexUtils.toHex4(e.keyCode) + " / " + HexUtils.toHex4(e.stateMask));
				e.doit = false;
			}
			
		});
		display.addFilter(SWT.KeyDown, new Listener() {

			public void handleEvent(Event e) {
				System.out.println("PRESSED " + HexUtils.toHex4(e.keyCode) + " / " + HexUtils.toHex4(e.stateMask));
				e.doit = false;
			}
			
		});
		shell.open();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();

	}

}
