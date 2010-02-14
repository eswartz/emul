/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import v9t9.emulator.clients.builtin.video.VideoRenderer;

/**
 * Interface implemented by SWT-compatible video renderers.
 * @author ejs
 *
 */
public interface ISwtVideoRenderer extends VideoRenderer {
	Control createControl(Composite parent, int flags);
	
	Control getControl();

	void addMouseEventListener(MouseListener listener);
	void addMouseMotionListener(MouseMoveListener listener);
}
