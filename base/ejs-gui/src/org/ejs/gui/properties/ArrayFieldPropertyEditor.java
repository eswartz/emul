/*
  ArrayFieldPropertyEditor.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
