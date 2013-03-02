/*
  ImageCanvas.java

  (c) 2012 Edward Swartz

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
package v9t9.gui.client.swt.bars;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import v9t9.gui.client.swt.IFocusRestorer;

/**
 * @author ejs
 *
 */
public class ImageCanvas extends Canvas  implements IImageCanvas{
	protected static final Point ZERO_POINT = new Point(0, 0);
	protected Composite buttonComposite;
	protected final IFocusRestorer focusRestorer;
	protected final boolean smoothResize;
	protected Gradient gradient;
	protected Gradient farGradient;
	protected final boolean isHorizontal;

	protected int edging;
	
	protected int paintOffsX;
	protected int paintOffsY;
	/**
	 * @param parent
	 * @param style
	 */
	public ImageCanvas(Composite parent, int style,
			Gradient gradient, IFocusRestorer focusRestorer, boolean smoothResize) {
		// the bar itself is the full width of the parent
		super(parent, style & ~(SWT.HORIZONTAL + SWT.VERTICAL) | SWT.NO_RADIO_GROUP | SWT.NO_FOCUS 
				| SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);
		this.focusRestorer = focusRestorer;
		this.smoothResize = smoothResize;
		
		this.isHorizontal = (style & SWT.HORIZONTAL) != 0;

		this.edging = (style & SWT.LEFT + SWT.RIGHT + SWT.TOP + SWT.BOTTOM);
		

		this.gradient = gradient;
		this.farGradient = new Gradient(isHorizontal, new int[] { 0, 0 }, new float[] { 1.0f });

		GridDataFactory.swtDefaults()
			.align(isHorizontal ? SWT.FILL : SWT.CENTER, isHorizontal ? SWT.CENTER : SWT.FILL)
			.grab(isHorizontal, !isHorizontal).indent(0, 0)
			.applyTo(this);

		// the inner composite contains the buttons, tightly packed
		buttonComposite = this;
		

		addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				if (e.count == 0)
					paintButtonBar(e.gc, ZERO_POINT, getSize());
			}
			
		});
	}

	public boolean isHorizontal() {
		return isHorizontal;
	}
	public IFocusRestorer getFocusRestorer() {
		return focusRestorer;
	}

	protected void paintButtonBar(GC gc, Point offset, Point size) {
		int y = size.y;
		int x = size.x;
		Point mySize = getSize();
		if (isHorizontal) {
			y = mySize.y;
		} else {
			x = mySize.x;
		}
		
		gradient.draw(gc, offset.x + paintOffsX, offset.y + paintOffsY, 
				x, y); 
		
		if (isHorizontal) {
			if (paintOffsY != 0) {
				if ((edging & SWT.TOP) != 0) {
					farGradient.draw(gc, offset.x, 
							offset.y + y + paintOffsY,
							x,
							-paintOffsY);
				} else {
					farGradient.draw(gc, offset.x, 
							offset.y,
							x,
							paintOffsY);
					
				}
				
			}
		} else {
			if (paintOffsX != 0) {
				if ((edging & SWT.LEFT) != 0) {
					farGradient.draw(gc, offset.x + x + paintOffsX, 
							offset.y,
							Math.max(0, -paintOffsX),
							y);
				} else {
					farGradient.draw(gc, offset.x, 
							offset.y,
							Math.max(0, paintOffsX),
							y);
					
				}				
			}
		}
	}

	public void drawBackground(GC gc) {
		paintButtonBar(gc, ZERO_POINT, getSize());
	}

	/**
	 * The composite to which to add buttons.
	 * @return
	 */
	public Composite getComposite() {
		return buttonComposite;
	}

	public void redrawAll() {
		redrawAll(this);
	}

	private void redrawAll(Control c) {
		c.redraw();
		if (c instanceof Composite)
			for (Control control : ((Composite) c).getChildren())
				redrawAll(control);
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.bars.IImageCanvas#isRetracted()
	 */
	@Override
	public boolean isRetracted() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.bars.IImageCanvas#getPaintOffset()
	 */
	@Override
	public Point getPaintOffset() {
		return ZERO_POINT;
	}
}