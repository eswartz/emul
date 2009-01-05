/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

import v9t9.emulator.clients.builtin.video.VideoRenderer;

class ButtonBar extends Composite {

	GridLayout layout;
	private boolean isHorizontal;
	final VideoRenderer videoRenderer;
	private Composite buttonComposite;

	/**
	 * Create a button bar with the given orientation.  This must be in a parent with a GridLayout.
	 * @param parent
	 * @param style
	 * @param videoRenderer
	 */
	public ButtonBar(Composite parent, int style, VideoRenderer videoRenderer) {
		// the bar itself is the full width of the parent
		super(parent, style & ~(SWT.HORIZONTAL + SWT.VERTICAL) | SWT.NO_RADIO_GROUP | SWT.NO_FOCUS);
		this.videoRenderer = videoRenderer;
		this.isHorizontal = (style & SWT.HORIZONTAL) != 0;
		
		setLayoutData(new GridData(isHorizontal ? SWT.FILL : SWT.CENTER, isHorizontal ? SWT.CENTER : SWT.FILL, false, false));

		GridLayout mainLayout = new GridLayout(1, false);
		mainLayout.marginHeight = mainLayout.marginWidth = 0;
		setLayout(mainLayout);
		

		// the inner composite contains the buttons, tightly packed
		buttonComposite = new Composite(this, SWT.NO_RADIO_GROUP | SWT.NO_FOCUS | SWT.NO_BACKGROUND);
		
		// start off with one horizontal cell; in #addedButton() the columns is increased for horizontal bars
		layout = new GridLayout(1, true);
		layout.marginHeight = layout.marginWidth = 0;
		buttonComposite.setLayout(layout);
		
		buttonComposite.setLayoutData(GridDataFactory.fillDefaults()
				.align(isHorizontal ? SWT.CENTER : SWT.FILL, isHorizontal ? SWT.FILL : SWT.CENTER)
				.grab(isHorizontal, !isHorizontal)
				.create());
		
		addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				paintButtonBar(e.gc, e.widget, new Point(0, 0), getSize());
			}
			
		});
		
		buttonComposite.addPaintListener(new PaintListener() {

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

	/**
	 * The composite to which to add buttons.
	 * @return
	 */
	public Composite getComposite() {
		return buttonComposite;
	}

	public void addedButton() {
		if (isHorizontal)
			layout.numColumns++;		
	}

	public void setHorizontal(boolean isHorizontal) {
		this.isHorizontal = isHorizontal;
	}

	public boolean isHorizontal() {
		return isHorizontal;
	}
}