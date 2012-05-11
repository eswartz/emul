/**
 * 
 */
package org.ejs.gui.properties;

import ejs.base.properties.FieldProperty;
import ejs.base.properties.PropertyUtils;


/**
 * @author ejs
 *
 */
public class ArrayFieldPropertyEditor extends FieldPropertyEditor {

	public ArrayFieldPropertyEditor(FieldProperty property) {
		super(property);
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * 
	 */
	@Override
	protected void setValueFromString(String txt) {
		Class<?> arrayType = property.getField().getType();
		Object value = PropertyUtils.convertStringToValue(txt,
				arrayType.getComponentType());
		setValue(value);
	}

}
