/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import java.awt.Component;
import java.awt.Frame;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Point;
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
			protected Point computeSize(Composite composite, int hint,
					int hint2, boolean flushCache) {
				Component awtCanvas = getAwtCanvas();
				return new Point(awtCanvas.getWidth(), awtCanvas.getHeight());
			}

			@Override
			protected void layout(Composite composite, boolean flushCache) {
				Point mySize = composite.getSize();
				Component awtCanvas = getAwtCanvas();
				awtCanvas.setSize(mySize.x, mySize.y);
				
				// 
				//Dimension awtSize = awtCanvas.getSize();
				//frame.setLocation((mySize.x - awtSize.width) / 2, (mySize.y - awtSize.height) / 2);
				//composite.setSize(awtCanvas.getWidth(), awtCanvas.getHeight());
			}
			
		});
		awtContainer.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).create());
		/*
		canvas.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				((Control) e.getSource()).getShell().pack();
			}
		});
		*/
		return awtContainer;
	}
	
	@Override
	protected void resizeTopLevel() {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				if (shell != null && !shell.isDisposed()) {
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
