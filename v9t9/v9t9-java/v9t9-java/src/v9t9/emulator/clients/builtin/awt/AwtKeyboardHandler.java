/**
 * 
 */
package v9t9.emulator.clients.builtin.awt;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import v9t9.emulator.clients.builtin.BaseKeyboardHandler;
import v9t9.emulator.common.IEventNotifier;
import v9t9.emulator.common.IEventNotifier.Level;
import v9t9.emulator.common.Machine;
import v9t9.emulator.runtime.cpu.Cpu;
import v9t9.keyboard.KeyboardState;

/**
 * @author Ed
 *
 */
public class AwtKeyboardHandler extends BaseKeyboardHandler {

	private long lastKeystrokeTime;
	private IEventNotifier eventNotifier;
	private static Pattern rawCodePattern = Pattern.compile(".*,rawCode=(\\d+),.*");

	public AwtKeyboardHandler(Component component, final KeyboardState keyboardState, Machine machine) {
		super(keyboardState, machine);
		component.addKeyListener(new KeyListener() {

			private int rawCode(KeyEvent e) {
				String v = e.toString();
				Matcher m = rawCodePattern.matcher(v);
				if (m.matches())
					return Integer.parseInt(m.group(1));
				else
					return e.getKeyCode();
			}
			public void keyPressed(KeyEvent e) {
				synchronized (keyboardState) {
					handleKey(true, e.getModifiers(), e.getKeyCode(), 
							e.getKeyChar(), e.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD,
							rawCode(e),
							e.getWhen());
				}
			}

			public void keyReleased(KeyEvent e) {
				synchronized (keyboardState) {
					handleKey(false, e.getModifiers(), e.getKeyCode(), 
							e.getKeyChar(), e.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD,
							rawCode(e),
							e.getWhen());
				}
			}

			public void keyTyped(KeyEvent e) {
			}
			
		});
	}

	protected void handleKey(boolean pressed, int modifiers, int keyCode, char ascii, boolean numpad, int realKey, long when) {
		if (keyboardState.isPasting() && pressed && keyCode == KeyEvent.VK_ESCAPE) {
			keyboardState.cancelPaste();
			return;
		}
		
		lastKeystrokeTime = when;
		
		//System.out.println("pressed="+pressed+"; modifiers="+Integer.toHexString(modifiers)+"; keyCode="+keyCode+"; ascii="+(int)ascii);
		
		if (ascii == KeyEvent.CHAR_UNDEFINED && keyCode < 128 && keyboardState.isAsciiDirectKey((char) keyCode)) {
			ascii = (char) keyCode;
		}
		if (ascii <= 32 && (modifiers & KeyEvent.CTRL_MASK) != 0) {
			// control char
			ascii = (char) keyCode;
		}
		if (ascii < 128) {
			if (Character.isLowerCase(ascii))
				ascii = Character.toUpperCase(ascii);
		}
		
		// backspace?
		if (ascii == 8 && modifiers == 0) {
			keyboardState.setKey(realKey, pressed, true, KeyboardState.SHIFT + KeyboardState.FCTN, 'S', when);
			return;
		}
		
		byte shift = 0;
		if ((modifiers & KeyEvent.SHIFT_DOWN_MASK + KeyEvent.SHIFT_MASK) != 0)
			shift |= KeyboardState.SHIFT;
		if ((modifiers & KeyEvent.CTRL_DOWN_MASK + KeyEvent.CTRL_MASK) != 0)
			shift |= KeyboardState.CTRL;
		if ((modifiers & KeyEvent.ALT_DOWN_MASK + KeyEvent.META_DOWN_MASK + KeyEvent.ALT_MASK + KeyEvent.META_MASK) != 0)
			shift |= KeyboardState.FCTN;
		
		boolean synthetic = true;
		
		int joy = (shift & KeyboardState.SHIFT) != 0 ? 2 : 1;
		
		if ((ascii == 0 || ascii == 0xffff) || 
				!keyboardState.postCharacter(machine, realKey, pressed, synthetic, shift, ascii, when)) {
			byte fctn = (byte) (KeyboardState.FCTN | shift);
			//System.out.println("??? " + keyCode + " : " + pressed);
			switch (keyCode) {
			case KeyEvent.VK_SHIFT:
				keyboardState.setKey(realKey, pressed, synthetic, KeyboardState.SHIFT, 0, when);
				break;
			case KeyEvent.VK_CONTROL:
				keyboardState.setKey(realKey, pressed, synthetic, KeyboardState.CTRL, 0, when);
				break;
			case KeyEvent.VK_ALT:
			case KeyEvent.VK_META:
				keyboardState.setKey(realKey, pressed, synthetic, KeyboardState.FCTN, 0, when);
				break;
			case KeyEvent.VK_ENTER:
				keyboardState.setKey(realKey, pressed, synthetic, shift, '\r', when);
				break;
				
			case KeyEvent.VK_ESCAPE:
				keyboardState.setKey(realKey, pressed, synthetic, KeyboardState.FCTN, '9', when);
				break;
				
			case KeyEvent.VK_CAPS_LOCK:
				if (pressed) {
					boolean on;
					try {
						on = !Toolkit.getDefaultToolkit().getLockingKeyState(keyCode);
					} catch (UnsupportedOperationException e) {
						on = !keyboardState.getAlpha();
					}
					keyboardState.setAlpha(on);
				}
				break;
			case KeyEvent.VK_CANCEL:
				System.exit(0);
				break;
			case KeyEvent.VK_PAUSE:
				if (pressed && (shift & KeyboardState.CTRL) != 0)
					System.exit(0);
				break;
			case KeyEvent.VK_F1:
			case KeyEvent.VK_F2:
			case KeyEvent.VK_F3:
			case KeyEvent.VK_F4:
			case KeyEvent.VK_F5:
			case KeyEvent.VK_F6:
			case KeyEvent.VK_F7:
			case KeyEvent.VK_F8:
			case KeyEvent.VK_F9:
				keyboardState.setKey(realKey, pressed, synthetic, fctn, '1' + KeyEvent.VK_F1 - keyCode, when);	
				break;
				
			case KeyEvent.VK_UP:
				keyboardState.setKey(realKey, pressed, synthetic, fctn, 'E', when);
				break;
			case KeyEvent.VK_DOWN:
				keyboardState.setKey(realKey, pressed, synthetic, fctn, 'X', when);
				break;
			case KeyEvent.VK_LEFT:
				keyboardState.setKey(realKey, pressed, synthetic, fctn, 'S', when);
				break;
			case KeyEvent.VK_RIGHT:
				keyboardState.setKey(realKey, pressed, synthetic, fctn, 'D', when);
				break;
				
			case KeyEvent.VK_KP_UP:
				keyboardState.setJoystick(joy,
						KeyboardState.JOY_Y,
						0, pressed ? -1 : 0, false, when);
				break;
			case KeyEvent.VK_KP_DOWN:
				keyboardState.setJoystick(joy,
						KeyboardState.JOY_Y,
						 0, pressed ? 1 : 0, false, when);
				break;
			case KeyEvent.VK_KP_LEFT:
				keyboardState.setJoystick(joy,
						KeyboardState.JOY_X,
						pressed ? -1 : 0, 0, false, when);
				break;
			case KeyEvent.VK_KP_RIGHT:
				keyboardState.setJoystick(joy,
						KeyboardState.JOY_X,
						pressed ? 1 : 0, 0, false, when);
				break;
				
			case KeyEvent.VK_HOME:
				if (!numpad) {
					keyboardState.setKey(realKey, pressed, synthetic, fctn, '5', when);		// BEGIN
				} else {
					keyboardState.setJoystick(joy,
							KeyboardState.JOY_Y | KeyboardState.JOY_X,
							pressed ? -1 : 0, pressed ? -1 : 0, false, when);
				}
				break;
				
			case KeyEvent.VK_INSERT:
				if (!numpad) {
					keyboardState.setKey(realKey, pressed, synthetic, fctn, '2', when);
				} else {
					keyboardState.setJoystick(joy,
							KeyboardState.JOY_B,
							0, 0, pressed, when);
				}
				break;
				
			case KeyEvent.VK_PAGE_UP:
				if (!numpad) {
					keyboardState.setKey(realKey, pressed, synthetic, fctn, '6', when); // (as per E/A and TI Writer)
				} else {
					keyboardState.setJoystick(joy,
							KeyboardState.JOY_Y | KeyboardState.JOY_X,
							pressed ? 1 : 0, pressed ? -1 : 0, false, when);
				}
				break;
			case KeyEvent.VK_PAGE_DOWN:
				if (!numpad) {
					keyboardState.setKey(realKey, pressed, synthetic, fctn, '4', when); // (as per E/A and TI Writer)
				} else {
					keyboardState.setJoystick(joy,
							KeyboardState.JOY_Y | KeyboardState.JOY_X,
							pressed ? 1 : 0, pressed ? 1 : 0, false, when);
				}
				break;
			case KeyEvent.VK_END:
				if (!numpad) {
					keyboardState.setKey(realKey, pressed, synthetic, fctn, '0', when);		// Fctn-0
				} else {
					keyboardState.setJoystick(joy,
							KeyboardState.JOY_Y | KeyboardState.JOY_X,
							pressed ? -1 : 0, pressed ? 1 : 0, false, when);
				}
				break;
				
			case KeyEvent.VK_SCROLL_LOCK:
				if (pressed) {
					boolean speedy;
					try {
						speedy = Toolkit.getDefaultToolkit().getLockingKeyState(keyCode);
					} catch (UnsupportedOperationException e) {
						// hmm
						speedy = Cpu.settingRealTime.getBoolean();
					}
					Cpu.settingRealTime.setBoolean(!speedy);
					if(eventNotifier != null)
						eventNotifier.notifyEvent(null, Level.INFO, 
								speedy ? "Scroll Lock: Executing at maximum speed" : 
									"Scroll Lock: Executing at fixed rate");
					//VdpTMS9918A.settingCpuSynchedVdpInterrupt.setBoolean(speedy);
				}
				break;
			default:
				System.out.println("Unhandled keycode: " + keyCode);
			}
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.KeyboardHandler#scan(v9t9.keyboard.KeyboardState)
	 */
	public void scan(KeyboardState state) {
		// all handled incrementally, but just in case something goes goofy...
		if (!state.isPasting()) {
			if (lastKeystrokeTime + 500 < System.currentTimeMillis()) {
				lastKeystrokeTime = System.currentTimeMillis();
				state.resetKeyboard();
			}
			
		}
	}

	/**
	 * @param eventNotifier the eventNotifier to set
	 */
	public void setEventNotifier(IEventNotifier eventNotifier) {
		this.eventNotifier = eventNotifier;
	}
}
