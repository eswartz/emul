/**
 * 
 */
package v9t9.gui.client.awt;

import static v9t9.common.keyboard.KeyboardConstants.*;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

import v9t9.common.client.IVideoRenderer;
import v9t9.common.keyboard.BaseKeyboardHandler;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.common.machine.IMachine;

/**
 * @author Ed
 *
 */
public class AwtKeyboardHandler extends BaseKeyboardHandler {

//	private long lastKeystrokeTime;
//	private Runnable keyTask;

	public AwtKeyboardHandler(final IKeyboardState keyboardState, IMachine machine) {
		super(keyboardState, machine);
		
//		keyTask = new Runnable() {
//			
//			@Override
//			public void run() {
//				// all handled incrementally, but just in case something goes goofy...
//				if (!isPasting()) {
//					if (lastKeystrokeTime + 500 < System.currentTimeMillis()) {
//						lastKeystrokeTime = System.currentTimeMillis();
//						keyboardState.resetKeyboard();
//					}
//				}
//			}
//		};
//		
//		machine.getFastMachineTimer().scheduleTask(keyTask, 4);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.client.IKeyboardHandler#init(v9t9.common.client.IVideoRenderer)
	 */
	@Override
	public void init(IVideoRenderer renderer) {
		Component component = ((AwtVideoRenderer) renderer).getAwtCanvas();
		
		component.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				handleKey(true, e.getModifiers(), e.getKeyCode(), 
						e.getKeyChar(), e.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD);
			}

			public void keyReleased(KeyEvent e) {
				handleKey(false, e.getModifiers(), e.getKeyCode(), 
						e.getKeyChar(), e.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD);
			}

			public void keyTyped(KeyEvent e) {
			}
			
		});
		
	}

	private static final Map<Integer, Integer> awtKeycodeToKey = new HashMap<Integer, Integer>(); 
	private static final int[] keycodesAndKeys = {
		KeyEvent.VK_SHIFT, KEY_SHIFT,
		KeyEvent.VK_CONTROL, KEY_CONTROL,
		KeyEvent.VK_ALT, KEY_ALT,
		KeyEvent.VK_META, KEY_ALT,
		KeyEvent.VK_CAPS_LOCK, KEY_CAPS_LOCK,
		KeyEvent.VK_NUM_LOCK, KEY_NUM_LOCK,
		KeyEvent.VK_SCROLL_LOCK, KEY_SCROLL_LOCK,
		//KeyEvent.VK_BREAK, KEY_BREAK,
		KeyEvent.VK_PAUSE, KEY_PAUSE,
		KeyEvent.VK_ESCAPE, KEY_ESCAPE,
		KeyEvent.VK_F1, KEY_F1,
		KeyEvent.VK_F2, KEY_F2,
		KeyEvent.VK_F3, KEY_F3,
		KeyEvent.VK_F4, KEY_F4,
		KeyEvent.VK_F5, KEY_F5,
		KeyEvent.VK_F6, KEY_F6,
		KeyEvent.VK_F7, KEY_F7,
		KeyEvent.VK_F8, KEY_F8,
		KeyEvent.VK_F9, KEY_F9,
		KeyEvent.VK_F10, KEY_F10,
		KeyEvent.VK_F11, KEY_F11,
		KeyEvent.VK_F12, KEY_F12,
		KeyEvent.VK_DOWN, KEY_ARROW_DOWN,
		KeyEvent.VK_UP, KEY_ARROW_UP,
		KeyEvent.VK_LEFT, KEY_ARROW_LEFT,
		KeyEvent.VK_RIGHT, KEY_ARROW_RIGHT,
		KeyEvent.VK_PAGE_DOWN, KEY_PAGE_DOWN,
		KeyEvent.VK_PAGE_UP, KEY_PAGE_UP,
		KeyEvent.VK_HOME, KEY_HOME,
		KeyEvent.VK_END, KEY_END,
		KeyEvent.VK_INSERT, KEY_INSERT,
		KeyEvent.VK_DELETE, KEY_DELETE,
		KeyEvent.VK_PRINTSCREEN, KEY_PRINT_SCREEN,
		KeyEvent.VK_KP_UP, KEY_KP_ARROW_UP,
		KeyEvent.VK_KP_DOWN, KEY_KP_ARROW_DOWN,
		KeyEvent.VK_KP_LEFT, KEY_KP_ARROW_LEFT,
		KeyEvent.VK_KP_RIGHT, KEY_KP_ARROW_RIGHT,
		KeyEvent.VK_TAB, KEY_TAB,
		KeyEvent.VK_BACK_SPACE, KEY_BACKSPACE,
		KeyEvent.VK_ENTER, KEY_ENTER,
		KeyEvent.VK_BEGIN, KEY_KP_SHIFT_5,
		KeyEvent.VK_NUMPAD0, KEY_KP_0,
		KeyEvent.VK_NUMPAD1, KEY_KP_1,
		KeyEvent.VK_NUMPAD2, KEY_KP_2,
		KeyEvent.VK_NUMPAD3, KEY_KP_3,
		KeyEvent.VK_NUMPAD4, KEY_KP_4,
		KeyEvent.VK_NUMPAD5, KEY_KP_5,
		KeyEvent.VK_NUMPAD6, KEY_KP_6,
		KeyEvent.VK_NUMPAD7, KEY_KP_7,
		KeyEvent.VK_NUMPAD8, KEY_KP_8,
		KeyEvent.VK_NUMPAD9, KEY_KP_9,
	};
	static {
		for (int i = 0; i < keycodesAndKeys.length; i += 2) {
			awtKeycodeToKey.put(keycodesAndKeys[i], keycodesAndKeys[i+1]);
		}
	}
	
	protected void handleKey(boolean pressed, int modifiers, int keyCode, char ascii, boolean keyPad) {
		if (isPasting() && pressed && keyCode == KeyEvent.VK_ESCAPE) {
			cancelPaste();
			return;
		}
		
//		lastKeystrokeTime = System.currentTimeMillis();
		
		if (ascii == KeyEvent.CHAR_UNDEFINED && keyCode < 128 && keyboardState.isAsciiDirectKey((char) keyCode) && !keyPad) {
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
		
		byte shiftMask = 0;
		if ((modifiers & KeyEvent.SHIFT_DOWN_MASK + KeyEvent.SHIFT_MASK) != 0)
			shiftMask |= MASK_SHIFT;
		if ((modifiers & KeyEvent.CTRL_DOWN_MASK + KeyEvent.CTRL_MASK) != 0)
			shiftMask |= MASK_CONTROL;
		if ((modifiers & KeyEvent.ALT_DOWN_MASK + KeyEvent.META_DOWN_MASK + KeyEvent.ALT_MASK + KeyEvent.META_MASK) != 0)
			shiftMask |= MASK_ALT;
		
		if (ascii > 0 && ascii < 128) {
			if (keyPad) {
				shiftMask &= ~MASK_SHIFT;
			}
			
			if (postCharacter(pressed, shiftMask, ascii)) {
				return;
			}
		}
		
		int key = KEY_UNKNOWN;
		if ((shiftMask & MASK_CONTROL) != 0 && keyCode == KeyEvent.VK_PAUSE) {
			key = KEY_BREAK;
		}

		if (key == KEY_UNKNOWN) {
			Integer ikey = awtKeycodeToKey.get(keyCode);
			if (ikey != null) {
				key = ikey;
			}
		}
		
		if (key != KEY_UNKNOWN) {
			handleSpecialKey(pressed, shiftMask, key, keyPad);
		}
		else {
			System.out.println("*** Unhandled AWT keycode: " + keyCode);
		}
	}

}
