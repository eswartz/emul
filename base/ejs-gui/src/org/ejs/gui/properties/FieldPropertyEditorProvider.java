/*
  FieldPropertyEditorProvider.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
