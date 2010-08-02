/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
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

class BasicButtonSVG extends Canvas {

	private final Rectangle bounds;
	private SVGLoader icon;
	private Rectangle overlayBounds;
	private List<SelectionListener> listeners;
	private boolean selected;
	private ButtonBar buttonBar;
	private boolean isHighlighted;
	private Image overlayImage;
	private Image image;
	
	public BasicButtonSVG(ButtonBar buttonBar, SVGLoader icon_, Rectangle bounds_, String tooltip) {
		super(buttonBar.getComposite(), SWT.NO_FOCUS | SWT.NO_RADIO_GROUP /*| SWT.NO_BACKGROUND*/);
		
		this.buttonBar = buttonBar;
		buttonBar.addedButton();
		
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
			public void mouseUp(MouseEvent e) {
				doClick();
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
	
	@Override
	public void dispose() {
		super.dispose();
		if (image != null) {
			image.dispose();
			image = null;
		}
		if (overlayImage != null) {
			overlayImage.dispose();
			overlayImage = null;
		}
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

	protected void doPaint(PaintEvent e) {
		Point size = getSize();
		this.buttonBar.paintButtonBar(e.gc, this, new Point(0, 0), size);
		
		e.gc.drawImage(getImage(), 0, 0);
		if (overlayBounds != null)
			e.gc.drawImage(getOverlayImage(), 0, 0);
		if (isHighlighted) {
			e.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
			e.gc.setLineStyle(SWT.LINE_DOT);
			e.gc.drawRectangle(0, 0, size.x - 1, size.y - 1);
		}
	}

	private synchronized Image getOverlayImage() {
		Rectangle bounds = new Rectangle(0, 0, getSize().x, getSize().y);
		if (overlayImage == null || !overlayImage.getBounds().equals(bounds)) {
			if (overlayImage != null)
				overlayImage.dispose();
			try {
				overlayImage = new Image(getDisplay(), icon.getImageData(overlayBounds, getSize()));
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return overlayImage;
	}

	private synchronized Image getImage() {
		Rectangle buttonbounds = new Rectangle(0, 0, getSize().x, getSize().y);
		if (image == null || !image.getBounds().equals(buttonbounds)) {
			if (image != null)
				image.dispose();
			try {
				image = new Image(getDisplay(), icon.getImageData(bounds, getSize()));
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return image;
	}

	protected void doClick() {
		SelectionListener[] array = (SelectionListener[]) listeners.toArray(new SelectionListener[listeners.size()]);
		Event event = new Event();
		event.widget = this;
		SelectionEvent selEvent = new SelectionEvent(event);
		for (SelectionListener listener : array) {
			listener.widgetSelected(selEvent);
		}
		getShell().setFocus();
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