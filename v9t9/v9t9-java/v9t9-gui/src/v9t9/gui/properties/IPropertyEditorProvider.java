/**
 * 
 */
package v9t9.gui.properties;

import v9t9.base.properties.IProperty;



/**
 * @author ejs
 *
 */
public interface IPropertyEditorProvider {
	String getLabel(IProperty property);
	IPropertyEditor createEditor(IProperty property);
}
