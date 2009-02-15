/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ContainerAdapter;
import java.io.File;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
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

	public SwtAwtVideoRenderer(Display display) {
		super();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.clients.builtin.video.ISwtVideoRenderer#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {
		shell = parent.getShell();
		
		awtContainer = new Canvas(parent, SWT.EMBEDDED | SWT.NO_MERGE_PAINTS | SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE);
		frame = SWT_AWT.new_Frame(awtContainer);
		frame.add(getAwtCanvas());
		frame.createBufferStrategy(1);
		frame.setFocusTraversalKeysEnabled(false);
		frame.setIgnoreRepaint(true);
		
		frame.setFocusable(true);

		// no layout -- let canvas size drive it
		//frame.setLayout(new FlowLayout());
		awtContainer.setLayout(new Layout() {

			@Override
			protected Point computeSize(Composite composite, int whint,
					int hhint, boolean flushCache) {
				Component awtCanvas = getAwtCanvas();
				return new Point(awtCanvas.getWidth(), awtCanvas.getHeight());
				//Rectangle area = composite.computeTrim(0, 0, awtCanvas.getWidth(), awtCanvas.getHeight());
				//System.out.println("Area is " + area + " for " +awtCanvas.getWidth() + " x " + awtCanvas.getHeight());
				//return new Point(area.width, area.height);
				
			}

			@Override
			protected void layout(Composite composite, boolean flushCache) {
				//Rectangle myBounds = composite.getClientArea();
				Point mySize = composite.getSize();
				//Point mySize = new Point(myBounds.width, myBounds.height);
				Component awtCanvas = getAwtCanvas();
				awtCanvas.setSize(mySize.x, mySize.y);
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
	
	@Override
	protected void resizeTopLevel() {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				if (shell != null && !shell.isDisposed()) {
					System.out.println("Packing");
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
