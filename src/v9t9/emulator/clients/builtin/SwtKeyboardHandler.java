/**
 * 
 */
package v9t9.emulator.clients.builtin;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import v9t9.emulator.Machine;
import v9t9.keyboard.KeyboardState;
import v9t9.utils.Utils;

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
	
	private Timer pasteTimer;

	public SwtKeyboardHandler(Control control, KeyboardState keyboardState, Machine machine) {
		super(keyboardState, machine);
		
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
		
		if (pasteTimer != null && pressed && keyCode == SWT.ESC) {
			cancelPaste();
			return;
		}
		
		//System.out.println("recordKey: pressed="+pressed+"; statemask="+Integer.toHexString(stateMask)
		//		+"; keyCode="+keyCode);

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
			
			// shift keys are reported sometimes in keycode and sometimes in stateMask
			if (keyCode >= 0x10000 && (keyCode & SWT.KEYCODE_BIT) == 0)
				if (pressed)
					pressedStateMask |= keyCode;
				else
					pressedStateMask &= ~keyCode;
			else
				pressedStateMask = stateMask;
		
		}
		
		// immediately record it
		synchronized (keyboardState) {
			//updateKey(pressed, stateMask, keyCode);
		}
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
		//byte realshift = shift;
		
		if (keyCode > 128 || !keyboardState.postCharacter(pressed, false, shift, (char) keyCode)) {
			if (keyCode == 0)
				keyCode = shift;
			
			int fctnShifted = shift | KeyboardState.FCTN;
			
			//System.out.println("Handling non-postable key: " + keyCode + "; shift="+shift);
			switch (keyCode) {

				// shifts
			case SWT.SHIFT:
			case KeyboardState.SHIFT:
				keyboardState.setKey(pressed, false, KeyboardState.SHIFT, 0);
				break;
			case SWT.CONTROL:
			case KeyboardState.CTRL:
				keyboardState.setKey(pressed, false, KeyboardState.CTRL, 0);
				break;
			case SWT.ALT:
			case KeyboardState.FCTN:
				keyboardState.setKey(pressed, false, KeyboardState.FCTN, 0);
				break;

			case SWT.CAPS_LOCK:
				if (pressed) {
					keyboardState.setAlpha(!keyboardState.getAlpha());
				}
				break;
			case SWT.BREAK:
				if (pressed)
					System.exit(0);
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
				keyboardState.setKey(pressed, false, fctnShifted, '1' + SWT.F1 - keyCode);	
				break;
				
			case SWT.ARROW_UP:
				keyboardState.setKey(pressed, false, fctnShifted, 'E');
				break;
			case SWT.ARROW_DOWN:
				keyboardState.setKey(pressed, false, fctnShifted, 'X');
				break;
			case SWT.ARROW_LEFT:
				keyboardState.setKey(pressed, false, fctnShifted, 'S');
				break;
			case SWT.ARROW_RIGHT:
				keyboardState.setKey(pressed, false, fctnShifted, 'D');
				break;
				
				
			//case SWT.DEL:
			//	keyboardState.setKey(pressed, fctnShifted, '1');	
			//	break;
			case SWT.INSERT:
				keyboardState.setKey(pressed, false, fctnShifted, '2');	
				break;
				
			case SWT.PAGE_UP:
				keyboardState.setKey(pressed, false, fctnShifted, '6'); // (as per E/A and TI Writer)
				break;
			case SWT.PAGE_DOWN:
				keyboardState.setKey(pressed, false, fctnShifted, '4'); // (as per E/A and TI Writer)
				break;

			case SWT.HOME:
				keyboardState.setKey(pressed, false, fctnShifted, '5');		// BEGIN
				break;
			case SWT.END:
				keyboardState.setKey(pressed, false, fctnShifted, '0');		// Fctn-0
				break;

			default:
				if (keyCode != shift) {
					System.out.println(keyCode);
				}
				
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.handlers.KeyboardHandler#scan(v9t9.keyboard.KeyboardState)
	 */
	public void scan(KeyboardState state) {
		if (pasteTimer != null)
			return;
		
		synchronized(state) {
			state.resetKeyboard();
		
			synchronized (pressedKeys) {
				updateKey(true, pressedStateMask, 0);
				for (KeyInfo info : pressedKeys) {
					updateKey(true, pressedStateMask, info.keyCode);
				}
			}
		}
	}
	
	
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		/*
		shell.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				System.out.println("PRESSED " + Utils.toHex4(e.keyCode) + " / " + Utils.toHex4(e.stateMask));
			}

			public void keyReleased(KeyEvent e) {
				System.out.println("RELEASE " + Utils.toHex4(e.keyCode) + " / " + Utils.toHex4(e.stateMask));
			}
			
		});
		*/
		display.addFilter(SWT.KeyUp, new Listener() {

			public void handleEvent(Event e) {
				System.out.println("RELEASE " + Utils.toHex4(e.keyCode) + " / " + Utils.toHex4(e.stateMask));
				e.doit = false;
			}
			
		});
		display.addFilter(SWT.KeyDown, new Listener() {

			public void handleEvent(Event e) {
				System.out.println("PRESSED " + Utils.toHex4(e.keyCode) + " / " + Utils.toHex4(e.stateMask));
				e.doit = false;
			}
			
		});
		shell.open();
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();

	}

	public void init(Control control) {
		if (true) {
			Shell shell = control.getShell();
		 	shell.getDisplay().addFilter(SWT.KeyDown, new Listener() {
	
				public void handleEvent(Event event) {
					recordKey(true, event.stateMask, event.keyCode);
					event.doit = false;
					
				}
				
			});
			shell.getDisplay().addFilter(SWT.KeyUp, new Listener() {
	
				public void handleEvent(Event event) {
					recordKey(false, event.stateMask, event.keyCode);
					event.doit = false;
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
}
