/**
 * 
 */
package org.ejs.coffee.core.jface;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author ejs
 *
 */
public interface IPropertyEditor {
	Control createEditor(Composite parent);
}
