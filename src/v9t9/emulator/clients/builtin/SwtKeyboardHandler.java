/**
 * 
 */
package v9t9.emulator.clients.builtin;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import v9t9.engine.KeyboardHandler;
import v9t9.keyboard.KeyboardState;

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
public class SwtKeyboardHandler implements KeyboardHandler {

	
	class KeyInfo {
		public KeyInfo(int keyCode, long timeout) {
			this.keyCode = keyCode;
			this.timeout = timeout;
		}
		int keyCode;
		long timeout;
	}

	private static final long KEY_LIFE = 1000 / 20;
	
	private LinkedList<KeyInfo> pressedKeys = new LinkedList<KeyInfo>();
	private int pressedStateMask;
	
	private final KeyboardState keyboardState;
	
	public SwtKeyboardHandler(Control control, KeyboardState keyboardState) {
		this.keyboardState = keyboardState;
		
		if (true) {
			Shell shell = control.getShell();
		 	shell.getDisplay().addFilter(SWT.KeyDown, new Listener() {
	
				public void handleEvent(Event event) {
					recordKey(true, event.stateMask, event.keyCode);
					
				}
				
			});
			shell.getDisplay().addFilter(SWT.KeyUp, new Listener() {
	
				public void handleEvent(Event event) {
					recordKey(false, event.stateMask, event.keyCode);
				}
				
			});
		} else {
			control.setFocus();
			control.addKeyListener(new KeyListener() {
	
				public void keyPressed(KeyEvent e) {
					recordKey(true, e.stateMask, e.keyCode);
				}
	
	
				public void keyReleased(KeyEvent e) {
					recordKey(false, e.stateMask, e.keyCode);
				}
				
			});
		}
	}

	/**
	 * Update the information about pressed keys
	 * @param pressed
	 * @param stateMask
	 * @param keyCode
	 */
	private void recordKey(boolean pressed, int stateMask, int keyCode) {
		long now = System.currentTimeMillis();
		boolean found = false;
		
		synchronized (pressedKeys) {
			for (Iterator<KeyInfo> iter = pressedKeys.iterator();
				iter.hasNext(); ) { 
				KeyInfo info = iter.next();
				if (info.keyCode == keyCode) {
					found = true;
					if (!pressed) {
						iter.remove();
					} else {
						info.timeout = now + KEY_LIFE; 
					}
				} else if (info.keyCode != keyCode && info.timeout < now) {
					iter.remove();
				}
			}
			
			if (!found && pressed) {
				pressedKeys.add(new KeyInfo(keyCode, now + KEY_LIFE));
			}
			
			// shift keys are reported in a keyup event
			if (!pressed && keyCode >= 0x10000)
				pressedStateMask &= ~stateMask;
			else
				pressedStateMask = stateMask;
		
		}
		
		// immediately record it
		updateKey(pressed, stateMask, keyCode);
	}
	
	private void updateKey(boolean pressed, int stateMask, int keyCode) {
		
		//System.out.println("keyCode="+keyCode+"; stateMask="+stateMask+"; pressed="+pressed);
		byte shift = 0;
		
		// separately pressed keys show up in keycode sometimes
		
		if (((stateMask | keyCode) & SWT.CTRL) != 0)
			shift |= KeyboardState.CTRL;
		if (((stateMask | keyCode) & SWT.SHIFT) != 0)
			shift |= KeyboardState.SHIFT;
		if (((stateMask | keyCode) & SWT.ALT) != 0)
			shift |= KeyboardState.FCTN;
		
		if ((keyCode & SWT.KEYCODE_BIT) == 0) {
			keyCode &= 0xff;
			if (Character.isLowerCase(keyCode))
				keyCode = Character.toUpperCase(keyCode);
		}
		
		//byte realshift = keyboardState.getRealShift();
		byte realshift = shift;
		
		if (keyCode < 128 && keyboardState.isAsciiDirectKey((char) keyCode)) {
			keyboardState.setKey(pressed, shift, (byte) keyCode);
		} else {
			if (keyCode == 0)
				keyCode = shift;
			
			int fctnShifted = shift | KeyboardState.FCTN;
			
			switch (keyCode) {

				// shifts
			case SWT.SHIFT:
			case 1:
				keyboardState.setKey(pressed, KeyboardState.SHIFT, 0);
				break;
			case SWT.CONTROL:
			case 4:
				keyboardState.setKey(pressed, KeyboardState.CTRL, 0);
				break;
			case SWT.ALT:
			case 2:
				keyboardState.setKey(pressed, KeyboardState.FCTN, 0);
				break;

			
			case 13:
				keyboardState.setKey(pressed, (byte)0, '\r');
				break;

			case SWT.CAPS_LOCK:
				if (!pressed) {
					keyboardState.setAlpha(!keyboardState.getAlpha());
				}
				break;
			case SWT.BREAK:
				if (pressed)
					System.exit(0);
				break;
				// faked keys
			case '`':
				if (0 == (realshift & KeyboardState.SHIFT) && !keyboardState.isSet(KeyboardState.FCTN, 'W'))
					keyboardState.setKey(pressed, fctnShifted, 'C');	/* ` */
				else
					keyboardState.setKey(pressed, fctnShifted, 'W');	/* ~ */
				break;
			case '-':
				if (0 == (realshift & KeyboardState.SHIFT) && !keyboardState.isSet(KeyboardState.FCTN, 'U'))
					keyboardState.setKey(pressed, KeyboardState.SHIFT, '/');	/* - */
				else
					keyboardState.setKey(pressed, fctnShifted, 'U');	/* _ */
				break;
			case '[':
				if (0 == (realshift & KeyboardState.SHIFT) && !keyboardState.isSet(KeyboardState.FCTN, 'F'))
					keyboardState.setKey(pressed, fctnShifted, 'R');	/* [ */
				else
					keyboardState.setKey(pressed, fctnShifted, 'F');	/* { */
				break;
			case ']':
				if (0 == (realshift & KeyboardState.SHIFT) && !keyboardState.isSet(KeyboardState.FCTN, 'G'))
					keyboardState.setKey(pressed, fctnShifted, 'T');	/* ] */
				else
					keyboardState.setKey(pressed, fctnShifted, 'G');	/* } */
				break;
			case '\'':
				if (0 == (realshift & KeyboardState.SHIFT) && !keyboardState.isSet(KeyboardState.FCTN, 'P'))
					keyboardState.setKey(pressed, fctnShifted, 'O');	/* ' */
				else
					keyboardState.setKey(pressed, fctnShifted, 'P');	/* " */
				break;
			case '/':
				if (0 == (realshift & KeyboardState.SHIFT) && !keyboardState.isSet(KeyboardState.FCTN, 'I'))
					keyboardState.setKey(pressed, (byte)0, '/');	/* / */
				else
					keyboardState.setKey(pressed, fctnShifted, 'I');	/* ? */
				break;
			case '\\':
				if (0 == (realshift & KeyboardState.SHIFT) && !keyboardState.isSet(KeyboardState.FCTN, 'A'))
					keyboardState.setKey(pressed, fctnShifted, 'Z');	/* \\ */
				else
					keyboardState.setKey(pressed, fctnShifted, 'A');	/* | */
				break;
				
			case 8:
				keyboardState.setKey(pressed, shift | KeyboardState.CTRL, 'H');	/* BKSP */
				break;
			case SWT.TAB:
				keyboardState.setKey(pressed, shift | KeyboardState.CTRL, 'I');	/* TAB */
				break;
				
			case SWT.F1:
			case SWT.F2:
			case SWT.F3:
			case SWT.F4:
			case SWT.F5:
			case SWT.F6:
			case SWT.F7:
			case SWT.F8:
			case SWT.F9:
				keyboardState.setKey(pressed, fctnShifted, '1' + SWT.F1 - keyCode);	
				break;
				
			case SWT.ARROW_UP:
				keyboardState.setKey(pressed, fctnShifted, 'E');
				break;
			case SWT.ARROW_DOWN:
				keyboardState.setKey(pressed, fctnShifted, 'X');
				break;
			case SWT.ARROW_LEFT:
				keyboardState.setKey(pressed, fctnShifted, 'S');
				break;
			case SWT.ARROW_RIGHT:
				keyboardState.setKey(pressed, fctnShifted, 'D');
				break;
				
			case SWT.DEL:
				keyboardState.setKey(pressed, fctnShifted, '1');	
				break;
			case SWT.INSERT:
				keyboardState.setKey(pressed, fctnShifted, '2');	
				break;
				
			case SWT.PAGE_UP:
				keyboardState.setKey(pressed, fctnShifted, '6'); // (as per E/A and TI Writer)
				break;
			case SWT.PAGE_DOWN:
				keyboardState.setKey(pressed, fctnShifted, '4'); // (as per E/A and TI Writer)
				break;

			case SWT.HOME:
				keyboardState.setKey(pressed, fctnShifted, '5');		// BEGIN
				break;
			case SWT.END:
				keyboardState.setKey(pressed, fctnShifted, '0');		// Fctn-0
				break;

			default:
				System.out.println(keyCode);
				
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.handlers.KeyboardHandler#scan(v9t9.keyboard.KeyboardState)
	 */
	public void scan(KeyboardState state) {
		state.resetKeyboard();
	
		synchronized (pressedKeys) {
			for (KeyInfo info : pressedKeys) {
				updateKey(true, pressedStateMask, info.keyCode);
			}
		}
	}
}
