/**
 * 
 */
package v9t9.gui.client.swt.bars;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;

import v9t9.gui.client.swt.IFocusRestorer;

public interface IImageBar {
	Composite getComposite();
	void drawBackground(GC gc);
	IFocusRestorer getFocusRestorer();
	boolean isHorizontal();
	void redrawAll();
}