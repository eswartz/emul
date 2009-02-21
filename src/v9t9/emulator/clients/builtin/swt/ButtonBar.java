/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Widget;

import v9t9.emulator.clients.builtin.swt.ImageButton.ButtonParentDrawer;

class ButtonBar extends Composite implements ButtonParentDrawer {

	ButtonBarLayout layout;
	private boolean isHorizontal;
	private Composite buttonComposite;

	/**
	 * Create a button bar with the given orientation.  This must be in a parent with a GridLayout.
	 * @param parent
	 * @param style
	 * @param videoRenderer
	 */
	public ButtonBar(Composite parent, int style) {
		// the bar itself is the full width of the parent
		super(parent, style & ~(SWT.HORIZONTAL + SWT.VERTICAL) | SWT.NO_RADIO_GROUP | SWT.NO_FOCUS);
		this.isHorizontal = (style & SWT.HORIZONTAL) != 0;
		
		//setLayoutData(new GridData(isHorizontal ? SWT.FILL : SWT.CENTER, isHorizontal ? SWT.CENTER : SWT.FILL, false, false));
		setLayoutData(new GridData(isHorizontal ? SWT.FILL : SWT.CENTER, isHorizontal ? SWT.CENTER : SWT.FILL, true, true));

		// the inner composite contains the buttons, tightly packed
		buttonComposite = new Composite(this, SWT.NO_RADIO_GROUP | SWT.NO_FOCUS | SWT.NO_BACKGROUND);
		layout = new ButtonBarLayout();
		buttonComposite.setLayout(layout);
		
		// //start off with one horizontal cell; in #addedButton() the columns is increased for horizontal bars
		//layout = new GridLayout(1, true);
		//layout.marginHeight = layout.marginWidth = 0;
		//buttonComposite.setLayout(layout);
		
		buttonComposite.setLayoutData(GridDataFactory.fillDefaults()
				.align(isHorizontal ? SWT.CENTER : SWT.FILL, isHorizontal ? SWT.FILL : SWT.CENTER)
				.grab(isHorizontal, !isHorizontal)
				.create());
		
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
			if (isHorizontal) {
				//maxsize =  cursize.x * 3 / 4 / num;
				axis = cursize.x;
				//maxsize =  cursize.x / num;
				//minsize = cursize.y;
			} else {
				//maxsize = cursize.y * 3 / 4 / num;
				//maxsize = cursize.y / num;
				//minsize = cursize.x;
				axis = cursize.y;
			}
			/*
			size = Math.max(minsize, maxsize);
			if (size < 16)
				size = 16;
			else if (size > 128)
				size = 128;
			*/
			int scale = 4;
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
			return new Point(w, h);
		}

		@Override
		protected void layout(Composite composite, boolean flushCache) {
			Control[] kids = composite.getChildren();
			int num = kids.length;
			if (num == 0)
				num = 1;
			
			Point curSize = composite.getSize();
			int size;
			int x = 0, y = 0;
			if (isHorizontal) {
				size = curSize.y;
				x = (curSize.x - size * num) / 2;
			} else {
				size = curSize.x;
				y = (curSize.y - size * num) / 2;
			}
			
			for (Control kid : kids) {
				kid.setBounds(x, y, size, size);
				if (isHorizontal)
					x += size;
				else
					y += size;
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
	public void drawBackground(GC gc, ImageButton imageButton, Point offset, Point size) {
		paintButtonBar(gc, imageButton, offset, size);
	}

	/**
	 * The composite to which to add buttons.
	 * @return
	 */
	public Composite getComposite() {
		return buttonComposite;
	}

	public void addedButton() {
	}

	public void setHorizontal(boolean isHorizontal) {
		this.isHorizontal = isHorizontal;
	}

	public boolean isHorizontal() {
		return isHorizontal;
	}
}