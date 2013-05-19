/*
  ImageButton.java

  (c) 2009-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.bars;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
import org.eclipse.swt.widgets.Listener;

import v9t9.gui.client.swt.IFocusRestorer;

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
//	private boolean isMenuHovering;
//	private Rectangle menuOverlayBounds;
	protected IFocusRestorer focusRestorer;
	private List<Rectangle> imageOverlays = new LinkedList<Rectangle>();
	private String baseTooltip;
	private List<IImageButtonAreaHandler> areaHandlers = new ArrayList<IImageButtonAreaHandler>();
	private List<IImageButtonAreaHandler> mouseDownActiveHandlers = new ArrayList<IImageButtonAreaHandler>();
	private IImageButtonAreaHandler lastMousedAreaHandler = null;

	public ImageButton(IImageCanvas parentBar, int style, 
			IImageProvider imageProvider, int iconIndex, String tooltip) {
		super(parentBar, style, imageProvider, iconIndex, tooltip);
		
		baseTooltip = tooltip;
		this.listeners = new ArrayList<SelectionListener>();
		
		addListener(SWT.MouseDown, new Listener() {
			
			@Override
			public void handleEvent(Event e) {
				if (parentDrawer.isRetracted()) {
					e.doit = false;
					return;
				}
				
				boolean wasHandled = false;
				for (IImageButtonAreaHandler handler : areaHandlers) {
					if (handler.isInBounds(e.x, e.y, getSize()) && handler.isActive()) {
						if (e.button == 1 && handler.isMenu()) {
							Event me = new Event();
							me.button = e.button;
							me.display = e.display;
							me.item = ImageButton.this;
							me.stateMask = e.stateMask;
							me.type = SWT.MenuDetect;
							me.widget = ImageButton.this;
							me.x = e.x;
							me.y = e.y;
							notifyListeners(SWT.MenuDetect, me);
							e.doit = false;
							return;
						}
						wasHandled = handler.mouseDown(e.button);
						if (wasHandled) {
							if (!mouseDownActiveHandlers.contains(handler))
								mouseDownActiveHandlers.add(handler);
							e.doit = false;
							break;
						}
					}
				}
				if (!wasHandled && e.button == 1) {
					doClickStart();
				}
			}
		});
		addListener(SWT.MouseUp, new Listener() {
			
			@Override
			public void handleEvent(Event e) {
				if (parentDrawer.isRetracted()) {
					e.doit = false;
					return;
				}
				boolean wasHandled = false;
				for (IImageButtonAreaHandler handler : areaHandlers) {
					if (handler.isInBounds(e.x, e.y, getSize()) && 
							mouseDownActiveHandlers.contains(handler)) {
						mouseDownActiveHandlers.remove(handler);
						wasHandled = handler.mouseUp(e.button);
						if (wasHandled) {
							e.doit = false;
							break;
						}
					}
				}
				if (!wasHandled && e.button == 1) {
					doClickStop(e);
				}
				if (focusRestorer != null)
					focusRestorer.restoreFocus();
		
			}
		});
		
		addMouseTrackListener(new MouseTrackListener() {

			public void mouseEnter(MouseEvent e) {
				if (parentDrawer.isRetracted()) 
					return;
				doMouseEnter(e);
			}

			public void mouseExit(MouseEvent e) {
				if (parentDrawer.isRetracted()) 
					return;
				doMouseExit(e);
			}

			public void mouseHover(MouseEvent e) {
				if (parentDrawer.isRetracted()) 
					return;
				doMouseHover(e);
			}
			
		});
		
		addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(MouseEvent e) {
				if (parentDrawer.isRetracted()) 
					return;
				doMouseMove(e);
			}
			
		});
		
		//setRetracted(false);
		setCursor(getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				for (IImageButtonAreaHandler handler : areaHandlers) {
					handler.detach(ImageButton.this);
				}
				areaHandlers.clear();
			}
		});
	}
	
	
	/**
	 * @return the imageOverlays
	 */
	public List<Rectangle> getImageOverlays() {
		return imageOverlays;
	}
	public void setOverlayBounds(Rectangle overlayBounds) {
		imageOverlays.clear();
		if (overlayBounds != null)
			imageOverlays.add(overlayBounds); 
	}

	public void addImageOverlay(Rectangle overlayBounds) {
		if (overlayBounds == null || overlayBounds.isEmpty())
			return;
		if (!imageOverlays.contains(overlayBounds)) {
			imageOverlays.add(overlayBounds);
			redraw();
		}
	}

	public void removeImageOverlay(Rectangle overlayBounds) {
		if (overlayBounds == null || overlayBounds.isEmpty())
			return;
		imageOverlays.remove(overlayBounds);
		redraw();
	}
	
	
	public void clearImageOverlays() {
		imageOverlays.clear();
	}
	
	public void setFocusRestorer(IFocusRestorer focusRestorer) {
		this.focusRestorer = focusRestorer;
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

	protected void updateDrawRect(Rectangle drawRect) {
		int offset = 0;
		if ((getStyle() & SWT.TOGGLE) != 0) {
			if (pressed && imageOverlays.isEmpty()) {
				offset = 2;
			}
		} else {
		}
		offset = pressed ? 2 : 0;
		drawRect.x += offset;
		drawRect.y += offset;
	}
	/**
	 * @param e
	 */
	@Override
	protected void drawImage(PaintEvent e) {
		Rectangle drawRect = getBounds();
		Point po = parentDrawer.getPaintOffset();
		
		//e.gc.setAntialias(SWT.ON);
		drawRect.x = po.x;
		drawRect.y = po.y;

		if ((getStyle() & SWT.TOGGLE) != 0) {
			if (pressed && imageOverlays.isEmpty()) {
				Color bg = e.gc.getBackground();
				e.gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
				e.gc.fillRectangle(po.x, po.y, drawRect.width, drawRect.height);
				e.gc.setBackground(bg);
			}
		}
		
		updateDrawRect(drawRect);
		try {
			if (bounds.width > 0) {
				imageProvider.drawImage(e.gc, 255 - invAlpha, drawRect, bounds);
			}
			drawRect.x = po.x;
			drawRect.y = po.y;
			
			for (Rectangle imgRect : imageOverlays) {
				imageProvider.drawImage(e.gc, 255 - invAlpha, drawRect, imgRect);
			}
//			if (menuOverlayBounds != null && isMenuHovering) {
//				imageProvider.drawImage(e.gc, drawRect, menuOverlayBounds);
//			}
		} catch (IllegalArgumentException e2) {
			e2.printStackTrace();
		}
		//e.gc.setAntialias(SWT.OFF);
		if (isHighlighted) {
			e.gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
			//e.gc.setLineStyle(SWT.LINE_DOT);
			//e.gc.drawRectangle(0, 0, imgRect.width - 1, imgRect.height - 1);
			e.gc.drawFocus(po.x, po.y, drawRect.width - 1, drawRect.height - 1);
		}
	}
	
	protected void doClickStart() {
		if ((getStyle() & SWT.PUSH) != 0)
			pressed = true;
		redraw();
	}

	protected void doClickStop(Event e) {
		if ((getStyle() & SWT.PUSH) != 0)
			pressed = false;
		else if ((getStyle() & SWT.TOGGLE) != 0)
			pressed = !pressed;
		redraw();

		// released outside button
		Point size = getSize();
		if (e.x < 0 || e.y < 0 || e.x > size.x || e.y > size.y)
			return;
		
		SelectionListener[] array = (SelectionListener[]) listeners.toArray(new SelectionListener[listeners.size()]);
		Event event = new Event();
		event.widget = this;
		event.x = e.x;
		event.y = e.y;
		SelectionEvent selEvent = new SelectionEvent(event);
		for (SelectionListener listener : array) {
			listener.widgetSelected(selEvent);
		}
		
	}
	
	protected void doMouseEnter(MouseEvent e) {
		if (parentDrawer.isRetracted())
			return;
		
		isHighlighted = true;
		setToolTipText(baseTooltip);
		
		for (IImageButtonAreaHandler handler : areaHandlers) {
			if (handler.isInBounds(e.x, e.y, getSize()) && handler.isActive()) {
				handler.mouseEnter();
				String tt = handler.getTooltip();
				if (tt != null) {
					setToolTipText(tt);
				}
				
				setMousedArea(handler);
				return;
			}
		}
		resetMousedArea();
	}
	/**
	 * @param handler
	 */
	private void setMousedArea(IImageButtonAreaHandler handler) {
		if (handler != lastMousedAreaHandler) {
			if (lastMousedAreaHandler != null) {
				lastMousedAreaHandler.mouseExit();
			}
			lastMousedAreaHandler = handler;
			handler.mouseEnter();
			String tt = handler.getTooltip();
			if (tt != null)
				setToolTipText(tt);
			redraw();
		}
	}


	protected void doMouseHover(MouseEvent e) {
		for (IImageButtonAreaHandler handler : areaHandlers) {
			if (handler.isInBounds(e.x, e.y, getSize()) && handler.isActive()) {
				setMousedArea(handler);
				handler.mouseHover();
				return;
			}
		}
		resetMousedArea();
	}
	protected void doMouseMove(MouseEvent e) {
		for (IImageButtonAreaHandler handler : areaHandlers) {
			if (handler.isInBounds(e.x, e.y, getSize()) && handler.isActive()) {
				setMousedArea(handler);
				return;
			}
		}
		resetMousedArea();
	}

	/**
	 * 
	 */
	private void resetMousedArea() {
		if (lastMousedAreaHandler != null)
			lastMousedAreaHandler.mouseExit();
		lastMousedAreaHandler = null;
		setToolTipText(baseTooltip);
		redraw();
		
	}


	/**
	 * @param e  
	 */
	protected void doMouseExit(MouseEvent e) {
		if (parentDrawer.isRetracted())
			return;
		isHighlighted = false;
		redraw();
		
		for (IImageButtonAreaHandler handler : areaHandlers) {
			if (handler.isInBounds(e.x, e.y, getSize()) && handler.isActive()) {
				handler.mouseExit();
				String tt = handler.getTooltip();
				if (tt != null)
					setToolTipText(baseTooltip);
				break;
			}
		}
		resetMousedArea();
	}

	public void addAreaHandler(IImageButtonAreaHandler handler) {
		if (!areaHandlers.contains(handler)) {
			areaHandlers.add(handler);
			handler.attach(this);
		}
	}

//	private void checkMenu(MouseEvent e) {
//		for (IImageButtonAreaHandler handler : areaHandlers) {
//			if (handler.getBounds(getSize()).contains(e.x, e.y) && handler.isActive()) {
//				handler.mouseEnter();
//			} else {
//					handler.mouseExit();
//				}
//			}
//		}
//		
//		if (menuOverlayBounds != null) {
//			boolean isOverMenu = isEventOverMenu(e); 
//				
//			if (isOverMenu) {
//				if (!isMenuHovering) {
//					isMenuHovering = true;
//					redraw();
//				}
//				return;
//			}
//			if (isMenuHovering) {
//				isMenuHovering = false;
//				redraw();
//			}
//		}
//		
//	}

	/**
	 * @param e
	 * @return
	 */
//	protected boolean isEventOverMenu(MouseEvent e) {
//		boolean isOverMenu;
//		Point corner = getSize();
//		corner.x -= corner.x / 3;
//		corner.y -= corner.y / 3;
//		isOverMenu = (e.x >= corner.x
//				&& e.y >= corner.y);
//		return isOverMenu;
//	}

}