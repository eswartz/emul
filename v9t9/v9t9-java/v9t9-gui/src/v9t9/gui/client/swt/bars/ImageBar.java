/**
 * 
 */
package v9t9.gui.client.swt.bars;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

import ejs.base.timer.FastTimer;
import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;

import v9t9.gui.client.swt.IFocusRestorer;


public class ImageBar extends Composite implements IImageBar {
	private static final Point ZERO_POINT = new Point(0, 0);
	private static final int MIN_ICON_SIZE = 24;
	private static final int MAX_ICON_SIZE = 64;
	private static final int RETRACT_VISIBLE = 0;

	private ButtonBarLayout bblayout;
	private final boolean isHorizontal;
	private Composite buttonComposite;
	private final IFocusRestorer focusRestorer;
	private final boolean smoothResize;
	private Gradient gradient;
	private Gradient farGradient;
	private int minIconSize = MIN_ICON_SIZE;
	private int maxIconSize = MAX_ICON_SIZE;

	private boolean canRetract;
	private MouseListener retractListener;
	private MouseMoveListener retractMoveListener;
	private MouseTrackListener retractTrackListener;
	private boolean retracted;
	/** when non-null, transitioning to the !retracted state */
	private Runnable retractTask;
	
	private int edging;
	
	private static FastTimer retractTimer = new FastTimer("Retractor");
	
	

	private Point targetPos;
	private Point origPos;

	private int retractTarget;
	private int retractStep;
	private int paintOffsX;
	private int paintOffsY;
	private ListenerList<IPaintOffsetListener> paintOffsListenerList = new ListenerList<IImageBar.IPaintOffsetListener>();
	
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
		this.edging = (style & SWT.LEFT + SWT.RIGHT + SWT.TOP + SWT.BOTTOM);
		
		this.gradient = gradient;
		this.farGradient = new Gradient(isHorizontal, new int[] { 0, 0 }, new float[] { 1.0f });

		GridDataFactory.swtDefaults()
			.align(isHorizontal ? SWT.FILL : SWT.CENTER, isHorizontal ? SWT.CENTER : SWT.FILL)
			.grab(isHorizontal, !isHorizontal).indent(0, 0)
			.applyTo(this);

		// the inner composite contains the buttons, tightly packed
		buttonComposite = this;
		bblayout = new ButtonBarLayout();
		buttonComposite.setLayout(bblayout);
		
		addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				paintButtonBar(e.gc, ZERO_POINT, getSize());
			}
			
		});
		
//		buttonComposite.addPaintListener(new PaintListener() {
//
//			public void paintControl(PaintEvent e) {
//				//paintButtonBar(e.gc, e.widget, new Point(0, 0), getSize());
//			}
//			
//		});
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
			
//			System.out.println("computeSize: " + prevSize);
			
			return prevSize;
		}

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
			int num = kids.length; //getIncludedKids(kids);
			
			Point curSize;
			if (!flushCache) {
				Rectangle cli = composite.getClientArea();
				curSize = new Point(cli.width,
						cli.height);
			} else {
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
						if (data.horizontalAlignment == SWT.LEFT) {
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
					} else {
						if (data.verticalAlignment == SWT.TOP) {
							kid.setBounds(x + data.horizontalIndent, min + data.verticalIndent, 
									size - data.horizontalIndent*2, size - data.verticalIndent*2);
							min += size;
						} else {
							kid.setBounds(x + data.horizontalIndent, max - size - data.verticalIndent, 
									size - data.horizontalIndent*2, size - data.verticalIndent*2);
							max -= size;
						}
					}
				}
			}
		}
		
	}

	protected void paintButtonBar(GC gc, Point offset, Point size) {
		int y = size.y;
		int x = size.x;
		if (isHorizontal) {
			y = getSize().y;
		} else {
			x = getSize().x;
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

	public boolean isHorizontal() {
		return isHorizontal;
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
	
	public synchronized void setRetractable(boolean retractable) {
		if (canRetract) {
			removeMouseListener(retractListener);
			removeMouseMoveListener(retractMoveListener);
			removeMouseTrackListener(retractTrackListener);
		}
		this.canRetract = retractable;
		
		if (canRetract) {
			retractListener = new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					if (e.button == 1) {
						startRetractTask();
					}
				}
			};
			retractMoveListener = new MouseMoveListener() {
				
				@Override
				public void mouseMove(MouseEvent e) {
					updateCursor();
				}
			};
			retractTrackListener = new MouseTrackListener() {

				
				@Override
				public void mouseEnter(MouseEvent e) {
					updateCursor();
					
					//startRetractTask();
					
					
				}


				@Override
				public void mouseExit(MouseEvent e) {
//					if (retractTask != null) {
//						if (fullBounds != null) {
//							Point realPos = ((Control) e.widget).getParent().toControl(((Control) e.widget).toDisplay(e.x, e.y));
//							if (!fullBounds.contains(realPos))
//								return;
//						}
//						retractTimer.cancelTask(retractTask);
//						
//						retractTask = null;
//						
//						setLocation(origPos);
//					}
				}

				@Override
				public void mouseHover(MouseEvent e) {
//					long now = System.currentTimeMillis();
//					long hoverTime = now - enterTime;
//					System.out.println("hover: " + hoverTime);
//					
					
					updateCursor();
				}
				
			};
			addMouseListener(retractListener);
			addMouseMoveListener(retractMoveListener);
			addMouseTrackListener(retractTrackListener);

		}
	}
	


	private void updateCursor() {
		int cursor;
		if (isHorizontal)
			if (((edging & SWT.TOP) != 0) != retracted)
				cursor = SWT.CURSOR_SIZEN;
			else //if ((edging & SWT.BOTTOM) != 0)
				cursor = SWT.CURSOR_SIZES;
		else
			if (((edging & SWT.LEFT) != 0) != retracted)
				cursor = SWT.CURSOR_SIZEW;
			else
				cursor = SWT.CURSOR_SIZEE;
		
		setCursor(getDisplay().getSystemCursor(cursor));
		
	}

	/**
	 * 
	 */
	protected synchronized void startRetractTask() {
		if (retractTask == null) {
			retractTask = new Runnable() {
				
				@Override
				public void run() {
					getDisplay().syncExec(new Runnable() {
						public void run() {
							synchronized (ImageBar.this) {
								++retractStep;
								
								int x = origPos.x + (targetPos.x - origPos.x) * retractStep / retractTarget; 
								int y = origPos.y + (targetPos.y - origPos.y) * retractStep / retractTarget;
								
								if (retractStep >= retractTarget) {
									retractTimer.cancelTask(retractTask);
									retractTask = null;
									x = targetPos.x;
									y = targetPos.y;
								}
								
								setPaintOffset(x, y);
								redrawAll();
							}
						}
					});
				}
			};
			

			//origPos = getLocation();
			if (!retracted) {
				origPos = new Point(0, 0);
				targetPos = getRetractedOffs();
			} else {
				//targetPos = getNormalPos();
				origPos = getPaintOffset();
				targetPos = new Point(0, 0);
			}
			
			retractTarget = 8;
			retractStep = 0;
			retracted = !retracted;
			retractTimer.scheduleTask(retractTask, 64);
		}
	}
	
	protected synchronized Point getRetractedPos() {
		if (!isHorizontal) {
			int sz = getSize().x;
			if ((edging & SWT.LEFT) != 0) { 
				return new Point(-sz + RETRACT_VISIBLE, getLocation().y);
			} else {
				return new Point(getParent().getSize().x - RETRACT_VISIBLE, getLocation().y);
			}
		} else {
			int sz = getSize().y;
			if ((edging & SWT.TOP) != 0) { 
				return new Point(getLocation().x, -sz + RETRACT_VISIBLE);
			} else {
				return new Point(getLocation().x, getParent().getSize().y - RETRACT_VISIBLE);
			}
		}
	}
	protected synchronized Point getRetractedOffs() {
		Point sz = getSize();
		if (!isHorizontal) {
			if ((edging & SWT.LEFT) != 0) { 
				return new Point(-sz.x + RETRACT_VISIBLE, 0);
			} else {
				return new Point(sz.x - RETRACT_VISIBLE, 0);
			}
		} else {
			if ((edging & SWT.TOP) != 0) { 
				return new Point(0, -sz.y + RETRACT_VISIBLE);
			} else {
				return new Point(0, sz.y - RETRACT_VISIBLE);
			}
		}
	}

	protected synchronized Point getNormalPos() {
		if (!isHorizontal) {
			int sz = getSize().x;
			if ((edging & SWT.LEFT) != 0) { 
				return new Point(0, getLocation().y);
			} else {
				return new Point(getParent().getSize().x - sz, getLocation().y);
			}
		} else {
			int sz = getSize().y;
			if ((edging & SWT.TOP) != 0) { 
				return new Point(getLocation().x, 0);
			} else {
				return new Point(getLocation().x, getParent().getSize().y - sz);
			}
		}
	}

	@Override
	public synchronized boolean isRetracted() {
		return retracted;
	}


	private void setPaintOffset(int x, int y) {
		paintOffsX = x;
		paintOffsY = y;
		if (!paintOffsListenerList.isEmpty()) {
			final Point po = getPaintOffset();
			paintOffsListenerList.fire(new IFire<IImageBar.IPaintOffsetListener>() {

				@Override
				public void fire(IPaintOffsetListener listener) {
					listener.offsetChanged(po);
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.bars.IImageBar#getPaintOffset()
	 */
	@Override
	public Point getPaintOffset() {
		return new Point(paintOffsX, paintOffsY);
	}


	public void addPaintOffsetListener(IPaintOffsetListener paintListener) {
		this.paintOffsListenerList.add(paintListener);
	}
}
