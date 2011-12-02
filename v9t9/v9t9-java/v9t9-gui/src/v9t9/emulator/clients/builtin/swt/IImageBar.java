/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;

public interface IImageBar {
	Composite getComposite();
	void drawBackground(GC gc);
	IFocusRestorer getFocusRestorer();
	boolean isHorizontal();
	void redrawAll();
}