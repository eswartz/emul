/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
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
	private ButtonBar buttonBar;
	private boolean isHighlighted;
	private boolean pressed;
	
	public BasicButton(ButtonBar buttonBar, int style, Image icon_, Rectangle bounds_, String tooltip) {
		super(buttonBar.getComposite(), SWT.NO_FOCUS | SWT.NO_RADIO_GROUP /*| SWT.NO_BACKGROUND*/);
		
		this.buttonBar = buttonBar;
		buttonBar.addedButton();
		
		this.icon = icon_;
		this.bounds = bounds_;
		//bounds.x *= 2; bounds.y *= 2;
		//bounds.width *= 2; bounds.height *= 2;
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
		data.minimumHeight = 8;	// the minimums above override this
		data.minimumWidth = 8;	// the minimums above override this
		data.grabExcessHorizontalSpace = false;
		data.grabExcessVerticalSpace = false;
		setLayoutData(data);
		setLayout(new FillLayout());
		
		setToolTipText(tooltip);
		
		addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				doPaint(e);
			}
			
		});
		addTraverseListener(new TraverseListener() {

			public void keyTraversed(TraverseEvent e) {
				e.doit = false;
			}
			
		});
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				doClickStart();
			}
			@Override
			public void mouseUp(MouseEvent e) {
				doClickStop(e);
			}
		});
		
		addMouseTrackListener(new MouseTrackListener() {

			public void mouseEnter(MouseEvent e) {
				doMouseEnter();
			}

			public void mouseExit(MouseEvent e) {
				doMouseExit();
			}

			public void mouseHover(MouseEvent e) {
				doMouseHover();
			}
			
		});
		
	}

	public void setOverlayBounds(Rectangle overlayBounds) {
		if (overlayBounds == null)
			this.overlayBounds = null;
		else
			this.overlayBounds = overlayBounds; 
				//new Rectangle(
				//overlayBounds.x * 2, overlayBounds.y * 2,
				//overlayBounds.width * 2, overlayBounds.height * 2);
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

	protected void doPaint(PaintEvent e) {
		Point size = getSize();
		this.buttonBar.paintButtonBar(e.gc, this, new Point(0, 0), size);
		//e.gc.setAntialias(SWT.ON);
		int offset = pressed ? 2 : 0;
		e.gc.drawImage(icon, bounds.x, bounds.y, bounds.width, bounds.height, 
				offset, offset, size.x, size.y);
		if (overlayBounds != null)
			e.gc.drawImage(icon, overlayBounds.x, overlayBounds.y, overlayBounds.width, overlayBounds.height, 
					0, 0, size.y, size.y);
		//e.gc.setAntialias(SWT.OFF);
		if (isHighlighted) {
			e.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
			//e.gc.setLineStyle(SWT.LINE_DOT);
			//e.gc.drawRectangle(0, 0, size.x - 1, size.y - 1);
			e.gc.drawFocus(0, 0, size.x - 1, size.y - 1);
		}
	}
	
	protected void doClickStart() {
		pressed = true;
		redraw();
	}

	protected void doClickStop(MouseEvent e) {
		pressed = false;
		redraw();

		// released outside button
		Point size = getSize();
		if (e.x < 0 || e.y < 0 || e.x > size.x || e.y > size.y)
			return;
		
		SelectionListener[] array = (SelectionListener[]) listeners.toArray(new SelectionListener[listeners.size()]);
		Event event = new Event();
		event.widget = this;
		SelectionEvent selEvent = new SelectionEvent(event);
		for (SelectionListener listener : array) {
			listener.widgetSelected(selEvent);
		}
		getShell().setFocus();
		//this.buttonBar.videoRenderer.setFocus();
		
	}
	
	protected void doMouseEnter() {
		setCursor(getShell().getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		isHighlighted = true;
		redraw();
	}
	
	protected void doMouseExit() {
		setCursor(null);
		isHighlighted = false;
		redraw();
	}

	protected void doMouseHover() {
		
	}
}