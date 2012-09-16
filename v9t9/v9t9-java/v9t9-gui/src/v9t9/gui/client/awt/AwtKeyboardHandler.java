/**
 * 
 */
package v9t9.gui.client.awt;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ejs.base.properties.IProperty;

import v9t9.common.client.IVideoRenderer;
import v9t9.common.events.IEventNotifier;
import v9t9.common.events.IEventNotifier.Level;
import v9t9.common.keyboard.BaseKeyboardHandler;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import static v9t9.common.keyboard.KeyboardConstants.*;

/**
 * @author Ed
 *
 */
public class AwtKeyboardHandler extends BaseKeyboardHandler {

	private long lastKeystrokeTime;
	private IEventNotifier eventNotifier;
	private Runnable keyTask;
	private static Pattern rawCodePattern = Pattern.compile(".*,rawCode=(\\d+),.*");

	public AwtKeyboardHandler(final IKeyboardState keyboardState, IMachine machine) {
		super(keyboardState, machine);
		
		keyTask = new Runnable() {
			
			@Override
			public void run() {
				// all handled incrementally, but just in case something goes goofy...
				if (!isPasting()) {
					if (lastKeystrokeTime + 500 < System.currentTimeMillis()) {
						lastKeystrokeTime = System.currentTimeMillis();
						keyboardState.resetKeyboard();
					}
				}
			}
		};
		
		machine.getFastMachineTimer().scheduleTask(keyTask, 4);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IKeyboardHandler#init(v9t9.common.client.IVideoRenderer)
	 */
	@Override
	public void init(IVideoRenderer renderer) {
		Component component = ((AwtVideoRenderer) renderer).getAwtCanvas();
		
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
				handleKey(true, e.getModifiers(), e.getKeyCode(), 
						e.getKeyChar(), e.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD,
						rawCode(e),
						e.getWhen());
			}

			public void keyReleased(KeyEvent e) {
				handleKey(false, e.getModifiers(), e.getKeyCode(), 
						e.getKeyChar(), e.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD,
						rawCode(e),
						e.getWhen());
			}

			public void keyTyped(KeyEvent e) {
			}
			
		});
		
	}

	protected void handleKey(boolean pressed, int modifiers, int keyCode, char ascii, boolean numpad, int realKey, long when) {
		if (isPasting() && pressed && keyCode == KeyEvent.VK_ESCAPE) {
			cancelPaste();
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
			setKey(realKey, pressed, true, (byte)(MASK_SHIFT + MASK_ALT), 'S', when);
			return;
		}
		
		byte shift = 0;
		if ((modifiers & KeyEvent.SHIFT_DOWN_MASK + KeyEvent.SHIFT_MASK) != 0)
			shift |= MASK_SHIFT;
		if ((modifiers & KeyEvent.CTRL_DOWN_MASK + KeyEvent.CTRL_MASK) != 0)
			shift |= MASK_CONTROL;
		if ((modifiers & KeyEvent.ALT_DOWN_MASK + KeyEvent.META_DOWN_MASK + KeyEvent.ALT_MASK + KeyEvent.META_MASK) != 0)
			shift |= MASK_ALT;
		
		boolean synthetic = true;
		
		int joy = (shift & MASK_SHIFT) != 0 ? 2 : 1;
		
		if ((ascii == 0 || ascii == 0xffff) || 
				!postCharacter(machine, realKey, pressed, synthetic, shift, ascii, when)) {
			byte fctn = (byte) (MASK_ALT | shift);
			//System.out.println("??? " + keyCode + " : " + pressed);
			switch (keyCode) {
			case KeyEvent.VK_SHIFT:
				setKey(realKey, pressed, synthetic, MASK_SHIFT, 0, when);
				break;
			case KeyEvent.VK_CONTROL:
				setKey(realKey, pressed, synthetic, MASK_CONTROL, 0, when);
				break;
			case KeyEvent.VK_ALT:
			case KeyEvent.VK_META:
				setKey(realKey, pressed, synthetic, MASK_ALT, 0, when);
				break;
			case KeyEvent.VK_ENTER:
				setKey(realKey, pressed, synthetic, shift, '\r', when);
				break;
				
			case KeyEvent.VK_ESCAPE:
				setKey(realKey, pressed, synthetic, MASK_ALT, '9', when);
				break;
				
			case KeyEvent.VK_CAPS_LOCK:
				if (pressed) {
					boolean on;
					try {
						on = !Toolkit.getDefaultToolkit().getLockingKeyState(keyCode);
					} catch (UnsupportedOperationException e) {
						on = (keyboardState.getLockMask() & MASK_CAPS_LOCK) == 0;
					}
					keyboardState.changeLocks(on, MASK_CAPS_LOCK);
				}
				break;
			case KeyEvent.VK_PAUSE:
				if (pressed) {
					if ((shift & MASK_CONTROL) != 0) {
						machine.getClient().close();
						System.exit(0);	// HACK: AWT seems to get stuck otherwise
					} else {
						IProperty paused = Settings.get(machine, IMachine.settingPauseMachine);
						paused.setBoolean(!paused.getBoolean());
					}
				}
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
				setKey(realKey, pressed, synthetic, fctn, '1' + KeyEvent.VK_F1 - keyCode, when);	
				break;
				
			case KeyEvent.VK_UP:
				setKey(realKey, pressed, synthetic, fctn, 'E', when);
				break;
			case KeyEvent.VK_DOWN:
				setKey(realKey, pressed, synthetic, fctn, 'X', when);
				break;
			case KeyEvent.VK_LEFT:
				setKey(realKey, pressed, synthetic, fctn, 'S', when);
				break;
			case KeyEvent.VK_RIGHT:
				setKey(realKey, pressed, synthetic, fctn, 'D', when);
				break;
				
			case KeyEvent.VK_KP_UP:
				if (isKeypadForJoystick())
					keyboardState.setJoystick(joy,
							IKeyboardState.JOY_Y,
							0, pressed ? -1 : 0, false, when);
				break;
			case KeyEvent.VK_KP_DOWN:
				if (isKeypadForJoystick())
					keyboardState.setJoystick(joy,
							IKeyboardState.JOY_Y,
							 0, pressed ? 1 : 0, false, when);
				break;
			case KeyEvent.VK_KP_LEFT:
				if (isKeypadForJoystick())
					keyboardState.setJoystick(joy,
							IKeyboardState.JOY_X,
							pressed ? -1 : 0, 0, false, when);
				break;
			case KeyEvent.VK_KP_RIGHT:
				if (isKeypadForJoystick())
					keyboardState.setJoystick(joy,
							IKeyboardState.JOY_X,
							pressed ? 1 : 0, 0, false, when);
				break;
				
			case KeyEvent.VK_HOME:
				if (!numpad) {
					setKey(realKey, pressed, synthetic, fctn, '5', when);		// BEGIN
				} else if (isKeypadForJoystick()) {
					keyboardState.setJoystick(joy,
							IKeyboardState.JOY_Y | IKeyboardState.JOY_X,
							pressed ? -1 : 0, pressed ? -1 : 0, false, when);
				}
				break;
				
			case KeyEvent.VK_INSERT:
				if (!numpad) {
					setKey(realKey, pressed, synthetic, fctn, '2', when);
				} else if (isKeypadForJoystick()) {
					keyboardState.setJoystick(joy,
							IKeyboardState.JOY_B,
							0, 0, pressed, when);
				}
				break;
				
			case KeyEvent.VK_PAGE_UP:
				if (!numpad) {
					setKey(realKey, pressed, synthetic, fctn, '6', when); // (as per E/A and TI Writer)
				} else if (isKeypadForJoystick()) {
					keyboardState.setJoystick(joy,
							IKeyboardState.JOY_Y | IKeyboardState.JOY_X,
							pressed ? 1 : 0, pressed ? -1 : 0, false, when);
				}
				break;
			case KeyEvent.VK_PAGE_DOWN:
				if (!numpad) {
					setKey(realKey, pressed, synthetic, fctn, '4', when); // (as per E/A and TI Writer)
				} else if (isKeypadForJoystick()) {
					keyboardState.setJoystick(joy,
							IKeyboardState.JOY_Y | IKeyboardState.JOY_X,
							pressed ? 1 : 0, pressed ? 1 : 0, false, when);
				}
				break;
			case KeyEvent.VK_END:
				if (!numpad) {
					setKey(realKey, pressed, synthetic, fctn, '0', when);		// Fctn-0
				} else if (isKeypadForJoystick()) {
					keyboardState.setJoystick(joy,
							IKeyboardState.JOY_Y | IKeyboardState.JOY_X,
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
						speedy = machine.getCpu().settingRealTime().getBoolean();
					}
					machine.getCpu().settingRealTime().setBoolean(!speedy);
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

	/**
	 * @return
	 */
	private boolean isKeypadForJoystick() {
		return true;
	}

	/**
	 * @param eventNotifier the eventNotifier to set
	 */
	public void setEventNotifier(IEventNotifier eventNotifier) {
		this.eventNotifier = eventNotifier;
	}
}
