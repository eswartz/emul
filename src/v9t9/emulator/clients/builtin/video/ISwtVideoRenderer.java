/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Interface implemented by SWT-compatible video renderers.
 * @author ejs
 *
 */
public interface ISwtVideoRenderer extends VideoRenderer {
	Control createControl(Composite parent);
}
