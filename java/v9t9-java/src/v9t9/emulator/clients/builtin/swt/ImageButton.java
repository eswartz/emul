/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

/**
 * A button with an image, which is allowed to be transparent.
 * @author ejs
 *
 */
public class ImageButton extends Canvas {

	public interface ButtonParentDrawer {
		Composite getComposite();
		void drawBackground(GC gc, ImageButton imageButton, Point offset, Point size);
		void addedButton();
	}
	private final Rectangle bounds;
	private Image icon;
	private Rectangle overlayBounds;
	private Rectangle menuOverlayBounds;
	private List<SelectionListener> listeners;
	private boolean selected;
	private ButtonParentDrawer parentDrawer;
	private boolean isHighlighted;
	private boolean pressed;
	private boolean isMenuHovering;
	
	public ImageButton(ButtonParentDrawer parentDrawer, int style, Image icon_, Rectangle bounds_, String tooltip) {
		super(parentDrawer.getComposite(),  style /*| SWT.NO_BACKGROUND*/);
		
		if (icon_ == null)
			throw new NullPointerException();
		
		this.parentDrawer = parentDrawer;
		parentDrawer.addedButton();
		
		this.icon = icon_;
		this.bounds = bounds_;
		this.listeners = new ArrayList<SelectionListener>();
		
		
		GridData data = new GridData(bounds.width, bounds.height);
		//data.minimumHeight = 8;	// the minimums above override this
		//data.minimumWidth = 8;	// the minimums above override this
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
		
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button == 1) {
					if (menuOverlayBounds != null && isMenuHovering) {
						Event me = new Event();
						me.button = e.button;
						me.display = e.display;
						me.item = ImageButton.this;
						me.stateMask = e.stateMask;
						me.type = SWT.MenuDetect;
						me.x = e.x;
						me.y = e.y;
						notifyListeners(SWT.MenuDetect, me);
						return;
					}
					doClickStart();
				}
			}
			@Override
			public void mouseUp(MouseEvent e) {
				if (e.button == 1)
					doClickStop(e);
			}
		});
		
		addMouseTrackListener(new MouseTrackListener() {

			public void mouseEnter(MouseEvent e) {
				doMouseEnter(e);
			}

			public void mouseExit(MouseEvent e) {
				doMouseExit(e);
			}

			public void mouseHover(MouseEvent e) {
				doMouseHover(e);
			}
			
		});
		
		addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(MouseEvent e) {
				doMouseMove(e);
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

	public void setMenuOverlayBounds(Rectangle menuOverlayBounds) {
		this.menuOverlayBounds = menuOverlayBounds;
	}
	public void addSelectionListener(SelectionListener listener) {
		listeners.add(listener);
	}

	public boolean getSelection() {
		if ((getStyle() & SWT.PUSH) != 0)
			return selected;
		else
			return pressed;
	}

	public void setSelection(boolean flag) {
		if (flag != selected) {
			this.selected = flag;
			redraw();
		}
	}

	protected void doPaint(PaintEvent e) {
		Point size = getSize();
		this.parentDrawer.drawBackground(e.gc, this, new Point(0, 0), size);
		//e.gc.setAntialias(SWT.ON);
		int offset = 0;
		if ((getStyle() & SWT.TOGGLE) != 0) {
			if (pressed && overlayBounds == null) {
				Color bg = e.gc.getBackground();
				e.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
				e.gc.fillRectangle(0, 0, size.x, size.y);
				e.gc.setBackground(bg);
				offset = 2;
			}
		} else {
		}
		offset = pressed ? 2 : 0;
		try {
			e.gc.drawImage(icon, bounds.x, bounds.y, bounds.width, bounds.height, 
					offset, offset, size.x, size.y);
			if (overlayBounds != null)
				e.gc.drawImage(icon, overlayBounds.x, overlayBounds.y, overlayBounds.width, overlayBounds.height, 
						0, 0, size.y, size.y);
			
			if (menuOverlayBounds != null && isMenuHovering) {
				e.gc.drawImage(icon, menuOverlayBounds.x, menuOverlayBounds.y, menuOverlayBounds.width, menuOverlayBounds.height, 
						0, 0, size.y, size.y);
			}
		} catch (IllegalArgumentException e2) {
			e2.printStackTrace();
		}
		//e.gc.setAntialias(SWT.OFF);
		if (isHighlighted) {
			e.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
			//e.gc.setLineStyle(SWT.LINE_DOT);
			//e.gc.drawRectangle(0, 0, size.x - 1, size.y - 1);
			e.gc.drawFocus(0, 0, size.x - 1, size.y - 1);
		}
	}
	
	protected void doClickStart() {
		if ((getStyle() & SWT.PUSH) != 0)
			pressed = true;
		redraw();
	}

	protected void doClickStop(MouseEvent e) {
		if ((getStyle() & SWT.PUSH) != 0)
			pressed = false;
		else
			pressed = !pressed;
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
	
	protected void doMouseEnter(MouseEvent e) {
		setCursor(getShell().getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		isHighlighted = true;

		checkMenu(e);
		
		redraw();
	}
	
	private void checkMenu(MouseEvent e) {
		if (menuOverlayBounds != null) {
			Point corner = getSize();
			corner.x -= corner.x / 3;
			corner.y -= corner.y / 3;
			if (e.x >= corner.x
					&& e.y >= corner.y) {
				if (!isMenuHovering) {
					isMenuHovering = true;
					redraw();
				}
				return;
			}
			if (isMenuHovering) {
				isMenuHovering = false;
				redraw();
			}
		}
		
	}

	protected void doMouseExit(MouseEvent e) {
		setCursor(null);
		isHighlighted = false;
		isMenuHovering = false;
		redraw();
	}

	protected void doMouseHover(MouseEvent e) {
		checkMenu(e);
	}
	protected void doMouseMove(MouseEvent e) {
		checkMenu(e);
	}
}