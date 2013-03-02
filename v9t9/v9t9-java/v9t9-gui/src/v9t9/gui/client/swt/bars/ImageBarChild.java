/*
  ImageBarChild.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.bars;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @author ejs
 *
 */
public class ImageBarChild extends Canvas {

	protected class EventForwarder implements Listener {

		private final Control targetControl;
		public EventForwarder(Control targetControl) {
			this.targetControl = targetControl;
		}
		
		@Override
		public void handleEvent(Event e) {
			if (e.widget == targetControl)
				return;
			if (!isIconMouseable())
				forwardEvent(e);
		}

		/**
		 * @param e
		 */
		public void forwardEvent(Event e) {
			Point disp = ((Control) e.widget).toDisplay(e.x, e.y); 
			Point ctrl = targetControl.toControl(disp.x, disp.y);
			e.x = ctrl.x;
			e.y = ctrl.y;
			e.widget = targetControl;
			targetControl.notifyListeners(e.type, e);
		}
		
	}

	protected IImageCanvas parentDrawer;
	protected EventForwarder forwarder;
	
	public ImageBarChild(IImageCanvas parentDrawer, int style) {
		super(parentDrawer.getComposite(), style | SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);
		this.parentDrawer = parentDrawer;

		forwarder = new EventForwarder(parentDrawer.getComposite());
		
		addListener(SWT.MouseEnter, forwarder);
		addListener(SWT.MouseExit, forwarder);
		addListener(SWT.MouseHover, forwarder);
		addListener(SWT.MouseDown, forwarder);
		addListener(SWT.MouseUp, forwarder);
		addListener(SWT.MouseDoubleClick, forwarder);
		addListener(SWT.MouseMove, forwarder);

		addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				doPaint(e);
			}
			
		});
		
		//setCursor(getDisplay().getSystemCursor(SWT.CURSOR_HAND));
	}

	protected boolean isIconMouseable() {
		return parentDrawer == null || (false == parentDrawer instanceof IImageBar 
				|| !((IImageBar) parentDrawer).isRetracted());
	}

	protected void doPaint(PaintEvent e) {
		parentDrawer.drawBackground(e.gc);
	}
}