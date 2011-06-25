/**
 * 
 */
package org.ejs.coffee.core.properties;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * @author ejs
 *
 */
public interface ICellEditorProvider {
	/**
	 * @param composite
	 * @return
	 */
	CellEditor createCellEditor(Composite composite);
}
