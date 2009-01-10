/**
 * 
 */
package v9t9.emulator.clients.builtin;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import v9t9.emulator.Machine;
import v9t9.engine.KeyboardHandler;
import v9t9.keyboard.KeyboardState;
import v9t9.utils.Utils;

/**
 * @author ejs
 * 
 */
public abstract class BaseKeyboardHandler implements KeyboardHandler {

	
	protected Timer pasteTimer;

	protected final Machine machine;

	protected KeyboardState keyboardState;
	
	public BaseKeyboardHandler(KeyboardState keyboardState, Machine machine) {
		this.keyboardState = keyboardState;
		this.machine = machine;
	}

	protected void cancelPaste() {
		keyboardState.resetKeyboard();
		pasteTimer.cancel();
		pasteTimer = null;
	}

	/**
	 * Paste text into the clipboard
	 * @param contents
	 */
	public void pasteText(String contents) {

		contents = contents.replaceAll("(\r\n|\r|\n)", "\r");
		contents = contents.replaceAll("\t", "    ");
		final char[] chs = contents.toCharArray();
		pasteTimer = new Timer("Paster");
		TimerTask pasteCharacterTask = new TimerTask() {
			int index = 0;
			byte prevShift = 0;
			char prevCh = 0;
			int successiveCharTimeout;
			@Override
			public void run() {
				if (pasteTimer == null)
					return;
				
				if (!machine.isAlive())
					cancelPaste();
				
				if (Machine.settingPauseMachine.getBoolean())
					return;
				
				if (index <= chs.length) {
					// only send chars as fast as the machine is reading
					if (!keyboardState.wasKeyboardProbed())
						return;
					
					if (prevCh != 0)
						keyboardState.postCharacter(false, true, prevShift, prevCh);
					
					if (index < chs.length) {
						char ch = chs[index];
						byte shift = 0;

						if (Character.isLowerCase(ch)) {
				    		ch = Character.toUpperCase(ch);
				    		shift &= ~ KeyboardState.SHIFT;
				    	} else if (Character.isUpperCase(ch)) {
				    		shift |= KeyboardState.SHIFT;
				    	}
				    	
						//System.out.println("ch="+ch+"; prevCh="+prevCh+"; sCT="+successiveCharTimeout);
						if (ch == prevCh) {
							if (successiveCharTimeout == 0) {
								// need to inject a spacer to distinguish 
								// successive repeated characters
								keyboardState.resetKeyboard();
								prevCh = 0;
								successiveCharTimeout = 2;
								return;
							} else if (--successiveCharTimeout > 0) {
								return;
							}
						}
						
						index++;
						
						keyboardState.postCharacter(true, true, shift, ch);
						
						
						prevCh = ch;
						prevShift = shift;
					} else {
						cancelPaste();
					}
				}
			}
			
		};
		// TODO: find a better way to ensure the keyboard was scanned fully
		pasteTimer.schedule(pasteCharacterTask, 0, 1000 / 30); 
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
}
