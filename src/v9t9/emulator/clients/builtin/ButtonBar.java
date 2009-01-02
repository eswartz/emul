/**
 * 
 */
package v9t9.emulator.clients.builtin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

import v9t9.emulator.clients.builtin.video.VideoRenderer;

class ButtonBar extends Composite {

	GridLayout layout;
	private boolean isHorizontal;
	final VideoRenderer videoRenderer;

	public ButtonBar(Composite parent, int style, VideoRenderer videoRenderer) {
		super(parent, style & ~(SWT.HORIZONTAL + SWT.VERTICAL) | SWT.NO_RADIO_GROUP | SWT.NO_FOCUS);
		this.videoRenderer = videoRenderer;
		this.isHorizontal = (style & SWT.HORIZONTAL) != 0;
		layout = new GridLayout(1, true);
		layout.marginHeight = layout.marginWidth = 0;
		setLayout(layout);
		
		addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				paintButtonBar(e.gc, e.widget, new Point(0, 0), getSize());
			}
			
		});
	}

	protected void paintButtonBar(GC gc, Widget w, Point offset,  Point size) {
		gc.setForeground(w.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		gc.setBackground(w.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		if (isHorizontal) {
			gc.fillGradientRectangle(offset.x, offset.y + size.y / 2, size.x, size.y / 2, true);
			gc.fillGradientRectangle(offset.x, offset.y + size.y / 2, size.x, -size.y / 2, true);
		} else {
			gc.fillGradientRectangle(offset.x + size.x / 2, offset.y, size.x, size.y, false);
			gc.fillGradientRectangle(offset.x + size.x / 2, offset.y, -size.x / 2, size.y, false);
			
		}
	}
}