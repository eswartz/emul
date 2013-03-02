/*
  AwtCanvas.java

  (c) 2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
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
