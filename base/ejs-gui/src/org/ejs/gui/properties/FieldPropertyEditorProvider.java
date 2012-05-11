/**
 * 
 */
package org.ejs.gui.properties;

import ejs.base.properties.FieldProperty;
import ejs.base.properties.IProperty;


/**
 * @author ejs
 *
 */
public class FieldPropertyEditorProvider implements IPropertyEditorProvider {

	/* (non-Javadoc)
	 * 
	 */
	public IPropertyEditor createEditor(IProperty property) {
		return new FieldPropertyEditor((FieldProperty) property);
	}

	/* (non-Javadoc)
	 * 
	 */
	public String getLabel(IProperty property) {
		return property.getLabel();
	}

}
