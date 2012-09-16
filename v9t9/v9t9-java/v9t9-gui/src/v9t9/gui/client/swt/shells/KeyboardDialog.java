/**
 * 
 */
package v9t9.gui.client.swt.shells;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.keyboard.IKeyboardMapping;
import v9t9.common.machine.IMachine;
import v9t9.gui.client.swt.bars.ImageBar;

/**
 * Shows a keyboard which allows input and shows converted keystrokes
 * @author ejs
 *
 */
public class KeyboardDialog extends Composite {

	public static final String KEYBOARD_TOOL_ID = "keyboard";

	public static IToolShellFactory getToolShellFactory(final IMachine machine, final ImageBar buttonBar) {
		return new IToolShellFactory() {
			Behavior behavior = new Behavior();
			{
				behavior.boundsPref = "KeyboardWindowBounds";
				behavior.centering = null;
				behavior.centerOverControl = buttonBar;
				behavior.dismissOnClickOutside = false;
			}
			public Control createContents(Shell shell) {
				return new KeyboardDialog(shell, machine);
			}
			public Behavior getBehavior() {
				return behavior;
			}
		};
	}

	
	private final IMachine machine;
	private IKeyboardMapping keyboardMapping;
	
	public KeyboardDialog(Shell shell, IMachine machine) {
		
		super(shell, SWT.NONE);
		
		this.machine = machine;
		
		shell.setText("Keyboard");

		keyboardMapping = machine.getKeyboardMapping();
		
		
		GridLayoutFactory.fillDefaults().applyTo(this);
		
	}

}
