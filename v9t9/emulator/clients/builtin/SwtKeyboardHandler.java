/**
 * 
 */
package v9t9.emulator.clients.builtin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import v9t9.engine.KeyboardHandler;
import v9t9.keyboard.KeyboardState;

/**
 * SWT keyboard control.
 * 
 * We establish a display-level filter because SWT doesn't route keyboard
 * events predictably to the widgets you'd expect.  Also, we need to impose
 * additional wankery to unset keys whose keyup events get lost.  This can
 * happen when, for instance, you hold down E, then press X, then release E --
 * you'll only see keydowns for E and X.
 * @author ejs
 *
 */
public class SwtKeyboardHandler implements KeyboardHandler {

	private final KeyboardState keyboardState;
	private Map<Integer, Long> keyTimeMap = new HashMap<Integer, Long>();
	
	public SwtKeyboardHandler(Shell shell, KeyboardState keyboardState) {
		this.keyboardState = keyboardState;
		shell.getDisplay().addFilter(SWT.KeyDown, new Listener() {

			public void handleEvent(Event event) {
				updateKey(true, event.stateMask, event.keyCode);
				
			}
			
		});
		shell.getDisplay().addFilter(SWT.KeyUp, new Listener() {

			public void handleEvent(Event event) {
				updateKey(false, event.stateMask, event.keyCode);
			}
			
		});
		/*
		shell.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				updateKey(true, e);
			}


			public void keyReleased(KeyEvent e) {
				updateKey(false, e);
			}
			
		});*/
	}

	private void updateKey(boolean pressed, int stateMask, int keyCode) {
		byte shift = 0;
		
		// separately pressed keys show up in keycode sometimes
		
		if (((stateMask | keyCode) & SWT.CTRL) != 0)
			shift |= KeyboardState.CTRL;
		if (((stateMask | keyCode) & SWT.SHIFT) != 0)
			shift |= KeyboardState.SHIFT;
		if (((stateMask | keyCode) & SWT.ALT) != 0)
			shift |= KeyboardState.FCTN;
		
		keyCode &= 0xff;
		if (Character.isLowerCase(keyCode))
			keyCode = Character.toUpperCase(keyCode);
		
		if (pressed) {
			int code = keyCode | (shift << 8);
			Long timeout = keyTimeMap.get(code);
			if (timeout != null)
				keyTimeMap.put(code, System.currentTimeMillis() + 1000 / 15);	// repeating
			else
				keyTimeMap.put(code, System.currentTimeMillis() + 1000);	// initial press
		}
		keyboardState.setKey(pressed, shift, (byte) keyCode);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.handlers.KeyboardHandler#scan(v9t9.keyboard.KeyboardState)
	 */
	public void scan(KeyboardState state) {
		//state.resetKeyboard();
		//for (Map.Entry<Integer, Integer> entry : keyMap.entrySet()) {
		//	updateKey(true, entry.getValue(), entry.getKey());
		//}
		/*if (System.currentTimeMillis() > resetTime) {
			keyMap.clear();
			resetTime = System.currentTimeMillis() + 1000;
		}*/
		long now = System.currentTimeMillis();
		for (Iterator<Map.Entry<Integer, Long>> iter = keyTimeMap.entrySet().iterator(); iter.hasNext(); ) {
			Map.Entry<Integer, Long> entry = iter.next();
			if (entry.getValue() <= now) {
				keyboardState.setKey(false, (byte)(entry.getKey() >> 8), (byte) (entry.getKey() & 0xff));
				iter.remove();
			}
		}
	}

}
