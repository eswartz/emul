/*
  ImageBar.java

  (c) 2009-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.bars;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

import v9t9.gui.client.swt.IFocusRestorer;
import ejs.base.timer.FastTimer;
import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;


public class ImageBar extends ImageCanvas implements IImageBar {

	private static final int MIN_ICON_SIZE = 24;
	private static final int MAX_ICON_SIZE = 96;
	private static final int RETRACT_VISIBLE = 0;

	private ButtonBarLayout bblayout;
	
	private int minIconSize = MIN_ICON_SIZE;
	private int maxIconSize = MAX_ICON_SIZE;

	private boolean canRetract;
	private MouseListener retractListener;
	private MouseMoveListener retractMoveListener;
	private MouseTrackListener retractTrackListener;
	private boolean retracted;
	/** when non-null, transitioning to the !retracted state */
	private Runnable retractTask;
	
	private boolean offerRetract;
	private Runnable retractCursorTask;
	
	
	private static FastTimer retractTimer = new FastTimer("Retractor");
	
	

	private Point targetPos;
	private Point origPos;

	private int retractTarget;
	private int retractStep;
	
	private ListenerList<IPaintOffsetListener> paintOffsListenerList = new ListenerList<IImageBar.IPaintOffsetListener>();
	
	private ImageBar pairedBar;
	private boolean changingPairedBar;
	
	/**
	 * Create a button bar with the given orientation.  This must be in a parent with a GridLayout.
	 * @param parent
	 * @param style
	 * @param midPoint 
	 * @param videoRenderer
	 */
	public ImageBar(Composite parent, int style,
			Gradient gradient, IFocusRestorer focusRestorer, boolean smoothResize) {
		
		super(parent, style, gradient, focusRestorer, smoothResize);
		
		
		// the inner composite contains the buttons, tightly packed
		bblayout = new ButtonBarLayout();
		buttonComposite.setLayout(bblayout);
		
		
	}
	
	public void setPairedBar(ImageBar pairedBar) {
		this.pairedBar = pairedBar;
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
			if (true||!flushCache) {
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


	public synchronized void setRetractable(boolean retractable) {
		if (canRetract) {
			removeMouseListener(retractListener);
			removeMouseMoveListener(retractMoveListener);
			removeMouseTrackListener(retractTrackListener);
		}
		this.canRetract = retractable;

		stopRetractOffer();
		if (canRetract) {
			retractListener = new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					if (e.button == 1 && e.count == 1) {
						if (offerRetract) {
							startRetractTask();
						} else {
							startRetractOffer();
						}
					}
				}
				@Override
				public void mouseDoubleClick(MouseEvent e) {
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
					setCursor(null);
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
	


	/**
	 * 
	 */
	private void startRetractOffer() {
		if (!offerRetract) {
			offerRetract = true;
			updateCursor();
			retractCursorTask = new Runnable() {
				long timeout = System.currentTimeMillis() + getDisplay().getDoubleClickTime() * 2;
				@Override
				public void run() {
					synchronized (ImageBar.this) {
						if (System.currentTimeMillis() >= timeout) {
							getDisplay().asyncExec(new Runnable() {
								public void run() {
									stopRetractOffer();
								}
							});
						}
					}
				}
			};
			retractTimer.scheduleTask(retractCursorTask, 20);
		}
	}

	/**
	 * 
	 */
	private void stopRetractOffer() {
		if (offerRetract) {
			offerRetract = false;
			setCursor(null);
			retractTimer.cancelTask(retractCursorTask);
			retractCursorTask = null;
		}
	}
	
	private void updateCursor() {
		updateCursor(offerRetract);
	}
	private void updateCursor(boolean actionPossible) {
		if (actionPossible) {
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
		} else {
			setCursor(null);
		}
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
									
									updateCursor(retracted);
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
			
			retractTarget = 10;
			retractStep = 0;
			setRetracted(!retracted);
			stopRetractOffer();
			retractTimer.scheduleTask(retractTask, 60);
		}
		
		
		if (pairedBar != null && !changingPairedBar) {
			changingPairedBar = true;
			try {
				pairedBar.startRetractTask();
			} finally {
				changingPairedBar = false;
			}
		}
	}
	
	/**
	 * @param b
	 */
	private void setRetracted(boolean b) {
		this.retracted = b;
		for (Control kid : getChildren()) {
//			if (kid instanceof ImageBarChild)
//				((ImageBarChild) kid).setRetracted(b);
			kid.setEnabled(!b);
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
