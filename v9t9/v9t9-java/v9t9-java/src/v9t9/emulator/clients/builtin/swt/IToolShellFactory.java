/**
 * 
 */
package v9t9.emulator.clients.builtin.swt;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public interface IToolShellFactory {
	Control createContents(Shell shell);
}