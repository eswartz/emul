/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Widget;

import v9t9.emulator.clients.builtin.swt.ImageIconCanvas.IImageBar;

class ImageBar extends Composite implements IImageBar {

	ButtonBarLayout layout;
	private boolean isHorizontal;
	private Composite buttonComposite;
	private final IFocusRestorer focusRestorer;
	private final boolean smoothResize;

	/**
	 * Create a button bar with the given orientation.  This must be in a parent with a GridLayout.
	 * @param parent
	 * @param style
	 * @param videoRenderer
	 */
	public ImageBar(Composite parent, int style, IFocusRestorer focusRestorer, boolean smoothResize) {
		// the bar itself is the full width of the parent
		super(parent, style & ~(SWT.HORIZONTAL + SWT.VERTICAL) | SWT.NO_RADIO_GROUP | SWT.NO_FOCUS);
		this.focusRestorer = focusRestorer;
		this.smoothResize = smoothResize;
		this.isHorizontal = (style & SWT.HORIZONTAL) != 0;
		
		GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(this);

		GridDataFactory.swtDefaults().align(isHorizontal ? SWT.FILL : SWT.CENTER, isHorizontal ? SWT.CENTER : SWT.FILL)
			.grab(isHorizontal, !isHorizontal).indent(0, 0).applyTo(this);

		// the inner composite contains the buttons, tightly packed
		buttonComposite = new Composite(this, SWT.NO_RADIO_GROUP | SWT.NO_FOCUS | SWT.NO_BACKGROUND);
		layout = new ButtonBarLayout();
		buttonComposite.setLayout(layout);
		
		GridDataFactory.fillDefaults().grab(true, true).indent(0, 0).applyTo(buttonComposite);
		addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				paintButtonBar(e.gc, e.widget, new Point(0, 0), getSize());
			}
			
		});
		
		buttonComposite.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				paintButtonBar(e.gc, e.widget, new Point(0, 0), getSize());
			}
			
		});
	}
	
	public IFocusRestorer getFocusRestorer() {
		return focusRestorer;
	}
	class ButtonBarLayout extends Layout {

		@Override
		protected Point computeSize(Composite composite, int whint, int hhint,
				boolean flushCache) {
			int w, h ;
			Control[] kids = composite.getChildren();
			int num = kids.length;
			if (num == 0)
				num = 1;
			
			int size;
			int axis;
			Point cursize = composite.getParent().getSize();
			System.out.println("cursize: "+ cursize);
			if (isHorizontal) {
				axis = cursize.x;
				size = cursize.y;
			} else {
				axis = cursize.y;
				size = cursize.x;
			}
			//System.out.println(axis+","+whint+","+hhint);
			if (smoothResize) {
				axis = axis * 7 / 8;
				if (axis / num < size) {
					size = axis / num;
				}
				if (isHorizontal) {
					w = axis;
					h = size;
				} else {
					w = size;
					h = axis;
				}
				System.out.println("..." + w + "/" + h);
			} else {
				int scale = isHorizontal ? 4 : 3;
				while (scale < 7 && (num * (1 << (scale + 1))) < axis) {
					scale++;
				}
				size = 1 << scale;
				
				if (isHorizontal) {
					w = whint >= 0 ? whint : size * num;
					h = hhint >= 0 ? hhint : size;
				} else {
					w = whint >= 0 ? whint : size;
					h = hhint >= 0 ? hhint : size * num;
				}
			}
			
			return new Point(w, h);
		}

		@Override
		protected void layout(Composite composite, boolean flushCache) {
			Control[] kids = composite.getChildren();
			int num = kids.length;
			if (num == 0)
				num = 1;
			
			//Point curSize = composite.getSize();
			Point curSize = computeSize(composite, SWT.DEFAULT, SWT.DEFAULT, true);
			int size;
			int x = 0, y = 0;
			int axisSize;
			if (isHorizontal) {
				axisSize = curSize.y;
				if (axisSize < 24)
					size = 24;
				else if (axisSize > 64)
					size = 64;
				else
					size = axisSize;
				x = (curSize.x - size * num) / 2;
			} else {
				axisSize = curSize.x;
				if (axisSize < 24)
					size = 24;
				else if (axisSize > 64)
					size = 64;
				else
					size = axisSize;
				y = (curSize.y - size * num) / 2;
			}
			
			for (Control kid : kids) {
				if (isHorizontal) {
					kid.setBounds(x, y, size, size);
					x += size;
				} else {
					kid.setBounds(x, y, size, size);
					y += size;
				}
			}
		}
		
	}

	protected void paintButtonBar(GC gc, Widget w, Point offset,  Point size) {
		gc.setForeground(w.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		gc.setBackground(w.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		if (isHorizontal) {
			gc.fillGradientRectangle(offset.x, offset.y + size.y / 2, size.x, size.y / 2, true);
			gc.fillGradientRectangle(offset.x, offset.y + size.y / 2, size.x, -size.y / 2, true);
		} else {
			gc.fillGradientRectangle(offset.x + size.x / 2, offset.y, size.x, size.y, false);
			gc.fillGradientRectangle(offset.x + size.x / 2, offset.y, -size.x / 2, size.y, false);
			
		}
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ImageButton.ButtonParentDrawer#draw(org.eclipse.swt.graphics.GC, v9t9.emulator.clients.builtin.swt.ImageButton, org.eclipse.swt.graphics.Point, org.eclipse.swt.graphics.Point)
	 */
	public void drawBackground(GC gc, ImageIconCanvas imageButton, Point offset, Point size) {
		paintButtonBar(gc, imageButton, offset, size);
	}

	/**
	 * The composite to which to add buttons.
	 * @return
	 */
	public Composite getComposite() {
		return buttonComposite;
	}

	public void setHorizontal(boolean isHorizontal) {
		this.isHorizontal = isHorizontal;
	}

	public boolean isHorizontal() {
		return isHorizontal;
	}

	/**
	 * 
	 */
	public void redrawAll() {
		redrawAll(this);
	}

	/**
	 * @param buttonBar
	 */
	private void redrawAll(Control c) {
		c.redraw();
		if (c instanceof Composite)
			for (Control control : ((Composite) c).getChildren())
				redrawAll(control);
		
	}
}
