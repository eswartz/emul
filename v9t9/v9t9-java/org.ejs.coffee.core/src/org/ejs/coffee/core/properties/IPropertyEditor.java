/**
 * 
 */
package org.ejs.coffee.core.properties;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author ejs
 *
 */
public interface IPropertyEditor {
	Control createEditor(Composite parent);
}
