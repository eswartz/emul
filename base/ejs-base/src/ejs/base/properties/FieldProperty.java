/*
  FieldProperty.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.properties;

import java.lang.reflect.Field;

/**
 * @author ejs
 *
 */
public class FieldProperty extends AbstractProperty {

	
	protected final String fieldName;
	protected final Object obj;
	protected Field field;

	public FieldProperty(IClassPropertyFactory factory, Object obj, String fieldName, String name) {
		super(factory, obj.getClass(), name); 
				//editorProvider != null ? editorProvider : new FieldPropertyEditorProvider());
		this.obj = obj;
		this.fieldName = fieldName;
		this.field = FieldUtils.fetchField(obj, fieldName);
		setType(field.getType());
	}
	

	public FieldProperty(Object obj, String fieldName, String name) {
		this(null, obj, fieldName, name); 
	}
	
	public FieldProperty(Object obj, String fieldName) {
		this(null, obj, fieldName, fieldName);
	}	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result
				+ ((fieldName == null) ? 0 : fieldName.hashCode());
		result = prime * result + ((obj == null) ? 0 : obj.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FieldProperty other = (FieldProperty) obj;
		if (field == null) {
			if (other.field != null) {
				return false;
			}
		} else if (!field.equals(other.field)) {
			return false;
		}
		if (fieldName == null) {
			if (other.fieldName != null) {
				return false;
			}
		} else if (!fieldName.equals(other.fieldName)) {
			return false;
		}
		if (this.obj == null) {
			if (other.obj != null) {
				return false;
			}
		} else if (!this.obj.equals(other.obj)) {
			return false;
		}
		return true;
	}



	/* (non-Javadoc)
	 * 
	 */
	/*
	public CellEditor createCellEditor(Composite composite) {
		return new TextCellEditor(composite);
	}
	*/
	
	protected FieldProperty getProperty() { return this; }
	
	/* (non-Javadoc)
	 * 
	 */
	@Override
	public String getName() {
		return fieldName;
	}
	
	/* (non-Javadoc)
	 * 
	 */
	public Object getValue() {
		return FieldUtils.getValue(field, obj);
	}


	/* (non-Javadoc)
	 * 
	 */
	public void setValue(Object value) {
		FieldUtils.setValue(field, obj, value);
		firePropertyChange();
	}
	
	/**
	 * @param txt
	 */
	public void setValueFromString(String txt) {
		FieldUtils.setValueFromString(field, obj, txt);		
		firePropertyChange();
	}



	/**
	 * @return
	 */
	public Field getField() {
		return field;
	}
	/**
	 * @return
	 */
	public Object getObject() {
		return obj;
	}


}
