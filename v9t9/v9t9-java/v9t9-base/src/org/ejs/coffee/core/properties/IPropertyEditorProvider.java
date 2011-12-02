/**
 * 
 */
package org.ejs.coffee.core.properties;



/**
 * @author ejs
 *
 */
public interface IPropertyEditorProvider {
	String getLabel(IProperty property);
	IPropertyEditor createEditor(IProperty property);
}
