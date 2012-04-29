package org.ejs.gui.properties;

import org.eclipse.swt.widgets.Control;

public interface IPropertyEditorControl {
	Control getControl();
	void reset();
}
