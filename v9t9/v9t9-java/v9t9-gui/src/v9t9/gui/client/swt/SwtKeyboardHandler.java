/**
 * 
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
import v9t9.common.keyboard.BaseKeyboardHandler;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.common.machine.IMachine;
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

	public SwtKeyboardHandler(IKeyboardState keyboardState, IMachine machine) {
		super(keyboardState, machine);
		
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
		updateKey(pressed, stateMask, keyCode, keyPad);
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
	private void updateKey(boolean pressed, int stateMask, int keyCode, boolean keyPad) {
		
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
			if (Character.isLowerCase(keyCode)) {
				keyCode = Character.toUpperCase(keyCode);
			}
			
			if ((shiftMask & MASK_SHIFT) != 0) {
				boolean unshiftIt = true;
				switch (keyCode) {
					case KEY_BACK_QUOTE:
						keyCode = KEY_TILDE; break;
					case KEY_MINUS:
						keyCode = KEY_UNDERSCORE; break;
					case KEY_EQUALS:
						keyCode = KEY_PLUS; break;
					case KEY_OPEN_BRACKET:
						keyCode = KEY_OPEN_BRACE; break;
					case KEY_CLOSE_BRACKET:
						keyCode = KEY_CLOSE_BRACE; break;
					case KEY_BACK_SLASH:
						keyCode = KEY_BAR; break;
					case KEY_SLASH:
						keyCode = KEY_QUESTION; break;
					case KEY_COMMA:
						keyCode = KEY_LESS; break;
					case KEY_PERIOD:
						keyCode = KEY_GREATER; break;
					case KEY_SINGLE_QUOTE:
						keyCode = KEY_QUOTE; break;
					case KEY_SEMICOLON:
						keyCode = KEY_COLON; break;
					default:
						unshiftIt = false;
				}
				if (unshiftIt) {
					pushShifts(pressed, (byte) (shiftMask & ~MASK_SHIFT));
					pushKey(pressed, keyCode);
					return;
				}
			}
			
			if (/*!keyPad &&*/ postCharacter(pressed, shiftMask, (char) keyCode)) {
				return;
			}
		}

		
		
		if (keyCode == 0) {
			pushShifts(pressed, shiftMask);
			return;
		}
		
		Integer ikey = swtKeycodeToKey.get(keyCode);
		if (ikey != null) {
			
			if (handleActionKey(pressed, ikey)) {
				return;
			}

			// convert keypad variants
			if (keyPad) {
				switch (ikey) {
				case KEY_ARROW_UP:
					ikey = KEY_KP_ARROW_UP; break;
				case KEY_ARROW_DOWN:
					ikey = KEY_KP_ARROW_DOWN; break;
				case KEY_ARROW_LEFT:
					ikey = KEY_KP_ARROW_LEFT; break;
				case KEY_ARROW_RIGHT:
					ikey = KEY_KP_ARROW_RIGHT; break;
				case KEY_HOME:
					ikey = KEY_KP_HOME; break;
				case KEY_END:
					ikey = KEY_KP_END; break;
				case KEY_PAGE_UP:
					ikey = KEY_KP_PAGE_UP; break;
				case KEY_PAGE_DOWN:
					ikey = KEY_KP_PAGE_DOWN; break;
				case KEY_INSERT:
					ikey = KEY_KP_INSERT; break;
				case KEY_DELETE:
					ikey = KEY_KP_DELETE; break;
				}
				ikey = convertKeypadToKey(ikey, shiftMask);
				ikey = convertKeypadToKey(ikey, shiftMask);
			}

			pushKey(pressed, ikey);
			return;
		}
		
		System.err.println("*** unhandled SWT keyCode: " + keyCode);
	}
	
	private int lastKeyPressedCode = -1;
	
	public void init(IVideoRenderer renderer) {
		final Control control = ((ISwtVideoRenderer) renderer).getControl();
		Shell shell = control.getShell();
		
	 	shell.getDisplay().addFilter(SWT.KeyDown, new Listener() {


			public void handleEvent(Event event) {
				if (!control.isFocusControl())
					return;
				
				if (isPasting() && event.keyCode == SWT.ESC) {
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
