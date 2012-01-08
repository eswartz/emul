/**
 * 
 */
package v9t9.gui.client.swt.shells;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public interface IToolShellFactory {
	public enum Centering {
		INSIDE,
		OUTSIDE,
		CENTER
	}
	public static class Behavior {
		public Centering centering;
		public boolean dismissOnClickOutside;
		public String boundsPref;
		public Rectangle defaultBounds;
		public Control centerOverControl;
	}
	Behavior getBehavior();
	Control createContents(Shell shell);
}