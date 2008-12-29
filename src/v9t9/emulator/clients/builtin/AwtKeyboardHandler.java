/**
 * 
 */
package v9t9.emulator.clients.builtin;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import v9t9.emulator.Machine;
import v9t9.keyboard.KeyboardState;

/**
 * @author Ed
 *
 */
public class AwtKeyboardHandler extends BaseKeyboardHandler {

	private long lastKeystrokeTime;

	public AwtKeyboardHandler(Component component, KeyboardState keyboardState, Machine machine) {
		super(keyboardState, machine);
		component.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				handleKey(true, e.getModifiers(), e.getKeyCode(), e.getKeyChar());
			}

			public void keyReleased(KeyEvent e) {
				handleKey(false, e.getModifiers(), e.getKeyCode(), e.getKeyChar());
			}

			public void keyTyped(KeyEvent e) {
			}
			
		});
	}

	protected void handleKey(boolean pressed, int modifiers, int keyCode, char ascii) {
		if (pasteTimer != null && pressed && keyCode == KeyEvent.VK_ESCAPE) {
			cancelPaste();
			return;
		}
		
		if (pasteTimer == null)
			lastKeystrokeTime = System.currentTimeMillis();
		
		if (ascii < 32 && (modifiers & KeyEvent.CTRL_MASK) != 0) {
			// control char
			ascii = (char) keyCode;
		}
		if (ascii < 128) {
			if (Character.isLowerCase(ascii))
				ascii = Character.toUpperCase(ascii);
		}
		
		byte shift = 0;
		if ((modifiers & KeyEvent.SHIFT_DOWN_MASK + KeyEvent.SHIFT_MASK) != 0)
			shift |= KeyboardState.SHIFT;
		if ((modifiers & KeyEvent.CTRL_DOWN_MASK + KeyEvent.CTRL_MASK) != 0)
			shift |= KeyboardState.CTRL;
		if ((modifiers & KeyEvent.ALT_DOWN_MASK + KeyEvent.META_DOWN_MASK + KeyEvent.ALT_MASK + KeyEvent.META_MASK) != 0)
			shift |= KeyboardState.FCTN;
		
		if (ascii == 0 || ascii == 0xffff || !keyboardState.postCharacter(pressed, false, shift, ascii)) {
			byte fctn = (byte) (KeyboardState.FCTN | shift);
			
			switch (keyCode) {
			case KeyEvent.VK_SHIFT:
				keyboardState.setKey(pressed, false, KeyboardState.SHIFT, 0);
				break;
			case KeyEvent.VK_CONTROL:
				keyboardState.setKey(pressed, false, KeyboardState.CTRL, 0);
				break;
			case KeyEvent.VK_ALT:
			case KeyEvent.VK_META:
				keyboardState.setKey(pressed, false, KeyboardState.FCTN, 0);
				break;
			case KeyEvent.VK_ENTER:
				keyboardState.setKey(pressed, false, shift, '\r');
				break;
				
			case KeyEvent.VK_CAPS_LOCK:
				if (pressed) {
					keyboardState.setAlpha(!keyboardState.getAlpha());
				}
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
				keyboardState.setKey(pressed, false, fctn, '1' + KeyEvent.VK_F1 - keyCode);	
				break;
				
			case KeyEvent.VK_UP:
			case KeyEvent.VK_KP_UP:
				keyboardState.setKey(pressed, false, fctn, 'E');
				break;
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_KP_DOWN:
				keyboardState.setKey(pressed, false, fctn, 'X');
				break;
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_KP_LEFT:
				keyboardState.setKey(pressed, false, fctn, 'S');
				break;
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_KP_RIGHT:
				keyboardState.setKey(pressed, false, fctn, 'D');
				break;
				
				
			case KeyEvent.VK_INSERT:
				keyboardState.setKey(pressed, false, fctn, '2');	
				break;
				
			case KeyEvent.VK_PAGE_UP:
				keyboardState.setKey(pressed, false, fctn, '6'); // (as per E/A and TI Writer)
				break;
			case KeyEvent.VK_PAGE_DOWN:
				keyboardState.setKey(pressed, false, fctn, '4'); // (as per E/A and TI Writer)
				break;

			case KeyEvent.VK_HOME:
				keyboardState.setKey(pressed, false, fctn, '5');		// BEGIN
				break;
			case KeyEvent.VK_END:
				keyboardState.setKey(pressed, false, fctn, '0');		// Fctn-0
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
		if (lastKeystrokeTime + 1000 < System.currentTimeMillis()) {
			lastKeystrokeTime = System.currentTimeMillis();
			state.resetKeyboard();
		}
	}

}
