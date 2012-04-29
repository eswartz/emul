/**
 * 
 */
package org.ejs.gui.properties;

import ejs.base.properties.IProperty;



/**
 * @author ejs
 *
 */
public interface IPropertyEditorProvider {
	String getLabel(IProperty property);
	IPropertyEditor createEditor(IProperty property);
}
