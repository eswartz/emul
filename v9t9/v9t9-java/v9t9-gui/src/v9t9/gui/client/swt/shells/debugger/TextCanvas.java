/*
  TextCanvas.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.debugger;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @author ejs
 *
 */
public class TextCanvas extends Canvas {

	private Point charSize;
	private int visRows; // visCols;
	private List<String> lines = new ArrayList<String>();
	private boolean dirty;
	
	/**
	 * @param parent
	 * @param style
	 */
	public TextCanvas(Composite parent, int style) {
		super(parent, style);
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		charSize = new Point(8, 8);
		//setFont(new Font(getDisplay(), "Mono", 10, SWT.NORMAL));
		setFont(JFaceResources.getTextFont());
		updateFont();
		
		addListener(SWT.Paint, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				int c = event.x / charSize.x;
				int r = event.y / charSize.y;
				int w = (event.width + event.x % charSize.x) / charSize.x; 
				int h = (event.height + event.y % charSize.y) / charSize.y;
				
				redrawMatrix(event.gc, c * charSize.x, r * charSize.y, c, r, w, h);
				synchronized (this) {
					dirty = false;
				}
			}
		});
		
		addListener(SWT.Skin, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				updateFont();
				redraw();
			}
		});
		
		addListener(SWT.Resize, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				updateBounds();
			}
		});
	}
	
	/**
	 * 
	 */
	protected void updateBounds() {
//		visCols = getSize().x / charSize.x;
		visRows = getSize().y / charSize.y;
	}

	public synchronized void addLine(String line) {
		if (lines.size() > 10000)
			lines = lines.subList(lines.size() - 5000, lines.size());
		lines.add(line);
		dirty = true;
	}
	
	/**
	 * @return the dirty
	 */
	public synchronized boolean isDirty() {
		return dirty;
	}
	/**
	 * @param r
	 * @param c
	 * @param w
	 * @param h
	 */
	protected void redrawMatrix(GC gc, int pc, int pr, int c_, int r_, int w_, int h_) {
		int r1;
		int r2;
		String[] drawLines;
		synchronized (this) {
			r1 = lines.size() - h_;
			r2 = lines.size();
			if (r1 < 0)
				r1 = 0;
			drawLines = lines.subList(r1, r2).toArray(new String[r2 - r1]);
		}
		gc.setTextAntialias(SWT.OFF);
		for (int r = r1; r < r2; r++) {
			String line = drawLines[r - r1];
			if (c_ >= line.length())
				continue;
			String seg = line.substring(c_, Math.min(line.length(), c_ + w_));
			gc.drawString(seg, pc, pr);
			pr += charSize.y;
		}
	}
	/**
	 * 
	 */
	private void updateFont() {
		GC gc = new GC(this);
		FontMetrics fontMetrics = gc.getFontMetrics();
		charSize = new Point(fontMetrics.getAverageCharWidth(), fontMetrics.getHeight());
		gc.dispose();
		
	}

	/**
	 * 
	 */
	public void clear() {
		synchronized (this) {
			lines.clear();
			dirty = true;
		}
		
	}

	/**
	 * @return
	 */
	public int getRows() {
		return visRows;
	}

	
}
