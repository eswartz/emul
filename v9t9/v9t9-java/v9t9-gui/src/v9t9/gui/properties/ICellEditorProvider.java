/**
 * 
 */
package v9t9.gui.properties;

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
