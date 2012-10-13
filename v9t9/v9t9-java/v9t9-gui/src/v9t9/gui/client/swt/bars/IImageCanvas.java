/**
 * 
 */
package v9t9.gui.client.swt.bars;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import v9t9.gui.client.swt.IFocusRestorer;

/**
 * @author ejs
 *
 */
public interface IImageCanvas {

	Composite getComposite();

	void drawBackground(GC gc);

	IFocusRestorer getFocusRestorer();

	boolean isHorizontal();

	void redrawAll();

	Point getPaintOffset();
	boolean isRetracted();

}