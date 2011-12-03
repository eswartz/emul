/**
 * 
 */
package v9t9.gui.client.swt;

import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import v9t9.engine.client.IVideoRenderer;
import v9t9.engine.hardware.VdpChip;
import v9t9.engine.video.VdpCanvas;

/**
 * Interface implemented by SWT-compatible video renderers.
 * @author ejs
 *
 */
public interface ISwtVideoRenderer extends IVideoRenderer {
	Control createControl(Composite parent, int flags);
	
	Control getControl();

	void addMouseEventListener(MouseListener listener);
	void addMouseMotionListener(MouseMoveListener listener);

	boolean isVisible();
	
    /** Get the basic canvas, before rendering */
	VdpCanvas getCanvas();
	
	void setCanvas(VdpCanvas vdpCanvas);

	void setFocus();

	VdpChip getVdpHandler();

	/**
	 * Reblit the screen (for indicator changes)
	 */
	void reblit();
}
