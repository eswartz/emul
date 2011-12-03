/**
 * 
 */
package v9t9.gui.properties;

import org.eclipse.swt.widgets.Composite;


/**
 * @author ejs
 *
 */
public interface IPropertyEditor {
	IPropertyEditorControl createEditor(Composite parent);
}
