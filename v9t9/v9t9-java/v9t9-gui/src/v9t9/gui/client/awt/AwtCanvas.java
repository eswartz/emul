/*
  AwtCanvas.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.awt;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Rectangle;


/**
 * @author ejs
 * 
 */
public class AwtCanvas extends Canvas {
	/**
	 * @param config
	 */
	public AwtCanvas(AwtVideoRenderer renderer) {
		super();
		this.renderer = renderer;

		new AwtDragDropHandler(this, renderer);
	}

	private static final long serialVersionUID = 8795221581767897631L;

	private AwtVideoRenderer renderer;

	@Override
	public void paint(Graphics g) {
		Rectangle clipRect = g.getClipBounds();
		// System.out.println("Clippy rect: " + clipRect);
		renderer.doRedraw(g, clipRect.x, clipRect.y, clipRect.width,
				clipRect.height);

	}

	@Override
	public void update(Graphics g) {
		// do not clear background
		paint(g);
	}
}
