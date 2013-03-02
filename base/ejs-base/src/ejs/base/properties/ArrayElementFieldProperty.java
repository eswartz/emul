/*
  ArrayElementFieldProperty.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.properties;

/**
 * @author ejs
 *
 */
public class ArrayElementFieldProperty extends FieldProperty implements
		IProperty {

	private final int index;

	public ArrayElementFieldProperty(
			Object obj, String arrayFieldName,
			int index, String name) {
		super(null, obj, arrayFieldName, name);
		this.index = index; 
	}

	/* (non-Javadoc)
	 * 
	 */
	@Override
	public void setValueFromString(String txt) {
		FieldUtils.setArrayValueFromString(field, index, obj, txt);
		firePropertyChange();
	}
	
	/* (non-Javadoc)
	 * 
	 */
	@Override
	public Object getValue() {
		return FieldUtils.getArrayValue(field, index, obj);
	}
	
	/* (non-Javadoc)
	 * 
	 */
	@Override
	public void setValue(Object value) {
		FieldUtils.setArrayValue(field, index, obj, value);
		firePropertyChange();
	}

	/* (non-Javadoc)
	 * 
	 */
	@Override
	public String getName() {
		return fieldName + "_" + name;
	}
	
	/**
	 * @return
	 */
	public int getIndex() {
		return index;
	}

	/* (non-Javadoc)
	 * 
	 */
	/*
	@Override
	public IPropertyEditor createEditor() {
		return new ArrayFieldPropertyEditor(this);
	}
	*/
}
