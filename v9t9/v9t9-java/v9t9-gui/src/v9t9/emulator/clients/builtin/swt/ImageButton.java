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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;

/**
 * A button with an image, which is allowed to be transparent.
 * @author ejs
 *
 */
public class ImageButton extends ImageIconCanvas {

	private List<SelectionListener> listeners;
	private boolean selected;
	private boolean isHighlighted;
	private boolean pressed;
	private boolean isMenuHovering;
	private Rectangle menuOverlayBounds;
	private Rectangle overlayBounds;
	protected IFocusRestorer focusRestorer;

	public ImageButton(IImageBar parentBar, int style, 
			ImageProvider imageProvider, int iconIndex, String tooltip) {
		super(parentBar, style, imageProvider, iconIndex, tooltip);
		
		this.listeners = new ArrayList<SelectionListener>();
		
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
				if (focusRestorer != null)
					focusRestorer.restoreFocus();
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

	public void setFocusRestorer(IFocusRestorer focusRestorer) {
		this.focusRestorer = focusRestorer;
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
		Rectangle drawRect = getBounds();
		this.parentDrawer.drawBackground(e.gc);
		//e.gc.setAntialias(SWT.ON);
		int offset = 0;
		if ((getStyle() & SWT.TOGGLE) != 0) {
			if (pressed && overlayBounds == null) {
				Color bg = e.gc.getBackground();
				e.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
				e.gc.fillRectangle(0, 0, drawRect.width, drawRect.height);
				e.gc.setBackground(bg);
				offset = 2;
			}
		} else {
		}
		offset = pressed ? 2 : 0;
		drawRect.x = drawRect.y = offset;
		try {
			
			imageProvider.drawImage(e.gc, drawRect, bounds);
			drawRect.x = drawRect.y = 0;
			if (overlayBounds != null)
				imageProvider.drawImage(e.gc, drawRect, overlayBounds);
			
			if (menuOverlayBounds != null && isMenuHovering) {
				imageProvider.drawImage(e.gc, drawRect, menuOverlayBounds);
			}
		} catch (IllegalArgumentException e2) {
			e2.printStackTrace();
		}
		//e.gc.setAntialias(SWT.OFF);
		if (isHighlighted) {
			e.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
			//e.gc.setLineStyle(SWT.LINE_DOT);
			//e.gc.drawRectangle(0, 0, imgRect.width - 1, imgRect.height - 1);
			e.gc.drawFocus(0, 0, drawRect.width - 1, drawRect.height - 1);
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

	/**
	 * @param e  
	 */
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