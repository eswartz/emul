/**
 * 
 */
package org.ejs.coffee.core.properties;

import org.eclipse.swt.widgets.Composite;

/**
 * @author ejs
 *
 */
public interface IPropertyEditor {
	IPropertyEditorControl createEditor(Composite parent);
}
