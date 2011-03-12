/**
 * Mar 12, 2011
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Point;

/**
 * @author ejs
 *
 */
public class BlankIcon extends ImageIconCanvas {

	public BlankIcon(IImageBar parentDrawer, int style) {
		super(parentDrawer, style, null, -1, null);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ImageIconCanvas#doPaint(org.eclipse.swt.events.PaintEvent)
	 */
	@Override
	protected void doPaint(PaintEvent e) {
		Point size = getSize();
		this.parentDrawer.drawBackground(e.gc, this, new Point(0, 0), size);
	}

}
