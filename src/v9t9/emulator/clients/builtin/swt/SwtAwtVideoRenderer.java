/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

import v9t9.emulator.clients.builtin.awt.AwtVideoRenderer;

/**
 * @author ejs
 *
 */
public class SwtAwtVideoRenderer extends AwtVideoRenderer implements ISwtVideoRenderer {

	private Frame frame;
	private Shell shell;
	private Canvas awtContainer;

	private List<org.eclipse.swt.events.MouseListener> mouseListeners = new ArrayList<org.eclipse.swt.events.MouseListener>();
	
	public SwtAwtVideoRenderer(Display display) {
		super();
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
				//convertMouseEvent(SWT.MouseDown, e);
			}

			public void mouseReleased(MouseEvent e) {
				convertMouseEvent(SWT.MouseUp, e);
				
			}
			public void mouseEntered(MouseEvent e) {
				//convertMouseEvent(SWT.MouseEnter, e);
			}

			public void mouseExited(MouseEvent e) {
				//convertMouseEvent(SWT.MouseExit, e);
			}

			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) 
					convertMouseEvent(SWT.MouseDoubleClick, e);
				else {
					convertMouseEvent(SWT.MouseDown, e);
					//convertMouseEvent(SWT.MouseUp, e);
				}
			}
			
			
		};
		getAwtCanvas().addMouseListener(l);
		
		
		// no layout -- let canvas size drive it
		//frame.setLayout(new FlowLayout());
		awtContainer.setLayout(new Layout() {

			@Override
			protected Point computeSize(Composite composite, int whint,
					int hhint, boolean flushCache) {
				Component awtCanvas = getAwtCanvas();
				
				Point canvasSize = new Point(awtCanvas.getWidth(), awtCanvas.getHeight());
				int width = canvasSize.x;
				int height = canvasSize.y;
				Rectangle trim = composite.computeTrim(0, 0, width, height);
				
				// hmm, the window manager seems to do weird things now
				
				return new Point(width - (trim.width - width), height - (trim.height - height));
				
/*
				Rectangle parentBounds = composite.getParent().getBounds();
				
				int width = getCanvas().getWidth();
				int height = getCanvas().getHeight();
				int zoom = 1;
				while (width * (zoom + 1) <= parentBounds.width && height * (zoom + 1) <= parentBounds.height) {
					zoom++;
				}
				width *= zoom;
				height *= zoom;
				
				if (width == 0 || height == 0)
					return canvasSize;
				
				return new Point(width, height);
				*/
				//Rectangle area = composite.computeTrim(0, 0, awtCanvas.getWidth(), awtCanvas.getHeight());
				//System.out.println("Area is " + area + " for " +awtCanvas.getWidth() + " x " + awtCanvas.getHeight());
				//return new Point(area.width, area.height);
				
			}

			@Override
			protected void layout(Composite composite, boolean flushCache) {
				//Rectangle myBounds = composite.getClientArea();
				//Point mySize = composite.getSize();
				//Point mySize = new Point(myBounds.width, myBounds.height);
				//Component awtCanvas = getAwtCanvas();
				//awtCanvas.setSize(mySize.x, mySize.y);
			}
			
		});
		
		awtContainer.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Point size = ((Control)e.widget).getSize();
				updateWidgetOnResize(size.x, size.y);
			}
		});
		
		return awtContainer;
	}
	
	public void addMouseEventListener(
			org.eclipse.swt.events.MouseListener listener) {
		mouseListeners.add(listener);		
	}
	protected void convertMouseEvent(int type, MouseEvent e) {
		//System.out.println("Converting " + e);
		e.consume();
		
		final Event event = new Event(); 
		event.type = type;
		event.button = e.getButton() - MouseEvent.BUTTON1 + 1;
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

	@Override
	protected void resizeTopLevel() {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				if (shell != null && !shell.isDisposed()) {
					//System.out.println("Packing");
					awtContainer.pack();
					shell.pack();				
				}
			}
			
		});
		
	}

	public void setFocus() {
		getAwtCanvas().requestFocus();
	}

}
