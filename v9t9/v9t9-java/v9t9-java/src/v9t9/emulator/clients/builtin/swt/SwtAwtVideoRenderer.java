/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import v9t9.emulator.clients.builtin.awt.AwtVideoRenderer;
import v9t9.emulator.common.Machine;

/**
 * AWT blitting is much faster than SWT's. 
 * Host the V9t9 screen inside an AWT control inside a SWT_AWT control inside an SWT canvas.
 * @author ejs
 *
 */
public class SwtAwtVideoRenderer extends AwtVideoRenderer implements ISwtVideoRenderer {

	private Frame frame;
	private Shell shell;
	private Canvas awtContainer;

	private List<org.eclipse.swt.events.MouseListener> mouseListeners = new ArrayList<org.eclipse.swt.events.MouseListener>();
	private List<org.eclipse.swt.events.MouseMoveListener> mouseMoveListeners = new ArrayList<org.eclipse.swt.events.MouseMoveListener>();
	private FixedAspectLayout fixedAspectLayout;
	
	protected IndicatorCanvas indicatorCanvas;
	
	public SwtAwtVideoRenderer(Machine machine) {
		super(machine.getVdp());
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ISwtVideoRenderer#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent, int flags) {
		shell = parent.getShell();
		
		awtContainer = new Canvas(parent, flags | SWT.EMBEDDED | SWT.NO_MERGE_PAINTS | SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE);
		frame = SWT_AWT.new_Frame(awtContainer);
		frame.add(getAwtCanvas());

		Panel panel = new Panel();
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(getAwtCanvas(), BorderLayout.CENTER);
		
		frame.add(panel);
		
		frame.createBufferStrategy(1);
		frame.setFocusTraversalKeysEnabled(false);
		frame.setIgnoreRepaint(true);
		
		frame.setFocusable(true);

		MouseListener l = new MouseListener() {
			public void mousePressed(MouseEvent e) {
				convertMouseEvent(SWT.MouseDown, e);
			}

			public void mouseReleased(MouseEvent e) {
				convertMouseMoveEvent(SWT.MouseUp, e);
				convertMouseEvent(SWT.MouseUp, e);
				
			}
			public void mouseEntered(MouseEvent e) {
				//convertMouseEvent(SWT.MouseEnter, e);
			}

			public void mouseExited(MouseEvent e) {
				///convertMouseMoveEvent(SWT.MouseUp, e);
			}

			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) 
					convertMouseEvent(SWT.MouseDoubleClick, e);
				else {
					//convertMouseEvent(SWT.MouseDown, e);
					//convertMouseEvent(SWT.MouseUp, e);
				}
			}
			
			
		};
		getAwtCanvas().addMouseListener(l);
		
		MouseMotionListener ml = new MouseMotionListener() {
			/* (non-Javadoc)
			 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
			 */
			public void mouseMoved(MouseEvent e) {
				convertMouseMoveEvent(SWT.MouseMove, e);
			}
			/* (non-Javadoc)
			 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
			 */
			public void mouseDragged(MouseEvent e) {
				convertMouseMoveEvent(SWT.MouseMove, e);
			}
		};
		
		getAwtCanvas().addMouseMotionListener(ml);
		
		fixedAspectLayout = new FixedAspectLayout(256, 192, 3.0, 3.0, 1., 5);
		awtContainer.setLayout(fixedAspectLayout);
		
		awtContainer.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Point size = ((Control)e.widget).getSize();
				System.out.println("Control resized to: " + size.x + "/" + size.y);

				updateWidgetOnResize(size.x, size.y);
			}
		});
		
		return awtContainer;
	}
	
	public Control getControl() {
		return awtContainer;
	}
	public void addMouseEventListener(
			org.eclipse.swt.events.MouseListener listener) {
		if (!mouseListeners.contains(listener))
			mouseListeners.add(listener);		
	}
	public void addMouseMotionListener(
			org.eclipse.swt.events.MouseMoveListener listener) {
		if (!mouseMoveListeners.contains(listener))
			mouseMoveListeners.add(listener);		
	}
	protected void convertMouseEvent(int type, MouseEvent e) {
		if (shell.isDisposed())
			return;
		
		//System.out.println("Converting " + e);
		e.consume();
		
		final Event event = new Event(); 
		event.type = type;
		if (e.getButton() != MouseEvent.NOBUTTON) {
			event.button = e.getButton() - MouseEvent.BUTTON1 + 1;
		} else {
			int mask = e.getModifiersEx();
			if ((mask & MouseEvent.BUTTON1_DOWN_MASK) != 0)
				event.button = 1;
			else if ((mask & MouseEvent.BUTTON2_DOWN_MASK) != 0)
				event.button = 2;
			else if ((mask & MouseEvent.BUTTON3_DOWN_MASK) != 0)
				event.button = 3;
		}
		event.x = e.getX();
		event.y = e.getY();
		event.item = shell;
		event.widget = shell;
		
		final org.eclipse.swt.events.MouseEvent mouseEvent = new org.eclipse.swt.events.MouseEvent(event);
		
		shell.getDisplay().asyncExec(new Runnable() {
			public void run() {
				for (org.eclipse.swt.events.MouseListener listener : mouseListeners) {
					switch (event.type) {
					case SWT.MouseDown:
						listener.mouseDown(mouseEvent);
						break;
					case SWT.MouseUp:
						listener.mouseUp(mouseEvent);
						break;
					case SWT.MouseDoubleClick:
						listener.mouseDoubleClick(mouseEvent);
						break;
					}
				}
			}
		});
		
	}

	protected void convertMouseMoveEvent(int type, MouseEvent e) {
		if (shell.isDisposed())
			return;
		
		//System.out.println("Converting " + e);
		e.consume();
		
		final Event event = new Event(); 
		event.type = type;
		if (e.getButton() != MouseEvent.NOBUTTON) {
			event.button = e.getButton() - MouseEvent.BUTTON1 + 1;
		} else {
			int mask = e.getModifiersEx();
			if ((mask & MouseEvent.BUTTON1_DOWN_MASK) != 0)
				event.button = 1;
			else if ((mask & MouseEvent.BUTTON2_DOWN_MASK) != 0)
				event.button = 2;
			else if ((mask & MouseEvent.BUTTON3_DOWN_MASK) != 0)
				event.button = 3;
		}
		event.x = e.getX();
		event.y = e.getY();
		event.item = shell;
		event.widget = shell;
		
		final org.eclipse.swt.events.MouseEvent mouseEvent = new org.eclipse.swt.events.MouseEvent(event);
		
		shell.getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (shell.isDisposed())
					return;
				for (org.eclipse.swt.events.MouseMoveListener listener : mouseMoveListeners) {
					switch (event.type) {
					case SWT.MouseMove:
						listener.mouseMove(mouseEvent);
						break;
					}
				}
			}
		});
		
	}
	/**
	 * Apply the current mode's X or Y resolutions to the aspect ratio.
	 */
	protected boolean updateWidgetSizeForMode() {
		boolean changed = false;
		
		int visibleWidth = getCanvas().getVisibleWidth();
		int visibleHeight = getCanvas().getVisibleHeight();
		if (visibleWidth != fixedAspectLayout.getWidth()) {
			changed = true;
		}
		if (visibleHeight != fixedAspectLayout.getHeight()) {
			changed = true;
		}
		if (changed) {
			fixedAspectLayout.setSize(visibleWidth, visibleHeight);
			if (visibleWidth > 256)
				visibleWidth /= 2;
			if (getCanvas().isInterlacedEvenOdd())
				visibleHeight /= 2;
			fixedAspectLayout.setAspect((double) visibleWidth / visibleHeight);
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					awtContainer.getParent().layout(true);
					
				}
			});
		}
		return changed;
		
	}

	
	@Override
	protected void resizeTopLevel() {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				
				if (shell != null && !shell.isDisposed()) {
					//System.out.println("Packing");
					
					awtContainer.pack();
					shell.layout(true);				
				}
			}
			
		});
		
	}

	public void setFocus() {
		getAwtCanvas().requestFocus();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISwtVideoRenderer#isVisible()
	 */
	@Override
	public boolean isVisible() {
		if (Display.getDefault().isDisposed())
			return false;
		final boolean[] visible = { false };
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				visible[0] = !shell.isDisposed() && shell.isVisible() && !shell.getMinimized();
			}
		});
		return visible[0];
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISwtVideoRenderer#setIndicatorCanvas(v9t9.emulator.clients.builtin.swt.IndicatorCanvas)
	 */
	@Override
	public void setIndicatorCanvas(IndicatorCanvas indicatorCanvas) {
		this.indicatorCanvas = indicatorCanvas;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.swt.ISwtVideoRenderer#reblit()
	 */
	@Override
	public void reblit() {
		getControl().getDisplay().syncExec(new Runnable() {
			public void run() {
				getControl().redraw();
			}
		});
	}
}
