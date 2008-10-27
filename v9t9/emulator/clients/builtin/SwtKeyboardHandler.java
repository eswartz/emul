/**
 * 
 */
package v9t9.emulator.clients.builtin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import v9t9.engine.KeyboardHandler;
import v9t9.keyboard.KeyboardState;

/**
 * @author ejs
 *
 */
public class SwtKeyboardHandler implements KeyboardHandler {

	private final KeyboardState keyboardState;

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
		if ((stateMask & SWT.CTRL) != 0)
			shift |= KeyboardState.CTRL;
		if ((stateMask & SWT.SHIFT) != 0)
			shift |= KeyboardState.CTRL;
		if ((stateMask & SWT.ALT) != 0)
			shift |= KeyboardState.FCTN;
		
		keyboardState.setKey(pressed, shift, (byte) keyCode);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.handlers.KeyboardHandler#scan(v9t9.keyboard.KeyboardState)
	 */
	public void scan(KeyboardState state) {
		// handled in events
	}

}
