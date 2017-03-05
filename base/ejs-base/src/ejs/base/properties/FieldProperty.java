/*
  FieldProperty.java

  (c) 2010-2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.properties;

import java.lang.reflect.Field;

/**
 * This kind of property reads/writes values directly upon
 * a Field in an Object.
 * @author ejs
 *
 */
public class FieldProperty extends AbstractClassBasedProperty {
	
	protected Field field;
	private String label;

	public FieldProperty(String name, Object obj, Field field) {
		super(field.getType(), name, obj); 
		this.field = field;
		field.setAccessible(true);
	}
	

	public FieldProperty(String name, Object obj, String fieldName) {
		super(Object.class /*placeholder*/, name, obj); 
		this.field = FieldUtils.fetchField(obj, fieldName);
		setType(field.getType());
		field.setAccessible(true);
	}
	
	public FieldProperty(Object obj, String fieldName) {
		this(fieldName, obj, fieldName);
	}	
	

	public FieldProperty(Object obj, String fieldName, String label) {
		this(fieldName, obj, fieldName);
		this.label = label;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result
				+ ((field== null) ? 0 : field.hashCode());
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
		if (field == null) {
			if (other.field != null) {
				return false;
			}
		} else if (!field.equals(other.field)) {
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

	@Override
	public String getLabel() {
		if (label != null)
			return label;
		return super.getLabel();
	}
	protected FieldProperty getProperty() { return this; }

	protected Object doGetValue() throws Exception {
		return field.get(obj);
	}

	/* (non-Javadoc)
	 * @see ejs.base.properties.BaseClassProperty#doSetValue(java.lang.Object)
	 */
	@Override
	protected void doSetValue(Object value) throws Exception {
		field.set(obj, value);
	}
	
	public Field getField() {
		return field;
	}
	public Object getObject() {
		return obj;
	}
}
