package org.ejs.coffee.core.properties;

import org.eclipse.swt.widgets.Control;

public interface IPropertyEditorControl {
	Control getControl();
	void reset();
}
