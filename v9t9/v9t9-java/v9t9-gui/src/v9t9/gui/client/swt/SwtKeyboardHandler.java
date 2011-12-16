/**
 * 
 */
package v9t9.gui.client.swt;

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

import ejs.base.utils.HexUtils;

import v9t9.common.keyboard.IKeyboardState;
import v9t9.common.machine.IMachine;
import v9t9.gui.common.BaseKeyboardHandler;

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
public class SwtKeyboardHandler extends BaseKeyboardHandler implements ISwtKeyboardHandler {

	
	static class KeyInfo {
		public KeyInfo(int keyCode, boolean keyPad, long when) {
			this.keyCode = keyCode;
			this.keyPad = keyPad;
			this.when = when;
		}
		int keyCode;
		long when;
		boolean keyPad;
	}

	private LinkedList<KeyInfo> pressedKeys = new LinkedList<KeyInfo>();
	private int pressedStateMask;
	
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
	private void recordKey(Event keyEvent, long now) {
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
		
		//System.out.println("recordKey: pressed="+pressed+"; statemask="+Integer.toHexString(stateMask)
		//		+"; keyCode="+keyCode);

		synchronized (pressedKeys) {
			for (Iterator<KeyInfo> iter = pressedKeys.iterator();
				iter.hasNext(); ) { 
				KeyInfo info = iter.next();
				if (info.keyCode == keyCode) {
					if (!pressed) {
						iter.remove();
					}
				}
			}
			
			if (pressed) {
				pressedKeys.add(new KeyInfo(keyCode, keyPad, now));
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
			updateKey(pressed, stateMask, keyCode, keyPad, now);
		}
	}
	
	private void updateKey(boolean pressed, int stateMask, int keyCode, boolean keyPad, long when) {
		
		//System.out.println("keyCode="+keyCode+"; stateMask="+stateMask+"; pressed="+pressed);
		byte shift = 0;
		
		int realKey = keyCode;
		
		// separately pressed keys show up in keycode sometimes
		
		if (((stateMask | keyCode) & SWT.CTRL) != 0)
			shift |= IKeyboardState.CTRL;
		if (((stateMask | keyCode) & SWT.SHIFT) != 0)
			shift |= IKeyboardState.SHIFT;
		if (((stateMask | keyCode) & SWT.ALT) != 0)
			shift |= IKeyboardState.FCTN;
		
		if ((keyCode & SWT.KEYCODE_BIT) == 0) {
			keyCode &= 0xff;
			if (Character.isLowerCase(keyCode))
				keyCode = Character.toUpperCase(keyCode);
		}
		
		//byte realshift = keyboardState.getRealShift();
		//byte realshift = shift;
		
		int joy = (shift & IKeyboardState.SHIFT) != 0 ? 2 : 1;
		
		if (keyCode > 128 || keyPad || !keyboardState.postCharacter(machine, realKey, pressed, false, shift, (char) keyCode, when)) {
			if (keyCode == 0)
				keyCode = shift;
			
			int fctnShifted = shift | IKeyboardState.FCTN;
			int nonShifted = shift & ~IKeyboardState.SHIFT;
			
			//System.out.println("Handling non-postable key: " + keyCode + "; shift="+shift);
			switch (keyCode) {

				// shifts
			case SWT.SHIFT:
			case IKeyboardState.SHIFT:
				keyboardState.setKey(realKey, pressed, false, IKeyboardState.SHIFT, 0, when);
				break;
			case SWT.CONTROL:
			case IKeyboardState.CTRL:
				keyboardState.setKey(realKey, pressed, false, IKeyboardState.CTRL, 0, when);
				break;
			case SWT.ALT:
			case IKeyboardState.FCTN:
				keyboardState.setKey(realKey, pressed, false, IKeyboardState.FCTN, 0, when);
				break;

			case SWT.CAPS_LOCK:
				if (pressed) {
					keyboardState.setAlpha(!keyboardState.getAlpha());
				}
				break;
			case SWT.BREAK:
				if (pressed)
					machine.getClient().close();
				break;
				
			case SWT.NUM_LOCK:
				if (pressed) {
					keyboardState.setNumLock(!keyboardState.getNumLock());
				}
				break;
				
			case SWT.ESC:
				keyboardState.setKey(realKey, pressed, false, IKeyboardState.FCTN, '9', when);
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
				keyboardState.setKey(realKey, pressed, false, fctnShifted, '1' + SWT.F1 - keyCode, when);	
				break;
				
			case '5':
			case SWT.KEYPAD_5:
				setJoystickOrKey(realKey, pressed, keyPad,
						nonShifted, '5', 
						nonShifted, '5', 
						joy, IKeyboardState.JOY_X | IKeyboardState.JOY_Y, 0, 0, 
						when);
				break;
			case SWT.ARROW_UP:
			case SWT.KEYPAD_8:
				setJoystickOrKey(realKey, pressed, keyPad,
						fctnShifted, 'E', 
						nonShifted, '8', 
						joy, IKeyboardState.JOY_Y, 0, pressed ? -1 : 0, 
						when);
				break;
			case SWT.ARROW_DOWN:
			case SWT.KEYPAD_2:
				setJoystickOrKey(realKey, pressed, keyPad,
						fctnShifted, 'X', 
						nonShifted, '2', 
						joy, IKeyboardState.JOY_Y, 0, pressed ? 1 : 0, 
						when);
				break;
			case SWT.ARROW_LEFT:
			case SWT.KEYPAD_4:
				setJoystickOrKey(realKey, pressed, keyPad,
						fctnShifted, 'S', 
						nonShifted, '4', 
						joy, IKeyboardState.JOY_X, pressed ? -1 : 0, 0, 
						when);
				break;
			case SWT.ARROW_RIGHT:
			case SWT.KEYPAD_6:
				setJoystickOrKey(realKey, pressed, keyPad,
						fctnShifted, 'D',
						nonShifted, '6',
						joy, IKeyboardState.JOY_X, pressed ? 1 : 0, 0, 
						when);
				break;
				
			case SWT.PAGE_UP:
			case SWT.KEYPAD_9:
				setJoystickOrKey(realKey, pressed, keyPad,
						fctnShifted, '6',	// FCTN-6  // (as per E/A and TI Writer)
						nonShifted, '9', 
						joy, IKeyboardState.JOY_X | IKeyboardState.JOY_Y, 
						pressed ? 1 : 0, pressed ? -1 : 0, 
								when);
				break;
			case SWT.PAGE_DOWN:
			case SWT.KEYPAD_3:
				setJoystickOrKey(realKey, pressed, keyPad,
						fctnShifted, '4',	// FCTN-6  // (as per E/A and TI Writer)
						nonShifted, '3',
						joy, IKeyboardState.JOY_X | IKeyboardState.JOY_Y, 
						pressed ? 1 : 0, pressed ? 1 : 0, 
								when);
				break;

			case SWT.HOME:
			case SWT.KEYPAD_7:
				setJoystickOrKey(realKey, pressed, keyPad,
						fctnShifted, '5',	// BEGIN
						nonShifted, '7', 
						joy, IKeyboardState.JOY_X | IKeyboardState.JOY_Y, 
						pressed ? -1 : 0, pressed ? -1 : 0, 
						when);
				break;
				
			case SWT.END:
			case SWT.KEYPAD_1:
				setJoystickOrKey(realKey, pressed, keyPad,
						fctnShifted, '0',	// FCTN-0
						nonShifted, '1',
						joy, IKeyboardState.JOY_X | IKeyboardState.JOY_Y, 
						pressed ? -1 : 0, pressed ? 1 : 0, 
								when);
				break;
				

			case SWT.INSERT:
			case SWT.KEYPAD_0:
				setJoystickOrKey(realKey, pressed, keyPad,
						fctnShifted, '2',	// INS
						nonShifted, '0',
						joy, IKeyboardState.JOY_B, 0, 0, 
						when);
				break;
				
			case SWT.KEYPAD_DIVIDE:
				setJoystickOrKey(realKey, pressed, keyPad,
						nonShifted, '/', 
						nonShifted, '/', 
						joy, IKeyboardState.JOY_B, 0, 0, 
						when);
				break;
			case SWT.KEYPAD_MULTIPLY:
				setJoystickOrKey(realKey, pressed, keyPad,
						shift | IKeyboardState.SHIFT, '8', 
						shift | IKeyboardState.SHIFT, '8', 
						joy, IKeyboardState.JOY_B, 0, 0, 
						when);
				break;
			case SWT.KEYPAD_ADD:
				setJoystickOrKey(realKey, pressed, keyPad,
						shift | IKeyboardState.SHIFT, '=', 
						shift | IKeyboardState.SHIFT, '=', 
						joy, IKeyboardState.JOY_B, 0, 0, 
						when);
				break;
			case SWT.KEYPAD_SUBTRACT:
				setJoystickOrKey(realKey, pressed, keyPad,
						shift | IKeyboardState.SHIFT, '/',
						shift | IKeyboardState.SHIFT, '/',
						joy, IKeyboardState.JOY_B, 0, 0, 
						when);
				break;
			case SWT.KEYPAD_CR:
				setJoystickOrKey(realKey, pressed, keyPad,
						shift, '\r', 
						shift, '\r', 
						joy, IKeyboardState.JOY_B, 0, 0, 
						when);
				break;
			case SWT.KEYPAD_DECIMAL:
				keyboardState.setKey(realKey, pressed, false, nonShifted, '.', when);
				break;
			case SWT.DEL:
				keyboardState.setKey(realKey, pressed, false, fctnShifted, '1', when);
				break;

			default:
				if (keyCode != shift) {
					System.out.println(keyCode);
				}
				
			}
		}
	}
	
	/**
	 * @param realKey
	 * @param pressed
	 * @param shift
	 * @param joy
	 * @param when
	 * @param c
	 * @param joyY
	 * @param i
	 * @param j
	 */
	private void setJoystickOrKey(int realKey, boolean pressed, boolean keyPad,
			int shift, char ch, 
			int keypadShift, char keypadCh, 
			int joy, int joyRow, int x, int y, 
			long when) {
		if (!keyPad)
			keyboardState.setKey(realKey, pressed, false, shift, ch, when);
		else if (((keyboardState.getShiftMask() & IKeyboardState.SHIFT) != 0) == !isNumLock())
			keyboardState.setJoystick(joy,
					joyRow, x, y, (joyRow & IKeyboardState.JOY_B) != 0 && pressed, when);
		else
			keyboardState.setKey(realKey, pressed, false, keypadShift, keypadCh, when);
		
	}

	private boolean isNumLock() {
		boolean on;
		// hmm, seems either unavailable or plain wrong
		//try {
		//	on = Toolkit.getDefaultToolkit().getLockingKeyState(java.awt.event.KeyEvent.VK_NUM_LOCK);
		//} catch (UnsupportedOperationException e) {
			on = keyboardState.getNumLock();
		//}
		return on;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.handlers.KeyboardHandler#scan(v9t9.keyboard.KeyboardState)
	 */
	public void scan(IKeyboardState state) {
		if (pasteTimer != null)
			return;

		if (true) return;
		
		synchronized(state) {
			state.resetKeyboard();
		
			synchronized (pressedKeys) {
				boolean first = true;
				for (KeyInfo info : pressedKeys) {
					if (first) {
						updateKey(true, pressedStateMask, 0, info.keyPad, info.when);
						first = false;
					}
					updateKey(true, pressedStateMask, info.keyCode, info.keyPad, info.when);
				}
				if (first) {
					updateKey(true, pressedStateMask, 0, false, System.currentTimeMillis());
					first = false;
				}
			}
		}
	}
	
	
	private int lastKeyPressedCode = -1;
	public void init(final Control control) {
		Shell shell = control.getShell();
		
	 	shell.getDisplay().addFilter(SWT.KeyDown, new Listener() {


			public void handleEvent(Event event) {
				if (!control.isFocusControl())
					return;
				
				if (keyboardState.isPasting() && event.keyCode == SWT.ESC) {
					keyboardState.cancelPaste();
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
		        	//recordKey(false, event.stateMask, lastKeyPressedCode, System.currentTimeMillis());
		        }

		        lastKeyPressedCode = event.keyCode;

				recordKey(event, System.currentTimeMillis());
				event.doit = false;
				
			}
			
		});
		shell.getDisplay().addFilter(SWT.KeyUp, new Listener() {

			public void handleEvent(Event event) {
				recordKey(event, System.currentTimeMillis());
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
