/**
 * 
 */
package org.ejs.coffee.core.model;

import org.ejs.coffee.core.jface.IPropertyEditor;


/**
 * @author ejs
 *
 */
public interface IPropertyEditorProvider {
	String getLabel(IProperty property);
	IPropertyEditor createEditor(IProperty property);
}
