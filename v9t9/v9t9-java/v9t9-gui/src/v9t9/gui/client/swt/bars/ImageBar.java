/**
 * 
 */
package v9t9.gui.client.swt.bars;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

import v9t9.gui.client.swt.IFocusRestorer;


public class ImageBar extends Composite implements IImageBar {
	private static final Point ZERO_POINT = new Point(0, 0);
	private static final int MIN_ICON_SIZE = 24;
	private static final int MAX_ICON_SIZE = 64;

	private ButtonBarLayout bblayout;
	private final boolean isHorizontal;
	private Composite buttonComposite;
	private final IFocusRestorer focusRestorer;
	private final boolean smoothResize;
	private Gradient gradient;
	private int minIconSize = MIN_ICON_SIZE;
	private int maxIconSize = MAX_ICON_SIZE;

	/**
	 * Create a button bar with the given orientation.  This must be in a parent with a GridLayout.
	 * @param parent
	 * @param style
	 * @param midPoint 
	 * @param videoRenderer
	 */
	public ImageBar(Composite parent, int style,
			Gradient gradient, IFocusRestorer focusRestorer, boolean smoothResize) {
		// the bar itself is the full width of the parent
		super(parent, style & ~(SWT.HORIZONTAL + SWT.VERTICAL) | SWT.NO_RADIO_GROUP | SWT.NO_FOCUS | SWT.NO_BACKGROUND);
		this.focusRestorer = focusRestorer;
		this.smoothResize = smoothResize;
		this.isHorizontal = (style & SWT.HORIZONTAL) != 0;

		//topLeftColor = getDisplay().getSystemColor(colors[0]);
		//centerColor = getDisplay().getSystemColor(colors[1]);
		//bottomRightColor = getDisplay().getSystemColor(colors[2]);
		//this.midPoint = midPoint;
		
		this.gradient = gradient;

		//GridLayoutFactory.swtDefaults().margins(0, 0).applyTo(this);

		GridDataFactory.swtDefaults()
			.align(isHorizontal ? SWT.FILL : SWT.CENTER, isHorizontal ? SWT.CENTER : SWT.FILL)
			.grab(isHorizontal, !isHorizontal).indent(0, 0)
			.applyTo(this);

		// the inner composite contains the buttons, tightly packed
		buttonComposite = this;
		//buttonComposite = new Composite(this, SWT.NO_RADIO_GROUP | SWT.NO_FOCUS | SWT.NO_BACKGROUND);
		bblayout = new ButtonBarLayout();
		buttonComposite.setLayout(bblayout);
		
		/*
		GridDataFactory.swtDefaults()
			.align(isHorizontal ? SWT.FILL : SWT.CENTER, isHorizontal ? SWT.CENTER : SWT.FILL)
			.grab(isHorizontal, !isHorizontal).indent(0, 0).applyTo(buttonComposite);
		*/
		addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				paintButtonBar(e.gc, ZERO_POINT, getSize());
			}
			
		});
		
		buttonComposite.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				//paintButtonBar(e.gc, e.widget, new Point(0, 0), getSize());
			}
			
		});
	}
	
	
	public int getMinIconSize() {
		return minIconSize;
	}


	public void setMinIconSize(int minIconSize) {
		this.minIconSize = minIconSize;
	}


	public int getMaxIconSize() {
		return maxIconSize;
	}


	public void setMaxIconSize(int maxIconSize) {
		this.maxIconSize = maxIconSize;
	}


	public IFocusRestorer getFocusRestorer() {
		return focusRestorer;
	}
	class ButtonBarLayout extends Layout {

		private Point prevSize;

		@Override
		protected Point computeSize(Composite composite, int whint, int hhint,
				boolean flushCache) {
			int w, h;
			Control[] kids = composite.getChildren();
			int num = getIncludedKids(kids);
			
			int size;
			int axis;
			Point cursize = composite.getParent().getSize();
			if (isHorizontal) {
				axis = cursize.x;
				size = hhint != SWT.DEFAULT ? Math.min(hhint, cursize.y) : cursize.y;
			} else {
				axis = cursize.y;
				size = whint != SWT.DEFAULT ? Math.min(whint, cursize.x) : cursize.x;
			}
			if (smoothResize) {
				axis = axis * 7 / 8;
				size = axis / num;
				if (isHorizontal) {
					w = axis;
					h = Math.min(maxIconSize, Math.max(minIconSize, size));
				} else {
					w = Math.min(maxIconSize, Math.max(minIconSize, size));
					h = axis;
				}
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

			prevSize = new Point(w, h);
			
			if (isHorizontal)
				((GridData) ImageBar.this.getLayoutData()).heightHint = h;
			else
				((GridData) ImageBar.this.getLayoutData()).widthHint = w; 
			
			return prevSize;
		}

		/**
		 * @param kids
		 * @return
		 */
		private int getIncludedKids(Control[] kids) {
			int cnt = 0;
			for (Control kid : kids) {
				if (kid.getLayoutData() instanceof GridData) {
					if (!((GridData) kid.getLayoutData()).exclude) {
						cnt++;
					}
				}
			}
			return cnt;
		}

		@Override
		protected void layout(Composite composite, boolean flushCache) {
			Control[] kids = composite.getChildren();
			int num = getIncludedKids(kids);
			
			Point curSize;
			if (!flushCache) {
				Rectangle cli = composite.getClientArea();
				curSize = new Point(cli.width,
						cli.height);
			} else {
			//Point curSize = composite.getSize();
				curSize = computeSize(composite, SWT.DEFAULT, SWT.DEFAULT, flushCache);
			}
			int size;
			int x = 0, y = 0;
			int axisSize;
			
			if (isHorizontal) {
				axisSize = Math.min(curSize.y, curSize.x / num);
				if (axisSize < minIconSize)
					size = minIconSize;
				else if (axisSize > maxIconSize)
					size = maxIconSize;
				else
					size = axisSize;
				x = (curSize.x - size * num) / 2;
			} else {
				axisSize = Math.min(curSize.x, curSize.y / num);
				if (axisSize < minIconSize)
					size = minIconSize;
				else if (axisSize > maxIconSize)
					size = maxIconSize;
				else
					size = axisSize;
				y = (curSize.y - size * num) / 2;
			}
			
			int min = 0, max = isHorizontal ? curSize.x : curSize.y;
			
			GridData empty = GridDataFactory.fillDefaults().create();
			for (Control kid : kids) {
				GridData data = empty;
				if (kid.getLayoutData() instanceof GridData) {
					data = (GridData) kid.getLayoutData();
				}
				if (isHorizontal) {
					if (!data.exclude) {
						kid.setBounds(x + data.horizontalIndent, y + data.verticalIndent,
								size - data.horizontalIndent*2, size - data.verticalIndent*2);
						x += size;
					} else {
						if (data.horizontalAlignment == SWT.LEAD) {
							kid.setBounds(min + data.horizontalIndent, y + data.verticalIndent, 
									size - data.horizontalIndent*2, size - data.verticalIndent*2);
							min += size;
						} else {
							kid.setBounds(max - size - data.horizontalIndent, y + data.verticalIndent, 
									size - data.horizontalIndent*2, size - data.verticalIndent*2);
							max -= size;
						}
					}
				} else {
					if (!data.exclude) {
						kid.setBounds(x + data.horizontalIndent, y + data.verticalIndent, 
								size - data.horizontalIndent*2, size - data.verticalIndent*2);
						y += size;
					}
				}
			}
			
			//ImageBar.this.getParent().layout(new Control[] { ImageBar.this });
		}
		
	}

	protected void paintButtonBar(GC gc, Point offset, Point size) {
		
		/*
		gc.setForeground(centerColor);
		int y = size.y;
		int x = size.x;
		if (isHorizontal) {
			y = getSize().y;
			gc.setBackground(topLeftColor);
			gc.fillGradientRectangle(offset.x, offset.y, 
					x, (int) ((y + 1) * midPoint), true);
			gc.setBackground(centerColor);
			gc.setForeground(bottomRightColor);
			gc.fillGradientRectangle(offset.x, (int) (offset.y + y), 
					x, (int) ((y + 1) * (midPoint - 1)), true);
		} else {
			x = getSize().x;
			gc.setForeground(topLeftColor);
			gc.fillGradientRectangle(offset.x, offset.y, 
					(int) ((x + 1) * midPoint), y, false);
			gc.setBackground(centerColor);
			gc.setForeground(bottomRightColor);
			gc.fillGradientRectangle((int) (offset.x + x ), offset.y, 
					(int) ((x + 1) * (midPoint - 1)), y, false);
			
		}
		*/
		int y = size.y;
		int x = size.x;
		if (isHorizontal) {
			y = getSize().y;
		} else {
			x = getSize().x;
		}
		gradient.draw(gc, offset.x, offset.y, x, y);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ImageButton.ButtonParentDrawer#draw(org.eclipse.swt.graphics.GC, v9t9.emulator.clients.builtin.swt.ImageButton, org.eclipse.swt.graphics.Point, org.eclipse.swt.graphics.Point)
	 */
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
