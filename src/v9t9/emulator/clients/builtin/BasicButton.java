/**
 * 
 */
package v9t9.emulator.clients.builtin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Event;

class BasicButton extends Canvas {

	private final Rectangle bounds;
	private Image icon;
	private Rectangle overlayBounds;
	private List<SelectionListener> listeners;
	private boolean selected;

	public BasicButton(ButtonBar buttonBar, int style, Image icon_, Rectangle bounds_, String tooltip) {
		super(buttonBar, SWT.NO_FOCUS | SWT.NO_RADIO_GROUP /*| SWT.NO_BACKGROUND*/);
		
		buttonBar.layout.numColumns++;
		
		this.icon = icon_;
		this.bounds = bounds_;
		this.listeners = new ArrayList<SelectionListener>();
		addKeyListener(new KeyListener() {
			
			public void keyPressed(KeyEvent e) {
				e.doit = false;
			}

			public void keyReleased(KeyEvent e) {
				e.doit = false;
			}
			
		});
		
		GridData data = new GridData(bounds.width, bounds.height);
		data.minimumHeight = 8;
		data.grabExcessHorizontalSpace = false;
		data.grabExcessVerticalSpace = false;
		setLayoutData(data);
		setLayout(new FillLayout());
		
		setToolTipText(tooltip);
		
		addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				Point size = getSize();
				((ButtonBar)getParent()).paintButtonBar(e.gc, BasicButton.this, new Point(0, 0), size);
				e.gc.drawImage(icon, bounds.x, bounds.y, bounds.width, bounds.height, 
						0, 0, size.x, size.y);
				if (overlayBounds != null)
					e.gc.drawImage(icon, overlayBounds.x, overlayBounds.y, overlayBounds.width, overlayBounds.height, 
							0, 0, size.y, size.y);
			}
			
		});
		addTraverseListener(new TraverseListener() {

			public void keyTraversed(TraverseEvent e) {
				e.doit = false;
			}
			
		});
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				SelectionListener[] array = (SelectionListener[]) listeners.toArray(new SelectionListener[listeners.size()]);
				Event event = new Event();
				event.widget = BasicButton.this;
				SelectionEvent selEvent = new SelectionEvent(event);
				for (SelectionListener listener : array) {
					listener.widgetSelected(selEvent);
				}
				((ButtonBar)getParent()).videoRenderer.setFocus();
			}
		});
		
	}

	public void setOverlayBounds(Rectangle overlayBounds) {
		this.overlayBounds = overlayBounds;
	}

	public void addSelectionListener(SelectionListener listener) {
		listeners.add(listener);
	}

	public boolean getSelection() {
		return selected;
	}

	public void setSelection(boolean flag) {
		if (flag != selected) {
			this.selected = flag;
			redraw();
		}
	}
}