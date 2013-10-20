/**
 * 
 */
package org.ejs.gui.common;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

/**
 * @author ejs
 *
 */
public class SwtUtils {

	public static void runMenu(final Control parent, final int x, final int y,
			final Menu menu) {
		
		if (menu.getItemCount() == 0) {
			menu.dispose();
			return;
		}
		
		if (parent != null) {
			Point loc = parent.toDisplay(x, y); 
			menu.setLocation(loc);
		}
		menu.setVisible(true);
		
		final Shell menuShell = parent != null ? parent.getShell() : menu.getShell();
		Display display = menuShell.getDisplay();
		while (display.readAndDispatch()) /**/ ;

		while (!menu.isDisposed() && menu.isVisible()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
	}
}
