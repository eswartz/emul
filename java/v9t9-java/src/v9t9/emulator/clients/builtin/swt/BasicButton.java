/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

class BasicButton extends ImageButton {
	
	public BasicButton(ButtonBar buttonBar, int style, TreeMap<Integer, Image> iconMap, Rectangle bounds_, String tooltip) {
		super(buttonBar, SWT.NO_FOCUS | SWT.NO_RADIO_GROUP |style, iconMap, bounds_, tooltip);
		
		addKeyListener(new KeyListener() {
			
			public void keyPressed(KeyEvent e) {
				e.doit = false;
			}

			public void keyReleased(KeyEvent e) {
				e.doit = false;
			}
			
		});
		addTraverseListener(new TraverseListener() {

			public void keyTraversed(TraverseEvent e) {
				e.doit = false;
			}
			
		});

		setFocusRestorer(buttonBar.getFocusRestorer());
	}


}