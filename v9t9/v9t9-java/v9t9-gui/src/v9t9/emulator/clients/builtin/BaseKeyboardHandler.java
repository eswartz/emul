/**
 * 
 */
package v9t9.emulator.clients.builtin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.common.IMachine;
import v9t9.engine.KeyboardHandler;
import v9t9.keyboard.KeyboardState;

/**
 * @author ejs
 * 
 */
public abstract class BaseKeyboardHandler implements KeyboardHandler {


	protected final IMachine machine;

	protected KeyboardState keyboardState;
	
	public BaseKeyboardHandler(KeyboardState keyboardState, IMachine machine) {
		this.keyboardState = keyboardState;
		this.machine = machine;
	}

	protected void cancelPaste() {
		keyboardState.cancelPaste();
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
